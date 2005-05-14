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

import java.sql.*;
import java.sql.SQLException;

import org.firebirdsql.jca.FBManagedConnection;

/**
 * Firebird connection class implementing JDBC 3.0 methods.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnection extends AbstractConnection {

	/**
     * Create instance of this class for the specified managed connection.
     * 
	 * @param mc managed connection.
	 */
	public FBConnection(FBManagedConnection mc) {
		super(mc);
	}
    
	/**
	 * Commit current transaction.
     * 
     * @throws SQLException if something went wrong.
	 */
	public synchronized void commit() throws SQLException {
		super.commit();
        
        invalidateSavepoints();
	}

	/**
     * Rollback current transaction.
     * 
     * @throws SQLException if something went wrong.
	 */
    public synchronized void rollback() throws SQLException {
		super.rollback();
        
        invalidateSavepoints();
	}

    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
                                                           int param2) throws SQLException 
    {
        throw new FBDriverNotCapableException();
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
                                                           int[] param2) throws SQLException 
    {
        throw new FBDriverNotCapableException();
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
                                                           String[] param2) throws SQLException 
    {
        throw new FBDriverNotCapableException();
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized CallableStatement prepareCall(String param1, int param2, 
                                                      int param3, int param4) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }

    
}
