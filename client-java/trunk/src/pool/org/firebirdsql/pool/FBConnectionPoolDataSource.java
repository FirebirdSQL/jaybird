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

import java.io.*;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.sql.*;

import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.jca.*;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Connection pool for Firebird JDBC driver.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnectionPoolDataSource extends AbstractConnectionPool
    implements PooledConnectionManager, ConnectionPoolConfiguration, 
    ConnectionPoolDataSource, ConnectionEventListener, 
    Serializable, Referenceable, ObjectFactory
{
    
    public static final String USER_NAME_PROPERTY = FBDriver.USER;
    public static final String PASSWORD_PROPERTY = FBDriver.PASSWORD;
    public static final String TPB_MAPPING_PROPERTY = FBDriver.TPB_MAPPING;
    public static final String BLOB_BUFFER_PROPERTY = FBDriver.BLOB_BUFFER_LENGTH;

    public static final String ENCODING_PROPERTY = "lc_ctype";
    public static final String SOCKET_BUFFER_PROPERTY = "socket_buffer_size";
    public static final String SQL_ROLE_PROPERTY = "sql_role_property";

    /**
     * Structure class to store user name and password. 
     */
    private static class UserPasswordPair {
        private String userName;
        private String password;

        public UserPasswordPair() {
            this(null, null);
        }
    
        public UserPasswordPair(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (!(obj instanceof UserPasswordPair)) return false;
        
            UserPasswordPair that = (UserPasswordPair)obj;
        
            boolean equal = true;
            
            equal &= userName != null ? 
                userName.equals(that.userName) : that.userName == null;
                
            equal &= password != null ? 
                password.equals(that.password) : that.password == null;
        
            return equal; 
        }
    
        public int hashCode() {
            int result = 3;
            
            result ^= userName != null ? userName.hashCode() : 0;
            result ^= password != null ? password.hashCode() : 0;
            
            return result;
        }
    }

    public static final UserPasswordPair EMPTY_USER_PASSWORD = new UserPasswordPair();
    private static final String PING_STATEMENT = ""
        + "SELECT cast(1 AS INTEGER) FROM rdb$database" 
        ;
    
    private static final Logger LOG =
        LoggerFactory.getLogger(FBConnectionPoolDataSource.class, false);
        
    private Reference reference;
    
	private int loginTimeout;
	private transient PrintWriter logWriter;
    
    private transient FBManagedConnectionFactory managedConnectionFactory;

    private int minConnections = FBPoolingDefaults.DEFAULT_MIN_SIZE;
    private int maxConnections = FBPoolingDefaults.DEFAULT_MAX_SIZE;
    
    private int blockingTimeout = FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT;
    private int retryInterval = FBPoolingDefaults.DEFAULT_RETRY_INTERVAL;
    private int idleTimeout = FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT;

    private int pingInterval = FBPoolingDefaults.DEFAULT_PING_INTERVAL;
    
    private Properties properties = new Properties();
    private String database;
    private GDSType gdsType = GDSType.PURE_JAVA;
    
    /**
     * Create instance of this class.
     * 
     * @param config configuration for this connection pool.
     * 
     * @throws SQLException if something went wrong.
     */
    public FBConnectionPoolDataSource() {
        super();
    }
    
    private synchronized FBManagedConnectionFactory getManagedConnectionFactory() {
        if (managedConnectionFactory == null) {
            managedConnectionFactory = new FBManagedConnectionFactory(getGDSType());

            managedConnectionFactory.setDatabase(getDatabase());
        
            FBConnectionRequestInfo defaultCri = 
                managedConnectionFactory.getDefaultConnectionRequestInfo();
            
            FBConnectionRequestInfo cri = FBConnectionHelper.getCri(
                getProperties(), defaultCri);
            
            managedConnectionFactory.setConnectionRequestInfo(cri);
        }
        
        return managedConnectionFactory;
    }

    protected Logger getLogger() {
        return LOG;
    }
    
    public ConnectionPoolConfiguration getConfiguration() {
        return this;
    }
    
    protected PooledConnectionManager getConnectionManager() {
        return this;
    }

    /**
     * Allocate new physical connection for the specified user name and 
     * password.
     * 
     * @param userName user name.
     * @param password password.
     *  
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    public PooledObject allocateConnection(Object key)
        throws SQLException
    {
        
        if (!(key instanceof UserPasswordPair))
            throw new SQLException("Incorrect key.");
            
        UserPasswordPair pair = (UserPasswordPair)key;
        String userName = pair.userName; 
        String password = pair.password;

        Properties props = new Properties();
        props.putAll(getProperties());
        
        if (userName != null)
            props.setProperty(USER_NAME_PROPERTY, userName);
            
        if (password != null)
            props.setProperty(PASSWORD_PROPERTY, password);
            
        FBConnectionRequestInfo defaultCri = 
            getManagedConnectionFactory().getDefaultConnectionRequestInfo();
             
        FBConnectionRequestInfo cri = FBConnectionHelper.getCri(props,
                GDSFactory.getGDSForType(this.getGDSType()));
            
        try {
            FBManagedConnection managedConnection = (FBManagedConnection)
                getManagedConnectionFactory().createManagedConnection(null, cri);

            PingablePooledConnection pooledConnection = null;

            if (isPingable())
                pooledConnection =
                    new FBPooledConnection(
                        managedConnection,
                        cri,
                        getPingStatement(),
                        getPingInterval());
            else
                pooledConnection = 
                    new FBPooledConnection(managedConnection, cri);

            return pooledConnection;

        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }
    
    
    /** 
     * Get name of the connection queue.
     * 
     * @see AbstractConnectionPoolDataSource#getQueueName()
     */
    protected String getPoolName() {
        return getDatabase();
    }
    
	public PrintWriter getLogWriter() {
	    return logWriter;
	}

	public void setLogWriter(PrintWriter out) {
	    logWriter = out;
	}

	/**
	 * Get login timeout.
	 * 
	 * @return value set in {@link #setLoginTimeout(int)} method or 0.
	 */
	public int getLoginTimeout() {
	    return loginTimeout;
	}

	/**
	 * Set login timeout for new connection. Currently ignored.
	 * 
	 * @param seconds how long pool should wait until new connection is 
	 * granted.
	 */
	public void setLoginTimeout(int seconds) {
	    loginTimeout = seconds;
	}

    /**
     * Get pooled connection from the pooled queue.
	 */
	protected synchronized PooledObject getPooledConnection(
        PooledConnectionQueue queue) throws SQLException
    {
		PingablePooledConnection connection = 
            (PingablePooledConnection)super.getPooledConnection(queue);

        connection.addConnectionEventListener(this);

        return connection;
	}

	/**
	 * Get pooled connection. This method will block until there will be 
	 * free connection to return.
	 * 
	 * @return instance of {@link PooledConnection}.
	 * 
	 * @throws SQLException if pooled connection cannot be obtained.
	 */
	public synchronized PooledConnection getPooledConnection() 
        throws SQLException 
    {
	    return (PooledConnection)getPooledConnection(
            getQueue(EMPTY_USER_PASSWORD));
	}

	/**
	 * Get pooled connection for the specified user name and password.
	 * 
	 * @param user user name.
	 * @param password password corresponding to specified user name.
	 * 
	 * @return instance of {@link PooledConnection} for the specified
	 * credentials.
	 * 
	 * @throws SQLException always, this method is not yet implemented.
	 */
	public PooledConnection getPooledConnection(String user, String password) 
        throws SQLException 
    {
	    return (PooledConnection)getPooledConnection(
            getQueue(new UserPasswordPair(user, password)));
	}
    
    /**
     * Notify about connection being closed.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
	 */
	public void connectionClosed(ConnectionEvent connectionEvent) {
		PooledObjectEvent event = 
            new PooledObjectEvent(connectionEvent.getSource());
            
        pooledObjectReleased(event);
	}

	/**
	 * Notify about serious error when using the connection. Currently
	 * these events are ignored.
	 * 
	 * @param event instance of {@link ConnectionEvent} containing 
	 * information about an error.
	 */
	public void connectionErrorOccurred(ConnectionEvent event) {
	    if (getLogger() != null)
	        getLogger().error("Error occured in connection.", 
	            event.getSQLException());
	}

    public int getFreeSize() throws SQLException {
        return getQueue(EMPTY_USER_PASSWORD).size();
    }

    public int getTotalSize() throws SQLException {
        return getQueue(EMPTY_USER_PASSWORD).totalSize();
    }

    public int getWorkingSize() throws SQLException {
        return getQueue(EMPTY_USER_PASSWORD).workingSize();
    }
    
    /**
     * Get database to which we will connect.
     * 
     * @return path to the database to which we will connect.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Get minimum number of open physical connections that are kept in pool. 
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getMinConnections()
     */
    public int getMinConnections() {
        return minConnections;
    }

    /**
     * Get maximum number of physical connections in the pool.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getMaxConnections()
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Get JDBC connection properties.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getProperties()
     */
    public Properties getProperties() {
        Properties result = new Properties();
        result.putAll(properties);
        return result;
    }
    
    /**
     * Get JDBC connection property by key.
     * 
     * @param key key of the property.
     * 
     * @return value of the property or <code>null</code> if propery not yet.
     */
    private String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get JDBC connection property as integer value. Note, it is not possible
     * to determine null value in this case.
     * 
     * @param key key of the property.
     * 
     * @return integer value of the property, or <code>0</code> if specified
     * property is not set.
     */
    private int getIntProperty(String key) {
        String value = getProperty(key);
        
        if (value == null)
            return 0;
        
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Check if this configuation defines a pingable connection JDBC pool.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#isPingable()
     */
    public boolean isPingable() {
        return true;
    }

    /**
     * Get SQL statement that will be used to "ping" the connection.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getPingStatement()
     */
    public String getPingStatement() {
        return PING_STATEMENT;
    }

    /**
     * Get time interval, after which connection is marked to be pinged on 
     * first request to it.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getPingInterval()
     */
    public int getPingInterval() {
        return pingInterval;
    }

    /**
     * Set JDBC URL that will be used to connect to the database.
     * 
     * @param jdbcUrl JDBC URL describing a database to connect.
     * 
     * @see #getJdbcUrl()
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Set maximum number of open physical connections in the pool.
     * 
     * @param maxConnections maximum allowed number of open connections in the 
     * pool.
     * 
     * @see #getMaxConnections()
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Set minimum number of open physical connections in the pool.
     * 
     * @param minConnections minimum number of open connections in the pool.
     * 
     * @see #getMinConnections()
     */
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    /**
     * Set ping interval for the connections.
     * 
     * @param pingInterval number of milliseconds after which connection is 
     * marked to "ping" before getting it from the pool or <code>0</code> to
     * remove "pingable" property of this pool.
     * 
     * @see #getPingInterval()
     */
    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * Set JDBC properties that will be passed when opening a connection.
     * 
     * @param properties instance of {@link Properties} containing properties
     * of a connection to open.
     * 
     * @see #getProperties()
     */
    public void setProperties(Properties properties) {
        if (properties == null)
            throw new NullPointerException("Specified properties are null.");
        
        this.properties.clear();
        this.properties.putAll(properties);
    }
    
    /**
     * Set specified property.
     * 
     * @param name name of the property.
     * @param value value of the property.
     */
    private void setProperty(String name, String value) {
        this.properties.setProperty(name, value);
    }
    
    /**
     * Set specified property as integer value.
     * 
     * @param name name of the property.
     * @param value value of the property.
     */
    public void setIntProperty(String name, int value) {
        setProperty(name, Integer.toString(value));
    }

    /**
     * Get type of the pool that should be created by a JNDI object factory
     * ({@link FBConnectionPoolObjectFactory}).
     */
    public Class getJNDIType() {
        return ConnectionPoolDataSource.class;
    }


    /**
     * Get pool blocking timeout.
     * 
     * @see ConnectionPoolConfiguration#getBlockingTimeout()
     */
    public int getBlockingTimeout() {
        return blockingTimeout;
    }
    
    /**
     * Set blocking timeout.
     * 
     * @param blockingTimeout blocking timeout to set.
     * 
     * @see ConnectionPoolConfiguration#getBlockingTimeout()
     */
    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    /** 
     * Get retry interval.
     * 
     * @see ConnectionPoolConfiguration#getRetryInterval()
     */
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Set retry interval.
     * 
     * @param retryInterval retry interval in milliseconds
     * 
     * @see ConnectionPoolConfiguration#getBlockingTimeout()
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
    
    /**
     * Get idle timeout.
     * 
     * @return idle timeout in milliseconds.
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Set idle timeout.
     * 
     * @param idleTimeout idle timeout in milliseconds.
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    
    /**
     * Get type of JDBC driver that will be used. Note, value returned by this
     * method might be different from that used in {@link #setType(String)} if
     * you used synonym (either <code>"TYPE4"</code> or <code>"TYPE2"</code>).
     * 
     * @return one of the following values:
     * <ul>
     * <li><code>"PURE_JAVA"</code> for pure Java type 4 JDBC driver.
     * <li><code>"NATIVE"</code> for type 2 JDBC driver that will use Firebird
     * client library.
     * <li><code>"EMBEDDED"</code> for type 2 JDBC driver that will use 
     * embedded engine.
     * </ul>
     */
    public String getType() {
        return getGDSType().toString();
    }
    
    /**
     * Set type of JDBC driver to use.
     * 
     * @param type type of driver to use. Possible values are (case insensitive):
     * <ul>
     * <li><code>"PURE_JAVA"</code> or <code>"TYPE4"</code> for pure Java type 4
     * JDBC driver;
     * <li><code>"NATIVE"</code> or <code>"TYPE2"</code> for type 2 JDBC driver
     * that will use Firebird client library.
     * <li><code>"EMBEDDED"</code> for type 2 JDBC driver that will use embedded
     * version of the server. 
     * </ul>
     * 
     * @throws SQLException if specified type is not known.
     */
    public void setType(String type) throws SQLException {
        GDSType gdsType = GDSType.getType(type);
        
        if (gdsType == null)
            throw new UnknownDriverTypeException(type);
            
        setGDSType(gdsType);
    }
    
    /**
     * Get type of JDBC driver that is used.
     * 
     * @return type of JDBC driver that is used.
     */
    public GDSType getGDSType() {
        return gdsType;
    }
    
    /**
     * Set type of the JDBC driver to use.
     *
     * @param type type of the JDBC driver.
     */
    public void setGDSType(GDSType gdsType) {
        this.gdsType = gdsType;
    }



    /*
     * Properties of this data source. These methods are created only
     * for user convenience and are shortcuts to setProperty(String, String)
     * method call with respective keys.
     */

    public String getNonStandardProperty(String key) {
        return getProperty(key);
    }
    
    public void setNonStandardProperty(String key, String value) {
        setProperty(key, value);
    }

    public int getBlobBufferSize() {
        return getIntProperty(BLOB_BUFFER_PROPERTY);
    }

    public void setBlobBufferSize(int blobBufferSize) {
        setIntProperty(BLOB_BUFFER_PROPERTY, blobBufferSize);
    }

    public String getEncoding() {
        return getProperty(ENCODING_PROPERTY);
    }

    public void setEncoding(String encoding) {
        setProperty(ENCODING_PROPERTY, encoding);
    }

    public String getCharSet() {
        return FBConnectionHelper.getJavaEncoding(getEncoding());
    }
    
    public void setCharSet(String charSet) throws SQLException {
        String iscEncoding = FBConnectionHelper.getIscEncoding(charSet);
        if (iscEncoding == null)
            throw new SQLException("Unknown character set " + charSet);
        
        setEncoding(iscEncoding);
    }
    
    public String getPassword() {
        return getProperty(PASSWORD_PROPERTY);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD_PROPERTY, password);
    }

    public int getSocketBufferSize() {
        return getIntProperty(SOCKET_BUFFER_PROPERTY);
    }

    public void setSocketBufferSize(int socketBufferSize) {
        setIntProperty(SOCKET_BUFFER_PROPERTY, socketBufferSize);
    }

    public String getSqlRole() {
        return getProperty(SQL_ROLE_PROPERTY);
    }

    public void setSqlRole(String sqlRole) {
        setProperty(SQL_ROLE_PROPERTY, sqlRole);
    }

    public String getTpbMapping() {
        return getProperty(TPB_MAPPING_PROPERTY);
    }

    public void setTpbMapping(String tpbMapping) {
        setProperty(TPB_MAPPING_PROPERTY, tpbMapping);
    }

    public String getUserName() {
        return getProperty(USER_NAME_PROPERTY);
    }

    public void setUserName(String userName) {
        setProperty(USER_NAME_PROPERTY, userName);
    }



    /*
     * JNDI-related code. 
     */

    private static final String REF_BLOCKING_TIMEOUT = "blockingTimeout";
    private static final String REF_DATABASE = "database";
    private static final String REF_IDLE_TIMEOUT = "idleTimeout";
    private static final String REF_LOGIN_TIMEOUT = "loginTimeout";
    private static final String REF_MAX_SIZE = "maxSize";
    private static final String REF_MIN_SIZE = "minSize";
    private static final String REF_PING_INTERVAL = "pingInterval";
    private static final String REF_TYPE = "type";
    private static final String PROPERTIES = "properties";

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

        FBConnectionPoolDataSource ds = new FBConnectionPoolDataSource();
        
        String addr;

        addr = getRefAddr(ref, REF_BLOCKING_TIMEOUT);
        if (addr != null)
            ds.setBlockingTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_DATABASE);
        if (addr != null)
            ds.setDatabase(addr);
            
        addr = getRefAddr(ref, REF_IDLE_TIMEOUT);
        if (addr != null)
            ds.setIdleTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_LOGIN_TIMEOUT);
        if (addr != null)
            ds.setLoginTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MAX_SIZE);
        if (addr != null)
            ds.setMaxConnections(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MIN_SIZE);
        if (addr != null)
            ds.setMinConnections(Integer.parseInt(addr));

        addr = getRefAddr(ref, REF_PING_INTERVAL);
        if (addr != null)
            ds.setPingInterval(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_TYPE);
        if (addr != null)
            ds.setType(addr);
            
        RefAddr binAddr = ref.get(PROPERTIES);
        if (binAddr != null) {
            byte[] data = (byte[])binAddr.getContent();
            Properties props = (Properties)deserialize(data);
            if (props != null) 
                ds.setProperties(props);
        }
        
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
        
        ref.add(new StringRefAddr(REF_BLOCKING_TIMEOUT, 
            String.valueOf(getBlockingTimeout())));

        if (getDatabase() != null)            
            ref.add(new StringRefAddr(REF_DATABASE, getDatabase()));
            
        ref.add(new StringRefAddr(REF_IDLE_TIMEOUT,
            String.valueOf(getIdleTimeout())));
            
        ref.add(new StringRefAddr(REF_LOGIN_TIMEOUT,
            String.valueOf(getLoginTimeout())));
            
        ref.add(new StringRefAddr(REF_MAX_SIZE, 
            String.valueOf(getMaxConnections())));
            
        ref.add(new StringRefAddr(REF_MIN_SIZE,
            String.valueOf(getMinConnections())));
            
        ref.add(new StringRefAddr(REF_PING_INTERVAL, 
            String.valueOf(getPingInterval())));
            
        if (getType() != null)
            ref.add(new StringRefAddr(REF_TYPE, getType()));
            
        byte[] data = serialize(getProperties());
        ref.add(new BinaryRefAddr(PROPERTIES, data));

        return ref;
    }
    
    private byte[] serialize(Object obj) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(obj);
            out.flush();
        } catch(IOException ex) {
            return null;
        }
        
        return bout.toByteArray();
    }
    
    private Object deserialize(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            return in.readObject();
        } catch(IOException ex) {
            return null;
        } catch(ClassNotFoundException ex) {
            return null;
        }
    }
}
