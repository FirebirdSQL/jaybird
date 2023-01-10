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

import org.firebirdsql.gds.ng.listeners.AbstractListenerDispatcher;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Dispatcher for {@link org.firebirdsql.gds.ng.wire.AsynchronousChannelListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class AsynchronousChannelListenerDispatcher extends AbstractListenerDispatcher<AsynchronousChannelListener>
        implements AsynchronousChannelListener {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousChannelListenerDispatcher.class);

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
        log.error(message, throwable);
    }
}
