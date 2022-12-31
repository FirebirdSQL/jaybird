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
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.sql.DataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FBManagedConnectionFactory is a factory for {@link FBManagedConnection}, and implements many of the internal
 * functions of FBManagedConnection. This behavior is required due to firebird requiring all work done in a transaction
 * to be done over one connection.
 * <p>
 * To support xa semantics, the correct db handle must be located whenever a managed connection is associated with a
 * xid.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class FBManagedConnectionFactory implements FirebirdConnectionProperties, Serializable {

    // This class uses a serialization proxy, see class at end of file

    /**
     * The {@code mcfInstances} weak hash map is used in deserialization to find the correct instance of a mcf after
     * deserializing.
     * <p>
     * It is also used to return a canonical instance to {@link org.firebirdsql.jdbc.FBDriver}.
     * </p>
     */
    private static final Map<FBConnectionProperties, SoftReference<FBManagedConnectionFactory>> mcfInstances =
            new ConcurrentHashMap<>();
    private static final ReferenceQueue<FBManagedConnectionFactory> mcfReferenceQueue = new ReferenceQueue<>();
    /**
     * Used to ensure that instances with a different connection manager type are different in the {@code mcfInstances}
     * map.
     */
    private static final String DEFAULT_CONNECTION_MANAGER_TYPE = "CONNECTION_MANAGER_TYPE";

    private XcaConnectionManager defaultCm;
    private int hashCode;
    private GDSType gdsType;

    // Maps supplied XID to internal transaction handle.
    private final Map<Xid, FBManagedConnection> xidMap = new ConcurrentHashMap<>();

    private final Object startLock = new Object();
    private boolean started = false;
    // When a MCF is shared, its configuration has to be stable after first connection/datasource creation
    private final boolean shared;

    private final FBConnectionProperties connectionProperties;

    /**
     * Create a new pure-Java FBManagedConnectionFactory.
     * <p>
     * This managed connection factory can be shared.
     * </p>
     */
    public FBManagedConnectionFactory() {
        this(true);
    }

    /**
     * Create a new pure-Java FBManagedConnectionFactory.
     *
     * @param shared
     *         Indicates that this Managed Connection Factory can be shared or not. When {@code true} configuration
     *         changes are not allowed after the first connection or datasource has been created to ensure all shared
     *         users have the same expectation of configuration.
     */
    public FBManagedConnectionFactory(boolean shared) {
        this(shared, GDSFactory.getDefaultGDSType(), null);
    }

    /**
     * Create a new FBManagedConnectionFactory based on the given GDSType.
     * <p>
     * This managed connection factory can be shared.
     * </p>
     *
     * @param gdsType
     *         The GDS implementation to use
     */
    public FBManagedConnectionFactory(GDSType gdsType) {
        this(true, gdsType);
    }

    /**
     * Create a new FBManagedConnectionFactory based on the given GDSType.
     *
     * @param shared
     *         Indicates that this Managed Connection Factory can be shared or not. When {@code true} configuration
     *         changes are not allowed after the first connection or datasource has been created to ensure all shared
     *         users have the same expectation of configuration.
     * @param gdsType
     *         The GDS implementation to use
     */
    public FBManagedConnectionFactory(boolean shared, GDSType gdsType) {
        this(shared, gdsType, null);
    }

    /**
     * Create a new FBManagedConnectionFactory based on the given GDSType and connection properties.
     * <p>
     * This managed connection factory can be shared.
     * </p>
     *
     * @param gdsType
     *         The GDS implementation to use
     * @param connectionProperties
     *         Initial connection properties (will be copied), use of {@code null} is allowed
     */
    public FBManagedConnectionFactory(GDSType gdsType, FBConnectionProperties connectionProperties) {
        this(true, gdsType, connectionProperties);
    }

    /**
     * Create a new FBManagedConnectionFactory based on the given GDSType and connection properties.
     *
     * @param shared
     *         Indicates that this Managed Connection Factory can be shared or not. When {@code true} configuration
     *         changes are not allowed after the first connection or datasource has been created to ensure all shared
     *         users have the same expectation of configuration.
     * @param gdsType
     *         The GDS implementation to use
     * @param connectionProperties
     *         Initial connection properties (will be copied), use of {@code null} is allowed
     */
    public FBManagedConnectionFactory(boolean shared, GDSType gdsType, FBConnectionProperties connectionProperties) {
        this.shared = shared;
        this.connectionProperties = connectionProperties != null
                ? (FBConnectionProperties) connectionProperties.clone()
                : new FBConnectionProperties();
        setType(gdsType.toString());
        setDefaultConnectionManager(new FBStandAloneConnectionManager());
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
        if (gdsType != null) {
            return gdsType;
        }

        gdsType = GDSType.getType(getType());
        return gdsType;
    }

    /**
     * @return {@code true} if this instance can be safely shared (modification disallowed after creation of first
     * connection/data source)
     */
    public boolean getShared() {
        return shared;
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return connectionProperties.getTransactionParameters(isolation);
    }

    public void setNonStandardProperty(String propertyMapping) {
        ensureCanModify(() -> connectionProperties.setNonStandardProperty(propertyMapping));
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        ensureCanModify(() -> connectionProperties.setTransactionParameters(isolation, tpb));
    }

    public void setDefaultConnectionManager(XcaConnectionManager defaultCm) {
        ensureCanModify(() -> {
            // Ensures that instances with different connection managers do not resolve to the same connection manager
            connectionProperties.setProperty(DEFAULT_CONNECTION_MANAGER_TYPE, defaultCm.getClass().getName());
            this.defaultCm = defaultCm;
        });
    }

    @Override
    public String getProperty(String name) {
        return connectionProperties.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value) {
        ensureCanModify(() -> {
            if (PropertyNames.type.equals(name) && gdsType != null) {
                throw new IllegalStateException("Cannot change GDS type at runtime.");
            }
            connectionProperties.setProperty(name, value);
        });
    }

    @Override
    public Integer getIntProperty(String name) {
        return connectionProperties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        ensureCanModify(() -> connectionProperties.setIntProperty(name, value));
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        return connectionProperties.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        ensureCanModify(() -> connectionProperties.setBooleanProperty(name, value));
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return connectionProperties.connectionPropertyValues();
    }

    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        if (!started) {
            return hashCodeImpl();
        }
        return hashCode = hashCodeImpl();
    }

    private int hashCodeImpl() {
        int result = connectionProperties.hashCode();
        if (result == 0) {
            return 17;
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;

        if (!(other instanceof FBManagedConnectionFactory)) return false;

        FBManagedConnectionFactory that = (FBManagedConnectionFactory) other;

        return this.connectionProperties.equals(that.connectionProperties);
    }

    public FBConnectionRequestInfo getDefaultConnectionRequestInfo() throws SQLException {
        return new FBConnectionRequestInfo(connectionProperties.asIConnectionProperties().asNewMutable());
    }

    public FBTpb getDefaultTpb() throws SQLException {
        int defaultTransactionIsolation = connectionProperties.getDefaultTransactionIsolation();
        return getTpb(defaultTransactionIsolation);
    }

    public FBTpb getTpb(int isolation) throws SQLException {
        return new FBTpb(connectionProperties.getMapper().getMapping(isolation));
    }

    /**
     * Get a copy of the current transaction mapping.
     *
     * @return Copy of the transaction mapping
     * @throws SQLException
     *         For errors on obtaining or creating the transaction mapping
     */
    FBTpbMapper getTransactionMappingCopy() throws SQLException {
        return (FBTpbMapper) connectionProperties.getMapper().clone();
    }

    /**
     * Creates a {@code javax.sql.DataSource} instance. The data source instance gets initialized with the passed
     * XcaConnectionManager.
     *
     * @param connectionManager
     *         Connection manager
     * @return data source instance
     */
    public DataSource createConnectionFactory(XcaConnectionManager connectionManager) {
        start();
        return new FBDataSource(this, connectionManager);
    }

    /**
     * Creates a {@code javax.sql.DataSource} instance. The data source instance gets initialized with a default
     * XcaConnectionManager provided by the resource adapter.
     *
     * @return data source instance
     */
    public DataSource createConnectionFactory() {
        return createConnectionFactory(defaultCm);
    }

    /**
     * Creates a new physical connection to the Firebird database using the default configuration.
     *
     * @return Managed connection instance
     * @throws SQLException
     *         generic exception
     * @see #createManagedConnection(FBConnectionRequestInfo)
     */
    public FBManagedConnection createManagedConnection() throws SQLException {
        start();
        return new FBManagedConnection(null, this);
    }

    /**
     * Creates a new physical connection to the Firebird database.
     * <p>
     * ManagedConnectionFactory uses the additional ConnectionRequestInfo to create this new connection.
     * </p>
     *
     * @param connectionRequestInfo
     *         Additional resource adapter specific connection request information, can be {@code null} for default
     * @return Managed connection instance
     * @throws SQLException
     *         generic exception
     * @see #createManagedConnection()
     */
    public FBManagedConnection createManagedConnection(FBConnectionRequestInfo connectionRequestInfo)
            throws SQLException {
        start();
        return new FBManagedConnection(connectionRequestInfo, this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * The {@code canonicalize} method is used in FBDriver to reuse previous fbmcf instances if they have been created.
     * It should really be package access level
     *
     * @return a {@code FBManagedConnectionFactory} value
     */
    public FBManagedConnectionFactory canonicalize() {
        if (!shared) {
            Logger logger = LoggerFactory.getLogger(FBManagedConnectionFactory.class);
            if (logger.isDebugEnabled()) {
                logger.debug("canonicalize called on MCF with shared=false", new RuntimeException("trace exception"));
            }
        }
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
            started = true;
            if (shared) {
                mcfInstances.put(getCacheKey(), new SoftReference<>(this, mcfReferenceQueue));
            }
        }
        cleanMcfInstances();
    }

    private void ensureCanModify(Runnable runnable) {
        synchronized (startLock) {
            if (started && shared) {
                throw new IllegalStateException(
                        "Managed connection factory is shared and already started, configuration change not allowed");
            }
            runnable.run();
            // Reset cached hash code
            hashCode = 0;
        }
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

    void notifyStart(FBManagedConnection mc, Xid xid) {
        xidMap.put(xid, mc);
    }

    void notifyEnd(FBManagedConnection mc, Xid xid) throws XAException {
        // empty
    }

    int notifyPrepare(FBManagedConnection mc, Xid xid) throws XAException {
        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null) {
            throw new FBXAException("Commit called with unknown transaction", XAException.XAER_NOTA);
        }

        return targetMc.internalPrepare(xid);
    }

    void notifyCommit(FBManagedConnection mc, Xid xid, boolean onePhase) throws XAException {
        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null) {
            tryCompleteInLimboTransaction(xid, true);
        } else {
            targetMc.internalCommit(xid, onePhase);
        }

        xidMap.remove(xid);
    }

    void notifyRollback(FBManagedConnection mc, Xid xid) throws XAException {
        FBManagedConnection targetMc = xidMap.get(xid);

        if (targetMc == null) {
            tryCompleteInLimboTransaction(xid, false);
        } else {
            targetMc.internalRollback(xid);
        }

        xidMap.remove(xid);
    }

    public void forget(FBManagedConnection mc, Xid xid) {
        xidMap.remove(xid);
    }

    public void recover(FBManagedConnection mc, Xid xid) {

    }

    /**
     * Try to complete the "in limbo" transaction. This method tries to
     * reconnect an "in limbo" transaction and complete it either by commit or
     * rollback. If no "in limbo" transaction can be found, or error happens
     * during completion, an exception is thrown.
     *
     * @param xid
     *         Xid of the transaction to reconnect.
     * @param commit
     *         {@code true} if "in limbo" transaction should be committed, otherwise {@code false}.
     * @throws XAException
     *         if "in limbo" transaction cannot be completed.
     */
    private void tryCompleteInLimboTransaction(Xid xid, boolean commit) throws XAException {
        try {
            FBManagedConnection tempMc = null;
            FBLocalTransaction tempLocalTx = null;
            try {
                tempMc = createManagedConnection();
                tempLocalTx = tempMc.getLocalTransaction();
                tempLocalTx.begin();

                long fbTransactionId = 0;
                boolean found = false;

                if (tempMc.getGDSHelper().compareToVersion(2, 0) < 0) {
                    // Find Xid by scanning
                    FBXid[] inLimboIds = (FBXid[]) tempMc.getXAResource().recover(XAResource.TMSTARTRSCAN);
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

                        FbTransaction trHandle2 = dbHandle.startTransaction(getDefaultTpb().getTransactionParameterBuffer());
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
        } catch (SQLException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
        }
    }

    FBConnection newConnection(FBManagedConnection mc) throws SQLException {
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
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Cannot instantiate connection class " + connectionClass.getName()
                    + ", no constructor accepting " + FBManagedConnection.class
                    + " class as single parameter was found.");
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }

            // TODO More specific exception, Jaybird error code
            throw new SQLException(ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Constructor for class " + connectionClass.getName() + " is not accessible.", ex);
        } catch (InstantiationException ex) {
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Cannot instantiate class" + connectionClass.getName(), ex);
        }
    }

    public FBConnectionProperties getCacheKey() {
        return (FBConnectionProperties) connectionProperties.clone();
    }

    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 1L;

        private final boolean shared;
        private final XcaConnectionManager fbCm;
        private final String type;
        private final FBConnectionProperties fbConnectionProperties;

        private SerializationProxy(FBManagedConnectionFactory connectionFactory) {
            this.shared = connectionFactory.shared;
            this.fbCm = connectionFactory.defaultCm;
            this.type = connectionFactory.getType();
            this.fbConnectionProperties = connectionFactory.connectionProperties;
        }

        protected Object readResolve() {
            GDSType gdsType = GDSType.getType(type);
            if (gdsType == null) {
                gdsType = GDSFactory.getDefaultGDSType();
            }
            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(shared, gdsType, fbConnectionProperties);
            mcf.setDefaultConnectionManager(fbCm);
            if (!shared) {
                return mcf;
            }
            FBManagedConnectionFactory canonicalizedMcf = mcf.internalCanonicalize();
            return canonicalizedMcf != null ? canonicalizedMcf : mcf;
        }
    }
}
