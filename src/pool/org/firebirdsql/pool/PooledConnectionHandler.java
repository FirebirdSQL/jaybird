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
import java.sql.*;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * This is a wrapper for {@link Connection} instances that caches prepared
 * statements.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class PooledConnectionHandler implements InvocationHandler {
	
	private static final boolean CACHE_PREPARED_STATEMENTS = true;
	
	private static final boolean LOG_PREPARE_STATEMENT = false;
	private static final boolean LOG_STATEMENT_IN_POOL = false;
	private static final boolean LOG_POOL_CLEANING = false;
	private static final boolean LOG_META_DATA = false;
	private static final boolean LOG_REENTRANT_ACCESS = false;
	
	private static Logger logChannel = LoggerFactory.getLogger(PooledConnectionHandler.class, false);
    
    /**
     * Helper function to find specified method in a specified class.
     * 
     * @param clazz class in which we look for a specified method.
     * @param name name of the method.
     * @param args types of method params.
     * 
     * @return instance of {@link Method} corresponding to specified name
     * and param types.
     */
    public static Method findMethod(Class clazz, String name, Class[] args) {
        try {
            return clazz.getMethod(name, args);
        } catch (NoSuchMethodException nmex) {
            throw new NullPointerException(
                "No method for proxying found. Please check your classpath.");
        }
    }
    
    private final static Method CONNECTION_PREPARE_STATEMENT = findMethod(
        Connection.class, "prepareStatement", new Class[] {String.class});

    private final static Method CONNECTION_PREPARE_STATEMENT2 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, Integer.TYPE, Integer.TYPE
        });
        
    private final static Method CONNECTION_CLOSE = findMethod(
        Connection.class, "close", new Class[0]);
        
    private final static Method CONNECTION_COMMIT = findMethod(
        Connection.class, "commit", new Class[0]);
        
    private final static Method CONNECTION_ROLLBACK = findMethod(
        Connection.class, "rollback", new Class[0]);
        
    private Connection connection;
	private XConnectionManager owner;
	
	private Connection proxy;
    
	private boolean closed;
	private String closeStackTrace = "";

    /**
     * Construct instance of this class. This method constructs new proxy
	 * that implements {@link Connection} interface and uses newly constructed
	 * object as invocation handler.
     * 
     * @param connection connection to wrap.
     * 
	 * @param owner instance of {@link XConnectionOwner} that owns this 
	 * connection instance.
     * 
     * @param pingable <code>true</code> if created connection should be 
     * pingable.
     * 
     * @param useProxy <code>true</code> if this class should use dynamic
     * proxies, otherwise {@link PingableConnectionWrapper} will be used.
     * 
     * @throws SQLException if something went wrong during initialization.
     */
    public 
	    PooledConnectionHandler(Connection connection, XConnectionManager owner) 
            throws SQLException
        {
            
            this.connection = connection;
            this.owner = owner;
            
            Class[] implementedInterfaces = connection.getClass().getInterfaces();

            proxy = (Connection)Proxy.newProxyInstance(
                PooledConnectionHandler.class.getClassLoader(),
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
     * @return instance of {@link XConnectionManager}.
     */
    public XConnectionManager getManager() {
        return owner;
    }

	private boolean invokeEntered;
	
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
			
			if (LOG_REENTRANT_ACCESS && invokeEntered)
			    logChannel.warn("Re-entrant access detected.", new Exception());
			
			invokeEntered = true;
			
			if (closed) 
			    throw new SQLException(
				    "Connection " + this + " was closed. Stack trace on close:\n" + 
					closeStackTrace + "\nat:\n");
			if ((owner != null && !owner.isValid(this)))
			    throw new SQLException(
				    "This connection owner is not valid anymore.");
			
			if (method.equals(CONNECTION_PREPARE_STATEMENT)){
				String statement = (String)args[0];
				return handlePrepareStatement(
                    statement, 
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            } else
            if (method.equals(CONNECTION_PREPARE_STATEMENT2)) {
                String statement = (String)args[0];
                Integer resultSetType = (Integer)args[1];
                Integer resultSetConcurrency = (Integer)args[2];
                return handlePrepareStatement(
                    statement, 
                    resultSetType.intValue(), 
                    resultSetConcurrency.intValue());
			} else
			if (method.equals(CONNECTION_COMMIT)) {
				handleConnectionCommit();
				return Void.TYPE;
			} else
			if (method.equals(CONNECTION_ROLLBACK)) {
				handleConnectionRollback();
				return Void.TYPE;
			} else
			if (method.equals(CONNECTION_CLOSE)) {
				handleConnectionClose();
				return Void.TYPE;
			} else
				return method.invoke(connection, args);
        } catch(InvocationTargetException ex) {
            
            if (ex.getTargetException() instanceof SQLException && owner != null)
                owner.connectionErrorOccured(this, (SQLException)ex.getTargetException());
                
            throw ex.getTargetException();
            
		} catch(SQLException ex) {
			
			if (owner != null) 
				owner.connectionErrorOccured(this, ex);
			
			throw ex;
		} finally {
			invokeEntered = false;
		}
    }
    
    /**
     * Handle {@link Connection#prepareStatement(String)} method call. This 
     * method check internal cache first and returns prepared statement if found.
     * Otherwise, it prepares statement and caches it.
     * 
     * @param statement statement to prepare.
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * <code>statement</code>.
     * 
     * @throws SQLException if there was problem preparing statement. 
     */
    synchronized PreparedStatement handlePrepareStatement(String statement, 
        int resultSetType, int resultSetConcurrency) throws SQLException 
    {
        return getManager().getPreparedStatement(
            statement, resultSetType, resultSetConcurrency);
    }
    
    /**
     * Handle {@link Connection#close()} method. This implementation closes the
     * connection and cleans the cache.
     * 
     * @throws SQLException if underlying connection threw this exception.
     */
    synchronized void handleConnectionClose() throws SQLException {
		if (owner != null) {
			owner.connectionClosed(this);            
        }

		closed = true;
        
		closeStackTrace = XConnectionUtil.getStackTrace(new Exception());
	}
    
    /**
     * Handle {@link Connection#commit()} method. This implementation commits the
     * connection and cleans the cache.
     * 
     * @throws SQLException if underlying connection threw this exception.
     */
    synchronized void handleConnectionCommit() throws SQLException {
        connection.commit();
		
		getManager().connectionCommitted(this);
    }
    
    /**
     * Handle {@link Connection#rollback()} method. This implementation rolls the
     * connection back and cleans the cache.
     * 
     * @throws SQLException if underlying connection threw this exception.
     */
    synchronized void handleConnectionRollback() throws SQLException {
        connection.rollback();
		
		getManager().connectionRolledBack(this);
    }
}