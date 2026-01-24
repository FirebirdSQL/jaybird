// SPDX-FileCopyrightText: Copyright 2005 Michael Romankiewicz
// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.OdsVersion;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NonNull;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Extension of {@link DatabaseMetaData} interface providing access to Firebird specific features.
 *
 * @author Michael Romankiewicz
 * @author Mark Rotteveel
 */
@SuppressWarnings("unused")
public interface FirebirdDatabaseMetaData extends DatabaseMetaData {

    /**
     * Firebird procedure type is unknown (value of column {@code JB_PROCEDURE_TYPE} of
     * {@link #getProcedures(String, String, String)})
     *
     * @since 7
     */
    int jbProcedureTypeUnknown = 0;
    /**
     * Firebird procedure type is selectable (value of column {@code JB_PROCEDURE_TYPE} of
     * {@link #getProcedures(String, String, String)})
     *
     * @since 7
     */
    int jbProcedureTypeSelectable = 1;
    /**
     * Firebird procedure type is executable (value of column {@code JB_PROCEDURE_TYPE} of
     * {@link #getProcedures(String, String, String)})
     *
     * @since 7
     */
    int jbProcedureTypeExecutable = 2;

    /**
     * Get the source of a stored procedure.
     * <p>
     * <strong>WARNING</strong>: On Firebird 6.0 and higher, the sources returned are for the first procedure found
     * (with an undefined schema order!), use {@link DatabaseMetaData#getProcedures(String, String, String)} instead
     * (column {@code JB_PROCEDURE_SOURCE}).
     * </p>
     *
     * @param procedureName
     *         name of the stored procedure
     * @return source of the stored procedure, or {@code null} if not found or if the source column is {@code NULL}
     * @throws SQLException
     *         for database access errors
     * @deprecated use {@link DatabaseMetaData#getProcedures(String, String, String)}, column
     * {@code JB_PROCEDURE_SOURCE}; there are currently no plans to remove this method
     */
    @Deprecated(forRemoval = false, since = "7")
    String getProcedureSourceCode(String procedureName) throws SQLException;

    /**
     * Get the source of a trigger.
     * <p>
     * <strong>WARNING</strong>: On Firebird 6.0 and higher, the sources returned are for the first trigger found
     * (with an undefined schema order!), use {@link #getTriggerSourceCode(String, String)} instead.
     * </p>
     *
     * @param triggerName
     *         name of the trigger
     * @return source of the trigger, or {@code null} if not found or if the source column is {@code NULL}
     * @throws SQLException
     *         for database access errors
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
     * @return source of the trigger, or {@code null} if not found or if the source column is {@code NULL}
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    String getTriggerSourceCode(String schema, String triggerName) throws SQLException;

    /**
     * Get the source of a view.
     * <p>
     * On Firebird 6.0 and higher, it is recommended to use {@link #getViewSourceCode(String, String)} instead.
     * </p>
     * <p>
     * <strong>WARNING</strong>: On Firebird 6.0 and higher, the sources returned are for the first view found
     * (with an undefined schema order!), use {@link #getViewSourceCode(String, String)} instead
     * </p>
     *
     * @param viewName
     *         name of the view
     * @return source of the view, or {@code null} if not found or if the source column is {@code NULL}
     * @throws SQLException
     *         for database access errors
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
     * @return source of the view, or {@code null} if not found or if the source column is {@code NULL}
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    String getViewSourceCode(String schema, String viewName) throws SQLException;

    /**
     * Get the Firebird server version.
     *
     * @return server version object
     * @throws SQLException
     *         if a database access error occurs
     */
    GDSServerVersion getServerVersion() throws SQLException;

    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     *
     * @return The major version number of the database itself
     * @throws SQLException
     *         if a database access error occurs
     */
    default int getOdsMajorVersion() throws SQLException {
        return getOdsVersion().major();
    }

    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     *
     * @return The minor version number of the database itself
     * @throws SQLException
     *         if a database access error occurs
     */
    default int getOdsMinorVersion() throws SQLException {
        return getOdsVersion().major();
    }

    /**
     * Get the ODS (On-Disk Structure) version of the database.
     * <p>
     * This method is marked internal API as {@link OdsVersion} is internal API. We don't expect this method to be
     * removed, nor the API of {@code OdsVersion} to radically change in future versions.
     * </p>
     *
     * @return ODS version object
     * @throws SQLException
     *         if a database access error occurs
     * @since 7
     */
    @InternalApi
    OdsVersion getOdsVersion() throws SQLException;

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

    /**
     * Returns whether {@code word} is a reserved word in Firebird.
     * <p>
     * This method may be inaccurate for unsupported Firebird versions. Jaybird uses internal lists of reserved words
     * per version, and selects the closest matching version if a version is unknown. It does not use the
     * {@code RDB$KEYWORDS} table.
     * </p>
     * <p>
     * Contrary to {@link #getSQLKeywords()}, which only returns reserved words that are not also reserved in SQL:2003,
     * this method checks against all Firebird reserved words. It will return {@code false} for SQL:2003 reserved words
     * that are not reserved words in Firebird.
     * </p>
     *
     * @param word
     *         word to check against the reserved word list (case-insensitive)
     * @return {@code true} if {@code word} is a reserved word, {@code false} otherwise
     * @throws NullPointerException
     *         if {@code word} is {@code null}
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    boolean isReservedWord(@NonNull String word) throws SQLException;

}
