// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.ng.AbstractEventHandle;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

/**
 * Event handle for the JNA protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class JnaEventHandle extends AbstractEventHandle {

    private static final System.Logger log = System.getLogger(JnaEventHandle.class.getName());

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
        Cleaners.getJbCleaner().register(this, new CleanupAction(eventBuffer, resultBuffer));
    }

    @SuppressWarnings("java:S1185")
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
    @SuppressWarnings("unused")
    public void debugMemoryDump() {
        if (!log.isLoggable(System.Logger.Level.DEBUG)) return;
        if (size == -1) {
            log.log(System.Logger.Level.DEBUG, "Event handle not allocated");
        }
        synchronized (JnaEventHandle.class) {
            log.log(System.Logger.Level.DEBUG, "{0}: Event Buffer: {1}, Result Buffer: {2}", getEventName(),
                    getEventBuffer().getValue().dump(0, size), getResultBuffer().getValue().dump(0, size));
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
            // We can't use a cleanable here, because the event handle might be reused, not passing eventNameMemory as
            // it shouldn't be cleaned here
            new CleanupAction(eventBuffer, resultBuffer).cleanup(clientLibrary);
        } finally {
            size = -1;
        }
    }

    @Override
    public String toString() {
        return "JnaEventHandle:{ name:" + getEventName() + ", eventId:" + getEventId() + " }";
    }

    private class JnaEventCallback implements FbClientLibrary.IscEventCallback {
        @Override
        public void apply(Pointer resultArgument, short eventBufferLength, Pointer eventsList) {
            final int length = eventBufferLength & 0xFFFF;
            if (length == 0 || eventsList == null) return;

            synchronized (JnaEventHandle.this) {
                byte[] tempBuffer = eventsList.getByteArray(0, length);
                resultArgument.write(0, tempBuffer, 0, length);
            }

            // TODO Push to executor?
            onEventOccurred();
        }
    }

    private class WinJnaEventCallback extends JnaEventCallback implements WinFbClientLibrary.IscEventStdCallback {
    }

    private record CleanupAction(PointerByReference eventBuffer, PointerByReference resultBuffer) implements Runnable {

        // NOTE: This cleanup action doesn't do anything with eventNameMemory. The release of that memory will be
        // handled by the cleaner within JNA class Memory.

        @Override
        public void run() {
            cleanup(FbClientDatabaseFactory.getInstance().getClientLibrary());
        }

        // Allow reuse by method releaseMemory
        private void cleanup(FbClientLibrary clientLibrary) {
            if (eventBuffer.getValue() != Pointer.NULL) {
                clientLibrary.isc_free(eventBuffer.getValue());
                eventBuffer.setValue(Pointer.NULL);
            }
            if (resultBuffer.getValue() != Pointer.NULL) {
                clientLibrary.isc_free(resultBuffer.getValue());
                resultBuffer.setValue(Pointer.NULL);
            }
        }
    }

}
