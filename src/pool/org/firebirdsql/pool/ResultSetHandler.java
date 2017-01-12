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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Wrapper for result sets. This class watches the {@link ResultSet#getStatement()}
 * invocations and substitutes the result of the method call with the proxy 
 * wrapping corresponding prepared statement.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public class ResultSetHandler implements InvocationHandler {
    
    private final static Method RESULT_SET_GET_STATEMENT = findMethod(
        ResultSet.class, "getStatement", new Class[0]);
        
        
    private Statement owner;
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
    public ResultSetHandler(Statement owner, ResultSet target) {
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
                if (ex.getTargetException() instanceof SQLException)
                    throw ex.getTargetException();
                else
                    throw ex;
            }
        }
	}

}
