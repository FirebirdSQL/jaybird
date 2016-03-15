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
package org.firebirdsql.gds;

import java.sql.SQLException;

/**
 * Notification listener for exceptions.
 *
 * @deprecated Use {@link org.firebirdsql.gds.ng.listeners.ExceptionListener}, interface will be removed in 3.1
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface ExceptionListener extends org.firebirdsql.gds.ng.listeners.ExceptionListener {

    /**
     * Notify about a GDSException.
     *
     * @param ex
     *         error that occurred.
     * @deprecated this method is never called
     */
    @Deprecated
    void errorOccurred(GDSException ex);

    /**
     * Listener that does nothing.
     */
    ExceptionListener NULL_LISTENER = new ExceptionListener() {
        @Override
        public void errorOccurred(GDSException ex) {
        }

        @Override
        public void errorOccurred(Object source, SQLException ex) {
        }
    };
}
