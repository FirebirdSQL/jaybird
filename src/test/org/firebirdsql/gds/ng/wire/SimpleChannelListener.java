/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.wire.AsynchronousChannelListener} for testing purposes
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class SimpleChannelListener implements AsynchronousChannelListener {

    private volatile boolean receivedChannelClosing;
    private final List<Event> receivedEvents = Collections.synchronizedList(new ArrayList<Event>());

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
            return new ArrayList<Event>(receivedEvents);
        }
    }
}
