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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jaybird.parser.LocalStatementClass;
import org.firebirdsql.jaybird.parser.LocalStatementType;
import org.firebirdsql.jaybird.parser.StatementDetector;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.util.Primitives;
import org.firebirdsql.jaybird.util.SQLExceptionThrowingFunction;
import org.firebirdsql.jdbc.escape.FBEscapedParser;
import org.firebirdsql.util.InternalApi;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.TRACE;
import static java.util.Collections.emptyList;
import static org.firebirdsql.jdbc.SQLStateConstants.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Implementation of {@link Statement}.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link Statement} and {@link FirebirdStatement} interfaces.
 * </p>
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
@SuppressWarnings({ "RedundantThrows", "SqlSourceToSinkFlow" })
@InternalApi
public class FBStatement extends AbstractStatement implements FirebirdStatement {

    private static final System.Logger log = System.getLogger(FBStatement.class.getName());

    protected final GDSHelper gdsHelper;
    protected final FBObjectListener.StatementListener statementListener;

    protected FbStatement fbStatement;

    //The normally retrieved result set. (no autocommit, not a cached rs).
    private FBResultSet currentRs;

    private SqlCountHolder sqlCountHolder;

    private boolean completed = true;
    private boolean escapedProcessing = true;
    private boolean currentStatementGeneratedKeys;

    // Currently only determined for Firebird statement type SELECT and STORED_PROCEDURE
    private LocalStatementType jbStatementType = LocalStatementType.OTHER;
    protected StatementResult currentStatementResult = StatementResult.NO_MORE_RESULTS;

    // Singleton result indicates it is a stored procedure or singleton [INSERT | UPDATE | DELETE] ... RETURNING ...
    // In Firebird 5+ some RETURNING statements are multi-row, and some are singleton, in Firebird 4 and earlier, they
    // are all singleton.
    protected boolean isSingletonResult;
    // Used for singleton or batch results for getGeneratedKeys, and singleton results of stored procedures
    protected final List<RowValue> specialResult = new ArrayList<>();

    private int maxFieldSize;

    private final FBObjectListener.ResultSetListener resultSetListener = new RSListener();
    protected final FBConnection connection;

    /**
     * Listener for the result sets.
     */
    private final class RSListener implements FBObjectListener.ResultSetListener {

        private boolean rowUpdaterSeparateTransaction;

        /**
         * Notify that result set was closed. This method cleans the result set reference, so that calls to
         * {@link #close()} method will not cause an exception.
         *
         * @param rs
         *         result set that was closed.
         */
        @Override
        public void resultSetClosed(ResultSet rs) throws SQLException {
            currentRs = null;

            // notify listener that statement is completed.
            notifyStatementCompleted();
            performCloseOnCompletion();
        }

        @Override
        public void executionCompleted(FirebirdRowUpdater updater, boolean success) throws SQLException {
            if (rowUpdaterSeparateTransaction) {
                // Only notify when executionStarted started a transaction specifically for the row-updater
                notifyStatementCompleted(success);
            }
        }

        @Override
        public void executionStarted(FirebirdRowUpdater updater) throws SQLException {
            FbTransaction stmtTransaction = fbStatement != null ? fbStatement.getTransaction() : null;
            if (stmtTransaction != null && stmtTransaction.getState() == TransactionState.ACTIVE) {
                // RowUpdater execution will piggyback on the current active transaction
                rowUpdaterSeparateTransaction = false;
            } else {
                rowUpdaterSeparateTransaction = true;
                // Notify statement started by this statement, so a transaction is started to be used by the row updater
                // This should only apply to holdable result sets when updated after the transaction boundary
                notifyStatementStarted(false);
            }
        }
    }

    protected FBStatement(GDSHelper c, ResultSetBehavior rsBehavior,
            FBObjectListener.StatementListener statementListener) throws SQLException {
        super(rsBehavior);
        this.gdsHelper = c;
        this.statementListener = statementListener;

        // TODO Find out if connection is actually ever null, because some parts of the code expect it not to be null
        this.connection = statementListener != null ? statementListener.getConnection() : null;
    }

    private static final Set<StatementState> INVALID_STATEMENT_STATES = EnumSet.of(
            StatementState.ERROR, StatementState.CLOSING, StatementState.CLOSED);

    @Override
    public boolean isValid() {
        return super.isValid() && !INVALID_STATEMENT_STATES.contains(fbStatement.getState());
    }

    @Override
    protected final LockCloseable withLock() {
        return gdsHelper.withLock();
    }

    public void completeStatement(CompletionReason reason) throws SQLException {
        if (currentRs != null && (reason != CompletionReason.COMMIT || currentRs.getHoldability() == ResultSet.CLOSE_CURSORS_AT_COMMIT)) {
            closeResultSet(false, reason);
        }

        if (reason == CompletionReason.CONNECTION_ABORT) {
            completed = true;
            // NOTE This will not "cleanly" end the statement, it might still have objects registered on listeners,
            // and those will not get notified
            fbStatement = null;
            super.close();
        } else {
            notifyStatementCompleted();
        }
    }

    @Override
    public ResultSet executeQuery(String sql) throws  SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            currentStatementGeneratedKeys = false;
            rejectIfTxStmt(sql, JaybirdErrorCodes.jb_executeQueryWithTxStmt);
            notifyStatementStarted();
            try {
                if (!internalExecute(sql)) {
                    throw queryProducedNoResultSet();
                }
                return getResultSet(false);
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    /**
     * Throws an exception if {@code sql} demarcates a transaction boundary (e.g. {@code COMMIT}).
     *
     * @param sql
     *         statement text
     * @param errorCode
     *         error code to use for the exception
     * @throws SQLException
     *         if {@code sql} starts or ends a transaction
     * @since 6
     */
    static void rejectIfTxStmt(String sql, int errorCode) throws SQLException {
        if (StatementDetector.determineLocalStatementType(sql).statementClass() == LocalStatementClass.TRANSACTION_BOUNDARY) {
            FbExceptionBuilder exceptionBuilder = switch (errorCode) {
                // Special handling to ensure a SQLFeatureNotSupportedException is thrown
                case JaybirdErrorCodes.jb_addBatchWithTxStmt, JaybirdErrorCodes.jb_prepareCallWithTxStmt ->
                        FbExceptionBuilder.forException(errorCode);
                default -> FbExceptionBuilder.forNonTransientException(errorCode);
            };
            throw exceptionBuilder.toSQLException();
        }
    }

    protected void notifyStatementStarted() throws SQLException {
        notifyStatementStarted(true);
    }

    protected void notifyStatementStarted(boolean closeResultSet) throws SQLException {
        if (closeResultSet) closeResultSet(false);

        // notify listener that statement execution is about to start
        statementListener.executionStarted(this);

        if (fbStatement != null) {
            fbStatement.setTransaction(gdsHelper.getCurrentTransaction());
        }
        completed = false;
    }

    /**
     * Notifies statement completion.
     * <p>
     * Equivalent to {@code notifyStatementCompleted(true)}
     * </p>
     *
     * @throws SQLException
     *         exception from handling statement completion (e.g. commit or rollback in auto-commit)
     * @see #notifyStatementCompleted(boolean) 
     */
    protected void notifyStatementCompleted() throws SQLException {
        notifyStatementCompleted(true);
    }

    /**
     * Notifies statement completion.
     * <p>
     * Use of {@code success = false} should not be generally used for failing execution. The only difference between
     * {@code true} and {@code false} is whether completion triggers commit or rollback in auto-commit mode, and in
     * general, even for failed execution, a commit should be triggered. The only exception is for batch execution in
     * auto-commit, where we rollback if one statement failed (and this behaviour is specified by JDBC as
     * implementation-specific), and ending a transaction if statement preparation failed in
     * {@link FBPreparedStatement}.
     * </p>
     *
     * @param success
     *         {@code true} notify successful completion, {@code false} for unsuccessful completion
     * @throws SQLException
     *         exception from handling statement completion (e.g. commit or rollback in auto-commit)
     */
    protected void notifyStatementCompleted(boolean success) throws SQLException {
        if (!completed) {
            completed = true;
            statementListener.statementCompleted(this, success);
        }
    }

    /**
     * Variant of {@link #notifyStatementCompleted(boolean)} which will not throw an exception.
     * <p>
     * If the exception received from {@link #notifyStatementCompleted(boolean)} is not a {@link SQLException}, or
     * {@code originalException} is not a {@link SQLException}, the thrown exception will be added as a suppressed
     * exception on {@code originalException}, otherwise it will be set using
     * {@link SQLException#setNextException(SQLException)} on {@code originalException}.
     * </p>
     *
     * @param success
     *         {@code true} notify successful completion, {@code false} for unsuccessful completion
     * @param originalException
     *         original exception that triggered the completion
     * @see #notifyStatementCompleted(boolean)
     */
    void notifyStatementCompleted(boolean success, Exception originalException) {
        try {
            notifyStatementCompleted(success);
        } catch (SQLException e) {
            if (originalException instanceof SQLException sqle) {
                sqle.setNextException(e);
            } else {
                originalException.addSuppressed(e);
            }
        } catch (RuntimeException e) {
            originalException.addSuppressed(e);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            currentStatementGeneratedKeys = false;
            if (executeIfTransactionStatement(sql)) return 0;
            notifyStatementStarted();
            try {
                if (internalExecute(sql)) {
                    throw updateReturnedResultSet();
                }
                int updateCount = getUpdateCountMinZero();
                notifyStatementCompleted();
                return updateCount;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    /**
     * If {@code sql} is a transaction management, executes it appropriately.
     *
     * @param sql
     *         statement text
     * @return {@code true} if {@code sql} contains a transaction management statement, and it was handled,
     * {@code false} if the statement was not a transaction management statement
     * @throws SQLException
     *         if {@code sql} was a transaction management statement, but connection configuration does not allow
     *         execution of transaction management statements, or if handling of the transaction management statement
     *         failed
     * @since 6
     */
    boolean executeIfTransactionStatement(String sql) throws SQLException {
        LocalStatementType statementType = StatementDetector.determineLocalStatementType(sql);
        return switch (statementType) {
            case HARD_COMMIT -> {
                requireConnection().handleHardCommitStatement();
                sqlCountHolder = SqlCountHolder.empty();
                yield true;
            }
            case HARD_ROLLBACK -> {
                requireConnection().handleHardRollbackStatement();
                sqlCountHolder = SqlCountHolder.empty();
                yield true;
            }
            case SET_TRANSACTION -> {
                requireConnection().handleSetTransactionStatement(sql);
                sqlCountHolder = SqlCountHolder.empty();
                yield true;
            }
            default -> false;
        };
    }

    private FBConnection requireConnection() throws SQLException {
        FBConnection connection = this.connection;
        if (connection == null) {
            // This may occur for some types of internal statements
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_statementNotAssociatedWithConnection)
                    .toSQLException();
        }
        return connection;
    }

    static SQLException queryProducedNoResultSet() {
        return new SQLNonTransientException("Query did not produce a result set", SQL_STATE_NO_RESULT_SET);
    }

    static SQLException updateReturnedResultSet() {
        return new SQLNonTransientException("Update statement returned result set", SQL_STATE_INVALID_STMT_TYPE);
    }

    static SQLException batchStatementReturnedResultSet() {
        return new SQLNonTransientException("Statement executed as batch returned result set",
                SQL_STATE_INVALID_STMT_TYPE);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, autoGeneratedKeys)) {
                SQLException e = updateReturnedResultSet();
                notifyStatementCompleted(true, e);
                throw e;
            }
            return getUpdateCountMinZero();
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnIndexes)) {
                SQLException e = updateReturnedResultSet();
                notifyStatementCompleted(true, e);
                throw e;
            }
            return getUpdateCountMinZero();
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnNames)) {
                SQLException e = updateReturnedResultSet();
                notifyStatementCompleted(true, e);
                throw e;
            }
            return getUpdateCountMinZero();
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, autoGeneratedKeys);
            return executeImpl(query);
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, columnIndexes);
            return executeImpl(query);
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, columnNames);
            return executeImpl(query);
        }
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkValidity();
        if (isGeneratedKeyQuery()) {
            return new FBResultSet(fbStatement.getRowDescriptor(), connection, specialResult, resultSetListener, true);
        }
        return new FBResultSet(fbStatement.emptyRowDescriptor(), emptyList());
    }

    @Override
    public void close() throws  SQLException {
        if (isClosed()) return;

        try (var ignored = withLock()) {
            try (FbStatement handle = fbStatement) {
                if (handle != null) {
                    closeResultSet(false, CompletionReason.STATEMENT_CLOSE);
                }
            } finally {
                fbStatement = null;
                batchList = null;
                super.close();
                statementListener.statementClosed(this);
            }
        }
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return maxFieldSize;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        if (max < 0) {
            throw new SQLNonTransientException("Can't set max field size negative", SQL_STATE_INVALID_STRING_LENGTH);
        }
        maxFieldSize = max;
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws  SQLException {
        escapedProcessing = enable;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Firebird 4 and higher also support attachment level and global statement timeouts. This method only reports the
     * value explicitly configured for this statement. In practice, a more stringent timeout might be applied by this
     * attachment level or global statement timeout.
     * </p>
     *
     * @see #setQueryTimeout(int)
     */
    @Override
    public int getQueryTimeout() throws  SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (fbStatement == null) {
                return 0;
            }
            return (int) TimeUnit.MILLISECONDS.toSeconds(fbStatement.getTimeout());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Query timeout is only supported on Firebird 4 and higher, and only for the pure-java wire protocol
     * implementation. For earlier versions or native/embedded connections, the timeout is ignored. The maximum timeout
     * for Firebird 4 is 4294967 seconds, higher values will be handled as if 0 was set. Firebird 4 also has attachment
     * level and global statement timeouts. This configuration governs the statement level statement timeout only. In
     * practice, a more stringent timeout might be applied by this attachment level or global statement timeout.
     * </p>
     * <p><b>Important:</b> Query timeouts in Firebird 4 and higher have an important caveat: for result set producing
     * statements, the timeout covers the time from execution start until the cursor is closed. This includes the time
     * that Firebird waits for your application to fetch more rows. This means that if you execute a {@code SELECT} and
     * take your time processing the results, the statement may be cancelled even when Firebird itself returns rows
     * quickly.
     * </p>
     * <p>
     * A query timeout is not applied for execution of DDL.
     * </p>
     */
    @Override
    public void setQueryTimeout(int seconds) throws  SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            requireStatement().setTimeout(TimeUnit.SECONDS.toMillis(seconds));
        }
    }

    @Override
    public void cancel() throws  SQLException {
        checkValidity();
        if (!supportInfoFor(connection).supportsCancelOperation()) {
            throw new SQLFeatureNotSupportedException("Cancel not supported");
        }
        // TODO This may be problematic, as it could also cancel something other than this statement
        gdsHelper.cancelOperation();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        checkValidity();
        currentStatementGeneratedKeys = false;
        return executeImpl(sql);
    }

    /**
     * Internal implementation of {@link #execute(String)}, so it can be used for normal queries
     * and for queries returning generated keys.
     *
     * @see #execute(String)
     */
    protected boolean executeImpl(String sql) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (executeIfTransactionStatement(sql)) return false;
            notifyStatementStarted();
            try {
                boolean hasResultSet = internalExecute(sql);
                if (!hasResultSet) {
                    notifyStatementCompleted();
                }
                return hasResultSet;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    private boolean executeImpl(GeneratedKeysSupport.Query query) throws SQLException {
        currentStatementGeneratedKeys = query.generatesKeys();
        return executeImpl(query.getQueryString());
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        checkValidity();
        return getResultSet(false);
    }

    protected FBResultSet getResultSet(boolean metaDataQuery) throws  SQLException {
        if (fbStatement == null) {
            throw new SQLException("No statement was executed", SQL_STATE_INVALID_STATEMENT_ID);
        } else if (currentRs != null) {
            return currentRs;
        }

        // A generated keys query does not produce a normal result set (but EXECUTE PROCEDURE or INSERT ... RETURNING without Statement.RETURN_GENERATED_KEYS do)
        if (!isGeneratedKeyQuery() && currentStatementResult.isResultSet()) {
            if (!isSingletonResult) {
                String cursorName = getCursorName();
                if (cursorName != null) {
                    fbStatement.setCursorName(cursorName);
                }
                return currentRs = new FBResultSet(this, resultSetListener, metaDataQuery);
            } else if (!specialResult.isEmpty()) {
                return currentRs = createSpecialResultSet(resultSetListener);
            }
        }
        return null;
    }

    /**
     * Create the result set for {@code specialResult}.
     * <p>
     * Should only be called from {@link #getResultSet(boolean)}. This exists because {@link FBCallableStatement} needs
     * to create the result set in a slightly different way to account for the fact that the blobs were already cached
     * earlier.
     * </p>
     *
     * @param resultSetListener
     *         result set listener (can be {@code null})
     * @return result set wrapping {@code specialResult}
     */
    protected FBResultSet createSpecialResultSet(FBObjectListener.ResultSetListener resultSetListener)
            throws SQLException {
        return new FBResultSet(fbStatement.getRowDescriptor(), connection, specialResult, resultSetListener, true);
    }

    @Override
	public boolean hasOpenResultSet() {
		return currentRs != null;
	}

    /**
     * Equivalent of {@link #getUpdateCount()}, with a minimum value of zero.
     * <p>
     * For use in {@code executeUpdate} methods as the API mandates {@code 0} instead of {@code -1} for no results,
     * and in (emulated) {@code executeBatch} methods for consistency with server-side batch execution.
     * </p>
     * @see #getLargeUpdateCountMinZero()
     */
    protected final int getUpdateCountMinZero() throws SQLException {
        return Math.max(0, getUpdateCount());
    }

    @Override
    public int getUpdateCount() throws  SQLException {
        checkValidity();

        if (currentStatementResult != StatementResult.UPDATE_COUNT) {
            return -1;
        }
        populateSqlCounts();
        int insCount = sqlCountHolder.getIntegerInsertCount();
        int updCount = sqlCountHolder.getIntegerUpdateCount();
        int delCount = sqlCountHolder.getIntegerDeleteCount();
        return Math.max(Math.max(updCount, delCount), insCount);
    }

    private void populateSqlCounts() throws SQLException {
        if (sqlCountHolder == null) {
            sqlCountHolder = fbStatement.getSqlCounts();
        }
    }

    private static final int INSERTED_ROWS_COUNT = 1;
    private static final int UPDATED_ROWS_COUNT = 2;
    private static final int DELETED_ROWS_COUNT = 3;

    private int getChangedRowsCount(int type) throws SQLException {
        if (currentStatementResult != StatementResult.UPDATE_COUNT) {
            return -1;
        }
        populateSqlCounts();
        return switch (type) {
            case INSERTED_ROWS_COUNT -> sqlCountHolder.getIntegerInsertCount();
            case UPDATED_ROWS_COUNT -> sqlCountHolder.getIntegerUpdateCount();
            case DELETED_ROWS_COUNT -> sqlCountHolder.getIntegerDeleteCount();
            default -> throw new IllegalArgumentException(format("Specified type %d is unknown", type));
        };
    }

    @Override
    public int getDeletedRowsCount() throws SQLException {
    	return getChangedRowsCount(DELETED_ROWS_COUNT);
    }

    @Override
	public int getInsertedRowsCount() throws SQLException {
		return getChangedRowsCount(INSERTED_ROWS_COUNT);
	}

    @Override
	public int getUpdatedRowsCount() throws SQLException {
		return getChangedRowsCount(UPDATED_ROWS_COUNT);
	}

    @Override
    public boolean getMoreResults() throws  SQLException {
        return getMoreResults(Statement.CLOSE_ALL_RESULTS);
    }

    @Override
    public boolean getMoreResults(int mode) throws SQLException {
        checkValidity();

        boolean closeResultSet = mode == Statement.CLOSE_ALL_RESULTS
                || mode == Statement.CLOSE_CURRENT_RESULT;

        if (currentStatementResult.isResultSet() && closeResultSet) {
            closeResultSet(true);
        }
        currentStatementResult = currentStatementResult.nextResult();

        // Technically the statement below is always false, as only the first result is ever a ResultSet
        return currentStatementResult.isResultSet();
    }

    private List<String> batchList = new ArrayList<>();

    @Override
    public void addBatch(String sql) throws  SQLException {
        checkValidity();
        rejectIfTxStmt(sql, JaybirdErrorCodes.jb_addBatchWithTxStmt);
        try (LockCloseable ignored = withLock()) {
            batchList.add(sql);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        checkValidity();
        try (LockCloseable ignored = withLock()) {
            batchList.clear();
        }
    }

    @Override
    public final int[] executeBatch() throws SQLException {
        if (connection.getAutoCommit()) {
            addWarning(new SQLWarning("Batch updates should be run with auto-commit disabled", SQL_STATE_WARNING));
        }

        return Primitives.toIntArray(executeBatchInternal());
    }

    protected List<Long> executeBatchInternal() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            currentStatementGeneratedKeys = false;
            if (batchList.isEmpty()) {
                return emptyList();
            }
            List<Long> responses = new ArrayList<>(batchList.size());
            notifyStatementStarted();
            try {
                for (String sql : batchList) {
                    responses.add(executeSingleForBatch(sql));
                }
                notifyStatementCompleted();
                return responses;
            } catch (SQLException e) {
                BatchUpdateException batchUpdateException = createBatchUpdateException(e, responses);
                notifyStatementCompleted(false, batchUpdateException);
                throw batchUpdateException;
            } catch (RuntimeException e) {
                notifyStatementCompleted(false, e);
                throw e;
            } finally {
                clearBatch();
            }
        }
    }

    private long executeSingleForBatch(String sql) throws SQLException {
        if (internalExecute(sql)) {
            throw batchStatementReturnedResultSet();
        }
        return getLargeUpdateCountMinZero();
    }

    protected final BatchUpdateException createBatchUpdateException(SQLException cause,
            List<? extends Number> updateCounts) {
        return createBatchUpdateException(cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), updateCounts,
                cause);
    }

    protected final BatchUpdateException createBatchUpdateException(String reason, String sqlState, int vendorCode,
            List<? extends Number> updateCounts, Throwable cause) {
        return new BatchUpdateException(reason, sqlState, vendorCode, Primitives.toLongArray(updateCounts), cause);
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkValidity();
        return connection;
    }

    @Override
    protected final FbStatement getStatementHandle() throws SQLException {
        checkValidity();
        return fbStatement;
    }

    void closeResultSet(boolean notifyListener) throws SQLException {
        closeResultSet(notifyListener, CompletionReason.OTHER);
    }

    void closeResultSet(boolean notifyListener, CompletionReason completionReason) throws SQLException {
        boolean wasCompleted = completed;

        try {
            FBResultSet currentRs = this.currentRs;
            if (currentRs != null) {
                this.currentRs = null;
                currentRs.close(notifyListener, completionReason);
            }
        } finally {
            try {
                if (fbStatement != null) {
                    // Primarily for execute() cases where getResultSet() was never called, ensures cursor is closed for
                    // all code paths (a no-op if there is no open cursor)
                    fbStatement.ensureClosedCursor(completionReason.isTransactionEnd());
                }
            } finally {
                if (notifyListener && !wasCompleted) {
                    statementListener.statementCompleted(this);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getResultSet()} instead, will be removed in Jaybird 7
     */
    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public ResultSet getCurrentResultSet() throws SQLException {
        return getResultSet();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface == null) return false;
        if (FbStatement.class.isAssignableFrom(iface)) {
            try (LockCloseable ignored = withLock()) {
                return iface.isInstance(fbStatement);
            }
        }
        return iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == null) {
            throw new SQLException("Unable to unwrap to class (null)");
        } else if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        } else if (FbStatement.class.isAssignableFrom(iface)) {
            try (LockCloseable ignored = withLock()) {
                if (iface.isInstance(fbStatement)) {
                    return iface.cast(fbStatement);
                }
            }
        }
        throw new SQLException("Unable to unwrap to class " + iface.getName());
    }

    protected boolean internalExecute(String sql) throws SQLException {
        checkValidity();

        prepareFixedStatement(sql);
        return internalExecute(RowValue.EMPTY_ROW_VALUE);
    }

    protected boolean internalExecute(RowValue rowValue) throws SQLException {
        try {
            fbStatement.execute(rowValue);
            boolean hasResultSet = currentStatementResult.isResultSet();
            if (hasResultSet && isGeneratedKeyQuery()) {
                fetchMultiRowGeneratedKeys();
                return false;
            }
            return hasResultSet;
        } catch (SQLException e) {
            currentStatementResult = StatementResult.NO_MORE_RESULTS;
            throw e;
        }
    }

    private void fetchMultiRowGeneratedKeys() throws SQLException {
        RowsFetchedListener rowsFetchedListener = new RowsFetchedListener();
        try {
            fbStatement.addStatementListener(rowsFetchedListener);
            while (!rowsFetchedListener.isAllRowsFetched()) {
                fbStatement.fetchRows(Integer.MAX_VALUE);
            }
            fbStatement.closeCursor();
            currentStatementResult = StatementResult.UPDATE_COUNT;
        } finally {
            fbStatement.removeStatementListener(rowsFetchedListener);
        }
    }

    protected void prepareFixedStatement(String sql) throws SQLException {
        // TODO: Statement should be created and allocated at FBStatement creation only.
        if (fbStatement == null) {
            requireStatement();
        } else {
            fbStatement.setTransaction(gdsHelper.getCurrentTransaction());
        }
        String statementText = escapedProcessing ? nativeSQL(sql) : sql;
        fbStatement.prepare(statementText);
        StatementType fbStatementType = fbStatement.getType();
        jbStatementType = fbStatementType == StatementType.SELECT || fbStatementType == StatementType.STORED_PROCEDURE
                ? StatementDetector.determineLocalStatementType(statementText) : LocalStatementType.OTHER;
    }

    protected FbStatement requireStatement() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (fbStatement == null) {
                fbStatement = gdsHelper.allocateStatement();
                fbStatement.addStatementListener(createStatementListener());
                if (needsScrollableCursorEnabled()) {
                    fbStatement.setCursorFlag(CursorFlag.CURSOR_TYPE_SCROLLABLE);
                }
            }
            return fbStatement;
        }
    }

    protected boolean needsScrollableCursorEnabled() {
        ResultSetBehavior resultSetBehavior = resultSetBehavior();
        return resultSetBehavior.isScrollable() && resultSetBehavior.isCloseCursorsAtCommit()
               && connection != null && connection.isScrollableCursor(PropertyConstants.SCROLLABLE_CURSOR_SERVER)
               && fbStatement.supportsFetchScroll();
    }

    protected String nativeSQL(String sql) throws SQLException {
        if (connection != null) {
            return connection.nativeSQL(sql);
        } else {
            return FBEscapedParser.toNativeSql(sql);
        }
    }

    /**
     * @return {@code true} when the current statement is expected to return generated keys, {@code false} otherwise.
     */
    protected boolean isGeneratedKeyQuery() {
        return currentStatementGeneratedKeys;
    }

    // Other cases where there is no execution plan is handled by validity check or server-side
    private static final Set<StatementState> NO_EXECUTION_PLAN_STATE =
            Set.of(StatementState.NEW, StatementState.ALLOCATED);

    private String getExecutionPlan(SQLExceptionThrowingFunction<FbStatement, String> getPlanFunction)
            throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (fbStatement == null || NO_EXECUTION_PLAN_STATE.contains(fbStatement.getState())) {
                return noExecutionPlan();
            }
            return getPlanFunction.apply(fbStatement);
        }
    }

    private static String noExecutionPlan() throws SQLException {
        throw new SQLException("No statement was executed or prepared, plan cannot be obtained",
                SQL_STATE_INVALID_STATEMENT_ID);
    }

    @Override
    public final String getExecutionPlan() throws SQLException {
        return getExecutionPlan(FbStatement::getExecutionPlan);
    }

    @Override
    public final String getExplainedExecutionPlan() throws SQLException {
        return getExecutionPlan(FbStatement::getExplainedExecutionPlan);
    }

    @Override
    public final int getStatementType() {
        if (fbStatement == null) {
            return StatementType.NONE.getStatementTypeCode();
        }
        return fbStatement.getType().getStatementTypeCode();
    }

    /**
     * Equivalent of {@link #getLargeUpdateCount()}, with a minimum value of zero.
     * <p>
     * For use in {@code executeLargeUpdate} methods as the API mandates {@code 0} instead of {@code -1} for no results,
     * and in (emulated) {@code executeBatch} methods for consistency with server-side batch execution.
     * </p>
     * @see #getUpdateCountMinZero()
     */
    protected final long getLargeUpdateCountMinZero() throws SQLException {
        return Math.max(0L, getLargeUpdateCount());
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        checkValidity();

        if (currentStatementResult != StatementResult.UPDATE_COUNT) {
            return -1;
        }
        populateSqlCounts();
        final long insCount = sqlCountHolder.insertCount();
        final long updCount = sqlCountHolder.updateCount();
        final long delCount = sqlCountHolder.deleteCount();
        return Math.max(Math.max(insCount, updCount), delCount);
    }

    @Override
    public final long[] executeLargeBatch() throws SQLException {
        if (connection.getAutoCommit()) {
            addWarning(new SQLWarning("Batch updates should be run with auto-commit disabled", SQL_STATE_WARNING));
        }

        return Primitives.toLongArray(executeBatchInternal());
    }

    @Override
    public final long executeLargeUpdate(String sql) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            executeUpdate(sql);
            return getLargeUpdateCountMinZero();
        }
    }

    @Override
    public final long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, autoGeneratedKeys)) {
                throw updateReturnedResultSet();
            }
            return getLargeUpdateCountMinZero();
        }
    }

    @Override
    public final long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnIndexes)) {
                throw updateReturnedResultSet();
            }
            return getLargeUpdateCountMinZero();
        }
    }

    @Override
    public final long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnNames)) {
                throw updateReturnedResultSet();
            }
            return getLargeUpdateCountMinZero();
        }
    }

    /**
     * Returns a {@code String} enclosed in single quotes. Any occurrence of a single quote within the string will be
     * replaced by two single quotes.
     * <p>
     * For a dialect 3 database, this will behave exactly like the JDBC 4.3 default implementation. For a
     * dialect 1 database this will quote literals with double quotes and escape double quotes by doubling.
     * </p>
     *
     * @param val a character string
     * @return A string enclosed by single quotes with every single quote
     * converted to two single quotes
     * @throws NullPointerException if val is {@code null}
     * @throws SQLException if a database access error occurs
     */
    @Override
    public String enquoteLiteral(String val)  throws SQLException {
        if (gdsHelper.getCurrentDatabase().getDatabaseDialect() == 1) {
            return '"' + val.replace("\"", "\"\"") + '"';
        }
        return "'" + val.replace("'", "''") +  "'";
    }

    /**
     * @see #enquoteLiteral(String)
     */
    @Override
    public String enquoteNCharLiteral(String val)  throws SQLException {
        return enquoteLiteral(val);
    }

    // NOTE: This intentionally does not take case sensitivity into account
    private static final Pattern SIMPLE_IDENTIFIER_PATTERN = Pattern.compile("\\p{Alpha}[\\p{Alnum}_$]*");

    /**
     * Returns a SQL identifier. If {@code identifier} is a simple SQL identifier:
     * <ul>
     * <li>Return the original value if {@code alwaysQuote} is
     * {@code false}</li>
     * <li>Return a delimited identifier if {@code alwaysQuote} is
     * {@code true}</li>
     * </ul>
     *
     * @param identifier a SQL identifier
     * @param alwaysQuote indicates if a simple SQL identifier should be
     * returned as a quoted identifier
     * @return A simple SQL identifier or a delimited identifier
     * @throws SQLException if identifier is not a valid identifier
     * @throws SQLFeatureNotSupportedException if the datasource does not support
     * delimited identifiers (ie: a dialect 1 database)
     * @throws NullPointerException if identifier is {@code null}
     */
    @Override
    public String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SQLException {
        int len = identifier.length();
        if (len < 1 || len > connection.getMetaData().getMaxColumnNameLength()) {
            throw new SQLException("Invalid name");
        }
        if (!alwaysQuote && SIMPLE_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        QuoteStrategy quoteStrategy = connection.getQuoteStrategy();
        if (quoteStrategy == QuoteStrategy.DIALECT_1) {
            throw new SQLFeatureNotSupportedException("Quoted identifiers not supported in dialect 1");
        }
        if (identifier.matches("^\".+\"$")) {
            // We assume double quotes are already properly escaped within
            return identifier;
        }
        return quoteStrategy.quoteObjectName(identifier);
    }

    @Override
    public boolean isSimpleIdentifier(String identifier) throws SQLException {
        int len = identifier.length();
        return len >= 1 && len <= connection.getMetaData().getMaxColumnNameLength()
                && SIMPLE_IDENTIFIER_PATTERN.matcher(identifier).matches();
    }

    /**
     * The current result of a statement.
     */
    protected enum StatementResult {
        // Normal SELECT (including selectable stored procedures) don't have an update count
        RESULT_SET(true) {
            @Override
            public StatementResult nextResult() {
                return NO_MORE_RESULTS;
            }
        },
        // Statements like EXECUTE BLOCK with SUSPEND and (Firebird 5) DML with RETURNING have an update count
        RESULT_SET_WITH_UPDATE_COUNT(true) {
            @Override
            public StatementResult nextResult() {
                return UPDATE_COUNT;
            }
        },
        UPDATE_COUNT(false) {
            @Override
            public StatementResult nextResult() {
                return NO_MORE_RESULTS;
            }
        },
        NO_MORE_RESULTS(false) {
            @Override
            public StatementResult nextResult() {
                return NO_MORE_RESULTS;
            }
        };

        private final boolean resultSet;

        StatementResult(boolean resultSet) {
            this.resultSet = resultSet;
        }

        /**
         * @return Next result
         */
        public abstract StatementResult nextResult();

        public final boolean isResultSet() {
            return resultSet;
        }
    }

    /**
     * Creates the {@link org.firebirdsql.gds.ng.listeners.StatementListener} to be associated with the instance of
     * {@link org.firebirdsql.gds.ng.FbStatement} created for this {@link FBStatement} or subclasses.
     *
     * @return instance of {@link org.firebirdsql.gds.ng.listeners.StatementListener}
     */
    protected StatementListener createStatementListener() {
        return new FBStatementListener();
    }

    private final class FBStatementListener implements StatementListener {
        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            if (isUnexpectedSender(sender)) return;
            // TODO May need extra condition to distinguish between singleton result of EXECUTE PROCEDURE and INSERT ... RETURNING ...
            if (isSingletonResult) {
                specialResult.clear();
                specialResult.add(rowValue);
            } else if (isGeneratedKeyQuery()) {
                specialResult.add(rowValue);
            }
        }

        @Override
        public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
            if (isUnexpectedSender(sender)) return;
            // TODO If currentStatementResult has result set, create ResultSet and attach listener to sender?
            currentStatementResult = determineInitialStatementResult(hasResultSet, hasSingletonResult);
            isSingletonResult = hasSingletonResult;
        }

        private StatementResult determineInitialStatementResult(boolean hasResultSet, boolean hasSingletonResult) {
            if (hasResultSet || hasSingletonResult && !isGeneratedKeyQuery()) {
                if (jbStatementType == LocalStatementType.SELECT) {
                    return StatementResult.RESULT_SET;
                }
                return StatementResult.RESULT_SET_WITH_UPDATE_COUNT;
            } else if (fbStatement.getType().isTypeWithUpdateCounts()
                    && jbStatementType != LocalStatementType.EXECUTE_PROCEDURE) {
                return StatementResult.UPDATE_COUNT;
            } else {
                return StatementResult.NO_MORE_RESULTS;
            }
        }

        @Override
        public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
            if (isUnexpectedSender(sender)) return;
            if (newState == StatementState.EXECUTING) {
                specialResult.clear();
                sqlCountHolder = null;
                currentStatementResult = StatementResult.NO_MORE_RESULTS;
                isSingletonResult = false;
                try {
                    clearWarnings();
                } catch (SQLException e) {
                    throw new AssertionError("Unexpected SQLException", e);
                }
            }
        }

        @Override
        public void warningReceived(FbStatement sender, SQLWarning warning) {
            if (isUnexpectedSender(sender)) return;
            addWarning(warning);
        }

        @Override
        public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
            if (isUnexpectedSender(sender)) return;
            sqlCountHolder = sqlCounts;
        }

        private boolean isUnexpectedSender(FbStatement sender) {
            if (sender != fbStatement) {
                log.log(TRACE, "Received statement listener update from unrelated statement [{0}]", sender);
                sender.removeStatementListener(this);
                return true;
            }
            return false;
        }
    }

    private static final class RowsFetchedListener implements StatementListener {

        private boolean allRowsFetched;

        @Override
        public void afterLast(FbStatement sender) {
            allRowsFetched = true;
        }

        public boolean isAllRowsFetched() {
            return allRowsFetched;
        }
    }
}
