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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

/**
 * Event handle for the JNA protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaEventHandle implements EventHandle {

    private final String eventName;
    private final byte[] eventNameBytes;
    private final Memory eventNameMemory;
    private volatile int eventCount;
    private final EventHandler eventHandler;
    private final IntByReference eventId = new IntByReference(0);
    private int size = -1;
    private final PointerByReference eventBuffer = new PointerByReference();
    private final PointerByReference resultBuffer = new PointerByReference();
    private final JnaEventHandle.JnaEventCallback callback = createEventCallback();

    JnaEventHandle(String eventName, EventHandler eventHandler, Encoding encoding) {
        this.eventName = eventName;
        this.eventHandler = eventHandler;
        // Requires null-termination
        eventNameBytes = encoding.encodeToCharset(eventName + '\0');
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
        eventNameMemory = new Memory(eventNameBytes.length);
        eventNameMemory.write(0, eventNameBytes, 0, eventNameBytes.length);
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    @Override
    public int getEventCount() {
        return eventCount;
    }

    @Override
    public int getEventId() {
        return eventId.getValue();
    }

    /**
     * @return The JNA Event id
     */
    IntByReference getJnaEventId() {
        return eventId;
    }

    /**
     * @return Null-terminated name of the event.
     */
    public byte[] getEventNameBytes() {
        return eventNameBytes;
    }

    public Memory getEventNameMemory() {
        return eventNameMemory;
    }

    /**
     * @param size Size of the event buffers
     */
    void setSize(int size) {
        this.size = size;
    }

    /**
     * @return Size of the event buffers
     */
    int getSize() {
        return size;
    }

    /**
     * @return The event buffer with the last queued count
     */
    PointerByReference getEventBuffer() {
        return eventBuffer;
    }

    /**
     * @return The result buffer with the last received count
     */
    PointerByReference getResultBuffer() {
        return resultBuffer;
    }

    /**
     * @return Event callback
     */
    FbClientLibrary.IscEventCallback getCallback() {
        return callback;
    }

    void debugMemoryDump() {
        System.out.println("Event Buffer");
        System.out.println(getEventBuffer().getValue().dump(0, size));
        System.out.println("Result Buffer");
        System.out.println(getResultBuffer().getValue().dump(0, size));
    }

    private JnaEventCallback createEventCallback() {
        return Platform.isWindows()
                ? new WinJnaEventCallback()
                : new JnaEventCallback();
    }

    private class JnaEventCallback implements FbClientLibrary.IscEventCallback {
        @Override
        public void apply(Pointer resultBuffer, short eventBufferLength, Pointer eventBuffer) {
            synchronized (JnaEventHandle.this) {
                final int length = eventBufferLength & 0xFFFF;
                if (length == 0 || eventBuffer == null) return;
                byte[] tempBuffer = new byte[length];
                eventBuffer.read(0, tempBuffer, 0, length);
                resultBuffer.write(0, tempBuffer, 0, length);

                debugMemoryDump();
            }

            // TODO Push to executor?
            eventHandler.eventOccurred(JnaEventHandle.this);
        }
    }

    private class WinJnaEventCallback extends JnaEventCallback implements WinFbClientLibrary.IscEventStdCallback {
    }
}
