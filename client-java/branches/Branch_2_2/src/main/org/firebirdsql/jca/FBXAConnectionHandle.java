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
package org.firebirdsql.jca;

import static org.firebirdsql.ds.ReflectionHelper.findMethod;
import static org.firebirdsql.ds.ReflectionHelper.getAllInterfaces;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdConnection;

/**
 * Simple Connection wrapper returned by FBXAConnection.
 * 
 * @author <a href="mailto:lorban@bitronix.be">Ludovic Orban</a>
 * @deprecated
 */
public class FBXAConnectionHandle implements InvocationHandler {
    
    private final static Method CONNECTION_CLOSE = findMethod(
        Connection.class, "close", new Class[0]);

    private final static Method CONNECTION_IS_CLOSED = findMethod(
        Connection.class, "isClosed", new Class[0]);

    
    private AbstractConnection connection;
    private XAConnection owner;
    
    private Connection proxy;
    
    private SQLException closeStackTrace;

    /**
     * Construct instance of this class. This method constructs new proxy
     * that implements {@link Connection} interface and uses newly constructed
     * object as invocation handler.
     * 
     * @param connection connection to wrap.
     * 
     * @param owner instance of {@link XAConnection} that owns this 
     * connection instance.
     * 
     * @throws SQLException if something went wrong during initialization.
     */
    public 
        FBXAConnectionHandle(AbstractConnection connection, XAConnection owner) 
            throws SQLException
        {
            
            this.connection = connection;
            this.owner = owner;
            
            Class[] implementedInterfaces = 
                getAllInterfaces(connection.getClass());

            proxy = (Connection)Proxy.newProxyInstance(
                FirebirdConnection.class.getClassLoader(),
                implementedInterfaces,
                this);
        }
    
    /**
     * Get proxy implementing {@link Connection} interface and using this
     * instance as invocation handler.
     * 
     * @return instance of {@link Connection}.
     */
    public Connection getProxy() {
        return proxy;
    }
    
    /**
     * Get manager of this connection wrapper.
     * 
     * @return instance of {@link XAConnection}.
     */
    public XAConnection getXAConnection() {
        return owner;
    }
    
    // Connection overridings
    
    public void handleConnectionClose() throws SQLException {
        connection = null;
        closeStackTrace = new SQLException("Close trace.");
    }

    /**
     * Invoke method on a specified proxy. Here we check if <code>method</code>
     * is a method {@link Connection#prepareStatement(String)}. If yes, we check
     * if there is already a prepared statement for the wrapped connection or
     * wrap a newly created one.
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
        try {
            
            // if object is closed, throw an exception
            if (connection == null) { 

                // check whether Connection.isClose() method is called first
                if (CONNECTION_IS_CLOSED.equals(method))
                    return Boolean.TRUE;
                
                FBSQLException ex = new FBSQLException(
                    "Connection " + this + " was closed. " +
                            "See the attached exception to find the place " +
                            "where it was closed");
                ex.setNextException(closeStackTrace);
                throw ex;
            }
            
            if (method.equals(CONNECTION_CLOSE)) {
                handleConnectionClose();
                return Void.TYPE;
            } else
                return method.invoke(connection, args);
        } catch(InvocationTargetException ex) {
            throw ex.getTargetException();
        } catch(SQLException ex) {
            throw ex;
        }
    }
}
