/*
 SPDX-FileCopyrightText: Copyright 2001-2003 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2008 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Nikolay Samofatov
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2006 Ludovic Orban
 SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.jspecify.annotations.Nullable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_attemptToDestroyManagedConnectionActiveTx;

/**
 * A physical connection handle to a Firebird database, providing a {@code XAResource}.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 * @version 1.0
 */
public final class FBManagedConnection implements ExceptionListener {

    private static final System.Logger log = System.getLogger(FBManagedConnection.class.getName());

    private final FBManagedConnectionFactory mcf;

    private final List<XcaConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();
    private static final AtomicReferenceFieldUpdater<FBManagedConnection, @Nullable FBConnection> connectionHandleUpdater =
            AtomicReferenceFieldUpdater.newUpdater(FBManagedConnection.class, FBConnection.class, "connectionHandle");
    @SuppressWarnings("java:S3077")
    private volatile @Nullable FBConnection connectionHandle;
    // This is a bit of hack to be able to get attach warnings into the FBConnection that is created later.
    private static final AtomicReferenceFieldUpdater<FBManagedConnection, @Nullable SQLWarning> unnotifiedWarningsUpdater =
            AtomicReferenceFieldUpdater.newUpdater(FBManagedConnection.class, SQLWarning.class, "unnotifiedWarnings");
    @SuppressWarnings("java:S3077")
    private volatile @Nullable SQLWarning unnotifiedWarnings;

    private int timeout = 0;

    private final Map<Xid, FbTransaction> xidMap = new ConcurrentHashMap<>();

    private @Nullable GDSHelper gdsHelper;
    private final FbDatabase database;
    private @Nullable XAResource xaResource;
    private final FBConnectionRequestInfo cri;
    private @Nullable FBTpbMapper transactionMapping;
    private TransactionParameterBuffer tpb;
    private int transactionIsolation;

    private volatile boolean managedEnvironment = true;
    private final Set<Xid> preparedXid = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean inDistributedTransaction = false;

    FBManagedConnection(FBConnectionRequestInfo cri, FBManagedConnectionFactory mcf) throws SQLException {
        this(cri, mcf, false);
    }

    FBManagedConnection(FBConnectionRequestInfo cri, FBManagedConnectionFactory mcf, boolean createDb)
            throws SQLException {
        this.mcf = requireNonNull(mcf, "mcf");
        this.cri = requireNonNull(cri, "cri");
        tpb = mcf.getDefaultTpb();
        transactionIsolation = mcf.getDefaultTransactionIsolation();

        //TODO: XIDs in limbo should be loaded so that XAER_DUPID can be thrown appropriately

        IConnectionProperties connectionProperties = cri.asIConnectionProperties();

        if (connectionProperties.getEncoding() == null && connectionProperties.getCharSet() == null) {
            String defaultEncoding = getDefaultConnectionEncoding();
            if (defaultEncoding == null) {
                throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_noConnectionEncoding);
            }
            connectionProperties.setEncoding(defaultEncoding);
        }

        if (connectionProperties.getConnectTimeout() == PropertyConstants.TIMEOUT_NOT_SET
                && DriverManager.getLoginTimeout() > 0) {
            connectionProperties.setConnectTimeout(DriverManager.getLoginTimeout());
        }

        database = mcf.getDatabaseFactory().connect(connectionProperties);
        database.addDatabaseListener(new MCDatabaseListener());
        database.addExceptionListener(this);
        if (createDb) {
            database.createDatabase();
        } else {
            database.attach();
        }

        gdsHelper = new GDSHelper(database);
    }

    @Override
    public void errorOccurred(Object source, SQLException ex) {
        log.log(TRACE, "Error occurred", ex);
        if (FatalErrorHelper.isFatal(ex)) {
            notify(connectionErrorOccurredNotifier,
                    new XcaConnectionEvent(this, XcaConnectionEvent.EventType.CONNECTION_ERROR_OCCURRED, ex));
        }
    }

    /**
     * Get instance of {@link GDSHelper} connected with this managed connection.
     *
     * @return instance of {@link GDSHelper}.
     * @throws SQLException
     *         If this connection has no GDSHelper
     */
    public GDSHelper getGDSHelper() throws SQLException {
        if (gdsHelper == null) {
            // TODO Right error code?
            throw FbExceptionBuilder.toException(ISCConstants.isc_req_no_trans);
        }
        return gdsHelper;
    }

    public boolean isManagedEnvironment() {
        return managedEnvironment;
    }

    public boolean inTransaction() {
        return gdsHelper != null && gdsHelper.inTransaction();
    }

    public void setManagedEnvironment(boolean managedEnvironment) throws SQLException {
        this.managedEnvironment = managedEnvironment;
        final FBConnection connection = connectionHandle;
        if (connection != null) {
            connection.setManagedEnvironment(managedEnvironment);
        }
    }

    /**
     * Returns a {@code FBLocalTransaction} instance.
     * <p>
     * The FBLocalTransaction is used by the container to manage local
     * transactions for a RM instance.
     * </p>
     *
     * @return FBLocalTransaction instance
     */
    public FBLocalTransaction getLocalTransaction() {
        return new FBLocalTransaction(this);
    }

    /**
     * Add an {@code XcaConnectionEventListener} listener. The listener will be notified when a
     * {@code XcaConnectionEvent} occurs.
     *
     * @param listener
     *         The {@code XcaConnectionEventListener} to be added
     */
    public void addConnectionEventListener(XcaConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    /**
     * Remove a {@code XcaConnectionEventListener} from the listing of listeners that will be notified for a
     * {@code XcaConnectionEvent}.
     *
     * @param listener
     *         The {@code FirebirdConnectionEventListener} to be removed
     */
    public void removeConnectionEventListener(XcaConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    /**
     * Application server calls this method to force any cleanup on the managed connection instance.
     * <p>
     * The method {@code cleanup} initiates a cleanup of the any client-specific state as maintained by a managed
     * connection instance. The cleanup should invalidate all connection handles that had been created using this
     * managed connection instance. Any attempt by an application component to use the connection handle after cleanup
     * of the underlying managed connection should result in an exception.
     * </p>
     * <p>
     * The cleanup of managed connection is always driven by an application server. An application server should not
     * invoke {@code cleanup} when there is an uncompleted transaction (associated with a managed connection instance)
     * in progress.
     * </p>
     * <p>
     * The invocation of the {@code cleanup} method on an already cleaned-up connection should not throw an exception.
     * </p>
     * <p>
     * The cleanup of a managed connection instance resets its client specific state and prepares the connection to be
     * put back in to a connection pool. The cleanup method should not cause resource adapter to close the physical pipe
     * and reclaim system resources associated with the physical connection.
     * </p>
     *
     * @throws SQLException
     *         generic exception if operation fails
     */
    // TODO Consider removing (though might be used to implement XADataSource/ConnectionPoolDataSource without proxies)
    public void cleanup() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            disassociateConnections();

            clearCurrentTransaction();

            // reset the transaction mapping to use the default of the MCF
            transactionMapping = null;
            // reset the TPB from the previous transaction.
            tpb = mcf.getDefaultTpb();
            transactionIsolation = mcf.getDefaultTransactionIsolation();
        }
    }

    private void clearCurrentTransaction() {
        GDSHelper gdsHelper = this.gdsHelper;
        if (gdsHelper != null) {
            gdsHelper.clearCurrentTransaction();
        }
    }

    private void setCurrentTransaction(FbTransaction transaction) throws SQLException {
        getGDSHelper().setCurrentTransaction(transaction);
    }

    /**
     * Disassociate connections from current managed connection.
     */
    private void disassociateConnections() throws SQLException {
        final FBConnection connection = connectionHandle;
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Disassociate connections without cleanly closing them.
     */
    private void forceDisassociateConnections() {
        final FBConnection connection = connectionHandleUpdater.getAndSet(this, null);
        if (connection != null) {
            try {
                connection.setManagedConnection(null);
                connection.close();
            } catch (SQLException sqlex) {
                log.log(DEBUG, "Exception ignored during forced disassociation", sqlex);
            }
        }
    }

    /**
     * Creates a new connection handle for the underlying physical connection represented by the managed connection
     * instance. This connection handle is used by the application code to refer to the underlying physical connection.
     *
     * @return instance representing the connection handle
     * @throws SQLException
     *         generic exception if operation fails
     */
    @SuppressWarnings("java:S2095")
    public FBConnection getConnection() throws SQLException {
        disassociateConnections();

        FBConnection c = mcf.newConnection(this);
        c.setManagedEnvironment(isManagedEnvironment());
        FBConnection previous = connectionHandleUpdater.getAndSet(this, c);
        if (previous != null) {
            previous.setManagedConnection(null);
            if (log.isLoggable(DEBUG)) {
                // This would indicate a concurrent getConnection call on this managed connection
                log.log(DEBUG, "A connection was already associated with the managed connection",
                        new RuntimeException("debug call trace"));
            }
            try {
                previous.setManagedConnection(null);
                previous.close();
            } catch (SQLException e) {
                log.log(DEBUG, "Error forcing previous connection to close", e);
            }
        }
        final SQLWarning warnings = unnotifiedWarningsUpdater.getAndSet(this, null);
        if (warnings != null) {
            c.addWarning(warnings);
        }
        return c;
    }

    /**
     * Destroys the physical connection to the underlying resource manager.
     * <p>
     * To manage the size of the connection pool, an application server can explicitly call {@code destroy} to destroy
     * a physical connection. A resource adapter should destroy all allocated system resources for this managed
     * connection instance when the method destroy is called.
     * </p>
     *
     * @throws SQLException
     *         generic exception if operation failed
     */
    public void destroy() throws SQLException {
        destroy(null);
    }

    public void destroy(@Nullable XcaConnectionEvent connectionEvent) throws SQLException {
        if (gdsHelper == null) return;

        try {
            if (isBrokenConnection(connectionEvent)) {
                FbDatabase currentDatabase = gdsHelper.getCurrentDatabase();
                currentDatabase.forceClose();
            } else {
                if (inTransaction()) {
                    // TODO should we skip disassociation in this case?
                    throw FbExceptionBuilder.toException(jb_attemptToDestroyManagedConnectionActiveTx);
                }

                gdsHelper.detachDatabase();
            }
        } finally {
            gdsHelper = null;
            forceDisassociateConnections();
        }
    }

    private boolean isBrokenConnection(@Nullable XcaConnectionEvent connectionEvent) {
        if (connectionEvent == null
                || connectionEvent.getEventType() != XcaConnectionEvent.EventType.CONNECTION_ERROR_OCCURRED) {
            return false;
        }

        return FatalErrorHelper.isBrokenConnection(connectionEvent.getException());
    }

    /**
     * Returns an {@code javax.transaction.xa.XAResource} instance. An application server enlists this XAResource
     * instance with the Transaction Manager if the FBManagedConnection instance is being used in a Java EE transaction
     * that is coordinated by the Transaction Manager.
     *
     * @return XAResource instance
     */
    public XAResource getXAResource() {
        log.log(TRACE, "XAResource requested from FBManagedConnection");
        try (LockCloseable ignored = withLock()) {
            if (xaResource == null) {
                xaResource = new FbMcXaResource();
            }
            return xaResource;
        }
    }

    // --------------------------------------------------------------
    // XAResource implementation
    // The actual XAResource is exposed using the inner class FbMcXaResource
    // --------------------------------------------------------------

    // TODO validate correctness of state set
    private static final Set<TransactionState> XID_ACTIVE_STATE =
            unmodifiableSet(EnumSet.of(TransactionState.ACTIVE, TransactionState.PREPARED, TransactionState.PREPARING));

    boolean isXidActive(Xid xid) {
        FbTransaction transaction = xidMap.get(xid);
        return transaction != null && XID_ACTIVE_STATE.contains(transaction.getState());
    }

    private void commit(Xid id, boolean onePhase) throws XAException {
        mcf.notifyCommit(this, id, onePhase);
    }

    /**
     * The {@code internalCommit} method performs the requested commit and may throw an XAException to be interpreted
     * by the caller.
     *
     * @param xid
     *         a {@code Xid} value
     * @param onePhase
     *         a {@code true} if this is not a two-phase commit (not a distributed transaction)
     * @throws XAException
     *         if an error occurs
     */
    void internalCommit(Xid xid, boolean onePhase) throws XAException {
        log.log(TRACE, "Commit called: {0}", xid);
        FbTransaction committingTr = xidMap.get(xid);

        // check that prepare has NOT been called when onePhase = true
        if (onePhase && isPrepared(xid)) {
            throw new FBXAException("Cannot commit one-phase when transaction has been prepared", XAException.XAER_PROTO);
        }

        // check that prepare has been called when onePhase = false
        if (!onePhase && !isPrepared(xid)) {
            throw new FBXAException("Cannot commit two-phase when transaction has not been prepared", XAException.XAER_PROTO);
        }

        if (committingTr == null) {
            throw new FBXAException("Commit called with unknown transaction", XAException.XAER_NOTA);
        }

        try {
            if (committingTr == getGDSHelper().getCurrentTransaction()) {
                throw new FBXAException("Commit called with non-ended xid", XAException.XAER_PROTO);
            }

            committingTr.commit();
        } catch (SQLException ge) {
            if (gdsHelper != null) {
                try {
                    committingTr.rollback();
                } catch (SQLException ge2) {
                    log.log(DEBUG, "Exception rolling back failed tx: ", ge2);
                }
            } else {
                log.log(WARNING, "Unable to rollback failed tx, connection closed or lost");
            }
            throw new FBXAException(ge.getMessage(), XAException.XAER_RMERR, ge);
        } finally {
            xidMap.remove(xid);
            preparedXid.remove(xid);
        }
    }

    private boolean isPrepared(Xid xid) {
        return preparedXid.contains(xid);
    }

    /**
     * Dissociates a resource from a global transaction.
     *
     * @throws XAException
     *         Occurs when the state was not correct (end called twice), or the transaction ID is wrong.
     */
    private void end(Xid id, int flags) throws XAException {
        if (flags != XAResource.TMSUCCESS && flags != XAResource.TMFAIL && flags != XAResource.TMSUSPEND)
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMSUCCESS, TMFAIL, TMSUSPEND", XAException.XAER_PROTO);
        internalEnd(id, flags);
        mcf.notifyEnd(this, id);
        inDistributedTransaction = false;

        try {
            // This will reset the managed environment of the associated connections and set the transaction coordinator to local
            // TODO This is a bit of a hack; need to find a better way
            setManagedEnvironment(isManagedEnvironment());
        } catch (SQLException ex) {
            throw new FBXAException("Reset of managed state failed", XAException.XAER_RMERR, ex);
        }
    }

    /**
     * The {@code internalEnd} method ends the xid as requested if appropriate and throws a XAException including the
     * appropriate XA error code and a message if not. The caller can decode the exception as necessary.
     *
     * @param xid
     *         a {@code Xid} value
     * @param flags
     *         an {@code int} value
     * @throws XAException
     *         if an error occurs
     */
    void internalEnd(Xid xid, int flags) throws XAException {
        log.log(TRACE, "End called: {0}", xid);
        FbTransaction endingTr = xidMap.get(xid);
        if (endingTr == null) {
            throw new FBXAException("Unrecognized transaction", XAException.XAER_NOTA);
        }

        switch (flags) {
        case XAResource.TMFAIL -> {
            try {
                endingTr.rollback();
                clearCurrentTransaction();
            } catch (SQLException ex) {
                throw new FBXAException("can't rollback transaction", XAException.XAER_RMFAIL, ex);
            }
        }
        case XAResource.TMSUCCESS, XAResource.TMSUSPEND -> {
            if (isCurrentTransaction(endingTr)) {
                clearCurrentTransaction();
            } else {
                throw new FBXAException("You are trying to %s a transaction that is not the current transaction"
                        .formatted(flags == XAResource.TMSUSPEND ? "suspend" : "end"),
                        XAException.XAER_INVAL);
            }
        }
        default -> {
            // do nothing
        }
        }
    }

    private boolean isCurrentTransaction(@Nullable FbTransaction transaction) {
        GDSHelper gdsHelper = this.gdsHelper;
        return gdsHelper != null && gdsHelper.getCurrentTransaction() == transaction;
    }

    private XidQueries getXidQueries() {
        return XidQueries.forVersion(database.getServerVersion());
    }

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure). It is assumed this will be
     * called after a failed commit or rollback.
     *
     * @throws XAException
     *         Occurs when the state was not correct (end never called), or the transaction ID is wrong.
     */
    private void forget(Xid id) throws XAException {
        // TODO Should this method call FBManagedConnectionFactory.forget?
        long inLimboId = -1;

        try {
            // find XID
            // TODO: Is there a reason why this piece of code can't use the JDBC Statement class?
            FbTransaction trHandle2 = database.startTransaction(tpb);
            try (FbStatement stmtHandle2 = database.createStatement(trHandle2)) {
                var gdsHelper2 = new GDSHelper(database);
                gdsHelper2.setCurrentTransaction(trHandle2);

                stmtHandle2.prepare(getXidQueries().forgetFindQuery());

                var dataProvider = new DataProvider(stmtHandle2);
                stmtHandle2.addStatementListener(dataProvider);

                RowDescriptor rowDescriptor = stmtHandle2.getRowDescriptor();
                var field0 = FBField.createField(rowDescriptor.getFieldDescriptor(0),
                        dataProvider.asFieldDataProvider(0), gdsHelper2, false);
                var field1 = FBField.createField(rowDescriptor.getFieldDescriptor(1),
                        dataProvider.asFieldDataProvider(1), gdsHelper2, false);

                while (dataProvider.next()) {
                    long inLimboTxId = field0.getLong();
                    if (matchesXid(id, inLimboTxId, field1.getBytes())) {
                        inLimboId = inLimboTxId;
                        break;
                    }
                }
            } finally {
                trHandle2.commit();
            }
        } catch (SQLException ex) {
            log.log(DEBUG, "can't perform query to fetch xids", ex);
            throw new FBXAException(XAException.XAER_RMFAIL, ex);
        }

        if (inLimboId == -1) {
            throw new FBXAException("XID not found", XAException.XAER_NOTA); // TODO: is XAER_NOTA the proper error code ?
        }

        try {
            // delete XID
            FbTransaction trHandle2 = database.startTransaction(tpb);
            try (FbStatement stmtHandle2 = database.createStatement(trHandle2)) {
                stmtHandle2.prepare(getXidQueries().forgetDelete() + inLimboId);
                stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);
            } finally {
                trHandle2.commit();
            }
        } catch (SQLException ex) {
            throw new FBXAException("can't perform query to delete xids", XAException.XAER_RMFAIL, ex);
        }
    }

    private static boolean matchesXid(Xid id, long fbTxId, byte[] fbTxMessage) {
        try {
            var xid = new FBXid(fbTxMessage, fbTxId);
            return Arrays.equals(xid.getGlobalTransactionId(), id.getGlobalTransactionId())
                    && Arrays.equals(xid.getBranchQualifier(), id.getBranchQualifier());
        } catch (FBIncorrectXidException ex) {
            if (log.isLoggable(WARNING)) {
                String message = "incorrect XID format in RDB$TRANSACTIONS where RDB$TRANSACTION_ID=%d: %s"
                        .formatted(fbTxId, ByteArrayHelper.toHexString(fbTxMessage));
                log.log(WARNING, message + "; see debug level for stacktrace", ex);
                log.log(DEBUG, message, ex);
            }
            return false;
        }
    }

    private int getTransactionTimeout() {
        return timeout;
    }

    /**
     * Prepares a transaction to commit.
     *
     * @throws XAException
     *         Occurs when the state was not correct (end never called), the transaction ID is wrong, or the connection
     *         was set to Auto-Commit.
     */
    private int prepare(Xid xid) throws XAException {
        return mcf.notifyPrepare(this, xid);
    }

    int internalPrepare(Xid xid) throws FBXAException {
        log.log(TRACE, "prepare called: {0}", xid);
        FbTransaction committingTr = xidMap.get(xid);
        if (committingTr == null) {
            throw new FBXAException("Prepare called with unknown transaction", XAException.XAER_NOTA);
        }
        try {
            if (isCurrentTransaction(committingTr)) {
                throw new FBXAException("Prepare called with non-ended xid", XAException.XAER_PROTO);
            }

            FBXid fbxid = xid instanceof FBXid castXid ? castXid : new FBXid(xid);
            byte[] message = fbxid.toBytes();

            committingTr.prepare(message);
        } catch (SQLException ge) {
            try {
                if (gdsHelper != null) {
                    committingTr.rollback();
                } else {
                    log.log(WARNING, "Unable to rollback failed tx, connection closed or lost");
                }
            } catch (SQLException ge2) {
                log.log(DEBUG, "Exception rolling back failed tx", ge2);
            } finally {
                xidMap.remove(xid);
            }

            log.log(WARNING, "error in prepare", ge);
            throw new FBXAException(XAException.XAER_RMERR, ge);
        }

        preparedXid.add(xid);
        return XAResource.XA_OK;
    }

    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     * The transaction manager calls this method during recovery to obtain the
     * list of transaction branches that are currently in prepared or
     * heuristically completed states.
     *
     * @param flags
     *         One of TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS. TMNOFLAGS must be used when no other flags are set in flags.
     * @return The resource manager returns zero or more XIDs for the transaction branches that are currently in a
     * prepared or heuristically completed state. If an error occurs during the operation, the resource manager should
     * throw the appropriate XAException.
     * @throws XAException
     *         An error has occurred. Possible values are XAER_RMERR, XAER_RMFAIL, XAER_INVAL, and XAER_PROTO.
     */
    private Xid[] recover(int flags) throws javax.transaction.xa.XAException {
        if (flags != XAResource.TMSTARTRSCAN && flags != XAResource.TMENDRSCAN && flags != XAResource.TMNOFLAGS
                && flags != (XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN)) {
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS, TMSTARTRSCAN|TMENDRSCAN", XAException.XAER_PROTO);
        }

        try {
            // if (!((flags & XAResource.TMSTARTRSCAN) == 0))
//            if ((flags & XAResource.TMENDRSCAN) == 0 && (flags & XAResource.TMNOFLAGS) == 0)
//                return new Xid[0];

            var xids = new ArrayList<FBXid>();

            FbTransaction trHandle = database.startTransaction(tpb);
            try (FbStatement stmtHandle = database.createStatement(trHandle)) {
                var gdsHelper = new GDSHelper(database);
                gdsHelper.setCurrentTransaction(trHandle);

                stmtHandle.prepare(getXidQueries().recoveryQuery());

                var dataProvider = new DataProvider(stmtHandle);
                stmtHandle.addStatementListener(dataProvider);

                stmtHandle.execute(RowValue.EMPTY_ROW_VALUE);

                RowDescriptor rowDescriptor = stmtHandle.getRowDescriptor();
                var field0 = FBField.createField(rowDescriptor.getFieldDescriptor(0),
                        dataProvider.asFieldDataProvider(0), gdsHelper, false);
                var field1 = FBField.createField(rowDescriptor.getFieldDescriptor(1),
                        dataProvider.asFieldDataProvider(1), gdsHelper, false);

                while (dataProvider.next()) {
                    long inLimboTxId = field0.getLong();
                    byte[] inLimboMessage = field1.getBytes();

                    FBXid xid = extractXid(inLimboMessage, inLimboTxId);
                    if (xid != null) {
                        xids.add(xid);
                    }
                }
            } finally {
                trHandle.commit();
            }

            return xids.toArray(new Xid[0]);
        } catch (SQLException e) {
            throw new FBXAException("can't perform query to fetch xids", XAException.XAER_RMFAIL, e);
        }
    }

    private static @Nullable FBXid extractXid(byte[] xidData, long txId) {
        try {
            return new FBXid(xidData, txId);
        } catch (FBIncorrectXidException e) {
            if (log.isLoggable(WARNING)) {
                log.log(WARNING,
                        "ignoring XID stored with invalid format in RDB$TRANSACTIONS for RDB$TRANSACTION_ID={0}: {1}",
                        txId, ByteArrayHelper.toHexString(xidData));
            }
        }
        return null;
    }

    /**
     * Obtain a single prepared transaction branch from a resource manager, based on a Xid
     *
     * @param externalXid
     *         The Xid to find
     * @return The Xid if found, otherwise null.
     * @throws XAException
     *         An error has occurred. Possible values are XAER_RMERR,
     *         XAER_RMFAIL, XAER_INVAL, and XAER_PROTO.
     */
    @Nullable Xid findSingleXid(Xid externalXid) throws javax.transaction.xa.XAException {
        try {
            FbTransaction trHandle = database.startTransaction(tpb);
            try (FbStatement stmtHandle = database.createStatement(trHandle)) {
                var gdsHelper = new GDSHelper(database);
                gdsHelper.setCurrentTransaction(trHandle);

                stmtHandle.prepare(getXidQueries().recoveryQueryParameterized());

                var dataProvider = new DataProvider(stmtHandle);
                stmtHandle.addStatementListener(dataProvider);

                var tempXid = new FBXid(externalXid);
                stmtHandle.execute(RowValue.of(stmtHandle.getParameterDescriptor(), tempXid.toBytes()));

                RowDescriptor rowDescriptor = stmtHandle.getRowDescriptor();
                var field0 = FBField.createField(rowDescriptor.getFieldDescriptor(0),
                        dataProvider.asFieldDataProvider(0), gdsHelper, false);
                var field1 = FBField.createField(rowDescriptor.getFieldDescriptor(1),
                        dataProvider.asFieldDataProvider(1), gdsHelper, false);

                stmtHandle.fetchRows(1);
                if (dataProvider.next()) {
                    long inLimboTxId = field0.getLong();
                    byte[] inLimboMessage = field1.getBytes();
                    return extractXid(inLimboMessage, inLimboTxId);
                }
                return null;
            } finally {
                trHandle.commit();
            }
        } catch (SQLException e) {
            throw new FBXAException("can't perform query to fetch xids", XAException.XAER_RMFAIL, e);
        }
    }

    /**
     * @see FbAttachment#withLock()
     */
    public LockCloseable withLock() {
        return database.withLock();
    }

    /**
     * @see FbAttachment#isLockedByCurrentThread()
     */
    public boolean isLockedByCurrentThread() {
        return database.isLockedByCurrentThread();
    }

    private static final class DataProvider implements StatementListener {

        private static final int NO_ASYNC_FETCH = -1;

        private final Deque<RowValue> rows = new ArrayDeque<>();
        private final FbStatement statementHandle;
        private @Nullable RowValue currentRow;
        private boolean moreRows = true;
        private int fetchAsyncAt = NO_ASYNC_FETCH;

        private DataProvider(FbStatement statementHandle) {
            this.statementHandle = requireNonNull(statementHandle, "statementHandle");
        }

        boolean hasNext() throws SQLException {
            if (rows.isEmpty() && moreRows) {
                fetch();
            } else if (rows.size() == fetchAsyncAt && moreRows) {
                fetchAsync();
            }
            return !rows.isEmpty();
        }

        boolean next() throws SQLException {
            if (hasNext()) {
                currentRow = requireNonNull(rows.pollFirst(), "row");
                return true;
            }
            currentRow = null;
            return false;
        }

        private void fetch() throws SQLException {
            statementHandle.fetchRows(Integer.MAX_VALUE);
        }

        private void fetchAsync() throws SQLException {
            statementHandle.asyncFetchRows(Integer.MAX_VALUE);
        }

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        @Override
        public void afterLast(FbStatement sender) {
            moreRows = false;
        }

        @Override
        public void fetchComplete(FbStatement sender, FetchDirection fetchDirection, int rows) {
            if (fetchAsyncAt * 3 < rows) {
                fetchAsyncAt = rows >= 15 ? Math.min(rows / 3, 200) : NO_ASYNC_FETCH;
            }
        }

        FieldDataProvider asFieldDataProvider(int fieldPos) {
            return new FieldDataProvider() {
                @Override
                public byte @Nullable [] getFieldData() {
                    assert currentRow != null : "call to getFieldData() while not on a row";
                    return currentRow.getFieldData(fieldPos);
                }

                @Override
                public void setFieldData(byte @Nullable [] data) {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

    /**
     * Rolls back the work, assuming it was done on behalf of the specified
     * transaction.
     *
     * @throws XAException
     *         Occurs when the state was not correct (end never called), the transaction ID is wrong, the connection
     *         was set to Auto-Commit, or the rollback on the underlying connection fails. The error code differs
     *         depending on the exact situation.
     */
    private void rollback(Xid xid) throws XAException {
        mcf.notifyRollback(this, xid);
    }

    void internalRollback(Xid xid) throws XAException {
        log.log(TRACE, "rollback called: {0}", xid);
        FbTransaction committingTr = xidMap.get(xid);
        if (committingTr == null) {
            throw new FBXAException("Rollback called with unknown transaction: " + xid);
        }

        try {
            if (isCurrentTransaction(committingTr)) {
                throw new FBXAException("Rollback called with non-ended xid", XAException.XAER_PROTO);
            }

            try {
                committingTr.rollback();
            } finally {
                xidMap.remove(xid);
                preparedXid.remove(xid);
            }
        } catch (SQLException ge) {
            log.log(DEBUG, "Exception in rollback", ge);
            throw new FBXAException(ge.getMessage(), XAException.XAER_RMERR, ge);
        }
    }

    /**
     * Sets the transaction timeout. This is saved, but the value is not used by
     * the current implementation.
     *
     * @param timeout
     *         The timeout to be set in seconds
     */
    private boolean setTransactionTimeout(int timeout) {
        this.timeout = timeout;
        return true;
    }

    public boolean inDistributedTransaction() {
        return inDistributedTransaction;
    }

    /**
     * Associates a JDBC connection with a global transaction. We assume that
     * end will be called followed by prepare, commit, or rollback. If start is
     * called after end but before commit or rollback, there is no way to
     * distinguish work done by different transactions on the same connection.
     * If start is called more than once before end, either it's a duplicate
     * transaction ID or illegal transaction ID (since you can't have two
     * transactions associated with one DB connection).
     *
     * @param id
     *         A global transaction identifier to be associated with the resource
     * @param flags
     *         One of TMNOFLAGS, TMJOIN, or TMRESUME
     * @throws XAException
     *         Occurs when the state was not correct (start called twice), the transaction ID is wrong, or the instance
     *         has already been closed.
     */
    private void start(Xid id, int flags) throws XAException {
        if (flags != XAResource.TMNOFLAGS && flags != XAResource.TMJOIN && flags != XAResource.TMRESUME) {
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMNOFLAGS, TMJOIN, TMRESUME", XAException.XAER_PROTO);
        }
        if (flags == XAResource.TMJOIN) {
            throw new FBXAException("Joining two transactions is not supported", XAException.XAER_RMFAIL);
        }

        try {
            // reset the transaction parameters for the managed scenario
            setTransactionIsolation(mcf.getDefaultTransactionIsolation());

            internalStart(id, flags);

            mcf.notifyStart(this, id);

            inDistributedTransaction = true;

            // This will reset the managed environment of the associated connections and set the transaction coordinator to managed
            // TODO This is a bit of a hack; need to find a better way
            setManagedEnvironment(isManagedEnvironment());

        } catch (SQLException e) {
            throw new FBXAException(XAException.XAER_RMERR, e);
        }
    }

    /**
     * Perform the internal processing to start associate a JDBC connection with
     * a global transaction.
     *
     * @param id
     *         A global transaction identifier to be associated with the resource
     * @param flags
     *         One of TMNOFLAGS, TMJOIN, or TMRESUME
     * @throws XAException
     *         If the transaction is already started, or this connection cannot participate in the distributed
     *         transaction
     * @see #start(Xid, int)
     */
    public void internalStart(Xid id, int flags) throws XAException, SQLException {
        log.log(TRACE, "start called: {0}", id);

        if (!isCurrentTransaction(null)) {
            throw new FBXAException("Transaction already started", XAException.XAER_PROTO);
        }

        findIscTrHandle(id, flags);
    }

    /**
     * Starts a transaction defined by {@code setTransactionSql}, and associate it with a JDBC connection.
     *
     * @param xid
     *         global transaction identifier to be associated with the resource
     * @param sql
     *         {@code SET TRANSACTION} statement text
     * @throws XAException
     *         if this connection cannot participate in the distributed transaction
     * @throws SQLException
     *         for database access errors
     */
    public void internalStart(Xid xid, String sql) throws XAException, SQLException {
        clearCurrentTransaction();
        requireNewXid(xid);
        registerNewTransaction(xid, database.startTransaction(sql));
    }

    // FB public methods. Could be package if packages reorganized.

    /**
     * Close this connection with regard to a wrapping {@code AbstractConnection}.
     *
     * @param c
     *         The {@code AbstractConnection} that is being closed
     */
    public void close(FBConnection c) {
        c.setManagedConnection(null);
        if (!connectionHandleUpdater.compareAndSet(this, c, null) && log.isLoggable(DEBUG)) {
            log.log(DEBUG, "Call of close for connection not currently associated with this managed connection",
                    new RuntimeException("debug call trace"));
        }
        var ce = new XcaConnectionEvent(this, XcaConnectionEvent.EventType.CONNECTION_CLOSED);
        ce.setConnectionHandle(c);
        notify(connectionClosedNotifier, ce);
    }

    /**
     * Get information about the current connection parameters.
     *
     * @return instance of {@link FBConnectionRequestInfo}.
     */
    public FBConnectionRequestInfo getConnectionRequestInfo() {
        return cri;
    }

    /**
     * @return a copy of the current TPB
     */
    public TransactionParameterBuffer getTransactionParameters() {
        TransactionParameterBuffer currentTpb;
        try (LockCloseable ignored = withLock()) {
            currentTpb = tpb;
        }
        return currentTpb.deepCopy();
    }

    /**
     * Sets the current TPB to a copy of {@code transactionParams}.
     *
     * @param transactionParams
     *         transaction parameters
     */
    public void setTransactionParameters(TransactionParameterBuffer transactionParams) {
        TransactionParameterBuffer copy = transactionParams.deepCopy();
        try (LockCloseable ignored = withLock()) {
            tpb = copy;
        }
    }

    // TODO The nullability is questionable and might need to be defined to @NonNull
    public @Nullable TransactionParameterBuffer getTransactionParameters(int isolation) {
        try (LockCloseable ignored = withLock()) {
            final FBTpbMapper mapping = transactionMapping;
            if (mapping == null) {
                return mcf.getTransactionParameters(isolation);
            }
            return mapping.getMapping(isolation);
        }
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer transactionParams)
            throws SQLException {
        try (LockCloseable ignored = withLock()) {
            FBTpbMapper mapping = transactionMapping;
            if (mapping == null) {
                mapping = transactionMapping = mcf.getTransactionMappingCopy();
            }
            mapping.setMapping(isolation, transactionParams);
            if (getTransactionIsolation() == isolation) {
                // Make sure next transaction uses the new config
                setTransactionIsolation(isolation);
            }
        }
    }

    private void findIscTrHandle(Xid xid, int flags) throws SQLException, XAException {
        // FIXME return old tr handle if it is still valid before proceeding
        clearCurrentTransaction();

        if (flags == XAResource.TMRESUME) {
            FbTransaction trHandle = xidMap.get(xid);
            if (trHandle == null) {
                throw new FBXAException(
                        "You are trying to resume a transaction that is not attached to this XAResource",
                        XAException.XAER_INVAL);
            }

            setCurrentTransaction(trHandle);
            return;
        }

        requireNewXid(xid);

        // new xid for us
        try {
            registerNewTransaction(xid, database.startTransaction(tpb));
        } catch (SQLException e) {
            throw new FBXAException(e.getMessage(), XAException.XAER_RMERR, e);
        }
    }

    /**
     * Registers {@code newTx} as the current transaction of the current GDS helper, and associates it with {@code xid}.
     * <p>
     * If there is no current GDS helper, the transaction will be ended (by commit), and not registered with the xid.
     * </p>
     *
     * @param xid Xid
     * @param newTx new transaction
     * @throws SQLException if there is no current GDS helper
     */
    private void registerNewTransaction(Xid xid, FbTransaction newTx) throws SQLException {
        try {
            setCurrentTransaction(newTx);
            xidMap.put(xid, newTx);
        } catch (SQLException e) {
            // failed to register tx, so commit it to prevent dangling tx
            newTx.commit();
            throw e;
        }
    }

    private void requireNewXid(Xid xid) throws XAException {
        for (Xid knownXid : xidMap.keySet()) {
            boolean sameFormatId = knownXid.getFormatId() == xid.getFormatId();
            boolean sameGtrid = Arrays.equals(knownXid.getGlobalTransactionId(), xid.getGlobalTransactionId());
            boolean sameBqual = Arrays.equals(knownXid.getBranchQualifier(), xid.getBranchQualifier());
            if (sameFormatId && sameGtrid && sameBqual) {
                throw new FBXAException("A transaction with the same XID has already been started",
                        XAException.XAER_DUPID);
            }
        }
    }

    void notify(CELNotifier notifier, XcaConnectionEvent ce) {
        for (XcaConnectionEventListener cel : connectionEventListeners) {
            notifier.notify(cel, ce);
        }
    }

    @FunctionalInterface
    interface CELNotifier {
        void notify(XcaConnectionEventListener cel, XcaConnectionEvent ce);
    }

    static final CELNotifier connectionClosedNotifier = XcaConnectionEventListener::connectionClosed;
    static final CELNotifier connectionErrorOccurredNotifier = XcaConnectionEventListener::connectionErrorOccurred;

    /**
     * Get the transaction isolation level of this connection. The level is one of the static final fields of
     * {@code java.sql.Connection} (i.e. {@code TRANSACTION_READ_COMMITTED}, {@code TRANSACTION_READ_UNCOMMITTED},
     * {@code TRANSACTION_REPEATABLE_READ}, {@code TRANSACTION_SERIALIZABLE}.
     *
     * @return Value representing a transaction isolation level defined in {@link java.sql.Connection}.
     * @throws SQLException
     *         If the transaction level cannot be retrieved
     * @see java.sql.Connection
     * @see #setTransactionIsolation(int)
     */
    @SuppressWarnings("RedundantThrows")
    public int getTransactionIsolation() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            return transactionIsolation;
        }
    }

    /**
     * Set the transaction level for this connection. The level is one of the static final fields of
     * {@code java.sql.Connection} (i.e. {@code TRANSACTION_READ_COMMITTED}, {@code TRANSACTION_READ_UNCOMMITTED},
     * {@code TRANSACTION_REPEATABLE_READ}, {@code TRANSACTION_SERIALIZABLE}.
     *
     * @param isolation
     *         Value representing a transaction isolation level defined in {@link java.sql.Connection}.
     * @throws SQLException
     *         If the transaction level cannot be retrieved
     * @see java.sql.Connection
     * @see #getTransactionIsolation()
     */
    public void setTransactionIsolation(int isolation) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            transactionIsolation = isolation;
            final FBTpbMapper mapping = transactionMapping;
            tpb = mapping == null ? mcf.getTpb(isolation) : mapping.getMapping(isolation);
        }
    }

    /**
     * Get the managed connection factory that created this managed connection.
     *
     * @return instance of {@link FBManagedConnectionFactory}.
     */
    public FBManagedConnectionFactory getManagedConnectionFactory() {
        return mcf;
    }

    /**
     * Set the current TPB to read-only.
     *
     * @param readOnly
     *         if {@code true}, the connection will be set read-only, otherwise it will be writable
     * @since 6
     */
    public void setTpbReadOnly(boolean readOnly) {
        tpb.setReadOnly(readOnly);
    }

    /**
     * Retrieve whether the current TPB is read-only.
     *
     * @return {@code true} if this connection is readonly, {@code false} otherwise
     * @since 6
     */
    public boolean isTpbReadOnly() {
        return tpb.isReadOnly();
    }

    /**
     * Sets the Firebird auto-commit flag on the current TPB. This change is transient and will be reset when the
     * transaction isolation level is set again or the TPB is otherwise replaced.
     * <p>
     * This shouldn't be confused with the normal JDBC auto-commit behavior. Effectively, setting this to {@code true}
     * will result in Firebird using commit retain after each executed statement.
     * </p>
     *
     * @param autoCommit
     *         {@code true} add the auto-commit flag, otherwise remove it
     * @see #isTpbAutoCommit()
     * @since 6
     */
    public void setTpbAutoCommit(boolean autoCommit) {
        tpb.setAutoCommit(autoCommit);
    }

    /**
     * Returns if the current TPB has the auto-commit flag set.
     *
     * @return {@code true} if the current TPB has the auto-commit flag, {@code false} otherwise
     * @see #setTpbAutoCommit(boolean)
     * @since 6
     */
    @SuppressWarnings("unused")
    public boolean isTpbAutoCommit() {
        return tpb.isAutoCommit();
    }

    @SuppressWarnings("java:S135")
    private void notifyWarning(SQLWarning warning) {
        final FBConnection connection = connectionHandle;
        if (connection == null) {
            while (true) {
                if (!unnotifiedWarningsUpdater.compareAndSet(this, null, warning)) {
                    final SQLWarning warnings = unnotifiedWarnings;
                    if (warnings == null) {
                        continue;
                    }
                    warnings.setNextWarning(warning);
                }
                break;
            }
        } else {
            final SQLWarning warnings = unnotifiedWarningsUpdater.getAndSet(this, null);
            if (warnings != null) {
                warnings.setNextWarning(warning);
                warning = warnings;
            }
            connection.addWarning(warning);
        }
    }

    private static @Nullable String getDefaultConnectionEncoding() {
        try {
            String defaultConnectionEncoding = JaybirdSystemProperties.getDefaultConnectionEncoding();
            if (defaultConnectionEncoding != null) {
                return defaultConnectionEncoding;
            } else if (JaybirdSystemProperties.isRequireConnectionEncoding()) {
                return null;
            }
        } catch (Exception e) {
            log.log(ERROR, "Exception obtaining default connection encoding", e);
        }
        return "NONE";
    }

    /**
     * DatabaseListener implementation for use by this managed connection.
     */
    private final class MCDatabaseListener implements DatabaseListener {
        @Override
        public void warningReceived(FbDatabase database, SQLWarning warning) {
            if (database != FBManagedConnection.this.database) {
                database.removeDatabaseListener(this);
                return;
            }
            notifyWarning(warning);
        }

        @Override
        public void detached(FbDatabase database) {
            if (database != FBManagedConnection.this.database) return;
            FBManagedConnection.this.gdsHelper = null;
            forceDisassociateConnections();
        }
    }

    /**
     * XAResource implementation that delegates to the managed connection itself.
     */
    private final class FbMcXaResource implements XAResource {

        private FBManagedConnection getMc() {
            return FBManagedConnection.this;
        }

        @Override
        public void start(Xid xid, int flags) throws XAException {
            FBManagedConnection.this.start(xid, flags);
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return FBManagedConnection.this.prepare(xid);
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            FBManagedConnection.this.commit(xid, onePhase);
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            FBManagedConnection.this.rollback(xid);
        }

        @Override
        public void end(Xid xid, int flags) throws XAException {
            FBManagedConnection.this.end(xid, flags);
        }

        @Override
        public void forget(Xid xid) throws XAException {
            FBManagedConnection.this.forget(xid);
        }

        @Override
        public Xid[] recover(int flag) throws XAException {
            return FBManagedConnection.this.recover(flag);
        }

        /**
         * Retrieve whether this {@code XAResource} uses the same ResourceManager as {@code res}. This method relies on
         * {@code res} being a Firebird implementation of {@code XAResource}.
         *
         * @param res
         *         The other {@code XAResource} to compare to
         * @return {@code true} if {@code res} uses the same ResourceManager, {@code false} otherwise
         */
        @Override
        public boolean isSameRM(XAResource res) {
            return res == this
                    || res instanceof FbMcXaResource fbMcXaResource && database == fbMcXaResource.getMc().database;
        }

        @Override
        public int getTransactionTimeout() {
            return FBManagedConnection.this.getTransactionTimeout();
        }

        @Override
        public boolean setTransactionTimeout(int seconds) {
            return FBManagedConnection.this.setTransactionTimeout(seconds);
        }
    }

    private interface XidQueries {
        String forgetFindQuery();
        String forgetDelete();
        String recoveryQuery();
        String recoveryQueryParameterized();

        static XidQueries forVersion(GDSServerVersion version) {
            if (version.isEqualOrAbove(6)) {
                return XidQueriesFB60.INSTANCE;
            } else if (version.isEqualOrAbove(3)) {
                return XidQueriesFB30.INSTANCE;
            } else if (version.isEqualOrAbove(2, 5)) {
                return XidQueriesFB25.INSTANCE;
            }
            return XidQueriesFB21.INSTANCE;
        }
    }

    /**
     * Relatively efficient XID queries that work with Firebird 6.0 and higher.
     */
    private static final class XidQueriesFB60 implements XidQueries {

        static final XidQueriesFB60 INSTANCE = new XidQueriesFB60();
        // We're no longer casting RDB$TRANSACTION_DESCRIPTION, as it will benefit from inline blobs
        private static final String FIND_TRANSACTION_FRAGMENT =
                "select RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION from SYSTEM.RDB$TRANSACTIONS\n";

        @Override
        public String forgetFindQuery() {
            return FIND_TRANSACTION_FRAGMENT + """
                    where RDB$TRANSACTION_STATE in (2, 3)"
                    and RDB$TRANSACTION_DESCRIPTION starting with x'0105'""";
        }

        @Override
        public String forgetDelete() {
            return "delete from SYSTEM.RDB$TRANSACTIONS where RDB$TRANSACTION_ID = ";
        }

        @Override
        public String recoveryQuery() {
            return FIND_TRANSACTION_FRAGMENT + "where RDB$TRANSACTION_DESCRIPTION starting with x'0105'";
        }

        @Override
        public String recoveryQueryParameterized() {
            return FIND_TRANSACTION_FRAGMENT
                    + "where RDB$TRANSACTION_DESCRIPTION = cast(? AS varchar(32764) character set octets)";
        }
    }

    /**
     * Relatively efficient XID queries that work with Firebird 3.0 and higher.
     */
    private static final class XidQueriesFB30 implements XidQueries {

        static final XidQueriesFB30 INSTANCE = new XidQueriesFB30();
        private static final String FIND_TRANSACTION_FRAGMENT = """
                select RDB$TRANSACTION_ID, cast(RDB$TRANSACTION_DESCRIPTION as varchar(32764) character set octets)
                from RDB$TRANSACTIONS
                """;

        @Override
        public String forgetFindQuery() {
            return FIND_TRANSACTION_FRAGMENT + """
                    where RDB$TRANSACTION_STATE in (2, 3)"
                    and RDB$TRANSACTION_DESCRIPTION starting with x'0105'""";
        }

        @Override
        public String forgetDelete() {
            return "delete from RDB$TRANSACTIONS where RDB$TRANSACTION_ID = ";
        }

        @Override
        public String recoveryQuery() {
            return FIND_TRANSACTION_FRAGMENT + "where RDB$TRANSACTION_DESCRIPTION starting with x'0105'";
        }

        @Override
        public String recoveryQueryParameterized() {
            return FIND_TRANSACTION_FRAGMENT
                    + "where RDB$TRANSACTION_DESCRIPTION = cast(? AS varchar(32764) character set octets)";
        }
    }

    /**
     * Less efficient XID queries that work with Firebird 2.5.
     */
    private static final class XidQueriesFB25 implements XidQueries {

        static final XidQueriesFB25 INSTANCE = new XidQueriesFB25();
        private static final String FIND_TRANSACTION_FRAGMENT =
                "select RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION from RDB$TRANSACTIONS\n";

        @Override
        public String forgetFindQuery() {
            return FIND_TRANSACTION_FRAGMENT + """
                    where RDB$TRANSACTION_STATE in (2, 3)
                    and RDB$TRANSACTION_DESCRIPTION starting with x'0105'""";
        }

        @Override
        public String forgetDelete() {
            return "delete from RDB$TRANSACTIONS where RDB$TRANSACTION_ID = ";
        }

        @Override
        public String recoveryQuery() {
            return FIND_TRANSACTION_FRAGMENT + "where RDB$TRANSACTION_DESCRIPTION starting with x'0105'";
        }

        @Override
        public String recoveryQueryParameterized() {
            return FIND_TRANSACTION_FRAGMENT
                    + "where RDB$TRANSACTION_DESCRIPTION = cast(? AS varchar(32764) character set octets)";
        }
    }

    /**
     * Even less efficient queries that work with Firebird 2.1 and older (unsupported versions).
     */
    private static final class XidQueriesFB21 implements XidQueries {

        static final XidQueriesFB21 INSTANCE = new XidQueriesFB21();
        private static final String FIND_TRANSACTION_FRAGMENT =
                "select RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION from RDB$TRANSACTIONS\n";

        @Override
        public String forgetFindQuery() {
            return FIND_TRANSACTION_FRAGMENT + "where RDB$TRANSACTION_STATE in (2, 3)";
        }

        @Override
        public String forgetDelete() {
            return "delete from RDB$TRANSACTIONS where RDB$TRANSACTION_ID = ";
        }

        @Override
        public String recoveryQuery() {
            return FIND_TRANSACTION_FRAGMENT;
        }

        @Override
        public String recoveryQueryParameterized() {
            return FIND_TRANSACTION_FRAGMENT
                    + "where RDB$TRANSACTION_DESCRIPTION = cast(? AS varchar(32764) character set octets)";
        }
    }
}
