// SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for retrieval of auto generated keys through {@link java.sql.Statement}
 * implementation {@link FBStatement}.
 * <p>
 * This is an integration test which uses an actual database.
 * </p>
 *
 * @author Mark Rotteveel
 */
class FBStatementGeneratedKeysTest extends FBTestGeneratedKeysBase {

    private static final String TEXT_VALUE = "Some text to insert";
    private static final String TEST_INSERT_QUERY =
            "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('" + TEXT_VALUE + "')";
    private static final String TEST_INSERT_QUERY_WITH_SCHEMA =
            "INSERT INTO PUBLIC.TABLE_WITH_TRIGGER(TEXT) VALUES ('" + TEXT_VALUE + "')";
    private static final String TEST_UPDATE_OR_INSERT =
            "UPDATE OR INSERT INTO TABLE_WITH_TRIGGER(ID, TEXT) VALUES (1, '" + TEXT_VALUE + "') MATCHING (ID)";

    /**
     * Test {@link FBStatement#execute(String, int)} with {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: empty generatedKeys result set.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_noGeneratedKeys(boolean withSchema) throws Exception {
        try (var stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(testInsertQuery(withSchema), Statement.NO_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (no result set) for INSERT without generated keys returned");

            try (var rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(0, metaData.getColumnCount(), "Expected result set without columns");

                assertFalse(rs.next(), "Expected no rows in result set");
            }
        }
    }

    private static String testInsertQuery(boolean withSchema) {
        return withSchema ? TEST_INSERT_QUERY_WITH_SCHEMA : TEST_INSERT_QUERY;
    }

    /**
     * Test {@link FBStatement#executeUpdate(String, int)} with {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: empty generatedKeys result set.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecuteUpdate_INSERT_noGeneratedKeys(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(testInsertQuery(withSchema), Statement.NO_GENERATED_KEYS);
            assertEquals(1, updateCount, "Expected update count of 1");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(0, metaData.getColumnCount(), "Expected result set without columns");

                assertFalse(rs.next(), "Expected no rows in result set");
            }
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_returnGeneratedKeys(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(testInsertQuery(withSchema), Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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
     * Test for {@link FBStatement#execute(String, int)} for an UPDATE statement including a WHERE with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     * <p>
     * Rationale: the (current) parser doesn't check the full UPDATE syntax, it just parses enough to find the table name.
     * </p>
     */
    @Test
    void testExecute_UPDATE_with_WHERE_returnGeneratedKeys() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsUpdateReturning(), "Test needs UPDATE .. RETURNING support");

        try (Statement stmt = con.createStatement()) {
            // Add row
            stmt.executeUpdate(TEST_INSERT_QUERY);

            boolean producedResultSet = stmt.execute(
                    "UPDATE TABLE_WITH_TRIGGER SET TEXT = '" + TEXT_VALUE + "_1' WHERE 1 = 1",
                    Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for UPDATE with generated keys returned");

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
                assertEquals(TEXT_VALUE + "_1", rs.getString(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecuteUpdate_INSERT_returnGeneratedKeys(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(testInsertQuery(withSchema), Statement.RETURN_GENERATED_KEYS);
            assertEquals(1, updateCount, "Expected update count");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

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
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT
     * which already has a RETURNING clause.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testExecute_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY + " RETURNING ID",
                    Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT
     * which already has a RETURNING * clause.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testExecute_INSERT_returnGeneratedKeys_withReturningAll() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsReturningAll(), "requires RETURNING * support");

        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet =
                    stmt.execute(TEST_INSERT_QUERY + " RETURNING *", Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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
     * Test for {@link FBStatement#executeUpdate(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an
     * INSERT which already has a RETURNING clause.
     * <p>
     * Expected: all columns of table returned, single row result set
     * </p>
     */
    @Test
    void testExecuteUpdate_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS);
            assertEquals(1, updateCount, "Expected update count");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

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
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT for
     * a non-existent table.
     * <p>
     * Expected: SQLException Table unknown
     * </p>
     */
    @Test
    void testExecute_INSERT_returnGeneratedKeys_nonExistentTable() throws Exception {
        try (Statement stmt = con.createStatement()){
            // Firebird 4+ uses RETURNING *, while earlier version produce a custom error as the columns can't be retrieved
            boolean usesReturningAll = getDefaultSupportInfo().supportsReturningAll();
            int errorCode = usesReturningAll
                    ? ISCConstants.isc_dsql_relation_err
                    : JaybirdErrorCodes.jb_generatedKeysNoColumnsFound;

            SQLException exception = assertThrows(SQLSyntaxErrorException.class,
                    () -> stmt.execute("INSERT INTO TABLE_NON_EXISTENT(TEXT) VALUES ('" + TEXT_VALUE + "')",
                            Statement.RETURN_GENERATED_KEYS));
            assertThat(exception, allOf(
                    errorCode(equalTo(errorCode)),
                    sqlState(equalTo("42S02")),
                    anyOf(
                            fbMessageContains(errorCode, "TABLE_NON_EXISTENT"),
                            fbMessageContains(errorCode, "\"TABLE_NON_EXISTENT\""))));
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int[])} with a single column index.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_columnIndexes(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(testInsertQuery(withSchema), new int[] { 1 });
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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
     * Test for {@link FBStatement#executeUpdate(String, int[])} with a single column index.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecuteUpdate_INSERT_columnIndexes(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(testInsertQuery(withSchema), new int[] { 1 });
            assertEquals(1, updateCount, "Expected update count");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

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
     * Test for {@link FBStatement#execute(String, int[])} with multiple indexes, one for a column which requires a
     * quoted name.
     * <p>
     * Expected: single row result set with only the specified columns.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_columnIndexes_quotedColumn(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(testInsertQuery(withSchema), new int[] { 1, 3 });
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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

    /**
     * Test for {@link FBStatement#executeUpdate(String, int[])} with multiple indexes, one for a column which requires
     * a quoted name.
     * <p>
     * Expected: single row result set with only the specified columns.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecuteUpdate_INSERT_columnIndexes_quotedColumn(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(testInsertQuery(withSchema), new int[] { 1, 3 });
            assertEquals(1, updateCount, "Expected update count");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

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

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            searchPath,            expectedSuffix
            <NIL>,                 _IN_PUBLIC
            'PUBLIC,OTHER_SCHEMA', _IN_PUBLIC
            'OTHER_SCHEMA,PUBLIC', _IN_OTHER_SCHEMA
            """)
    void testINSERT_schemalessTable_columnIndexes_schemaSearchPath(String searchPath, String expectedSuffix)
            throws Exception {
        assumeSchemaSupport();
        try (var stmt = con.createStatement()) {
            if (searchPath != null) {
                stmt.execute("set search_path to " + searchPath);
            }

            stmt.execute("insert into SAME_NAME default values", new int[] { 1, 2 });

            var rs = stmt.getGeneratedKeys();
            assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");
            var metaData = rs.getMetaData();
            assertEquals("ID" + expectedSuffix, metaData.getColumnLabel(1), "Unexpected name column 1");
            assertEquals("TEXT" + expectedSuffix, metaData.getColumnLabel(2), "Unexpected name column 2");
        }
    }

    @Test
    void testINSERT_schemalessTable_columnIndex_tableNotOnSearchPath() throws Exception {
        assumeSchemaSupport();
        try (var stmt = con.createStatement()) {
            stmt.execute("set search_path to SYSTEM");

            var exception = assertThrows(SQLNonTransientException.class,
                    () -> stmt.execute("insert into SAME_NAME default values", new int[] { 1, 2 }));
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysNoColumnsFound,
                    "\"SAME_NAME\"", "schemaless table not on the search path"));
        }
    }

    // The executeXXX(String, int[]) variants are the only ones that have special handling for schemaless tables
    // We consider testing through execute sufficient to cover the other methods as well

    // Other combination for execute(String, int[]) already covered in TestGeneratedKeysQuery

    /**
     * Test for {@link FBStatement#execute(String, String[])} with a single column name.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_columnNames(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(testInsertQuery(withSchema), new String[] { "ID" });
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for INSERT with generated keys returned");

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
     * Test for {@link FBStatement#executeUpdate(String, String[])} with a single column name.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecuteUpdate_INSERT_columnNames(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            int updateCount = stmt.executeUpdate(testInsertQuery(withSchema), new String[] { "ID" });
            assertEquals(1, updateCount, "Expected update count");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

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
     * Test for {@link FBStatement#execute(String, String[])} with an array of columns containing a non-existent column
     * name.
     * <p>
     * Expected: SQLException for Column unknown.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("withOrWithoutSchema")
    void testExecute_INSERT_columnNames_nonExistentColumn(boolean withSchema) throws Exception {
        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class,
                    () -> stmt.execute(testInsertQuery(withSchema), new String[] { "ID", "NON_EXISTENT" }));
            assertThat(exception, allOf(
                    errorCode(equalTo(ISCConstants.isc_dsql_field_err)),
                    sqlState(equalTo("42S22")),
                    anyOf(
                            message(containsString("Column unknown; NON_EXISTENT")),
                            message(containsString("Column unknown; \"NON_EXISTENT\"")))));
        }
    }

    @Test
    void testExecute_SELECT_RETURN_GENERATED_KEYS_handledNormally() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean isResultSet = stmt.execute("SELECT * FROM RDB$DATABASE", Statement.RETURN_GENERATED_KEYS);
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = stmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testExecute_SELECT_columnIndexes_handledNormally() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean isResultSet = stmt.execute("SELECT * FROM RDB$DATABASE", new int[] { 1, 2 });
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = stmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testExecute_SELECT_columnNames_handledNormally() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean isResultSet = stmt.execute("SELECT * FROM RDB$DATABASE", new String[] { "field1", "field2" });
            assertTrue(isResultSet, "Expected first result to be a result set");
            try (ResultSet rs = stmt.getResultSet()) {
                assertNotNull(rs, "Expected a result set");
                assertTrue(rs.next(), "Expected a row");
            }
        }
    }

    @Test
    void testExecute_UPDATE_OR_INSERT_returnGeneratedKeys() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet = stmt.execute(TEST_UPDATE_OR_INSERT, Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for UPDATE OR INSERT with generated keys returned");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(1, rs.getInt(1));
                assertEquals(TEXT_VALUE, rs.getString(2));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    @Test
    void testExecute_UPDATE_OR_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        try (Statement stmt = con.createStatement()) {
            boolean producedResultSet =
                    stmt.execute(TEST_UPDATE_OR_INSERT + " RETURNING id", Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for UPDATE OR INSERT with generated keys returned");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(1, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(1, metaData.getColumnCount(), "Expected result set with 1 column");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");

                assertTrue(rs.next(), "Expected first row in result set");
                assertEquals(1, rs.getInt(1));
                assertFalse(rs.next(), "Expected no second row");
            }
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int)} for an UPDATE statement affecting multiple rows with {@link Statement#RETURN_GENERATED_KEYS}.
     */
    @Test
    void testExecute_multiRow_UPDATE_returnGeneratedKeys() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMultiRowReturning(), "Requires multirow RETURNING support");

        try (Statement stmt = con.createStatement()) {
            // Add two rows
            stmt.executeUpdate(TEST_INSERT_QUERY);
            stmt.executeUpdate(TEST_INSERT_QUERY);

            boolean producedResultSet = stmt.execute(
                    "UPDATE TABLE_WITH_TRIGGER SET TEXT = '" + TEXT_VALUE + "_' || ID ORDER BY ID",
                    Statement.RETURN_GENERATED_KEYS);
            assertFalse(producedResultSet,
                    "Expected execute to report false (has no result set) for UPDATE with generated keys returned");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertNotNull(rs, "Expected a non-null result set from getGeneratedKeys");

                assertEquals(2, stmt.getUpdateCount(), "Update count should be directly available");
                assertFalse(rs.isClosed(), "Generated keys result set should be open");

                ResultSetMetaData metaData = rs.getMetaData();
                assertEquals(3, metaData.getColumnCount(), "Expected result set with 3 columns");
                assertEquals("ID", metaData.getColumnName(1), "Unexpected first column");
                assertEquals("TEXT", metaData.getColumnName(2), "Unexpected second column");

                assertTrue(rs.next(), "Expected first row in result set");
                int firstId = rs.getInt(1);
                assertEquals(513, firstId);
                assertEquals(TEXT_VALUE + "_" + firstId, rs.getString(2));
                assertTrue(rs.next(), "Expected second row in result set");
                int secondId = rs.getInt(1);
                assertEquals(514, secondId);
                assertEquals(TEXT_VALUE + "_" + secondId, rs.getString(2));
                assertFalse(rs.next(), "Expected no third row");
            }
        }
    }

    /**
     * Checks if {@link FBStatement#getGeneratedKeys()} in auto-commit mode can return a blob column without error.
     * <p>
     * Rationale: <a href="https://github.com/FirebirdSQL/jaybird/issues/844">#844</a>.
     * </p>
     */
    @Test
    void getGeneratedKeys_autoCommit_withBlob() throws Exception {
        con.setAutoCommit(false);
        try (var stmt = con.createStatement()) {
            stmt.execute(ADD_BLOB_COLUMN);
            con.setAutoCommit(true);
            try {
                stmt.execute("INSERT INTO TABLE_WITH_TRIGGER(BLOB_COLUMN)"
                        + " VALUES ('" + TEXT_VALUE + "') RETURNING ID, BLOB_COLUMN", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet generatedKeys = assertDoesNotThrow(stmt::getGeneratedKeys)) {
                    assertNextRow(generatedKeys);
                    assertEquals(TEXT_VALUE, generatedKeys.getString("BLOB_COLUMN"));
                }
            } finally {
                stmt.execute(DROP_BLOB_COLUMN);
            }
        }
    }

}
