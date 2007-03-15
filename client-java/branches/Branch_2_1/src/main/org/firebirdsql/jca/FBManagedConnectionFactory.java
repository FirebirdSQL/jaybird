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
import java.sql.SQLException;
import java.util.*;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;
import javax.transaction.xa.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.*;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
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
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 */

public class FBManagedConnectionFactory implements ManagedConnectionFactory,
        Serializable, FirebirdConnectionProperties {

    private static final Logger log = LoggerFactory.getLogger(FBManagedConnectionFactory.class, false);

    
    /**
     * The <code>mcfInstances</code> weak hash map is used in deserialization
     * to find the correct instance of a mcf after deserializing.
     */
    private final static Map mcfInstances = new WeakHashMap();

    // /**
    // * @todo Claudio suggests this should be 1024*64 -1, we should find out I
    // * thought this was the largest value I could make work, but I didn't
    // * write down my experiments.
    // */
    // public final static int MAX_BLOB_BUFFER_LENGTH = 1024 * 32 - 1;
    //
    // public final static int MIN_BLOB_BUFFER_LENGTH = 1024;


    private ConnectionManager defaultCm;
    private int hashCode;
    private GDSType gdsType;

    // Maps supplied XID to internal transaction handle.
    // a concurrent reader map would be better
    private transient final Map xidMap = Collections.synchronizedMap(new HashMap());

    private transient final Object startLock = new Object();
    private transient boolean started = false;

    private FBConnectionProperties connectionProperties;

    /**
     * Create a new pure-Java FBManagedConnectionFactory.
     */
    public FBManagedConnectionFactory() {
        this(GDSFactory.getDefaultGDSType(), new FBConnectionProperties());
    }

    /**
     * Create a new FBManagedConnectionFactory based around the given GDSType.
     * 
     * @param gdsType
     *            The GDS implementation to use
     */
    public FBManagedConnectionFactory(GDSType gdsType) {
        this(gdsType, new FBConnectionProperties());
    }
    
    public FBManagedConnectionFactory(GDSType gdsType, FBConnectionProperties connectionProperties) {
        this.defaultCm = new FBStandAloneConnectionManager();
        this.connectionProperties = connectionProperties;
        setType(gdsType.toString());
    }

    public GDS getGDS() {
        return GDSFactory.getGDSForType(getGDSType());
    }

    /**
     * Get the GDS implementation type around which this factory is based.
     * 
     * @return The GDS implementation type
     */
    public GDSType getGDSType() {
        if (gdsType != null)
            return gdsType;
        
        gdsType = GDSType.getType(getType());
        
        return gdsType;
    }
    
    /**
     * @deprecated use {@link #getBlobBufferSize()}
     */
    public int getBlobBufferLength() {
        return getBlobBufferSize();
    }
    
    /**
     * @deprecated use {@link #setBlobBufferSize(int)}
     */
    public void setBlobBufferLength(int value) {
        setBlobBufferSize(value);
    }
    
    /**
     * @deprecated use {@link #getDefaultTransactionIsolation()}
     */
    public Integer getTransactionIsolation() {
        return new Integer(getDefaultTransactionIsolation());
    }
    
    /**
     * @deprecated use {@link #setDefaultTransactionIsolation(int)}
     */
    public void setTransactionIsolation(Integer value) {
        if (value != null)
            setDefaultTransactionIsolation(value.intValue());
    }
    
    /**
     * @deprecated use {@link #getDefaultIsolation()}
     */
    public String getTransactionIsolationName() {
        return getDefaultIsolation();
    }

    /**
     * @deprecated use {@link #setDefaultIsolation(String)} 
     */
    public void setTransactionIsolationName(String name) {
        setDefaultIsolation(name);
    }
    
    /**
     * @deprecated use {@link #getCharSet()} instead.
     */
    public String getLocalEncoding() {
        return getCharSet();
    }
    
    /**
     * @deprecated use {@link #setCharSet(String)} instead.
     */
    public void setLocalEncoding(String localEncoding) {
        setCharSet(localEncoding);
    }
    
    public int getBlobBufferSize() {
        return connectionProperties.getBlobBufferSize();
    }

    public int getBuffersNumber() {
        return connectionProperties.getBuffersNumber();
    }

    public String getCharSet() {
        return connectionProperties.getCharSet();
    }

    public String getDatabase() {
        return connectionProperties.getDatabase();
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        return connectionProperties.getDatabaseParameterBuffer();
    }

    public String getDefaultIsolation() {
        return connectionProperties.getDefaultIsolation();
    }

    public int getDefaultTransactionIsolation() {
        return connectionProperties.getDefaultTransactionIsolation();
    }

    public String getEncoding() {
        return connectionProperties.getEncoding();
    }

    public String getNonStandardProperty(String key) {
        return connectionProperties.getNonStandardProperty(key);
    }

    public String getPassword() {
        return connectionProperties.getPassword();
    }

    public String getRoleName() {
        return connectionProperties.getRoleName();
    }

    public int getSocketBufferSize() {
        return connectionProperties.getSocketBufferSize();
    }

    public String getSqlDialect() {
        return connectionProperties.getSqlDialect();
    }

    public String getTpbMapping() {
        return connectionProperties.getTpbMapping();
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return connectionProperties.getTransactionParameters(isolation);
    }

    public String getType() {
        return connectionProperties.getType();
    }

    public String getUserName() {
        return connectionProperties.getUserName();
    }

    public String getUseTranslation() {
        return connectionProperties.getUseTranslation();
    }

    public boolean isTimestampUsesLocalTimezone() {
        return connectionProperties.isTimestampUsesLocalTimezone();
    }

    public boolean isUseStandardUdf() {
        return connectionProperties.isUseStandardUdf();
    }

    public boolean isUseStreamBlobs() {
        return connectionProperties.isUseStreamBlobs();
    }

    public void setBlobBufferSize(int bufferSize) {
        connectionProperties.setBlobBufferSize(bufferSize);
    }

    public void setBuffersNumber(int buffersNumber) {
        connectionProperties.setBuffersNumber(buffersNumber);
    }

    public void setCharSet(String charSet) {
        connectionProperties.setCharSet(charSet);
    }

    public void setDatabase(String database) {
        connectionProperties.setDatabase(database);
    }

    public void setDefaultIsolation(String isolation) {
        connectionProperties.setDefaultIsolation(isolation);
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        connectionProperties.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public void setEncoding(String encoding) {
        connectionProperties.setEncoding(encoding);
    }

    public void setNonStandardProperty(String key, String value) {
        connectionProperties.setNonStandardProperty(key, value);        
    }

    public void setNonStandardProperty(String propertyMapping) {
        connectionProperties.setNonStandardProperty(propertyMapping);
    }

    public void setPassword(String password) {
        connectionProperties.setPassword(password);
    }

    public void setRoleName(String roleName) {
        connectionProperties.setRoleName(roleName);        
    }

    public void setSocketBufferSize(int socketBufferSize) {
        connectionProperties.setSocketBufferSize(socketBufferSize);        
    }

    public void setSqlDialect(String sqlDialect) {
        connectionProperties.setSqlDialect(sqlDialect);
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        connectionProperties.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);        
    }

    public void setTpbMapping(String tpbMapping) {
        connectionProperties.setTpbMapping(tpbMapping);        
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        connectionProperties.setTransactionParameters(isolation, tpb);        
    }

    public void setType(String type) {
        if (gdsType != null)
            throw new java.lang.IllegalStateException(
                    "Cannot change GDS type at runtime.");
        
        connectionProperties.setType(type);
    }

    public void setUserName(String userName) {
        connectionProperties.setUserName(userName);        
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        connectionProperties.setUseStandardUdf(useStandardUdf);        
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        connectionProperties.setUseStreamBlobs(useStreamBlobs);        
    }

    public void setUseTranslation(String translationPath) {
        connectionProperties.setUseTranslation(translationPath);        
    }

    public boolean isDefaultResultSetHoldable() {
        return connectionProperties.isDefaultResultSetHoldable();
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        connectionProperties.setDefaultResultSetHoldable(isHoldable);
    }

    public void setDefaultConnectionManager(ConnectionManager defaultCm) {
        this.defaultCm = defaultCm;
    }

    public int hashCode() {
        if (hashCode != 0) 
            return hashCode;

        int result = 17;
        result = 37 * result + connectionProperties.hashCode();
        if (result == 0)
            result = 17;
        
        if (gdsType != null)
            hashCode = result;
        
        return result;
    }

    public boolean equals(Object other) {
        if (other == this) return true;

        if (!(other instanceof FBManagedConnectionFactory)) return false;

        FBManagedConnectionFactory that = (FBManagedConnectionFactory) other;

        return this.connectionProperties.equals(that.connectionProperties);
    }
    
    public FBConnectionRequestInfo getDefaultConnectionRequestInfo() throws ResourceException {
        try {
            return new FBConnectionRequestInfo(getDatabaseParameterBuffer().deepCopy());
        } catch(SQLException ex) {
            throw new FBResourceException(ex);
        }
    }
    
    public FBTpb getDefaultTpb() throws ResourceException {
        int defaultTransactionIsolation = 
            connectionProperties.getMapper().getDefaultTransactionIsolation();

        return getTpb(defaultTransactionIsolation);
    }

    public FBTpb getTpb(int defaultTransactionIsolation) throws FBResourceException {
        return new FBTpb(connectionProperties.getMapper().getMapping(
                defaultTransactionIsolation));
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
     * @param subject
     *            Caller's security information
     * @param cri
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
     * @param subject
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
        FBManagedConnectionFactory mcf = 
            (FBManagedConnectionFactory) mcfInstances.get(this);
        
        if (mcf != null)  return mcf; 
        
        mcf = new FBManagedConnectionFactory(getGDSType(), 
                (FBConnectionProperties)this.connectionProperties.clone());
        
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
        FBManagedConnectionFactory mcf = (FBManagedConnectionFactory) mcfInstances.get(this);
        
        if (mcf != null) 
            return mcf;
        
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

    void notifyStart(FBManagedConnection mc, Xid xid) throws GDSException {
        xidMap.put(xid, mc);
    }

    void notifyEnd(FBManagedConnection mc, Xid xid) throws XAException {
        // empty
    }

    int notifyPrepare(FBManagedConnection mc, Xid xid) throws GDSException,
            XAException {
        FBManagedConnection targetMc = (FBManagedConnection) xidMap.get(xid);

        if (targetMc == null)
            throw new FBXAException("Commit called with unknown transaction",
                    XAException.XAER_NOTA);

        return targetMc.internalPrepare(xid);
    }

    void notifyCommit(FBManagedConnection mc, Xid xid, boolean onePhase)
            throws GDSException, XAException {

        FBManagedConnection targetMc = (FBManagedConnection) xidMap.get(xid);

        if (targetMc == null)
            tryCompleteInLimboTransaction(getGDS(), xid, true);
        else
            targetMc.internalCommit(xid, onePhase);

        xidMap.remove(xid);
    }

    void notifyRollback(FBManagedConnection mc, Xid xid) throws GDSException,
            XAException {
        FBManagedConnection targetMc = (FBManagedConnection) xidMap.get(xid);

        if (targetMc == null)
            tryCompleteInLimboTransaction(getGDS(), xid, false);
        else
            targetMc.internalRollback(xid);

        xidMap.remove(xid);
    }

    public void forget(FBManagedConnection mc, Xid xid) throws GDSException {
        xidMap.remove(xid);
    }

    public void recover(FBManagedConnection mc, Xid xid) throws GDSException {

    }

    /**
     * Try to complete the "in limbo" transaction. This method tries to
     * reconnect an "in limbo" transaction and complete it either by commit or
     * rollback. If no "in limbo" transaction can be found, or error happens
     * during completion, an exception is thrown.
     * 
     * @param gdsHelper
     *            instance of {@link GDSHelper} that will be used to reconnect
     *            transaction.
     * @param xid
     *            Xid of the transaction to reconnect.
     * @param commit
     *            <code>true</code> if "in limbo" transaction should be
     *            committed, otherwise <code>false</code>.
     * 
     * @throws XAException
     *             if "in limbo" transaction cannot be completed.
     */
    private void tryCompleteInLimboTransaction(GDS gds, Xid xid, boolean commit)
            throws XAException {

        try {
            FBManagedConnection tempMc = new FBManagedConnection(null, null, this);
            try {
                try {
                    FirebirdLocalTransaction tempLocalTx = 
                        (FirebirdLocalTransaction) tempMc.getLocalTransaction();
                    
                    long fbTransactionId = 0;
                    boolean found = false;
                    
                    // loop through all in-limbo transactions and look for the required one
                    tempLocalTx.begin();
                    try {
                        ArrayList inLimboIds = fetchInLimboXids(getGDS(), tempMc.getGDSHelper());
                        
                        for (Iterator iter = inLimboIds.iterator(); iter.hasNext();) {
                            FBXid foundXid = (FBXid) iter.next();
                            if (foundXid.equals(xid)) {
                                found = true;
                                fbTransactionId = foundXid.getFirebirdTransactionId();
                            }
                        }
                        
                    } catch(SQLException ex) {
                        if (log != null)
                            log.debug("can't perform query to fetch xids", ex);
                        
                        throw new FBXAException(XAException.XAER_RMFAIL, ex);
                        
                    } finally {
                        tempLocalTx.commit();
                    }
    
                    // if no such transaction found, notify the resource manager
                    if (!found)
                        throw new FBXAException((commit ? "Commit" : "Rollback")
                                + " called with unknown transaction.",
                                XAException.XAER_NOTA);
    
                    IscDbHandle dbHandle = tempMc.getGDSHelper().getCurrentDbHandle();
    
                    // reconnect the in-limbo transaction to temp connection
                    IscTrHandle trHandle = gds.createIscTrHandle();
                    gds.iscReconnectTransaction(trHandle, dbHandle, fbTransactionId);
    
                    // complete transaction by commit or rollback
                    if (commit)
                        gds.iscCommitTransaction(trHandle);
                    else
                        gds.iscRollbackTransaction(trHandle);
    
                } catch (GDSException ex) {
                    throw new FBXAException(XAException.XAER_RMERR, ex);
                }
            } finally {
                tempMc.destroy();
            }
        } catch (ResourceException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
        }
    }
    
    // simple implementation of the FieldDataProvider interface
    private static class DataProvider implements FieldDataProvider {

        private AbstractIscStmtHandle stmtHandle;
        private int fieldPos;
        private int row;
        
        private DataProvider(AbstractIscStmtHandle stmtHandle, int fieldPos) {
            this.stmtHandle = stmtHandle;
            this.fieldPos = fieldPos;
        }
        
        public void setRow(int row) {
            this.row = row;
        }
        
        public byte[] getFieldData() {
            return ((byte[][])stmtHandle.getRows()[row])[fieldPos];
        }
        public void setFieldData(byte[] data) {
            throw new UnsupportedOperationException();
        }
    }

    private static final String RECOVERY_QUERY = ""
        + "SELECT RDB$TRANSACTION_ID, RDB$TRANSACTION_DESCRIPTION "
        + "FROM RDB$TRANSACTIONS WHERE RDB$TRANSACTION_STATE = 1";

    
    public static ArrayList fetchInLimboXids(GDS gds, GDSHelper gdsHelper2) throws GDSException, SQLException, ResourceException {
        ArrayList xids = new ArrayList();

        AbstractIscStmtHandle stmtHandle2 = (AbstractIscStmtHandle)gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(gdsHelper2.getCurrentDbHandle(), stmtHandle2);
        
        
        gdsHelper2.prepareStatement(stmtHandle2, RECOVERY_QUERY, false);
        gdsHelper2.executeStatement(stmtHandle2, false);
        gdsHelper2.fetch(stmtHandle2, 10);
        
        DataProvider dataProvider0 = new DataProvider(stmtHandle2, 0);
        DataProvider dataProvider1 = new DataProvider(stmtHandle2, 1);
        
        FBField field0 = FBField.createField(stmtHandle2.getOutSqlda().sqlvar[0], dataProvider0, gdsHelper2, false);
        FBField field1 = FBField.createField(stmtHandle2.getOutSqlda().sqlvar[1], dataProvider1, gdsHelper2, false);
        
        field0.setConnection(gdsHelper2);
        field1.setConnection(gdsHelper2);
        
        int row = 0;
        while(row < stmtHandle2.getRows().length) {
        
            if (stmtHandle2.getRows()[row] == null) {
                row++;
                continue;
            }
            
            dataProvider0.setRow(row);
            dataProvider1.setRow(row);
            
            long inLimboTxId = field0.getLong();
            byte[] inLimboMessage = field1.getBytes();
        
            try {
                FBXid xid = new FBXid(new ByteArrayInputStream(inLimboMessage), inLimboTxId);
                xids.add(xid);
            } catch(FBIncorrectXidException ex) {
                // ignore this Xid
            }
   
            row++;
        }
   
        gdsHelper2.closeStatement(stmtHandle2, true);
        return xids;
    }

    AbstractConnection newConnection(FBManagedConnection mc)
            throws ResourceException {
        Class connectionClass = GDSFactory.getConnectionClass(getGDSType());

        if (!AbstractConnection.class.isAssignableFrom(connectionClass))
            throw new IllegalArgumentException("Specified connection class"
                    + " does not extend " + AbstractConnection.class.getName()
                    + " class");

        try {
            Constructor constructor = connectionClass
                    .getConstructor(new Class[] { FBManagedConnection.class});

            return (AbstractConnection) constructor
                    .newInstance(new Object[] { mc});

        } catch (NoSuchMethodException ex) {
            throw new FBResourceException(
                    "Cannot instantiate connection class "
                            + connectionClass.getName()
                            + ", no constructor accepting "
                            + FBManagedConnection.class
                            + " class as single parameter was found.");
        } catch (InvocationTargetException ex) {

            if (ex.getTargetException() instanceof RuntimeException)
                throw (RuntimeException) ex.getTargetException();

            if (ex.getTargetException() instanceof Error)
                throw (Error) ex.getTargetException();

            throw new FBResourceException((Exception) ex.getTargetException());
        } catch (IllegalAccessException ex) {
            throw new FBResourceException("Constructor for class "
                    + connectionClass.getName() + " is not accessible.");
        } catch (InstantiationException ex) {
            throw new FBResourceException("Cannot instantiate class"
                    + connectionClass.getName());
        }
    }
}
