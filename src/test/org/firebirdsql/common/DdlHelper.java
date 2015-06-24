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
package org.firebirdsql.common;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Helper class for executing DDL while ignoring certain errors.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class DdlHelper {

    private DdlHelper() {
    }

    /**
     * Helper method for executing CREATE TABLE, ignoring <code>isc_no_meta_update</code> errors.
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         Create table statement
     * @throws SQLException
     *         SQLException for executing statement, except if there error is <code>isc_no_meta_update</code>
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeCreateTable(Connection connection, String sql) throws SQLException {
        DdlHelper.executeDDL(connection, sql, isc_no_meta_update);
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         DDL statement
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         <code>ignoreErrors</code>
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(Connection connection, String sql, int... ignoreErrors) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } catch(SQLException ex) {
            if (ignoreErrors == null || ignoreErrors.length == 0)
                throw ex;

            boolean ignoreException = false;

            int errorCode = ex.getErrorCode();
            Throwable current = ex;
            errorcodeloop: do {
                for (int ignoreError : ignoreErrors) {
                    if (ignoreError == errorCode) {
                        ignoreException = true;
                        break errorcodeloop;
                    }
                }
                if (current instanceof GDSException) {
                    current = ((GDSException)current).getNext();
                } else {
                    current = current.getCause();
                }
                if (current == null || !(current instanceof GDSException)) {
                    break;
                } else {
                    errorCode = ((GDSException)current).getFbErrorCode();
                }
            } while (errorCode != -1);

            if (!ignoreException)
                throw ex;
        }
    }

    /**
     * Helper method for executing DROP TABLE, ignoring errors if the table or view doesn't exist.
     * <p>
     * Ignored errors are:
     * <ul>
     *     <li><code>isc_no_meta_update</code></li>
     *     <li><code>isc_dsql_table_not_found</code></li>
     *     <li><code>isc_dsql_view_not_found</code></li>
     *     <li><code>isc_dsql_error</code> (Firebird 1.5 or earlier only)</li>
     * </ul>
     * </p>
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         Drop table statement
     * @throws SQLException
     *         SQLException for executing statement, except for the listed errors.
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeDropTable(Connection connection, String sql) throws SQLException {
        executeDDL(connection, sql, DdlHelper.getDropIgnoreErrors(connection));
    }

    private static int[] getDropIgnoreErrors(Connection connection) throws SQLException {
        try {
            if (connection instanceof FBConnection) {
                GDSHelper gdsHelper = ((FBConnection) connection).getGDSHelper();
                if (gdsHelper.compareToVersion(2, 0) >= 0) {
                    return new int[] { isc_no_meta_update, isc_dsql_table_not_found, isc_dsql_view_not_found };
                }
            }
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
        // Firebird 1.5 and earlier do not always return specific error codes
        return new int[] { isc_dsql_error, isc_no_meta_update, isc_dsql_table_not_found, isc_dsql_view_not_found };
    }

}
