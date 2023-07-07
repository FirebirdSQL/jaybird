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

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Abstract listener for native resource unload.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public abstract class NativeResourceUnloadAbstractWebListener<T extends java.util.EventObject> {

    protected NativeResourceUnloadAbstractWebListener(){
    }

    public void contextInitialized(T servletContextEvent) {
        if (!isNativeResourceShutdownDisabled() && jaybirdLoadedInContext(servletContextEvent)) {
            NativeResourceTracker.disableShutdownHook();
        }
    }

    public void contextDestroyed(T servletContextEvent) {
        if (!isNativeResourceShutdownDisabled() && jaybirdLoadedInContext(servletContextEvent)) {
            NativeResourceTracker.shutdownNativeResources();
        }
    }

    protected abstract ClassLoader getContextClassLoader(T servletContextEvent);

    private boolean jaybirdLoadedInContext(T servletContextEvent) {
        ClassLoader servletContextClassLoader = getContextClassLoader(servletContextEvent);
        ClassLoader fbClientDatabaseFactoryClassLoader = FbClientDatabaseFactory.class.getClassLoader();

        // TODO Maybe to naive, search parents of fbClientDatabaseFactoryClassLoader as well?
        return servletContextClassLoader == fbClientDatabaseFactoryClassLoader;
    }
}
