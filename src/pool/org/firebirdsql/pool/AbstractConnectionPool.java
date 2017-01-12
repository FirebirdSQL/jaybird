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

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.logging.Logger;

/**
 * Abstract class for creating connection pools. Subclasses must implement
 * factory method to produce physical connections to the database (method
 * {@link #getConnectionManager()} and few utility methods ({@link #getLogger()}
 * and {@link #getPoolName}).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public abstract class AbstractConnectionPool extends RootCommonDataSource implements PooledObjectListener {
        
    /**
     * Structure class to store user name and password. 
     */
    protected static class UserPasswordPair {
        private final String userName;
        private final String password;
    
        public UserPasswordPair() {
            this(null, null);
        }
    
        public String getUserName() {
            return userName;
        }
        
        public String getPassword() {
            return password;
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

    /**
     * This constant controls behavior of this class in case of
     * severe error situation. Usually, if value of this constant 
     * is <code>true</code>, such error condition will result in
     * raising either runtime exception or error.
     */
    private static final boolean PARANOID_MODE = true;

    /**
     * This map contains mapping between key and pooled connection queue. 
     */
    private final Map<UserPasswordPair, PooledConnectionQueue> connectionQueues =
            Collections.synchronizedMap(new HashMap<UserPasswordPair, PooledConnectionQueue>());

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
    protected AbstractConnectionPool(){
        // empty
    }

    /**
     * Shutdown pool if object is garbage collected.
     * 
     * @throws Throwable if something bad happened.
     */
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Restart this JDBC pool.  This method restarts all JDBC connections.
     */
    public void restart() {
        synchronized (connectionQueues) {
            Iterator<PooledConnectionQueue> iter = connectionQueues.values().iterator();
            while(iter.hasNext()) {
                PooledConnectionQueue queue = iter.next();
                try {
                    queue.restart();
                } catch (SQLException sqlex) {
                    getLogger().warn("Errors during pool restart: ", sqlex);
                    try {
                        queue.shutdown();
                    } catch (SQLException ex) {
                        // ignore
                    } finally {
                        iter.remove();
                    }
                }
            }
        }

        if (getLogger() != null)
            getLogger().info("Pool restarted. Pool name was " + getPoolName() + ".");

    }

    /**
     * Shutdown this JDBC pool. This method closes all JDBC connections
     * and marks pool as shut down.
     */
    public void shutdown() {
        synchronized (connectionQueues) {
            Iterator<PooledConnectionQueue> iter = connectionQueues.values().iterator();
            while(iter.hasNext()) {
                PooledConnectionQueue queue = iter.next();
                try {
                    queue.shutdown();
                } catch (SQLException sqlex) {
                    getLogger().error("Errors during pool shutdown: ", sqlex);
                } finally {
                    iter.remove();
                }
            }
        }

        if (getLogger() != null)
            getLogger().info("Pool shutdown. Pool name was " + getPoolName() + ".");
    }
    
    /**
     * Get queue for the specified user name and password.
     * 
     * @param key key identifying pool.
     * 
     * @return instance of {@link PooledConnectionQueue}.
     * 
     * @throws SQLException if something went wrong.
     */
    public PooledConnectionQueue getQueue(Object key) throws SQLException {
        synchronized(connectionQueues) {
            PooledConnectionQueue queue = connectionQueues.get(key);

            if (queue == null) {
                queue = new PooledConnectionQueue(
                    getConnectionManager(),
                    getLogger(),
                    getConfiguration(),
                    getPoolName(),
                    key);

                queue.start();

                connectionQueues.put((UserPasswordPair) key, queue);
            }

            return queue;
        }
    }

    /**
     * Get pooled connection. This method will block until there will be 
     * free connection to return.
     * 
     * @param queue instance of {@link PooledConnectionQueue} where connection
     * will be obtained.
     * 
     * @return instance of {@link PooledObject}.
     * 
     * @throws SQLException if pooled connection cannot be obtained.
     */
    protected PooledObject getPooledConnection(PooledConnectionQueue queue) throws SQLException {
        PooledObject result = queue.take();

        if (result instanceof XPingableConnection) {
            boolean isValid;
            do {
                XPingableConnection pingableConnection = (XPingableConnection) result;
    
                final long lastPingTime = pingableConnection.getLastPingTime();
                final long pingInterval = System.currentTimeMillis() - lastPingTime;

                isValid = true;
                if (getConfiguration().getPingInterval() > 0)
                    isValid = pingInterval < getConfiguration().getPingInterval();
                
                if (!isValid && !(isValid = pingableConnection.ping())) {
                    if (getLogger() != null)
                        getLogger().warn("Connection " + result + " was not valid, trying to get another one.");
                            
                    // notify queue that invalid connection should be destroyed
                    queue.destroyConnection(result);

                    // take another one
                    result = queue.take();
                }
            } while (!isValid);
        }
        return result;
    }
    
    /**
     * Notify about new available connection. This method is called by 
     * {@link javax.sql.PooledConnection} when its wrapped connection being closed.
     * 
     * @param event instance of {@link PooledObjectEvent} containing 
     * information about closed connection.
     */
    public void pooledObjectReleased(PooledObjectEvent event) {
        try {
            final PooledObject connection = (PooledObject) event.getSource();
            final PooledConnectionQueue queue = connection.getOwningQueue();
                
            if (queue == null) {
                if (getLogger() != null)
                    getLogger().warn("Connection " + connection + " does not have corresponding queue");

                if (PARANOID_MODE) {
                    throw new IllegalStateException("Connection " + connection + " does not have corresponding queue");
                } else {
                    connection.deallocate();
                }
            } else if (event.isDeallocated()) {
                queue.physicalConnectionDeallocated(connection);
            } else {
                queue.put(connection);
            }
        } catch (SQLException ex) {
            if (getLogger() != null)
                getLogger().warn("Error releasing connection.", ex);
        }
    }    

    /**
     * Notify about the deallocation of the physical connection (for example,
     * when connection is removed by the idle remover thread).
     * 
     * @param event instance of {@link PooledObjectEvent}.
     */
    protected void physicalConnectionDeallocated(PooledObjectEvent event) {
        // Nothing to do
    }
 
    /**
     * Get configuration of this data source.
     * 
     * @return instance of {@link ConnectionPoolConfiguration} describing
     * this data source.
     */
    public abstract ConnectionPoolConfiguration getConfiguration();

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
    public abstract int getFreeSize() throws SQLException;

    /**
     * Get total size of physical connections opened to the database.
     * 
     * @return total number of opened connections to the database.
     */
    public abstract int getTotalSize() throws SQLException;

    /**
     * Get number of connections that are in use.
     * 
     * @return number of working connections.
     */
    public abstract int getWorkingSize() throws SQLException ;
}