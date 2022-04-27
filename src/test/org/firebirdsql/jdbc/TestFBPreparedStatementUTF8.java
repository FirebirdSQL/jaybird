/*
 * $Id$
 *
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBPreparedStatement} specifically involving UTF-8.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBPreparedStatementUTF8 extends FBJUnit4TestBase {

    @SuppressWarnings("deprecation")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE utf8table (" +
            " id INTEGER," +
            " char_1_none CHAR(1) CHARACTER SET NONE," +
            " char_1_utf8 CHAR(1) CHARACTER SET UTF8," +
            " char_1_win1252 CHAR(1) CHARACTER SET WIN1252," +
//            " varchar_1_none VARCHAR(1) CHARACTER SET NONE," +
//            " varchar_1_utf8 VARCHAR(1) CHARACTER SET UTF8," +
//            " varchar_1_win1252 VARCHAR(1) CHARACTER SET WIN1252," +
            " char_5_none CHAR(5) CHARACTER SET NONE," +
            " char_5_utf8 CHAR(5) CHARACTER SET UTF8," +
            " char_5_win1252 CHAR(5) CHARACTER SET WIN1252," +
            " varchar_5_none VARCHAR(5) CHARACTER SET NONE," +
            " varchar_5_utf8 VARCHAR(5) CHARACTER SET UTF8," +
            " varchar_5_win1252 VARCHAR(5) CHARACTER SET WIN1252" +
            ")";

    private static final String INSERT_FORMAT = "INSERT INTO utf8table(id, %s) VALUES (?, ?)";

    private static final String SELECT_FORMAT = "SELECT %s FROM utf8table WHERE id = ?";
    //@formatter:on

    private static final int DEFAULT_ID = 1;

    @Before
    public void setUp() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            assumeTrue(supportInfoFor(connection).supportsUtf8());
            executeCreateTable(connection, CREATE_TABLE);
        }
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_1_none_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_1_none", "W", "W");
    }

    @Test
    public void connectionUtf8_insertMultiByte_char_1_none_fails() throws Exception {
        basicFailureTest("UTF8", "char_1_none", "\u00e0", isA(DataTruncation.class));
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_5_none_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_none", "W", "W    ");
    }

    @Test
    public void connectionUtf8_insertMultiByte_char_5_none_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_none", "\u00e0", "\u00e0   "); // expectation padded up to length 4 not 5 (due to NONE encoding!)
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_1_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_1_win1252", "W", "W");
    }

    @Test
    public void connectionUtf8_insertMultiByte_inWin1252_char_1_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_1_win1252", "\u00e0", "\u00e0");
    }

    @Test
    public void connectionUtf8_insertMultiByte_notInWin1252_char_1_win1252_fails() throws Exception {
        // TODO Might be fixed once we apply correct encoding
        basicFailureTest("UTF8", "char_1_win1252", "\u0157", getTransliterationFailedMatcher());
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_5_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_win1252", "W", "W    ");
    }

    @Test
    public void connectionUtf8_insertMultipleInWin1252_char_5_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_win1252", "\u00FE\u00A3a\u0160,", "\u00FE\u00A3a\u0160,");
    }

    @Test
    public void connectionUtf8_insertMultipleInWin1252_varchar_5_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "varchar_5_win1252", "\u00FE\u00A3a\u0160,", "\u00FE\u00A3a\u0160,");
    }

    @Test
    public void connectionUtf8_insertMultipleInWin1252_lessThanMax_varchar_5_win1252_succeeds() throws Exception {
        basicSuccessTest("UTF8", "varchar_5_win1252", "\u00FE\u00A3a,", "\u00FE\u00A3a,");
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_1_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_1_utf8", "W", "W");
    }

    @Test
    public void connectionUtf8_insertMultiByte_char_1_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_1_utf8", "\u00e0", "\u00e0");
    }

    @Test
    public void connectionUtf8_insertMultipleSingleByte_char_1_utf8_fails() throws Exception {
        // TODO: expect DataTruncation.class instead?
        basicFailureTest("UTF8", "char_1_utf8", "ab", getStringTruncationMatcher());
    }

    @Test
    public void connectionUtf8_insertSingleByte_char_5_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_utf8", "W", "W    ");
    }

    @Test
    public void connectionUtf8_insertMultiByte_char_5_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_utf8", "\u00e0", "\u00e0    ");
    }

    @Test
    public void connectionUtf8_insertMultiple_char_5_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "char_5_utf8", "\u00FE\u00A3a\u0160,", "\u00FE\u00A3a\u0160,");
    }

    @Test
    public void connectionUtf8_insertMultiple_varchar_5_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "varchar_5_utf8", "\u00FE\u00A3a\u0160,", "\u00FE\u00A3a\u0160,");
    }

    @Test
    public void connectionUtf8_insertMultiple_lessThanMax_varchar_5_utf8_succeeds() throws Exception {
        basicSuccessTest("UTF8", "varchar_5_utf8", "\u00FEa\u0160,", "\u00FEa\u0160,");
    }

    /**
     * Get a connection with the specified (Firebird) encoding
     *
     * @param fbCharacterSet
     *         Firebird encoding name
     * @return Connection
     */
    private Connection getConnection(String fbCharacterSet) throws SQLException {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("lc_ctype", fbCharacterSet);

        return DriverManager.getConnection(getUrl(), props);
    }

    /**
     * Inserts a string into the specified column name.
     *
     * @param connection
     *         Connection to use
     * @param columnName
     *         Name of the column to use in {@link #INSERT_FORMAT}
     * @param testString
     *         String value to insert
     * @param id
     *         ID of the record
     */
    private void insertString(Connection connection, String columnName, String testString, int id) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(String.format(INSERT_FORMAT, columnName))) {
            insert.setInt(1, id);
            insert.setString(2, testString);
            insert.executeUpdate();
        }
    }

    /**
     * Selects a string from the specified column name.
     * <p>
     * Note: selection of at least one record is asserted and might throw an Exception
     * </p>
     *
     * @param connection
     *         Connection to use
     * @param columnName
     *         Name of the column to use in {@link #SELECT_FORMAT}
     * @param id
     *         ID of the record
     * @return Selected String value
     */
    private String selectString(Connection connection, String columnName, int id) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(String.format(SELECT_FORMAT, columnName))) {
            select.setInt(1, id);
            try (ResultSet rs = select.executeQuery()) {
                assertTrue("Expected a row", rs.next());
                return rs.getString(1);
            }
        }
    }

    /**
     * Test the basic success scenario.
     *
     * @param connectionEncoding
     *         Connection encoding
     * @param columnName
     *         Column name
     * @param testInput
     *         String to insert
     * @param expectedOutput
     *         Expected result of select
     */
    private void basicSuccessTest(String connectionEncoding, String columnName, String testInput,
            String expectedOutput) throws Exception {
        try (Connection connection = getConnection(connectionEncoding)) {
            insertString(connection, columnName, testInput, DEFAULT_ID);

            assertEquals(expectedOutput, selectString(connection, columnName, DEFAULT_ID));
        }
    }

    /**
     * Test the basic failure scenario.
     *
     * @param connectionEncoding
     *         Connection encoding
     * @param columnName
     *         Column name
     * @param testInput
     *         String to insert
     * @param exceptionExpectation
     *         Expected exception matcher
     */
    private void basicFailureTest(String connectionEncoding, String columnName, String testInput,
            Matcher<?> exceptionExpectation) throws Exception {
        try (Connection connection = getConnection(connectionEncoding)) {
            expectedException.expect(exceptionExpectation);

            insertString(connection, columnName, testInput, DEFAULT_ID);
        }
    }

    /**
     * @return Matcher for the Firebird string truncation error.
     */
    private Matcher<SQLException> getStringTruncationMatcher() {
        return allOf(
                isA(SQLException.class),
                errorCodeEquals(ISCConstants.isc_string_truncation),
                message(containsString(getFbMessage(ISCConstants.isc_string_truncation)))
        );
    }

    /**
     * @return Matcher for the Firebird transliteration failed error.
     */
    private Matcher<SQLException> getTransliterationFailedMatcher() {
        return allOf(
                isA(SQLException.class),
                errorCodeEquals(ISCConstants.isc_transliteration_failed),
                message(containsString(getFbMessage(ISCConstants.isc_transliteration_failed)))
        );
    }
}
