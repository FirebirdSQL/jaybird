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
import org.firebirdsql.logging.LoggerFactory;

/**
 * This class provides JDBC connection pooling capabilities for Java 
 * applications. There are two usage scenarios: local and JNDI-enabled.
 * 
 * <p>
 * 
 * In local scenario application developer creates instance of 
 * {@link ConnectionPoolConfiguration} containing configuration parameters for
 * this pool and JDBC driver and instantiates this class passing this 
 * configuration into constructor. Before using this connection pool 
 * {@link AbstractConnectionPoolDataSource#start()} must be called.
 * 
 * <pre>
 * ConnectionPoolConfiguration config = 
 *     new FBConnectionPoolConfiguration();
 * 
 * // set properties here
 * ...
 * 
 * ConnectionPoolDataSource pool = new FBConnectionPoolDataSource(config);
 * pool.start();
 * </pre>
 * 
 * <p>
 * 
 * JNDI-enabled scenario differs from local scenario:
 * <ul>
 * 
 * <li>Pool instance is not needed at startup. It is enough to create instance 
 * of {@link ConnectionPoolConfiguration} implementing {@link Referenceable} 
 * interface. It must provide valid JNDI reference containing enough information 
 * to instantiate new JDBC pool. For example, one can use 
 * {@link FBConnectionPoolConfiguration}, it implements {@link Referenceable}
 * interface.
 * 
 * <li>Referenceable pool configuration is bound into JNDI under the desired name. 
 * JNDI provider extracts JNDI reference from configuration instance and stores
 * it under the specified name.
 * 
 * <li>It is required to add reference to {@link FBConnectionPoolObjectFactory} 
 * object factory when instantiating {@link InitialContext}. This can be done
 * by prepanding property "java.naming.factory.object" with 
 * "org.firebirdsql.pool.FBConnectionPoolObjectFactory:". This will register object
 * factory in JNDI. One can specify this property during VM startup.
 * 
 * <li>During each JNDI lookup operation our object factory will be called.
 * This object factory will check if the required JDBC pool was already created,
 * create it and start if necessary, and resolve specified reference into the 
 * type specified in pool's configuration.
 * </ul>
 * 
 * One can create instance of {@link AbstractConnectionPoolDataSource} and bind it
 * into JNDI, however, in most cases it will result in the procedure described
 * above (the only difference is that JNDI reference will be extracted not from
 * pool configuration, but from pool itself). However, this might be required 
 * with JNDI providers that are not able to handle JNDI references. In this case
 * application developer is required to instantiate and start pool before
 * binding to JNDI.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public abstract class AbstractConnectionPoolDataSource
    implements ConnectionPoolDataSource, ConnectionEventListener, Referenceable {

    private static final boolean LOG_DEBUG_INFO = false;
    private static final boolean SHOW_STACK_ON_BLOCK = false;

    private static final Logger LOG =
        LoggerFactory.getLogger(AbstractConnectionPoolDataSource.class, false);

    private ConnectionPoolConfiguration config;

    private boolean available;

    private int loginTimeout;
    private PrintWriter logWriter;

    private FBPooledConnectionQueue freeConnections;
    
    /**
     * Get logger for this instance. By default all log messages belong to 
     * this class. Subclasses can override this behavior.
     * 
     * @return instance of {@link Logger}.
     */
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Create instance of this class for the specified configuration. 
     * Required configuration parameters are:
     * 
     * @param config instance of {@link ConnectionPoolConfiguration} where 
     * all connection parameters will be queried from.
     * 
     * @throws ConfigurationException if there was configuration problem.
     */
    protected AbstractConnectionPoolDataSource(ConnectionPoolConfiguration config)
        throws SQLException 
    {

        this.config = config;

        freeConnections = new FBPooledConnectionQueue(
            getQueueName(), getConfiguration().getBlockingTimeout());
    }

    /**
     * Get configuration of this data source.
     * 
     * @return instance of {@link ConnectionPoolConfiguration} describing
     * this data source.
     */
    public ConnectionPoolConfiguration getConfiguration() {
        return config;
    }

    /**
     * Get JNDI reference for this pool instance. This method delegates call
     * to its configuration.
     * 
     * @return instance of {@link Referenceable} 
     * 
     * @throws NamingException if reference cannot be obtained.
     */
    public Reference getReference() throws NamingException {
        if (config instanceof Referenceable)
            return ((Referenceable) config).getReference();
        else
            throw new NamingException("This pool is not JNDI-enabled.");
    }

    /**
     * Start this pool by making it available through JNDI.
     * 
     * @throws SQLException if JDBC pool cannot be started.
     */
    public void start() throws SQLException {

        if (available)
            return;

        freeConnections.start();

        available = true;

        if (getLogger() != null)
            getLogger().info(
                "JDBC pool started. Queue name "
                + getQueueName()
                + ", minConnections "
                + config.getMinConnections()
                + ", maxConnections "
                + config.getMaxConnections()
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
                "Local JDBC pool shutted down. Queue name was "
                + getQueueName()
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

    /**
     * Get maximum number of connections that can be allocated by this pool.
     * 
     * @return maximum number of connections that can be allocated by this
     * pool or 0 if no upper limit exist.
     */
    public int getMaxSize() {
        return config.getMaxConnections();
    }

    /**
     * Get maximum number of connections that will remain open during 
     * lifetime of this pool.
     * 
     * @return minimum number of connections that will be allocated by this
     * pool during startup.
     */
    public int getMinSize() {
        return config.getMinConnections();
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
     * @return instance of {@link PooledConnection}
     * 
     * @throws SQLException
     */
    public synchronized PooledConnection getPooledConnection()
        throws SQLException {

        if (!available)
            throw new SQLException("Connection pool is not started.");

        PingablePooledConnection result;

        result = freeConnections.take();

        while (!result.isValid()) {
            if (getLogger() != null)
                getLogger().warn(
                    "Connection "
                    + result
                    + " was not valid, trying to get another one.");
                    
            result = (PingablePooledConnection) freeConnections.take();
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
            PingablePooledConnection connection =
                (PingablePooledConnection) event.getSource();
                
            connection.removeConnectionEventListener(this);
            
            freeConnections.put(connection);
            
        } catch (SQLException ex) {
            /** @todo think about correctly handling this thing */
            // ups, bad luck :(
            
            if (getLogger() != null)
                getLogger().warn("Error closing connection.", ex);
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
        /** @todo implement this method */
        // what do we do here?
        if (getLogger() != null)
            getLogger().error("Error occured in connection.", 
                event.getSQLException());
    }
    
    /**
     * Allocate new physical connection. This implementation uses 
     * {@link DriverManager} to allocate connection. Other implementations might
     * consider overriding this method to provide more optimized implementation.
     * 
     * @return instance of {@link Connection}
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    protected abstract Connection allocateConnection() throws SQLException;
    
    /**
     * Get name of the queue. This name will be displayed in log when pool
     * is started.
     * 
     * @return name of the connection queue.
     */
    protected abstract String getQueueName();

    /**
     * Implementation of free connection queue.
     */
    private class FBPooledConnectionQueue {

        private String queueName;
        private int blockingTimeout;

        private int size;
        private LinkedQueue queue = new LinkedQueue();
        private boolean blocked;

        private int totalConnections;

        private HashSet workingConnections = new HashSet();

        /**
         * Create instance of this queue.
         * 
         * @param queueName name of this queue.
         */
        public FBPooledConnectionQueue(String queueName, int blockingTimeout) {
            this.queueName = queueName;
            this.blockingTimeout = blockingTimeout;
        }

        /**
         * Get size of this queue. This method can be used to check how many
         * free connections is left.
         * 
         * @return size of this queue.
         */
        public int size() {
            return size;
        }

        /**
         * Get total number of physical connections opened to the database.
         * 
         * @return total number of physical connections opened to the 
         * database.
         */
        public int totalSize() {
            return totalConnections;
        }

        /**
         * Get number of working connections.
         * 
         * @return number for working connections.
         */
        public int workingSize() {
            return workingConnections.size();
        }

        /**
         * Start this queue.
         * 
         * @throws SQLException if initial number of connections 
         * could not be created.
         */
        private void start() throws SQLException {
            for (int i = 0; i < config.getMinConnections(); i++)
                try {
                    addConnection();
                } catch (InterruptedException iex) {
                    throw new SQLException("Could not start connection queue.");
                }
        }

        /**
         * Shutdown this queue.
         */
        private void shutdown() {
            // close all working connections.
            Iterator iter = workingConnections.iterator();
            while (iter.hasNext()) {
                PingablePooledConnection item = (PingablePooledConnection) iter.next();

                try {
                    item.close();
                } catch (SQLException ex) {
                    /** @todo think what to do here */
                }
            }

            // close all free connections
            while (size() > 0)
                try {
                    take().close();
                } catch (SQLException ex) {
                    /** @todo think what to do here */
                }

            totalConnections = 0;
        }
        
        /**
         * Check if {@link #take()} method can keep blocking.
         * 
         * @param startTime time when the method was entered.
         * 
         * @return <code>true</code> if method can keep blocking.
         */
        private boolean keepBlocking(long startTime) {
            return System.currentTimeMillis() - startTime < blockingTimeout;
        }

        /**
         * Put connection to this queue.
         * 
         * @param connection free pooled connection.
         * 
         * @throws SQLException if connection cannot be added to this
         * queue.
         */
        public void put(PingablePooledConnection connection) throws SQLException {
            try {
                if (blocked && getLogger() != null)
                    getLogger().warn("Pool " + queueName + " will be unblocked");

                queue.put(connection);

                size++;
                blocked = false;

                workingConnections.remove(connection);

                if (LOG_DEBUG_INFO && getLogger() != null)
                    getLogger().debug(
                        "Thread "
                            + Thread.currentThread().getName()
                            + " released connection.");

            } catch (InterruptedException iex) {
                if (getLogger() != null)
                    getLogger().warn(
                        "Thread "
                        + Thread.currentThread().getName()
                        + " was interrupted.",
                        iex);

                connection.close();
            }
        }

        /**
         * Take pooled connection from this queue. This method will block
         * until free and valid connection is available.
         * 
         * @return free instance of {@link FBPooledConnection}.
         * 
         * @throws SQLException if no free connection was available and 
         * waiting thread was interruped while waiting for a new free
         * connection.
         */
        public PingablePooledConnection take() throws SQLException {
            
            long startTime = System.currentTimeMillis();

            if (LOG_DEBUG_INFO && getLogger() != null)
                getLogger().debug(
                    "Thread "
                        + Thread.currentThread().getName()
                        + " wants to take connection.");

            PingablePooledConnection result = null;

            try {

                if (queue.isEmpty()) {

                    while (result == null) {
                        
                        if (!keepBlocking(startTime))
                            throw new SQLException(
                                "Could not obtain connection during " + 
                                "blocking timeout (" + blockingTimeout + " ms)");

                        boolean connectionAdded = false;

                        try {
                            connectionAdded = addConnection();
                        } catch (SQLException sqlex) {
                            if (getLogger() != null)
                                getLogger().warn(
                                    "Could not create connection."
                                    + sqlex.getMessage());

                            // could not add connection... bad luck
                            // let's wait more

                        }

                        if (connectionAdded) {
                            String message =
                                "Pool "
                                    + queueName
                                    + " is empty and will block here."
                                    + " thread "
                                    + Thread.currentThread().getName();

                            if (SHOW_STACK_ON_BLOCK) {
                                if (getLogger() != null)
                                    getLogger().warn(message, new Exception());
                            } else {
                                if (getLogger() != null)
                                    getLogger().warn(message);
                            }

                            blocked = true;
                        }
                        
                        result = (PingablePooledConnection) queue.poll(
                            getConfiguration().getRetryInterval());
                            
                        if (result == null && getLogger() != null)
                            getLogger().warn("No connection in pool.");
                    }

                } else {
                    result = (PingablePooledConnection)queue.take();
                }

            } catch (InterruptedException iex) {
                throw new SQLException(
                    "No free connection was available and "
                        + "waiting thread was interrupted. Sorry.");
            }

            size--;

            workingConnections.add(result);

            if (LOG_DEBUG_INFO && getLogger() != null)
                getLogger().debug(
                    "Thread "
                        + Thread.currentThread().getName()
                        + " got connection.");

            return result;
        }

        /**
         * Open new connection to database.
         * 
         * @return <code>true</code> if new physical connection was created,
         * otherwise false.
         * 
         * @throws SQLException if new connection cannot be opened.
         * @throws InterruptedException if thread was interrupted.
         */
        private boolean addConnection()
            throws SQLException, InterruptedException {

            synchronized (this) {

                if (LOG_DEBUG_INFO && getLogger() != null)
                    getLogger().debug(
                        "Trying to create connection, total connections "
                            + totalConnections
                            + ", max allowed "
                            + config.getMaxConnections());

                if (totalConnections >= config.getMaxConnections()
                    && config.getMaxConnections() != 0) {

                    if (LOG_DEBUG_INFO && getLogger() != null)
                        getLogger().debug("Was not able to add more connections.");

                    return false;
                }

                Connection connection = allocateConnection();

                PingablePooledConnection pooledConnection = null;

                if (config.isPingable())
                    pooledConnection =
                        new PingablePooledConnection(
                            connection,
                            config.getPingStatement(),
                            config.getPingInterval());
                else
                    pooledConnection = new PingablePooledConnection(connection);

                if (LOG_DEBUG_INFO && getLogger() != null)
                    getLogger().debug(
                        "Thread "
                            + Thread.currentThread().getName()
                            + " created connection.");

                queue.put(pooledConnection);
                size++;

                totalConnections++;

                return true;
            }
        }
    }
}