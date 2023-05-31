/*
 * Firebird Open Source JDBC Driver
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Process asynchronous channels for notification of events.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class AsynchronousProcessor {

    private static final System.Logger log = System.getLogger(AsynchronousProcessor.class.getName());

    /**
     * Initialize on demand holder
     */
    private static final class ProcessorHolder {
        private static final AsynchronousProcessor INSTANCE = new AsynchronousProcessor();
    }

    private final AsynchronousChannelListener channelListener = new ProcessorChannelListener();
    private final List<FbWireAsynchronousChannel> newChannels = Collections.synchronizedList(new ArrayList<>());
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
        selectorThread.setUncaughtExceptionHandler(new LogUncaughtExceptionHandler());
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
        channel.addChannelListener(channelListener);
        newChannels.add(channel);
        selector.wakeup();
    }

    public void unregisterAsynchronousChannel(FbWireAsynchronousChannel channel) {
        if (!newChannels.remove(channel)) {
            // TODO Replace with map from channel to selectionkey?
            for (SelectionKey key : new ArrayList<>(selector.keys())) {
                if (key.isValid() && key.attachment() == channel) {
                    key.cancel();
                    break;
                }
            }
        }
        channel.removeChannelListener(channelListener);
    }

    // TODO Reduce visibility or remove entirely?
    public void shutdown() {
        selectorTask.stop();
        selector.wakeup();
    }

    private final class ProcessorChannelListener implements AsynchronousChannelListener {
        @Override
        public void channelClosing(FbWireAsynchronousChannel channel) {
            unregisterAsynchronousChannel(channel);
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

                    selector.select(this::handleReadable);
                } catch (Exception ex) {
                    log.log(ERROR, "Exception in async event processing", ex);
                }
            }
            try {
                selector.close();
            } catch (Exception e) {
                // ignore
                log.log(ERROR, "Exception closing event selector", e);
            } finally {
                newChannels.clear();
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
                if (!(selectionKey.isValid() && selectionKey.isReadable())) {
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
                        log.log(ERROR, "SQLException closing event channel", e);
                    }
                }
            } catch (AsynchronousCloseException e) {
                // Channel closed
                log.log(TRACE, "AsynchronousCloseException reading from event channel; cancelling key", e);
                selectionKey.cancel();
            } catch (CancelledKeyException e) {
                // ignore; key cancelled as part of close
            } catch (Exception e) {
                log.log(ERROR, "Exception reading from event channel; attempting to close async channel", e);
                FbWireAsynchronousChannel channel = (FbWireAsynchronousChannel) selectionKey.attachment();
                try {
                    channel.close();
                } catch (Exception e1) {
                    log.log(ERROR, "Attempt to close async channel failed", e1);
                }
            }
        }

        private void stop() {
            running = false;
        }
    }

    private static class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.log(ERROR, "Jaybird asynchronous processing terminated. Uncaught exception on " + t.getName(), e);
        }
    }
}
