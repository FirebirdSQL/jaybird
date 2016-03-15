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

import org.firebirdsql.gds.ng.listeners.AbstractListenerDispatcher;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Dispatcher for {@link org.firebirdsql.gds.ng.wire.AsynchronousChannelListener}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class AsynchronousChannelListenerDispatcher extends AbstractListenerDispatcher<AsynchronousChannelListener>
        implements AsynchronousChannelListener {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousChannelListenerDispatcher.class);

    @Override
    public void channelClosing(FbWireAsynchronousChannel channel) {
        for (AsynchronousChannelListener listener : this) {
            try {
                listener.channelClosing(channel);
            } catch (Exception ex) {
                log.error("Error on notify channelClosing to listener " + listener, ex);
            }
        }
    }

    @Override
    public void eventReceived(FbWireAsynchronousChannel channel, Event event) {
        for (AsynchronousChannelListener listener : this) {
            try {
                listener.eventReceived(channel, event);
            } catch (Exception ex) {
                log.error("Error on notify eventReceived to listener " + listener, ex);
            }
        }
    }
}
