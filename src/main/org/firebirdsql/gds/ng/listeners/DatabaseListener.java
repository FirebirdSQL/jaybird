// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbDatabase;

import java.sql.SQLWarning;

/**
 * Listener for database events.
 * <p>
 * All listener methods have a default implementation that does nothing.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface DatabaseListener {

    /**
     * Called before the {@code database} will be detached.
     * <p>
     * This event is intended for cleanup action, implementer should take care that
     * no exceptions are thrown from this method.
     * </p>
     *
     * @param database
     *         The database object that is detaching
     */
    default void detaching(FbDatabase database) { }

    /**
     * Called when the {@code database} connection has been detached
     *
     * @param database
     *         The database object that was detached
     */
    default void detached(FbDatabase database) { }

    /**
     * Called when a warning was received for the {@code database} connection.
     * <p>
     * In implementation, it is possible that some warnings are not sent to listeners on the database, but only to
     * listeners on specific connection derived objects (like an {@link org.firebirdsql.gds.ng.FbStatement}
     * implementation).
     * </p>
     *
     * @param database
     *         Database receiving the warning
     * @param warning
     *         Warning
     */
    default void warningReceived(FbDatabase database, SQLWarning warning) { }
}
