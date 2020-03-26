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
package org.firebirdsql.jdbc;

import java.sql.SQLException;

public class FBSQLException extends SQLException {

    private static final long serialVersionUID = 8157410954186424083L;

    public FBSQLException(Exception ex) {
        this("Exception. " + ex.getMessage());
        initCause(ex);
    }

    public FBSQLException(String message) {
        super(message, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     *
     * @param message
     *            Exception message
     * @param ex
     *            SQLException that should be set as the 'next exception'
     * @deprecated In all most all cases use
     *             {@link #FBSQLException(String, String)} in combination with
     *             {@link #setNextException(SQLException)}.
     */
    @Deprecated
    public FBSQLException(String message, SQLException ex) {
        this(message);
        setNextException(ex);
    }

    /**
     *
     * @param message
     *            Exception message
     * @param sqlState
     *            SQL State for this exception. Replaced with
     *            {@link SQLStateConstants#SQL_STATE_GENERAL_ERROR} if null
     */
    public FBSQLException(String message, String sqlState) {
        super(message, defaultSQLStateIfNull(sqlState));
    }

    /**
     * @deprecated use {@link #getCause()} instead.
     */
    @Deprecated
    public Exception getInternalException() {
        return (Exception) getCause();
    }

    /**
     * @param sqlState
     *            SQL State value (or null)
     * @return The passed sqlState or
     *         {@link SQLStateConstants#SQL_STATE_GENERAL_ERROR} if sqlState is
     *         null.
     */
    public static String defaultSQLStateIfNull(String sqlState) {
        return sqlState != null ? sqlState : SQLStateConstants.SQL_STATE_GENERAL_ERROR;
    }
}
