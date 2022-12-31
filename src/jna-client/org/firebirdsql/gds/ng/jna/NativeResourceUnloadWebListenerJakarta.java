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
package org.firebirdsql.gds.ng.jna;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Servlet context listener for {@code jakarta.servlet} for unloading native libraries if loaded in the current context.
 * <p>
 * Twin of {@link NativeResourceUnloadWebListenerJavaX}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@WebListener
public class NativeResourceUnloadWebListenerJakarta implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (!isNativeResourceShutdownDisabled() && jaybirdLoadedInContext(servletContextEvent)) {
            NativeResourceTracker.disableShutdownHook();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (!isNativeResourceShutdownDisabled() && jaybirdLoadedInContext(servletContextEvent)) {
            NativeResourceTracker.shutdownNativeResources();
        }
    }

    private boolean jaybirdLoadedInContext(ServletContextEvent servletContextEvent) {
        ClassLoader servletContextClassLoader = servletContextEvent.getServletContext().getClassLoader();
        ClassLoader fbClientDatabaseFactoryClassLoader = FbClientDatabaseFactory.class.getClassLoader();

        // TODO Maybe to naive, search parents of fbClientDatabaseFactoryClassLoader as well?
        return servletContextClassLoader == fbClientDatabaseFactoryClassLoader;
    }
}
