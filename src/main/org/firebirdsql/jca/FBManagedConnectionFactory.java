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

import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.gds.isc_db_handle;
import org.firebirdsql.gds.isc_tr_handle;
import org.firebirdsql.jdbc.FBConnectionHelper;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FBStatement;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * FBManagedConnectionFactory implements the jca ManagedConnectionFactory 
 * interface and also many of the internal functions of ManagedConnection.
 * This nonstandard behavior is required due to firebird requiring
 * all work done in a transaction to be done over one connection.
 * To support xa semantics, the correct db handle must be located whenever 
 * a ManagedConnection is associated with an xid.
 *
 * WARNING: this adapter will probably not work properly in an environment
 * where ManagedConnectionFactory is serialized and deserialized, and the 
 * deserialized copy is expected to function as anything other than a key.
 *
 * @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $ $
 *
 * @todo add support for separate specification of host/port/filename.
 */


public class FBManagedConnectionFactory 
    implements  ManagedConnectionFactory, Serializable
{

    /**
     * Describe constant <code>MAX_BLOB_BUFFER_LENGTH</code> here.
     * @todo  Claudio suggests this should be 1024*64 -1, we should find out
     *  I thought this was the largest value I could make work, but I didn't 
     *  write down my experiments.
     */
    private final static int MAX_BLOB_BUFFER_LENGTH = 1024 * 32 - 1;
    private final static int MIN_BLOB_BUFFER_LENGTH = 1024;

    /**
     * The <code>mcfInstances</code> weak hash map is used in deserialization
     * to find the correct instance of a mcf.
     *
     */
    private final static Map mcfInstances = new WeakHashMap();

    //These properties give the mcf its identity
    //should add handling for host/port/file separately.
    private String dbAlias;

    private FBConnectionRequestInfo defaultCri;

    private final FBTpb tpb = new FBTpb();


    //must be less than 1024 * 32: 1-24 * 32 -  is ok.
    private int blobBufferLength = 1024 * 16;

    //These hold non-serializable stuff.
    private transient final Logger log = LoggerFactory.getLogger(getClass(),true);

    transient final GDS gds = GDSFactory.newGDS();

    /**
     * The <code>criToFreeDbHandlesMap</code> maps cri to lists of physical
     * connections that are not currently used by a managed connection.
     *
     */
    private transient final Map criToFreeDbHandlesMap = new HashMap();

    /**
     * The <code>waitingToClose</code> set holds physical connections that 
     * should be closed as soon as all transactions on them are complete.
     *
     */
    private transient final Set waitingToClose = Collections.synchronizedSet(new HashSet());

    //Maps supplied XID to internal transaction handle.
    //a concurrent reader map would be better
    private transient final Map xidMap = Collections.synchronizedMap(new HashMap());

    //Maps transaction handle to list of statements with resultsets.
    private transient final Map TransactionStatementMap = new HashMap();

    private transient final Object startLock = new Object();
    private transient boolean started = false;

    private volatile int hashCode = 0;

    public FBManagedConnectionFactory() {
        defaultCri = FBConnectionHelper.getDefaultCri();
    }  //Default constructor.

    //rar properties


    public void setDatabase(String database) 
    {
        checkNotStarted();
        hashCode = 0;
        this.dbAlias = database;
    }

    public String getDatabase() {
        return dbAlias;
    }

    public void setConnectionRequestInfo(FBConnectionRequestInfo cri) {
        checkNotStarted();
        hashCode = 0;
        this.defaultCri = new FBConnectionRequestInfo(cri);
    }


    public FBConnectionRequestInfo getDefaultConnectionRequestInfo() {
        return new FBConnectionRequestInfo(defaultCri);
    }

    public void setUserName(String userName)
    {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setUser(userName);
    }

    public String getUserName()
    {
        return defaultCri.getUser();
    }

    public void setPassword(String password)
    {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setPassword(password);
    }

    public String getPassword()
    {
        return defaultCri.getPassword();
    }

    public void setTpb(FBTpb tpb)
    {
        checkNotStarted();
        hashCode = 0;
        this.tpb.setTpb(tpb);
    }


    public FBTpb getTpb() {
        return new FBTpb(tpb);
    }

    public void setTransactionIsolation(Integer level) throws ResourceException
    {
        checkNotStarted();
        hashCode = 0;
        if (level == null) 
        {
            throw new FBResourceException("You must supply a isolation level");
        } // end of if ()
        else
        {
            tpb.setTransactionIsolation(level.intValue());
        } // end of else
    }

    public Integer getTransactionIsolation() throws ResourceException
    {
        return new Integer(tpb.getTransactionIsolation());
    }

    public void setTransactionIsolationName(String level) throws ResourceException
    {
        checkNotStarted();
        hashCode = 0;
        tpb.setTransactionIsolationName(level);
    }

    public String getTransactionIsolationName() throws ResourceException
    {
        return tpb.getTransactionIsolationName();
    }
    
    public void setEncoding(String encoding) {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setProperty(GDS.isc_dpb_lc_ctype, encoding);
    }
    
    public String getEncoding() {
        String result = defaultCri.getStringProperty(GDS.isc_dpb_lc_ctype);
        if (result == null)
            result = "NONE";
        return result;
    }

    /**
     * Get the BlobBufferLength value.
     * @return the BlobBufferLength value.
     */
    public Integer getBlobBufferLength()
    {
        return new Integer(blobBufferLength);
    }

    /**
     * Set the BlobBufferLength value.
     * @param newBlobBufferLength The new BlobBufferLength value.
     */
    public void setBlobBufferLength(final Integer blobBufferLengthWrapper)
    {
        checkNotStarted();
        hashCode = 0;
        int blobBufferLength = blobBufferLengthWrapper.intValue();
        if (blobBufferLength > MAX_BLOB_BUFFER_LENGTH) 
        {
            this.blobBufferLength = MAX_BLOB_BUFFER_LENGTH;
            if (log!=null) log.warn("Supplied blob buffer length greater than maximum of " + MAX_BLOB_BUFFER_LENGTH);
        } // end of if ()
        else if (blobBufferLength < MIN_BLOB_BUFFER_LENGTH ) 
        {
            this.blobBufferLength = MIN_BLOB_BUFFER_LENGTH;
            if (log!=null) log.warn("Supplied blob buffer length less than minimum of " + MIN_BLOB_BUFFER_LENGTH);
        } // end of if ()
        else
        {
            this.blobBufferLength = blobBufferLength;
        } // end of else
    }


    public int hashCode()
    {
        if (hashCode != 0) 
        {
            return hashCode;
        } // end of if ()
        int result = 17;
        result = 37 * result + ((dbAlias == null)? 0: dbAlias.hashCode());
        result = 37 * result + defaultCri.hashCode();
        result = 37 * result + tpb.hashCode();
        result = 37 * result + blobBufferLength;
        hashCode = result;
        return hashCode;
    }
    
    public boolean equals(Object other)
    {
        if (other == this) 
        {
            return true;
        } // end of if ()
        if (!(other instanceof FBManagedConnectionFactory)) 
        {
            return false;
        } // end of if ()
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory)other;
        return
            (dbAlias == null ? mcf.dbAlias == null : dbAlias.equals(mcf.dbAlias))
            && (defaultCri.equals(mcf.defaultCri))
            && (tpb.equals(mcf.tpb))
            && (blobBufferLength == mcf.blobBufferLength);
    }

    /**
     * The <code>createConnectionFactory</code> method creates a DataSource
     * using the supplied ConnectionManager..
     *
     * @param cxManager a <code>ConnectionManager</code> value
     * @return a <code>java.lang.Object</code> value
     * @exception ResourceException if an error occurs
     */
    public java.lang.Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        start();
        return new FBDataSource(this, cxManager);
    }


    /**
     * The <code>createConnectionFactory</code> method creates a DataSource
     * with a default stand alone ConnectionManager.  Ours can implement pooling.
     *
     * @return a <code>java.lang.Object</code> value
     * @exception ResourceException if an error occurs
     */
    public java.lang.Object createConnectionFactory() throws ResourceException {
        start();
        return new FBDataSource(this, new FBStandAloneConnectionManager());
    }



/**
     Creates a new physical connection to the underlying EIS resource manager,

     ManagedConnectionFactory uses the security information (passed as Subject) and additional
     ConnectionRequestInfo (which is specific to ResourceAdapter and opaque to application server)
     to create this new connection.
     Parameters:
         Subject - Caller's security information
         cxRequestInfo - Additional resource adapter specific connection request information
     Returns:
         ManagedConnection instance
     Throws:
         ResourceException - generic exception
         SecurityException - security related error
         ResourceAllocationException - failed to allocate system resources for connection
         request
         ResourceAdapterInternalException - resource adapter related error condition
         EISSystemException - internal error condition in EIS instance

**/
    public ManagedConnection createManagedConnection(Subject subject,
                                                     ConnectionRequestInfo cri)
        throws ResourceException 
    {
        start();
        return new FBManagedConnection(subject, cri, this);
    }




/**
     Returns a matched connection from the candidate set of connections.

     ManagedConnectionFactory uses the security info (as in Subject) and information provided
     through ConnectionRequestInfo and additional Resource Adapter specific criteria to do
     matching. Note that criteria used for matching is specific to a resource adapter and is not
     prescribed by the Connector specification.

     This method returns a ManagedConnection instance that is the best match for handling the
     connection allocation request.

     Parameters:
         connectionSet - candidate connection set
         Subject - caller's security information
         cxRequestInfo - additional resource adapter specific connection request information
     Returns:
         ManagedConnection if resource adapter finds an acceptable match otherwise null
     Throws:
         ResourceException - generic exception
         SecurityException - security related error
         ResourceAdapterInternalException - resource adapter related error condition
         NotSupportedException - if operation is not supported

**/

    public ManagedConnection matchManagedConnections(java.util.Set connectionSet,
                                                 javax.security.auth.Subject subject,
                                                 ConnectionRequestInfo cxRequestInfo)
                                          throws ResourceException {
    Iterator i = connectionSet.iterator();
    while (i.hasNext()) {
        FBManagedConnection mc = (FBManagedConnection)i.next();
            if (mc.matches(subject, (FBConnectionRequestInfo)cxRequestInfo)) {
                return mc;
            }
    }
    return null;
    }




/**
     Set the log writer for this ManagedConnectionFactory instance.

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnectionfactory instance will be printed.

     ApplicationServer manages the association of output stream with the
     ManagedConnectionFactory. When a ManagedConnectionFactory object is created the log
     writer is initially null, in other words, logging is disabled. Once a log writer is associated with a
     ManagedConnectionFactory, logging and tracing for ManagedConnectionFactory instance is
     enabled.

     The ManagedConnection instances created by ManagedConnectionFactory "inherits" the log
     writer, which can be overridden by ApplicationServer using ManagedConnection.setLogWriter
     to set ManagedConnection specific logging and tracing.
     Parameters:
         out - PrintWriter - an out stream for error logging and tracing
     Throws:
         ResourceException - generic exception
         ResourceAdapterInternalException - resource adapter related error condition

**/

    public void setLogWriter(PrintWriter out) throws ResourceException {
       //ignore - we're using log4j
    }



/**
     Get the log writer for this ManagedConnectionFactory instance.

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnectionFactory instance will be printed

     ApplicationServer manages the association of output stream with the
     ManagedConnectionFactory. When a ManagedConnectionFactory object is created the log
     writer is initially null, in other words, logging is disabled.
     Returns:
         PrintWriter
     Throws:
         ResourceException - generic exception

**/
    public PrintWriter getLogWriter() {
       return null;//we're using log4j
    }


    isc_tr_handle getTrHandleForXid(Xid xid)
    {
        return (isc_tr_handle)xidMap.get(xid);
    }


    isc_tr_handle getCurrentIscTrHandle(Xid xid, FBManagedConnection mc, int flags) 
        throws XAException 
    {
        isc_tr_handle tr = getTrHandleForXid(xid);
        if (tr == null) {
            if (flags != XAResource.TMNOFLAGS) {
                throw new XAException("Transaction flags wrong, this xid new for this rm");
            }
            //new xid for us
            try 
            {
                isc_db_handle db = mc.getIscDBHandle(waitingToClose);
                tr = gds.get_new_isc_tr_handle();
                gds.isc_start_transaction(tr, db, mc.getTpb());
            }
            catch (GDSException ge) {
                throw new XAException(ge.getMessage());
            }
            xidMap.put(xid, tr);
        }
        else {
            if (flags != XAResource.TMJOIN && flags != XAResource.TMRESUME) {
                throw new XAException("Transaction flags wrong, this xid already known");
            }
        }
        return tr;
    }


    isc_db_handle getDbHandle(FBConnectionRequestInfo cri) throws XAException 
    {
        try 
        {
            try 
            {
                LinkedList freeDbHandles = null;
                synchronized (criToFreeDbHandlesMap)
                {
                    freeDbHandles = (LinkedList)criToFreeDbHandlesMap.get(cri);
                }
                if (freeDbHandles != null) 
                {
                    isc_db_handle db = null;
                    synchronized (freeDbHandles) 
                    {
                        db = (isc_db_handle)freeDbHandles.removeLast();
                    }
                    return db;
                } // end of if ()
                return createDbHandle(cri);
            } 
            catch (NoSuchElementException e) 
            {
                return createDbHandle(cri);
            }
        }
        catch (GDSException ge)
        {
            if (log!=null) log.error("GDS Exception in getDbHandle", ge);
            throw new XAException(ge.getMessage());
        } // end of try-catch
    }

    isc_db_handle createDbHandle(FBConnectionRequestInfo cri) throws GDSException
    {
        isc_db_handle db = gds.get_new_isc_db_handle();
        gds.isc_attach_database(dbAlias, db, cri.getDpb());
        return db;
    }

    void returnDbHandle(isc_db_handle db, FBConnectionRequestInfo cri) throws GDSException
    {
        if (db == null) 
        {
            return;
        } // end of if ()
        
        if (waitingToClose.contains(db))
        {
            releaseDbHandle(db, cri);
        }
        else
        {
            LinkedList freeDbHandles = null;
            synchronized(criToFreeDbHandlesMap)
            {
                freeDbHandles = (LinkedList)criToFreeDbHandlesMap.get(cri);
                if (freeDbHandles == null) 
                {
                    freeDbHandles = new LinkedList();
                    criToFreeDbHandlesMap.put(cri, freeDbHandles);
                } // end of if ()
            }
            synchronized(freeDbHandles)
            {
                //This is slow, but there should be very few freeDbHandles.
                if (!freeDbHandles.contains(db)) 
                {
                    freeDbHandles.addLast(db);
                } // end of if ()
            }
        }
    }

    void releaseDbHandle(isc_db_handle db, FBConnectionRequestInfo cri) 
        throws GDSException 
    {
        if (db == null) 
        {
            throw new IllegalArgumentException("Attempt to release a null db handle!");
        } // end of if ()
        
        synchronized (db)
        {
            LinkedList freeDbHandles = null;
            synchronized(criToFreeDbHandlesMap)
            {
                freeDbHandles = (LinkedList)criToFreeDbHandlesMap.get(cri);
            }
            if (freeDbHandles != null) 
            {
                synchronized(freeDbHandles)
                {
                    freeDbHandles.remove(db); 
                } // end of if ()
            }
            if (db.hasTransactions())
            {
                synchronized (waitingToClose)
                {
                    //double synchronization, but saves some ugly synch wrappers.
                    if (!waitingToClose.contains(db)) 
                    {
                        waitingToClose.add(db);
                    } // end of if ()
                }
            }
            else
            {
                waitingToClose.remove(db);
                gds.isc_detach_database(db);
            }
        }
    }




    void commit(Xid xid) throws XAException {
        isc_tr_handle tr = getTrHandleForXid(xid);
        forgetResultSets(tr);
        try {
            gds.isc_commit_transaction(tr);
        }
        catch (GDSException ge) {
            throw new XAException(ge.getMessage());
        }
        finally {
            xidMap.remove(xid);
        }
    }

    void prepare(Xid xid) throws XAException {
        try {
            FBXid fbxid;
            if (xid instanceof FBXid) {
                fbxid = (FBXid)xid;
            }
            else {
                fbxid = new FBXid(xid);
            }
            gds.isc_prepare_transaction2(getTrHandleForXid(xid), fbxid.toBytes());
        }
        catch (GDSException ge) {
            if (log!=null) log.warn("error in prepare", ge);
            xidMap.remove(xid);
            throw new XAException(ge.getMessage());
        }
    }

    void rollback(Xid xid) throws XAException {
        isc_tr_handle tr = getTrHandleForXid(xid);
        forgetResultSets(tr);
        try {
            gds.isc_rollback_transaction(tr);
        }
        catch (GDSException ge) {
            throw new XAException(ge.getMessage());
        }
        finally {
            xidMap.remove(xid);
        }
    }


    void registerStatementWithTransaction(isc_tr_handle tr, FBStatement stmt) {
        ArrayList stmts = null;
        synchronized (tr) {
            stmts = (ArrayList)TransactionStatementMap.get(tr);
            if (stmts == null) {
                stmts = new ArrayList();
                TransactionStatementMap.put(tr, stmts);
            }
        }
        stmts.add(stmt);
    }

    private void forgetResultSets(isc_tr_handle tr) {
        //shouldn't need synchronization, only called by rollback and commit- then we're done
        //transaction/thread should also help.
        ArrayList stmts = (ArrayList)TransactionStatementMap.get(tr);
        if (stmts != null) {
            Iterator i = stmts.iterator();
            while (i.hasNext()) {
                ((FBStatement)i.next()).forgetResultSet();
            }
            stmts.clear();
        }
        TransactionStatementMap.remove(tr);
    }

    //Serialization support
    private Object readResolve() throws ObjectStreamException
    {
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory)mcfInstances.get(this);
        if (mcf != null) 
        {
            return mcf;
        } // end of if ()
        mcf = new FBManagedConnectionFactory();
        mcf.setDatabase(this.getDatabase());
        mcf.setConnectionRequestInfo(this.getDefaultConnectionRequestInfo());
        mcf.setTpb(this.getTpb());
        mcf.setBlobBufferLength(this.getBlobBufferLength());
        return mcf;
    }

    /**
     * The <code>canonicalize</code> method is used in FBDriver to reuse
     * previous fbmcf instances if they have been create.  It should
     * really be package access level
     *
     * @return a <code>FBManagedConnectionFactory</code> value
     */
    public FBManagedConnectionFactory canonicalize()
    {
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory)mcfInstances.get(this);
        if (mcf != null) 
        {
            return mcf;
        } // end of if ()
        return this;
    }

    private void start()
    {
        synchronized (startLock)
        {
            if (!started) 
            {
                synchronized (mcfInstances)
                {
                    mcfInstances.put(this, this);
                }
                started = true;
            } // end of if ()
        }
    }

    private void checkNotStarted() throws IllegalStateException
    {
        synchronized (startLock)
        {
            if (started) 
            {
                throw new IllegalStateException("Operation not permitted after ManagedConnectionFactory in use");
            } // end of if ()
        }
    } 

}





