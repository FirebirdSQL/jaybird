// SPDX-FileCopyrightText: Copyright 2015-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.EventHandle;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;

/**
 * Interface for the asynchronous channel used for event notification.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbWireAsynchronousChannel {

    /**
     * Connects the asynchronous channel to the specified port.
     *
     * @param hostName
     *         hostname
     * @param portNumber
     *         port number
     * @throws java.sql.SQLException
     *         for errors connecting, or if the connection is already established
     * @since 7
     */
    void connect(String hostName, int portNumber) throws SQLException;

    /**
     * Disconnect the asynchronous channel.
     * <p>
     * Once closed, the connection can be reestablished using {@link #connect(String, int)}.
     * </p>
     * <p>
     * Calling {@code close} on a closed channel is a no-op; no exception should be thrown.
     * </p>
     *
     * @throws SQLException
     *         For errors closing the channel
     */
    void close() throws SQLException;

    /**
     * @return {@code true} if connected, otherwise {@code false}
     */
    boolean isConnected();

    /**
     * Register a listener for this channel.
     *
     * @param listener Listener
     */
    void addChannelListener(AsynchronousChannelListener listener);

    /**
     * Remove a listener from this channel
     *
     * @param listener Listener
     */
    void removeChannelListener(AsynchronousChannelListener listener);

    /**
     * @return The socket channel associated with this asynchronous channel
     * @throws java.sql.SQLException
     *         If not currently connected
     */
    SocketChannel getSocketChannel() throws SQLException;

    /**
     * @return The byte buffer for event data
     */
    ByteBuffer getEventBuffer();

    /**
     * Process the current event data in the buffer.
     * <p>
     * This is only to be called by the {@link org.firebirdsql.gds.ng.wire.AsynchronousProcessor}. Implementations
     * should be ready to deal with incomplete data in the event buffer (eg by not processing).
     * </p>
     */
    void processEventData();

    /**
     * Queues a wait for an event.
     *
     * @param eventHandle
     *         Event handle
     */
    void queueEvent(EventHandle eventHandle) throws SQLException;

    /**
     * Cancels a registered event.
     *
     * @param eventHandle
     *         The event handle to cancel
     * @throws SQLException
     *         For errors cancelling the event
     */
    void cancelEvent(EventHandle eventHandle) throws SQLException;
}
