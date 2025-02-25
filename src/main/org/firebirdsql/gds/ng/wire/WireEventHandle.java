// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author Mark Rotteveel
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
        if (event.eventId() != getLocalId()) return;

        channel.removeChannelListener(this);
        synchronized (this) {
            internalCount = event.eventCount();
        }
        onEventOccurred();
    }

    @Override
    public String toString() {
        return "WireEventHandle:{ name:" + getEventName() + ", localId:" + localId +
                ", internalCount:" + internalCount + ", previousInternalCount:" + previousInternalCount + " }";
    }
}
