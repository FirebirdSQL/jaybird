package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.ng.AbstractEventHandle;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IEventBlock;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IEventCallback;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IEventCallbackIntf;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Event handle for the native OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IEventBlockImpl extends AbstractEventHandle {

    private static final Logger log = LoggerFactory.getLogger(IEventBlockImpl.class);

    private final CloseableMemory eventNameMemory;
    private int size = -1;
    private IEventBlock eventBlock;
    private IEventCallback callback = new IEventCallback(new IEventCallbackImpl());
    private int referenceCount = 0;

    IEventBlockImpl(String eventName, EventHandler eventHandler, Encoding encoding) {
        super(eventName, eventHandler);
        // Requires null-termination
        final byte[] eventNameBytes = encoding.encodeToCharset(eventName + '\0');
        if (eventNameBytes.length > 256) {
            throw new IllegalArgumentException("Event name as bytes too long");
        }
        eventNameMemory = new CloseableMemory(eventNameBytes.length);
        eventNameMemory.write(0, eventNameBytes, 0, eventNameBytes.length);
    }

    @Override
    protected void setEventCount(int eventCount) {
        super.setEventCount(eventCount);
    }

    @Override
    public int getEventId() {
        throw new UnsupportedOperationException( "Native OO API not support event id");
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
        return eventBlock.getLength();
    }

    /**
     * @return Event callback.
     */
    IEventCallback getCallback() {
        return callback;
    }

    public IEventBlock getEventBlock() {
        return eventBlock;
    }

    public void setEventBlock(IEventBlock eventBlock) {
        this.eventBlock = eventBlock;
    }

    public synchronized void releaseMemory() {
        if (size == -1) return;
        try {
            eventNameMemory.close();
        } finally {
            size = -1;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            releaseMemory();
        } finally {
            super.finalize();
        }
    }

    private class IEventCallbackImpl implements IEventCallbackIntf {

        @Override
        public void addRef() {
            synchronized (this) {
                ++referenceCount;
            }
        }

        @Override
        public int release() {
            synchronized (this) {
                return --referenceCount;
            }
        }

        @Override
        public void eventCallbackFunction(int length, com.sun.jna.Pointer events) {
            synchronized (this) {
                if (events != null) {
                    eventBlock.getValues().write(0, events.getByteArray(0, length), 0, length);
                    this.release();

                    onEventOccurred();
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                release();
            } finally {
                super.finalize();
            }
        }
    }

}
