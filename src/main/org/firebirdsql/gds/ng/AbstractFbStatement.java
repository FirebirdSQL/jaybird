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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.sql.SQLWarning;
import java.util.EnumSet;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbStatement implements FbStatement {

    /**
     * Set of states that will be reset to {@link StatementState#PREPARED} on transaction change
     */
    private static final EnumSet<StatementState> RESET_TO_PREPARED = EnumSet.of(StatementState.EXECUTING, StatementState.CURSOR_OPEN);
    private static final Logger log = LoggerFactory.getLogger(AbstractFbStatement.class);

    private final Object syncObject = new Object();
    private final WarningMessageCallback warningCallback = new WarningMessageCallback() {
        @Override
        public void processWarning(SQLWarning warning) {
            statementListenerDispatcher.warningReceived(AbstractFbStatement.this, warning);
        }
    };
    protected final StatementListenerDispatcher statementListenerDispatcher = new StatementListenerDispatcher();
    private volatile boolean allRowsFetched = false;
    private volatile StatementState state = StatementState.NEW;
    private volatile StatementType type = StatementType.NONE;
    private volatile RowDescriptor parameterDescriptor;
    private volatile RowDescriptor fieldDescriptor;
    private volatile FbTransaction transaction;

    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
                TransactionState previousState) {
            if (getTransaction() != transaction) {
                transaction.removeTransactionListener(this);
                return;
            }
            switch (newState) {
            case COMMITTED:
            case ROLLED_BACK:
                synchronized (getSynchronizationObject()) {
                    try {
                        if (RESET_TO_PREPARED.contains(getState())) {
                            // Cursor has been closed due to commit, rollback, etc, back to prepared state
                            try {
                                switchState(StatementState.PREPARED);
                            } catch (SQLException e) {
                                throw new IllegalStateException("Received an SQLException when none was expected", e);
                            }
                            reset(false);
                        }
                    } finally {
                        transaction.removeTransactionListener(this);
                        try {
                            setTransaction(null);
                        } catch (SQLException e) {
                            //noinspection ThrowFromFinallyBlock
                            throw new IllegalStateException("Received an SQLException when none was expected", e);
                        }
                    }
                }
            }
        }
    };

    /**
     * Gets the {@link TransactionListener} instance for this statement.
     * <p>
     * This method should only be called by this object itself. Subclasses may provide their own transaction listener, but
     * the instance returned by this method should be the same for the lifetime of this {@link FbStatement}.
     * </p>
     *
     * @return The transaction listener instance for this statement.
     */
    protected final TransactionListener getTransactionListener() {
        return transactionListener;
    }

    protected final WarningMessageCallback getStatementWarningCallback() {
        return warningCallback;
    }

    /**
     * Get synchronization object.
     *
     * @return object, cannot be <code>null</code>.
     */
    protected final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public void close() throws SQLException {
        if (getState() == StatementState.CLOSED) return;
        synchronized (getSynchronizationObject()) {
            // TODO do additional checks (see also old implementation and .NET)
            try {
                final StatementState currentState = getState();
                forceState(StatementState.CLOSING);
                if (currentState != StatementState.NEW) {
                    free(ISCConstants.DSQL_drop);
                }
            } finally {
                forceState(StatementState.CLOSED);
                setType(StatementType.NONE);
                statementListenerDispatcher.shutdown();
                setTransaction(null);
            }
        }
    }

    @Override
    public final void closeCursor() throws SQLException {
        closeCursor(false);
    }

    @Override
    public final void closeCursor(boolean transactionEnd) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (!getState().isCursorOpen()) return;
            // TODO do additional checks (see also old implementation and .NET)
            try {
                if (!transactionEnd && getType().isTypeWithCursor()) {
                    free(ISCConstants.DSQL_close);
                }
                // TODO Any statement types that cannot be prepared and would need to go to ALLOCATED?
                switchState(StatementState.PREPARED);
            } catch (SQLException e) {
                // TODO Close in case of exception?
                switchState(StatementState.ERROR);
                throw e;
            }
        }
    }

    @Override
    public StatementState getState() {
        return state;
    }

    /**
     * Sets the StatementState.
     *
     * @param newState
     *         New state
     * @throws SQLException
     *         When the state is changed to an illegal next state
     */
    protected final void switchState(final StatementState newState) throws SQLException {
        synchronized (getSynchronizationObject()) {
            final StatementState currentState = state;
            if (currentState == newState || currentState == StatementState.CLOSED) return;
            if (currentState.isValidTransition(newState)) {
                state = newState;
                statementListenerDispatcher.statementStateChanged(this, newState, currentState);
            } else {
                throw new SQLNonTransientException(String.format("Statement state %s only allows next states %s, received %s", currentState, currentState.validTransitionSet(), newState));
            }
        }
    }

    /**
     * Forces the statement to the specified state without throwing an exception if this is not a valid transition.
     * <p>
     * Does nothing if current state is CLOSED.
     * </p>
     *
     * @param newState
     *         New state
     * @see #switchState(StatementState)
     */
    private void forceState(final StatementState newState) {
        synchronized (getSynchronizationObject()) {
            final StatementState currentState = state;
            if (currentState == newState || currentState == StatementState.CLOSED) return;
            if (log.isDebugEnabled() && !currentState.isValidTransition(newState)) {
                log.debug(
                        String.format("Forced statement transition is invalid; state %s only allows next states %s, forced to set %s",
                                currentState, currentState.validTransitionSet(), newState),
                        new IllegalStateException());
            }
            state = newState;
            statementListenerDispatcher.statementStateChanged(this, newState, currentState);
        }
    }

    @Override
    public final StatementType getType() {
        return type;
    }

    /**
     * Sets the StatementType
     *
     * @param type
     *         New type
     */
    protected void setType(StatementType type) {
        synchronized (getSynchronizationObject()) {
            this.type = type;
        }
    }

    /**
     * Queues row data for consumption
     *
     * @param rowData
     *         Row data
     */
    protected final void queueRowData(RowValue rowData) {
        statementListenerDispatcher.receivedRow(this, rowData);
    }

    /**
     * Sets the <code>allRowsFetched</code> property.
     * <p>
     * When set to true all registered {@link org.firebirdsql.gds.ng.listeners.StatementListener} instances are notified
     * for the {@link org.firebirdsql.gds.ng.listeners.StatementListener#allRowsFetched(FbStatement)} event.
     * </p>
     *
     * @param allRowsFetched
     *         <code>true</code>: all rows fetched, <code>false</code> not all rows fetched.
     */
    protected final void setAllRowsFetched(boolean allRowsFetched) {
        synchronized (getSynchronizationObject()) {
            this.allRowsFetched = allRowsFetched;
        }
        if (allRowsFetched) {
            statementListenerDispatcher.allRowsFetched(this);
        }
    }

    protected final boolean isAllRowsFetched() {
        return allRowsFetched;
    }

    /**
     * Reset statement state, equivalent to calling {@link #reset(boolean)} with <code>false</code>
     */
    protected final void reset() {
        reset(false);
    }

    /**
     * Reset statement state and clear parameter description, equivalent to calling {@link #reset(boolean)} with <code>true</code>
     */
    protected final void resetAll() {
        reset(true);
    }

    /**
     * Resets the statement for next execution. Implementation in derived class must synchronize on {@link #getSynchronizationObject()} and
     * call <code>super.reset(resetAll)</code>
     *
     * @param resetAll
     *         Also reset field and parameter info
     */
    protected void reset(boolean resetAll) {
        synchronized (getSynchronizationObject()) {
            setAllRowsFetched(false);

            if (resetAll) {
                setParameterDescriptor(null);
                setFieldDescriptor(null);
                setType(StatementType.NONE);
            }
        }
    }

    private static final EnumSet<StatementState> PREPARE_ALLOWED_STATES = EnumSet.of(
            StatementState.NEW, StatementState.ALLOCATED, StatementState.PREPARED);

    /**
     * Is a call to {@link #prepare(String)} allowed for the supplied {@link StatementState}.
     *
     * @param state
     *         The statement state
     * @return <code>true</code> call to <code>prepare</code> is allowed
     */
    protected boolean isPrepareAllowed(final StatementState state) {
        return PREPARE_ALLOWED_STATES.contains(state);
    }

    @Override
    public final RowDescriptor getParameterDescriptor() {
        return parameterDescriptor;
    }

    /**
     * Sets the parameter descriptor.
     *
     * @param parameterDescriptor
     *         Parameter descriptor
     */
    protected void setParameterDescriptor(RowDescriptor parameterDescriptor) {
        synchronized (getSynchronizationObject()) {
            this.parameterDescriptor = parameterDescriptor;
        }
    }

    @Override
    public final RowDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }

    /**
     * Sets the (result set) field descriptor.
     *
     * @param fieldDescriptor
     *         Field descriptor
     */
    protected void setFieldDescriptor(RowDescriptor fieldDescriptor) {
        synchronized (getSynchronizationObject()) {
            this.fieldDescriptor = fieldDescriptor;
        }
    }

    /**
     * @return The (full) statement info request items.
     * @see #getParameterDescriptionInfoRequestItems()
     */
    public byte[] getStatementInfoRequestItems() {
        return ((AbstractFbDatabase) getDatabase()).getStatementInfoRequestItems();
    }

    /**
     * @return The {@code isc_info_sql_describe_vars} info request items.
     * @see #getStatementInfoRequestItems()
     */
    public byte[] getParameterDescriptionInfoRequestItems() {
        return ((AbstractFbDatabase) getDatabase()).getParameterDescriptionInfoRequestItems();
    }

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
    @Override
    public final <T> T getSqlInfo(final byte[] requestItems, final int bufferLength,
            final InfoProcessor<T> infoProcessor) throws SQLException {
        return infoProcessor.process(getSqlInfo(requestItems, bufferLength));
    }

    @Override
    public final String getExecutionPlan() throws SQLException {
        checkStatementValid();
        final ExecutionPlanProcessor processor = createExecutionPlanProcessor();
        return getSqlInfo(processor.getDescribePlanInfoItems(), getDefaultSqlInfoSize(), processor);
    }

    /**
     * @return New instance of {@link ExecutionPlanProcessor} (or subclass) for this statement.
     */
    protected ExecutionPlanProcessor createExecutionPlanProcessor() {
        return new ExecutionPlanProcessor(this);
    }

    @Override
    public SqlCountHolder getSqlCounts() throws SQLException {
        checkStatementValid();
        if (getState() == StatementState.CURSOR_OPEN && !isAllRowsFetched()) {
            // We disallow fetching count when we haven't fetched all rows yet.
            // TODO SQLState
            throw new SQLNonTransientException("Cursor still open, fetch all rows or close cursor before fetching SQL counts");
        }
        final SqlCountProcessor countProcessor = createSqlCountProcessor();
        // NOTE: implementation of SqlCountProcessor assumes the specified size is sufficient (actual requirement is 49 bytes max) and does not handle truncation
        final SqlCountHolder sqlCounts = getSqlInfo(countProcessor.getRecordCountInfoItems(), 64, countProcessor);
        statementListenerDispatcher.sqlCounts(this, sqlCounts);
        return sqlCounts;
    }

    /**
     * @return New instance of {@link SqlCountProcessor} (or subclass) for this statement.
     */
    protected SqlCountProcessor createSqlCountProcessor() {
        return new SqlCountProcessor(this);
    }

    /**
     * Frees the currently allocated statement (either close the cursor with {@link ISCConstants#DSQL_close} or drop the statement
     * handle using {@link ISCConstants#DSQL_drop}.
     *
     * @param option
     *         Free option
     * @throws SQLException
     */
    protected abstract void free(int option) throws SQLException;

    /**
     * Validates if the number of parameters matches the expected number and types, and if all values have been set.
     *
     * @param parameters
     *         Parameter values to validate
     * @throws SQLException
     *         When the number or type of parameters does not match {@link #getParameterDescriptor()}, or when a parameter has not been set.
     */
    protected void validateParameters(final RowValue parameters) throws SQLException {
        final RowDescriptor parameterDescriptor = getParameterDescriptor();
        final int expectedSize = parameterDescriptor != null ? parameterDescriptor.getCount() : 0;
        final int actualSize = parameters.getCount();
        // TODO Externalize sqlstates
        if (actualSize != expectedSize) {
            // TODO use HY021 (inconsistent descriptor information) instead?
            throw new SQLNonTransientException(String.format("Invalid number of parameters, expected %d, got %d",
                    expectedSize, actualSize), "07008"); // invalid descriptor count
        }
        for (int fieldIndex = 0; fieldIndex < actualSize; fieldIndex++) {
            FieldValue fieldValue = parameters.getFieldValue(fieldIndex);
            if (fieldValue == null || !fieldValue.isInitialized()) {
                // Communicating 1-based index, so it doesn't cause confusion when JDBC user sees this.
                // TODO use HY000 (dynamic parameter value needed) instead?
                throw new SQLTransientException(String.format("Parameter with index %d was not set",
                        fieldIndex + 1), "0700C"); // undefined DATA value
            }
        }
    }

    @Override
    public final void addStatementListener(StatementListener statementListener) {
        if (getState() == StatementState.CLOSED) return;
        statementListenerDispatcher.addListener(statementListener);
    }

    @Override
    public final void removeStatementListener(StatementListener statementListener) {
        statementListenerDispatcher.removeListener(statementListener);
    }

    /**
     * Checks if this statement is not in {@link StatementState#CLOSED}, {@link StatementState#NEW} or {@link StatementState#ERROR},
     * and throws an <code>SQLException</code> if it is.
     *
     * @throws SQLException
     *         When this statement is closed or in error state.
     */
    protected final void checkStatementValid() throws SQLException {
        switch (getState()) {
        case NEW:
            // TODO Externalize sqlstate
            // TODO See if there is a firebird error code matching this (isc_cursor_not_open is not exactly the same)
            throw new SQLNonTransientException("Statement not yet allocated", "24000");
        case CLOSED:
            // TODO Externalize sqlstate
            // TODO See if there is a firebird error code matching this (isc_cursor_not_open is not exactly the same)
            throw new SQLNonTransientException("Statement closed", "24000");
        case ERROR:
            // TODO SQLState?
            // TODO See if there is a firebird error code matching this
            throw new SQLNonTransientException("Statement is in error state and needs to be closed");
        default:
            // Valid state, continue
            break;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (getState() != StatementState.CLOSED) close();
        } finally {
            super.finalize();
        }
    }

    @Override
    public FbTransaction getTransaction() {
        return transaction;
    }

    /**
     * Method to decide if a transaction implementation class is valid for the statement implementation.
     * <p>
     * Eg a {@link org.firebirdsql.gds.ng.wire.version10.V10Statement} will only work with an
     * {@link org.firebirdsql.gds.ng.wire.FbWireTransaction} implementation.
     * </p>
     *
     * @param transactionClass
     *         Class of the transaction
     * @return <code>true</code> when the transaction class is valid for the statement implementation.
     */
    protected abstract boolean isValidTransactionClass(Class<? extends FbTransaction> transactionClass);

    @Override
    public final void setTransaction(final FbTransaction newTransaction) throws SQLException {
        if (newTransaction == null || isValidTransactionClass(newTransaction.getClass())) {
            // TODO Is there a statement or transaction state where we should not be switching transactions?
            // Probably an error to switch when newTransaction is not null and current state is ERROR, CURSOR_OPEN, EXECUTING, CLOSING or CLOSED
            synchronized (getSynchronizationObject()) {
                if (newTransaction == transaction) return;
                if (transaction != null) {
                    transaction.removeTransactionListener(getTransactionListener());
                }
                transaction = newTransaction;
                if (newTransaction != null) {
                    newTransaction.addTransactionListener(getTransactionListener());
                }
            }
        } else {
            throw new SQLNonTransientException(String.format("Invalid transaction handle type, got \"%s\"", newTransaction.getClass().getName()),
                    FBSQLException.SQL_STATE_GENERAL_ERROR);
        }
    }

    /**
     * Parse the statement info response in <code>statementInfoResponse</code>. If the response is truncated, a new
     * request is done using {@link #getStatementInfoRequestItems()}
     *
     * @param statementInfoResponse
     *         Statement info response
     */
    protected void parseStatementInfo(final byte[] statementInfoResponse) throws SQLException {
        final StatementInfoProcessor infoProcessor = new StatementInfoProcessor(this, this.getDatabase());
        InfoProcessor.StatementInfo statementInfo = infoProcessor.process(statementInfoResponse);

        setType(statementInfo.getStatementType());
        setFieldDescriptor(statementInfo.getFields());
        setParameterDescriptor(statementInfo.getParameters());
    }
}
