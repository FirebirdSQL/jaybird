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
import org.firebirdsql.jaybird.parser.FirebirdReservedWords;
import org.firebirdsql.jaybird.parser.LocalStatementType;
import org.firebirdsql.jaybird.parser.SqlParser;
import org.firebirdsql.jaybird.parser.StatementDetector;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jdbc.escape.FBEscapedParser;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.Primitives;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Implementation of {@link Statement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public class FBStatement implements FirebirdStatement {

    private static final org.firebirdsql.logging.Logger log = LoggerFactory.getLogger(FBStatement.class);

    private static final AtomicInteger STATEMENT_ID_GENERATOR = new AtomicInteger();

    private final int localStatementId = STATEMENT_ID_GENERATOR.incrementAndGet();
    protected final GDSHelper gdsHelper;
    protected final FBObjectListener.StatementListener statementListener;

    protected FbStatement fbStatement;

    //The normally retrieved result set. (no autocommit, not a cached rs).
    private FBResultSet currentRs;

    private SqlCountHolder sqlCountHolder;

    private boolean closed;
    protected boolean completed = true;
    private boolean escapedProcessing = true;
    private volatile boolean closeOnCompletion;
    private boolean currentStatementGeneratedKeys;

	protected SQLWarning firstWarning;

    // Currently only determined for Firebird statement type SELECT and STORED_PROCEDURE
    private LocalStatementType jbStatementType = LocalStatementType.OTHER;
    protected StatementResult currentStatementResult = StatementResult.NO_MORE_RESULTS;

    // Singleton result indicates it is a stored procedure or singleton [INSERT | UPDATE | DELETE] ... RETURNING ...
    // In Firebird 5+ some RETURNING statements are multi-row, and some are singleton, in Firebird 4 and earlier, they
    // are all singleton.
    protected boolean isSingletonResult;
    // Used for singleton or batch results for getGeneratedKeys, and singleton results of stored procedures
    protected final List<RowValue> specialResult = new ArrayList<>();

    protected int maxRows;
    protected int fetchSize;
    private int maxFieldSize;
    private String cursorName;

    private final int rsConcurrency;
    private final int rsType;
    private final int rsHoldability;
    private int fetchDirection = ResultSet.FETCH_FORWARD;

    private final FBObjectListener.ResultSetListener resultSetListener = new RSListener();
    protected final FBConnection connection;

    /**
     * Listener for the result sets.
     */
    private final class RSListener implements FBObjectListener.ResultSetListener {

        private boolean rowUpdaterSeparateTransaction;

        /**
         * Notify that result set was closed. This method cleans the result
         * set reference, so that call to {@link #close()} method will not cause
         * exception.
         *
         * @param rs result set that was closed.
         */
        @Override
        public void resultSetClosed(ResultSet rs) throws SQLException {
            currentRs = null;

            // notify listener that statement is completed.
            notifyStatementCompleted();
            if (closeOnCompletion) {
                close();
            }
        }

        @Override
        public void allRowsFetched(ResultSet rs) throws SQLException {

            /*
             * According to the JDBC 3.0 specification (p.62) the result set
             * is closed in the autocommit mode if one of the following occurs:
             *
             * - all of the rows have been retrieved
             * - the associated Statement object is re-executed
             * - another Statement object is executed on the same connection
             */

            // according to the specification we close the result set and 
            // generate the "resultSetClosed" event, that in turn generates
            // the "statementCompleted" event

            if (connection != null && connection.getAutoCommit())
                rs.close();
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

    protected FBStatement(GDSHelper c, int rsType, int rsConcurrency, int rsHoldability, FBObjectListener.StatementListener statementListener) throws SQLException {
        this.gdsHelper = c;

        this.rsConcurrency = rsConcurrency;
        this.rsType = rsType;
        this.rsHoldability = rsHoldability;

        this.statementListener = statementListener;

        // TODO Find out if connection is actually ever null, because some parts of the code expect it not to be null
        this.connection = statementListener != null ? statementListener.getConnection() : null;

        closed = false;
    }

    String getCursorName() {
        return cursorName;
    }

    private static final Set<StatementState> INVALID_STATEMENT_STATES = EnumSet.of(
            StatementState.ERROR, StatementState.CLOSING, StatementState.CLOSED);

    @Override
    public boolean isValid() {
        return !closed && !INVALID_STATEMENT_STATES.contains(fbStatement.getState());
    }

    /**
     * @see FbAttachment#withLock()
     */
    protected final LockCloseable withLock() {
        return gdsHelper.withLock();
    }

    public void completeStatement() throws SQLException {
        completeStatement(CompletionReason.OTHER);
    }

    public void completeStatement(CompletionReason reason) throws SQLException {
        if (currentRs != null && (reason != CompletionReason.COMMIT || currentRs.getHoldability() == ResultSet.CLOSE_CURSORS_AT_COMMIT)) {
            closeResultSet(false, reason);
        }

        if (!completed)
            notifyStatementCompleted();
    }

    @Override
    public ResultSet executeQuery(String sql) throws  SQLException {
        checkValidity();
        currentStatementGeneratedKeys = false;
        try (LockCloseable ignored = withLock()) {
            notifyStatementStarted();
            if (!internalExecute(sql)) {
                throw new FBSQLException("Query did not return a result set.",
                        SQLStateConstants.SQL_STATE_NO_RESULT_SET);
            }

            return getResultSet();
        }
    }

    protected void notifyStatementStarted() throws SQLException {
        notifyStatementStarted(true);
    }

    protected void notifyStatementStarted(boolean closeResultSet) throws SQLException {
        if (closeResultSet)
            closeResultSet(false);

        // notify listener that statement execution is about to start
        statementListener.executionStarted(this);

        if (fbStatement != null) {
            fbStatement.setTransaction(gdsHelper.getCurrentTransaction());
        }
        completed = false;
    }

    protected void notifyStatementCompleted() throws SQLException {
        notifyStatementCompleted(true);
    }

    protected void notifyStatementCompleted(boolean success) throws SQLException {
        completed = true;
        statementListener.statementCompleted(this, success);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkValidity();
        currentStatementGeneratedKeys = false;
        try (LockCloseable ignored = withLock()) {
            notifyStatementStarted();
            try {
                if (internalExecute(sql)) {
                    throw new FBSQLException("Update statement returned results.");
                }
                return getUpdateCountMinZero();
            } finally {
                notifyStatementCompleted();
            }
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, autoGeneratedKeys)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getUpdateCountMinZero();
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnIndexes)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getUpdateCountMinZero();
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnNames)) {
                throw new FBSQLException("Update statement returned results.");
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
            return new FBResultSet(fbStatement.getRowDescriptor(), connection, new ArrayList<>(specialResult),
                    resultSetListener, true, false);
        }
        return new FBResultSet(fbStatement.emptyRowDescriptor(), emptyList());
    }

    @Override
    public void close() throws  SQLException {
        close(true);
    }

    void close(boolean ignoreAlreadyClosed) throws SQLException {
        if (isClosed()) {
            if (ignoreAlreadyClosed)
                return;

            throw new FBSQLException("This statement is already closed.");
        }

        try (LockCloseable ignored = withLock()) {
            if (fbStatement != null) {
                try {
                    try {
                        closeResultSet(false, CompletionReason.STATEMENT_CLOSE);
                    } finally {
                        //may need ensureTransaction?
                        fbStatement.close();
                    }
                } finally {
                    fbStatement = null;
                    batchList = null;
                }
            }
        }

        closed = true;
        statementListener.statementClosed(this);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return maxFieldSize;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        if (max < 0)
            throw new FBSQLException("Can't set max field size negative",
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        else
            maxFieldSize = max;
    }

    @Override
    public int getMaxRows() throws  SQLException {
        return maxRows;
    }

    @Override
    public void setMaxRows(int max) throws  SQLException {
        if (max < 0)
            throw new FBSQLException("Max rows can't be less than 0", SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        else
            maxRows = max;
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
    public SQLWarning getWarnings() throws  SQLException {
        return firstWarning;
    }

    @Override
    public void clearWarnings() throws  SQLException {
        firstWarning = null;
    }

    @Override
    public void setCursorName(String name) throws  SQLException {
        this.cursorName = name;
    }

    boolean isUpdatableCursor() {
        return cursorName != null;
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
            notifyStatementStarted();
            boolean hasResultSet = false;
            try {
                hasResultSet = internalExecute(sql);
            } finally {
                if (!hasResultSet) {
                    notifyStatementCompleted();
                }
            }
            return hasResultSet;
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

    public ResultSet getResultSet(boolean metaDataQuery) throws  SQLException {
        if (fbStatement == null) {
            throw new FBSQLException("No statement was executed.");
        }

        if (cursorName != null) {
            fbStatement.setCursorName(cursorName);
        }

        if (currentRs != null) {
            throw new FBSQLException("Only one result set at a time/statement.");
        }

        // A generated keys query does not produce a normal result set (but EXECUTE PROCEDURE or INSERT ... RETURNING without Statement.RETURN_GENERATED_KEYS do)
        // TODO Behavior might not be correct for callable statement implementation
        if (!isGeneratedKeyQuery() && currentStatementResult.isResultSet()) {
            if (!isSingletonResult) {
                currentRs = new FBResultSet(connection, this, fbStatement, resultSetListener, metaDataQuery, rsType,
                        rsConcurrency, rsHoldability, false);
            } else if (!specialResult.isEmpty()) {
                currentRs = createSpecialResultSet(resultSetListener);
            }
            return currentRs;
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
        return new FBResultSet(fbStatement.getRowDescriptor(), connection, new ArrayList<>(specialResult),
                resultSetListener, true, false);
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
        switch (type) {
        case INSERTED_ROWS_COUNT:
            return sqlCountHolder.getIntegerInsertCount();
        case UPDATED_ROWS_COUNT:
            return sqlCountHolder.getIntegerUpdateCount();
        case DELETED_ROWS_COUNT:
            return sqlCountHolder.getIntegerDeleteCount();
        default:
            throw new IllegalArgumentException(format("Specified type %d is unknown.", type));
        }
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

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        checkValidity();
        switch (direction) {
        case ResultSet.FETCH_FORWARD:
        case ResultSet.FETCH_REVERSE:
        case ResultSet.FETCH_UNKNOWN:
            fetchDirection = direction;
            break;
        default:
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidFetchDirection)
                    .messageParameter(direction)
                    .toSQLException();
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        checkValidity();
        return fetchDirection;
    }

    @Override
    public void setFetchSize(int rows) throws  SQLException {
        checkValidity();
        if (rows < 0)
            throw new FBSQLException("Can't set negative fetch size", SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        else
            fetchSize = rows;
    }

    @Override
    public int getFetchSize() throws  SQLException {
        checkValidity();
        return fetchSize;
    }

    @Override
    public int getResultSetConcurrency() throws  SQLException {
        return rsConcurrency;
    }

    @Override
    public int getResultSetType()  throws  SQLException {
        return rsType;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return rsHoldability;
    }

    private List<String> batchList = new ArrayList<>();

    @Override
    public void addBatch(String sql) throws  SQLException {
        checkValidity();
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
            addWarning(new SQLWarning("Batch updates should be run with auto-commit disabled.", "01000"));
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

            notifyStatementStarted();
            boolean success = false;
            try {
                List<Long> responses = new ArrayList<>(batchList.size());
                try {
                    for (String sql : batchList) {
                        executeSingleForBatch(responses, sql);
                    }

                    success = true;
                    return responses;
                } catch (SQLException e) {
                    throw createBatchUpdateException(e.getMessage(), e.getSQLState(),
                            e.getErrorCode(), Primitives.toLongArray(responses), e);
                } finally {
                    clearBatch();
                }
            } finally {
                notifyStatementCompleted(success);
            }
        }
    }

    private void executeSingleForBatch(List<Long> responses, String sql) throws SQLException {
        if (internalExecute(sql)) {
            // TODO SQL state?
            throw createBatchUpdateException(
                    "Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_GENERAL_ERROR, 0, Primitives.toLongArray(responses), null);
        } else {
            responses.add(getLargeUpdateCountMinZero());
        }
    }

    protected final BatchUpdateException createBatchUpdateException(String reason, String SQLState, int vendorCode,
            long[] updateCounts, Throwable cause) {
        return new BatchUpdateException(reason, SQLState, vendorCode, updateCounts, cause);
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkValidity();
        return connection;
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

    @Override
    public ResultSet getCurrentResultSet() throws SQLException {
        return currentRs;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        checkValidity();
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        checkValidity();
        // ignore the hint
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

    @Override
    public void closeOnCompletion() {
        closeOnCompletion = true;
    }

    @Override
    public boolean isCloseOnCompletion() {
        return closeOnCompletion;
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
                ? determineJaybirdStatementType(statementText)
                : LocalStatementType.OTHER;
    }

    private static LocalStatementType determineJaybirdStatementType(String statementText) {
        StatementDetector detector = new StatementDetector(false);
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(statementText)
                .parse();
        return detector.getStatementType();
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
        return rsType != ResultSet.TYPE_FORWARD_ONLY && rsHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT
                && connection != null && connection.isScrollableCursor(PropertyConstants.SCROLLABLE_CURSOR_SERVER)
                && fbStatement.supportsFetchScroll();
    }

    protected void addWarning(SQLWarning warning) {
        if (firstWarning == null) {
            firstWarning = warning;
        } else {
            firstWarning.setNextWarning(warning);
        }
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

    /**
     * Get the execution plan of this PreparedStatement
     *
     * @return The execution plan of the statement
     */
    String getExecutionPlan() throws SQLException {
        return fbStatement.getExecutionPlan();
    }

    /**
     * Get the detailed execution plan of this PreparedStatement
     *
     * @return The detailed execution plan of the statement
     */
    String getExplainedExecutionPlan() throws SQLException {
        return fbStatement.getExplainedExecutionPlan();
    }

    @Override
    public String getLastExecutionPlan() throws SQLException {
        checkValidity();

        if (fbStatement == null) {
            throw new FBSQLException("No statement was executed, plan cannot be obtained.");
        }

        return getExecutionPlan();
    }

    @Override
    public String getLastExplainedExecutionPlan() throws SQLException {
        checkValidity();

        if (fbStatement == null) {
            throw new FBSQLException("No statement was executed, detailed plan cannot be obtained.");
        }

        return getExplainedExecutionPlan();
    }

    /**
     * Get the statement type of this PreparedStatement.
     * The returned value will be one of the {@code TYPE_*} constant
     * values.
     *
     * @return The identifier for the given statement's type
     */
    int getStatementType() throws SQLException {
        if (fbStatement == null) {
            return StatementType.NONE.getStatementTypeCode();
        }
        return fbStatement.getType().getStatementTypeCode();
    }

    /**
     * Check if this statement is valid. This method should be invoked before
     * executing any action which requires a valid connection.
     *
     * @throws SQLException if this Statement has been closed and cannot be used anymore.
     */
    protected void checkValidity() throws SQLException {
        if (isClosed()) {
            throw new FBSQLException("Statement is already closed.", SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID);
        }
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
        final long insCount = sqlCountHolder.getLongInsertCount();
        final long updCount = sqlCountHolder.getLongUpdateCount();
        final long delCount = sqlCountHolder.getLongDeleteCount();
        return Math.max(Math.max(insCount, updCount), delCount);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support maxRows exceeding {@link Integer#MAX_VALUE}, if a larger value is set, Jaybird will
     * add a warning to the statement and reset the maximum to 0.
     * </p>
     */
    public void setLargeMaxRows(long max) throws SQLException {
        if (max > Integer.MAX_VALUE) {
            addWarning(new SQLWarning(
                    format("Implementation limit: maxRows cannot exceed Integer.MAX_VALUE, value was %d, reset to 0", max),
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE));
            max = 0;
        }
        setMaxRows((int) max);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support maxRows exceeding {@link Integer#MAX_VALUE}, the return value of this method is the
     * same as {@link #getMaxRows()}.
     * </p>
     */
    public long getLargeMaxRows() throws SQLException {
        return getMaxRows();
    }

    public final long[] executeLargeBatch() throws SQLException {
        if (connection.getAutoCommit()) {
            addWarning(new SQLWarning("Batch updates should be run with auto-commit disabled.", "01000"));
        }

        return Primitives.toLongArray(executeBatchInternal());
    }

    public final long executeLargeUpdate(String sql) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            executeUpdate(sql);
            return getLargeUpdateCountMinZero();
        }
    }

    public final long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, autoGeneratedKeys)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getLargeUpdateCountMinZero();
        }
    }

    public final long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnIndexes)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getLargeUpdateCountMinZero();
        }
    }

    public final long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (execute(sql, columnNames)) {
                throw new FBSQLException("Update statement returned results.");
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
    public String enquoteLiteral(String val)  throws SQLException {
        if (gdsHelper.getCurrentDatabase().getDatabaseDialect() == 1) {
            return '"' + val.replace("\"", "\"\"") + '"';
        }
        return "'" + val.replace("'", "''") +  "'";
    }

    /**
     * @see #enquoteLiteral(String)
     */
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
    public String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SQLException {
        int len = identifier.length();
        if (len < 1 || len > connection.getMetaData().getMaxColumnNameLength()) {
            throw new SQLException("Invalid name");
        }
        if (!alwaysQuote && SIMPLE_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        QuoteStrategy quoteStrategy = connection.getQuoteStrategy();
        if (quoteStrategy == QuoteStrategy.NO_QUOTES) {
            throw new SQLFeatureNotSupportedException("Quoted identifiers not supported in dialect 1");
        }
        if (identifier.matches("^\".+\"$")) {
            // We assume double quotes are already properly escaped within
            return identifier;
        }
        return quoteStrategy.quoteObjectName(identifier);
    }

    public boolean isSimpleIdentifier(String identifier) throws SQLException {
        int len = identifier.length();
        return len >= 1 && len <= connection.getMetaData().getMaxColumnNameLength()
                && SIMPLE_IDENTIFIER_PATTERN.matcher(identifier).matches();
    }

    @Override
    public final int getLocalStatementId() {
        return localStatementId;
    }

    @Override
    public final int hashCode() {
        return localStatementId;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof FirebirdStatement)) {
            return false;
        }

        FirebirdStatement otherStmt = (FirebirdStatement) other;
        return this.localStatementId == otherStmt.getLocalStatementId();
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
            switch (newState) {
            case PREPARED:
                // TODO Evaluate correct changes when state goes to prepared
                break;
            case EXECUTING:
                specialResult.clear();
                sqlCountHolder = null;
                currentStatementResult = StatementResult.NO_MORE_RESULTS;
                isSingletonResult = false;
                try {
                    clearWarnings();
                } catch (SQLException e) {
                    // Ignoring exception (can't happen in current implementation)
                    throw new AssertionError("Unexpected SQLException", e);
                }
                break;
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
                log.debugf("Received statement listener update from unrelated statement [%s]", sender);
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
