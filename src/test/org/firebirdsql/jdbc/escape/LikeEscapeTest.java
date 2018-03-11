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

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests for support of the <code>LIKE</code> escape character escape as defined
 * in section 13.4.5 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class LikeEscapeTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private static final String[] TEST_DATA = { "abcdef", "abc_ef", "abc%ef", "abc&%ef", "abc&_ef" };

    @BeforeClass
    public static void setupTestData() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            try (Statement stmt = con.createStatement()) {
//@formatter:off
                stmt.execute(
                    "CREATE TABLE TAB1 (" +
                    "  ID INT NOT NULL CONSTRAINT PK_TAB1 PRIMARY KEY," +
                    "  VAL VARCHAR(30)" +
                    ")");
//@formatter:on
            }

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
    }

    /**
     * Test for LIKE with an alternate escape defined, with % wildcard, but no
     * escaped values.
     */
    @Test
    public void testSimpleLike_percent() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc%' {escape '&'}")) {

            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(TEST_DATA)));

            assertEquals("Unexpected result for LIKE 'abc%' {escape '&'}", expectedStrings, getStrings(rs, 1));
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with _ wildcard, but no
     * escaped values.
     */
    @Test
    public void testSimpleLike_underscore() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc_ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Arrays.asList("abcdef", "abc_ef", "abc%ef")));

            assertEquals("Unexpected result for LIKE 'abc_ef' {escape '&'}", expectedStrings, getStrings(rs, 1));
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped % character.
     */
    @Test
    public void testEscapedLike_percent() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&%ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("abc%ef")));

            assertEquals("Unexpected result for LIKE 'abc&%ef' {escape '&'}", expectedStrings, getStrings(rs, 1));
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped _ character.
     */
    @Test
    public void testEscapedLike_underscore() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&_ef' {escape '&'}")) {

            Set<String> expectedStrings =
                    Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("abc_ef")));

            assertEquals("Unexpected result for LIKE 'abc&_ef' {escape '&'}", expectedStrings, getStrings(rs, 1));
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
    private Set<String> getStrings(ResultSet rs, int columnIndex) throws SQLException {
        Set<String> strings = new HashSet<>();
        while (rs.next()) {
            strings.add(rs.getString(columnIndex));
        }
        return strings;
    }
}
