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

/**
 * This interface describes a configuration for {@link AbstractConnectionPoolDataSource} 
 * instances. Motivation for separating pool configuration into interface is 
 * quite simple, it allows third-party applications to load configuration from
 * various sources (resource bundle, XML file, etc.).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface ConnectionPoolConfiguration {
	
	/**
	 * Get minimum number of open JDBC connections that will be created
	 * when pool is started.
	 * 
	 * @return minimum number of open JDBC connections to open at startup.
	 */
	int getMinConnections();
	
	/**
	 * Get maximum number of physical connections that can be simultaneously
	 * open.
	 * 
	 * @return maximum number of simultaneously open physical database 
	 * connections, 0 if no limit exists.
	 */
	int getMaxConnections();
    
    /**
     * Get time during which {@link ConnectionPoolDataSource#getPooledConnection()}
     * can block. By default method blocks forever.
     * 
     * @return pool blocking timeout in milliseconds, {@link Integer#MAX_VALUE} 
     * indicates blocking forever. 
     */
    int getBlockingTimeout();

    /**
     * Get interval of getting connections retries. It might happen that pool
     * contains no free connection. In order not to wait until connection is 
     * returned into the pool, pool will try to obtain connection again and
     * again with the interval returned by this method. Default value is 1000
     * (1 sec).
     * 
     * @return retry interval in milliseconds.
     */    
    int getRetryInterval();
    
	/**
	 * Check if this pool supports pingable connections. Pingable connections
	 * are used to correctly check if connection is still alive or not.
	 * 
	 * @return <code>true</code> if this pool supports pingable connections.
	 */
	boolean isPingable();
	
	/**
	 * Get SQL statement that will be used to ping connection. Ping SQL
	 * statement returns exactly one row without throwing an exception. Ping
	 * SQL statement must not influence the outcome of a transaction.
	 * <p>
	 * Examples of ping SQL statements:
	 * <ul>
	 * <li>Oracle: <code>"SELECT CAST(1 AS INTEGER) FROM DUAL"</code>
	 * <li>Firebird: <code>"SELECT CAST(1 AS INTEGER) FROM RDB$DATABASE"</code>
	 * </ul>
	 * 
	 * @return SQL statement that will be used to ping connection.
	 */
	String getPingStatement();
	
	/**
	 * Get time interval after which connection should be pinged.
	 * 
	 * @return number of milliseconds in the ping interval.
	 */
	int getPingInterval();
}