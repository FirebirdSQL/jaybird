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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * FBManagedConnectionFactory implements the jca ManagedConnectionFactory
 * interface and also many of the internal functions of ManagedConnection. This
 * nonstandard behavior is required due to firebird requiring all work done in a
 * transaction to be done over one connection. To support xa semantics, the
 * correct db handle must be located whenever a ManagedConnection is associated
 * with an xid.
 * 
 * WARNING: this adapter will probably not work properly in an environment where
 * ManagedConnectionFactory is serialized and deserialized, and the deserialized
 * copy is expected to function as anything other than a key.
 * 
 * @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @version 1.0
 * 
 * @todo add support for separate specification of host/port/filename.
 */

public class FBManagedConnectionFactory implements ManagedConnectionFactory,
        Serializable {

    /**
     * @todo Claudio suggests this should be 1024*64 -1, we should find out I
     *       thought this was the largest value I could make work, but I didn't
     *       write down my experiments.
     */
    public final static int MAX_BLOB_BUFFER_LENGTH = 1024 * 32 - 1;

    public final static int MIN_BLOB_BUFFER_LENGTH = 1024;

    /**
     * The <code>mcfInstances</code> weak hash map is used in deserialization
     * to find the correct instance of a mcf.
     * 
     */
    private final static Map mcfInstances = new WeakHashMap();

    // These properties give the mcf its identity
    // should add handling for host/port/file separately.
    private String dbAlias;

    private FBConnectionRequestInfo defaultCri;
    private ConnectionManager defaultCm;

    private FBTpb tpb;

    // must be less than 1024 * 32: 1-24 * 32 - is ok.
    private int blobBufferLength = FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE;

    // These hold non-serializable stuff.
    private transient final static Logger log = LoggerFactory.getLogger(
        FBManagedConnectionFactory.class, false);

    GDS gds;

    private GDSType type;

    // Maps supplied XID to internal transaction handle.
    // a concurrent reader map would be better
    private transient final Map xidMap = Collections
            .synchronizedMap(new HashMap());

    private transient final Object startLock = new Object();

    private transient boolean started = false;

    private volatile int hashCode = 0;

    /**
     * Create a new pure-Java FBManagedConnectionFactory.
     */
    public FBManagedConnectionFactory() {
        this(((AbstractGDS)GDSFactory.getDefaultGDS()).getType());
    }

    /**
     * Create a new FBManagedConnectionFactory based around the given GDSType.
     * 
     * @param GDSType
     *            The GDS implementation to use
     */
    public FBManagedConnectionFactory(GDSType type) {
        this.type = type;
        gds = GDSFactory.getGDSForType(type);
        defaultCri = FBConnectionHelper.getDefaultCri(gds);
        defaultCm = new FBStandAloneConnectionManager();
        tpb = new FBTpb(FBTpbMapper.getDefaultMapper(gds));
    }

    public GDS getGDS() {
        return gds;
    }
    
    /**
     * Get the GDS implementation type around which this factory is based.
     * 
     * @return The GDS implementation type
     */
    public GDSType getType() {
        return this.type;
    }

    // rar properties

    /**
     * Set the name of the database to which managed connections will be
     * created.
     * 
     * @param database
     *            The name of the database to which connections will be created
     */
    public void setDatabase(String database) {
        checkNotStarted();
        hashCode = 0;
        this.dbAlias = database;
    }

    /**
     * Get the name of the database to which managed connections will be
     * created.
     * 
     * @return The name of the target database
     */
    public String getDatabase() {
        return dbAlias;
    }

    /**
     * Set the connection information for creating new connections.
     * 
     * @param cri
     *            A {@link FBConnectionRequestInfo}instance that contains the
     *            parameters to be used for creating new connections
     */
    public void setConnectionRequestInfo(FBConnectionRequestInfo cri) {
        checkNotStarted();
        hashCode = 0;
        this.defaultCri = cri.deepCopy();
    }

    /**
     * Returns a <b>copy </b> of the connection parameters used for creating new
     * connections. To update the connections parameters, use
     * {@link #setConnectionRequestInfo}or one of the <code>setXXX</code>
     * methods.
     * 
     * @return A {@link FBConnectionRequestInfo}instance that represents the
     *         parameters used for creating new connections
     */
    public FBConnectionRequestInfo getDefaultConnectionRequestInfo() {
        return defaultCri.deepCopy();
    }

    /**
     * Set the username to be used for creating new connections.
     * 
     * @param userName
     *            The username for new connections
     */
    public void setUserName(String userName) {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setUser(userName);
    }

    /**
     * Get the username that is to be used for creating new connections.
     * 
     * @return The username used for creating connections
     */
    public String getUserName() {
        return defaultCri.getUser();
    }

    /**
     * Set the password that is to be used for creating new connections.
     * 
     * @param password
     *            The password to be used
     */
    public void setPassword(String password) {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setPassword(password);
    }

    /**
     * Get the password that is to be used for creating new connections.
     * 
     * @return The password used for creating connections
     */
    public String getPassword() {
        return defaultCri.getPassword();
    }

    /**
     * Set the {@link FBTpb Transaction Parameters Block}instance to be used to
     * determine transaction parameters for new connections.
     * 
     * @param tpb
     *            <code>FBTpb</code> instance that sets transaction parameters
     */
    public void setTpb(FBTpb tpb) {
        checkNotStarted();
        hashCode = 0;
        this.tpb.setTpb(tpb);
    }

    /**
     * Get the {@link FBTpb Transaction Parameters Block}instance that is used
     * to determine transaction parameters for new connections.
     * 
     * @return <code>FBTpb</code> instance that sets transaction parameters
     */
    public FBTpb getTpb() {
        return new FBTpb(tpb);
    }

    /**
     * Set the {@link FBTpbMapper}instance that is used to map JDBC transaction
     * isolation levels to a Firebird Transaction Parameter Block (TPB).
     * 
     * @param mapper
     *            The {@link FBTpbMapper}instance to be used
     * @throws ResourceException
     *             if the TpbMapper cannot be set
     */
    public void setTpbMapper(FBTpbMapper mapper) throws FBResourceException {
        this.tpb.setMapper(mapper);
    }

    /**
     * Set the transaction isolation level to be used for new connections. The
     * isolatin level is one of the <code>TRANSACTION_*</code> constants in
     * the <code>java.sql.Connection</code> interface.
     * <code>TRANSACTION_NONE</code> cannot be used.
     * 
     * @param level
     *            The transaction isolation level to be set
     * @throws ResourceException
     *             if the transaction level cannot be set to the given level, or
     *             the given level is not valid
     */
    public void setTransactionIsolation(Integer level) throws ResourceException {
        checkNotStarted();
        hashCode = 0;
        if (level == null)
            throw new FBResourceException("You must supply a isolation level");
        else
            tpb.setTransactionIsolation(level.intValue());
    }

    /**
     * Get the current transaction isolation level that is used for creating new
     * connections. The level will have the int value of one of the
     * <code>TRANSACTION_*</code> constants in the
     * <code>java.sql.Connection</code> interface.
     * 
     * @return The current transaction isolation level
     * @throws ResourceException
     *             if the current transaction isolation level cannot be
     *             retrieved
     */
    public Integer getTransactionIsolation() throws ResourceException {
        return new Integer(tpb.getTransactionIsolation());
    }

    /**
     * Set the current transaction isolation level for new connections by name.
     * The name should be equal to the name of one of the
     * <code>TRANSACTION_*</code> static final fields of
     * <code>java.sql.Connection</code>, other than
     * <code>TRANSACTION_NONE</code>. These names are also defined as static
     * final fields in {@link FBTpb}.
     * 
     * @param level
     *            The name of the transaction level to be set
     * @throws ResourceException
     *             if the transaction level cannot be set
     */
    public void setTransactionIsolationName(String level)
            throws ResourceException {
        checkNotStarted();
        hashCode = 0;
        tpb.setTransactionIsolationName(level);
    }

    /**
     * Get the name of the transaction isolation level that is currently used
     * for creating new connections. The name will have the same name as one of
     * the <code>TRANSACTION_*</code> static final fields of
     * <code>java.sql.Connection</code>.
     * 
     * @return The name of the current transaction isolation level
     * @throws ResourceException
     *             if the current isolation level cannot be retrieved
     */
    public String getTransactionIsolationName() throws ResourceException {
        return tpb.getTransactionIsolationName();
    }

    /**
     * Set the default encoding that is to be used for new connections.
     * 
     * @param encoding
     *            The name of the encoding to be used
     */
    public void setEncoding(String encoding) {
        checkNotStarted();
        hashCode = 0;
        defaultCri.setProperty(ISCConstants.isc_dpb_lc_ctype, encoding);

        String localEncoding = defaultCri
                .getStringProperty(ISCConstants.isc_dpb_local_encoding);
        if (localEncoding == null) {
            localEncoding = FBConnectionHelper.getJavaEncoding(encoding);
            defaultCri.setProperty(ISCConstants.isc_dpb_local_encoding,
                localEncoding);
        }
    }

    /**
     * Get the name of the default encoding that is used for new connections.
     * 
     * @return The name of the current default encoding
     */
    public String getEncoding() {
        String result = defaultCri
                .getStringProperty(ISCConstants.isc_dpb_lc_ctype);
        if (result == null) result = "NONE";
        return result;
    }

    /**
     * Set the name of the local encoding that is to be used for new
     * connections.
     * 
     * @param encoding
     *            The name of the local encoding to be used
     */
    public void setLocalEncoding(String localEncoding) {
        checkNotStarted();
        hashCode = 0;

        defaultCri.setProperty(ISCConstants.isc_dpb_local_encoding,
            localEncoding);
        String iscEncoding = defaultCri
                .getStringProperty(ISCConstants.isc_dpb_lc_ctype);
        if (iscEncoding == null) {
            iscEncoding = FBConnectionHelper.getIscEncoding(localEncoding);
            defaultCri.setProperty(ISCConstants.isc_dpb_lc_ctype, iscEncoding);
        }
    }

    /**
     * Get the name of the local encoding that is to be used for new
     * connections.
     * 
     * @return The name of the current local encoding
     */
    public String getLocalEncoding() {
        return defaultCri.getStringProperty(ISCConstants.isc_dpb_local_encoding);
    }

    /**
     * Get the BlobBufferLength value.
     * 
     * @return the BlobBufferLength value.
     */
    public Integer getBlobBufferLength() {
        return new Integer(blobBufferLength);
    }

    /**
     * Set the BlobBufferLength value.
     * 
     * @param blobBufferLengthWrapper
     *            The new BlobBufferLength value.
     */
    public void setBlobBufferLength(Integer blobBufferLengthWrapper) {
        checkNotStarted();
        
        hashCode = 0;
        int blobBufferLength = blobBufferLengthWrapper.intValue();
        
        if (blobBufferLength > MAX_BLOB_BUFFER_LENGTH) {
            this.blobBufferLength = MAX_BLOB_BUFFER_LENGTH;
            if (log != null)
                log.warn("Supplied blob buffer length greater than maximum of "
                        + MAX_BLOB_BUFFER_LENGTH);
        } else 
        if (blobBufferLength < MIN_BLOB_BUFFER_LENGTH) {
            this.blobBufferLength = MIN_BLOB_BUFFER_LENGTH;
            if (log != null)
                log.warn("Supplied blob buffer length less than minimum of "
                        + MIN_BLOB_BUFFER_LENGTH);
        } else {
            this.blobBufferLength = blobBufferLength;
        } 
    }

    public void setDefaultConnectionManager(ConnectionManager defaultCm) {
        this.defaultCm = defaultCm;
    }
    
    public int hashCode() {
        if (hashCode != 0) 
            return hashCode;
        
        int result = 17;
        result = 37 * result + ((dbAlias == null) ? 0 : dbAlias.hashCode());
        result = 37 * result + defaultCri.hashCode();
        result = 37 * result + tpb.hashCode();
        result = 37 * result + blobBufferLength;
        result = 37 * result + type.hashCode();
        hashCode = result;
        return hashCode;
    }

    public boolean equals(Object other) {
        if (other == this) 
            return true;
        
        if (!(other instanceof FBManagedConnectionFactory)) 
            return false;
        
        FBManagedConnectionFactory that = (FBManagedConnectionFactory) other;
        
        return (dbAlias == null ? that.dbAlias == null : dbAlias
                .equals(that.dbAlias))
                && (defaultCri.equals(that.defaultCri))
                && (tpb.equals(that.tpb))
                && (blobBufferLength == that.blobBufferLength)
                && (type == that.type);
    }

    /**
     * The <code>createConnectionFactory</code> method creates a DataSource
     * using the supplied ConnectionManager.
     * 
     * @param cxManager
     *            a <code>ConnectionManager</code> value
     * @return a <code>java.lang.Object</code> value
     * @exception ResourceException
     *                if an error occurs
     */
    public Object createConnectionFactory(ConnectionManager cxManager)
            throws ResourceException {
        start();
        return new FBDataSource(this, cxManager);
    }

    /**
     * The <code>createConnectionFactory</code> method creates a DataSource
     * with a default stand alone ConnectionManager. Ours can implement pooling.
     * 
     * @return a new <code>javax.sql.DataSource</code> based around this
     *         connection factory
     * @exception ResourceException
     *                if an error occurs
     */
    public Object createConnectionFactory() throws ResourceException {
        start();
        return new FBDataSource(this, defaultCm);
    }

    /**
     * Creates a new physical connection to the underlying EIS resource manager,
     * ManagedConnectionFactory uses the security information (passed as
     * Subject) and additional ConnectionRequestInfo (which is specific to
     * ResourceAdapter and opaque to application server) to create this new
     * connection.
     * 
     * @param Subject
     *            Caller's security information
     * @param cxRequestInfo
     *            Additional resource adapter specific connection request
     *            information
     * @return ManagedConnection instance
     * @throws ResourceException
     *             generic exception
     * @throws SecurityException
     *             security related error
     * @throws ResourceAllocationException
     *             failed to allocate system resources for connection request
     * @throws ResourceAdapterInternalException
     *             resource adapter related error condition
     * @throws EISSystemException
     *             internal error condition in EIS instance
     */
    public ManagedConnection createManagedConnection(Subject subject,
            ConnectionRequestInfo cri) throws ResourceException {
        start();
        return new FBManagedConnection(subject, cri, this);
    }

    /**
     * Returns a matched connection from the candidate set of connections.
     * ManagedConnectionFactory uses the security info (as in
     * <code>Subject</code>) and information provided through
     * <code>ConnectionRequestInfo</code> and additional Resource Adapter
     * specific criteria to do matching. Note that criteria used for matching is
     * specific to a resource adapter and is not prescribed by the
     * <code>Connector</code> specification.
     * <p>
     * This method returns a <code>ManagedConnection</code> instance that is
     * the best match for handling the connection allocation request.
     * 
     * @param connectionSet
     *            candidate connection set
     * @param Subject
     *            caller's security information
     * @param cxRequestInfo
     *            additional resource adapter specific connection request
     *            information
     * @return ManagedConnection if resource adapter finds an acceptable match
     *         otherwise null
     * @throws ResourceException -
     *             generic exception
     * @throws SecurityException -
     *             security related error
     * @throws ResourceAdapterInternalException -
     *             resource adapter related error condition
     * @throws NotSupportedException -
     *             if operation is not supported
     */
    public ManagedConnection matchManagedConnections(Set connectionSet,
            javax.security.auth.Subject subject,
            ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        Iterator i = connectionSet.iterator();
        while (i.hasNext()) {
            FBManagedConnection mc = (FBManagedConnection) i.next();
            
            if (mc.matches(subject, (FBConnectionRequestInfo) cxRequestInfo))
                return mc;
        }
        return null;
    }

    /**
     * Set the log writer for this <code>ManagedConnectionFactory</code>
     * instance. The log writer is a character output stream to which all
     * logging and tracing messages for this
     * <code>ManagedConnectionFactory</code> instance will be printed.
     * ApplicationServer manages the association of output stream with the
     * <code>ManagedConnectionFactory</code>. When a
     * <code>ManagedConnectionFactory</code> object is created the log writer
     * is initially <code>null</code>, in other words, logging is disabled.
     * Once a log writer is associated with a
     * <code>ManagedConnectionFactory</code>, logging and tracing for
     * <code>ManagedConnectionFactory</code> instance is enabled.
     * <p>
     * The <code>ManagedConnection</code> instances created by
     * <code>ManagedConnectionFactory</code> "inherits" the log writer, which
     * can be overridden by ApplicationServer using
     * {@link ManagedConnection#setLogWriter}to set
     * <code>ManagedConnection</code> specific logging and tracing.
     * 
     * @param out
     *            an out stream for error logging and tracing
     * @throws ResourceException
     *             generic exception
     * @throws ResourceAdapterInternalException
     *             resource adapter related error condition
     */
    public void setLogWriter(PrintWriter out) throws ResourceException {
        // ignore - we're using log4j
    }

    /**
     * Get the log writer for this <code>ManagedConnectionFactory</code>
     * instance. The log writer is a character output stream to which all
     * logging and tracing messages for this
     * <code>ManagedConnectionFactory</code> instance will be printed.
     * ApplicationServer manages the association of output stream with the
     * <code>ManagedConnectionFactory</code>. When a
     * <code>ManagedConnectionFactory</code> object is created the log writer
     * is initially null, in other words, logging is disabled.
     * 
     * @return PrintWriter instance
     * @throws ResourceException
     *             generic exception
     */
    public PrintWriter getLogWriter() {
        return null;// we're using log4j
    }

    // Serialization support
    private Object readResolve() throws ObjectStreamException {
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory) mcfInstances
                .get(this);
        if (mcf != null) { return mcf; } // end of if ()
        mcf = new FBManagedConnectionFactory(type);
        mcf.setDatabase(this.getDatabase());
        mcf.setConnectionRequestInfo(this.getDefaultConnectionRequestInfo());
        mcf.setTpb(this.getTpb());
        mcf.setBlobBufferLength(this.getBlobBufferLength());
        return mcf;
    }

    /**
     * The <code>canonicalize</code> method is used in FBDriver to reuse
     * previous fbmcf instances if they have been create. It should really be
     * package access level
     * 
     * @return a <code>FBManagedConnectionFactory</code> value
     */
    public FBManagedConnectionFactory canonicalize() {
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory) mcfInstances
                .get(this);
        if (mcf != null) { return mcf; } // end of if ()
        return this;
    }

    private void start() {
        synchronized (startLock) {
            if (!started) {
                synchronized (mcfInstances) {
                    mcfInstances.put(this, this);
                }
                started = true;
            } // end of if ()
        }
    }

    private void checkNotStarted() throws java.lang.IllegalStateException {
        synchronized (startLock) {
            if (started)
                throw new java.lang.IllegalStateException(
                        "Operation not permitted after "
                                + "ManagedConnectionFactory in use");
        }
    }

    
    void notifyStart(FBManagedConnection mc, Xid xid) throws GDSException {
        xidMap.put(xid, mc);
    }
    
    void notifyEnd(FBManagedConnection mc, Xid xid) throws XAException {
        // empty
    }
    
    int notifyPrepare(FBManagedConnection mc, Xid xid) throws GDSException, XAException {
        FBManagedConnection targetMc = (FBManagedConnection)xidMap.get(xid);
        
        if (targetMc == null)
            throw new FBXAException("Commit called with unknown transaction",
                XAException.XAER_NOTA);

        return targetMc.internalPrepare(xid);
    }

    void notifyCommit(FBManagedConnection mc, Xid xid, boolean onePhase) throws GDSException, XAException {

        FBManagedConnection targetMc = (FBManagedConnection)xidMap.get(xid);
        
        if (targetMc == null)
            tryCompleteInLimboTransaction(gds, xid,  true);

        targetMc.internalCommit(xid, onePhase);
        xidMap.remove(xid);
    }
    
    void notifyRollback(FBManagedConnection mc, Xid xid) throws GDSException, XAException {
        FBManagedConnection targetMc = (FBManagedConnection)xidMap.get(xid);
        
        if (targetMc == null)
            tryCompleteInLimboTransaction(gds, xid,  false);

        targetMc.internalRollback(xid);
        xidMap.remove(xid);
    }
    
    void forget(FBManagedConnection mc, Xid xid) throws GDSException {
        
    }
    
    void recover(FBManagedConnection mc, Xid xid) throws GDSException {
        
    }
    
    /**
     * Try to complete the "in limbo" transaction. This method tries to 
     * reconnect an "in limbo" transaction and complete it either by commit or
     * rollback. If no "in limbo" transaction can be found, or error happens
     * during completion, an exception is thrown.
     * 
     * @param gdsHelper instance of {@link GDSHelper} that will be used to
     * reconnect transaction.
     * @param xid Xid of the transaction to reconnect.
     * @param commit <code>true</code> if "in limbo" transaction should be 
     * committed, otherwise <code>false</code>.
     * 
     * @throws XAException if "in limbo" transaction cannot be completed.
     */
    private void tryCompleteInLimboTransaction(GDS gds, Xid xid, 
            boolean commit) throws XAException {
        
        // construct our own Xid implementation that can produce us a
        // byte array that is used in isc_prepare_transaction2 call
        FBXid fbXid;
        if (xid instanceof FBXid)
            fbXid = (FBXid)xid;
        else
            fbXid = new FBXid(xid);
        
        // this flag is used in exception handler to return correct error
        // code depending on the situation
        boolean knownTransaction = false;
        
        try {
            FBManagedConnection tempMc = null;
            try {
                tempMc = new FBManagedConnection(null, null, this);
                
                long fbTransactionId = 0;
                boolean found = false;
                
                FBXid[] inLimboIds = (FBXid[])tempMc.recover(XAResource.TMSTARTRSCAN);
                for (int i = 0; i < inLimboIds.length; i++) {
                    if (inLimboIds[i].equals(xid)) {
                        found = true;
                        fbTransactionId = inLimboIds[i].getFirebirdTransactionId();
                    }
                }
                
                if (!found)
                    throw new FBXAException((commit ? "Commit" : "Rollback") + 
                        " called with unknown transaction.", XAException.XAER_NOTA);
                
                IscDbHandle dbHandle = tempMc.getGDSHelper().getCurrentDbHandle();
    
                IscTrHandle trHandle = gds.createIscTrHandle();
                gds.iscReconnectTransaction(trHandle, dbHandle, fbTransactionId);
                
                // tell exception handler that we know the transaction
                knownTransaction = true;
                
                // complete transaction by commit or rollback
                if (commit)
                    gds.iscCommitTransaction(trHandle);
                else
                    gds.iscRollbackTransaction(trHandle);
                
            } catch(GDSException ex) {
                throw new FBXAException(XAException.XAER_RMERR, ex);
            } finally {
                if (tempMc != null)
                    tempMc.destroy();
            }
        } catch(ResourceException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
        }
    }
    
    AbstractConnection newConnection(FBManagedConnection mc) throws ResourceException {
        Class connectionClass = GDSFactory.getConnectionClass(getType());
        
        if (!AbstractConnection.class.isAssignableFrom(connectionClass))
            throw new IllegalArgumentException("Specified connection class" +
                    " does not extend " + AbstractConnection.class.getName() + 
                    " class");
        
        try {
            Constructor constructor = connectionClass.getConstructor(new Class[]{FBManagedConnection.class});
            
            return (AbstractConnection)constructor.newInstance(new Object[]{mc});
            
        } catch(NoSuchMethodException ex) {
            throw new FBResourceException(
                    "Cannot instantiate connection class "
                            + connectionClass.getName()
                            + ", no constructor accepting "
                            + FBManagedConnection.class
                            + " class as single parameter was found.");
        } catch(InvocationTargetException ex) {
            
            if (ex.getTargetException() instanceof RuntimeException)
                throw (RuntimeException)ex.getTargetException();
            
            if (ex.getTargetException() instanceof Error)
                throw (Error)ex.getTargetException();
            
            throw new FBResourceException((Exception)ex.getTargetException());
        } catch(IllegalAccessException ex) {
            throw new FBResourceException("Constructor for class "
                    + connectionClass.getName() + " is not accessible.");
        } catch(InstantiationException ex) {
            throw new FBResourceException("Cannot instantiate class"
                    + connectionClass.getName());
        }
    }
}
