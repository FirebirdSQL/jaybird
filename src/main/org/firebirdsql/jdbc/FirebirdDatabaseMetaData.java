// SPDX-FileCopyrightText: Copyright 2005 Michael Romankiewicz
// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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
     * 
     * @param procedureName
     *            name of the stored procedure.
     * @return source of the stored procedure.
     * @throws SQLException
     *             if specified procedure cannot be found.
     */
    String getProcedureSourceCode(String procedureName) throws SQLException;

    /**
     * Get the source of a trigger.
     * 
     * @param triggerName
     *            name of the trigger.
     * @return source of the trigger.
     * @throws SQLException
     *             if specified trigger cannot be found.
     */
    String getTriggerSourceCode(String triggerName) throws SQLException;

    /**
     * Get the source of a view.
     * 
     * @param viewName
     *            name of the view.
     * @return source of the view.
     * @throws SQLException
     *             if specified view cannot be found.
     */
    String getViewSourceCode(String viewName) throws SQLException;
    
    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     * 
     * @return The major version number of the database itself
     * @exception SQLException if a database access error occurs
     */
    int getOdsMajorVersion() throws SQLException;
    
    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     * 
     * @return The minor version number of the database itself
     * @exception SQLException if a database access error occurs
     */
    int getOdsMinorVersion() throws SQLException;

    /**
     * Get the dialect of the database.
     *
     * @return The dialect of the database
     * @throws SQLException if a database access error occurs
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
     * @throws SQLException if a database access error occurs
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
     * @since 4.0
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

}
