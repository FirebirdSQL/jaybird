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

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.StatementListener;

import java.sql.SQLException;

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
public interface FbStatement extends ExceptionListenable {

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
     * @throws SQLException
     */
    void setTransaction(FbTransaction transaction) throws SQLException;

    /**
     * @return descriptor of the parameters of this statement
     */
    RowDescriptor getParameterDescriptor();

    /**
     * @return descriptor of the fields returned by this statement
     */
    RowDescriptor getFieldDescriptor();

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
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * Closes the cursor associated with this statement, leaving the
     * statement itself allocated.
     * <p>
     * Equivalent to calling {@link #closeCursor(boolean)} with {@code false}.
     * </p>
     *
     * @throws SQLException
     */
    void closeCursor() throws SQLException;

    /**
     * Closes the cursor associated with this statement, leaving the statement itself allocated.
     * <p>
     * When this method is called in preparation of a commit or rollback (see {@code transactionEnd}), then
     * implementations may opt to not close the cursor on the server if the server closes the cursor automatically.
     * </p>
     *
     * @param transactionEnd Close is in response to a transaction end.
     * @throws SQLException
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
     * Requests this statement to fetch the next <code>fetchSize</code> rows.
     * <p>
     * Fetched rows are not returned from this method, but sent to the registered {@link org.firebirdsql.gds.ng.listeners.StatementListener} instances.
     * </p>
     *
     * @param fetchSize
     *         Number of rows to fetch (must be <code>&gt; 0</code>)
     * @throws SQLException
     *         For database access errors, when called on a closed statement, when no cursor is open or when the fetch
     *         size is not <code>&gt; 0</code>.
     */
    void fetchRows(int fetchSize) throws SQLException;

    /**
     * Registers a {@link org.firebirdsql.gds.ng.listeners.StatementListener}.
     *
     * @param statementListener
     *         The row listener
     */
    void addStatementListener(StatementListener statementListener);

    /**
     * Removes a {@link org.firebirdsql.gds.ng.listeners.StatementListener}.
     *
     * @param statementListener
     *         The row listener
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
     */
    byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException;

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
     * Retrieves the SQL counts for the last execution of this statement.
     * <p>
     * The retrieved SQL counts are also notified to all registered {@link StatementListener}s.
     * </p>
     * <p>
     * In general the {@link FbStatement} will (should) retrieve and notify listeners of the SQL counts automatically at times were
     * it is relevant (eg after executing a statement that does not produce multiple rows, or after fetching all rows).
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
     * Ensures that the statement cursor is closed. Resets a statement so it is ready to be reused for re-execute or
     * prepare.
     * <p>
     * Implementations should only close an open cursor and log this fact with a stacktrace on debug. This is a stopgap
     * measure for situations where the code using this statement handle has not been able to properly close the
     * cursor.
     * </p>
     *
     * @param transactionEnd Close is in response to a transaction end
     *
     * @throws SQLException If this statement is closed or the cursor could not be closed.
     * @since 3.0.6
     */
    void ensureClosedCursor(boolean transactionEnd) throws SQLException;
}
