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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBSQLException;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.jmock.Expectations.returnValue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.TransactionHelper}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestTransactionHelper {

    private static final int TEST_ERROR_CODE = ISCConstants.isc_dsql_error;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void testTransactionActiveDefault_Active_noException() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ACTIVE);

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_null_exception() throws Exception {
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(null);
    }

    @Test
    public void testTransactionActiveDefault_Preparing_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.PREPARING);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_Prepared_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.PREPARED);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_Committing_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.COMMITTING);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_Committed_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.COMMITTED);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_RollingBack_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ROLLING_BACK);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveDefault_RolledBack_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ROLLED_BACK);
        expectDefaultNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction);
    }

    @Test
    public void testTransactionActiveErrorCode_Active_noException() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ACTIVE);

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_null_exception() throws Exception {
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(null, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_Preparing_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.PREPARING);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_Prepared_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.PREPARED);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_Committing_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.COMMITTING);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_Committed_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.COMMITTED);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_RollingBack_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ROLLING_BACK);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    @Test
    public void testTransactionActiveErrorCode_RolledBack_exception() throws Exception {
        final FbTransaction transaction = setupTransaction(TransactionState.ROLLED_BACK);
        expectErrorCodeNoTransactionException();

        TransactionHelper.checkTransactionActive(transaction, TEST_ERROR_CODE);
    }

    private FbTransaction setupTransaction(TransactionState transactionState) {
        final FbTransaction transaction = context.mock(FbTransaction.class);
        final Expectations expectations = new Expectations();
        expectations.oneOf(transaction).getState();
        expectations.will(returnValue(transactionState));
        context.checking(expectations);
        return transaction;
    }

    private void expectDefaultNoTransactionException() {
        expectedException.expect(allOf(
                isA(SQLNonTransientException.class),
                message(equalTo(TransactionHelper.NO_TRANSACTION_ACTIVE)),
                sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_TX_STATE))
        ));
    }

    private void expectErrorCodeNoTransactionException() {
        expectedException.expect(allOf(
                isA(SQLNonTransientException.class),
                errorCode(equalTo(TEST_ERROR_CODE)),
                fbMessageStartsWith(TEST_ERROR_CODE)
        ));
    }
}
