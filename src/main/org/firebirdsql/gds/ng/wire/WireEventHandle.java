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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.AbstractEventHandle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event handle for the wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class WireEventHandle extends AbstractEventHandle implements AsynchronousChannelListener {

    private static final AtomicInteger localEventId = new AtomicInteger();

    private final byte[] eventNameBytes;
    private int internalCount;
    private int previousInternalCount;
    private int localId;
    private int eventId;

    public WireEventHandle(String eventName, EventHandler eventHandler, Encoding encoding) {
        super(eventName, eventHandler);
        eventNameBytes = encoding.encodeToCharset(eventName);
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
    }

    public synchronized void calculateCount() {
        // TODO Can't we just set the count directly?
        setEventCount(internalCount - previousInternalCount);
        previousInternalCount = internalCount;
    }

    /**
     * @param eventId The server side id of this event
     */
    public synchronized void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public synchronized int getEventId() {
        return eventId;
    }

    /**
     * Generates a new local id for this event.
     */
    public int assignNewLocalId() {
        final int newLocalId = localEventId.incrementAndGet();
        synchronized (this) {
            localId = newLocalId;
            return localId;
        }
    }

    /**
     * @return The current local id of this event.
     */
    public synchronized int getLocalId() {
        return localId;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(2 + eventNameBytes.length + 4);

        byteOut.write(1); // Event version
        byteOut.write(eventNameBytes.length);
        byteOut.write(eventNameBytes);
        final int currentInternalCount;
        synchronized (this) {
            currentInternalCount = internalCount;
        }
        VaxEncoding.encodeVaxIntegerWithoutLength(byteOut, currentInternalCount);

        return byteOut.toByteArray();
    }

    @Override
    public void channelClosing(FbWireAsynchronousChannel channel) {
        channel.removeChannelListener(this);
    }

    @Override
    public void eventReceived(FbWireAsynchronousChannel channel, Event event) {
        if (event.getEventId() != getLocalId()) return;

        channel.removeChannelListener(this);
        final int newCount = event.getEventCount();
        synchronized (this) {
            internalCount = newCount;
        }
        onEventOccurred();
    }
}
