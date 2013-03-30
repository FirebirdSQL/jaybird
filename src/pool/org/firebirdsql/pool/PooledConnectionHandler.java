/*
 * $Id$
 * 
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

/**
 * This is a wrapper for {@link Connection} instances that caches prepared
 * statements.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class PooledConnectionHandler implements InvocationHandler {
	
	private static final boolean LOG_REENTRANT_ACCESS = PoolDebugConfiguration.DEBUG_REENTRANT;
	
	private static Logger logChannel = LoggerFactory.getLogger(PooledConnectionHandler.class, false);
    
    private final static Method CONNECTION_PREPARE_STATEMENT = findMethod(
        Connection.class, "prepareStatement", new Class[] {String.class});

    private final static Method CONNECTION_PREPARE_STATEMENT2 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, Integer.TYPE, Integer.TYPE
        });
        
    private final static Method CONNECTION_PREPARE_STATEMENT3 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE
        });

    private final static Method CONNECTION_PREPARE_STATEMENT_GENKEYS1 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, Integer.TYPE
        });

    private final static Method CONNECTION_PREPARE_STATEMENT_GENKEYS2 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, new int[0].getClass()
        });

    private final static Method CONNECTION_PREPARE_STATEMENT_GENKEYS3 = findMethod(
        Connection.class, "prepareStatement", new Class[] {
            String.class, new String[0].getClass()
        });
    
    private final static Method CONNECTION_CREATE_STATEMENT = findMethod(
            Connection.class, "createStatement", new Class[0]);

        private final static Method CONNECTION_CREATE_STATEMENT2 = findMethod(
            Connection.class, "createStatement", new Class[] {
                Integer.TYPE, Integer.TYPE
            });
    
    private final static Method CONNECTION_CLOSE = findMethod(
        Connection.class, "close", new Class[0]);
        
    private final static Method CONNECTION_COMMIT = findMethod(
        Connection.class, "commit", new Class[0]);
        
    private final static Method CONNECTION_ROLLBACK = findMethod(
        Connection.class, "rollback", new Class[0]);
    
    private final static Method CONNECTION_IS_CLOSED = findMethod(
        Connection.class, "isClosed", new Class[0]);
        
    private Connection connection;
	private XConnectionManager owner;
	
	private Connection proxy;
    
	private boolean closed;
    private ObjectCloseTraceException closeStackTrace;

    /**
     * Construct instance of this class. This method constructs new proxy
	 * that implements {@link Connection} interface and uses newly constructed
	 * object as invocation handler.
     * 
     * @param connection connection to wrap.
	 * @param owner instance of {@link XConnectionManager} that owns this 
	 * connection instance.
     * @throws SQLException if something went wrong during initialization.
     */
    public 
	    PooledConnectionHandler(Connection connection, XConnectionManager owner) 
            throws SQLException
        {
            
            this.connection = connection;
            this.owner = owner;
            
            Class[] implementedInterfaces = 
                getAllInterfaces(connection.getClass());

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
    
    /**
     * Check whether the {@link Connection#close()} method was called.
     * 
     * @return <code>true</code> if the method was called, false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Deallocate current connection. This call is similar to the call
     * {@link Connection#close()} when invoked on the proxy object. However,
     * unlike that call no listener is notified that connection being closed;
     *
     * @throws SQLException if something goes wrong.
     */
    public void deallocate() throws SQLException {
        handleConnectionClose(false);
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
			
			if (LOG_REENTRANT_ACCESS && invokeEntered && logChannel != null)
			    logChannel.warn("Re-entrant access detected.", new Exception());
			
			invokeEntered = true;
			
            // if object is closed, throw an exception
			if (closed) { 

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
            
			if ((owner != null && !owner.isValid(this)))
			    throw new SQLException(
				    "This connection owner is not valid anymore.");
			
			// Connection.prepareStatement(...) methods
			if (method.equals(CONNECTION_PREPARE_STATEMENT)){
				String statement = (String)args[0];
				return handlePrepareStatement(
                    statement, 
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            } else
            if (method.equals(CONNECTION_PREPARE_STATEMENT2)) {
                String statement = (String)args[0];
                Integer resultSetType = (Integer)args[1];
                Integer resultSetConcurrency = (Integer)args[2];
                return handlePrepareStatement(
                    statement, 
                    resultSetType.intValue(), 
                    resultSetConcurrency.intValue(),
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} else
            if (method.equals(CONNECTION_PREPARE_STATEMENT3)) {
                String statement = (String)args[0];
                Integer resultSetType = (Integer)args[1];
                Integer resultSetConcurrency = (Integer)args[2];
                Integer resultSetHoldability = (Integer)args[3];
                return handlePrepareStatement(
                    statement, 
                    resultSetType.intValue(), 
                    resultSetConcurrency.intValue(),
                    resultSetHoldability.intValue());
            } else
            
            // Connection.prepareStatement(...) for generated keys
                
            if (method.equals(CONNECTION_PREPARE_STATEMENT_GENKEYS1)){
                String statement = (String)args[0];
                Integer returnGeneratedKeys = (Integer)args[1];
                
                if (returnGeneratedKeys.intValue() == Statement.RETURN_GENERATED_KEYS)
                    return handlePrepareStatement(statement, null, null);
                else
                    return handlePrepareStatement(
                        statement, 
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT);
            } else
            if (method.equals(CONNECTION_PREPARE_STATEMENT_GENKEYS2)) {
                String statement = (String)args[0];
                int[] keyIndexes = (int[])args[1];
                return handlePrepareStatement(statement, keyIndexes, null);
            } else
                if (method.equals(CONNECTION_PREPARE_STATEMENT_GENKEYS3)) {
                    String statement = (String)args[0];
                    String[] keyColumns = (String[])args[1];
                    return handlePrepareStatement(statement, null, keyColumns);
            } else
                
            // Connection.createStatement(...) methods
            if (method.equals(CONNECTION_CREATE_STATEMENT)){
                return handleCreateStatement(
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            } else
            if (method.equals(CONNECTION_CREATE_STATEMENT2)) {
                Integer resultSetType = (Integer)args[0];
                Integer resultSetConcurrency = (Integer)args[1];
                return handleCreateStatement(
                    resultSetType.intValue(), 
                    resultSetConcurrency.intValue());
            } else
                
            // Connection lifycycle methods
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
     * @param resultSetType result set type.
     * @param resultSetConcurrency result set concurrency.
     * @param resultSetHoldability result set holdability
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * <code>statement</code>.
     * 
     * @throws SQLException if there was problem preparing statement. 
     */
    synchronized PreparedStatement handlePrepareStatement(String statement,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        
        return getManager().getPreparedStatement(statement, resultSetType,
            resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Handle {@link Connection#prepareStatement(String, int)}, 
     * {@link Connection#prepareStatement(String, int[])} and 
     * {@link Connection#prepareStatement(String, String[])} calls. This 
     * method checks internal cache first and returns the prepared statement 
     * if found. Otherwise, it prepares statement and caches it.
     * 
     * @param statement statement to prepare.
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * <code>statement</code>.
     * 
     * @throws SQLException if there was problem preparing statement. 
     */
    synchronized PreparedStatement handlePrepareStatement(String statement,
            int[] keyIndexes, String[] keyColumns) throws SQLException {
        
        return getManager().getPreparedStatement(statement, keyIndexes,
            keyColumns);
    }
    
    private HashSet openStatements = new HashSet();
    
    /**
     * Handle {@link Connection#createStatement(int, int)} method call.
     * @param resultSetType
     * @param resultSetConcurrency
     * @return
     * @throws SQLException
     */
    synchronized Statement handleCreateStatement(int resultSetType, 
            int resultSetConcurrency) throws SQLException 
    {
        Statement result = 
            connection.createStatement(resultSetType, resultSetConcurrency);
        
        StatementHandler handler = new StatementHandler(this, result);
        openStatements.add(handler);
        
        return handler.getProxy();
    }
    
    /**
     * Forget about a statement. This method removes a statement from the 
     * internal collection containing open statements. This method should be
     * called only from a dynamic proxy intercepting {@link Statement#close()}
     * method.
     * 
     * @param handler instance of {@link StatementHandler} wrapping a statement
     * to forget.
     */
    public synchronized void forgetStatement(StatementHandler handler) {
        openStatements.remove(handler);
    }
    
    /**
     * Close all open statements that were not correctly closed by the
     * application.
     * 
     * @throws SQLException if some error happened during close.
     */
    synchronized void closeOpenStatements() throws SQLException {
        SQLExceptionChainBuilder chain = new SQLExceptionChainBuilder();
        
        // Copy to prevent ConcurrentModificationException
        List copyStatements = new ArrayList(openStatements);
        for (Iterator iter = copyStatements.iterator(); iter.hasNext();) {
            StatementHandler handler = (StatementHandler) iter.next();
            
            try {
                handler.getWrappedObject().close();
            } catch(SQLException ex) {
                chain.append(ex);
            }
        }
        openStatements.clear();
        
        if (chain.hasException())
            throw chain.getException();
    }
    
    /**
     * Handle {@link Connection#close()} method. This implementation closes the
     * connection, cleans the cache and notifies the owner.
     * 
     * @throws SQLException if underlying connection threw this exception.
     */
    synchronized void handleConnectionClose() throws SQLException {
        handleConnectionClose(true);
    }
    
    /**
     * Handle {@link Connection#close()} method. This implementation closes the
     * connection and cleans the cache.
     * 
     * @param notifyOwner <code>true</code> when connection owner should be 
     * notified.
     * 
     * @throws SQLException if underlying connection threw this exception.
     */
    synchronized void handleConnectionClose(boolean notifyOwner) throws SQLException {
        try {
            closeOpenStatements();
        } finally {
    		if (owner != null && notifyOwner) {
    			owner.connectionClosed(this);            
            }
    
    		closed = true;
            closeStackTrace = new ObjectCloseTraceException();
        }
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