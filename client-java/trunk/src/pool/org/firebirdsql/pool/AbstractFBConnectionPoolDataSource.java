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
import java.util.*;

import javax.naming.*;
import javax.resource.ResourceException;
import javax.sql.*;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.*;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Connection pool for Firebird JDBC driver.
 * 
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
 * <li><code>sqlRole</code> 
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
abstract public class AbstractFBConnectionPoolDataSource extends BasicAbstractConnectionPool
    implements PooledConnectionManager, ConnectionPoolDataSource, 
    XADataSource, PooledConnectionEventListener,
    FirebirdPool
{
    
    public static final AbstractConnectionPool.UserPasswordPair 
        EMPTY_USER_PASSWORD = new AbstractConnectionPool.UserPasswordPair();
    
    private static final String PING_STATEMENT = ""
        + "SELECT cast(1 AS INTEGER) FROM rdb$database" 
        ;
    
    private static final Logger LOG =
        LoggerFactory.getLogger(FBConnectionPoolDataSource.class, false);
        
	private transient PrintWriter logWriter;
    
    private transient FBManagedConnectionFactory mcf;
    
    private FBConnectionProperties connectionProperties = new FBConnectionProperties();

    /**
     * Create instance of this class.
     */
    public AbstractFBConnectionPoolDataSource() {
        super();
    }
    
    /**
     * Get connection properties. This method returns a live object where 
     * connection properties can be set. Usually application does not need to
     * call this method, since this class implements appropriate interface and
     * all properties can set directly. However, this is needed for custom
     * serialization.
     * 
     * @return "live" instance of {@link FBConnectionProperties}.
     */
    public FBConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }
    
    /**
     * Set the connection properties in bulk. This method replaces the instance
     * created in constructor, therefore, if the managed connection factory was
     * already initialized with that instance, change will not be visible to it.
     * In this case appropriate exception is thrown.
     * 
     * @param props instance of {@link FBConnectionProperties}.
     * 
     * @throws IllegalPoolStateException if managed connection factory is already
     * initialized.
     */
    public void setConnectionProperties(FBConnectionProperties props) {
        if (props == null)
            throw new NullPointerException();
        
        checkNotStarted();
        
        this.connectionProperties = props;
    }

    private void checkNotStarted() throws IllegalStateException {
        if (mcf != null)
            throw new IllegalStateException(
                    "ManagedConnectionFactory is already instantiated, " +
                    "changing connection properties in bulk is not allowed.");
    }
    
    private synchronized FBManagedConnectionFactory getManagedConnectionFactory() {
        if (mcf != null)
            return mcf;
        
        GDSType gdsType = GDSType.getType(getType());
        
        mcf = new FBManagedConnectionFactory(GDSFactory.getGDSForType(gdsType), connectionProperties);
        
        return mcf;
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

        try {
            FBConnectionRequestInfo defaultCri = 
                getManagedConnectionFactory().getDefaultConnectionRequestInfo();
            
            if (userName != null)
                defaultCri.setUserName(userName);
            
            if (password != null)
                defaultCri.setPassword(password);

            FBManagedConnection managedConnection = (FBManagedConnection)
                getManagedConnectionFactory().createManagedConnection(null, defaultCri);
            managedConnection.setConnectionSharing(false);
            managedConnection.setManagedEnvironment(false);

            PingablePooledConnection pooledConnection = null;

            if (isPingable())
                pooledConnection =
                    new FBPooledConnection(
                        managedConnection,
                        defaultCri,
                        getPingStatement(),
                        getPingInterval(),
                        isStatementPooling(),
                        /*getTransactionIsolationLevel(),*/
                        getMaxStatements(),
                        isKeepStatements());
            else
                pooledConnection = 
                    new FBPooledConnection(
                        managedConnection, 
                        defaultCri, 
                        isStatementPooling(),
                        /*getTransactionIsolationLevel(),*/
                        getMaxStatements(),
                        isKeepStatements());

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
		FBPooledConnection connection = 
            (FBPooledConnection)super.getPooledConnection(queue);

        connection.addConnectionEventListener(this);
        connection.setManagedEnvironment(false);

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
        FBPooledConnection result = (FBPooledConnection)getPooledConnection();
        
        result.setManagedEnvironment(true);
        
        return result;
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
        FBPooledConnection result = (FBPooledConnection)getPooledConnection(user, password);
        
        result.setManagedEnvironment(true);
        
        return result;
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
     * Notify about physical connection being closed.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
     */
    public void physicalConnectionClosed(ConnectionEvent connectionEvent) {
        PooledObjectEvent event = 
            new PooledObjectEvent(connectionEvent.getSource(), true);
            
        pooledObjectReleased(event);
    }

    /**
     * Notify about the deallocation of the physical connection.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
     */
    public void physicalConnectionDeallocated(ConnectionEvent connectionEvent) {
        PooledObjectEvent event = 
            new PooledObjectEvent(connectionEvent.getSource(), true);
        
        physicalConnectionDeallocated(event);
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
     * Set JDBC properties that will be passed when opening a connection.
     * 
     * @param properties instance of {@link Properties} containing properties
     * of a connection to open.
     * 
     * @see #getProperties()
     */
    public void setProperties(Properties properties) {
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            
            setNonStandardProperty(key, value);
        }
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
        return connectionProperties.getType();
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
    public void setType(String type) {
        checkNotStarted();
        connectionProperties.setType(type);
    }
    
    /**
     * Get type of JDBC driver that is used.
     * 
     * @return type of JDBC driver that is used.
     */
    public GDSType getGDSType() {
        return GDSType.getType(getType());
    }
    
    /**
     * Set type of the JDBC driver to use.
     *
     * @param gdsType type of the JDBC driver.
     */
    public void setGDSType(GDSType gdsType) {
        checkNotStarted();
        setType(gdsType.toString());
    }



    /*
     * Properties of this data source. These methods are created only
     * for user convenience and are shortcuts to setProperty(String, String)
     * method call with respective keys.
     */

    public String getNonStandardProperty(String key) {
        return connectionProperties.getNonStandardProperty(key);
    }
    
    public void setNonStandardProperty(String key, String value) {
        checkNotStarted();
        connectionProperties.setNonStandardProperty(key, value);
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
        checkNotStarted();
        connectionProperties.setNonStandardProperty(propertyMapping);
    }

    public int getBlobBufferSize() {
        return connectionProperties.getBlobBufferSize();
    }

    public void setBlobBufferSize(int blobBufferSize) {
        checkNotStarted();
        connectionProperties.setBlobBufferSize(blobBufferSize);
    }

    public String getEncoding() {
        return connectionProperties.getEncoding();
    }

    public void setEncoding(String encoding) {
        checkNotStarted();
        connectionProperties.setEncoding(encoding);
    }

    public String getCharSet() {
        return connectionProperties.getCharSet();
    }
    
    public void setCharSet(String charSet) {
        checkNotStarted();
        connectionProperties.setCharSet(charSet);
    }
    
    public String getPassword() {
        return connectionProperties.getPassword();
    }

    public void setPassword(String password) {
        checkNotStarted();
        connectionProperties.setPassword(password);
    }

    public int getSocketBufferSize() {
        checkNotStarted();
        return connectionProperties.getSocketBufferSize();
    }

    public void setSocketBufferSize(int socketBufferSize) {
        checkNotStarted();
        connectionProperties.setSocketBufferSize(socketBufferSize);
    }
    
    public String getRoleName() {
        return connectionProperties.getRoleName();
    }
    
    public void setRoleName(String roleName) {
        checkNotStarted();
        connectionProperties.setRoleName(roleName);
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

    public String getTpbMapping() {
        return connectionProperties.getTpbMapping();
    }

    public void setTpbMapping(String tpbMapping) {
        checkNotStarted();
        connectionProperties.setTpbMapping(tpbMapping);
    }

    public String getUserName() {
        return connectionProperties.getUserName();
    }

    public void setUserName(String userName) {
        checkNotStarted();
        connectionProperties.setUserName(userName);
    }
    
    public int getBuffersNumber() {
        return connectionProperties.getBlobBufferSize();
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

    public String getSqlDialect() {
        return connectionProperties.getSqlDialect();
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return connectionProperties.getTransactionParameters(isolation);
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

    public void setBuffersNumber(int buffersNumber) {
        checkNotStarted();
        connectionProperties.setBuffersNumber(buffersNumber);
    }

    public void setDatabase(String database) {
        checkNotStarted();
        connectionProperties.setDatabase(database);
    }

    public void setDefaultIsolation(String isolation) {
        checkNotStarted();
        connectionProperties.setDefaultIsolation(isolation);
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        checkNotStarted();
        connectionProperties.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public void setSqlDialect(String sqlDialect) {
        checkNotStarted();
        connectionProperties.setSqlDialect(sqlDialect);
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        checkNotStarted();
        connectionProperties.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        checkNotStarted();
        connectionProperties.setTransactionParameters(isolation, tpb);
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        checkNotStarted();
        connectionProperties.setUseStandardUdf(useStandardUdf);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        checkNotStarted();
        connectionProperties.setUseStreamBlobs(useStreamBlobs);
    }

    public void setUseTranslation(String translationPath) {
        checkNotStarted();
        connectionProperties.setUseTranslation(translationPath);
    }

    
    protected static final String REF_PROPERTIES = "properties";
    protected static final String REF_NON_STANDARD_PROPERTY = "nonStandard";
    
    public Reference getDefaultReference() {
        Reference ref = super.getDefaultReference();
        
        byte[] data = serialize(connectionProperties);
        ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
        
        return ref;
    }
    
    protected Object processObjectInstance(FBConnectionPoolDataSource ds, Reference ref) throws Exception
    {
        if (ds == null)
            return null;

        for (int i = 0; i < ref.size(); i++) {
            RefAddr element = ref.get(i);

            String type = element.getType();

            if (REF_NON_STANDARD_PROPERTY.equals(type))
                ds.setNonStandardProperty(element.getContent().toString());
            else
            if (REF_PROPERTIES.equals(type)) {

                byte[] data = (byte[]) element.getContent();
                FBConnectionProperties props = (FBConnectionProperties) deserialize(data);
                ds.setConnectionProperties(props);
            } else 
            if (element.getContent() instanceof String)
                ds.setNonStandardProperty(type, element.getContent().toString());
        }

        return ds;
    }
    
    
    protected BasicAbstractConnectionPool createObjectInstance() {
        return new FBConnectionPoolDataSource();
    }

}
