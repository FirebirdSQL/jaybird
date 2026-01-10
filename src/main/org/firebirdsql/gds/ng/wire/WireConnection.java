// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.NonNull;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Class managing the TCP/IP connection and initial handshaking with the Firebird server.
 *
 * @param <T>
 *         Type of attach properties
 * @param <C>
 *         Type of connection handle
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class WireConnection<T extends IAttachProperties<T>, C extends FbWireAttachment>
        extends AbstractConnection<T, C> implements Closeable {

    // TODO Check if methods currently throwing IOException should throw SQLException instead

    private static final System.Logger log = System.getLogger(WireConnection.class.getName());
    private static final String REJECTION_POSSIBLE_REASON =
            "The server and client could not agree on connection options. A possible reason is attempting to connect "
            + "to an unsupported Firebird version; see the documentation of connection property 'enableProtocol' for "
            + "a workaround.";
    private static final WarningMessageCallback NOOP_WARNING_MESSAGE_CALLBACK = warning -> {};

    // Micro-optimization: we usually expect at most 3 (Firebird 5)
    private final List<KnownServerKey> knownServerKeys = new ArrayList<>(3);
    private final DbAttachInfo dbAttachInfo;
    private ClientAuthBlock clientAuthBlock;
    private Socket socket;
    private ProtocolCollection protocols;
    private int protocolVersion;
    private int protocolArchitecture;
    private int protocolType;

    private XdrOutputStream xdrOut;
    private XdrInputStream xdrIn;
    private final XdrStreamAccess streamAccess = new XdrStreamAccess() {

        private final ReentrantLock transmitLock = new ReentrantLock();

        @Override
        public @NonNull XdrInputStream getXdrIn() throws SQLException {
            if (isConnected() && xdrIn != null) {
                return xdrIn;
            } else {
                throw FbExceptionBuilder.connectionClosed();
            }
        }

        @Override
        public @NonNull XdrOutputStream getXdrOut() throws SQLException {
            if (isConnected() && xdrOut != null) {
                return xdrOut;
            } else {
                throw FbExceptionBuilder.connectionClosed();
            }
        }

        @Override
        public void withTransmitLock(@NonNull TransmitAction transmitAction) throws IOException, SQLException {
            transmitLock.lock();
            try {
                transmitAction.transmit(getXdrOut());
            } finally {
                transmitLock.unlock();
            }
        }

    };

    /**
     * Creates a not-connected WireConnection with the default protocol collection.
     *
     * @param attachProperties
     *         attach properties
     */
    protected WireConnection(T attachProperties) throws SQLException {
        this(attachProperties, EncodingFactory.getPlatformDefault(),
                ProtocolCollection.getProtocols(attachProperties.getEnableProtocol()));
    }

    /**
     * Creates a not-connected WireConnection.
     *
     * @param attachProperties
     *         attach properties
     * @param encodingFactory
     *         factory for encoding definitions
     * @param protocols
     *         collection of protocols to use for this connection
     */
    protected WireConnection(T attachProperties, IEncodingFactory encodingFactory,
            ProtocolCollection protocols) throws SQLException {
        super(attachProperties, encodingFactory);
        this.protocols = protocols;
        clientAuthBlock = new ClientAuthBlock(this.attachProperties);
        dbAttachInfo = toDbAttachInfo(attachProperties);
    }

    // Allow access to withLock() at package level, without making it public in parent class
    final LockCloseable withLockProxy() {
        return withLock();
    }

    private void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException {
        getXdrStreamAccess().withTransmitLock(transmitAction);
    }

    public final String getServerName() {
        return dbAttachInfo.serverName();
    }

    public final int getPortNumber() {
        return dbAttachInfo.portNumber();
    }

    /**
     * @return The file name to use in the p_cnct_file of the op_connect request
     */
    protected String getCnctFile() {
        return getAttachObjectName();
    }

    public final String getAttachObjectName() {
        return dbAttachInfo.attachObjectName();
    }

    protected abstract DbAttachInfo toDbAttachInfo(T attachProperties) throws SQLException;

    public final boolean isConnected() {
        return !(socket == null || socket.isClosed());
    }

    public final int getProtocolVersion() {
        return protocolVersion;
    }

    public final int getProtocolArchitecture() {
        return protocolArchitecture;
    }

    /**
     * @deprecated Use {@link #getProtocolType()}, will be removed in Jaybird 8 (or possibly before Jaybird 7 release)
     */
    @Deprecated(forRemoval = true, since = "7")
    public final int getProtocolMinimumType() {
        return protocolType;
    }

    public final int getProtocolType() {
        return protocolType;
    }

    public final ClientAuthBlock getClientAuthBlock() {
        return clientAuthBlock;
    }

    /**
     * Sets the socket blocking timeout (SO_TIMEOUT) of the socket.
     * <p>
     * This method can also be called if a connection is established
     * </p>
     *
     * @param socketTimeout
     *         Value of the socket timeout (in milliseconds)
     * @throws SQLException
     *         If the timeout value cannot be changed
     */
    public final void setSoTimeout(int socketTimeout) throws SQLException {
        attachProperties.setSoTimeout(socketTimeout);
        resetSocketTimeout();
    }

    /**
     * Resets the socket timeout to the configured socketTimeout. Does nothing
     * if currently not connected.
     *
     * @throws SQLException
     *         If the timeout value cannot be changed
     */
    public final void resetSocketTimeout() throws SQLException {
        if (isConnected()) {
            try {
                final int soTimeout = attachProperties.getSoTimeout();
                final int desiredTimeout = soTimeout != -1 ? soTimeout : 0;
                if (socket.getSoTimeout() != desiredTimeout) {
                    socket.setSoTimeout(desiredTimeout);
                }
            } catch (SocketException e) {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_couldNotChangeSoTimeout).cause(e)
                        .toSQLException();
            }
        }
    }

    /**
     * Establishes the TCP/IP connection to serverName and portNumber of this connection.
     *
     * @throws SQLTimeoutException
     *         if the connection cannot be established within the connect timeout (either explicitly set or implied by
     *         the OS timeout of the socket)
     * @throws SQLException
     *         if the connection cannot be established.
     */
    public final void socketConnect() throws SQLException {
        try {
            socket = createSocket();
            socket.setTcpNoDelay(true);
            final int connectTimeout = attachProperties.getConnectTimeout();
            // connectTimeout is in seconds, need milliseconds, lower bound 0 (indefinite, for overflow or not set)
            final int socketConnectTimeout = Math.max(0, (int) TimeUnit.SECONDS.toMillis(connectTimeout));
            if (socketConnectTimeout != 0) {
                // Blocking timeout initially identical to connect timeout
                socket.setSoTimeout(socketConnectTimeout);
            } else {
                // Blocking timeout to normal socket timeout, 0 if not set
                socket.setSoTimeout(Math.max(attachProperties.getSoTimeout(), 0));
            }

            final int socketBufferSize = attachProperties.getSocketBufferSize();
            if (socketBufferSize != IAttachProperties.DEFAULT_SOCKET_BUFFER_SIZE) {
                socket.setReceiveBufferSize(socketBufferSize);
                socket.setSendBufferSize(socketBufferSize);
            }

            socket.connect(new InetSocketAddress(getServerName(), getPortNumber()), socketConnectTimeout);
        } catch (SocketTimeoutException ste) {
            throw FbExceptionBuilder.forTimeoutException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName())
                    .cause(ste)
                    .toSQLException();
        } catch (IOException ioex) {
            throw FbExceptionBuilder.forNonTransientConnectionException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName())
                    .cause(ioex)
                    .toSQLException();
        }
    }

    private Socket createSocket() throws IOException, SQLException {
        try {
            return createSocketFactory().createSocket();
        } catch (RuntimeException e) {
            throw FbExceptionBuilder
                    .forNonTransientConnectionException(JaybirdErrorCodes.jb_socketFactoryFailedToCreateSocket)
                    .messageParameter(attachProperties.getSocketFactory())
                    .cause(e)
                    .toSQLException();
        }
    }

    private SocketFactory createSocketFactory() throws SQLException {
        String socketFactoryName = attachProperties.getSocketFactory();
        if (socketFactoryName == null) {
            return SocketFactory.getDefault();
        }
        return createSocketFactory0(socketFactoryName);
    }

    private SocketFactory createSocketFactory0(String socketFactoryName) throws SQLException {
        log.log(DEBUG, "Attempting to create custom socket factory {0}", socketFactoryName);
        try {
            Class<? extends SocketFactory> socketFactoryClass =
                    Class.forName(socketFactoryName).asSubclass(SocketFactory.class);
            try {
                Constructor<? extends SocketFactory> propsConstructor =
                        socketFactoryClass.getConstructor(Properties.class);
                return propsConstructor.newInstance(getSocketFactoryProperties());
            } catch (ReflectiveOperationException e) {
                log.log(DEBUG, socketFactoryName
                        + " has no Properties constructor, or constructor execution resulted in an exception", e);
            }
            try {
                Constructor<? extends SocketFactory> noArgConstructor = socketFactoryClass.getConstructor();
                return noArgConstructor.newInstance();
            } catch (ReflectiveOperationException e) {
                log.log(DEBUG, socketFactoryName
                        + " has no no-arg constructor, or constructor execution resulted in an exception", e);
            }
            throw FbExceptionBuilder
                    .forNonTransientConnectionException(JaybirdErrorCodes.jb_socketFactoryConstructorNotFound)
                    .messageParameter(socketFactoryName)
                    .toSQLException();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_socketFactoryClassNotFound)
                    .messageParameter(socketFactoryName)
                    .cause(e)
                    .toSQLException();
        }
    }

    private Properties getSocketFactoryProperties() {
        var props = new Properties();
        attachProperties.connectionPropertyValues().entrySet().stream()
                .filter(e ->
                        e.getValue() != null && e.getKey().name().endsWith("@socketFactory"))
                .forEach(e -> {
                    ConnectionProperty connectionProperty = e.getKey();
                    props.setProperty(connectionProperty.name(), connectionProperty.type().asString(e.getValue()));
                });
        return props;
    }

    public final XdrStreamAccess getXdrStreamAccess() {
        return streamAccess;
    }

    /**
     * Performs the connection identification phase of the Wire protocol and
     * returns the FbWireDatabase implementation for the agreed protocol.
     *
     * @return FbWireDatabase
     */
    @Override
    public final C identify() throws SQLException {
        try {
            xdrIn = new XdrInputStream(socket.getInputStream());
            xdrOut = new XdrOutputStream(socket.getOutputStream());

            withTransmitLock(xdrOut -> {
                sendConnectAttachMsg(xdrOut);
                xdrOut.flush();
            });
            int operation = handleCryptKeyCallbackBeforeAttachResponse();

            if (operation == op_accept || operation == op_cond_accept || operation == op_accept_data) {
                return handleConnectAttachAccept(xdrIn, operation);
            } else {
                throw handleConnectAttachReject(operation);
            }
        } catch (SocketTimeoutException ste) {
            throw FbExceptionBuilder.forTimeoutException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName()).cause(ste).toSQLException();
        } catch (IOException ioex) {
            throw FbExceptionBuilder.forException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName()).cause(ioex).toSQLException();
        }
    }

    /**
     * Sends the connect attach packet.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     */
    private void sendConnectAttachMsg(XdrOutputStream xdrOut) throws IOException, SQLException {
        xdrOut.writeInt(op_connect); // p_operation
        xdrOut.writeInt(op_attach); // p_cnct_operation
        xdrOut.writeInt(CONNECT_VERSION3); // p_cnct_cversion
        xdrOut.writeInt(arch_generic); // p_cnct_client

        xdrOut.writeString(getCnctFile(), getEncoding()); // p_cnct_file
        xdrOut.writeInt(protocols.getProtocolCount()); // p_cnct_count - Count of protocols understood
        xdrOut.writeBuffer(createUserIdentificationBlock()); // p_cnct_user_id

        for (ProtocolDescriptor protocol : protocols) {
            writeProtocolDescriptor(xdrOut, protocol);
        }
    }

    /**
     * Writes a protocol descriptor for the connect attach packet.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     */
    private void writeProtocolDescriptor(XdrOutputStream xdrOut, ProtocolDescriptor protocol) throws IOException {
        xdrOut.writeInt(protocol.getVersion()); // p_cnct_version - Protocol version
        xdrOut.writeInt(protocol.getArchitecture()); // p_cnct_architecture - Architecture of client
        xdrOut.writeInt(protocol.getMinimumType()); // p_cnct_min_type - Minimum type
        if (protocol.supportsWireCompression() && attachProperties.isWireCompression()) {
            xdrOut.writeInt(protocol.getMaximumType() | pflag_compress); // p_cnct_max_type - Maximum type
        } else {
            xdrOut.writeInt(protocol.getMaximumType()); // p_cnct_max_type - Maximum type
        }
        xdrOut.writeInt(protocol.getWeight()); // p_cnct_weight - Preference weight
    }

    private int handleCryptKeyCallbackBeforeAttachResponse() throws IOException, SQLException {
        int operation = readNextOperation();
        FbWireOperations cryptKeyCallbackWireOperations = null;
        DbCryptCallback dbCryptCallback = null;
        while (operation == op_crypt_key_callback) {
            if (cryptKeyCallbackWireOperations == null) {
                cryptKeyCallbackWireOperations = getCryptKeyCallbackWireOperations();
            }
            if (dbCryptCallback == null) {
                dbCryptCallback = createDbCryptCallback();
            }
            cryptKeyCallbackWireOperations.handleCryptKeyCallback(dbCryptCallback);
            operation = readNextOperation();
        }
        return operation;
    }

    private C handleConnectAttachAccept(XdrInputStream xdrIn, int operation) throws IOException, SQLException {
        var acceptPacket = new FbWireAttachment.AcceptPacket();
        acceptPacket.operation = operation;
        protocolVersion = xdrIn.readInt(); // p_acpt_version - Protocol version
        protocolArchitecture = xdrIn.readInt(); // p_acpt_architecture - Architecture for protocol
        int acceptType = xdrIn.readInt(); // p_acpt_type - Accepted type
        protocolType = acceptType & ptype_MASK;
        final boolean compress = (acceptType & pflag_compress) != 0;

        if (protocolVersion < 0) {
            protocolVersion = (protocolVersion & FB_PROTOCOL_MASK) | FB_PROTOCOL_FLAG;
        }

        if (operation == op_cond_accept || operation == op_accept_data) {
            byte[] data = acceptPacket.p_acpt_data = xdrIn.readBuffer(); // p_acpt_data
            acceptPacket.p_acpt_plugin = xdrIn.readString(getEncoding()); // p_acpt_plugin
            final boolean authComplete = xdrIn.readInt() == 1; // p_acpt_authenticated
            byte[] serverKeys = acceptPacket.p_acpt_keys = xdrIn.readBuffer(); // p_acpt_keys

            clientAuthBlock.setServerData(data);
            clientAuthBlock.setAuthComplete(authComplete);
            addServerKeys(serverKeys);
            clientAuthBlock.resetClient(serverKeys);
            clientAuthBlock.switchPlugin(acceptPacket.p_acpt_plugin);
        } else {
            clientAuthBlock.resetClient(null);
        }

        if (compress) {
            xdrOut.enableCompression();
            xdrIn.enableDecompression();
        }

        ProtocolDescriptor descriptor = protocols.getProtocolDescriptor(protocolVersion);
        if (descriptor == null) {
            throw new SQLException(String.format(
                    "Unsupported or unexpected protocol version %d connecting to database %s. Supported version(s): %s",
                    protocolVersion, getServerName(), protocols.getProtocolVersions()));
        }
        C connectionHandle = createConnectionHandle(descriptor);
        if (operation == op_cond_accept) {
            connectionHandle.authReceiveResponse(acceptPacket);
        }
        return connectionHandle;
    }

    private SQLException handleConnectAttachReject(int operation) throws IOException {
        try {
            if (operation == op_response) {
                // Handle exception from response
                AbstractWireOperations wireOperations = getDefaultWireOperations();
                Response response = wireOperations.processOperation(operation);
                if (response instanceof GenericResponse genericResponse && genericResponse.exception() != null) {
                    return genericResponse.exception();
                }
            } else if (operation == op_reject) {
                return FbExceptionBuilder.forException(ISCConstants.isc_connect_reject)
                        .messageParameter(REJECTION_POSSIBLE_REASON).toSQLException();
            }
            log.log(DEBUG, "Reached end of identify without error or connection, last operation: {0}", operation);
            // If we reach here, authentication failed (or never authenticated for lack of username and password)
            return FbExceptionBuilder.toException(ISCConstants.isc_login);
        } catch (SQLException e) {
            return e;
        } finally {
            try {
                close();
            } catch (Exception ex) {
                log.log(DEBUG, "Ignoring exception on disconnect in connect phase of protocol", ex);
            }
        }
    }

    /**
     * Clear authentication data.
     */
    public final void clearAuthData() {
        clientAuthBlock = null;
        clearServerKeys();
    }

    private byte[] createUserIdentificationBlock() throws IOException, SQLException {
        // Here we identify the user to the engine.
        // This may or may not be used as login info to a database.
        final byte[] userBytes = getSystemUserName().getBytes(StandardCharsets.UTF_8);
        final byte[] hostBytes = getSystemHostName().getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream userId = new ByteArrayOutputStream();

        clientAuthBlock.authenticateStep0();
        clientAuthBlock.writePluginDataTo(userId);

        userId.write(CNCT_client_crypt);
        VaxEncoding.encodeVaxInteger(userId, attachProperties.getWireCryptAsEnum().getWireProtocolCryptLevel());

        userId.write(CNCT_user);
        int userLength = Math.min(userBytes.length, 255);
        userId.write(userLength);
        userId.write(userBytes, 0, userLength);

        userId.write(CNCT_host);
        int hostLength = Math.min(hostBytes.length, 255);
        userId.write(hostLength);
        userId.write(hostBytes, 0, hostLength);
        userId.write(CNCT_user_verification);
        userId.write(0);
        return userId.toByteArray();
    }

    void addServerKeys(byte[] serverKeys) throws SQLException {
        final var newKeys = new ClumpletReader(ClumpletReader.Kind.UnTagged, serverKeys);
        for (newKeys.rewind(); !newKeys.isEof(); newKeys.moveNext()) {
            addServerKey(newKeys);
        }
    }

    private void addServerKey(ClumpletReader newKeys) throws SQLException {
        int currentTag = newKeys.getClumpTag();
        switch (currentTag) {
        case TAG_KNOWN_PLUGINS -> {
            // Nothing to do (yet)
        }
        case TAG_PLUGIN_SPECIFIC ->
                // Nothing to do (yet)
                log.log(DEBUG, "Possible implementation problem, found TAG_PLUGIN_SPECIFIC without TAG_KEY_TYPE");
        case TAG_KEY_TYPE -> extractServerKey(newKeys).ifPresent(knownServerKeys::add);
        default -> log.log(DEBUG, "Ignored unexpected tag type: {0}", currentTag);
        }
    }

    private static Optional<KnownServerKey> extractServerKey(ClumpletReader newKeys) throws SQLException {
        String keyType = newKeys.getString(StandardCharsets.ISO_8859_1);

        newKeys.moveNext();
        if (newKeys.isEof()) return Optional.empty();

        int currentTag = newKeys.getClumpTag();
        if (currentTag != TAG_KEY_PLUGINS) {
            throw new SQLException("Unexpected tag type: " + currentTag);
        }
        String keyPlugins = newKeys.getString(StandardCharsets.ISO_8859_1);

        Map<String, byte[]> pluginSpecificData = null;
        while (newKeys.directNext(TAG_PLUGIN_SPECIFIC)) {
            byte[] data = newKeys.getBytes();
            int sepIdx = ByteArrayHelper.indexOf(data, (byte) 0);
            if (sepIdx > 0) {
                String plugin = new String(data, 0, sepIdx, StandardCharsets.ISO_8859_1);
                byte[] specificData = Arrays.copyOfRange(data, sepIdx + 1, data.length);
                if (pluginSpecificData == null) {
                    pluginSpecificData = new HashMap<>();
                }
                pluginSpecificData.put(plugin, specificData);
            }
        }

        return Optional.of(new KnownServerKey(keyType, keyPlugins, pluginSpecificData));
    }

    void clearServerKeys() {
        knownServerKeys.forEach(KnownServerKey::clear);
        knownServerKeys.clear();
    }

    private AbstractWireOperations getDefaultWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION10);
        return (AbstractWireOperations) protocolDescriptor.createWireOperations(this, NOOP_WARNING_MESSAGE_CALLBACK);
    }

    /**
     * @return Instance of FbWireOperations that can read crypt key callbacks (in practice: v15).
     */
    private FbWireOperations getCryptKeyCallbackWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION15);
        return protocolDescriptor.createWireOperations(this, NOOP_WARNING_MESSAGE_CALLBACK);
    }

    /**
     * Creates the connection handle for this type of connection.
     *
     * @param protocolDescriptor
     *         The protocol descriptor selected by the identify phase
     * @return Connection handle
     */
    protected abstract C createConnectionHandle(ProtocolDescriptor protocolDescriptor);

    /**
     * Reads the next operation code. Skips all {@link org.firebirdsql.gds.impl.wire.WireProtocolConstants#op_dummy}
     * codes received.
     *
     * @return Operation code
     * @throws IOException
     *         if an error occurs while reading from the underlying InputStream
     */
    public final int readNextOperation() throws IOException {
        int op;
        do {
            op = xdrIn.readInt();
        } while (op == op_dummy);
        return op;
    }

    /**
     * Closes the TCP/IP connection. This is not a normal detach operation.
     *
     * @throws IOException
     *         if closing fails
     */
    @SuppressWarnings("EmptyTryBlock")
    public final void close() throws IOException {
        try (var ignored1 = socket;
             var ignored2 = xdrIn;
             var ignored3 = xdrOut) {
            // Ignored: Use try-with-resources to close
        } finally {
            xdrOut = null;
            xdrIn = null;
            socket = null;
            protocols = null;
        }
    }

    private static String getSystemUserName() {
        try {
            return getSystemPropertyPrivileged("user.name");
        } catch (SecurityException ex) {
            log.log(DEBUG, "Unable to retrieve user.name property", ex);
            return "jaybird";
        }
    }

    private static String getSystemHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex1) {
                return "127.0.0.1";
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(propertyName));
    }

    final List<KnownServerKey.PluginSpecificData> getPluginSpecificData() {
        if (knownServerKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<KnownServerKey.PluginSpecificData> pluginSpecificData = new ArrayList<>();
        for (KnownServerKey knownServerKey : knownServerKeys) {
            pluginSpecificData.addAll(knownServerKey.getPluginSpecificData());
        }
        return pluginSpecificData;
    }
}
