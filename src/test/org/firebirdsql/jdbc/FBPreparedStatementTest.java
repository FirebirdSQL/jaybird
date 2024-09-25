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
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
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
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.FbAssumptions.assumeServerBatchSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
class FBPreparedStatementTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static final String DROP_GENERATOR = "DROP GENERATOR test_generator";
    private static final String CREATE_GENERATOR = "CREATE GENERATOR test_generator";

    private static final String CREATE_TEST_BLOB_TABLE = """
            RECREATE TABLE test_blob (
              ID INTEGER,
              OBJ_DATA BLOB,
              CLOB_DATA BLOB SUB_TYPE TEXT,
              TS_FIELD TIMESTAMP,
              T_FIELD TIME
            )""";

    private static final String CREATE_TEST_CHARS_TABLE = """
            RECREATE TABLE TESTTAB (
              ID INTEGER,
              FIELD1 VARCHAR(10) NOT NULL PRIMARY KEY,
              FIELD2 VARCHAR(30),
              FIELD3 VARCHAR(20),
              FIELD4 FLOAT,
              FIELD5 CHAR,
              FIELD6 VARCHAR(5),
              FIELD7 CHAR(1),
              num_field numeric(9,2),
              UTFFIELD CHAR(1) CHARACTER SET UTF8,
              CHAR_OCTETS CHAR(15) CHARACTER SET OCTETS,
              VARCHAR_OCTETS VARCHAR(15) CHARACTER SET OCTETS
            )""";

    private static final String CREATE_TEST_BIG_INTEGER_TABLE = """
            recreate table test_big_integer (
              bigintfield bigint,
              varcharfield varchar(255)
            )""";

    private static final String CREATE_TEST_VARCHAR_5_UTF8_TABLE = """
            recreate table test_varchar_5_utf8 (
              varchar_field varchar(5) character set utf8
            )""";

    private static final String TEST_STRING = "This is simple test string.";
    private static final String ANOTHER_TEST_STRING = "Another test string.";

    private static final int DATA_ITEMS = 5;
    private static final String CREATE_TABLE = "RECREATE TABLE test ( col1 INTEGER )";
    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";

    private FBConnection con;

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager().unwrap(FBConnection.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        con.close();
    }

    @Test
    void testModifyBlob() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        final int id = 1;

        try (var insertPs = con.prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES (?,?)")) {
            insertPs.setInt(1, id);
            insertPs.setBytes(2, TEST_STRING.getBytes());

            assertEquals(1, insertPs.executeUpdate(), "Row should be inserted with update count 1");
        }

        checkSelectString(TEST_STRING, id);

        // Update item
        try (var updatePs = con.prepareStatement("UPDATE test_blob SET obj_data=? WHERE id=?")) {
            updatePs.setBytes(1, ANOTHER_TEST_STRING.getBytes());
            updatePs.setInt(2, id);
            updatePs.execute();

            updatePs.clearParameters();

            checkSelectString(ANOTHER_TEST_STRING, id);

            updatePs.setBytes(1, TEST_STRING.getBytes());
            updatePs.setInt(2, id + 1);

            assertEquals(0, updatePs.executeUpdate(), "No rows should be updated (update count 0)");
        }

        checkSelectString(ANOTHER_TEST_STRING, id);
    }

    /**
     * The method {@link java.sql.Statement#executeQuery(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteQuery_String() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeQuery("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#addBatch(String)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedAddBatch_String() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.addBatch("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, int)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_int() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(
                    () -> ps.executeUpdate("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_intArr() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, String[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_StringArr() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.executeUpdate("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int)} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_int() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_intArr() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, String[])} should not work on PreparedStatement.
     */
    @Test
    void testUnsupportedExecute_String_StringArr() throws Exception {
        try (var ps = con.prepareStatement("SELECT 1 FROM RDB$DATABASE")) {
            assertStatementOnlyException(() -> ps.execute("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    private void assertStatementOnlyException(Executable executable) {
        var exception = assertThrows(SQLException.class, executable);
        assertThat(exception, allOf(
                sqlState(equalTo(SQLStateConstants.SQL_STATE_GENERAL_ERROR)),
                message(equalTo(FBPreparedStatement.METHOD_NOT_SUPPORTED))));
    }

    @SuppressWarnings("SameParameterValue")
    private void checkSelectString(String stringToTest, int id) throws Exception {
        try (var selectPs = con.prepareStatement("SELECT obj_data FROM test_blob WHERE id = ?")) {
            selectPs.setInt(1, id);
            var rs = selectPs.executeQuery();

            assertNextRow(rs);
            assertEquals(stringToTest, rs.getString(1), "Selected string must be equal to inserted one");
            assertNoNextRow(rs);
        }
    }

    @Test
    void testGenerator() throws Exception {
        executeDDL(con, DROP_GENERATOR, ISCConstants.isc_no_meta_update);
        executeDDL(con, CREATE_GENERATOR);

        try (var ps = con.prepareStatement("SELECT gen_id(test_generator, 1) as new_value FROM rdb$database")) {
            var rs = ps.executeQuery();

            assertNextRow(rs);
            assertThat("Generator value should be > 0", rs.getInt("new_value"), OrderingComparison.greaterThan(0));
            assertNoNextRow(rs);
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     */
    @Test
    void testLongParameter() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        try (var stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(id, field1, field6) VALUES(1, '', 'a')");
        }

        try (var ps = con.prepareStatement("UPDATE testtab SET field6=? WHERE id = 1")) {
            assertThrows(SQLException.class, () -> ps.setString(1, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
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
        executeCreateTable(con, """
                RECREATE TABLE foo (
                 bar varchar(64) NOT NULL,
                 baz varchar(8) NOT NULL,
                 CONSTRAINT pk_foo PRIMARY KEY (bar, baz)
                )""");

        try (var con = getConnectionViaDriverManager(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
             var ps = con.prepareStatement("insert into foo values (?, ?)")) {
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
        try (var stmt = con.createStatement()) {
            var rs = stmt.executeQuery("select * from foo order by bar, baz");
            assertNextRow(rs);
            assertRowEquals("Mismatch in expected values in table foo", rs, "one", "three");
            assertNextRow(rs);
            assertRowEquals("Mismatch in expected values in table foo", rs, "one", "two");
            assertNoNextRow(rs);
        }
    }

    @Test
    void testTimestampWithCalendar() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);

        try (var stmt = con.prepareStatement("INSERT INTO test_blob(id, ts_field) VALUES (?, ?)")) {
            var calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"));
            var utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            var ts = new Timestamp(calendar.getTime().getTime());

            stmt.setInt(1, 2);
            stmt.setTimestamp(2, ts, calendar);
            stmt.execute();

            stmt.setInt(1, 3);
            stmt.setTimestamp(2, ts, utcCalendar);
            stmt.execute();
        }

        try (var selectStmt = con.createStatement()) {
            var rs = selectStmt.executeQuery("SELECT id, CAST(ts_field AS VARCHAR(30)), ts_field FROM test_blob");

            Timestamp ts2 = null;
            Timestamp ts3 = null;

            String ts2AsStr = null;
            String ts3AsStr = null;

            final int maxLength = 22;

            while (rs.next()) {
                switch (rs.getInt(1)) {
                case 2:
                    ts2 = rs.getTimestamp(3);
                    ts2AsStr = truncate(rs.getString(2), maxLength);
                    break;

                case 3:
                    ts3 = rs.getTimestamp(3);
                    ts3AsStr = truncate(rs.getString(2), maxLength);
                    break;
                }
            }

            assertNotNull(ts2);
            assertNotNull(ts3);

            assertEquals(3600 * 1000, Math.abs(ts2.getTime() - ts3.getTime()),
                    "Timestamps 2 and 3 should differ for 3600 seconds");
            String ts2ToStr = truncate(fixTimestampString(ts2.toString(), ts2AsStr.length()), maxLength);
            assertEquals(ts2AsStr, ts2ToStr, "Server should see the same timestamp");
            String ts3ToStr = truncate(fixTimestampString(ts3.toString(), ts3AsStr.length()), maxLength);
            assertEquals(ts3AsStr, ts3ToStr, "Server should see the same timestamp");
        }
    }

    /**
     * Account for presentation difference with trailing 0.
     *
     * @param timestampString
     *         timestamp string
     * @param expectedLength
     *         expected length
     * @return value padded with one {@code 0} if {@code timestampString} is one character shorter than
     * {@code expectedLength}, otherwise {@code timestampString}
     */
    private static String fixTimestampString(String timestampString, int expectedLength) {
        if (timestampString.length() == expectedLength - 1) {
            return timestampString + '0';
        }
        return timestampString;
    }

    /**
     * Truncate string to {@code maxLength}.
     *
     * @param stringToTruncate
     *         string to truncate
     * @param maxLength
     *         maximum length
     * @return either {@code stringToTruncate} if shorter than {@code maxLength}, or a string of the requested length
     */
    @SuppressWarnings("SameParameterValue")
    private static String truncate(String stringToTruncate, int maxLength) {
        return stringToTruncate.substring(0, Math.min(stringToTruncate.length(), maxLength));
    }

    @Test
    void testTimeWithCalendar() throws Exception {
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);

        try (var stmt = con.prepareStatement("INSERT INTO test_blob(id, t_field) VALUES (?, ?)")) {
            var calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"));
            var utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            var t = new Time(calendar.getTime().getTime());

            stmt.setInt(1, 2);
            stmt.setTime(2, t, calendar);
            stmt.execute();

            stmt.setInt(1, 3);
            stmt.setTime(2, t, utcCalendar);
            stmt.execute();
        }

        try (var selectStmt = con.createStatement()) {
            var rs = selectStmt.executeQuery("SELECT id, CAST(t_field AS VARCHAR(30)), t_field FROM test_blob");

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

    /**
     * Test if failure in setting the parameter leaves the driver in correct
     * state (i.e. "Parameter with index 1 was not set").
     */
    @Test
    void testBindParameter() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        con.setAutoCommit(false);

        try (var ps = con.prepareStatement("UPDATE testtab SET field1 = ? WHERE id = ?")) {
            assertThrows(DataTruncation.class,
                    // Failure to set leaves parameter uninitialized
                    () -> ps.setString(1, "veeeeeeeeeeeeeeeeeeeeery looooooooooooooooooooooong striiiiiiiiiiiiiiiiiiing"),
                    "Expected data truncation");

            ps.setInt(2, 1);

            var exception = assertThrows(SQLException.class, ps::execute, "expected exception on execute");
            assertThat(exception, message(startsWith("Parameter with index 1 was not set")));
        }

        // verify connection still valid
        try (var stmt = con.createStatement()) {
            assertDoesNotThrow(() -> stmt.execute("SELECT 1 FROM RDB$DATABASE"));
        }
    }

    /**
     * Test case for <a href="https://github.com/FirebirdSQL/jaybird/issues/396">jaybird#396</a>.
     */
    @ParameterizedTest
    @ValueSource(strings = { "NONE", "UTF8", "WIN1252" })
    void testBindParameterUtf8_396(String connectionCharset) throws Exception {
        executeCreateTable(con, CREATE_TEST_VARCHAR_5_UTF8_TABLE);

        try (var con = getConnectionViaDriverManager("lc_ctype", connectionCharset)) {
            con.setAutoCommit(false);
            try (var pstmt = con.prepareStatement("insert into test_varchar_5_utf8 (varchar_field) values (?)")) {
                // 20 bytes, 5 codepoints (for WIN1252: 5 bytes, 5 codepoints, all '?'))
                pstmt.setString(1, "\uD83D\uDE03".repeat(5));
                pstmt.execute();

                pstmt.clearParameters();

                // 6 bytes, 6 codepoints
                var exceptionOnSetString = assertThrows(DataTruncation.class,
                        // Failure to set leaves parameter uninitialized
                        () -> pstmt.setString(1, "abcdef"),
                        "Expected data truncation");
                assertAll(
                        () -> assertEquals(5, exceptionOnSetString.getTransferSize(),
                                "expected transfer size in codepoints"),
                        () -> assertEquals(6, exceptionOnSetString.getDataSize(), "expected data size in codepoints"));
                var exceptionOnExecute = assertThrows(SQLException.class, pstmt::execute,
                        "expected exception on execute");
                assertThat(exceptionOnExecute, message(startsWith("Parameter with index 1 was not set")));
            }
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

        try (var ps = con.prepareStatement("SELECT * FROM testtab WHERE field7 LIKE ?")) {
            assertThrows(DataTruncation.class, () -> ps.setString(1, tooLongValue),
                    "expected not to be able to set too long value");

            var exception = assertThrows(SQLException.class, ps::execute, "expected exception on execute");
            assertThat(exception, message(startsWith("Parameter with index 1 was not set")));
        }

        // verify connection still valid
        try (var stmt = con.createStatement()) {
            stmt.execute("SELECT 1 FROM RDB$DATABASE");
        }
    }

    @Test
    void testGetExecutionPlan() throws SQLException {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var stmt = con.prepareStatement("SELECT * FROM TESTTAB WHERE ID = 2").unwrap(FBPreparedStatement.class)) {
            assertThat("Ensure that a valid execution plan is retrieved",
                    stmt.getExecutionPlan(), containsString("TESTTAB"));
        }
    }

    @Test
    void testGetExplainedExecutionPlan() throws SQLException {
        assumeFeature(FirebirdSupportInfo::supportsExplainedExecutionPlan,
                "Test requires explained execution plan support");

        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var stmt = con.prepareStatement("SELECT * FROM TESTTAB WHERE ID = 2").unwrap(FBPreparedStatement.class)) {
            assertThat("Ensure that a valid detailed execution plan is retrieved",
                    stmt.getExplainedExecutionPlan(), containsString("TESTTAB"));
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @MethodSource
    void testGetStatementType(String query, int expectedStatementType, String assertionMessage) throws SQLException {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        try (var stmt = con.prepareStatement(query).unwrap(FBPreparedStatement.class)) {
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
    @Disabled("With existing Firebird versions, this will throw a DataTruncation")
    void testLikeFullLength() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(field1) VALUES('abcdefghij')");
        }

        try (var ps = con.prepareStatement("SELECT field1 FROM testtab WHERE field1 LIKE ?")) {
            ps.setString(1, "%abcdefghi%");

            var rs = ps.executeQuery();
            assertNextRow(rs);
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     */
    @Test
    void testNumeric15_2() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var connection = getConnectionViaDriverManager(PropertyNames.sqlDialect, "1")) {
            try (var stmt = connection.createStatement()) {
                stmt.execute("INSERT INTO testtab(id, field1, num_field) VALUES(1, '', 10.02)");
            }

            try (var ps = connection.prepareStatement("SELECT num_field FROM testtab WHERE id = 1")) {
                var rs = ps.executeQuery();

                assertNextRow(rs);

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

        try (var stmt = con
                .prepareStatement("INSERT INTO testtab(id, field1) VALUES(gen_id(test_generator, 1), 'a') RETURNING id")
                .unwrap(FirebirdPreparedStatement.class)) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, stmt.getStatementType(),
                    "TYPE_EXEC_PROCEDURE should be returned for an INSERT...RETURNING statement");
            var rs = stmt.executeQuery();

            assertNextRow(rs);
            assertThat("Generator value should be > 0", rs.getInt(1), OrderingComparison.greaterThan(0));
            assertNoNextRow(rs);
        }
    }

    private static final String LONG_RUNNING_STATEMENT = """
            execute block
            as
             declare variable i integer;
             declare variable a varchar(100);
            begin
             i = 1;
             while(i < 1000000) do  begin
              EXECUTE STATEMENT 'SELECT ' || :i || ' FROM rdb$database' INTO :a;
              i = i + 1;
             end
            end""";

    @Test
    void testCancelStatement() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsCancelOperation, "Test requires fb_cancel_operations support");
        assumeFeature(FirebirdSupportInfo::supportsExecuteBlock, "Test requires EXECUTE BLOCK support");

        try (var stmt = con.createStatement()) {
            final var cancelFailed = new AtomicBoolean(true);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                try {
                    stmt.cancel();
                    cancelFailed.set(false);
                } catch (SQLException ignored) {
                }
            }, 10, TimeUnit.MILLISECONDS);
            executor.shutdown();

            var exception = assertThrows(SQLException.class, () -> stmt.execute(LONG_RUNNING_STATEMENT),
                    "Statement should raise a cancel exception");
            assertThat("Unexpected exception for cancellation", exception, allOf(
                    message(startsWith(getFbMessage(ISCConstants.isc_cancelled))),
                    errorCode(equalTo(ISCConstants.isc_cancelled)),
                    sqlState(equalTo("HY008"))));
            assertFalse(cancelFailed.get(), "Issuing statement cancel failed");
        }
    }

    /**
     * Tests NULL parameter when using {@link PreparedStatement#setNull(int, int)}
     */
    @Test
    void testParameterIsNullQuerySetNull() throws Throwable {
        assumeFeature(FirebirdSupportInfo::supportsNullDataType, "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (var ps = con.prepareStatement("SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setNull(1, Types.VARCHAR);
            ps.setNull(2, Types.VARCHAR);

            var rs = ps.executeQuery();

            assertNextRow(rs, "Step 1.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 1.1 - ID should be equal 1");
            assertNextRow(rs, "Step 1.2 - should get a record");
            assertEquals(2, rs.getInt(1), "Step 1.2 - ID should be equal 2");
            assertNoNextRow(rs);
        }
    }

    /**
     * Tests NULL parameter when using actual (non-null) value in {@link PreparedStatement#setString(int, String)}
     */
    @Test
    void testParameterIsNullQueryWithValues() throws Throwable {
        assumeFeature(FirebirdSupportInfo::supportsNullDataType, "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (var ps = con.prepareStatement("SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setString(1, "a");
            ps.setString(2, "a");

            var rs = ps.executeQuery();

            assertNextRow(rs, "Step 2.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 2.1 - ID should be equal 1");
            assertNoNextRow(rs, "Step 2 - should get only one record");
        }
    }

    /**
     * Tests NULL parameter when using null value in {@link PreparedStatement#setString(int, String)}
     */
    @Test
    void testParameterIsNullQueryWithNull() throws Throwable {
        assumeFeature(FirebirdSupportInfo::supportsNullDataType, "test requires support for ? IS NULL");
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        createIsNullTestData();

        try (var ps = con.prepareStatement("SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1")) {
            ps.setString(1, null);
            ps.setString(2, null);

            var rs = ps.executeQuery();

            assertNextRow(rs, "Step 1.1 - should get a record");
            assertEquals(1, rs.getInt(1), "Step 1.1 - ID should be equal 1");
            assertNextRow(rs, "Step 1.2 - should get a record");
            assertEquals(2, rs.getInt(1), "Step 1.2 - ID should be equal 2");
            assertNoNextRow(rs);
        }
    }

    private void createIsNullTestData() throws SQLException {
        con.setAutoCommit(false);
        try (var stmt = con.createStatement()) {
            stmt.execute("INSERT INTO testtab(id, field1, field2) VALUES (1, '1', 'a')");
            stmt.execute("INSERT INTO testtab(id, field1, field2) VALUES (2, '2', NULL)");
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * Closing a statement twice should not result in an exception.
     */
    @Test
    void testDoubleClose() throws SQLException {
        var stmt = con.prepareStatement("SELECT 1, 2 FROM RDB$DATABASE");
        stmt.close();
        assertDoesNotThrow(stmt::close);
    }

    /**
     * Tests insertion of a single character into a single character field on a UTF8 connection.
     * <p>
     * See JDBC-234 for rationale of this test.
     * </p>
     */
    @Test
    void testInsertSingleCharOnUTF8() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var connection = getConnectionViaDriverManager("lc_ctype", "UTF8")) {
            final int id = 1;
            final String testString = "\u27F0"; // using high unicode character
            try (var pstmt = connection.prepareStatement(
                    "INSERT INTO testtab (id, field1, UTFFIELD) values (?, '01234567', ?)")) {
                pstmt.setInt(1, id);
                pstmt.setString(2, testString);
                pstmt.executeUpdate();
            }

            try (var pstmt2 = connection.prepareStatement("SELECT UTFFIELD FROM testtab WHERE id = ?")) {
                pstmt2.setInt(1, id);
                ResultSet rs = pstmt2.executeQuery();

                assertNextRow(rs);
                assertEquals(testString, rs.getString(1), "Unexpected value");
                assertNoNextRow(rs);
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
        try (var stmt = con.prepareStatement("SELECT CAST(? AS VARCHAR(1)) FROM RDB$DATABASE")) {
            stmt.setObject(1, null);

            var rs = stmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getString(1), "Expected column to have NULL value");
            assertNoNextRow(rs);
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
        var expectedData = new ArrayList<byte[]>();

        try (var con = getConnectionViaDriverManager(PropertyNames.useServerBatch, String.valueOf(useServerBatch))) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (var insert = con.prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    expectedData.add(testData.clone());
                    insert.setInt(1, i);
                    var in = new ByteArrayInputStream(testData);
                    insert.setBinaryStream(2, in, testData.length);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (var select = con.createStatement()) {
                var rs = select.executeQuery("SELECT id, obj_data FROM test_blob ORDER BY id");
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    byte[] data = rs.getBytes(2);

                    assertArrayEquals(expectedData.get(id), data, "Unexpected blob data for id" + id);
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
        var expectedData = new ArrayList<byte[]>();

        try (var con = getConnectionViaDriverManager(PropertyNames.useServerBatch, String.valueOf(useServerBatch))) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (var insert = con.prepareStatement("INSERT INTO test_blob (id, clob_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    expectedData.add(testData.clone());
                    insert.setInt(1, i);
                    var in = new ByteArrayInputStream(testData);
                    insert.setBinaryStream(2, in, testData.length);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (var select = con.createStatement()) {
                var rs = select.executeQuery("SELECT id, clob_data FROM test_blob ORDER BY id");
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
    @Unstable("Susceptible to character set transliteration issues")
    void testRepeatedBatchExecutionWithClobFromString(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        executeCreateTable(con, CREATE_TEST_BLOB_TABLE);
        var expectedData = new ArrayList<String>();

        try (var con = getConnectionViaDriverManager(Map.of(
                PropertyNames.useServerBatch, String.valueOf(useServerBatch),
                PropertyNames.encoding, "ISO8859_1"))) {
            con.setAutoCommit(false);
            // Execute two separate batches inserting a random blob
            try (var insert = con.prepareStatement("INSERT INTO test_blob (id, clob_data) VALUES (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    byte[] testData = DataGenerator.createRandomBytes(50);
                    String testString = new String(testData, StandardCharsets.ISO_8859_1);
                    expectedData.add(testString);
                    insert.setInt(1, i);
                    insert.setString(2, testString);
                    insert.addBatch();
                    insert.executeBatch();
                }
            }

            // Check if the stored data matches the retrieved data
            try (var select = con.createStatement()) {
                var rs = select.executeQuery("SELECT id, clob_data FROM test_blob ORDER BY id");
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    String data = rs.getString(2);

                    assertEquals(expectedData.get(id), data, "Unexpected blob data for id " + id);
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

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @MethodSource
    void testOctetsInsertAndSelect(String columName, int jdbcType) throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);

        try (var insert = con.prepareStatement("INSERT INTO TESTTAB(ID, " + columName + ", FIELD1) VALUES (?, ?, ?)")) {
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

            try (var select = con.createStatement()) {
                var rs = select.executeQuery("SELECT ID, " + columName + " FROM TESTTAB ORDER BY ID");
                ResultSetMetaData rsmd = rs.getMetaData();
                assertEquals(jdbcType, rsmd.getColumnType(2));

                assertNextRow(rs);
                assertEquals(1, rs.getInt(1));
                assertArrayEquals(data1, rs.getBytes(2));

                assertNextRow(rs);
                assertEquals(2, rs.getInt(1));
                assertArrayEquals(expectedData2, rs.getBytes(2));

                assertNoNextRow(rs);
            }
        }
    }

    @Test
    void testSetBigIntegerParameters() throws Exception {
        executeCreateTable(con, CREATE_TEST_BIG_INTEGER_TABLE);
        final String testBigIntegerValueString = "1" + Long.MAX_VALUE;
        try (var pstmt = con.prepareStatement(
                "insert into test_big_integer(bigintfield, varcharfield) values (?, ?)")) {
            pstmt.setObject(1, BigInteger.ONE);
            pstmt.setObject(2, new BigInteger(testBigIntegerValueString));
            pstmt.executeUpdate();
        }

        try (var pstmt = con.prepareStatement("select bigintfield, varcharfield from test_big_integer")) {
            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertEquals(1, rs.getLong("bigintfield"), "Unexpected value for bigintfield");
            assertEquals(BigInteger.ONE, rs.getObject("bigintfield", BigInteger.class),
                    "Unexpected value for bigintfield as BigInteger");
            assertEquals(testBigIntegerValueString, rs.getString("varcharfield"), "Unexpected value for varcharfield");
            assertEquals(new BigInteger(testBigIntegerValueString), rs.getObject("varcharfield", BigInteger.class),
                    "Unexpected value for varcharfield as BigInteger");
            assertNoNextRow(rs);
        }
    }

    // See JDBC-472; TODO this test doesn't reproduce the issue
    @Test
    void testExecuteProcedureWithoutReturnValues() throws Exception {
        executeDDL(con, """
                recreate table example_table (
                  id integer primary key,
                  example_date date
                )""");
        executeDDL(con, """
                recreate table another_example_table (
                  ex_id integer,
                  example_date varchar(100)
                )""");
        executeDDL(con, """
                recreate table example_table_2 (
                  id integer,
                  example_date date
                )""");
        executeDDL(con, """
                CREATE OR ALTER PROCEDURE EXAMPLE_PROCEDURE(
                    EX_ID integer)
                AS
                DECLARE VARIABLE EX_BL integer;
                declare variable EXAMPLE_DATE date;
                declare variable EXAMPLE_DATE_2 date;
                  /* ... (declaring other variables) */
                BEGIN

                  ex_bl = 0;  /*
                  RECOVERING INFORMATION
                  */
                  SELECT
                    /* SELECTING SOME FIELDS */
                    COALESCE(EXAMPLE_DATE - 1, CURRENT_DATE)
                  FROM
                    EXAMPLE_TABLE
                  WHERE
                    ID = :EX_ID
                  INTO
                    /* SAME AS TABLE FIELD NAMES */
                    EXAMPLE_DATE_2;

                  IF (EX_BL = 1) THEN
                    DELETE FROM ANOTHER_EXAMPLE_TABLE WHERE EX_ID = :EX_ID;
                  ELSE
                  BEGIN
                    /*
                    ANOTHER SELECT
                    */
                    SELECT FIRST 1
                      example_date
                    FROM
                      ANOTHER_EXAMPLE_TABLE
                    WHERE
                      EX_ID = :EX_ID
                    ORDER BY
                      EXAMPLE_DATE
                    INTO
                      example_date_2;
                     /* ALIASES */

                    IF (example_date_2 is null or example_date_2 < current_date) THEN
                    BEGIN
                      EXAMPLE_DATE = EXAMPLE_DATE_2;
                      WHILE (example_date < current_date) DO
                      BEGIN
                        INSERT INTO EXAMPLE_TABLE_2 (
                          id,
                          example_date)
                        VALUES (
                          :EX_ID,
                          :example_date);
                        EXAMPLE_DATE = EXAMPLE_DATE + 1;
                      END
                    END

                    SELECT FIRST 1
                      example_date
                    FROM
                      EXAMPLE_TABLE_2
                    WHERE
                      ID = :EX_ID
                    ORDER BY
                      EXAMPLE_DATE DESC
                    INTO
                      example_date_2;

                    IF (example_date_2 is null or example_date_2 < current_date) THEN
                    BEGIN
                      EXAMPLE_DATE = EXAMPLE_DATE_2;
                      WHILE (example_date < current_date) DO
                      BEGIN
                        INSERT INTO EXAMPLE_TABLE_2 (
                          id,
                         example_date)
                        VALUES (
                          :EX_ID,
                          :example_date);
                        EXAMPLE_DATE = EXAMPLE_DATE + 1;
                      END
                    END

                    DELETE FROM
                      EXAMPLE_TABLE_2
                    WHERE
                      (ID = :EX_ID) AND
                      ((EXAMPLE_DATE < current_date - 5) OR (EXAMPLE_DATE > current_date));
                  END

                END""");

        try (var stmt = con.createStatement()) {
            stmt.executeUpdate("insert into example_table(id, example_date) values (1, current_date - 5)");
        }

        con.setAutoCommit(false);

        try (var pstmt = con.prepareStatement("execute procedure EXAMPLE_PROCEDURE(?)")) {
            //            for (int cnt = 0; cnt < 100; cnt++) {
            pstmt.setInt(1, 1);
            assertDoesNotThrow(() -> pstmt.executeUpdate());
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
        executeDDL(con, """
                recreate table long_string (
                 id integer primary key,
                 stringcolumn varchar(1024)
                )""");
        final int testLength = 1024;
        final String testvalue = "a".repeat(testLength);
        try (var pstmt = con.prepareStatement("insert into long_string (id, stringcolumn) values (?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, testvalue);
            pstmt.executeUpdate();
        }
        try (var stmt = con.createStatement()) {
            var rs = stmt.executeQuery("select char_length(stringcolumn), stringcolumn from long_string where id = 1");
            assertNextRow(rs);
            assertEquals(testLength, rs.getInt(1), "Unexpected string length in Firebird");
            assertEquals(testvalue, rs.getString(2), "Selected string value does not match inserted");
            assertNoNextRow(rs);
        }
    }

    /**
     * Tests if reexecuting a prepared statement after fetch failure works for holdable result set.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-531">JDBC-531</a>
     * </p>
     */
    @Test
    @SuppressWarnings("java:S5783")
    void testReexecuteStatementAfterFailure() throws Exception {
        executeDDL(con, "recreate table encoding_error ("
                        + " id integer primary key, "
                        + " stringcolumn varchar(10) character set NONE"
                        + ")");
        try (var pstmt = con.prepareStatement("insert into encoding_error (id, stringcolumn) values (?, ?)")) {
            pstmt.setInt(1, 1);
            pstmt.setBytes(2, new byte[] { (byte) 0xFF, (byte) 0xFF });
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.executeUpdate();
        }
        con.setAutoCommit(false);
        try (var pstmt = con.prepareStatement(
                "select cast(stringcolumn as varchar(10) character set UTF8) from encoding_error",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            assertThrows(SQLException.class, () -> {
                var rs = pstmt.executeQuery();
                rs.next();
            });

            var exception = assertThrows(SQLException.class, () -> {
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

        try (var stmt = con.prepareStatement(SELECT_DATA)) {
            assertTrue(stmt.execute(), "expected a result set");
            var rs = stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertResultSetOpen(rs);
                assertFalse(stmt.isClosed(), "Statement should be open");
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertResultSetOpen(rs, "Result set should be still open after last result read in auto-commit");
            assertFalse(stmt.getMoreResults(), "expected no result set for getMoreResults");
            assertResultSetClosed(rs, "Result set should be closed after getMoreResults");
            assertEquals(-1, stmt.getUpdateCount(), "no update count (-1) was expected");
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @ValueSource(strings = { "binary", "text" })
    void testSetClobNullClob(String subType) throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type " + subType + ") from rdb$database")) {
            pstmt.setClob(1, (Clob) null);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getClob(1));
            assertNoNextRow(rs);
        }
    }

    @Test
    void testSetClobNullReader() throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type text) from rdb$database")) {
            pstmt.setClob(1, (Reader) null);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getClob(1));
            assertNoNextRow(rs);
        }
    }

    @Test
    void testSetClobNullReaderWithLength() throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type text) from rdb$database")) {
            pstmt.setClob(1, null, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getClob(1));
            assertNoNextRow(rs);
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @ValueSource(strings = { "binary", "text" })
    void testSetBlobNullBlob(String subType) throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type " + subType + ") from rdb$database")) {
            pstmt.setBlob(1, (Blob) null);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getBlob(1));
            assertNoNextRow(rs);
        }
    }

    @Test
    void testSetBlobNullInputStream() throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type binary) from rdb$database")) {
            pstmt.setBlob(1, (InputStream) null);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getBlob(1));
            assertNoNextRow(rs);
        }
    }

    @Test
    void testSetBlobNullInputStreamWithLength() throws Exception {
        try (var pstmt = con.prepareStatement("select cast(? as blob sub_type binary) from rdb$database")) {
            pstmt.setBlob(1, null, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            assertNull(rs.getBlob(1));
            assertNoNextRow(rs);
        }
    }

    /**
     * Tests for <a href="https://github.com/FirebirdSQL/jaybird/issues/729">jaybird#729</a>.
     */
    @Test
    void preparedStatementExecuteProcedureShouldNotTrim_729() throws Exception {
        executeDDL(con, """
                create procedure char_return returns (val char(5)) as
                begin
                  val = 'A';
                end""");

        try (var stmt = con.prepareStatement("execute procedure char_return")) {
            var rs = stmt.executeQuery();
            assertNextRow(rs);
            assertAll(
                    () -> assertEquals("A    ", rs.getObject(1), "Unexpected trim by getObject"),
                    () -> assertEquals("A    ", rs.getString(1), "Unexpected trim by getString"));
            assertNoNextRow(rs);
        }
    }

    /**
     * Test for <a href="https://github.com/FirebirdSQL/jaybird/issues/788">jaybird#788</a>
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void executeBatchWithoutParameters(boolean useServerBatch) throws Exception {
        executeCreateTable(con, CREATE_TABLE);

        try (var con = getConnectionViaDriverManager(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
             var pstmt = con.prepareStatement("insert into test default values")) {
            pstmt.addBatch();
            assertDoesNotThrow(pstmt::executeBatch);
        }
        try (var stmt = con.createStatement()) {
            var rs = stmt.executeQuery("select count(*) from test");
            assertNextRow(rs);
            assertEquals(1, rs.getInt(1), "Expected one row in table test");
            assertNoNextRow(rs);
        }
    }

    @Test
    void poolable_value() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);
        try (var stmt = con.prepareStatement(INSERT_DATA)) {
            assertTrue(stmt.isPoolable(), "expected poolable initially true");
            stmt.setPoolable(false);
            assertFalse(stmt.isPoolable(), "expected poolable false after set false");
            stmt.setPoolable(true);
            assertTrue(stmt.isPoolable(), "expected poolable true after set true");
        }
    }

    @Test
    void executeWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, "recreate exception EX_TEST_EXCEPTION 'exception to end execution with error'");
        try (var stmt = con.prepareStatement("""
                execute block as
                begin
                  exception EX_TEST_EXCEPTION;
                end""")) {
            assertThrows(SQLException.class, stmt::execute);
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeWithResultSetWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, """
                recreate procedure RAISE_EXCEPTION_RS (PARAM1 varchar(50) not null) returns (COLUMN1 varchar(50)) as
                begin
                  suspend;
                end""");

        try (var stmt = con.prepareStatement("select * from RAISE_EXCEPTION_RS(?)")) {
            stmt.setString(1, null);
            assertThrows(SQLException.class, stmt::execute);
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeUpdateWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, "recreate exception EX_TEST_EXCEPTION 'exception to end execution with error'");
        try (var stmt = con.prepareStatement("""
                execute block as
                begin
                  exception EX_TEST_EXCEPTION;
                end""")) {
            assertThrows(SQLException.class, stmt::executeUpdate);
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeQueryWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, """
                recreate procedure RAISE_EXCEPTION_RS (PARAM1 varchar(50) not null) returns (COLUMN1 varchar(50)) as
                begin
                  suspend;
                end""");

        try (var stmt = con.prepareStatement("select * from RAISE_EXCEPTION_RS(?)")) {
            stmt.setString(1, null);
            assertThrows(SQLException.class, stmt::executeQuery);
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void setObject_Reader_scaleOrLength() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        String sourceValue = "1234567890";
        final int usedLength = 5;
        try (var pstmt = con.prepareStatement("INSERT INTO testtab(id, field1, field2) VALUES(1, '', ?)")) {
            pstmt.setObject(1, new StringReader(sourceValue), JDBCType.VARCHAR, usedLength);
            pstmt.execute();
        }

        try (var stmt = con.createStatement()) {
            var rs = stmt.executeQuery("select field2 from testtab");
            assertNextRow(rs);
            assertRowEquals(rs, List.of(sourceValue.substring(0, usedLength)));
        }
    }

    @Test
    void setObject_InputStream_scaleOrLength() throws Exception {
        executeCreateTable(con, CREATE_TEST_CHARS_TABLE);
        String sourceValue = "1234567890";
        final int usedLength = 5;
        try (var pstmt = con.prepareStatement("INSERT INTO testtab(id, field1, field2) VALUES(1, '', ?)")) {
            pstmt.setObject(1, new ByteArrayInputStream(sourceValue.getBytes(StandardCharsets.US_ASCII)),
                    JDBCType.VARCHAR, usedLength);
            pstmt.execute();
        }

        try (var stmt = con.createStatement()) {
            var rs = stmt.executeQuery("select field2 from testtab");
            assertNextRow(rs);
            assertRowEquals(rs, List.of(sourceValue.substring(0, usedLength)));
        }
    }

    private void prepareTestData() throws SQLException {
        con.setAutoCommit(false);
        try (var pstmt = con.prepareStatement(INSERT_DATA)) {
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
