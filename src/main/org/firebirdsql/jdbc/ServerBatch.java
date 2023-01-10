/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbBatchConfig;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jaybird.fb.constants.BatchItems;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.Primitives;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * Batch implementation using server-side batch updates.
 * <p>
 * This implementation itself is not thread-safe, and expects the caller to lock appropriately using
 * {@link FbStatement#withLock()} of the executing statement.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class ServerBatch implements Batch, StatementListener {

    private static final Logger log = LoggerFactory.getLogger(ServerBatch.class);

    private volatile BatchState state = BatchState.INITIAL;
    private final FbBatchConfig batchConfig;
    private Deque<Batch.BatchRowValue> batchRowValues = new ArrayDeque<>();
    private FbStatement statement;

    ServerBatch(FbBatchConfig batchConfig, FbStatement statement) throws SQLException {
        if (!statement.supportBatchUpdates()) {
            throw new FBDriverNotCapableException(
                    format("FbStatement implementation %s does not support server-side batch updates",
                            statement.getClass().getName()));
        }
        this.batchConfig = batchConfig.immutable();
        this.statement = statement;
        statement.addStatementListener(this);
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
        if (isClosed() || sender != statement) {
            sender.removeStatementListener(this);
            return;
        }

        switch (newState) {
        case ALLOCATED:
        case PREPARING:
            // NOTE: These state transition shouldn't occur for usage in FBPreparedStatement; included for robustness
            // Server-side batch is deallocated when unprepared or when preparing a new statement text
            if (state != BatchState.INITIAL) {
                // NOTE: Changing state before clearing batch to avoid server-side cancel (which would fail)
                state = BatchState.INITIAL;
                try {
                    clearBatch();
                } catch (SQLException e) {
                    // path when state is INITIAL should not result in SQLException
                    log.debug("Unexpected exception clearing batch, this might indicate a bug in Jaybird", e);
                }
            }
            break;
        case CLOSED:
            // Normal usage from FBPreparedStatement will have already closed it; included for robustness
            close();
            break;
        default:
            // do nothing
            break;
        }
    }

    @Override
    public void addBatch(Batch.BatchRowValue rowValue) throws SQLException {
        checkOpen();
        batchRowValues.addLast(rowValue);
    }

    private boolean isEmpty() throws SQLException {
        checkOpen();
        return batchRowValues.isEmpty();
    }

    @Override
    public List<Long> execute() throws SQLException {
        try {
            checkOpen();
            if (isEmpty()) {
                return emptyList();
            }
            Collection<RowValue> rowValues = toRowValues();
            SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
            // Create server-side batch
            if (!state.isOpenOnServer()) {
                createBatch(chain);
            }
            // Send batch to server
            sendBatch(rowValues, chain);

            BatchCompletion batchCompletion = executeBatch(chain);

            if (batchCompletion.hasErrors()) {
                throw chain.addFirst(toBatchUpdateException(batchCompletion)).getException();
            } else if (chain.hasException()) {
                // We had an earlier exception on sending the batch, causing us to execute an empty batch
                throw chain.getException();
            } else {
                int[] updateCounts = toJdbcUpdateCounts(batchCompletion);
                return Primitives.toLongList(updateCounts);
            }
        } finally {
            clearBatch();
        }
    }

    /**
     * Sends batch create with deferred response processing.
     *
     * @param chain
     *         Chain to append error returned from server, if any, or to throw previously chained exceptions
     * @throws SQLException
     *         For database access errors (I/O errors)
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void createBatch(SQLExceptionChainBuilder<SQLException> chain) throws SQLException {
        try {
            statement.deferredBatchCreate(batchConfig,
                    new BatchDeferredAction(chain, "exception creating batch") {
                        @Override
                        public void onException(Exception exception) {
                            super.onException(exception);
                            state = BatchState.INITIAL;
                        }
                    });
            // We assume success on opening, otherwise it's reset in onException of the deferred action
            state = state.onServerOpen();
        } catch (SQLException e) {
            chain.append(e);
            throw chain.getException();
        }
    }

    /**
     * Sends batch data with deferred response processing.
     *
     * @param chain
     *         Chain to append error returned from server, if any, or to throw previously chained exceptions
     * @throws SQLException
     *         For database access errors (I/O errors)
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void sendBatch(Collection<RowValue> rowValues, SQLExceptionChainBuilder<SQLException> chain)
            throws SQLException {
        try {
            statement.deferredBatchSend(rowValues,
                    new BatchDeferredAction(chain, "exception sending batch message") {
                        @Override
                        public void onException(Exception exception) {
                            super.onException(exception);
                            state = BatchState.SERVER_OPEN;
                        }
                    });
            // We assume success on opening, otherwise it's reset in onException of the deferred action
            state = state.onSend();
        } catch (SQLException e) {
            chain.append(e);
            throw chain.getException();
        }
    }

    /**
     * Execute the batch on the server.
     *
     * @param chain
     *         Chain to append error returned from server, if any, or to throw previously chained exceptions
     * @return batch completion response
     * @throws SQLException
     *         for database access errors
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private BatchCompletion executeBatch(SQLExceptionChainBuilder<SQLException> chain) throws SQLException {
        try {
            state = state.onExecute();
            BatchCompletion batchCompletion = statement.batchExecute();
            state = state.onBatchComplete();
            return batchCompletion;
        } catch (SQLException e) {
            chain.append(e);
            throw chain.getException();
        }
    }

    private Collection<RowValue> toRowValues() throws SQLException {
        Deque<Batch.BatchRowValue> batchRowValues = this.batchRowValues;
        List<RowValue> rowValues = new ArrayList<>(batchRowValues.size());
        Batch.BatchRowValue batchRowValue;
        while ((batchRowValue = batchRowValues.pollFirst()) != null) {
            rowValues.add(batchRowValue.toRowValue());
        }
        return rowValues;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    @Override
    public void clearBatch() throws SQLException {
        checkOpen();
        try {
            if (state.isBatchOnServer()) {
                statement.batchCancel();
                state = state.onServerCancel();
            }
        } finally {
            batchRowValues.clear();
        }
    }

    private void checkOpen() throws SQLException {
        if (isClosed()) {
            throw new SQLException("batch has been closed");
        }
    }

    boolean isClosed() {
        return state == BatchState.CLOSED;
    }

    @Override
    public void close() {
        if (isClosed()) return;
        state = BatchState.CLOSED;
        batchRowValues = null;
        FbStatement copyStmt = statement;
        if (copyStmt != null) {
            statement = null;
            copyStmt.removeStatementListener(this);
        }
    }

    /**
     * Produces update counts as expected by JDBC.
     * <p>
     * When no update counts ({@code TAG_RECORD_COUNTS}) were requested, this method will generate appropriate
     * update counts (populated with {@link Statement#SUCCESS_NO_INFO}, and - if multi-error -
     * {@link Statement#EXECUTE_FAILED} for errors).
     * </p>
     *
     * @param batchCompletion
     *         Batch completion data
     * @return update counts as expected by JDBC
     * @see #toBatchUpdateException(BatchCompletion)
     */
    int[] toJdbcUpdateCounts(BatchCompletion batchCompletion) {
        int elementCount = batchCompletion.elementCount();
        int[] jdbcUpdateCounts = batchCompletion.updateCounts();
        List<BatchCompletion.DetailedError> detailedErrors = batchCompletion.detailedErrors();
        int[] simplifiedErrors = batchCompletion.simplifiedErrors();
        if (!batchCompletion.hasErrors()) {
            if (jdbcUpdateCounts.length == 0 && elementCount > 0) {
                // FbBatchConfig.updateCounts() was false, report as SUCCESS_NO_INFO for each element
                jdbcUpdateCounts = new int[elementCount];
                Arrays.fill(jdbcUpdateCounts, Statement.SUCCESS_NO_INFO);
            }
        } else if (jdbcUpdateCounts.length == 0 && elementCount > 0) {
            // FbBatchConfig.updateCounts() was false, report as SUCCESS_NO_INFO
            if (batchConfig.multiError()) {
                jdbcUpdateCounts = new int[elementCount];
                Arrays.fill(jdbcUpdateCounts, Statement.SUCCESS_NO_INFO);
                // Populate EXECUTE_FAILED for errors
                for (BatchCompletion.DetailedError error : detailedErrors) {
                    jdbcUpdateCounts[error.element()] = Statement.EXECUTE_FAILED;
                }
                for (int element : simplifiedErrors) {
                    jdbcUpdateCounts[element] = Statement.EXECUTE_FAILED;
                }
            } else {
                int firstErrorPosition = detailedErrors.isEmpty()
                        ? simplifiedErrors[0]
                        : detailedErrors.get(0).element();
                if (firstErrorPosition != 0) {
                    jdbcUpdateCounts = new int[firstErrorPosition];
                    Arrays.fill(jdbcUpdateCounts, Statement.SUCCESS_NO_INFO);
                }
            }
        } else if (batchConfig.multiError()) {
            for (int i = 0; i < jdbcUpdateCounts.length; i++) {
                // Replace Firebird failure code (-1) with JDBC failure code (-3)
                if (jdbcUpdateCounts[i] == BatchItems.BATCH_EXECUTE_FAILED) {
                    jdbcUpdateCounts[i] = Statement.EXECUTE_FAILED;
                }
            }
        } else if (jdbcUpdateCounts[jdbcUpdateCounts.length - 1] == BatchItems.BATCH_EXECUTE_FAILED) {
            // Exclude update count for failed row for non-multi-error case (see JDBC requirements)
            jdbcUpdateCounts = Arrays.copyOf(jdbcUpdateCounts, jdbcUpdateCounts.length - 1);
        }
        return jdbcUpdateCounts;
    }

    /**
     * Produces a {@code BatchUpdateException} with exception information and update counts.
     * <p>
     * If available, the message, SQLstate and error code of the first detailed error is used for
     * the {@code BatchUpdateException}. The chain of detailed errors is added as the next exception
     * this {@code BatchUpdateException}, and can be accessed through {@link SQLException#getNextException()}.
     * </p>
     *
     * @param batchCompletion
     *         Batch completion data
     * @return populated {@code BatchUpdateException}
     * @throws IllegalStateException
     *         When this response has no errors ({@link BatchCompletion#hasErrors()} returns {@code false}
     * @see #toJdbcUpdateCounts(BatchCompletion)
     */
    BatchUpdateException toBatchUpdateException(BatchCompletion batchCompletion) {
        if (!batchCompletion.hasErrors()) {
            throw new IllegalStateException("toBatchUpdateException called while BatchCompletion has no errors");
        }
        int[] updateCounts = toJdbcUpdateCounts(batchCompletion);

        List<BatchCompletion.DetailedError> detailedErrors = batchCompletion.detailedErrors();
        if (detailedErrors.isEmpty()) {
            // NOTE: Currently not configurable, we're always using the default of 64 errors
            return new BatchUpdateException("Batch execution failed without detailed errors; "
                    + "this can happen when detailedErrors batch config is set to zero",
                    SQLStateConstants.SQL_STATE_GENERAL_ERROR, 0, updateCounts);
        }
        SQLException exception = detailedErrors.get(0).error();
        BatchUpdateException bue = new BatchUpdateException(exception.getMessage(), exception.getSQLState(),
                exception.getErrorCode(), updateCounts);
        detailedErrors.stream()
                .map(BatchCompletion.DetailedError::error)
                .forEach(bue::setNextException);
        return bue;
    }

    private static class BatchDeferredAction implements DeferredResponse<Void> {

        private final SQLExceptionChainBuilder<? super SQLException> chain;
        private final String genericExceptionMessage;

        BatchDeferredAction(SQLExceptionChainBuilder<? super SQLException> chain, String genericExceptionMessage) {
            this.chain = chain;
            this.genericExceptionMessage = genericExceptionMessage;
        }

        @Override
        public void onException(Exception exception) {
            if (exception instanceof SQLException) {
                chain.append((SQLException) exception);
            } else {
                chain.append(new SQLNonTransientException(genericExceptionMessage, exception));
            }
        }

    }

    private enum BatchState {
        /**
         * Initial state when open, but not yet open on server, including after preparing a new statement on a handle
         */
        INITIAL,
        /**
         * Batch is open on server
         */
        SERVER_OPEN,
        /**
         * At least a part of the batch has been sent to the server
         */
        PARTIAL_SEND,
        /**
         * The batch is being executed.
         */
        EXECUTING,
        /**
         * Batch has been closed
         */
        CLOSED;

        /**
         * @return {@code true} if the batch is open on the server for this state
         */
        boolean isOpenOnServer() {
            return !(this == INITIAL || this == CLOSED);
        }

        /**
         * @return {@code true} if there is batch data on the server for this state
         */
        boolean isBatchOnServer() {
            return this == PARTIAL_SEND || this == EXECUTING;
        }

        /**
         * @return new state on server-open.
         */
        BatchState onServerOpen() throws SQLException {
            if (this == INITIAL) {
                return SERVER_OPEN;
            } else {
                throw new SQLNonTransientException("Cannot server-open in state " + this);
            }
        }

        /**
         * @return new state on sending batch data.
         */
        BatchState onSend() throws SQLException {
            switch (this) {
            case INITIAL:
                // Assume we open as part of the send operation
            case SERVER_OPEN:
            case PARTIAL_SEND:
                return PARTIAL_SEND;
            // Assume we send more data as part of executing a very large batch
            case EXECUTING:
                return EXECUTING;
            case CLOSED:
                throw new SQLNonTransientException("Cannot send in state CLOSED");
            default:
                throw new SQLNonTransientException("Unexpected state " + this);
            }
        }

        /**
         * @return new state on execute
         */
        BatchState onExecute() throws SQLException {
            switch (this) {
            case INITIAL:
                // Assume we open as part of the execute operation
            case SERVER_OPEN:
            case PARTIAL_SEND:
                // Assume we execute as part of executing a very large batch
            case EXECUTING:
                return EXECUTING;
            case CLOSED:
                throw new SQLNonTransientException("Cannot execute in state CLOSED");
            default:
                throw new SQLNonTransientException("Unexpected state " + this);
            }
        }

        /**
         * @return new state on completing batch execute
         */
        BatchState onBatchComplete() throws SQLException {
            switch (this) {
            case EXECUTING:
                return SERVER_OPEN;
            case CLOSED:
                throw new SQLNonTransientException("Cannot complete in state CLOSED");
            default:
                throw new SQLNonTransientException("Unexpected state " + this);
            }
        }

        BatchState onServerCancel() throws SQLException {
            if (this == CLOSED || this == INITIAL) {
                return this;
            }
            return SERVER_OPEN;
        }
    }
}
