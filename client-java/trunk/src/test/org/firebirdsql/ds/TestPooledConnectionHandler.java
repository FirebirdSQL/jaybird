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

import javax.sql.PooledConnection;

import org.firebirdsql.jdbc.FirebirdStatement;

/**
 * Tests for {@link PooledConnectionHandler} and its proxy object (obtained from {@link FBConnectionPoolDataSource}).
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestPooledConnectionHandler extends FBConnectionPoolTestBase {

    public TestPooledConnectionHandler(String name) {
        super(name);
    }

    /**
     * Test if closing the logical connection does not produce errors when 
     * it is closed with statements open.
     * <p>
     * See JDBC-250
     * </p>
     */
    public void testStatementOnConnectionClose() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        // Cast to FirebirdStatement to make this work with Java 5 / JDBC 3
        FirebirdStatement stmt = (FirebirdStatement)con.createStatement();
        
        con.close();
        assertTrue("Statement should be closed", stmt.isClosed());
        assertTrue("Connection should be closed", con.isClosed());
    }
    
    /**
     * Test if obtaining a new logical connection while one is open does not produce errors 
     * when the older logical connection is closed with statements open.
     * <p>
     * See JDBC-250
     * </p>
     */
    public void testStatementOnConnectionReuse() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        // Cast to FirebirdStatement to make this work with Java 5 / JDBC 3
        FirebirdStatement stmt = (FirebirdStatement)con.createStatement();

        pc.getConnection();
        assertTrue("Statement should be closed", stmt.isClosed());
        assertTrue("Connection should be closed", con.isClosed());
    }
    
    /**
     * Tests for equals() on connections. 
     */
    public void testConnectionEquals() throws SQLException {
        PooledConnection pc1 = getPooledConnection();
        PooledConnection pc2 = getPooledConnection();
        Connection con1 = pc1.getConnection();
        Connection con2 = pc2.getConnection();
        
        assertTrue("A connection should be equal to itself", con1.equals(con1));
        assertFalse("A connection should not be equal to a different connection", con1.equals(con2));
        Connection con1_2 = pc1.getConnection();
        assertFalse("A connection from the same pooled connection should be different", con1.equals(con1_2));
    }
    
    /**
     * Tests hashCode() call for connections.
     * <p>
     * NOTE: This tests an implementation detail of the hashCode!
     * </p>
     */
    public void testConnectionHashCode() throws SQLException {
        PooledConnection pc1 = getPooledConnection();
        PooledConnection pc2 = getPooledConnection();
        Connection con1 = pc1.getConnection();
        Connection con2 = pc2.getConnection();
        
        // Test for implementation detail!
        assertEquals(con1.hashCode(), System.identityHashCode(con1));
        // Warning: tests below might occasionally fail (no 100% guarantee that the hashCode() result is different)
        assertFalse("Expected two connections to have different hashCode()", con1.hashCode() == con2.hashCode());
        Connection con1_2 = pc1.getConnection();
        assertFalse("Expected two connections from same PooledConnection to have different hashCode()", con1.hashCode() == con1_2.hashCode());
    }
    
    /**
     * Test closing a connection twice should not throw an error.
     */
    public void testConnectionDoubleClose() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        
        assertFalse("Connection should be open", con.isClosed());
        con.close();
        assertTrue("Connection should be closed", con.isClosed());
        con.close();
        assertTrue("Connection should be closed", con.isClosed());
    }
    
    /**
     * Tests toString() of connection (proxy).
     * <p>
     * NOTE: This tests an implementation detail of toString()
     * </p>
     */
    public void testConnectionToString() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        
        // Test for implementation detail!
        assertTrue(con.toString().startsWith("Proxy for org.firebirdsql.jdbc.FBConnection"));
    }
}
