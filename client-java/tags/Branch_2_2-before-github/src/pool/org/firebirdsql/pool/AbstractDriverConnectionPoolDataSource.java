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
import java.sql.*;
import java.util.Properties;

import javax.naming.*;
import javax.sql.*;

import org.firebirdsql.jdbc.FBConnectionHelper;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Generic implementation of {@link javax.sql.ConnectionPoolDataSource} that
 * uses {@link java.sql.DriverManager} to open physical connections to the 
 * database.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
abstract public class AbstractDriverConnectionPoolDataSource extends BasicAbstractConnectionPool 
    implements ConnectionPoolDataSource, PooledConnectionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDriverConnectionPoolDataSource.class, false);
    
    public static final UserPasswordPair EMPTY_USER_PASSWORD = new UserPasswordPair();

    private PrintWriter logWriter;
        
    private String jdbcUrl;
    private String driverClassName;
    private int transactionIsolation = FBPoolingDefaults.DEFAULT_ISOLATION;

    private final Properties props = new Properties();
    private final DriverPooledConnectionManager connectionManager = new DriverPooledConnectionManager();
        
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    public String getProperty(String name) {
        return props.getProperty(name);
    }
    
    public void setProperty(String name, String value) {
        props.setProperty(name, value);
    }
    
    public Properties getProperties() {
        return props;
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
        
        props.putAll(properties);
    }

    protected Logger getLogger() {
        return logger;
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
    
    public int getTransactionIsolationLevel() {
        return transactionIsolation;
    }

    public void setTransactionIsolationLevel(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public String getIsolation() {
        switch (getTransactionIsolationLevel()) {

            case Connection.TRANSACTION_READ_COMMITTED:
                return FBConnectionHelper.TRANSACTION_READ_COMMITTED;

            case Connection.TRANSACTION_REPEATABLE_READ:
                return FBConnectionHelper.TRANSACTION_REPEATABLE_READ;

            case Connection.TRANSACTION_SERIALIZABLE:
                return FBConnectionHelper.TRANSACTION_SERIALIZABLE;

            default:
                throw new IllegalStateException(
                        "Unknown transaction isolation level");
        }
    }

    public void setIsolation(String isolation) throws SQLException {
        if (FBConnectionHelper.TRANSACTION_READ_COMMITTED
                .equalsIgnoreCase(isolation))
            setTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        else if (FBConnectionHelper.TRANSACTION_REPEATABLE_READ
                .equalsIgnoreCase(isolation))
            setTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        else if (FBConnectionHelper.TRANSACTION_SERIALIZABLE
                .equalsIgnoreCase(isolation))
            setTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE);
        else
            throw new FBSQLException("Unknown transaction isolation.",
                    FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
    }

    
    /**
     * Get connection manager that will allocate physical connections to the
     * database. 
     * 
     * @return instance of {@link PooledConnectionManager} class.
     */
    protected PooledConnectionManager getConnectionManager() throws SQLException {
        return connectionManager;
    }

    /**
     * Get name of this connection pool.
     * 
     * @return name of the pool, equal to the JDBC URL value.
     */
    protected String getPoolName() {
        return getJdbcUrl();
    }
    
    /**
     * Get pooled connection from the pooled queue.
     */
    protected PooledObject getPooledConnection(PooledConnectionQueue queue) throws SQLException {
        PingablePooledConnection connection = (PingablePooledConnection) super.getPooledConnection(queue);

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
    public PooledConnection getPooledConnection() throws SQLException {
        return (PooledConnection) getPooledConnection(getQueue(EMPTY_USER_PASSWORD));
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
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return (PooledConnection) getPooledConnection(getQueue(new UserPasswordPair(user, password)));
    }
    
    /**
     * Notify about connection being closed.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
     */
    public void connectionClosed(ConnectionEvent connectionEvent) {
        PooledObjectEvent event = new PooledObjectEvent(connectionEvent.getSource());
        pooledObjectReleased(event);
    }
    
    /**
     * Notify about physical connection being closed.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
     */
    public void physicalConnectionClosed(ConnectionEvent connectionEvent) {
        PooledObjectEvent event = new PooledObjectEvent(connectionEvent.getSource(), true);
        pooledObjectReleased(event);
    }
    
    /**
     * Notify about the deallocation of the physical connection.
     * 
     * @param connectionEvent instance of {@link ConnectionEvent}.
     */
    public void physicalConnectionDeallocated(ConnectionEvent connectionEvent) {
        PooledObjectEvent event = new PooledObjectEvent(connectionEvent.getSource(), true);
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

    /*
     * JNDI-related stuff
     */
    protected static final String PROPERTIES = "properties";
    protected static final String DRIVER_CLASS_NAME = "driverClassName";
    protected static final String JDBC_URL = "jdbcUrl";
    
    /**
     * Create instance of this data source.
     */
    protected BasicAbstractConnectionPool createObjectInstance() {
        return FBPooledDataSourceFactory.createDriverConnectionPoolDataSource();
    }
    
    /**
     * Get object instance for the specified name in the specified context.
     */
    /**
     * Get default JNDI reference for this instance.
     */
    public Reference getDefaultReference() {
        Reference ref = super.getDefaultReference();
        
        if (getDriverClassName() != null)            
            ref.add(new StringRefAddr(DRIVER_CLASS_NAME, getDriverClassName()));

        if (getJdbcUrl() != null)            
            ref.add(new StringRefAddr(JDBC_URL, getJdbcUrl()));
        
        byte[] data = serialize(getProperties());
        ref.add(new BinaryRefAddr(PROPERTIES, data));
        
        return ref;
    }
    
    protected Object processObjectInstance(AbstractDriverConnectionPoolDataSource ds, Object obj) throws Exception
    {
        if (ds == null)
            return null;

        Reference ref = (Reference) obj;

        String addr;

        addr = getRefAddr(ref, DRIVER_CLASS_NAME);
        if (addr != null)
            ds.setDriverClassName(addr);

        addr = getRefAddr(ref, JDBC_URL);
        if (addr != null)
            ds.setJdbcUrl(addr);

        RefAddr binAddr = ref.get(PROPERTIES);
        if (binAddr != null) {
            byte[] data = (byte[]) binAddr.getContent();
            Properties props = (Properties) deserialize(data);
            if (props != null)
                ds.setProperties(props);
        }

        return ds;
    }

    /**
     * Pooled connection manager that uses {@link java.sql.DriverManager}
     * to allocate physical connections.
     */
    private class DriverPooledConnectionManager implements PooledConnectionManager {
    
        private static final String USER_NAME_PROPERTY = "user";
        private static final String PASSWORD_PROPERTY = "password";
        
        private boolean driverInitialized;
    
        public PooledObject allocateConnection(Object key, PooledConnectionQueue queue) throws SQLException {
            if (!driverInitialized) {
                try {
                    Class.forName(getDriverClassName());
                    driverInitialized = true;
                    
                } catch(ClassNotFoundException ex) {
                    throw new FBSQLException(
                        "Class " + getDriverClassName() + " not found.");
                }
            }
            
            if (!(key instanceof UserPasswordPair))
                throw new FBSQLException("Incorrect key.");
            final UserPasswordPair pair = (UserPasswordPair) key;
                
            // set all properties
            final Properties props = new Properties();
            props.putAll(getProperties());

            final String userName = pair.getUserName();
            if (userName != null)
                props.setProperty(USER_NAME_PROPERTY, userName);
            final String password = pair.getPassword();
            if (password != null)
                props.setProperty(PASSWORD_PROPERTY, password);
            
            // open JDBC connection to the database
            Connection connection = DriverManager.getConnection(getJdbcUrl(), props);
            
            // wrap connection into PooledObject implementation
            final PingablePooledConnection pooledConnection;

            if (isPingable())
                pooledConnection = new PingablePooledConnection(
                        connection, getPingStatement(), getPingInterval(),
                        isStatementPooling(), getMaxStatements(), isKeepStatements(), queue);
            else
                pooledConnection = new PingablePooledConnection(
                        connection, isStatementPooling(), getMaxStatements(),
                        isKeepStatements(), queue);
            
            pooledConnection.setDefaultTransactionIsolation(getTransactionIsolationLevel());

            return pooledConnection;
        }
    }
    
 }
