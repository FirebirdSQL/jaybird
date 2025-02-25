// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Subclass of {@link SocketFactory} throwing {@link UnsupportedOperationException} for all {@code createSocket}
 * methods.
 * <p>
 * Intended as a base for socket factories for testing purposes.
 * </p>
 *
 * @author Mark Rotteveel
 */
public abstract class BaseSocketFactory extends SocketFactory {

    @Override
    public Socket createSocket() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress host, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
        throw new UnsupportedOperationException();
    }

}
