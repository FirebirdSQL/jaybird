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
package org.firebirdsql.gds.ng.listeners;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Dispatcher to maintain a list of listeners of type <code>TListener</code>
 *
 * @param <TListener> Listener type
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractListenerDispatcher<TListener> implements Iterable<TListener> {

    private final CopyOnWriteArrayList<TListener> listeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<WeakReference<TListener>> weakListeners = new CopyOnWriteArrayList<>();
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
        listeners.addIfAbsent(listener);
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
        weakListeners.removeIf(ref -> {
            TListener refValue = ref.get();
            return refValue == null || refValue == listener;
        });
    }

    protected final void notify(Consumer<TListener> notificationHandler, String notificationLogName) {
        for (TListener listener : this) {
            try {
                notificationHandler.accept(listener);
            } catch (Exception e) {
                logError("Error on notify " + notificationLogName + " to listener " + listener, e);
            }
        }
    }

    protected abstract void logError(String message, Throwable throwable);

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
        return new ListenerIterator<>(listeners, weakListeners);
    }

    private void cleanWeakListeners() {
        weakListeners.removeIf(ref -> ref.get() == null);
    }

    /**
     * Iterator implementation that access the weak listeners in reverse order, and then the strong listeners in
     * reverse order.
     *
     * @param <TListener> Listener type
     */
    private static final class ListenerIterator<TListener> implements Iterator<TListener> {

        private final ListIterator<TListener> strongIterator;
        private final ListIterator<WeakReference<TListener>> weakIterator;
        private boolean useStrongIterator;
        private TListener nextWeakListener;

        private ListenerIterator(
                final CopyOnWriteArrayList<TListener> strongListeners,
                final CopyOnWriteArrayList<WeakReference<TListener>> weakListeners) {
            this.strongIterator = listIteratorAtEnd(strongListeners);
            this.weakIterator = listIteratorAtEnd(weakListeners);
        }

        @Override
        public boolean hasNext() {
            // Be aware we are reverse iterating the listeners, but present it as forward iteration!
            if (!useStrongIterator) {
                if (getNextWeakListener() != null) {
                    return true;
                } else {
                    useStrongIterator = true;
                }
            }
            return strongIterator.hasPrevious();
        }

        @Override
        public TListener next() {
            // Be aware we are reverse iterating the listeners, but present it as forward iteration!
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (useStrongIterator) {
                return strongIterator.previous();
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
            // Be aware we are reverse iterating the listeners, but present it as forward iteration!
            final ListIterator<WeakReference<TListener>> weakIterator = this.weakIterator;
            TListener nextWeakListener = this.nextWeakListener;
            while (nextWeakListener == null && weakIterator.hasPrevious()) {
                WeakReference<TListener> currentRef = weakIterator.previous();
                nextWeakListener = currentRef.get();
            }
            return this.nextWeakListener = nextWeakListener;
        }

        /**
         * Produces a list iterator that is at the end.
         * <p>
         * This allows for a thread safe variant of {@code list.listIterator(list.size())} without requiring
         * synchronization for all modifications at the small expense of localized complexity.
         * </p>
         *
         * @return List iterator at end of list
         */
        private static <T> ListIterator<T> listIteratorAtEnd(final CopyOnWriteArrayList<T> list) {
            ListIterator<T> listIterator;
            try {
                // Try to prevent IndexOutOfBoundsException if size of list is reduced under concurrent access
                listIterator = list.listIterator(Math.max(0, list.size() - 1));
            } catch (IndexOutOfBoundsException e) {
                // Reduction was greater than one, just start at beginning
                // Note that in most forms of access this should not happen, this is just to prevent edge cases
                listIterator = list.listIterator();
            }
            // Scan ahead for the real end
            while (listIterator.hasNext()) {
                listIterator.next();
            }
            return listIterator;
        }
    }
}
