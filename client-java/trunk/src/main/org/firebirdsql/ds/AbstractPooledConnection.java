/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

import org.firebirdsql.jdbc.FBSQLException;

/**
 * PooledConnection implementation for {@link FBConnectionPoolDataSource}
 * <p>
 * This class is abstract to account for both a JDBC 3.0 and JDBC 4.0 compliant implementation.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public abstract class AbstractPooledConnection implements PooledConnection {

    private final List connectionEventListeners = Collections.synchronizedList(new LinkedList());

    protected Connection connection;
    protected volatile PooledConnectionHandler handler;

    protected AbstractPooledConnection(Connection connection) {
        this.connection = connection;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null) {
            FBSQLException ex = new FBSQLException("The PooledConnection has been closed",
                    FBSQLException.SQL_STATE_CONNECTION_CLOSED);
            fireFatalConnectionError(ex);
            throw ex;
        }
        try {
            if (handler != null) {
                handler.close();
            }
            // TODO Verify if this is correct behavior, or if it needs to be configurable;
            // TODO 2: may need to handle this in separate overridable method in light of FBXAConnection
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            fireFatalConnectionError(ex);
            throw ex;
        }
        handler = createConnectionHandler();

        return handler.getProxy();
    }

    /**
     * Creates the PooledConnectionHandler for the connection.
     * <p>
     * Subclasses may override this method to return their own subclass of PooledConnectionHandler.
     * </p>
     * 
     * @return PooledConnectionHandler
     */
    protected PooledConnectionHandler createConnectionHandler() {
        return new PooledConnectionHandler(connection, this);
    }

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
        // Make a copy to prevent errors when listeners remove themselves
        List listeners = new ArrayList(connectionEventListeners);
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ConnectionEventListener listener = (ConnectionEventListener) iter.next();
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
        // TODO Do we need to walk over Exception chain to check if it wraps a fatal SQLException?
        String sqlState = ex.getSQLState();
        if (isFatalState(sqlState)) {
            fireFatalConnectionError(ex);
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
        for (int idx = 0; idx < FATAL_SQL_STATE_CLASSES.length; idx++) {
            if (sqlState.startsWith(FATAL_SQL_STATE_CLASSES[idx])) {
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
        // Make a copy to prevent errors when listeners remove themselves
        List listeners = new ArrayList(connectionEventListeners);
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ConnectionEventListener listener = (ConnectionEventListener) iter.next();
            listener.connectionClosed(evt);
        }
    }
    
    /**
     * Releases the current handler if it is equal to <code>pch</code>.
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
}
