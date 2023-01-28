/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
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
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.ByteArrayHelper;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;

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

    private static final Logger log = LoggerFactory.getLogger(WireConnection.class);

    // Micro-optimization: we usually expect at most 3 (Firebird 5)
    private final List<KnownServerKey> knownServerKeys = new ArrayList<>(3);
    private final DbAttachInfo dbAttachInfo;
    private ClientAuthBlock clientAuthBlock;
    private Socket socket;
    private ProtocolCollection protocols;
    private int protocolVersion;
    private int protocolArchitecture;
    private int protocolMinimumType;

    private XdrOutputStream xdrOut;
    private XdrInputStream xdrIn;
    private final XdrStreamAccess streamAccess = new XdrStreamAccess() {
        @Override
        public XdrInputStream getXdrIn() throws SQLException {
            if (isConnected() && xdrIn != null) {
                return xdrIn;
            } else {
                throw new SQLException("Connection closed or no connection available");
            }
        }

        @Override
        public XdrOutputStream getXdrOut() throws SQLException {
            if (isConnected() && xdrOut != null) {
                return xdrOut;
            } else {
                throw new SQLException("Connection closed or no connection available");
            }
        }
    };

    /**
     * Creates a WireConnection (without establishing a connection to the
     * server) with the default protocol collection.
     *
     * @param attachProperties
     *         Attach properties
     */
    protected WireConnection(T attachProperties) throws SQLException {
        this(attachProperties, EncodingFactory.getPlatformDefault(), ProtocolCollection.getDefaultCollection());
    }

    /**
     * Creates a WireConnection (without establishing a connection to the
     * server).
     *
     * @param attachProperties
     *         Attach properties
     * @param encodingFactory
     *         Factory for encoding definitions
     * @param protocols
     *         The collection of protocols to use for this connection.
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

    public final int getProtocolMinimumType() {
        return protocolMinimumType;
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
     * Establishes the TCP/IP connection to serverName and portNumber of this
     * Connection
     *
     * @throws SQLTimeoutException
     *         If the connection cannot be established within the connect
     *         timeout (either explicitly set or implied by the OS timeout
     *         of the socket)
     * @throws SQLException
     *         If the connection cannot be established.
     */
    public final void socketConnect() throws SQLException {
        try {
            socket = new Socket();
            socket.setTcpNoDelay(true);
            final int connectTimeout = attachProperties.getConnectTimeout();
            final int socketConnectTimeout;
            if (connectTimeout != -1) {
                // connectTimeout is in seconds, need milliseconds
                socketConnectTimeout = (int) TimeUnit.SECONDS.toMillis(connectTimeout);
                // Blocking timeout initially identical to connect timeout
                socket.setSoTimeout(socketConnectTimeout);
            } else {
                // socket connect timeout is not set, so indefinite (0)
                socketConnectTimeout = 0;
                // Blocking timeout to normal socket timeout, 0 if not set
                socket.setSoTimeout(Math.max(attachProperties.getSoTimeout(), 0));
            }

            final int socketBufferSize = attachProperties.getSocketBufferSize();
            if (socketBufferSize != IConnectionProperties.DEFAULT_SOCKET_BUFFER_SIZE) {
                socket.setReceiveBufferSize(socketBufferSize);
                socket.setSendBufferSize(socketBufferSize);
            }

            socket.connect(new InetSocketAddress(getServerName(), getPortNumber()), socketConnectTimeout);
        } catch (SocketTimeoutException ste) {
            throw new FbExceptionBuilder().timeoutException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName())
                    .cause(ste)
                    .toSQLException();
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().nonTransientConnectionException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName())
                    .cause(ioex)
                    .toSQLException();
        }
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

            xdrOut.writeInt(op_connect);
            xdrOut.writeInt(op_attach); // p_cnct_operation
            xdrOut.writeInt(CONNECT_VERSION3); // p_cnct_cversion
            xdrOut.writeInt(arch_generic); // p_cnct_client

            xdrOut.writeString(getCnctFile(), getEncoding()); // p_cnct_file
            xdrOut.writeInt(protocols.getProtocolCount()); // Count of protocols understood
            xdrOut.writeBuffer(createUserIdentificationBlock());

            for (ProtocolDescriptor protocol : protocols) {
                xdrOut.writeInt(protocol.getVersion()); // Protocol version
                xdrOut.writeInt(protocol.getArchitecture()); // Architecture of client
                xdrOut.writeInt(protocol.getMinimumType()); // Minimum type
                if (protocol.supportsWireCompression() && attachProperties.isWireCompression()) {
                    xdrOut.writeInt(protocol.getMaximumType() | pflag_compress);
                } else {
                    xdrOut.writeInt(protocol.getMaximumType()); // Maximum type
                }
                xdrOut.writeInt(protocol.getWeight()); // Preference weight
            }

            xdrOut.flush();
            
            FbWireOperations cryptKeyCallbackWireOperations = null;
            DbCryptCallback dbCryptCallback = null;
            int operation = readNextOperation();
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
            if (operation == op_accept || operation == op_cond_accept || operation == op_accept_data) {
                FbWireAttachment.AcceptPacket acceptPacket = new FbWireAttachment.AcceptPacket();
                acceptPacket.operation = operation;
                protocolVersion = xdrIn.readInt(); // p_acpt_version - Protocol version
                protocolArchitecture = xdrIn.readInt(); // p_acpt_architecture - Architecture for protocol
                int acceptType = xdrIn.readInt(); // p_acpt_type - Minimum type
                protocolMinimumType = acceptType & ptype_MASK;
                final boolean compress = (acceptType & pflag_compress) != 0;

                if (protocolVersion < 0) {
                    protocolVersion = (protocolVersion & FB_PROTOCOL_MASK) | FB_PROTOCOL_FLAG;
                }

                if (operation == op_cond_accept || operation == op_accept_data) {
                    byte[] data = acceptPacket.p_acpt_data = xdrIn.readBuffer();
                    acceptPacket.p_acpt_plugin = xdrIn.readString(getEncoding());
                    final int isAuthenticated = xdrIn.readInt();
                    byte[] serverKeys = acceptPacket.p_acpt_keys = xdrIn.readBuffer();

                    clientAuthBlock.setServerData(data);
                    clientAuthBlock.setAuthComplete(isAuthenticated == 1);
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
            } else {
                try {
                    if (operation == op_response) {
                        // Handle exception from response
                        AbstractWireOperations wireOperations = getDefaultWireOperations();
                        wireOperations.processResponse(wireOperations.processOperation(operation));
                    }
                } finally {
                    try {
                        close();
                    } catch (Exception ex) {
                        log.debug("Ignoring exception on disconnect in connect phase of protocol", ex);
                    }
                }
                log.debugf("Reached end of identify without error or connection, last operation: %d", operation);
                // If we reach here, authentication failed (or never authenticated for lack of username and password)
                throw new FbExceptionBuilder().exception(ISCConstants.isc_login).toSQLException();
            }
        } catch (SocketTimeoutException ste) {
            throw new FbExceptionBuilder().timeoutException(ISCConstants.isc_network_error)
                    .messageParameter(getServerName()).cause(ste).toSQLException();
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_network_error)
                    .messageParameter(getServerName()).cause(ioex).toSQLException();
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
        final ClumpletReader newKeys = new ClumpletReader(ClumpletReader.Kind.UnTagged, serverKeys);
        for (newKeys.rewind(); !newKeys.isEof(); newKeys.moveNext()) {
            int currentTag = newKeys.getClumpTag();
            switch (currentTag) {
            case TAG_KNOWN_PLUGINS:
                // Nothing to do (yet)
                break;
            case TAG_PLUGIN_SPECIFIC:
                // Nothing to do (yet)
                log.debug("Possible implementation problem, found TAG_PLUGIN_SPECIFIC without TAG_KEY_TYPE");
                break;
            case TAG_KEY_TYPE: {
                String keyType = newKeys.getString(StandardCharsets.US_ASCII);

                newKeys.moveNext();
                if (newKeys.isEof()) {
                    break;
                }
                currentTag = newKeys.getClumpTag();
                if (currentTag != TAG_KEY_PLUGINS) {
                    throw new SQLException("Unexpected tag type: " + currentTag);
                }
                String keyPlugins = newKeys.getString(StandardCharsets.US_ASCII);

                Map<String, byte[]> pluginSpecificData = null;
                while (newKeys.directNext(TAG_PLUGIN_SPECIFIC)) {
                    byte[] data = newKeys.getBytes();
                    int sepIdx = ByteArrayHelper.indexOf(data, (byte) 0);
                    if (sepIdx > 0) {
                        String plugin = new String(data, 0, sepIdx, StandardCharsets.US_ASCII);
                        byte[] specificData = Arrays.copyOfRange(data, sepIdx + 1, data.length);
                        if (pluginSpecificData == null) {
                            pluginSpecificData = new HashMap<>();
                        }
                        pluginSpecificData.put(plugin, specificData);
                    }
                }

                knownServerKeys.add(new KnownServerKey(keyType, keyPlugins, pluginSpecificData));
                break;
            }
            default:
                log.debugf("Ignored unexpected tag type: %d", currentTag);
                break;
            }
        }
    }

    void clearServerKeys() {
        knownServerKeys.forEach(KnownServerKey::clear);
        knownServerKeys.clear();
    }

    private AbstractWireOperations getDefaultWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION10);
        return (AbstractWireOperations) protocolDescriptor.createWireOperations(this, null);
    }

    /**
     * @return Instance of FbWireOperations that can read crypt key callbacks (in practice: v15).
     */
    private FbWireOperations getCryptKeyCallbackWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION15);
        return protocolDescriptor.createWireOperations(this, null);
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
    public final void close() throws IOException {
        IOException ioex = null;
        try {
            if (socket != null) {
                try {
                    if (xdrOut != null) xdrOut.close();
                } catch (IOException ex) {
                    ioex = ex;
                }

                try {
                    if (xdrIn != null) xdrIn.close();
                } catch (IOException ex) {
                    if (ioex == null) ioex = ex;
                }

                try {
                    socket.close();
                } catch (IOException ex) {
                    if (ioex == null) ioex = ex;
                }

                if (ioex != null) throw ioex;
            }
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
            log.debug("Unable to retrieve user.name property", ex);
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

    /**
     * Writes directly to the {@code OutputStream} of the underlying socket.
     *
     * @param data
     *         Data to write
     * @throws IOException
     *         If there is no socket, the socket is closed, or for errors writing to the socket.
     */
    public final void writeDirect(byte[] data) throws IOException {
        xdrOut.writeDirect(data);
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
