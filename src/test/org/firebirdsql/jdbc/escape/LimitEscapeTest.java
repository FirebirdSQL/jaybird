// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for support of the Limiting Returned Rows Escape as defined in section
 * 13.4.6 of the JDBC 4.1 specification.
 *
 * @author Mark Rotteveel
 */
class LimitEscapeTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            "CREATE TABLE TAB1 (ID INT NOT NULL CONSTRAINT PK_TAB1 PRIMARY KEY)");

    private static final int ROW_COUNT = 25;

    private static Connection con;

    @BeforeAll
    static void setupTestData() throws Exception {
        con = FBTestProperties.getConnectionViaDriverManager();
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

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            con.close();
        } finally {
            con = null;
        }
    }

    /**
     * Test of limit escape with only the rows parameter with a literal value
     */
    @Test
    void testLimitLiteralWithoutOffset() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM TAB1 ORDER BY ID {limit 10}")) {

            int id = 0;
            while (rs.next()) {
                id++;
                assertEquals(id, rs.getInt(1));
            }

            assertEquals(10, id, "Unexpected final value for ID returned");
        }
    }

    /**
     * Test of limit escape with only the rows parameter with a parametrized value
     */
    @Test
    void testLimitParametrizedWithoutOffset() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement("SELECT ID FROM TAB1 ORDER BY ID {limit ?}")) {

            stmt.setInt(1, 10);
            try (ResultSet rs = stmt.executeQuery()) {
                int id = 0;
                while (rs.next()) {
                    id++;
                    assertEquals(id, rs.getInt(1));
                }

                assertEquals(10, id, "Unexpected final value for ID returned");
            }
        }
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a literal value for both
     */
    @Test
    void testLimitLiteralWithOffset() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM TAB1 ORDER BY ID {limit 10 offset 5}")) {

            int id = 4;
            while (rs.next()) {
                id++;
                assertEquals(id, rs.getInt(1));
            }

            assertEquals(15, id, "Unexpected final value for ID returned");
        }
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a parameter for rows and a literal value for offset_rows.
     */
    @Test
    void testLimitRowsParameter() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT ID FROM TAB1 ORDER BY ID {limit ? offset 10}")) {

            pstmt.setInt(1, 8);
            try (ResultSet rs = pstmt.executeQuery()) {
                int id = 9;
                while (rs.next()) {
                    id++;
                    assertEquals(id, rs.getInt(1));
                }

                assertEquals(18, id, "Unexpected final value for ID returned");
            }
        }
    }
}
