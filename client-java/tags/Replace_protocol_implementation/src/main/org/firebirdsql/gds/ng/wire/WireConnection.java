/*
 * $Id$
 * 
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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Class managing the TCP/IP connection and initial handshaking with the
 * Firebird server.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class WireConnection {

    // TODO Include character set
    // TODO Check if methods currently throwing IOException should throw SQLException instead
    // TODO Change how information is passed?

    private static final Logger log = LoggerFactory.getLogger(WireConnection.class, false);

    private Socket socket;
    private ProtocolCollection protocols;
    private IConnectionProperties connectionProperties;
    private int protocolVersion;
    private int protocolArchitecture;
    private int protocolMinimumType;
    private EncodingDefinition encodingDefinition;
    private IEncodingFactory encodingFactory;

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
     * @param connectionProperties
     *         Connection properties
     */
    public WireConnection(IConnectionProperties connectionProperties) throws SQLException {
        this(connectionProperties, EncodingFactory.getDefaultInstance(), ProtocolCollection.getDefaultCollection());
    }

    /**
     * Creates a WireConnection (without establishing a connection to the
     * server).
     *
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     * @param protocols
     *         The collection of protocols to use for this connection.
     */
    public WireConnection(IConnectionProperties connectionProperties, IEncodingFactory encodingFactory, ProtocolCollection protocols) throws SQLException {
        this.connectionProperties = new FbConnectionProperties(connectionProperties);
        this.protocols = protocols;
        encodingDefinition = encodingFactory.getEncodingDefinition(connectionProperties.getEncoding(), connectionProperties.getCharSet());
        if (encodingDefinition == null || encodingDefinition.isInformationOnly()) {
            // TODO Don't throw exception if encoding/charSet is null (see also TODO inside EncodingFactory.getEncodingDefinition)
            throw new SQLNonTransientConnectionException(String.format("No valid encoding definition for Firebird encoding %s and/or Java charset %s",
                    connectionProperties.getEncoding(), connectionProperties.getCharSet()), FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
        this.encodingFactory = encodingFactory.withDefaultEncodingDefinition(encodingDefinition);
    }

    public boolean isConnected() {
        return !(socket == null || socket.isClosed());
    }

    public String getServerName() {
        return connectionProperties.getServerName();
    }

    public int getPortNumber() {
        return connectionProperties.getPortNumber();
    }

    public String getDatabaseName() {
        return connectionProperties.getDatabaseName();
    }

    public short getConnectionDialect() {
        return connectionProperties.getConnectionDialect();
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public int getProtocolArchitecture() {
        return protocolArchitecture;
    }

    public int getProtocolMinimumType() {
        return protocolMinimumType;
    }

    /**
     * @return An immutable copy of the current connection properties.
     */
    public IConnectionProperties getConnectionProperties() {
        return connectionProperties.asImmutable();
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
    public void setSoTimeout(int socketTimeout) throws SQLException {
        connectionProperties.setSoTimeout(socketTimeout);
        resetSocketTimeout();
    }

    /**
     * Resets the socket timeout to the configured socketTimeout. Does nothing
     * if currently not connected.
     *
     * @throws SQLException
     *         If the timeout value cannot be changed
     */
    public void resetSocketTimeout() throws SQLException {
        if (isConnected()) {
            try {
                final int soTimeout = connectionProperties.getSoTimeout();
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
    public void socketConnect() throws SQLException {
        try {
            socket = new Socket();
            socket.setTcpNoDelay(true);
            final int connectTimeout = connectionProperties.getConnectTimeout();
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
                socket.setSoTimeout(Math.max(connectionProperties.getSoTimeout(), 0));
            }

            final int socketBufferSize = connectionProperties.getSocketBufferSize();
            if (socketBufferSize != -1) {
                socket.setReceiveBufferSize(socketBufferSize);
                socket.setSendBufferSize(socketBufferSize);
            }

            socket.connect(new InetSocketAddress(getServerName(), getPortNumber()), socketConnectTimeout);
        } catch (SocketTimeoutException ste) {
            throw new FbExceptionBuilder().timeoutException(ISCConstants.isc_network_error).messageParameter(getServerName()).cause(ste).toSQLException();
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_network_error).messageParameter(getServerName()).cause(ioex).toSQLException();
        }
    }

    public XdrStreamAccess getXdrStreamAccess() {
        return streamAccess;
    }

    public EncodingDefinition getEncodingDefinition() {
        return this.encodingDefinition;
    }

    public Encoding getEncoding() {
        return this.encodingDefinition.getEncoding();
    }

    public IEncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    /**
     * Performs the connection identification phase of the Wire protocol and
     * returns the FbWireDatabase implementation for the agreed protocol.
     *
     * @return FbWireDatabase
     * @throws SQLTimeoutException
     * @throws SQLException
     */
    public FbWireDatabase identify() throws SQLException {
        try {
            xdrIn = new XdrInputStream(socket.getInputStream());
            xdrOut = new XdrOutputStream(socket.getOutputStream());

            // Here we identify the user to the engine. 
            // This may or may not be used as login info to a database.
            final byte[] userBytes = getSystemUserName().getBytes();
            final byte[] hostBytes = getSystemHostName().getBytes();

            ByteArrayOutputStream userId = new ByteArrayOutputStream();
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

            xdrOut.writeInt(op_connect);
            xdrOut.writeInt(op_attach);
            xdrOut.writeInt(CONNECT_VERSION2);
            xdrOut.writeInt(arch_generic);

            xdrOut.writeString(getDatabaseName(), getEncoding());
            xdrOut.writeInt(protocols.getProtocolCount()); // Count of protocols understood
            xdrOut.writeBuffer(userId.toByteArray());

            for (ProtocolDescriptor protocol : protocols) {
                xdrOut.writeInt(protocol.getVersion()); // Protocol version
                xdrOut.writeInt(protocol.getArchitecture()); // Architecture of client
                xdrOut.writeInt(protocol.getMinimumType()); // Minimum type
                xdrOut.writeInt(protocol.getMaximumType()); // Maximum type
                xdrOut.writeInt(protocol.getWeight()); // Preference weight
            }

            xdrOut.flush();

            if (readNextOperation() == op_accept) {
                protocolVersion = xdrIn.readInt(); // Protocol version
                protocolArchitecture = xdrIn.readInt(); // Architecture for protocol
                protocolMinimumType = xdrIn.readInt(); // Minimum type
                if (protocolVersion < 0) {
                    protocolVersion = (protocolVersion & FB_PROTOCOL_MASK) | FB_PROTOCOL_FLAG;
                }

                ProtocolDescriptor descriptor = protocols.getProtocolDescriptor(protocolVersion);
                if (descriptor == null) {
                    throw new SQLException(
                            String.format(
                                    "Unsupported or unexpected protocol version %d connecting to database %s. Supported version(s): %s",
                                    protocolVersion, getServerName(), protocols.getProtocolVersions()));
                }
                return descriptor.createDatabase(this);
            } else {
                try {
                    disconnect();
                } catch (Exception ex) {
                    log.debug("Ignoring exception on disconnect in connect phase of protocol", ex);
                }
                throw new FbExceptionBuilder().exception(ISCConstants.isc_connect_reject).toSQLException();
            }
        } catch (SocketTimeoutException ste) {
            throw new FbExceptionBuilder().timeoutException(ISCConstants.isc_network_error).messageParameter(getServerName()).cause(ste).toSQLException();
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_network_error).messageParameter(getServerName()).cause(ioex).toSQLException();
        }
    }

    /**
     * Reads the next operation code. Skips all {@link org.firebirdsql.gds.impl.wire.WireProtocolConstants#op_dummy} codes received.
     *
     * @return Operation code
     * @throws IOException
     *         if an error occurs while reading from the underlying InputStream
     */
    public int readNextOperation() throws IOException {
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
    public void disconnect() throws IOException {
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
            connectionProperties = null;
            encodingDefinition = null;
            encodingFactory = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            disconnect();
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
}
