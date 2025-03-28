// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.JaybirdSystemProperties;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class responsible for tracking loaded native resources for cleanup/disposal on exit.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class NativeResourceTracker {

    private static final List<Reference<NativeResource>> registeredNativeResources =
            Collections.synchronizedList(new ArrayList<>());
    private static final List<NativeResource> strongRegisteredNativeResources =
            Collections.synchronizedList(new ArrayList<>());
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
            registeredNativeResources.add(new WeakReference<>(resource));
        }
        return resource;
    }

    /**
     * Registers a native resource for automatic shutdown with a strong reference.
     * <p>
     * Use this method if the resource will not be strongly held by others and must be cleaned up on exit.
     * </p>
     *
     * @param resource
     *         FbClientResource instance
     * @return Value of {@code resource}
     * @see #registerNativeResource(NativeResource)
     */
    static <T extends NativeResource> T strongRegisterNativeResource(T resource) {
        registerShutdownThreadIfNecessary();
        synchronized (strongRegisteredNativeResources) {
            cleanupExpiredReferences();
            strongRegisteredNativeResources.add(resource);
        }
        return resource;
    }

    private static void cleanupExpiredReferences() {
        synchronized (registeredNativeResources) {
            registeredNativeResources.removeIf(resourceReference -> resourceReference.get() == null);
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
                    System.getLogger(NativeResourceTracker.class.getName())
                            .log(System.Logger.Level.ERROR, "Error disposing resource " + resource, e);
                }
            }
            registeredNativeResources.clear();
        }
        synchronized (strongRegisteredNativeResources) {
            for (NativeResource resource : strongRegisteredNativeResources) {
                try {
                    resource.dispose();
                } catch (Throwable e) {
                    System.getLogger(NativeResourceTracker.class.getName())
                            .log(System.Logger.Level.ERROR, "Error disposing resource " + resource, e);
                }
            }
            strongRegisteredNativeResources.clear();
        }
    }

    static void disableShutdownHook() {
        if (shutdownThread.getAndSet(new Object()) instanceof Thread thread) {
            try {
                Runtime.getRuntime().removeShutdownHook(thread);
            } catch (IllegalStateException e) {
                // ignore
            } catch (SecurityException e) {
                System.getLogger(NativeResourceTracker.class.getName()).log(System.Logger.Level.WARNING,
                        "Could not remove NativeLibraryTracker shutdown hook, this may possibly lead to a memory leak "
                        + "until JVM exit", e);
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
                System.getLogger(NativeResourceTracker.class.getName()).log(System.Logger.Level.WARNING,
                        "Could not register NativeLibraryTracker shutdown hook, this may result in errors or crashes "
                        + "on exit when Firebird Embedded is used and connections have not been properly closed", e);
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
    abstract static class NativeResource {
        /**
         * Dispose method to clean up the native resource.
         * <p>
         * If needed, implementations are expected to have registered with {@link java.lang.ref.Cleaner} at construction
         * and invoke the {@link Cleaner.Cleanable#clean()} from this method.
         * </p>
         */
        abstract void dispose();
    }
}
