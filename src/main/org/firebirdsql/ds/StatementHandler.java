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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jdbc.FirebirdStatement;

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
class StatementHandler implements InvocationHandler {

    private final PooledConnectionHandler owner;
    @SuppressWarnings("java:S3077")
    private volatile Statement stmt;
    @SuppressWarnings("java:S3077")
    private volatile Statement proxy;

    /**
     * Constructor for StatementHandler.
     *
     * @param owner
     *         The PooledConnectionHandler which owns the Statement
     * @param stmt
     *         Statement to proxy
     */
    StatementHandler(PooledConnectionHandler owner, Statement stmt) {
        this.owner = owner;
        this.stmt = stmt;

        proxy = (Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
                getAllInterfaces(stmt.getClass()), this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_stmtClosed).toSQLException();
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
    protected Statement getProxy() {
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
            stmt.close();
        } finally {
            owner.forgetStatement(this);
            stmt = null;
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
