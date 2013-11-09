/*
 * $Id$
 * 
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

import java.util.*;

/**
 * Dispatcher to maintain a list of listeners of type <code>TListener</code>
 *
 * @param <TListener> Listener type
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class AbstractListenerDispatcher<TListener> implements Iterable<TListener> {

    private final Set<TListener> listeners = Collections.synchronizedSet(new HashSet<TListener>());
    private boolean shutdown = false;

    /**
     * Adds the supplied listener to this dispatcher.
     * <p>
     * A call to this method has no effect after {@link #shutdown()} has been called.
     * </p>
     *
     * @param listener Listener object
     */
    public final synchronized void addListener(TListener listener) {
        if (isShutdown()) return;
        if (listener == this) {
            throw new IllegalArgumentException("Adding this instance to itself is not allowed");
        }
        listeners.add(listener);
    }

    /**
     * Removes the supplied listener from this dispatcher.
     *
     * @param listener Listener object
     */
    public final void removeListener(TListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners from this dispatcher.
     */
    public final void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Shuts down this dispatcher and removes all listeners.
     * <p>
     * After shutdown calls to {@link #addListener(TListener)} are ignored.
     * </p>
     */
    public synchronized final void shutdown() {
        shutdown = true;
        removeAllListeners();
    }

    /**
     * @return <code>true</code> when this dispatcher has been shut down.
     */
    public synchronized final boolean isShutdown() {
        return shutdown;
    }

    @Override
    public final Iterator<TListener> iterator() {
        synchronized (listeners) {
            return new ArrayList<TListener>(listeners).iterator();
        }
    }
}
