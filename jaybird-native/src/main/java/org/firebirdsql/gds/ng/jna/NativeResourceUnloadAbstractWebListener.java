// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.jspecify.annotations.NullMarked;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Abstract listener for native resource unload.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@NullMarked
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
