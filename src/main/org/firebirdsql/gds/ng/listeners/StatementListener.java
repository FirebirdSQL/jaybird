// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
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
     *         the {@code FbStatement} that called this method
     * @param rowValue
     *         The row values.
     */
    default void receivedRow(FbStatement sender, RowValue rowValue) { }

    /**
     * Method to be notified of the number of rows fetched in a single {@link FbStatement#fetchRows(int)}.
     * <p>
     * This method will not be called for singleton results of statements like {@code EXECUTE PROCEDURE}.
     * </p>
     * <p>
     * When end of cursor is reached, it is undefined whether this method will be called first or
     * {@code afterLast/beforeFirst}. Listeners must be prepared to handle either order.
     * </p>
     *
     * @param sender
     *         the {@code FbStatement} that called this method
     * @param fetchDirection
     *         fetch direction of the completed fetch operation
     * @param rows
     *         number of rows fetched in the completed fetch operation (NOTE: for native implementations, this will
     *         always be {@code 1} or {@code 0} for end of cursor)
     * @since 6
     */
    default void fetchComplete(FbStatement sender, FetchDirection fetchDirection, int rows) { }

    /**
     * Method to be notified when the cursor of a statement is positioned before the first row.
     * <p>
     * When server-side scrolling is used, this method can be called multiple times during the lifetime of a single
     * open cursor. This method may be called even if the cursor is already <i>before-first</i>.
     * </p>
     *
     * @param sender
     *         the {@code FbStatement} that called this method
     * @see #statementExecuted(FbStatement, boolean, boolean)
     * @see #receivedRow(FbStatement, RowValue)
     * @see #afterLast(FbStatement)
     * @since 5
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
     *         the {@code FbStatement} that called this method
     * @see #statementExecuted(FbStatement, boolean, boolean)
     * @see #receivedRow(FbStatement, RowValue)
     * @see #beforeFirst(FbStatement)
     * @since 5
     */
    default void afterLast(FbStatement sender) { }

    /**
     * Method to be notified when a statement has been executed.
     * <p>
     * This event with {@code hasResultSet=true} can be seen as a counterpart of {@link #afterLast(FbStatement)}.
     * </p>
     *
     * @param sender
     *         the {@code FbStatement} that called this method
     * @param hasResultSet
     *         {@code true} there is a result set, {@code false} there is no result set
     * @param hasSingletonResult
     *         {@code true} singleton result, {@code false} statement will produce indeterminate number of rows;
     *         can be ignored when {@code hasResultSet} is {@code false}
     */
    default void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) { }

    /**
     * Method to be notified when the state of a statement has changed.
     *
     * @param sender
     *         the {@code FbStatement} that called this method
     * @param newState
     *         new state of the statement
     * @param previousState
     *         old state of the statement
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
