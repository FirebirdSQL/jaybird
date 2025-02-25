// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.wire.AsynchronousChannelListener} for testing purposes
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class SimpleChannelListener implements AsynchronousChannelListener {

    private volatile boolean receivedChannelClosing;
    private final List<Event> receivedEvents = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void channelClosing(FbWireAsynchronousChannel channel) {
        receivedChannelClosing = true;
    }

    @Override
    public void eventReceived(FbWireAsynchronousChannel channel, Event event) {
        receivedEvents.add(event);
    }

    public boolean hasReceivedChannelClosing() {
        return receivedChannelClosing;
    }

    public List<Event> getReceivedEvents() {
        synchronized (receivedEvents) {
            return new ArrayList<>(receivedEvents);
        }
    }
}
