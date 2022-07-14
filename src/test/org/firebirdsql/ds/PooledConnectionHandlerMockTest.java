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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PooledConnectionHandler} using mocks.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@ExtendWith(MockitoExtension.class)
class PooledConnectionHandlerMockTest {

    @Mock
    private FirebirdConnection physicalConnection;
    @Mock
    private FBPooledConnection pooled;
    @InjectMocks
    private PooledConnectionHandler handler;

    /**
     * The isClosed() method of PooledConnectionHandler and its proxy should report {@code true} after handler close.
     */
    @Test
    void testHandlerClose_IsClosed() throws SQLException {
        setupConnectionHandleRelease();

        Connection proxy = handler.getProxy();
        handler.close();
        assertTrue(handler.isClosed(), "Closed handler should report isClosed() true");
        assertTrue(proxy.isClosed(), "Proxy of closed handler should report isClosed() true");
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
    }

    /**
     * The isClosed() method of PooledConnectionHandler and its proxy should report {@code true} after proxy close.
     */
    @Test
    void testProxyClose_IsClosed() throws SQLException {
        setupConnectionHandleRelease();

        Connection proxy = handler.getProxy();
        proxy.close();
        assertTrue(handler.isClosed(), "Handler of closed proxy should report isClosed() true");
        assertTrue(proxy.isClosed(), "Closed proxy should report isClosed() true");
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
        verify(pooled).fireConnectionClosed();
    }

    /**
     * Closing the PooledConnectionHandler should not notify the owner and not close the physical connection.
     */
    @Test
    void testHandlerClose_NoNotify() throws SQLException {
        setupConnectionHandleRelease();

        handler.close();
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
        verify(physicalConnection, never()).close();
        verify(pooled, never()).fireConnectionClosed();
    }

    /**
     * Closing the Proxy of the PooledConnectionHandler should notify the owner but not close the physical connection.
     */
    @Test
    void testProxyClose_Notify() throws SQLException {
        setupConnectionHandleRelease();

        handler.getProxy().close();
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
        verify(physicalConnection, never()).close();
        verify(pooled).fireConnectionClosed();
    }

    /**
     * Calling any Connection method (except isClosed() and close()) on a proxy
     * that was closed by closing the handler should throw an SQLException
     * mentioning the connection was forcibly closed; the owner should not be
     * notified of the exception.
     *
     * TODO: Consider testing for all Connection methods
     */
    @Test
    void testClosedHandler_throwsException() throws SQLException {
        setupConnectionHandleRelease();

        Connection proxy = handler.getProxy();
        handler.close();
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
        verify(pooled, atMostOnce()).fireConnectionClosed();

        SQLException exception = assertThrows(SQLException.class, proxy::clearWarnings);
        assertThat(exception, message(equalTo(PooledConnectionHandler.FORCIBLY_CLOSED_MESSAGE)));
        verify(pooled, never()).fireConnectionError(any(SQLException.class));
    }

    /**
     * Calling any Connection method (except isClosed() and close()) on a proxy
     * that was closed itself should throw an SQLException mentioning the
     * connection was closed; the owner should not be notified of the exception.
     *
     * TODO: Consider testing for all Connection methods
     */
    @Test
    void testClosedProxy_throwsException() throws SQLException {
        setupConnectionHandleRelease();

        Connection proxy = handler.getProxy();
        proxy.close();
        verifyConnectionHandleRelease();
        verify(pooled).releaseConnectionHandler(handler);
        verify(pooled, atMostOnce()).fireConnectionClosed();

        SQLException exception = assertThrows(SQLException.class, proxy::clearWarnings);
        assertThat(exception, message(equalTo(PooledConnectionHandler.CLOSED_MESSAGE)));
        verify(pooled, never()).fireConnectionError(any(SQLException.class));
    }

    /**
     * Calling a Connection method on an open proxy should notify the owner of the occurrence of an exception.
     */
    @Test
    void testException_Notify() throws SQLException {
        SQLException sqle = new FBSQLException("Mock Exception");

        Connection proxy = handler.getProxy();
        doThrow(sqle).when(physicalConnection).clearWarnings();

        SQLException exception = assertThrows(SQLException.class, proxy::clearWarnings);
        assertSame(sqle, exception);
        verify(pooled).fireConnectionError(sqle);
    }

    /**
     * Closing a proxy should roll back the physical connection if not in auto-commit.
     */
    @Test
    void testCloseNotAutoCommit_rollback() throws SQLException {
        when(physicalConnection.getAutoCommit()).thenReturn(false);

        Connection proxy = handler.getProxy();
        proxy.close();

        verify(physicalConnection).rollback();
        verify(physicalConnection, atMostOnce()).clearWarnings();
    }

    /**
     * Calling close() on a closed proxy should not throw an exception.
     */
    @Test
    void testDoubleClose_allowed() throws SQLException {
        Connection proxy = handler.getProxy();
        proxy.close();
        // Expectation: no exception for double close
        assertDoesNotThrow(proxy::close);
    }

    private void setupConnectionHandleRelease() throws SQLException {
        lenient().when(physicalConnection.getAutoCommit()).thenReturn(true);
        lenient().when(physicalConnection.isWrapperFor(FirebirdConnection.class)).thenReturn(true);
        lenient().when(physicalConnection.unwrap(FirebirdConnection.class)).thenReturn(physicalConnection);
        lenient().when(physicalConnection.isUseFirebirdAutoCommit())
                .thenReturn(FBTestProperties.USE_FIREBIRD_AUTOCOMMIT);
    }

    private void verifyConnectionHandleRelease() throws SQLException {
        if (FBTestProperties.USE_FIREBIRD_AUTOCOMMIT) {
            InOrder inOrder = inOrder(physicalConnection);
            inOrder.verify(physicalConnection).setAutoCommit(true);
            inOrder.verify(physicalConnection).setAutoCommit(false);
        }

        verify(physicalConnection).clearWarnings();
    }
}
