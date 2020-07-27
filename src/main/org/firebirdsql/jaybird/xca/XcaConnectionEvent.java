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

import org.firebirdsql.jdbc.FirebirdConnection;

/**
 * The {@code XcaConnectionEvent} class provides information about the source of a connection related event. A
 * {@code XcaConnectionEvent} instance contains the following information:
 * <ul>
 * <li>Type of the connection event</li>
 * <li>{@link FBManagedConnection} instance that generated the connection event. A {@code FBManagedConnection}
 * instance is returned from the method {@link #getSource()}</li>
 * <li>Connection handle associated with the {@code FBManagedConnection} instance; required for the
 * {@code CONNECTION_CLOSED} event and optional for the other event types</li>
 * <li>Optionally, an exception indicating the connection related error. Note that exception is used for
 * {@code CONNECTION_ERROR_OCCURRED}</li>
 * </ul>
 */
public class XcaConnectionEvent {

    private final FBManagedConnection source;
    private final EventType eventType;
    private final Exception exception;
    private FirebirdConnection connectionHandle;

    /**
     * Construct a {@code ConnectionEvent} object.
     *
     * @param source
     *         the source of the event
     * @param eventType
     *         Type of event
     */
    public XcaConnectionEvent(FBManagedConnection source, EventType eventType) {
        this(source, eventType, null);
    }

    /**
     * Construct a {@code ConnectionEvent} object.
     *
     * @param source
     *         the source of the event
     * @param eventType
     *         Type of event
     * @param exception
     *         Exception associated with the event
     */
    public XcaConnectionEvent(FBManagedConnection source, EventType eventType, Exception exception) {
        assert exception != null || eventType != EventType.CONNECTION_ERROR_OCCURRED
                : "Exception required for CONNECTION_ERROR_OCCURRED";
        this.source = source;
        this.eventType = eventType;
        this.exception = exception;
    }

    /**
     * @return The managed connection on which the event initially occurred.
     */
    public FBManagedConnection getSource() {
        return source;
    }

    /**
     * Get the connection handle associated with the managed connection instance. Used for {@code CONNECTION_CLOSED}
     * event.
     *
     * @return The connection handle, can be {@code null}
     */
    public FirebirdConnection getConnectionHandle() {
        return connectionHandle;
    }

    public void setConnectionHandle(FirebirdConnection connectionHandle) {
        this.connectionHandle = connectionHandle;
    }

    /**
     * Get the exception associated with this event.
     *
     * @return Exception for this event, can be {@code null} for event type other than {@code CONNECTION_ERROR_OCCURRED}
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return The type of event
     */
    public EventType getEventType() {
        return eventType;
    }

    public enum EventType {
        /**
         * Event notification that an application component has closed the connection.
         */
        CONNECTION_CLOSED,
        /**
         * Event notification that an error occurred on the connection. This event indicates that the managed
         * connection instance is now invalid and unusable.
         */
        CONNECTION_ERROR_OCCURRED,
    }
}
