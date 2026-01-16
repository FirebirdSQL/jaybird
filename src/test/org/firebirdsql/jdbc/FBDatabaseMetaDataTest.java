/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.RegexMatcher.matchesRegex;
import static org.firebirdsql.gds.ISCConstants.isc_dsql_drop_schema_failed;
import static org.firebirdsql.gds.ISCConstants.isc_dsql_drop_trigger_failed;
import static org.firebirdsql.gds.ISCConstants.isc_dsql_table_not_found;
import static org.firebirdsql.gds.ISCConstants.isc_dsql_view_not_found;
import static org.firebirdsql.gds.ISCConstants.isc_no_meta_update;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test for the {@link FBDatabaseMetaData} implementation of {@link java.sql.DatabaseMetaData}
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection connection;
    private static final boolean supportsComment = getDefaultSupportInfo().supportsComment();
    private static DatabaseMetaData dmd;

    @BeforeAll
    static void setupConnection() throws SQLException {
        connection = getConnectionViaDriverManager();
        dmd = connection.getMetaData();
    }

    @AfterAll
    static void tearDownConnection() throws SQLException {
        try {
            connection.close();
        } finally {
            connection = null;
            dmd = null;
        }
    }

    @Test
    void testGetTablesTypesNull() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", null)) {
            assertTrue(rs.next(), "Expected one row in result");
            String name = rs.getString(3);
            assertEquals("T1", name, "Didn't get back the name expected");
            assertFalse(rs.next(), "Got more than one table name back!");
        }
    }

    @Test
    void testGetTableTypes() throws Exception {
        List<String> retrieved = new ArrayList<>();
        ResultSet rs = dmd.getTableTypes();
        while (rs.next()) {
            retrieved.add(rs.getString(1));
        }

        assertEquals(getExpectedTableTypes(), retrieved, "Unexpected result for getTableTypes");
    }

    private static List<String> getExpectedTableTypes() {
        return getDefaultSupportInfo().supportsGlobalTemporaryTables()
                ? Arrays.asList(GLOBAL_TEMPORARY, SYSTEM_TABLE, TABLE, VIEW)
                : Arrays.asList(SYSTEM_TABLE, TABLE, VIEW);
    }

    @Test
    void testGetTableTypeNames() throws Exception {
        List<String> retrieved = Arrays.asList(dmd.unwrap(FirebirdDatabaseMetaData.class).getTableTypeNames());

        assertEquals(getExpectedTableTypes(), retrieved, "Unexpected result for getTableTypeNames");
    }

    @Test
    void testGetTablesTypesSystem() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "SYSTEM TABLE" })) {
            assertFalse(rs.next(), "Expected no tables to be returned (T1 is not a system table)");
        }
    }

    @Test
    void testGetTablesTypesTable() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "TABLE" })) {
            assertTrue(rs.next(), "Expected one result in result set");
            String name = rs.getString(3);
            assertEquals("T1", name, "Didn't get back the name expected");
            assertFalse(rs.next(), "Expected only one row in result set");
        }
    }

    @Test
    void testGetTablesTypesView() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "VIEW" })) {
            assertFalse(rs.next(), "Expected no result set (table T1 is not a view)");
        }
    }

    @Test
    void testGetSystemTablesTypesSystem() throws Exception {
        try (ResultSet rs = dmd.getTables(null, null, "RDB$RELATIONS", new String[] { "SYSTEM TABLE" })) {
            assertTrue(rs.next(), "Expected one result in result set");
            String name = rs.getString(3);
            assertEquals("RDB$RELATIONS", name, "Didn't get back the name expected");
            assertFalse(rs.next(), "Expected only one row in result set");
        }
    }

    @Test
    void testGetAllSystemTablesTypesSystem() throws Exception {
        try (ResultSet rs = dmd.getTables(null, null, "%", new String[] { "SYSTEM TABLE" })) {
            int count = 0;
            while (rs.next()) {
                assertNotNull(rs.getString(3), "expected table name");
                count++;
            }

            int sysTableCount = getDefaultSupportInfo().getSystemTableCount();
            assertNotEquals(-1, sysTableCount,
                    format("Unsupported database server version %d.%d for this test case: found table count %d",
                            dmd.getDatabaseMajorVersion(), dmd.getDatabaseMinorVersion(), count));

            assertEquals(sysTableCount, count, "# of system tables is not expected count");
        }
    }

    @Test
    void testEscapeWildcards() {
        // NOTE: fully tested in MetadataPatternTest#testEscapeWildcards
        assertEquals("test\\_me", FBDatabaseMetaData.escapeWildcards("test_me"), "escape wildcard incorrect");
    }

    @Test
    void testGetTablesWildcardQuote() throws Exception {
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        try (ResultSet rs = dmd.getTables(null, null, "TEST%M_", new String[] { "TABLE" })) {
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                assertThat("wrong name found", name, anyOf(equalTo("TEST_ME"), equalTo("TEST__ME")));
                count++;
            }
            assertEquals(2, count, "more than two tables found");
        }

        try (ResultSet rs = dmd.getTables(null, null, "TEST\\_ME", new String[] { "TABLE" })) {
            assertTrue(rs.next(), "Expected one row in result set");
            String name = rs.getString(3);
            assertEquals("TEST_ME", name, "wrong name found");
            assertFalse(rs.next(), "Only one row expected in results et");
        }

        try (ResultSet rs = dmd.getTables(null, null, "test\\_ me", new String[] { "TABLE" })) {
            assertTrue(rs.next(), "Expected on row in result set");
            String name = rs.getString(3);
            assertEquals("test_ me", name, "wrong name found");
            assertFalse(rs.next(), "Expected only one row in result set");
        }

        try (ResultSet rs = dmd.getTables(null, null, "test\\_ me%", new String[] { "TABLE" })) {
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                assertThat("wrong name found", name, anyOf(equalTo("test_ me"), equalTo("test_ me too")));
                count++;
            }
            assertEquals(2, count, "more than one table found");
        }

        try (ResultSet rs = dmd.getTables(null, null, "RDB_RELATIONS", new String[] { "SYSTEM TABLE" })) {
            assertTrue(rs.next(), "Expected one row in resultset");
            String name = rs.getString(3);
            assertEquals("RDB$RELATIONS", name, "wrong name found");
            assertFalse(rs.next(), "Expected only one row in result set");
        }
    }

    @Test
    void testGetColumnsWildcardQuote() throws Exception {
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        try (ResultSet rs = dmd.getColumns(null, null, "test%m_", "my\\_ column2")) {
            assertTrue(rs.next(), "Expected one row in result set");
            assertNotNull(rs.getString(3), "expected table name");
            String column = rs.getString(4);
            assertEquals("my_ column2", column, "wrong column found");
            assertFalse(rs.next(), "Expected only one row in result set");
        }
    }

    // test case for JDBC-130, similar to the one above
    @Test
    void testGetColumnsWildcardQuote2() throws Exception {
        createTable("TABLE_A");
        createTable("TABLE_A_B");

        try (ResultSet rs = dmd.getColumns(null, null, "TABLE_A", "%")) {
            Set<String> tableNames = new HashSet<>();
            while (rs.next()) {
                tableNames.add(rs.getString(3));
            }
            assertEquals(1, tableNames.size(), "should find one table");
        }
    }

    @Test
    void testGetColumnsIdentityInformation() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsIdentityColumns());
        DdlHelper.executeCreateTable(connection,
                "create table table_with_identity (id integer generated by default as identity primary key, charcol CHAR(10))");

        try (ResultSet rs = dmd.getColumns(null, null, "TABLE_WITH_IDENTITY", "%")) {
            assertTrue(rs.next(), "Expected a row");
            assertEquals("ID", rs.getString("COLUMN_NAME"));
            assertEquals("YES", rs.getString("IS_AUTOINCREMENT"));
            assertEquals("YES", rs.getString("IS_GENERATEDCOLUMN"));
            assertEquals("YES", rs.getString("JB_IS_IDENTITY"));
            assertEquals("BY DEFAULT", rs.getString("JB_IDENTITY_TYPE"));
            assertTrue(rs.next(), "Expected a row");
            assertEquals("CHARCOL", rs.getString("COLUMN_NAME"));
            assertEquals("NO", rs.getString("IS_AUTOINCREMENT"));
            assertEquals("NO", rs.getString("IS_GENERATEDCOLUMN"));
            assertEquals("NO", rs.getString("JB_IS_IDENTITY"));
            assertNull(rs.getString("JB_IDENTITY_TYPE"));
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * should return a result.
     */
    @Test
    void testGetTablesLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        ResultSet rs = dmd.getTables(null, null, tableName, null);
        assertTrue(rs.next(), "Should return primary key information");
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * and a pattern consisting of the full name + the {@code %} symbol should return a result.
     */
    @Test
    void testGetTablesLongTableName_WithWildcard() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        ResultSet rs = dmd.getTables(null, null, tableName + "%", null);
        assertTrue(rs.next(), "Should return primary key information");
    }

    @Test
    void testGetProcedures() throws Exception {
        dropProcedures();
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        try (ResultSet rs = dmd.getProcedures(null, null, "%")) {
            boolean gotproc1 = false;
            boolean gotproc2 = false;
            while (rs.next()) {
                String name = rs.getString(3);
                String lit_name = rs.getString("PROCEDURE_NAME");
                assertEquals(name, lit_name,
                        "result set from getProcedures schema mismatch: field 3 should be PROCEDURE_NAME");
                String remarks = rs.getString(7);
                String lit_remarks = rs.getString("REMARKS");
                // TODO: Double check meaning and reason for this check
                if (remarks == null || lit_remarks == null) {
                    if (remarks != null || lit_remarks != null)
                        fail("result set from getProcedures schema mismatch only one of field 7 or 'REMARKS' returned null");
                } else {
                    assertEquals(remarks, lit_remarks,
                            "result set from getProcedures schema mismatch: field 7 should be REMARKS");
                }
                short type = rs.getShort(8);
                short lit_type = rs.getShort("PROCEDURE_TYPE");
                assertEquals(type, lit_type,
                        "result set from getProcedures schema mismatch: field 8 should be PROCEDURE_TYPE");

                if (name.equals("TESTPROC1")) {
                    assertFalse(gotproc1, "result set from getProcedures had duplicate entry for TESTPROC1");
                    gotproc1 = true;
                    assertEquals(DatabaseMetaData.procedureReturnsResult, type,
                            "result set from getProcedures had wrong procedure type for TESTPROC1 (should be procedureReturnsResult)");
                    if (supportsComment) {
                        assertNotNull(remarks, "result set from getProcedures did not return a value for REMARKS");
                        assertEquals("Test description", remarks,
                                "result set from getProcedures did not return correct REMARKS section");
                    } else {
                        assertNull(remarks, "result set from getProcedures should not return a value for REMARKS");
                    }
                } else if (name.equals("TESTPROC2")) {
                    assertFalse(gotproc2, "result set from getProcedures had duplicate entry for TESTPROC2");
                    gotproc2 = true;
                    assertEquals(DatabaseMetaData.procedureNoResult, type,
                            "result set from getProcedures had wrong procedure type for TESTPROC2 (should be procedureNoResult)");
                } else {
                    fail("result set from getProcedures returned unknown procedure " + name);
                }
            }
            assertTrue(gotproc1, "result set from getProcedures did not return procedure testproc1");
            assertTrue(gotproc2, "result set from getProcedures did not return procedure testproc2");
        }
    }

    @Test
    void testGetProcedures_excludesPackages() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        dropProcedures();
        dropPackages();
        createProcedure("procedure1", true);
        createPackage("package1", "pkgprocedure1");

        try (ResultSet rs = dmd.getProcedures(null, null, "%")) {
            assertTrue(rs.next(), "expected a row");
            assertEquals("PROCEDURE1", rs.getString("PROCEDURE_NAME"), "Unexpected procedure");
            assertFalse(rs.next(), "expected no more procedures");
        }
    }

    @Test
    void testGetProcedureColumns() throws Exception {
        dropProcedures();
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        try (ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%")) {
            int rownum = 0;
            while (rs.next()) {
                String procname = rs.getString(3);
                rownum++;
                String colname = rs.getString(4);
                short coltype = rs.getShort(5);
                short datatype = rs.getShort(6);
                String typename = rs.getString(7);
                short radix = rs.getShort(11);
                short nullable = rs.getShort(12);
                String remarks = rs.getString(13);

                // per JDBC 2.0 spec, there is a very specific order these
                // rows should come back, so if field names don't match
                // what I'm expecting, in the order I expect them, there
                // is a bug.
                switch (rownum) {
                case 4:
                    assertEquals("TESTPROC1", procname, "wrong pr name");
                    assertEquals("IN1", colname, "wrong f name");
                    assertEquals(DatabaseMetaData.procedureColumnIn, coltype, "wrong c type");
                    assertEquals(Types.INTEGER, datatype, "wrong d type");
                    assertEquals("INTEGER", typename, "wrong t name");
                    assertEquals(10, radix, "wrong radix");
                    assertEquals(DatabaseMetaData.procedureNullable, nullable, "wrong nullable");
                    assertNull(remarks, "wrong comment");
                    break;
                case 5:
                    assertEquals("TESTPROC1", procname, "wrong pr name");
                    assertEquals("IN2", colname, "wrong f name");
                    break;
                case 1:
                    assertEquals("TESTPROC1", procname, "wrong pr name");
                    assertEquals("OUT1", colname, "wrong f name");
                    assertEquals(DatabaseMetaData.procedureColumnOut, coltype, "wrong c type");
                    break;
                case 2:
                    assertEquals("TESTPROC1", procname, "wrong pr name");
                    assertEquals("OUT2", colname, "wrong f name");
                    break;
                case 3:
                    assertEquals("TESTPROC1", procname, "wrong pr name");
                    assertEquals("OUT3", colname, "wrong f name");
                    break;
                case 6:
                    assertEquals("TESTPROC2", procname, "wrong pr name");
                    assertEquals("INP", colname, "wrong f name");
                    break;
                default:
                    fail("unexpected field returned from getProcedureColumns");
                }
            }
        }
    }

    @Test
    void testGetProcedureColumns_excludesPackages() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        dropProcedures();
        dropPackages();
        createProcedure("procedure1", false);
        createPackage("package1", "pkgprocedure1");

        try (ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%")) {
            assertTrue(rs.next(), "expected a row");
            assertEquals("PROCEDURE1", rs.getString("PROCEDURE_NAME"), "Unexpected procedure");
            assertEquals("INP", rs.getString("COLUMN_NAME"), "Unexpected column name");
            assertFalse(rs.next(), "expected no more procedures");
        }
    }

    @Test
    void testGetTypeInfo() throws Exception {
        try (ResultSet rs = dmd.getTypeInfo()) {
            assertNotNull(rs, "No result set returned from getTypeInfo");
            int count = 0;
            StringBuilder out = new StringBuilder();
            while (rs.next()) {
                count++;
                for (int i = 1; i <= 18; i++) {
                    out.append(rs.getObject(i));
                }
                out.append(getProperty("line.separator"));
            }
            System.getLogger(getClass().getName()).log(System.Logger.Level.TRACE, "getTypeInfo returned: {0}", out);
            assertThat("Not enough TypeInfo rows fetched", count, greaterThanOrEqualTo(15));
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for DECIMAL and NUMERIC).
     */
    @Test
    void databaseMetaData_decimalAndNumericPrecision() throws Exception {
        // intentionally not using FirebirdSupportInfo.maxDecimalPrecision() as tested implementation uses that method
        final int expectedPrecision = getDefaultSupportInfo().isVersionEqualOrAbove(4, 0) ? 38 : 18;
        try (ResultSet rs = dmd.getTypeInfo()) {
            boolean foundNumeric = false;
            boolean foundDecimal = false;
            while (rs.next()) {
                String typeName = rs.getString("TYPE_NAME");
                final int expectedTypeCode;
                if ("NUMERIC".equals(typeName)) {
                    foundNumeric = true;
                    expectedTypeCode = Types.NUMERIC;
                } else if ("DECIMAL".equals(typeName)) {
                    foundDecimal = true;
                    expectedTypeCode = Types.DECIMAL;
                } else {
                    continue;
                }
                assertEquals(expectedTypeCode, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                assertEquals(expectedPrecision, rs.getInt("PRECISION"), "Unexpected PRECISION");
                assertEquals(0, rs.getInt("MINIMUM_SCALE"), "Unexpected MINIMUM_SCALE");
                assertEquals(expectedPrecision, rs.getInt("MAXIMUM_SCALE"), "Unexpected MAXIMUM_SCALE");
                assertEquals(DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                assertFalse(rs.getBoolean("CASE_SENSITIVE"), "Unexpected CASE_SENSITIVE");
                assertEquals(DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"), "Unexpected SEARCHABLE");
                assertFalse(rs.getBoolean("UNSIGNED_ATTRIBUTE"), "Unexpected UNSIGNED_ATTRIBUTE");
                assertTrue(rs.getBoolean("FIXED_PREC_SCALE"), "Unexpected FIXED_PREC_SCALE");
                assertFalse(rs.getBoolean("AUTO_INCREMENT"), "Unexpected AUTO_INCREMENT");
                assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                // Not testing other values
            }
            assertTrue(foundNumeric, "Expected to find numeric type in typeInfo");
            assertTrue(foundDecimal, "Expected to find decimal type in typeInfo");
        }
    }

    @Test
    void testDefaultValue() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_default (test_col INTEGER DEFAULT 0 NOT NULL)");

            try (ResultSet rs = dmd.getColumns(null, "%", "TEST_DEFAULT", null)) {
                assertTrue(rs.next(), "Should return at least one row");

                String defaultValue = rs.getString("COLUMN_DEF");
                assertEquals("0", defaultValue, "Default value should be correct");
            }
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    @Test
    void testGetPrimaryKeysLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableName)) {
            assertTrue(rs.next(), "Should return primary key information");
        }
    }

    /**
     * Using a very short table name for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    @Test
    void testGetPrimaryKeysShortTableName() throws Exception {
        String tableName = "A";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableName)) {
            assertTrue(rs.next(), "Should return primary key information");
        }
    }

    /**
     * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)} should not accept a LIKE pattern.
     */
    @Test
    void testGetPrimaryKeys_LikePattern_NoResult() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        String tableNamePattern = "PLANILLAS_PREVISION_%";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableNamePattern)) {
            assertFalse(rs.next(), "Should not return primary key information");
        }

    }

    private void createTable(String tableName, String constraint) throws Exception {
        String sql = "CREATE TABLE " + tableName + " ( "
                + "C1 INTEGER not null, "
                + "C2 SMALLINT, "
                + "C3 DECIMAL(18,0), "
                + "C4 FLOAT, "
                + "C5 DOUBLE PRECISION, "
                + "C6 INTEGER, "
                + "\"my column1\" CHAR(10), "
                + "\"my_ column2\" VARCHAR(20)"
                + (constraint != null ? ", " + constraint + ")" : ")");
        DdlHelper.executeCreateTable(connection, sql);
    }

    private void createTable(String tableName) throws Exception {
        createTable(tableName, "PRIMARY KEY (c1)");
    }

    private void createProcedure(String procedureName, boolean returnsData) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            try {
                stmt.execute("drop procedure " + procedureName);
            } catch (SQLException e) {
                // ignore
            }
            if (returnsData) {

                stmt.execute("CREATE PROCEDURE " + procedureName
                        + "(IN1 INTEGER, IN2 FLOAT)"
                        + "RETURNS (OUT1 VARCHAR(20), "
                        + "OUT2 DOUBLE PRECISION, OUT3 INTEGER) AS "
                        + "DECLARE VARIABLE X INTEGER;"
                        + "BEGIN"
                        + " OUT1 = 'Out String';"
                        + " OUT2 = 45;"
                        + " OUT3 = IN1;"
                        + "END");

                if (supportsComment) {
                    stmt.execute("COMMENT ON PROCEDURE " + procedureName + " IS 'Test description'");
                }
            } else {
                stmt.execute("CREATE PROCEDURE " + procedureName
                        + " (INP INTEGER) AS BEGIN exit; END");
            }
        }
    }

    @Test
    void testGetVersionColumns() throws Exception {
        ResultSet rs = dmd.getVersionColumns(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse(rs.next(), "Expected no results for getVersionColumns");
    }

    @Test
    void testGetImportedKeys_NoForeignKeys() throws Exception {
        createTable("table1");

        ResultSet rs = dmd.getImportedKeys(null, null, "TABLE1");

        assertFalse(rs.next(), "Expected no imported keys for table without foreign key");
    }

    @Test
    void testGetImportedKeys_WithForeignKey() throws Exception {
        createTable("table1");
        createTable("table2", "FOREIGN KEY (c6) REFERENCES table1 (c1)");

        // TODO Extend to verify columns as defined in JDBC
        ResultSet rs = dmd.getImportedKeys(null, null, "TABLE2");

        assertTrue(rs.next(), "Expected at least one row");

        assertEquals("TABLE1", rs.getString("PKTABLE_NAME"), "Unexpected PKTABLE_NAME");
        assertEquals("C1", rs.getString("PKCOLUMN_NAME"), "Unexpected PKCOLUMN_NAME");
        assertEquals("TABLE2", rs.getString("FKTABLE_NAME"), "Unexpected FKTABLE_NAME");
        assertEquals("C6", rs.getString("FKCOLUMN_NAME"), "Unexpected FKCOLUMN_NAME");
        assertEquals(1, rs.getInt("KEY_SEQ"), "Unexpected KEY_SEQ");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"), "Unexpected UPDATE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"), "Unexpected DELETE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"), "Unexpected DEFERRABILITY");

        assertFalse(rs.next(), "Expected no more than one row");
    }

    @Test
    void testGetExportedKeys_NoForeignKeys() throws Exception {
        createTable("tablenofk1");

        ResultSet rs = dmd.getExportedKeys(null, null, "TABLENOFK1");

        assertFalse(rs.next(), "Expected no exported keys for table without foreign key references");
    }

    @Test
    void testGetExportedKeys_WithForeignKey() throws Exception {
        createTable("table1");
        createTable("table2", "FOREIGN KEY (c6) REFERENCES table1 (c1)");

        ResultSet rs = dmd.getExportedKeys(null, null, "TABLE1");

        // TODO Extend to verify columns as defined in JDBC
        assertTrue(rs.next(), "Expected at least one row");

        assertEquals("TABLE1", rs.getString("PKTABLE_NAME"), "Unexpected PKTABLE_NAME");
        assertEquals("C1", rs.getString("PKCOLUMN_NAME"), "Unexpected PKCOLUMN_NAME");
        assertEquals("TABLE2", rs.getString("FKTABLE_NAME"), "Unexpected FKTABLE_NAME");
        assertEquals("C6", rs.getString("FKCOLUMN_NAME"), "Unexpected FKCOLUMN_NAME");
        assertEquals(1, rs.getInt("KEY_SEQ"), "Unexpected KEY_SEQ");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"), "Unexpected UPDATE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"), "Unexpected DELETE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"), "Unexpected DEFERRABILITY");

        assertFalse(rs.next(), "Expected no more than one row");
    }

    @Test
    void testGetCrossReference_NoForeignKeys() throws Exception {
        createTable("tablenofk1");
        createTable("tablenofk2");

        ResultSet rs = dmd.getCrossReference(null, null, "TABLENOFK1", null, null, "TABLENOFK2");

        assertFalse(rs.next(), "Expected no cross reference for tables without foreign key references");
    }

    @Test
    void testGetCrossReference_WithForeignKey() throws Exception {
        createTable("tablewithfk1");
        createTable("tablewithfk2", "FOREIGN KEY (c6) REFERENCES tablewithfk1 (c1)");

        ResultSet rs = dmd.getCrossReference(null, null, "TABLEWITHFK1", null, null, "TABLEWITHFK2");

        // TODO Extend to verify columns as defined in JDBC
        assertTrue(rs.next(), "Expected at least one row");

        assertEquals("TABLEWITHFK1", rs.getString("PKTABLE_NAME"), "Unexpected PKTABLE_NAME");
        assertEquals("C1", rs.getString("PKCOLUMN_NAME"), "Unexpected PKCOLUMN_NAME");
        assertEquals("TABLEWITHFK2", rs.getString("FKTABLE_NAME"), "Unexpected FKTABLE_NAME");
        assertEquals("C6", rs.getString("FKCOLUMN_NAME"), "Unexpected FKCOLUMN_NAME");
        assertEquals(1, rs.getInt("KEY_SEQ"), "Unexpected KEY_SEQ");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"), "Unexpected UPDATE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"), "Unexpected DELETE_RULE");
        assertEquals(DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"), "Unexpected DEFERRABILITY");

        assertFalse(rs.next(), "Expected no more than one row");
    }

    @Test
    void testGetSuperTypes() throws Exception {
        ResultSet rs = dmd.getSuperTypes(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse(rs.next(), "Expected no results for getSuperTypes");
    }

    @Test
    void testGetSuperTables() throws Exception {
        ResultSet rs = dmd.getSuperTables(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse(rs.next(), "Expected no results for getSuperTables");
    }

    /**
     * Tests if the driver version information is consistent (and doesn't throw exceptions).
     */
    @Test
    void testDriverVersionInformation() throws Exception {
        assumeThat("Running with unfiltered org/firebirdsql/jaybird/version.properties; test ignored",
                dmd.getDriverVersion(), not(equalTo("@VERSION@")));
        String expectedVersionPattern =
                format("%d\\.%d\\.\\d+", dmd.getDriverMajorVersion(), dmd.getDriverMinorVersion());
        assertThat(dmd.getDriverVersion(), matchesRegex(expectedVersionPattern));
    }

    @Test
    void testSupportsGetGeneratedKeys() throws Exception {
        assertEquals(supportInfoFor(connection).supportsInsertReturning(), dmd.supportsGetGeneratedKeys());
    }

    @Test
    void testGetJDBCMajorVersion() throws Exception {
        assertEquals(4, dmd.getJDBCMajorVersion(), "JDBCMajorVersion");
    }

    @Test
    void testGetJDBCMinorVersion() throws Exception {
        final int expectedMinor = Runtime.version().compareTo(Runtime.Version.parse("24")) >= 0 ? 4 : 3;
        assertEquals(expectedMinor, dmd.getJDBCMinorVersion(), "JDBCMinorVersion");
    }

    /**
     * @see FBDatabaseMetaDataDialect1Test#testGetIdentifierQuoteString_dialect1Db(String, String)
     */
    @ParameterizedTest
    @CsvSource({
            "1, ' '",
            "2, \"",
            "3, \""
    })
    void testGetIdentifierQuoteString_dialect3Db(String connectionDialect, String expectedIdentifierQuote)
            throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", connectionDialect);
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            DatabaseMetaData md = connection.getMetaData();
            assertEquals(expectedIdentifierQuote, md.getIdentifierQuoteString());
        }
    }

    @Test
    void testGetSchemaTerm() throws Exception {
        final String expected = getDefaultSupportInfo().supportsSchemas() ? "SCHEMA" : null;
        assertEquals(expected, dmd.getSchemaTerm(), "schemaTerm");
    }

    @Test
    void testGetMaxSchemaNameLength() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        final int expected = getDefaultSupportInfo().supportsSchemas()
                ? supportInfo.maxIdentifierLengthCharacters() : 0;
        assertEquals(expected, dmd.getMaxSchemaNameLength(), "maxSchemaNameLength");
    }

    @Test
    void testSupportsSchemasInXXX() {
        final boolean expected = getDefaultSupportInfo().supportsSchemas();
        assertAll(
                () -> assertEquals(expected, dmd.supportsSchemasInDataManipulation(), "DataManipulation"),
                () -> assertEquals(expected, dmd.supportsSchemasInIndexDefinitions(), "IndexDefinitions"),
                () -> assertEquals(expected, dmd.supportsSchemasInPrivilegeDefinitions(), "PrivilegeDefinitions"),
                () -> assertEquals(expected, dmd.supportsSchemasInProcedureCalls(), "ProcedureCalls"),
                () -> assertEquals(expected, dmd.supportsSchemasInTableDefinitions(), "TableDefinitions")
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    void testGetProcedureSourceCode_String() throws SQLException {
        var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
        assertNull(dbmd.getProcedureSourceCode("TEST_PROC_1"), "Expected no procedure source");

        final String procedureBody = """
                begin
                  /* TEST_PROC_1 body */
                end""";
        try (var stmt = connection.createStatement()) {
            stmt.execute("create procedure TEST_PROC_1 (VARIN integer) as " + procedureBody);
        }

        assertEquals(procedureBody, dbmd.getProcedureSourceCode("TEST_PROC_1").trim(), "Procedure source");
    }

    @Test
    void testGetTriggerSourceCode_String() throws SQLException {
        var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
        assertNull(dbmd.getTriggerSourceCode("TEST_TRIG_1"), "Expected no trigger source");

        final String triggerBody = """
                as
                begin
                  /* TEST_TRIG_1 body */
                end""";

        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                stmt.execute("create table TEST_1 (id integer)");
                stmt.execute("create trigger TEST_TRIG_1 before insert on TEST_1 " + triggerBody);
                connection.commit();

                assertEquals(triggerBody, dbmd.getTriggerSourceCode("TEST_TRIG_1"), "Trigger source");
            } finally {
                DdlHelper.executeDDL(stmt, List.of("drop table TEST_1"), isc_no_meta_update, isc_dsql_table_not_found,
                        isc_dsql_view_not_found, isc_dsql_drop_trigger_failed);
            }
        } finally {
            connection.setAutoCommit(false);
        }
    }

    @Test
    void testGetTriggerSourceCode_String_String() throws SQLException {
        assumeSchemaSupport();
        var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
        assertNull(dbmd.getTriggerSourceCode("TEST_SCHEMA_1", "TEST_TRIG_1"), "Expected no trigger source");

        final String triggerBody = """
                as
                begin
                  /* TEST_SCHEMA_1.TEST_TRIG_1 body */
                end""";

        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                stmt.execute("create schema TEST_SCHEMA_1");
                stmt.execute("create table TEST_SCHEMA_1.TEST_1 (id integer)");
                stmt.execute("create trigger TEST_TRIG_1 before insert on TEST_SCHEMA_1.TEST_1 " + triggerBody);
                connection.commit();

                assertEquals(triggerBody, dbmd.getTriggerSourceCode("TEST_SCHEMA_1", "TEST_TRIG_1"), "Trigger source");
                assertEquals(triggerBody, dbmd.getTriggerSourceCode("TEST_TRIG_1"), "Trigger source");
                assertNull(dbmd.getTriggerSourceCode("PUBLIC", "TEST_TRIG_1"), "Expected no trigger source");
            } finally {
                DdlHelper.executeDDL(stmt, List.of("drop table TEST_SCHEMA_1.TEST_1", "drop schema TEST_SCHEMA_1"),
                        isc_no_meta_update, isc_dsql_table_not_found, isc_dsql_view_not_found,
                        isc_dsql_drop_trigger_failed, isc_dsql_drop_schema_failed);
            }
        } finally {
            connection.setAutoCommit(true);
        }


    }

    @Test
    void testGetViewSourceCode_String() throws SQLException {
        var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
        assertNull(dbmd.getViewSourceCode("TEST_VIEW_1"), "Expected no view source");

        final String viewBody = """
                select 'TEST_VIEW_1' as VIEW_NAME, 1 as SOME_COLUMN
                from RDB$DATABASE""";
        try (var stmt = connection.createStatement()) {
            try {
                stmt.execute("create view TEST_VIEW_1 as " + viewBody);

                assertEquals(viewBody, dbmd.getViewSourceCode("TEST_VIEW_1").trim(), "View source");
            } finally {
                DdlHelper.executeDDL(stmt, List.of("drop view TEST_VIEW_1"), isc_no_meta_update,
                        isc_dsql_table_not_found, isc_dsql_view_not_found, isc_dsql_drop_trigger_failed);
            }
        }
    }

    @Test
    void testGetViewSourceCode_String_String() throws SQLException {
        assumeSchemaSupport();
        var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
        assertNull(dbmd.getViewSourceCode("TEST_SCHEMA_1", "TEST_VIEW_1"), "Expected no view source");

        final String viewBody = """
                select 'TEST_SCHEMA_1' as VIEW_SCHEMA, 'TEST_VIEW_1' as VIEW_NAME, 1 as SOME_COLUMN
                from RDB$DATABASE""";
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                stmt.execute("create schema TEST_SCHEMA_1");
                stmt.execute("create view TEST_SCHEMA_1.TEST_VIEW_1 as " + viewBody);
                connection.commit();

                assertEquals(viewBody, dbmd.getViewSourceCode("TEST_SCHEMA_1", "TEST_VIEW_1"), "View source");
                assertEquals(viewBody, dbmd.getViewSourceCode("TEST_VIEW_1"), "View source");
                assertNull(dbmd.getViewSourceCode("PUBLIC", "TEST_VIEW_1"), "Expected no view source");
            } finally {
                DdlHelper.executeDDL(stmt, List.of("drop view TEST_SCHEMA_1.TEST_VIEW_1", "drop schema TEST_SCHEMA_1"),
                        isc_no_meta_update, isc_dsql_table_not_found, isc_dsql_view_not_found,
                        isc_dsql_drop_trigger_failed, isc_dsql_drop_schema_failed);
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void createPackage(String packageName, String procedureName) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("create package " + packageName + " as "
                    + "begin "
                    + "  procedure " + procedureName + " (param1 int) returns (return1 int);"
                    + "end");
            stmt.execute("create package body " + packageName + " as "
                    + "begin "
                    + "  procedure " + procedureName + " (param1 int) returns (return1 int) "
                    + "  as "
                    + "  begin"
                    + "  end "
                    + "end");
        }
    }

    private void dropProcedures() throws Exception {
        connection.setAutoCommit(false);
        String procedureSelectQuery = "select RDB$PROCEDURE_NAME from RDB$PROCEDURES where coalesce(RDB$SYSTEM_FLAG, 0) <> 1";
        if (getDefaultSupportInfo().supportsPackages()) {
            procedureSelectQuery += " and RDB$PACKAGE_NAME is null";
        }
        try (Statement select = connection.createStatement();
             Statement drop = connection.createStatement();
             ResultSet procedures = select.executeQuery(procedureSelectQuery)) {
            while (procedures.next()) {
                drop.execute("drop procedure " + procedures.getString(1));
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void dropPackages() throws Exception {
        connection.setAutoCommit(false);
        String packageSelectQuery = "select RDB$PACKAGE_NAME from RDB$PACKAGES where coalesce(RDB$SYSTEM_FLAG, 0) <> 1";
        try (Statement select = connection.createStatement();
             Statement drop = connection.createStatement();
             ResultSet packages = select.executeQuery(packageSelectQuery)) {
            while (packages.next()) {
                drop.execute("drop package " + packages.getString(1));
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
