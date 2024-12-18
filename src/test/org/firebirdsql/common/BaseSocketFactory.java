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
