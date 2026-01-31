// SPDX-FileCopyrightText: Copyright 2016-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.listeners;

import java.sql.SQLException;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Listener dispatcher for {@link ExceptionListener}.
 * <p>
 * This implementation uses {@code WeakReference} (or more specifically {@link WeakHashMap}). Therefor listeners
 * without a strong reference may be removed and no longer get notified at any time.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ExceptionListenerDispatcher implements Iterable<ExceptionListener>, ExceptionListener {

    private static final System.Logger log = System.getLogger(ExceptionListenerDispatcher.class.getName());

    private static final Object PRESENT = new Object();
    private final Map<ExceptionListener, Object> listeners = Collections.synchronizedMap(new WeakHashMap<>());
    private final Object source;
    private volatile boolean shutdown = false;

    public ExceptionListenerDispatcher(Object source) {
        this.source = requireNonNull(source, "source");
    }

    @Override
    public void errorOccurred(Object source, SQLException exception) {
        errorOccurred(exception);
    }

    public void errorOccurred(SQLException exception) {
        for (ExceptionListener listener : this) {
            try {
                listener.errorOccurred(source, exception);
            } catch (Exception e) {
                log.log(System.Logger.Level.ERROR, "Error on notify errorOccurred to listener " + listener, e);
            }
        }
    }

    /**
     * Adds the supplied listener to this dispatcher.
     * <p>
     * A call to this method has no effect after {@link #shutdown()} has been called.
     * </p>
     *
     * @param listener
     *         Listener object
     */
    public void addListener(ExceptionListener listener) {
        if (requireNonNull(listener, "listener") == this) {
            throw new IllegalArgumentException("Adding this instance to itself is not allowed");
        }
        synchronized (listeners) {
            if (isShutdown()) return;
            listeners.put(listener, PRESENT);
        }
    }

    /**
     * Removes the supplied listener from this dispatcher.
     *
     * @param listener
     *         Listener object
     */
    public void removeListener(ExceptionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners from this dispatcher.
     */
    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Shuts down this dispatcher and removes all listeners.
     * <p>
     * After shutdown calls to {@link #addListener(ExceptionListener)} are ignored.
     * </p>
     */
    public void shutdown() {
        shutdown = true;
        removeAllListeners();
    }

    /**
     * @return {@code true} when this dispatcher has been shut down.
     */
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public Iterator<ExceptionListener> iterator() {
        synchronized (listeners) {
            return new ArrayList<>(listeners.keySet()).iterator();
        }
    }
}
