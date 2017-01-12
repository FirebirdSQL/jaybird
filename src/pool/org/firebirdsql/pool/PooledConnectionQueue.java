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

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of free connection queue.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@Deprecated
final class PooledConnectionQueue {

    private static final boolean LOG_DEBUG_INFO = PoolDebugConfiguration.LOG_DEBUG_INFO;
    private static final boolean SHOW_STACK_ON_BLOCK = PoolDebugConfiguration.SHOW_TRACE;
    private static final boolean SHOW_STACK_ON_ALLOCATION = PoolDebugConfiguration.SHOW_TRACE;

    private final PooledConnectionManager connectionManager;
    private final Logger logger;
    private final ConnectionPoolConfiguration configuration;

    private final Object key;

    private final String queueName;
    private final int blockingTimeout;
    private final Object addConnectionMutex = new Object();

    private IdleRemover idleRemover;

    private final BlockingQueue<PooledObject> idleConnections;
    private final Set<PooledObject> allConnections;
    private final Set<PooledObject> workingConnections;
    private final Set<PooledObject> workingConnectionsToClose;

    private final AtomicReference<QueueState> queueState = new AtomicReference<QueueState>(QueueState.NEW);

    /**
     * Create instance of this queue for the specified user name and password.
     *
     * @param connectionManager
     *         instance of {@link PooledConnectionManager}.
     * @param logger
     *         instance of {@link Logger}.
     * @param configuration
     *         instance of {@link ConnectionPoolConfiguration}.
     * @param queueName
     *         name of this queue.
     * @param key
     *         Key used for this instance
     */
    public PooledConnectionQueue(PooledConnectionManager connectionManager,
                                 Logger logger, ConnectionPoolConfiguration configuration,
                                 String queueName, Object key) {
        this.connectionManager = connectionManager;
        this.logger = logger;
        this.configuration = configuration;
        this.queueName = queueName;
        blockingTimeout = configuration.getBlockingTimeout();
        this.key = key;

        // NOTE: We are always creating an unbounded queue as the max pool size could change at runtime
        idleConnections = new LinkedBlockingQueue<PooledObject>();
        final int initialSize = Math.max(16, configuration.getMaxPoolSize());
        allConnections = Collections.synchronizedSet(new HashSet<PooledObject>(initialSize));
        workingConnections = Collections.synchronizedSet(new HashSet<PooledObject>(initialSize));
        workingConnectionsToClose = Collections.synchronizedSet(new HashSet<PooledObject>(initialSize));
    }

    /**
     * Get size of this queue. This method can be used to check how many free connections are left.
     *
     * @return size of this queue.
     */
    public int size() {
        return idleConnections.size();
    }

    /**
     * Get total number of physical connections opened to the database.
     *
     * @return total number of physical connections opened to the database.
     */
    public int totalSize() {
        return allConnections.size();
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
     * @throws SQLException
     *         if initial number of connections
     *         could not be created.
     */
    public void start() throws SQLException {
        if (!queueState.compareAndSet(QueueState.NEW, QueueState.STARTED)) return;

        for (int i = 0; i < configuration.getMinPoolSize(); i++) {
            addConnection();
        }

        idleRemover = new IdleRemover();
        Thread t = new Thread(idleRemover, "Pool " + queueName + " idleRemover");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Restart this queue.
     */
    public void restart() throws SQLException {
        if (!queueState.compareAndSet(QueueState.STARTED, QueueState.RESTARTING)) {
            // NOTE: Minor chance of a race condition here so it could potentially show that the state is STARTED
            final QueueState currentState = queueState.get();
            throw new SQLException("Queue " + queueName + " cannot be restarted, current state is: " + currentState);
        }
        try {
            // Move current idle connections to list for removal
            final List<PooledObject> connectionsToClose = new ArrayList<PooledObject>(size());
            idleConnections.drainTo(connectionsToClose);
            // flag working connections for deallocation when returned to the queue.
            workingConnectionsToClose.addAll(workingConnections);

            // close all free connections
            for (PooledObject connection : connectionsToClose) {
                try {
                    if (connection.isValid()) {
                        connection.deallocate();
                    }
                } catch (Exception ex) {
                    if (logger != null) {
                        logger.warn("Could not close connection.", ex);
                    }
                } finally {
                    physicalConnectionDeallocated(connection);
                }
            }
            //Create enough connections to restore the queue to MinPoolSize.
            while (totalSize() < configuration.getMinPoolSize()) {
                if (queueState.get() != QueueState.RESTARTING) {
                    break;
                }
                try {
                    addConnection();
                } catch (Exception e) {
                    if (logger != null) {
                        logger.warn("Could not add connection.", e);
                    }
                }
            }
        } finally {
            if (!queueState.compareAndSet(QueueState.RESTARTING, QueueState.STARTED)
                    && queueState.get() == QueueState.RETRY_SHUTDOWN) {
                // Shutdown of the queue was signalled while we were restarting, shutdown now
                shutdown();
            }
        }
    }

    /**
     * Shutdown this queue.
     */
    public void shutdown() throws SQLException {
        final QueueState previousState = queueState.getAndSet(QueueState.SHUTDOWN);
        switch (previousState) {
        case SHUTDOWN:
            return;
        case RESTARTING:
            if (queueState.compareAndSet(QueueState.SHUTDOWN, QueueState.RETRY_SHUTDOWN)) {
                return;
            } else if (!queueState.compareAndSet(QueueState.STARTED, QueueState.SHUTDOWN)) {
                // Restart has apparently completed, but is not in state STARTED
                throw new SQLException("Current queue state prevented shutdown. Please retry.");
            }
        }

        if (idleRemover != null)
            idleRemover.stop();
        idleRemover = null;

        /* Synchronizing on addConnectionMutex to ensure that no add or create operation in progress interferes.
         */
        synchronized (addConnectionMutex) {
            idleConnections.clear();
            workingConnections.clear();
            workingConnectionsToClose.clear();

            // close all current connections.
            for (PooledObject item : allConnections) {
                try {
                    if (item.isValid())
                        item.deallocate();
                } catch (Exception ex) {
                    if (logger != null) {
                        logger.warn("Could not deallocate connection.", ex);
                    }
                }
            }

            allConnections.clear();
        }
    }

    /**
     * Check if {@link #take()} method can keep blocking.
     *
     * @param startTime
     *         time when the method was entered.
     * @return <code>true</code> if method can keep blocking.
     */
    private boolean keepBlocking(long startTime) {
        return System.currentTimeMillis() - startTime < blockingTimeout;
    }

    /**
     * Destroy connection and restore the balance of connections in the pool.
     *
     * @param connection
     *         connection to destroy
     */
    public void destroyConnection(PooledObject connection) {
        try {
            connection.deallocate();
        } finally {
            physicalConnectionDeallocated(connection);
        }
    }

    /**
     * Notify queue that a physical connection was deallocated.
     *
     * @param connection
     *         connection that was deallocated.
     */
    public void physicalConnectionDeallocated(PooledObject connection) {
        allConnections.remove(connection);
        workingConnections.remove(connection);
    }

    /**
     * Put connection to this queue.
     *
     * @param connection
     *         free pooled connection.
     * @throws SQLException
     *         if connection cannot be added to this
     *         queue.
     */
    public void put(PooledObject connection) throws SQLException {
        final QueueState currentState = queueState.get();
        if (currentState == QueueState.NEW) {
            throw new SQLException("Queue has not been started yet");
        } else if (!currentState.isAllowPut()) {
            destroyConnection(connection);
            if (LOG_DEBUG_INFO && logger != null) {
                logger.debug("Thread " + Thread.currentThread().getName() + " released connection while pool was in state ." + currentState);
            }
            return;
        }
        if (configuration.isPooling()) {
            connection.setInPool(true);
            if (workingConnectionsToClose.remove(connection)) {
                destroyConnection(connection);
                addConnection();
            } else if (!idleConnections.offer(connection)) {
                // Maximum capacity reached
                destroyConnection(connection);
            } else {
                workingConnections.remove(connection);
            }
        } else {
            // deallocate connection if pooling is not enabled.
            destroyConnection(connection);
        }

        if (LOG_DEBUG_INFO && logger != null) {
            logger.debug("Thread " + Thread.currentThread().getName() + " released connection.");
        }
    }

    /**
     * Take pooled connection from this queue. This method will block
     * until free and valid connection is available.
     *
     * @return free instance of {@link FBPooledConnection}.
     * @throws SQLException
     *         if no free connection was available and
     *         waiting thread was interruped while waiting for a new free
     *         connection.
     */
    public PooledObject take() throws SQLException {
        final QueueState currentState = queueState.get();
        if (!currentState.isAllowTake()) {
            throw new SQLException("Current queue state " + currentState + " does not allow take() on connection queue");
        }
        final long startTime = System.currentTimeMillis();
        if (LOG_DEBUG_INFO && logger != null) {
            logger.debug("Thread " + Thread.currentThread().getName() + " wants to take connection.");
        }

        final SQLExceptionChainBuilder pendingExceptions = new SQLExceptionChainBuilder();

        PooledObject result = null;
        try {
            do {
                if (idleConnections.isEmpty()) {
                    // Queue empty, Attempt to create a new connection directly
                    try {
                        result = createPooledConnection();
                    } catch (SQLException sqlex) {
                        if (logger != null) {
                            logger.warn("Could not create connection." + sqlex.getMessage());
                        }
                        if (!pendingExceptions.hasException()) {
                            pendingExceptions.append(sqlex);
                        } else if (pendingExceptions.getException().getErrorCode() != sqlex.getErrorCode()) {
                            pendingExceptions.append(sqlex);
                        }
                    }
                } // intentionally no else here!
                if (result == null) {
                    // Try to obtain connection from pool
                    result = idleConnections.poll(configuration.getRetryInterval(), TimeUnit.MILLISECONDS);
                }
                if (result != null) {
                    // Retrieved from pool
                    if (logger != null) {
                        logger.info("Obtained connection. Thread " + Thread.currentThread().getName());
                    }
                    result.setInPool(false);
                    workingConnections.add(result);
                    break;
                }

                if (!keepBlocking(startTime)) {
                    final String message = "Could not obtain connection during blocking timeout (" + blockingTimeout + " ms)";
                    FBSQLException ex = new FBSQLException(message, FBSQLException.SQL_STATE_CONNECTION_FAILURE);
                    if (pendingExceptions.hasException()) {
                        ex.setNextException(pendingExceptions.getException());
                    }
                    throw ex;
                }

                String message = "Pool " + queueName + " is empty and will block here. Thread " + Thread.currentThread().getName();
                if (logger != null) {
                    if (SHOW_STACK_ON_BLOCK) {
                        logger.warn(message, new Exception());
                    } else {
                        logger.warn(message);
                    }
                }
            } while (true);
        } catch (InterruptedException iex) {
            throw new SQLException("No free connection was available and waiting thread was interrupted.");
        }

        if (LOG_DEBUG_INFO && logger != null) {
            final String message = "Thread " + Thread.currentThread().getName() + " got connection.";
            if (SHOW_STACK_ON_ALLOCATION) {
                logger.debug(message, new Exception());
            } else {
                logger.debug(message);
            }
        }
        return result;
    }

    /**
     * Opens new connection to database and adds it to the pool.
     *
     * @return <code>true</code> if new physical connection was created,
     * otherwise false.
     * @throws SQLException
     *         if new connection cannot be opened.
     */
    private boolean addConnection() throws SQLException {
        if (LOG_DEBUG_INFO && logger != null) {
            logger.debug("Trying to create connection, total connections " + allConnections.size()
                    + ", max allowed " + configuration.getMaxPoolSize());
        }
        if (idleConnections.remainingCapacity() == 0) {
            if (LOG_DEBUG_INFO && logger != null) {
                logger.debug("Unable to add more connections, maximum capacity reached.");
            }
            return false;
        }

        synchronized (addConnectionMutex) {
            if (!queueState.get().isAllowAdd()) return false;

            PooledObject pooledConnection = createPooledConnection();
            if (pooledConnection == null) {
                if (LOG_DEBUG_INFO && logger != null) {
                    logger.debug("Unable to add more connections, maximum capacity reached.");
                }
                return false;
            }

            if (LOG_DEBUG_INFO && logger != null) {
                logger.debug("Thread " + Thread.currentThread().getName() + " created connection.");
            }

            if (idleConnections.offer(pooledConnection)) {
                return true;
            }

            /* We cannot add the connection to the pool, we will destroy it again
             * NOTE: In the current implementation idleConnections is unbounded so we should never get here
             */
            destroyConnection(pooledConnection);
            if (LOG_DEBUG_INFO && logger != null) {
                logger.debug("Thread " + Thread.currentThread().getName() + " forced to abandon created connection, capacity reached.");
            }
            return false;
        }
    }

    /**
     * Creates a new pooledConnection without adding it to the idleConnections queue.
     *
     * @return A new PooledConnection or null when the maximum pool capacity was already reached or allocating new connections is currently not allowed.
     * @throws SQLException
     *         For errors allocating the exception.
     */
    private PooledObject createPooledConnection() throws SQLException {
        synchronized (addConnectionMutex) {
            if (!queueState.get().isAllowAdd()) return null;

            boolean maximumCapacityReached =
                    configuration.isPooling() &&
                    configuration.getMaxPoolSize() != 0 &&
                    configuration.getMaxPoolSize() <= totalSize();

            if (maximumCapacityReached) {
                if (LOG_DEBUG_INFO && logger != null) {
                    logger.debug("Unable to add more connections, maximum capacity reached.");
                }
                return null;
            }

            PooledObject pooledConnection = connectionManager.allocateConnection(key, this);
            allConnections.add(pooledConnection);

            if (LOG_DEBUG_INFO && logger != null) {
                logger.debug("Thread " + Thread.currentThread().getName() + " created connection.");
            }

            return pooledConnection;
        }
    }

    /**
     * Release first connection in the queue if it was idle longer than idle
     * timeout interval.
     *
     * @return <code>true</code> if method removed idle connection, otherwise
     * <code>false</code>
     * @throws SQLException
     *         if exception happened when releasing the connection.
     */
    private boolean releaseNextIdleConnection() throws SQLException {
        if (totalSize() <= configuration.getMinPoolSize())
            return false;

        /* Initial check without removing object from idleConnections queue.
         * This is to prevent too much unnecessary removal of items from head and adding them to to the back
         * of the queue which might lead to too many idle connections older than maxIdleTime at the end
         * of the queue while younger items exist at the head of the queue.
         */
        final PooledObject initialCandidate = idleConnections.peek();
        if (initialCandidate == null
                || initialCandidate.getInstantInPool() >= System.currentTimeMillis() - configuration.getMaxIdleTime()) {
            // No candidate or initial candidate is too recent, abandon further checks
            return false;
        }

        // We temporarily remove the connection from the pool to make further checks
        // We repeat all checks as in the meantime the previous (peeked) candidate may have been obtained from the pool
        final PooledObject candidate = idleConnections.poll();
        if (candidate == null) {
            return false;
        }

        final long lastUsageTime = candidate.getInstantInPool();
        if (lastUsageTime == PooledObject.INSTANT_IN_USE && idleConnections.offer(candidate)) {
            return false;
        } else {
            final long idleTime = System.currentTimeMillis() - lastUsageTime;
            if (idleTime < configuration.getMaxIdleTime() && idleConnections.offer(candidate)) {
                return false;
            }
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(String.format("Going to remove connection with idleTime %d (max is %d)%n", idleTime, configuration.getMaxIdleTime()));
            }
        }

        destroyConnection(candidate);
        if (totalSize() < configuration.getMinPoolSize()) {
            // We assume our current action caused the pool to shrink below the minimum size, so we add one
            addConnection();
        }
        return true;
    }

    /**
     * Implementation of {@link Runnable} interface responsible for removing
     * idle connections.
     */
    private class IdleRemover implements Runnable {

        private volatile boolean running;

        public IdleRemover() {
            running = true;
        }

        public void stop() {
            running = false;
        }

        public void run() {
            while (running) {
                try {
                    int releasedInIteration = 0;
                    while (releaseNextIdleConnection()) {
                        releasedInIteration++;
                    }
                    if (logger != null && releasedInIteration > 0) {
                        logger.trace("IdleRemover for " + queueName + " released " + releasedInIteration + " connections");
                    }
                } catch (SQLException ex) {
                    // do nothing, we hardly can handle this situation
                }

                try {
                    final int idleTimeout = configuration.getMaxIdleTime();
                    Thread.sleep(Math.max(500, idleTimeout / 4));
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    }

    private enum QueueState {
        NEW(false, false, false),
        STARTED(true, true, true),
        RESTARTING(true, true, true),
        RETRY_SHUTDOWN(false, false, false),
        SHUTDOWN(false, false, false);

        private final boolean allowPut;
        private final boolean allowTake;
        private final boolean allowAdd;

        private QueueState(boolean allowPut, boolean allowTake, boolean allowAdd) {

            this.allowPut = allowPut;
            this.allowTake = allowTake;
            this.allowAdd = allowAdd;
        }

        public boolean isAllowPut() {
            return allowPut;
        }

        public boolean isAllowTake() {
            return allowTake;
        }

        public boolean isAllowAdd() {
            return allowAdd;
        }
    }
}

