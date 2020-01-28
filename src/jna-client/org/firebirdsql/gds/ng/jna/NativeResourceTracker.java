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

import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.logging.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class responsible for tracking loaded native resources for cleanup/disposal on exit.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public final class NativeResourceTracker {

    private static final List<Reference<NativeResource>> registeredNativeResources =
            Collections.synchronizedList(new ArrayList<Reference<NativeResource>>());
    private static final boolean NATIVE_RESOURCE_SHUTDOWN_DISABLED =
            JaybirdSystemProperties.isNativeResourceShutdownDisabled();

    // Contains either
    // - null (no shutdown thread registered),
    // - a Thread (shutdown thread registered),
    // - or an Object (shutdown thread disabled, eg by NativeLibraryUnloadWebListener)
    private static final AtomicReference<Object> shutdownThread = new AtomicReference<>();

    private NativeResourceTracker() {
        // no instances
    }

    /**
     * Registers a native resource for automatic shutdown.
     *
     * @param resource
     *         FbClientResource instance
     * @return Value of {@code resource}
     */
    static <T extends NativeResource> T registerNativeResource(T resource) {
        registerShutdownThreadIfNecessary();
        synchronized (registeredNativeResources) {
            cleanupExpiredReferences();
            registeredNativeResources.add((Reference<NativeResource>) new WeakReference<>(resource));
        }
        return resource;
    }

    private static void cleanupExpiredReferences() {
        synchronized (registeredNativeResources) {
            Iterator<Reference<NativeResource>> iterator = registeredNativeResources.iterator();
            while (iterator.hasNext()) {
                Reference<NativeResource> resourceReference = iterator.next();
                if (resourceReference.get() == null) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Disposes native resources (currently: native libraries).
     * <p>
     * Calling this method with active native/embedded connections may break those connections and lead to errors.
     * </p>
     */
    public static void shutdownNativeResources() {
        synchronized (registeredNativeResources) {
            for (Reference<NativeResource> resourceReference : registeredNativeResources) {
                NativeResource resource = resourceReference.get();
                try {
                    if (resource != null) resource.dispose();
                } catch (Throwable e) {
                    LoggerFactory.getLogger(NativeResourceTracker.class)
                            .error("Error disposing resource " + resource, e);
                }
            }
            registeredNativeResources.clear();
        }
    }

    static void disableShutdownHook() {
        Object currentShutdownThread = shutdownThread.getAndSet(new Object());
        if (currentShutdownThread instanceof Thread) {
            try {
                Runtime.getRuntime().removeShutdownHook((Thread) currentShutdownThread);
            } catch (IllegalStateException e) {
                // ignore
            } catch (SecurityException e) {
                LoggerFactory.getLogger(NativeResourceTracker.class)
                        .warn("Could not remove NativeLibraryTracker shutdown hook, this may possibly lead to a memory "
                                + "leak until JVM exit", e);
            }
        }
    }

    static boolean isNativeResourceShutdownDisabled() {
        return NATIVE_RESOURCE_SHUTDOWN_DISABLED;
    }

    private static void registerShutdownThreadIfNecessary() {
        if (shutdownThread.get() != null) {
            return;
        }
        registerShutdownThread();
    }

    private static void registerShutdownThread() {
        if (isNativeResourceShutdownDisabled()) {
            shutdownThread.compareAndSet(null, new Object());
            return;
        }
        Thread newShutdownThread = new Thread(new NativeLibraryShutdownRunnable());
        if (shutdownThread.compareAndSet(null, newShutdownThread)) {
            try {
                Runtime.getRuntime().addShutdownHook(newShutdownThread);
            } catch (IllegalStateException e) {
                // ignore
            } catch (SecurityException e) {
                LoggerFactory.getLogger(NativeResourceTracker.class)
                        .warn("Could not register NativeLibraryTracker shutdown hook, this may result in errors or "
                                + "crashes on exit when Firebird Embedded is used and connections have not been "
                                + "properly closed", e);
            }
        }
    }

    private static class NativeLibraryShutdownRunnable implements Runnable {
        @Override
        public void run() {
            shutdownNativeResources();
        }
    }

    /**
     * A native resource that can be registered with {@link NativeResourceTracker}.
     */
    static abstract class NativeResource {
        /**
         * Dispose method to clean up the native resource.
         */
        abstract void dispose();

        /**
         * Finalizer that calls {@link #dispose()}.
         */
        @Override
        protected void finalize() throws Throwable {
            try {
                dispose();
            } finally {
                super.finalize();
            }
        }
    }
}
