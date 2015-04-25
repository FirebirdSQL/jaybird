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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.jdbc.FBDriverNotCapableException;

/**
 * This is simple implementation of {@link DataSource} interface that uses
 * {@link ConnectionPoolDataSource} as connection provider.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @deprecated Use {@link FBSimpleDataSource} for non-pooling, this implementation 
 * only works correctly with the deprecated {@link FBConnectionPoolDataSource} and 
 * {@link DriverConnectionPoolDataSource} classes
 */
public class SimpleDataSource extends RootCommonDataSource implements DataSource {
	
	private ConnectionPoolDataSource pool;
	private int timeout;
	
	/**
	 * Create instance of this class.
	 * 
	 * @param pool instance of {@link ConnectionPoolDataSource} that will provide
	 * connections to this data source.
	 */
	public SimpleDataSource(ConnectionPoolDataSource pool) {
		this.pool = pool;
	}
	
	/**
	 * Get JDBC connection.
	 * 
	 * @return instance of {@link Connection} from this data source.
	 * 
	 * @throws SQLException if connection cannot be obtained.
	 */
    public Connection getConnection() throws SQLException {
		return pool.getPooledConnection().getConnection();
    }

	/**
	 * Get JDBC connection for the specified username and password.
	 * 
	 * @param username username for new connection.
	 * @param password password corresponding to the username.
	 * 
	 * @return instance of {@link Connection}.
	 * 
	 * @throws SQLException if connection cannot be obtained.
	 */
    public Connection getConnection(String username, String password) throws SQLException {
		return pool.getPooledConnection(username, password).getConnection();
    }

    public int getLoginTimeout() throws SQLException {
		return timeout;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
		this.timeout = seconds;
    }
	
    // JDBC 4.0
    
    public boolean isWrapperFor(Class iface) throws SQLException {
    	return false;
    }
    
    public Object unwrap(Class iface) throws SQLException {
    	throw new FBDriverNotCapableException();
    }
    
}