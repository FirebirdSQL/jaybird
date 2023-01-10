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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeServerBatchSupport;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for retrieval of auto generated keys through {@link java.sql.PreparedStatement}
 * implementation {@link FBPreparedStatement} created through {@link FBConnection}
 * <p>
 * This is an integration test which uses an actual database.
 * </p>
 *
 * @author Mark Rotteveel
 */
class FBPreparedStatementGeneratedKeysTest extends FBTestGeneratedKeysBase {

    // TODO Add test cases with quoted and unquoted object names

    private static final String TEXT_VALUE = "Some text to insert";
    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES (?)";

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with value {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: INSERT statement type and empty generatedKeys result set.
     * </p>
     */
    @Test
    void testPrepare_INSERT_noGeneratedKeys() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_INSERT, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement not to produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(0, metaData.getColumnCount(), "Expected result set without columns");

                assertFalse(rs.next(), "Expected no rows in result set");
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testPrepare_INSERT_returnGeneratedKeys() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertEquals(TEXT_VALUE, rs.getString(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * The same test as {@link #testPrepare_INSERT_returnGeneratedKeys()}, but with {@code executeUpdate}.
     */
    @Test
    void testPrepare_INSERT_returnGeneratedKeys_executeUpdate() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertEquals(1, stmt.executeUpdate(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertEquals(TEXT_VALUE, rs.getString(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    @ParameterizedTest(name = "[{index}] useServerBatch = {0}")
    @ValueSource(booleans = { true, false })
    void testReturnGeneratedKeysWithBatchExecution(boolean useServerBatch) throws Exception {
        if (useServerBatch) {
            assumeServerBatchSupport();
        }
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useServerBatch, String.valueOf(useServerBatch));
        try (Connection con = DriverManager.getConnection(FBTestProperties.getUrl(), props);
             PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, TEXT_VALUE + "1");
            stmt.addBatch();
            stmt.setString(1, TEXT_VALUE + "2");
            stmt.addBatch();
            stmt.setString(1, TEXT_VALUE + "3");
            stmt.addBatch();

            int[] updateCounts = stmt.executeBatch();
            assertArrayEquals(new int[] { 1, 1, 1 }, updateCounts, "update counts");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                for (int i = 1; i <= 3; i++) {
                    assertTrue(rs.next(), "Expected row " + i);
                    assertEquals(512 + i, rs.getInt(1), "Unexpected value");
                    assertEquals(TEXT_VALUE + i, rs.getString(2), "Unexpected value");
                }
                assertFalse(rs.next(), "Expected no more rows");
            }

            stmt.setString(1, TEXT_VALUE);
            assertEquals(1, stmt.executeUpdate(), "expected update count of 1");

            // Checking that generated keys result set of a normal execute doesn't contain the rows from executeBatch
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "Expected row");
                assertEquals(512 + 4, rs.getInt(1), "Unexpected value");
                assertEquals(TEXT_VALUE, rs.getString(2), "Unexpected value");
                assertFalse(rs.next(), "Expected no more rows");
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testPrepare_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(1, metaData.getColumnCount(), "Expected result set with 1 column");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testPrepare_INSERT_returnGeneratedKeys_withReturningAll() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsReturningAll(), "requires RETURNING * support");

        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY + " RETURNING *", Statement.RETURN_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertEquals(TEXT_VALUE, rs.getString(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with
     * {@link Statement#RETURN_GENERATED_KEYS} with an INSERT for a non existent table.
     * <p>
     * Expected: SQLException Table unknown
     * </p>
     */
    @Test
    void testPrepare_INSERT_returnGeneratedKeys_nonExistentTable() {
        // Firebird 4+ uses RETURNING *, while earlier version produce a custom error as the columns can't be retrieved
        boolean usesReturningAll = getDefaultSupportInfo().supportsReturningAll();
        int errorCode = usesReturningAll
                ? ISCConstants.isc_dsql_relation_err
                : JaybirdErrorCodes.jb_generatedKeysNoColumnsFound;

        SQLException exception = assertThrows(SQLSyntaxErrorException.class,
                () -> con.prepareStatement("INSERT INTO TABLE_NON_EXISTENT(TEXT) VALUES (?)", Statement.RETURN_GENERATED_KEYS));
        assertThat(exception, allOf(
                errorCode(equalTo(errorCode)),
                sqlState(equalTo("42S02")),
                fbMessageContains(errorCode, "TABLE_NON_EXISTENT")));
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int[])} with a single column index.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, single row result set with only the specified column.
     * </p>
     */
    @Test
    void testPrepare_INSERT_columnIndexes() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new int[] { 1 })) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(1, metaData.getColumnCount(), "Expected result set with 1 column");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int[])} with a multiple indexes, one for a quoted column.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, single row result set with only the specified columns
     * </p>
     */
    @Test
    void testPrepare_INSERT_columnIndexes_quotedColumn() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new int[] { 1, 3 })) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(2, metaData.getColumnCount(), "Expected result set with 2 column");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("quote_column", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertEquals(2, rs.getInt(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    // Other combination for execute(String, int[]) already covered in TestGeneratedKeysQuery

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, String[])} with a single column name.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @Test
    void testPrepare_INSERT_columnNames() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new String[] { "ID" })) {
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE);
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(1, stmt.getUpdateCount(), "expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(1, metaData.getColumnCount(), "Expected result set with 1 column");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, String[])} with an array of columns containing a non-existent column name.
     * <p>
     * Expected: SQLException for Column unknown.
     * </p>
     */
    @Test
    void testPrepare_INSERT_columnNames_nonExistentColumn() {
        SQLException exception = assertThrows(SQLException.class,
                () -> con.prepareStatement(TEST_INSERT_QUERY, new String[] { "ID", "NON_EXISTENT" }));
        assertThat(exception, allOf(
                errorCode(equalTo(ISCConstants.isc_dsql_field_err)),
                sqlState(equalTo("42S22")),
                message(containsString("Column unknown; NON_EXISTENT"))));
    }

    // TODO In the current implementation executeUpdate uses almost identical logic as execute, decide to test separately or not

    @Test
    void testPrepare_SELECT_RETURN_GENERATED_KEYS_handledNormally() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", Statement.RETURN_GENERATED_KEYS)) {
            boolean isResultSet = pstmt.execute();
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = pstmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testPrepare_SELECT_columnIndexes_handledNormally() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", new int[] { 1, 2 })) {
            boolean isResultSet = pstmt.execute();
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = pstmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testPrepare_SELECT_columnNames_handledNormally() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", new String[] { "field1", "field2" })) {
            boolean isResultSet = pstmt.execute();
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = pstmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testPrepare_INSERT_multipleRows_returnGeneratedKeys() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMultiRowReturning(), "Requires multirow RETURNING support");
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO TABLE_WITH_TRIGGER(TEXT) "
                        + "(select cast(? as varchar(200)) from rdb$database "
                        + "union all select cast(? as varchar(200)) from rdb$database)",
                Statement.RETURN_GENERATED_KEYS)) {
            assertEquals(FirebirdPreparedStatement.TYPE_SELECT, ((FirebirdPreparedStatement) stmt).getStatementType());

            stmt.setString(1, TEXT_VALUE + "_1");
            stmt.setString(2, TEXT_VALUE + "_2");
            assertFalse(stmt.execute(), "Expected statement to not produce a result set");
            assertEquals(2, stmt.getUpdateCount(), "expected update count of 2");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(2, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(513, rs.getInt(1));
                assertEquals(TEXT_VALUE + "_1", rs.getString(2));
                assertTrue(rs.next(), "Expected second row in result set");
                assertEquals(514, rs.getInt(1));
                assertEquals(TEXT_VALUE + "_2", rs.getString(2));
                assertFalse(rs.next(), "Expected no third row");
            }
        }
    }
}
