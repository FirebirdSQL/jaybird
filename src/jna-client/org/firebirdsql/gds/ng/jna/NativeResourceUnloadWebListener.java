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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.jna.fbclient.FbClientLibrary;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Servlet context listener responsible for unloading native libraries if loaded in the current context.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@WebListener
public class NativeResourceUnloadWebListener implements ServletContextListener {

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
        ClassLoader fbClientLibraryClassLoader = FbClientLibrary.class.getClassLoader();

        // TODO Maybe to naive, search parents of fbClientLibraryClassLoader as well?
        return servletContextClassLoader == fbClientLibraryClassLoader;
    }
}
