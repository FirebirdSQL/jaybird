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
package org.firebirdsql.jca;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.LocalGDSFactoryPlugin;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DefaultDatabaseListener;
import org.firebirdsql.gds.ng.listeners.DefaultStatementListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

/**
 * The class <code>FBManagedConnection</code> implements both the
 * ManagedConnection and XAResource interfaces.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBManagedConnection implements ManagedConnection, XAResource, ExceptionListener {

    public static final String WARNING_NO_CHARSET = "WARNING: No connection characterset specified (property lc_ctype, encoding, charSet or localEncoding), defaulting to characterset NONE";

    private static final Logger log = LoggerFactory.getLogger(FBManagedConnection.class);

    private final FBManagedConnectionFactory mcf;

    private final List<ConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();
    // TODO Review synchronization of connectionHandles (especially in blocks like in disassociateConnections, setConnectionSharing etc)
    private final List<FBConnection> connectionHandles = Collections.synchronizedList(new ArrayList<FBConnection>());
    // This is a bit of hack to be able to get attach warnings into the FBConnection that is created later.
    private SQLWarning unnotifiedWarnings;

    private int timeout = 0;

    private final Map<Xid, FbTransaction> xidMap = new ConcurrentHashMap<>();
    
    private GDSHelper gdsHelper;
    private final FbDatabase database;

    private final FBConnectionRequestInfo cri;
    private FBTpb tpb;
    private int transactionIsolation;

    private volatile boolean managedEnvironment = true;
    private volatile boolean connectionSharing = true;
    private final Set<Xid> preparedXid = Collections.synchronizedSet(new HashSet<Xid>());
    private volatile boolean inDistributedTransaction = false;

    FBManagedConnection(Subject subject, ConnectionRequestInfo cri, FBManagedConnectionFactory mcf)
            throws ResourceException {
        this.mcf = mcf;
        this.cri = getCombinedConnectionRequestInfo(subject, cri);
        this.tpb = mcf.getDefaultTpb();
        this.transactionIsolation = mcf.getDefaultTransactionIsolation();
        
        //TODO: XIDs in limbo should be loaded so that XAER_DUPID can be thrown appropriately
        
        try {
            DatabaseParameterBuffer dpb = this.cri.getDpb();

            // TODO Add at lower level in database?
            if (dpb.getArgumentAsString(DatabaseParameterBuffer.LC_CTYPE) == null) {
                if (log != null) {
                    log.warn(WARNING_NO_CHARSET);
                }
                notifyWarning(new SQLWarning(WARNING_NO_CHARSET));
            }
            
            if (!dpb.hasArgument(DatabaseParameterBuffer.CONNECT_TIMEOUT) && DriverManager.getLoginTimeout() > 0) {
                dpb.addArgument(DatabaseParameterBuffer.CONNECT_TIMEOUT, DriverManager.getLoginTimeout());
            }

            final FbConnectionProperties connectionProperties = new FbConnectionProperties();
            connectionProperties.fromDpb(dpb);
            // TODO Move this logic to the GDSType or database factory?
            final String gdsTypeName = mcf.getGDSType().toString();
            if (!(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME.equals(gdsTypeName)
                    || LocalGDSFactoryPlugin.LOCAL_TYPE_NAME.equals(gdsTypeName))) {
                final DbAttachInfo dbAttachInfo = new DbAttachInfo(mcf.getDatabase());
                connectionProperties.setServerName(dbAttachInfo.getServer());
                connectionProperties.setPortNumber(dbAttachInfo.getPort());
                connectionProperties.setDatabaseName(dbAttachInfo.getFileName());
            } else {
                connectionProperties.setDatabaseName(mcf.getDatabase());
            }

            database = mcf.getDatabaseFactory().connect(connectionProperties);
            database.addDatabaseListener(new MCDatabaseListener());
            database.addExceptionListener(this);
            database.attach();

            gdsHelper = new GDSHelper(database);
        } catch(SQLException ex) {
            throw new FBResourceException(ex);
        }
    }

    @Override
    public void errorOccurred(Object source, SQLException ex) {
        if (log != null) log.trace(ex.getMessage());

        if (!FatalGDSErrorHelper.isFatal(ex))
            return;

        ConnectionEvent event = new ConnectionEvent(FBManagedConnection.this, ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                ex);

        FBManagedConnection.this.notify(connectionErrorOccurredNotifier, event);
    }
    
    private FBConnectionRequestInfo getCombinedConnectionRequestInfo(Subject subject, ConnectionRequestInfo cri)
            throws ResourceException {
        if (cri == null) {
            cri = mcf.getDefaultConnectionRequestInfo();
        }
        try {
            FBConnectionRequestInfo fbcri = (FBConnectionRequestInfo) cri;
            if (subject != null) {
                // see connector spec, section 8.2.6, contract for
                // ManagedConnectionFactory, option A.
                for (Object cred : subject.getPrivateCredentials()) {
                    if (cred instanceof PasswordCredential
                            && mcf.equals(((PasswordCredential) cred)
                            .getManagedConnectionFactory())) {
                        PasswordCredential pcred = (PasswordCredential) cred;
                        String user = pcred.getUserName();
                        String password = new String(pcred.getPassword());
                        fbcri.setPassword(password);
                        fbcri.setUserName(user);
                        break;
                    }
                } 
            } 
    
            return fbcri;
        } catch (ClassCastException cce) {
            throw new FBResourceException("Incorrect ConnectionRequestInfo class supplied");
        }
    }
    
    /**
     * Get instance of {@link GDSHelper} connected with this managed connection.
     * 
     * @return instance of {@link GDSHelper}.
     * @throws SQLException If this connection has no GDSHelper
     */
    public GDSHelper getGDSHelper() throws SQLException {
        if (gdsHelper == null)
            // TODO Right error code?
            throw new FbExceptionBuilder().exception(ISCConstants.isc_req_no_trans).toSQLException();

        return gdsHelper;
    }
    
    public String getDatabase() {
        return mcf.getDatabase();
    }

    public boolean isManagedEnvironment() {
        return managedEnvironment;
    }
    
    public boolean inTransaction() {
        return gdsHelper != null && gdsHelper.inTransaction();
    }
    
    public void setManagedEnvironment(boolean managedEnvironment) throws ResourceException{
        this.managedEnvironment = managedEnvironment;
        
        // if connection sharing is not enabled, notify currently associated
        // connection handle about the state change.
        if (!connectionSharing) {
            if (connectionHandles.size() > 1)
                throw new javax.resource.spi.IllegalStateException(
                    "Multiple connections associated with this managed " +
                    "connection in non-sharing mode.");
            
            // there will be at most one connection.
            for (FBConnection connection : connectionHandles) {
                try {
                    connection.setManagedEnvironment(managedEnvironment);
                } catch(SQLException ex) {
                    throw new FBResourceException(ex);
                }
            }
        }
    }
    
    /**
     * Check if connection sharing is enabled. When connection sharing is 
     * enabled, multiple connection handles ({@link FBConnection} instances)
     * can access this managed connection in thread-safe manner (they synchronize
     * on this instance). This feature can be enabled only in JCA environment,
     * any other environment must not use connection sharing.
     * 
     * @return <code>true</code> if connection sharing is enabled.
     */
    public boolean isConnectionSharing() {
        return connectionSharing;
    }
    
    /**
     * Enable or disable connection sharing. See {@link #isConnectionSharing()}
     * method for details.
     * 
     * @param connectionSharing <code>true</code> if connection sharing must be
     * enabled.
     * @throws ResourceException If connection sharing state cannot be changed
     */
    public void setConnectionSharing(boolean connectionSharing) throws ResourceException {
        if (!connectionHandles.isEmpty())
            throw new javax.resource.spi.IllegalStateException(
                "Cannot change connection sharing with active connection handles.");
        
        this.connectionSharing = connectionSharing;
    }
    /**
     * Returns a <code>javax.resource.spi.LocalTransaction</code> instance.
     * The LocalTransaction interface is used by the container to manage local
     * transactions for a RM instance.
     * 
     * @return LocalTransaction instance
     * @throws ResourceException
     *             generic exception if operation fails
     * @throws javax.resource.NotSupportedException
     *             if the operation is not supported
     * @throws ResourceAdapterInternalException
     *             resource adapter internal error condition
     */
    public LocalTransaction getLocalTransaction() {
        return new FBLocalTransaction(this, null);
    }

    /**
     * Gets the metadata information for this connection's underlying EIS
     * resource manager instance. The ManagedConnectionMetaData interface
     * provides information about the underlying EIS instance associated with
     * the ManagedConenction instance.
     * 
     * @return ManagedConnectionMetaData instance
     * @throws ResourceException
     *             generic exception if operation fails
     * @throws javax.resource.NotSupportedException
     *             if the operation is not supported
     */
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new FBManagedConnectionMetaData(this);
    }

    /**
     * Sets the log writer for this ManagedConnection instance.
     * <P>
     * The log writer is a character output stream to which all logging and
     * tracing messages for this ManagedConnection instance will be printed.
     * Application Server manages the association of output stream with the
     * ManagedConnection instance based on the connection pooling requirements.
     * <P>
     * When a ManagedConnection object is initially created, the default log
     * writer associated with this instance is obtained from the
     * <code>ManagedConnectionFactory</code>. An application server can set a
     * log writer specific to this ManagedConnection to log/trace this instance
     * using setLogWriter method.
     * 
     * @param out
     *            Character Output stream to be associated
     * @throws ResourceException
     *             generic exception if operation fails
     * @throws ResourceAdapterInternalException
     *             resource adapter related error condition
     */
    public void setLogWriter(PrintWriter out) {
        // ignore, we are using log4j.
    }

    /**
     * Gets the log writer for this ManagedConnection instance.
     * <P>
     * The log writer is a character output stream to which all logging and
     * tracing messages for this ManagedConnection instance will be printed.
     * <code>ConnectionManager</code> manages the association of output stream
     * with the <code>ManagedConnection</code> instance based on the
     * connection pooling requirements.
     * <P>
     * The Log writer associated with a <code>ManagedConnection</code>
     * instance can be one set as default from the ManagedConnectionFactory
     * (that created this connection) or one set specifically for this instance
     * by the application server.
     * 
     * @return Character ourput stream associated with this
     *         <code>ManagedConnection</code>
     * @throws ResourceException
     *             generic exception if operation fails
     */
    public PrintWriter getLogWriter() {
        return null;// we are using log4j.
    }

    /**
     * Add an <code>ConnectionEventListener</code> listener. The listener will
     * be notified when a <code>ConnectionEvent</code> occurs.
     * 
     * @param listener
     *            The <code>ConnectionEventListener</code> to be added
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    /**
     * Remove a <code>ConnectionEventListner</code> from the listing of
     * listeners that will be notified for a <code>ConnectionEvent</code>.
     * 
     * @param listener
     *            The <code>ConnectionEventListener</code> to be removed
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    /**
     * Used by the container to change the association of an application-level
     * connection handle with a ManagedConneciton instance. The container should
     * find the right ManagedConnection instance and call the
     * associateConnection method.
     * <P>
     * The resource adapter is required to implement the associateConnection
     * method. The method implementation for a ManagedConnection should
     * dissociate the connection handle (passed as a parameter) from its
     * currently associated ManagedConnection and associate the new connection
     * handle with itself.
     * 
     * @param connection
     *            Application-level connection handle
     * @throws ResourceException
     *             Failed to associate the connection handle with this
     *             ManagedConnection instance
     * @throws javax.resource.spi.IllegalStateException
     *             Illegal state for invoking this method
     * @throws ResourceAdapterInternalException
     *             Resource adapter internal error condition
     */
    public void associateConnection(Object connection) throws ResourceException {
        if (!connectionSharing)
            disassociateConnections();
        
        try {
            final FBConnection abstractConnection = (FBConnection) connection;
            abstractConnection.setManagedConnection(this);
            connectionHandles.add(abstractConnection);
        } catch (ClassCastException cce) {
            throw new FBResourceException("invalid connection supplied to associateConnection.", cce);
        }
    }

    /**
     * Application server calls this method to force any cleanup on the
     * <code>ManagedConnection</code> instance.
     * <P>
     * The method {@link ManagedConnection#cleanup}initiates a cleanup of the
     * any client-specific state as maintained by a ManagedConnection instance.
     * The cleanup should invalidate all connection handles that had been
     * created using this <code>ManagedConnection</code> instance. Any attempt
     * by an application component to use the connection handle after cleanup of
     * the underlying <code>ManagedConnection</code> should result in an
     * exception.
     * <P>
     * The cleanup of ManagedConnection is always driven by an application
     * server. An application server should not invoke
     * {@link ManagedConnection#cleanup}when there is an uncompleted
     * transaction (associated with a ManagedConnection instance) in progress.
     * <P>
     * The invocation of {@link ManagedConnection#cleanup}method on an already
     * cleaned-up connection should not throw an exception.
     * 
     * The cleanup of <code>ManagedConnection</code> instance resets its
     * client specific state and prepares the connection to be put back in to a
     * connection pool. The cleanup method should not cause resource adapter to
     * close the physical pipe and reclaim system resources associated with the
     * physical connection.
     * 
     * @throws ResourceException
     *             generic exception if operation fails
     * @throws ResourceAdapterInternalException
     *             resource adapter internal error condition
     * @throws javax.resource.spi.IllegalStateException
     *             Illegal state for calling connection cleanup. Example - if a
     *             local transaction is in progress that doesn't allow
     *             connection cleanup
     */
    public void cleanup() throws ResourceException {
        disassociateConnections();

        try {
            getGDSHelper().setCurrentTransaction(null);
        } catch (SQLException e) {
            throw new FBResourceException(e);
        }

        // reset the TPB from the previous transaction.
        this.tpb = mcf.getDefaultTpb();
        this.transactionIsolation = mcf.getDefaultTransactionIsolation();
    }

    /**
     * Disassociate connections from current managed connection.
     */
    private void disassociateConnections() throws ResourceException {
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        
        // Iterate over copy of list as connection.close() will remove connection
        List<FBConnection> connectionHandleCopy = new ArrayList<>(connectionHandles);
        for (FBConnection connection : connectionHandleCopy) {
            try {
                connection.close();
            } catch(SQLException sqlex) {
                chain.append(sqlex);
            }
        }
        
        if (chain.hasException())
            throw new FBResourceException(chain.getException());
    }

    /**
     * Creates a new connection handle for the underlying physical connection
     * represented by the <code>ManagedConnection</code> instance. This
     * connection handle is used by the application code to refer to the
     * underlying physical connection. A connection handle is tied to its
     * <code>ManagedConnection</code> instance in a resource adapter
     * implementation specific way.
     * <P>
     * 
     * The <code>ManagedConnection</code> uses the Subject and additional
     * <code>ConnectionRequestInfo</code> (which is specific to resource
     * adapter and opaque to application server) to set the state of the
     * physical connection.
     * 
     * @param subject
     *            security context as JAAS subject
     * @param cri
     *            ConnectionRequestInfo instance
     * @return generic <code>Object</code> instance representing the
     *         connection handle. For CCI, the connection handle created by a
     *         <code>ManagedConnection</code> instance is of the type
     *         <code>javax.resource.cci.Connection</code>.
     * @throws ResourceException
     *             generic exception if operation fails
     * @throws ResourceAdapterInternalException
     *             resource adapter internal error condition
     * @throws javax.resource.spi.SecurityException
     *             security related error condition
     * @throws CommException
     *             failed communication with EIS instance
     * @throws EISSystemException
     *             internal error condition in EIS instance - used if EIS
     *             instance is involved in setting state of
     *             <code>ManagedConnection</code>
     */
    public Object getConnection(Subject subject, ConnectionRequestInfo cri)
            throws ResourceException {
        
        if (!matches(subject, cri))
            throw new FBResourceException("Incompatible subject or ConnectionRequestInfo in getConnection!");

        if (!connectionSharing)
            disassociateConnections();
        
        FBConnection c = mcf.newConnection(this);
        try {
            if (unnotifiedWarnings != null) {
                c.addWarning(unnotifiedWarnings);
                unnotifiedWarnings = null;
            }
            c.setManagedEnvironment(isManagedEnvironment());
            connectionHandles.add(c);
            return c;
        } catch(SQLException ex) {
            throw new FBResourceException(ex);
        }
    }

    /**
     * Destroys the physical connection to the underlying resource manager. To
     * manage the size of the connection pool, an application server can
     * explictly call {@link ManagedConnection#destroy}to destroy a physical
     * connection. A resource adapter should destroy all allocated system
     * resources for this <code>ManagedConnection</code> instance when the
     * method destroy is called.
     * 
     * @throws ResourceException
     *             generic exception if operation failed
     * @throws javax.resource.spi.IllegalStateException
     *             illegal state for destroying connection
     */
    public void destroy() throws ResourceException {
        if (gdsHelper == null)
            return;
        
        if (inTransaction())
            throw new javax.resource.spi.IllegalStateException(
                "Can't destroy managed connection  with active transaction");
        
        try {
            gdsHelper.detachDatabase();
        } catch (SQLException ge) {
            throw new FBResourceException("Can't detach from db.", ge);
        } finally {
            gdsHelper = null;
        }
    }

    /**
     * Return an XA resource to the caller.
     * <P>
     * In both <code>javax.sql.XAConnection</code> and
     * <code>javax.resource.spi.MangagedConnection</code>.
     * 
     * @return the XAResource
     */
    public XAResource getXAResource() {
        if (log != null)
            log.debug("XAResource requested from FBManagedConnection");
        return this;
    }

    // --------------------------------------------------------------
    // XAResource implementation
    // --------------------------------------------------------------

    // TODO validate correctness of state set
    private static final Set<TransactionState> XID_ACTIVE_STATE = Collections.unmodifiableSet(EnumSet.of(TransactionState.ACTIVE, TransactionState.PREPARED, TransactionState.PREPARING));

    boolean isXidActive(Xid xid) {
        FbTransaction transaction = xidMap.get(xid);
        return transaction != null && XID_ACTIVE_STATE.contains(transaction.getState());
    }

    /**
     * Commits a transaction.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end never called), the
     *             transaction ID is wrong, the connection was set to
     *             Auto-Commit, or the commit on the underlying connection
     *             fails. The error code differs depending on the exact
     *             situation.
     */
    public void commit(Xid id, boolean onePhase) throws XAException {
        try {
            mcf.notifyCommit(this, id, onePhase);
        } catch (GDSException ge) {
            throw new XAException(ge.getXAErrorCode());
        }
    }

    /**
     * The <code>internalCommit</code> method performs the requested commit
     * and may throw a GDSException to be interpreted by the caller.
     * 
     * @param xid
     *            a <code>Xid</code> value
     * @param onePhase
     *            a <code>boolean</code> value
     * @exception XAException
     *                if an error occurs
     */
    void internalCommit(Xid xid, boolean onePhase) throws XAException {
        if (log != null) log.trace("Commit called: " + xid);
        FbTransaction committingTr = xidMap.get(xid);
        
        // check that prepare has NOT been called when onePhase = true
        if (onePhase && isPrepared(xid))
            throw new FBXAException("Cannot commit one-phase when transaction has been prepared", XAException.XAER_PROTO);
            
        // check that prepare has been called when onePhase = false
        if (!onePhase && !isPrepared(xid))
            throw new FBXAException("Cannot commit two-phase when transaction has not been prepared", XAException.XAER_PROTO);
        
        if (committingTr == null)
            throw new FBXAException("Commit called with unknown transaction", XAException.XAER_NOTA);

        try {
            if (committingTr == getGDSHelper().getCurrentTransaction())
                throw new FBXAException("Commit called with non-ended xid", XAException.XAER_PROTO);

            // TODO Equivalent or handled by listeners?
            //committingTr.forgetResultSets();

            getGDSHelper().commitTransaction(committingTr);
        } catch (SQLException ge) {
            if (gdsHelper != null) {
                try {
                    gdsHelper.rollbackTransaction(committingTr);
                } catch (SQLException ge2) {
                    if (log != null) log.debug("Exception rolling back failed tx: ", ge2);
                }
            } else if (log != null) {
                log.warn("Unable to rollback failed tx, connection closed or lost");
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
     *             Occurs when the state was not correct (end called twice), or
     *             the transaction ID is wrong.
     */
    public void end(Xid id, int flags) throws XAException {
        if (flags != XAResource.TMSUCCESS && flags != XAResource.TMFAIL && flags != XAResource.TMSUSPEND)
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMSUCCESS, TMFAIL, TMSUSPEND", XAException.XAER_PROTO);
        try {
            internalEnd(id, flags);
        } catch (SQLException e) {
            throw new FBXAException(XAException.XAER_RMERR, e);
        }
        mcf.notifyEnd(this, id);
        inDistributedTransaction = false;

        try {
            // This will reset the managed environment of the associated connections and set the transaction coordinator to local
            // TODO This is a bit of a hack; need to find a better way; this doesn't work with connectionSharing = true
            setManagedEnvironment(isManagedEnvironment());
        } catch (ResourceException ex) {
            throw new FBXAException("Reset of managed state failed", XAException.XAER_RMERR, ex);
        }
    }

    /**
     * The <code>internalEnd</code> method ends the xid as requested if
     * appropriate and throws a GDSException including the appropriate XA error
     * code and a message if not. The caller can decode the exception as
     * necessary.
     * 
     * @param xid
     *            a <code>Xid</code> value
     * @param flags
     *            an <code>int</code> value
     * @exception XAException
     *                if an error occurs
     */
    void internalEnd(Xid xid, int flags) throws XAException, SQLException {
        if (log != null) log.debug("End called: " + xid);
        FbTransaction endingTr = xidMap.get(xid);
        
        if (endingTr == null)
            throw new FBXAException("Unrecognized transaction", XAException.XAER_NOTA);

        if (flags == XAResource.TMFAIL) {
            try {
                endingTr.rollback();
                getGDSHelper().setCurrentTransaction(null);
            } catch (SQLException ex) {
                throw new FBXAException("can't rollback transaction", XAException.XAER_RMFAIL, ex);
            }
        }
        else if (flags == XAResource.TMSUCCESS) {
            if (gdsHelper != null && endingTr == gdsHelper.getCurrentTransaction())
                gdsHelper.setCurrentTransaction(null);
            else
                throw new FBXAException("You are trying to end a transaction that is not the current transaction",
                        XAException.XAER_INVAL);
        }
        else if (flags == XAResource.TMSUSPEND) {
            if (gdsHelper != null && endingTr == gdsHelper.getCurrentTransaction())
                gdsHelper.setCurrentTransaction(null);
            else 
                throw new FBXAException("You are trying to suspend a transaction that is not the current transaction",
                        XAException.XAER_INVAL);
            
        }
    }
    
    private final static String FORGET_FIND_QUERY = "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
                                                  + "FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE IN (2, 3)";
    private final static String FORGET_DELETE_QUERY = "DELETE FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_ID = ";

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure). It is assumed this will be
     * called after a failed commit or rollback.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end never called), or
     *             the transaction ID is wrong.
     */
    public void forget(Xid id) throws XAException {
        long inLimboId = -1;

        try {
            // find XID
            // TODO: Is there a reason why this piece of code can't use the JDBC Statement class?
            FbTransaction trHandle2 = database.startTransaction(tpb.getTransactionParameterBuffer());
            FbStatement stmtHandle2 = database.createStatement(trHandle2);

            GDSHelper gdsHelper2 = new GDSHelper(database);
            gdsHelper2.setCurrentTransaction(trHandle2);

            stmtHandle2.prepare(FORGET_FIND_QUERY);

            DataProvider dataProvider0 = new DataProvider(0);
            stmtHandle2.addStatementListener(dataProvider0);
            DataProvider dataProvider1 = new DataProvider(1);
            stmtHandle2.addStatementListener(dataProvider1);

            stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);
            stmtHandle2.fetchRows(10);

            FBField field0 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(0), dataProvider0, gdsHelper2, false);
            FBField field1 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(1), dataProvider1, gdsHelper2, false);

            int row = 0;
            while(row < dataProvider0.getRowCount()) {
                dataProvider0.setRow(row);
                dataProvider1.setRow(row);
                
                long inLimboTxId = field0.getLong();
                byte[] inLimboMessage = field1.getBytes();
            
                try {
                    FBXid xid = new FBXid(new ByteArrayInputStream(inLimboMessage), inLimboTxId);
                    
                    boolean gtridEquals = Arrays.equals(xid.getGlobalTransactionId(), id.getGlobalTransactionId());
                    boolean bqualEquals = Arrays.equals(xid.getBranchQualifier(), id.getBranchQualifier());
                    
                    if (gtridEquals && bqualEquals) {
                        inLimboId = inLimboTxId;
                        break;
                    }
                } catch(FBIncorrectXidException ex) {
                    if (log != null)
                        log.warn("incorrect XID format in RDB$TRANSACTIONS where RDB$TRANSACTION_ID=" + inLimboTxId, ex);
                }

                row++;
            }

            stmtHandle2.close();
            trHandle2.commit();
        } catch (SQLException | ResourceException ex) {
            if (log != null)
                log.debug("can't perform query to fetch xids", ex);
            throw new FBXAException(XAException.XAER_RMFAIL, ex);
        }

        if (inLimboId == -1)
            throw new FBXAException("XID not found", XAException.XAER_NOTA); // TODO: is XAER_NOTA the proper error code ?
            
        try {    
            // delete XID

            FbTransaction trHandle2 = database.startTransaction(tpb.getTransactionParameterBuffer());

            FbStatement stmtHandle2 = database.createStatement(trHandle2);

            GDSHelper gdsHelper2 = new GDSHelper(database);
            gdsHelper2.setCurrentTransaction(trHandle2);

            stmtHandle2.prepare(FORGET_DELETE_QUERY + inLimboId);
            stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);

            stmtHandle2.close();
            trHandle2.commit();
        } catch (SQLException ex) {
            throw new FBXAException("can't perform query to fetch xids", XAException.XAER_RMFAIL, ex);
        }
    }

    /**
     * Gets the transaction timeout.
     */
    public int getTransactionTimeout() throws javax.transaction.xa.XAException {
        return timeout;
    }

    /**
     * Retrieve whether this <code>FBManagedConnection</code> uses the same
     * ResourceManager as <code>res</code>. This method relies on
     * <code>res</code> being a Firebird implementation of
     * <code>XAResource</code>.
     * 
     * @param res
     *            The other <code>XAResource</code> to compare to
     * @return <code>true</code> if <code>res</code> uses the same
     *         ResourceManager, <code>false</code> otherwise
     */
    public boolean isSameRM(XAResource res) throws XAException {
        return res instanceof FBManagedConnection
                && database == ((FBManagedConnection) res).database;
    }

    /**
     * Prepares a transaction to commit.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end never called), the
     *             transaction ID is wrong, or the connection was set to
     *             Auto-Commit.
     */
    public int prepare(Xid xid) throws javax.transaction.xa.XAException {
        try {
            return mcf.notifyPrepare(this, xid);
        } catch (GDSException ge) {
            throw new FBXAException(XAException.XAER_RMERR, ge);
        }
    }

    int internalPrepare(Xid xid) throws FBXAException {
        if (log != null) log.trace("prepare called: " + xid);
        FbTransaction committingTr = xidMap.get(xid);
        if (committingTr == null)
            throw new FBXAException("Prepare called with unknown transaction", XAException.XAER_NOTA);
        try {
            if (committingTr == getGDSHelper().getCurrentTransaction())
                throw new FBXAException("Prepare called with non-ended xid", XAException.XAER_PROTO);

            FBXid fbxid;
            if (xid instanceof FBXid) {
                fbxid = (FBXid) xid;
            } else {
                fbxid = new FBXid(xid);
            }
            byte[] message = fbxid.toBytes();

            getGDSHelper().prepareTransaction(committingTr, message);
        } catch (SQLException ge) {
            try {
                if (gdsHelper != null) {
                    gdsHelper.rollbackTransaction(committingTr);
                } else if (log != null) {
                    log.warn("Unable to rollback failed tx, connection closed or lost");
                }
            } catch (SQLException ge2) {
                if (log != null)
                    log.debug("Exception rolling back failed tx: ", ge2);
            } finally {
                xidMap.remove(xid);
            } 
            
            if (log != null) log.warn("error in prepare", ge);
            throw new FBXAException(XAException.XAER_RMERR, ge);
        }

        preparedXid.add(xid);
        return XA_OK;
    }

    private static final String RECOVERY_QUERY =
            "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
            + "FROM RDB$TRANSACTIONS";

    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     * The transaction manager calls this method during recovery to obtain the
     * list of transaction branches that are currently in prepared or
     * heuristically completed states.
     *
     * @param flags
     *            One of TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS. TMNOFLAGS must be
     *            used when no other flags are set in flags.
     * @return The resource manager returns zero or more XIDs for the
     *         transaction branches that are currently in a prepared or
     *         heuristically completed state. If an error occurs during the
     *         operation, the resource manager should throw the appropriate
     *         XAException.
     * @throws XAException
     *             An error has occurred. Possible values are XAER_RMERR,
     *             XAER_RMFAIL, XAER_INVAL, and XAER_PROTO.
     */
    public Xid[] recover(int flags) throws javax.transaction.xa.XAException {
        if (flags != XAResource.TMSTARTRSCAN && flags != XAResource.TMENDRSCAN && flags != XAResource.TMNOFLAGS && flags != (XAResource.TMSTARTRSCAN|XAResource.TMENDRSCAN))
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS, TMSTARTRSCAN|TMENDRSCAN", XAException.XAER_PROTO);

        try {
            // if (!((flags & XAResource.TMSTARTRSCAN) == 0))
//            if ((flags & XAResource.TMENDRSCAN) == 0 && (flags & XAResource.TMNOFLAGS) == 0)
//                return new Xid[0];

            List<FBXid> xids = new ArrayList<>();

            FbTransaction trHandle2 = database.startTransaction(tpb.getTransactionParameterBuffer());

            FbStatement stmtHandle2 = database.createStatement(trHandle2);

            GDSHelper gdsHelper2 = new GDSHelper(database);
            gdsHelper2.setCurrentTransaction(trHandle2);

            stmtHandle2.prepare(RECOVERY_QUERY);

            DataProvider dataProvider0 = new DataProvider(0);
            stmtHandle2.addStatementListener(dataProvider0);
            DataProvider dataProvider1 = new DataProvider(1);
            stmtHandle2.addStatementListener(dataProvider1);

            stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);
            stmtHandle2.fetchRows(10);

            FBField field0 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(0), dataProvider0, gdsHelper2, false);
            FBField field1 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(1), dataProvider1, gdsHelper2, false);

            int row = 0;
            while(row < dataProvider0.getRowCount()) {
                dataProvider0.setRow(row);
                dataProvider1.setRow(row);

                long inLimboTxId = field0.getLong();
                byte[] inLimboMessage = field1.getBytes();

                try {
                    FBXid xid = new FBXid(new ByteArrayInputStream(inLimboMessage), inLimboTxId);
                    xids.add(xid);
                } catch(FBIncorrectXidException ex) {
                    if (log != null)
                        log.warn("ignoring XID stored with invalid format in RDB$TRANSACTIONS for RDB$TRANSACTION_ID=" + inLimboTxId);
                }

                row++;
            }

            stmtHandle2.close();
            trHandle2.commit();

            return xids.toArray(new FBXid[0]);
        } catch (SQLException | ResourceException e) {
            throw new FBXAException("can't perform query to fetch xids", XAException.XAER_RMFAIL, e);
        }
    }

    private static final String RECOVERY_QUERY_PARAMETRIZED =
            "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
                    + "FROM RDB$TRANSACTIONS "
                    + "WHERE RDB$TRANSACTION_DESCRIPTION = CAST(? AS VARCHAR(32764) CHARACTER SET OCTETS)";

    /**
     * Obtain a single prepared transaction branch from a resource manager, based on a Xid
     *
     * @param externalXid
     *            The Xid to find
     * @return The Xid if found, otherwise null.
     * @throws XAException
     *             An error has occurred. Possible values are XAER_RMERR,
     *             XAER_RMFAIL, XAER_INVAL, and XAER_PROTO.
     */
    protected Xid findSingleXid(Xid externalXid) throws javax.transaction.xa.XAException {
        try {
            FbTransaction trHandle2 = database.startTransaction(tpb.getTransactionParameterBuffer());

            FbStatement stmtHandle2 = database.createStatement(trHandle2);

            GDSHelper gdsHelper2 = new GDSHelper(database);
            gdsHelper2.setCurrentTransaction(trHandle2);

            stmtHandle2.prepare(RECOVERY_QUERY_PARAMETRIZED);

            DataProvider dataProvider0 = new DataProvider(0);
            stmtHandle2.addStatementListener(dataProvider0);
            DataProvider dataProvider1 = new DataProvider(1);
            stmtHandle2.addStatementListener(dataProvider1);

            final RowValue parameters = stmtHandle2.getParameterDescriptor().createDefaultFieldValues();
            FBXid tempXid = new FBXid(externalXid);
            parameters.getFieldValue(0).setFieldData(tempXid.toBytes());
            stmtHandle2.execute(parameters);
            stmtHandle2.fetchRows(1);

            FBField field0 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(0), dataProvider0, gdsHelper2, false);
            FBField field1 = FBField.createField(stmtHandle2.getFieldDescriptor().getFieldDescriptor(1), dataProvider1, gdsHelper2, false);

            FBXid xid = null;
            if (dataProvider0.getRowCount() > 0) {
                dataProvider0.setRow(0);
                dataProvider1.setRow(0);

                long inLimboTxId = field0.getLong();
                byte[] inLimboMessage = field1.getBytes();

                try {
                    xid = new FBXid(new ByteArrayInputStream(inLimboMessage), inLimboTxId);
                } catch(FBIncorrectXidException ex) {
                    if (log != null)
                        log.warn("ignoring XID stored with invalid format in RDB$TRANSACTIONS for RDB$TRANSACTION_ID=" + inLimboTxId);
                }
            }

            stmtHandle2.close();
            trHandle2.commit();

            return xid;
        } catch (SQLException | ResourceException e) {
            throw new FBXAException("can't perform query to fetch xids", XAException.XAER_RMFAIL, e);
        }
    }

    private static class DataProvider extends DefaultStatementListener implements FieldDataProvider {
        private final List<RowValue> rows = new ArrayList<>();
        private final int fieldPos;
        private int row;
        
        private DataProvider(int fieldPos) {
            this.fieldPos = fieldPos;
        }
        
        public void setRow(int row) {
            this.row = row;
        }
        
        public byte[] getFieldData() {
            return rows.get(row).getFieldValue(fieldPos).getFieldData();
        }

        public void setFieldData(byte[] data) {
            throw new UnsupportedOperationException();
        }

        public int getRowCount() {
            return rows.size();
        }

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }
    }
    
    /**
     * Rolls back the work, assuming it was done on behalf of the specified
     * transaction.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end never called), the
     *             transaction ID is wrong, the connection was set to
     *             Auto-Commit, or the rollback on the underlying connection
     *             fails. The error code differs depending on the exact
     *             situation.
     */
    public void rollback(Xid xid) throws XAException {
        try {
            mcf.notifyRollback(this, xid);
        } catch (GDSException ge) {
            throw new FBXAException(ge.getXAErrorCode(), ge);
        }
    }

    void internalRollback(Xid xid) throws XAException {
        if (log != null) log.trace("rollback called: " + xid);
        FbTransaction committingTr = xidMap.get(xid);
        if (committingTr == null) {
            throw new FBXAException ("Rollback called with unknown transaction: " + xid);
        }

        try {
            if (committingTr == getGDSHelper().getCurrentTransaction())
                throw new FBXAException("Rollback called with non-ended xid", XAException.XAER_PROTO);

            // TODO Equivalent needed or handled by listeners?
            //committingTr.forgetResultSets();
            try {
                getGDSHelper().rollbackTransaction(committingTr);
            } finally {
                xidMap.remove(xid);
                preparedXid.remove(xid);
            }
        } catch (SQLException ge) {
            if (log != null) log.debug("Exception in rollback", ge);
            throw new FBXAException(ge.getMessage(), XAException.XAER_RMERR, ge);
        }
    }

    /**
     * Sets the transaction timeout. This is saved, but the value is not used by
     * the current implementation.
     * 
     * @param timeout
     *            The timeout to be set in seconds
     */
    public boolean setTransactionTimeout(int timeout)
            throws javax.transaction.xa.XAException {
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
     * distinguish work done by different transactions on the same connection).
     * If start is called more than once before end, either it's a duplicate
     * transaction ID or illegal transaction ID (since you can't have two
     * transactions associated with one DB connection).
     * 
     * 
     * @param id
     *            A global transaction identifier to be associated with the
     *            resource
     * @param flags
     *            One of TMNOFLAGS, TMJOIN, or TMRESUME
     * @throws XAException
     *             Occurs when the state was not correct (start called twice),
     *             the transaction ID is wrong, or the instance has already been
     *             closed.
     */
    public void start(Xid id, int flags) throws XAException {
        if (flags != XAResource.TMNOFLAGS && flags != XAResource.TMJOIN && flags != XAResource.TMRESUME)
            throw new FBXAException("flag not allowed in this context: " + flags + ", valid flags are TMNOFLAGS, TMJOIN, TMRESUME", XAException.XAER_PROTO);
        if (flags == XAResource.TMJOIN)
            throw new FBXAException("Joining two transactions is not supported", XAException.XAER_RMFAIL);
        
        try {
            // reset the transaction parameters for the managed scenario 
            setTransactionIsolation(mcf.getDefaultTransactionIsolation());
            
            internalStart(id, flags);
            
            mcf.notifyStart(this, id);
            
            inDistributedTransaction = true;

            // This will reset the managed environment of the associated connections and set the transaction coordinator to managed
            // TODO This is a bit of a hack; need to find a better way; this doesn't work with connectionSharing = true
            setManagedEnvironment(isManagedEnvironment());
            
        } catch (GDSException ge) {
            throw new FBXAException(ge.getXAErrorCode(), ge);
        } catch (SQLException | ResourceException e) {
            throw new FBXAException(XAException.XAER_RMERR, e);
        }
    }

    /**
     * Perform the internal processing to start associate a JDBC connection with
     * a global transaction.
     * 
     * @see #start(Xid, int)
     * @param id
     *            A global transaction identifier to be associated with the
     *            resource
     * @param flags
     *            One of TMNOFLAGS, TMJOIN, or TMRESUME
     * @throws XAException If the transaction is already started, or this connection cannot participate in the distributed transaction
     * @throws SQLException
     */
    public void internalStart(Xid id, int flags) throws XAException, SQLException {
        if (log != null) log.trace("start called: " + id);

        if (getGDSHelper().getCurrentTransaction() != null)
            throw new FBXAException("Transaction already started", XAException.XAER_PROTO);

        findIscTrHandle(id, flags);
    }

    // FB public methods. Could be package if packages reorganized.

    /**
     * Close this connection with regards to a wrapping
     * <code>AbstractConnection</code>.
     * 
     * @param c
     *            The <code>AbstractConnection</code> that is being closed
     */
    public void close(FBConnection c) {
        c.setManagedConnection(null);
        connectionHandles.remove(c);
        ConnectionEvent ce = new ConnectionEvent(this,
                ConnectionEvent.CONNECTION_CLOSED, null);
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

    public TransactionParameterBuffer getTransactionParameters() {
        return tpb.getTransactionParameterBuffer();
    }
    
    public void setTransactionParameters(TransactionParameterBuffer transactionParameters) {
        tpb.setTransactionParameterBuffer(transactionParameters);
    }
    
    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return mcf.getTransactionParameters(isolation);
    }
    
    public void setTransactionParameters(int isolation, TransactionParameterBuffer transactionParams) {
        mcf.setTransactionParameters(isolation, transactionParams);
    }
    
    // --------------------------------------------------------------------
    // package visibility
    // --------------------------------------------------------------------

    private void findIscTrHandle(Xid xid, int flags) throws SQLException, XAException {
        // FIXME return old tr handle if it is still valid before proceeding
        getGDSHelper().setCurrentTransaction(null);
        
        if (flags == XAResource.TMRESUME) {
            FbTransaction trHandle = xidMap.get(xid);
            if (trHandle == null) {
                throw new FBXAException(
                        "You are trying to resume a transaction that is not attached to this XAResource",
                        XAException.XAER_INVAL);
            }
            
            getGDSHelper().setCurrentTransaction(trHandle);
            return;
        }
        
        for (Xid knownXid : xidMap.keySet()) {
            boolean sameFormatId = knownXid.getFormatId() == xid.getFormatId();
            boolean sameGtrid = Arrays.equals(knownXid.getGlobalTransactionId(), xid.getGlobalTransactionId());
            boolean sameBqual = Arrays.equals(knownXid.getBranchQualifier(), xid.getBranchQualifier());
            if (sameFormatId && sameGtrid && sameBqual)
                throw new FBXAException(
                        "A transaction with the same XID has already been started",
                        XAException.XAER_DUPID);
        }
        
        // new xid for us
        try {
            FbTransaction transaction = getGDSHelper().startTransaction(tpb.getTransactionParameterBuffer());
            xidMap.put(xid, transaction);
        } catch (SQLException e) {
            throw new FBXAException(e.getMessage(), XAException.XAER_RMERR, e);
        }
    }
    
    void notify(CELNotifier notifier, ConnectionEvent ce) {
        for (ConnectionEventListener cel : connectionEventListeners) {
            notifier.notify(cel, ce);
        }
    }

    interface CELNotifier {

        void notify(ConnectionEventListener cel, ConnectionEvent ce);
    }

    static final CELNotifier connectionClosedNotifier = new CELNotifier() {

        public void notify(ConnectionEventListener cel, ConnectionEvent ce) {
            cel.connectionClosed(ce);
        }
    };

    static final CELNotifier connectionErrorOccurredNotifier = new CELNotifier() {

        public void notify(ConnectionEventListener cel, ConnectionEvent ce) {
            cel.connectionErrorOccurred(ce);
        }
    };

    static final CELNotifier localTransactionStartedNotifier = new CELNotifier() {

        public void notify(ConnectionEventListener cel, ConnectionEvent ce) {
            cel.localTransactionStarted(ce);
        }
    };

    static final CELNotifier localTransactionCommittedNotifier = new CELNotifier() {

        public void notify(ConnectionEventListener cel, ConnectionEvent ce) {
            cel.localTransactionCommitted(ce);
        }
    };

    static final CELNotifier localTransactionRolledbackNotifier = new CELNotifier() {

        public void notify(ConnectionEventListener cel, ConnectionEvent ce) {
            cel.localTransactionRolledback(ce);
        }
    };

    boolean matches(Subject subj, ConnectionRequestInfo cri) {
        
        if (cri == null) {
            return true;
        }
        
        if (!(cri instanceof FBConnectionRequestInfo))
            return false;
        
        try {
            return this.cri.equals(getCombinedConnectionRequestInfo(subj, cri));
        } catch (ResourceException re) {
            return false;
        }
    }

    /**
     * Get the transaction isolation level of this connection. The level is one
     * of the static final fields of <code>java.sql.Connection</code> (i.e.
     * <code>TRANSACTION_READ_COMMITTED</code>,
     * <code>TRANSACTION_READ_UNCOMMITTED</code>,
     * <code>TRANSACTION_REPEATABLE_READ</code>,
     * <code>TRANSACTION_SERIALIZABLE</code>.
     * 
     * @see java.sql.Connection
     * @see #setTransactionIsolation(int)
     * @return Value representing a transaction isolation level defined in
     *         {@link java.sql.Connection}.
     * @throws ResourceException
     *             If the transaction level cannot be retrieved
     */
    public int getTransactionIsolation() throws ResourceException {
        return transactionIsolation;
    }

    /**
     * Set the transaction level for this connection. The level is one of the
     * static final fields of <code>java.sql.Connection</code> (i.e.
     * <code>TRANSACTION_READ_COMMITTED</code>,
     * <code>TRANSACTION_READ_UNCOMMITTED</code>,
     * <code>TRANSACTION_REPEATABLE_READ</code>,
     * <code>TRANSACTION_SERIALIZABLE</code>.
     * 
     * @see java.sql.Connection
     * @see #getTransactionIsolation()
     * @param isolation
     *            Value representing a transaction isolation level defined in
     *            {@link java.sql.Connection}.
     * @throws ResourceException
     *             If the transaction level cannot be retrieved
     */
    public void setTransactionIsolation(int isolation) throws ResourceException {
        transactionIsolation = isolation;
        
        tpb = mcf.getTpb(isolation);
    }

    /**
     * Get the managed connection factory that created this managed connection.
     * 
     * @return instance of {@link ManagedConnectionFactory}.
     */
    public ManagedConnectionFactory getManagedConnectionFactory() {
        return mcf;
    }
    
    /**
     * Set whether this connection is to be readonly
     * 
     * @param readOnly
     *            If <code>true</code>, the connection will be set read-only,
     *            otherwise it will be writable
     */
    public void setReadOnly(boolean readOnly) {
        tpb.setReadOnly(readOnly);
    }

    /**
     * Retrieve whether this connection is readonly.
     * 
     * @return <code>true</code> if this connection is readonly,
     *         <code>false</code> otherwise
     */
    public boolean isReadOnly() {
        return tpb.isReadOnly();
    }

    private void notifyWarning(SQLWarning warning) {
        // Note: minor chance of a race condition here, but we take the chance.
        if (connectionHandles.isEmpty()) {
            if (unnotifiedWarnings == null) {
                unnotifiedWarnings = warning;
            } else {
                unnotifiedWarnings.setNextWarning(warning);
            }
        }
        for (FBConnection connection : new ArrayList<>(connectionHandles)) {
            connection.addWarning(warning);
        }
    }

    /**
     * DatabaseListener implementation for use by this ManagedConnection.
     */
    private class MCDatabaseListener extends DefaultDatabaseListener {
        @Override
        public void warningReceived(FbDatabase database, SQLWarning warning) {
            if (database != FBManagedConnection.this.database) {
                database.removeDatabaseListener(this);
                return;
            }
            notifyWarning(warning);
        }
    }
}
