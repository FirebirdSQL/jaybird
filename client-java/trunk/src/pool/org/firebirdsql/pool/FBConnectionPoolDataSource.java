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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.*;
import javax.resource.ResourceException;
import javax.sql.*;

import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.gds.GDSType;
import org.firebirdsql.jca.*;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Connection pool for Firebird JDBC driver.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnectionPoolDataSource extends BasicAbstractConnectionPool
    implements PooledConnectionManager, ConnectionPoolDataSource, 
    XADataSource, ConnectionEventListener
{
    
    public static final String USER_NAME_PROPERTY = FBDriver.USER;
    public static final String PASSWORD_PROPERTY = FBDriver.PASSWORD;
    public static final String TPB_MAPPING_PROPERTY = FBDriver.TPB_MAPPING;
    public static final String BLOB_BUFFER_PROPERTY = FBDriver.BLOB_BUFFER_LENGTH;

    public static final String ENCODING_PROPERTY = "lc_ctype";
    public static final String SOCKET_BUFFER_PROPERTY = "socket_buffer_size";
    public static final String SQL_ROLE_PROPERTY = "sql_role_property";

    private static final HashMap PROPERTY_NAME_MAPPING = new HashMap();
    static {
        PROPERTY_NAME_MAPPING.put("blobBufferSize", BLOB_BUFFER_PROPERTY);
        PROPERTY_NAME_MAPPING.put("encoding", ENCODING_PROPERTY);
        PROPERTY_NAME_MAPPING.put("userName", USER_NAME_PROPERTY);
        PROPERTY_NAME_MAPPING.put("socketBufferSize", SOCKET_BUFFER_PROPERTY);
        PROPERTY_NAME_MAPPING.put("sqlRole", SQL_ROLE_PROPERTY);
        PROPERTY_NAME_MAPPING.put("tpbMapping", TPB_MAPPING_PROPERTY);
    }
    

    public static final AbstractConnectionPool.UserPasswordPair 
        EMPTY_USER_PASSWORD = new AbstractConnectionPool.UserPasswordPair();
    
    private static final String PING_STATEMENT = ""
        + "SELECT cast(1 AS INTEGER) FROM rdb$database" 
        ;
    
    private static final Logger LOG =
        LoggerFactory.getLogger(FBConnectionPoolDataSource.class, false);
        
	private transient PrintWriter logWriter;
    
    private transient FBManagedConnectionFactory managedConnectionFactory;

    private Properties properties = new Properties();
    private String database;
    private GDSType gdsType = GDSType.PURE_JAVA;
    
    /**
     * Create instance of this class.
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
    
    protected PooledConnectionManager getConnectionManager() {
        return this;
    }

    /**
     * Allocate new physical connection for the specified user name and 
     * password.
     * 
     * @param key key identifying pooled object.
     *  
     * @return instance of {@link PooledObject}.
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    public PooledObject allocateConnection(Object key)
        throws SQLException
    {
        
        if (!(key instanceof AbstractConnectionPool.UserPasswordPair))
            throw new SQLException("Incorrect key.");
            
        AbstractConnectionPool.UserPasswordPair pair = 
                (AbstractConnectionPool.UserPasswordPair)key;
        
        String userName = pair.getUserName(); 
        String password = pair.getPassword();

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
                        getPingInterval(),
                        isStatementPooling(),
                        getTransactionIsolationLevel(),
                        getMaxStatements());
            else
                pooledConnection = 
                    new FBPooledConnection(
                        managedConnection, 
                        cri, 
                        isStatementPooling(),
                        getTransactionIsolationLevel(),
                        getMaxStatements());

            return pooledConnection;

        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }
    
    
    /** 
     * Get name of the connection queue.
     * 
     * @see AbstractConnectionPool#getPoolName()
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
	    return getBlockingTimeout() / 1000;
	}

	/**
	 * Set login timeout for new connection. Currently ignored.
	 * 
	 * @param seconds how long pool should wait until new connection is 
	 * granted.
	 */
	public void setLoginTimeout(int seconds) {
	    setBlockingTimeout(seconds * 1000);
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
	public synchronized PooledConnection getPooledConnection(String user, String password) 
        throws SQLException 
    {
	    return (PooledConnection)getPooledConnection(
            getQueue(new AbstractConnectionPool.UserPasswordPair(user, password)));
	}
    
    /**
     * Get XA connection. This method will block until there will be 
     * free connection to return.
     * 
     * @return instance of {@link XAConnection}.
     * 
     * @throws SQLException if pooled connection cannot be obtained.
     */
    public XAConnection getXAConnection() throws SQLException {
        return (XAConnection)getPooledConnection();
    }

    /**
     * Get XA connection for the specified user name and password.
     * 
     * @param user user name.
     * @param password password corresponding to specified user name.
     * 
     * @return instance of {@link XAConnection} for the specified
     * credentials.
     * 
     * @throws SQLException always, this method is not yet implemented.
     */
    public XAConnection getXAConnection(String user, String password)
            throws SQLException 
    {
        return (XAConnection)getPooledConnection(user, password);
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
     * Get JDBC connection properties.
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
        String pingStatement = super.getPingStatement();
        if (pingStatement != null)
            return pingStatement;
        else
            return PING_STATEMENT;
    }

    /**
     * Set database name.
     * 
     * @param database connection URL without <code>"jdbc:firebirdsql:"</code>
     * prefix (<code>"//localhost:3050/c:/database/employee.gdb"</code>) for
     * example).
     */
    public void setDatabase(String database) {
        this.database = database;
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
        
        this.properties.putAll(properties);
    }
    
    /**
     * Set specified property.
     * 
     * @param name name of the property.
     * @param value value of the property.
     */
    private void setProperty(String name, String value) {
        String canonicalName = (String)PROPERTY_NAME_MAPPING.get(name);
        if (canonicalName == null)
            canonicalName = name;
        
        this.properties.setProperty(canonicalName, value);
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
     * @param gdsType type of the JDBC driver.
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
    
    /**
     * Method that allows setting non-standard property in the form "key=value"
     * form. This method is needed by some containers to specify properties
     * in the configuration.
     * 
     * @param propertyMapping mapping between property name (key) and its value.
     * Name and value are separated with "=", ":" or whitespace character. 
     * Whitespace characters on the beginning of the string and between key and
     * value are ignored. No escaping is possible: "\n" is backslash-en, not
     * a new line mark.
     */
    public void setNonStandardProperty(String propertyMapping) {
        char[] chars = propertyMapping.toCharArray();
        StringBuffer key = new StringBuffer();
        StringBuffer value = new StringBuffer();
        
        boolean keyProcessed = false;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            
            boolean isSeparator = Character.isWhitespace(ch) || ch == '=' || ch == ':';
            
            // if no key was processed, ignore white spaces
            if (key.length() == 0 && isSeparator)
                continue;
            
            if (!keyProcessed && !isSeparator) {
                key.append(ch);
            } else if (!keyProcessed && isSeparator) {
                keyProcessed = true;
            } else if (keyProcessed && value.length() == 0 && isSeparator) {
                continue;
            } else if (keyProcessed) {
                value.append(ch);
            }
        }
        
        String keyStr = key.toString().trim();
        String valueStr = value.length() > 0 ? value.toString().trim() : null;
        
        setProperty(keyStr, valueStr);
    }

    public int getBlobBufferSize() {
        if (getProperty(BLOB_BUFFER_PROPERTY) != null)
            return getIntProperty(BLOB_BUFFER_PROPERTY);
        else
            return FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE;
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
        if (getProperty(SOCKET_BUFFER_PROPERTY) != null)
            return getIntProperty(SOCKET_BUFFER_PROPERTY);
        else
            return FBConnectionDefaults.DEFAULT_SOCKET_BUFFER_SIZE;
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
    
    private static final String REF_DATABASE = "database";
    private static final String REF_TYPE = "type";
    private static final String REF_PROPERTIES = "properties";
    private static final String REF_NON_STANDARD_PROPERTY = "nonStandard";
    
    public Reference getDefaultReference() {
        Reference ref = super.getDefaultReference();
        
        if (getDatabase() != null)            
            ref.add(new StringRefAddr(REF_DATABASE, getDatabase()));

        if (getType() != null)
            ref.add(new StringRefAddr(REF_TYPE, getType()));
        
        byte[] data = serialize(getProperties());
        ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
        
        return ref;
    }
    
    
    protected BasicAbstractConnectionPool createObjectInstance() {
        return new FBConnectionPoolDataSource();
    }
    
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
        Hashtable environment) throws Exception 
    {
        if (!(obj instanceof Reference))
            return null;
        
        Reference ref = (Reference)obj;
        ref = (Reference)ref.clone();

        FBConnectionPoolDataSource ds = 
            (FBConnectionPoolDataSource)super.getObjectInstance(
                obj, name, nameCtx, environment);
        
        if (ds == null)
            return null;
        
        for (int i = 0; i < ref.size(); i++) {
            RefAddr element = ref.get(i);
            
            String type = element.getType();
            
            if (REF_DATABASE.equals(type))
                ds.setDatabase(element.getContent().toString());
            else 
            if (REF_TYPE.equals(type))
                ds.setType(element.getContent().toString());
            else 
            if (REF_PROPERTIES.equals(element.getType())) {
                
                byte[] data = (byte[])element.getContent();
                Properties props = (Properties)deserialize(data);
                if (props != null) 
                    ds.setProperties(props);
            } else
            if (REF_NON_STANDARD_PROPERTY.equals(type)) 
                ds.setNonStandardProperty(element.getContent().toString());
            else
            if (element.getContent() instanceof String) 
                ds.setProperty(type, element.getContent().toString());
        }
        
        return ds;
    }
}
