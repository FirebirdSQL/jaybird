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
 * This interface describes a configuration for {@link AbstractConnectionPool} 
 * instances. Motivation for separating pool configuration into interface is 
 * quite simple, it allows third-party applications to load configuration from
 * various sources (resource bundle, XML file, etc.).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public interface ConnectionPoolConfiguration {
	
	/**
	 * Get minimum number of open JDBC connections that will be created
	 * when pool is started.
	 * 
	 * @return minimum number of open JDBC connections to open at startup.
     * 
     * @deprecated please use {@link #getMinPoolSize()} instead.
	 */
	int getMinConnections();
    
    /**
     * Get minimum number of open JDBC connections that will be created
     * when pool is started.
     * 
     * @return minimum number of open JDBC connections to open at startup.
     */
    int getMinPoolSize();
	
	/**
	 * Get maximum number of physical connections that can be simultaneously
	 * open.
	 * 
	 * @return maximum number of simultaneously open physical database 
	 * connections, 0 if no limit exists.
     * 
     * @deprecated please use {@link #getMaxPoolSize()} instead.
	 */
	int getMaxConnections();
    
    /**
     * Get maximum number of physical connections that can be simultaneously
     * open.
     * 
     * @return maximum number of simultaneously open physical database 
     * connections, 0 if no limit exists.
     */
    int getMaxPoolSize();
    
    /**
     * Get time during which {@link javax.sql.ConnectionPoolDataSource#getPooledConnection()}
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
     * Get time after which idle connections will be deallocated.
     * 
     * @return idle timeout in milliseconds, {@link Integer#MAX_VALUE}
     * indicates that idle connections are not removed.
     * 
     * @deprecated please use {@link #getMaxIdleTime()} instead.
     */
    int getIdleTimeout();

    /**
     * Get time after which idle connections will be deallocated.
     * 
     * @return idle timeout in milliseconds, {@link Integer#MAX_VALUE}
     * indicates that idle connections are not removed.
     */
    int getMaxIdleTime();
    
    /**
     * Check if this connection pool uses connection pooling, or just implements
     * JDBC 2.0 SE interfaces. By default pooling is on. It might make sense to 
     * set pooling off to check performance degradation in test environment. It
     * could be also used in the environment where physical connection must be
     * closed right after usage, however using JDBC 2.0 SE interfaces is either
     * a requirement or is simpler than standard <code>java.sql.Driver</code>.
     * 
     * @return <code>true</code> if pooling is enabled.
     */
    boolean isPooling();
    
    /**
     * Check if this connection pool provides also prepared statement pooling.
     * By default prepared statement pooling is enabled, however there might be
     * situations where statement pooling is not desired, for example in 
     * environments where database can quickly run out of handles and fast 
     * handle reuse is required.
     * 
     * @return <code>true</code> if prepared statement pooling is enabled.
     */
    boolean isStatementPooling();
    
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