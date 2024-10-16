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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.sql.ResultSet.*;
import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.assertions.ResultSetAssertions.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.jaybird.props.PropertyConstants.SCROLLABLE_CURSOR_EMULATED;
import static org.firebirdsql.jaybird.props.PropertyConstants.SCROLLABLE_CURSOR_SERVER;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FBResultSetTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private static final String SELECT_STATEMENT = """
            SELECT
              1 AS col1,
              2 AS "col1",
              3 AS "Col1"
            FROM rdb$database""";

    private static final String CREATE_TABLE_STATEMENT = """
            CREATE TABLE test_table(
              id INTEGER NOT NULL PRIMARY KEY,
              str VARCHAR(10),
              long_str VARCHAR(255),
              very_long_str VARCHAR(20000),
              blob_str BLOB SUB_TYPE TEXT,
              "CamelStr" VARCHAR(255),
              blob_bin BLOB SUB_TYPE BINARY
            )""";

    private static final String SELECT_TEST_TABLE = "SELECT id, str FROM test_table order by id";

    private static final String CREATE_TABLE_STATEMENT2 = """
            CREATE TABLE test_table2(
              id INTEGER NOT NULL,
              str VARCHAR(10),
              long_str VARCHAR(255),
              very_long_str VARCHAR(20000),
              blob_str BLOB SUB_TYPE 1,
              "CamelStr" VARCHAR(255)
            )""";

    private static final String CREATE_WITH_COMPOSITE_PK = """
            create table WITH_COMPOSITE_PK (
              ID1 integer not null,
              ID2 integer not null,
              VAL varchar(50),
              constraint PK_WITH_COMPOSITE_PK primary key (ID1, ID2)
            )""";

    private static final String CREATE_VIEW_STATEMENT = """
            CREATE VIEW test_empty_string_view(marker, id, empty_char)
              AS
              SELECT
                CAST('marker' AS VARCHAR(6)),
                id,
                ''
              FROM
                test_table""";

    private static final String CREATE_SUBSTR_FUNCTION = """
            DECLARE EXTERNAL FUNCTION substr
              CSTRING(80), SMALLINT, SMALLINT
            RETURNS CSTRING(80) FREE_IT
            ENTRY_POINT 'IB_UDF_substr' MODULE_NAME 'ib_udf'""";

    private static final String SELECT_FROM_VIEW_STATEMENT = "SELECT * FROM test_empty_string_view";

    private static final String CURSOR_NAME = "some_cursor";

    private static final String UPDATE_TABLE_STATEMENT =
            "UPDATE test_table SET str = ? WHERE CURRENT OF " + CURSOR_NAME;

    /**
     * Test if all columns are found correctly.
     */
    @Test
    void testFindColumn() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_STATEMENT)) {
            assertTrue(rs.next(), "Should have at least one row");

            assertEquals(1, rs.getInt("COL1"), "COL1 should be 1");
            assertEquals(1, rs.getInt("col1"), "col1 should be 1");
            assertEquals(2, rs.getInt("\"col1\""), "\"col1\" should be 2");
            assertEquals(1, rs.getInt("Col1"), "Col1 should be 1");
        }
    }

    /**
     * Test if positioned updates work correctly.
     */
    @Test
    void testPositionedUpdate() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            connection.setAutoCommit(false);

            try (Statement select = connection.createStatement()) {
                select.setCursorName(CURSOR_NAME);
                ResultSet rs = select.executeQuery("SELECT id, str FROM test_table FOR UPDATE OF " + CURSOR_NAME);

                assertTrue(rs.isBeforeFirst(), "ResultSet.isBeforeFirst() should be true");

                try (PreparedStatement update = connection.prepareStatement(UPDATE_TABLE_STATEMENT)) {
                    int counter = 1;

                    while (rs.next()) {
                        assertEquals(counter == 1, rs.isFirst(), "ResultSet.isFirst() should be true for first row");
                        if (counter == recordCount) {
                            assertThrows(FBDriverNotCapableException.class, rs::isLast,
                                    "Named cursor (for update) cannot detect last position");
                        }
                        assertEquals(counter, rs.getRow(), "ResultSet.getRow() should be correct");

                        update.setInt(1, rs.getInt(1) + 1);
                        int updatedCount = update.executeUpdate();

                        assertEquals(1, updatedCount, "Number of update rows");
                        counter++;
                    }

                    assertTrue(rs.isAfterLast(), "ResultSet.isAfterLast() should be true");
                    assertFalse(rs.next(), "ResultSet.next() should return false");
                }
            }
            connection.commit();

            try (Statement select = connection.createStatement();
                 ResultSet rs = select.executeQuery("SELECT id, str FROM test_table")) {
                int counter = 1;

                assertTrue(rs.isBeforeFirst(), "ResultSet.isBeforeFirst() should be true");

                while (rs.next()) {
                    assertEquals(counter == 1, rs.isFirst(), "ResultSet.isFirst() should be true for first row");
                    assertEquals(counter == recordCount, rs.isLast(), "ResultSet.isLast() should be true for last row");

                    int idValue = rs.getInt(1);
                    int strValue = rs.getInt(2);

                    assertEquals(idValue + 1, strValue, "Value of str column must be equal to id + 1");
                    counter++;
                }

                assertTrue(rs.isAfterLast(), "ResultSet.isAfterLast() should be true");
                assertFalse(rs.next(), "ResultSet.next() should return false");
            }
            connection.commit();
        }
    }

    /**
     * This test checks if an empty column in a view is correctly returned
     * to the client.
     */
    @Test
    void testEmptyColumnInView() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            executeCreateTable(connection, CREATE_VIEW_STATEMENT);

            createTestData(10, i -> "", connection, "long_str");

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT_FROM_VIEW_STATEMENT)) {
                int counter = 1;
                while (rs.next()) {
                    String marker = rs.getString(1);
                    int key = rs.getInt(2);
                    String value = rs.getString(3);

                    assertEquals("marker", marker, "Marker should be correct");
                    assertEquals(counter, key, "Key should be same as counter");
                    assertEquals("", value, "EMPTY_CHAR string should be empty");

                    counter++;
                }

                assertEquals(10, counter - 1, "Should read 10 records");
            }
        }
    }

    /**
     * Test cursor scrolling in case of ResultSet.TEST_SCROLL_INSENSITIVE.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testScrollInsensitive(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY);
                 ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
                int testValue;

                rs.last();
                testValue = recordCount;
                assertEquals(testValue, rs.getInt(1), "ID of last record should be equal to " + testValue);
                assertTrue(rs.isLast(), "isLast() should return true");

                rs.absolute(recordCount / 2);
                testValue = recordCount / 2;
                assertEquals(testValue, rs.getInt(1), "ID after absolute positioning should return " + testValue);

                rs.absolute(-1);
                testValue = recordCount;
                assertEquals(testValue, rs.getInt(1),
                        "ID after absolute positioning with negative position should return " + testValue);

                rs.first();
                testValue = 1;
                assertEquals(testValue, rs.getInt(1), "ID after first() should return " + testValue);
                assertTrue(rs.isFirst(), "isFirst() should report true");

                boolean hasRow = rs.previous();
                assertFalse(hasRow, "Should not point to the row");
                assertTrue(rs.isBeforeFirst(), "isBeforeFirst() should return true");

                rs.relative(5);
                rs.relative(-4);
                //noinspection ConstantConditions
                testValue = 1;
                assertEquals(testValue, rs.getInt(1), "ID after relative positioning should return " + testValue);

                rs.beforeFirst();
                assertTrue(rs.isBeforeFirst(), "isBeforeFirst() should return true");
                assertThrows(SQLException.class, () -> rs.getInt(1),
                        "Should not be possible to access column if cursor does not point to a row");

                rs.afterLast();
                assertTrue(rs.isAfterLast(), "isAfterLast() should return true");
                assertThrows(SQLException.class, () -> rs.getInt(1),
                        "Should not be possible to access column if cursor does not point to a row");

                assertFalse(rs.next(), "ResultSet.next() should return false");
            }
        }
    }

    /**
     * Test {@link ResultSet#absolute(int)} cursor scrolling in case of ResultSet.TEST_SCROLL_INSENSITIVE.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testScrollInsensitive_Absolute(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
                ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table");
                assertTrue(rs.isBeforeFirst(), "Should be before first");
                assertFalse(rs.isAfterLast(), "Should not be after last");

                assertTrue(rs.absolute(1), "Position 1 is in result set");
                assertEquals(1, rs.getInt(1));
                assertFalse(rs.isBeforeFirst(), "Should not be before first");
                assertFalse(rs.isAfterLast(), "Should not be after last");

                assertFalse(rs.absolute(0), "Position 0 is outside result set");
                assertFalse(rs.absolute(11), "Position 11 is outside result set");
                assertFalse(rs.isBeforeFirst(), "Should not be before first");
                assertTrue(rs.isAfterLast(), "Should be after last");

                assertTrue(rs.absolute(10), "Position 10 is inside result set");
                assertFalse(rs.isBeforeFirst(), "Should not be before first");
                assertFalse(rs.isAfterLast(), "Should not be after last");
                assertEquals(10, rs.getInt(1));

                assertFalse(rs.absolute(15), "Position 15 is outside result set");
                assertFalse(rs.isBeforeFirst(), "Should not be before first");
                assertTrue(rs.isAfterLast(), "Should be after last");

                assertTrue(rs.absolute(-1), "Position -1 is inside result set");
                assertFalse(rs.isBeforeFirst(), "Should not be before first");
                assertFalse(rs.isAfterLast(), "Should not be after last");
                assertEquals(10, rs.getInt(1));

                assertFalse(rs.absolute(-11), "Position -11 is outside result set");
                assertTrue(rs.isBeforeFirst(), "Should be before first");
                assertFalse(rs.isAfterLast(), "Should not be after last");
            }
        }
    }

    /**
     * This test case tries to reproduce a NPE reported in Firebird-Java group
     * by vmdd_tech after Jaybird 1.5 beta 1 release.
     */
    @Test
    void testBugReport1() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNativeUserDefinedFunctions(), "Test requires UDF support");
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            executeDDL(connection, CREATE_SUBSTR_FUNCTION);

            IntFunction<String> rowData = id -> switch (id) {
                case 1 -> "aaa";
                case 2 -> "'more than 80 chars are in hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
                case 3 -> "more than 80 chars are in hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
                default -> throw new IllegalArgumentException("Expected values 1, 2 or 3, received: " + id);
            };
            createTestData(3, rowData, connection, "long_str");

            final String query = "SELECT id, substr(long_str,1,2) FROM test_table ORDER BY id DESC";
            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(query)) {
                    assertTrue(rs.next(), "Should have at least one row");
                } catch (SQLException ex) {
                    if (ex.getErrorCode() != ISCConstants.isc_string_truncation
                            && !ex.getMessage().contains("string truncation")) throw ex;
                    // it is ok as well, since substr is declared as CSTRING(80)
                    // and truncation error happens
                    System.out.println("First query generated exception " + ex.getMessage());
                }

                try (ResultSet rs = stmt.executeQuery(query)) {
                    assertTrue(rs.next(), "Should have at least one row");

                    rs.getObject(1);
                } catch (SQLException ex) {
                    if (ex.getErrorCode() != ISCConstants.isc_string_truncation
                            && !ex.getMessage().contains("string truncation")) throw ex;
                    // it is ok as well, since substr is declared as CSTRING(80)
                    // and truncation error happens
                    System.out.println("Second query generated exception " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Test if result set type and concurrency is correct.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testBugReport2(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY);
                 ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals(TYPE_SCROLL_INSENSITIVE, rs.getType(), "ResultSet type should be TYPE_SCROLL_INSENSITIVE");
                assertEquals(CONCUR_READ_ONLY, rs.getConcurrency(), "ResultSet concurrency should be CONCUR_READ_ONLY");

                rs.last();

                assertEquals(TYPE_SCROLL_INSENSITIVE, rs.getType(), "ResultSet type should not change");
            }
        }
    }

    @Test
    void testBugReport3() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            try (Statement stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
                 ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
                assertThrows(SQLException.class, rs::first, "first() should not work in TYPE_FORWARD_ONLY result sets");

                //noinspection StatementWithEmptyBody
                while (rs.next()) {
                    // do nothing, just loop.
                }

                assertThrows(SQLException.class, rs::first, "first() should not work in TYPE_FORWARD_ONLY result sets");
            }
        }
    }

    @Test
    void testResultSetNotClosed() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            connection.setAutoCommit(false);

            final int recordCount = 1;
            IntFunction<String> rowData = id -> new String(DataGenerator.createRandomAsciiBytes(19000));
            createTestData(recordCount, rowData, connection, "very_long_str");
            connection.commit();

            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM test_table WHERE id = ?")) {
                stmt.setInt(1, recordCount + 10);

                try (ResultSet rs = stmt.executeQuery()) {
                    assertFalse(rs.next(), "Should not find any record");
                }

                stmt.setInt(1, recordCount);

                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Should find a record");
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableResultSet(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            final int recordCount = 10;
            createTestData(recordCount, id -> "oldString" + id, connection, "long_str");

            connection.clearWarnings();

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                assertNull(connection.getWarnings(), "No warnings should be added");

                try (ResultSet rs = stmt.executeQuery(
                        "SELECT id, long_str, str, \"CamelStr\" FROM test_table ORDER BY id")) {
                    int counter = 1;
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        assertEquals(counter, id);

                        String longStr = rs.getString(2);
                        assertEquals("oldString" + counter, longStr);

                        rs.updateString(2, "newString" + counter);

                        assertEquals(counter, rs.getInt(1));
                        assertEquals("newString" + counter, rs.getString(2));

                        assertNull(rs.getString(3));
                        rs.updateString(3, "str" + counter);

                        assertNull(rs.getString(4));
                        rs.updateString(4, "str" + counter);

                        // check whether row can be updated
                        rs.updateRow();

                        // visibility of updates without refresh
                        assertEquals(counter, rs.getInt(1));
                        assertEquals("newString" + counter, rs.getString(2));
                        assertEquals("str" + counter, rs.getString(3));
                        assertEquals("str" + counter, rs.getString(4));

                        // check whether row can be refreshed
                        rs.refreshRow();

                        // visibility of updates after refresh
                        assertEquals(counter, rs.getInt(1));
                        assertEquals("newString" + counter, rs.getString(2));
                        assertEquals("str" + counter, rs.getString(3));
                        assertEquals("str" + counter, rs.getString(4));

                        counter++;
                    }

                    assertEquals(counter - 1, recordCount, "Should process " + recordCount + " rows");

                    // check the insertRow() feature
                    int newId = recordCount + 1;
                    rs.moveToInsertRow();
                    rs.updateInt(1, newId);
                    rs.updateString(2, "newString" + newId);
                    rs.updateString(3, "bug");
                    rs.updateString(4, "quoted column");
                    rs.insertRow();
                    rs.moveToCurrentRow();

                    // check whether newly inserted row can be updated
                    rs.last();
                    rs.updateString(3, "str" + newId);
                    rs.updateRow();
                }

                try (ResultSet rs = stmt.executeQuery("SELECT id, long_str, str FROM test_table ORDER BY id")) {
                    int counter = 1;
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        assertEquals(counter, id);

                        String longStr = rs.getString(2);
                        assertEquals("newString" + counter, longStr);
                        assertEquals("str" + counter, rs.getString(3));
                        counter++;

                        if (counter == recordCount + 1) rs.deleteRow();
                    }

                    assertEquals(recordCount + 1, counter - 1);
                }

                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM test_table")) {
                    assertTrue(rs.next());
                    assertEquals(recordCount, rs.getInt(1));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableResultSetNoPK(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            final int recordCount = 10;
            createTestData(recordCount, id -> "oldString" + id, connection, "long_str");
            connection.clearWarnings();

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                assertNull(connection.getWarnings(), "No warnings should be added");

                try (ResultSet rs = stmt.executeQuery(
                        "SELECT rdb$db_key, id, long_str, str, \"CamelStr\" FROM test_table ORDER BY 2")) {
                    int counter = 1;
                    while (rs.next()) {
                        int id = rs.getInt(2);
                        assertEquals(counter, id);

                        String longStr = rs.getString(3);
                        assertEquals("oldString" + counter, longStr);

                        rs.updateString(3, "newString" + counter);

                        assertEquals(counter, rs.getInt(2));
                        assertEquals("newString" + counter, rs.getString(3));

                        assertNull(rs.getString(4));
                        rs.updateString(4, "str" + counter);

                        assertNull(rs.getString(5));
                        rs.updateString(5, "str" + counter);

                        // check whether row can be updated
                        rs.updateRow();

                        // visibility of updates without refresh
                        assertEquals(counter, rs.getInt(2));
                        assertEquals("newString" + counter, rs.getString(3));
                        assertEquals("str" + counter, rs.getString(4));
                        assertEquals("str" + counter, rs.getString(5));

                        // check whether row can be refreshed
                        rs.refreshRow();

                        // visibility of updates after refresh
                        assertEquals(counter, rs.getInt(2));
                        assertEquals("newString" + counter, rs.getString(3));
                        assertEquals("str" + counter, rs.getString(4));
                        assertEquals("str" + counter, rs.getString(5));

                        counter++;
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableStatementResultSetDowngradeToReadOnlyWhenQueryNotUpdatable(
            String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            executeCreateTable(connection, CREATE_TABLE_STATEMENT2);

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery("select * from test_table t1 left join test_table2 t2 on t1.id = t2.id")) {
                assertThat(stmt.getWarnings(), allOf(
                        notNullValue(),
                        fbMessageStartsWith(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)));

                assertEquals(CONCUR_READ_ONLY, rs.getConcurrency(), "Expected downgrade to CONCUR_READ_ONLY");
            }
        }
    }

    /**
     * Tests if a result set that doesn't contain all PK columns (but only a prefix) will be downgraded to read-only.
     * <p>
     * Rationale: a previous implementation was satisfied if at least the first PK column was found
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableStatementPrefixPK_downgradeToReadOnly(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_WITH_COMPOSITE_PK);
            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery("select id1, val from WITH_COMPOSITE_PK")) {
                assertThat(stmt.getWarnings(), allOf(
                        notNullValue(),
                        fbMessageStartsWith(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)));

                assertEquals(CONCUR_READ_ONLY, rs.getConcurrency(), "Expected downgrade to CONCUR_READ_ONLY");
            }
        }
    }

    /**
     * Tests if a result set that doesn't contain all PK columns (but only a suffix) will be downgraded to read-only.
     * <p>
     * Rationale: a previous implementation was satisfied if at least the first PK column was found
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableStatementSuffixPK_downgradeToReadOnly(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_WITH_COMPOSITE_PK);
            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery("select id2, val from WITH_COMPOSITE_PK")) {
                assertThat(stmt.getWarnings(), allOf(
                        notNullValue(),
                        fbMessageStartsWith(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)));

                assertEquals(CONCUR_READ_ONLY, rs.getConcurrency(), "Expected downgrade to CONCUR_READ_ONLY");
            }
        }
    }

    /**
     * Tests if a result set that doesn't contain all PK columns, but does contain RDB$DB_KEY, is updatable.
     * <p>
     * Rationale: see also {@link #testUpdatableStatementPrefixPK_downgradeToReadOnly(String)}, and previous
     * implementation considered either the primary key or RDB$DB_KEY, current implementation falls back to RDB$DB_KEY.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableStatementPartialPKAndDBKey(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_WITH_COMPOSITE_PK);
            try (var pstmt = connection.prepareStatement(
                    "insert into WITH_COMPOSITE_PK (ID1, ID2, VAL) values (?, ?, ?)")) {
                pstmt.setInt(1, 1);
                pstmt.setInt(2, 1);
                pstmt.setString(3, "ID_1_1");
                pstmt.addBatch();
                pstmt.setInt(2, 2);
                pstmt.setString(3, "ID_1_2");
                pstmt.addBatch();
                pstmt.executeBatch();
            }

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                try (var rs = stmt.executeQuery("select ID1, RDB$DB_KEY, VAL from WITH_COMPOSITE_PK where ID2 = 1")) {
                    assertEquals(CONCUR_UPDATABLE, rs.getConcurrency(), "Expected CONCUR_UPDATABLE result set");
                    assertTrue(rs.next(), "expected a row");
                    assertEquals("ID_1_1", rs.getString(3), "unexpected value for VAL");

                    rs.updateString(3, "modified");
                    rs.updateRow();
                }
                try (var rs = stmt.executeQuery("select ID1, ID2, VAL from WITH_COMPOSITE_PK order by ID1, ID2")) {
                    assertTrue(rs.next(), "expected a row");
                    assertEquals("modified", rs.getString(3), "expected modified value for VAL of first row");
                    assertTrue(rs.next(), "expected a row");
                    assertEquals("ID_1_2", rs.getString(3), "expected original value (ID_1_2) for VAL of second row");
                }
            }
        }
    }

    @Test
    void testGetExecutionPlan() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement();
                 FBResultSet rs = (FBResultSet) stmt.executeQuery("SELECT id, str FROM test_table")) {

                String execPlan = rs.getExecutionPlan();
                assertNotNull(execPlan);
                assertThat("Execution plan should reference test_table",
                        execPlan.toUpperCase(Locale.ROOT), containsString("TEST_TABLE"));
            }

            try (PreparedStatement pStmt = connection.prepareStatement("SELECT * FROM TEST_TABLE");
                 FBResultSet rs = (FBResultSet) pStmt.executeQuery()) {
                String execPlan = rs.getExecutionPlan();
                assertNotNull(execPlan);
                assertThat("Execution plan should reference test_table",
                        execPlan.toUpperCase(Locale.ROOT), containsString("TEST_TABLE"));
            }

            // Ensure there isn't a crash when attempting to retrieve the
            // execution plan from a non-statement-based ResultSet
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            try (FBResultSet rs = (FBResultSet) metaData.getSchemas()) {
                assertEquals("", rs.getExecutionPlan(), "Non-statement-based result set has no execution plan");
            }
        }
    }

    @Test
    void testGetExplainedExecutionPlan() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsExplainedExecutionPlan(),
                "Test requires explained execution plan support");

        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement();
                 FBResultSet rs = (FBResultSet) stmt.executeQuery("SELECT id, str FROM test_table")) {

                String execPlan = rs.getExplainedExecutionPlan();
                assertNotNull(execPlan);
                assertThat("Detailed execution plan should reference test_table",
                        execPlan.toUpperCase(Locale.ROOT), containsString("TEST_TABLE"));
            }

            try (PreparedStatement pStmt = connection.prepareStatement("SELECT * FROM TEST_TABLE");
                 FBResultSet rs = (FBResultSet) pStmt.executeQuery()) {
                String execPlan = rs.getExplainedExecutionPlan();
                assertNotNull(execPlan);
                assertThat("Detailed execution plan should reference test_table",
                        execPlan.toUpperCase(Locale.ROOT), containsString("TEST_TABLE"));
            }

            // Ensure there isn't a crash when attempting to retrieve the
            // detailed execution plan from a non-statement-based ResultSet
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            try (FBResultSet rs = (FBResultSet) metaData.getSchemas()) {
                assertEquals("", rs.getExplainedExecutionPlan(),
                        "Non-statement-based result set has no detailed execution plan");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testHoldabilityStatement(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final int recordCount = 10;

            createTestData(recordCount, connection);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY,
                    HOLD_CURSORS_OVER_COMMIT);
                 Statement stmt2 = connection.createStatement()) {
                // execute first query
                ResultSet rs = stmt.executeQuery(SELECT_TEST_TABLE);

                // now execute another query, causes commit in auto-commit mode
                stmt2.executeQuery("SELECT * FROM rdb$database");

                // now let's access the result set
                int actualCount = 0;
                assertEquals(HOLD_CURSORS_OVER_COMMIT, rs.getHoldability(), "Unexpected holdability");
                while (rs.next()) {
                    rs.getString(1);
                    actualCount++;
                }
                assertEquals(recordCount, actualCount, "Unexpected number of reads from holdable result set");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testHoldabilityPreparedStatement(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            final int recordCount = 10;

            createTestData(recordCount, connection);

            try (PreparedStatement stmt = connection.prepareStatement(SELECT_TEST_TABLE, TYPE_SCROLL_INSENSITIVE,
                    CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT);
                 Statement stmt2 = connection.createStatement()) {
                // execute first query
                ResultSet rs = stmt.executeQuery();

                // now execute another query, causes commit in auto-commit mode
                stmt2.executeQuery("SELECT * FROM rdb$database");

                // now let's access the result set
                int actualCount = 0;
                assertEquals(HOLD_CURSORS_OVER_COMMIT, rs.getHoldability(), "Unexpected holdability");
                while (rs.next()) {
                    rs.getString(1);
                    actualCount++;
                }
                assertEquals(recordCount, actualCount, "Unexpected number of reads from holdable resultset");
            }
        }
    }

    @Test
    void testFetchSize() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            final int requestedFetchSize = 3;
            try (Statement stmt = connection.createStatement()) {
                int fetchSize = stmt.getFetchSize();
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    assertEquals(fetchSize, rs.getFetchSize(),
                            "Default stmt fetch size must match ResultSet fetch size");
                }

                stmt.setFetchSize(requestedFetchSize);
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    assertEquals(requestedFetchSize, rs.getFetchSize(),
                            "ResultSet fetchsize must match Statement fetchSize");
                }
            }
        }
    }

    @Test
    void testDoubleNext() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM rdb$database");
                assertTrue(rs.next(), "Should find at least one row");
                assertFalse(rs.next(), "Should find only one row");
                assertFalse(rs.next(), "Should not throw when after next");
            }
            connection.setAutoCommit(true);
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testInsertUpdatableCursor(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE)) {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    // rs.next();
                    rs.moveToInsertRow();
                    rs.updateInt("id", 1);
                    rs.updateString("blob_str", "test");
                    rs.updateString("CamelStr", "quoted string");
                    try {
                        rs.updateRow();
                        fail("Should fail, since updateRow() is used to update rows");
                    } catch (SQLException ex) {
                        // ok, let's try to insert row
                        rs.insertRow();
                    }
                }

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    assertTrue(rs.next(), "Should have at least one row");
                    assertEquals(1, rs.getInt("id"), "First record should have ID=1");
                    assertEquals("test", rs.getString("blob_str"), "BLOB should be also saved");
                    assertFalse(rs.next(), "Should have only one row");
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testMetaDataQueryShouldKeepRsOpen(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
                 ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                try (ResultSet bestRowId = connection.getMetaData()
                        .getBestRowIdentifier(null, null, "TEST_TABLE", 1, false)) {
                    assertTrue(bestRowId.next(), "Should have row ID");
                }

                rs.next();
            } catch (SQLException ex) {
                fail("Should throw no exception that result set is closed");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableResultSetMultipleStatements(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            int recordCount = 10;
            createTestData(recordCount, id -> "oldString" + id, connection, "long_str");

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {

                rs.first();

                try (PreparedStatement anotherStmt = stmt.getConnection()
                        .prepareStatement("SELECT * FROM rdb$database")) {
                    try (ResultSet anotherRs = anotherStmt.executeQuery()) {
                        while (anotherRs.next()) {
                            anotherRs.getObject(1);
                        }
                    }

                    assertThrows(SQLException.class, () -> {
                        rs.updateInt("id", 1);
                        rs.updateString("blob_str", "test");
                        rs.updateNull("str");
                        rs.updateRow();
                    });
                }
            }
        }
    }

    @Test
    void testRelAlias() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             // execute first query
             ResultSet rs = stmt.executeQuery(
                     "SELECT a.rdb$description, b.rdb$character_set_name " +
                             "FROM rdb$database a, rdb$database b " +
                             "where a.rdb$relation_id = b.rdb$relation_id")) {
            // now let's access the result set
            assertTrue(rs.next());

            FirebirdResultSetMetaData frsMeta = (FirebirdResultSetMetaData) rs.getMetaData();

            assertEquals("A", frsMeta.getTableAlias(1));
            assertEquals("B", frsMeta.getTableAlias(2));
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableHoldableResultSet(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            int recordCount = 10;
            createTestData(recordCount, id -> "oldString" + id, connection, "long_str");

            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE,
                    HOLD_CURSORS_OVER_COMMIT)) {
                try (ResultSet rs = stmt.executeQuery("SELECT id, long_str FROM test_table")) {
                    while (rs.next()) {
                        rs.updateString(2, rs.getString(2) + "a");
                        rs.updateRow();
                        connection.commit();
                    }
                }

                int counter = 1;

                try (ResultSet rs = stmt.executeQuery("SELECT id, long_str FROM test_table")) {
                    while (rs.next()) {
                        assertEquals("oldString" + counter + "a", rs.getString(2));
                        counter++;
                    }
                }
            }
        }
    }

    @Test
    void testClosedOnCommit() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
                assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, rs.getHoldability(), "Unexpected holdability");
                assertFalse(rs.isClosed(), "Expected result set to be open");

                connection.commit();
                assertTrue(rs.isClosed(), "Expected result set to be closed");
            }
        }
    }

    @Test
    void testClosedOnRollback() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
                assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, rs.getHoldability(), "Unexpected holdability");
                assertFalse(rs.isClosed(), "Expected result set to be open");

                connection.rollback();
                assertTrue(rs.isClosed(), "Expected result set to be closed");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableBinaryStream(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                stmt.executeUpdate("insert into test_table(id, blob_bin) values (1, null)");

                byte[] value = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
                connection.setAutoCommit(false);
                try (ResultSet rs = stmt.executeQuery("select id, blob_bin from test_table")) {
                    assertTrue(rs.next());
                    ByteArrayInputStream bais = new ByteArrayInputStream(value);
                    rs.updateBinaryStream(2, bais);
                    rs.updateRow();
                }

                try (ResultSet rs = stmt.executeQuery("select id, blob_bin from test_table")) {
                    assertTrue(rs.next());

                    assertArrayEquals(value, rs.getBytes(2));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableBinaryStream_intLength(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                stmt.executeUpdate("insert into test_table(id, blob_bin) values (1, null)");

                byte[] value = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
                connection.setAutoCommit(false);
                try (ResultSet rs = stmt.executeQuery("select id, blob_bin from test_table")) {
                    assertTrue(rs.next());
                    ByteArrayInputStream bais = new ByteArrayInputStream(value);
                    rs.updateBinaryStream(2, bais, 5);
                    rs.updateRow();
                }

                try (ResultSet rs = stmt.executeQuery("select id, blob_bin from test_table")) {
                    assertTrue(rs.next());

                    assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, rs.getBytes(2));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableCharacterStream(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                stmt.executeUpdate("insert into test_table(id, blob_str) values (1, null)");

                String value = "String for testing";
                connection.setAutoCommit(false);
                try (ResultSet rs = stmt.executeQuery("select id, blob_str from test_table")) {
                    assertTrue(rs.next());
                    StringReader stringReader = new StringReader(value);
                    rs.updateCharacterStream(2, stringReader);
                    rs.updateRow();
                }

                try (ResultSet rs = stmt.executeQuery("select id, blob_str from test_table")) {
                    assertTrue(rs.next());

                    assertEquals(value, rs.getString(2));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdatableCharacterStream_intLength(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                stmt.executeUpdate("insert into test_table(id, blob_str) values (1, null)");

                String value = "String for testing";
                connection.setAutoCommit(false);
                try (ResultSet rs = stmt.executeQuery("select id, blob_str from test_table")) {
                    assertTrue(rs.next());
                    StringReader stringReader = new StringReader(value);
                    rs.updateCharacterStream(2, stringReader, 6);
                    rs.updateRow();
                }

                try (ResultSet rs = stmt.executeQuery("select id, blob_str from test_table")) {
                    assertTrue(rs.next());

                    assertEquals("String", rs.getString(2));
                }
            }
        }
    }

    @Test
    void testGetMetaDataThrowsSQLExceptionAfterClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs;
            rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            assertFalse(rs.isClosed(), "Expected result set to be open");
            rs.close();

            assertTrue(rs.isClosed(), "Expected result set to be closed");

            SQLException exception = assertThrows(SQLException.class, rs::getMetaData);
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_CURSOR_STATE));
        }
    }

    @Test
    void testGetMetaDataThrowsSQLExceptionAfterConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs;
            rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            assertFalse(rs.isClosed(), "Expected result set to be open");
            connection.close();

            assertTrue(rs.isClosed(), "Expected result set to be closed");

            SQLException exception = assertThrows(SQLException.class, rs::getMetaData);
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_CURSOR_STATE));
        }
    }

    @Test
    void testInheritsFetchDirectionFromStatement() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                assertEquals(FETCH_FORWARD, rs.getFetchDirection(), "Unexpected fetch direction");
            }

            stmt.setFetchDirection(FETCH_REVERSE);

            // Note we inherit FETCH_REVERSE even though we are forward only, the JDBC spec is a bit ambiguous here,
            // because FETCH_REVERSE is allowed to be set on a forward only statement, but not on a forward only result set...
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                assertEquals(FETCH_REVERSE, rs.getFetchDirection(), "Unexpected fetch direction");
            }
        }
    }

    @Test
    void testSetFetchDirection_Forward() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.setFetchDirection(FETCH_UNKNOWN);

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                rs.setFetchDirection(FETCH_FORWARD);

                assertEquals(FETCH_FORWARD, rs.getFetchDirection(), "Unexpected fetch direction");
            }
        }
    }

    @Test
    void testSetFetchDirection_closedResultSet_throwsException() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            rs.close();

            SQLException sqlException = assertThrows(SQLException.class, () -> rs.setFetchDirection(FETCH_FORWARD));
            assertThat(sqlException, message(containsString("result set is closed")));
        }
    }

    @Test
    void testGetFetchDirection_closedResultSet_throwsException() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            rs.close();

            SQLException sqlException = assertThrows(SQLException.class, rs::getFetchDirection);
            assertThat(sqlException, message(containsString("result set is closed")));
        }
    }

    @Test
    void testSetFetchDirection_Reverse_onForwardOnlyThrowsException() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {

            SQLNonTransientException sqlException =
                    assertThrows(SQLNonTransientException.class, () -> rs.setFetchDirection(FETCH_REVERSE));
            assertThat(sqlException, fbMessageStartsWith(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly));
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testSetFetchDirection_Reverse_onScrollable(String scrollableCursorPropertyValue) throws SQLException {
        try (Connection connection = createConnection(scrollableCursorPropertyValue);
             Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            rs.setFetchDirection(FETCH_REVERSE);

            assertEquals(FETCH_REVERSE, rs.getFetchDirection(), "Unexpected fetch direction");
        }
    }

    @Test
    void testSetFetchDirection_Unknown_onForwardOnlyThrowsException() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {

            SQLNonTransientException sqlException =
                    assertThrows(SQLNonTransientException.class, () -> rs.setFetchDirection(FETCH_UNKNOWN));
            assertThat(sqlException, fbMessageStartsWith(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly));
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testSetFetchDirection_Unknown_onScrollable(String scrollableCursorPropertyValue) throws SQLException {
        try (Connection connection = createConnection(scrollableCursorPropertyValue);
             Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            rs.setFetchDirection(FETCH_UNKNOWN);

            assertEquals(FETCH_UNKNOWN, rs.getFetchDirection(), "Unexpected fetch direction");
        }
    }

    @Test
    void testSetFetchDirection_InvalidValue() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {

            //noinspection MagicConstant
            SQLException sqlException = assertThrows(SQLException.class, () -> rs.setFetchDirection(-1));
            assertThat(sqlException, allOf(
                    not(instanceOf(SQLFeatureNotSupportedException.class)),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_invalidFetchDirection, "-1"),
                    sqlState(equalTo("HY106"))));
        }
    }

    /**
     * Rationale: rdb$db_key column is actually identified as DB_KEY in result set
     */
    @Test
    void testRetrievalOfDbKeyByRDB$DB_KEY() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);

            try (PreparedStatement pstmt = connection.prepareStatement("select rdb$db_key, id from test_table");
                 ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");

                RowId rowId = rs.getRowId("RDB$DB_KEY");
                assertNotNull(rowId);
            }
        }
    }

    /**
     * Rationale: rdb$db_key column is actually identified as DB_KEY in result set
     */
    @Test
    void testRetrievalOfDbKeyByDB_KEY() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);

            try (PreparedStatement pstmt = connection.prepareStatement("select rdb$db_key, id from test_table");
                 ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");

                RowId rowId = rs.getRowId("DB_KEY");
                assertNotNull(rowId);
            }
        }
    }

    /**
     * Rationale: see <a href="http://tracker.firebirdsql.org/browse/JDBC-623">JDBC-623</a>.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testResultSetUpdateDoesNotNullUntouchedBlob(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            final String blob_str_value = "blob_str_value";
            final byte[] blob_bin_value = "blob_bin_value".getBytes(StandardCharsets.US_ASCII);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into test_table (id, long_str, blob_str, blob_bin) values (?, ?, ?, ?)")) {
                pstmt.setInt(1, 1);
                pstmt.setString(2, "long_str_initial");
                pstmt.setString(3, blob_str_value);
                pstmt.setBytes(4, blob_bin_value);
                pstmt.execute();
            }

            try (Statement stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 ResultSet rs = stmt.executeQuery("select id, long_str, blob_str, blob_bin from test_table")) {
                assertTrue(rs.next(), "expected a row");
                rs.updateString("long_str", "long_str_updated");
                rs.updateRow();
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("select id, long_str, blob_str, blob_bin from test_table")) {
                assertTrue(rs.next(), "expected a row");
                assertEquals(1, rs.getInt("id"), "id");
                assertEquals("long_str_updated", rs.getString("long_str"), "long_str");
                assertEquals(blob_str_value, rs.getString("blob_str"), "blob_str");
                assertArrayEquals(blob_bin_value, rs.getBytes("blob_bin"), "blob_bin");
            }
        }
    }

    /**
     * Rationale: see <a href="https://github.com/FirebirdSQL/jaybird/issues/689">jaybird#689</a> and
     * <a href="https://github.com/FirebirdSQL/jaybird/issues/690">jaybird#690</a>.
     */
    @Test
    void testIsAfterLast_bug689() throws Exception {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery("select * from RDB$DATABASE")) {

            while (rs.next()) {
                assertNotAfterLast(rs);
            }

            assertAfterLast(rs);
        }
    }

    /**
     * Test with a sufficient number of rows to trigger multiple fetches, including async fetches if supported.
     */
    @Test
    void testRequiringMultipleFetches() throws Exception {
        try (var connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(999, connection);
            try (var stmt = connection.createStatement()) {
                // Reduce fetch size to trigger more fetches
                stmt.setFetchSize(200);
                try (var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    int expectedId = 0;
                    while (rs.next()) {
                        expectedId++;
                        assertEquals(expectedId, rs.getInt(1));
                        assertEquals(String.valueOf(expectedId), rs.getString(2));
                    }
                    assertEquals(999, expectedId);
                }
            }
        }
    }

    /**
     * Rationale: see <a href="https://github.com/FirebirdSQL/jaybird/issues/807">jaybird#807</a>
     */
    @ParameterizedTest
    @MethodSource
    void testIsBeforeFirst_isAfterLast_emptyResultSet_bug807(int resultSetType, int resultSetConcurrency,
            String scrollableCursorPropertyValue) throws SQLException {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (var stmt = connection.createStatement(resultSetType, resultSetConcurrency);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertNotBeforeFirst(rs, "isBeforeFirst for empty result set should be false before next");
                assertNotAfterLast(rs, "isAfterLast for empty result set should be false before next");
                assertNoNextRow(rs, "Expected no row in empty result set");
                assertNotBeforeFirst(rs, "isBeforeFirst for empty result set should be false after next");
                assertNotAfterLast(rs, "isAfterLast for empty result set should be false after next");
            }
        }
    }

    static Stream<Arguments> testIsBeforeFirst_isAfterLast_emptyResultSet_bug807() {
        Stream<Arguments> defaultArguments = Stream.of(
                Arguments.of(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, SCROLLABLE_CURSOR_EMULATED),
                Arguments.of(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE, SCROLLABLE_CURSOR_EMULATED),
                Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY, SCROLLABLE_CURSOR_EMULATED),
                Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, SCROLLABLE_CURSOR_EMULATED));
        if (getDefaultSupportInfo().supportsScrollableCursors()) {
            return Stream.concat(defaultArguments, Stream.of(
                    Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY, SCROLLABLE_CURSOR_SERVER),
                    Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, SCROLLABLE_CURSOR_SERVER)));
        }
        return defaultArguments;
    }

    /**
     * Rationale: see <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2024-03-do-not-close-result-set-after-last-row-in-auto-commit.adoc">jdp-2024-03: Do not close result set after last row in auto-commit</a>
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testNextAfterLastRowShouldNotCloseResultSet(boolean autoCommit) throws Exception {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement()) {
            connection.setAutoCommit(autoCommit);
            var rs = stmt.executeQuery("select 1 from rdb$database");
            assertNextRow(rs);
            assertNoNextRow(rs);
            assertResultSetOpen(rs, "ResultSet should not be closed after next returned false");
        }
    }

    @ParameterizedTest
    @MethodSource
    void wasNull_onInsertRow(int resultSetType, String scrollableCursorPropertyValue) throws Exception {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);

            try (var stmt = connection.createStatement(resultSetType, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                rs.moveToInsertRow();
                rs.updateNull(1);
                rs.updateString(2, "1 s");

                assertNull(rs.getObject(1), "column 1");
                assertTrue(rs.wasNull(), "column 1");
                assertEquals("1 s", rs.getObject(2), "column 2");
                assertFalse(rs.wasNull(), "column 2");
            }
        }
    }

    static Stream<Arguments> wasNull_onInsertRow() {
        var defaultArguments = Stream.of(
                Arguments.of(TYPE_FORWARD_ONLY, SCROLLABLE_CURSOR_EMULATED),
                Arguments.of(TYPE_SCROLL_INSENSITIVE, SCROLLABLE_CURSOR_EMULATED));
        if (getDefaultSupportInfo().supportsScrollableCursors()) {
            return Stream.concat(defaultArguments, Stream.of(
                    Arguments.of(TYPE_SCROLL_INSENSITIVE, SCROLLABLE_CURSOR_SERVER)));
        }
        return defaultArguments;
    }

    @Test
    void testWasNull() throws Exception {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery(
                     "select cast(null as integer), 1, 'abc', cast(null as char(3)) from rdb$database")) {
            assertWasNotNull(rs, "wasNull == false before row");
            assertNextRow(rs);
            assertWasNotNull(rs, "wasNull == false on row, before requesting value");
            assertEquals(0, rs.getInt(1), "Expected default value 0 for getInt on NULL (first column)");
            assertWasNull(rs, "wasNull == true after requesting null value");
            assertEquals(1, rs.getInt(2), "Expected actual value of second column");
            assertWasNotNull(rs, "wasNull == false after requesting non-null value");
            assertEquals("abc", rs.getObject(3), "Expected actual value of third column");
            assertWasNotNull(rs, "wasNull == false after requesting non-null value");
            assertNull(rs.getObject(4), "Expected null value of fourth column");
            assertWasNull(rs, "wasNull == true after requesting null value");
            assertNoNextRow(rs);
            assertWasNotNull(rs, "wasNull == false after end of result set");
            rs.close();
            var e = assertThrows(SQLException.class, rs::wasNull, "wasNull not allowed after result set close");
            assertThat(e, message(equalTo("The result set is closed")));
        }
    }

    /**
     * Rationale: see <a href="https://github.com/FirebirdSQL/jaybird/issues/819">#819</a>.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void insertRowImmediatelyAfterExecuteQuery_doesNotReportRowTwice(String scrollableCursorPropertyValue)
            throws Exception {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                rs.moveToInsertRow();
                rs.updateInt(1, 2);
                rs.updateString(2, "2");
                rs.insertRow();
                rs.moveToCurrentRow();
                rs.beforeFirst();

                List<List<Object>> expectedRows = List.of(List.of(1, "1"), List.of(2, "2"));

                for (int i = 0; i < expectedRows.size(); i++) {
                    List<Object> expectedRow = expectedRows.get(i);
                    assertNextRow(rs);
                    assertRowEquals("row " + i, rs, expectedRow);
                }
                assertNoNextRow(rs, "expected only " + expectedRows.size() + " rows");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void scrollableCursor_detectsOwnUpdates(String scrollableCursorPropertyValue) throws Exception {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(2, connection);

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertAbsoluteRow(rs, 1);
                rs.updateString(2, "updated");
                rs.updateRow();

                assertRowUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);

                // second row untouched
                assertNextRow(rs);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);

                // check first row again
                assertPreviousRow(rs);
                assertRowUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);

                assertRowEquals("expected updated values", rs, List.of(1, "updated"));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void scrollableCursor_detectsOwnDeletes(String scrollableCursorPropertyValue) throws Exception {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(2, connection);

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertAbsoluteRow(rs, 1);
                rs.deleteRow();

                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowDeleted(rs);

                // second row untouched
                assertNextRow(rs);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);

                // check first row again
                assertPreviousRow(rs);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowDeleted(rs);

                assertRowEquals("expected all-null row", rs, Arrays.asList(null, null));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void scrollableCursor_detectsOwnInserts(String scrollableCursorPropertyValue) throws Exception {
        try (var connection = createConnection(scrollableCursorPropertyValue)) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(2, connection);

            List<Object> firstRow = List.of(1, "1");
            List<Object> secondRow = List.of(2, "2");

            try (var stmt = connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                 var rs = stmt.executeQuery(SELECT_TEST_TABLE)) {
                // first row untouched
                assertNextRow(rs);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);
                assertRowEquals("expected first row", rs, firstRow);

                rs.moveToInsertRow();
                rs.updateInt(1, 3);
                rs.updateString(2, "inserted");
                rs.insertRow();

                // back to first row
                rs.moveToCurrentRow();
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);
                assertRowEquals("expected first row", rs, firstRow);

                // second row untouched
                assertNextRow(rs);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);
                assertRowEquals("expected second row", rs, secondRow);

                // check third row inserted
                assertNextRow(rs);
                assertRowNotUpdated(rs);
                assertRowInserted(rs);
                assertRowNotDeleted(rs);
                List<Object> thirdRow = List.of(3, "inserted");
                assertRowEquals("expected inserted row", rs, thirdRow);

                // check first row again
                assertAbsoluteRow(rs, 1);
                assertRowNotUpdated(rs);
                assertRowNotInserted(rs);
                assertRowNotDeleted(rs);
                assertRowEquals("expected first row", rs, firstRow);

                // check third row again
                assertAbsoluteRow(rs, -1);
                assertRowNotUpdated(rs);
                assertRowInserted(rs);
                assertRowNotDeleted(rs);
                assertRowEquals("expected inserted row", rs, thirdRow);
            }
        }
    }

    @Test
    void updateObject_int_Reader_type_scaleOrLength() throws SQLException {
        try (var connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);
            String sourceValue = "1234567890";
            final int usedLength = 5;

            try (var stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
                var rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);

                rs.updateObject(2, new StringReader(sourceValue), JDBCType.VARCHAR, usedLength);
                rs.updateRow();

                rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);
                assertRowEquals(rs, List.of(1, sourceValue.substring(0, usedLength)));
            }
        }
    }

    @Test
    void updateObject_String_Reader_type_scaleOrLength() throws SQLException {
        try (var connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);
            String sourceValue = "1234567890";
            final int usedLength = 5;

            try (var stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
                var rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);

                rs.updateObject("str", new StringReader(sourceValue), JDBCType.VARCHAR, usedLength);
                rs.updateRow();

                rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);
                assertRowEquals(rs, List.of(1, sourceValue.substring(0, usedLength)));
            }
        }
    }

    //

    @Test
    void updateObject_int_InputStream_type_scaleOrLength() throws SQLException {
        try (var connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);
            String sourceValue = "1234567890";
            final int usedLength = 5;

            try (var stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
                var rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);

                rs.updateObject(2, new ByteArrayInputStream(sourceValue.getBytes(StandardCharsets.US_ASCII)),
                        JDBCType.VARCHAR, usedLength);
                rs.updateRow();

                rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);
                assertRowEquals(rs, List.of(1, sourceValue.substring(0, usedLength)));
            }
        }
    }

    @Test
    void updateObject_String_InputStream_type_scaleOrLength() throws SQLException {
        try (var connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_STATEMENT);
            createTestData(1, connection);
            String sourceValue = "1234567890";
            final int usedLength = 5;

            try (var stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
                var rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);

                rs.updateObject("str", new ByteArrayInputStream(sourceValue.getBytes(StandardCharsets.US_ASCII)),
                        JDBCType.VARCHAR, usedLength);
                rs.updateRow();

                rs = stmt.executeQuery(SELECT_TEST_TABLE);
                assertNextRow(rs);
                assertRowEquals(rs, List.of(1, sourceValue.substring(0, usedLength)));
            }
        }
    }

    static Stream<String> scrollableCursorPropertyValues() {
        // We are unconditionally emitting SERVER, to check if the value behaves appropriately on versions that do
        // not support server-side scrollable cursors
        return Stream.of(SCROLLABLE_CURSOR_EMULATED, SCROLLABLE_CURSOR_SERVER);
    }

    private static Connection createConnection(String scrollableCursorPropertyValue) throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.scrollableCursor, scrollableCursorPropertyValue);
        return DriverManager.getConnection(getUrl(), props);
    }

    private void createTestData(int recordCount, Connection connection) throws SQLException {
        createTestData(recordCount, String::valueOf, connection, "str");
    }

    private void createTestData(int recordCount, IntFunction<String> strValueFunction, Connection connection,
            String stringColumn) throws SQLException {
        boolean currentAutoCommit = connection.getAutoCommit();
        if (currentAutoCommit) {
            connection.setAutoCommit(false);
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO test_table (id, " + stringColumn + ") VALUES(?, ?)")) {
            for (int i = 1; i <= recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, strValueFunction.apply(i));
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            if (currentAutoCommit) {
                connection.setAutoCommit(true);
            }
        }
    }
}
