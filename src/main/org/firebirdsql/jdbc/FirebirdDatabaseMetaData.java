// SPDX-FileCopyrightText: Copyright 2005 Michael Romankiewicz
// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Extension of {@link DatabaseMetaData} interface providing access to Firebird
 * specific features.
 *
 * @author Michael Romankiewicz
 */
@SuppressWarnings("unused")
public interface FirebirdDatabaseMetaData extends DatabaseMetaData {

    /**
     * Get the source of a stored procedure.
     * <p>
     * On Firebird 6.0 and higher, it is recommended to use {@link #getProcedureSourceCode(String, String)} instead.
     * </p>
     *
     * @param procedureName
     *         name of the stored procedure
     * @return source of the stored procedure
     * @throws SQLException
     *         if specified procedure cannot be found
     * @see #getProcedureSourceCode(String, String)
     */
    String getProcedureSourceCode(String procedureName) throws SQLException;

    /**
     * Get the source of a stored procedure.
     *
     * @param schema
     *         schema of the stored procedure ({@code null} drops the schema from the search; ignored on Firebird 5.0
     *         and older)
     * @param procedureName
     *         name of the stored procedure
     * @return source of the stored procedure
     * @throws SQLException
     *         if specified procedure cannot be found
     * @since 7
     */
    String getProcedureSourceCode(String schema, String procedureName) throws SQLException;

    /**
     * Get the source of a trigger.
     * <p>
     * On Firebird 6.0 and higher, it is recommended to use {@link #getTriggerSourceCode(String, String)} instead.
     * </p>
     *
     * @param triggerName
     *         name of the trigger
     * @return source of the trigger
     * @throws SQLException
     *         if specified trigger cannot be found
     * @see #getTriggerSourceCode(String, String)
     */
    String getTriggerSourceCode(String triggerName) throws SQLException;

    /**
     * Get the source of a trigger.
     *
     * @param schema
     *         schema of the trigger ({@code null} drops the schema from the search; ignored on Firebird 5.0 and older)
     * @param triggerName
     *         name of the trigger
     * @return source of the trigger
     * @throws SQLException
     *         if specified trigger cannot be found
     * @since 7
     */
    String getTriggerSourceCode(String schema, String triggerName) throws SQLException;

    /**
     * Get the source of a view.
     * <p>
     * On Firebird 6.0 and higher, it is recommended to use {@link #getViewSourceCode(String, String)} instead.
     * </p>
     *
     * @param viewName
     *         name of the view
     * @return source of the view
     * @throws SQLException
     *         if specified view cannot be found
     * @see #getViewSourceCode(String, String)
     */
    String getViewSourceCode(String viewName) throws SQLException;

    /**
     * Get the source of a view.
     *
     * @param schema
     *         schema of the trigger ({@code null} drops the schema from the search; ignored on Firebird 5.0 and older)
     * @param viewName
     *         name of the view
     * @return source of the view
     * @throws SQLException
     *         if specified view cannot be found
     * @since 7
     */
    String getViewSourceCode(String schema, String viewName) throws SQLException;

    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     *
     * @return The major version number of the database itself
     * @throws SQLException
     *         if a database access error occurs
     */
    int getOdsMajorVersion() throws SQLException;

    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     *
     * @return The minor version number of the database itself
     * @throws SQLException
     *         if a database access error occurs
     */
    int getOdsMinorVersion() throws SQLException;

    /**
     * Get the dialect of the database.
     *
     * @return The dialect of the database
     * @throws SQLException
     *         if a database access error occurs
     * @see #getConnectionDialect()
     */
    int getDatabaseDialect() throws SQLException;

    /**
     * Get the dialect of the connection.
     * <p>
     * The connection dialect may be different from the database dialect.
     * </p>
     *
     * @return The dialect of the connection
     * @throws SQLException
     *         if a database access error occurs
     * @see #getDatabaseDialect()
     */
    int getConnectionDialect() throws SQLException;

    /**
     * Closes any cached metadata statements held by this database metadata implementation.
     * <p>
     * The database metadata object itself remains usable. Exceptions during statement close are logged and suppressed.
     * </p>
     */
    void close();

    /**
     * Supported table type names.
     *
     * @return An array with the supported table types names for {@link #getTables(String, String, String, String[])}
     * @throws SQLException
     *         For problems determining supported table types
     * @see #getTableTypes()
     * @since 4
     */
    String[] getTableTypeNames() throws SQLException;

    /***
     * The default maximum identifier length.
     * <p>
     * NOTE: This method reports the standard maximum length, and does not take into account restrictions configured
     * through {@code MaxIdentifierByteLength} or {@code MaxIdentifierCharLength}.
     * </p>
     *
     * @return the (default) maximum identifier length
     */
    int getMaxObjectNameLength() throws SQLException;

    /**
     * Attempts to find the schema of {@code tableName} on the current search path.
     * <p>
     * On Firebird versions that support schemas, this will return either a non-empty optional with the first schema
     * containing {@code tableName}, or an empty optional if {@code tableName} was not found in the schemas on
     * the search path.
     * </p>
     * <p>
     * On Firebird versions that do not support schemas, this will <strong>always</strong> return a non-empty optional
     * with an empty string ({@code ""}), meaning <em>&quot;table has no schema&quot;</em>. This is an analogue to
     * the meaning of empty string for {@code schema} or {@code schemaPattern} in other {@link DatabaseMetaData}
     * methods. It will not query the server to check for existence of the table.
     * </p>
     *
     * @param tableName
     *         table name, matching exactly as stored in the metadata (not a like-pattern)
     * @return the first schema name of the search path containing {@code tableName}, or empty string ({@code ""}) if
     * schemas are not supported; returns an empty optional if schemas are supported, but {@code tableName} was not
     * found on the search path
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    Optional<String> findTableSchema(String tableName) throws SQLException;

}
