// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jaybird.xca.FatalErrorHelper;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * PooledConnection implementation for {@link FBConnectionPoolDataSource}.
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
sealed class FBPooledConnection implements PooledConnection permits FBXAConnection {

    private final List<ConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();

    private final Lock lock = new ReentrantLock();
    private final LockCloseable unlock = lock::unlock;
    private @Nullable Connection connection;
    private @Nullable PooledConnectionHandler handler;

    FBPooledConnection(Connection connection) {
        this.connection = requireNonNull(connection, "connection");
    }

    LockCloseable withLock() {
        lock.lock();
        return unlock;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try (var ignored = withLock()) {
            try {
                Connection connection = requireConnection();
                if (handler != null) {
                    handler.close();
                }
                resetConnection(connection);
                PooledConnectionHandler handler = this.handler = createConnectionHandler(connection);
                return handler.getProxy();
            } catch (SQLException ex) {
                fireFatalConnectionError(ex);
                throw ex;
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = this.connection;
        if (connection != null) return connection;
        throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_pooledConnectionClosed);
    }

    void resetConnection(Connection connection) throws SQLException {
        connection.setAutoCommit(true);
        if (connection.isWrapperFor(FirebirdConnection.class)) {
            connection.unwrap(FirebirdConnection.class)
                    .resetKnownClientInfoProperties();
        }
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
    PooledConnectionHandler createConnectionHandler(Connection connection) {
        return new PooledConnectionHandler(connection, this);
    }

    @Override
    public void close() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            var chain = new SQLExceptionChainBuilder();
            PooledConnectionHandler handler = this.handler;
            if (handler != null) {
                try {
                    handler.close();
                } catch (SQLException se) {
                    chain.append(se);
                } finally {
                    this.handler = null;
                }
            }
            Connection connection = this.connection;
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se) {
                    // We want the exception from closing the physical connection to be the first
                    chain.addFirst(se);
                } finally {
                    this.connection = null;
                }
            }
            chain.throwIfPresent();
        }
    }

    /**
     * Helper method to fire the connectionErrorOccurred event. To be used with fatal (connection) errors only.
     *
     * @param ex
     *         The exception
     */
    void fireFatalConnectionError(SQLException ex) {
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
    void fireConnectionError(SQLException ex) {
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
    void fireConnectionClosed() {
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
    void releaseConnectionHandler(PooledConnectionHandler pch) {
        try (LockCloseable ignored = withLock()) {
            if (handler == pch) {
                handler = null;
            }
        }
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation does nothing.
     * </p>
     */
    @Override
    public void addStatementEventListener(StatementEventListener listener) {
        // TODO Implement statement events
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation does nothing.
     * </p>
     */
    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
        // TODO Implement statement events
    }
    
}
