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
 * Instances of this class are able to fetch records from the server.
 */
interface FBFetcher {

    static final int MAX_FETCH_ROWS = 400;
	
    /**
     * Move cursor to the rist row.
     * 
     * @return <code>true</code> if cursor was moved to the first row.
     * 
     * @throws SQLException if something went wrong.
     */
    boolean first() throws SQLException;
    
    /**
     * Move cursor to the last row.
     * 
     * @return <code>true</code> if cursor was moved to the last row.
     * 
     * @throws SQLException if something went wrong.
     */
    boolean last() throws SQLException;
    
    /**
     * Move cursor to the previous row.
     * 
     * @return <code>true</code> if cursor was moved to the prevous row.
     * 
     * @throws SQLException if something went wrong.
     */
    boolean previous() throws SQLException;
    
    /**
     * Move to next row.
     * 
     * @return <code>true</code> if cursor was moved.
     * 
     * @throws SQLException if something went wrong.
     */
    boolean next() throws SQLException;

    /**
     * Move cursor to the absolute row.
     * 
     * @param row absolute row number.
     * 
     * @return <code>true</code> if cursor was successfully moved. 
     * 
     * @throws SQLException if something went wrong.
     */
    boolean absolute(int row) throws SQLException;
    
    /**
     * Move cursor relative to the current row.
     *  
     * @param row relative row position.
     * 
     * @return <code>true</code> if cursor was successfully moved.
     * 
     * @throws SQLException if something went wrong.
     */
    boolean relative(int row) throws SQLException;    
    
    /**
     * Move cursor before first record.
     * 
     * @throws SQLException if something went wrong.
     */
    void beforeFirst() throws SQLException;
    
    /**
     * Move cursor after last record.
     * 
     * @throws SQLException if something went wrong.
     */
    void afterLast() throws SQLException;
    
    /**
     * Close this fetcher and corresponding result set.
     * 
     * @throws SQLException if something went wrong.
     */
    void close() throws SQLException;

    /**
     * Get row number.
     * 
     * @return row number.
     */
	int getRowNum();
    
	boolean isEmpty() throws SQLException;
	boolean isBeforeFirst() throws SQLException;
	boolean isFirst() throws SQLException;
	boolean isLast() throws SQLException;
	boolean isAfterLast() throws SQLException;
}