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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.xca.FBLocalTransaction;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jdbc.escape.FBEscapedParser;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Function;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.stream.Collectors.toMap;
import static org.firebirdsql.gds.ISCConstants.fb_cancel_abort;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_CONNECTION_CLOSED;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_TX_STATE;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_TX_ACTIVE;

/**
 * The class {@code FBConnection} is a handle to a {@link FBManagedConnection} and implements {@link Connection}.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
public class FBConnection implements FirebirdConnection {

    private static final System.Logger log = System.getLogger(FBConnection.class.getName());

    private static final SQLPermission PERMISSION_SET_NETWORK_TIMEOUT = new SQLPermission("setNetworkTimeout");
    private static final SQLPermission PERMISSION_CALL_ABORT = new SQLPermission("callAbort");

    protected volatile FBManagedConnection mc;

    private FBLocalTransaction localTransaction;
    private FBDatabaseMetaData metaData;

    protected final InternalTransactionCoordinator txCoordinator;

    private SQLWarning firstWarning;

    // This set contains all allocated but not closed statements
    // It is used to close them before the connection is closed
    protected final Set<Statement> activeStatements = Collections.synchronizedSet(new HashSet<>());

    private int resultSetHoldability;

    private StoredProcedureMetaData storedProcedureMetaData;
    private GeneratedKeysSupport generatedKeysSupport;
    private ClientInfoProvider clientInfoProvider;

    /**
     * Create a new FBConnection instance based on a {@link FBManagedConnection}.
     *
     * @param mc
     *         A FBManagedConnection around which this connection is based
     */
    public FBConnection(FBManagedConnection mc) {
        this.mc = mc;
        txCoordinator = new InternalTransactionCoordinator(this);

        IConnectionProperties props = mc.getConnectionRequestInfo().asIConnectionProperties();

        resultSetHoldability = props.isDefaultResultSetHoldable()
                ? ResultSet.HOLD_CURSORS_OVER_COMMIT
                : ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public int getHoldability() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return resultSetHoldability;
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            this.resultSetHoldability = holdability;
        }
    }

    /**
     * Check if this connection is valid. This method should be invoked before
     * executing any action in this class.
     *
     * @throws SQLException
     *         if this connection has been closed and cannot be used anymore
     */
    protected void checkValidity() throws SQLException {
        if (isClosed()) {
            throw new SQLNonTransientConnectionException("This connection is closed and cannot be used now",
                    SQL_STATE_CONNECTION_CLOSED);
        }
    }

    /**
     * This method should be invoked by each of the statements in the {@link Statement#close()} method. Here we remove
     * statement from the {@code activeStatements} set, so we do not need to close it later.
     *
     * @param stmt
     *         statement that was closed.
     */
    void notifyStatementClosed(FBStatement stmt) {
        if (!activeStatements.remove(stmt)) {
            if (stmt instanceof FBPreparedStatement pstmt && !pstmt.isInitialized()) {
                // Close was likely triggered by finalizer of a prepared statement that failed on prepare in
                // the constructor: Do not log warning
                return;
            }
            log.log(WARNING, "Specified statement was not created by this connection: {0}", stmt);
        }
    }

    /**
     * This method closes all active statements and cleans resources.
     *
     * @throws SQLException
     *         if at least one of the active statements failed to close gracefully.
     */
    protected void freeStatements() throws SQLException {
        // copy statements to avoid concurrent modification exception
        List<Statement> statements = new ArrayList<>(activeStatements);

        // iterate through the set, close statements and collect exceptions
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        for (Statement stmt : statements) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                chain.append(ex);
            }
        }

        // throw exception if there is any
        if (chain.hasException()) throw chain.getException();
    }

    /**
     * Set the {@link FBManagedConnection} around which this connection is
     * based.
     *
     * @param mc
     *         The FBManagedConnection around which this connection is based
     */
    public void setManagedConnection(FBManagedConnection mc) {
        if (mc == null && this.mc == null) return;
        try (LockCloseable ignored = withLock()) {
            //close any prepared statements we may have executed.
            if (this.mc != mc && metaData != null) {
                try {
                    metaData.close();
                } finally {
                    metaData = null;
                }
            }
            this.mc = mc;
        }
    }

    public FBManagedConnection getManagedConnection() {
        try (LockCloseable ignored = withLock()) {
            return mc;
        }
    }

    @Override
    public FbDatabase getFbDatabase() throws SQLException {
        return getGDSHelper().getCurrentDatabase();
    }

    /**
     * Get database connection properties for this connection.
     *
     * @return immutable instance of {@link DatabaseConnectionProperties}.
     */
    public DatabaseConnectionProperties connectionProperties() {
        // TODO Obtain elsewhere than from getConnectionRequestInfo()
        return mc != null ? mc.getConnectionRequestInfo().asIConnectionProperties().asImmutable() : null;
    }

    @Deprecated(since = "2")
    @Override
    public void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            TransactionParameterBuffer tpbParams = createTransactionParameterBuffer();

            for (int parameter : parameters) {
                tpbParams.addArgument(parameter);
            }

            setTransactionParameters(isolationLevel, tpbParams);
        }
    }

    @Override
    public TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return mc.getTransactionParameters(isolationLevel);
        }
    }

    @Override
    public TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return getFbDatabase().createTransactionParameterBuffer();
        }
    }

    @Override
    public void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (mc.isManagedEnvironment()) {
                throw new SQLException("Cannot set transaction parameters in managed environment.",
                        SQL_STATE_GENERAL_ERROR);
            }

            mc.setTransactionParameters(isolationLevel, tpb);
        }
    }

    @Override
    public void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (getLocalTransaction().inTransaction()) {
                // TODO More specific exception, jaybird error code
                throw new SQLException("Cannot set transaction parameters when transaction is already started.",
                        SQL_STATE_TX_ACTIVE);
            }

            mc.setTransactionParameters(tpb);
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public Blob createBlob() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return createBlob(FBBlob.createConfig(ISCConstants.BLOB_SUB_TYPE_BINARY, connectionProperties(),
                    getFbDatabase().getDatatypeCoder()));
        }
    }

    private FBBlob createBlob(FBBlob.Config blobConfig) throws SQLException {
        return new FBBlob(getGDSHelper(), txCoordinator, blobConfig);
    }

    @Override
    public Clob createClob() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            FBBlob blob = createBlob(FBBlob.createConfig(ISCConstants.BLOB_SUB_TYPE_TEXT, connectionProperties(),
                    getFbDatabase().getDatatypeCoder()));
            return new FBClob(blob);
        }
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type STRUCT not supported");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return FBEscapedParser.toNativeSql(sql);
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (getAutoCommit() == autoCommit) {
                return;
            }

            // FIXME : Behavior in switch might be wrong, see also setSavePoint
            txCoordinator.switchTransactionCoordinator(autoCommit);
        }
    }

    protected void setTransactionCoordinator(boolean managedConnection, boolean autoCommit) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            txCoordinator.setTransactionCoordinator(managedConnection, autoCommit);
        }
    }

    public void setManagedEnvironment(boolean managedConnection) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            setTransactionCoordinator(managedConnection, true);
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (isClosed()) {
                throw new SQLNonTransientConnectionException(
                        "You cannot getAutoCommit on an unassociated closed connection.", SQL_STATE_CONNECTION_CLOSED);
            }
            return txCoordinator.getAutoCommit();
        }
    }

    @Override
    public void commit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (isClosed()) {
                throw new SQLNonTransientConnectionException("You cannot commit a closed connection.",
                        SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction", SQL_STATE_INVALID_TX_STATE);
            }

            txCoordinator.commit();
            invalidateTransactionLifetimeObjects();
        }
    }

    @Override
    public void rollback() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (isClosed()) {
                throw new SQLNonTransientConnectionException("You cannot rollback closed connection.",
                        SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction", SQL_STATE_INVALID_TX_STATE);
            }

            txCoordinator.rollback();
            invalidateTransactionLifetimeObjects();
        }
    }

    /**
     * Invalidate everything that should only last for the lifetime of the current transaction.
     */
    protected void invalidateTransactionLifetimeObjects() {
        invalidateSavepoints();
        storedProcedureMetaData = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Certain fatal errors also result in a closed Connection.
     * </p>
     */
    @Override
    public void close() throws SQLException {
        if (isClosed()) return;
        if (log.isLoggable(TRACE)) {
            log.log(TRACE, "Connection closed requested at", new RuntimeException("Connection close logging"));
        }
        var chainBuilder = new SQLExceptionChainBuilder<>();
        try (LockCloseable ignored = withLock()) {
            try {
                if (metaData != null) metaData.close();
                freeStatements();
            } catch (SQLException e) {
                chainBuilder.append(e);
            } finally {
                metaData = null;
                FBManagedConnection mc = this.mc;
                if (mc != null) {
                    // leave managed transactions alone, they are normally
                    // committed after the Connection handle is closed.
                    if (!mc.inDistributedTransaction()) {
                        try {
                            txCoordinator.handleConnectionClose();
                        } catch (SQLException e) {
                            chainBuilder.append(e);
                        } finally {
                            try {
                                setAutoCommit(true);
                            } catch (SQLException e) {
                                if (!SQL_STATE_CONNECTION_CLOSED.equals(e.getSQLState())) {
                                    chainBuilder.append(e);
                                }
                            }
                        }
                    }

                    mc.close(this);
                }
            }
        }
        if (chainBuilder.hasException()) {
            throw chainBuilder.getException();
        }
    }

    @Override
    public boolean isClosed() {
        return mc == null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (timeout < 0) {
            throw new SQLException("Timeout should be >= 0", SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE);
        }
        if (isLockedByCurrentThread()) {
            // Trying to async check validity will not work (this shouldn't normally happen, except maybe when Jaybird
            // internals call isValid(int) or user code locks on result of withLock())
            return isValidImpl(timeout);
        }
        return isValidAsync(timeout);
    }

    private boolean isValidAsync(int timeout) {
        Future<Boolean> isValidFuture = ForkJoinPool.commonPool().submit(() -> isValidImpl(timeout));
        try {
            return timeout != 0 ? isValidFuture.get(timeout, TimeUnit.SECONDS) : isValidFuture.get();
        } catch (ExecutionException e) {
            log.log(DEBUG, "isValidImpl produced an exception", e);
            return false;
        } catch (InterruptedException e) {
            isValidFuture.cancel(true);
            // restore interrupted state
            Thread.currentThread().interrupt();
            return false;
        } catch (TimeoutException e) {
            isValidFuture.cancel(true);
            return false;
        }
    }

    private boolean isValidImpl(int timeout) {
        try (LockCloseable ignored = withLock()) {
            if (isClosed()) {
                return false;
            }
            int originalNetworkTimeout = -1;
            boolean networkTimeoutChanged = false;
            try {
                FbDatabase db = getFbDatabase();
                if (timeout != 0) {
                    try {
                        originalNetworkTimeout = db.getNetworkTimeout();
                        db.setNetworkTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
                        networkTimeoutChanged = true;
                    } catch (SQLFeatureNotSupportedException ignored2) {
                        // Implementation doesn't support network timeout
                    }
                }

                db.getDatabaseInfo(new byte[] { ISCConstants.isc_info_ods_version, ISCConstants.isc_info_end }, 10);
                return true;
            } catch (SQLException ex) {
                log.log(DEBUG, "Exception while checking connection validity", ex);
                return false;
            } finally {
                if (networkTimeoutChanged) {
                    try {
                        getFbDatabase().setNetworkTimeout(originalNetworkTimeout);
                    } catch (SQLException e) {
                        log.log(DEBUG, "Exception while resetting connection network timeout", e);
                        // We're interpreting this as an indication the connection is no longer valid
                        //noinspection ReturnInsideFinallyBlock
                        return false;
                    }
                }
            }
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (metaData == null)
                metaData = new FBDatabaseMetaData(this);
            return metaData;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (getLocalTransaction().inTransaction() && !mc.isManagedEnvironment()) {
                // TODO More specific exception, jaybird error code
                throw new SQLException(
                        "Calling setReadOnly(boolean) method is not allowed when transaction is already started.",
                        SQL_STATE_TX_ACTIVE);
            }
            mc.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return mc.isReadOnly();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation ignores calls to this method as catalogs are not supported.
     * </p>
     */
    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkValidity();
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@code null} as catalogs are not supported.
     */
    @Override
    public String getCatalog() throws SQLException {
        checkValidity();
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (!getAutoCommit() && !mc.isManagedEnvironment()) {
                txCoordinator.commit();
            }
            mc.setTransactionIsolation(level);
        }
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getTransactionIsolation() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return mc.getTransactionIsolation();
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            return firstWarning;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            firstWarning = null;
        }
    }

    @Override
    public Statement createStatement(int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return createStatement(resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT &&
                    resultSetType == ResultSet.TYPE_FORWARD_ONLY) {

                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            checkHoldability(resultSetType, resultSetHoldability);

            Statement stmt = new FBStatement(getGDSHelper(), resultSetType, resultSetConcurrency, resultSetHoldability,
                    txCoordinator);

            activeStatements.add(stmt);
            return stmt;
        }
    }

    /**
     * Check whether result set type and holdability are compatible.
     *
     * @param resultSetType
     *         desired result set type.
     * @param resultSetHoldability
     *         desired result set holdability.
     * @throws SQLException
     *         if specified result set type and holdability are not compatible.
     */
    private void checkHoldability(int resultSetType, int resultSetHoldability) throws SQLException {
        boolean holdable = resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT;

        boolean notScrollable = resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE;

        if (holdable && notScrollable) {
            // TODO jaybird error code
            throw new FBDriverNotCapableException(
                    "Holdable cursors are supported only for scrollable insensitive result sets.");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return prepareStatement(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability, false, false);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = getGeneratedKeysSupport()
                    .buildQuery(sql, autoGeneratedKeys);
            return prepareStatement(query);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = getGeneratedKeysSupport()
                    .buildQuery(sql, columnIndexes);
            return prepareStatement(query);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            GeneratedKeysSupport.Query query = getGeneratedKeysSupport()
                    .buildQuery(sql, columnNames);
            return prepareStatement(query);
        }
    }

    /**
     * Prepares a statement for generated keys.
     *
     * @param query
     *         AbstractGeneratedKeysQuery instance
     * @return PreparedStatement object
     * @throws SQLException
     *         if a database access error occurs or this method is called on a closed connection
     */
    private PreparedStatement prepareStatement(GeneratedKeysSupport.Query query) throws SQLException {
        if (query.generatesKeys()) {
            return prepareStatement(query.getQueryString(),
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT,
                    false, true);
        } else {
            return prepareStatement(query.getQueryString());
        }
    }

    protected PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability, boolean metaData, boolean generatedKeys) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            resultSetType = verifyResultSetType(resultSetType, resultSetHoldability);

            checkHoldability(resultSetType, resultSetHoldability);

            FBObjectListener.StatementListener coordinator = txCoordinator;
            if (metaData)
                coordinator = new InternalTransactionCoordinator.MetaDataTransactionCoordinator(txCoordinator);

            FBObjectListener.BlobListener blobCoordinator = metaData ? null : txCoordinator;

            PreparedStatement stmt = new FBPreparedStatement(getGDSHelper(), sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability, coordinator, blobCoordinator, metaData, false, generatedKeys);

            activeStatements.add(stmt);
            return stmt;
        }
    }

    private int verifyResultSetType(int resultSetType, int resultSetHoldability) {
        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT
            && resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
            addWarning(FbExceptionBuilder
                    .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                    .toSQLException(SQLWarning.class));
            return ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
            addWarning(FbExceptionBuilder
                    .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                    .toSQLException(SQLWarning.class));
            return ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else {
            return resultSetType;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            resultSetType = verifyResultSetType(resultSetType, resultSetHoldability);

            if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonStoredProcedure)
                        .toSQLException(SQLWarning.class));
                resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
            }

            checkHoldability(resultSetType, resultSetHoldability);

            if (storedProcedureMetaData == null) {
                storedProcedureMetaData = StoredProcedureMetaDataFactory.getInstance(this);
            }

            FBCallableStatement stmt = new FBCallableStatement(getGDSHelper(), sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability, storedProcedureMetaData, txCoordinator, txCoordinator);
            activeStatements.add(stmt);

            return stmt;
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return new HashMap<>();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    private static final AtomicIntegerFieldUpdater<FBConnection> SAVEPOINT_COUNTER_UPDATE =
            AtomicIntegerFieldUpdater.newUpdater(FBConnection.class, "savepointCounter");
    private volatile int savepointCounter;
    private final List<FBSavepoint> savepoints = new ArrayList<>();

    private int getNextSavepointCounter() {
        return SAVEPOINT_COUNTER_UPDATE.getAndIncrement(this);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            FBSavepoint savepoint = new FBSavepoint(getNextSavepointCounter());
            setSavepoint(savepoint);

            return savepoint;
        }
    }

    /**
     * Set the savepoint on the server.
     *
     * @param savepoint
     *         savepoint to set
     * @throws SQLException
     *         if something went wrong
     */
    private void setSavepoint(FBSavepoint savepoint) throws SQLException {
        if (getAutoCommit()) {
            throw new SQLException("Connection.setSavepoint() method cannot be used in auto-commit mode",
                    SQL_STATE_INVALID_TX_STATE);
        }

        if (mc.inDistributedTransaction()) {
            throw new SQLException("Connection enlisted in distributed transaction", SQL_STATE_INVALID_TX_STATE);
        }

        txCoordinator.ensureTransaction();

        getGDSHelper().executeImmediate(savepoint.toSavepointStatement(getQuoteStrategy()));
        savepoints.add(savepoint);
    }

    /**
     * Creates a named savepoint.
     * <p>
     * Savepoint names need to be valid Firebird identifiers, and the maximum length is restricted to the maximum
     * identifier length (see {@link DatabaseMetaData#getMaxColumnNameLength()}. The implementation will take care of
     * quoting the savepoint name appropriately for the connection dialect. The {@code name} should be passed unquoted.
     * </p>
     * <p>
     * With connection dialect 1, the name is restricted to the rules for unquoted identifier names, that is, its
     * characters are restricted to {@code A-Za-z0-9$_} and handled case insensitive.
     * </p>
     * <p>
     * For dialect 2 and 3, the name is restricted to the rules for Firebird quoted identifiers (essentially any
     * printable character and space is valid), and the name is handled case sensitive.
     * </p>
     *
     * @param name
     *         Savepoint name
     * @return Savepoint object
     * @throws SQLException
     *         if a database access error occurs, this method is called while participating in a distributed
     *         transaction, this method is called on a closed connection or this {@code Connection} object is currently
     *         in auto-commit mode
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            FBSavepoint savepoint = new FBSavepoint(name);
            setSavepoint(savepoint);

            return savepoint;
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.rollback(Savepoint) method cannot be used in auto-commit mode",
                        SQL_STATE_INVALID_TX_STATE);
            } else if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction", SQL_STATE_INVALID_TX_STATE);
            }

            FBSavepoint fbSavepoint = validateSavepoint(savepoint);
            getGDSHelper().executeImmediate(fbSavepoint.toRollbackStatement(getQuoteStrategy()));
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.releaseSavepoint() method cannot be used in auto-commit mode",
                        SQL_STATE_INVALID_TX_STATE);
            }

            FBSavepoint fbSavepoint = validateSavepoint(savepoint);
            getGDSHelper().executeImmediate(fbSavepoint.toReleaseStatement(getQuoteStrategy()));

            savepoints.remove(fbSavepoint);
        }
    }

    /**
     * Invalidate all savepoints.
     */
    protected void invalidateSavepoints() {
        try (LockCloseable ignored = withLock()) {
            savepoints.clear();
        }
    }

    FBSavepoint validateSavepoint(Savepoint savepoint) throws SQLException {
        // TODO The error message and actual condition do not match
        if (!(savepoint instanceof FBSavepoint fbSavepoint)) {
            throw new SQLException("Specified savepoint was not obtained from this connection");
        }

        if (!savepoints.contains(fbSavepoint)) {
            throw new SQLException("Savepoint is no longer valid");
        }

        return fbSavepoint;
    }

    /**
     * Returns a FBLocalTransaction instance that enables a component to
     * demarcate resource manager local transactions on this connection.
     */
    public FBLocalTransaction getLocalTransaction() {
        try (LockCloseable ignored = withLock()) {
            if (localTransaction == null) {
                localTransaction = mc.getLocalTransaction();
            }
            return localTransaction;
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBConnection.class);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation ignores calls to this method as schemas are not supported.
     * </p>
     */
    @Override
    public void setSchema(String schema) throws SQLException {
        // Ignore: no schema support
        checkValidity();
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@code null} as schemas ar not supported
     */
    @Override
    public String getSchema() throws SQLException {
        checkValidity();
        return null;
    }

    public void addWarning(SQLWarning warning) {
        try (LockCloseable ignored = withLock()) {
            // TODO: Find way so this method can be protected (or less visible) again.
            if (firstWarning == null)
                firstWarning = warning;
            else {
                firstWarning.setNextWarning(warning);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #createClob()}.
     * </p>
     */
    @Override
    public NClob createNClob() throws SQLException {
        return (NClob) createClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    public GDSHelper getGDSHelper() throws SQLException {
        if (mc == null)
            // TODO Right error code?
            throw FbExceptionBuilder.forException(ISCConstants.isc_req_no_trans).toSQLException();

        return mc.getGDSHelper();
    }

    @Override
    public boolean isUseFirebirdAutoCommit() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null && props.isUseFirebirdAutocommit();
    }

    /**
     * Returns, and if necessary creates, an instance of ClientInfoProvider for this connection.
     *
     * @return client info provider instance
     * @throws SQLFeatureNotSupportedException
     *         if {@code connection} is to a Firebird version which does not support RDB$GET/SET_CONTEXT
     * @throws SQLException
     *         if {@code connection} is not valid (e.g. closed)
     */
    ClientInfoProvider getClientInfoProvider() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            ClientInfoProvider clientInfoProvider = this.clientInfoProvider;
            if (clientInfoProvider != null) return clientInfoProvider;
            return this.clientInfoProvider = new ClientInfoProvider(this);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves the known properties of this connection. The initial known properties are {@code ApplicationName} (with
     * fallback to {@code CLIENT_PROCESS@SYSTEM}), {@code ClientUser} (no default value or fallback), and
     * {@code ClientHostname} (no default value or fallback). Successful retrieval or storing of properties with
     * {@link #getClientInfo(String)}, {@link #setClientInfo(String, String)} and {@link #setClientInfo(Properties)}
     * will register that property as a known property <em>for this connection only</em>.
     * </p>
     * <p>
     * When auto-commit is enabled, known properties in context {@code USER_TRANSACTION} are skipped, and not included
     * in the returned {@code Properties} object.
     * </p>
     * <p>
     * Properties which were registered with suffix {@code @USER_SESSION} are included in the returned
     * {@code Properties} object <strong>without</strong> that suffix. Known properties with value {@code null} are not
     * included in the returned {@code Properties} object.
     * </p>
     *
     * @see #getClientInfo(String)
     * @see #setClientInfo(Properties)
     */
    @Override
    public Properties getClientInfo() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            return getClientInfoProvider().getClientInfo();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves properties from context {@code USER_SESSION} or an explicitly specified context with
     * {@code RDB$GET_CONTEXT}. Names ending in {@code @USER_SESSION}, {@code @USER_TRANSACTION} or {@code @SYSTEM} are
     * handled as {@code <property-name>@<context-name>}, and the property is retrieved from that context. Bare names
     * are handled the same as {@code name + "@USER_SESSION"}, unknown or unsupported context suffixes are handled as a
     * property name in {@code USER_SESSION} (that is {@code DDL_EVENT@DDL_TRIGGER} is handled as a property with that
     * name in {@code USER_SESSION}, and not as a property DDL_EVENT in context DDL_TRIGGER, which only exists in
     * PSQL DDL triggers).
     * </p>
     * <p>
     * When auto-commit is enabled, properties in context {@code USER_TRANSACTION} will always return {@code null},
     * and no attempts are made to retrieve the property.
     * </p>
     * <p>
     * Successful retrieval of a property will register it as a known property <em>for this connection only</em> for use
     * with {@link #getClientInfo()} (i.e. known properties will be retrieved) and {@link #setClientInfo(Properties)}
     * (i.e. if a known property is not included, it will be cleared).
     * </p>
     */
    @Override
    public String getClientInfo(String name) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            return getClientInfoProvider().getClientInfo(name);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the properties in {@code properties} in context {@code USER_SESSION} or an explicitly specified user context
     * with {@code RDB$SET_CONTEXT}. See also {@link #getClientInfo(String)} for handling of property names. Absent, but
     * known properties are cleared. Contrary to {@link #setClientInfo(String, String)}, properties in the
     * {@code SYSTEM} context are <strong>silently ignored</strong> as they are read-only.
     * </p>
     * <p>
     * When auto-commit is enabled, properties in context {@code USER_TRANSACTION} are silently ignored (they are not
     * set nor cleared).
     * </p>
     * <p>
     * Successful setting of properties will register it as a known property <em>for this connection only</em> for use
     * with {@link #getClientInfo()} (i.e. known properties will be retrieved) and {@link #setClientInfo(Properties)}
     * (i.e. if a known property is not included, it will be cleared).
     * </p>
     */
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try (LockCloseable ignored = withLock()) {
            getClientInfoProvider().setClientInfo(properties);
        } catch (SQLClientInfoException e) {
            throw e;
        } catch (SQLException e) {
            Map<String, ClientInfoStatus> failedProperties = properties.stringPropertyNames().stream()
                    .collect(toMap(Function.identity(), name -> ClientInfoStatus.REASON_UNKNOWN));
            throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), e.getErrorCode(), failedProperties, e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets properties in context {@code USER_SESSION} or an explicitly specified user context with
     * {@code RDB$SET_CONTEXT}. See also {@link #getClientInfo(String)} for handling of property names.
     * </p>
     * <p>
     * When auto-commit is enabled, properties in context {@code USER_TRANSACTION} will not be set.
     * </p>
     * <p>
     * Successful storing of a property will register it as a known property <em>for this connection only</em> for use
     * with {@link #getClientInfo()} (i.e. known properties will be retrieved) and {@link #setClientInfo(Properties)}
     * (i.e. if a known property is not included, it will be cleared).
     * </p>
     *
     * @param name
     *         name of the client info property to set (cannot be a name ending in {@code @SYSTEM} as those are
     *         read-only)
     * @throws SQLClientInfoException
     *         if {@code name} is {@code null} or ends in {@code @SYSTEM}, or for database access errors
     * @see #getClientInfo(String)
     */
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try (LockCloseable ignored = withLock()) {
            getClientInfoProvider().setClientInfo(name, value);
        } catch (SQLClientInfoException e) {
            throw e;
        } catch (SQLException e) {
            throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), e.getErrorCode(),
                    Map.of(name, ClientInfoStatus.REASON_UNKNOWN), e);
        }
    }

    @Override
    public void resetKnownClientInfoProperties() {
        try (LockCloseable ignored = withLock()) {
            if (isClosed()) return;
            ClientInfoProvider clientInfoProvider = this.clientInfoProvider;
            if (clientInfoProvider != null) {
                clientInfoProvider.resetKnownProperties();
            }
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // NOTE: None of these operations are performed under lock (except maybe very late in the cleanup)
        if (isClosed()) return;
        PERMISSION_CALL_ABORT.checkGuard(this);
        if (executor == null) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidExecutor).toSQLException();
        }
        final FbDatabase fbDatabase;
        try {
            fbDatabase = getFbDatabase();
        } finally {
            metaData = null;
            FBManagedConnection mc = this.mc;
            if (mc != null) {
                // Avoid lock in Connection.setManagedConnection during mc.close
                this.mc = null;
                mc.close(this);
            }
        }
        executor.execute(() -> {
            try {
                try {
                    // Try and close statements and result sets
                    txCoordinator.handleConnectionAbort();
                } finally {
                    try {
                        // should forcibly close connection
                        fbDatabase.cancelOperation(fb_cancel_abort);
                    } catch (SQLException e) {
                        log.log(DEBUG, "Failed to raise abort on FbDatabase", e);
                    }
                }
            } catch (SQLException e) {
                log.log(DEBUG, "txCoordinator.handleConnectionAbort() caused an exception", e);
            } finally {
                try {
                    // alternative attempt to forcibly close connection
                    fbDatabase.forceClose();
                } catch (SQLException e) {
                    log.log(DEBUG, "Could not forceClose FbDatabase on abort", e);
                }
            }
        });
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        PERMISSION_SET_NETWORK_TIMEOUT.checkGuard(this);
        if (executor == null) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidExecutor).toSQLException();
        }
        if (milliseconds < 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidTimeout).toSQLException();
        }
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            getFbDatabase().setNetworkTimeout(milliseconds);
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return getFbDatabase().getNetworkTimeout();
    }

    /**
     * @see org.firebirdsql.gds.ng.FbAttachment#withLock()
     */
    protected final LockCloseable withLock() {
        FBManagedConnection mc = this.mc;
        if (mc != null) {
            return mc.withLock();
        }
        return LockCloseable.NO_OP;
    }

    protected final boolean isLockedByCurrentThread() {
        FBManagedConnection mc = this.mc;
        if (mc != null) {
            return mc.isLockedByCurrentThread();
        }
        return false;
    }

    /**
     * Get the quote strategy appropriate for the dialect of this connection.
     *
     * @return Quote strategy
     * @throws SQLException
     *         If the connection is closed
     */
    QuoteStrategy getQuoteStrategy() throws SQLException {
        return QuoteStrategy.forDialect(getGDSHelper().getDialect());
    }

    GeneratedKeysSupport getGeneratedKeysSupport() throws SQLException {
        if (generatedKeysSupport == null) {
            generatedKeysSupport = GeneratedKeysSupportFactory
                    .createFor(getGeneratedKeysEnabled(), (FirebirdDatabaseMetaData) getMetaData());
        }
        return generatedKeysSupport;
    }

    private String getGeneratedKeysEnabled() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null ? props.getGeneratedKeysEnabled() : null;
    }

    boolean isIgnoreProcedureType() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null && props.isIgnoreProcedureType();
    }

    /**
     * Checks (case-insensitive) value of the {@code scrollableCursor} connection property.
     * <p>
     * <b>Important:</b> this does not verify actual server support for the requested feature, just the value of
     * the connection property.
     * </p>
     *
     * @param scrollableCursor
     *         Value to check (case-insensitive)
     * @return {@code true} if the {@code scrollableCursor} connection property matches the specified value,
     * {@code false} otherwise.
     */
    boolean isScrollableCursor(String scrollableCursor) {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null && scrollableCursor != null
                && scrollableCursor.equalsIgnoreCase(props.getScrollableCursor());
    }

    boolean isUseServerBatch() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null && props.isUseServerBatch();
    }

    int getServerBatchBufferSize() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null ? props.getServerBatchBufferSize() : PropertyConstants.DEFAULT_SERVER_BATCH_BUFFER_SIZE;
    }

}
