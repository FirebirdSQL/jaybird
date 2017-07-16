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
package org.firebirdsql.ds;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnectionProperties;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a simple implementation of {@link DataSource} interface. Connections
 * are physically opened in {@link DataSource#getConnection()} method and
 * physically closed in {@link Connection#close()} method.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBSimpleDataSource extends RootCommonDataSource implements DataSource, Serializable, Referenceable, FirebirdConnectionProperties {
    
    private static final long serialVersionUID = 3156578540634970427L;
    
    protected FBManagedConnectionFactory mcf;
    protected transient FBDataSource ds;
    
    protected Reference jndiReference;
    protected String description;

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource() {
        this(GDSFactory.getDefaultGDSType());
    }

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource(GDSType type) {
        mcf = new FBManagedConnectionFactory(type);
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
     * @param length new length of the BLOB buffer.
     */
    public void setBlobBufferLength(Integer length) {
        mcf.setBlobBufferSize(length);
    }
    
    
    
    /**
     * Get name of the database. 
     * 
     * @return database name, value is equal to the part of full JDBC URL without
     * the {@code jdbc:firebirdsql:} part.
     * 
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
     * @param name connection URL without {@code "jdbc:firebirdsql:"}
     * prefix ({@code "//localhost:3050/c:/database/employee.fdb"}) for
     * example).
     * 
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
     * @param name connection URL without {@code "jdbc:firebirdsql:"}
     * prefix ({@code "//localhost:3050/c:/database/employee.fdb"}) for
     * example).
     */
    public void setDatabase(String name) {
        mcf.setDatabase(name);
    }
    
    /**
     * Get user name that is used in {@link #getConnection()} method.
     * 
     * @return default user name.
     * 
     * @deprecated use {@link #getUserName()} instead for the sake of naming
     * compatibility.
     */
    @Deprecated
    public String getUser() {
        return getUserName();
    }
    
    /**
     * Set user name that will be used in {@link #getConnection()} method.
     * 
     * @param user default user name.
     * 
     * @deprecated use {@link #setUserName(String)} instead for the sake of
     * naming compatibility.
     */
    @Deprecated
    public void setUser(String user) {
        setUserName(user);
    }
    
    /**
     * Get user name that is used in {@link #getConnection()} method.
     * 
     * @return default user name.
     */
    public String getUserName() {
        return mcf.getUserName();
    }
    
    /**
     * Set user name that will be used in {@link #getConnection()} method.
     * 
     * @param userName default user name.
     */
    public void setUserName(String userName) {
        mcf.setUserName(userName);
    }
    
    /**
     * Get password used in {@link #getConnection()} method.
     * 
     * @return password corresponding to the user name returned by 
     * {@link #getUserName()}.
     */
    public String getPassword() {
        return mcf.getPassword();
    }
    
    /**
     * Set password that will be used in the {@link #getConnection()} method.
     * 
     * @param password password corresponding to the user name set in 
     * {@link #setUserName(String)}.
     */
    public void setPassword(String password) {
        mcf.setPassword(password);
    }
    
    /**
     * Get encoding for connections produced by this data source.
     * 
     * @return encoding for the connection.
     */
    public String getEncoding() {
        return mcf.getEncoding();
    }
    
    /**
     * Set encoding for connections produced by this data source.
     * 
     * @param encoding encoding for the connection.
     */
    public void setEncoding(String encoding) {
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

    public String getCharSet() {
        return mcf.getCharSet();
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

    public String getNonStandardProperty(String key) {
        return mcf.getNonStandardProperty(key);
    }

    public String getRoleName() {
        return mcf.getRoleName();
    }

    public int getSocketBufferSize() {
        return mcf.getSocketBufferSize();
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

    public String getUseTranslation() {
        return mcf.getUseTranslation();
    }

    public boolean isTimestampUsesLocalTimezone() {
        return mcf.isTimestampUsesLocalTimezone();
    }

    public boolean isUseStandardUdf() {
        return mcf.isUseStandardUdf();
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
        mcf.setCharSet(charSet);
    }

    public void setDefaultIsolation(String isolation) {
        mcf.setDefaultIsolation(isolation);
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        mcf.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public void setNonStandardProperty(String key, String value) {
        mcf.setNonStandardProperty(key, value);
    }

    public void setNonStandardProperty(String propertyMapping) {
        mcf.setNonStandardProperty(propertyMapping);
    }

    public void setRoleName(String roleName) {
        mcf.setRoleName(roleName);
    }

    public void setSocketBufferSize(int socketBufferSize) {
        mcf.setSocketBufferSize(socketBufferSize);
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

    public void setUseStandardUdf(boolean useStandardUdf) {
        mcf.setUseStandardUdf(useStandardUdf);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        mcf.setUseStreamBlobs(useStreamBlobs);
    }

    public void setUseTranslation(String translationPath) {
        mcf.setUseTranslation(translationPath);
    }

    public boolean isDefaultResultSetHoldable() {
        return mcf.isDefaultResultSetHoldable();
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        mcf.setDefaultResultSetHoldable(isHoldable);
    }    
    
    public int getSoTimeout() {
        return mcf.getSoTimeout();
    }

    public void setSoTimeout(int soTimeout) {
        mcf.setSoTimeout(soTimeout);
    }
    
    public int getConnectTimeout() {
        return mcf.getConnectTimeout();
    }
    
    public void setConnectTimeout(int connectTimeout) {
        mcf.setConnectTimeout(connectTimeout);
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
    public String getWireCrypt() {
        return mcf.getWireCrypt();
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        mcf.setWireCrypt(wireCrypt);
    }
    
    /*
     * INTERFACES IMPLEMENTATION
     */

    /**
     * Get previously set JNDI reference.
     * 
     * @return instance of {@link Reference} set previously.
     * 
     * @throws NamingException if something went wrong.
     */
    public Reference getReference() throws NamingException {
        return jndiReference;
    }

    /**
     * Set JNDI reference for this data source.
     * 
     * @param reference reference to set.
     */
    public void setReference(Reference reference) {
        jndiReference = reference;
    }

    /**
     * Get JDBC connection with default credentials.
     * 
     * @return new JDBC connection.
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get JDBC connection with the specified credentials.
     * 
     * @param username user name for the connection.
     * @param password password for the connection.
     * 
     * @return new JDBC connection.
     * 
     * @throws SQLException if something went wrong.
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
     * @param description description of this datasource.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get underlying connection factory (in our case instance of 
     * {@link FBDataSource} class) that will provide JDBC connections.
     * 
     * @return JDBC connection factory.
     * 
     * @throws SQLException if something went wrong.
     */
    protected synchronized DataSource getDataSource() throws SQLException {
        if (ds != null)
            return ds;
            
        if (mcf.getDatabase() == null || "".equals(mcf.getDatabase().trim()))
            throw new SQLException(
                "Database was not specified. Cannot provide connections.");
                
        try {
            ds = (FBDataSource)mcf.createConnectionFactory();
            
            return ds;
        } catch(ResourceException rex) {
            
            throw new SQLException(rex.getMessage());
            
        }
    }
    
    // JDBC 4.0
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());
        
        return iface.cast(this);
    }
}
