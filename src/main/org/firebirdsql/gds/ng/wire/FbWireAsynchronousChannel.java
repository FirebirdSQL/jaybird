/*
 * $Id$
 *
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
     *         Hostname
     * @param portNumber
     *         The port number
     * @param auxHandle
     *         Handle identifier for this asynchronous channel
     * @throws java.sql.SQLException
     *         For errors connecting, or if the connection is already established
     */
    void connect(String hostName, int portNumber, int auxHandle) throws SQLException;

    /**
     * Disconnect the asynchronous channel.
     * <p>
     * Once closed, the connection can be reestablished using {@link #connect(String, int, int)}.
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
