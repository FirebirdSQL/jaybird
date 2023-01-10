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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for large update count support.
 * <p>
 * These tests only check whether large update support returns correct values, it doesn't test whether it can actually
 * return values larger than {@link Integer#MAX_VALUE} as that would take hours.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class LargeUpdateCountSupportTest {

    private static final String CREATE_SIMPLE_TABLE = "create table simpletable (value_column varchar(4) check (value_column <> 'err'))";
    private static final String INSERT_SIMPLE_TABLE = "insert into simpletable (value_column) values ('abc')";
    private static final String INSERT_SIMPLE_TABLE_ERR = "insert into simpletable (value_column) values ('abcde')";
    private static final String INSERT_SIMPLE_TABLE_PARAMETER = "insert into simpletable (value_column) values (?)";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_SIMPLE_TABLE);

    private static Connection connection;

    @BeforeAll
    static void setupAll() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setup() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from simpletable");
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void testStatementExecuteLargeUpdate() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE);
            assertEquals(1, updateCount, "updateCount");
        }
    }

    @Test
    void testStatementExecuteLargeUpdate_RETURN_GENERATED_KEYS() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, Statement.RETURN_GENERATED_KEYS);
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testStatementExecuteLargeUpdate_arrInt() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, new int[] { 1 });
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testStatementExecuteLargeUpdate_arrString() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, new String[] { "VALUE_COLUMN" });
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testStatementExecuteAndGetLargeUpdateCount() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            boolean hasResultSet = stmt.execute(INSERT_SIMPLE_TABLE);
            assertFalse(hasResultSet, "hasResultSet");
            assertEquals(1, stmt.getLargeUpdateCount(), "updateCount");
        }
    }

    @Test
    void testStatementExecuteLargeBatch() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            stmt.addBatch(INSERT_SIMPLE_TABLE);
            stmt.addBatch(INSERT_SIMPLE_TABLE);
            stmt.addBatch(INSERT_SIMPLE_TABLE);

            long[] updateCounts = stmt.executeLargeBatch();
            assertThat("updateCounts", updateCounts, equalTo(new long[] { 1, 1, 1 }));
        }
    }

    @Test
    void testStatementExecuteLargeBatchWithError() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            stmt.addBatch(INSERT_SIMPLE_TABLE);
            stmt.addBatch(INSERT_SIMPLE_TABLE_ERR);
            stmt.addBatch(INSERT_SIMPLE_TABLE);

            BatchUpdateException exception = assertThrows(BatchUpdateException.class, stmt::executeLargeBatch);
            long[] updateCounts = exception.getLargeUpdateCounts();
            assertThat("updateCounts should only include successfully executed statements up to first error",
                    updateCounts, equalTo(new long[] { 1 }));
        }
    }

    // TODO Tests for get/setLargeMaxRows

    @Test
    void testPreparedStatementExecuteLargeUpdate() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE)) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals(1, updateCount, "updateCount");
        }
    }

    @Test
    void testPreparedStatementExecuteLargeUpdate_RETURN_GENERATED_KEYS() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testPreparedStatementExecuteLargeUpdate_arrInt() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, new int[] { 1 })) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testPreparedStatementExecuteLargeUpdate_arrString() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, new String[] { "VALUE_COLUMN" })) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals(1, updateCount, "updateCount");
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "should have 'generated' key");
                assertEquals("abc", rs.getString("VALUE_COLUMN"), "value_column");
            }
        }
    }

    @Test
    void testPreparedStatementExecuteAndGetLargeUpdateCount() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE)) {
            boolean hasResultSet = pstmt.execute();
            assertFalse(hasResultSet, "hasResultSet");
            assertEquals(1, pstmt.getLargeUpdateCount(), "updateCount");
        }
    }

    @Test
    void testPreparedStatementExecuteLargeBatch() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE_PARAMETER)) {
            pstmt.setString(1, "abc");
            pstmt.addBatch();
            pstmt.setString(1, "bcd");
            pstmt.addBatch();
            pstmt.setString(1, "cde");
            pstmt.addBatch();

            long[] updateCounts = pstmt.executeLargeBatch();
            assertThat("updateCounts", updateCounts, equalTo(new long[] { 1, 1, 1 }));
        }
    }

    @Test
    void testPreparedStatementExecuteLargeBatchWithError() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE_PARAMETER)) {
            pstmt.setString(1, "abc");
            pstmt.addBatch();
            pstmt.setString(1, "err");
            pstmt.addBatch();
            pstmt.setString(1, "cde");
            pstmt.addBatch();

            BatchUpdateException exception = assertThrows(BatchUpdateException.class, pstmt::executeLargeBatch);
            long[] updateCounts = exception.getLargeUpdateCounts();
            assertThat("updateCounts should only include successfully executed statements up to first error",
                    updateCounts, equalTo(new long[] { 1 }));
        }
    }

    // TODO Test CallableStatement large update count support
}
