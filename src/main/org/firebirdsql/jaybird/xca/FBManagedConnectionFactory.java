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
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.firebirdsql.jdbc.*;

import javax.sql.DataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * @author David Jencks
 * @author Mark Rotteveel
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
    /**
     * Suffix of properties that are applied when creating a database if createDatabaseIfNotExist = true.
     */
    private static final String DB_CREATE_PROPERTY_SUFFIX = "@create";

    private XcaConnectionManager defaultCm;
    private int hashCode;
    private GDSType gdsType;

    // Maps supplied XID to internal transaction handle.
    private final Map<Xid, FBManagedConnection> xidMap = new ConcurrentHashMap<>();

    @SuppressWarnings("java:S1948" /* we're using a serialization proxy */)
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
        return other instanceof FBManagedConnectionFactory that
               && this.connectionProperties.equals(that.connectionProperties);
    }

    public FBConnectionRequestInfo getDefaultConnectionRequestInfo() throws SQLException {
        return new FBConnectionRequestInfo(connectionProperties.asIConnectionProperties().asNewMutable());
    }

    public TransactionParameterBuffer getDefaultTpb() throws SQLException {
        return getTpb(connectionProperties.getDefaultTransactionIsolation());
    }

    public TransactionParameterBuffer getTpb(int isolation) throws SQLException {
        // getMapping makes a defensive copy
        return connectionProperties.getMapper().getMapping(isolation);
    }

    /**
     * Get a copy of the current transaction mapping.
     *
     * @return Copy of the transaction mapping
     * @throws SQLException
     *         For errors on obtaining or creating the transaction mapping
     */
    FBTpbMapper getTransactionMappingCopy() throws SQLException {
        return FBTpbMapper.copyOf(connectionProperties.getMapper());
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
        return createManagedConnection(null);
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
        if (connectionRequestInfo == null) {
            connectionRequestInfo = getDefaultConnectionRequestInfo();
        }
        try {
            return new FBManagedConnection(connectionRequestInfo, this);
        } catch (SQLException e) {
            return createNewDatabaseIfRequested(connectionRequestInfo, e);
        }
    }

    private FBManagedConnection createNewDatabaseIfRequested(FBConnectionRequestInfo originalCri,
            SQLException originalConnectionFailure) throws SQLException {
        IConnectionProperties props = originalCri.asIConnectionProperties();
        if (!(props.isCreateDatabaseIfNotExist() && signalsDatabaseDoesNotExist(originalConnectionFailure))) {
            throw originalConnectionFailure;
        }

        try {
            return new FBManagedConnection(createDatabaseConnectionRequestInfo(props), this, true);
        } catch (SQLException e) {
            e.addSuppressed(originalConnectionFailure);
            throw e;
        }
    }

    // Error codes that report connection failures not related to the existence of the database
    private static final int[] ERROR_CODES_NOT_RELATED_TO_DB_EXISTENCE = {
            ISCConstants.isc_login, ISCConstants.isc_network_error, ISCConstants.isc_connect_reject,
            ISCConstants.isc_net_read_err, ISCConstants.isc_net_write_err, ISCConstants.isc_net_connect_err };
    static {
        Arrays.sort(ERROR_CODES_NOT_RELATED_TO_DB_EXISTENCE);
    }

    /**
     * Checks if {@code exception} signals that the database possibly does not exist.
     *
     * @param exception
     *         exception
     * @return {@code true} if the exception signals that the database possibly does not exist
     */
    private static boolean signalsDatabaseDoesNotExist(SQLException exception) {
        int errorCode = exception.getErrorCode();
        // TODO In case of isc_io_error, check message for OS errors that indicate the DB does exist?
        return errorCode == ISCConstants.isc_io_error
               || Arrays.binarySearch(ERROR_CODES_NOT_RELATED_TO_DB_EXISTENCE, errorCode) < 0;
    }

    private static FBConnectionRequestInfo createDatabaseConnectionRequestInfo(
            IConnectionProperties originalProperties) {
        IConnectionProperties newProperties = originalProperties.asNewMutable();
        for (Map.Entry<ConnectionProperty, Object> entry : originalProperties.connectionPropertyValues().entrySet()) {
            asCreateOverrideProperty(entry.getKey()).map(ConnectionProperty::name).ifPresent(name -> {
                Object value = entry.getValue();
                if (value == null) {
                    newProperties.setProperty(name, null);
                } else if (value instanceof String s) {
                    newProperties.setProperty(name, s);
                } else if (value instanceof Boolean b) {
                    newProperties.setBooleanProperty(name, b);
                } else if (value instanceof Integer i) {
                    newProperties.setIntProperty(name, i);
                } else {
                    // Not expected to occur, but just in case
                    newProperties.setProperty(name, String.valueOf(value));
                }
            });
        }
        return new FBConnectionRequestInfo(newProperties);
    }

    // Property names which are not allowed to be overridden when creating a database
    private static final Set<String> DISALLOW_CREATE_OVERRIDE =
            Set.of(PropertyNames.attachObjectName, PropertyNames.serverName, PropertyNames.portNumber);

    /**
     * Returns the property to override if the name of {@code property} ends in {@code @create} and is allowed to be
     * overridden.
     *
     * @param property
     *         property
     * @return the property to be overridden, or empty if this is not an {@code @create} property, or if the property is
     * not allowed to be overridden
     */
    private static Optional<ConnectionProperty> asCreateOverrideProperty(ConnectionProperty property) {
        String originalName = property.name();
        if (originalName.endsWith(DB_CREATE_PROPERTY_SUFFIX)) {
            String name = originalName.substring(0, originalName.length() - DB_CREATE_PROPERTY_SUFFIX.length());
            ConnectionProperty override = ConnectionPropertyRegistry.getInstance().getOrUnknown(name);
            if (DISALLOW_CREATE_OVERRIDE.contains(override.name())) {
                return Optional.empty();
            }
            return Optional.of(override);
        }
        return Optional.empty();
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    @Serial
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
            var logger = System.getLogger(FBManagedConnectionFactory.class.getName());
            if (logger.isLoggable(System.Logger.Level.DEBUG)) {
                logger.log(System.Logger.Level.DEBUG, "canonicalize called on MCF with shared=false",
                        new RuntimeException("trace exception"));
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

    void notifyEnd(FBManagedConnection mc, Xid xid) {
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
        try {
            FBManagedConnection targetMc = xidMap.get(xid);
            if (targetMc == null) {
                tryCompleteInLimboTransaction(xid, true);
            } else {
                targetMc.internalCommit(xid, onePhase);
            }
        } finally {
            forget(mc, xid);
        }
    }

    void notifyRollback(FBManagedConnection mc, Xid xid) throws XAException {
        try {
            FBManagedConnection targetMc = xidMap.get(xid);
            if (targetMc == null) {
                tryCompleteInLimboTransaction(xid, false);
            } else {
                targetMc.internalRollback(xid);
            }
        } finally {
            forget(mc, xid);
        }
    }

    public void forget(FBManagedConnection mc, Xid xid) {
        xidMap.remove(xid);
    }

    public void recover(FBManagedConnection mc, Xid xid) {
        // nothing to do
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
    @SuppressWarnings("java:S1141")
    private void tryCompleteInLimboTransaction(Xid xid, boolean commit) throws XAException {
        try {
            FBManagedConnection tempMc = null;
            FBLocalTransaction tempLocalTx = null;
            try {
                tempMc = createManagedConnection();
                tempLocalTx = tempMc.getLocalTransaction();
                tempLocalTx.begin();

                long fbTransactionId = findTransaction(tempMc, xid);
                if (fbTransactionId == -1) {
                    throw new FBXAException((commit ? "Commit" : "Rollback") + " called with unknown transaction.",
                            XAException.XAER_NOTA);
                }

                GDSHelper gdsHelper = tempMc.getGDSHelper();
                completeTransaction(gdsHelper, fbTransactionId, commit);
                tryDeleteTransactionInfo(gdsHelper, fbTransactionId);
            } catch (SQLException e) {
                throw new FBXAException("unable to complete in limbo transaction",
                        determineLimboCompletionErrorCode(e), e);
            } finally {
                try {
                    if (tempLocalTx != null && tempLocalTx.inTransaction()) {
                        tempLocalTx.commit();
                    }
                } finally {
                    if (tempMc != null) tempMc.destroy();
                }
            }
        } catch (SQLException ex) {
            throw new FBXAException(XAException.XAER_RMERR, ex);
        }
    }

    /**
     * Finds the transaction id associated with {@code xid}.
     *
     * @param xid
     *         XID to find
     * @param mc
     *         managed connection to use for the query
     * @return the transaction id, or {@code -1} if no transaction was found for {@code xid}
     */
    private static long findTransaction(FBManagedConnection mc, Xid xid) throws SQLException, XAException {
        if (mc.getGDSHelper().compareToVersion(2) < 0) {
            // Find Xid by scanning
            FBXid[] inLimboIds = (FBXid[]) mc.getXAResource().recover(XAResource.TMSTARTRSCAN);
            for (FBXid inLimboId : inLimboIds) {
                if (inLimboId.equals(xid)) {
                    return inLimboId.getFirebirdTransactionId();
                }
            }
        } else {
            // Find Xid by intelligent scan
            FBXid foundXid = (FBXid) mc.findSingleXid(xid);
            if (foundXid != null && foundXid.equals(xid)) {
                return foundXid.getFirebirdTransactionId();
            }
        }
        return -1;
    }

    /**
     * Completes the specified transaction id by reconnecting and commiting or rolling back.
     *
     * @param gdsHelper
     *         GDS helper
     * @param fbTransactionId
     *         transaction id
     * @param commit
     *         {@code true} to complete by <em>commit</em>, {@code false} to complete by <em>rollback</em>
     * @throws SQLException
     *         if it is not possible to reconnect the transaction, or the commit or rollback fails
     */
    private static void completeTransaction(GDSHelper gdsHelper, long fbTransactionId, boolean commit)
            throws SQLException {
        FbDatabase dbHandle = gdsHelper.getCurrentDatabase();
        FbTransaction trHandle = dbHandle.reconnectTransaction(fbTransactionId);
        // complete transaction by commit or rollback
        if (commit) {
            trHandle.commit();
        } else {
            trHandle.rollback();
        }
    }

    /**
     * Attempts to delete the transaction information from the database (only on Firebird 2.5 and older).
     *
     * @param gdsHelper
     *         GDS helper
     * @param fbTransactionId
     *         transaction id to attempt to delete
     * @throws FBXAException
     *         if an attempt was made to delete, and this failed with an exception
     */
    private void tryDeleteTransactionInfo(GDSHelper gdsHelper, long fbTransactionId) throws FBXAException {
        if (gdsHelper.compareToVersion(3) >= 0) return;
        // remove heuristic data from rdb$transactions (only possible in versions before Firebird 3)
        try {
            String query = "delete from rdb$transactions where rdb$transaction_id = " + fbTransactionId;

            FbDatabase dbHandle = gdsHelper.getCurrentDatabase();
            FbTransaction trHandle = dbHandle.startTransaction(getDefaultTpb());
            try (FbStatement stmtHandle = dbHandle.createStatement(trHandle)) {
                stmtHandle.prepare(query);
                stmtHandle.execute(RowValue.EMPTY_ROW_VALUE);
            } finally {
                trHandle.commit();
            }
        } catch (SQLException sqle) {
            throw new FBXAException("unable to remove in limbo transaction from rdb$transactions where rdb$transaction_id = " + fbTransactionId, XAException.XAER_RMERR);
        }
    }

    private static int determineLimboCompletionErrorCode(SQLException ex) {
        /* if ex.getIntParam() is 335544353 (transaction is not in limbo) and next ex.getIntParam() is 335544468
        (transaction {0} is {1})  => detected heuristic */
        // TODO: We may need to parse the exception to get the details (or we need to handle this specific one differently)
        if (ex.getErrorCode() == ISCConstants.isc_no_recon) {
            String message = ex.getMessage();
            if (message.contains("committed") || message.contains("rolled back")) {
                return XAException.XA_HEURCOM;
            }
        }
        return XAException.XAER_RMERR;
    }

    FBConnection newConnection(FBManagedConnection mc) throws SQLException {
        Class<?> connectionClass = GDSFactory.getConnectionClass(getGDSType());
        return connectionClass == FBConnection.class ? new FBConnection(mc) : newConnection(mc, connectionClass);
    }

    private static FBConnection newConnection(FBManagedConnection mc, Class<?> connectionClass) throws SQLException {
        if (!FBConnection.class.isAssignableFrom(connectionClass)) {
            throw new IllegalArgumentException(
                    "Specified connection class does not extend " + FBConnection.class.getName() + " class");
        }

        try {
            Constructor<?> constructor = connectionClass.getConstructor(FBManagedConnection.class);
            return (FBConnection) constructor.newInstance(mc);
        } catch (NoSuchMethodException ex) {
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Cannot instantiate connection class " + connectionClass.getName()
                                   + ", no constructor accepting " + FBManagedConnection.class
                                   + " class as single parameter was found.");
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            } else if (cause instanceof Error error) {
                throw error;
            } else if (cause instanceof SQLException sqle) {
                throw sqle;
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

        @Serial
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

        @Serial
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
