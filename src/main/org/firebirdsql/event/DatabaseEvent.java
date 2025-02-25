// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.event;

/**
 * An interface for retrieving information about events that have occurred 
 *
 * @author Gabriel Reid
 */
public interface DatabaseEvent {
   
    /**
     * Get the name of the event that occurred.
     *
     * @return The event name
     */ 
    String getEventName();

    /**
     * Get the number of times the event occurred.
     *
     * @return The number of times the event occurred
     */
    int getEventCount();
}
