// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
