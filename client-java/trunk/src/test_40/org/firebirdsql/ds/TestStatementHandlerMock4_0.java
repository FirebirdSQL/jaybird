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

import java.sql.SQLException;
import java.sql.Statement;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Tests for JDBC 4 features of {@link StatementHandler} using jMock.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestStatementHandlerMock4_0 extends MockObjectTestCase {
    
    {
        setImposteriser(ClassImposteriser.INSTANCE);
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
    public void testHandlerClose_IsClosed() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        checking(new Expectations() {
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
    public void testProxyClose_IsClosed() throws SQLException {
        final PooledConnectionHandler conHandler = mock(PooledConnectionHandler.class);
        final Statement statement = mock(Statement.class);
        final StatementHandler handler = new StatementHandler(conHandler, statement);

        checking(new Expectations() {
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
