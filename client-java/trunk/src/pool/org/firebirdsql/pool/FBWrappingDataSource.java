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
package org.firebirdsql.pool;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.resource.Referenceable;
import javax.sql.DataSource;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBConnectionProperties;
import org.firebirdsql.jdbc.FBDriverNotCapableException;

/**
 * Implementation of {@link javax.sql.DataSource} including connection pooling.
 * Following properties are supported:
 * <ul>
 * <li><code>blobBufferSize</code> 
 *      size of the buffer used to transfer BLOB data.
 * 
 * <li><code>blockingTimeout</code> 
 *      time in milliseconds during which {@link #getConnection()} method will 
 *      block if no free connection is in pool.
 * 
 * <li><code>charSet</code>
 *      similar to <code>encoding</code>, but takes Java character set name
 *      instead of Firebird's encoding.
 * 
 * <li><code>database</code> 
 *      path to a database including the server name; for example 
 *      <code>localhost/3050:c:/path/to/database.gdb</code>.
 * 
 * <li><code>encoding</code> 
 *      character encoding for the JDBC connection.
 * 
 * <li><code>freeSize</code>
 *      read-only: gives amount of free connections in the pool, when 0, blocking
 *      will occur if <code>workingSize</code> is equal to <code>maxPoolSize</code>.
 * 
 * <li><code>isolation</code>
 *      default transaction isolation level for connections as string; possible
 *      values are:
 *      <ul>
 *      <li>TRANSACTION_READ_COMMITTED
 *      <li>TRANSACTION_REPEATABLE_READ
 *      <li>TRANSACTION_SERIALIZABLE
 *      </ul>
 * 
 * <li><code>loginTimeout</code> 
 *      property from {@link javax.sql.DataSource}, in this context is a synonym 
 *      for <code>blockingTimeout</code> (however value is specified in seconds).
 * 
 * <li><code>maxIdleTime</code> 
 *      time in milliseconds after which idle physical connection in the 
 *      pool is closed.
 * 
 * <li><code>maxStatements</code>
 *      maximum number of pooled prepared statements, if 0, pooling is switched
 *      off.
 * 
 * <li><code>maxPoolSize</code> 
 *      maximum number of physical connections that can be opened by this data 
 *      source.
 * 
 * <li><code>minPoolSize</code> 
 *      minimum number of connections that will remain open by this data source.
 * 
 * <li><code>nonStandardProperty</code>
 *      a non-standard connection parameter in form <code>name[=value]</code>.
 * 
 * <li><code>password</code> 
 *      password that is used to connect to database.
 * 
 * <li><code>pingInterval</code> 
 *      time interval during which connection will be proved for aliveness.
 * 
 * <li><code>pooling</code>
 *      allows switching pooling off.
 * 
 * <li><code>statementPooling</code>
 *      alternative way to switch statement pooling off.
 * 
 * <li><code>socketBufferSize</code> 
 *      size of the socket buffer in bytes. In some cases values used by JVM by 
 *      default are not optimal. This results in performance degradation 
 *      (especially when you transfer big BLOBs). Usually 8192 bytes provides 
 *      good results.
 * 
 * <li><code>roleName</code> 
 *      SQL role name.
 * 
 * <li><code>tpbMapping</code> 
 *      mapping of the TPB parameters to JDBC transaction isolation levels.
 * 
 * <li><code>transactionIsolationLevel</code>
 *      default transaction isolation level, number from {@link java.sql.Connection}
 *      interface.
 * 
 * <li><code>totalSize</code>
 *      total number of allocated connections.
 * 
 * <li><code>type</code> 
 *      type of connection that will be created. There are four possible types: 
 *      pure Java (or type 4), type 2 that will use Firebird client library to 
 *      connect to the database, local-mode type 2 driver, and embedded that 
 *      will use embedded engine (access to local databases). Possible values 
 *      are (case insensitive):
 *      <ul> 
 *      <li><code>"PURE_JAVA"</code> or <code>"TYPE4"</code> 
 *          for pure Java (type 4) JDBC connections;
 * 
 *      <li><code>"NATIVE"</code> or <code>"TYPE2"</code> 
 *          to use Firebird client library;
 * 
 *      <li><code>"LOCAL"</code> 
 *          to use Firebird client library in local-mode (IPC link to server);
 * 
 *      <li><code>"EMBEDDED"</code> 
 *          to use embedded engine.
 *      </ul>
 * 
 * <li><code>userName</code> 
 *      name of the user that will be used to access the database.
 * 
 * <li><code>workingSize</code>
 *      number of connections that are in use (e.g. were obtained using
 *      {@link #getConnection()} method, but not yet closed).
 * </ul>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBWrappingDataSource extends RootCommonDataSource implements DataSource, 
    ObjectFactory, Referenceable, Serializable, FirebirdPool {

    private static final long serialVersionUID = -2282667414407311473L;

    private AbstractFBConnectionPoolDataSource pool;
    
    private Reference reference;

    private String description;
    
    /**
     * Create instance of this class.
     */
    public FBWrappingDataSource() {
        // empty
    }
    
    private synchronized AbstractFBConnectionPoolDataSource getPool() {
        if (pool == null)
            pool = FBPooledDataSourceFactory.createFBConnectionPoolDataSource();
            
        return pool;
    }

    /**
     * Finalize this instance. This method will shut the pool down.
     * 
     * @throws Throwable if something went wrong.
     */
    protected void finalize() throws Throwable {
        if (pool != null) {
            pool.shutdown();

        }
        super.finalize();
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.pool.FirebirdPool#restart()
     */
    public void restart() {
    	if (pool != null)
    		pool.restart();
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.pool.FirebirdPool#shutdown()
     */
    public void shutdown() {
        if (pool != null)
            pool.shutdown();
    }

    /**
     * Get JDBC connection from this data source.
     * 
     * @return instance of {@link Connection}.
     * 
     * @throws SQLException if connection cannot be obtained due to some reason.
     */
    public Connection getConnection() throws SQLException {
        return getPool().getPooledConnection().getConnection();
    }

    /**
     * Get JDBC connection for the specified user name and password.
     * 
     * @return instance of {@link Connection}
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection(String user, String password) 
        throws SQLException 
    {
        return getPool().getPooledConnection(user, password).getConnection();
    }

    /**
     * Get login timeout.
     * 
     * @return login timeout.
     */
    public int getLoginTimeout() {
        return getBlockingTimeout() * 1000;
    }

    /**
     * Get log writer.
     * 
     * @return instance of {@link PrintWriter}.
     */
    public PrintWriter getLogWriter() {
        return getPool().getLogWriter();
    }

    /**
     * Set login timeout.
     * 
     * @param seconds login timeout.
     */
    public void setLoginTimeout(int seconds) {
        setBlockingTimeout(seconds * 1000);
    }

    /**
     * Set log writer.
     * 
     * @param printWriter instance of {@link PrintWriter}.
     */
    public void setLogWriter(PrintWriter printWriter) {
        getPool().setLogWriter(printWriter);
    }

    /*
     * Properties of this datasource.
     */

    public int getBlockingTimeout() {
        return getPool().getBlockingTimeout();
    }

    public void setBlockingTimeout(int blockingTimeoutValue) {
        getPool().setBlockingTimeout(blockingTimeoutValue);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionValue) {
        this.description = descriptionValue;
    }

    public int getMaxIdleTime() {
        return getPool().getMaxIdleTime();
    }
    
    public void setMaxIdleTime(int maxIdleTime) {
        getPool().setMaxIdleTime(maxIdleTime);
    }
    
    /**
     * @deprecated non-standard name, use {@link #getMaxIdleTime()}.
     */
    public int getIdleTimeout() {
        return getPool().getIdleTimeout();
    }

    /**
     * @deprecated non-standard name, use {@link #setMaxIdleTime(int)}.
     */
    public void setIdleTimeout(int idleTimeoutValue) {
        getPool().setIdleTimeout(idleTimeoutValue);
    }

    public int getMaxStatements() {
        return getPool().getMaxStatements();
    }
    
    public void setMaxStatements(int maxStatements) {
        getPool().setMaxStatements(maxStatements);
    }
    
    public int getMaxPoolSize() {
        return getPool().getMaxPoolSize();
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        getPool().setMaxPoolSize(maxPoolSize);
    }
    
    /**
     * @deprecated non-standard name, use {@link #getMaxPoolSize()}.
     */
    public int getMaxConnections() {
        return getPool().getMaxConnections();
    }
    
    /**
     * @deprecated non-standard name, use {@link #setMaxPoolSize(int)}.
     */
    public void setMaxConnections(int maxConnections) {
        getPool().setMaxConnections(maxConnections);
    }
    
    public int getMinPoolSize() {
        return getPool().getMinPoolSize();
    }
    
    public void setMinPoolSize(int minPoolSize) {
        getPool().setMinPoolSize(minPoolSize);
    }
    
    /**
     * @deprecated non-standard name, use {@link #getMinPoolSize()}
     */
    public int getMinConnections() {
        return getMinPoolSize();
    }
    
    /**
     * @deprecated non-standard name, use {@link #setMinPoolSize(int)}
     */
    public void setMinConnections(int minConnections) {
        setMinPoolSize(minConnections);
    }
    
    public boolean isKeepStatements() {
        return getPool().isKeepStatements();
    }
    
    public void setKeepStatements(boolean keepStatements) {
        getPool().setKeepStatements(keepStatements);
    }
    
    public int getPingInterval() {
        return getPool().getPingInterval();
    }

    public void setPingInterval(int pingIntervalValue) {
        getPool().setPingInterval(pingIntervalValue);
    }
    
    public String getPingStatement() {
        return getPool().getPingStatement();
    }

    public void setPingStatement(String pingStatement) {
        getPool().setPingStatement(pingStatement);
    }
    
    public int getRetryInterval() {
        return getPool().getRetryInterval();
    }
    
    public void setRetryInterval(int retryInterval) {
        getPool().setRetryInterval(retryInterval);
    }

    public boolean isPingable() {
        return getPool().isPingable();
    }

    /**
     * @deprecated please use {@link #getRoleName()} instead.
     */
    public String getSqlRole() {
        return getRoleName();
    }

    /**
     * @deprecated please use {@link #setRoleName(String)} instead. 
     */
    public void setSqlRole(String sqlRole) {
        setRoleName(sqlRole);
    }

    /**
     * @deprecated use {@link #isPooling()} method.
     */
    public boolean getPooling() {
        return getPool().isPooling();
    }
    
    public boolean isPooling() {
        return getPool().isPooling();
    }
    
    public void setPooling(boolean pooling) {
        getPool().setPooling(pooling);
    }
    
    public boolean isStatementPooling() {
        return getPool().isStatementPooling();
    }
    
    public void setStatementPooling(boolean statementPooling) {
        getPool().setStatementPooling(statementPooling);
    }
    
    /**
     * @deprecated Confusing name. Use {@link #getFreeSize()} instead.
     */
    public int getConnectionCount() throws SQLException {
        return getPool().getFreeSize();
    }
    
    public int getFreeSize() throws SQLException {
        return getPool().getFreeSize();
    }
    
    public int getWorkingSize() throws SQLException {
        return getPool().getWorkingSize();
    }
    
    public int getTotalSize() throws SQLException {
        return getPool().getTotalSize();
    }
    
    /**
     * @deprecated Use {@link #getDefaultTransactionIsolation()} instead.
     */
    public int getTransactionIsolationLevel() {
        return getDefaultTransactionIsolation();
    }
    
    /**
     * @deprecated Use {@link #setDefaultTransactionIsolation(int)} instead.
     */
    public void setTransactionIsolationLevel(int level) {
        setDefaultTransactionIsolation(level);
    }
    
    /**
     * @deprecated Use {@link #getDefaultIsolation()} instead.
     */
    public String getIsolation() {
        return getDefaultIsolation();
    }
    
    /**
     * @deprecated Use {@link #setDefaultIsolation(String)} instead.
     */
    public void setIsolation(String isolation) throws SQLException {
        setDefaultIsolation(isolation);
    }
    
    public void setProperties(Properties props) {
        getPool().setProperties(props);
    }
    
    public int getBlobBufferSize() {
        return getPool().getBlobBufferSize();
    }

    public int getBuffersNumber() {
        return getPool().getBuffersNumber();
    }

    public String getCharSet() {
        return getPool().getCharSet();
    }

    public String getDatabase() {
        return getPool().getDatabase();
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        return getPool().getDatabaseParameterBuffer();
    }

    public String getDefaultIsolation() {
        return getPool().getDefaultIsolation();
    }

    public int getDefaultTransactionIsolation() {
        return getPool().getDefaultTransactionIsolation();
    }

    public String getEncoding() {
        return getPool().getEncoding();
    }

    public String getNonStandardProperty(String key) {
        return getPool().getNonStandardProperty(key);
    }

    public String getPassword() {
        return getPool().getPassword();
    }

    public String getRoleName() {
        return getPool().getRoleName();
    }

    public int getSocketBufferSize() {
        return getPool().getSocketBufferSize();
    }

    public String getSqlDialect() {
        return getPool().getSqlDialect();
    }

    public String getTpbMapping() {
        return getPool().getTpbMapping();
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return getPool().getTransactionParameters(isolation);
    }

    public String getType() {
        return getPool().getType();
    }

    public String getUserName() {
        return getPool().getUserName();
    }

    public String getUseTranslation() {
        return getPool().getUseTranslation();
    }

    public boolean isTimestampUsesLocalTimezone() {
        return getPool().isTimestampUsesLocalTimezone();
    }

    public boolean isUseStandardUdf() {
        return getPool().isUseStandardUdf();
    }

    public boolean isUseStreamBlobs() {
        return getPool().isUseStreamBlobs();
    }

    public void setBlobBufferSize(int bufferSize) {
        getPool().setBlobBufferSize(bufferSize);
    }

    public void setBuffersNumber(int buffersNumber) {
        getPool().setBuffersNumber(buffersNumber);
    }

    public void setCharSet(String charSet) {
        getPool().setCharSet(charSet);
    }

    public void setDatabase(String database) {
        getPool().setDatabase(database);
    }

    public void setDefaultIsolation(String isolation) {
        getPool().setDefaultIsolation(isolation);
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        getPool().setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public void setEncoding(String encoding) {
        getPool().setEncoding(encoding);
    }

    public void setNonStandardProperty(String key, String value) {
        getPool().setNonStandardProperty(key, value);
    }

    public void setNonStandardProperty(String propertyMapping) {
        getPool().setNonStandardProperty(propertyMapping);
    }

    public void setPassword(String password) {
        getPool().setPassword(password);
    }

    public void setRoleName(String roleName) {
        getPool().setRoleName(roleName);
    }

    public void setSocketBufferSize(int socketBufferSize) {
        getPool().setSocketBufferSize(socketBufferSize);
    }

    public void setSqlDialect(String sqlDialect) {
        getPool().setSqlDialect(sqlDialect);
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        getPool().setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
    }

    public void setTpbMapping(String tpbMapping) {
        getPool().setTpbMapping(tpbMapping);
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        getPool().setTransactionParameters(isolation, tpb);
    }

    public void setType(String type) {
        getPool().setType(type);
    }

    public void setUserName(String userName) {
        getPool().setUserName(userName);
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        getPool().setUseStandardUdf(useStandardUdf);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        getPool().setUseStreamBlobs(useStreamBlobs);
    }

    public void setUseTranslation(String translationPath) {
        getPool().setUseTranslation(translationPath);
    }
    
    public boolean isDefaultResultSetHoldable() {
        return getPool().isDefaultResultSetHoldable();
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        getPool().setDefaultResultSetHoldable(isHoldable);
    }
    
    public int getSoTimeout() {
        return getPool().getSoTimeout();
    }

    public void setSoTimeout(int soTimeout) {
        getPool().setSoTimeout(soTimeout);
    }
    

    /*
     * JNDI-related code. 
     */


    private static final String REF_BLOCKING_TIMEOUT = "blockingTimeout";
//    private static final String REF_DATABASE = "database";
    private static final String REF_DESCRIPTION = "description";
    private static final String REF_MAX_IDLE_TIME = "maxIdleTime";
    private static final String REF_IDLE_TIMEOUT = "idleTimeout";
    private static final String REF_LOGIN_TIMEOUT = "loginTimeout";
    private static final String REF_MAX_POOL_SIZE = "maxPoolSize";
    private static final String REF_MIN_POOL_SIZE = "minPoolSize";
    private static final String REF_MAX_CONNECTIONS = "maxConnections";
    private static final String REF_MIN_CONNECTIONS = "minConnections";
    
    private static final String REF_PING_INTERVAL = "pingInterval";
    private static final String REF_RETRY_INTERVAL = "retryInterval";
    private static final String REF_POOLING = "pooling";
    private static final String REF_STATEMENT_POOLING = "statementPooling";
    private static final String REF_PING_STATEMENT = "pingStatement";
    
//    private static final String REF_TYPE = "type";
//    private static final String REF_TX_ISOLATION = "transactionIsolationLevel";
//    private static final String REF_ISOLATION = "isolation";
    private static final String REF_PROPERTIES = "properties";
    private static final String REF_NON_STANDARD_PROPERTY = "nonStandard";

    /**
     * Get object instance for the specified name in the specified context.
     * This method constructs new datasource if <code>obj</code> represents
     * {@link Reference}, whose factory class is equal to this class.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, 
        Hashtable environment) throws Exception 
    {
        if (!(obj instanceof Reference)) return null;

        Reference ref = (Reference)obj;
        ref = (Reference)ref.clone();

        if (!getClass().getName().equals(ref.getClassName()))
            return null;

        FBWrappingDataSource ds = new FBWrappingDataSource();
        
        for(int i = 0; i < ref.size(); i++) {
            RefAddr element = ref.get(i);
            
            String type = element.getType();
            if (REF_BLOCKING_TIMEOUT.equals(type))
                ds.setBlockingTimeout(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_DESCRIPTION.equals(type))
                ds.setDescription(element.getContent().toString());
            else
            if (REF_IDLE_TIMEOUT.equals(type))
                ds.setIdleTimeout(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_MAX_IDLE_TIME.equals(type))
                ds.setMaxIdleTime(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_LOGIN_TIMEOUT.equals(type))
                ds.setLoginTimeout(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_MAX_POOL_SIZE.equals(type))
                ds.setMaxPoolSize(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_MIN_POOL_SIZE.equals(type))
                ds.setMinPoolSize(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_MIN_CONNECTIONS.equals(type))
                ds.setMinConnections(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_MAX_CONNECTIONS.equals(type))
                ds.setMaxConnections(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_PING_INTERVAL.equals(type))
                ds.setPingInterval(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_RETRY_INTERVAL.equals(type))
                ds.setRetryInterval(Integer.parseInt(element.getContent().toString()));
            else
            if (REF_POOLING.equals(type))
                ds.setPooling(Boolean.valueOf(element.getContent().toString()).booleanValue());
            else
            if (REF_STATEMENT_POOLING.equals(type))
                ds.setStatementPooling(Boolean.valueOf(element.getContent().toString()).booleanValue());
            else
            if (REF_PING_STATEMENT.equals(type))
                ds.setPingStatement(element.getContent().toString());
            if (REF_NON_STANDARD_PROPERTY.equals(type))
                ds.setNonStandardProperty(element.getContent().toString());
            else
            if (REF_PROPERTIES.equals(type)) {
                byte[] data = (byte[])element.getContent();
                FBConnectionProperties props = (FBConnectionProperties)BasicAbstractConnectionPool.deserialize(data);
                ds.getPool().setConnectionProperties(props);
            } else
            if (element.getContent() instanceof String) 
                ds.setNonStandardProperty(type, element.getContent().toString());
            
        }
        
        return ds;
    }

    /**
     * Get JDNI reference.
     * 
     * @return instance of {@link Reference}.
     */
    public Reference getReference() {
        if (reference == null)
            return getDefaultReference();
        else
            return reference;
    }
    
    /**
     * Set JNDI reference for this data source.
     * 
     * @param reference JNDI reference.
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    /**
     * Get default JNDI reference for this datasource. This method is called if
     * datasource is used in non-JCA environment.
     * 
     * @return instance of {@link Reference} containing all information 
     * that allows to reconstruct the datasource.
     */
    public Reference getDefaultReference() {
        Reference ref = new Reference(getClass().getName(), 
                getClass().getName(), null);
        
        if (getBlockingTimeout() != FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT)
            ref.add(new StringRefAddr(REF_BLOCKING_TIMEOUT, 
                String.valueOf(getBlockingTimeout())));

//        if (getDatabase() != null)            
//            ref.add(new StringRefAddr(REF_DATABASE, getDatabase()));
            
        if (getDescription() != null)
            ref.add(new StringRefAddr(REF_DESCRIPTION, getDescription()));
          
        if (getMaxIdleTime() != FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT)
            ref.add(new StringRefAddr(REF_MAX_IDLE_TIME,
                String.valueOf(getMaxIdleTime())));
            
        if (getLoginTimeout() != FBPoolingDefaults.DEFAULT_LOGIN_TIMEOUT)
            ref.add(new StringRefAddr(REF_LOGIN_TIMEOUT,
                String.valueOf(getLoginTimeout())));

        if (getMaxPoolSize() != FBPoolingDefaults.DEFAULT_MAX_SIZE)
            ref.add(new StringRefAddr(REF_MAX_POOL_SIZE, 
                String.valueOf(getMaxPoolSize())));

        if (getMinPoolSize() != FBPoolingDefaults.DEFAULT_MIN_SIZE)
            ref.add(new StringRefAddr(REF_MIN_POOL_SIZE,
                String.valueOf(getMinPoolSize())));

        if (getPingInterval() != FBPoolingDefaults.DEFAULT_PING_INTERVAL)
            ref.add(new StringRefAddr(REF_PING_INTERVAL, 
                String.valueOf(getPingInterval())));
        
        if (getRetryInterval() != FBPoolingDefaults.DEFAULT_RETRY_INTERVAL)
            ref.add(new StringRefAddr(REF_RETRY_INTERVAL, 
                    String.valueOf(getRetryInterval())));
        
        if (!isPooling())
            ref.add(new StringRefAddr(REF_POOLING, String.valueOf(isPooling())));
        
        if (!isStatementPooling())
            ref.add(new StringRefAddr(REF_STATEMENT_POOLING, 
                    String.valueOf(isStatementPooling())));
        
        ref.add(new StringRefAddr(REF_PING_STATEMENT, getPingStatement()));
        
//        if (getType() != null)
//       	    ref.add(new StringRefAddr(REF_TYPE, getType()));
//        
//        if (getDefaultTransactionIsolation() != FBPoolingDefaults.DEFAULT_ISOLATION)
//            ref.add(new StringRefAddr(REF_TX_ISOLATION, 
//                String.valueOf(getDefaultTransactionIsolation())));
        
        byte[] data = 
            BasicAbstractConnectionPool.serialize(getPool().getConnectionProperties());
        ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
        
        return ref;
    }
    
    // JBBC 4.0
    
    public boolean isWrapperFor(Class iface) throws SQLException {
    	return false;
    }
    
    public Object unwrap(Class iface) throws SQLException {
    	throw new FBDriverNotCapableException();
    }

}
