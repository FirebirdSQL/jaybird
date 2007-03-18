/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jca;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.*;
import org.firebirdsql.gds.impl.GDSHelper.GDSHelperErrorListener;
import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * The class <code>FBManagedConnection</code> implements both the
 * ManagedConnection and XAResource interfaces.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @version 1.0
 */
public class FBManagedConnection implements ManagedConnection, XAResource, GDSHelperErrorListener {

    private static final Logger log = LoggerFactory.getLogger(FBManagedConnection.class, false);

    private FBManagedConnectionFactory mcf;

    private ArrayList connectionEventListeners = new ArrayList();
    private ArrayList connectionHandles = new ArrayList();

    private int timeout = 0;

    private Map xidMap = new HashMap();
    
    private GDS gds;
    private IscDbHandle dbHandle;
    private GDSHelper gdsHelper;

    private FBConnectionRequestInfo cri;
    private FBTpb tpb;
    private int transactionIsolation;

    private boolean managedEnvironment = true;
    private boolean connectionSharing = true;

    FBManagedConnection(Subject subject, ConnectionRequestInfo cri,
            FBManagedConnectionFactory mcf) throws ResourceException {
        
        this.mcf = mcf;
        this.gds = mcf.getGDS();
        this.cri = getCombinedConnectionRequestInfo(subject, cri);
        this.tpb = mcf.getDefaultTpb();
        this.transactionIsolation = mcf.getDefaultTransactionIsolation();
        
        try {
            this.dbHandle = gds.createIscDbHandle();

            DatabaseParameterBuffer dpb = this.cri.getDpb();
            gds.iscAttachDatabase(mcf.getDatabase(), dbHandle, dpb);
            
            this.gdsHelper = new GDSHelper(this.gds, dpb, (AbstractIscDbHandle)this.dbHandle, this);
            
        } catch(GDSException ex) {
            throw new FBResourceException(ex);
        }
    }

    /**
     * Notify GDS container that error occured, if the <code>ex</code> 
     * represents a "fatal" one
     * 
     * @see FatalGDSErrorHelper#isFatal(GDSException)
     */
    public void errorOccured(GDSException ex) {
        
        if (log != null) log.trace(ex.getMessage());
        
        if (!FatalGDSErrorHelper.isFatal(ex))
            return;
        
        ConnectionEvent event = new ConnectionEvent(
            FBManagedConnection.this, 
            ConnectionEvent.CONNECTION_ERROR_OCCURRED, ex);
        
        FBManagedConnection.this.notify(
            connectionErrorOccurredNotifier, event);
    }

    
    private FBConnectionRequestInfo getCombinedConnectionRequestInfo(
            Subject subject, ConnectionRequestInfo cri)
            throws ResourceException {
        if (cri == null) {
            cri = mcf.getDefaultConnectionRequestInfo();
        }
        try {
            FBConnectionRequestInfo fbcri = (FBConnectionRequestInfo) cri;
            if (subject != null) {
                // see connector spec, section 8.2.6, contract for
                // ManagedConnectinFactory, option A.
                for (Iterator i = subject.getPrivateCredentials().iterator(); i
                        .hasNext();) {
                    Object cred = i.next();
                    if (cred instanceof PasswordCredential
                            && equals(((PasswordCredential) cred)
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
            throw new FBResourceException(
                    "Incorrect ConnectionRequestInfo class supplied");
        }
    }
    
    /**
     * Get instance of {@link GDSHelper} connected with this managed connection.
     * 
     * @return instance of {@link GDSHelper}.
     */
    public GDSHelper getGDSHelper() throws GDSException {
        if (gdsHelper == null)
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_req_no_trans);
        
        return gdsHelper;
    }
    
    public String getDatabase() {
        return mcf.getDatabase();
    }

    public boolean isManagedEnvironment() {
        return managedEnvironment;
    }
    
    public boolean inTransaction() {
        return gdsHelper.inTransaction();
    }
    
    public void setManagedEnvironment(boolean managedEnvironment) throws ResourceException{
        this.managedEnvironment = managedEnvironment;
        
        // if connection sharing is not enabled, notify currently associated
        // connection handle about the state change.
        if (!connectionSharing) {
            if (connectionHandles.size() > 1)
                throw new java.lang.IllegalStateException(
                    "Multiple connections associated with this managed " +
                    "connection in non-sharing mode.");
            
            // there will be at most one connection.
            for (Iterator iter = connectionHandles.iterator(); iter.hasNext();) {
                AbstractConnection connection = (AbstractConnection) iter.next();
                
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
     * enabled, multiple connection handles ({@link AbstractConnection} instances)
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
     */
    public void setConnectionSharing(boolean connectionSharing) {
        if (!connectionHandles.isEmpty())
            throw new java.lang.IllegalStateException(
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
     * @throws NotSupportedException
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
     * @throws NotSupportedException
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
     * @throws IllegalStateException
     *             Illegal state for invoking this method
     * @throws ResourceAdapterInternalException
     *             Resource adapter internal error condition
     */
    public void associateConnection(Object connection) throws ResourceException {
        
        if (!connectionSharing)
            disassociateConnections();
        
        try {
            ((AbstractConnection) connection).setManagedConnection(this);
            connectionHandles.add(connection);
        } catch (ClassCastException cce) {
            throw new FBResourceException(
                    "invalid connection supplied to associateConnection.", cce);
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
     * @throws IllegalStateException
     *             Illegal state for calling connection cleanup. Example - if a
     *             local transaction is in progress that doesn't allow
     *             connection cleanup
     */
    public void cleanup() throws ResourceException {
        disassociateConnections();
        
        this.gdsHelper.setCurrentTrHandle(null);

        // reset the TPB from the previous transaction.
        this.tpb = mcf.getDefaultTpb();
        this.transactionIsolation = mcf.getDefaultTransactionIsolation();
    }

    /**
     * Disassociate connections from current managed connection.
     *
     */
    private void disassociateConnections() throws ResourceException {
        
        ResourceException ex = null;
        
        for (Iterator i = connectionHandles.iterator(); i.hasNext();) {
            AbstractConnection connection = (AbstractConnection) i.next();
            
            try {
                connection.close();
            } catch(SQLException sqlex) {
                if (ex == null)
                    ex = new FBResourceException(sqlex);
                else
                    ((SQLException)ex.getLinkedException()).setNextException(sqlex);
            }
        }
        
        connectionHandles.clear();
        
        if (ex != null)
            throw ex;
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
     * @throws SecurityException
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
            throw new FBResourceException("Incompatible subject or "
                    + "ConnectionRequestInfo in getConnection!");  

        if (!connectionSharing)
            disassociateConnections();
        
        AbstractConnection c = mcf.newConnection(this);
        try {
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
     * @throws IllegalStateException
     *             illegal state for destroying connection
     */
    public void destroy() throws ResourceException {
        
        if (gdsHelper == null)
            return;
        
        if (gdsHelper.inTransaction()) 
            throw new java.lang.IllegalStateException(
                "Can't destroy managed connection  with active transaction");
        
        try {
            gdsHelper.detachDatabase();
        } catch (GDSException ge) {
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
    public javax.transaction.xa.XAResource getXAResource() {
        if (log != null)
            log.debug("XAResource requested from FBManagedConnection");
        return this;
    }

    // --------------------------------------------------------------
    // XAResource implementation
    // --------------------------------------------------------------

    boolean isXidActive(Xid xid) {
        IscTrHandle trHandle = (IscTrHandle)xidMap.get(xid); //mcf.getTrHandleForXid(xid);

        if (trHandle == null) return false;

        AbstractIscDbHandle dbHandle = (AbstractIscDbHandle)trHandle.getDbHandle();

        if (dbHandle == null) return false;

        return dbHandle.isValid();
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
     * @param id
     *            a <code>Xid</code> value
     * @param onePhase
     *            a <code>boolean</code> value
     * @exception GDSException
     *                if an error occurs
     */
    void internalCommit(Xid xid, boolean onePhase) throws XAException,
            GDSException {
        if (log != null) log.trace("Commit called: " + xid);
        AbstractIscTrHandle committingTr = (AbstractIscTrHandle)xidMap.get(xid);

        if (committingTr == null)
            throw new FBXAException("Commit called with unknown transaction",
                    XAException.XAER_NOTA);

        if (committingTr == gdsHelper.getCurrentTrHandle())
            throw new FBXAException("Commit called with current xid",
                    XAException.XAER_PROTO);

        try {
            committingTr.forgetResultSets();
            try {
                gdsHelper.commitTransaction(committingTr);
            } catch (GDSException ge) {
                try {
                    gdsHelper.rollbackTransaction(committingTr);
                } catch (GDSException ge2) {
                    if (log != null)
                        log.debug("Exception rolling back failed tx: ", ge2);
                }
                throw ge;
            } finally {
                xidMap.remove(xid);
            }
            
        } catch (GDSException ge) {
            ge.setXAErrorCode(XAException.XAER_RMERR);
            throw ge;
        }

    }

    /**
     * Dissociates a resource from a global transaction.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end called twice), or
     *             the transaction ID is wrong.
     */
    public void end(Xid id, int flags) throws XAException {

        if (flags != XAResource.TMSUSPEND && flags != XAResource.TMSUCCESS
                && flags != XAResource.TMFAIL)
            throw new FBXAException(
                    "Invalid flag in end: must be TMSUSPEND, TMSUCCESS, or TMFAIL",
                    XAException.XAER_INVAL); 

        
        internalEnd(id, flags);
        
        mcf.notifyEnd(this, id);
    }

    /**
     * The <code>internalEnd</code> method ends the xid as requested if
     * approprriate and throws a GDSException including the appropriate XA error
     * code and a message if not. The caller can decode the exception as
     * necessary.
     * 
     * @param id
     *            a <code>Xid</code> value
     * @param flags
     *            an <code>int</code> value
     * @exception GDSException
     *                if an error occurs
     */
    void internalEnd(Xid xid, int flags) throws XAException {

        if (log != null) log.debug("End called: " + xid);
        IscTrHandle endingTr = (IscTrHandle)xidMap.get(xid);

        if (endingTr == null)
            throw new FBXAException("Unrecognized transaction",
                    XAException.XAER_NOTA);

        if (endingTr == gdsHelper.getCurrentTrHandle())
            gdsHelper.setCurrentTrHandle(null);
        else 
        if (flags == XAResource.TMSUSPEND)
            throw new FBXAException("You are trying to suspend a transaction "
                    + "that is not the current transaction", XAException.XAER_INVAL);

        // Otherwise, it is fail or success for a tx that will be committed or
        // rolled back shortly.
    }

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure). It is assumed this will be
     * called after a failed commit or rollback. This should actually never be
     * called since we don't use heuristic tx completion on timeout.
     * 
     * @throws XAException
     *             Occurs when the state was not correct (end never called), or
     *             the transaction ID is wrong.
     */
    public void forget(Xid id) throws javax.transaction.xa.XAException {
        throw new FBXAException("Not yet implemented");
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
    public boolean isSameRM(XAResource res)
            throws javax.transaction.xa.XAException {
        return (res instanceof FBManagedConnection)
                && (dbHandle.equals(((FBManagedConnection) res).dbHandle));
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

    int internalPrepare(Xid xid) throws FBXAException, GDSException {
        if (log != null) log.trace("prepare called: " + xid);
        AbstractIscTrHandle committingTr = (AbstractIscTrHandle)xidMap.get(xid);
        if (committingTr == null)
            throw new FBXAException("Prepare called with unknown transaction",
                    XAException.XAER_INVAL);
        if (committingTr == gdsHelper.getCurrentTrHandle())
            throw new FBXAException("Prepare called with current xid",
                    XAException.XAER_PROTO);
            try {
                FBXid fbxid;
                if (xid instanceof FBXid) {
                    fbxid = (FBXid) xid;
                } else {
                    fbxid = new FBXid(xid);
                }
                byte[] message = fbxid.toBytes();
                
                gdsHelper.prepareTransaction(committingTr, message);
            } catch (GDSException ge) {
                try {
                    gdsHelper.rollbackTransaction(committingTr);
                } catch (GDSException ge2) {
                    if (log != null)
                        log.debug("Exception rolling back failed tx: ", ge2);
                } finally {
                    xidMap.remove(xid);
                } 
                
                if (log != null) log.warn("error in prepare", ge);
                throw ge;
            }
        return XA_OK;
    }

    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     * The transaction manager calls this method during recovery to obtain the
     * list of transaction branches that are currently in prepared or
     * heuristically completed states.
     * 
     * @param flag
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
    public Xid[] recover(int flag) throws javax.transaction.xa.XAException {
        try {
            
            AbstractIscTrHandle trHandle2 = (AbstractIscTrHandle)gds.createIscTrHandle();
            gds.iscStartTransaction(trHandle2, gdsHelper.getCurrentDbHandle(), tpb.getTransactionParameterBuffer());
            
            GDSHelper gdsHelper2 = new GDSHelper(gds, 
                gdsHelper.getDatabaseParameterBuffer(), 
                (AbstractIscDbHandle) gdsHelper.getCurrentDbHandle(), null);
            
            gdsHelper2.setCurrentTrHandle(trHandle2);
            
            ArrayList xids = FBManagedConnectionFactory.fetchInLimboXids(gds, gdsHelper2);
            gds.iscCommitTransaction(trHandle2);
            
            return (FBXid[])xids.toArray(new FBXid[xids.size()]);

        } catch(GDSException ex) {
            if (log != null)
                log.debug("can't perform query to fetch xids", ex);
            throw new FBXAException(XAException.XAER_RMFAIL, ex);
        } catch (SQLException sqle) {
            if (log != null)
                log.debug("can't perform query to fetch xids", sqle);
            throw new FBXAException(XAException.XAER_RMFAIL, sqle);
        } 
        catch (ResourceException re) {
            if (log != null)
                log.debug("can't perform query to fetch xids", re);
            throw new FBXAException(XAException.XAER_RMFAIL, re);
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
    public void rollback(Xid id) throws XAException {
        try {
            mcf.notifyRollback(this, id);
        } catch (GDSException ge) {
            throw new FBXAException(ge.getXAErrorCode(), ge);
        }
    }

    void internalRollback(Xid xid) throws XAException, GDSException {
        if (log != null) log.trace("rollback called: " + xid);
        AbstractIscTrHandle committingTr = (AbstractIscTrHandle)xidMap.get(xid); //mcf.getTrHandleForXid(id);
        if (committingTr == null) {
            if (log != null)
                log.warn("rollback called with unknown transaction: " + xid);
            return;
        }

        if (committingTr == gdsHelper.getCurrentTrHandle())
            throw new FBXAException("Rollback called with current xid",
                    XAException.XAER_PROTO);

        try {
            committingTr.forgetResultSets();
            try {
                gdsHelper.rollbackTransaction(committingTr);
            } finally {
                xidMap.remove(xid);
            }
        } catch (GDSException ge) {
            if (log != null) log.debug("Exception in rollback", ge);
            ge.setXAErrorCode(XAException.XAER_RMERR);
            throw ge;
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
        try {
            
            // reset the transaction parameters for the managed scenario 
            setTransactionIsolation(mcf.getDefaultTransactionIsolation());
            
            internalStart(id, flags);
            
            mcf.notifyStart(this, id);
            
        } catch (GDSException ge) {
            throw new FBXAException(ge.getXAErrorCode());
        } catch(ResourceException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
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
     */
    public void internalStart(Xid id, int flags) throws XAException, GDSException {
        if (log != null) log.trace("start called: " + id);

        if (gdsHelper.getCurrentTrHandle() != null) throw new XAException(XAException.XAER_PROTO);

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
    public void close(AbstractConnection c) {
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

    private void findIscTrHandle(Xid xid, int flags) throws GDSException,
            XAException {

        // FIXME return old tr handle if it is still valid before proceeding
        gdsHelper.setCurrentTrHandle(null);
        
        AbstractIscTrHandle trHandle = (AbstractIscTrHandle) xidMap.get(xid);
        
        if (trHandle != null) {
            if (flags != XAResource.TMJOIN && flags != XAResource.TMRESUME) {
                // this xid is already known, should have join or resume flag.
                // DUPID might be better?
                throw new FBXAException(
                        "You are trying to start a transaction as new "
                                + "that is already known to this XAResource",
                        XAException.XAER_INVAL);
            }

            gdsHelper.setCurrentTrHandle(trHandle);
            return;
        }
        
        if (flags != XAResource.TMNOFLAGS) {
            // We don't know this xid, should come with no flags.
            throw new FBXAException(
                    "You are trying to resume a transaction that has is new",
                    XAException.XAER_INVAL);
        }
        
        // new xid for us
        // TODO check the exception handling here!!!
        trHandle = gdsHelper.startTransaction(tpb.getTransactionParameterBuffer());

        xidMap.put(xid, trHandle);
    }
    
    void notify(CELNotifier notifier, ConnectionEvent ce) {
        if (connectionEventListeners.size() == 0) { return; }
        if (connectionEventListeners.size() == 1) {
            ConnectionEventListener cel = (ConnectionEventListener) connectionEventListeners
                    .get(0);
            notifier.notify(cel, ce);
            return;
        } // end of if ()
        ArrayList cels = (ArrayList) connectionEventListeners.clone();
        for (Iterator i = cels.iterator(); i.hasNext();) {
            notifier.notify((ConnectionEventListener) i.next(), ce);
        } // end of for ()
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
            return this.cri.equals(getCombinedConnectionRequestInfo(subj, (FBConnectionRequestInfo)cri));
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

}
