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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.firebirdsql.gds.ng.TransactionHelper}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class TransactionHelperTest {

    private static final int TEST_ERROR_CODE = ISCConstants.isc_dsql_error;

    @Test
    void testTransactionActiveDefault_Active_noException() throws Exception {
        FbTransaction transaction = setupTransaction(TransactionState.ACTIVE);

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    void testTransactionActiveDefault_null_exception() {
        //noinspection ConstantConditions
        assertDefaultNoTransactionException(() -> TransactionHelper.checkTransactionActive(null));
    }

    @ParameterizedTest
    @EnumSource(names = { "ACTIVE" }, mode = EnumSource.Mode.EXCLUDE)
    void testTransactionActiveDefault_exception(TransactionState transactionState) {
        FbTransaction transaction = setupTransaction(transactionState);
        
        assertDefaultNoTransactionException(()-> TransactionHelper.checkTransactionActive(transaction));
    }

    @Test
    void testTransactionActiveErrorCode_Active_noException() throws Exception {
        FbTransaction transaction = setupTransaction(TransactionState.ACTIVE);

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    void testTransactionActiveErrorCode_null_exception() {
        //noinspection ConstantConditions
        assertErrorCodeNoTransactionException(() -> TransactionHelper.checkTransactionActive(null, TEST_ERROR_CODE));
    }

    @ParameterizedTest
    @EnumSource(names = { "ACTIVE" }, mode = EnumSource.Mode.EXCLUDE)
    void testTransactionActiveErrorCode_exception(TransactionState transactionState) {
        FbTransaction transaction = setupTransaction(transactionState);

        assertErrorCodeNoTransactionException(
                () -> TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE));
    }

    @ParameterizedTest
    @EnumSource(names = { "COMMITTING", "ROLLING_BACK", "PREPARING"})
    void testIsTransactionEnding_true(TransactionState transactionState) {
        FbTransaction transaction = setupTransaction(transactionState);

        assertTrue(TransactionHelper.isTransactionEnding(transaction));
    }

    @ParameterizedTest
    @EnumSource(names = { "COMMITTING", "ROLLING_BACK", "PREPARING"}, mode = EnumSource.Mode.EXCLUDE)
    void testIsTransactionEnding_false(TransactionState transactionState) {
        FbTransaction transaction = setupTransaction(transactionState);

        assertFalse(TransactionHelper.isTransactionEnding(transaction));
    }

    @Test
    void testIsTransactionEnding_nullTransaction_false() {
        assertFalse(TransactionHelper.isTransactionEnding(null));
    }

    private FbTransaction setupTransaction(TransactionState transactionState) {
        FbTransaction transaction = mock(FbTransaction.class);
        when(transaction.getState()).thenReturn(transactionState);
        return transaction;
    }

    private void assertDefaultNoTransactionException(Executable executable) {
        SQLException exception = assertThrows(SQLNonTransientException.class, executable);
        assertThat(exception, allOf(
                message(equalTo(TransactionHelper.NO_TRANSACTION_ACTIVE)),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_TX_STATE))));
    }

    private void assertErrorCodeNoTransactionException(Executable executable) {
        SQLException exception = assertThrows(SQLNonTransientException.class, executable);
        assertThat(exception, allOf(
                errorCode(equalTo(TEST_ERROR_CODE)),
                fbMessageStartsWith(TEST_ERROR_CODE)));
    }
}
