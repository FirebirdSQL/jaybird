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
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This interface defines a cachable prepared statement. It should be used
 * internally only.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface XCachablePreparedStatement extends PreparedStatement {
    
    /**
     * Set associated connection.
     * 
     * @param connection connection that will be associated or <code>null</code>
     * to clean the association.
     * 
     * @throws SQLException
     */
    void setConnection(Connection connection) throws SQLException;

    /**
     * Force {@link PreparedStatement#close()} method on a cached prepared 
     * statement. This method deallocates prepared statement in JDBC driver.
     * Implementation should simply call {@link PreparedStatement#close()} on
     * physical prepared statement.
     * 
     * @throws SQLException if {@link PreparedStatement#close()} threw this
     * exception.
     */
    void forceClose() throws SQLException;
}
