/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
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
package org.firebirdsql.jdbc;

import java.sql.SQLException;

public interface FirebirdResultSet extends java.sql.ResultSet {

    /**
     * Get execution plan for the specified result set. 
     * 
     * @return execution plan for this query.
     * 
     * @throws SQLException if execution plan cannot be obtained or this result
     * set is already closed.
     * 
     * @see FirebirdPreparedStatement#getExecutionPlan()
     */
    String getExecutionPlan() throws SQLException;
    
    /**
     * Retrieves the holdability of this <code>ResultSet</code> object
     * <p>
     * Copied from java.sql.ResultSet of Java 6 for Java 5 compatibility
     * </p>
     * @return  either <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException if a database access error occurs
     * or this method is called on a closed result set
     * @since 1.6
     */
    int getHoldability() throws SQLException;
    
    /**
     * Retrieves whether this <code>ResultSet</code> object has been closed. A <code>ResultSet</code> is closed if the
     * method close has been called on it, or if it is automatically closed.
     * <p>
     * Copied from java.sql.ResultSet of Java 6 for Java 5 compatibility
     * </p>
     * @return true if this <code>ResultSet</code> object is closed; false if it is still open
     * @throws SQLException if a database access error occurs
     * @since 1.6
     */
    boolean isClosed() throws SQLException;
}
