// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

/**
 * Handle to internal event-handling structures.
 */
public interface EventHandle {
   
    /**
     * Get the name of the event for which this handle is set to listen for
     *
     * @return The name of the event
     */
    String getEventName();

    /**
     * Get the count of event occurrences for the most recent occurrence(s)
     * of the event for which this handle is registered.
     *
     * @return The event count
     */
    int getEventCount();

    /**
     * Get the internal event id number for this handle
     *
     * @return The internal event id
     */
    int getEventId();

}
