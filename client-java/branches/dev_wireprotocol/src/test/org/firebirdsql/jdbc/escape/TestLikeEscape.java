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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for support of the <code>LIKE</code> escape character escape as defined
 * in section 13.4.5 of the JDBC 4.1 specification.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestLikeEscape extends FBJUnit4TestBase {

    private static final String[] TEST_DATA = { "abcdef", "abc_ef", "abc%ef" };

    @Before
    public void setupTestData() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
//@formatter:off
            stmt.execute(
                    "CREATE TABLE TAB1 (" +
                    "  ID INT CONSTRAINT PK_TAB1 PRIMARY KEY," +
                    "  VAL VARCHAR(30)" +
                    ")");
//@formatter:on
            stmt.close();

            con.setAutoCommit(false);
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO TAB1 (ID, VAL) VALUES (?, ?)");
            for (int idx = 0; idx < TEST_DATA.length; idx++) {
                pstmt.setInt(1, idx + 1);
                pstmt.setString(2, TEST_DATA[idx]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            con.commit();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with % wildcard, but no
     * escaped values.
     */
    @Test
    public void testSimpleLike_percent() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc%' {escape '&'}");
            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(TEST_DATA)));

            assertEquals("Unexpected result for LIKE 'abc%' {escape '&'}", expectedStrings, getStrings(rs, 1));

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with _ wildcard, but no
     * escaped values.
     */
    @Test
    public void testSimpleLike_underscore() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc_ef' {escape '&'}");
            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(TEST_DATA)));

            assertEquals("Unexpected result for LIKE 'abc_ef' {escape '&'}", expectedStrings, getStrings(rs, 1));

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped % character.
     */
    @Test
    public void testEscapedLike_percent() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&%ef' {escape '&'}");
            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("abc%ef")));

            assertEquals("Unexpected result for LIKE 'abc&%ef' {escape '&'}", expectedStrings, getStrings(rs, 1));

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Test for LIKE with an alternate escape defined, with escaped _ character.
     */
    @Test
    public void testEscapedLike_underscore() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VAL FROM TAB1 WHERE VAL LIKE 'abc&_ef' {escape '&'}");
            Set<String> expectedStrings = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("abc_ef")));

            assertEquals("Unexpected result for LIKE 'abc&_ef' {escape '&'}", expectedStrings, getStrings(rs, 1));

            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Helper method to sequentially process a ResultSet and add all strings in
     * columnIndex into a Set.
     * 
     * @param rs
     *            ResultSet to process
     * @param columnIndex
     *            Index of the column
     * @return Set of strings in columnIndex.
     * @throws SQLException
     */
    private Set<String> getStrings(ResultSet rs, int columnIndex) throws SQLException {
        Set<String> strings = new HashSet<String>();
        while (rs.next()) {
            strings.add(rs.getString(columnIndex));
        }
        return strings;
    }
}
