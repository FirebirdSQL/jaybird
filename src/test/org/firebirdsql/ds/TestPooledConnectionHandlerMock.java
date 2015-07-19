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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests for {@link PooledConnectionHandler} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestPooledConnectionHandlerMock {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        context.setThreadingPolicy(new Synchroniser());
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * The isClosed() method of PooledConnectionHandler and its proxy should
     * report <code>true</code> after handler close.
     * 
     * @throws SQLException
     */
    @Test
    public void testHandlerClose_IsClosed() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
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
    @Test
    public void testProxyClose_IsClosed() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
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
    @Test
    public void testHandlerClose_NoNotify() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
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
    @Test
    public void testProxyClose_Notify() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
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
    @Test
    public void testClosedHandler_throwsException() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
                allowing(pooled).releaseConnectionHandler(handler);
                allowing(pooled).fireConnectionClosed();
                never(pooled).fireConnectionError(with(any(SQLException.class)));
            }
        });

        Connection proxy = handler.getProxy();
        handler.close();

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(PooledConnectionHandler.FORCIBLY_CLOSED_MESSAGE);

        proxy.clearWarnings();
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
    @Test
    public void testClosedProxy_throwsException() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
            {
                connectionHandleReleaseExpectations(this, physicalConnection);
                allowing(pooled).releaseConnectionHandler(handler);
                allowing(pooled).fireConnectionClosed();
                never(pooled).fireConnectionError(with(any(SQLException.class)));
            }
        });

        Connection proxy = handler.getProxy();
        proxy.close();

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(PooledConnectionHandler.CLOSED_MESSAGE);

        proxy.clearWarnings();
    }

    /**
     * Calling a Connection method on an open proxy should notify the owner of
     * the occurrence of an exception.
     * 
     * @throws SQLException
     */
    @Test
    public void testException_Notify() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);
        final Sequence exceptionSequence = context.sequence("exceptionSequence");

        context.checking(new Expectations() {
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

        expectedException.expect(SQLException.class);

        proxy.clearWarnings();
    }

    /**
     * Closing a proxy should rollback the physical connection if not in
     * auto-commit.
     * 
     * @throws SQLException
     */
    @Test
    public void testCloseNotAutoCommit_rollback() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);
        final Sequence closeSequence = context.sequence("closeSequence");

        context.checking(new Expectations() {
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
    @Test
    public void testDoubleClose_allowed() throws SQLException {
        final FirebirdConnection physicalConnection = context.mock(FirebirdConnection.class);
        final FBPooledConnection pooled = context.mock(FBPooledConnection.class);
        final PooledConnectionHandler handler = new PooledConnectionHandler(physicalConnection,
                pooled);

        context.checking(new Expectations() {
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

    private void connectionHandleReleaseExpectations(Expectations expectations, FirebirdConnection physicalConnection)
            throws SQLException {
        expectations.allowing(physicalConnection).getAutoCommit();
        expectations.will(Expectations.returnValue(true));
        expectations.allowing(physicalConnection).isWrapperFor(FirebirdConnection.class);
        expectations.will(Expectations.returnValue(true));
        expectations.allowing(physicalConnection).unwrap(FirebirdConnection.class);
        expectations.will(Expectations.returnValue(physicalConnection));
        expectations.allowing(physicalConnection).isUseFirebirdAutoCommit();
        expectations.will(Expectations.returnValue(FBTestProperties.USE_FIREBIRD_AUTOCOMMIT));
        if (FBTestProperties.USE_FIREBIRD_AUTOCOMMIT) {
            expectations.oneOf(physicalConnection).setAutoCommit(false);
            expectations.oneOf(physicalConnection).setAutoCommit(true);
        }
        expectations.allowing(physicalConnection).clearWarnings();
    }
}
