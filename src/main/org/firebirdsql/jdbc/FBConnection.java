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
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.xca.FBLocalTransaction;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.jdbc.escape.FBEscapedParser;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * The class <code>FBConnection</code> is a handle to a
 * {@link FBManagedConnection} and implements {@link Connection}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public class FBConnection implements FirebirdConnection, Synchronizable {

    private static final Logger log = LoggerFactory.getLogger(FBConnection.class);

    private static final String GET_CLIENT_INFO_SQL = "SELECT "
            + "    rdb$get_context('USER_SESSION', ?) session_context "
            + "  , rdb$get_context('USER_TRANSACTION', ?) tx_context "
            + "FROM rdb$database";

    private static final String SET_CLIENT_INFO_SQL = "SELECT "
            + "  rdb$set_context('USER_SESSION', ?, ?) session_context "
            + "FROM rdb$database";

    private static final String PERMISSION_SET_NETWORK_TIMEOUT = "setNetworkTimeout";

    protected FBManagedConnection mc;

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

    /**
     * Create a new AbstractConnection instance based on a
     * {@link FBManagedConnection}.
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

    public FBObjectListener.StatementListener getStatementListener() {
        return txCoordinator;
    }

    @Override
    public int getHoldability() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return resultSetHoldability;
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            this.resultSetHoldability = holdability;
        }
    }

    /**
     * Check if this connection is valid. This method should be invoked before
     * executing any action in this class.
     *
     * @throws SQLException
     *         if this connection has been closed and cannot be used anymore.
     */
    protected void checkValidity() throws SQLException {
        if (isClosed()) {
            throw new FBSQLException("This connection is closed and cannot be used now.",
                    SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
        }
    }

    /**
     * This method should be invoked by each of the statements in the
     * {@link Statement#close()} method. Here we remove statement from the
     * <code>activeStatements</code> set, so we do not need to close it
     * later.
     *
     * @param stmt
     *         statement that was closed.
     */
    void notifyStatementClosed(FBStatement stmt) {
        if (!activeStatements.remove(stmt)) {
            if (stmt instanceof FBPreparedStatement && !((FBPreparedStatement) stmt).isInitialized()) {
                // Close was likely triggered by finalizer of a prepared statement that failed on prepare in
                // the constructor: Do not log warning
                return;
            }
            log.warn("Specified statement was not created by this connection: " + stmt);
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
        synchronized (getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
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
        // TODO Do we need mutability?
        // TODO Obtain elsewhere than from getConnectionRequestInfo()
        return mc != null ? mc.getConnectionRequestInfo().asIConnectionProperties().asImmutable() : null;
    }

    @Deprecated
    @Override
    public void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException {
        synchronized (getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return mc.getTransactionParameters(isolationLevel);
        }
    }

    @Override
    public TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return getFbDatabase().createTransactionParameterBuffer();
        }
    }

    @Override
    public void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (mc.isManagedEnvironment()) {
                throw new FBSQLException("Cannot set transaction parameters in managed environment.");
            }

            mc.setTransactionParameters(isolationLevel, tpb);
        }
    }

    @Override
    public void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getLocalTransaction().inTransaction()) {
                // TODO More specific exception, jaybird error code
                throw new FBSQLException("Cannot set transaction parameters when transaction is already started.");
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return new FBBlob(getGDSHelper(), txCoordinator);
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        FBBlob blob = (FBBlob) createBlob();
        return new FBClob(blob);
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return FBEscapedParser.toNativeSql(sql);
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit() == autoCommit) {
                return;
            }

            // FIXME : Behavior in switch might be wrong, see also setSavePoint
            txCoordinator.switchTransactionCoordinator(autoCommit);
        }
    }

    protected void setTransactionCoordinator(boolean managedConnection, boolean autoCommit) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            txCoordinator.setTransactionCoordinator(managedConnection, autoCommit);
        }
    }

    public void setManagedEnvironment(boolean managedConnection) throws SQLException {
        synchronized (getSynchronizationObject()) {
            setTransactionCoordinator(managedConnection, true);
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException("You cannot getAutoCommit on an unassociated closed connection.");
            }
            return txCoordinator.getAutoCommit();
        }
    }

    @Override
    public void commit() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException(
                        "You cannot commit a closed connection.",
                        SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new FBSQLException("Connection enlisted in distributed transaction", SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            txCoordinator.commit();
            invalidateTransactionLifetimeObjects();
        }
    }

    @Override
    public void rollback() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException(
                        "You cannot rollback closed connection.",
                        SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new FBSQLException("Connection enlisted in distributed transaction", SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
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
        SQLExceptionChainBuilder<SQLException> chainBuilder = new SQLExceptionChainBuilder<>();
        synchronized (getSynchronizationObject()) {
            if (log.isTraceEnabled()) {
                log.trace("Connection closed requested at", new RuntimeException("Connection close logging"));
            }
            try {
                freeStatements();
                if (metaData != null) metaData.close();
            } catch (SQLException e) {
                chainBuilder.append(e);
            } finally {
                metaData = null;
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
                                if (!SQLStateConstants.SQL_STATE_CONNECTION_CLOSED.equals(e.getSQLState())) {
                                    chainBuilder.append(e);
                                }
                            }
                        }
                    }

                    mc.close(this);
                    mc = null;
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
            throw new SQLException("Timeout should be >= 0", SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }
        if (Thread.holdsLock(getSynchronizationObject())) {
            // Trying to async check validity will not work (this shouldn't normally happen, except maybe when Jaybird
            // internals call isValid(int) or user code locks on result of getSynchronizationObject())
            return isValidImpl(timeout);
        }
        return isValidAsync(timeout);
    }

    private boolean isValidAsync(int timeout) {
        Future<Boolean> isValidFuture = ForkJoinPool.commonPool().submit(() -> isValidImpl(timeout));
        try {
            return timeout != 0 ? isValidFuture.get(timeout, TimeUnit.SECONDS) : isValidFuture.get();
        } catch (ExecutionException e) {
            log.debug("isValidImpl produced an exception", e);
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
        synchronized (getSynchronizationObject()) {
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
                    } catch (SQLFeatureNotSupportedException ignored) {
                        // Implementation doesn't support network timeout
                    }
                }

                db.getDatabaseInfo(new byte[] { ISCConstants.isc_info_ods_version, ISCConstants.isc_info_end }, 10);
                return true;
            } catch (SQLException ex) {
                log.debug("Exception while checking connection validity", ex);
                return false;
            } finally {
                if (networkTimeoutChanged) {
                    try {
                        getFbDatabase().setNetworkTimeout(originalNetworkTimeout);
                    } catch (SQLException e) {
                        log.debug("Exception while resetting connection network timeout", e);
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (metaData == null)
                metaData = new FBDatabaseMetaData(this);
            return metaData;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getLocalTransaction().inTransaction() && !mc.isManagedEnvironment()) {
                // TODO More specific exception, jaybird error code
                throw new FBSQLException("Calling setReadOnly(boolean) method " +
                        "is not allowed when transaction is already started.");
            }
            mc.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        synchronized (getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (!getAutoCommit() && !mc.isManagedEnvironment()) {
                txCoordinator.commit();
            }
            mc.setTransactionIsolation(level);
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return mc.getTransactionIsolation();
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return firstWarning;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        synchronized (getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT &&
                    resultSetType == ResultSet.TYPE_FORWARD_ONLY) {

                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
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

    // TODO Why unused? Remove?
    protected PreparedStatement prepareMetaDataStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability, true, false);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            GeneratedKeysSupport.Query query = getGeneratedKeysSupport()
                    .buildQuery(sql, autoGeneratedKeys);
            return prepareStatement(query);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            GeneratedKeysSupport.Query query = getGeneratedKeysSupport()
                    .buildQuery(sql, columnIndexes);
            return prepareStatement(query);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        synchronized (getSynchronizationObject()) {
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT
                    && resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

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

    @Override
    public CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT
                    && resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

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

    private Set<String> clientInfoPropNames = new HashSet<>();

    private static final AtomicIntegerFieldUpdater<FBConnection> SAVEPOINT_COUNTER_UPDATE =
            AtomicIntegerFieldUpdater.newUpdater(FBConnection.class, "savepointCounter");
    private volatile int savepointCounter;
    private final List<FBSavepoint> savepoints = new ArrayList<>();

    private int getNextSavepointCounter() {
        return SAVEPOINT_COUNTER_UPDATE.getAndIncrement(this);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        synchronized (getSynchronizationObject()) {
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
     *         savepoint to set.
     * @throws SQLException
     *         if something went wrong.
     */
    private void setSavepoint(FBSavepoint savepoint) throws SQLException {
        if (getAutoCommit()) {
            throw new SQLException("Connection.setSavepoint() method cannot be used in auto-commit mode.",
                    SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
        }

        if (mc.inDistributedTransaction()) {
            throw new SQLException("Connection enlisted in distributed transaction",
                    SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
        }

        txCoordinator.ensureTransaction();

        StringBuilder setSavepoint = new StringBuilder("SAVEPOINT ");
        getQuoteStrategy().appendQuoted(savepoint.getServerSavepointId(), setSavepoint);

        getGDSHelper().executeImmediate(setSavepoint.toString());
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            FBSavepoint savepoint = new FBSavepoint(name);
            setSavepoint(savepoint);

            return savepoint;
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.rollback(Savepoint) method cannot be used in auto-commit mode.",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            // TODO The error message and actual condition do not match
            if (!(savepoint instanceof FBSavepoint)) {
                throw new SQLException("Specified savepoint was not obtained from this connection.");
            }

            if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            FBSavepoint fbSavepoint = (FBSavepoint) savepoint;

            if (!fbSavepoint.isValid()) {
                throw new SQLException("Savepoint is no longer valid.");
            }

            StringBuilder rollbackSavepoint = new StringBuilder("ROLLBACK TO ");
            getQuoteStrategy().appendQuoted(fbSavepoint.getServerSavepointId(), rollbackSavepoint);
            getGDSHelper().executeImmediate(rollbackSavepoint.toString());
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.releaseSavepoint() method cannot be used in auto-commit mode.",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            // TODO The error message and actual condition do not match
            if (!(savepoint instanceof FBSavepoint)) {
                throw new SQLException("Specified savepoint was not obtained from this connection.");
            }

            FBSavepoint fbSavepoint = (FBSavepoint) savepoint;

            if (!fbSavepoint.isValid()) {
                throw new SQLException("Savepoint is no longer valid.");
            }

            StringBuilder rollbackSavepoint = new StringBuilder("RELEASE SAVEPOINT ");
            getQuoteStrategy().appendQuoted(fbSavepoint.getServerSavepointId(), rollbackSavepoint).append(" ONLY");
            getGDSHelper().executeImmediate(rollbackSavepoint.toString());

            fbSavepoint.invalidate();

            savepoints.remove(fbSavepoint);
        }
    }

    /**
     * Invalidate all savepoints.
     */
    protected void invalidateSavepoints() {
        synchronized (getSynchronizationObject()) {
            for (FBSavepoint savepoint : savepoints) {
                savepoint.invalidate();
            }

            savepoints.clear();
        }
    }

    /**
     * Returns a FBLocalTransaction instance that enables a component to
     * demarcate resource manager local transactions on this connection.
     */
    public FBLocalTransaction getLocalTransaction() {
        synchronized (getSynchronizationObject()) {
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

    /**
     * Check if this connection is currently involved in a transaction
     */
    public boolean inTransaction() throws SQLException {
        return getGDSHelper().inTransaction();
    }

    @Override
    public String getIscEncoding() throws SQLException {
        return getGDSHelper().getIscEncoding();
    }

    public void addWarning(SQLWarning warning) {
        synchronized (getSynchronizationObject()) {
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
            throw new FbExceptionBuilder().exception(ISCConstants.isc_req_no_trans).toFlatSQLException();

        return mc.getGDSHelper();
    }

    @Override
    public boolean isUseFirebirdAutoCommit() {
        DatabaseConnectionProperties props = connectionProperties();
        return props != null && props.isUseFirebirdAutocommit();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Checks if client info is supported.
     *
     * @throws SQLException
     *         If the client info is not supported, or if there is no database connection.
     */
    protected void checkClientInfoSupport() throws SQLException {
        if (!getFbDatabase().getServerVersion().isEqualOrAbove(2, 0)) {
            throw new FBDriverNotCapableException(
                    "Required functionality (RDB$SET_CONTEXT()) only available in Firebird 2.0 or higher");
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        checkValidity();
        checkClientInfoSupport();

        Properties result = new Properties();
        try (PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL)) {
            for (String propName : clientInfoPropNames) {
                result.put(propName, getClientInfo(stmt, propName));
            }
        }

        return result;
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        checkValidity();
        checkClientInfoSupport();

        try (PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL)) {
            return getClientInfo(stmt, name);
        }
    }

    protected String getClientInfo(PreparedStatement stmt, String name) throws SQLException {
        stmt.clearParameters();

        stmt.setString(1, name);
        stmt.setString(2, name);

        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next())
                return null;

            String sessionContext = rs.getString(1);
            String transactionContext = rs.getString(2);

            if (transactionContext != null)
                return transactionContext;
            else if (sessionContext != null)
                return sessionContext;
            else
                return null;

        }

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        SQLExceptionChainBuilder<SQLClientInfoException> chain = new SQLExceptionChainBuilder<>();
        try {
            checkValidity();
            checkClientInfoSupport();

            try (PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL)) {
                for (String propName : properties.stringPropertyNames()) {
                    String propValue = properties.getProperty(propName);

                    try {
                        setClientInfo(stmt, propName, propValue);
                    } catch (SQLClientInfoException ex) {
                        chain.append(ex);
                    }
                }
            }

        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }

        if (chain.hasException())
            throw chain.getException();
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            checkValidity();
            checkClientInfoSupport();

            try (PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL)) {
                setClientInfo(stmt, name, value);
            }
        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }
    }

    protected void setClientInfo(PreparedStatement stmt, String name, String value) throws SQLException {
        try {
            stmt.clearParameters();
            stmt.setString(1, name);
            stmt.setString(2, value);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new FBDriverConsistencyCheckException("Expected result from RDB$SET_CONTEXT call");
                }

                // needed, since the value is set on fetch!!!
                rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new SQLClientInfoException(null, ex);
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // TODO Write implementation
        checkValidity();
        throw new FBDriverNotCapableException();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            SQLPermission sqlPermission = new SQLPermission(PERMISSION_SET_NETWORK_TIMEOUT);
            securityManager.checkPermission(sqlPermission);
        }
        if (executor == null) {
            throw FbExceptionBuilder
                    .forException(JaybirdErrorCodes.jb_invalidExecutor)
                    .toFlatSQLException();
        }
        if (milliseconds < 0) {
            throw FbExceptionBuilder
                    .forException(JaybirdErrorCodes.jb_invalidTimeout)
                    .toFlatSQLException();
        }
        synchronized (getSynchronizationObject()) {
            checkValidity();
            getFbDatabase().setNetworkTimeout(milliseconds);
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return getFbDatabase().getNetworkTimeout();
    }

    @Override
    public final Object getSynchronizationObject() {
        final FBManagedConnection managedConnection = mc;
        if (managedConnection != null) {
            return managedConnection.getSynchronizationObject();
        } else {
            return this;
        }
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

}
