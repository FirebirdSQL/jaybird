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
package org.firebirdsql.gds;

import java.sql.SQLException;

/**
 * Notification listener for exceptions.
 * <p>
 * TODO: consider deprecating/removal
 * </p>
 */
public interface ExceptionListener {

    /**
     * Notify about a GDSException.
     *
     * @param ex
     *         error that occurred.
     */
    @Deprecated
    void errorOccurred(GDSException ex);

    /**
     * Notify about a SQLException
     *
     * @param ex
     *         error that occurred.
     */
    void errorOccurred(SQLException ex);

    /**
     * Listener that does nothing.
     */
    static final ExceptionListener NULL_LISTENER = new ExceptionListener() {
        @Override
        public void errorOccurred(GDSException ex) { }

        @Override
        public void errorOccurred(SQLException ex) { }
    };
}
