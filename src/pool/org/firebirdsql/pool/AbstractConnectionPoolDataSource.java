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

import java.sql.*;
import javax.sql.*;
import java.io.PrintWriter;

import java.util.*;

import javax.naming.*;

import EDU.oswego.cs.dl.util.concurrent.*;
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

    private boolean available;

    private int loginTimeout;
    private PrintWriter logWriter;

    private PooledConnectionQueue freeConnections;
    
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

        freeConnections = new PooledConnectionQueue(this, 
            getPoolName(), getConfiguration().getBlockingTimeout());

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

        if (getLogger() != null)
            getLogger().info(
                "Pool shutted down. Pool name was "
                + getPoolName()
                + ".");

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
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if pooled connection cannot be obtained.
     */
    public synchronized PooledConnection getPooledConnection()
        throws SQLException {

        if (!available)
            throw new SQLException("Connection pool is not started.");

        PooledConnection result;

        result = freeConnections.take();

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
                    freeConnections.destroyConnection(result);

                    // take another one
                    result = (PooledConnection)freeConnections.take();
                }
            }
        }

        result.addConnectionEventListener(this);
        return result;
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
        throws SQLException {
        /**@todo Implement this getPooledConnection() method*/
        throw new SQLException("Method getPooledConnection() not yet implemented.");
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
            
            freeConnections.put(connection);
            
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
     * Allocate new physical connection. 
     * 
     * @return instance of {@link PooledConnection}
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    protected abstract PooledConnection allocateConnection() throws SQLException;
    
    /**
     * Get name of the pool. This name will be displayed in log when pool
     * is started.
     * 
     * @return name of the connection queue.
     */
    protected abstract String getPoolName();
}