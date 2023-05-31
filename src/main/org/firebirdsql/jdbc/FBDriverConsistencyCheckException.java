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

import java.io.Serial;

/**
 * Internal driver consistency check failed. This exception is thrown when some
 * internal consistency check fails.
 *
 * @author Roman Rokytskyy
 */
public class FBDriverConsistencyCheckException extends FBSQLException {

    @Serial
    private static final long serialVersionUID = 5310800494603767792L;

    /**
     * Create instance of this exception for the specified message.
     *
     * @param message
     *         message to display.
     */
    public FBDriverConsistencyCheckException(String message) {
        super(message, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

}
