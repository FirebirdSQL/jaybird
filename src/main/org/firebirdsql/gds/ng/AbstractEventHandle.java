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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;

/**
 * Abstract implementation for event handle.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractEventHandle implements EventHandle {

    private final String eventName;
    private final EventHandler eventHandler;
    private volatile int eventCount;

    protected AbstractEventHandle(String eventName, EventHandler eventHandler) {
        this.eventName = eventName;
        this.eventHandler = eventHandler;
    }

    @Override
    public final String getEventName() {
        return eventName;
    }

    /**
     * Sets the current known event count for this handle.
     *
     * @param eventCount The event count
     */
    protected void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    @Override
    public final int getEventCount() {
        return eventCount;
    }

    protected final void onEventOccurred() {
        eventHandler.eventOccurred(this);
    }
}
