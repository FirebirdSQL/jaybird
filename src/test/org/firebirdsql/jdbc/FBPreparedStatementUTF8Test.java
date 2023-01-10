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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBPreparedStatement} specifically involving UTF-8.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBPreparedStatementUTF8Test {

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsUtf8, "Test requires UTF8 support")
            .build();

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE utf8table (" +
            " id INTEGER PRIMARY KEY," +
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

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static final AtomicInteger idGenerator = new AtomicInteger();

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
    @ParameterizedTest
    @CsvSource({
            "UTF8, char_1_none, W, W",
            "UTF8, char_5_none, W, 'W    '",
            // multi-byte UTF-8 char, expectation padded up to length 4 not 5 (due to NONE encoding!)
            "UTF8, char_5_none, \u00e0, '\u00e0   '",
            "UTF8, char_1_win1252, W, W",
            // multi-byte UTF-8 char that exists in WIN1252
            "UTF8, char_1_win1252, \u00e0, \u00e0",
            "UTF8, char_5_win1252, W, 'W    '",
            // mix of multi-byte and single byte UTF-8 that exist in WIN1252
            "UTF8, char_5_win1252, '\u00FE\u00A3a\u0160,', '\u00FE\u00A3a\u0160,'",
            // mix of multi-byte and single byte UTF-8 that exist in WIN1252
            "UTF8, varchar_5_win1252, '\u00FE\u00A3a\u0160,', '\u00FE\u00A3a\u0160,'",
            // mix of multi-byte and single byte UTF-8 that exist in WIN1252, less than max length
            "UTF8, varchar_5_win1252, '\u00FE\u00A3a,', '\u00FE\u00A3a,'",
            "UTF8, char_1_utf8, W, W",
            // multi-byte UTF-8 char
            "UTF8, char_1_utf8, \u00e0, \u00e0",
            "UTF8, char_5_utf8, W, 'W    '",
            // multi-byte UTF-8 char
            "UTF8, char_5_utf8, \u00e0, '\u00e0    '",
            // mix of multi-byte and single byte UTF-8
            "UTF8, char_5_utf8, '\u00FE\u00A3a\u0160,', '\u00FE\u00A3a\u0160,'",
            // mix of multi-byte and single byte UTF-8
            "UTF8, varchar_5_utf8, '\u00FE\u00A3a\u0160,', '\u00FE\u00A3a\u0160,'",
            // mix of multi-byte and single byte UTF-8, less than max length
            "UTF8, varchar_5_utf8, '\u00FEa\u0160,', '\u00FEa\u0160,'",
    })
    void basicSuccessTest(String connectionEncoding, String columnName, String testInput, String expectedOutput)
            throws Exception {
        try (Connection connection = getConnection(connectionEncoding)) {
            int id = nextId();
            insertString(connection, columnName, testInput, id);

            assertEquals(expectedOutput, selectString(connection, columnName, id));
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
    @ParameterizedTest
    @MethodSource
    void basicFailureTest(String connectionEncoding, String columnName, String testInput,
            Matcher<SQLException> exceptionExpectation) throws Exception {
        try (Connection connection = getConnection(connectionEncoding)) {
            int id = nextId();

            SQLException exception = assertThrows(SQLException.class,
                    () -> insertString(connection, columnName, testInput, id));
            assertThat(exception, exceptionExpectation);
        }
    }

    static Stream<Arguments> basicFailureTest() {
        return Stream.of(
                // multi-byte char
                Arguments.of("UTF8", "char_1_none", "\u00e0", instanceOf(DataTruncation.class)),
                // TODO Might be fixed once we apply correct encoding
                Arguments.of("UTF8", "char_1_win1252", "\u0157", getTransliterationFailedMatcher()),
                // TODO: expect DataTruncation.class instead?
                Arguments.of("UTF8", "char_1_utf8", "ab", getStringTruncationMatcher()));
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
                assertTrue(rs.next(), "Expected a row");
                return rs.getString(1);
            }
        }
    }

    private static int nextId() {
        return idGenerator.incrementAndGet();
    }

    /**
     * @return Matcher for the Firebird string truncation error.
     */
    private static Matcher<SQLException> getStringTruncationMatcher() {
        return allOf(
                errorCodeEquals(ISCConstants.isc_string_truncation),
                message(containsString(getFbMessage(ISCConstants.isc_string_truncation))));
    }

    /**
     * @return Matcher for the Firebird transliteration failed error.
     */
    private static Matcher<SQLException> getTransliterationFailedMatcher() {
        return allOf(
                errorCodeEquals(ISCConstants.isc_transliteration_failed),
                message(containsString(getFbMessage(ISCConstants.isc_transliteration_failed))));
    }
}
