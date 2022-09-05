/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

/**
 * Reasons for statement (or other resources) completion. This is intended for the {@link InternalTransactionCoordinator}
 * to notify the statement and related objects on why it should complete, and for statement to notify its dependent
 * objects.
 * 
 * @since 2.2.3
 */
public enum CompletionReason {
    COMMIT(true, true),
    ROLLBACK(true, true),
    /**
     * Completion was signalled from a statement close.
     *
     * @since 5
     */
    STATEMENT_CLOSE(false, true),
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
