/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;

/**
 * PooledConnection implementation for {@link FBConnectionPoolDataSource}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBPooledConnection implements PooledConnection {

    private final List<ConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();

    private Connection connection;
    private PooledConnectionHandler handler;

    protected FBPooledConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null) {
            FBSQLException ex = new FBSQLException("The PooledConnection has been closed",
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
    public synchronized void close() throws SQLException {
    	SQLException receivedException = null;
        if (handler != null) {
            try {
                handler.close();
            } catch (SQLException se) {
            	receivedException = se;
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException se) {
            	// We want the exception of closing the physical connection to be the first
            	if (receivedException != null) {
                    se.setNextException(receivedException);
                }
                receivedException = se;
            } finally {
                connection = null;
            }
        }
        if (receivedException != null) {
            throw receivedException;
        }
    }

    /**
     * Helper method to fire the connectionErrorOccurred event. To be used with
     * fatal (connection) errors only.
     * 
     * @param ex
     *            The exception
     */
    protected void fireFatalConnectionError(SQLException ex) {
        ConnectionEvent evt = new ConnectionEvent(this, ex);
        for (ConnectionEventListener listener : connectionEventListeners) {
            listener.connectionErrorOccurred(evt);
        }
    }

    /**
     * Helper method to fire the connectionErrorOccurred event.
     * <p>
     * This method will decide which errors warrant a connectionErrorOccurred
     * event to be reported or not.
     * </p>
     * 
     * @param ex
     *            The exception
     */
    protected void fireConnectionError(SQLException ex) {
        SQLException currentException = ex;
        while (currentException != null) {
            String sqlState = currentException.getSQLState();
            if (isFatalState(sqlState)) {
                fireFatalConnectionError(ex);
            }
            currentException = ex.getNextException();
        }
    }

    /**
     * Decides if the given SQL state is a fatal connection error.
     * 
     * @param sqlState
     *            SQL State value
     * @return <code>true</code> if the SQL state is considered fatal
     */
    private boolean isFatalState(String sqlState) {
        if (sqlState == null || sqlState.length() < 2) {
            // No SQL State or no class specified, assume it's fatal
            return true;
        }
        for (String fatalSqlStateClass : FATAL_SQL_STATE_CLASSES) {
            if (sqlState.startsWith(fatalSqlStateClass)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] FATAL_SQL_STATE_CLASSES = {
            // TODO double check firebird and Jaybird implementation for other states
            "08", // Connection errors
            "XX", // Internal errors
            "01002", // Disconnect error
            "01S00", // Invalid connection string attribute
            "2D000", // Invalid transaction termination
            "2E000", // Invalid connection name
            "HY000", // General error (TODO: maybe too general?)
            "HY001", // Memory allocation error
            "HYT00", // Timeout expired
            "HYT01", // Connection timeout expired
    };

    /**
     * Helper method to fire the connectionClosed event.
     */
    protected void fireConnectionClosed() {
        ConnectionEvent evt = new ConnectionEvent(this);
        for (ConnectionEventListener listener : connectionEventListeners) {
            listener.connectionClosed(evt);
        }
    }
    
    /**
     * Releases the current handler if it is equal to the handler passed in <code>pch</code>.
     * <p>
     * To be called by the PooledConnectionHandler when it has been closed.
     * </p>
     * 
     * @param pch PooledConnectionHandler to release.
     */
    protected synchronized void releaseConnectionHandler(PooledConnectionHandler pch) {
        if (handler == pch) {
            handler = null;
        }
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    public void addStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void removeStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
