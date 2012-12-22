/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jdbc.escape;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Test;

/**
 * Tests for support of the scalar system function escapes as defined in
 * appendix D.3 of the JDBC 4.1 specification.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestScalarSystemFunctions extends FBJUnit4TestBase {
    
    @Test
    public void testDatabase() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            stmt.executeQuery("SELECT {fn DATABASE()} FROM RDB$DATABASE");
            // TODO Consider implementing using RDB$GET_CONTEXT('SYSTEM', 'DB_NAME')
            fail("JDBC escape DATABASE() not supported");
        } catch (SQLException ex) {
            // TODO validate exception instead of throwing
            fail("Validation of unsupported functions not yet implemented");  
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
    @Test
    public void testIfNull() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {fn IFNULL(NULL, 'abcd')} FROM RDB$DATABASE");
            assertTrue("Expected at least one row", rs.next());
            assertEquals("Unexpected result for function escape IFNULL(NULL, 'abcd')", "abcd", rs.getString(1));
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
    @Test
    public void testUser() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {fn USER()} FROM RDB$DATABASE");
            // TODO Currently fails, implement using CURRENT_USER
            assertTrue("Expected at least one row", rs.next());
            assertEquals("Unexpected result for function escape USER()", "SYSDBA", rs.getString(1));
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
}
