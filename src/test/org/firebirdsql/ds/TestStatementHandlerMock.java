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
import java.sql.Statement;

import org.firebirdsql.jdbc.FBSQLException;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Tests for {@link StatementHandler} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestStatementHandlerMock extends MockObjectTestCase {

    {
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Closing the statement proxy for a second time should not throw an
     * exception.
     * 
     * @throws SQLException
     */
    public void testProxyDoubleClose_allowed() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        checking(new Expectations() {
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
    public void testClosedProxy_throwsException() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        checking(new Expectations() {
            {
                oneOf(statement).close();
                oneOf(conHandler).forgetStatement(handler);
                never(conHandler).statementErrorOccurred(with(equal(handler)),
                        with(any(SQLException.class)));
            }
        });

        Statement proxy = handler.getProxy();
        proxy.close();

        try {
            proxy.getFetchSize();
            fail("Calling a method on a closed Statement proxy should throw an Exception");
        } catch (SQLException e) {
            assertEquals("Unexpected SQLState", FBSQLException.SQL_STATE_INVALID_STATEMENT_ID,
                    e.getSQLState());
        }
    }

    /**
     * Calling a Statement method on an open proxy should notify the owner of
     * the occurrence of an exception.
     * 
     * @throws SQLException
     */
    public void testException_notify() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);
        final Sequence exceptionSequence = sequence("exceptionSequence");

        checking(new Expectations() {
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

        try {
            proxy.getFetchSize();
            fail("Expected test exception to be thrown");
        } catch (SQLException e) {
            // ignore: expected exception
        }
    }
    
    public void testStatementGetConnection_IsProxyConnection() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final Connection connectionProxy = mock(Connection.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);
        
        checking(new Expectations() {
            {
                oneOf(conHandler).getProxy(); will(returnValue(connectionProxy));
            }
        });
        
        Statement proxy = handler.getProxy();
        
        Connection con = proxy.getConnection();
        assertSame("Statement.getConnection should return the Connection-proxy of the PooledConnectionHandler", connectionProxy, con);
    }

}
