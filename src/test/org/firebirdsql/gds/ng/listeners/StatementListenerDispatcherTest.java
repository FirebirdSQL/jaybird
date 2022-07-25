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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLWarning;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class StatementListenerDispatcherTest {

    private final StatementListenerDispatcher dispatcher = new StatementListenerDispatcher();
    @Mock
    private StatementListener listener;
    @Mock
    private FbStatement statement;

    @BeforeEach
    void setUp() {
        dispatcher.addListener(listener);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#receivedRow(FbStatement, RowValue)} is forwarded correctly.
     */
    @Test
    void testReceivedRow() {
        dispatcher.receivedRow(statement, RowValue.EMPTY_ROW_VALUE);

        verify(listener).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testReceivedRow_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
        doThrow(new RuntimeException("test")).when(listener2).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);

        dispatcher.receivedRow(statement, RowValue.EMPTY_ROW_VALUE);

        verify(listener).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
        verify(listener2).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#beforeFirst(FbStatement)} is forwarded correctly.
     */
    @Test
    void testBeforeFirst() {
        dispatcher.beforeFirst(statement);

        verify(listener).beforeFirst(statement);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testBeforeFirst_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).beforeFirst(statement);
        doThrow(new RuntimeException("test")).when(listener2).beforeFirst(statement);

        dispatcher.beforeFirst(statement);

        verify(listener).beforeFirst(statement);
        verify(listener2).beforeFirst(statement);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#afterLast(FbStatement)} is forwarded correctly.
     */
    @Test
    void testAfterLast() {
        dispatcher.afterLast(statement);

        verify(listener).afterLast(statement);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testAfterLast_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).afterLast(statement);
        doThrow(new RuntimeException("test")).when(listener2).afterLast(statement);

        dispatcher.afterLast(statement);

        verify(listener).afterLast(statement);
        verify(listener2).afterLast(statement);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#statementExecuted(FbStatement, boolean, boolean)}
     * is forwarded correctly.
     */
    @Test
    void testStatementExecuted() {
        dispatcher.statementExecuted(statement, true, false);

        verify(listener).statementExecuted(statement, true, false);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testStatementExecuted_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).statementExecuted(statement, true, false);
        doThrow(new RuntimeException("test")).when(listener2).statementExecuted(statement, true, false);

        dispatcher.statementExecuted(statement, true, false);

        verify(listener).statementExecuted(statement, true, false);
        verify(listener2).statementExecuted(statement, true, false);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#statementStateChanged(FbStatement, StatementState, StatementState)}
     * is forwarded correctly.
     */
    @Test
    void testStatementStateChanged() {
        dispatcher.statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);

        verify(listener).statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testStatementStateChanged_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener)
                .statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
        doThrow(new RuntimeException("test")).when(listener2)
                .statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);

        dispatcher.statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);

        verify(listener).statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
        verify(listener2).statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#warningReceived(FbStatement, SQLWarning)}
     * is forwarded correctly.
     */
    @Test
    void testWarningReceived() {
        final SQLWarning warning = new SQLWarning();

        dispatcher.warningReceived(statement, warning);

        verify(listener).warningReceived(statement, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testWarningReceived_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        doThrow(new RuntimeException("test")).when(listener).warningReceived(statement, warning);
        doThrow(new RuntimeException("test")).when(listener2).warningReceived(statement, warning);

        dispatcher.warningReceived(statement, warning);

        verify(listener).warningReceived(statement, warning);
        verify(listener2).warningReceived(statement, warning);
    }

    /**
     * Test if call to {@link StatementListenerDispatcher#sqlCounts(FbStatement, SqlCountHolder)}
     * is forwarded correctly.
     */
    @Test
    void testSqlCounts() {
        final SqlCountHolder count = new SqlCountHolder(1, 2 ,3, 4);

        dispatcher.sqlCounts(statement, count);

        verify(listener).sqlCounts(statement, count);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testSqlCounts_withException(@Mock StatementListener listener2) {
        dispatcher.addListener(listener2);
        final SqlCountHolder count = new SqlCountHolder(1, 2 ,3, 4);
        doThrow(new RuntimeException("test")).when(listener).sqlCounts(statement, count);
        doThrow(new RuntimeException("test")).when(listener2).sqlCounts(statement, count);

        dispatcher.sqlCounts(statement, count);

        verify(listener).sqlCounts(statement, count);
        verify(listener2).sqlCounts(statement, count);
    }
}
