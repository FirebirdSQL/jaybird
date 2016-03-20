/*
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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DefaultDatabaseListener;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Asynchronous channel implementation for the V10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10AsynchronousChannel implements FbWireAsynchronousChannel {

    private static Logger log = LoggerFactory.getLogger(V10AsynchronousChannel.class);

    /*
     * Expecting:
     * single operation: 1 byte (op_dummy, op_exit, op_disconnect, op_void)
     *
     * Normal response:
     * (see processing)
     *
     * Event:
     * - 1 byte operation (op_event)
     * - 4 bytes db handle
     * - 4 bytes event buffer length
     * - buffer consisting of
     * -- 1 byte event buffer version (1)
     * -- 1 byte event name length
     * -- max 256 bytes event name
     * -- 4 bytes event count (vax integer)
     * - 8 bytes AST info
     * - 4 bytes event id
     *
     * Total: 282 per event; allocating 2048 to have sufficient space (TODO: might not be enough for 'normal' response)
     */
    private static final int EVENT_BUFFER_SIZE = 2048;

    private final AsynchronousChannelListenerDispatcher channelListenerDispatcher = new AsynchronousChannelListenerDispatcher();
    private final FbWireDatabase database;
    private final ByteBuffer eventBuffer = ByteBuffer.allocate(EVENT_BUFFER_SIZE);
    private int auxHandle;
    private SocketChannel socketChannel;

    public V10AsynchronousChannel(FbWireDatabase database) {
        this.database = database;
        database.addDatabaseListener(new ChannelDatabaseListener());
    }

    @Override
    public void connect(String hostName, int portNumber, int auxHandle) throws SQLException {
        if (isConnected()) throw new SQLException("Asynchronous channel already established");
        this.auxHandle = auxHandle;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.socket().setTcpNoDelay(true);
            SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
            socketChannel.connect(socketAddress);
            socketChannel.configureBlocking(false);
        } catch (IOException ex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
        }
    }

    private final Lock closeLock = new ReentrantLock();

    @Override
    public void close() throws SQLException {
        if (!isConnected()) return;
        // Lock already held by another close of the channel
        if (!closeLock.tryLock()) return;
        try {
            if (!isConnected()) return;
            channelListenerDispatcher.channelClosing(this);
            socketChannel.close();
        } catch (IOException ex) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_errorAsynchronousEventChannelClose)
                    .cause(ex)
                    .toFlatSQLException();
        } finally {
            socketChannel = null;
            closeLock.unlock();
        }
    }

    @Override
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    @Override
    public void addChannelListener(AsynchronousChannelListener listener) {
        channelListenerDispatcher.addListener(listener);
    }

    @Override
    public void removeChannelListener(AsynchronousChannelListener listener) {
        channelListenerDispatcher.removeListener(listener);
    }

    @Override
    public SocketChannel getSocketChannel() throws SQLException {
        if (!isConnected()) throw new SQLException("Asynchronous channel not connected");
        return socketChannel;
    }

    @Override
    public ByteBuffer getEventBuffer() {
        return eventBuffer;
    }

    @Override
    public void processEventData() {
        // TODO We assume the caller has called eventBuffer.flip() is that wise, or should we do that here?
        try {
            bufferProcessing:
            while (eventBuffer.remaining() >= 4) {
                eventBuffer.mark();
                final int operation = eventBuffer.getInt();
                switch (operation) {
                case op_dummy:
                    continue;
                case op_exit:
                case op_disconnect:
                    close();
                    break bufferProcessing;
                case op_event:
                    if (!processSingleEvent()) {
                        log.debug("Could not process entire event, resetting position for next channel read");
                        // Restoring position so we reprocess the event if the rest of the data has been received
                        eventBuffer.reset();
                        break bufferProcessing;
                    }
                    break;
                default:
                    if (log.isErrorEnabled()) {
                        log.error("Unexpected event operation received: " + operation + " position " +
                                eventBuffer.position() + " limit " + eventBuffer.limit());
                    }
                    break;
                }
            }
            eventBuffer.compact();
        } catch (SQLException e) {
            log.fatal("SQLException processing event data: " + e.getMessage(), e);
        } catch (Exception e) {
            log.fatal("Unexpected exception processing events: " + e.getMessage(), e);
        }
    }

    @Override
    public void queueEvent(EventHandle eventHandle) throws SQLException {
        if (!(eventHandle instanceof WireEventHandle))
            throw new SQLNonTransientException("Invalid event handle type: " + eventHandle.getClass().getName());
        final WireEventHandle wireEventHandle = (WireEventHandle) eventHandle;
        wireEventHandle.assignNewLocalId();
        addChannelListener(wireEventHandle);

        synchronized (database.getSynchronizationObject()) {
            try {
                final XdrOutputStream dbXdrOut = database.getXdrStreamAccess().getXdrOut();
                dbXdrOut.writeInt(op_que_events);
                dbXdrOut.writeInt(auxHandle);
                dbXdrOut.writeBuffer(wireEventHandle.toByteArray());
                dbXdrOut.writeLong(0); // AST info
                dbXdrOut.writeInt(wireEventHandle.getLocalId());
                dbXdrOut.flush();
            } catch (IOException e) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
            }
            try {
                final GenericResponse response = database.readGenericResponse(null);
                wireEventHandle.setEventId(response.getObjectHandle());
            } catch (IOException e) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
            }
        }
    }

    @Override
    public void cancelEvent(EventHandle eventHandle) throws SQLException {
        if (!(eventHandle instanceof WireEventHandle))
            throw new SQLNonTransientException("Invalid event handle type: " + eventHandle.getClass().getName());
        final WireEventHandle wireEventHandle = (WireEventHandle) eventHandle;
        removeChannelListener(wireEventHandle);

        synchronized (database.getSynchronizationObject()) {
            try {
                final XdrOutputStream dbXdrOut = database.getXdrStreamAccess().getXdrOut();
                dbXdrOut.writeInt(op_cancel_events);
                dbXdrOut.writeInt(database.getHandle());
                dbXdrOut.writeInt(wireEventHandle.getLocalId());
                dbXdrOut.flush();
            } catch (IOException e) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
            }
            try {
                database.readGenericResponse(null);
            } catch (IOException e) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
            }
        }
    }

    /**
     * Processes the event buffer for a single event.
     *
     * @return {@code true} if the full event was read, {@code false} if the event data was incomplete,
     * and we need to wait for another read.
     */
    private boolean processSingleEvent() {
        if (eventBuffer.remaining() < 20) {
            // Not enough data for db handle, buffer length, AST and event id (ignoring space for buffer).
            return false;
        }
        try {
            eventBuffer.getInt(); // DB handle (ignore)
            final int bufferLength = eventBuffer.getInt();
            final int padding = (4 - bufferLength) & 3;

            // No need to process if we don't have the full buffer + AST + event id
            if (eventBuffer.remaining() < bufferLength + padding + 12) return false;

            final byte[] buffer = new byte[bufferLength];
            eventBuffer.get(buffer);
            // Skip padding
            eventBuffer.position(eventBuffer.position() + padding);

            // We are only interested in the event count (last 4 bytes of the buffer)
            int eventCount = 0;
            if (bufferLength > 4) {
                // TODO Isn't this always the case? (old implementation also made this check)
                eventCount = iscVaxInteger(buffer, bufferLength - 4, 4);
            }

            eventBuffer.getLong(); // AST info (ignore)
            int eventId = eventBuffer.getInt();

            log.debug(String.format("Received event id %d, eventCount %d", eventId, eventCount));

            channelListenerDispatcher.eventReceived(this, new AsynchronousChannelListener.Event(eventId, eventCount));

            return true;
        } catch (BufferUnderflowException ex) {
            // Insufficient data to process full event
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private class ChannelDatabaseListener extends DefaultDatabaseListener {

        @Override
        public void detached(FbDatabase database) {
            try {
                close();
            } catch (Exception ex) {
                log.error("Exception closing asynchronous channel in response to a FbDatabase detached event", ex);
            }
        }
    }
}
