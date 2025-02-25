// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.jakarta;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.firebirdsql.gds.ng.jna.NativeResourceUnloadAbstractWebListener;

/**
 * Servlet context listener for {@code jakarta.servlet} for unloading native libraries if loaded in the current context.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@WebListener
@SuppressWarnings("java:S1185")
public class NativeResourceUnloadWebListenerJakarta
        extends NativeResourceUnloadAbstractWebListener<ServletContextEvent> implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
    }

    @Override
    protected final ClassLoader getContextClassLoader(ServletContextEvent servletContextEvent) {
        return servletContextEvent.getServletContext().getClassLoader();
    }

}
