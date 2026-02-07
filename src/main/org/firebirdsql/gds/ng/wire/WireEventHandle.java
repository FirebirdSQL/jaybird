// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.AbstractEventHandle;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.gds.ISCConstants.EPB_version1;

/**
 * Event handle for the wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public final class WireEventHandle extends AbstractEventHandle implements AsynchronousChannelListener {

    private static final AtomicInteger localEventId = new AtomicInteger();

    private final byte[] eventNameBytes;
    private volatile int internalCount;
    private int previousInternalCount;
    private int localId;

    public WireEventHandle(String eventName, EventHandler eventHandler, Encoding encoding) {
        super(eventName, eventHandler);
        eventNameBytes = encoding.encodeToCharset(eventName);
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
    }

    public synchronized void calculateCount() {
        // Determine event counts since previous notification
        setEventCount(internalCount - previousInternalCount);
        previousInternalCount = internalCount;
    }

    @Override
    public synchronized int getEventId() {
        return localId;
    }

    /**
     * Generates a new local id for this event.
     */
    @SuppressWarnings("UnusedReturnValue")
    public int assignNewLocalId() {
        final int newLocalId = localEventId.incrementAndGet();
        synchronized (this) {
            return localId = newLocalId;
        }
    }

    /**
     * @return The current local id of this event.
     */
    public synchronized int getLocalId() {
        return localId;
    }

    public byte[] toByteArray() throws IOException {
        var byteOut = new byte[2 + eventNameBytes.length + 4];
        byteOut[0] = EPB_version1; // event buffer version
        byteOut[1] = (byte) eventNameBytes.length;
        System.arraycopy(eventNameBytes, 0, byteOut, 2, eventNameBytes.length);
        VaxEncoding.encodeVaxIntegerWithoutLength(byteOut, 2 + eventNameBytes.length, internalCount);
        return byteOut;
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
