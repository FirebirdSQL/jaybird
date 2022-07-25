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

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlStateEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link FBPooledConnection} using mocks.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@ExtendWith(MockitoExtension.class)
class FBPooledConnectionMockTest {

    @Mock
    private Connection physical;

    @InjectMocks
    private FBPooledConnection pooled;

    /**
     * Two logical connections obtained from a PooledConnection should be distinct.
     */
    @Test
    void testLogicalConnectionDistinct() throws SQLException {
        Connection logical1 = pooled.getConnection();
        Connection logical2 = pooled.getConnection();

        assertNotSame(logical1, logical2, "Logical connections returned by FBPooledConnection should be different");
    }

    /**
     * Obtaining a new logical connection should close the old logical connection.
     */
    @Test
    void testLogicalConnectionClosedOnNew() throws SQLException {
        Connection logical1 = pooled.getConnection();
        // Precondition: logical1 is open
        assertFalse(logical1.isClosed(), "Logical connection should be open");

        // Obtaining new connection, should close logical1
        Connection logical2 = pooled.getConnection();
        // Postcondition: logical1 closed, logical2 open
        assertTrue(logical1.isClosed(), "Logical connection should be closed after obtaining new connection");
        assertFalse(logical2.isClosed());

        logical2.close();
        pooled.close();
    }

    /**
     * Closing the PooledConnection should close the logical connection
     */
    @Test
    void testClosingPooledClosesLogical() throws SQLException {
        Connection logical = pooled.getConnection();
        // Precondition: logical is open
        assertFalse(logical.isClosed(), "Logical connection should be open");

        pooled.close();
        // Postcondition: logical is closed
        assertTrue(logical.isClosed(), "Logical connection should be closed if pooled connection is closed");
    }

    /**
     * Closing the PooledConnection should close the physical connection
     */
    @Test
    void testClosingPooledClosesPhysical() throws SQLException {
        pooled.close();

        verify(physical).close();
    }

    /**
     * Explicitly closing a logical connection should fire a connectionClosed to
     * the listener with the PooledConnection as source.
     */
    @Test
    void testClosingLogicalFiresConnectionClosed(@Mock ConnectionEventListener cel) throws SQLException {
        pooled.addConnectionEventListener(cel);

        Connection logical = pooled.getConnection();
        logical.close();

        verify(cel).connectionClosed(argThat(new ConnectionEventMatcher(pooled, nullValue(SQLException.class))));
    }

    /**
     * Closing a logical connection by obtaining a new logical connection should
     * not fire a connectionClosed to the listener
     */
    @SuppressWarnings("unused")
    @Test
    void testClosingLogicalByObtainingNewDoesNotFireConnectionClosed(@Mock ConnectionEventListener cel)
            throws SQLException {
        pooled.addConnectionEventListener(cel);

        Connection logical1 = pooled.getConnection();
        Connection logical2 = pooled.getConnection();
        verify(cel, never()).connectionClosed(any(ConnectionEvent.class));
    }

    /**
     * Closing the PooledConnection should not fire a connectionClosed to the listener.
     */
    @SuppressWarnings("unused")
    @Test
    void testClosingPooledDoesNotFireConnectionClosed(@Mock ConnectionEventListener cel) throws SQLException {
        pooled.addConnectionEventListener(cel);

        Connection logical = pooled.getConnection();
        pooled.close();
        verify(cel, never()).connectionClosed(any(ConnectionEvent.class));
    }

    /**
     * A fatal SQLException thrown during getConnection should fire a connectionErrorOccurred event.
     */
    @Test
    void testFatalExceptionFiresConnectionErrorOccurred(@Mock ConnectionEventListener cel) throws SQLException {
        pooled.addConnectionEventListener(cel);

        doThrow(new FBSQLException("Mock Exception", SQLStateConstants.SQL_STATE_CONNECTION_FAILURE))
                .when(physical).setAutoCommit(true);

        SQLException exception = assertThrows(SQLException.class, () -> pooled.getConnection());
        assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_FAILURE));

        verify(cel).connectionErrorOccurred(argThat(new ConnectionEventMatcher(pooled, instanceOf(SQLException.class))));
    }

    /**
     * Obtaining a logical connection when the PooledConnection has been closed
     * should throw an SQLException with SQLstate 08003.
     */
    @Test
    void testGetConnectionWhenClosed() throws SQLException {
        pooled.close();

        SQLException exception = assertThrows(SQLException.class, () -> pooled.getConnection());
        assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_CLOSED));
    }

    /**
     * When a logical connection is obtained, the physical connection should be reset to auto commit.
     */
    @Test
    void testGetConnectionRestoresAutoCommit() throws SQLException {
        pooled.getConnection();

        verify(physical).setAutoCommit(true);
    }
}
