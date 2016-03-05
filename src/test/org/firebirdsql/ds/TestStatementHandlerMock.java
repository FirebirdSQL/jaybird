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
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlStateEquals;
import static org.junit.Assert.*;

/**
 * Tests for {@link StatementHandler} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestStatementHandlerMock {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        context.setThreadingPolicy(new Synchroniser());
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Closing the statement proxy for a second time should not throw an
     * exception.
     * 
     * @throws SQLException
     */
    @Test
    public void testProxyDoubleClose_allowed() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        context.checking(new Expectations() {
            {
                ignoring(conHandler);
                ignoring(statement);
            }
        });

        Statement proxy = handler.getProxy();
        proxy.close();
        // Expectation: no exception for double close
        proxy.close();
    }

    /**
     * Calling any Statement method (except isClosed() and close()) on a proxy
     * that was closed itself should throw an SQLException mentioning the
     * statement was closed; the owner should not be notified of the exception.
     * 
     * TODO: Consider testing for all Connection methods
     * 
     * @throws SQLException
     */
    @Test
    public void testClosedProxy_throwsException() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        context.checking(new Expectations() {
            {
                oneOf(statement).close();
                oneOf(conHandler).forgetStatement(handler);
                never(conHandler).statementErrorOccurred(with(equal(handler)),
                        with(any(SQLException.class)));
            }
        });

        Statement proxy = handler.getProxy();
        proxy.close();

        expectedException.expect(SQLException.class);
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID));

        proxy.getFetchSize();
    }

    /**
     * Calling a Statement method on an open proxy should notify the owner of
     * the occurrence of an exception.
     * 
     * @throws SQLException
     */
    @Test
    public void testException_notify() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);
        final Sequence exceptionSequence = context.sequence("exceptionSequence");

        context.checking(new Expectations() {
            {
                SQLException ex = new FBSQLException("Mock Exception");
                oneOf(statement).getFetchSize();
                will(throwException(ex));
                inSequence(exceptionSequence);
                oneOf(conHandler).statementErrorOccurred(handler, ex);
                inSequence(exceptionSequence);
            }
        });

        Statement proxy = handler.getProxy();

        expectedException.expect(SQLException.class);

        proxy.getFetchSize();
    }

    @Test
    public void testStatementGetConnection_IsProxyConnection() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final Connection connectionProxy = context.mock(Connection.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);
        
        context.checking(new Expectations() {
            {
                oneOf(conHandler).getProxy(); will(returnValue(connectionProxy));
            }
        });
        
        Statement proxy = handler.getProxy();
        
        Connection con = proxy.getConnection();
        assertSame("Statement.getConnection should return the Connection-proxy of the PooledConnectionHandler", connectionProxy, con);
    }
    
    /**
     * The isClosed() method of the StatementHandler and its proxy should return
     * <code>true</code> after closing the Handler.
     * <p>
     * As a secondary test, checks 1) if the wrapped statement is closed and 2)
     * if owner is notified of statement close.
     * </p>
     * 
     * @throws SQLException
     */
    @Test
    public void testHandlerClose_IsClosed() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        context.checking(new Expectations() {
            {
                oneOf(statement).close();
                oneOf(conHandler).forgetStatement(handler);
            }
        });

        Statement proxy = handler.getProxy();
        handler.close();
        assertTrue("Closed handler should report isClosed() true", handler.isClosed());
        assertTrue("Proxy of closed handler should report isClosed() true", proxy.isClosed());
    }

    /**
     * The isClosed() method of the StatementHandler and its proxy should return
     * <code>true</code> after closing the proxy.
     * <p>
     * As a secondary test, checks 1) if the wrapped statement is closed and 2)
     * if owner is notified of statement close.
     * </p>
     * 
     * @throws SQLException
     */
    @Test
    public void testProxyClose_IsClosed() throws SQLException {
        final PooledConnectionHandler conHandler = context.mock(PooledConnectionHandler.class);
        final Statement statement = context.mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        context.checking(new Expectations() {
            {
                oneOf(statement).close();
                oneOf(conHandler).forgetStatement(handler);
            }
        });

        Statement proxy = handler.getProxy();
        proxy.close();
        assertTrue("Handler of closed proxy should report isClosed() true", handler.isClosed());
        assertTrue("Closed proxy should report isClosed() true", proxy.isClosed());
    }

}
