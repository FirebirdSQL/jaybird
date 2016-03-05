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
package org.firebirdsql.jdbc;

/**
 * Exception is thrown when trying to modify the non-updatable result set.
 */
public class FBResultSetNotUpdatableException extends FBSQLException {

    /**
     * Create default instance of this class.
     */
    public FBResultSetNotUpdatableException() {
        this("Underlying ResultSet is not updatable.");
    }

    /**
     * Create instance of this class for the specified message.
     *
     * @param message
     *         message to display.
     */
    public FBResultSetNotUpdatableException(String message) {
        super(message, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

}
