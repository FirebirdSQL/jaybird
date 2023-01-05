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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jdbc.FBDriverNotCapableException;

import java.sql.SQLException;
import java.util.Collection;

/**
 * API for statement handles.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface FbStatement extends ExceptionListenable, AutoCloseable {

    /**
     * @return Transaction currently associated with this statement
     */
    FbTransaction getTransaction();

    /**
     * @return The database connection that created this statement
     */
    FbDatabase getDatabase();

    /**
     * Associates a transaction with this statement
     *
     * @param transaction
     *         The transaction
     */
    void setTransaction(FbTransaction transaction) throws SQLException;

    /**
     * @return descriptor of the parameters of this statement
     */
    RowDescriptor getParameterDescriptor();

    /**
     * @return descriptor of the row returned by this statement
     */
    RowDescriptor getRowDescriptor();

    /**
     * @return The statement type
     */
    StatementType getType();

    /**
     * @return The current state of this statement
     */
    StatementState getState();

    /**
     * @return The Firebird statement handle identifier
     */
    int getHandle();

    /**
     * Close and deallocate this statement.
     */
    @Override
    void close() throws SQLException;

    /**
     * Closes the cursor associated with this statement, leaving the
     * statement itself allocated.
     * <p>
     * Equivalent to calling {@link #closeCursor(boolean)} with {@code false}.
     * </p>
     */
    void closeCursor() throws SQLException;

    /**
     * Closes the cursor associated with this statement, leaving the statement itself allocated.
     * <p>
     * When this method is called in preparation of a commit, rollback or another operation which will close the cursor
     * (see {@code transactionEnd}), then implementations may opt to not close the cursor on the server as the server
     * closes the cursor automatically, or the statement as a whole is closed by the implementation.
     * </p>
     *
     * @param transactionEnd
     *         close is in response to a transaction end or another operation which will close the cursor
     */
    void closeCursor(boolean transactionEnd) throws SQLException;

    /**
     * Prepare the statement text.
     * <p>
     * If this handle is in state {@link StatementState#NEW} then it will first allocate the statement.
     * </p>
     *
     * @param statementText
     *         Statement text
     * @throws SQLException
     *         If a database access error occurs, or this statement is currently executing a query.
     */
    void prepare(String statementText) throws SQLException;

    /**
     * Attempts to unprepare the currently prepared statement.
     * <p>
     * For Firebird versions that do not support {@code DSQL_unprepare}, the implementation should attempt to close the
     * cursor (using {@link #closeCursor()}).
     * </p>
     *
     * @throws SQLException
     *         If a database access error occurs
     */
    void unprepare() throws SQLException;

    /**
     * Validates if the number of parameters matches the expected number and types, and if all values have been set.
     *
     * @param parameters
     *         Parameter values to validate
     * @throws SQLException
     *         When the number or type of parameters does not match {@link #getParameterDescriptor()}, or when a
     *         parameter has not been set.
     */
    void validateParameters(final RowValue parameters) throws SQLException;

    /**
     * Execute the statement.
     *
     * @param parameters
     *         The list of parameter values to use for execution.
     * @throws SQLException
     *         When the number of type of parameters does not match the types returned by {@link #getParameterDescriptor()},
     *         a parameter value was not set, or when an error occurred executing this statement.
     */
    void execute(RowValue parameters) throws SQLException;

    /**
     * Requests this statement to fetch the next {@code fetchSize} rows.
     * <p>
     * Fetched rows are not returned from this method, but sent to the registered {@link org.firebirdsql.gds.ng.listeners.StatementListener} instances.
     * </p>
     *
     * @param fetchSize
     *         Number of rows to fetch (must be greater than {@code 0})
     * @throws SQLException
     *         For database access errors, when called on a closed statement, when no cursor is open or when the fetch
     *         size is not greater than {@code 0}.
     */
    void fetchRows(int fetchSize) throws SQLException;

    /**
     * Requests this statement to fetch rows using the specified fetch type.
     * <p>
     * The default implementation only supports {@link FetchType#NEXT} by redirecting to {@link #fetchRows(int)} and
     * throws an {@code SQLFeatureNotSupported} for other types.
     * </p>
     * <p>
     * The caller is responsible for tracking and correcting for server-side positional state, taking into account any
     * rows already fetched. For example, if 100 rows have been fetched with {@code NEXT} or {@code PRIOR}, and 80
     * rows are still in the local buffer, the server-side position is actually 80 rows ahead (or behind). The next
     * fetch with {@code RELATIVE} will need to correct this in {@code position}, and a {@code PRIOR} after
     * a {@code NEXT} or a {@code NEXT} after a {@code PRIOR} will need to reposition with {@code RELATIVE} or
     * {@code ABSOLUTE}, or know how many rows to ignore from the fetched batch.
     * </p>
     *
     * @param fetchType
     *         Fetch type
     * @param fetchSize
     *         Number of rows to fetch (must be {@code > 0}) (ignored by server for types other than
     *         {@link FetchType#NEXT} and {@link FetchType#PRIOR})
     * @param position
     *         Absolute or relative position for the row to fetch (ignored by server for types other than
     *         {@link FetchType#ABSOLUTE} and {@link FetchType#RELATIVE})
     * @throws java.sql.SQLFeatureNotSupportedException
     *         For types other than {@link FetchType#NEXT} if the protocol version or the implementation does not
     *         support scroll fetch
     * @throws SQLException
     *         For database access errors, when called on a closed statement, when no cursor is open or for server-side
     *         error conditions
     * @see #supportsFetchScroll()
     * @since 5
     */
    default void fetchScroll(FetchType fetchType, int fetchSize, int position) throws SQLException {
        if (fetchType == FetchType.NEXT) {
            fetchRows(fetchSize);
            return;
        }
        throw new FBDriverNotCapableException("implementation does not support fetchScroll");
    }

    /**
     * Has at least one fetch been executed on the current cursor?
     *
     * @return {@code true} if at least one fetch has been executed on the current cursor, {@code false} otherwise
     * (including if nothing has been executed, or the current statement has no cursor)
     * @since 5
     */
    boolean hasFetched();

    /**
     * Reports whether this statement implementation supports {@link #fetchScroll(FetchType, int, int)} with anything
     * other than {@link FetchType#NEXT}.
     *
     * @return {@code true} {@code fetchScroll} supported, {@code false} if not supported (default implementation
     * returns {@code false})
     * @since 5
     */
    default boolean supportsFetchScroll() {
        return false;
    }

    /**
     * Registers a {@link org.firebirdsql.gds.ng.listeners.StatementListener}.
     *
     * @param statementListener
     *         The statement listener
     */
    void addStatementListener(StatementListener statementListener);

    /**
     * Adds a {@link StatementListener} instance to this database using a weak reference.
     * <p>
     * If the listener is already strongly referenced, this call will be ignored
     * </p>
     *
     * @param statementListener
     *         statement listener
     */
    void addWeakStatementListener(StatementListener statementListener);

    /**
     * Removes a {@link org.firebirdsql.gds.ng.listeners.StatementListener}.
     *
     * @param statementListener
     *         The statement listener
     */
    void removeStatementListener(StatementListener statementListener);

    /**
     * Request statement info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T> T getSqlInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor) throws SQLException;

    /**
     * Request statement info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @return Response buffer
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException;

    /**
     * Request cursor info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If requesting cursor info is not supported (Firebird 4.0 or earlier, or native implementation)
     * @see #supportsCursorInfo()
     * @since 5
     */
    default <T> T getCursorInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support getCursorInfo");
    }

    /**
     * Request cursor info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @return Response buffer
     * @throws SQLException
     *         For errors retrieving or transforming the response
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If requesting cursor info is not supported (Firebird 4.0 or earlier, or native implementation)
     * @since 5
     */
    default byte[] getCursorInfo(byte[] requestItems, int bufferLength) throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support getCursorInfo");
    }

    /**
     * Reports whether this statement implementation supports {@link #getCursorInfo(byte[], int, InfoProcessor)} and
     * {@link #getCursorInfo(byte[], int)}.
     *
     * @return {@code true} {@code getCursorInfo} supported, {@code false} if not supported (default implementation
     * returns {@code false})
     * @since 5
     */
    default boolean supportsCursorInfo() {
        return false;
    }

    /**
     * @return The default size to use for the sql info buffer
     */
    int getDefaultSqlInfoSize();

    /**
     * @return The maximum size to use for the sql info buffer
     */
    int getMaxSqlInfoSize();

    /**
     * @return The execution plan of the currently prepared statement
     * @throws SQLException
     *         If this statement is closed.
     */
    String getExecutionPlan() throws SQLException;

    /**
     * @return The detailed execution plan of the currently prepared statement
     * @throws SQLException
     *         If this statement is closed.
     */
    String getExplainedExecutionPlan() throws SQLException;

    /**
     * Retrieves the SQL counts for the last execution of this statement.
     * <p>
     * The retrieved SQL counts are also notified to all registered {@link StatementListener}s.
     * </p>
     * <p>
     * In general the {@link FbStatement} will (should) retrieve and notify listeners of the SQL counts automatically
     * at times where it is relevant (eg after executing a statement that does not produce multiple rows, or after
     * fetching all rows).
     * </p>
     *
     * @return The SQL counts of the last execution of this statement
     * @throws SQLException
     *         If this statement is closed, or if this statement is in state {@link StatementState#CURSOR_OPEN} and not
     *         all rows have been fetched.
     */
    SqlCountHolder getSqlCounts() throws SQLException;

    /**
     * Sets the named cursor name for this statement.
     *
     * @param cursorName
     *         Name of the cursor
     * @throws SQLException
     *         If this statement is closed, TODO: Other reasons (eg cursor open)?
     */
    void setCursorName(String cursorName) throws SQLException;

    /**
     * @return A potentially cached empty row descriptor for this statement or database.
     */
    RowDescriptor emptyRowDescriptor();

    /**
     * Ensures that the statement cursor is closed. Resets a statement, so it is ready to be reused for re-execute or
     * prepare.
     *
     * @param transactionEnd
     *         Close is in response to a transaction end
     * @throws SQLException
     *         If this statement is closed or the cursor could not be closed.
     * @since 3.0.6
     */
    void ensureClosedCursor(boolean transactionEnd) throws SQLException;

    /**
     * Sets the statement timeout.
     * <p>
     * The statement timeout value is ignored in implementations that do not support timeouts. If the provided
     * timeout value is greater than supported (eg greater than ‭4294967295‬ milliseconds on Firebird 4), the
     * implementation should behave as if zero ({@code 0}) was set, but still report the original value.
     * </p>
     * <p>
     * The configured timeout only affects subsequent executes on this statement. The timeout includes time spent
     * between reading from the result set.
     * </p>
     *
     * @param timeoutMillis
     *         Timeout value in milliseconds
     * @throws SQLException
     *         If the value is less than zero, this statement is closed, or a database access error occurs
     * @since 4.0
     */
    void setTimeout(long timeoutMillis) throws SQLException;

    /**
     * Gets the current statement timeout for this statement.
     * <p>
     * This method will only return the current statement timeout value for this method, it will not consider attachment
     * or connection level timeouts. This is an implementation decision that might change in a point release.
     * </p>
     *
     * @return The configured timeout in milliseconds; read the documentation in {@link #setTimeout(long)}
     * @throws SQLException
     *         If this statement is closed, or a database access error occurs
     * @see #setTimeout(long)
     * @since 4.0
     */
    long getTimeout() throws SQLException;

    /**
     * Set cursor flag.
     * <p>
     * If a protocol version does not support cursor flags, this is silently ignored.
     * </p>
     *
     * @param flag
     *         Cursor flag to set
     * @since 5
     */
    default void setCursorFlag(CursorFlag flag) {
        // do nothing
    }

    /**
     * Clears cursor flag.
     * <p>
     * Setting a cursor flag only affects subsequent executes. A currently open cursor will not be affected.
     * </p>
     * <p>
     * If a protocol version does not support cursor flags, this is silently ignored.
     * </p>
     *
     * @param flag
     *         Cursor flag to clear
     * @since 5
     */
    default void clearCursorFlag(CursorFlag flag) {
        // do nothing
    }

    /**
     * Reports whether a cursor flag is set.
     * <p>
     * If a protocol version does not support cursor flags, {@code false} should be returned.
     * </p>
     *
     * @param flag
     *         Cursor flag
     * @return {@code true} when set, {@code false} otherwise
     * @since 5
     */
    default boolean isCursorFlagSet(CursorFlag flag) {
        return false;
    }

    /**
     * Reports whether this statement implementation supports server-side batch updates.
     *
     * @return {@code true} server-side batch updates supported, {@code false} if not supported (default implementation
     * returns {@code false})
     * @since 5
     */
    default boolean supportBatchUpdates() {
        return false;
    }

    /**
     * Sends batch create with deferred response processing.
     * <p>
     * Implementations that do not supported deferred or async response processing should call
     * {@link DeferredResponse#onResponse(Object)} and - optionally - {@link DeferredResponse#onException(Exception)}
     * synchronously. If the response is deferred, but the implementation is not capable of connecting the response
     * back, it should call {@code onResponse} before method return, and any exceptions generated by deferred processing
     * should then be thrown from the method that causes the response to be received.
     * </p>
     *
     * @param batchConfig
     *         batch configuration
     * @param onResponse
     *         deferred action to call when response is received
     * @throws SQLException
     *         for database access errors (I/O errors)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default void deferredBatchCreate(FbBatchConfig batchConfig, DeferredResponse<Void> onResponse) throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support deferredBatchCreate");
    }

    /**
     * Sends batch data with deferred response processing.
     * <p>
     * For implementations that do not supported deferred or async response processing, see
     * {@link #deferredBatchCreate(FbBatchConfig, DeferredResponse)} for expected behaviour.
     * </p>
     *
     * @param rowValues
     *         collection of row values
     * @param onResponse
     *         deferred action to call when response is received
     * @throws SQLException
     *         for database access errors (I/O errors)
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default void deferredBatchSend(Collection<RowValue> rowValues, DeferredResponse<Void> onResponse) throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support deferredBatchSend");
    }

    /**
     * Execute the batch on the server.
     *
     * @return batch completion response
     * @throws SQLException
     *         for database access errors
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default BatchCompletion batchExecute() throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support batchExecute");
    }

    /**
     * Cancels the server side batch (that is, clear any rows batched on the server).
     *
     * @throws SQLException
     *         for database access errors
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default void batchCancel() throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support batchCancel");
    }

    /**
     * Closes (releases) the batch on the server with deferred response processing.
     * <p>
     * For implementations that do not supported deferred or async response processing, see
     * {@link #deferredBatchCreate(FbBatchConfig, DeferredResponse)} for expected behaviour.
     * </p>
     *
     * @param onResponse
     *         deferred action to call when response is received
     * @throws SQLException
     *         for database access errors
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default void deferredBatchRelease(DeferredResponse<Void> onResponse) throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support batchCancel");
    }

    /**
     * Creates a {@link BatchParameterBuffer} instance compatible with this protocol version.
     *
     * @return batch parameter buffer
     * @throws SQLException
     *         if this statement is closed, or a database access error occurs, or when the parameter buffer could not be
     *         created for other reasons
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when this statement implementation does not support batch updates
     * @see #supportBatchUpdates()
     * @since 5
     */
    default BatchParameterBuffer createBatchParameterBuffer() throws SQLException {
        throw new FBDriverNotCapableException("implementation does not support createBatchParameterBuffer");
    }

    /**
     * Locks the lock with {@link java.util.concurrent.locks.Lock#lock()} (or equivalent).
     * <p>
     * Implementations are expected to apply the same lock as {@link FbAttachment#withLock()}.
     * </p>
     *
     * @return lock closeable which unlocks the lock on close
     * @see FbAttachment#withLock() 
     */
    LockCloseable withLock();

}
