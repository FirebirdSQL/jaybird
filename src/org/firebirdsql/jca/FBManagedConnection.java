/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jca;


// imports --------------------------------------




import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.isc_blob_handle;
import org.firebirdsql.gds.isc_db_handle;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.gds.isc_tr_handle;
import org.firebirdsql.gds.SqlInfo;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBStatement;
import org.firebirdsql.logging.Logger;

/**
 *
 *   @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *   @version $ $
 */



/**This class implements the ManagedConnection interface.

/**
 * <p>
 */

public class FBManagedConnection implements ManagedConnection, XAResource {

   private final Logger log = Logger.getLogger(getClass());

    private FBManagedConnectionFactory mcf;

    private ArrayList connectionEventListeners = new ArrayList();

    private ArrayList connectionHandles = new ArrayList();

    private int timeout = 0;

    private FBConnectionRequestInfo cri;


    private isc_tr_handle currentTr;

    private isc_db_handle currentDbHandle;

    private Set tpb;

    FBManagedConnection(final Subject subject, 
                        final ConnectionRequestInfo cri, 
                        final FBManagedConnectionFactory mcf)
        throws ResourceException
    {
        this.mcf = mcf;
        this.cri = getCombinedConnectionRequestInfo(subject, cri);//cri;
        this.tpb = mcf.getTpb(); //getTpb supplies a copy.
        //Make sure we can get a connection to the db.
        mcf.assureDbHandle(this.cri);
    }



    //javax.resource.spi.ManagedConnection implementation

    /**
     Returns an javax.resource.spi.LocalTransaction instance. The LocalTransaction interface
     is used by the container to manage local transactions for a RM instance.
     Returns:
         LocalTransaction instance
     Throws:
         ResourceException - generic exception if operation fails
         NotSupportedException - if the operation is not supported
         ResourceAdapterInternalException - resource adapter internal error condition



    **/

    public LocalTransaction getLocalTransaction()
    {
       return new FBLocalTransaction(this, null);
    }



    /**
     Gets the metadata information for this connection's underlying EIS resource manager instance.
     The ManagedConnectionMetaData interface provides information about the underlying EIS
     instance associated with the ManagedConenction instance.
     Returns:
         ManagedConnectionMetaData instance
     Throws:
         ResourceException - generic exception if operation fails
         NotSupportedException - if the operation is not supported
    **/
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new FBManagedConnectionMetaData(this);
    }

    /**
     Sets the log writer for this ManagedConnection instance.

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnection instance will be printed. Application Server manages the association of
     output stream with the ManagedConnection instance based on the connection pooling
     requirements.

     When a ManagedConnection object is initially created, the default log writer associated with this
     instance is obtained from the ManagedConnectionFactory. An application server can set a log
     writer specific to this ManagedConnection to log/trace this instance using setLogWriter method.

     Parameters:
         out - Character Output stream to be associated
     Throws:
         ResourceException - generic exception if operation fails
         ResourceAdapterInternalException - resource adapter related error condition
    **/
    public void setLogWriter(PrintWriter out){
       //ignore, we are using log4j.
    }


    /**
     Gets the log writer for this ManagedConnection instance.

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnection instance will be printed. ConnectionManager manages the association of
     output stream with the ManagedConnection instance based on the connection pooling
     requirements.

     The Log writer associated with a ManagedConnection instance can be one set as default from the
     ManagedConnectionFactory (that created this connection) or one set specifically for this
     instance by the application server.

     Returns:
         Character ourput stream associated with this Managed- Connection instance
     Throws:
         ResourceException - generic exception if operation fails
    **/

    public PrintWriter getLogWriter() {
       return null;//we are using log4j.
    }

  /**<P> Add an event listener.
   */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }



  /**<P> Remove an event listener.
   */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

  /**Used by the container to change the association of an application-level connection handle with a
     ManagedConneciton instance. The container should find the right ManagedConnection instance
     and call the associateConnection method.

     The resource adapter is required to implement the associateConnection method. The method
     implementation for a ManagedConnection should dissociate the connection handle (passed as a
     parameter) from its currently associated ManagedConnection and associate the new connection
     handle with itself.
     Parameters:
         connection - Application-level connection handle
     Throws:
         ResourceException - Failed to associate the connection handle with this
         ManagedConnection instance
         IllegalStateException - Illegal state for invoking this method
         ResourceAdapterInternalException - Resource adapter internal error condition
*/
    public void associateConnection(java.lang.Object connection) throws ResourceException {
        try {
            ((FBConnection)connection).setManagedConnection(this);
            connectionHandles.add(connection);
        }
        catch (ClassCastException cce) {
            throw new ResourceException("invalid connection supplied to associateConnection: " + cce);
        }
    }
/**


     Application server calls this method to force any cleanup on the ManagedConnection instance.

     The method ManagedConnection.cleanup initiates a cleanup of the any client-specific state as
     maintained by a ManagedConnection instance. The cleanup should invalidate all connection
     handles that had been created using this ManagedConnection instance. Any attempt by an
     application component to use the connection handle after cleanup of the underlying
     ManagedConnection should result in an exception.

     The cleanup of ManagedConnection is always driven by an application server. An application
     server should not invoke ManagedConnection.cleanup when there is an uncompleted transaction
     (associated with a ManagedConnection instance) in progress.

     The invocation of ManagedConnection.cleanup method on an already cleaned-up connection
     should not throw an exception.

     The cleanup of ManagedConnection instance resets its client specific state and prepares the
     connection to be put back in to a connection pool. The cleanup method should not cause resource
     adapter to close the physical pipe and reclaim system resources associated with the physical
     connection.
     Throws:
         ResourceException - generic exception if operation fails
         ResourceAdapterInternalException - resource adapter internal error condition
         IllegalStateException - Illegal state for calling connection cleanup. Example - if a
         localtransaction is in progress that doesn't allow connection cleanup
*/
    public void cleanup() throws ResourceException 
    {
        for (Iterator i = connectionHandles.iterator(); i.hasNext();)
        {
            ((FBConnection)i.next()).setManagedConnection(null);
        } // end of for ()
        connectionHandles.clear();
        /*
        for (int i = connectionHandles.size() - 1; i>= 0; i--) {
            ((FBConnection)connectionHandles.get(i)).close();
        }
        */
    }

/**


     Creates a new connection handle for the underlying physical connection represented by the
     ManagedConnection instance. This connection handle is used by the application code to refer to
     the underlying physical connection. A connection handle is tied to its ManagedConnection
     instance in a resource adapter implementation specific way.

     The ManagedConnection uses the Subject and additional ConnectionRequest Info (which is
     specific to resource adapter and opaque to application server) to set the state of the physical
     connection.

     Parameters:
         Subject - security context as JAAS subject
         cxRequestInfo - ConnectionRequestInfo instance
     Returns:
         generic Object instance representing the connection handle. For CCI, the connection handle
         created by a ManagedConnection instance is of the type javax.resource.cci.Connection.
     Throws:
         ResourceException - generic exception if operation fails
         ResourceAdapterInternalException - resource adapter internal error condition
         SecurityException - security related error condition
         CommException - failed communication with EIS instance
         EISSystemException - internal error condition in EIS instance - used if EIS instance is
         involved in setting state of ManagedConnection
**/
    public Object getConnection(Subject subject, ConnectionRequestInfo cri)
        throws ResourceException 
    {
        if (!matches(subject, cri)) 
        {
            throw new ResourceException("Incompatible subject or ConnectionRequestInfo in getConnection!");        
        } // end of if ()
        
        FBConnection c = new FBConnection(this);
        connectionHandles.add(c);
        return c;
    }


/**


     Destroys the physical connection to the underlying resource manager.

     To manage the size of the connection pool, an application server can explictly call
     ManagedConnection.destroy to destroy a physical connection. A resource adapter should destroy
     all allocated system resources for this ManagedConnection instance when the method destroy is
     called.
     Throws:
         ResourceException - generic exception if operation failed
         IllegalStateException - illegal state for destroying connection
**/
    public void destroy() throws ResourceException {
        if (currentTr != null) {
            throw new IllegalStateException("Can't destroy managed connection  with active transaction");
        }
        if (currentDbHandle != null) {
            try {
               //if (log.isDebugEnabled()) {log.debug("in ManagedConnection.destroy",new Exception());}
                mcf.releaseDbHandle(currentDbHandle, cri);
            }
            catch (GDSException ge) {
                throw new ResourceException("Can't detach from db: " + ge.toString());
            }
            finally {
                currentDbHandle = null;
            }
        }
    }


  /**<P>In both javax.sql.XAConnection and javax.resource.spi.MangagedConnection
   * <P>Return an XA resource to the caller.
   *
   * @return the XAResource
   * @exception SQLException if a database-access error occurs
   */
    public javax.transaction.xa.XAResource getXAResource() throws ResourceException {
       if (log.isDebugEnabled()) 
       {
          log.debug("XAResource requested from FBManagedConnection");
       } // end of if ()
       return this;
    }

    //--------------------------------------------------------------
    //XAResource implementation
    //--------------------------------------------------------------


    /**
     * Commits a transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the commit on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void commit(Xid id, boolean twoPhase) throws XAException {
       if (log.isDebugEnabled()) 
       {
          log.debug("Commit called: " + id);
       } // end of if ()
        if (mcf.lookupXid(id) == null) {
            throw new XAException("commit called with unknown transaction");
        }
        if (mcf.lookupXid(id) == currentTr) {
            throw new XAException("commit called with current xid");
        }
        mcf.commit(id);
    }

    /**
     * Dissociates a resource from a global transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end called twice), or the
     *     transaction ID is wrong.
     */
     //what do we do with flags?????
    public void end(Xid id, int flags) throws javax.transaction.xa.XAException {       if (log.isDebugEnabled()) 
       {
          log.debug("End called: " + id);
       } // end of if ()
        if (currentTr == null) {
            //XAException xae = new XAException("end called with no transaction associated");
           if (log.isDebugEnabled()) 
           {
               log.debug("end called with no transaction associated: " + id + ", flags: " + flags);//, new Exception());
           } // end of if ()

           //throw xae;

           return;
        }
        if (mcf.lookupXid(id) != currentTr) {
            throw new XAException("end called with wrong xid");
        }
        currentTr = null;
    }

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure).  It is assumed this will be
     * called after a failed commit or rollback.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), or the
     *     transaction ID is wrong.
     */
    public void forget(Xid id) throws javax.transaction.xa.XAException {
       if (log.isDebugEnabled()) 
       {
          log.debug("forget called: " + id);
       } // end of if ()
        if (mcf.lookupXid(id) == null) {
            throw new XAException("forget called with unknown transaction");
        }
        if (mcf.lookupXid(id) == currentTr) {
            throw new XAException("forget called with current xid");
        }
        mcf.forgetXid(id);
    }

    /**
     * Gets the transaction timeout.
     */
    public int getTransactionTimeout() throws javax.transaction.xa.XAException {
        return timeout;
    }

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
       if (log.isDebugEnabled()) 
       {
          log.debug("prepare called: " + id);
       } // end of if ()
        if (mcf.lookupXid(id) == null) {
            throw new XAException("prepare called with unknown transaction");
        }
        if (mcf.lookupXid(id) == currentTr) {
            throw new XAException("prepare called with current xid");
        }
        mcf.prepare(id);
        return XA_OK;
    }

    private static final String RECOVERY_QUERY = "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE = 1";

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
            throw new XAException("can't perform query to fetch xids" + sqle);
        } // end of try-catch
        catch (ResourceException re) 
        {
            throw new XAException("can't perform query to fetch xids" + re);
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
    public void rollback(Xid id) throws javax.transaction.xa.XAException {
       if (log.isDebugEnabled()) 
       {
          log.debug("rollback called: " + id);
       } // end of if ()
        if (mcf.lookupXid(id) == null) {
            log.warn("rollback called with unknown transaction: " + id);
            return;
        }
        if (mcf.lookupXid(id) == currentTr) {
            throw new XAException("rollback called with current xid");
        }
        mcf.rollback(id);
    }

    /**
     * Sets the transaction timeout.  This is saved, but the value is not used
     * by the current implementation.
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
     * @throws XAException
     *     Occurs when the state was not correct (start called twice), the
     *     transaction ID is wrong, or the instance has already been closed.
     */
    public void start(Xid id, int flags) throws XAException {
       if (log.isDebugEnabled()) 
       {
          log.debug("start called: " + id);
       } // end of if ()
        if (currentTr != null) {
            throw new XAException("start called with transaction associated");
        }
        findIscTrHandle(id, flags);
    }

    //FB public methods. Could be package if packages reorganized.

    public isc_stmt_handle getAllocatedStatement() throws GDSException {
        //Should we test for dbhandle?
        if (currentTr == null) {
            throw new GDSException("No transaction started for allocate statement");
        }
        isc_stmt_handle stmt = mcf.gds.get_new_isc_stmt_handle();
        mcf.gds.isc_dsql_allocate_statement(currentTr.getDbHandle(), stmt);
        return stmt;
    }

    public boolean inTransaction() {
        return currentTr != null;
    }

    public void prepareSQL(isc_stmt_handle stmt, String sql, boolean describeBind) throws GDSException {
       if (log.isDebugEnabled()) 
       {
          log.debug("preparing sql: " + sql);
       } // end of if ()
        //Should we test for dbhandle?
        XSQLDA out = mcf.gds.isc_dsql_prepare(currentTr, stmt, sql, GDS.SQL_DIALECT_CURRENT);
        if (out.sqld != out.sqln) {
            throw new GDSException("Not all columns returned");
        }
        if (describeBind) {
            mcf.gds.isc_dsql_describe_bind(stmt, GDS.SQLDA_VERSION1);
        }
    }

    public void executeStatement(isc_stmt_handle stmt, boolean sendOutSqlda) throws GDSException {
        mcf.gds.isc_dsql_execute2(currentTr, stmt,
                                 GDS.SQLDA_VERSION1, stmt.getInSqlda(), (sendOutSqlda) ? stmt.getOutSqlda() : null);

    }

    public Object[] fetch(isc_stmt_handle stmt) throws GDSException {
        return mcf.gds.isc_dsql_fetch(stmt, GDS.SQLDA_VERSION1, stmt.getOutSqlda());
    }

    public void closeStatement(isc_stmt_handle stmt, boolean deallocate) throws GDSException {
        mcf.gds.isc_dsql_free_statement(stmt, (deallocate) ? GDS.DSQL_drop: GDS.DSQL_close);
    }

    public void close(FBConnection c) {
        c.setManagedConnection(null);
        connectionHandles.remove(c);
        notify(ConnectionEvent.CONNECTION_CLOSED, c, null);
    }

    public void registerStatement(FBStatement fbStatement) {
        if (currentTr == null) {
            throw new Error("registerStatement called with no transaction");
        }

        mcf.registerStatementWithTransaction(currentTr, fbStatement);
    }

    public isc_blob_handle openBlobHandle(long blob_id) throws GDSException {
        isc_blob_handle blob = mcf.gds.get_new_isc_blob_handle();
        blob.setBlobId(blob_id);
        mcf.gds.isc_open_blob2(currentDbHandle, currentTr, blob, null);//no bpb for now, segmented
        return blob;
    }

    public isc_blob_handle createBlobHandle() throws GDSException {
        isc_blob_handle blob = mcf.gds.get_new_isc_blob_handle();
        mcf.gds.isc_create_blob2(currentDbHandle, currentTr, blob, null);//no bpb for now, segmented
        return blob;
    }

    public byte[] getBlobSegment(isc_blob_handle blob, int len) throws GDSException {
        return mcf.gds.isc_get_segment(blob, len);
    }

    public void closeBlob(isc_blob_handle blob) throws GDSException {
        mcf.gds.isc_close_blob(blob);
    }

    public void putBlobSegment(isc_blob_handle blob, byte[] buf) throws GDSException {
        mcf.gds.isc_put_segment(blob, buf);
    }

    private static byte[] stmtInfo = new byte[]
        {GDS.isc_info_sql_records,
         GDS.isc_info_sql_stmt_type,
         GDS.isc_info_end};
    private static int INFO_SIZE = 128;

    public SqlInfo getSqlInfo(isc_stmt_handle stmt) throws GDSException {
        return new SqlInfo(mcf.gds.isc_dsql_sql_info(stmt, stmtInfo.length, stmtInfo, INFO_SIZE), mcf.gds);
    }


    //for DatabaseMetaData.
    public String getDatabase() {
        return mcf.getDatabase();
    }

    public String getUserName() 
    {
        return cri.getUser();
    }

    public int getTransactionIsolation() {
        if (tpb.contains(new Integer(GDS.isc_tpb_consistency))) {
            return GDS.isc_tpb_consistency;
        }
        if (tpb.contains(new Integer(GDS.isc_tpb_read_committed))) {
            return GDS.isc_tpb_read_committed;
        }
        return GDS.isc_tpb_concurrency; //default.
    }

    public void setTransactionIsolation(int isolation) {
        tpb.remove(new Integer(GDS.isc_tpb_read_committed));
        tpb.remove(new Integer(GDS.isc_tpb_concurrency));
        tpb.remove(new Integer(GDS.isc_tpb_consistency));
        tpb.remove(new Integer(GDS.isc_tpb_rec_version));
        switch (isolation) {
            case GDS.isc_tpb_read_committed: 
                tpb.add(new Integer(GDS.isc_tpb_read_committed));
                tpb.add(new Integer(GDS.isc_tpb_rec_version));
                break;
            case GDS.isc_tpb_concurrency: 
                tpb.add(new Integer(GDS.isc_tpb_concurrency));
                break;
            case GDS.isc_tpb_consistency: 
                tpb.add(new Integer(GDS.isc_tpb_consistency));
                break;
            default: break;
        }
    }

    public void setReadOnly(boolean readOnly) {
        tpb.remove(new Integer(GDS.isc_tpb_read));
        tpb.remove(new Integer(GDS.isc_tpb_write));
        if (readOnly) {
            tpb.add(new Integer(GDS.isc_tpb_read));
        }
        else {
            tpb.add(new Integer(GDS.isc_tpb_write));
        }
    }

    public boolean isReadOnly() {
        return tpb.contains(new Integer(GDS.isc_tpb_read));
    }


    public int getBlobBufferLength()
    {
        return mcf.getBlobBufferLength();
    }
    
    public String getIscEncoding() {
        try {
            String result = cri.getStringProperty(GDS.isc_dpb_lc_ctype);
            if (result == null) result = "NONE";
            return result;
        } catch(NullPointerException ex) {
            return "NONE";
        }
    }


    //--------------------------------------------------------------------
    //package visibility
    //--------------------------------------------------------------------

    void findIscTrHandle(Xid xid, int flags) throws XAException {
        currentTr = mcf.getCurrentIscTrHandle(xid, this, flags);
        if (currentTr.getDbHandle() != currentDbHandle) {
            mcf.returnDbHandle(currentDbHandle, cri);
            currentDbHandle = currentTr.getDbHandle();
        }
    }

    isc_db_handle getIscDBHandle() throws XAException {
        if (currentDbHandle == null) {
            currentDbHandle = mcf.getDbHandle(cri);
        }
        return currentDbHandle;
    }


    void notify(int type, FBConnection c, Exception e) {
       //if (log.isDebugEnabled()) {log.debug("in ManagedConnection.notify",new Exception());}
        ConnectionEvent ce = new ConnectionEvent(this, type, e);
        ce.setConnectionHandle(c);
        //avoid a concurrent modification exception - notification modifies list.
        ArrayList cels = (ArrayList)connectionEventListeners.clone();
        Iterator i = cels.iterator();
        switch (type) {
            case ConnectionEvent.CONNECTION_CLOSED:
                while (i.hasNext()) {
                    ((ConnectionEventListener)i.next()).connectionClosed(ce);
                }
                break;
            case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                while (i.hasNext()) {
                    ((ConnectionEventListener)i.next()).connectionErrorOccurred(ce);
                }
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                while (i.hasNext()) {
                    ((ConnectionEventListener)i.next()).localTransactionStarted(ce);
                }
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                while (i.hasNext()) {
                    ((ConnectionEventListener)i.next()).localTransactionCommitted(ce);
                }
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                while (i.hasNext()) {
                    ((ConnectionEventListener)i.next()).localTransactionRolledback(ce);
                }
                break;
            default:
//                throw new
                break;
        }
    }


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

    Set getTpb() {
        return tpb;
    }


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
            throw new ResourceException("Incorrect ConnectionRequestInfo class supplied");
        } // end of try-catch

    }

/*
    public static class SqlInfo {
        private int statementType;
        private int insertCount;
        private int updateCount;
        private int deleteCount;
        private int selectCount; //????

        SqlInfo(byte[] buffer, GDS gds) {
            int pos = 0;
            int length;
            int type;
            while ((type = buffer[pos++]) != GDS.isc_info_end) {
                length = gds.isc_vax_integer(buffer, pos, 2);
                pos += 2;
                switch (type) {
                    case GDS.isc_info_sql_records:
                        int l;
                        int t;
                        while ((t = buffer[pos++]) != GDS.isc_info_end) {
                            l = gds.isc_vax_integer(buffer, pos, 2);
                            pos += 2;
                            switch (t) {
                                case GDS.isc_info_req_insert_count:
                                    insertCount = gds.isc_vax_integer(buffer, pos, l);
                                    break;
                                case GDS.isc_info_req_update_count:
                                    updateCount = gds.isc_vax_integer(buffer, pos, l);
                                    break;
                                case GDS.isc_info_req_delete_count:
                                    deleteCount = gds.isc_vax_integer(buffer, pos, l);
                                    break;
                                case GDS.isc_info_req_select_count:
                                    selectCount = gds.isc_vax_integer(buffer, pos, l);
                                    break;
                                default:
                                    break;
                            }
                            pos += l;
                        }
                        break;
                    case GDS.isc_info_sql_stmt_type:
                        statementType = gds.isc_vax_integer(buffer, pos, length);
                        pos += length;
                        break;
                    default:
                        pos += length;
                        break;
                }
            }
        }

        public int getStatementType() {
            return statementType;
        }

        public int getInsertCount() {
            return insertCount;
        }

        public int getUpdateCount() {
            return updateCount;
        }

        public int getDeleteCount() {
            return deleteCount;
        }

        public int getSelectCount() {
            return selectCount;
        }
    }

*/
	 
}
