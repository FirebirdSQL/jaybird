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
 * Implementation of free connection queue.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class PooledConnectionQueue {
    
    private static final boolean LOG_DEBUG_INFO = false;
    private static final boolean SHOW_STACK_ON_BLOCK = false;
    

    private AbstractConnectionPoolDataSource owner;

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
     * @param owner instance of {@link AbstractConnectionPoolDataSource} that
     * owns this queue.
     * 
     * @param queueName name of this queue.
     * 
     * @param blockingTimeout number of milliseconds during which current thread
     * will still wait for free connection in {@link #take()} method.
     */
    public PooledConnectionQueue(AbstractConnectionPoolDataSource owner, 
        String queueName, int blockingTimeout) 
    {
        this.owner = owner;
        this.queueName = queueName;
        this.blockingTimeout = blockingTimeout;
    }
    
    /**
     * Get logger for this instance. By default all log messages belong to 
     * this class. Subclasses can override this behavior.
     * 
     * @return instance of {@link Logger}.
     */
    protected Logger getLogger() {
        return owner.getLogger();
    }
    
    
    private ConnectionPoolConfiguration getConfiguration() {
        return owner.getConfiguration();
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
        for (int i = 0; i < getConfiguration().getMinConnections(); i++)
            try {
                addConnection();
            } catch (InterruptedException iex) {
                throw new SQLException("Could not start connection queue.");
            }
    }

    /**
     * Shutdown this queue.
     */
    public void shutdown() {
        // close all working connections.
        Iterator iter = workingConnections.iterator();
        while (iter.hasNext()) {
            PooledConnection item = (PooledConnection) iter.next();

            try {
                item.close();
            } catch (SQLException ex) {
                getLogger().warn("Could not close connection.", ex);
            }
        }

        // close all free connections
        while (size() > 0)
            try {
                take().close();
            } catch (SQLException ex) {
                getLogger().warn("Could not close connection.", ex);
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
     * Destroy connection and restore the balance of connections in the pool.
     * 
     * @param connection connection to destroy
     */
    public void destroyConnection(PooledConnection connection) {
        try {
            connection.close();
        } catch(SQLException ex) {
            // do nothing, connection is invalid anyway
        }
        
        size++;
    }

    /**
     * Put connection to this queue.
     * 
     * @param connection free pooled connection.
     * 
     * @throws SQLException if connection cannot be added to this
     * queue.
     */
    public void put(PooledConnection connection) throws SQLException {
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
    public PooledConnection take() throws SQLException {
        
        long startTime = System.currentTimeMillis();

        if (LOG_DEBUG_INFO && getLogger() != null)
            getLogger().debug(
                "Thread "
                    + Thread.currentThread().getName()
                    + " wants to take connection.");

        PooledConnection result = null;

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
                    
                    result = (PooledConnection) queue.poll(
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
                result = (PooledConnection)queue.take();
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
                "Thread "+ Thread.currentThread().getName() +
                 " got connection.");

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
                        + getConfiguration().getMaxConnections());

            if (totalConnections >= getConfiguration().getMaxConnections()
                && getConfiguration().getMaxConnections() != 0) {

                if (LOG_DEBUG_INFO && getLogger() != null)
                    getLogger().debug("Was not able to add more connections.");

                return false;
            }

            PooledConnection pooledConnection = owner.allocateConnection();

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

