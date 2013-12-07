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

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.firebirdsql.jdbc.FBSQLException;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * Tests for {@link FBPooledConnection} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBPooledConnectionMock extends MockObjectTestCase {

    /**
     * Two logical connections obtained from a PooledConnection should be
     * distinct.
     * 
     * @throws SQLException
     */
    public void testLogicalConnectionDistinct() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                ignoring(physical);
            }
        });

        Connection logical1 = pooled.getConnection();
        Connection logical2 = pooled.getConnection();

        assertNotSame("Logical connections returned by FBPooledConnection should be different",
                logical1, logical2);
    }

    /**
     * Obtaining a new logical connection should close the old logical
     * connection.
     * 
     * @throws SQLException
     */
    public void testLogicalConnectionClosedOnNew() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                ignoring(physical);
            }
        });

        Connection logical1 = pooled.getConnection();
        // Precondition: logical1 is open
        assertFalse("Logical connection should be open", logical1.isClosed());

        // Obtaining new connection, should close logical1
        Connection logical2 = pooled.getConnection();
        // Postcondition: logical1 closed, logical2 open
        assertTrue("Logical connection should be closed after obtaining new connection",
                logical1.isClosed());
        assertFalse(logical2.isClosed());

        logical2.close();
        pooled.close();
    }

    /**
     * Closing the PooledConnection should close the logical connection
     * 
     * @throws SQLException
     */
    public void testClosingPooledClosesLogical() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                ignoring(physical);
            }
        });

        Connection logical = pooled.getConnection();
        // Precondition: logical is open
        assertFalse("Logical connection should be open", logical.isClosed());

        pooled.close();
        // Postcondition: logical is closed
        assertTrue("Logical connection should be closed if pooled connection is closed",
                logical.isClosed());
    }

    /**
     * Closing the PooledConnection should close the physical connection
     * 
     * @throws SQLException
     */
    public void testClosingPooledClosesPhysical() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                oneOf(physical).close();
            }
        });

        pooled.close();
    }

    /**
     * Explicitly closing a logical connection should fire a connectionClosed to
     * the listener with the PooledConnection as source.
     * 
     * @throws SQLException
     */
    public void testClosingLogicalFiresConnectionClosed() throws SQLException {
        final Connection physical = mock(Connection.class);
        final FBPooledConnection pooled = new FBPooledConnection(physical);
        final ConnectionEventListener cel = mock(ConnectionEventListener.class);
        pooled.addConnectionEventListener(cel);

        checking(new Expectations() {
            {
                ignoring(physical);
                oneOf(cel).connectionClosed(
                        with(new ConnectionEventMatcher(pooled, aNull(SQLException.class))));
            }
        });

        Connection logical = pooled.getConnection();
        logical.close();
    }

    /**
     * Closing a logical connection by obtaining a new logical connection should
     * not fire a connectionClosed to the listener
     * 
     * @throws SQLException
     */
    @SuppressWarnings("unused")
    public void testClosingLogicalByObtainingNewDoesNotFireConnectionClosed() throws SQLException {
        final Connection physical = mock(Connection.class);
        final FBPooledConnection pooled = new FBPooledConnection(physical);
        final ConnectionEventListener cel = mock(ConnectionEventListener.class);
        pooled.addConnectionEventListener(cel);

        checking(new Expectations() {
            {
                ignoring(physical);
                never(cel).connectionClosed(with(any(ConnectionEvent.class)));
            }
        });

        Connection logical1 = pooled.getConnection();
        Connection logical2 = pooled.getConnection();
    }

    /**
     * Closing the PooledConnection should not fire a connectionClosed to the
     * listener.
     * 
     * @throws SQLException
     */
    @SuppressWarnings("unused")
    public void testClosingPooledDoesNotFireConnectionClosed() throws SQLException {
        final Connection physical = mock(Connection.class);
        final FBPooledConnection pooled = new FBPooledConnection(physical);
        final ConnectionEventListener cel = mock(ConnectionEventListener.class);
        pooled.addConnectionEventListener(cel);

        checking(new Expectations() {
            {
                ignoring(physical);
                never(cel).connectionClosed(with(any(ConnectionEvent.class)));
            }
        });

        Connection logical = pooled.getConnection();
        pooled.close();
    }

    /**
     * A fatal SQLException thrown during getConnection should fire a
     * connectionErrorOccurred event.
     * 
     * @throws SQLException
     */
    public void testFatalExceptionFiresConnectionErrorOccurred() throws SQLException {
        final Connection physical = mock(Connection.class);
        final FBPooledConnection pooled = new FBPooledConnection(physical);
        final ConnectionEventListener cel = mock(ConnectionEventListener.class);
        pooled.addConnectionEventListener(cel);

        checking(new Expectations() {
            {
                oneOf(physical).setAutoCommit(true);
                will(throwException(new FBSQLException("Mock Exception",
                        FBSQLException.SQL_STATE_CONNECTION_FAILURE)));
                oneOf(cel).connectionErrorOccurred(
                        with(new ConnectionEventMatcher(pooled, aNonNull(SQLException.class))));
            }
        });

        try {
            pooled.getConnection();
            fail("Expected an SQLException");
        } catch (SQLException ex) {
            // expected
        }
    }

    /**
     * Obtaining a logical connection when the PooledConnection has been closed
     * should throw an SQLException with SQLstate 08003.
     * 
     * @throws SQLException
     */
    public void testGetConnectionWhenClosed() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                ignoring(physical);
            }
        });

        pooled.close();

        try {
            pooled.getConnection();
            fail("Obtaining connection from closed PooledConnection should throw exception");
        } catch (SQLException e) {
            assertEquals("Unexpected SQLState value for Exception",
                    FBSQLException.SQL_STATE_CONNECTION_CLOSED, e.getSQLState());
        }
    }

    /**
     * When a logical connection is obtained, the physical connection should be
     * reset to auto commit.
     * 
     * @throws SQLException
     */
    public void testGetConnectionRestoresAutoCommit() throws SQLException {
        final Connection physical = mock(Connection.class);
        FBPooledConnection pooled = new FBPooledConnection(physical);

        checking(new Expectations() {
            {
                oneOf(physical).setAutoCommit(true);
            }
        });

        pooled.getConnection();
    }
}
