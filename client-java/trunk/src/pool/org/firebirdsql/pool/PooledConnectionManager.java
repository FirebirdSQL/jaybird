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

import javax.sql.PooledConnection;

/**
 * Manager of pooled connections. Class implementing this interface allocates 
 * physical connections to the database.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface PooledConnectionManager {

    /**
     * Allocate pooled connection.
     * 
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    PooledConnection allocateConnection() throws SQLException;
    
    /**
     * Allocate pooled connection using specified user name and password.
     * 
     * @param userName user name that will be used to access database.
     * @param password password corresponding to the specified user name.
     * 
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if something went wrong.
     */
    PooledConnection allocateConnection(String userName, String password)
        throws SQLException;
}
