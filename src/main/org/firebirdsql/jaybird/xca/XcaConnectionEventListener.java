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
package org.firebirdsql.jaybird.xca;

/**
 * Listener to receives notification of events on a managed connection instance.
 */
public interface XcaConnectionEventListener {

    /**
     * Notifies the close of a connection.
     * <p>
     * A managed connection notifies its listeners by calling this method when an application component closes a
     * connection handle. The owner of the managed connection can use this event to put the managed connection instance
     * back in to the connection pool, or close the physical connection.
     * </p>
     *
     * @param connectionEvent
     *         Connection event
     */
    void connectionClosed(XcaConnectionEvent connectionEvent);

    /**
     * Notifies a connection related error.
     * <p>
     * The managed connection instance calls this method to notify its listeners of the occurrence of a physical
     * connection-related error. The event notification happens just before it throws an exception to the application
     * component using the connection handle.
     * </p>
     * <p>
     * This method indicates that the associated managed connection instance is now invalid and unusable. The owner of
     * the managed connection handles the connection error event notification by initiating owner-specific cleanup (for
     * example, removing the managed connection instance from the connection pool) and then calling the destroy method
     * to destroy the physical connection.
     * </p>
     *
     * @param connectionEvent
     *         Connection event
     */
    void connectionErrorOccurred(XcaConnectionEvent connectionEvent);

}
