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
