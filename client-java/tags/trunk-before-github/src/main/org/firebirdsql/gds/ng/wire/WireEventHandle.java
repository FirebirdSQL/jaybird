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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event handle for the wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class WireEventHandle implements EventHandle, AsynchronousChannelListener {

    private static final AtomicInteger localEventId = new AtomicInteger();

    private final String eventName;
    private final byte[] eventNameBytes;
    private volatile int eventCount;
    private int internalCount;
    private int previousInternalCount;
    private final EventHandler eventHandler;
    private int localId;
    private int eventId;

    public WireEventHandle(String eventName, EventHandler eventHandler, Encoding encoding) {
        this.eventName = eventName;
        this.eventHandler = eventHandler;
        eventNameBytes = encoding.encodeToCharset(eventName);
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public synchronized void calculateCount() {
        // TODO Can't we just set the count directly?
        eventCount = internalCount - previousInternalCount;
        previousInternalCount = internalCount;
    }

    @Override
    public int getEventCount() {
        return eventCount;
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
    public synchronized int assignNewLocalId() {
        localId = localEventId.incrementAndGet();
        return localId;
    }

    /**
     * @return The current local id of this event.
     */
    public synchronized int getLocalId() {
        return localId;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        XdrOutputStream xdr = new XdrOutputStream(byteOut, false);

        xdr.write(1); // Event version
        xdr.write(eventNameBytes.length);
        xdr.write(eventNameBytes);
        final int currentInternalCount = internalCount;
        for (int shift = 0; shift <= 24; shift += 8) {
            // Write count as VAX integer
            xdr.write((currentInternalCount >> shift) & 0xff);
        }

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
        synchronized (this) {
            internalCount = event.getEventCount();
        }
        eventHandler.eventOccurred(this);
    }
}
