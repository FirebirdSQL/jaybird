/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.util.InternalApi;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extension of {@link Connection} interface providing access to Firebird specific features.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdConnection extends Connection {

    @Deprecated
    int TPB_READ_COMMITTED = TpbItems.isc_tpb_read_committed;
    @Deprecated
    int TPB_CONCURRENCY = TpbItems.isc_tpb_concurrency;
    @Deprecated
    int TPB_CONSISTENCY = TpbItems.isc_tpb_consistency;

    @Deprecated
    int TPB_READ = TpbItems.isc_tpb_read;
    @Deprecated
    int TPB_WRITE = TpbItems.isc_tpb_write;

    @Deprecated
    int TPB_WAIT = TpbItems.isc_tpb_wait;
    @Deprecated
    int TPB_NOWAIT = TpbItems.isc_tpb_nowait;

    @Deprecated
    int TPB_REC_VERSION = TpbItems.isc_tpb_rec_version;
    @Deprecated
    int TPB_NO_REC_VERSION = TpbItems.isc_tpb_no_rec_version;

    /**
     * {@inheritDoc}
     *
     * @return instance of {@link FirebirdBlob}.
     */
    Blob createBlob() throws SQLException;

    /**
     * Get current ISC encoding.
     *
     * @return current ISC encoding.
     * @deprecated Will be removed in Jaybird 6
     */
    @Deprecated
    String getIscEncoding() throws SQLException;

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
    @Deprecated
    void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException;

    /**
     * Get transaction parameters for the specified transaction isolation level.
     *
     * @param isolationLevel
     *         isolation level defined in the {@link Connection} interface.
     * @return instance of {@link TransactionParameterBuffer} containing current transaction parameters.
     * @throws SQLException
     *         if error occurred obtaining transaction parameters.
     */
    TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException;

    /**
     * Create new instance of {@link TransactionParameterBuffer}.
     *
     * @return empty instance of {@link TransactionParameterBuffer}.
     * @throws SQLException
     *         if error occurred during this operation.
     */
    TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException;

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * <p>
     * This method replaces the default TPB mapping with the specified one,
     * changes will be effective from the next transaction start.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with parameters
     *         to set.
     * @throws SQLException
     *         if error occurred during this operation.
     */
    void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Set transaction parameters for the next transactions.
     * <p>
     * This method does not change the TPB mapping, but replaces the mapping for the current transaction isolation
     * until {@link Connection#setTransactionIsolation(int)} is called.
     * </p>
     * <p>
     * Method cannot be called when transaction has already started.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with new
     *         transaction parameters.
     * @throws SQLException
     *         if method is called within a transaction.
     */
    void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * @return {@code true} if this connection is configured to use {@code isc_tpb_autocommit} when in auto commit.
     */
    boolean isUseFirebirdAutoCommit();

    /**
     * Provides access to the low-level connection handle.
     * <p>
     * <b>WARNING</b> using this connection handle directly may bring the JDBC connection in an inconsistent state.
     * </p>
     *
     * @return The low-level connection handle.
     */
    @InternalApi
    FbDatabase getFbDatabase() throws SQLException;

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
     * @since 5.0.12
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
     * @since 5.0.12
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
     * @since 5.0.12
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
     * @since 5.0.12
     */
    boolean isSimpleIdentifier(String identifier) throws SQLException;

}