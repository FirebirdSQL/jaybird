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
 * <p>
 * All listener methods have a default implementation that does nothing.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface StatementListener {

    /**
     * Method to be notified of a new row of data.
     * <p>
     * Listeners that process {@link #beforeFirst(FbStatement)} and/or {@link #afterLast(FbStatement)} should consider
     * calls to this method to clear the <i>before-first</i> or <i>after-last</i> state to an <li>in-cursor</li> state.
     * </p>
     *
     * @param sender
     *         The {@code FbStatement}that called this method.
     * @param rowValue
     *         The row values.
     */
    default void receivedRow(FbStatement sender, RowValue rowValue) { }

    /**
     * Method to be notified when the cursor of a statement is positioned before the first row.
     * <p>
     * When server-side scrolling is used, this method can be called multiple times during the lifetime of a single
     * open cursor. This method may be called even if the cursor is already <i>before-first</i>.
     * </p>
     *
     * @param sender
     *         The {@code FbStatement} that called this method.
     * @see #statementExecuted(FbStatement, boolean, boolean)
     * @see #receivedRow(FbStatement, RowValue)
     * @see #afterLast(FbStatement)
     */
    default void beforeFirst(FbStatement sender) { }

    /**
     * Method to be notified when the cursor of a statement is positioned after the last row.
     * <p>
     * When server-side scrolling is used, this method might be called multiple times during the lifetime of a single
     * open cursor. This method may be called even if the cursor is already <i>after-last</i>.
     * </p>
     *
     * @param sender
     *         The {@code FbStatement} that called this method.
     * @see #statementExecuted(FbStatement, boolean, boolean)
     * @see #receivedRow(FbStatement, RowValue)
     * @see #beforeFirst(FbStatement)
     */
    default void afterLast(FbStatement sender) { }

    /**
     * Method to be notified when a statement has been executed.
     * <p>
     * This event with {@code hasResultSet=true} can be seen as a counterpart of {@link #afterLast(FbStatement)}.
     * </p>
     *
     * @param sender
     *         The {@code FbStatement} that called this method.
     * @param hasResultSet
     *         {@code true} there is a result set, {@code false} there is no result set
     * @param hasSingletonResult
     *         {@code true} singleton result, {@code false} statement will produce indeterminate number of rows;
     *         can be ignored when {@code hasResultSet} is {@code false}.
     */
    default void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) { }

    /**
     * Method to be notified when the state of a statement has changed.
     *
     * @param sender
     *         The {@code FbStatement} that called this method.
     * @param newState
     *         The new state of the statement
     * @param previousState
     *         The old state of the statement
     */
    default void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) { }

    /**
     * Called when a warning was received for the {@code sender} statement.
     *
     * @param sender
     *         Statement receiving the warning
     * @param warning
     *         Warning
     */
    default void warningReceived(FbStatement sender, SQLWarning warning) { }

    /**
     * Called when the SQL counts of a statement have been retrieved.
     *
     * @param sender
     *         Statement that called this method
     * @param sqlCounts
     *         SQL counts
     */
    default void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) { }
}
