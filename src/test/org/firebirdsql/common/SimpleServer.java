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
package org.firebirdsql.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple server accepting a single connection at a time for testing purposes.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class SimpleServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private Socket socket;

    /**
     * Constructs the ServerSocket with an ephemeral port, this port can be retrieved using {@link #getPort()}.
     *
     * @throws IOException If an I/O error occurs when opening the socket.
     */
    public SimpleServer() throws IOException {
        serverSocket = new ServerSocket(0, 1);
    }

    /**
     * @return The port number of the server socket
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * @return {@code true} when connected, otherwise {@code false}
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * @return The input stream of the open connection
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (!isConnected()) throw new IllegalStateException("Not connected");
        return socket.getInputStream();
    }

    /**
     * @return The output stream of the open connection
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        if (!isConnected()) throw new IllegalStateException("Not connected");
        return socket.getOutputStream();
    }

    /**
     * Waits for and accepts a connection.
     * <p>
     * Important: when testing the client connect and this accept should run on separate threads.
     * </p>
     *
     * @throws IOException
     */
    public void acceptConnection() throws IOException {
        if (isConnected()) throw new IllegalStateException("Already connected");
        socket = serverSocket.accept();
    }

    /**
     * Closes the open connection only. The server socket remains open.
     *
     * @throws IOException
     */
    public void closeConnection() throws IOException {
        if (!isConnected()) return;
        socket.close();
    }

    /**
     * Closes the open connection and the server socket.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        try (ServerSocket refServerSocket = serverSocket){
            closeConnection();
        }
    }
}
