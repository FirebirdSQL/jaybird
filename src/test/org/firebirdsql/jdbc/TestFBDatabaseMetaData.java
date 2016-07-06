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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getProperty;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.*;

/**
 * Test for the {@link FBDatabaseMetaData} implementation of {@link java.sql.DatabaseMetaData}
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class TestFBDatabaseMetaData extends FBJUnit4TestBase {

    private Connection connection;
    private boolean supportsComment;
    private DatabaseMetaData dmd;

    @Before
    public void setupConnection() throws SQLException {
        connection = getConnectionViaDriverManager();
        supportsComment = supportInfoFor(connection).supportsComment();
        dmd = connection.getMetaData();
    }

    @After
    public void tearDownConnection() throws SQLException {
        connection.close();
        connection = null;
        dmd = null;
    }

    @Test
    public void testGetTablesTypesNull() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", null)) {
            assertTrue("Expected one row in result", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("Didn't get back the name expected", "T1", name);
            assertFalse("Got more than one table name back!", rs.next());
        }
    }

    @Test
    public void testGetTableTypes() throws Exception {
        final Set<String> expected = new HashSet<>(Arrays.asList(FBDatabaseMetaData.ALL_TYPES));
        final Set<String> retrieved = new HashSet<>();

        ResultSet rs = dmd.getTableTypes();
        while (rs.next()) {
            retrieved.add(rs.getString(1));
        }

        assertEquals("Unexpected result for getTableTypes", expected, retrieved);
    }

    @Test
    public void testGetTablesTypesSystem() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "SYSTEM TABLE" })) {
            assertFalse("Expected no tables to be returned (T1 is not a system table)", rs.next());
        }
    }

    @Test
    public void testGetTablesTypesTable() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "TABLE" })) {
            assertTrue("Expected one result in result set", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("Didn't get back the name expected", "T1", name);
            assertFalse("Expected only one row in result set", rs.next());
        }
    }

    @Test
    public void testGetTablesTypesView() throws Exception {
        createTable("T1");

        try (ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "VIEW" })) {
            assertFalse("Expected no result set (table T1 is not a view)", rs.next());
        }
    }

    @Test
    public void testGetSystemTablesTypesSystem() throws Exception {
        try (ResultSet rs = dmd.getTables(null, null, "RDB$RELATIONS", new String[] { "SYSTEM TABLE" })) {
            assertTrue("Expected one result in result set", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("Didn't get back the name expected", "RDB$RELATIONS", name);
            assertFalse("Expected only one row in result set", rs.next());
        }
    }

    @Test
    public void testGetAllSystemTablesTypesSystem() throws Exception {
        try (ResultSet rs = dmd.getTables(null, null, "%", new String[] { "SYSTEM TABLE" })) {
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                if (log != null) log.info("table name: " + name);
                count++;
            }

            int sysTableCount;
            final int databaseMajorVersion = dmd.getDatabaseMajorVersion();
            final int databaseMinorVersion = dmd.getDatabaseMinorVersion();
            if (databaseMajorVersion < 2)
                sysTableCount = 32;
            else if (databaseMajorVersion == 2 && databaseMinorVersion == 0)
                sysTableCount = 33;
            else if (databaseMajorVersion == 2 && databaseMinorVersion == 1)
                sysTableCount = 40;
            else if (databaseMajorVersion == 2 && databaseMinorVersion == 5)
                sysTableCount = 42;
            else if (databaseMajorVersion == 3 && databaseMinorVersion == 0) {
                sysTableCount = 50;
            } else {
                fail(String.format("Unsupported database server version %d.%d for this test case: found table count %d", databaseMajorVersion, databaseMinorVersion, count));

                // needed to make compiler happy - it does not know that fail() throws an exception
                return;
            }

            assertEquals("# of system tables is not expected count", sysTableCount, count);
        }
    }

    @Test
    public void testHasNoWildcards() {
        assertTrue("claims test\\_me has wildcards", FBDatabaseMetaData.hasNoWildcards("test\\_me"));
        assertTrue("claims test\\%me has wildcards", FBDatabaseMetaData.hasNoWildcards("test\\%me"));
        assertFalse("claims test\\\\%me has no wildcards", FBDatabaseMetaData.hasNoWildcards("test\\\\%me"));
        assertFalse("claims test_me has no wildcards", FBDatabaseMetaData.hasNoWildcards("test_me"));
        assertFalse("claims test%me has no wildcards", FBDatabaseMetaData.hasNoWildcards("test%me"));
    }

    @Test
    public void testStripEscape() {
        assertEquals("strip escape wrong", "test_me", FBDatabaseMetaData.stripEscape("test\\_me"));
        assertEquals("strip escape wrong", "test\\me", FBDatabaseMetaData.stripEscape("test\\\\me"));
        assertEquals("strip escape wrong", "test\\_me", FBDatabaseMetaData.stripEscape("test\\\\\\_me"));
    }

    @Test
    public void testEscapeWildcards() {
        assertEquals("escape wildcard incorrect", "test\\\\me", FBDatabaseMetaData.escapeWildcards("test\\me"));
        assertEquals("escape wildcard incorrect", "test\\%me", FBDatabaseMetaData.escapeWildcards("test%me"));
        assertEquals("escape wildcard incorrect", "test\\_me", FBDatabaseMetaData.escapeWildcards("test_me"));
    }

    @Test
    public void testGetTablesWildcardQuote() throws Exception {
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        try (ResultSet rs = dmd.getTables(null, null, "TEST%M_", new String[] { "TABLE" })) {
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                if (log != null) log.info("table name: " + name);
                assertTrue("wrong name found: " + name, "TEST_ME".equals(name) || "TEST__ME".equals(name));
                count++;
            }
            assertEquals("more than two tables found", 2, count);
        }

        try (ResultSet rs = dmd.getTables(null, null, "TEST\\_ME", new String[] { "TABLE" })) {
            assertTrue("Expected one row in result set", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("wrong name found", "TEST_ME", name);
            assertFalse("Only one row expected in results et", rs.next());
        }

        try (ResultSet rs = dmd.getTables(null, null, "test\\_ me", new String[] { "TABLE" })) {
            assertTrue("Expected on row in result set", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("wrong name found", "test_ me", name);
            assertFalse("Expected only one row in result set", rs.next());
        }

        try (ResultSet rs = dmd.getTables(null, null, "test\\_ me%", new String[] { "TABLE" })) {
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                if (log != null) log.info("table name: " + name);
                assertTrue("wrong name found: " + name, "test_ me".equals(name) || "test_ me too".equals(name));
                count++;
            }
            assertEquals("more than one table found", 2, count);
        }

        try (ResultSet rs = dmd.getTables(null, null, "RDB_RELATIONS", new String[] { "SYSTEM TABLE" })) {
            assertTrue("Expected one row in resultset", rs.next());
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertEquals("wrong name found", "RDB$RELATIONS", name);
            assertFalse("Expected only one row in resultset", rs.next());
        }
    }

    @Test
    public void testGetColumnsWildcardQuote() throws Exception {
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        try (ResultSet rs = dmd.getColumns(null, null, "test%m_", "my\\_ column2")) {
            assertTrue("Expected one row in resultset", rs.next());
            String name = rs.getString(3);
            String column = rs.getString(4);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong column found: " + column, "my_ column2".equals(column));
            assertFalse("Expected only one row in resultset", rs.next());
        }
    }

    // test case for JDBC-130, similar to the one above
    @Test
    public void testGetColumnsWildcardQuote2() throws Exception {
        createTable("TABLE_A");
        createTable("TABLE_A_B");

        try (ResultSet rs = dmd.getColumns(null, null, "TABLE_A", "%")) {
            Set<String> tableNames = new HashSet<>();
            while (rs.next()) {
                tableNames.add(rs.getString(3));
            }
            assertEquals("should find one table", 1, tableNames.size());
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * should return a result.
     */
    @Test
    public void testGetTablesLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        ResultSet rs = dmd.getTables(null, null, tableName, null);
        assertTrue("Should return primary key information", rs.next());
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * and a pattern consisting of the full name + the <code>%</code> symbol should return a result.
     */
    @Test
    public void testGetTablesLongTableName_WithWildcard() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        ResultSet rs = dmd.getTables(null, null, tableName + "%", null);
        assertTrue("Should return primary key information", rs.next());
    }

    @Test
    public void testGetProcedures() throws Exception {
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        try (ResultSet rs = dmd.getProcedures(null, null, "%")) {
            boolean gotproc1 = false;
            boolean gotproc2 = false;
            while (rs.next()) {
                String name = rs.getString(3);
                String lit_name = rs.getString("PROCEDURE_NAME");
                assertEquals("result set from getProcedures schema mismatch: field 3 should be PROCEDURE_NAME",
                        name, lit_name);
                String remarks = rs.getString(7);
                String lit_remarks = rs.getString("REMARKS");
                // TODO: Double check meaning and reason for this check
                if (remarks == null || lit_remarks == null) {
                    if (remarks != null || lit_remarks != null)
                        fail("result set from getProcedures schema mismatch only one of field 7 or 'REMARKS' returned null");
                    else
                        assertTrue("all OK on the western front", true);
                } else {
                    assertEquals("result set from getProcedures schema mismatch: field 7 should be REMARKS",
                            remarks, lit_remarks);
                }
                short type = rs.getShort(8);
                short lit_type = rs.getShort("PROCEDURE_TYPE");
                assertEquals("result set from getProcedures schema mismatch: field 8 should be PROCEDURE_TYPE",
                        type, lit_type);
                if (log != null) log.info(" got procedure " + name);
                if (name.equals("TESTPROC1")) {
                    assertFalse("result set from getProcedures had duplicate entry for TESTPROC1", gotproc1);
                    gotproc1 = true;
                    assertEquals("result set from getProcedures had wrong procedure type for TESTPROC1 "
                            + "(should be procedureReturnsResult)", DatabaseMetaData.procedureReturnsResult, type);
                    if (supportsComment) {
                        assertNotNull("result set from getProcedures did not return a value for REMARKS.", remarks);
                        assertEquals("result set from getProcedures did not return correct REMARKS section.",
                                "Test description", remarks);
                    } else {
                        assertNull("result set from getProcedures should not return a value for REMARKS.", remarks);
                    }
                } else if (name.equals("TESTPROC2")) {
                    assertFalse("result set from getProcedures had duplicate entry for TESTPROC2", gotproc2);
                    gotproc2 = true;
                    assertEquals("result set from getProcedures had wrong procedure type for TESTPROC2 "
                            + "(should be procedureNoResult)", DatabaseMetaData.procedureNoResult, type);
                } else {
                    fail("result set from getProcedures returned unknown procedure " + name);
                }
            }
            assertTrue("result set from getProcedures did not return procedure testproc1", gotproc1);
            assertTrue("result set from getProcedures did not return procedure testproc2", gotproc2);
        }
    }

    @Test
    public void testGetProcedureColumns() throws Exception {
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        try (ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%")) {
            int rownum = 0;
            while (rs.next()) {
                rownum++;
                String procname = rs.getString(3);
                String colname = rs.getString(4);
                short coltype = rs.getShort(5);
                short datatype = rs.getShort(6);
                String typename = rs.getString(7);
                short radix = rs.getShort(11);
                short nullable = rs.getShort(12);
                String remarks = rs.getString(13);
                if (log != null) log.info("row " + rownum + "proc " + procname + " field " + colname);

                // per JDBC 2.0 spec, there is a very specific order these
                // rows should come back, so if field names don't match
                // what I'm expecting, in the order I expect them, there
                // is a bug.
                switch (rownum) {
                case 4:
                    assertEquals("wrong pr name.", "TESTPROC1", procname);
                    assertEquals("wrong f name.", "IN1", colname);
                    assertEquals("wrong c type.", DatabaseMetaData.procedureColumnIn, coltype);
                    assertEquals("wrong d type.", Types.INTEGER, datatype);
                    assertEquals("wrong t name.", "INTEGER", typename);
                    assertEquals("wrong radix.", 10, radix);
                    assertEquals("wrong nullable.", DatabaseMetaData.procedureNullable, nullable);
                    assertNull("wrong comment.", remarks);
                    break;
                case 5:
                    assertEquals("wrong pr name", "TESTPROC1", procname);
                    assertEquals("wrong f name", "IN2", colname);
                    break;
                case 1:
                    assertEquals("wrong pr name", "TESTPROC1", procname);
                    assertEquals("wrong f name", "OUT1", colname);
                    assertEquals("wrong c type", DatabaseMetaData.procedureColumnOut, coltype);
                    break;
                case 2:
                    assertEquals("wrong pr name", "TESTPROC1", procname);
                    assertEquals("wrong f name", "OUT2", colname);
                    break;
                case 3:
                    assertEquals("wrong pr name", "TESTPROC1", procname);
                    assertEquals("wrong f name", "OUT3", colname);
                    break;
                case 6:
                    assertEquals("wrong pr name", "TESTPROC2", procname);
                    assertEquals("wrong f name", "INP", colname);
                    break;
                default:
                    fail("unexpected field returned from getProcedureColumns.");
                }
            }
        }
    }

    @Test
    public void testGetColumnPrivileges() throws Exception {
        try (ResultSet rs = dmd.getColumnPrivileges(null, null, "RDB$RELATIONS", "%")) {
            assertNotNull("No result set returned from getColumnPrivileges", rs);
            // TODO Actual test?
        }
    }

    @Test
    public void testGetTablePrivileges() throws Exception {
        try (ResultSet rs = dmd.getTablePrivileges(null, null, "%")) {
            assertNotNull("No result set returned from getTablePrivileges", rs);
            // TODO Actual test?
        }
    }

    @Test
    public void testGetTypeInfo() throws Exception {
        try (ResultSet rs = dmd.getTypeInfo()) {
            assertNotNull("No result set returned from getTypeInfo", rs);
            int count = 0;
            StringBuilder out = new StringBuilder();
            while (rs.next()) {
                count++;
                for (int i = 1; i <= 18; i++) {
                    Object o = rs.getObject(i);
                    if (o == null) {
                        o = "null";
                    }
                    out.append(o);
                }
                out.append(getProperty("line.separator"));
            }
            if (log != null) log.info("getTypeInfo returned: " + out);
            assertTrue("Not enough TypeInfo rows fetched: " + count, count >= 15);
        }
    }

    @Test
    public void testDefaultValue() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_default ("
                    + "test_col INTEGER DEFAULT 0 NOT NULL)");

            try (ResultSet rs = dmd.getColumns(null, "%", "TEST_DEFAULT", null)) {
                assertTrue("Should return at least one row", rs.next());

                String defaultValue = rs.getString("COLUMN_DEF");
                assertEquals("Default value should be correct.", "0", defaultValue);
            }
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    @Test
    public void testGetPrimaryKeysLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableName)) {
            assertTrue("Should return primary key information", rs.next());
        }
    }

    /**
     * Using a very short table name for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    @Test
    public void testGetPrimaryKeysShortTableName() throws Exception {
        String tableName = "A";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableName)) {
            assertTrue("Should return primary key information", rs.next());
        }
    }

    /**
     * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)} should not accept a LIKE pattern.
     */
    @Test
    public void testGetPrimaryKeys_LikePattern_NoResult() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        String tableNamePattern = "PLANILLAS_PREVISION_%";
        createTable(tableName);

        try (ResultSet rs = dmd.getPrimaryKeys(null, null, tableNamePattern)) {
            assertFalse("Should not return primary key information", rs.next());
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

    // TODO Test does not actually check results
    @Test
    public void testCatalogsAndSchema() throws Exception {
        try (ResultSet rs = dmd.getSchemas()) {
            while (rs.next()) {
                String sn = rs.getString(1);
                System.out.println(".getAllTables() schema=" + sn);
            }
        }

        try (ResultSet rs = dmd.getCatalogs()) {
            while (rs.next()) {
                String sn = rs.getString(1);
                System.out.println(".getAllTables() catalogs=" + sn);
            }
        }

        try (ResultSet rs = dmd.getTables(null, null, "%", new String[] { "TABLE" })) {
            System.out.println(".getAllTables() rs=" + rs);

            while (rs.next()) {
                String tn = rs.getString("TABLE_NAME");
                String tt = rs.getString("TABLE_TYPE");
                String remarks = rs.getString("REMARKS");

                System.out.println(".getAllTables() found table" + tn + ", type=" + tt + ", remarks=" + remarks);
            }
        }

    }

    @Test
    public void testGetBestRowIdentifier() throws Exception {
        createTable("best_row_pk");
        createTable("best_row_no_pk", null);

        try (ResultSet rs = dmd.getBestRowIdentifier("", "", "BEST_ROW_PK", DatabaseMetaData.bestRowSession, true)) {
            assertTrue("Should have rows", rs.next());
            assertEquals("Column name should be C1", "C1", rs.getString(2));
            assertEquals("Column type should be INTEGER", "INTEGER", rs.getString(4));
            assertFalse("Should have only one row", rs.next());
        }

        try (ResultSet rs = dmd.getBestRowIdentifier("", "", "BEST_ROW_NO_PK", DatabaseMetaData.bestRowSession, true)) {
            assertTrue("Should have rows", rs.next());
            assertEquals("Column name should be RDB$DB_KEY", "RDB$DB_KEY", rs.getString(2));
            assertFalse("Should have only one row", rs.next());
        }
    }

    @Test
    public void testGetVersionColumns() throws Exception {
        ResultSet rs = dmd.getVersionColumns(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getVersionColumns", rs.next());
    }

    @Test
    public void testGetImportedKeys_NoForeignKeys() throws Exception {
        createTable("table1");

        ResultSet rs = dmd.getImportedKeys(null, null, "TABLE1");

        assertFalse("Expected no imported keys for table without foreign key", rs.next());
    }

    @Test
    public void testGetImportedKeys_WithForeignKey() throws Exception {
        createTable("table1");
        createTable("table2", "FOREIGN KEY (c6) REFERENCES table1 (c1)");

        // TODO Extend to verify columns as defined in JDBC
        ResultSet rs = dmd.getImportedKeys(null, null, "TABLE2");

        assertTrue("Expected at least one row", rs.next());

        assertEquals("Unexpected PKTABLE_NAME", "TABLE1", rs.getString("PKTABLE_NAME"));
        assertEquals("Unexpected PKCOLUMN_NAME", "C1", rs.getString("PKCOLUMN_NAME"));
        assertEquals("Unexpected FKTABLE_NAME", "TABLE2", rs.getString("FKTABLE_NAME"));
        assertEquals("Unexpected FKCOLUMN_NAME", "C6", rs.getString("FKCOLUMN_NAME"));
        assertEquals("Unexpected KEY_SEQ", 1, rs.getInt("KEY_SEQ"));
        assertEquals("Unexpected UPDATE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"));
        assertEquals("Unexpected DELETE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"));
        assertEquals("Unexpected DEFERRABILITY", DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"));

        assertFalse("Expected no more than one row", rs.next());
    }

    @Test
    public void testGetExportedKeys_NoForeignKeys() throws Exception {
        createTable("table1");

        ResultSet rs = dmd.getExportedKeys(null, null, "TABLE1");

        assertFalse("Expected no exported keys for table without foreign key references", rs.next());
    }

    @Test
    public void testGetExportedKeys_WithForeignKey() throws Exception {
        createTable("table1");
        createTable("table2", "FOREIGN KEY (c6) REFERENCES table1 (c1)");

        ResultSet rs = dmd.getExportedKeys(null, null, "TABLE1");

        // TODO Extend to verify columns as defined in JDBC
        assertTrue("Expected at least one row", rs.next());

        assertEquals("Unexpected PKTABLE_NAME", "TABLE1", rs.getString("PKTABLE_NAME"));
        assertEquals("Unexpected PKCOLUMN_NAME", "C1", rs.getString("PKCOLUMN_NAME"));
        assertEquals("Unexpected FKTABLE_NAME", "TABLE2", rs.getString("FKTABLE_NAME"));
        assertEquals("Unexpected FKCOLUMN_NAME", "C6", rs.getString("FKCOLUMN_NAME"));
        assertEquals("Unexpected KEY_SEQ", 1, rs.getInt("KEY_SEQ"));
        assertEquals("Unexpected UPDATE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"));
        assertEquals("Unexpected DELETE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"));
        assertEquals("Unexpected DEFERRABILITY", DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"));

        assertFalse("Expected no more than one row", rs.next());
    }

    @Test
    public void testGetCrossReference_NoForeignKeys() throws Exception {
        createTable("table1");
        createTable("table2");

        ResultSet rs = dmd.getCrossReference(null, null, "TABLE1", null, null, "TABLE2");

        assertFalse("Expected no cross reference for tables without foreign key references", rs.next());
    }

    @Test
    public void testGetCrossReference_WithForeignKey() throws Exception {
        createTable("table1");
        createTable("table2", "FOREIGN KEY (c6) REFERENCES table1 (c1)");

        ResultSet rs = dmd.getCrossReference(null, null, "TABLE1", null, null, "TABLE2");

        // TODO Extend to verify columns as defined in JDBC
        assertTrue("Expected at least one row", rs.next());

        assertEquals("Unexpected PKTABLE_NAME", "TABLE1", rs.getString("PKTABLE_NAME"));
        assertEquals("Unexpected PKCOLUMN_NAME", "C1", rs.getString("PKCOLUMN_NAME"));
        assertEquals("Unexpected FKTABLE_NAME", "TABLE2", rs.getString("FKTABLE_NAME"));
        assertEquals("Unexpected FKCOLUMN_NAME", "C6", rs.getString("FKCOLUMN_NAME"));
        assertEquals("Unexpected KEY_SEQ", 1, rs.getInt("KEY_SEQ"));
        assertEquals("Unexpected UPDATE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("UPDATE_RULE"));
        assertEquals("Unexpected DELETE_RULE", DatabaseMetaData.importedKeyNoAction, rs.getShort("DELETE_RULE"));
        assertEquals("Unexpected DEFERRABILITY", DatabaseMetaData.importedKeyNotDeferrable, rs.getShort("DEFERRABILITY"));

        assertFalse("Expected no more than one row", rs.next());
    }

    @Test
    public void testGetSuperTypes() throws Exception {
        ResultSet rs = dmd.getSuperTypes(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getSuperTypes", rs.next());
    }

    @Test
    public void testGetSuperTables() throws Exception {
        ResultSet rs = dmd.getSuperTables(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getSuperTables", rs.next());
    }

    @Test
    public void testGetClientInfoProperties() throws Exception {
        ResultSet rs = dmd.getClientInfoProperties();

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getClientInfoProperties", rs.next());
    }

    /**
     * Tests if the driver version information is consistent (and doesn't throw exceptions).
     */
    @Test
    public void testDriverVersionInformation() throws Exception {
        String expectedVersion = String.format("%d.%d", dmd.getDriverMajorVersion(), dmd.getDriverMinorVersion());
        assertEquals(expectedVersion, dmd.getDriverVersion());
    }

    @Test
    public void testSupportsGetGeneratedKeys() throws Exception {
        // Note: we are not testing behavior for absence of antlr-runtime
        assertEquals(supportInfoFor(connection).supportsInsertReturning(), dmd.supportsGetGeneratedKeys());
    }
}
