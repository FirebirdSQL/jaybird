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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdStatement;
import org.firebirdsql.jdbc.SQLStateConstants;

import static org.firebirdsql.util.ReflectionHelper.*;

/**
 * InvocationHandler for statements.
 * <p>
 * Using an InvocationHandler together with a Proxy removes the need to create
 * wrappers for every individual JDBC version.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
class StatementHandler implements InvocationHandler {

    private final PooledConnectionHandler owner;
    private volatile Statement stmt;
    private volatile Statement proxy;

    /**
     * Constructor for StatementHandler.
     * 
     * @param owner
     *            The PooledConnectionHandler which owns the Statement
     * @param stmt
     *            Statement to proxy
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
        }
        if (method.equals(EQUALS)) {
            // Using parameter proxy (and not field) on purpose as field is
            // nulled after closing
            return proxy == args[0];
        }
        if (method.equals(HASH_CODE)) {
            // Using parameter proxy (and not field) on purpose as field is
            // nulled after closing
            return System.identityHashCode(proxy);
        }
        // Other methods from object
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                return method.invoke(stmt, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        // Methods of statement and subinterfaces
        if (method.equals(STATEMENT_IS_CLOSED) || method.equals(FIREBIRDSTATEMENT_IS_CLOSED)) {
            return isClosed();
        }
        if (isClosed() && !method.equals(STATEMENT_CLOSE)) {
            throw new FBSQLException("Statement already closed", SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID);
        }
        
        try {
            if (method.equals(STATEMENT_CLOSE)) {
                if (!isClosed()) {
                    handleClose();
                }
                return null;
            }
            if (method.equals(GET_CONNECTION)) {
                // Ensure we do not leak the physical connection by returning the proxy
                return owner.getProxy();
            }
            // All other methods
            return method.invoke(stmt, args);
        } catch (InvocationTargetException ite) {
            Throwable inner = ite.getTargetException();
            if (inner instanceof SQLException) {
                owner.statementErrorOccurred(this, (SQLException) inner);
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
     *             If closing the statement threw an Exception
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
     * 
     * @throws SQLException
     */
    protected void close() throws SQLException {
        handleClose();
    }

    /**
     * @return <code>true</code> when this handler is closed
     */
    public boolean isClosed() {
        return proxy == null || stmt == null;
    }

    // Statement methods
    private final static Method STATEMENT_IS_CLOSED = findMethod(Statement.class, "isClosed",
            new Class[0]);
    private final static Method FIREBIRDSTATEMENT_IS_CLOSED = findMethod(FirebirdStatement.class, "isClosed", 
            new Class[0]);
    private final static Method STATEMENT_CLOSE = findMethod(Statement.class, "close", new Class[0]);
    private final static Method GET_CONNECTION = findMethod(Statement.class, "getConnection", new Class[0]);

    // Object Methods
    private final static Method TO_STRING = findMethod(Object.class, "toString", new Class[0]);
    private final static Method EQUALS = findMethod(Object.class, "equals",
            new Class[] { Object.class });
    private final static Method HASH_CODE = findMethod(Object.class, "hashCode", new Class[0]);
}
