/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbConnectTimeoutException;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbException;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Class managing the TCP/IP connection and initial handshaking with the
 * Firebird server.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class WireConnection implements XdrStreamAccess {

    // TODO Include character set
    // TODO Check if methods currently throwing IOException should throw FbException instead
    // TODO Change how information is passed?

    private static final Logger log = LoggerFactory.getLogger(WireConnection.class, false);

    private Socket socket;
    private final ProtocolCollection protocols;
    private final IConnectionProperties connectionProperties;
    private int protocolVersion;
    private int protocolArchitecture;
    private int protocolMinimumType;

    private XdrOutputStream xdrOut;
    private XdrInputStream xdrIn;

    /**
     * Creates a WireConnection (without establishing a connection to the
     * server) with the default protocol collection.
     * 
     * @param connectionProperties
     *            Connection properties
     */
    public WireConnection(IConnectionProperties connectionProperties) {
        this(connectionProperties, ProtocolCollection.getDefaultCollection());
    }

    /**
     * Creates a WireConnection (without establishing a connection to the
     * server).
     * 
     * @param connectionProperties
     *            Connection properties
     * @param protocols
     *            The collection of protocols to use for this connection.
     */
    public WireConnection(IConnectionProperties connectionProperties, ProtocolCollection protocols) {
        this.connectionProperties = new FbConnectionProperties(connectionProperties);
        this.protocols = protocols;
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
     * Sets the socket blocking timeout (SO_TIMEOUT) of the socket.
     * <p>
     * This method can also be called if a connection is established
     * </p>
     * 
     * @param socketTimeout
     *            Value of the socket timeout (in milliseconds)
     * @throws FbException
     *             If the timeout value cannot be changed
     */
    public void setSoTimeout(int socketTimeout) throws FbException {
        connectionProperties.setSoTimeout(socketTimeout);
        resetSocketTimeout();
    }

    /**
     * Resets the socket timeout to the configured socketTimeout. Does nothing
     * if currently not connected.
     * 
     * @throws FbException
     *             If the timeout value cannot be changed
     */
    public void resetSocketTimeout() throws FbException {
        if (isConnected()) {
            try {
                final int soTimeout = connectionProperties.getSoTimeout();
                final int desiredTimeout = soTimeout != -1 ? soTimeout : 0;
                if (socket.getSoTimeout() != desiredTimeout) {
                    socket.setSoTimeout(desiredTimeout);
                }
            } catch (SocketException e) {
                throw new FbException("Unable to change socket timeout (SO_TIMEOUT)", e);
            }
        }
    }

    /**
     * Establishes the TCP/IP connection to serverName and portNumber of this
     * Connection
     * 
     * @throws FbConnectTimeoutException
     *             If the connection cannot be established within the connect
     *             timeout (either explicitly set or implied by the OS timeout
     *             of the socket)
     * @throws FbException
     *             If the connection cannot be established.
     */
    public void socketConnect() throws FbConnectTimeoutException, FbException {
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
                // socket connect timeout is net set, so indefinite (0)
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
            throw new FbConnectTimeoutException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error,
                    getServerName(), ste);
        } catch (IOException ioex) {
            throw new FbException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error, getServerName(), ioex);
        }
    }

    public XdrInputStream getXdrIn() throws FbException {
        if (isConnected()) {
            return xdrIn;
        } else {
            throw new FbException("Connection closed or no connection available");
        }
    }

    public XdrOutputStream getXdrOut() throws FbException {
        if (isConnected()) {
            return xdrOut;
        } else {
            throw new FbException("Connection closed or no connection available");
        }
    }

    /**
     * Performs the connection identification phase of the Wire protocol and
     * returns the FbWireDatabase implementation for the agreed protocol.
     * 
     * @param databaseName
     *            Name of the database
     * @return FbWireDatabase
     * @throws FbException
     */
    public FbWireDatabase identify() throws FbException {
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

            xdrOut.writeString(getDatabaseName());
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

            if (xdrIn.readNextOperation() == op_accept) {
                protocolVersion = xdrIn.readInt(); // Protocol version
                protocolArchitecture = xdrIn.readInt(); // Architecture for protocol
                protocolMinimumType = xdrIn.readInt(); // Minimum type
                if (protocolVersion < 0) {
                    protocolVersion = (protocolVersion & FB_PROTOCOL_MASK) | FB_PROTOCOL_FLAG;
                }

                ProtocolDescriptor descriptor = protocols.getProtocolDescriptor(protocolVersion);
                if (descriptor == null) {
                    throw new FbException(
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
                throw new FbException(ISCConstants.isc_connect_reject);
            }
        } catch (SocketTimeoutException ste) {
            throw new FbConnectTimeoutException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error,
                    getServerName(), ste);
        } catch (IOException ioex) {
            throw new FbException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error, getServerName(), ioex);
        }
    }

    /**
     * Closes the TCP/IP connection. This is not a normal detach operation.
     * 
     * @throws IOException
     *             if closing fails
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
