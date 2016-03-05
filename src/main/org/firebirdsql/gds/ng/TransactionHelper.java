/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.jdbc.SQLStateConstants;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

/**
 * Class with static helper methods for use with transactions
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TransactionHelper {

    public static final String NO_TRANSACTION_ACTIVE = "No transaction or transaction not ACTIVE";

    private TransactionHelper() {
    }

    /**
     * Checks if this statement has a transaction and that the transaction is {@link TransactionState#ACTIVE}.
     *
     * @param transaction
     *         The transaction to check
     * @throws SQLException
     *         When this statement does not have a transaction, or if that transaction is not active.
     */
    public static void checkTransactionActive(final FbTransaction transaction) throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            throw new SQLNonTransientException(NO_TRANSACTION_ACTIVE, SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
        }
    }

    /**
     * Checks if this statement has a transaction and that the transaction is {@link TransactionState#ACTIVE}.
     *
     * @param transaction
     *         The transaction to check
     * @param fbErrorCode
     *         Firebird error code to use for generating the exception message
     * @throws SQLException
     *         When this statement does not have a transaction, or if that transaction is not active.
     */
    public static void checkTransactionActive(final FbTransaction transaction, final int fbErrorCode)
            throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            throw new FbExceptionBuilder().nonTransientException(fbErrorCode).toSQLException();
        }
    }
}
