// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Class with static helper methods for use with transactions
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class TransactionHelper {

    private TransactionHelper() {
        // no instances
    }

    /**
     * Checks if the transaction is {@link TransactionState#ACTIVE}.
     *
     * @param transaction
     *         transaction to check
     * @throws SQLException
     *         when {@code transaction} is {@code null}, or its state is not active
     */
    public static void checkTransactionActive(@Nullable FbTransaction transaction) throws SQLException {
        checkTransactionActive(transaction, JaybirdErrorCodes.jb_noActiveTransaction);
    }

    /**
     * Checks if the transaction is {@link TransactionState#ACTIVE}.
     *
     * @param transaction
     *         transaction to check
     * @param fbErrorCode
     *         Firebird error code to use for generating the exception message
     * @throws SQLException
     *         when {@code transaction} is {@code null}, or its state is not active
     */
    public static void checkTransactionActive(@Nullable FbTransaction transaction, final int fbErrorCode) throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            throw FbExceptionBuilder.toNonTransientException(fbErrorCode);
        }
    }

    /**
     * Checks if the transaction is ending (meaning its state is {@link TransactionState#COMMITTING},
     * {@link TransactionState#ROLLING_BACK} or {@link TransactionState#PREPARING}).
     *
     * @param transaction
     *         transaction to check
     * @return {@code true} if the state is {@code COMMITTING}, {@code ROLLING_BACK} or {@code PREPARING}, otherwise
     * {@code false} (including when {@code transaction} is {@code null})
     * @since 6
     */
    public static boolean isTransactionEnding(@Nullable FbTransaction transaction) {
        if (transaction == null) return false;
        return switch(transaction.getState()) {
            case COMMITTING, ROLLING_BACK, PREPARING -> true;
            default -> false;
        };
    }

}
