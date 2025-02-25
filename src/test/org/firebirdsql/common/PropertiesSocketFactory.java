// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * Socket factory for testing the {@code socketFactory} connection property with {@link Properties} constructor
 * argument.
 *
 * @author Mark Rotteveel
 */
public final class PropertiesSocketFactory extends BaseSocketFactory {

    private static final ThreadLocal<Properties> lastPropertiesOnThread = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> createSocketCalledOnThread = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public PropertiesSocketFactory(Properties props) {
        createSocketCalledOnThread.remove();
        lastPropertiesOnThread.set(props);
    }

    @Override
    public Socket createSocket() throws IOException {
        createSocketCalledOnThread.set(true);
        return SocketFactory.getDefault().createSocket();
    }

    public static Properties getLastPropertiesOnThread() {
        return lastPropertiesOnThread.get();
    }

    public static Boolean getCreateSocketCalledOnThread() {
        return createSocketCalledOnThread.get();
    }

    public static void clearCurrentThread() {
        lastPropertiesOnThread.remove();
        createSocketCalledOnThread.remove();
    }

}
