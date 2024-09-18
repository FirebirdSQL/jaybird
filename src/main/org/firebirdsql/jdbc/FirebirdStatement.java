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

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Firebird-specific extensions to the {@link Statement} interface.
 * 
 * @author Roman Rokytskyy
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

    /**
     * Get current result set. Behaviour of this method is similar to the behavior of the
     * {@link Statement#getResultSet()}.
     *
     * @return instance of {@link ResultSet} representing current result set or {@code null} if it is not available.
     * @throws SQLException
     *         if a database access error happens
     * @deprecated Use {@link #getResultSet()} instead, will be removed in Jaybird 7
     */
    @Deprecated(since = "6", forRemoval = true)
    @Nullable ResultSet getCurrentResultSet() throws SQLException;
    
    /**
     * Check if this statement is valid.
     * 
     * @return <code>true</code> if statement is valid and can be used to 
     * execute SQL.
     */
    boolean isValid();
    
    /**
     * Alias for {@link #getExecutionPlan()}.
     *
     * @deprecated use {@link #getExecutionPlan()}; there are currently no plans to remove this method
     */
    @Deprecated(since = "6", forRemoval = false)
    default String getLastExecutionPlan() throws SQLException {
        return getExecutionPlan();
    }

    /**
     * Gets the simple execution plan of the statement.
     * <p>
     * Behaviour depends on the specific type:
     * </p>
     * <ul>
     * <li>{@link java.sql.Statement} but not {@link java.sql.PreparedStatement} &mdash; returns the plan of the last
     * executed statement</li>
     * <li>{@link java.sql.PreparedStatement} &mdash; returns the plan of the prepared statement</li>
     * <li>{@link java.sql.CallableStatement} &mdash; is not guaranteed to return the plan until after the first
     * execution</li>
     * </ul>
     *
     * @return execution plan returned by the server
     * @throws SQLException
     *         if no statement was executed or prepared before calling this method, if the statement is not valid or
     *         open, or there was an error when obtaining the execution plan
     * @since 6
     * @see #getExplainedExecutionPlan()
     */
    String getExecutionPlan() throws SQLException;

    /**
     * Alias for {@link #getExplainedExecutionPlan()}.
     *
     * @see #getExplainedExecutionPlan()
     * @deprecated use {@link #getExplainedExecutionPlan()}; there are currently no plans to remove this method
     */
    @Deprecated(since = "6", forRemoval = false)
    default String getLastExplainedExecutionPlan() throws SQLException {
        return getExplainedExecutionPlan();
    }

    /**
     * Gets the explained (or detailed) execution plan of the statement.
     * <p>
     * The explained execution plan is a more structured and comprehensible form compared to the (simple) execution
     * plan returned by {@link #getExecutionPlan()}. This requires Firebird 3.0 or higher.
     * </p>
     * <p>
     * Behaviour depends on the specific type:
     * </p>
     * <ul>
     * <li>{@link java.sql.Statement} but not {@link java.sql.PreparedStatement} &mdash; returns the plan of the last
     * executed statement</li>
     * <li>{@link java.sql.PreparedStatement} &mdash; returns the plan of the prepared statement</li>
     * <li>{@link java.sql.CallableStatement} &mdash; is not guaranteed to return the plan until after the first
     * execution</li>
     * </ul>
     *
     * @return execution plan returned by the server
     * @throws SQLException
     *         if no statement was executed or prepared before calling this method, if the statement is not valid or
     *         open, or there was an error when obtaining the execution plan
     * @since 6
     * @see #getExecutionPlan()
     */
    String getExplainedExecutionPlan() throws SQLException;
    
    /**
     * The local statement id is intended to identify the statement for internal implementation purposes.
     * <p>
     * NOTE: This method is only included in this interface to try and avoid problems with proxied and bytecode enhanced
     * classes in certain libraries, you should normally have no reason to call this method directly.
     * </p>
     * <p>
     * The id is expected - but not guaranteed - to be unique for a single connection, and - preferably - for
     * the entire JVM run time as well.
     * </p>
     *
     * @return The local statement id.
     */
    @InternalApi
    int getLocalStatementId();
}
