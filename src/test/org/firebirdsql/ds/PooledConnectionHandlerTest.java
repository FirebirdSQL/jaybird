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

import org.firebirdsql.jdbc.FirebirdStatement;
import org.junit.jupiter.api.Test;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PooledConnectionHandler} and its proxy object (obtained from {@link FBConnectionPoolDataSource}).
 * 
 * @author Mark Rotteveel
 */
class PooledConnectionHandlerTest extends FBConnectionPoolTestBase {

    /**
     * Test if closing the logical connection does not produce errors when 
     * it is closed with statements open.
     * <p>
     * See JDBC-250
     * </p>
     */
    @Test
    void testStatementOnConnectionClose() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        // Cast to FirebirdStatement to make this work with Java 5 / JDBC 3
        FirebirdStatement stmt = (FirebirdStatement)con.createStatement();
        
        con.close();
        assertTrue(stmt.isClosed(), "Statement should be closed");
        assertTrue(con.isClosed(), "Connection should be closed");
    }
    
    /**
     * Test if obtaining a new logical connection while one is open does not produce errors 
     * when the older logical connection is closed with statements open.
     * <p>
     * See JDBC-250
     * </p>
     */
    @Test
    void testStatementOnConnectionReuse() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        // Cast to FirebirdStatement to make this work with Java 5 / JDBC 3
        FirebirdStatement stmt = (FirebirdStatement)con.createStatement();

        pc.getConnection();
        assertTrue(stmt.isClosed(), "Statement should be closed");
        assertTrue(con.isClosed(), "Connection should be closed");
    }
    
    /**
     * Tests for equals() on connections. 
     */
    @Test
    void testConnectionEquals() throws SQLException {
        PooledConnection pc1 = getPooledConnection();
        PooledConnection pc2 = getPooledConnection();
        Connection con1 = pc1.getConnection();
        Connection con2 = pc2.getConnection();

        assertEquals(con1, con1, "A connection should be equal to itself");
        assertNotEquals(con1, con2, "A connection should not be equal to a different connection");
        Connection con1_2 = pc1.getConnection();
        assertNotEquals(con1, con1_2, "A connection from the same pooled connection should be different");
    }
    
    /**
     * Tests hashCode() call for connections.
     * <p>
     * NOTE: This tests an implementation detail of the hashCode!
     * </p>
     */
    @Test
    void testConnectionHashCode() throws SQLException {
        PooledConnection pc1 = getPooledConnection();
        PooledConnection pc2 = getPooledConnection();
        Connection con1 = pc1.getConnection();
        Connection con2 = pc2.getConnection();
        
        // Test for implementation detail!
        assertEquals(con1.hashCode(), System.identityHashCode(con1));
        // Warning: tests below might occasionally fail (no 100% guarantee that the hashCode() result is different)
        assertNotEquals(con1.hashCode(), con2.hashCode(), "Expected two connections to have different hashCode()");
        Connection con1_2 = pc1.getConnection();
        assertNotEquals(con1.hashCode(), con1_2.hashCode(),
                "Expected two connections from same PooledConnection to have different hashCode()");
    }
    
    /**
     * Test closing a connection twice should not throw an error.
     */
    @Test
    void testConnectionDoubleClose() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        
        assertFalse(con.isClosed(), "Connection should be open");
        con.close();
        assertTrue(con.isClosed(), "Connection should be closed");
        con.close();
        assertTrue(con.isClosed(), "Connection should be closed");
    }
    
    /**
     * Tests toString() of connection (proxy).
     * <p>
     * NOTE: This tests an implementation detail of toString()
     * </p>
     */
    @Test
    void testConnectionToString() throws SQLException {
        PooledConnection pc = getPooledConnection();
        Connection con = pc.getConnection();
        
        // Test for implementation detail!
        assertThat(con.toString(), startsWith("Proxy for org.firebirdsql.jdbc.FBConnection"));
    }
}
