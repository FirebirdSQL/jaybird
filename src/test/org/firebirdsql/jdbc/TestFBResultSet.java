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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class TestFBResultSet extends FBJUnit4TestBase {

    //@formatter:off
    private static final String SELECT_STATEMENT =
        "SELECT " +
        "  1 AS col1," +
        "  2 AS \"col1\"," +
        "  3 AS \"Col1\""  +
        "FROM rdb$database";

    private static final String CREATE_TABLE_STATEMENT =
        "CREATE TABLE test_table(" +
        "  id INTEGER NOT NULL PRIMARY KEY," +
        "  str VARCHAR(10)," +
        "  long_str VARCHAR(255)," +
        "  very_long_str VARCHAR(20000)," +
        "  blob_str BLOB SUB_TYPE TEXT," +
        "  \"CamelStr\" VARCHAR(255)," +
        "  blob_bin BLOB SUB_TYPE BINARY" +
        ")";

    private static final String SELECT_TEST_TABLE =
        "SELECT id, str FROM test_table";

    private static final String CREATE_TABLE_STATEMENT2 =
        "CREATE TABLE test_table2(" +
        "  id INTEGER NOT NULL, " +
        "  str VARCHAR(10), " +
        "  long_str VARCHAR(255), " +
        "  very_long_str VARCHAR(20000), " +
        "  blob_str BLOB SUB_TYPE 1, " +
        "  \"CamelStr\" VARCHAR(255)" +
        ")";

    private static final String CREATE_VIEW_STATEMENT =
        "CREATE VIEW test_empty_string_view(marker, id, empty_char) " +
        "  AS  " +
        "  SELECT " +
        "    CAST('marker' AS VARCHAR(6)), " +
        "    id, " +
        "    '' " +
        "  FROM " +
        "    test_table";

    private static final String CREATE_SUBSTR_FUNCTION =
        "DECLARE EXTERNAL FUNCTION substr " +
        "  CSTRING(80), SMALLINT, SMALLINT " +
        "RETURNS CSTRING(80) FREE_IT " +
        "ENTRY_POINT 'IB_UDF_substr' MODULE_NAME 'ib_udf'";

    private static final String SELECT_FROM_VIEW_STATEMENT =
        "SELECT * FROM test_empty_string_view";

    private static final String INSERT_INTO_TABLE_STATEMENT =
        "INSERT INTO test_table (id, str) VALUES(?, ?)";

    private static final String INSERT_LONG_STR_STATEMENT =
        "INSERT INTO test_table (id, long_str) VALUES(?, ?)";

    private static final String CURSOR_NAME = "some_cursor";

    private static final String UPDATE_TABLE_STATEMENT =
        "UPDATE test_table SET str = ? WHERE CURRENT OF " + CURSOR_NAME;
    //@formatter:on

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @After
    public void tearDown() throws Exception {
        closeQuietly(connection);
    }

    /**
     * Test if all columns are found correctly.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testFindColumn() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_STATEMENT)) {
            assertTrue("Should have at least one row.", rs.next());

            assertEquals("COL1 should be 1.", 1, rs.getInt("COL1"));
            assertEquals("col1 should be 1.", 1, rs.getInt("col1"));
            assertEquals("\"col1\" should be 2.", 2, rs.getInt("\"col1\""));
            assertEquals("Col1 should be 1.", 1, rs.getInt("Col1"));
        }
    }

    /**
     * Test if positioned updates work correctly.
     *
     * @throws java.lang.Exception
     *         if something went wrong.
     */
    @Test
    public void testPositionedUpdate() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        connection.setAutoCommit(false);

        try (Statement select = connection.createStatement()) {
            select.setCursorName(CURSOR_NAME);
            ResultSet rs = select.executeQuery("SELECT id, str FROM test_table FOR UPDATE OF " + CURSOR_NAME);

            assertTrue("ResultSet.isBeforeFirst() should be true.", rs.isBeforeFirst());

            try (PreparedStatement update = connection.prepareStatement(UPDATE_TABLE_STATEMENT)) {
                int counter = 0;

                while (rs.next()) {
                    if (counter == 0) {
                        assertTrue("ResultSet.isFirst() should be true", rs.isFirst());
                    } else if (counter == recordCount - 1) {
                        try {
                            rs.isLast();
                            assertTrue("ResultSet.isLast() should be true", false);
                        } catch (SQLException ex) {
                            // TODO Ignoring exception probably wrong
                            // correct
                        }
                    }

                    counter++;

                    assertEquals("ResultSet.getRow() should be correct", counter, rs.getRow());

                    update.setInt(1, rs.getInt(1) + 1);
                    int updatedCount = update.executeUpdate();

                    assertEquals("Number of update rows", 1, updatedCount);
                }

                assertTrue("ResultSet.isAfterLast() should be true", rs.isAfterLast());
                assertTrue("ResultSet.next() should return false.", !rs.next());
            }
        }
        connection.commit();

        try (Statement select = connection.createStatement();
             ResultSet rs = select.executeQuery("SELECT id, str FROM test_table")) {
            int counter = 0;

            assertTrue("ResultSet.isBeforeFirst() should be true", rs.isBeforeFirst());

            while (rs.next()) {
                if (counter == 0) {
                    assertTrue("ResultSet.isFirst() should be true", rs.isFirst());
                } else if (counter == recordCount - 1) {
                    assertTrue("ResultSet.isLast() should be true", rs.isLast());
                }

                counter++;

                int idValue = rs.getInt(1);
                int strValue = rs.getInt(2);

                assertEquals("Value of str column must be equal to id + 1", idValue + 1, strValue);
            }

            assertTrue("ResultSet.isAfterLast() should be true", rs.isAfterLast());
            assertTrue("ResultSet.next() should return false.", !rs.next());
        }
        connection.commit();
    }

    /**
     * This test checks if an empty column in a view is correctly returned
     * to the client.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testEmptyColumnInView() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        executeCreateTable(connection, CREATE_VIEW_STATEMENT);

        try (PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT)) {
            for (int i = 0; i < 10; i++) {
                ps.setInt(1, i);
                ps.setString(2, "");
                ps.executeUpdate();
            }
        }

        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_FROM_VIEW_STATEMENT)) {
            int counter = 0;
            while (rs.next()) {
                String marker = rs.getString(1);
                int key = rs.getInt(2);
                String value = rs.getString(3);

                assertEquals("Marker should be correct.", "marker", marker);
                assertEquals("Key should be same as counter.", counter, key);
                assertEquals("EMPTY_CHAR string should be empty.", "", value);

                counter++;
            }

            assertEquals("Should read 10 records", 10, counter);
        }

        connection.setAutoCommit(true);
    }

    /**
     * Test cursor scrolling in case of ResultSet.TEST_SCROLL_INSENSITIVE.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testScrollInsensitive() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
            int testValue;

            rs.last();
            testValue = recordCount - 1;
            assertEquals("ID of last record should be equal to " + testValue, testValue, rs.getInt(1));
            assertTrue("isLast() should return true", rs.isLast());

            rs.absolute(recordCount / 2);
            testValue = recordCount / 2 - 1;
            assertEquals("ID after absolute positioning should return " + testValue, testValue, rs.getInt(1));

            rs.absolute(-1);
            testValue = recordCount - 1;
            assertEquals("ID after absolute positioning with negative position should return " + testValue,
                    testValue, rs.getInt(1));

            rs.first();
            testValue = 0;
            assertEquals("ID after first() should return " + testValue, testValue, rs.getInt(1));
            assertTrue("isFirst() should report true", rs.isFirst());

            boolean hasRow = rs.previous();
            assertTrue("Should not point to the row", !hasRow);
            assertTrue("isBeforeFirst() should return true", rs.isBeforeFirst());

            rs.relative(5);
            rs.relative(-4);
            testValue = 0;
            assertEquals("ID after relative positioning should return " + testValue, testValue, rs.getInt(1));

            rs.beforeFirst();
            assertTrue("isBeforeFirst() should return true", rs.isBeforeFirst());
            try {
                rs.getInt(1);
                fail("Should not be possible to access column if cursor does not point to a row.");
            } catch (SQLException ex) {
                // everything is fine
            }

            rs.afterLast();
            assertTrue("isAfterLast() should return true", rs.isAfterLast());
            try {
                rs.getInt(1);
                fail("Should not be possible to access column if cursor does not point to a row.");
            } catch (SQLException ex) {
                // everything is fine
            }
            assertTrue("ResultSet.next() should return false.", !rs.next());
        }
    }

    /**
     * Test {@link ResultSet#absolute(int)} cursor scrolling in case of ResultSet.TEST_SCROLL_INSENSITIVE.
     */
    @Test
    public void testScrollInsensitive_Absolute() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table");
            assertTrue("Should be before first", rs.isBeforeFirst());
            assertFalse("Should not be after last", rs.isAfterLast());

            assertTrue("Position 1 is in result set", rs.absolute(1));
            assertEquals(0, rs.getInt(1));
            assertFalse("Should not be before first", rs.isBeforeFirst());
            assertFalse("Should not be after last", rs.isAfterLast());

            assertFalse("Position 0 is outside result set", rs.absolute(0));
            assertFalse("Position 11 is outside result set", rs.absolute(11));
            assertFalse("Should not be before first", rs.isBeforeFirst());
            assertTrue("Should be after last", rs.isAfterLast());

            assertTrue("Position 10 is inside result set", rs.absolute(10));
            assertFalse("Should not be before first", rs.isBeforeFirst());
            assertFalse("Should not be after last", rs.isAfterLast());
            assertEquals(9, rs.getInt(1));

            assertFalse("Position 15 is outside result set", rs.absolute(15));
            assertFalse("Should not be before first", rs.isBeforeFirst());
            assertTrue("Should be after last", rs.isAfterLast());

            assertTrue("Position -1 is inside result set", rs.absolute(-1));
            assertFalse("Should not be before first", rs.isBeforeFirst());
            assertFalse("Should not be after last", rs.isAfterLast());
            assertEquals(9, rs.getInt(1));

            assertFalse("Position -11 is outside result set", rs.absolute(-11));
            assertTrue("Should be before first", rs.isBeforeFirst());
            assertFalse("Should not be after last", rs.isAfterLast());
        }
    }

    /**
     * This test case tries to reproduce a NPE reported in Firebird-Java group
     * by vmdd_tech after Jaybird 1.5 beta 1 release.
     *
     * @throws Exception
     *         if something goes wrong.
     */
    @Test
    public void testBugReport1() throws Exception {
        assumeTrue("Test requires UDF support", getDefaultSupportInfo().supportsNativeUserDefinedFunctions());
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        executeDDL(connection, CREATE_SUBSTR_FUNCTION);

        try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_LONG_STR_STATEMENT)) {
            insertStmt.setInt(1, 1);
            insertStmt.setString(2, "aaa");

            insertStmt.execute();

            insertStmt.setInt(1, 2);
            insertStmt.setString(2, "'more than 80 chars are in " +
                    "hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

            insertStmt.execute();

            insertStmt.setInt(1, 3);
            insertStmt.setString(2, "more than 80 chars are in " +
                    "hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

            insertStmt.execute();
        }

        final String query = "SELECT id, substr(long_str,1,2) FROM test_table ORDER BY id DESC";
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(query)) {
                assertTrue("Should have at least one row", rs.next());
            } catch (SQLException ex) {
                if (ex.getErrorCode() != ISCConstants.isc_string_truncation
                        && !ex.getMessage().contains("string truncation")) throw ex;
                // it is ok as well, since substr is declared as CSTRING(80)
                // and truncation error happens
                System.out.println("First query generated exception " + ex.getMessage());
            }

            try (ResultSet rs = stmt.executeQuery(query)) {
                assertTrue("Should have at least one row", rs.next());

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

    /**
     * Test if result set type and concurrency is correct.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testBugReport2() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
            assertTrue("Should have at least one row", rs.next());
            assertEquals("ResultSet type should be TYPE_SCROLL_INSENSITIVE",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, rs.getType());
            assertEquals("ResultSet concurrency should be CONCUR_READ_ONLY",
                    ResultSet.CONCUR_READ_ONLY, rs.getConcurrency());

            rs.last();

            assertEquals("ResultSet type should not change.", ResultSet.TYPE_SCROLL_INSENSITIVE, rs.getType());
        }
    }

    @Test
    public void testBugReport3() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        connection.setAutoCommit(true);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table")) {
            try {
                rs.first();
                fail("first() should not work in TYPE_FORWARD_ONLY result sets");
            } catch (SQLException ex) {
                // should fail, everything is fine.
            }

            while (rs.next()) {
                // do nothing, just loop.
            }

            try {
                rs.first();
                fail("first() should not work in TYPE_FORWARD_ONLY result sets.");
            } catch (SQLException ex) {
                // everything is fine
            }
        }
    }

    @Ignore
    @Test
    public void testMemoryGrowth() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("no_result_set_tracking", "");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            connection.setAutoCommit(false);

            System.out.println("Inserting...");
            int recordCount = 1;

            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO test_table(id, very_long_str) VALUES (?, ?)")) {
                byte[] string = createRandomByteString(19000);

                for (int i = 0; i < recordCount; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, new String(string));
                    ps.executeUpdate();
                }
            }

            connection.commit();

            System.gc();

            long memoryBeforeSelects = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            System.out.println("Selecting...");
            int selectRuns = 10000;
            for (int i = 0; i < selectRuns; i++) {
                if ((i % 1000) == 0) System.out.println("Select no. " + i);

                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    while (rs.next()) {
                        // just loop through result set
                    }
                }
            }
            System.gc();

            long memoryAfterSelects = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            connection.commit();

            System.gc();

            long memoryAfterCommit = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            System.out.println("Memory before selects " + memoryBeforeSelects);
            System.out.println("Memory after selects " + memoryAfterSelects);
            System.out.println("Memory after commit " + memoryAfterCommit);
            System.out.println("Commit freed " + (memoryAfterSelects - memoryAfterCommit));
        }
    }

    @Test
    public void testResultSetNotClosed() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        connection.setAutoCommit(false);

        final int recordCount = 1;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table(id, very_long_str) VALUES (?, ?)")) {
            byte[] string = createRandomByteString(19000);

            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, new String(string));
                ps.executeUpdate();
            }
        }

        connection.commit();
        connection.setAutoCommit(false);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM test_table WHERE id = ?")) {
            stmt.setInt(1, recordCount + 10);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Should not find any record", !rs.next());
            }

            stmt.setInt(1, recordCount - 1);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Should find a record", rs.next());
            }
        }
    }

    private byte[] createRandomByteString(int length) {
        Random random = new Random();
        byte[] string = new byte[length];
        for (int i = 0; i < length; i++) {
            string[i] = (byte) random.nextInt(128);
        }
        return string;
    }

    @Test
    public void testUpdatableResultSet() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        connection.setAutoCommit(false);

        final int recordCount = 10;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table(id, long_str) VALUES (?, ?)")) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        }

        connection.commit();

        connection.setAutoCommit(true);

        connection.clearWarnings();

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            assertNull("No warnings should be added", connection.getWarnings());

            try (ResultSet rs = stmt.executeQuery("SELECT id, long_str, str, \"CamelStr\" FROM test_table ORDER BY id")) {

                int counter = 0;
                while (rs.next()) {
                    int id = rs.getInt(1);
                    assertEquals(counter, id);

                    String longStr = rs.getString(2);
                    assertEquals("oldString" + counter, longStr);

                    rs.updateString(2, "newString" + counter);

                    assertEquals(counter, rs.getInt(1));
                    assertEquals("newString" + counter, rs.getString(2));

                    assertEquals(null, rs.getString(3));
                    rs.updateString(3, "str" + counter);

                    assertEquals(null, rs.getString(4));
                    rs.updateString(4, "str" + counter);

                    // check whether row can be updated
                    rs.updateRow();

                    // check whether row can be refreshed
                    rs.refreshRow();

                    assertEquals(counter, rs.getInt(1));
                    assertEquals("newString" + counter, rs.getString(2));
                    assertEquals("str" + counter, rs.getString(3));
                    assertEquals("str" + counter, rs.getString(4));

                    counter++;
                }

                assertTrue("Should process " + recordCount + " rows.", counter == recordCount);

                // check the insertRow() feature
                rs.moveToInsertRow();
                rs.updateInt(1, recordCount);
                rs.updateString(2, "newString" + recordCount);
                rs.updateString(3, "bug");
                rs.updateString(4, "quoted column");
                rs.insertRow();
                rs.moveToCurrentRow();

                // check whether newly inserted row can be updated
                rs.last();
                rs.updateString(3, "str" + recordCount);
                rs.updateRow();
            }

            try (ResultSet rs = stmt.executeQuery("SELECT id, long_str, str FROM test_table ORDER BY id")) {
                int counter = 0;
                while (rs.next()) {
                    int id = rs.getInt(1);
                    assertEquals(counter, id);

                    String longStr = rs.getString(2);
                    assertEquals("newString" + counter, longStr);
                    assertEquals("str" + counter, rs.getString(3));
                    counter++;

                    if (counter == recordCount + 1)
                        rs.deleteRow();
                }

                assertEquals(counter, recordCount + 1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM test_table")) {

                assertTrue(rs.next());
                assertEquals(recordCount, rs.getInt(1));
            }
        }
    }

    @Test
    public void testUpdatableResultSetNoPK() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT2);

        connection.setAutoCommit(false);

        final int recordCount = 10;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table2(id, long_str) VALUES (?, ?)")) {
            for (int i = 1; i <= recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        }

        connection.commit();
        connection.setAutoCommit(true);
        connection.clearWarnings();

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            assertNull("No warnings should be added", connection.getWarnings());

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT rdb$db_key, id, long_str, str, \"CamelStr\" FROM test_table2 ORDER BY 2")) {

                int counter = 1;
                while (rs.next()) {

                    int id = rs.getInt(2);
                    assertEquals(counter, id);

                    String longStr = rs.getString(3);
                    assertEquals("oldString" + counter, longStr);

                    rs.updateString(3, "newString" + counter);

                    assertEquals(counter, rs.getInt(2));
                    assertEquals("newString" + counter, rs.getString(3));

                    assertEquals(null, rs.getString(4));
                    rs.updateString(4, "str" + counter);

                    assertEquals(null, rs.getString(5));
                    rs.updateString(5, "str" + counter);

                    // check whether row can be updated
                    rs.updateRow();

                    // check whether row can be refreshed
                    rs.refreshRow();

                    assertEquals(counter, rs.getInt(2));
                    assertEquals("newString" + counter, rs.getString(3));
                    assertEquals("str" + counter, rs.getString(4));
                    assertEquals("str" + counter, rs.getString(5));

                    counter++;
                }
            }
        }
    }

    @Test
    public void testUpdatableStatementResultSetDowngradeToReadOnlyWhenQueryNotUpdatable() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        executeCreateTable(connection, CREATE_TABLE_STATEMENT2);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt.executeQuery("select * from test_table t1 left join test_table2 t2 on t1.id = t2.id")) {

            SQLWarning warning = stmt.getWarnings();
            assertThat(warning, allOf(
                    notNullValue(),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)));

            assertEquals("Expected downgrade to CONCUR_READ_ONLY", ResultSet.CONCUR_READ_ONLY, rs.getConcurrency());
        }
    }

    @Test
    public void testGetExecutionPlan() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement();
             FBResultSet rs = (FBResultSet) stmt.executeQuery("SELECT id, str FROM test_table")) {

            String execPlan = rs.getExecutionPlan();
            assertTrue("Execution plan should reference test_table", execPlan.toUpperCase().contains("TEST_TABLE"));
        }

        try (PreparedStatement pStmt = connection.prepareStatement("SELECT * FROM TEST_TABLE");
             FBResultSet rs = (FBResultSet) pStmt.executeQuery()) {
            String execPlan = rs.getExecutionPlan();
            assertTrue("Execution plan should reference test_table", execPlan.toUpperCase().contains("TEST_TABLE"));
        }

        // Ensure there isn't a crash when attempting to retrieve the
        // execution plan from a non-statement-based ResultSet
        java.sql.DatabaseMetaData metaData = connection.getMetaData();
        try (FBResultSet rs = (FBResultSet) metaData.getSchemas()) {
            assertEquals("Non-statement-based result set has no execution plan", "", rs.getExecutionPlan());
        }
    }

    @Test
    public void testHoldabilityStatement() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        final int recordCount = 10;

        createTestData(recordCount);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
                ResultSet.HOLD_CURSORS_OVER_COMMIT);
             Statement stmt2 = connection.createStatement()) {
            // execute first query
            FirebirdResultSet rs = (FirebirdResultSet) stmt.executeQuery(SELECT_TEST_TABLE);

            // now execute another query, causes commit in auto-commit mode
            stmt2.executeQuery("SELECT * FROM rdb$database");

            // now let's access the result set
            int actualCount = 0;
            assertEquals("Unexpected holdability", ResultSet.HOLD_CURSORS_OVER_COMMIT, rs.getHoldability());
            while (rs.next()) {
                rs.getString(1);
                actualCount++;
            }
            assertEquals("Unexpected number of reads from holdable result set", recordCount, actualCount);
        }
    }

    @Test
    public void testHoldabilityPreparedStatement() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        final int recordCount = 10;

        createTestData(recordCount);

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_TEST_TABLE, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
             Statement stmt2 = connection.createStatement()) {
            // execute first query
            FirebirdResultSet rs = (FirebirdResultSet) stmt.executeQuery();

            // now execute another query, causes commit in auto-commit mode
            stmt2.executeQuery("SELECT * FROM rdb$database");

            // now let's access the result set
            int actualCount = 0;
            assertEquals("Unexpected holdability", ResultSet.HOLD_CURSORS_OVER_COMMIT, rs.getHoldability());
            while (rs.next()) {
                rs.getString(1);
                actualCount++;
            }
            assertEquals("Unexpected number of reads from holdable resultset", recordCount, actualCount);
        }
    }

    @Test
    public void testFetchSize() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        final int FETCH_SIZE = 3;
        try (Statement stmt = connection.createStatement()) {
            int fetchSize = stmt.getFetchSize();
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                assertEquals("Default stmt fetch size must match ResultSet fetch size", fetchSize, rs.getFetchSize());
            }

            stmt.setFetchSize(FETCH_SIZE);
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                assertEquals("ResultSet fetchsize must match Statement fetchSize", FETCH_SIZE, rs.getFetchSize());
            }
        }
    }

    @Test
    public void testDoubleNext() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM rdb$database");
            assertTrue("Should find at least one row", rs.next());
            assertFalse("Should find only one row", rs.next());
            assertFalse("Should not throw when after next", rs.next());
        }
        connection.setAutoCommit(true);
    }

    @Test
    public void testInsertUpdatableCursor() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                // rs.next();
                rs.moveToInsertRow();
                rs.updateInt("id", 1);
                rs.updateString("blob_str", "test");
                rs.updateString("CamelStr", "quoted string");
                try {
                    rs.updateRow();
                    fail("Should fail, since updateRow() is used to update rows.");
                } catch (SQLException ex) {
                    // ok, let's try to insert row
                    rs.insertRow();
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                assertTrue("Should have at least one row", rs.next());
                assertEquals("First record should have ID=1", 1, rs.getInt("id"));
                assertEquals("BLOB should be also saved", "test", rs.getString("blob_str"));
                assertTrue("Should have only one row.", !rs.next());
            }
        }
    }

    @Test
    public void testMetaDataQueryShouldKeepRsOpen() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
            try (ResultSet bestRowId = connection.getMetaData().getBestRowIdentifier(null, null, "TEST_TABLE", 1,
                    false)) {
                assertTrue("Should have row ID", bestRowId.next());
            }

            rs.next();
        } catch (SQLException ex) {
            fail("Should throw no exception that result set is closed.");
        }
    }

    @Test
    public void testUpdatableResultSetMultipleStatements() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        int recordCount = 10;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO test_table(id, long_str) VALUES (?, ?)")) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        }

        connection.setAutoCommit(true);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {

            rs.first();

            try (PreparedStatement anotherStmt = stmt.getConnection().prepareStatement("SELECT * FROM rdb$database")) {
                try (ResultSet anotherRs = anotherStmt.executeQuery()) {
                    while (anotherRs.next()) {
                        anotherRs.getObject(1);
                    }
                }

                try {
                    rs.updateInt("id", 1);
                    rs.updateString("blob_str", "test");
                    rs.updateNull("str");
                    rs.updateRow();

                    fail("Should produce exception.");
                } catch (SQLException ex) {
                    // everything is ok
                }
            }

            rs.close();
        }
        connection.setAutoCommit(true);
    }

    @Test
    public void testRelAlias() throws Exception {
        try (Statement stmt = connection.createStatement();
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

    @Test
    public void testUpdatableHoldableResultSet() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        connection.setAutoCommit(true);

        int recordCount = 10;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table(id, long_str) VALUES (?, ?)")) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        }

        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
                ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            try (ResultSet rs = stmt.executeQuery("SELECT id, long_str FROM test_table")) {
                while (rs.next()) {
                    rs.updateString(2, rs.getString(2) + "a");
                    rs.updateRow();
                    connection.commit();
                }
            }

            int counter = 0;

            try (ResultSet rs = stmt.executeQuery("SELECT id, long_str FROM test_table")){
                while (rs.next()) {
                    assertEquals("oldString" + counter + "a", rs.getString(2));
                    counter++;
                }
            }
        }
    }

    @Test
    public void testClosedOnCommit() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            assertEquals("Unexpected holdability", ResultSet.CLOSE_CURSORS_AT_COMMIT, rs.getHoldability());
            assertFalse("Expected resultset to be open", rs.isClosed());

            connection.commit();
            assertTrue("Expected resultset to be closed", rs.isClosed());
        }
    }

    @Test
    public void testClosedOnRollback() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            assertEquals("Unexpected holdability", ResultSet.CLOSE_CURSORS_AT_COMMIT, rs.getHoldability());
            assertFalse("Expected resultset to be open", rs.isClosed());

            connection.rollback();
            assertTrue("Expected resultset to be closed", rs.isClosed());
        }
    }

    @Test
    public void testUpdatableBinaryStream() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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

    @Test
    public void testUpdatableBinaryStream_intLength() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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

    @Test
    public void testUpdatableCharacterStream() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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

    @Test
    public void testUpdatableCharacterStream_intLength() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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

    @Test
    public void testGetMetaDataThrowsSQLExceptionAfterClose() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs;
            rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            assertFalse("Expected result set to be open", rs.isClosed());
            rs.close();

            assertTrue("Expected result set to be closed", rs.isClosed());

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_NO_RESULT_SET));

            rs.getMetaData();
        }
    }

    @Test
    public void testGetMetaDataThrowsSQLExceptionAfterConnectionClose() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs;
        rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
        assertFalse("Expected result set to be open", rs.isClosed());
        connection.close();

        assertTrue("Expected result set to be closed", rs.isClosed());

        expectedException.expect(SQLException.class);
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_NO_RESULT_SET));

        rs.getMetaData();
    }

    @Test
    public void testInheritsFetchDirectionFromStatement() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                assertEquals("Unexpected fetch direction", ResultSet.FETCH_FORWARD, rs.getFetchDirection());
            }

            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);

            // Note we inherit FETCH_REVERSE event though we are forward only, the JDBC spec is a bit ambiguous here,
            // because FETCH_REVERSE is allowed to be set on a forward only statement, but not on a forward only result set...
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                assertEquals("Unexpected fetch direction", ResultSet.FETCH_REVERSE, rs.getFetchDirection());
            }
        }
    }

    @Test
    public void testSetFetchDirection_Forward() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchDirection(ResultSet.FETCH_UNKNOWN);

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
                rs.setFetchDirection(ResultSet.FETCH_FORWARD);

                assertEquals("Unexpected fetch direction", ResultSet.FETCH_FORWARD, rs.getFetchDirection());
            }
        }
    }

    @Test
    public void testSetFetchDirection_closedResultSet_throwsException() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            rs.close();

            expectedException.expect(allOf(
                    instanceOf(SQLException.class),
                    message(containsString("result set is closed"))
            ));

            rs.setFetchDirection(ResultSet.FETCH_FORWARD);
        }
    }

    @Test
    public void testGetFetchDirection_closedResultSet_throwsException() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE");
            rs.close();

            expectedException.expect(allOf(
                    instanceOf(SQLException.class),
                    message(containsString("result set is closed"))
            ));

            rs.getFetchDirection();
        }
    }

    @Test
    public void testSetFetchDirection_Reverse_onForwardOnlyThrowsException() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            expectedException.expect(allOf(
                    instanceOf(SQLNonTransientException.class),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)));

            rs.setFetchDirection(ResultSet.FETCH_REVERSE);
        }
    }

    @Test
    public void testSetFetchDirection_Reverse_onScrollable() throws SQLException {
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            rs.setFetchDirection(ResultSet.FETCH_REVERSE);

            assertEquals("Unexpected fetch direction", ResultSet.FETCH_REVERSE, rs.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_Unknown_onForwardOnlyThrowsException() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            expectedException.expect(allOf(
                    instanceOf(SQLNonTransientException.class),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)));

            rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        }
    }

    @Test
    public void testSetFetchDirection_Unknown_onScrollable() throws SQLException {
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);

            assertEquals("Unexpected fetch direction", ResultSet.FETCH_UNKNOWN, rs.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_InvalidValue() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RDB$DATABASE")) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    not(isA(SQLFeatureNotSupportedException.class)),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_invalidFetchDirection, "-1"),
                    sqlState(equalTo("HY106"))
            ));

            //noinspection MagicConstant
            rs.setFetchDirection(-1);
        }
    }

    /**
     * Rationale: rdb$db_key column is actually identified as DB_KEY in result set
     */
    @Test
    public void testRetrievalOfDbKeyByRDB$DB_KEY() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        createTestData(1);

        try (PreparedStatement pstmt = connection.prepareStatement("select rdb$db_key, id from test_table");
             ResultSet rs = pstmt.executeQuery()) {
            assertTrue("Expected a row", rs.next());

            RowId rowId = rs.getRowId("RDB$DB_KEY");
            assertNotNull(rowId);
        }
    }

    /**
     * Rationale: rdb$db_key column is actually identified as DB_KEY in result set
     */
    @Test
    public void testRetrievalOfDbKeyByDB_KEY() throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        createTestData(1);

        try (PreparedStatement pstmt = connection.prepareStatement("select rdb$db_key, id from test_table");
             ResultSet rs = pstmt.executeQuery()) {
            assertTrue("Expected a row", rs.next());

            RowId rowId = rs.getRowId("DB_KEY");
            assertNotNull(rowId);
        }
    }

    private void createTestData(int recordCount) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT)) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setInt(2, i);
                ps.executeUpdate();
            }
        }
    }
}
