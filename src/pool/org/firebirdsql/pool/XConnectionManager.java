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

import java.sql.*;

/**
 * This interface represents the manager of {@link PooledConnectionHandler} 
 * instance.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
interface XConnectionManager {
    
	/**
	 * Check if specified connection is still valid. 
	 * 
	 * @param connection instance of {@link PooledConnectionHandler} to check.
	 * 
	 * @return <code>true</code> if connection owner is still valid, 
	 * <code>false</code> otherwise.
	 */
	boolean isValid(PooledConnectionHandler connection);
	
	/**
	 * Ping physical connection.
	 * 
	 * @return <code>true</code> if connection was successfully pinged.
	 */
	boolean ping();
	
	/**
	 * Get the time when connection was pinged last time.
	 * 
	 * @return time when connection was pinged last time.
	 */
	long getLastPingTime();
    
    /**
     * Get instance of {@link PreparedStatement} for the specified SQL statement.
     * Default implementation would call 
     * {@link Connection#prepareStatement(String, int, int)} on physical 
     * connection. However it is also possible to implement prepared statement
     * pooling for increased performance.
     * 
     * @param sql SQL statement for which prepared statement must be constructed.
     * 
     * @param resultSetType type of the result set.
     * @param resultSetConcurrency result set concurrency.
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if prepared statement cannot be obtained.
     * 
     * @deprecated
     */
    PreparedStatement getPreparedStatement(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException;
    
    /**
     * Get instance of {@link PreparedStatement} for the specified SQL statement.
     * Default implementation would call 
     * {@link Connection#prepareStatement(String, int, int)} on physical 
     * connection. However it is also possible to implement prepared statement
     * pooling for increased performance.
     * 
     * @param sql SQL statement for which prepared statement must be constructed.
     * 
     * @param resultSetType type of the result set.
     * @param resultSetConcurrency result set concurrency.
     * @param resultSetHoldability result set holdability.
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if prepared statement cannot be obtained.
     */
    PreparedStatement getPreparedStatement(String sql, int resultSetType,
        int resultSetConcurrency, int resultSetHoldability) throws SQLException;
	
    /**
     * Prepare specified SQL statement that will return the generated keys. 
     * This method should call corresponding methods on physical JDBC
     * connection:
     * <ul> 
     * <li>{@link java.sql.Connection#prepareStatement(String, int)}
     * <li>{@link java.sql.Connection#prepareStatement(String, int[])}
     * <li>{@link java.sql.Connection#prepareStatement(String, String[])}
     * </ul>
     * 
     * @param sql SQL statement for which prepared statement must be constructed.
     * 
     * @param keyIndexes - array of key column indexes if they were specified or 
     * <code>null</code> in other cases. 
     * 
     * @param keyColumns - array of key column names if they were specified or 
     * <code>null</code> in other cases. 
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if prepared statement cannot be obtained.
     */
    PreparedStatement getPreparedStatement(String sql, int[] keyIndexes,
            String[] keyColumns) throws SQLException;
    
	/**
	 * Notify connection owner about invocation of the {@link Connection#close()} 
	 * operation on {@link PooledConnectionHandler} instance.
	 * 
	 * @param connection instance of {@link PooledConnectionHandler} that 
     * initiated the call.
	 */
	void connectionClosed(PooledConnectionHandler connection) throws SQLException;
	
	/**
	 * Notify connection owner about the {@link SQLException} that happened
	 * during method invocation on the wrapped connection.
	 * 
	 * @param connection instance of {@link PooledConnectionHandler} that 
     * catched exception.
	 * 
	 * @param ex instance of {@link SQLException} that was thrown.
	 */
	void connectionErrorOccured(PooledConnectionHandler connection, SQLException ex);
    
    /**
     * Notify connection owner about transaction commit.
     * 
     * @param connection instance of {@link PooledConnectionHandler} that 
     * initiated the call.
     */
    void connectionCommitted(PooledConnectionHandler connection) throws SQLException;
    
    /**
     * Notify connection owner about transaction rollback.
     * 
     * @param connection instance of {@link PooledConnectionHandler} that 
     * initiated the call.
     */
    void connectionRolledBack(PooledConnectionHandler connection) throws SQLException;
}