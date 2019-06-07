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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jdbc.escape.FBEscapedParser;
import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Implementation of {@link Statement}.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public class FBStatement implements FirebirdStatement, Synchronizable {

    private static final org.firebirdsql.logging.Logger log = LoggerFactory.getLogger(FBStatement.class);
    protected static final JdbcVersionSupport jdbcVersionSupport =
            JdbcVersionSupportHolder.INSTANCE.getJdbcVersionSupport();

    private static final AtomicInteger STATEMENT_ID_GENERATOR = new AtomicInteger();

    private final int localStatementId = STATEMENT_ID_GENERATOR.incrementAndGet();
    protected final GDSHelper gdsHelper;
    private final Object syncObject;
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

    protected StatementResult currentStatementResult = StatementResult.NO_MORE_RESULTS;

    // Singleton result indicates it is a stored procedure or [INSERT | UPDATE | DELETE] ... RETURNING ...
    protected boolean isSingletonResult;
    // Used for singleton or batch results for getGeneratedKeys, and singleton results of stored procedures
    protected final List<RowValue> specialResult = new LinkedList<>();

    protected int maxRows;	 
    protected int fetchSize;
    private int maxFieldSize;
    private int queryTimeout;
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
    private class RSListener implements FBObjectListener.ResultSetListener {
        
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
            notifyStatementCompleted(success);
        }

        @Override
        public void executionStarted(FirebirdRowUpdater updater) throws SQLException {
            notifyStatementStarted(false);
        }
    }

    protected FBStatement(GDSHelper c, int rsType, int rsConcurrency, int rsHoldability, FBObjectListener.StatementListener statementListener) throws SQLException {
        this.gdsHelper = c;
        syncObject = c.getSynchronizationObject();
        
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

    private static Set<StatementState> INVALID_STATEMENT_STATES = EnumSet.of(
            StatementState.ERROR, StatementState.CLOSING, StatementState.CLOSED);

    @Override
    public boolean isValid() {
        return !closed && !INVALID_STATEMENT_STATES.contains(fbStatement.getState());
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!closed)
                close();
        } finally {
            super.finalize();
        }
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
        synchronized(getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
            notifyStatementStarted();
            try {
                if (internalExecute(sql)) { throw new FBSQLException(
                        "Update statement returned results."); }
                return getUpdateCount();
            } finally {
                notifyStatementCompleted();
            }
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, autoGeneratedKeys)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getUpdateCount();
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, columnIndexes)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getUpdateCount();
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, columnNames)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getUpdateCount();
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, autoGeneratedKeys);
            return executeImpl(query);
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, columnIndexes);
            return executeImpl(query);
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            GeneratedKeysSupport.Query query = connection.getGeneratedKeysSupport()
                    .buildQuery(sql, columnNames);
            return executeImpl(query);
        }
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkValidity();
        if (isGeneratedKeyQuery() && isSingletonResult) {
            return new FBResultSet(fbStatement.getFieldDescriptor(), new ArrayList<>(specialResult),
                    resultSetListener);
        }
        return new FBResultSet(fbStatement.emptyRowDescriptor(), Collections.<RowValue>emptyList());
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

        synchronized(getSynchronizationObject()) {
            if (fbStatement != null) {
                try {
                    try {
                        closeResultSet(false);
                    } finally {
                        //may need ensureTransaction?
                        fbStatement.close();
                    }
                } finally {
                    fbStatement = null;
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

    @Override
    public int getQueryTimeout() throws  SQLException {
        return queryTimeout;
    }

    @Override
    public void setQueryTimeout(int seconds) throws  SQLException {
        if (seconds < 0) {
            throw new FBSQLException("Can't set query timeout negative", SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }
        queryTimeout = seconds;
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
        synchronized (getSynchronizationObject()) {
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
        if (!isGeneratedKeyQuery() && currentStatementResult == StatementResult.RESULT_SET) {
            if (!isSingletonResult) {
                currentRs = new FBResultSet(connection, this, fbStatement, resultSetListener, metaDataQuery, rsType,
                        rsConcurrency, rsHoldability, false);
            } else if (!specialResult.isEmpty()) {
                currentRs = new FBResultSet(fbStatement.getFieldDescriptor(),
                        new ArrayList<>(specialResult), resultSetListener);
            }
            return currentRs;
        }
        return null;
    }

    @Override
	public boolean hasOpenResultSet() {
		return currentRs != null;
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
            throw new IllegalArgumentException(String.format("Specified type %d is unknown.", type));
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
        
        if (currentStatementResult == StatementResult.RESULT_SET && closeResultSet) {
            closeResultSet(true);
        }
        currentStatementResult = currentStatementResult.nextResult();

        // Technically the statement below is always false, as only the first result is ever a ResultSet
        return currentStatementResult == StatementResult.RESULT_SET;
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
                    .toFlatSQLException();
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
        batchList.add(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        batchList.clear();
    }

    @Override
    public final int[] executeBatch() throws SQLException {
        if (connection.getAutoCommit()) {
            addWarning(new SQLWarning("Batch updates should be run with auto-commit disabled.", "01000"));
        }

        return toArray(executeBatchInternal());
    }

    protected List<Long> executeBatchInternal() throws SQLException {
        checkValidity();
        currentStatementGeneratedKeys = false;

        notifyStatementStarted();
        synchronized (getSynchronizationObject()) {
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
                    throw jdbcVersionSupport.createBatchUpdateException(e.getMessage(), e.getSQLState(),
                            e.getErrorCode(), toLargeArray(responses), e);
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
            throw jdbcVersionSupport.createBatchUpdateException(
                    "Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_GENERAL_ERROR, 0, toLargeArray(responses), null);
        } else {
            responses.add(getLargeUpdateCount());
        }
    }

    /**
     * Convert collection of {@link Long} update counts into array of int.
     * 
     * @param updateCounts
     *            collection of integer elements.
     * 
     * @return array of int.
     */
    protected int[] toArray(Collection<Long> updateCounts) {
        int[] result = new int[updateCounts.size()];
        int counter = 0;
        for (long value : updateCounts) {
        	result[counter++] = (int) value;
        }
        return result;
    }

    /**
     * Convert collection of {@link Integer} update counts into array of int.
     *
     * @param updateCounts
     *            collection of integer elements.
     *
     * @return array of int.
     */
    protected long[] toLargeArray(Collection<Long> updateCounts) {
        long[] result = new long[updateCounts.size()];
        int counter = 0;
        for (long value : updateCounts) {
            result[counter++] = value;
        }
        return result;
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
            if (currentRs != null) {
                try {
                    currentRs.close(notifyListener, completionReason);
                } finally {
                    currentRs = null;
                }
            } else if (fbStatement != null) {
                fbStatement.ensureClosedCursor(completionReason.isTransactionEnd());
            }
        } finally {
            if (notifyListener && !wasCompleted)
                statementListener.statementCompleted(this);
        }
    }
    
    public void forgetResultSet() { //yuck should be package
        // TODO Use case unclear, find out if this needs to be added to fbStatement somehow
        currentRs = null;
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
     
    /**
     * This method checks if supplied statement is executing procedure or
     * it is generic statement. This check is needed to handle correctly 
     * parameters that are returned from non-selectable procedures.
     * 
     * @param sql SQL statement to check
     * 
     * @return <code>true</code> if supplied statement is EXECUTE PROCEDURE
     * type of statement.
     * 
     * @throws SQLException if translating statement into native code failed.
     */
    protected boolean isExecuteProcedureStatement(String sql) throws SQLException {
        // TODO Unused? Remove?
        final String trimmedSql = nativeSQL(sql).trim();
        return trimmedSql.startsWith("EXECUTE");
    }

    protected boolean internalExecute(String sql) throws SQLException {
        checkValidity();

        prepareFixedStatement(sql);
        fbStatement.execute(RowValue.EMPTY_ROW_VALUE);

        return currentStatementResult == StatementResult.RESULT_SET;
    }

    protected void prepareFixedStatement(String sql) throws SQLException {
        // TODO: Statement should be created and allocated at FBStatement creation only.
        if (fbStatement == null) {
            fbStatement = gdsHelper.allocateStatement();
            fbStatement.addStatementListener(createStatementListener());
        } else {
            fbStatement.setTransaction(gdsHelper.getCurrentTransaction());
        }
        fbStatement.prepare(escapedProcessing ? nativeSQL(sql) : sql);
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
            DatabaseParameterBuffer dpb = gdsHelper.getDatabaseParameterBuffer();
            EscapeParserMode mode = dpb.hasArgument(DatabaseParameterBufferExtension.USE_STANDARD_UDF)
                    ? EscapeParserMode.USE_STANDARD_UDF
                    : EscapeParserMode.USE_BUILT_IN;
            return new FBEscapedParser(mode).parse(sql);
        }
    }

    /**
     * @return <code>true</code> when the current statement is expected to return generated keys, <code>false</code> otherwise.
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
     * The returned value will be one of the <code>TYPE_*</code> constant
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
                    String.format("Implementation limit: maxRows cannot exceed Integer.MAX_VALUE, value was %d, reset to 0", max),
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

        return toLargeArray(executeBatchInternal());
    }

    public final long executeLargeUpdate(String sql) throws SQLException {
        synchronized (getSynchronizationObject()) {
            executeUpdate(sql);
            return getLargeUpdateCount();
        }
    }

    public final long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, autoGeneratedKeys)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getLargeUpdateCount();
        }
    }

    public final long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, columnIndexes)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getLargeUpdateCount();
        }
    }

    public final long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (execute(sql, columnNames)) {
                throw new FBSQLException("Update statement returned results.");
            }
            return getLargeUpdateCount();
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
    private static final Pattern SIMPLE_IDENTIFIER_PATTERN = Pattern.compile("[\\p{Alpha}][\\p{Alnum}_$]*");

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
        RESULT_SET {
            @Override
            public StatementResult nextResult() {
                return UPDATE_COUNT;
            }
        },
        UPDATE_COUNT {
            @Override
            public StatementResult nextResult() {
                return NO_MORE_RESULTS;
            }
        },
        NO_MORE_RESULTS {
            @Override
            public StatementResult nextResult() {
                return NO_MORE_RESULTS;
            }
        };

        /**
         * @return Next result
         */
        public abstract StatementResult nextResult();
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
            if (!isValidSender(sender)) return;
            // TODO May need extra condition to distinguish between singleton result of EXECUTE PROCEDURE and INSERT ... RETURNING ...
            if (isSingletonResult) {
                specialResult.clear();
                specialResult.add(rowValue);
            }
        }

        @Override
        public void allRowsFetched(FbStatement sender) {
            if (!isValidSender(sender)) return;
            // TODO Evaluate if we need to do any processing
        }

        @Override
        public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
            if (!isValidSender(sender)) return;
            // TODO If true create ResultSet and attach listener to sender
            currentStatementResult = hasResultSet || hasSingletonResult && !isGeneratedKeyQuery()
                    ? StatementResult.RESULT_SET
                    : StatementResult.UPDATE_COUNT;
            isSingletonResult = hasSingletonResult;
        }

        @Override
        public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
            if (!isValidSender(sender)) return;
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
                    throw new AssertionError("Unexpected SQLException");
                }
                break;
            }
        }

        @Override
        public void warningReceived(FbStatement sender, SQLWarning warning) {
            if (!isValidSender(sender)) return;
            addWarning(warning);
        }

        @Override
        public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
            if (!isValidSender(sender)) return;
            sqlCountHolder = sqlCounts;
        }

        private boolean isValidSender(FbStatement sender) {
            if (sender != fbStatement) {
                log.debug(String.format("Received statement listener update from unrelated statement [%s]", sender.toString()));
                sender.removeStatementListener(this);
                return false;
            }
            return true;
        }
    }
}
