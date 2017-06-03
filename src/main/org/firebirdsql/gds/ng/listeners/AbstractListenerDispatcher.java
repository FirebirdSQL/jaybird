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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Dispatcher to maintain a list of listeners of type <code>TListener</code>
 *
 * @param <TListener> Listener type
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class AbstractListenerDispatcher<TListener> implements Iterable<TListener> {

    private final Set<TListener> listeners = new CopyOnWriteArraySet<>();
    private final List<WeakReference<TListener>> weakListeners = new CopyOnWriteArrayList<>();
    private volatile boolean shutdown = false;

    /**
     * Adds the supplied listener to this dispatcher as a strongly referenced listener.
     * <p>
     * A call to this method has no effect after {@link #shutdown()} has been called.
     * </p>
     *
     * @param listener Listener object
     */
    public final void addListener(TListener listener) {
        if (listener == this) {
            throw new IllegalArgumentException("Adding this instance to itself is not allowed");
        }
        if (isShutdown()) return;
        listeners.add(listener);
    }

    /**
     * Adds the supplied listener to this dispatcher as a weakly referenced listener.
     * <p>
     * A call to this method has no effect after {@link #shutdown()} has been called.
     * </p>
     * <p>
     * Attempts to add a listener that is already strongly referenced will be ignored.
     * </p>
     *
     * @param listener Listener object
     */
    public final void addWeakListener(TListener listener) {
        if (listener == this) {
            throw new IllegalArgumentException("Adding this instance to itself is not allowed");
        }
        if (isShutdown() || listeners.contains(listener)) return;
        removeListener(listener);
        WeakReference<TListener> weakReference = new WeakReference<>(listener);
        weakListeners.add(weakReference);
        cleanWeakListeners();
    }

    /**
     * Removes the supplied listener from this dispatcher (both weak and strong).
     *
     * @param listener Listener object
     */
    public final void removeListener(TListener listener) {
        if (listeners.remove(listener)) {
            return;
        }
        // Try to remove weak listener
        for (WeakReference<TListener> ref : weakListeners) {
            TListener refValue = ref.get();
            if (refValue == listener) {
                weakListeners.remove(ref);
                return;
            } else if (refValue == null) {
                weakListeners.remove(ref);
            }
        }
    }

    /**
     * Removes all listeners from this dispatcher.
     */
    public final void removeAllListeners() {
        listeners.clear();
        weakListeners.clear();
    }

    /**
     * Shuts down this dispatcher and removes all listeners.
     * <p>
     * After shutdown calls to {@link #addListener(Object)} are ignored.
     * </p>
     */
    public final void shutdown() {
        shutdown = true;
        removeAllListeners();
    }

    /**
     * @return <code>true</code> when this dispatcher has been shut down.
     */
    public final boolean isShutdown() {
        return shutdown;
    }

    @Override
    public final Iterator<TListener> iterator() {
        cleanWeakListeners();
        return new ListenerIterator<>(listeners.iterator(), weakListeners.iterator());
    }

    private void cleanWeakListeners() {
        for (WeakReference<TListener> ref : weakListeners) {
            if (ref.get() == null) {
                weakListeners.remove(ref);
            }
        }
    }

    private static final class ListenerIterator<TListener> implements Iterator<TListener> {

        private final Iterator<TListener> strongIterator;
        private final Iterator<WeakReference<TListener>> weakIterator;
        private boolean useStrongIterator = true;
        private TListener nextWeakListener;

        private ListenerIterator(Iterator<TListener> strongIterator, Iterator<WeakReference<TListener>> weakIterator) {
            this.strongIterator = strongIterator;
            this.weakIterator = weakIterator;
        }

        @Override
        public boolean hasNext() {
            if (useStrongIterator) {
                if (strongIterator.hasNext()) {
                    return true;
                } else {
                    useStrongIterator = false;
                }
            }
            return getNextWeakListener() != null;
        }

        @Override
        public TListener next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (useStrongIterator) {
                return strongIterator.next();
            }
            TListener next = nextWeakListener;
            nextWeakListener = null;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        private TListener getNextWeakListener() {
            final Iterator<WeakReference<TListener>> weakIterator = this.weakIterator;
            TListener nextWeakListener = this.nextWeakListener;
            while (nextWeakListener == null && weakIterator.hasNext()) {
                WeakReference<TListener> currentRef = weakIterator.next();
                nextWeakListener = currentRef.get();
            }
            return this.nextWeakListener = nextWeakListener;
        }
    }
}
