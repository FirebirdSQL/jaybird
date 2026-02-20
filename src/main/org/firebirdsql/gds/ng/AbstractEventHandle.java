// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;

import static java.util.Objects.requireNonNull;

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
        this.eventName = requireNonNull(eventName, "eventName");
        this.eventHandler = requireNonNull(eventHandler, "eventHandler");
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
