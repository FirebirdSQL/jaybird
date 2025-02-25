// SPDX-FileCopyrightText: Copyright 2016-2020 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

import java.sql.SQLException;

/**
 * Listener for notifications of SQL Exceptions that occurred in the object listened on.
 * <p>
 * The primary use case of this interface is to bridge the gap between the XCA managed connection or connection
 * pools that need to detect fatal errors. In the implementation only the methods defined in the various {@code Fb*}
 * interfaces in {@code org.firebirdsql.gds.ng} are required to notify the listeners.
 * </p>
 * <p>
 * Listeners registered on a {@link org.firebirdsql.gds.ng.FbDatabase} or {@link org.firebirdsql.gds.ng.FbService} will
 * also be notified of errors occurring in subordinate objects (eg statements).
 * </p>
 * <p>
 * It is possible that a single exception is notified multiple times. Listeners should be prepared to handle this
 * appropriately.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ExceptionListener {

    /**
     * Notify about a SQLException
     *
     * @param source
     *         The source of the event; note for caller: this should be the object this listener is registered at.
     * @param ex
     *         error that occurred.
     */
    void errorOccurred(Object source, SQLException ex);

}
