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

package org.firebirdsql.jdbc;

import java.sql.SQLException;

/**
 * Instances of this field cache data in auto-commit case if no transaction is
 * yet available and must be flushed before transaction is committed.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public interface FBFlushableField {
    
    /**
     * Flush cached data to the database server.
     * 
     * @throws SQLException if something went wrong.
     */
    void flushCachedData() throws SQLException;
    
    /**
     * Get cached object.
     * 
     * @return cached object of this field.
     * 
     * @throws SQLException if something went wrong.
     */
    byte[] getCachedObject() throws SQLException;
    
}