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

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.*;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.*;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 * The class <code>FBManagedConnection</code> implements both the
 * ManagedConnection and XAResource interfaces.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBManagedConnection implements ManagedConnection, XAResource {

   private final Logger log = LoggerFactory.getLogger(getClass(),false);

    private FBManagedConnectionFactory mcf;

    private ArrayList connectionEventListeners = new ArrayList();

    private ArrayList connectionHandles = new ArrayList();

    private int timeout = 0;

    /**
     * Needed from mcf when killing a db handle when a new tx cannot be started.
     */
    protected FBConnectionRequestInfo cri;


    private isc_tr_handle currentTr;

    private isc_db_handle currentDbHandle;

    //Autocommit flag.  This should be left true if you are using Local or
    //XATransactions and want to execute statements outside a transaction.
    //Set it false only if you use the Connection.commit and rollback methods.

    public boolean autoCommit = true;

    private final FBTpb tpb;

    FBManagedConnection(final Subject subject,
                        final ConnectionRequestInfo cri,
                        final FBManagedConnectionFactory mcf)
        throws ResourceException
    {
        this.mcf = mcf;
        this.cri = getCombinedConnectionRequestInfo(subject, cri);//cri;
        this.tpb = mcf.getTpb(); //getTpb supplies a copy.
        //Make sure we can get a connection to the db.
        try
        {
            currentDbHandle =  mcf.createDbHandle(this.cri);
        }
        catch (GDSException ge)
        {
            if (log!=null) log.debug("Could not get a db connection!", ge);
            throw new FBResourceException(ge);
        } // end of try-catch

    }



    //javax.resource.spi.ManagedConnection implementation

    /**
     * Returns a <code>javax.resource.spi.LocalTransaction</code> instance. 
     * The LocalTransaction interface is used by the container to manage 
     * local transactions for a RM instance.
     *
     * @return LocalTransaction instance
     * @throws ResourceException generic exception if operation fails
     * @throws NotSupportedException if the operation is not supported
     * @throws ResourceAdapterInternalException resource adapter internal 
     *         error condition
     */
    public LocalTransaction getLocalTransaction()
    {
       return new FBLocalTransaction(this, null);
    }



    /**
     * Gets the metadata information for this connection's underlying EIS 
     * resource manager instance. The ManagedConnectionMetaData interface 
     * provides information about the underlying EIS instance associated with 
     * the ManagedConenction instance.
     *
     * @return ManagedConnectionMetaData instance
     * @throws ResourceException generic exception if operation fails
     * @throws NotSupportedException if the operation is not supported
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
     * ManagedConnection instance based on the connection pooling
     * requirements.
     * <P>
     * When a ManagedConnection object is initially created, the default log 
     * writer associated with this instance is obtained from the 
     * <code>ManagedConnectionFactory</code>. An application server can set a 
     * log writer specific to this ManagedConnection to log/trace this 
     * instance using setLogWriter method.
     *
     * @param out Character Output stream to be associated
     * @throws ResourceException generic exception if operation fails
     * @throws ResourceAdapterInternalException resource adapter related error 
     *         condition
     */
    public void setLogWriter(PrintWriter out){
       //ignore, we are using log4j.
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
     * (that created this connection) or one set specifically for this
     * instance by the application server.
     *
     * @return Character ourput stream associated with this 
     *         <code>ManagedConnection</code>
     *  @throws ResourceException generic exception if operation fails
     */
    public PrintWriter getLogWriter() {
       return null;//we are using log4j.
    }

    /** 
     * Add an <code>ConnectionEventListener</code> listener. The listener will
     * be notified when a <code>ConnectionEvent</code> occurs.
     *
     * @param listener The <code>ConnectionEventListener</code> to be added
   */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }



    /**
     * Remove a <code>ConnectionEventListner</code> from the listing of
     * listeners that will be notified for a <code>ConnectionEvent</code>.
     *
     * @param listener The <code>ConnectionEventListener</code> to be removed
   */
    public void removeConnectionEventListener(ConnectionEventListener listener){
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
   * @param connection Application-level connection handle
   * @throws ResourceException Failed to associate the connection handle 
   *         with this ManagedConnection instance
   * @throws IllegalStateException Illegal state for invoking this method
   * @throws ResourceAdapterInternalException Resource adapter internal error 
   *         condition
   */
    public void associateConnection(Object connection) 
    throws ResourceException {
        try {
            ((AbstractConnection)connection).setManagedConnection(this);
            connectionHandles.add(connection);
        }
        catch (ClassCastException cce) {
            throw new FBResourceException("invalid connection supplied to associateConnection.", cce);
        }
    }


    /** 
     * Application server calls this method to force any cleanup on the 
     * <code>ManagedConnection</code> instance.
     * <P> 
     * The method {@link ManagedConnection#cleanup} initiates a cleanup of the 
     * any client-specific state as maintained by a ManagedConnection instance. 
     * The cleanup should invalidate all connection handles that had been 
     * created using this <code>ManagedConnection</code> instance. Any attempt 
     * by an application component to use the connection handle after cleanup 
     * of the underlying <code>ManagedConnection</code> should result in an 
     * exception.
     * <P>
     * The cleanup of ManagedConnection is always driven by an application 
     * server. An application server should not invoke 
     * {@link ManagedConnection#cleanup} when there is an uncompleted 
     * transaction (associated with a ManagedConnection instance) in progress.
     * <P>
     * The invocation of {@link ManagedConnection#cleanup} method on an 
     * already cleaned-up connection should not throw an exception.
     *
     * The cleanup of <code>ManagedConnection</code> instance resets its 
     * client specific state and prepares the connection to be put back in to 
     * a connection pool. The cleanup method should not cause resource adapter 
     * to close the physical pipe and reclaim system resources associated with 
     * the physical connection.
     *
     * @throws ResourceException generic exception if operation fails
     * @throws ResourceAdapterInternalException resource adapter internal 
     *         error condition
     * @throws IllegalStateException Illegal state for calling connection 
     *         cleanup. Example - if a local transaction is in progress that 
     *         doesn't allow connection cleanup
     */
    public void cleanup() throws ResourceException
    {
        for (Iterator i = connectionHandles.iterator(); i.hasNext();)
        {
            ((AbstractConnection)i.next()).setManagedConnection(null);
        } // end of for ()
        connectionHandles.clear();
        this.currentTr = null;

        // reset the TPB from the previous transaction.
        this.tpb.setTpb(mcf.getTpb());
    }

    /** 
     * Creates a new connection handle for the underlying physical connection 
     * represented by the <code>ManagedConnection</code> instance. This 
     * connection handle is used by the application code to refer to 
     * the underlying physical connection. A connection handle is tied to its 
     * <code>ManagedConnection</code> instance in a resource adapter 
     * implementation specific way.  
     * <P>
     *
     * The <code>ManagedConnection</code> uses the Subject and additional 
     * <code>ConnectionRequestInfo</code> (which is specific to resource 
     * adapter and opaque to application server) to set the state of the 
     * physical connection.
     *
     * @param subject security context as JAAS subject
     * @param cxRequestInfo ConnectionRequestInfo instance
     * @return generic <code>Object</code> instance representing the 
     *         connection handle. For CCI, the connection handle created by a 
     *         <code>ManagedConnection</code> instance is of the type 
     *         <code>javax.resource.cci.Connection</code>.  
     * @throws ResourceException generic exception if operation fails 
     * @throws ResourceAdapterInternalException resource adapter internal 
     *         error condition 
     * @throws SecurityException security related error condition 
     * @throws CommException failed communication with EIS instance 
     * @throws EISSystemException internal error condition in EIS instance - 
     *         used if EIS instance is involved in setting state 
     *         of <code>ManagedConnection</code>
     */
    public Object getConnection(Subject subject, ConnectionRequestInfo cri)
        throws ResourceException
    {
        if (!matches(subject, cri))
        {
            throw new FBResourceException("Incompatible subject or ConnectionRequestInfo in getConnection!");
        } // end of if ()

        AbstractConnection c = new FBConnection(this);
        connectionHandles.add(c);
        return c;
    }


    /**
     * Destroys the physical connection to the underlying resource manager.
     * To manage the size of the connection pool, an application server can 
     * explictly call {@link ManagedConnection#destroy} to destroy a physical 
     * connection. A resource adapter should destroy all allocated system 
     * resources for this <code>ManagedConnection</code> instance when the 
     * method destroy is called.
     * 
     * @throws ResourceException generic exception if operation failed 
     * @throws IllegalStateException illegal state for destroying connection
     */
    public void destroy() throws ResourceException {
        if (currentTr != null) {
            throw new java.lang.IllegalStateException("Can't destroy managed connection  with active transaction");
        }
        if (currentDbHandle != null) {
            try {
               // if (log!=null) log.debug("in ManagedConnection.destroy",new Exception());
                mcf.releaseDbHandle(currentDbHandle, cri);
            }
            catch (GDSException ge) {
                throw new FBResourceException("Can't detach from db.", ge);
            }
            finally {
                currentDbHandle = null;
            }
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
       if (log!=null) log.debug("XAResource requested from FBManagedConnection");
       return this;
    }

    //--------------------------------------------------------------
    //XAResource implementation
    //--------------------------------------------------------------


    boolean isXidActive(Xid xid) {
        isc_tr_handle trHandle = mcf.getTrHandleForXid(xid);
        
        if (trHandle == null)
            return false;
        
        isc_db_handle dbHandle = trHandle.getDbHandle();
        
        if (dbHandle == null)
            return false;
        
        return dbHandle.isValid();
    }
    
    /**
     * Commits a transaction.
     *
     * @throws XAException Occurs when the state was not correct (end never 
     *         called), the transaction ID is wrong, the connection was set to 
     *         Auto-Commit, or the commit on the underlying connection fails.  
     *         The error code differs depending on the exact situation.
     */
    public void commit(Xid id, boolean twoPhase) throws XAException {
        try
        {
            internalCommit(id, twoPhase);
        }
        catch (GDSException ge)
        {
            throw new XAException(ge.getXAErrorCode());
        } 
    }

    /**
     * The <code>internalCommit</code> method performs the requested
     * commit and may throw a GDSException to be interpreted by the
     * caller.
     *
     * @param id a <code>Xid</code> value
     * @param twoPhase a <code>boolean</code> value
     * @exception GDSException if an error occurs
     */
    void internalCommit(Xid id, boolean twoPhase) throws XAException, GDSException
    {
        if (log!=null) log.debug("Commit called: " + id);
        isc_tr_handle committingTr = mcf.getTrHandleForXid(id);

        if (committingTr == null)
            throw new FBXAException(
                    "Commit called with unknown transaction",
                    XAException.XAER_NOTA);

        if (committingTr == currentTr)
            throw new FBXAException(
                    "Commit called with current xid",
                    XAException.XAER_PROTO);

        isc_db_handle committingDbHandle = committingTr.getDbHandle();

        try {
            mcf.commit(id);
        } catch (GDSException ge) {
            checkFatalXA(ge, committingDbHandle);
            if (log!=null) log.debug("Fatal error committing, ", ge);
            ge.setXAErrorCode(XAException.XAER_RMERR);
            throw ge;
        }

    }

    /**
     * Dissociates a resource from a global transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end called twice), or the
     *     transaction ID is wrong.
     */
    public void end(Xid id, int flags) throws XAException
    {
        internalEnd(id, flags);
    }

    /**
     * The <code>internalEnd</code> method ends the xid as requested
     * if approprriate and throws a GDSException including the
     * appropriate XA error code and a message if not.  The caller can
     * decode the exception as necessary.
     *
     * @param id a <code>Xid</code> value
     * @param flags an <code>int</code> value
     * @exception GDSException if an error occurs
     */
    void internalEnd(Xid id, int flags) throws XAException
    {
        if (flags != XAResource.TMSUSPEND
            && flags != XAResource.TMSUCCESS
            && flags != XAResource.TMFAIL)
        {
            throw new FBXAException(
                "Invalid flag in end: must be TMSUSPEND, TMSUCCESS, or TMFAIL",
                XAException.XAER_INVAL);
        } 

        if (log!=null) log.debug("End called: " + id);
        isc_tr_handle endingTr = mcf.getTrHandleForXid(id);

        if (endingTr == null)
            throw new FBXAException(
                    "Unrecognized transaction", XAException.XAER_NOTA);

        if (endingTr == currentTr)
            currentTr = null;
        else 
        if (flags == XAResource.TMSUSPEND)
            throw new FBXAException(
                    "You are trying to suspend a transaction " +
                    "that is not the current transaction", 
                    XAException.XAER_INVAL);

        //Otherwise, it is fail or success for a tx that will be committed or
        //rolled back shortly.
    }

    /**
     * Indicates that no further action will be taken on behalf of
     * this transaction (after a heuristic failure).  It is assumed
     * this will be called after a failed commit or rollback.  This
     * should actually never be called since we don't use heuristic tx
     * completion on timeout.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), or the
     *     transaction ID is wrong.
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
     * Retrieve whether this <code>FBManagedConnection</code> uses the 
     * same ResourceManager as <code>res</code>. This method relies on 
     * <code>res</code> being a Firebird implementation of 
     * <code>XAResource</code>.
     *
     * @param res The other <code>XAResource</code> to compare to
     * @return <code>true</code> if <code>res</code> uses the same 
     *         ResourceManager, <code>false</code> otherwise
     */
    public boolean isSameRM(XAResource res) throws javax.transaction.xa.XAException {
        return (res instanceof FBManagedConnection)
            && (mcf == ((FBManagedConnection)res).mcf);
    }

    /**
     * Prepares a transaction to commit.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, or the connection was set to Auto-Commit.
     */
    public int prepare(Xid id) throws javax.transaction.xa.XAException {
        if (log!=null) log.debug("prepare called: " + id);
        isc_tr_handle committingTr = mcf.getTrHandleForXid(id);
        if (committingTr == null) 
            throw new FBXAException(
                    "Prepare called with unknown transaction", 
                    XAException.XAER_INVAL);

        if (committingTr == currentTr) 
            throw new FBXAException(
                    "Prepare called with current xid",
                    XAException.XAER_PROTO);

        isc_db_handle committingDbHandle = committingTr.getDbHandle();

        try
        {
            mcf.prepare(id);
        }
        catch (GDSException ge)
        {
            checkFatalXA(ge, committingDbHandle);
            if (log!=null) log.debug("Exception in prepare", ge);
            throw new FBXAException(XAException.XAER_RMERR, ge);
        }
        return XA_OK;
    }

    private static final String RECOVERY_QUERY = ""
        + "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
        + "FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE = 1";

    
   /**
    * Obtain a list of prepared transaction branches from a resource manager. 
    * The transaction manager calls this method during recovery to obtain the 
    * list of transaction branches that are currently in prepared or 
    * heuristically completed states.  
    *
    * @param flag One of TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS. TMNOFLAGS must 
    *        be used when no other flags are set in flags.
    * @return The resource manager returns zero or more XIDs for the 
    *         transaction branches that are currently in a prepared or 
    *         heuristically completed state. If an error occurs during the 
    *         operation, the resource manager should throw the appropriate 
    *         XAException.
    * @throws XAException An error has occurred. Possible values are 
    *         XAER_RMERR, XAER_RMFAIL, XAER_INVAL, and XAER_PROTO.
    */
    public Xid[] recover(int flag) throws javax.transaction.xa.XAException
    {
        ArrayList xids = new ArrayList();
        Connection conn = null;
        try
        {
            conn = (Connection)getConnection(null, null);

            try
            {

                Statement statement = conn.createStatement();
                ResultSet recoveredRS = statement.executeQuery(RECOVERY_QUERY);
                while (recoveredRS.next())
                {
                    try
                    {
                        long transactionID = recoveredRS.getLong(1);
                        InputStream xidIn = recoveredRS.getBinaryStream(2);
                        FBXid xid = new FBXid(xidIn);
                        xids.add(xid);
                        //what do we do with the Firebird transactionID?
                    }
                    catch (SQLException sqle)
                    { } // end of try-catch
                    catch (ResourceException sqle)
                    { } // end of try-catch

                } // end of while ()
                return (Xid[])xids.toArray(new Xid[xids.size()]);
            }
            finally
            {
                conn.close();
            } // end of finally
        }
        catch (SQLException sqle)
        {
            if (log!=null) log.debug("can't perform query to fetch xids", sqle);
            throw new XAException(XAException.XAER_RMFAIL);
        } // end of try-catch
        catch (ResourceException re)
        {
            if (log!=null) log.debug("can't perform query to fetch xids", re);
            throw new XAException(XAException.XAER_RMFAIL);
        } // end of try-catch
     }

    /**
     * Rolls back the work, assuming it was done on behalf of the specified
     * transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the rollback on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void rollback(Xid id) throws XAException
    {
        try
        {
            internalRollback(id);
        }
        catch (GDSException ge)
        {
            throw new XAException(ge.getXAErrorCode());
        } // end of try-catch
    }

    void internalRollback(Xid id) throws XAException, GDSException
    {
        if (log!=null) log.debug("rollback called: " + id);
        isc_tr_handle committingTr = mcf.getTrHandleForXid(id);
        if (committingTr == null)
        {
            if (log!=null) log.warn("rollback called with unknown transaction: " + id);
            return;
        }

        if (committingTr == currentTr)
            throw new FBXAException(
                    "Rollback called with current xid", XAException.XAER_PROTO);

        isc_db_handle committingDbHandle = committingTr.getDbHandle();

        try
        {
            mcf.rollback(id);
        }
        catch (GDSException ge)
        {
            checkFatalXA(ge, committingDbHandle);
            if (log!=null) log.debug("Exception in rollback", ge);
            ge.setXAErrorCode(XAException.XAER_RMERR);
            throw ge;
        }
    }

    /**
     * Sets the transaction timeout.  This is saved, but the value is not used
     * by the current implementation.
     *
     * @param timeout The timeout to be set in seconds
     */
    public boolean setTransactionTimeout(int timeout) throws javax.transaction.xa.XAException {
        this.timeout = timeout;
        return true;
    }

    /**
     * Associates a JDBC connection with a global transaction.  We assume that
     * end will be called followed by prepare, commit, or rollback.
     * If start is called after end but before commit or rollback, there is no
     * way to distinguish work done by different transactions on the same
     * connection).  If start is called more than once before
     * end, either it's a duplicate transaction ID or illegal transaction ID
     * (since you can't have two transactions associated with one DB
     * connection).
     *
     *
     * @param xid A global transaction identifier to be associated with the 
     *        resource
     * @param flags One of TMNOFLAGS, TMJOIN, or TMRESUME      
     * @throws XAException Occurs when the state was not correct (start 
     *         called twice), the transaction ID is wrong, or the instance 
     *         has already been closed.
     */
    public void start(Xid id, int flags) throws XAException
    {
        try
        {
            internalStart(id, flags);
        }
        catch (GDSException ge)
        {
            throw new XAException(ge.getXAErrorCode());
        } 
    }

    /**
     * Perform the internal processing to start associate a JDBC connection 
     * with a global transaction.
     *
     * @see start
     * @param xid A global transaction identifier to be associated with the 
     *        resource
     * @param flags One of TMNOFLAGS, TMJOIN, or TMRESUME      
     */
    public void internalStart(Xid id, int flags) throws XAException, GDSException {
        if (log!=null) log.debug("start called: " + id);

        if (currentTr != null)
            throw new XAException(XAException.XAER_PROTO);

        findIscTrHandle(id, flags);
    }

    /**
     * The <code>checkFatalXA</code> method checks if the supplied
     * GDSException is fatal and if so destroys the supplied db handle
     * and sends the ConnectionErrorOccurred notification if
     * appropriate.  It is called from methods in the XAResource.  It
     * needs to destroy the db handle itself because it may be
     * different from the current db handle.  This mc should be
     * destroyed only if the tx's db handle and the current db handle
     * are the same.
     *
     * @param ge a <code>GDSException</code> value
     * @param committingDbHandle an <code>isc_db_handle</code> value
     */
    private void checkFatalXA(GDSException ge, isc_db_handle committingDbHandle)
    {
        if (ge.isFatal())
        {
            //all db handles associated with a tx will have the
            //same cri, so we can use ours.
            mcf.destroyDbHandle(committingDbHandle, cri);

            if (currentDbHandle == committingDbHandle)
            {
                //lose the current tx if any
                currentTr = null;
                ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ge);
                notify(connectionErrorOccurredNotifier, ce);

            } // end of if ()

        } // end of if ()
    }

    /**
     * The <code>checkFatal</code> method checks if the supplied
     * GDSException is fatal and sends the ConnectionErrorOccurred
     * notification if it is.  It should be called from every method
     * that does database access except the ones initiated from the
     * XAResource which use checkFatalXA since they may be using a
     * different db handle than the database operations.
     *
     * @param ge a <code>GDSException</code> value
     */
    private void checkFatal(GDSException ge)
    {
        if (ge.isFatal())
        {
            //Since it is fatal we destroy the actual db handle immediately.
            mcf.destroyDbHandle(currentDbHandle, cri);
            //lose the current tx if any
            currentTr = null;
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ge);
            notify(connectionErrorOccurredNotifier, ce);
        } // end of if ()
    }

    //FB public methods. Could be package if packages reorganized.

    /**
     * Retrieve a newly allocated statment handle with the current connection.
     *
     * @return The new statement handle
     * @throws GDSException if a database access error occurs
     */
    public isc_stmt_handle getAllocatedStatement() throws GDSException {
        //Should we test for dbhandle?
        if (currentTr == null) {
            throw new GDSException("No transaction started for allocate statement");
        }
        isc_stmt_handle stmt = mcf.gds.get_new_isc_stmt_handle();
        try
        {
            mcf.gds.isc_dsql_allocate_statement(currentTr.getDbHandle(), stmt);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch
        return stmt;
    }

    /**
     * Retrieve whether this connection is currently involved in a transaction
     *
     * @return <code>true</code> if this connection is currently in a 
     *         transaction, <code>false</code> otherwise.
     */
    public boolean inTransaction() {
        return currentTr != null;
    }

    /**
     * Prepare an SQL string for execution (within the database server) in the 
     * context of a statement handle. 
     *
     * @param stmt The statement handle within which the SQL statement will
     *        be prepared
     * @param sql The SQL statement to be prepared
     * @param describeBind Send bind data to the database server
     * @throws GDSException if a Firebird-specific error occurs
     * @throws SQLException if a database access error occurs
     */
    public void prepareSQL(isc_stmt_handle stmt, String sql, boolean describeBind) throws GDSException, SQLException {
        if (log!=null) log.debug("preparing sql: " + sql);
        //Should we test for dbhandle?

        String localEncoding = cri.getStringProperty(ISCConstants.isc_dpb_local_encoding);
        String mappingPath = cri.getStringProperty(ISCConstants.isc_dpb_mapping_path);
        
        Encoding encoding = EncodingFactory.getEncoding(localEncoding, mappingPath);

        int dialect = ISCConstants.SQL_DIALECT_CURRENT;
        if (cri.hasArgument(ISCConstants.isc_dpb_sql_dialect))
            dialect = cri.getIntProperty(ISCConstants.isc_dpb_sql_dialect);
        
        try
        {
            XSQLDA out = mcf.gds.isc_dsql_prepare(currentTr, stmt, encoding.encodeToCharset(sql) , dialect);
            if (out.sqld != out.sqln) {
                throw new GDSException("Not all columns returned");
            }
            if (describeBind) {
                mcf.gds.isc_dsql_describe_bind(stmt, ISCConstants.SQLDA_VERSION1);
            }
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch
    }

    /**
     * Execute a statement in the database. 
     *
     * @param stmt The handle to the statement to be executed
     * @param sendOutSqlda Determines if the XSQLDA structure should be sent
     *        to the database
     * @throws GDSException if a Firebird-specific error occurs
     */
    public void executeStatement(isc_stmt_handle stmt, boolean sendOutSqlda)
        throws GDSException
    {
        try
        {
            mcf.gds.isc_dsql_execute2(currentTr,
                                      stmt,
                                      ISCConstants.SQLDA_VERSION1,
                                      stmt.getInSqlda(),
                                      (sendOutSqlda) ? stmt.getOutSqlda() : null);

        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch
    }
    
    /**
     * Execute a SQL statement directly with the current connection.
     *
     * @param statement The SQL statement to execute
     * @throws GDSException if a Firebird-specific error occurs
     */
    public void executeImmediate(String statement) throws GDSException {
        try {
            mcf.gds.isc_dsql_exec_immed2(
                getIscDBHandle(), 
                currentTr, 
                statement, 
                3, 
                null, 
                null
            );
        } catch(GDSException ex) {
            checkFatal(ex);
            throw ex;
        }
    }

    /**
     * Fetch data from a statement in the database.
     *
     * @param stmt handle to the statement from which data will be fetched
     * @param fetchSize The number of records to fetch
     * @throws GDSException if a Firebird-specific error occurs
     */
    public void fetch(isc_stmt_handle stmt, int fetchSize)
        throws GDSException
    {
        try
        {
            mcf.gds.isc_dsql_fetch(stmt,
                                   ISCConstants.SQLDA_VERSION1,
                                   stmt.getOutSqlda(),
                                   fetchSize);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }
    
    /**
     * Set the cursor name for a statement.
     *
     * @param stmt handle to statement for which the cursor name will be set
     * @param cursorName the name for the cursor
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public void setCursorName(isc_stmt_handle stmt, String cursorName) 
        throws GDSException
    {
        try {
            mcf.gds.isc_dsql_set_cursor_name(
                stmt, cursorName, 0); // type is reserved for future use
        } catch(GDSException ge) {
            checkFatal(ge);
            throw ge;
        }
    }

    /**
     * Close a statement that is allocated in the database. The statement 
     * can be optionally deallocated.
     *
     * @param stmt handle to the statement to be closed
     * @param deallocate if <code>true</code>, the statement will be 
     *        deallocated, otherwise it will not be deallocated
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public void closeStatement(isc_stmt_handle stmt, boolean deallocate)
        throws GDSException
    {
        try
        {
            mcf.gds.isc_dsql_free_statement(stmt, (deallocate) ? ISCConstants.DSQL_drop: ISCConstants.DSQL_close);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Close this connection with regards to a wrapping 
     * <code>AbstractConnection</code>.
     *
     * @param c The <code>AbstractConnection</code> that is being closed
     */
    public void close(AbstractConnection c) {
        c.setManagedConnection(null);
        connectionHandles.remove(c);
        ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
        ce.setConnectionHandle(c);
        notify(connectionClosedNotifier, ce);
    }

    /**
     * Register a statement with the current transaction. There must be a 
     * currently-active transaction to complete this operation.
     *
     * @param fbStatement handle to the statement to be registered
     */
    public void registerStatement(isc_stmt_handle fbStatement) {
        if (currentTr == null) {
            throw new java.lang.IllegalStateException("registerStatement called with no transaction");
        }

        currentTr.registerStatementWithTransaction(fbStatement);
    }

    /**
     * Open a handle to a new blob within the current transaction with 
     * the given id.
     *
     * @param blob_id The identifier to be given to the blob
     * @param segmented If <code>true</code>, the blob will be segmented,
     *        otherwise is will be streamed
     * @throws GDSException if a Firebird-specific database error occurs
     */
    public isc_blob_handle openBlobHandle(long blob_id, boolean segmented) throws GDSException {
        try
        {
            isc_blob_handle blob = mcf.gds.get_new_isc_blob_handle();
            blob.setBlob_id(blob_id);

            final BlobParameterBuffer blobParameterBuffer = mcf.gds.newBlobParameterBuffer();

            blobParameterBuffer.addArgument(BlobParameterBuffer.type,
                    segmented ? BlobParameterBuffer.type_segmented : BlobParameterBuffer.type_stream);

            mcf.gds.isc_open_blob2(currentDbHandle, currentTr, blob, blobParameterBuffer);

            return blob;
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Create a new blob within the current transaction.
     *
     * @param segmented If <code>true</code> the blob will be segmented,
     *        otherwise it will be streamed
     * @throws GDSException if a Firebird-specific database error occurs
     */
    public isc_blob_handle createBlobHandle(boolean segmented) throws GDSException {
        try
        {
            isc_blob_handle blob = mcf.gds.get_new_isc_blob_handle();

            final BlobParameterBuffer blobParameterBuffer = mcf.gds.newBlobParameterBuffer();

            blobParameterBuffer.addArgument(BlobParameterBuffer.type,
                    segmented ? BlobParameterBuffer.type_segmented : BlobParameterBuffer.type_stream);

            mcf.gds.isc_create_blob2(currentDbHandle, currentTr, blob, blobParameterBuffer);

            return blob;
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Get a segment from a blob.
     *
     * @param blob Handle to the blob from which the segment is to be fetched
     * @param len The maximum length to fetch
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public byte[] getBlobSegment(isc_blob_handle blob, int len) throws GDSException {
        try
        {
            return mcf.gds.isc_get_segment(blob, len);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Close a blob that has been opened within the database.
     *
     * @param blob Handle to the blob to be closed
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public void closeBlob(isc_blob_handle blob) throws GDSException {
        try
        {
            mcf.gds.isc_close_blob(blob);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Write a segment of data to a blob.
     *
     * @param blob handle to the blob to which data will be written
     * @param buf segment of data to be written to the blob
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public void putBlobSegment(isc_blob_handle blob, byte[] buf) throws GDSException {
        try
        {
            mcf.gds.isc_put_segment(blob, buf);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }

    /**
     * Fetch the count information for a statement handle. The count
     * information that is updated includes the counts for update, insert,
     * delete and select, and it is set in the handle itself.
     *
     * @param stmt handle to the statement for which counts will be fetched
     * @throws GDSException if a Firebird-specific database access error occurs
     */
    public void getSqlCounts(isc_stmt_handle stmt) throws GDSException {
        try
        {
            mcf.gds.getSqlCounts(stmt);
        }
        catch (GDSException ge)
        {
            checkFatal(ge);
            throw ge;
        } // end of catch

    }
    
    //for DatabaseMetaData.

    /**
     * Get the name of the database product that we're connected to.
     * 
     * @return The database product name (i.e. Firebird or Interbase)
     */
    public String getDatabaseProductName() {
        /**@todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductName();
    }

    /**
     * Get the version of the the database that we're connected to
     * 
     * @return the database product version
     */
    public String getDatabaseProductVersion() {
        /**@todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductVersion();
    }

    /**
     * Get the major version number of the database that we're connected to.
     *
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        /**@todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductMajorVersion();
    }

    /**
     * Get the minor version number of the database that we're connected to.
     *
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
        /**@todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductMinorVersion();
    }

    /**
     * Get the name of the database that we're connected to.
     *
     * @return The name of the database involved in this connection
     */
    public String getDatabase() {
        return mcf.getDatabase();
    }

    /**
     * Get the database login name of the user that we're connected as.
     *
     * @return The username of the current database user
     */
    public String getUserName()
    {
        return cri.getUser();
    }

    /**
     * Get the transaction isolation level of this connection. The level is
     * one of the static final fields of <code>java.sql.Connection</code> (i.e.
     * <code>TRANSACTION_READ_COMMITTED</code>, 
     * <code>TRANSACTION_READ_UNCOMMITTED</code>,
     * <code>TRANSACTION_REPEATABLE_READ</code>,
     * <code>TRANSACTION_SERIALIZABLE</code>.
     *
     * @see java.sql.Connection
     * @see setTransactionIsolation
     * @return Value representing a transaction isolation level defined
     *         in {@link java.sql.Connection}.
     * @throws ResourceException If the transaction level cannot be retrieved
     */
    public int getTransactionIsolation() throws ResourceException {
        return tpb.getTransactionIsolation();
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
     * @see getTransactionIsolation
     * @param isolation Value representing a transaction isolation level 
     *        defined in {@link java.sql.Connection}.
     * @throws ResourceException If the transaction level cannot be retrieved
     */
    public void setTransactionIsolation(int isolation) throws ResourceException {
        tpb.setTransactionIsolation(isolation);
    }

    /**
     * Get the name of the current transaction isolation level for this
     * connection.
     *
     * @see getTransactionIsolation
     * @see setTransactionIsolationName
     * @return The name of the current transaction isolation level
     * @throws ResourceException If the transaction level cannot be retrieved
     */
    public String getTransactionIsolationName() throws ResourceException {
        return tpb.getTransactionIsolationName();
    }

    /**
     * Set the current transaction isolation level for this connection by name 
     * of the level. The transaction isolation name must be one of the
     * TRANSACTION_* static final fields in {@link org.firebirdsql.jca.FBTpb}.
     *
     * @see getTransactionIsolationName
     * @see FBTpb
     * @param isolation The name of the transaction isolation level to be set
     * @throws ResourceException if the transaction isolation level cannot be
     *         set to the requested level, or if <code>isolation</code> is not
     *         a valid transaction isolation name
     */
    public void setTransactionIsolationName(String isolation) throws ResourceException {
        tpb.setTransactionIsolationName(isolation);
    }

    /**
     * @deprecated you should not use internal transaction isolation levels
     * directrly.
     */
    public int getIscTransactionIsolation() throws ResourceException {
        return tpb.getIscTransactionIsolation();
    }

    /**
     * @deprecated you should not use internal transaction isolation levels
     * directrly.
     */
    public void setIscTransactionIsolation(int isolation) throws ResourceException {
        tpb.setIscTransactionIsolation(isolation);
    }

    /**
     * Set whether this connection is to be readonly 
     *
     * @param readOnly If <code>true</code>, the connection will be set 
     *        read-only, otherwise it will be writable
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

    /**
     * Get the buffer length for blobs for this connection.
     *
     * @return The length of blob buffers 
     */
    public Integer getBlobBufferLength()
    {
        return mcf.getBlobBufferLength();
    }

    /**
     * Get the encoding used for this connection.
     *
     * @return The name of the encoding used
     */
    public String getIscEncoding() {
        try {
            String result = cri.getStringProperty(ISCConstants.isc_dpb_lc_ctype);
            if (result == null) result = "NONE";
            return result;
        } catch(NullPointerException ex) {
            return "NONE";
        }
    }

    /**
     * Get all warnings associated with current connection.
     *
     * @return list of {@link GDSException} instances representing warnings
     * for this database connection.
     */
    public List getWarnings() {
        if (currentDbHandle == null)
            return Collections.EMPTY_LIST;
        else
            return currentDbHandle.getWarnings();
    }

    /**
     * Clear warnings for this database connection.
     */
    public void clearWarnings() {
        if (currentDbHandle != null)
            currentDbHandle.clearWarnings();
    }

    /**
     * Get connection handle for direct Firebird API access
     *
     * @return internal handle for connection
     */
    public isc_db_handle getIscDBHandle() throws GDSException {
        if (currentDbHandle == null) {
            currentDbHandle = mcf.getDbHandle(cri);
        }
        return currentDbHandle;
    }

    /**
     * Get Firebird API handler (sockets/native/embeded/etc)
     * @return handler object for internal API calls
     */
    public GDS getInternalAPIHandler() {
        return mcf.gds;
    }
    
    /**
     * Get information about the current connection parameters.
     * 
     * @return instance of {@link FBConnectionRequestInfo}.
     */
    public FBConnectionRequestInfo getConnectionRequestInfo() {
        return cri;
    }

    //--------------------------------------------------------------------
    //package visibility
    //--------------------------------------------------------------------

    private void findIscTrHandle(Xid xid, int flags)
        throws GDSException, XAException
    {
        try
        {
            currentTr = mcf.getCurrentIscTrHandle(xid, this, flags);
        }
        catch (GDSException ge)
        {
            currentTr = null;
            //All errors are fatal, kill the connection.
            //First check if currentDbHandle is still ok, return if possible
            if (currentDbHandle.isValid())
            {
                try
                {
                    mcf.returnDbHandle(currentDbHandle, cri);
                }
                catch (GDSException ge2)
                {
                    if (log!=null) log.debug("Another exception killing a dead connection", ge2);
                } // end of try-catch
            } // end of if ()
            //Notify the connection manager.
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ge);
            notify(connectionErrorOccurredNotifier, ce);
            ge.setXAErrorCode(XAException.XAER_RMERR);
            throw ge;
        } // end of try-catch


        if (currentTr.getDbHandle() != currentDbHandle)
        {
            try
            {
                mcf.returnDbHandle(currentDbHandle, cri);
            }
            catch (GDSException ge)
            {
                ge.setXAErrorCode(XAException.XAER_RMERR);
                throw ge;
            } // end of try-catch

            currentDbHandle = currentTr.getDbHandle();
        }
    }

    isc_db_handle getIscDBHandle(Set reserved) throws GDSException {
        if (currentDbHandle == null) {
            currentDbHandle = mcf.getDbHandle(cri);
        }
        else if (reserved.contains(currentDbHandle))
        {
            mcf.releaseDbHandle(currentDbHandle, cri);
            currentDbHandle = mcf.getDbHandle(cri);
        } // end of if ()

        return currentDbHandle;
    }

    void notify(CELNotifier notifier, ConnectionEvent ce)
    {
        if (connectionEventListeners.size() == 0)
        {
            return;
        }
        if (connectionEventListeners.size() == 1)
        {
            ConnectionEventListener cel = (ConnectionEventListener)connectionEventListeners.get(0);
            notifier.notify(cel, ce);
            return;
        } // end of if ()
        ArrayList cels = (ArrayList)connectionEventListeners.clone();
        for (Iterator i = cels.iterator(); i.hasNext();)
        {
            notifier.notify((ConnectionEventListener)i.next(), ce);
        } // end of for ()
    }

    interface CELNotifier
    {
        void notify(ConnectionEventListener cel, ConnectionEvent ce);
    }

    static final CELNotifier connectionClosedNotifier = new CELNotifier()
        {
            public void notify(ConnectionEventListener cel, ConnectionEvent ce)
            {
                cel.connectionClosed(ce);
            }
        };

    static final CELNotifier connectionErrorOccurredNotifier = new CELNotifier()
        {
            public void notify(ConnectionEventListener cel, ConnectionEvent ce)
            {
                cel.connectionErrorOccurred(ce);
            }
        };

    static final CELNotifier localTransactionStartedNotifier = new CELNotifier()
        {
            public void notify(ConnectionEventListener cel, ConnectionEvent ce)
            {
                cel.localTransactionStarted(ce);
            }
        };

    static final CELNotifier localTransactionCommittedNotifier = new CELNotifier()
        {
            public void notify(ConnectionEventListener cel, ConnectionEvent ce)
            {
                cel.localTransactionCommitted(ce);
            }
        };

    static final CELNotifier localTransactionRolledbackNotifier = new CELNotifier()
        {
            public void notify(ConnectionEventListener cel, ConnectionEvent ce)
            {
                cel.localTransactionRolledback(ce);
            }
        };


    boolean matches(Subject subj, ConnectionRequestInfo cri)
    {
        try
        {
            return this.cri.equals(getCombinedConnectionRequestInfo(subj, cri));
        }
        catch (ResourceException re)
        {
            return false;
        } // end of try-catch
    }

    public FBTpb getTpb(){
        return tpb;
    }
    
    /*
    public void setTpb(FBTpb tpb) {
    	this.t
    }
    */


    //-----------------------------------------
    //Private methods
    //-----------------------------------------


    private FBConnectionRequestInfo getCombinedConnectionRequestInfo(Subject subject, ConnectionRequestInfo cri) throws ResourceException
    {
        if (cri == null) {
            cri = mcf.getDefaultConnectionRequestInfo();
        }
        try
        {
            FBConnectionRequestInfo fbcri = (FBConnectionRequestInfo)cri;
            if (subject != null)
            {
               //see connector spec, section 8.2.6, contract for ManagedConnectinFactory, option A.
               for (Iterator i = subject.getPrivateCredentials().iterator(); i.hasNext(); )
               {
                  Object cred = i.next();
                  if (cred instanceof PasswordCredential && mcf.equals(((PasswordCredential)cred).getManagedConnectionFactory()))
                  {
                     PasswordCredential pcred = (PasswordCredential)cred;
                     String user = pcred.getUserName();
                     String password = new String(pcred.getPassword());
                     fbcri.setPassword(password);
                     fbcri.setUser(user);
                     break;
                  } // end of if ()
               } // end of for ()
            } // end of if ()

            return fbcri;
        }
        catch (ClassCastException cce)
        {
            throw new FBResourceException("Incorrect ConnectionRequestInfo class supplied");
        } // end of try-catch

    }


}
