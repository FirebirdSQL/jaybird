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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;

import org.firebirdsql.jdbc.FBConnectionDefaults;
import org.firebirdsql.jdbc.FBDriver;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.Referenceable;
import javax.sql.DataSource;

/**
 * Implementation of {@link javax.sql.DataSource} including connection pooling.
 * Following properties are supported:
 * <ul>
 * <li><code>blobBufferSize</code> size of the buffer used to transfer BLOB data.
 * <li><code>blockingTimeout</code> time in milliseconds during which 
 * {@link #getConnection()} method will block if no free connection is in pool.
 * <li><code>database</code> path to a database including the server name;
 * for example <code>localhost/3050:c:/path/to/database.gdb</code>.
 * <li><code>encoding</code> character encoding for the JDBC connection.
 * <li><code>idleTimeout</code> time in milliseconds after which 
 * idle physical connection in the pool is closed.
 * <li><code>loginTimeout</code> property from {@link javax.sql.DataSource},
 * in this context is a synonym for <code>blockingTimeout</code> (however
 * value is specified in seconds).
 * <li><code>maxSize</code> maximum number of physical connections that can
 * be opened by this data source.
 * <li><code>minSize</code> minimum number of connections that will remain open
 * by this data source.
 * <li><code>password</code> password that is used to connect to database.
 * <li><code>pingInterval</code> time interval during which connection will
 * be proved for aliveness.
 * <li><code>socketBufferSize</code> size of the socket buffer in bytes. In some 
 * cases values used by JVM by default are not optimal. This results in 
 * performance degradation (especially when you transfer big BLOBs). Usually 
 * 8192 bytes provides good results.
 * <li><code>sqlRole</code> SQL role name.
 * <li><code>tpbMapping</code> mapping of the TPB parameters to JDBC transaction
 * isolation levels.
 * <li><code>type</code> type of connection that will be created. There are 
 * three possible types: pure Java (or type 4), type 2 that will use Firebird
 * client library to connect to the database, and embedded that will use 
 * embedded engine (access to local databases). Possible values are:
 * <ul> 
 * <li><code>"pure"</code> or <code>"type4"</code> for pure Java (type 4) JDBC
 * connections;
 * <li><code>"native"</code> or <code>"type2"</code> to use Firebird client
 * library;
 * <li><code>"embedded"</code> or <code>"native_embedded"</code> to use
 * embedded engine.
 * </ul>
 * <li><code>userName</code> name of the user that will be used to access the 
 * database.
 * </ul>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBWrappingDataSource implements DataSource, ObjectFactory, Referenceable {

    public static final String TYPE4_PREFIX = FBDriver.FIREBIRD_PROTOCOL;
    public static final String TYPE2_PREFIX = FBDriver.FIREBIRD_PROTOCOL_NATIVE;
    public static final String EMBEDDED_PREFIX = FBDriver.FIREBIRD_PROTOCOL_NATIVE_EMBEDDED;

    public static final String USER_NAME_PROPERTY = FBDriver.USER;
    public static final String PASSWORD_PROPERTY = FBDriver.PASSWORD;
    public static final String TPB_MAPPING_PROPERTY = FBDriver.TPB_MAPPING;
    public static final String BLOB_BUFFER_PROPERTY = FBDriver.BLOB_BUFFER_LENGTH;

    public static final String ENCODING_PROPERTY = "lc_ctype";
    public static final String SOCKET_BUFFER_PROPERTY = "socket_buffer_size";
    public static final String SQL_ROLE_PROPERTY = "sql_role_property";

    public static final String TYPE4 = "type4";
    public static final String PURE_JAVA = "pure";

    public static final String TYPE2 = "type2";
    public static final String NATIVE = "native";

    public static final String EMBEDDED = "embedded";
    public static final String NATIVE_EMBEDDED = "native_embedded";

    private Object configSyncObject = new Object();
    private FBConnectionPoolDataSource pool;
    private DataSource dataSource;
    
    private Reference reference;

    private PrintWriter logWriter;

    private String userName;
    private String password;
    private String sqlRole;

    private String database;
    private String encoding;
    private String description;

    private String tpbMapping;
    private String type;
    
    private Properties nonStandardProperties = new Properties();

    private int blobBufferSize = FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE;
    private int socketBufferSize = FBConnectionDefaults.DEFAULT_SOCKET_BUFFER_SIZE;

    private int minSize = FBPoolingDefaults.DEFAULT_MIN_SIZE;
    private int maxSize = FBPoolingDefaults.DEFAULT_MAX_SIZE;
    private int blockingTimeout = FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT;
    private int idleTimeout = FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT;
    private int pingInterval = FBPoolingDefaults.DEFAULT_PING_INTERVAL;

    /**
     * Create instance of this class.
     */
    public FBWrappingDataSource() {
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

    /**
     * Get data source instance. This method will instantiate a connection
     * pool if necessary.
     * 
     * @return instance of {@link DataSource}.
     * 
     * @throws SQLException if something went wrong.
     */
    protected DataSource getDataSource() throws SQLException {
        synchronized (configSyncObject) {
            if (pool == null) {
                FBConnectionPoolConfiguration config = getConfiguration();
                pool = new FBConnectionPoolDataSource(config);
                pool.start();

                dataSource = new SimpleDataSource(pool);
                dataSource.setLogWriter(logWriter);
                dataSource.setLoginTimeout(getBlockingTimeout() / 60 / 1000);
            }
        }

        return dataSource;
    }

    /**
     * Get JDBC connection from this data source.
     * 
     * @return instance of {@link Connection}.
     * 
     * @throws SQLException if connection cannot be obtained due to some reason.
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get JDBC connection for the specified user name and password.
     * 
     * @link instance of {@link Connection}
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection(String user, String password) 
        throws SQLException 
    {
        return getDataSource().getConnection(user, password);
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
        return logWriter;
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
        logWriter = printWriter;
    }

    /**
     * Get pool configuration corrsponding to the configuration of this 
     * data source.
     * 
     * @return instance of {@link FBConnectionPoolConfiguration}.
     */
    protected FBConnectionPoolConfiguration getConfiguration() {
        FBConnectionPoolConfiguration config =
            new FBConnectionPoolConfiguration();

        config.setBlockingTimeout(blockingTimeout);
        config.setMaxConnections(maxSize);
        config.setMinConnections(minSize);
        config.setPingInterval(pingInterval);

        if (userName != null)
            config.setProperty(USER_NAME_PROPERTY, userName);
        
        if (password != null)
            config.setProperty(PASSWORD_PROPERTY, password);
        
        if (blobBufferSize != FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE)
            config.setProperty(BLOB_BUFFER_PROPERTY, 
                Integer.toString(blobBufferSize));
            
        if (encoding != null)
            config.setProperty(ENCODING_PROPERTY, encoding);
            
        if (tpbMapping != null)
            config.setProperty(TPB_MAPPING_PROPERTY, tpbMapping);

        if (database != null) {
            String url;
    
            if (TYPE4.equals(type) || PURE_JAVA.equals(type)) {
                url = TYPE4_PREFIX + database;
            } else
            if (TYPE2.equals(type) || NATIVE.equals(type)) {
                url = TYPE2_PREFIX + database;
            } else
            if (NATIVE_EMBEDDED.equals(type) || EMBEDDED.equals(type)) {
                url = EMBEDDED_PREFIX + database;
            } else {
                url = TYPE4_PREFIX + database;
            }
            
            config.setJdbcUrl(url);
        }
        
        // set non-standard properties
        Iterator iter = nonStandardProperties.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            config.setProperty(
                (String)entry.getKey(), (String)entry.getValue());
        }

        return config;
    }

    /*
     * Properties of this datasource.
     */

    public int getBlockingTimeout() {
        return blockingTimeout;
    }

    public void setBlockingTimeout(int blockingTimeoutValue) {
        this.blockingTimeout = blockingTimeoutValue;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String databaseValue) {
        this.database = databaseValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionValue) {
        this.description = descriptionValue;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encodingValue) {
        this.encoding = encodingValue;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeoutValue) {
        this.idleTimeout = idleTimeoutValue;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSizeValue) {
        this.maxSize = maxSizeValue;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSizeValue) {
        this.minSize = minSizeValue;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordValue) {
        this.password = passwordValue;
    }

    public String getTpbMapping() {
        return tpbMapping;
    }

    public void setTpbMapping(String tpbMappingValue) {
        this.tpbMapping = tpbMappingValue;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userNameValue) {
        this.userName = userNameValue;
    }

    public int getBlobBufferSize() {
        return blobBufferSize;
    }

    public void setBlobBufferSize(int blobBufferSizeValue) {
        this.blobBufferSize = blobBufferSizeValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String typeValue) {
        this.type = typeValue;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingIntervalValue) {
        this.pingInterval = pingIntervalValue;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    public String getSqlRole() {
        return sqlRole;
    }

    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public void setSqlRole(String sqlRole) {
        this.sqlRole = sqlRole;
    }
    
    public String getNonStandardProperty(String key) {
        return nonStandardProperties.getProperty(key);
    }
    
    public void setNonStandardProperty(String key, String value) {
        if (key == null)
            throw new NullPointerException("Key is null");
            
        if (value == null)
            value = "";
            
        nonStandardProperties.setProperty(key, value);
    }
    
    /*
     * Deprecated methods included for compatibility reasons. Will be removed
     * in next release.
     */
     
    private boolean pooling;
    public boolean getPooling() {
        return pooling;
    }
    
    public void setPooling(boolean pooling) {
        this.pooling = pooling;
    }
    
    public int getIdleTimeoutMinutes() {
        return getIdleTimeout() / 60 / 1000;
    }
    
    public void setIdleTimeoutMinutes(int timeout) {
        setIdleTimeout(timeout * 60 * 1000);
    }
    
    public int getConnectionCount() {
        if (pool != null)
            return pool.getFreeSize();
        else
            return 0;
    }
    
    /*
     * JNDI-related code. 
     */

    private static final String REF_BLOB_BUFFER_SIZE = "blobBufferSize";
    private static final String REF_BLOCKING_TIMEOUT = "blockingTimeout";
    private static final String REF_DATABASE = "database";
    private static final String REF_DESCRIPTION = "description";
    private static final String REF_ENCODING = "encoding";
    private static final String REF_IDLE_TIMEOUT = "idleTimeout";
    private static final String REF_LOGIN_TIMEOUT = "loginTimeout";
    private static final String REF_MAX_SIZE = "maxSize";
    private static final String REF_MIN_SIZE = "minSize";
    private static final String REF_PASSWORD = "password";
    private static final String REF_PING_INTERVAL = "pingInterval";
    private static final String REF_SOCKET_BUFFER_SIZE = "socketBufferSize";
    private static final String REF_SQL_ROLE = "sqlRole";
    private static final String REF_TPB_MAPPING = "tpbMapping";
    private static final String REF_TYPE = "type";
    private static final String REF_USER_NAME = "userName";

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

        if (!getClass().getName().equals(ref.getClassName()))
            return null;

        FBWrappingDataSource ds = new FBWrappingDataSource();
        
        String addr = getRefAddr(ref, REF_BLOB_BUFFER_SIZE);
        if (addr != null)
            ds.setBlobBufferSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_BLOCKING_TIMEOUT);
        if (addr != null)
            ds.setBlockingTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_DATABASE);
        if (addr != null)
            ds.setDatabase(addr);
            
        addr = getRefAddr(ref, REF_DESCRIPTION);
        if (addr != null)
            ds.setDescription(addr);
            
        addr = getRefAddr(ref, REF_ENCODING);
        if (addr != null)
            ds.setEncoding(addr);
            
        addr = getRefAddr(ref, REF_IDLE_TIMEOUT);
        if (addr != null)
            ds.setIdleTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_LOGIN_TIMEOUT);
        if (addr != null)
            ds.setLoginTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MAX_SIZE);
        if (addr != null)
            ds.setMaxSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MIN_SIZE);
        if (addr != null)
            ds.setMinSize(Integer.parseInt(addr));

        addr = getRefAddr(ref, REF_PASSWORD);
        if (addr != null)
            ds.setPassword(addr);
            
        addr = getRefAddr(ref, REF_PING_INTERVAL);
        if (addr != null)
            ds.setPingInterval(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_SOCKET_BUFFER_SIZE);
        if (addr != null)
            ds.setSocketBufferSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_SQL_ROLE);
        if (addr != null)
            ds.setSqlRole(addr);
            
        addr = getRefAddr(ref, REF_TPB_MAPPING);
        if (addr != null)
            ds.setTpbMapping(addr);
            
        addr = getRefAddr(ref, REF_TYPE);
        if (addr != null)
            ds.setType(addr);
            
        addr = getRefAddr(ref, REF_USER_NAME);
        if (addr != null)
            ds.setUserName(addr);
            
        return ds;
    }
    
    private String getRefAddr(Reference ref, String type) {
        RefAddr addr = ref.get(type);
        if (addr == null)
            return null;
        else
            return addr.getContent().toString();
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
        Reference ref = new Reference(getClass().getName());
        
        if (getBlobBufferSize() != FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE)
            ref.add(new StringRefAddr(REF_BLOB_BUFFER_SIZE, 
                String.valueOf(getBlobBufferSize())));
            
        ref.add(new StringRefAddr(REF_BLOCKING_TIMEOUT, 
            String.valueOf(getBlockingTimeout())));

        if (getDatabase() != null)            
            ref.add(new StringRefAddr(REF_DATABASE, getDatabase()));
            
        if (getDescription() != null)
            ref.add(new StringRefAddr(REF_DESCRIPTION, getDescription()));
            
        if (getEncoding() != null)
            ref.add(new StringRefAddr(REF_ENCODING, getEncoding()));

        ref.add(new StringRefAddr(REF_IDLE_TIMEOUT,
            String.valueOf(getIdleTimeout())));
            
        ref.add(new StringRefAddr(REF_LOGIN_TIMEOUT,
            String.valueOf(getLoginTimeout())));
            
        ref.add(new StringRefAddr(REF_MAX_SIZE, 
            String.valueOf(getMaxSize())));
            
        ref.add(new StringRefAddr(REF_MIN_SIZE,
            String.valueOf(getMinSize())));
            
        if (getPassword() != null)
            ref.add(new StringRefAddr(REF_PASSWORD, getPassword()));

        ref.add(new StringRefAddr(REF_PING_INTERVAL, 
            String.valueOf(getPingInterval())));
            
        ref.add(new StringRefAddr(REF_SOCKET_BUFFER_SIZE, 
        	String.valueOf(getSocketBufferSize())));
        	
        if (getSqlRole() != null)
       	    ref.add(new StringRefAddr(REF_SQL_ROLE, getSqlRole()));
       	
        if (getTpbMapping() != null)
       	    ref.add(new StringRefAddr(REF_TPB_MAPPING, getTpbMapping()));
            
        if (getType() != null)
       	    ref.add(new StringRefAddr(REF_TYPE, getType()));
        
        if (getUserName() != null)
       	    ref.add(new StringRefAddr(REF_USER_NAME, getUserName()));
        
        return ref;
    }

}
