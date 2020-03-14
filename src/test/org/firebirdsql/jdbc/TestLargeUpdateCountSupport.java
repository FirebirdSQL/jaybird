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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * Test for large update count support.
 * <p>
 * These tests only check whether large update support returns correct values, it doesn't test whether it can actually
 * return values larger than {@link Integer#MAX_VALUE} as that would take hours.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@SuppressWarnings("Since15")
public class TestLargeUpdateCountSupport extends FBJUnit4TestBase {

    private static final String CREATE_SIMPLE_TABLE = "create table simpletable (value_column varchar(4) check (value_column <> 'err'))";
    private static final String INSERT_SIMPLE_TABLE = "insert into simpletable (value_column) values ('abc')";
    private static final String INSERT_SIMPLE_TABLE_ERR = "insert into simpletable (value_column) values ('abcde')";
    private static final String INSERT_SIMPLE_TABLE_PARAMETER = "insert into simpletable (value_column) values (?)";
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        DdlHelper.executeCreateTable(connection, CREATE_SIMPLE_TABLE);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testStatementExecuteLargeUpdate() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE);
            assertEquals("updateCount", 1, updateCount);
        }
    }

    @Test
    public void testStatementExecuteLargeUpdate_RETURN_GENERATED_KEYS() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, Statement.RETURN_GENERATED_KEYS);
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testStatementExecuteLargeUpdate_arrInt() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, new int[] { 1 });
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testStatementExecuteLargeUpdate_arrString() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            long updateCount = stmt.executeLargeUpdate(INSERT_SIMPLE_TABLE, new String[] { "VALUE_COLUMN" });
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testStatementExecuteAndGetLargeUpdateCount() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            boolean hasResultSet = stmt.execute(INSERT_SIMPLE_TABLE);
            assertFalse("hasResultSet", hasResultSet);
            assertEquals("updateCount", 1, stmt.getLargeUpdateCount());
        }
    }

    @Test
    public void testStatementExecuteLargeBatch() throws Exception {
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
    public void testStatementExecuteLargeBatchWithError() throws Exception {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            stmt.addBatch(INSERT_SIMPLE_TABLE);
            stmt.addBatch(INSERT_SIMPLE_TABLE_ERR);
            stmt.addBatch(INSERT_SIMPLE_TABLE);

            stmt.executeLargeBatch();

            fail("should throw exception");
        } catch (BatchUpdateException e) {
            long[] updateCounts = e.getLargeUpdateCounts();
            assertThat("updateCounts should only include successfully executed statements up to first error",
                    updateCounts, equalTo(new long[] { 1 }));
        }
    }

    // TODO Tests for get/setLargeMaxRows

    @Test
    public void testPreparedStatementExecuteLargeUpdate() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE)) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals("updateCount", 1, updateCount);
        }
    }

    @Test
    public void testPreparedStatementExecuteLargeUpdate_RETURN_GENERATED_KEYS() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testPreparedStatementExecuteLargeUpdate_arrInt() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, new int[] { 1 })) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testPreparedStatementExecuteLargeUpdate_arrString() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE, new String[] { "VALUE_COLUMN" })) {
            long updateCount = pstmt.executeLargeUpdate();
            assertEquals("updateCount", 1, updateCount);
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue("should have 'generated' key", rs.next());
                assertEquals("value_column", "abc", rs.getString("VALUE_COLUMN"));
            }
        }
    }

    @Test
    public void testPreparedStatementExecuteAndGetLargeUpdateCount() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE)) {
            boolean hasResultSet = pstmt.execute();
            assertFalse("hasResultSet", hasResultSet);
            assertEquals("updateCount", 1, pstmt.getLargeUpdateCount());
        }
    }

    @Test
    public void testPreparedStatementExecuteLargeBatch() throws Exception {
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
    public void testPreparedStatementExecuteLargeBatchWithError() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SIMPLE_TABLE_PARAMETER)) {
            pstmt.setString(1, "abc");
            pstmt.addBatch();
            pstmt.setString(1, "err");
            pstmt.addBatch();
            pstmt.setString(1, "cde");
            pstmt.addBatch();

            pstmt.executeLargeBatch();

            fail("should throw exception");
        } catch (BatchUpdateException e) {
            long[] updateCounts = e.getLargeUpdateCounts();
            assertThat("updateCounts should only include successfully executed statements up to first error",
                    updateCounts, equalTo(new long[] { 1 }));
        }
    }

    // TODO Test CallableStatement large update count support
}
