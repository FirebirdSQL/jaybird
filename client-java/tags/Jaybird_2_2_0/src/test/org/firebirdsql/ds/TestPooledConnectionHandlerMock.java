/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;

import org.firebirdsql.jdbc.FBSQLException;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Tests for {@link PooledConnectionHandler} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestPooledConnectionHandlerMock extends MockObjectTestCase {

    {
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * The isClosed() method of PooledConnectionHandler and its proxy should
     * report <code>true</code> after handler close.
     * 
     * @throws SQLException
     */
    public void testHandlerClose_IsClosed() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
            }
        });

        Connection proxy = handler.getProxy();
        handler.close();
        assertTrue("Closed handler should report isClosed() true", handler.isClosed());
        assertTrue("Proxy of closed handler should report isClosed() true", proxy.isClosed());
    }

    /**
     * The isClosed() method of PooledConnectionHandler and its proxy should
     * report <code>true</code> after proxy close.
     * 
     * @throws SQLException
     */
    public void testProxyClose_IsClosed() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
                allowing(pooled).fireConnectionClosed();
            }
        });

        Connection proxy = handler.getProxy();
        proxy.close();
        assertTrue("Handler of closed proxy should report isClosed() true", handler.isClosed());
        assertTrue("Closed proxy should report isClosed() true", proxy.isClosed());
    }

    /**
     * Closing the PooledConnectionHandler should not notify the owner and not
     * close the physical connection.
     * 
     * @throws SQLException
     */
    public void testHandlerClose_NoNotify() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
                never(physicalConnection).close();
                never(pooled).fireConnectionClosed();
            }
        });

        handler.close();
    }

    /**
     * Closing the Proxy of the PooledConnectionHandler should notify the owner
     * but not close the physical connection.
     * 
     * @throws SQLException
     */
    public void testProxyClose_Notify() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
                never(physicalConnection).close();
                oneOf(pooled).fireConnectionClosed();
            }
        });

        handler.getProxy().close();
    }

    /**
     * Calling any Connection method (except isClosed() and close()) on a proxy
     * that was closed by closing the handler should throw an SQLException
     * mentioning the connection was forcibly closed; the owner should not be
     * notified of the exception.
     * 
     * TODO: Consider testing for all Connection methods
     * 
     * @throws SQLException
     */
    public void testClosedHandler_throwsException() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
                allowing(pooled).fireConnectionClosed();
                never(pooled).fireConnectionError(with(any(SQLException.class)));
            }
        });

        Connection proxy = handler.getProxy();
        handler.close();

        try {
            proxy.clearWarnings();
            fail("Calling clearWarnings on closed proxy should throw SQLException");
        } catch (SQLException ex) {
            assertEquals("Expected forcibly closed message",
                    PooledConnectionHandler.FORCIBLY_CLOSED_MESSAGE, ex.getMessage());
        }
    }

    /**
     * Calling any Connection method (except isClosed() and close()) on a proxy
     * that was closed itself should throw an SQLException mentioning the
     * connection was closed; the owner should not be notified of the exception.
     * 
     * TODO: Consider testing for all Connection methods
     * 
     * @throws SQLException
     */
    public void testClosedProxy_throwsException() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                allowing(physicalConnection).getAutoCommit();
                will(returnValue(true));
                allowing(physicalConnection).clearWarnings();
                allowing(pooled).releaseConnectionHandler(handler);
                allowing(pooled).fireConnectionClosed();
                never(pooled).fireConnectionError(with(any(SQLException.class)));
            }
        });

        Connection proxy = handler.getProxy();
        proxy.close();

        try {
            proxy.clearWarnings();
            fail("Calling clearWarnings on closed proxy should throw SQLException");
        } catch (SQLException ex) {
            assertEquals("Expected normal closed message", PooledConnectionHandler.CLOSED_MESSAGE,
                    ex.getMessage());
        }
    }

    /**
     * Calling a Connection method on an open proxy should notify the owner of
     * the occurrence of an exception.
     * 
     * @throws SQLException
     */
    public void testException_Notify() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);
        final Sequence exceptionSequence = sequence("exceptionSequence");

        checking(new Expectations() {
            {
                SQLException sqle = new FBSQLException("Mock Exception");
                oneOf(physicalConnection).clearWarnings();
                will(throwException(sqle));
                inSequence(exceptionSequence);
                oneOf(pooled).fireConnectionError(sqle);
                inSequence(exceptionSequence);

            }
        });

        Connection proxy = handler.getProxy();

        try {
            proxy.clearWarnings();
            fail("Expected test exception to be thrown");
        } catch (SQLException ex) {
            // ignore: exception expected
        }
    }

    /**
     * Closing a proxy should rollback the physical connection if not in
     * auto-commit.
     * 
     * @throws SQLException
     */
    public void testCloseNotAutoCommit_rollback() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);
        final Sequence closeSequence = sequence("closeSequence");

        checking(new Expectations() {
            {
                oneOf(physicalConnection).getAutoCommit();
                will(returnValue(false));
                inSequence(closeSequence);
                oneOf(physicalConnection).rollback();
                inSequence(closeSequence);
                allowing(physicalConnection).clearWarnings();
                allowing(pooled);
            }
        });

        Connection proxy = handler.getProxy();
        proxy.close();
    }

    /**
     * Calling close() on a closed proxy should not throw an exception.
     * 
     * @throws SQLException
     */
    public void testDoubleClose_allowed() throws SQLException {
        final Connection physicalConnection = mock(Connection.class);
        final FBPooledConnection pooled = mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        checking(new Expectations() {
            {
                ignoring(physicalConnection);
                ignoring(pooled);
            }
        });

        Connection proxy = handler.getProxy();
        proxy.close();
        // Expectation: no exception for double close
        proxy.close();
    }
}
