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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for support of the outer join escapes as defined in section 13.4.3 of
 * the JDBC 4.1 specification.
 * <p>
 * NOTE: We only test the basics, because Firebird supports the full join syntax
 * required, and we just strip the {oj ...} from the query.
 * <p>
 *
 * @author Mark Rotteveel
 */
class OuterJoinEscapesTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
//@formatter:off
            "CREATE TABLE TAB1 (" +
            "  ID INT NOT NULL CONSTRAINT PK_TAB1 PRIMARY KEY" +
            ")",
            "CREATE TABLE TAB2 (" +
            "  ID INT NOT NULL CONSTRAINT PK_TAB2 PRIMARY KEY," +
            "  TAB1_ID INT CONSTRAINT FK_TAB2_TAB1 REFERENCES TAB1 (ID)" +
            ")"
//@formatter:on
    );

    private static Connection con;

    @BeforeAll
    static void setupTestData() throws Exception {
        con = FBTestProperties.getConnectionViaDriverManager();
        con.setAutoCommit(false);
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB1 (ID) VALUES (?)")) {
            pstmt.setInt(1, 1);
            pstmt.execute();

            pstmt.setInt(1, 3);
            pstmt.execute();
        }

        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB2 (ID, TAB1_ID) VALUES (?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.execute();

            pstmt.setInt(1, 2);
            pstmt.setNull(2, Types.INTEGER);
            pstmt.execute();
        }

        con.commit();
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            con.close();
        } finally {
            con = null;
        }
    }

    @Test
    void testFullOuterJoinEscape() throws Exception {
        try (Statement stmt = con.createStatement();
//@formatter:off
             ResultSet rs = stmt.executeQuery(
                    "SELECT TAB1.ID, TAB2.ID " +
                    "FROM {oj TAB1 FULL OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} " +
                    "ORDER BY TAB1.ID NULLS LAST")) {
//@formatter:on

            assertTrue(rs.next(), "Expected first row");
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));

            assertTrue(rs.next(), "Expected second row");
            assertEquals(3, rs.getInt(1));
            assertEquals(0, rs.getInt(2));
            assertTrue(rs.wasNull());

            assertTrue(rs.next(), "Expected third row");
            assertEquals(0, rs.getInt(1));
            assertTrue(rs.wasNull());
            assertEquals(2, rs.getInt(2));

            assertFalse(rs.next(), "No more rows expected");
        }
    }

    @Test
    void testLeftOuterJoinEscape() throws Exception {
        try (Statement stmt = con.createStatement();
//@formatter:off
             ResultSet rs = stmt.executeQuery(
                    "SELECT TAB1.ID, TAB2.ID " +
                    "FROM {oj TAB1 LEFT OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} " +
                    "ORDER BY TAB1.ID NULLS LAST")) {
//@formatter:on

            assertTrue(rs.next(), "Expected first row");
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));

            assertTrue(rs.next(), "Expected second row");
            assertEquals(3, rs.getInt(1));
            assertEquals(0, rs.getInt(2));
            assertTrue(rs.wasNull());

            assertFalse(rs.next(), "No more rows expected");
        }
    }

    @Test
    public void testRightOuterJoinEscape() throws Exception {
        try (Statement stmt = con.createStatement();
//@formatter:off
             ResultSet rs = stmt.executeQuery(
                    "SELECT TAB1.ID, TAB2.ID " +
                    "FROM {oj TAB1 RIGHT OUTER JOIN TAB2 ON TAB2.TAB1_ID = TAB1.ID} " +
                    "ORDER BY TAB1.ID NULLS LAST")) {
//@formatter:on

            assertTrue(rs.next(), "Expected first row");
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));

            assertTrue(rs.next(), "Expected second row");
            assertEquals(0, rs.getInt(1));
            assertTrue(rs.wasNull());
            assertEquals(2, rs.getInt(2));

            assertFalse(rs.next(), "No more rows expected");
        }
    }
}
