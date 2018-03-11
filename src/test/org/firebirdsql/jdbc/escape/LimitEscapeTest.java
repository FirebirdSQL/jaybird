/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * Tests for support of the Limiting Returned Rows Escape as defined in section
 * 13.4.6 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class LimitEscapeTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private static final int ROW_COUNT = 25;

    @BeforeClass
    public static void setupTestData() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            try (Statement stmt = con.createStatement()) {
//@formatter:off
                stmt.execute("CREATE TABLE TAB1 (" +
                         "  ID INT NOT NULL CONSTRAINT PK_TAB1 PRIMARY KEY" +
                         ")");
//@formatter:on
            }

            con.setAutoCommit(false);
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB1 (ID) VALUES (?)")) {
                for (int id = 1; id <= ROW_COUNT; id++) {
                    pstmt.setInt(1, id);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            con.commit();
        }
    }

    /**
     * Test of limit escape with only the rows parameter with a literal value
     */
    @Test
    public void testLimitLiteralWithoutOffset() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM TAB1 ORDER BY ID {limit 10}")) {

            int id = 0;
            while (rs.next()) {
                id++;
                assertEquals(id, rs.getInt(1));
            }

            assertEquals("Unexpected final value for ID returned", 10, id);
        }
    }

    /**
     * Test of limit escape with only the rows parameter with a parametrized value
     */
    @Test
    public void testLimitParametrizedWithoutOffset() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             PreparedStatement stmt = con.prepareStatement("SELECT ID FROM TAB1 ORDER BY ID {limit ?}")) {

            stmt.setInt(1, 10);
            try (ResultSet rs = stmt.executeQuery()) {
                int id = 0;
                while (rs.next()) {
                    id++;
                    assertEquals(id, rs.getInt(1));
                }

                assertEquals("Unexpected final value for ID returned", 10, id);
            }
        }
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a literal value for both
     */
    @Test
    public void testLimitLiteralWithOffset() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM TAB1 ORDER BY ID {limit 10 offset 5}")) {

            int id = 4;
            while (rs.next()) {
                id++;
                assertEquals(id, rs.getInt(1));
            }

            assertEquals("Unexpected final value for ID returned", 15, id);
        }
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a parameter for rows and a literal value for offset_rows.
     */
    @Test
    public void testLimitRowsParameter() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             PreparedStatement pstmt = con.prepareStatement("SELECT ID FROM TAB1 ORDER BY ID {limit ? offset 10}")) {

            pstmt.setInt(1, 8);
            try (ResultSet rs = pstmt.executeQuery()) {
                int id = 9;
                while (rs.next()) {
                    id++;
                    assertEquals(id, rs.getInt(1));
                }

                assertEquals("Unexpected final value for ID returned", 18, id);
            }
        }
    }
}
