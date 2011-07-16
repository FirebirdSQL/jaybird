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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;

/**
 * Implementation of free connection queue.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class PooledConnectionQueue {
    
    private static final boolean LOG_DEBUG_INFO = PoolDebugConfiguration.LOG_DEBUG_INFO;
    private static final boolean SHOW_STACK_ON_BLOCK = PoolDebugConfiguration.SHOW_TRACE;
    private static final boolean SHOW_STACK_ON_ALLOCATION = PoolDebugConfiguration.SHOW_TRACE;

    private PooledConnectionManager connectionManager;
    private Logger logger;
    private ConnectionPoolConfiguration configuration;
    
    private Object key;

    private String queueName;
    private int blockingTimeout;

    private int size;
    private BlockingStack stack = new BlockingStack();
    private boolean blocked;
    
    private Object takeMutex = new Object();
    
    private IdleRemover idleRemover;

    private int totalConnections;

    private HashSet workingConnections = new HashSet();
    private HashSet workingConnectionsToClose = new HashSet();
    private HashMap connectionIdleTime = new HashMap();

    /**
     * Create instance of this queue.
     * 
     * @param connectionManager instance of {@link PooledConnectionManager}.
     * @param logger instance of {@link Logger}.
     * @param configuration instance of {@link Configuration}.
     * @param queueName name of this queue.
     */
    private PooledConnectionQueue(PooledConnectionManager connectionManager,
        Logger logger, ConnectionPoolConfiguration configuration, 
        String queueName) 
    {
        this.connectionManager = connectionManager;
        this.logger = logger;
        this.configuration = configuration;
        
        this.queueName = queueName;
        this.blockingTimeout = configuration.getBlockingTimeout();
    }
    
    /**
     * Create instance of this queue for the specified user name and password.
     * 
     * @param connectionManager instance of {@link PooledConnectionManager}.
     * @param logger instance of {@link Logger}.
     * @param configuration instance of {@link Configuration}.
     * @param queueName name of this queue.
     */
    public PooledConnectionQueue(PooledConnectionManager connectionManager,
        Logger logger, ConnectionPoolConfiguration configuration, 
        String queueName, Object key)
    {
        this(connectionManager, logger, configuration, queueName);
        this.key = key;
    }
    
    /**
     * Get logger for this instance. By default all log messages belong to 
     * this class. Subclasses can override this behavior.
     * 
     * @return instance of {@link Logger}.
     */
    protected Logger getLogger() {
        return logger;
    }
    
    
    private ConnectionPoolConfiguration getConfiguration() {
        return configuration;
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
    public void start() throws SQLException {
        for (int i = 0; i < getConfiguration().getMinPoolSize(); i++) {
            try {
                addConnection(stack);
            } catch (InterruptedException iex) {
                throw new SQLException("Could not start connection queue.");
            }
        }
        
        idleRemover = new IdleRemover();
        Thread t = new Thread(idleRemover, "Pool " + queueName + " idleRemover");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Restart this queue.
     */
    public synchronized void restart()
    {
    	// flag working connections for deallocation when returned to the queue.
   		workingConnectionsToClose.addAll(workingConnections);
    	
        // close all free connections
        while (size() > 0)
            try {
            	PooledObject connection = take();
                if (connection.isValid()) {
                	connection.deallocate();
                	
                	physicalConnectionDeallocated(connection);
                }
            } catch (SQLException ex) {
                if (getLogger() != null)
                getLogger().warn("Could not close connection.", ex);
            }
        //Create enough connections to restore the queue to MinPoolSize.
        while (totalSize() < getConfiguration().getMinPoolSize())
        	try {
        		addConnection(stack);
        	}
            catch (Exception e)
            {
            	if (getLogger() != null)
                    getLogger().warn("Could not add connection.", e);
            }
    }
    
    /**
     * Shutdown this queue.
     */
    public void shutdown() {
        try {
            // close all working connections.
            Iterator iter = workingConnections.iterator();
            while (iter.hasNext()) {
                PooledObject item = (PooledObject)iter.next();
    
                if (item.isValid())
                    item.deallocate();
            }
    
            // close all free connections
            while (size() > 0)
                try {
                    PooledObject item = (PooledObject)take();
                    if (item.isValid())
                        item.deallocate();
                    
                } catch (SQLException ex) {
                    if (getLogger() != null)
                    getLogger().warn("Could not close connection.", ex);
                }
    
            totalConnections = 0;
            workingConnections.clear();
            workingConnectionsToClose.clear();
        } finally {
            if (idleRemover != null)
                idleRemover.stop();
            
            idleRemover = null;
        }
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
     * Destroy connection and restore the balance of connections in the pool.
     * 
     * @param connection connection to destroy
     */
    public void destroyConnection(PooledObject connection) {
        connection.deallocate();
        
        totalConnections--;
    }
    
    /**
     * Notify queue that a physical connection was deallocated.
     * 
     * @param connection connection that was deallocated.
     */
    public synchronized void physicalConnectionDeallocated(PooledObject connection) {
        totalConnections--;
        connectionIdleTime.remove(connection);
        workingConnections.remove(connection);
    }

    /**
     * Put connection to this queue.
     * 
     * @param connection free pooled connection.
     * 
     * @throws SQLException if connection cannot be added to this
     * queue.
     */
    public void put(PooledObject connection) throws SQLException {
        try {
            if (blocked && getLogger() != null)
                getLogger().warn("Pool " + queueName + " will be unblocked");

            if (getConfiguration().isPooling()) {
            	
            	if (workingConnectionsToClose.remove(connection)) {
            		connection.deallocate();
            		
            		physicalConnectionDeallocated(connection);
            		
            		addConnection(stack);
            	}
            	else {
            		stack.push(connection);
                    connection.setInPool(true);
                
            		// save timestamp when connection was returned to queue
            		connectionIdleTime.put(
            				connection, new Long(System.currentTimeMillis()));
    
            		size++;
            	}
            } else {
                connectionIdleTime.remove(connection);
            }

            blocked = false;

            workingConnections.remove(connection);

            // deallocate connection if pooling is not enabled.
            if (!getConfiguration().isPooling())
                destroyConnection(connection);

            if (LOG_DEBUG_INFO && getLogger() != null)
                getLogger().debug("Thread "
                        + Thread.currentThread().getName()
                        + " released connection.");

        } catch (InterruptedException iex) {
            if (getLogger() != null)
                getLogger().warn("Thread "
                    + Thread.currentThread().getName()
                    + " was interrupted.",
                    iex);

            connection.deallocate();
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
    public PooledObject take() throws SQLException {
        
        long startTime = System.currentTimeMillis();

        if (LOG_DEBUG_INFO && getLogger() != null)
            getLogger().debug(
                "Thread "
                    + Thread.currentThread().getName()
                    + " wants to take connection.");

        PooledObject result = null;
        
        SQLException pendingExceptions = null;

        try {

            synchronized(takeMutex) {
                if (stack.isEmpty()) {
    
                    while (result == null) {
                        
                        if (!keepBlocking(startTime)) {
                            String message = "Could not obtain connection during " + 
                                "blocking timeout (" + blockingTimeout + " ms)";
                            
                            FBSQLException ex = new FBSQLException(message, FBSQLException.SQL_STATE_CONNECTION_FAILURE);
                            if (pendingExceptions != null) {
                                ex.setNextException(ex);
                            }
                                
                            throw ex;
                        };
    
                        boolean connectionAdded = false;
    
                        try {
                            connectionAdded = addConnection(stack);
                        } catch (SQLException sqlex) {
                            if (getLogger() != null)
                                getLogger().warn(
                                    "Could not create connection."
                                    + sqlex.getMessage());
    
                            // could not add connection... bad luck
                            // let's wait more
                            
                            if (pendingExceptions == null)
                                pendingExceptions = sqlex;
                            else
                            if (pendingExceptions.getErrorCode() != sqlex.getErrorCode())
                                pendingExceptions.setNextException(sqlex);
    
                        }
    
                        if (!connectionAdded) {
                            String message =
                                "Pool "
                                    + queueName
                                    + " is empty and will block here."
                                    + " Thread "
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
                        
                        result = (PooledObject) stack.pop(
                            getConfiguration().getRetryInterval());
                            
                        if (result == null && getLogger() != null)
                            getLogger().warn("No connection in pool. Thread " +
                                Thread.currentThread().getName());
                        else
                        if (result != null && !connectionAdded && getLogger() != null)
                            getLogger().info("Obtained connection. Thread " + 
                                Thread.currentThread().getName());
                    }
    
                } else {
                    result = (PooledObject)stack.pop();
                }
            }

        } catch (InterruptedException iex) {
            throw new SQLException(
                "No free connection was available and "
                    + "waiting thread was interrupted.");
        }

        size--;

        workingConnections.add(result);

        if (LOG_DEBUG_INFO && getLogger() != null) {
            if (SHOW_STACK_ON_ALLOCATION)
                getLogger().debug(
                    "Thread "+ Thread.currentThread().getName() +
                     " got connection.", new Exception());
            else
                getLogger().debug(
                    "Thread "+ Thread.currentThread().getName() +
                     " got connection.");

        }

        result.setInPool(false);
        
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
    private boolean addConnection(BlockingStack queue)
        throws SQLException, InterruptedException {

        synchronized (this) {

            if (LOG_DEBUG_INFO && getLogger() != null)
                getLogger().debug(
                    "Trying to create connection, total connections "
                        + totalConnections
                        + ", max allowed "
                        + getConfiguration().getMaxPoolSize());
            
            boolean maximumCapacityReached = 
                getConfiguration().getMaxPoolSize() <= totalConnections  && 
                getConfiguration().getMaxPoolSize() != 0 &&
                getConfiguration().isPooling();

            if (maximumCapacityReached) {

                if (LOG_DEBUG_INFO && getLogger() != null)
                    getLogger().debug("Was not able to add more connections.");

                return false;
            }

            Object pooledConnection = connectionManager.allocateConnection(key);

            if (LOG_DEBUG_INFO && getLogger() != null)
                getLogger().debug(
                    "Thread "
                        + Thread.currentThread().getName()
                        + " created connection.");

            queue.push(pooledConnection);
            size++;

            totalConnections++;

            return true;
        }
    }

    /**
     * Release first connection in the queue if it was idle longer than idle
     * timeout interval.
     * 
     * @return <code>true</code> if method removed idle connection, otherwise
     * <code>false</code>
     * 
     * @throws SQLException if exception happened when releasing the connection.
     */
    private boolean releaseNextIdleConnection() throws SQLException {
                  
        synchronized(takeMutex) {
            
            if (totalSize() <= getConfiguration().getMinPoolSize())
                return false;
            
            PooledObject candidate = (PooledObject)stack.peek();
            
            if (candidate == null)
                return false;
            
            Long lastUsageTime = (Long)connectionIdleTime.get(candidate);
            if (lastUsageTime == null)
                return false;
            
            long idleTime = System.currentTimeMillis() - lastUsageTime.longValue();
            
            if (idleTime < getConfiguration().getMaxIdleTime()) 
                return false;
            
            try {    
                take().deallocate();
            } finally {
                workingConnections.remove(candidate);
                connectionIdleTime.remove(candidate);
                totalConnections--;
            }
            
            return true;
        }
    }

    /**
     * Implementation of {@link Runnable} interface responsible for removing
     * idle connections.
     */
    private class IdleRemover implements Runnable {
        
        private boolean running;
        
        public IdleRemover() {
            running = true;
        }
        
        public void stop() {
            running = false;
        }
        
        public void run() {
            while (running) {
                
                try {
                    while(releaseNextIdleConnection()) {
                        // do nothing, we already released connection
                        // next one, please :)
                    }
                } catch(SQLException ex) {
                    // do nothing, we hardly can handle this situation
                }
                
                try {
                    int idleTimeout = getConfiguration().getMaxIdleTime();
                    int maxConnections =  getConfiguration().getMaxPoolSize();
                    
                    if (maxConnections < 1)
                        maxConnections = 1;
                    
                    Thread.sleep(idleTimeout / maxConnections);
                } catch(InterruptedException ex) {
                    // do nothing
                }
            }
        }
        
    }
}

