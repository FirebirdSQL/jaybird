// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * SQL function call for processing JDBC function escapes
 *
 * @author Mark Rotteveel
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
