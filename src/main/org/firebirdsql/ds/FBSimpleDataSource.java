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
package org.firebirdsql.ds;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnectionProperties;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * This is a simple implementation of {@link DataSource} interface. Connections
 * are physically opened in {@link DataSource#getConnection()} method and
 * physically closed in {@link Connection#close()} method.
 * <p>
 * If you need a standalone connection pool, consider using a connection pool implementation like HikariCP, c3p0 or
 * DBCP.
 * </p>
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class FBSimpleDataSource extends RootCommonDataSource implements DataSource, Serializable, Referenceable,
        FirebirdConnectionProperties {

    private static final long serialVersionUID = 3156578540634970427L;
    static final String REF_DESCRIPTION = "description";
    static final String REF_MCF = "mcf";

    protected final FBManagedConnectionFactory mcf;
    protected transient FBDataSource ds;

    protected String description;

    /**
     * Creates an instance using the default GDS type (PURE_JAVA).
     */
    public FBSimpleDataSource() {
        this(GDSFactory.getDefaultGDSType());
    }

    /**
     * Creates an instance using the specified GDS type.
     *
     * @param type
     *         GDS type
     */
    public FBSimpleDataSource(GDSType type) {
        mcf = new FBManagedConnectionFactory(false, type);
    }

    /**
     * Creates an instance using an existing FBManagedConnectionFactory.
     *
     * @param mcf
     *         Managed connection factory
     * @see DataSourceFactory
     */
    FBSimpleDataSource(FBManagedConnectionFactory mcf) {
        this.mcf = mcf;
    }

    // For a lot of properties, we redirect to the default implementations of the interface to ensure the
    // data source can be introspected as a JavaBean (default methods are not returned by the introspector)

    /**
     * Get buffer length for the BLOB fields.
     *
     * @return length of BLOB buffer.
     * @deprecated Use {@link #getBlobBufferSize()}; will be removed in Jaybird 6
     */
    @Deprecated
    public Integer getBlobBufferLength() {
        return getBlobBufferSize();
    }

    /**
     * Set BLOB buffer length. This value influences the performance when
     * working with BLOB fields.
     *
     * @param length
     *         new length of the BLOB buffer.
     * @deprecated Use {@link #setBlobBufferSize(int)}; will be removed in Jaybird 6
     */
    @Deprecated
    public void setBlobBufferLength(Integer length) {
        setBlobBufferSize(length);
    }

    /**
     * Get name of the database.
     *
     * @return database name, value is equal to the part of full JDBC URL without
     * the {@code jdbc:firebirdsql:} part.
     * @deprecated use {@link #getDatabase} instead for the sake of naming
     * compatibility.
     */
    @Deprecated
    public String getDatabaseName() {
        return getDatabase();
    }

    /**
     * Set database name.
     *
     * @param name
     *         connection URL without {@code "jdbc:firebirdsql:"}
     *         prefix ({@code "//localhost:3050/c:/database/employee.fdb"} for
     *         example).
     * @deprecated use {@link #setDatabase(String)} instead for the sake of
     * naming compatibility.
     */
    @Deprecated
    public void setDatabaseName(String name) {
        setDatabase(name);
    }

    /**
     * Get name of the database.
     *
     * @return database name, value is equal to the part of full JDBC URL without
     * the {@code jdbc:firebirdsql:} part.
     */
    public String getDatabase() {
        return mcf.getDatabase();
    }

    /**
     * Set database name.
     *
     * @param name
     *         connection URL without {@code "jdbc:firebirdsql:"}
     *         prefix ({@code "//localhost:3050/c:/database/employee.fdb"}) for
     *         example).
     */
    public void setDatabase(String name) {
        mcf.setDatabase(name);
    }

    @Override
    public String getTpbMapping() {
        return FirebirdConnectionProperties.super.getTpbMapping();
    }

    @Override
    public void setTpbMapping(String tpbMapping) {
        FirebirdConnectionProperties.super.setTpbMapping(tpbMapping);
    }

    @Override
    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return mcf.getTransactionParameters(isolation);
    }

    @Override
    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        mcf.setTransactionParameters(isolation, tpb);
    }

    @Override
    public String getType() {
        return FirebirdConnectionProperties.super.getType();
    }

    @Override
    public void setType(String type) {
        FirebirdConnectionProperties.super.setType(type);
    }

    @Override
    public String getDefaultIsolation() {
        return FirebirdConnectionProperties.super.getDefaultIsolation();
    }

    @Override
    public void setDefaultIsolation(String isolation) {
        FirebirdConnectionProperties.super.setDefaultIsolation(isolation);
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return FirebirdConnectionProperties.super.getDefaultTransactionIsolation();
    }

    @Override
    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        FirebirdConnectionProperties.super.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    @Override
    public String getUser() {
        return FirebirdConnectionProperties.super.getUser();
    }

    @Override
    public void setUser(String user) {
        FirebirdConnectionProperties.super.setUser(user);
    }

    @Override
    public String getPassword() {
        return FirebirdConnectionProperties.super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        FirebirdConnectionProperties.super.setPassword(password);
    }

    @Override
    public String getRoleName() {
        return FirebirdConnectionProperties.super.getRoleName();
    }

    @Override
    public void setRoleName(String roleName) {
        FirebirdConnectionProperties.super.setRoleName(roleName);
    }

    @Override
    public String getCharSet() {
        return FirebirdConnectionProperties.super.getCharSet();
    }

    @Override
    public void setCharSet(String charSet) {
        FirebirdConnectionProperties.super.setCharSet(charSet);
    }

    @Override
    public String getEncoding() {
        return FirebirdConnectionProperties.super.getEncoding();
    }

    @Override
    public void setEncoding(String encoding) {
        FirebirdConnectionProperties.super.setEncoding(encoding);
    }

    @Override
    public Integer getProcessId() {
        return FirebirdConnectionProperties.super.getProcessId();
    }

    @Override
    public void setProcessId(Integer processId) {
        FirebirdConnectionProperties.super.setProcessId(processId);
    }

    @Override
    public String getProcessName() {
        return FirebirdConnectionProperties.super.getProcessName();
    }

    @Override
    public void setProcessName(String processName) {
        FirebirdConnectionProperties.super.setProcessName(processName);
    }

    @Override
    public int getSocketBufferSize() {
        return FirebirdConnectionProperties.super.getSocketBufferSize();
    }

    @Override
    public void setSocketBufferSize(int socketBufferSize) {
        FirebirdConnectionProperties.super.setSocketBufferSize(socketBufferSize);
    }

    @Override
    public int getSoTimeout() {
        return FirebirdConnectionProperties.super.getSoTimeout();
    }

    @Override
    public void setSoTimeout(int soTimeout) {
        FirebirdConnectionProperties.super.setSoTimeout(soTimeout);
    }

    @Override
    public int getConnectTimeout() {
        return FirebirdConnectionProperties.super.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        FirebirdConnectionProperties.super.setConnectTimeout(connectTimeout);
    }

    @Override
    public String getWireCrypt() {
        return FirebirdConnectionProperties.super.getWireCrypt();
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        FirebirdConnectionProperties.super.setWireCrypt(wireCrypt);
    }

    @Override
    public String getDbCryptConfig() {
        return FirebirdConnectionProperties.super.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        FirebirdConnectionProperties.super.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return FirebirdConnectionProperties.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        FirebirdConnectionProperties.super.setAuthPlugins(authPlugins);
    }

    @Override
    public boolean isWireCompression() {
        return FirebirdConnectionProperties.super.isWireCompression();
    }

    @Override
    public void setWireCompression(boolean wireCompression) {
        FirebirdConnectionProperties.super.setWireCompression(wireCompression);
    }

    @Override
    public int getSqlDialect() {
        return FirebirdConnectionProperties.super.getSqlDialect();
    }

    @Override
    public void setSqlDialect(int sqlDialect) {
        FirebirdConnectionProperties.super.setSqlDialect(sqlDialect);
    }

    @Override
    public int getPageCacheSize() {
        return FirebirdConnectionProperties.super.getPageCacheSize();
    }

    @Override
    public void setPageCacheSize(int pageCacheSize) {
        FirebirdConnectionProperties.super.setPageCacheSize(pageCacheSize);
    }

    @Override
    public String getDataTypeBind() {
        return FirebirdConnectionProperties.super.getDataTypeBind();
    }

    @Override
    public void setDataTypeBind(String dataTypeBind) {
        FirebirdConnectionProperties.super.setDataTypeBind(dataTypeBind);
    }

    @Override
    public String getSessionTimeZone() {
        return FirebirdConnectionProperties.super.getSessionTimeZone();
    }

    @Override
    public void setSessionTimeZone(String sessionTimeZone) {
        FirebirdConnectionProperties.super.setSessionTimeZone(sessionTimeZone);
    }

    @Override
    public int getBlobBufferSize() {
        return FirebirdConnectionProperties.super.getBlobBufferSize();
    }

    @Override
    public void setBlobBufferSize(int blobBufferSize) {
        FirebirdConnectionProperties.super.setBlobBufferSize(blobBufferSize);
    }

    @Override
    public boolean isUseStreamBlobs() {
        return FirebirdConnectionProperties.super.isUseStreamBlobs();
    }

    @Override
    public void setUseStreamBlobs(boolean useStreamBlobs) {
        FirebirdConnectionProperties.super.setUseStreamBlobs(useStreamBlobs);
    }

    @Override
    public boolean isDefaultResultSetHoldable() {
        return FirebirdConnectionProperties.super.isDefaultResultSetHoldable();
    }

    @Override
    public void setDefaultResultSetHoldable(boolean defaultResultSetHoldable) {
        FirebirdConnectionProperties.super.setDefaultResultSetHoldable(defaultResultSetHoldable);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return FirebirdConnectionProperties.super.isUseFirebirdAutocommit();
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        FirebirdConnectionProperties.super.setUseFirebirdAutocommit(useFirebirdAutocommit);
    }

    @Override
    public boolean isColumnLabelForName() {
        return FirebirdConnectionProperties.super.isColumnLabelForName();
    }

    @Override
    public void setColumnLabelForName(boolean columnLabelForName) {
        FirebirdConnectionProperties.super.setColumnLabelForName(columnLabelForName);
    }

    @Override
    public String getGeneratedKeysEnabled() {
        return FirebirdConnectionProperties.super.getGeneratedKeysEnabled();
    }

    @Override
    public void setGeneratedKeysEnabled(String generatedKeysEnabled) {
        FirebirdConnectionProperties.super.setGeneratedKeysEnabled(generatedKeysEnabled);
    }

    @Override
    public boolean isIgnoreProcedureType() {
        return FirebirdConnectionProperties.super.isIgnoreProcedureType();
    }

    @Override
    public void setIgnoreProcedureType(boolean ignoreProcedureType) {
        FirebirdConnectionProperties.super.setIgnoreProcedureType(ignoreProcedureType);
    }

    @Override
    public String getDecfloatRound() {
        return FirebirdConnectionProperties.super.getDecfloatRound();
    }

    @Override
    public void setDecfloatRound(String decfloatRound) {
        FirebirdConnectionProperties.super.setDecfloatRound(decfloatRound);
    }

    @Override
    public String getDecfloatTraps() {
        return FirebirdConnectionProperties.super.getDecfloatTraps();
    }

    @Override
    public void setDecfloatTraps(String decfloatTraps) {
        FirebirdConnectionProperties.super.setDecfloatTraps(decfloatTraps);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public boolean isTimestampUsesLocalTimezone() {
        return FirebirdConnectionProperties.super.isTimestampUsesLocalTimezone();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        FirebirdConnectionProperties.super.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
    }

    public void setNonStandardProperty(String propertyMapping) {
        mcf.setNonStandardProperty(propertyMapping);
    }

    @Override
    public String getProperty(String name) {
        return mcf.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value) {
        mcf.setProperty(name, value);
    }

    @Override
    public Integer getIntProperty(String name) {
        return mcf.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        mcf.setIntProperty(name, value);
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        return mcf.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        mcf.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return mcf.connectionPropertyValues();
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(FBSimpleDataSource.class.getName(), DataSourceFactory.class.getName(), null);
        ref.add(new StringRefAddr(REF_DESCRIPTION, getDescription()));
        byte[] data = DataSourceFactory.serialize(mcf);
        ref.add(new BinaryRefAddr(REF_MCF, data));
        return ref;
    }

    /**
     * Get JDBC connection with default credentials.
     *
     * @return new JDBC connection.
     * @throws SQLException
     *         if something went wrong.
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get JDBC connection with the specified credentials.
     *
     * @param username
     *         user name for the connection.
     * @param password
     *         password for the connection.
     * @return new JDBC connection.
     * @throws SQLException
     *         if something went wrong.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    public int getLoginTimeout() throws SQLException {
        return getConnectTimeout();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        setConnectTimeout(loginTimeout);
    }

    /**
     * Get description of this datasource.
     *
     * @return description of this datasource.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description of this datasource.
     *
     * @param description
     *         description of this datasource.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get underlying connection factory (in our case instance of {@link FBDataSource} class) that will provide JDBC
     * connections.
     *
     * @return JDBC connection factory.
     * @throws SQLException
     *         if something went wrong.
     */
    protected synchronized DataSource getDataSource() throws SQLException {
        if (ds != null) {
            return ds;
        }

        if (mcf.getDatabase() == null || "".equals(mcf.getDatabase().trim())) {
            throw new SQLException("Database was not specified. Cannot provide connections.");
        }

        ds = (FBDataSource) mcf.createConnectionFactory();

        return ds;
    }

    // JDBC 4.0

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException("Unable to unwrap to class " + iface.getName());
        }

        return iface.cast(this);
    }
}
