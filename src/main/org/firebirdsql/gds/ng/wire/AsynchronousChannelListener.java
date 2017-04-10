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

/**
 * Listener interface for events on the asynchronous channel.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface AsynchronousChannelListener {

    /**
     * Signals the closing of an asynchronous channel.
     * <p>
     * Fired before the channel is actually closed.
     * </p>
     *
     * @param channel The channel that is being closed
     */
    void channelClosing(FbWireAsynchronousChannel channel);

    /**
     * Signals that an event has been received.
     * <p>
     * Implementations should take care to only perform short processing on the current thread. If longer or
     * complicated processing is necessary, please offload it to another thread or executor.
     * </p>
     *
     * @param channel The channel that received the event
     * @param event The event received
     */
    void eventReceived(FbWireAsynchronousChannel channel, Event event);

    /**
     * Event count notification
     */
    class Event {
        private final int eventId;
        private final int eventCount;

        public Event(int eventId, int eventCount) {
            this.eventId = eventId;
            this.eventCount = eventCount;
        }

        public int getEventId() {
            return eventId;
        }

        public int getEventCount() {
            return eventCount;
        }
    }
}
