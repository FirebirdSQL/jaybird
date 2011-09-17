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

import static org.firebirdsql.ds.ReflectionHelper.findMethod;
import static org.firebirdsql.ds.ReflectionHelper.getAllInterfaces;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.firebirdsql.jdbc.FBSQLException;

/**
 * InvocationHandler for the logical connection returned by FBPooledConnection.
 * <p>
 * Using an InvocationHandler together with a Proxy removes the need to create
 * wrappers for every individual JDBC version.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
class PooledConnectionHandler implements InvocationHandler {

    protected static final String CLOSED_MESSAGE = "Logical connection already closed";
    protected static final String FORCIBLY_CLOSED_MESSAGE = "Logical connection was forcibly closed by the connection pool";
    protected final AbstractPooledConnection owner;
    protected volatile Connection connection;
    protected volatile Connection proxy;
    protected volatile boolean forcedClose;

    private final List openStatements = Collections.synchronizedList(new LinkedList());

    protected PooledConnectionHandler(Connection connection, AbstractPooledConnection owner) {
        this.connection = connection;
        this.owner = owner;
        proxy = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                getAllInterfaces(connection.getClass()), this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Methods from object
        if (method.equals(TO_STRING)) {
            return "Proxy for " + connection;
        }
        if (method.equals(EQUALS)) {
            // Using parameter proxy (and not field) on purpose as field is
            // nulled after closing
            return Boolean.valueOf(proxy == args[0]);
        }
        if (method.equals(HASH_CODE)) {
            // Using parameter proxy (and not field) on purpose as field is
            // nulled after closing
            return Integer.valueOf(System.identityHashCode(proxy));
        }
        // Other methods from object
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        // Methods from Connection
        if (method.equals(CONNECTION_IS_CLOSED)) {
            return Boolean.valueOf(isClosed());
        }
        if (isClosed() && !method.equals(CONNECTION_CLOSE)) {
            String message = forcedClose ? FORCIBLY_CLOSED_MESSAGE : CLOSED_MESSAGE;
            throw new FBSQLException(message, FBSQLException.SQL_STATE_CONNECTION_CLOSED);
        }

        try {
            // Life cycle methods
            if (method.equals(CONNECTION_CLOSE)) {
                if (isClosed()) {
                    return null;
                }
                handleClose(true);
                return null;
            }

            if (method.getDeclaringClass().equals(Connection.class)
                    && STATEMENT_CREATION_METHOD_NAMES.contains(method.getName())) {
                Statement pstmt = (Statement) method.invoke(connection, args);
                StatementHandler stmtHandler = new StatementHandler(this, pstmt);
                openStatements.add(stmtHandler);
                return stmtHandler.getProxy();
            }

            // All other methods
            return method.invoke(connection, args);
        } catch (InvocationTargetException ite) {
            Throwable inner = ite.getTargetException();
            if (inner instanceof SQLException) {
                owner.fireConnectionError((SQLException) inner);
            }
            throw inner;
        } catch (SQLException se) {
            owner.fireConnectionError(se);
            throw se;
        }
    }
    
    /**
     * Method to decide if calling rollback on the physical connection for cleanup (in handleClose()) is allowed.
     * <p>
     * NOTE: This method is not involved in rollback decisions for calls to the proxy.
     * </p>
     * 
     * @return <code>true</code> when calling rollback is allowed
     */
    protected boolean isRollbackAllowed() throws SQLException {
        return !connection.getAutoCommit();
    }

    /**
     * Handle {@link Connection#close()} method. This implementation closes the
     * connection and associated statements.
     * 
     * @param notifyOwner
     *            <code>true</code> when connection owner should be notified of
     *            closure.
     * 
     * @throws SQLException
     *             if underlying connection threw an exception.
     */
    protected void handleClose(boolean notifyOwner) throws SQLException {
        SQLException sqle = null;
        try {
            closeStatements();
        } catch (SQLException ex) {
            sqle = ex;
        }
        if (isRollbackAllowed()) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                if (sqle != null) {
                    sqle.setNextException(ex);
                } else {
                    sqle = ex;
                }
            }
        }
        try {
            connection.clearWarnings();
        } catch (SQLException ex) {
            if (sqle != null) {
                sqle.setNextException(ex);
            } else {
                sqle = ex;
            }
        }
        proxy = null;
        connection = null;
        owner.releaseConnectionHandler(this);
        if (notifyOwner) {
            owner.fireConnectionClosed();
        }
        if (sqle != null) {
            throw sqle;
        }
    }

    /**
     * @return Proxy for the Connection object
     */
    protected Connection getProxy() {
        return proxy;
    }

    /**
     * Closes this PooledConnectionHandler. Intended to be called by the
     * ConnectionPoolDataSource when it wants to forcibly close the logical
     * connection to reuse it.
     * 
     * @throws SQLException
     */
    protected void close() throws SQLException {
        if (!isClosed()) {
            try {
                handleClose(false);
            } finally {
                forcedClose = true;
            }
        }
    }

    protected boolean isClosed() {
        return connection == null || proxy == null;
    }

    protected void statementErrorOccurred(StatementHandler stmtHandler, SQLException sqle) {
        // TODO: Log?, forward fatal connection related errors?
        owner.fireConnectionError(sqle);
    }

    protected void forgetStatement(StatementHandler stmtHandler) {
        openStatements.remove(stmtHandler);
    }

    protected void closeStatements() throws SQLException {
        SQLException sqle = null;
        synchronized (openStatements) {
            Iterator iter = openStatements.iterator();
            while (iter.hasNext()) {
                StatementHandler stmt = (StatementHandler) iter.next();
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    if (sqle != null) {
                        sqle.setNextException(ex);
                    } else {
                        sqle = ex;
                    }
                    // TODO : Log, ignore, something else?
                } catch (Throwable t) {
                    // ignore?
                } finally {
                    iter.remove();
                }
            }
        }
        if (sqle != null) {
            throw sqle;
        }
    }

    // Connection methods
    private final static Method CONNECTION_IS_CLOSED = findMethod(Connection.class, "isClosed",
            new Class[0]);
    private final static Method CONNECTION_CLOSE = findMethod(Connection.class, "close",
            new Class[0]);
    
    private static final Set STATEMENT_CREATION_METHOD_NAMES;
    static {
        Set temp = new HashSet();
        temp.add("createStatement");
        temp.add("prepareCall");
        temp.add("prepareStatement");
        STATEMENT_CREATION_METHOD_NAMES = Collections.unmodifiableSet(temp);
    }

    // Object Methods
    private final static Method TO_STRING = findMethod(Object.class, "toString", new Class[0]);
    private final static Method EQUALS = findMethod(Object.class, "equals",
            new Class[] { Object.class });
    private final static Method HASH_CODE = findMethod(Object.class, "hashCode", new Class[0]);
}
