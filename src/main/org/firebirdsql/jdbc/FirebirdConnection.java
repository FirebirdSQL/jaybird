// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jaybird.util.SearchPathHelper;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Extension of the {@link java.sql.Connection} interface providing access to Firebird specific features.
 *
 * @author Roman Rokytskyy
 * @since 1.5
 */
public interface FirebirdConnection extends java.sql.Connection {

    /**
     * {@inheritDoc}
     *
     * @return instance of {@link FirebirdBlob}.
     */
    Blob createBlob() throws SQLException;

    /**
     * Set transaction parameters for the specified isolation level. They will
     * take effect only on the newly started transaction.
     *
     * @param isolationLevel
     *         JDBC isolation level.
     * @param parameters
     *         array of TPB parameters, see all TPB_* constants.
     * @throws SQLException
     *         if specified transaction parameters cannot be set.
     * @deprecated use {@link #setTransactionParameters(int, TransactionParameterBuffer)} instead.
     */
    @Deprecated(since = "2")
    void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException;

    /**
     * Get transaction parameters for the specified transaction isolation level.
     *
     * @param isolationLevel
     *         isolation level defined in the {@link java.sql.Connection} interface
     * @return instance of {@link TransactionParameterBuffer} containing current transaction parameters
     * @throws SQLException
     *         if an error occurred obtaining transaction parameters
     * @since 2
     */
    // TODO The nullability is questionable and might need to be defined to @NonNull
    @Nullable TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException;

    /**
     * Create new instance of {@link TransactionParameterBuffer}.
     *
     * @return empty instance of {@link TransactionParameterBuffer}.
     * @throws SQLException
     *         if error occurred during this operation.
     * @since 2
     */
    TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException;

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * <p>
     * This method replaces the default TPB mapping with the specified one, changes will be effective from the next
     * transaction start.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with parameters to set.
     * @throws SQLException
     *         if error occurred during this operation.
     * @since 2
     */
    void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Set transaction parameters for the next transactions.
     * <p>
     * This method does not change the TPB mapping, but replaces the mapping for the current transaction isolation
     * until {@link java.sql.Connection#setTransactionIsolation(int)} is called.
     * </p>
     * <p>
     * This method cannot be called when a transaction is currently active.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with new transaction parameters
     * @throws SQLException
     *         if this method is called within a transaction
     * @since 2
     */
    void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * @return {@code true} if this connection is configured to use {@code isc_tpb_autocommit} when in auto commit.
     * @since 3
     */
    boolean isUseFirebirdAutoCommit();

    /**
     * Provides access to the low-level connection handle.
     * <p>
     * <b>WARNING</b> using this connection handle directly may bring the JDBC connection in an inconsistent state.
     * </p>
     *
     * @return The low-level connection handle.
     * @since 3
     */
    @InternalApi
    FbDatabase getFbDatabase() throws SQLException;

    /**
     * Resets the known client info properties of this connection to the defaults. This does not reset the values of
     * those properties on the server, but only resets the list of known properties held by this connection
     * <p>
     * If this connection is closed, this is effectively a no-op. Primary use-case for this method is to reset a
     * connection held in a connection pool.
     * </p>
     *
     * @since 6
     */
    void resetKnownClientInfoProperties();

    /**
     * Sets the search path as if executing {@code SET SEARCH_PATH TO ...}.
     *
     * @param searchPath
     *         comma-separated search path (names must be correctly quoted &mdash; if needed)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if the server does not support schemas (Firebird 5.0 or older)
     * @throws SQLException
     *         if {@code schemas} is null or blank, or for database access errors
     * @see #getSearchPath()
     * @see #setSearchPathList(List)
     * @see #setSchema(String)
     * @since 7
     */
    void setSearchPath(String searchPath) throws SQLException;

    /**
     * Returns the schema search path.
     *
     * @return comma-separated list of quoted schema names of the search path, or {@code null} if schemas are not
     * supported
     * @throws SQLException
     *         if the connections is closed, or for database access errors
     * @see #setSearchPath(String)
     * @see #getSearchPathList()
     * @since 7
     */
    @Nullable String getSearchPath() throws SQLException;

    /**
     * Sets the search path as if executing {@code SET SEARCH_PATH TO ...}.
     *
     * @param schemas
     *         schemas to set as search path (names must be unquoted)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if the server does not support schemas (Firebird 5.0 or older)
     * @throws SQLException
     *         if {@code schemas} is empty, or for database access errors
     * @see #setSearchPathList(List)
     * @see #getSearchPathList()
     * @see #setSearchPath(String)
     * @see #setSchema(String)
     * @since 7
     */
    default void setSearchPathList(String... schemas) throws SQLException {
        setSearchPathList(Arrays.asList(schemas));
    }

    /**
     * Sets the search path as if executing {@code SET SEARCH_PATH TO ...}.
     *
     * @param schemas
     *         schemas to set as search path (names must be unquoted)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if the server does not support schemas (Firebird 5.0 or older)
     * @throws SQLException
     *         if {@code schemas} is empty, or for database access errors
     * @see #setSearchPathList(List)
     * @see #getSearchPathList()
     * @see #setSearchPath(String)
     * @see #setSchema(String)
     * @since 7
     */
    void setSearchPathList(List<String> schemas) throws SQLException;

    /**
     * Returns the schema search path as a list of unquoted schema names.
     *
     * @return list of unquoted schema names, or an empty list if schemas are not supported
     * @throws SQLException
     *         if the connection is closed, or for database access errors
     * @see #setSearchPathList(String...)
     * @see #setSearchPathList(List)
     * @see #getSearchPath()
     * @since 7
     */
    default List<String> getSearchPathList() throws SQLException {
        return SearchPathHelper.parseSearchPath(getSearchPath());
    }

    // TODO: If and when below JDBC 4.5 methods are removed from this interface, we may need to move parts of their
    //  descriptions to FBConnection

    /**
     * Returns a string appropriately quoted as a string literal for the connection dialect.
     * <p>
     * This method is defined in {@link java.sql.Connection} starting with JDBC 4.5 (Java 26). The definition in this
     * interface may be removed without notice once Jaybird only supports Java versions that expect JDBC 4.5 or higher.
     * </p>
     *
     * @param val
     *         a character string
     * @return for dialect 3, a string enclosed by single quotes with every single quote converted to two single quotes,
     * for dialect 1, with double quotes instead of single quotes.
     * @throws NullPointerException
     *         if {@code val} is {@code null}
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    String enquoteLiteral(String val) throws SQLException;

    /**
     * Returns a string appropriately quoted as a string literal for the connection dialect.
     * <p>
     * Implementations should call their implementation of {@link #enquoteLiteral(String)}. Given the future removal
     * of this method from this interface, we're not providing a default implementation in this interface. Contrary
     * to the requirements stated in JDBC 4.5, the returned string is <strong>not</strong> prefixed with {@code N} as
     * Firebird doesn't have NCHAR literals.
     * </p>
     * <p>
     * This method is defined in {@link java.sql.Connection} starting with JDBC 4.5 (Java 26). The definition in this
     * interface may be removed without notice once Jaybird only supports Java versions that expect JDBC 4.5 or higher.
     * </p>
     *
     * @param val
     *         a character string
     * @return for dialect 3, a string enclosed by single quotes with every single quote converted to two single quotes,
     * for dialect 1, with double quotes instead of single quotes.
     * @throws NullPointerException
     *         if {@code val} is {@code null}
     * @throws SQLException
     *         for database access errors
     * @see #enquoteLiteral(String)
     * @since 7
     */
    String enquoteNCharLiteral(String val) throws SQLException;

    /**
     * Returns a simple SQL identifier or a delimited identifier, as appropriate for the connection dialect.
     * <p>
     * For dialect 3, if {@code identifier} already starts and ends in a double quote, we strip the quotes, unescape
     * doubled double quotes, and requote and reescape. Reserved words known to Jaybird are not considered simple
     * identifiers, and are always delimited.
     * </p>
     * <p>
     * For dialect 1, if {@code identifier} is not a simple identifier or if {@code alwaysDelimit} is {@code true},
     * this method will throw a {@link java.sql.SQLFeatureNotSupportedException} as dialect 1 does not support delimited
     * identifiers.
     * </p>
     * <p>
     * This method is defined in {@link java.sql.Connection} starting with JDBC 4.5 (Java 26). The definition in this
     * interface may be removed without notice once Jaybird only supports Java versions that expect JDBC 4.5 or higher.
     * </p>
     *
     * @param identifier
     *         a SQL identifier
     * @param alwaysDelimit
     *         indicates if a simple SQL identifier should be returned as a delimited identifier
     * @return a simple SQL identifier or a delimited identifier
     * @throws NullPointerException
     *         if {@code identifier} is {@code null}
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if the datasource does not support delimited identifiers and {@code identifier} is not a simple
     *         identifier or {@code alwaysDelimit} is {@code true}
     * @throws SQLException
     *         if {@code identifier} is not a valid identifier
     * @see #isSimpleIdentifier(String)
     * @since 7
     */
    String enquoteIdentifier(String identifier, boolean alwaysDelimit) throws SQLException;

    /**
     * Returns whether {@code identifier} is a simple identifier.
     * <p>
     * Reserved words known to Jaybird are not considered simple identifiers.
     * </p>
     * <p>
     * This method is defined in {@link java.sql.Connection} starting with JDBC 4.5 (Java 26). The definition in this
     * interface may be removed without notice once Jaybird only supports Java versions that expect JDBC 4.5 or higher.
     * </p>
     *
     * @param identifier
     *         a SQL identifier
     * @return {@code true} if a simple SQL identifier, {@code false} otherwise
     * @throws NullPointerException
     *         if {@code identifier} is {@code null}
     * @throws SQLException
     *         for database access errors
     * @since 7
     */
    boolean isSimpleIdentifier(String identifier) throws SQLException;

}