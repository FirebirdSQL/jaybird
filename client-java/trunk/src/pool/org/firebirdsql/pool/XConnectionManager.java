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
 * This interface represents the manager of {@link XConnection} instance.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
interface XConnectionManager {
    
    /**
     * Get array of interfaces that should be implemented by a dynamic proxy
     * that will intercept all calls to the connection. Result must contain at
     * least {@link java.sql.Connection} interface 
     * 
     * @return array of interfaces that should be implemented by dynamic proxy.
     */
    Class[] getImplementedInterfaces();
	
	/**
	 * Check if specified connection is still valid. 
	 * 
	 * @param connection instance of {@link XConnection} to check.
	 * 
	 * @return <code>true</code> if connection owner is still valid, 
	 * <code>false</code> otherwise.
	 */
	boolean isValid(XConnection connection);
	
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
     */
    PreparedStatement getPreparedStatement(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException;
	
	/**
	 * Notify connection owner about invocation of the {@link Connection#close()} 
	 * operation on {@link XConnection} instance.
	 * 
	 * @param connection instance of {@link XConnection} that initiated the call.
	 */
	void connectionClosed(XConnection connection) throws SQLException;
	
	/**
	 * Notify connection owner about the {@link SQLException} that happened
	 * during method invocation on the wrapped connection.
	 * 
	 * @param connection instance of {@link XConnection} that catched exception.
	 * 
	 * @param ex instance of {@link SQLException} that was thrown.
	 */
	void connectionErrorOccured(XConnection connection, SQLException ex);
    
    /**
     * Notify connection owner about transaction commit.
     * 
     * @param connection instance of {@link XConnection} that initiated the call.
     */
    void connectionCommitted(XConnection connection) throws SQLException;
    
    /**
     * Notify connection owner about transaction rollback.
     * 
     * @param connection instance of {@link XConnection} that initiated the call.
     */
    void connectionRolledBack(XConnection connection) throws SQLException;
}