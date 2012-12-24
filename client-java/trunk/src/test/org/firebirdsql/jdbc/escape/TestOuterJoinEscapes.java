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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for support of the outer join escapes as defined in section 13.4.3 of
 * the JDBC 4.1 specification.
 * <p>
 * NOTE: We only test the basics, because Firebird supports the full join syntax
 * required, and we just strip the {oj ...} from the query.
 * <p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestOuterJoinEscapes extends FBJUnit4TestBase {

    @Before
    public void setupTestData() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE TAB1 (" + "  ID INT CONSTRAINT PK_TAB1 PRIMARY KEY" + ")");
            stmt.execute("CREATE TABLE TAB2 (" + "  ID INT CONSTRAINT PK_TAB2 PRIMARY KEY,"
                    + "  TAB1_ID INT CONSTRAINT FK_TAB2_TAB1 REFERENCES TAB1 (ID)" + ")");
            stmt.close();
            con.setAutoCommit(false);
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB1 (ID) VALUES (?)");
            pstmt.setInt(1, 1);
            pstmt.executeUpdate();
            pstmt.setInt(1, 3);
            pstmt.executeUpdate();
            pstmt.close();

            pstmt = con.prepareStatement("INSERT INTO TAB2 (ID, TAB1_ID) VALUES (?, ?)");
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.executeUpdate();
            pstmt.setInt(1, 2);
            pstmt.setNull(2, Types.INTEGER);
            pstmt.executeUpdate();
            pstmt.close();

            con.commit();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    @Test
    public void testFullOuterJoinEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TAB1.ID, TAB2.ID "
                    + "FROM {oj TAB1 FULL OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} " + "ORDER BY TAB1.ID NULLS LAST");

            assertTrue("Expected first row", rs.next());
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));
            assertTrue("Expected second row", rs.next());
            assertEquals(3, rs.getInt(1));
            assertEquals(0, rs.getInt(2));
            assertTrue(rs.wasNull());
            assertTrue("Expected third row", rs.next());
            assertEquals(0, rs.getInt(1));
            assertTrue(rs.wasNull());
            assertEquals(2, rs.getInt(2));
            assertFalse("No more rows expected", rs.next());

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    @Test
    public void testLeftOuterJoinEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TAB1.ID, TAB2.ID "
                    + "FROM {oj TAB1 LEFT OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} " + "ORDER BY TAB1.ID NULLS LAST");

            assertTrue("Expected first row", rs.next());
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));
            assertTrue("Expected second row", rs.next());
            assertEquals(3, rs.getInt(1));
            assertEquals(0, rs.getInt(2));
            assertTrue(rs.wasNull());
            assertFalse("No more rows expected", rs.next());

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    @Test
    public void testRightOuterJoinEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT TAB1.ID, TAB2.ID "
                            + "FROM {oj TAB1 RIGHT OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} "
                            + "ORDER BY TAB1.ID NULLS LAST");

            assertTrue("Expected first row", rs.next());
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));
            assertTrue("Expected second row", rs.next());
            assertEquals(0, rs.getInt(1));
            assertTrue(rs.wasNull());
            assertEquals(2, rs.getInt(2));
            assertFalse("No more rows expected", rs.next());

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
}
