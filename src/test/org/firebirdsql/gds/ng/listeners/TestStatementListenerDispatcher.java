/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLWarning;
import java.util.Arrays;

import static org.jmock.Expectations.throwException;

/**
 * Tests for {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestStatementListenerDispatcher {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private StatementListenerDispatcher dispatcher;
    private StatementListener listener;
    private FbStatement statement;

    @Before
    public void setUp() {
        dispatcher = new StatementListenerDispatcher();
        listener = context.mock(StatementListener.class, "listener");
        dispatcher.addListener(listener);
        statement = context.mock(FbStatement.class, "statement");
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#receivedRow(org.firebirdsql.gds.ng.FbStatement, org.firebirdsql.gds.ng.fields.RowValue)}
     * is forwarded correctly.
     */
    @Test
    public void testReceivedRow() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
        context.checking(expectations);

        dispatcher.receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testReceivedRow_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.receivedRow(statement, RowValue.EMPTY_ROW_VALUE);
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#allRowsFetched(org.firebirdsql.gds.ng.FbStatement)}
     * is forwarded correctly.
     */
    @Test
    public void testAllRowsFetched() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).allRowsFetched(statement);
        context.checking(expectations);

        dispatcher.allRowsFetched(statement);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testAllRowsFetched_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).allRowsFetched(statement);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.allRowsFetched(statement);
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#statementExecuted(org.firebirdsql.gds.ng.FbStatement, boolean, boolean)}
     * is forwarded correctly.
     */
    @Test
    public void testStatementExecuted() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).statementExecuted(statement, true, false);
        context.checking(expectations);

        dispatcher.statementExecuted(statement, true, false);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testStatementExecuted_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).statementExecuted(statement, true, false);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.statementExecuted(statement, true, false);
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#statementStateChanged(org.firebirdsql.gds.ng.FbStatement, org.firebirdsql.gds.ng.StatementState, org.firebirdsql.gds.ng.StatementState)}
     * is forwarded correctly.
     */
    @Test
    public void testStatementStateChanged() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
        context.checking(expectations);

        dispatcher.statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testStatementStateChanged_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.statementStateChanged(statement, StatementState.CLOSED, StatementState.CLOSING);
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#warningReceived(org.firebirdsql.gds.ng.FbStatement, java.sql.SQLWarning)}
     * is forwarded correctly.
     */
    @Test
    public void testWarningReceived() {
        final Expectations expectations = new Expectations();
        final SQLWarning warning = new SQLWarning();
        expectations.exactly(1).of(listener).warningReceived(statement, warning);
        context.checking(expectations);

        dispatcher.warningReceived(statement, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testWarningReceived_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).warningReceived(statement, warning);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.warningReceived(statement, warning);
    }

    /**
     * Test if call to {@link org.firebirdsql.gds.ng.listeners.StatementListenerDispatcher#sqlCounts(org.firebirdsql.gds.ng.FbStatement, org.firebirdsql.gds.ng.SqlCountHolder)}
     * is forwarded correctly.
     */
    @Test
    public void testSqlCounts() {
        final Expectations expectations = new Expectations();
        final SqlCountHolder count = new SqlCountHolder(1, 2 ,3, 4);
        expectations.exactly(1).of(listener).sqlCounts(statement, count);
        context.checking(expectations);

        dispatcher.sqlCounts(statement, count);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testSqlCounts_withException() {
        final StatementListener listener2 = context.mock(StatementListener.class, "listener2");
        dispatcher.addListener(listener2);
        final SqlCountHolder count = new SqlCountHolder(1, 2 ,3, 4);
        final Expectations expectations = new Expectations();
        for (StatementListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).sqlCounts(statement, count);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.sqlCounts(statement, count);
    }
}
