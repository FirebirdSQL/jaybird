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
import java.lang.reflect.Method;
import java.sql.*;

/**
 * Wrapper for result sets. This class watches the {@link ResultSet#getStatement()}
 * invocations and substitutes the result of the method call with the proxy 
 * wrapping corresponding prepared statement.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class ResultSetHandler implements InvocationHandler {


    public static Method findMethod(Class clazz, String name, Class[] params) {
        return PooledConnectionHandler.findMethod(clazz, name, params);
    }
    
    private final static Method RESULT_SET_GET_STATEMENT = findMethod(
        ResultSet.class, "getStatement", new Class[0]);
        
        
    private PreparedStatement owner;
    private ResultSet target;
    
    /**
     * Create instance of this class for the specified prepared statement proxy
     * and target result set.
     * 
     * @param owner dynamic proxy implementing {@link PreparedStatement} 
     * interface that generated result set to wrap.
     * 
     * @param target result set to wrap.
     */
    public ResultSetHandler(PreparedStatement owner, ResultSet target) {
        this.owner = owner;
        this.target = target;
    } 
        
    /**
     * Implementation of the dynamic proxy handler interface.
     */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        if (RESULT_SET_GET_STATEMENT.equals(method))
            return owner;
        else { 
            try {
                return method.invoke(target, args);
            } catch(InvocationTargetException ex) {
                if (ex.getCause() instanceof SQLException)
                    throw ex.getCause();
                else
                    throw ex;
            }
        }
	}

}
