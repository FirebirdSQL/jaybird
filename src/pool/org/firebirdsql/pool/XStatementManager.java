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
 * Currently only {@link XConnection} is implementing this interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface XStatementManager {
    
    /**
     * Prepare specified SQL statement. This method should call 
     * {@link Connection#prepareStatement(String)} method on physical JDBC
     * connection.
     * 
     * @param sql SQL statement to prepare.
     * 
     * @param resultSetType type of result set
     * 
     * @param resultSetConcurrency result set concurrency
     * 
     * @return instance of {@link PreparedStatement} corresponding to the 
     * specified SQL statement.
     * 
     * @throws SQLException if something went wrong.
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
     */
    XCachablePreparedStatement prepareStatement(String sql, int resultSetType, 
        int resultSetConcurrency) throws SQLException;
        
    /**
     * Notify about statement close.
     * 
     * @param statement SQL statement of an object that is being closed.
     * @param proxy proxy on which {@link Statement#close()} method was called.
     * 
     * @param stmt statement that was closed.
     */
    void statementClosed(String statement, Object proxy) throws SQLException;
}
