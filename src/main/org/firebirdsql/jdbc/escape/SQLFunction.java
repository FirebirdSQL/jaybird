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
package org.firebirdsql.jdbc.escape;

/**
 * SQL function call for processing JDBC function escapes
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
interface SQLFunction {

    /**
     * Render this function call with the supplied parameters.
     *
     * @param parameters
     *         Parameters for the function call.
     * @return Rendered function call, or {@code null} to fallback to server-side handling
     * @throws FBSQLParseException
     *         Optionally, if the number of parameters or values of parameters are invalid
     */
    String apply(String... parameters) throws FBSQLParseException;
}
