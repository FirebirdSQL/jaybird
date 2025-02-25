// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
