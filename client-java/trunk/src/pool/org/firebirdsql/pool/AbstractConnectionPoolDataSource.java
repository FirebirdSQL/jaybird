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
import java.util.Iterator;
import java.util.Map;

import javax.naming.*;
import javax.sql.*;

import org.firebirdsql.logging.Logger;

/**
 * Abstract class for creating connection pools. Subclasses must implement
 * factory method to produce physical connections to the database (method
 * {@link #allocateConnection()} and few utility methods ({@link #getConfiguration()},
 * {@link #getLogger()} and {@link #getPoolName}).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public abstract class AbstractConnectionPoolDataSource
    implements ConnectionPoolDataSource, ConnectionEventListener, Referenceable {
        
    /**
     * This constant controls behavior of this class in case of
     * severe error situation. Usually, if value of this constant 
     * is <code>true</code>, such error condition will result in
     * raising either runtime exception or error.
     */
    private static final boolean PARANOID_MODE = true;

    /**
     * Structure class to store user name and password. 
     */
    private static class UserPasswordPair {
        private String userName;
        private String password;
    
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

    private boolean available;

    private int loginTimeout;
    private PrintWriter logWriter;

    private PooledConnectionQueue freeConnections;
    
    private HashMap personalizedQueues = new HashMap();
    private HashMap connectionQueues = new HashMap();
    
    /**
     * Get logger for this instance. By default all log messages belong to 
     * this class. Subclasses can override this behavior.
     * 
     * @return instance of {@link Logger}.
     */
    protected abstract Logger getLogger();

    /**
     * Create instance of this class.
     */
    protected AbstractConnectionPoolDataSource(){
        // empty
    }

    /**
     * Get configuration of this data source.
     * 
     * @return instance of {@link ConnectionPoolConfiguration} describing
     * this data source.
     */
    public abstract ConnectionPoolConfiguration getConfiguration();

    /**
     * Get JNDI reference for this pool instance. This method delegates call
     * to its configuration.
     * 
     * @return instance of {@link Referenceable} 
     * 
     * @throws NamingException if reference cannot be obtained.
     */
    public Reference getReference() throws NamingException {
        if (getConfiguration() instanceof Referenceable)
            return ((Referenceable) getConfiguration()).getReference();
        else
            throw new NamingException("This pool is not JNDI-enabled.");
    }

    /**
     * Start this pool.
     * 
     * @throws SQLException if JDBC pool cannot be started.
     */
    public void start() throws SQLException {

        freeConnections = new PooledConnectionQueue(getConnectionManager(), 
            getLogger(), getConfiguration(), getPoolName());

        if (available)
            return;

        freeConnections.start();

        available = true;

        if (getLogger() != null)
            getLogger().info(
                "Pool started. Pool name "
                + getPoolName()
                + ", minConnections "
                + getConfiguration().getMinConnections()
                + ", maxConnections "
                + getConfiguration().getMaxConnections()
                + ".");
    }

    /**
     * Shutdown pool if object is garbage collected.
     * 
     * @throws Throwable if something bad happened.
     */
    protected void finalize() throws Throwable {
        shutdown();
    }

    /**
     * Shutdown this JDBC pool. This method closes all JDBC connections
     * and marks pool as shut down.
     */
    public void shutdown() {
        available = false;

        freeConnections.shutdown();
        
        Iterator iter = personalizedQueues.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            PooledConnectionQueue queue = 
                (PooledConnectionQueue)entry.getValue();
                
            queue.shutdown();
        }

        if (getLogger() != null)
            getLogger().info(
                "Pool shutted down. Pool name was "
                + getPoolName()
                + ".");

    }
    
    /**
     * Get queue for the specified user name and password.
     * 
     * @param userName user name.
     * @param password password.
     * 
     * @return instance of {@link PooledConnectionQueue}.
     * 
     * @throws SQLException if something went wrong.
     */
    public PooledConnectionQueue getQueue(String userName, String password)
        throws SQLException 
    {
        UserPasswordPair key = new UserPasswordPair(userName, password);
        
        synchronized(personalizedQueues) {
            PooledConnectionQueue queue = 
                (PooledConnectionQueue)personalizedQueues.get(key);
                
            if (queue == null) {
                queue = new PooledConnectionQueue(
                    getConnectionManager(), 
                    getLogger(), 
                    getConfiguration(), 
                    getPoolName(),
                    userName,
                    password);
                    
                queue.start();
                    
                personalizedQueues.put(key, queue);
            }
            
            return queue;
        }
    }

    /**
     * Get number of free connections in this pool. This method returns the 
     * number of free open connections to the specified database. It might
     * return 0, but this does not mean that next request will block. This 
     * will happen only if 
     * <code>getMaxSize() != 0 && getMaxSize() == getWorkingSize()</code>,
     * meaning that we have allocated maximum number of connections and all
     * of them are in use.
     * 
     * @return number of free connections left.
     */
    public int getFreeSize() {
        return freeConnections.size();
    }

    /**
     * Get total size of physical connections opened to the database.
     * 
     * @return total number of opened connections to the database.
     */
    public int getTotalSize() {
        return freeConnections.totalSize();
    }

    /**
     * Get number of connections that are in use.
     * 
     * @return number of working connections.
     */
    public int getWorkingSize() {
        return freeConnections.workingSize();
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
     * Get pooled connection. This method will block until there will be 
     * free connection to return.
     * 
     * @param queue instance of {@link PooledConnectionQueue} where connection
     * will be obtained.
     * 
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if pooled connection cannot be obtained.
     */
    protected synchronized PooledConnection getPooledConnection(
        PooledConnectionQueue queue) throws SQLException 
    {

        if (!available)
            throw new SQLException("Connection pool is not started.");

        PooledConnection result;

        result = queue.take();

        if (result instanceof XPingableConnection) {
            
            boolean isValid = false;
            
            while (!isValid) {

                XPingableConnection pingableConnection = (XPingableConnection)result;
    
                long lastPingTime = pingableConnection.getLastPingTime();
                long pingInterval = System.currentTimeMillis() - lastPingTime;
                
                isValid = pingInterval < getConfiguration().getPingInterval();
                
                if (!isValid && !pingableConnection.ping()) {
                    if (getLogger() != null)
                        getLogger().warn(
                            "Connection " + result
                            + " was not valid, trying to get another one.");
                            
                    // notify queue that invalid connection was destroyed
                    queue.destroyConnection(result);

                    // take another one
                    result = (PooledConnection)queue.take();
                }
            }
        }

        result.addConnectionEventListener(this);
        
        // save the queue to which this connection belongs to
        connectionQueues.put(result, queue);
        
        return result;
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
        return getPooledConnection(freeConnections);
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
        return getPooledConnection(getQueue(user, password));
    }

    /**
     * Notify about new available connection. This method is called by 
     * {@link PooledConnection} when its wrapped connection being closed.
     * 
     * @param event instance of {@link ConnectionEvent} containing 
     * information about closed connection.
     */
    public void connectionClosed(ConnectionEvent event) {
        try {
            PooledConnection connection =
                (PooledConnection) event.getSource();
                
            connection.removeConnectionEventListener(this);
            
            PooledConnectionQueue queue = 
                (PooledConnectionQueue)connectionQueues.get(connection);
                
            if (queue == null) {
                if (getLogger() != null)
                    getLogger().warn("Connection " + connection + 
                        " does not have corresponding queue");
                    
                if (PARANOID_MODE)
                    throw new IllegalStateException(
                        "Connection " + connection + 
                        " does not have corresponding queue");
                else
                    connection.close();
            } else
                queue.put(connection);
                
        } catch (SQLException ex) {

            if (getLogger() != null)
                getLogger().warn("Error releasing connection.", ex);
        }
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
    
    /**
     * Get instance of {@link PooledConnectionManager} responsible for 
     * instantiating pooled connections.
     * 
     * @return instance of {@link PooledConnectionManager}
     * 
     * @throws SQLException if connection manager cannot be obtained.
     */
    protected abstract PooledConnectionManager getConnectionManager()
        throws SQLException;
    
    /**
     * Get name of the pool. This name will be displayed in log when pool
     * is started.
     * 
     * @return name of the connection queue.
     */
    protected abstract String getPoolName();
}