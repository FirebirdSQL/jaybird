/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.xca.FatalErrorHelper;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.util.SQLExceptionChainBuilder;

/**
 * PooledConnection implementation for {@link FBConnectionPoolDataSource}
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
public class FBPooledConnection implements PooledConnection {

    private final List<ConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();

    private final Lock lock = new ReentrantLock();
    private final LockCloseable unlock = lock::unlock;
    private Connection connection;
    private PooledConnectionHandler handler;

    protected FBPooledConnection(Connection connection) {
        this.connection = connection;
    }

    protected LockCloseable withLock() {
        lock.lock();
        return unlock;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (connection == null) {
                var ex = new SQLNonTransientConnectionException("The PooledConnection has been closed",
                        SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
                fireFatalConnectionError(ex);
                throw ex;
            }
            try {
                if (handler != null) {
                    handler.close();
                }
                resetConnection(connection);
            } catch (SQLException ex) {
                fireFatalConnectionError(ex);
                throw ex;
            }
            handler = createConnectionHandler(connection);

            return handler.getProxy();
        }
    }

    protected void resetConnection(Connection connection) throws SQLException {
        connection.setAutoCommit(true);
    }

    /**
     * Creates the PooledConnectionHandler for the connection.
     * <p>
     * Subclasses may override this method to return their own subclass of PooledConnectionHandler.
     * </p>
     *
     * @param connection Connection
     * @return PooledConnectionHandler
     */
    protected PooledConnectionHandler createConnectionHandler(Connection connection) {
        return new PooledConnectionHandler(connection, this);
    }

    @Override
    public void close() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            var chain = new SQLExceptionChainBuilder<>();
            if (handler != null) {
                try {
                    handler.close();
                } catch (SQLException se) {
                    chain.append(se);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se) {
                    // We want the exception from closing the physical connection to be the first
                    chain.addFirst(se);
                } finally {
                    connection = null;
                }
            }
            if (chain.hasException()) {
                throw chain.getException();
            }
        }
    }

    /**
     * Helper method to fire the connectionErrorOccurred event. To be used with fatal (connection) errors only.
     *
     * @param ex
     *         The exception
     */
    protected void fireFatalConnectionError(SQLException ex) {
        var evt = new ConnectionEvent(this, ex);
        for (ConnectionEventListener listener : connectionEventListeners) {
            listener.connectionErrorOccurred(evt);
        }
    }

    /**
     * Helper method to fire the connectionErrorOccurred event.
     * <p>
     * This method will decide which errors warrant a connectionErrorOccurred event to be reported or not.
     * </p>
     *
     * @param ex
     *         The exception
     */
    protected void fireConnectionError(SQLException ex) {
        SQLException currentException = ex;
        while (currentException != null) {
            if (FatalErrorHelper.isFatal(currentException)) {
                fireFatalConnectionError(ex);
                return;
            }
            currentException = ex.getNextException();
        }
    }

    /**
     * Helper method to fire the connectionClosed event.
     */
    protected void fireConnectionClosed() {
        var evt = new ConnectionEvent(this);
        for (ConnectionEventListener listener : connectionEventListeners) {
            listener.connectionClosed(evt);
        }
    }

    /**
     * Releases the current handler if it is equal to the handler passed in {@code pch}.
     * <p>
     * To be called by the PooledConnectionHandler when it has been closed.
     * </p>
     *
     * @param pch
     *         PooledConnectionHandler to release.
     */
    protected void releaseConnectionHandler(PooledConnectionHandler pch) {
        try (LockCloseable ignored = withLock()) {
            if (handler == pch) {
                handler = null;
            }
        }
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation does nothing.
     * </p>
     */
    public void addStatementEventListener(StatementEventListener listener) {
        // TODO Implement statement events
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation does nothing.
     * </p>
     */
    public void removeStatementEventListener(StatementEventListener listener) {
        // TODO Implement statement events
    }
}
