// SPDX-FileCopyrightText: Copyright 2019-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.NativeLibrary;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger.Level;
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

    @SuppressWarnings("java:S3077")
    private volatile @Nullable FbClientLibrary library;
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
                    .log(Level.ERROR, "Error disposing of " + local, e);
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
                log.log(Level.TRACE, "Calling fb_shutdown on {0}", library);
                library.fb_shutdown(0, 1);
            } finally {
                FbClientFeatureAccessHandler handler =
                        (FbClientFeatureAccessHandler) Proxy.getInvocationHandler(library);
                NativeLibrary nativeLibrary = handler.getNativeLibrary();
                log.log(Level.TRACE, "Disposing JNA native library {0}", nativeLibrary);
                try {
                    // Retaining use of dispose for backwards compatibility with older JNA versions for now
                    nativeLibrary.dispose();
                } catch (Throwable e) {
                    log.log(Level.ERROR, "Error disposing of " + nativeLibrary, e);
                }
            }
        }
    }

}
