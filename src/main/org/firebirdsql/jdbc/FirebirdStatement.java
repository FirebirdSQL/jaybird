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
import java.sql.Statement;

/**
 * Firebird-specific extensions to the {@link Statement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdStatement extends Statement {

    /**
     * Get number of inserted rows. You can call this method multiple times,
     * it does not affect the JDBC result number.
     * 
     * @return number of inserted rows or -1 if current result is result set.
     * 
     * @throws SQLException if database error occurs.
     */
	int getInsertedRowsCount() throws SQLException;
    
    /**
     * Get number of updated rows. You can call this method multiple times,
     * it does not affect the JDBC result number.
     * 
     * @return number of updated rows or -1 if current result is result set.
     * 
     * @throws SQLException if database error occurs.
     */
    int getUpdatedRowsCount() throws SQLException;
    
    /**
     * Get number of deleted rows. You can call this method multiple times,
     * it does not affect the JDBC result number.
     * 
     * @return number of deleted rows or -1 if current result is result set.
     * 
     * @throws SQLException if database error occurs.
     */
    int getDeletedRowsCount() throws SQLException;
    
    /**
     * Check if this statement has open result set. Note, this method works
     * correctly if auto-commit is disabled. In auto-commit mode it will always
     * return <code>false</code> because from the statement's point of view
     * result set is not open (in auto-commit mode complete result set is fetched
     * and cached in wrapping object before returning from the 
     * {@link #getResultSet()} method).
     * 
     * @return <code>true</code> if there's already open result set associated
     * with this statement, otherwise <code>false</code>.
     */
    boolean hasOpenResultSet();
}
