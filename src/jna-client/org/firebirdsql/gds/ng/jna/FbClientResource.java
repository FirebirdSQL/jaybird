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

import com.sun.jna.NativeLibrary;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.lang.ref.Cleaner;
import java.lang.reflect.Proxy;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.isNativeResourceShutdownDisabled;

/**
 * Resource for {@link org.firebirdsql.jna.fbclient.FbClientLibrary}.
 * <p>
 * Allows for disposing native libraries by {@link NativeResourceTracker}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class FbClientResource extends NativeResourceTracker.NativeResource {

    private volatile FbClientLibrary library;
    private final AbstractNativeDatabaseFactory owner;
    private final Cleaner.Cleanable cleanable;

    FbClientResource(FbClientLibrary library, AbstractNativeDatabaseFactory owner) {
        this.library = library;
        this.owner = owner;
        // only explicitly shutdown and dispose if native resource shutdown is not disabled
        cleanable = isNativeResourceShutdownDisabled()
                ? Cleaners.getNoOp()
                : Cleaners.getJbCleaner().register(this, new DisposeAction(library));
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
            disposeImpl();
        } catch (Throwable e) {
            System.getLogger(FbClientResource.class.getName())
                    .log(System.Logger.Level.ERROR, "Error disposing of " + local, e);
        }
    }

    // separate method to be able to test if this dispose doesn't result in errors
    private void disposeImpl() {
        final FbClientLibrary local = library;
        if (local == null) {
            return;
        }
        try {
            owner.disposing(this, () -> library = null);
        } finally {
            cleanable.clean();
        }
    }

    private record DisposeAction(FbClientLibrary library) implements Runnable {
        @SuppressWarnings("deprecation")
        public void run() {
            System.Logger log = System.getLogger(FbClientResource.class.getName());
            try {
                log.log(System.Logger.Level.TRACE, "Calling fb_shutdown on %s", library);
                library.fb_shutdown(0, 1);
            } finally {
                FbClientFeatureAccessHandler handler =
                        (FbClientFeatureAccessHandler) Proxy.getInvocationHandler(library);
                NativeLibrary nativeLibrary = handler.getNativeLibrary();
                log.log(System.Logger.Level.TRACE, "Disposing JNA native library {0}", nativeLibrary);
                try {
                    // Retaining use of dispose for backwards compatibility with older JNA versions for now
                    nativeLibrary.dispose();
                } catch (Throwable e) {
                    log.log(System.Logger.Level.ERROR, "Error disposing of " + nativeLibrary, e);
                }
            }
        }
    }

}
