/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;

import java.sql.SQLWarning;

/**
 * Listener interface for receiving rows and related information as retrieved by
 * an {@link org.firebirdsql.gds.ng.FbStatement#fetchRows(int)}, or
 * {@link org.firebirdsql.gds.ng.FbStatement#execute(RowValue)} with a singleton result.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface StatementListener {

    /**
     * Method to be notified of a new row of data.
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @param rowValue
     *         The row values.
     */
    void receivedRow(FbStatement sender, RowValue rowValue);

    /**
     * Method to be notified when all rows have been fetched.
     * <p>
     * This method may also be called when the statement did not produce any rows (or did not open a result set).
     * </p>
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @see #statementExecuted(FbStatement, boolean, boolean)
     */
    void allRowsFetched(FbStatement sender);

    /**
     * Method to be notified when a statement has been executed.
     * <p>
     * This event with <code>hasResultSet=true</code> can be seen as the counter part of {@link #allRowsFetched(FbStatement)}.
     * </p>
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @param hasResultSet
     *         <code>true</code> there is a result set, <code>false</code> there is no result set
     * @param hasSingletonResult
     *         <code>true</code> singleton result, <code>false</code> statement will produce indeterminate number of rows;
     *         can be ignored when <code>hasResultSet</code> is false.
     */
    void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult);

    /**
     * Method to be notified when the state of a statement has changed.
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @param newState
     *         The new state of the statement
     * @param previousState
     *         The old state of the statement
     */
    void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState);

    /**
     * Called when a warning was received for the <code>sender</code> statement.
     *
     * @param sender
     *         Statement receiving the warning
     * @param warning
     *         Warning
     */
    void warningReceived(FbStatement sender, SQLWarning warning);

    /**
     * Called when the SQL counts of a statement have been retrieved.
     *
     * @param sender
     *         Statement that called this method
     * @param sqlCounts
     *         SQL counts
     */
    void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts);
}
