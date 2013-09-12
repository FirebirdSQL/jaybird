/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
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
 * @since 2.3
 */
public class AbstractListenerDispatcher<TListener> implements Iterable<TListener> {

    private final Set<TListener> listeners = Collections.synchronizedSet(new HashSet<TListener>());

    /**
     * Adds the supplied listener to this dispatcher.
     *
     * @param listener Listener object
     */
    public final void addListener(TListener listener) {
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

    public final void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public final Iterator<TListener> iterator() {
        synchronized (listeners) {
            return new ArrayList<TListener>(listeners).iterator();
        }
    }
}
