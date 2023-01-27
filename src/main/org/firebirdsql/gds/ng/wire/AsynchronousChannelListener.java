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
package org.firebirdsql.gds.ng.wire;

/**
 * Listener interface for events on the asynchronous channel.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface AsynchronousChannelListener {

    /**
     * Signals the closing of an asynchronous channel.
     * <p>
     * Fired before the channel is actually closed.
     * </p>
     *
     * @param channel
     *         channel that is being closed
     */
    void channelClosing(FbWireAsynchronousChannel channel);

    /**
     * Signals that an event has been received.
     * <p>
     * Implementations should take care to only perform short processing on the current thread. If longer or
     * complicated processing is necessary, please offload it to another thread or executor.
     * </p>
     *
     * @param channel
     *         channel that received the event
     * @param event
     *         event received
     */
    void eventReceived(FbWireAsynchronousChannel channel, Event event);

    /**
     * Event count notification
     */
    record Event(int eventId, int eventCount) {
    }
}
