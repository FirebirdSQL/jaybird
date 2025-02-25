// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.listeners.AbstractListenerDispatcher;

/**
 * Dispatcher for {@link org.firebirdsql.gds.ng.wire.AsynchronousChannelListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class AsynchronousChannelListenerDispatcher extends AbstractListenerDispatcher<AsynchronousChannelListener>
        implements AsynchronousChannelListener {

    private static final System.Logger log = System.getLogger(AsynchronousChannelListenerDispatcher.class.getName());

    @Override
    public void channelClosing(FbWireAsynchronousChannel channel) {
        notify(listener -> listener.channelClosing(channel), "channelClosing");
    }

    @Override
    public void eventReceived(FbWireAsynchronousChannel channel, Event event) {
        notify(listener -> listener.eventReceived(channel, event), "eventReceived");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.log(System.Logger.Level.ERROR, message, throwable);
    }
}
