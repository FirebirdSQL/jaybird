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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Listener dispatcher for {@link ExceptionListener}.
 * <p>
 * This implementation uses {@code WeakReference} (or more specifically {@link WeakHashMap}. Therefor listeners
 * without a strong reference may be removed an no longer notified at any time.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class ExceptionListenerDispatcher implements Iterable<ExceptionListener>, ExceptionListener {

    private static final Logger log = LoggerFactory.getLogger(ExceptionListenerDispatcher.class);

    private static final Object PRESENT = new Object();
    private final Map<ExceptionListener, Object> listeners =
            Collections.synchronizedMap(new WeakHashMap<ExceptionListener, Object>());
    private final Object source;
    private volatile boolean shutdown = false;

    public ExceptionListenerDispatcher(Object source) {
        this.source = source;
    }

    public void errorOccurred(Object source, SQLException exception) {
        errorOccurred(exception);
    }

    public void errorOccurred(SQLException exception) {
        for (ExceptionListener listener : this) {
            try {
                listener.errorOccurred(source, exception);
            } catch (Exception e) {
                log.error("Error on notify errorOccurred to listener " + listener, e);
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
        if (listener == this) {
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
     * @return <code>true</code> when this dispatcher has been shut down.
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
