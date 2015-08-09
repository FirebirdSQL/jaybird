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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBConnectionProperties;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnectionProperties;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBManagedConnectionFactory implements ManagedConnectionFactory, FirebirdConnectionProperties,
        Serializable {
    
    private static final long serialVersionUID = 7500832904323015501L;

    /**
     * The <code>mcfInstances</code> weak hash map is used in deserialization
     * to find the correct instance of a mcf after deserializing.
     * <p>
     * It is also used to return a canonical instance to {@link org.firebirdsql.jdbc.FBDriver}.
     * </p>
     */
    private static final Map<FBConnectionProperties, SoftReference<FBManagedConnectionFactory>> mcfInstances =
            new ConcurrentHashMap<>();
    private static final ReferenceQueue<FBManagedConnectionFactory> mcfReferenceQueue = new ReferenceQueue<>();

    private ConnectionManager defaultCm;
    private int hashCode;
    private GDSType gdsType;

    // Maps supplied XID to internal transaction handle.
    private transient final Map<Xid, FBManagedConnection> xidMap = new ConcurrentHashMap<>();

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

    public FbDatabaseFactory getDatabaseFactory() {
        return GDSFactory.getDatabaseFactoryForType(getGDSType());
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
    @Deprecated
    public int getBlobBufferLength() {
        return getBlobBufferSize();
    }

    /**
     * @deprecated use {@link #setBlobBufferSize(int)}
     */
    @Deprecated
    public void setBlobBufferLength(int value) {
        setBlobBufferSize(value);
    }

    /**
     * @deprecated use {@link #getDefaultTransactionIsolation()}
     */
    @Deprecated
    public Integer getTransactionIsolation() {
        return getDefaultTransactionIsolation();
    }
    
    /**
     * @deprecated use {@link #setDefaultTransactionIsolation(int)}
     */
    @Deprecated
    public void setTransactionIsolation(Integer value) {
        if (value != null)
            setDefaultTransactionIsolation(value);
    }
    
    /**
     * @deprecated use {@link #getDefaultIsolation()}
     */
    @Deprecated
    public String getTransactionIsolationName() {
        return getDefaultIsolation();
    }

    /**
     * @deprecated use {@link #setDefaultIsolation(String)} 
     */
    @Deprecated
    public void setTransactionIsolationName(String name) {
        setDefaultIsolation(name);
    }
    
    /**
     * @deprecated use {@link #getCharSet()} instead.
     */
    @Deprecated
    public String getLocalEncoding() {
        return getCharSet();
    }
    
    /**
     * @deprecated use {@link #setCharSet(String)} instead.
     */
    @Deprecated
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
    
    public int getSoTimeout() {
        return connectionProperties.getSoTimeout();
    }

    public void setSoTimeout(int soTimeout) {
        connectionProperties.setSoTimeout(soTimeout);
    }
    
    public int getConnectTimeout() {
        return connectionProperties.getConnectTimeout();
    }
    
    public void setConnectTimeout(int connectTimeout) {
        connectionProperties.setConnectTimeout(connectTimeout);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return connectionProperties.isUseFirebirdAutocommit();
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        connectionProperties.setUseFirebirdAutocommit(useFirebirdAutocommit);
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
    public ManagedConnection matchManagedConnections(Set connectionSet, javax.security.auth.Subject subject,
            ConnectionRequestInfo cxRequestInfo) throws ResourceException {

        for (Object connection : connectionSet) {
            if (!(connection instanceof FBManagedConnection)) continue;
            FBManagedConnection mc = (FBManagedConnection) connection;

            if (mc.matches(subject, cxRequestInfo))
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
        FBManagedConnectionFactory mcf = internalCanonicalize();
        if (mcf != null)  return mcf;
        
        mcf = new FBManagedConnectionFactory(getGDSType(), (FBConnectionProperties)this.connectionProperties.clone());
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
        final FBManagedConnectionFactory mcf = internalCanonicalize();
        if (mcf != null) return mcf;
        start();
        return this;
    }

    private FBManagedConnectionFactory internalCanonicalize() {
        final SoftReference<FBManagedConnectionFactory> factoryReference = mcfInstances.get(getCacheKey());
        return factoryReference != null ? factoryReference.get() : null;
    }

    /**
     * Starts this MCF and adds this instance to {@code #mcfInstances} cache.
     * <p>
     * This implementation (together with {@link #canonicalize()} has a race condition with regard to the
     * instance cache. As this is a relatively harmless one, we leave it as is.
     * </p>
     */
    private void start() {
        synchronized (startLock) {
            if (started) return;
            mcfInstances.put(getCacheKey(), new SoftReference<>(this, mcfReferenceQueue));
            started = true;
        }
        cleanMcfInstances();
    }

    /**
     * Removes cleared references from the {@link #mcfInstances} cache.
     */
    private void cleanMcfInstances() {
        Reference<? extends FBManagedConnectionFactory> reference;
        while ((reference = mcfReferenceQueue.poll()) != null) {
            mcfInstances.values().remove(reference);
        }
    }

    void notifyStart(FBManagedConnection mc, Xid xid) throws GDSException {
        xidMap.put(xid, mc);
    }

    void notifyEnd(FBManagedConnection mc, Xid xid) throws XAException {
        // empty
    }

    int notifyPrepare(FBManagedConnection mc, Xid xid) throws GDSException, XAException {
        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null)
            throw new FBXAException("Commit called with unknown transaction", XAException.XAER_NOTA);

        return targetMc.internalPrepare(xid);
    }

    void notifyCommit(FBManagedConnection mc, Xid xid, boolean onePhase) throws GDSException, XAException {

        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null)
            tryCompleteInLimboTransaction(xid, true);
        else
            targetMc.internalCommit(xid, onePhase);

        xidMap.remove(xid);
    }

    void notifyRollback(FBManagedConnection mc, Xid xid) throws GDSException, XAException {
        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null)
            tryCompleteInLimboTransaction(xid, false);
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
     * @param xid
     *            Xid of the transaction to reconnect.
     * @param commit
     *            <code>true</code> if "in limbo" transaction should be
     *            committed, otherwise <code>false</code>.
     *
     * @throws XAException
     *             if "in limbo" transaction cannot be completed.
     */
    private void tryCompleteInLimboTransaction(Xid xid, boolean commit) throws XAException {
        try {
            FBManagedConnection tempMc = null;
            FirebirdLocalTransaction tempLocalTx = null;
            try {
                tempMc = new FBManagedConnection(null, null, this);
                tempLocalTx = (FirebirdLocalTransaction) tempMc.getLocalTransaction();
                tempLocalTx.begin();

                long fbTransactionId = 0;
                boolean found = false;

                if (tempMc.getGDSHelper().compareToVersion(2, 0) < 0) {
                    // Find Xid by scanning
                    FBXid[] inLimboIds = (FBXid[]) tempMc.recover(XAResource.TMSTARTRSCAN);
                    for (FBXid inLimboId : inLimboIds) {
                        if (inLimboId.equals(xid)) {
                            found = true;
                            fbTransactionId = inLimboId.getFirebirdTransactionId();
                        }
                    }
                } else {
                    // Find Xid by intelligent scan
                    FBXid foundXid = (FBXid) tempMc.findSingleXid(xid);
                    if (foundXid != null && foundXid.equals(xid)) {
                        found = true;
                        fbTransactionId = foundXid.getFirebirdTransactionId();
                    }
                }

                if (!found) {
                    throw new FBXAException((commit ? "Commit" : "Rollback") + " called with unknown transaction.",
                            XAException.XAER_NOTA);
                }

                FbDatabase dbHandle = tempMc.getGDSHelper().getCurrentDatabase();
                FbTransaction trHandle = dbHandle.reconnectTransaction(fbTransactionId);

                // complete transaction by commit or rollback
                if (commit) {
                    trHandle.commit();
                } else {
                    trHandle.rollback();
                }

                if (tempMc.getGDSHelper().compareToVersion(3, 0) < 0) {
                    // remove heuristic data from rdb$transactions (only possible in versions before Firebird 3)
                    try {
                        String query = "delete from rdb$transactions where rdb$transaction_id = " + fbTransactionId;
                        GDSHelper gdsHelper = new GDSHelper(null, dbHandle);

                        FbTransaction trHandle2 = dbHandle.startTransaction(getDefaultTpb().getTransactionParameterBuffer());
                        gdsHelper.setCurrentTransaction(trHandle2);

                        FbStatement stmtHandle2 = dbHandle.createStatement(trHandle2);

                        stmtHandle2.prepare(query);
                        stmtHandle2.execute(RowValue.EMPTY_ROW_VALUE);

                        stmtHandle2.close();
                        trHandle2.commit();
                    } catch (SQLException sqle) {
                        throw new FBXAException("unable to remove in limbo transaction from rdb$transactions where rdb$transaction_id = " + fbTransactionId, XAException.XAER_RMERR);
                    }
                }
            } catch (SQLException ex) {
                /*
                 * if ex.getIntParam() is 335544353 (transaction is not in limbo) and next ex.getIntParam() is 335544468 (transaction {0} is {1})
                 *  => detected heuristic
                 */
                // TODO: We may need to parse the exception to get the details (or we need to handle this specific one differently)
                int errorCode = XAException.XAER_RMERR;
                int sqlError = ex.getErrorCode();
                //int nextIntParam = ex.getNext().getIntParam();

                if (sqlError == ISCConstants.isc_no_recon /*&& nextIntParam == ISCConstants.isc_tra_state*/) {
                    if (ex.getMessage().contains("committed")) {
                        errorCode = XAException.XA_HEURCOM;
                    } else if (ex.getMessage().contains("rolled back")) {
                        errorCode = XAException.XA_HEURCOM;
                    }
                }

                throw new FBXAException("unable to complete in limbo transaction", errorCode, ex);
            } finally {
                try {
                    if (tempLocalTx != null && tempLocalTx.inTransaction())
                        tempLocalTx.commit();
                } finally {
                    if (tempMc != null) tempMc.destroy();
                }
            }
        } catch (ResourceException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
        }
    }

    FBConnection newConnection(FBManagedConnection mc)
            throws ResourceException {
        Class<?> connectionClass = GDSFactory.getConnectionClass(getGDSType());

        if (!FBConnection.class.isAssignableFrom(connectionClass))
            throw new IllegalArgumentException("Specified connection class"
                    + " does not extend " + FBConnection.class.getName()
                    + " class");

        try {
            Constructor<?> constructor = connectionClass
                    .getConstructor(FBManagedConnection.class);

            return (FBConnection) constructor
                    .newInstance(mc);

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

    public final FBConnectionProperties getCacheKey() {
        return (FBConnectionProperties) connectionProperties.clone();
    }
}
