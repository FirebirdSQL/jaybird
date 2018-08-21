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

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Process asynchronous channels for notification of events.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class AsynchronousProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousProcessor.class);

    /**
     * Initialize on demand holder
     */
    private static class ProcessorHolder {
        private static final AsynchronousProcessor INSTANCE = new AsynchronousProcessor();
    }

    private final AsynchronousChannelListener channelListener = new ProcessorChannelListener();
    private final List<FbWireAsynchronousChannel> newChannels =
            Collections.synchronizedList(new ArrayList<FbWireAsynchronousChannel>());
    private final SelectorTask selectorTask = new SelectorTask();
    private final Selector selector;

    private AsynchronousProcessor() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize asynchronous processor", e);
        }
        Thread selectorThread = new Thread(selectorTask, "Jaybird asynchronous processing");
        selectorThread.setDaemon(true);
        selectorThread.setUncaughtExceptionHandler(new LogUncaughtException());
        selectorThread.start();
    }

    /**
     * @return Singleton instance
     */
    public static AsynchronousProcessor getInstance() {
        return ProcessorHolder.INSTANCE;
    }

    /**
     * Registers an asynchronous channel with the asynchronous processor.
     *
     * @param channel
     *         The channel to register
     */
    public void registerAsynchronousChannel(FbWireAsynchronousChannel channel) {
        newChannels.add(channel);
        channel.addChannelListener(channelListener);
        selector.wakeup();
    }

    // TODO Reduce visibility or remove entirely?
    public void shutdown() {
        selectorTask.stop();
        selector.wakeup();
    }

    private class ProcessorChannelListener implements AsynchronousChannelListener {
        @Override
        public void channelClosing(FbWireAsynchronousChannel channel) {
            if (!newChannels.remove(channel)) {
                // TODO Replace with map from channel to selectionkey?
                for (SelectionKey key : new ArrayList<>(selector.keys())) {
                    if (key.isValid() && key.attachment() == channel) {
                        key.cancel();
                        break;
                    }
                }
            }
            channel.removeChannelListener(this);
        }

        @Override
        public void eventReceived(FbWireAsynchronousChannel channel, Event event) {
            // Ignore
        }
    }

    private class SelectorTask implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (newChannels) {
                        for (FbWireAsynchronousChannel channel : newChannels) {
                            addChannel(channel);
                        }
                        newChannels.clear();
                    }

                    if (selector.select() == 0) continue;

                    handleReadableKeys(selector.selectedKeys());
                } catch (IOException ex) {
                    // TODO check any other necessary handling
                    log.error("IOException in async event processing", ex);
                }
            }
            try {
                selector.close();
            } catch (IOException e) {
                // ignore
                log.error("IOException closing event selector", e);
            }
        }

        private void handleReadableKeys(final Set<SelectionKey> selectedKeys) {
            synchronized (selectedKeys) {
                Iterator<SelectionKey> selectedKeysIterator = selectedKeys.iterator();
                while (selectedKeysIterator.hasNext()) {
                    final SelectionKey selectionKey = selectedKeysIterator.next();
                    selectedKeysIterator.remove();
                    if (!selectionKey.isValid()) continue;

                    handleReadable(selectionKey);
                }
            }
        }

        private void addChannel(FbWireAsynchronousChannel channel) throws ClosedChannelException {
            try {
                channel.getSocketChannel().register(selector, SelectionKey.OP_READ, channel);
            } catch (SQLException ex) {
                // channel closed, remove listener
                channel.removeChannelListener(channelListener);
            }
        }

        private void handleReadable(SelectionKey selectionKey) {
            try {
                if (!selectionKey.isReadable()) {
                    return;
                }
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                FbWireAsynchronousChannel channel = (FbWireAsynchronousChannel) selectionKey.attachment();

                final ByteBuffer eventBuffer = channel.getEventBuffer();
                int count = socketChannel.read(eventBuffer);
                if (count > 0) {
                    channel.processEventData();
                } else if (count < 0) {
                    try {
                        channel.close();
                    } catch (SQLException e) {
                        // ignore
                        log.error("SQLException closing event channel", e);
                    }
                }
            } catch (AsynchronousCloseException e) {
                // Channel closed
                log.debug("AsynchronousCloseException reading from event channel; cancelling key", e);
                selectionKey.cancel();
            } catch (CancelledKeyException e) {
                // ignore; key cancelled as part of close
            } catch (Exception e) {
                log.error(e.getClass().getName() + " reading from event channel; attempting to close async channel", e);
                FbWireAsynchronousChannel channel = (FbWireAsynchronousChannel) selectionKey.attachment();
                try {
                    channel.close();
                } catch (Exception e1) {
                    log.error("Attempt to close async channel failed", e1);
                }
            }
        }

        private void stop() {
            running = false;
        }
    }

    private static class LogUncaughtException implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Jaybird asynchronous processing terminated. Uncaught exception on " + t.getName(), e);
        }
    }
}
