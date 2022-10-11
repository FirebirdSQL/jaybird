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
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.Unstable;
import org.hamcrest.number.OrderingComparison;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeServerBatchSupport;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBPreparedStatementTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    //@formatter:off
    private static final String DROP_GENERATOR = "DROP GENERATOR test_generator";
    private static final String CREATE_GENERATOR = "CREATE GENERATOR test_generator";

    private static final String CREATE_TEST_BLOB_TABLE =
              "RECREATE TABLE test_blob ("
            + "  ID INTEGER,"
            + "  OBJ_DATA BLOB,"
            + "  CLOB_DATA BLOB SUB_TYPE TEXT,"
            + "  TS_FIELD TIMESTAMP,"
            + "  T_FIELD TIME"
            + ")";

    private static final String CREATE_TEST_CHARS_TABLE =
              "RECREATE TABLE TESTTAB ("
            + "ID INTEGER, "
            + "FIELD1 VARCHAR(10) NOT NULL PRIMARY KEY,"
            + "FIELD2 VARCHAR(30),"
            + "FIELD3 VARCHAR(20),"
            + "FIELD4 FLOAT,"
            + "FIELD5 CHAR,"
            + "FIELD6 VARCHAR(5),"
            + "FIELD7 CHAR(1),"
            + "num_field numeric(9,2),"
            + "UTFFIELD CHAR(1) CHARACTER SET UTF8,"
            + "CHAR_OCTETS CHAR(15) CHARACTER SET OCTETS,"
            + "VARCHAR_OCTETS VARCHAR(15) CHARACTER SET OCTETS"
            + ")";

    private static final String CREATE_TEST_BIG_INTEGER_TABLE =
              "recreate table test_big_integer ("
            + "bigintfield bigint,"
            + "varcharfield varchar(255)"
            + ")";

    private static final String TEST_STRING = "This is simple test string.";
    private static final String ANOTHER_TEST_STRING = "Another test string.";

    private static final int DATA_ITEMS = 5;
    private static final String CREATE_TABLE = "RECREATE TABLE test ( col1 INTEGER )";
    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";
    //@formatter:on

    private Connection con;

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeQuietly(con);
    }

    @Test
    void testModifyBlob() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        final int id = 1;

        try (PreparedStatement insertPs = con.prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES (?,?)")) {
            insertPs.setInt(1, id);
            insertPs.setBytes(2, TEST_STRING.getBytes());

            int inserted = insertPs.executeUpdate();

            assertEquals(1, inserted, "Row should be inserted");
        }

        checkSelectString(TEST_STRING, id);

        // Update item
        try (PreparedStatement updatePs = con.prepareStatement("UPDATE test_blob SET obj_data=? WHERE id=?")) {
            updatePs.setBytes(1, ANOTHER_TEST_STRING.getBytes());
            updatePs.setInt(2, id);
            updatePs.execute();

            updatePs.clearParameters();

            checkSelectString(ANOTHER_TEST_STRING, id);

            updatePs.setBytes(1, TEST_STRING.getBytes());
            updatePs.setInt(2, id + 1);
            int updated = updatePs.executeUpdate();

            assertEquals(0, updated, "No rows should be updated");

            checkSelectString(ANOTHER_TEST_STRING, id);
        }
    }

    /**
     * The method {@link java.sql.Statement#executeQuery(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteQuery_String() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeQuery("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#addBatch(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedAddBatch_String() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.addBatch("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, int)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_int() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(
                    () -> ps.executeUpdate("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_intArr() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, String[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_StringArr() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_int() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_intArr() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, String[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_StringArr() throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    private void assertStatementOnlyException(Executable executable) {
        SQLException exception = assertThrows(SQLException.class, executable);
        assertThat(exception, allOf(
                sqlState(equalTo(SQLStateConstants.SQL_STATE_GENERAL_ERROR)),
                message(equalTo(FBPreparedStatement.METHOD_NOT_SUPPORTED))));
    }

    @SuppressWarnings("SameParameterValue")
    private void checkSelectString(String stringToTest, int id) throws Exception {
        try (PreparedStatement selectPs = con.prepareStatement("SELECT obj_data FROM test_blob WHERE id = ?")) {
            selectPs.setInt(1, id);
            ResultSet rs = selectPs.executeQuery();

            assertTrue(rs.next(), "There must be at least one row available");
            assertEquals(stringToTest, rs.getString(1), "Selected string must be equal to inserted one");
            assertFalse(rs.next(), "There must be exactly one row");

            rs.close();
        }
    }

    @Test
    void testGenerator() throws Exception {
        executeDDL(con, DROP_GENERATOR, ISCConstants.isc_no_meta_update);
        executeDDL(con, CREATE_GENERATOR);

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT gen_id(test_generator, 1) as new_value FROM rdb$database");
             ResultSet rs = ps.executeQuery()) {

            assertTrue(rs.next(), "Should get at least one row");

            rs.getLong("new_value");

            assertFalse(rs.next(), "should have only one row");
        }
    }

    /**
     * Test case to reproduce problem with the connection when "operation was
     * cancelled" happens. Bug is fixed, however due to workaround for this
     * problem (@see org.firebirdsql.jdbc.field.FBWorkaroundStringField) this
     * test case is no longer relevant. In order to make it execute correctly
     * one has to remove this workaround.
     */
    @Test
    @Disabled(value="Broken due to FBWorkaroundStringField")
    void testOpCancelled() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (PreparedStatement prep = con.prepareStatement(
                "INSERT INTO TESTTAB (FIELD1, FIELD3, FIELD4, FIELD5 ) VALUES ( ?, ?, ?, ? )")) {
            for (int i = 0; i < 5; i++) {
                if (i == 0) {
                    prep.setObject(1, "0123456789");
                    prep.setObject(2, "01234567890123456789");
                    prep.setObject(3, "1259.9");
                    prep.setObject(4, "A");
                }
                if (i == 1) {
                    prep.setObject(1, "0123456787");
                    prep.setObject(2, "012345678901234567890");
                    prep.setObject(3, "0.9");
                    prep.setObject(4, "B");
                }
                if (i == 2) {
                    prep.setObject(1, "0123456788");
                    prep.setObject(2, "Fld3-Rec3");
                    prep.setObject(3, "0.9");
                    prep.setObject(4, "B");
                }
                if (i == 3) {
                    prep.setObject(1, "0123456780");
                    prep.setObject(2, "Fld3-Rec4");
                    prep.setObject(3, "1299.5");
                    prep.setObject(4, "Q");
                }
                if (i == 4) {
                    prep.setObject(1, "0123456779");
                    prep.setObject(2, "Fld3-Rec5");
                    prep.setObject(3, "1844");
                    prep.setObject(4, "Z");
                }
                prep.execute();
            }
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     */
    @Test
    void testLongParameter() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        try (Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(id, field1, field6) VALUES(1, '', 'a')");
        }

        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement("UPDATE testtab SET field6=? WHERE id = 1")) {
            try {
                ps.setString(1, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                ps.execute();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
        }
    }

    /**
     * Test if batch execution works correctly.
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testBatch(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        executeCreateTable(con, "RECREATE TABLE foo ("
                    + "bar varchar(64) NOT NULL, "
                    + "baz varchar(8) NOT NULL, "
                    + "CONSTRAINT pk_foo PRIMARY KEY (bar, baz))");

        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        try (Connection con = DriverManager.getConnection(FBTestProperties.getUrl(), props);
             PreparedStatement ps = con.prepareStatement("Insert into foo values (?, ?)")) {
            ps.setString(1, "one");
            ps.setString(2, "two");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
            ps.setString(1, "one");
            ps.setString(2, "three");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
        }
    }

    @Test
    void testTimestampWithCalendar() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("timestamp_uses_local_timezone", "true");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_blob(id, ts_field) VALUES (?, ?)")) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"));
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                Timestamp ts = new Timestamp(calendar.getTime().getTime());

                stmt.setInt(1, 2);
                stmt.setTimestamp(2, ts, calendar);
                stmt.execute();

                stmt.setInt(1, 3);
                stmt.setTimestamp(2, ts, utcCalendar);
                stmt.execute();
            }

            try (Statement selectStmt = connection.createStatement()) {
                ResultSet rs = selectStmt.executeQuery(
                        "SELECT id, CAST(ts_field AS VARCHAR(30)), ts_field FROM test_blob");

                Timestamp ts2 = null;
                Timestamp ts3 = null;

                String ts2AsStr = null;
                String ts3AsStr = null;

                final int maxLength = 22;

                while (rs.next()) {
                    switch (rs.getInt(1)) {
                    case 2:
                        ts2 = rs.getTimestamp(3);
                        ts2AsStr = rs.getString(2);
                        ts2AsStr = ts2AsStr.substring(0, Math.min(ts2AsStr.length(), maxLength));
                        break;

                    case 3:
                        ts3 = rs.getTimestamp(3);
                        ts3AsStr = rs.getString(2);
                        ts3AsStr = ts3AsStr.substring(0, Math.min(ts3AsStr.length(), maxLength));
                        break;
                    }
                }

                assertNotNull(ts2);
                assertNotNull(ts3);

                assertEquals(3600 * 1000, Math.abs(ts2.getTime() - ts3.getTime()),
                        "Timestamps 2 and 3 should differ for 3600 seconds");
                String ts2ToStr = ts2.toString();
                ts2ToStr = ts2ToStr.substring(0, Math.min(ts2ToStr.length(), maxLength));
                assertEquals(ts2AsStr, ts2ToStr, "Server should see the same timestamp");
                String ts3ToStr = ts3.toString();
                ts3ToStr = ts3ToStr.substring(0, Math.min(ts3ToStr.length(), maxLength));
                assertEquals(ts3AsStr, ts3ToStr, "Server should see the same timestamp");
            }
        }
    }

    @Test
    void testTimeWithCalendar() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("timestamp_uses_local_timezone", "true");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_blob(id, t_field) VALUES (?, ?)")) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"));
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                Time t = new Time(calendar.getTime().getTime());

                stmt.setInt(1, 2);
                stmt.setTime(2, t, calendar);
                stmt.execute();

                stmt.setInt(1, 3);
                stmt.setTime(2, t, utcCalendar);
                stmt.execute();
            }

            try (Statement selectStmt = connection.createStatement()) {
                ResultSet rs = selectStmt.executeQuery(
                        "SELECT id, CAST(t_field AS VARCHAR(30)), t_field FROM test_blob");

                Time t2 = null;
                Time t3 = null;

                String t2Str = null;
                String t3Str = null;

                while (rs.next()) {
                    switch (rs.getInt(1)) {
                    case 2:
                        t2 = rs.getTime(3);
                        t2Str = rs.getString(2);
                        break;

                    case 3:
                        t3 = rs.getTime(3);
                        t3Str = rs.getString(2);
                        break;
                    }
                }

                assertNotNull(t2);
                assertNotNull(t3);

                assertEquals(3600 * 1000, Math.abs(t2.getTime() - t3.getTime()),
                        "Timestamps 2 and 3 should differ for 3600 seconds");
                assertEquals(t2Str.substring(0, 8), t2.toString(), "Server should see the same timestamp");
                assertEquals(t3Str.substring(0, 8), t3.toString(), "Server should see the same timestamp");
            }
        }
    }

    /**
     * Test if failure in setting the parameter leaves the driver in correct
     * state (i.e. "Parameter with index 1 was not set").
     */
    @Test
    void testBindParameter() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement("UPDATE testtab SET field1 = ? WHERE id = ?")) {
            assertThrows(DataTruncation.class,
                    // Failure to set leaves parameter uninitialized
                    () -> ps.setString(1, "veeeeeeeeeeeeeeeeeeeeery looooooooooooooooooooooong striiiiiiiiiiiiiiiiiiing"),
                    "Expected data truncation");

            ps.setInt(2, 1);

            SQLException exception = assertThrows(SQLException.class, ps::execute, "expected exception on execute");
            assertThat(exception, message(startsWith("Parameter with index 1 was not set")));
        }

        try (Statement stmt = con.createStatement()) {
            stmt.execute("SELECT 1 FROM RDB$DATABASE");
        }
    }

    /**
     * Test if failure in setting the parameter leaves the driver in correct
     * state (i.e. "Parameter with index 1 was not set").
     */
    @ParameterizedTest
    @ValueSource(strings = { "a%", "%a", "%a%" })
    void testLikeParameter(String tooLongValue) throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM testtab WHERE field7 LIKE ?")) {
            assertThrows(DataTruncation.class, () -> ps.setString(1, tooLongValue),
                    "expected not to be able to set too long value");

            SQLException exception = assertThrows(SQLException.class, ps::execute, "expected exception on execute");
            assertThat(exception, message(startsWith("Parameter with index 1 was not set")));
        }

        // verify connection still valid
        try (Statement stmt = con.createStatement()) {
            stmt.execute("SELECT 1 FROM RDB$DATABASE");
        }
    }

    @Test
    void testGetExecutionPlan() throws SQLException {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (FBPreparedStatement stmt = (FBPreparedStatement) con.prepareStatement("SELECT * FROM TESTTAB WHERE ID = 2")) {
            String executionPlan = stmt.getExecutionPlan();
            assertThat("Ensure that a valid execution plan is retrieved", executionPlan, containsString("TESTTAB"));
        }
    }

    @Test
    void testGetExplainedExecutionPlan() throws SQLException {
        assumeTrue(getDefaultSupportInfo().supportsExplainedExecutionPlan(),
                "Test requires explained execution plan support");

        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (FBPreparedStatement stmt = (FBPreparedStatement) con.prepareStatement("SELECT * FROM TESTTAB WHERE ID = 2")) {
            String detailedExecutionPlan = stmt.getExplainedExecutionPlan();
            assertThat("Ensure that a valid detailed execution plan is retrieved",
                    detailedExecutionPlan, containsString("TESTTAB"));
        }
    }

    @ParameterizedTest
    @MethodSource
    void testGetStatementType(String query, int expectedStatementType, String assertionMessage) throws SQLException {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        try (FBPreparedStatement stmt = (FBPreparedStatement) con.prepareStatement(query)) {
            assertEquals(expectedStatementType, stmt.getStatementType(), assertionMessage);
        }
    }

    static Stream<Arguments> testGetStatementType() {
        return Stream.of(
                Arguments.of("SELECT * FROM TESTTAB", FirebirdPreparedStatement.TYPE_SELECT,
                        "TYPE_SELECT should be returned for a SELECT statement"),
                Arguments.of("INSERT INTO testtab(id, field1, field6) VALUES(?, ?, ?)",
                        FirebirdPreparedStatement.TYPE_INSERT,
                        "TYPE_INSERT should be returned for an INSERT statement"),
                Arguments.of("DELETE FROM TESTTAB WHERE ID = ?", FirebirdPreparedStatement.TYPE_DELETE,
                        "TYPE_DELETE should be returned for a DELETE statement"),
                Arguments.of("UPDATE TESTTAB SET FIELD1 = ? WHERE ID = ?", FirebirdPreparedStatement.TYPE_UPDATE,
                        "TYPE_UPDATE should be returned for an UPDATE statement"),
                Arguments.of("INSERT INTO testtab(field1) VALUES(?) RETURNING id",
                        FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE,
                        "TYPE_EXEC_PROCEDURE should be returned for an INSERT ... RETURNING statement"));
    }

    @Test
    @Disabled
    void testLikeFullLength() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(field1) VALUES('abcdefghij')");
        }

        try (PreparedStatement ps = con.prepareStatement("SELECT field1 FROM testtab WHERE field1 LIKE ?")) {
            ps.setString(1, "%abcdefghi%");

            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Should find a record");
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     */
    @Test
    void testNumeric15_2() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", "1");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("INSERT INTO testtab(id, field1, num_field) VALUES(1, '', 10.02)");
            }

            try (PreparedStatement ps = connection.prepareStatement("SELECT num_field FROM testtab WHERE id = 1")) {
                ResultSet rs = ps.executeQuery();

                assertTrue(rs.next());

                float floatValue = rs.getFloat(1);
                double doubleValue = rs.getDouble(1);
                BigDecimal bigDecimalValue = rs.getBigDecimal(1);

                assertEquals(10.02f, floatValue, 0.001f);
                assertEquals(10.02, doubleValue, 0.001);
                assertEquals(new BigDecimal("10.02"), bigDecimalValue);
            }
        }
    }

    @Test
    void testInsertReturning() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        executeDDL(con, DROP_GENERATOR, ISCConstants.isc_no_meta_update);
        executeDDL(con, CREATE_GENERATOR);

        try (FirebirdPreparedStatement stmt = (FirebirdPreparedStatement) con.prepareStatement(
                "INSERT INTO testtab(id, field1) VALUES(gen_id(test_generator, 1), 'a') RETURNING id")) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, stmt.getStatementType(),
                    "TYPE_EXEC_PROCEDURE should be returned for an INSERT...RETURNING statement");
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "Should return at least 1 row");
            assertThat("Generator value should be > 0", rs.getInt(1), OrderingComparison.greaterThan(0));
            assertFalse(rs.next(), "Should return exactly one row");
        }
    }

    //@formatter:off
    private static final String LONG_RUNNING_STATEMENT =
            "execute block " +
            " as" +
            "     declare variable i integer;" +
            "     declare variable a varchar(100);" +
            " begin" +
            "    i = 1;" +
            "    while(i < 1000000) do begin" +
            "      EXECUTE STATEMENT 'SELECT ' || :i || ' FROM rdb$database' INTO :a;" +
            "      i = i + 1;" +
            "    end" +
            " end";
    //@formatter:on

    @Test
    void testCancelStatement() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsCancelOperation(), "Test requires fb_cancel_operations support");
        assumeTrue(getDefaultSupportInfo().supportsExecuteBlock(), "Test requires EXECUTE BLOCK support");
        final AtomicBoolean cancelFailed = new AtomicBoolean(false);
        try (Statement stmt = con.createStatement()) {
            Thread cancelThread = new Thread(() -> {
                try {
                    Thread.sleep(5);
                    stmt.cancel();
                } catch (SQLException ex) {
                    cancelFailed.set(true);
                } catch (InterruptedException ex) {
                    // empty
                }
            }, "cancel-thread");

            cancelThread.start();

            try {
                long start = System.currentTimeMillis();
                SQLException exception = assertThrows(SQLException.class, () -> stmt.execute(LONG_RUNNING_STATEMENT),
                        "Statement should raise a cancel exception");
                long end = System.currentTimeMillis();
                System.out.println("testCancelStatement: statement cancelled after " + (end - start) + " milliseconds");
                assertThat("Unexpected exception for cancellation", exception, allOf(
                        message(startsWith(getFbMessage(ISCConstants.isc_cancelled))),
                        errorCode(equalTo(ISCConstants.isc_cancelled)),
                        sqlState(equalTo("HY008"))));
            } finally {
                cancelThread.join();
            }
            assertFalse(cancelFailed.get(), "Issuing statement cancel failed");
        }
    }

    /**
     * Tests NULL parameter when using {@link PreparedStatement#setNull(int, int)}
     */
    @Test
    void testParameterIsNullQuerySetNull() throws Throwable {
        assumeTrue(getDefaultSupportInfo().supportsNullDataType(), "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setNull(1, Types.VARCHAR);
            ps.setNull(2, Types.VARCHAR);

            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "Step 1.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 1.1 - ID should be equal 1");
            assertTrue(rs.next(), "Step 1.2 - should get a record");
            assertEquals(2, rs.getInt(1), "Step 1.2 - ID should be equal 2");
        }
    }

    /**
     * Tests NULL parameter when using actual (non-null) value in {@link PreparedStatement#setString(int, String)}
     */
    @Test
    void testParameterIsNullQueryWithValues() throws Throwable {
        assumeTrue(getDefaultSupportInfo().supportsNullDataType(), "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setString(1, "a");
            ps.setString(2, "a");

            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "Step 2.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 2.1 - ID should be equal 1");
            assertFalse(rs.next(), "Step 2 - should get only one record");
        }
    }

    /**
     * Tests NULL parameter when using null value in {@link PreparedStatement#setString(int, String)}
     */
    @Test
    void testParameterIsNullQueryWithNull() throws Throwable {
        assumeTrue(getDefaultSupportInfo().supportsNullDataType(), "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setString(1, null);
            ps.setString(2, null);

            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "Step 1.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 1.1 - ID should be equal 1");
            assertTrue(rs.next(), "Step 1.2 - should get a record");
            assertEquals(2, rs.getInt(1), "Step 1.2 - ID should be equal 2");
        }
    }

    private void createIsNullTestData() throws SQLException {
        con.setAutoCommit(false);
        try (Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(id, field1, field2) VALUES (1, '1', 'a')");
            stmt.execute("INSERT INTO testtab(id, field1, field2) VALUES (2, '2', NULL)");
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * Closing a statement twice should not result in an Exception.
     */
    @Test
    void testDoubleClose() throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT 1, 2 FROM RDB$DATABASE");
        stmt.close();
        stmt.close();
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     * <p>
     * JDBC 4.1 feature
     * </p>
     */
    @Test
    void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);
        prepareTestData();

        try (PreparedStatement stmt = con.prepareStatement(SELECT_DATA)) {
            stmt.closeOnCompletion();
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse(rs.isClosed(), "Result set should be open");
                assertFalse(stmt.isClosed(), "Statement should be open");
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue(rs.isClosed(), "Result set should be closed (automatically closed after last result read)");
            assertTrue(stmt.isClosed(), "Statement should be closed");
        }
    }

    // Other closeOnCompletion behavior considered to be sufficiently tested in TestFBStatement

    /**
     * Tests insertion of a single character into a single character field on a UTF8 connection.
     * <p>
     * See JDBC-234 for rationale of this test.
     * </p>
     */
    @Test
    void testInsertSingleCharOnUTF8() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "UTF8");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            final int id = 1;
            final String testString = "\u27F0"; // using high unicode character
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO testtab (id, field1, UTFFIELD) values (?, '01234567', ?)")) {
                pstmt.setInt(1, id);
                pstmt.setString(2, testString);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt2 = connection.prepareStatement(
                    "SELECT UTFFIELD FROM testtab WHERE id = ?")) {
                pstmt2.setInt(1, id);
                ResultSet rs = pstmt2.executeQuery();

                assertTrue(rs.next(), "Expected a row");
                assertEquals(testString, rs.getString(1), "Unexpected value");
            }
        }
    }

    /**
     * Tests if a parameter with a CAST around it will correctly be NULL when set
     * <p>
     * See JDBC-271 for rationale of this test.
     * </p>
     */
    @Test
    void testNullParameterWithCast() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement("SELECT CAST(? AS VARCHAR(1)) FROM RDB$DATABASE")) {
            stmt.setObject(1, null);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                assertNull(rs.getString(1), "Expected column to have NULL value");
            }
        }
    }

    /**
     * Tests multiple batch executions in a row when using blobs created from a stream
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-312">JDBC-312</a>
     * </p>
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testRepeatedBatchExecutionWithBlobFromStream(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        List<byte[]> expectedData = new ArrayList<>();
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        try (Connection con = DriverManager.getConnection(FBTestProperties.getUrl(), props)) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (PreparedStatement insert = con.prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    expectedData.add(testData.clone());
                    insert.setInt(1, i);
                    InputStream in = new ByteArrayInputStream(testData);
                    insert.setBinaryStream(2, in, testData.length);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (Statement select = con.createStatement();
                 ResultSet rs = select.executeQuery("SELECT id, obj_data FROM test_blob ORDER BY id")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    byte[] data = rs.getBytes(2);

                    assertArrayEquals(expectedData.get(id), data, String.format("Unexpected blob data for id %d", id));
                }
                assertEquals(2, count, "Unexpected number of blobs in table");
            }
        }
    }

    /**
     * Tests multiple batch executions in a row when using clobs created from a stream
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-433">JDBC-433</a>
     * </p>
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testRepeatedBatchExecutionWithClobFromBinaryStream(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        List<byte[]> expectedData = new ArrayList<>();
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        try (Connection con = DriverManager.getConnection(FBTestProperties.getUrl(), props)) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO test_blob (id, clob_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    expectedData.add(testData.clone());
                    insert.setInt(1, i);
                    InputStream in = new ByteArrayInputStream(testData);
                    insert.setBinaryStream(2, in, testData.length);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (Statement select = con.createStatement();
                 ResultSet rs = select.executeQuery("SELECT id, clob_data FROM test_blob ORDER BY id")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    byte[] data = rs.getBytes(2);

                    assertArrayEquals(expectedData.get(id), data, String.format("Unexpected blob data for id %d", id));
                }
                assertEquals(2, count, "Unexpected number of blobs in table");
            }
        }
    }

    /**
     * Tests multiple batch executions in a row when using clobs created from a String
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-433">JDBC-433</a>
     * </p>
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    @Disabled
    @Unstable("Susceptible to character set transliteration issues")
    void testRepeatedBatchExecutionWithClobFromString(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        List<String> expectedData = new ArrayList<>();
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        try (Connection con = DriverManager.getConnection(FBTestProperties.getUrl(), props)) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (PreparedStatement insert = con.prepareStatement("INSERT INTO test_blob (id, clob_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    String testString = new String(testData, "Cp1252");
                    expectedData.add(testString);
                    insert.setInt(1, i);
                    insert.setString(2, testString);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (Statement select = con.createStatement();
                 ResultSet rs = select.executeQuery("SELECT id, clob_data FROM test_blob ORDER BY id")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    String data = rs.getString(2);

                    assertEquals(expectedData.get(id), data, String.format("Unexpected blob data for id %d", id));
                }
                assertEquals(2, count, "Unexpected number of blobs in table");
            }
        }
    }

    static Stream<Arguments> testOctetsInsertAndSelect() {
        return Stream.of(
                Arguments.of("CHAR_OCTETS", Types.BINARY),
                Arguments.of("VARCHAR_OCTETS", Types.VARBINARY));
    }

    @ParameterizedTest
    @MethodSource
    void testOctetsInsertAndSelect(String columName, int jdbcType) throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (PreparedStatement insert = con.prepareStatement(
                "INSERT INTO TESTTAB(ID, " + columName + ", FIELD1) VALUES (?, ?, ?)")) {
            ParameterMetaData pmd = insert.getParameterMetaData();
            assertEquals(jdbcType, pmd.getParameterType(2));
            final int fieldLength = pmd.getPrecision(2);
            final byte[] data1 = DataGenerator.createRandomBytes(fieldLength);
            final byte[] data2 = DataGenerator.createRandomBytes(fieldLength - 2);
            final byte[] expectedData2 = jdbcType == Types.BINARY
                    ? Arrays.copyOf(data2, fieldLength)
                    : data2;

            insert.setInt(1, 1);
            insert.setBytes(2, data1);
            insert.setInt(3, 1);
            insert.execute();

            insert.setInt(1, 2);
            insert.setBytes(2, data2);
            insert.setInt(3, 2);
            insert.execute();

            try (Statement select = con.createStatement();
                 ResultSet rs = select.executeQuery("SELECT ID, " + columName + " FROM TESTTAB ORDER BY ID")) {
                ResultSetMetaData rsmd = rs.getMetaData();
                assertEquals(jdbcType, rsmd.getColumnType(2));

                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
                assertArrayEquals(data1, rs.getBytes(2));

                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
                assertArrayEquals(expectedData2, rs.getBytes(2));
            }
        }
    }

    @Test
    void testSetBigIntegerParameters() throws Exception {
        executeCreateTable(con, CREATE_TEST_BIG_INTEGER_TABLE);
        final String testBigIntegerValueString = "1" + Long.MAX_VALUE;
        try (PreparedStatement pstmt = con.prepareStatement(
                "insert into test_big_integer(bigintfield, varcharfield) values (?, ?)")) {
            pstmt.setObject(1, BigInteger.ONE);
            pstmt.setObject(2, new BigInteger(testBigIntegerValueString));
            pstmt.executeUpdate();
        }

        try (PreparedStatement pstmt = con.prepareStatement("select bigintfield, varcharfield from test_big_integer");
             ResultSet rs = pstmt.executeQuery()) {
            assertTrue(rs.next(), "Expected a row");
            assertEquals(1, rs.getLong("bigintfield"), "Unexpected value for bigintfield");
            assertEquals(BigInteger.ONE, rs.getObject("bigintfield", BigInteger.class),
                    "Unexpected value for bigintfield as BigInteger");
            assertEquals(testBigIntegerValueString, rs.getString("varcharfield"), "Unexpected value for varcharfield");
            assertEquals(new BigInteger(testBigIntegerValueString), rs.getObject("varcharfield", BigInteger.class),
                    "Unexpected value for varcharfield as BigInteger");
        }
    }

    // See JDBC-472; TODO this test doesn't reproduce the issue
    @Test
    void testExecuteProcedureWithoutReturnValues() throws Exception {
        executeDDL(con, "recreate table example_table (\n"
                + "  id integer primary key,\n"
                + "  example_date date\n"
                + ")");
        executeDDL(con, "recreate table another_example_table (\n"
                + "  ex_id integer,\n"
                + "  example_date varchar(100)\n"
                + ")");
        executeDDL(con, "recreate table example_table_2 (\n"
                + "  id integer,\n"
                + "  example_date date\n"
                + ")");
        executeDDL(con, "CREATE OR ALTER PROCEDURE EXAMPLE_PROCEDURE(\n"
                + "    EX_ID integer)\n"
                + "AS\n"
                + "DECLARE VARIABLE EX_BL integer;\n"
                + "declare variable EXAMPLE_DATE date;\n"
                + "declare variable EXAMPLE_DATE_2 date;\n"
                + "  /* ... (declaring other variables) */\n"
                + "BEGIN\n"
                + "\n"
                + "  ex_bl = 0;"
                + "  /*\n"
                + "  RECOVERING INFORMATION\n"
                + "  */\n"
                + "  SELECT\n"
                + "    /* SELECTING SOME FIELDS */\n"
                + "    COALESCE(EXAMPLE_DATE - 1, CURRENT_DATE)\n"
                + "  FROM\n"
                + "    EXAMPLE_TABLE\n"
                + "  WHERE\n"
                + "    ID = :EX_ID\n"
                + "  INTO\n"
                + "    /* SAME AS TABLE FIELD NAMES */\n"
                + "    EXAMPLE_DATE_2;\n"
                + "\n"
                + "  IF (EX_BL = 1) THEN\n"
                + "    DELETE FROM ANOTHER_EXAMPLE_TABLE WHERE EX_ID = :EX_ID;\n"
                + "  ELSE\n"
                + "  BEGIN\n"
                + "    /*\n"
                + "    ANOTHER SELECT\n"
                + "    */\n"
                + "    SELECT FIRST 1\n"
                + "      example_date\n"
                + "    FROM\n"
                + "      ANOTHER_EXAMPLE_TABLE\n"
                + "    WHERE\n"
                + "      EX_ID = :EX_ID\n"
                + "    ORDER BY\n"
                + "      EXAMPLE_DATE\n"
                + "    INTO\n"
                + "      example_date_2;\n"
                + "     /* ALIASES */\n"
                + "\n"
                + "    IF (example_date_2 is null or example_date_2 < current_date) THEN\n"
                + "    BEGIN\n"
                + "      EXAMPLE_DATE = EXAMPLE_DATE_2;\n"
                + "      WHILE (example_date < current_date) DO\n"
                + "      BEGIN\n"
                + "        INSERT INTO EXAMPLE_TABLE_2 (\n"
                + "          id,\n"
                + "          example_date)\n"
                + "        VALUES (\n"
                + "          :EX_ID,\n"
                + "          :example_date);\n"
                + "        EXAMPLE_DATE = EXAMPLE_DATE + 1;\n"
                + "      END\n"
                + "    END\n"
                + "\n"
                + "    SELECT FIRST 1\n"
                + "      example_date\n"
                + "    FROM\n"
                + "      EXAMPLE_TABLE_2\n"
                + "    WHERE\n"
                + "      ID = :EX_ID\n"
                + "    ORDER BY\n"
                + "      EXAMPLE_DATE DESC\n"
                + "    INTO\n"
                + "      example_date_2;\n"
                + "\n"
                + "    IF (example_date_2 is null or example_date_2 < current_date) THEN\n"
                + "    BEGIN\n"
                + "      EXAMPLE_DATE = EXAMPLE_DATE_2;\n"
                + "      WHILE (example_date < current_date) DO\n"
                + "      BEGIN\n"
                + "        INSERT INTO EXAMPLE_TABLE_2 (\n"
                + "          id,\n"
                + "         example_date)\n"
                + "        VALUES (\n"
                + "          :EX_ID,\n"
                + "          :example_date);\n"
                + "        EXAMPLE_DATE = EXAMPLE_DATE + 1;\n"
                + "      END\n"
                + "    END\n"
                + "\n"
                + "    DELETE FROM\n"
                + "      EXAMPLE_TABLE_2\n"
                + "    WHERE\n"
                + "      (ID = :EX_ID) AND\n"
                + "      ((EXAMPLE_DATE < current_date - 5) OR (EXAMPLE_DATE > current_date));\n"
                + "  END\n"
                + "\n"
                + "END");

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("insert into example_table(id, example_date) values (1, current_date - 5)");
        }

        con.setAutoCommit(false);

        try (PreparedStatement pstmt = con.prepareStatement("execute procedure EXAMPLE_PROCEDURE(?)")) {
//            for (int cnt = 0; cnt < 100; cnt++) {
                pstmt.setInt(1, 1);
                pstmt.executeUpdate();
//            }
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        }
    }

    /**
     * Check if strings longer than 255 are correctly stored and retrieved (see also JDBC-518)
     */
    @Test
    void testLongStringInsertAndRetrieve() throws Exception {
        executeDDL(con, "recreate table long_string ( "
                + " id integer primary key, "
                + " stringcolumn varchar(1024)"
                + ")");
        char[] chars = new char[1024];
        Arrays.fill(chars, 'a');
        final String testvalue = new String(chars);
        try (PreparedStatement pstmt = con.prepareStatement(
                "insert into long_string (id, stringcolumn) values (?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, testvalue);
            pstmt.executeUpdate();
        }
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "select char_length(stringcolumn), stringcolumn from long_string where id = 1")) {
            assertTrue(rs.next(), "expected a row");
            assertEquals(chars.length, rs.getInt(1), "Unexpected string length in Firebird");
            assertEquals(testvalue, rs.getString(2), "Selected string value does not match inserted");
        }
    }

    /**
     * Tests if reexecuting a prepared statement after fetch failure works for holdable result set.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-531">JDBC-531</a>
     * </p>
     */
    @Test
    void testReexecuteStatementAfterFailure() throws Exception {
        executeDDL(con, "recreate table encoding_error ("
                + " id integer primary key, "
                + " stringcolumn varchar(10) character set NONE"
                + ")");
        try (PreparedStatement pstmt = con.prepareStatement(
                "insert into encoding_error (id, stringcolumn) values (?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setBytes(2, new byte[] { (byte) 0xFF, (byte) 0xFF });
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.executeUpdate();
        }
        con.setAutoCommit(false);
        try (PreparedStatement pstmt = con.prepareStatement(
                "select cast(stringcolumn as varchar(10) character set UTF8) from encoding_error",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            assertThrows(SQLException.class, () -> {
                ResultSet rs = pstmt.executeQuery();
                rs.next();
            });

            SQLException exception = assertThrows(SQLException.class, () -> {
                ResultSet rs2 = pstmt.executeQuery();
                rs2.next();
            });
            assertThat(exception, allOf(
                    errorCodeEquals(ISCConstants.isc_malformed_string),
                    fbMessageStartsWith(ISCConstants.isc_malformed_string)));
        }
    }

    @Test
    void testSelectHasNoUpdateCount() throws Exception {
        executeCreateTable(con, CREATE_TABLE);
        prepareTestData();

        try (PreparedStatement stmt = con.prepareStatement(SELECT_DATA)) {
            assertTrue(stmt.execute(), "expected a result set");
            ResultSet rs = stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse(rs.isClosed(), "Result set should be open");
                assertFalse(stmt.isClosed(), "Statement should be open");
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue(rs.isClosed(), "Result set should be closed (automatically closed after last result read)");
            assertFalse(stmt.getMoreResults(), "expected no result set for getMoreResults");
            assertEquals(-1, stmt.getUpdateCount(), "no update count (-1) was expected");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "binary", "text" })
    void testSetClobNullClob(String subType) throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(
                "select cast(? as blob sub_type " + subType + ") from rdb$database")) {
            pstmt.setClob(1, (Clob) null);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getClob(1));
        }
    }

    @Test
    void testSetClobNullReader() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("select cast(? as blob sub_type text) from rdb$database")) {
            pstmt.setClob(1, (Reader) null);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getClob(1));
        }
    }

    @Test
    void testSetClobNullReaderWithLength() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("select cast(? as blob sub_type text) from rdb$database")) {
            pstmt.setClob(1, null, 1);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getClob(1));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "binary", "text" })
    void testSetBlobNullBlob(String subType) throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(
                "select cast(? as blob sub_type " + subType + ") from rdb$database")) {
            pstmt.setBlob(1, (Blob) null);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getBlob(1));
        }
    }

    @Test
    void testSetBlobNullInputStream() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(
                "select cast(? as blob sub_type binary) from rdb$database")) {
            pstmt.setBlob(1, (InputStream) null);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getBlob(1));
        }
    }

    @Test
    void testSetBlobNullInputStreamWithLength() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(
                "select cast(? as blob sub_type binary) from rdb$database")) {
            pstmt.setBlob(1, null, 1);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next(), "expected a row");
            assertNull(rs.getBlob(1));
        }
    }

    private void prepareTestData() throws SQLException {
        con.setAutoCommit(false);
        try (PreparedStatement pstmt = con.prepareStatement(INSERT_DATA)) {
            for (int i = 0; i < DATA_ITEMS; i++) {
                pstmt.setInt(1, i);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } finally {
            con.setAutoCommit(true);
        }
    }
}
