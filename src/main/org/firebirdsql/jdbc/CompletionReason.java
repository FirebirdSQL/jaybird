// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

/**
 * Reasons for statement (or other resources) completion. This is intended for the {@link InternalTransactionCoordinator}
 * to notify the statement and related objects on why it should complete, and for statement to notify its dependent
 * objects.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 * 
 * @since 2.2.3
 */
@InternalApi
public enum CompletionReason {
    COMMIT(true, true),
    ROLLBACK(true, true),
    /**
     * Completion was signalled from a statement close.
     *
     * @since 5
     */
    STATEMENT_CLOSE(false, true),
    /**
     * Completion was signalled from a connection abort.
     *
     * @since 6
     */
    CONNECTION_ABORT(true, true),
    OTHER(false, false);

    private final boolean transactionEnd;
    private final boolean completesStatement;

    CompletionReason(boolean transactionEnd, boolean completesStatement) {
        this.transactionEnd = transactionEnd;
        this.completesStatement = completesStatement;
    }

    /**
     * @return {@code true} if this completion indicates a transaction end
     */
    final boolean isTransactionEnd() {
        return transactionEnd;
    }

    /**
     * @return {@code true} if this completion will automatically complete a statement (as in close the cursor)
     */
    final boolean isCompletesStatement() {
        return completesStatement;
    }
}
