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

import java.sql.Statement;
import java.sql.SQLException;

/**
 * Instances of this class are able to fetch records from the server.
 */
interface FBFetcher {

    static final int MAX_FETCH_ROWS = 400;
	 
    Statement getStatement();
    boolean next() throws SQLException;
    void close() throws SQLException;

	int getRowNum();
    
	boolean getIsEmpty() throws SQLException;
	boolean getIsBeforeFirst() throws SQLException;
	boolean getIsFirst() throws SQLException;
	boolean getIsLast() throws SQLException;
	boolean getIsAfterLast() throws SQLException;
}