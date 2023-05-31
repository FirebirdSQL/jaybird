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
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlStateEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link StatementHandler} using mocks.
 * 
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class StatementHandlerMockTest {

    @Mock
    private PooledConnectionHandler conHandler;
    @Mock
    private Statement statement;
    @InjectMocks
    private StatementHandler handler;

    /**
     * Closing the statement proxy for a second time should not throw an exception.
     */
    @Test
    void testProxyDoubleClose_allowed() throws SQLException {
        Statement proxy = handler.getProxy();
        proxy.close();
        // Expectation: no exception for double close
        assertDoesNotThrow(proxy::close);
    }

    /**
     * Calling any Statement method (except isClosed() and close()) on a proxy
     * that was closed itself should throw an SQLException mentioning the
     * statement was closed; the owner should not be notified of the exception.
     */
    // TODO: Consider testing for all Connection methods
    @Test
    void testClosedProxy_throwsException() throws SQLException {
        Statement proxy = handler.getProxy();
        proxy.close();
        verify(statement).close();
        verify(conHandler).forgetStatement(handler);

        SQLException exception = assertThrows(SQLException.class, proxy::getFetchSize);
        assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID));
        verify(conHandler, never()).statementErrorOccurred(eq(handler), any(SQLException.class));
    }

    /**
     * Calling a Statement method on an open proxy should notify the owner of the occurrence of an exception.
     */
    @Test
    void testException_notify() throws SQLException {
        Statement proxy = handler.getProxy();
        SQLException testException = new FBSQLException("Mock Exception");
        when(statement.getFetchSize()).thenThrow(testException);

        SQLException exception = assertThrows(SQLException.class, proxy::getFetchSize);
        assertSame(testException, exception);
        verify(conHandler).statementErrorOccurred(handler, testException);
    }

    @Test
    void testStatementGetConnection_IsProxyConnection(@Mock Connection connectionProxy) throws SQLException {
        Statement proxy = handler.getProxy();
        when(conHandler.getProxy()).thenReturn(connectionProxy);

        Connection con = proxy.getConnection();
        assertSame(connectionProxy, con,
                "Statement.getConnection should return the Connection-proxy of the PooledConnectionHandler");
    }

    /**
     * The isClosed() method of the StatementHandler and its proxy should return {@code true} after closing the Handler.
     * <p>
     * As a secondary test, checks 1) if the wrapped statement is closed and 2) if owner is notified of statement close.
     * </p>
     */
    @Test
    void testHandlerClose_IsClosed() throws SQLException {
        Statement proxy = handler.getProxy();
        handler.close();
        assertTrue(handler.isClosed(), "Closed handler should report isClosed() true");
        assertTrue(proxy.isClosed(), "Proxy of closed handler should report isClosed() true");
        verify(statement).close();
        verify(conHandler).forgetStatement(handler);
    }

    /**
     * The isClosed() method of the StatementHandler and its proxy should return {@code true} after closing the proxy.
     * <p>
     * As a secondary test, checks 1) if the wrapped statement is closed and 2) if owner is notified of statement close.
     * </p>
     */
    @Test
    void testProxyClose_IsClosed() throws SQLException {
        Statement proxy = handler.getProxy();
        proxy.close();
        assertTrue(handler.isClosed(), "Handler of closed proxy should report isClosed() true");
        assertTrue(proxy.isClosed(), "Closed proxy should report isClosed() true");
        verify(statement).close();
        verify(conHandler).forgetStatement(handler);
    }

}
