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

import java.sql.SQLException;

/**
 * Manager of prepared statement. This interface defines an entity that is able 
 * to prepare SQL statements. Also this instance is notified when statement is 
 * closed.
 * <p>
 * Currently only {@link PingablePooledConnection} is implementing this interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface XStatementManager {
    
    /**
     * Prepare specified SQL statement. This method should call 
     * {@link java.sql.Connection#prepareStatement(String)} method on physical JDBC
     * connection.
     * 
     * @param sql SQL statement to prepare.
     * 
     * @param resultSetType type of result set
     * 
     * @param resultSetConcurrency result set concurrency
     * 
     * @param cached <code>true</code> if prepared statement will be cached.
     * 
     * @return instance of {@link java.sql.PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if something went wrong.
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
     * 
     * @deprecated use {@link #prepareStatement(String, int, int, int, boolean)}
     * intead.
     */
    XCachablePreparedStatement prepareStatement(String sql, int resultSetType, 
        int resultSetConcurrency, boolean cached) throws SQLException;
    
    /**
     * Prepare specified SQL statement. This method should call 
     * {@link java.sql.Connection#prepareStatement(String)} method on physical JDBC
     * connection.
     * 
     * @param key instance of {@link XPreparedStatementModel} containing all needed
     * information to prepare a statement.
     * 
     * @param cached <code>true</code> if prepared statement will be cached.
     * 
     * @return instance of {@link java.sql.PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if something went wrong.
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
     */
    XCachablePreparedStatement prepareStatement(XPreparedStatementModel key, boolean cached) 
    throws SQLException;
    
    /**
     * Notify about statement close.
     * 
     * @param statement SQL statement of an object that is being closed.
     * @param proxy proxy on which {@link java.sql.Statement#close()} method was called.
     * 
     * @throws SQLException if something went wrong.
     * 
     * @deprecated 
     */
    void statementClosed(String statement, Object proxy) throws SQLException;
    
    /**
     * Notify about statement close.
     * 
     * @param key Key of the SQL statement that was closed.
     * @param proxy proxy on which {@link java.sql.Statement#close()} method was called.
     * 
     * @throws SQLException if something went wrong.
     */
    void statementClosed(XPreparedStatementModel key, Object proxy) throws SQLException;
}
