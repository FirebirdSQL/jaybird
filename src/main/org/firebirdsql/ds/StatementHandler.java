// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jdbc.FirebirdStatement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.util.ReflectionHelper.*;

/**
 * InvocationHandler for statements.
 * <p>
 * Using an InvocationHandler together with a Proxy removes the need to create wrappers for every individual JDBC
 * version.
 * </p>
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
@NullUnmarked /* InvocationHandler and reflection helper make nullability a bit messy; we annotated what we could */
class StatementHandler implements InvocationHandler {

    private final @NonNull PooledConnectionHandler owner;
    @SuppressWarnings("java:S3077")
    private volatile @Nullable Statement stmt;
    @SuppressWarnings("java:S3077")
    private volatile @Nullable Statement proxy;

    /**
     * Constructor for StatementHandler.
     *
     * @param owner
     *         The PooledConnectionHandler which owns the Statement
     * @param stmt
     *         Statement to proxy
     */
    @NullMarked
    StatementHandler(PooledConnectionHandler owner, Statement stmt) {
        this.owner = requireNonNull(owner, "owner");
        this.stmt = requireNonNull(stmt, "stmt");

        proxy = (Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
                getAllInterfaces(stmt.getClass()), this);
    }

    public Object invoke(@NonNull Object proxy, @NonNull Method method, Object[] args) throws Throwable {
        // Methods from object
        if (method.equals(TO_STRING)) {
            return "Proxy for " + stmt;
        } else if (method.equals(EQUALS)) {
            // Using parameter proxy (and not field) on purpose as field is nulled after closing
            return proxy == args[0];
        } else if (method.equals(HASH_CODE)) {
            // Using parameter proxy (and not field) on purpose as field is nulled after closing
            return System.identityHashCode(proxy);
        } else if (method.getDeclaringClass().equals(Object.class)) {
            // Other methods from object
            try {
                return method.invoke(stmt, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if (method.equals(STATEMENT_IS_CLOSED) || method.equals(FIREBIRD_STATEMENT_IS_CLOSED)) {
            return isClosed();
        } else if (isClosed() && !method.equals(STATEMENT_CLOSE)) {
            throw FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_stmtClosed);
        }

        // Methods of statement and subinterfaces
        try {
            if (method.equals(STATEMENT_CLOSE)) {
                if (!isClosed()) {
                    handleClose();
                }
                return null;
            } else if (method.equals(GET_CONNECTION)) {
                // Ensure we do not leak the physical connection by returning the proxy
                return owner.getProxy();
            }
            // All other methods
            return method.invoke(stmt, args);
        } catch (InvocationTargetException ite) {
            Throwable inner = ite.getTargetException();
            if (inner instanceof SQLException se) {
                owner.statementErrorOccurred(this, se);
            }
            throw inner;
        } catch (SQLException se) {
            owner.statementErrorOccurred(this, se);
            throw se;
        }
    }

    /**
     * @return Proxy for the Statement object
     */
    protected Statement getProxy() throws SQLException {
        Statement proxy = this.proxy;
        if (proxy == null) throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_stmtClosed).toSQLException();
        return proxy;
    }

    /**
     * Handle {@link Statement#close()} method. This method closes the wrapped
     * Statement and notifies the owner it can forget the statement.
     *
     * @throws SQLException
     *         If closing the statement threw an Exception
     */
    private void handleClose() throws SQLException {
        if (isClosed()) {
            return;
        }
        try {
            Statement stmt = this.stmt;
            if (stmt != null) stmt.close();
        } finally {
            owner.forgetStatement(this);
            this.stmt = null;
            proxy = null;
        }
    }

    /**
     * Closes this StatementHandler
     */
    protected void close() throws SQLException {
        handleClose();
    }

    /**
     * @return {@code true} when this handler is closed
     */
    public boolean isClosed() {
        return proxy == null || stmt == null;
    }

    // Statement methods
    private static final Method STATEMENT_IS_CLOSED = findMethod(Statement.class, "isClosed", new Class[0]);
    private static final Method FIREBIRD_STATEMENT_IS_CLOSED = findMethod(FirebirdStatement.class, "isClosed",
            new Class[0]);
    private static final Method STATEMENT_CLOSE = findMethod(Statement.class, "close", new Class[0]);
    private static final Method GET_CONNECTION = findMethod(Statement.class, "getConnection", new Class[0]);

    // Object Methods
    private static final Method TO_STRING = findMethod(Object.class, "toString", new Class[0]);
    private static final Method EQUALS = findMethod(Object.class, "equals", new Class[] { Object.class });
    private static final Method HASH_CODE = findMethod(Object.class, "hashCode", new Class[0]);
}
