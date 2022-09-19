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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for support of the {@code LIKE} escape character escape as defined
 * in section 13.4.5 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class LikeEscapeTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
//@formatter:off
            "CREATE TABLE TAB1 (" +
            "  ID INT NOT NULL CONSTRAINT PK_TAB1 PRIMARY KEY," +
            "  VAL VARCHAR(30)" +
            ")"
//@formatter:on
    );

    private static final String[] TEST_DATA = { "abcdef", "abc_ef", "abc%ef", "abc&%ef", "abc&_ef" };

    private static Connection con;

    @BeforeAll
    static void setupTestData() throws Exception {
        con = FBTestProperties.getConnectionViaDriverManager();
        con.setAutoCommit(false);
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB1 (ID, VAL) VALUES (?, ?)")) {
            for (int idx = 0; idx < TEST_DATA.length; idx++) {
                pstmt.setInt(1, idx + 1);
                pstmt.setString(2, TEST_DATA[idx]);
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
     * Test for LIKE with an alternate escape defined, with % wildcard, but no escaped values.
     */
    @Test
    void testSimpleLike_percent() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc%' {escape '&'}")) {

            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(TEST_DATA)));

            assertEquals(expectedStrings, getStrings(rs, 1), "Unexpected result for LIKE 'abc%' {escape '&'}");
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with _ wildcard, but no
     * escaped values.
     */
    @Test
    void testSimpleLike_underscore() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc_ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Arrays.asList("abcdef", "abc_ef", "abc%ef")));

            assertEquals(expectedStrings, getStrings(rs, 1), "Unexpected result for LIKE 'abc_ef' {escape '&'}");
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped % character.
     */
    @Test
    void testEscapedLike_percent() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&%ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("abc%ef")));

            assertEquals(expectedStrings, getStrings(rs, 1), "Unexpected result for LIKE 'abc&%ef' {escape '&'}");
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped _ character.
     */
    @Test
    void testEscapedLike_underscore() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&_ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("abc_ef")));

            assertEquals(expectedStrings, getStrings(rs, 1), "Unexpected result for LIKE 'abc&_ef' {escape '&'}");
        }
    }

    /**
     * Helper method to sequentially process a ResultSet and add all strings in
     * columnIndex into a Set.
     *
     * @param rs
     *         ResultSet to process
     * @param columnIndex
     *         Index of the column
     * @return Set of strings in columnIndex.
     */
    @SuppressWarnings("SameParameterValue")
    private Set<String> getStrings(ResultSet rs, int columnIndex) throws SQLException {
        Set<String> strings = new HashSet<>();
        while (rs.next()) {
            strings.add(rs.getString(columnIndex));
        }
        return strings;
    }
}
