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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.ng.AbstractEventHandle;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Event handle for the JNA protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class JnaEventHandle extends AbstractEventHandle {

    private static final Logger LOG = LoggerFactory.getLogger(JnaEventHandle.class);

    private final Memory eventNameMemory;
    private final IntByReference eventId = new IntByReference(0);
    private int size = -1;
    private final PointerByReference eventBuffer = new PointerByReference();
    private final PointerByReference resultBuffer = new PointerByReference();
    private final JnaEventHandle.JnaEventCallback callback = createEventCallback();

    JnaEventHandle(String eventName, EventHandler eventHandler, Encoding encoding) {
        super(eventName, eventHandler);
        // Requires null-termination
        final byte[] eventNameBytes = encoding.encodeToCharset(eventName + '\0');
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
        eventNameMemory = new Memory(eventNameBytes.length);
        eventNameMemory.write(0, eventNameBytes, 0, eventNameBytes.length);
    }

    @Override
    protected void setEventCount(int eventCount) {
        super.setEventCount(eventCount);
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

    Memory getEventNameMemory() {
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

    /**
     * Dumps the event buffers to the logger, if debug is enabled.
     */
    public void debugMemoryDump() {
        if (!LOG.isDebugEnabled()) return;
        if (size == -1) {
            LOG.debug("Event handle not allocated");
        }
        synchronized (JnaEventHandle.class) {
            String sb = "Event Buffer " + getEventName() + ':' +
                    getEventBuffer().getValue().dump(0, size) +
                    "Result Buffer " + getEventName() + ':' +
                    getResultBuffer().getValue().dump(0, size);
            LOG.debug(sb);
        }
    }

    private JnaEventCallback createEventCallback() {
        return Platform.isWindows()
                ? new WinJnaEventCallback()
                : new JnaEventCallback();
    }

    /**
     * Releases the native memory held by this event handle.
     *
     * @param clientLibrary The client library instance
     */
    public synchronized void releaseMemory(FbClientLibrary clientLibrary) {
        if (size == -1) return;
        try {
            if (eventBuffer.getValue() != Pointer.NULL) {
                clientLibrary.isc_free(eventBuffer.getValue());
                eventBuffer.setValue(Pointer.NULL);
            }
            if (resultBuffer.getValue() != Pointer.NULL) {
                clientLibrary.isc_free(resultBuffer.getValue());
                resultBuffer.setValue(Pointer.NULL);
            }
        } finally {
            size = -1;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            releaseMemory(FbClientDatabaseFactory.getInstance().getClientLibrary());
        } finally {
            super.finalize();
        }
    }

    private class JnaEventCallback implements FbClientLibrary.IscEventCallback {
        @Override
        public synchronized void apply(Pointer resultArgument, short eventBufferLength, Pointer eventsList) {
            final int length = eventBufferLength & 0xFFFF;
            if (length == 0 || eventsList == null) return;

            byte[] tempBuffer = eventsList.getByteArray(0, length);
            resultArgument.write(0, tempBuffer, 0, length);

            // TODO Push to executor?
            onEventOccurred();
        }
    }

    private class WinJnaEventCallback extends JnaEventCallback implements WinFbClientLibrary.IscEventStdCallback {
    }
}
