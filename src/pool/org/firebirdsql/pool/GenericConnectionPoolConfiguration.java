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
 * This is the implementation of {@link ConnectionPoolConfiguration} 
 * interface that allows using various JDBC drivers.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class GenericConnectionPoolConfiguration 
    extends FBConnectionPoolConfiguration 
{

    /**
     * Create instance of this class for the specified JDBC driver.
     * 
     * @param driverClassName name of the driver class.
     * 
     * @throws SQLException if driver was not found.
     */
    public GenericConnectionPoolConfiguration(String driverClassName) 
        throws SQLException 
    {
        try {
            Class.forName(driverClassName);
        } catch(ClassNotFoundException ex) {
            throw new SQLException(
                "Driver " + driverClassName + " not found.");
        }
    }

}
