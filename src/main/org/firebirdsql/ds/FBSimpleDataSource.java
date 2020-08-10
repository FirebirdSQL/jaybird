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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnectionProperties;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

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

    /**
     * Get buffer length for the BLOB fields.
     *
     * @return length of BLOB buffer.
     */
    public Integer getBlobBufferLength() {
        return mcf.getBlobBufferSize();
    }

    /**
     * Set BLOB buffer length. This value influences the performance when
     * working with BLOB fields.
     *
     * @param length
     *         new length of the BLOB buffer.
     */
    public void setBlobBufferLength(Integer length) {
        mcf.setBlobBufferSize(length);
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
     *         prefix ({@code "//localhost:3050/c:/database/employee.fdb"}) for
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

    /**
     * Set encoding for connections produced by this data source.
     *
     * @param encoding
     *         encoding for the connection.
     */
    public void setEncoding(String encoding) {
        // TODO Remove when logic in FBConnectionProperties rewritten
        mcf.setEncoding(encoding);
    }

    public String getTpbMapping() {
        return mcf.getTpbMapping();
    }

    public void setTpbMapping(String tpbMapping) {
        mcf.setTpbMapping(tpbMapping);
    }

    public int getBlobBufferSize() {
        return mcf.getBlobBufferSize();
    }

    public int getBuffersNumber() {
        return mcf.getBuffersNumber();
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        return mcf.getDatabaseParameterBuffer();
    }

    public String getDefaultIsolation() {
        return mcf.getDefaultIsolation();
    }

    public int getDefaultTransactionIsolation() {
        return mcf.getDefaultTransactionIsolation();
    }

    public String getSqlDialect() {
        return mcf.getSqlDialect();
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return mcf.getTransactionParameters(isolation);
    }

    public String getType() {
        return mcf.getType();
    }

    public boolean isTimestampUsesLocalTimezone() {
        return mcf.isTimestampUsesLocalTimezone();
    }

    public boolean isUseStreamBlobs() {
        return mcf.isUseStreamBlobs();
    }

    public void setBlobBufferSize(int bufferSize) {
        mcf.setBlobBufferSize(bufferSize);
    }

    public void setBuffersNumber(int buffersNumber) {
        mcf.setBuffersNumber(buffersNumber);
    }

    public void setCharSet(String charSet) {
        // TODO Remove when logic in FBConnectionProperties rewritten
        mcf.setCharSet(charSet);
    }

    public void setDefaultIsolation(String isolation) {
        mcf.setDefaultIsolation(isolation);
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        mcf.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    @Deprecated
    public void setNonStandardProperty(String key, String value) {
        // TODO Remove when logic in FBConnectionProperties rewritten
        mcf.setNonStandardProperty(key, value);
    }

    public void setNonStandardProperty(String propertyMapping) {
        mcf.setNonStandardProperty(propertyMapping);
    }

    public void setSqlDialect(String sqlDialect) {
        mcf.setSqlDialect(sqlDialect);
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        mcf.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        mcf.setTransactionParameters(isolation, tpb);
    }

    public void setType(String type) {
        mcf.setType(type);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        mcf.setUseStreamBlobs(useStreamBlobs);
    }

    public boolean isDefaultResultSetHoldable() {
        return mcf.isDefaultResultSetHoldable();
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        mcf.setDefaultResultSetHoldable(isHoldable);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return mcf.isUseFirebirdAutocommit();
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        mcf.setUseFirebirdAutocommit(useFirebirdAutocommit);
    }

    @Override
    public String getGeneratedKeysEnabled() {
        return mcf.getGeneratedKeysEnabled();
    }

    @Override
    public void setGeneratedKeysEnabled(String generatedKeysEnabled) {
        mcf.setGeneratedKeysEnabled(generatedKeysEnabled);
    }

    @Override
    public String getDataTypeBind() {
        return mcf.getDataTypeBind();
    }

    @Override
    public void setDataTypeBind(String dataTypeBind) {
        mcf.setDataTypeBind(dataTypeBind);
    }

    @Override
    public String getSessionTimeZone() {
        return mcf.getSessionTimeZone();
    }

    @Override
    public void setSessionTimeZone(String sessionTimeZone) {
        mcf.setSessionTimeZone(sessionTimeZone);
    }

    @Override
    public boolean isIgnoreProcedureType() {
        return mcf.isIgnoreProcedureType();
    }

    @Override
    public void setIgnoreProcedureType(boolean ignoreProcedureType) {
        mcf.setIgnoreProcedureType(ignoreProcedureType);
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
