// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.event;

/**
 * An interface for callbacks in response to Firebird events 
 *
 * @author Gabriel Reid
 */
@FunctionalInterface
public interface EventListener {

    /**
     * Called when a database event occurs.
     *
     * @param event Object with information about the event that has occurred 
     */
    void eventOccurred(DatabaseEvent event);

}

