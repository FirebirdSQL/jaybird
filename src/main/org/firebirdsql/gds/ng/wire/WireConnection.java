/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Class managing the TCP/IP connection and initial handshaking with the Firebird server.
 *
 * @param <T>
 *         Type of attach properties
 * @param <C>
 *         Type of connection handle
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class WireConnection<T extends IAttachProperties<T>, C extends FbWireAttachment>
        extends AbstractConnection<T, C> implements Closeable {

    // TODO Check if methods currently throwing IOException should throw SQLException instead

    private static final Logger log = LoggerFactory.getLogger(WireConnection.class);

    // Micro-optimization: we usually expect at most 1 (Firebird 3), and usually 0 (Firebird 2.5 and earlier)
    private final List<KnownServerKey> knownServerKeys = new ArrayList<>(1);
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
    }

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
                // TODO Add SQLState
                throw new SQLException("Unable to change socket timeout (SO_TIMEOUT)", e);
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
     * @throws SQLTimeoutException
     * @throws SQLException
     */
    @Override
    public final C identify() throws SQLException {
        try {
            xdrIn = new XdrInputStream(socket.getInputStream());
            xdrOut = new XdrOutputStream(socket.getOutputStream());

            xdrOut.writeInt(op_connect);
            xdrOut.writeInt(op_attach);
            xdrOut.writeInt(CONNECT_VERSION3);
            xdrOut.writeInt(arch_generic);

            xdrOut.writeString(getAttachObjectName(), getEncoding());
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
                throw new FbExceptionBuilder().exception(ISCConstants.isc_connect_reject).toFlatSQLException();
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
        VaxEncoding.encodeVaxInteger(userId, attachProperties.getWireCrypt().getWireProtocolCryptLevel());

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
            case TAG_PLUGIN_SPECIFIC:
                // Nothing to do (yet) 
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
                knownServerKeys.add(new KnownServerKey(keyType, keyPlugins));
                break;
            }
            default:
                log.debug("Ignored unexpected tag type: " + currentTag);
                break;
            }
        }
    }

    void clearServerKeys() {
        knownServerKeys.clear();
    }

    private AbstractWireOperations getDefaultWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION10);
        return (AbstractWireOperations) protocolDescriptor
                .createWireOperations(this, null, this);
    }

    /**
     * @return Instance of FbWireOperations that can read crypt key callbacks (in practice: v15).
     */
    private FbWireOperations getCryptKeyCallbackWireOperations() {
        ProtocolDescriptor protocolDescriptor = protocols
                .getProtocolDescriptor(WireProtocolConstants.PROTOCOL_VERSION15);
        return protocolDescriptor.createWireOperations(this, null, this);
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

    @Override
    protected final void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
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

    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(propertyName);
            }
        });
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

    final List<EncryptionIdentifier> getEncryptionIdentifiers() {
        if (knownServerKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<EncryptionIdentifier> encryptionIdentifiers = new ArrayList<>();
        for (KnownServerKey knownServerKey : knownServerKeys) {
            encryptionIdentifiers.addAll(knownServerKey.getIdentifiers());
        }
        return encryptionIdentifiers;
    }
}
