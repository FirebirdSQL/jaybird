// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple server accepting a single connection at a time for testing purposes.
 *
 * @author Mark Rotteveel
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
     */
    public InputStream getInputStream() throws IOException {
        if (!isConnected()) throw new IllegalStateException("Not connected");
        return socket.getInputStream();
    }

    /**
     * @return The output stream of the open connection
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
     */
    public void acceptConnection() throws IOException {
        if (isConnected()) throw new IllegalStateException("Already connected");
        socket = serverSocket.accept();
        socket.setTcpNoDelay(true);
        // This should be unnecessary, but it seems to reduce the occurrence of test failures when my machine is
        // running with the "Power saver" power plan
        Thread.yield();
    }

    /**
     * Closes the open connection only. The server socket remains open.
     */
    public void closeConnection() throws IOException {
        if (!isConnected()) return;
        socket.close();
    }

    /**
     * Closes the open connection and the server socket.
     */
    @Override
    public void close() throws IOException {
        try (serverSocket){
            closeConnection();
        }
    }
}
