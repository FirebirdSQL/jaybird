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

import javax.sql.*;

import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.PrintWriter;

/**
 * This is simple implementation of {@link DataSource} interface that uses
 * {@link ConnectionPoolDataSource} as connection provider.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class SimpleDataSource implements DataSource {
	
	private ConnectionPoolDataSource pool;
	private int timeout;
	private PrintWriter log;
	
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

    public PrintWriter getLogWriter() throws SQLException {
		return log;
    }

    public int getLoginTimeout() throws SQLException {
		return timeout;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
		this.log = out;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
		this.timeout = seconds;
    }

    public boolean isWrapperFor(Class arg0) throws SQLException {
        return arg0 != null && arg0.isAssignableFrom(SimpleDataSource.class);
    }

    public Object unwrap(Class arg0) throws SQLException {
        if (!isWrapperFor(arg0))
            throw new FBSQLException("No compatible class found.");
        
        return this;
    }

}