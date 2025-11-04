// SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.firebirdsql.gds.ISCConstants;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Helper class for executing DDL while ignoring certain errors.
 *
 * @author Mark Rotteveel
 */
public final class DdlHelper {

    private DdlHelper() {
    }

    /**
     * Helper method for executing CREATE TABLE, ignoring {@code isc_no_meta_update} errors.
     *
     * @param connection
     *         connection to execute {@code sql}
     * @param sql
     *         create table statement
     * @throws SQLException
     *         SQLException for executing statement, except if the error is {@code isc_no_meta_update}
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeCreateTable(final Connection connection, final String sql) throws SQLException {
        executeDDL(connection, sql, ISCConstants.isc_no_meta_update);
    }

    /**
     * Helper method for executing CREATE TABLE, ignoring {@code isc_no_meta_update} errors.
     *
     * @param statement
     *         statement to execute {@code sql}
     * @param sql
     *         create table statement
     * @throws SQLException
     *         SQLException for executing statement, except if the error is {@code isc_no_meta_update}
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeCreateTable(final Statement statement, final String sql) throws SQLException {
        executeDDL(statement, sql, ISCConstants.isc_no_meta_update);
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param connection
     *         connection to execute {@code sql}
     * @param sql
     *         DDL statement
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         {@code ignoreErrors}
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(final Connection connection, final String sql, final int... ignoreErrors)
            throws SQLException {
        executeDDL(connection, List.of(sql), ignoreErrors);
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param connection
     *         connection to execute {@code sql}
     * @param sql
     *         DDL statements
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         {@code ignoreErrors}
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(final Connection connection, final Collection<String> sql, final int... ignoreErrors)
            throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            executeDDL(stmt, sql, ignoreErrors);
        }
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param statement
     *         statement to execute {@code sql}
     * @param sql
     *         DDL statement
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         {@code ignoreErrors}
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(final Statement statement, final String sql, final int... ignoreErrors)
            throws SQLException {
        executeDDL(statement, List.of(sql), ignoreErrors);
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param statement
     *         statement to execute {@code sql}
     * @param sql
     *         DDL statements
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         {@code ignoreErrors}
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(final Statement statement, final Collection<String> sql, final int... ignoreErrors)
            throws SQLException {
        if (ignoreErrors != null) {
            Arrays.sort(ignoreErrors);
        }
        Connection connection = statement.getConnection();
        final boolean autoCommitAtStart = connection.getAutoCommit();
        if (autoCommitAtStart && sql.size() > 1) {
            connection.setAutoCommit(false);
        }
        try {
            for (String currentSql : sql) {
                executeDDL0(statement, currentSql, ignoreErrors);
            }
            if (!autoCommitAtStart) {
                connection.commit();
            }
        } finally {
            // if we were not in auto commit at start and an exception occurred, the transaction will still be pending
            if (autoCommitAtStart) {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void executeDDL0(Statement statement, String sql, int[] ignoreErrors) throws SQLException {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            if (ignoreErrors == null || ignoreErrors.length == 0)
                throw e;

            for (Throwable current : e) {
                if (current instanceof SQLException currentSqle
                        && Arrays.binarySearch(ignoreErrors, currentSqle.getErrorCode()) >= 0) {
                    return;
                }
            }

            throw e;
        }
    }

    /**
     * Helper method for executing DROP TABLE, ignoring errors if the table or view doesn't exist.
     * <p>
     * Ignored errors are:
     * <ul>
     *     <li>{@code isc_no_meta_update}</li>
     *     <li>{@code isc_dsql_table_not_found}</li>
     *     <li>{@code isc_dsql_view_not_found}</li>
     *     <li>{@code isc_dsql_error} (Firebird 1.5 or earlier only)</li>
     * </ul>
     * </p>
     *
     * @param connection
     *         connection to execute {@code sql}
     * @param sql
     *         Drop table statement
     * @throws SQLException
     *         SQLException for executing statement, except for the listed errors.
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeDropTable(final Connection connection, final String sql) throws SQLException {
        executeDDL(connection, sql, getDropIgnoreErrors(connection));
    }

    /**
     * Helper method for executing DROP TABLE, ignoring errors if the table or view doesn't exist.
     * <p>
     * Ignored errors are:
     * <ul>
     *     <li>{@code isc_no_meta_update}</li>
     *     <li>{@code isc_dsql_table_not_found}</li>
     *     <li>{@code isc_dsql_view_not_found}</li>
     *     <li>{@code isc_dsql_error} (Firebird 1.5 or earlier only)</li>
     * </ul>
     * </p>
     *
     * @param statement
     *         statement to execute {@code sql}
     * @param sql
     *         drop table statement
     * @throws SQLException
     *         SQLException for executing statement, except for the listed errors.
     * @see #executeDDL(java.sql.Statement, String, int...)
     */
    public static void executeDropTable(final Statement statement, final String sql) throws SQLException {
        executeDDL(statement, sql, getDropIgnoreErrors(statement.getConnection()));
    }

    private static int[] getDropIgnoreErrors(final Connection connection) {
        if (supportInfoFor(connection).isVersionEqualOrAbove(2, 0)) {
            return new int[] { ISCConstants.isc_no_meta_update, ISCConstants.isc_dsql_table_not_found,
                    ISCConstants.isc_dsql_view_not_found };
        } else {
            // Firebird 1.5 and earlier do not always return specific error codes
            return new int[] { ISCConstants.isc_dsql_error, ISCConstants.isc_no_meta_update,
                    ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found };
        }
    }

}
