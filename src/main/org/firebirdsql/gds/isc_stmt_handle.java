/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;

/**
 * <code>isc_stmt_handle</code> describes a handle to a database statement.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface isc_stmt_handle {

    /**
     * Get the input data structure that contains data that is put into
     * the statement.
     *
     * @return The input data structure
     */
    XSQLDA getInSqlda();

    /**
     * Get the output data structure that contains data that is retrieved
     * from the statement.
     *
     * @return The output data structure
     */
    XSQLDA getOutSqlda();

    /**
     * Clear all rows that have been fetched for this statement. This 
     * method differs from {@link #removeRows} in that it only affects rows 
     * that have already been fetched.
     *
     * @see #removeRows
     */
    void clearRows();

    /**
     * Get the number of rows that were inserted by executing this statement.
     *
     * @return The number of inserted rows
     */
    int getInsertCount();

    /**
     * Get the number of rows that were updated by executing this statement.
     *
     * @return The number of updated rows
     */
    int getUpdateCount();

    /**
     * Get the number of rows that were deleted by executing this statement.
     *
     * @return The number of deleted rows
     */
    int getDeleteCount();

    /**
     * Retrieve whether all rows have been fetched of the rows selected
     * by executing this statement.
     *
     * @return <code>true</code> if all rows have been fetched,
     *         <code>false</code> otherwise
     */
    boolean getAllRowsFetched();

    /**
     * Get the rows retrieved by executing this statement.
     *
     * @return Array of rows retrieved
     */
    Object[] getRows();

    /**
     * Get the number of rows contained in this statement.
     *
     * @return The rowcount for this statement
     */
    int size();

    /**
     * Remove all rows contained by this statement. This method differs from
     * {@link #clearRows} in that it effectively clears all rows from this
     * statement.
     *
     * @see #clearRows
     */
    void removeRows();
    
    /**
     * Retrieve whether or not this statement is valid.
     *
     * @return <code>true</code> if this is a valid statement, 
     *         <code>false</code> otherwise
     */
    boolean isValid();
    
    /**
     * Retrieve whether this statement has an open <code>ResultSet</code>.
     *
     * @return <code>true</code> if this statement has an open 
     *         <code>ResultSet</code>, false otherwise
     */
    boolean hasOpenResultSet();
}
