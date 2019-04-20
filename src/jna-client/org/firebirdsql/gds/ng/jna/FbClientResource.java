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

import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.lang.reflect.Proxy;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Resource for {@link org.firebirdsql.jna.fbclient.FbClientLibrary}.
 * <p>
 * Allows for disposing native libraries by {@link NativeResourceTracker}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
class FbClientResource extends NativeResourceTracker.NativeResource {

    private volatile FbClientLibrary library;
    private final AbstractNativeDatabaseFactory owner;

    FbClientResource(FbClientLibrary library, AbstractNativeDatabaseFactory owner) {
        this.library = library;
        this.owner = owner;
    }

    FbClientLibrary get() {
        final FbClientLibrary local = library;
        if (local == null) {
            throw new IllegalStateException("Library was already disposed");
        }
        return local;
    }

    /*
     IMPLEMENTATION NOTE:
     
     Using both embedded and native in one application might lead to libraries sharing the same native image (once for
     FbEmbeddedDatabaseFactory and once for FbClientDatabaseFactory). This may result in the native library being
     disposed twice (which may log errors, but should not be a real problem), or - potentially - in being disposed by
     one and continue to be in use by the other, which could result in application failures. Looking at the current
     implementation, that should only be a theoretical concern.
    */

    @Override
    void dispose() {
        final FbClientLibrary local = library;
        if (local == null) {
            return;
        }
        try {
            owner.disposing(this, new Runnable() {
                @Override
                public void run() {
                    final Logger logger = LoggerFactory.getLogger(FbClientResource.class);
                    library = null;
                    try {
                        if (logger.isDebugEnabled()) logger.debug("Calling fb_shutdown on " + local);
                        local.fb_shutdown(0, 1);
                    } finally {
                        if (!isNativeResourceShutdownDisabled()) {
                            // only explicitly dispose if native resource shutdown is not disabled
                            Library.Handler handler = (Library.Handler) Proxy.getInvocationHandler(local);
                            NativeLibrary nativeLibrary = handler.getNativeLibrary();
                            if (logger.isDebugEnabled()) logger.debug("Disposing JNA native library " + nativeLibrary);
                            try {
                                nativeLibrary.dispose();
                            } catch (Throwable e) {
                                logger.error("Error disposing of " + nativeLibrary, e);
                            }
                        }
                    }
                }
            });
        } catch (Throwable e) {
            LoggerFactory.getLogger(FbClientResource.class).error("Error disposing of " + local, e);
        }
    }

}
