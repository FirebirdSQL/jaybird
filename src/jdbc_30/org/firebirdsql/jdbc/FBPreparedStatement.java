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

import java.net.URL;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

/**
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBPreparedStatement extends AbstractPreparedStatement {

	public FBPreparedStatement(AbstractConnection c, String sql, int rsType, 
                               int rsConcurrency) throws SQLException 
    {
		super(c, sql, rsType, rsConcurrency);
	}

    /**
     * Sets the designated parameter to the given <code>java.net.URL</code> value. 
     * The driver converts this to an SQL <code>DATALINK</code> value
     * when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param url the <code>java.net.URL</code> object to be set
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */ 
    public void setURL(int parameterIndex, URL url) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("Not yet implemented");
    }


    /**
     * Retrieves the number, types and properties of this 
     * <code>PreparedStatement</code> object's parameters.
     *
     * @return a <code>ParameterMetaData</code> object that contains information
     *         about the number, types and properties of this 
     *         <code>PreparedStatement</code> object's parameters
     * @exception SQLException if a database access error occurs
     * @see ParameterMetaData
     * @since 1.4
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return new FBParameterMetaData(fixedStmt.getInSqlda().sqlvar, c);
    }
    
}
