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
import org.firebirdsql.common.MaxFbTimePrecision;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.FbAssumptions.assumeServerBatchSupport;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class BatchUpdatesTest {

    private static final String RECREATE_BATCH_UPDATES_TABLE = """
            RECREATE TABLE batch_updates(
              id INTEGER primary key,
              str_value varchar(50),
              clob_value BLOB SUB_TYPE 1
            )""";

    // Only 2.5 types
    private static final String RECREATE_WIDE_BATCH_TABLE = """
            RECREATE TABLE wide_batch (
            /* 1*/ id integer,
            /* 2*/ bigintval bigint,
            /* 3*/ smallintval smallint,
            /* 4*/ sintnumval numeric(4, 2),
            /* 5*/ intnumval numeric(9, 2),
            /* 6*/ bintnumval numeric(18, 2),
            /* 7*/ doubleval double precision,
            /* 8*/ floatval float,
            /* 9*/ smallcharval char(11),
            /*10*/ largecharval char(1025),
            /*11*/ smallvarcharval varchar(11),
            /*12*/ largevarcharval varchar(1025),
            /*13*/ dateval date,
            /*14*/ timeval time,
            /*15*/ timestampval timestamp,
            /*16*/ blobval blob
            )""";

    private static final String INSERT_WIDE_BATCH = "insert into wide_batch (id, bigintval, smallintval, sintnumval, "
            + "intnumval, bintnumval, doubleval, floatval, smallcharval, largecharval, smallvarcharval, "
            + "largevarcharval, dateval, timeval, timestampval, blobval) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CREATE_DUMMY_PROCEDURE = """
            create procedure dummy_procedure (value1 integer)
            as
            begin
              /* intentionally empty */
            end""";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_DUMMY_PROCEDURE);

    /**
     * Test if batch updates in {@link Statement} implementation works correctly.
     */
    @Test
    void testStatementBatch() throws SQLException {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement()) {
            stmt.execute(RECREATE_BATCH_UPDATES_TABLE);
            connection.setAutoCommit(false);
            stmt.addBatch("INSERT INTO batch_updates(id, str_value) VALUES (1, 'test')");
            stmt.addBatch("INSERT INTO batch_updates(id, str_value) VALUES (2, 'another')");
            stmt.addBatch("UPDATE batch_updates SET id = 3 WHERE id = 2");

            int[] updates = stmt.executeBatch();

            assertEquals(3, updates.length, "Should contain 3 results");
            assertArrayEquals(new int[] { 1, 1, 1 }, updates, "Should update one row each time");

            var rs = stmt.executeQuery("SELECT * FROM batch_updates");
            int counter = 0;
            while (rs.next())
                counter++;

            assertEquals(2, counter, "Should insert 2 rows");

            stmt.addBatch("DELETE FROM batch_updates");
            stmt.clearBatch();
            updates = stmt.executeBatch();

            assertEquals(0, updates.length, "No updates should have been made");

            connection.commit();
        }
    }

    /**
     * Test if batch updates work correctly with prepared statement.
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testPreparedStatementBatch(boolean useServerBatch) throws SQLException {
        // NOTE: Intentionally not checking server batch support when useServerBatch=true to verify fall back works
        try (Connection connection = createConnection(useServerBatch);
             var stmt = connection.createStatement()) {
            stmt.execute(RECREATE_BATCH_UPDATES_TABLE);
            connection.setAutoCommit(false);

            try (var ps = connection.prepareStatement(
                    "INSERT INTO batch_updates(id, str_value, clob_value) VALUES (?, ?, ?)")) {
                ps.setInt(1, 1);
                ps.setString(2, "test");
                ps.setNull(3, Types.LONGVARBINARY);
                ps.addBatch();

                ps.setInt(1, 3);
                ps.setCharacterStream(2, new StringReader("stream"), 11);
                ps.setString(3, "string");
                ps.addBatch();

                ps.setInt(1, 2);
                ps.setString(2, "another");
                ps.setNull(3, Types.LONGVARBINARY);
                ps.addBatch();

                ps.executeBatch();
            }

            var rs = stmt.executeQuery("SELECT * FROM batch_updates order by id");
            assertTrue(rs.next(), "expected row 1");
            assertEquals(rs.getInt(1), 1, "id=1");
            assertEquals("test", rs.getString(2), "id=1 str_value");
            assertTrue(rs.next(), "expected row 2");
            assertEquals(rs.getInt(1), 2, "id=2");
            assertEquals("another", rs.getString(2), "id=2 str_value");
            assertTrue(rs.next(), "expected row 3");
            assertEquals(rs.getInt(1), 3, "id=3");
            assertEquals("stream", rs.getString(2), "id=3 str_value");
            assertEquals("string", rs.getString(3), "id=3 clob_value");
            assertFalse(rs.next(), "no more rows");

            connection.commit();
        }
    }

    /**
     * Tests batch with a column for each data type supported by Firebird 2.5 (except decimal).
     * <p>
     * Rationale: check if wider batches are handled correctly (especially for server-side batches).
     * </p>
     */
    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testPreparedStatementWideBatch(boolean useServerBatch) throws SQLException {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        final long bigintBase = Long.MAX_VALUE / 2;
        final int smallintBase = Short.MAX_VALUE / 2;
        final var sintNumBase = new BigDecimal("45.23");
        final var intNumBase = new BigDecimal("8367205.49");
        final var bintNumBase = BigDecimal.valueOf(bigintBase, 2);
        final double doubleBase = bigintBase * 1.3;
        final float floatBase = smallintBase * 0.9f;
        final var shortStringBase = new String(DataGenerator.createRandomAsciiBytes(10), US_ASCII);
        final var largeStringBase = new String(DataGenerator.createRandomAsciiBytes(1024), US_ASCII);
        final var localDateBase = LocalDate.now();
        final var localTimeBase = LocalTime.now();
        final var localDateTimeBase = LocalDateTime.now();
        final int noOfRows = 11;

        try (Connection connection = createConnection(useServerBatch);
             var stmt = connection.createStatement()) {
            stmt.execute(RECREATE_WIDE_BATCH_TABLE);
            connection.setAutoCommit(false);

            try (var pstmt = connection.prepareStatement(INSERT_WIDE_BATCH)) {
                IntStream.rangeClosed(1, noOfRows).forEach(id -> {
                    try {
                        pstmt.setInt(1, id);
                        pstmt.setLong(2, bigintBase + id);
                        pstmt.setInt(3, smallintBase + id);
                        pstmt.setBigDecimal(4, sintNumBase.add(BigDecimal.valueOf(id, 2)));
                        pstmt.setBigDecimal(5, intNumBase.add(BigDecimal.valueOf(id, 2)));
                        pstmt.setBigDecimal(6, bintNumBase.add(BigDecimal.valueOf(id, 2)));
                        pstmt.setDouble(7, doubleBase + id);
                        pstmt.setFloat(8, floatBase + id);
                        String smallString = (id + shortStringBase).substring(0, 11);
                        String largeString = (id + largeStringBase).substring(0, 1025);
                        pstmt.setString(9, smallString);
                        pstmt.setString(10, largeString);
                        pstmt.setString(11, smallString);
                        pstmt.setString(12, largeString);
                        pstmt.setObject(13, localDateBase.plusDays(id));
                        pstmt.setObject(14, localTimeBase.plusMinutes(id));
                        pstmt.setObject(15, localDateTimeBase.plusHours(id));
                        pstmt.setString(16, largeString);
                        pstmt.addBatch();
                    } catch (SQLException e) {
                        fail(e);
                    }
                });
                int[] updateCounts = pstmt.executeBatch();
                int[] expectedCounts = new int[noOfRows];
                Arrays.fill(expectedCounts, 1);
                assertArrayEquals(expectedCounts, updateCounts, "update counts");
            }

            connection.commit();

            try (var rs = stmt.executeQuery("select * from wide_batch order by id")) {
                IntStream.rangeClosed(1, noOfRows).forEach(id -> {
                    try {
                        assertTrue(rs.next(), "expected row for " + id);
                        assertEquals(id, rs.getInt(1));
                        assertEquals(bigintBase + id, rs.getLong(2));
                        assertEquals(smallintBase + id, rs.getInt(3));
                        assertEquals(sintNumBase.add(BigDecimal.valueOf(id, 2)), rs.getBigDecimal(4));
                        assertEquals(intNumBase.add(BigDecimal.valueOf(id, 2)), rs.getBigDecimal(5));
                        assertEquals(bintNumBase.add(BigDecimal.valueOf(id, 2)), rs.getBigDecimal(6));
                        assertEquals(doubleBase + id, rs.getDouble(7), 0.1);
                        assertEquals(floatBase + id, rs.getFloat(8), 0.1);
                        String expectedSmallString = (id + shortStringBase).substring(0, 11);
                        String expectedLargeString = (id + largeStringBase).substring(0, 1025);
                        assertEquals(expectedSmallString, rs.getString(9));
                        assertEquals(expectedLargeString, rs.getString(10));
                        assertEquals(expectedSmallString, rs.getString(11));
                        assertEquals(expectedLargeString, rs.getString(12));
                        assertEquals(localDateBase.plusDays(id), rs.getObject(13, LocalDate.class));
                        assertEquals(localTimeBase.plusMinutes(id).truncatedTo(MaxFbTimePrecision.INSTANCE),
                                rs.getObject(14, LocalTime.class));
                        assertEquals(localDateTimeBase.plusHours(id).truncatedTo(MaxFbTimePrecision.INSTANCE),
                                rs.getObject(15, LocalDateTime.class));
                        assertEquals(expectedLargeString, rs.getString(16));
                    } catch (SQLException e) {
                        fail(e);
                    }
                });
                assertFalse(rs.next(), "expected no more rows");
            }
        }
    }

    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testExecuteProcedureInPreparedStatementBatch(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        try (Connection connection = createConnection(useServerBatch)) {
            connection.setAutoCommit(false);

            try (var pstmt = connection.prepareStatement("{call dummy_procedure(?)}")) {
                int rows = 5;
                IntStream.rangeClosed(1, 5).forEach(i -> {
                    try {
                        pstmt.setInt(1, i);
                        pstmt.addBatch();
                    } catch (SQLException e) {
                        fail(e);
                    }
                });
                int[] updateCounts = pstmt.executeBatch();
                int[] expectedUpdateCounts = new int[rows];
                assertArrayEquals(expectedUpdateCounts, updateCounts);
            }
        }
    }

    @Test
    void testExecuteProcedureInCallableStatementBatch() throws Exception {
        // NOTE: server batch not supported for callable statement
        try (Connection connection = getConnectionViaDriverManager();
             var pstmt = connection.prepareCall("{call dummy_procedure(?)}")) {
            int rows = 5;
            IntStream.rangeClosed(1, 5).forEach(i -> {
                try {
                    pstmt.setInt(1, i);
                    pstmt.addBatch();
                } catch (SQLException e) {
                    fail(e);
                }
            });
            int[] updateCounts = pstmt.executeBatch();
            int[] expectedUpdateCounts = new int[rows];
            assertArrayEquals(expectedUpdateCounts, updateCounts);
        }
    }

    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testPreparedStatementExecuteBatch_endAtFirstFailure(boolean useServerBatch) throws Exception {
        try (Connection connection = createConnection(useServerBatch);
             var stmt = connection.createStatement()) {
            stmt.execute(RECREATE_BATCH_UPDATES_TABLE);
            connection.setAutoCommit(false);
            try (var ps = connection.prepareStatement("INSERT INTO batch_updates(id, str_value) VALUES (?, ?)")) {
                ps.setInt(1, 1);
                ps.setString(2, "first");
                ps.addBatch();
                ps.setInt(1, 1);
                ps.setString(2, "should fail");
                ps.addBatch();
                ps.setInt(1, 2);
                ps.setString(2, "should not execute");
                ps.addBatch();

                BatchUpdateException bue = assertThrows(BatchUpdateException.class, ps::executeBatch);
                assertArrayEquals(new int[] { 1 }, bue.getUpdateCounts());
                assertThat(bue, message(containsString("violation of PRIMARY or UNIQUE KEY constraint")));
            }

            try (var rs = stmt.executeQuery("select id from batch_updates")) {
                assertTrue(rs.next(), "expected a row");
                assertEquals(1, rs.getInt(1));
                assertFalse(rs.next(), "expected no more rows");
            } finally {
                connection.commit();
            }
        }
    }

    /**
     * Test if batch updates work correctly with more than 64 blobs (triggers ping/batch_sync in enqueueDeferredAction)
     */
    @Test
    void testPreparedStatementBatch_65Blobs() throws SQLException {
        // Only useful to test with real batch support
        assumeServerBatchSupport();
        try (Connection connection = createConnection(true);
             var stmt = connection.createStatement()) {
            stmt.execute(RECREATE_BATCH_UPDATES_TABLE);
            connection.setAutoCommit(false);

            // one more than the BATCH_LIMIT
            final int blobCount = 100;
            try (var ps = connection.prepareStatement("INSERT INTO batch_updates(id, clob_value) VALUES (?, ?)")) {
                IntStream.rangeClosed(1, blobCount).forEach(i -> {
                    try {
                        ps.setInt(1, i);
                        ps.setString(2, "string" + i);
                        ps.addBatch();
                    } catch (SQLException e) {
                        fail(e);
                    }
                });

                ps.executeBatch();
            }

            try (var rs = stmt.executeQuery("SELECT id, clob_value FROM batch_updates order by id")) {
                IntStream.rangeClosed(1, blobCount).forEach(i -> {
                    try {
                        assertTrue(rs.next(), "Expected row " + i);
                        assertEquals(i, rs.getInt(1), "id");
                        assertEquals("string" + i, rs.getString(2), "clob_value");
                    } catch (SQLException e) {
                        fail(e);
                    }
                });
                assertFalse(rs.next(), "no more rows");
            }

            connection.commit();
        }
    }

    private static Connection createConnection(boolean useServerBatch) throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        return DriverManager.getConnection(getUrl(), props);
    }

}
