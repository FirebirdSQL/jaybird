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

import java.lang.reflect.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Wrapper for prepared statements. This invocation handler notifies statement
 * manager about closing prepared statement.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class PooledPreparedStatementHandler implements InvocationHandler {
    
    /**
     * Flag whether check is performed in tolerant mode or not. In tolerant 
     * mode {@link SQLException} is thrown, otherwise 
     * {@link IllegalStateException}.
     */
    private static final boolean TOLERANT_CHECK_MODE = false;
    
    public static Method findMethod(Class clazz, String name, Class[] params) {
        return PooledConnectionHandler.findMethod(clazz, name, params);
    }
    
    private final static Method PREPARED_STATEMENT_CLOSE = findMethod(
        PreparedStatement.class, "close", new Class[0]);
        
    private final static Method PREPARED_STATEMENT_GET_CONNECTION = findMethod(
        PreparedStatement.class, "getConnection", new Class[0]);
        
    private final static Method PREPARED_STATEMENT_FORCE_CLOSE = findMethod(
        XCachablePreparedStatement.class, "forceClose", new Class[0]);

    private final static Method PREPARED_STATEMENT_SET_CONNECTION = findMethod(
        XCachablePreparedStatement.class, "setConnection", new Class[] {
            Connection.class});

    private final static Method PREPARED_STATEMENT_GET_ORIGINAL = findMethod(
        XCachablePreparedStatement.class, "getOriginal", new Class[0]);
        
    private final static Method PREPARED_STATEMENT_EXECUTE_QUERY_1 = findMethod(
        PreparedStatement.class, "executeQuery", new Class[0]);

    private final static Method PREPARED_STATEMENT_EXECUTE_QUERY_2 = findMethod(
        PreparedStatement.class, "executeQuery", new Class[]{String.class});
        
    private final static Method PREPARED_STATEMENT_GET_RESULT_SET = findMethod(
        PreparedStatement.class, "getResultSet", new Class[0]);
    
    private String statement;
    private PreparedStatement preparedStatement;
    private XStatementManager owner;
    private Connection associatedConnection;
    
    private boolean invalid;
    private String invalidateStackTrace = "";
        
    /**
     * Create instance of this class.
     * 
     * @param statement SQL statement to which prepared statement corresponds
     * to.
     * 
     * @param preparedStatement prepared statement corresponding to statement.
     * 
     * @param owner instance of {@link XConnection} that created prepared
     * statement.
     */
    PooledPreparedStatementHandler(String statement, PreparedStatement preparedStatement, 
        XStatementManager owner) 
    {
        this.statement = statement;
        this.preparedStatement = preparedStatement;
        this.owner = owner;
        this.invalid = false;
    }
    
    /**
     * Handle {@link Statement#close()} call.
     * 
     * @param statement SQL statement corresponding to the wrapped prepared 
     * statement.
     * 
     * @param proxy dynamic proxy wrapping prepared statement.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void handleStatementClose(String statement, Object proxy) 
        throws SQLException 
    {
        if (invalid)
            throw new SQLException("Statement is already closed.");
            
        owner.statementClosed(statement, proxy);
    }
    
    protected void handleForceClose() throws SQLException {
        preparedStatement.close();
        associatedConnection = null;
        invalid = true;
        invalidateStackTrace = XConnectionUtil.getStackTrace(new Exception());
    }
        
    /**
     * Invoke method on a specified proxy. Here we check if <code>method</code>
     * is a method {@link PreparedStatement#close()}. If yes, we notify owner
     * that this prepared statement was closed.
     * 
     * @param proxy proxy on which method is invoked.
     * @param method instance of {@link Method} describing method being invoked.
     * @param args array with arguments.
     * 
     * @return result of method invokation.
     * 
     * @throws Throwable if invoked method threw an exception.
     */
    public Object invoke(Object proxy, Method method, Object[] args) 
        throws Throwable 
    {
        checkCorrectness(method);
        
        try {
            if (method.equals(PREPARED_STATEMENT_CLOSE)) {
                handleStatementClose(statement, proxy);
                return Void.TYPE;
            } else
            if (method.equals(PREPARED_STATEMENT_GET_CONNECTION)) {
                return associatedConnection;
            } else
            if (method.equals(PREPARED_STATEMENT_SET_CONNECTION)) {
                this.associatedConnection = (Connection)args[0];
                return Void.TYPE;
            } else
            if (method.equals(PREPARED_STATEMENT_GET_ORIGINAL)) {
                return preparedStatement;
            } else
            if (method.equals(PREPARED_STATEMENT_FORCE_CLOSE)) {
                handleForceClose();
                return Void.TYPE;
            } else {
                
                Object result = method.invoke(preparedStatement, args);
                
                if (result instanceof ResultSet) {
                    
                    ResultSetHandler handler = new ResultSetHandler(
                        (PreparedStatement)proxy, 
                        (ResultSet)result
                        );
                        
                    result = Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{ResultSet.class},
                        handler
                        );
                    
                } 
                
                return result;
            }
            
        } catch(InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    /**
     * Check if method call is correct. If method was declared in 
     * {@link PreparedStatement} class and there is no connection proxy
     * associated with this statement (i.e. statement in in pool), exception is 
     * thrown (this situation usually happens if somebody tries to call method
     * on prepared statement after it has been closed).
     * 
     * @param method method to check.
     * 
     * @throws SQLException if statement has incorrect statement.
     */
    protected void checkCorrectness(Method method) throws SQLException {
        String message =  "Statemenet has illegal state because it "+
            "does not have any associated connection at this time. " + 
            "Usually this means that Statement.getConnection() " + 
            "method was called on a closed statement that currently " + 
            "lives in a statement pool.";
            
        if (invalid)
            message += "\n" + invalidateStackTrace;
        
        boolean incorrectState = 
            (associatedConnection == null &&
             method.getDeclaringClass().equals(PreparedStatement.class)) ||
             invalid;
            
        if (incorrectState)
            if (TOLERANT_CHECK_MODE)
                throw new SQLException(message);
            else
                throw new IllegalStateException(message);
    }

}
