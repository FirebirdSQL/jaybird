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
import java.net.Socket;

/**
 * Socket factory for testing the {@code socketFactory} connection property without any constructor arguments.
 *
 * @author Mark Rotteveel
 */
public final class NoArgSocketFactory extends BaseSocketFactory {

    private static final ThreadLocal<Boolean> createSocketCalledOnThread = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public NoArgSocketFactory() {
        createSocketCalledOnThread.remove();
    }

    @Override
    public Socket createSocket() throws IOException {
        createSocketCalledOnThread.set(true);
        return SocketFactory.getDefault().createSocket();
    }

    public static boolean getCreateSocketCalledOnThread() {
        return createSocketCalledOnThread.get();
    }

    public static void clearCurrentThread() {
        createSocketCalledOnThread.remove();
    }

}
