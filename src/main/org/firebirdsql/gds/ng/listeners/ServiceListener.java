// SPDX-FileCopyrightText: Copyright 2013-2016 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbService;

import java.sql.SQLWarning;

/**
 * Listener for service events.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ServiceListener {

    /**
     * Called before the {@code service} will be detached.
     * <p>
     * This event is intended for cleanup action, implementer should take care that
     * no exceptions are thrown from this method.
     * </p>
     *
     * @param service
     *         The service object that is detaching
     */
    void detaching(FbService service);

    /**
     * Called when the {@code service} connection has been detached
     *
     * @param service
     *         The database object that was detached
     */
    void detached(FbService service);

    /**
     * Called when a warning was received for the {@code service} connection.
     * <p>
     * In implementation it is possible that some warnings are not sent to listeners on the database, but only to
     * listeners on specific connection derived objects.
     * </p>
     *
     * @param service
     *         service receiving the warning
     * @param warning
     *         Warning
     */
    void warningReceived(FbService service, SQLWarning warning);
}
