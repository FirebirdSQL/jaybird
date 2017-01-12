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
 * Manager of pooled connections. Class implementing this interface allocates 
 * physical connections to the database.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public interface PooledConnectionManager {

    /**
     * Allocate pooled connection using specified key.
     * 
     * @param key Key of the pool
     * @param queue The queue that will own the connection
     *
     * @return instance of {@link javax.sql.PooledConnection}.
     * 
     * @throws SQLException if something went wrong.
     */
    PooledObject allocateConnection(Object key, PooledConnectionQueue queue)
        throws SQLException;
}
