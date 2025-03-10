// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jdbc.FirebirdConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_logicalConnectionForciblyClosed;
import static org.firebirdsql.jaybird.util.ReflectionHelper.findMethod;
import static org.firebirdsql.jaybird.util.ReflectionHelper.getAllInterfaces;

/**
 * InvocationHandler for the logical connection returned by FBPooledConnection.
 * <p>
 * Using an InvocationHandler together with a Proxy removes the need to create rappers for every individual JDBC
 * version.
 * </p>
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
sealed class PooledConnectionHandler implements InvocationHandler permits XAConnectionHandler {

    private final FBPooledConnection owner;
    @SuppressWarnings("java:S3077")
    volatile Connection connection;
    @SuppressWarnings("java:S3077")
    private volatile Connection proxy;
    private volatile boolean forcedClose;

    private final List<StatementHandler> openStatements = new ArrayList<>();

    PooledConnectionHandler(Connection connection, FBPooledConnection owner) {
        this.connection = connection;
        this.owner = owner;
        proxy = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                getAllInterfaces(connection.getClass()), this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Methods from object
        if (method.equals(TO_STRING)) {
            return "Proxy for " + connection;
        } else if (method.equals(EQUALS)) {
            // Using parameter proxy (and not field) on purpose as field is nulled after closing
            return proxy == args[0];
        } else if (method.equals(HASH_CODE)) {
            // Using parameter proxy (and not field) on purpose as field is nulled after closing
            return System.identityHashCode(proxy);
        } else if (method.getDeclaringClass().equals(Object.class)) {
            // Other methods from object
            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if (method.equals(CONNECTION_IS_CLOSED)) {
            return isClosed();
        } else if (method.equals(RESET_KNOWN_CLIENT_INFO_PROPERTIES)) {
            try {
                method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if (isClosed() && !method.equals(CONNECTION_CLOSE)) {
            throw forcedClose
                    ? FbExceptionBuilder.toNonTransientConnectionException(jb_logicalConnectionForciblyClosed)
                    : FbExceptionBuilder.connectionClosed();
        }

        try {
            // Life cycle methods
            if (method.equals(CONNECTION_CLOSE)) {
                if (!isClosed()) {
                    handleClose(true);
                }
                return null;
            } else if (method.getDeclaringClass().equals(Connection.class)
                    && STATEMENT_CREATION_METHOD_NAMES.contains(method.getName())) {
                Statement pstmt = (Statement) method.invoke(connection, args);
                StatementHandler stmtHandler = new StatementHandler(this, pstmt);
                try (LockCloseable ignored = owner.withLock()) {
                    openStatements.add(stmtHandler);
                }
                return stmtHandler.getProxy();
            }
            // All other methods
            return method.invoke(connection, args);
        } catch (InvocationTargetException ite) {
            Throwable inner = ite.getTargetException();
            if (inner instanceof SQLException se) {
                owner.fireConnectionError(se);
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
     * @return {@code true} when calling rollback is allowed
     */
    boolean isRollbackAllowed() throws SQLException {
        return !connection.getAutoCommit();
    }

    /**
     * Handle {@link Connection#close()} method. This implementation closes the connection and associated statements.
     *
     * @param notifyOwner
     *         {@code true} when connection owner should be notified of closure.
     * @throws SQLException
     *         if underlying connection threw an exception.
     */
    void handleClose(boolean notifyOwner) throws SQLException {
        var chain = new SQLExceptionChainBuilder();
        try {
            closeStatements();
        } catch (SQLException ex) {
            chain.append(ex);
        }

        if (isRollbackAllowed()) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                chain.append(ex);
            }
        } else if (connection.getAutoCommit() && connection.isWrapperFor(FirebirdConnection.class)
                && connection.unwrap(FirebirdConnection.class).isUseFirebirdAutoCommit()) {
            // Force commit when in Firebird autocommit mode
            try {
                connection.setAutoCommit(false);
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                chain.append(ex);
            }
        }

        try {
            connection.clearWarnings();
        } catch (SQLException ex) {
            chain.append(ex);
        }

        proxy = null;
        connection = null;
        owner.releaseConnectionHandler(this);

        if (notifyOwner) {
            owner.fireConnectionClosed();
        }

        chain.throwIfPresent();
    }

    /**
     * @return Proxy for the Connection object
     */
    Connection getProxy() {
        return proxy;
    }

    /**
     * Closes this PooledConnectionHandler. Intended to be called by the ConnectionPoolDataSource when it wants to
     * forcibly close the logical connection to reuse it.
     */
    void close() throws SQLException {
        if (isClosed()) return;
        try {
            handleClose(false);
        } finally {
            forcedClose = true;
        }
    }

    boolean isClosed() {
        return connection == null || proxy == null;
    }

    @SuppressWarnings("UnusedParameters")
    void statementErrorOccurred(StatementHandler stmtHandler, SQLException sqle) {
        owner.fireConnectionError(sqle);
    }

    void forgetStatement(StatementHandler stmtHandler) {
        try (LockCloseable ignored = owner.withLock()) {
            openStatements.remove(stmtHandler);
        }
    }

    void closeStatements() throws SQLException {
        var chain = new SQLExceptionChainBuilder();
        try (LockCloseable ignored = owner.withLock()) {
            // Make copy as the StatementHandler close will remove itself from openStatements
            for (StatementHandler stmt : new ArrayList<>(openStatements)) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    chain.append(ex);
                } catch (RuntimeException e) {
                    chain.append(new SQLException("Runtime exception on statement close", e));
                }
            }
            openStatements.clear();
        }
        chain.throwIfPresent();
    }

    // FirebirdConnection methods
    private static final Method RESET_KNOWN_CLIENT_INFO_PROPERTIES =
            findMethod(FirebirdConnection.class, "resetKnownClientInfoProperties", new Class[0]);

    // Connection methods
    private static final Method CONNECTION_IS_CLOSED = findMethod(Connection.class, "isClosed", new Class[0]);
    private static final Method CONNECTION_CLOSE = findMethod(Connection.class, "close", new Class[0]);
    
    private static final Set<String> STATEMENT_CREATION_METHOD_NAMES =
            Set.of("createStatement", "prepareCall", "prepareStatement");

    // Object Methods
    private static final Method TO_STRING = findMethod(Object.class, "toString", new Class[0]);
    private static final Method EQUALS = findMethod(Object.class, "equals", new Class[] { Object.class });
    private static final Method HASH_CODE = findMethod(Object.class, "hashCode", new Class[0]);
}
