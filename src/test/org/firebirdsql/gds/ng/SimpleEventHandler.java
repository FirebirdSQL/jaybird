// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author Mark Rotteveel
*/
@NullMarked
public class SimpleEventHandler implements EventHandler {

    private final List<EventHandle> receivedEventHandles = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void eventOccurred(EventHandle eventHandle) {
        receivedEventHandles.add(eventHandle);
    }

    public List<EventHandle> getReceivedEventHandles() {
        synchronized (receivedEventHandles) {
            return new ArrayList<>(receivedEventHandles);
        }
    }

    public void clearEvents() {
        receivedEventHandles.clear();
    }
}
