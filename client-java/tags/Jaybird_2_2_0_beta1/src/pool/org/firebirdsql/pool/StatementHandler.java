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

import static org.firebirdsql.ds.ReflectionHelper.findMethod;
import static org.firebirdsql.ds.ReflectionHelper.getAllInterfaces;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Dynamic proxy handler that together with {@link org.firebirdsql.pool.PooledConnectionHandler}
 * ensure that statements will be closed before connection is returned to the
 * pool.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class StatementHandler implements InvocationHandler {
    
    private static final Method STATEMENT_CLOSE = 
        findMethod(Statement.class, "close", new Class[0]);
    
    private PooledConnectionHandler connectionHandler;
    private Statement wrappedObject;
    
    private Statement proxy;
    
    /**
     * Create instance of this class for the specified connection handler and
     * 
     * @param connectionHandler instance of {@link PooledConnectionHandler} that is 
     * responsible for a connection that created a statement to wrap.
     * 
     * @param wrappedObject instance of {@link Statement} to wrap.
     */
    public StatementHandler(PooledConnectionHandler connectionHandler, 
            Statement wrappedObject) 
    {
        this.connectionHandler = connectionHandler;
        this.wrappedObject = wrappedObject;
        
        proxy = (Statement)Proxy.newProxyInstance(
                wrappedObject.getClass().getClassLoader(),
                getAllInterfaces(wrappedObject.getClass()),
                this);
    }

    /**
     * Get dynamic proxy instant wrapping the statement.
     * 
     * @return instance of {@link Statement}.
     */
    public Statement getProxy() {
        return proxy;
    }
    
    /**
     * Get original statement that was wrapped.
     * 
     * @return instance of {@link Statement}.
     */
    public Statement getWrappedObject() {
        return wrappedObject;
    }
    
    /**
     * Invoke a specified method with the specified arguments on the specified
     * proxy. This method checks if {@link Statement#close()} is called
     * and de-registers the statement from automatic close on connection close.
     * 
     * @param proxy proxy, on which the method was called.
     * @param method method that was invoked.
     * @param args arguments that were passed to the method.
     * 
     * @return object that was returned by the method.
     * 
     * @throws Throwable throwable that was thrown during method processing.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        
        if (STATEMENT_CLOSE.equals(method)) {
            connectionHandler.forgetStatement(this);
        }
        
        try {
            Object result = method.invoke(wrappedObject, args);
            
            if (result instanceof ResultSet) {
                
                ResultSetHandler handler = new ResultSetHandler(
                        (Statement) proxy, (ResultSet) result);

                result = Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[] { ResultSet.class}, handler);
                
            } 
            
            return result;

        } catch(InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
}
