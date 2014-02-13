/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jca;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.LocalTransaction;
import javax.sql.DataSource;

import org.firebirdsql.jdbc.*;

/**
 * Describe class <code>TestFBDatabaseMetaData</code> here.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @version 1.0
 */
public class TestFBDatabaseMetaData extends TestXABase {

    private AbstractConnection c;
    private Statement s;
    private DatabaseMetaData dmd;
    private LocalTransaction t;

    public TestFBDatabaseMetaData(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        c = (AbstractConnection) ds.getConnection();
        s = c.createStatement();
        t = c.getLocalTransaction();
        dmd = c.getMetaData();
    }

    public void tearDown() throws Exception {
        s.close();
        if (c.inTransaction()) {
            t.commit();
        }
        c.close();
        super.tearDown();
    }

    public void testGetTablesNull() throws Exception {
        if (log != null) log.info("testGetTablesNull");
        createTable("T1");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "T1", null);
        assertTrue("Expected one row in result", rs.next());
        String name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("Didn't get back the name expected", "T1", name);
        assertFalse("Got more than one table name back!", rs.next());
        rs.close();
        t.commit();

        dropTable("T1");
    }

    public void testGetTableTypes() throws Exception {
        final Set<String> expected = new HashSet<String>(Arrays.asList(AbstractDatabaseMetaData.ALL_TYPES));
        final Set<String> retrieved = new HashSet<String>();

        ResultSet rs = dmd.getTableTypes();
        while (rs.next()) {
            retrieved.add(rs.getString(1));
        }

        assertEquals("Unexpected result for getTableTypes", expected, retrieved);
    }

    public void testGetTablesSystem() throws Exception {
        if (log != null) log.info("testGetTablesSystem");
        createTable("T1");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "SYSTEM TABLE" });
        assertFalse("Expected no tables to be returned (T1 is not a system table)", rs.next());
        rs.close();
        t.commit();

        dropTable("T1");
    }

    public void testGetTablesTable() throws Exception {
        if (log != null) log.info("testGetTablesTable");
        createTable("T1");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "TABLE" });
        assertTrue("Expected one result in resultset", rs.next());
        String name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("Didn't get back the name expected", "T1", name);
        assertFalse("Expected only one row in resultset", rs.next());
        rs.close();
        t.commit();

        dropTable("T1");
    }

    public void testGetTablesView() throws Exception {
        if (log != null) log.info("testGetTablesView");
        createTable("T1");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "T1", new String[] { "VIEW" });
        assertFalse("Expected no resultset (table T1 is not a view)", rs.next());
        rs.close();
        t.commit();

        dropTable("T1");
    }

    public void testGetSystemTablesSystem() throws Exception {
        if (log != null) log.info("testGetSystemTablesSystem");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "RDB$RELATIONS", new String[] { "SYSTEM TABLE" });
        assertTrue("Expected one result in resultset", rs.next());
        String name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("Didn't get back the name expected", "RDB$RELATIONS", name);
        assertFalse("Expected only one row in resultset", rs.next());
        rs.close();

        t.commit();
    }

    public void testGetAllSystemTablesSystem() throws Exception {
        if (log != null) log.info("testGetSystemTablesSystem");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "%", new String[] { "SYSTEM TABLE" });
        int count = 0;
        while (rs.next()) {
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
        }

        int sysTableCount;
        FirebirdDatabaseMetaData metaData = (FirebirdDatabaseMetaData) c.getMetaData();
        final int databaseMajorVersion = metaData.getDatabaseMajorVersion();
        final int databaseMinorVersion = metaData.getDatabaseMinorVersion();
        if (databaseMajorVersion < 2)
            sysTableCount = 32;
        else if (databaseMajorVersion == 2 && databaseMinorVersion == 0)
            sysTableCount = 33;
        else if (databaseMajorVersion == 2 && databaseMinorVersion == 1)
            sysTableCount = 40;
        else if (databaseMajorVersion == 2 && databaseMinorVersion == 5)
            sysTableCount = 42;
        else if (databaseMajorVersion == 3 && databaseMinorVersion == 0) {
            sysTableCount = 44;
        } else {
            fail(String.format("Unsupported database server version %d.%d for this test case: found table count %d", databaseMajorVersion, databaseMinorVersion, count));

            // needed to make compiler happy - it does not know that fail() throws an exception
            return;
        }

        assertEquals("# of system tables is not expected count", sysTableCount, count);
        rs.close();
        t.commit();
    }

    public void testAAStringFunctions() {
        if (log != null) log.info("testAAStringFunctions");
        FBDatabaseMetaData d = (FBDatabaseMetaData) dmd;

        assertTrue("claims test\\_me has wildcards", d.hasNoWildcards("test\\_me"));
        assertEquals("strip escape wrong", "test_me", d.stripEscape("test\\_me"));

        String str = d.stripQuotes("test_me", true);
        assertEquals("strip quotes wrong", "TEST_ME", str);

        assertEquals("strip quotes wrong", "test_me", d.stripQuotes("\"test_me\"", false));
    }

    public void testGetTablesWildcardQuote() throws Exception {
        if (log != null) log.info("testGetTablesWildcardQuote");
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        t.begin();
        ResultSet rs = dmd.getTables(null, null, "test%m_", new String[] { "TABLE" });
        int count = 0;
        while (rs.next()) {
            String name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "TEST_ME".equals(name) || "TEST__ME".equals(name));
            count++;
        }
        assertEquals("more than two tables found", 2, count);
        rs.close();

        rs = dmd.getTables(null, null, "test\\_me", new String[] { "TABLE" });
        assertTrue("Expected one row in resultset", rs.next());
        String name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("wrong name found", "TEST_ME", name);
        assertFalse("Only one row expected in resultset", rs.next());
        rs.close();

        rs = dmd.getTables(null, null, "\"test\\_ me\"", new String[] { "TABLE" });
        assertTrue("Expected on row in resultset", rs.next());
        name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("wrong name found", "test_ me", name);
        assertFalse("Expected only one row in resultset", rs.next());
        rs.close();

        rs = dmd.getTables(null, null, "\"test\\_ me%\"", new String[] { "TABLE" });
        count = 0;
        while (rs.next()) {
            name = rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "test_ me".equals(name) || "test_ me too".equals(name));
            count++;
        }
        assertEquals("more than one table found", 2, count);
        rs.close();

        rs = dmd.getTables(null, null, "RDB_RELATIONS", new String[] { "SYSTEM TABLE" });
        assertTrue("Expected one row in resultset", rs.next());
        name = rs.getString(3);
        if (log != null) log.info("table name: " + name);
        assertEquals("wrong name found", "RDB$RELATIONS", name);
        assertFalse("Expected only one row in resultset", rs.next());
        rs.close();
        t.commit();

        dropTable("test_me");
        dropTable("test__me");
        dropTable("\"test_ me\"");
        dropTable("\"test_ me too\"");
        dropTable("\"test_me too\"");
    }

    public void testGetColumnsWildcardQuote() throws Exception {
        if (log != null) log.info("testGetColumnsWildcardQuote");
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        t.begin();
        ResultSet rs = dmd.getColumns(null, null, "test%m_", "\"my\\_ column2\"");
        assertTrue("Expected one row in resultset", rs.next());
        String name = rs.getString(3);
        String column = rs.getString(4);
        if (log != null) log.info("table name: " + name);
        // assertTrue("wrong name found: " + name, "TEST_ME".equals(name) || "TEST__ME".equals(name));
        assertTrue("wrong column found: " + column, "my_ column2".equals(column));
        // TODO: Is this correct result? Shouldn't there be two columns (of two tables) returned?
        assertFalse("Expected only one row in resultset", rs.next());
        rs.close();
        t.commit();

        dropTable("test_me");
        dropTable("test__me");
        dropTable("\"test_ me\"");
        dropTable("\"test_ me too\"");
        dropTable("\"test_me too\"");
    }

    // test case for JDBC-130, similar to the one above
    public void testGetColumnsWildcardQuote2() throws Exception {
        if (log != null) log.info("testGetColumnsWildcardQuote2");
        createTable("TABLE_A");
        createTable("TABLE_A_B");

        t.begin();
        ResultSet rs = dmd.getColumns(null, null, "TABLE_A", "%");
        Set tableNames = new HashSet();
        while (rs.next()) {
            tableNames.add(rs.getString(3));
        }
        assertEquals("should find one table", 1, tableNames.size());
        rs.close();
        t.commit();

        dropTable("TABLE_A");
        dropTable("TABLE_A_B");
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * should return a result.
     */
    public void testGetTablesLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);
        try {
            t.begin();
            ResultSet rs = dmd.getTables(null, null, tableName, null);
            assertTrue("Should return primary key information", rs.next());
            t.commit();
        } finally {
            dropTable(tableName);
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * and a pattern consisting of the full name + the <code>%</code> symbol should return a result.
     */
    public void testGetTablesLongTableName_WithWildcard() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);
        try {
            t.begin();
            ResultSet rs = dmd.getTables(null, null, tableName + "%", null);
            assertTrue("Should return primary key information", rs.next());
            t.commit();
        } finally {
            dropTable(tableName);
        }
    }

    public void testGetProcedures() throws Exception {
        if (log != null) log.info("testGetProcedures");
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        t.begin();
        ResultSet rs = dmd.getProcedures(null, null, "%");
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
                assertNotNull("result set from getProcedures did not return a value for REMARKS.", remarks);
                assertEquals("result set from getProcedures did not return correct REMARKS section.",
                        "Test description", remarks);
            } else if (name.equals("TESTPROC2")) {
                assertFalse("result set from getProcedures had duplicate entry for TESTPROC2", gotproc2);
                gotproc2 = true;
                assertEquals("result set from getProcedures had wrong procedure type for TESTPROC2 "
                        + "(should be procedureNoResult)", DatabaseMetaData.procedureNoResult, type);
            } else
                fail("result set from getProcedures returned unknown procedure " + name);
        }
        assertTrue("result set from getProcedures did not return procedure testproc1", gotproc1);
        assertTrue("result set from getProcedures did not return procedure testproc2", gotproc2);
        rs.close();
        t.commit();

        dropProcedure("testproc1");
        dropProcedure("testproc2");
    }

    public void testGetProcedureColumns() throws Exception {
        if (log != null) log.info("testGetProcedureColumns");
        createProcedure("testproc1", true);
        createProcedure("testproc2", false);

        t.begin();
        ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%");
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

        t.commit();
        dropProcedure("testproc1");
        dropProcedure("testproc2");
    }

    public void testGetColumnPrivileges() throws Exception {
        if (log != null) log.info("testGetColumnPrivileges");

        t.begin();
        ResultSet rs = dmd.getColumnPrivileges(null, null, "RDB$RELATIONS", "%");
        assertNotNull("No resultset returned from getColumnPrivileges", rs);
        // TODO Actual test?
        t.commit();
    }

    public void testGetTablePrivileges() throws Exception {
        if (log != null) log.info("testGetTablePrivileges");

        t.begin();
        ResultSet rs = dmd.getTablePrivileges(null, null, "%");
        assertNotNull("No resultset returned from getTablePrivileges", rs);
        // TODO Actual test?
        t.commit();
    }

    public void testGetTypeInfo() throws Exception {
        if (log != null) log.info("testGetTypeInfo");

        ResultSet rs = dmd.getTypeInfo();
        assertNotNull("No resultset returned from getTypeInfo", rs);
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
        if (log != null) log.info("getTypeInfoblePrivileges returned: " + out);
        assertTrue("Not enough TypeInfo rows fetched: " + count, count >= 15);
    }

    public void testDefaultValue() throws Exception {
        t.begin();
        try {
            s.execute("CREATE TABLE test_default ("
                    + "test_col INTEGER DEFAULT 0 NOT NULL)");

            ResultSet rs = dmd.getColumns(null, "%", "test_default", null);
            assertTrue("Should return at least one row", rs.next());

            String defaultValue = rs.getString("COLUMN_DEF");
            assertEquals("Default value should be correct.", "0", defaultValue);
        } finally {
            // s.execute("DROP TABLE test_default");
            t.commit();
        }
    }

    /**
     * Using a table name of 31 characters for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    public void testGetPrimaryKeysLongTableName() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        createTable(tableName);
        try {
            t.begin();
            ResultSet rs = dmd.getPrimaryKeys(null, null, tableName);
            assertTrue("Should return primary key information", rs.next());
            t.commit();
        } finally {
            dropTable(tableName);
        }
    }

    /**
     * Using a very short table name for {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     * should return a result.
     */
    public void testGetPrimaryKeysShortTableName() throws Exception {
        String tableName = "A";
        createTable(tableName);
        try {
            t.begin();
            ResultSet rs = dmd.getPrimaryKeys(null, null, tableName);
            assertTrue("Should return primary key information", rs.next());
            t.commit();
        } finally {
            dropTable(tableName);
        }
    }

    /**
     * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)} should not accept a LIKE pattern.
     */
    public void testGetPrimaryKeys_LikePattern_NoResult() throws Exception {
        String tableName = "PLANILLAS_PREVISION_MANTENIMIEN";
        String tableNamePattern = "PLANILLAS_PREVISION_%";
        createTable(tableName);
        try {
            t.begin();
            ResultSet rs = dmd.getPrimaryKeys(null, null, tableNamePattern);
            assertFalse("Should return primary key information", rs.next());
            t.commit();
        } finally {
            dropTable(tableName);
        }
    }

    private void createTable(String tableName, String constraint) throws Exception {
        dropTable(tableName);
        t.begin();
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

        s.execute(sql);
        t.commit();
    }

    private void createTable(String tableName) throws Exception {
        createTable(tableName, "PRIMARY KEY (c1)");
    }

    private void createProcedure(String procedureName, boolean returnsData) throws Exception {
        dropProcedure(procedureName);
        t.begin();
        try {
            if (returnsData) {
                s.execute("CREATE PROCEDURE " + procedureName
                		+ "(IN1 INTEGER, IN2 FLOAT)"
                        + "RETURNS (OUT1 VARCHAR(20), "
                		+ "OUT2 DOUBLE PRECISION, OUT3 INTEGER) AS "
                        + "DECLARE VARIABLE X INTEGER;"
                		+ "BEGIN"
                        + " OUT1 = 'Out String';"
                		+ " OUT2 = 45;"
                        + " OUT3 = IN1;" 
                		+ "END");

                int updateCount = s.executeUpdate("UPDATE RDB$PROCEDURES "
                        + "SET RDB$DESCRIPTION='Test description' "
                        + "WHERE RDB$PROCEDURE_NAME='"
                        + procedureName.toUpperCase() + "'");

                assertEquals("Could not set procedure description", 1, updateCount);
            } else
                s.execute("CREATE PROCEDURE " + procedureName
                		+ " (INP INTEGER) AS BEGIN exit; END");
        } catch (Exception e) {
            if (log != null) log.warn("error creating procedure: " + e.getMessage());
            throw e;
        }
        t.commit();
    }

    private void dropTable(String tableName) throws Exception {
        t.begin();
        try {
            s.execute("drop table " + tableName);
        } catch (Exception e) {
        }
        t.commit();
    }

    private void dropProcedure(String procedureName) throws Exception {
        t.begin();
        try {
            s.execute("DROP PROCEDURE " + procedureName);
        } catch (Exception e) {
        }
        t.commit();
    }


    // TODO Test does not actually check results
    public void testCatalogsAndSchema() throws Exception {
        DatabaseMetaData dmd = c.getMetaData();

        t.begin();
        ResultSet rs = dmd.getSchemas();
        while (rs.next()) {
            String sn = rs.getString(1);
            System.out.println(".getAllTables() schema=" + sn);
        }
        rs.close();

        rs = dmd.getCatalogs();
        while (rs.next()) {
            String sn = rs.getString(1);
            System.out.println(".getAllTables() catalogs=" + sn);
        }
        rs.close();

        rs = dmd.getTables(null, null, "%", new String[] { "TABLE" });
        System.out.println(".getAllTables() rs=" + rs);

        while (rs.next()) {
            String tn = rs.getString("TABLE_NAME");
            String tt = rs.getString("TABLE_TYPE");
            String remarks = rs.getString("REMARKS");

            System.out.println(".getAllTables() found table" + tn + ", type=" + tt + ", remarks=" + remarks);
        }
        t.commit();
    }

    public void testGetBestRowIdentifier() throws Exception {
        try {
            createTable("best_row_pk");
            createTable("best_row_no_pk", null);

            DatabaseMetaData dmd = c.getMetaData();

            t.begin();
            ResultSet rs = dmd.getBestRowIdentifier("", "", "BEST_ROW_PK", DatabaseMetaData.bestRowSession, true);

            assertTrue("Should have rows", rs.next());
            assertEquals("Column name should be C1", "C1", rs.getString(2));
            assertEquals("Column type should be INTEGER", "INTEGER", rs.getString(4));
            assertFalse("Should have only one row", rs.next());

            rs.close();

            rs = dmd.getBestRowIdentifier("", "", "BEST_ROW_NO_PK", DatabaseMetaData.bestRowSession, true);

            assertTrue("Should have rows", rs.next());
            assertEquals("Column name should be RDB$DB_KEY", "RDB$DB_KEY", rs.getString(2));
            assertFalse("Should have only one row", rs.next());

            rs.close();
            t.commit();
        } catch (Exception e) {
            t.rollback();
            throw e;
        } finally {
            dropTable("best_row_pk");
            dropTable("best_row_no_pk");
        }
    }

    public void testGetVersionColumns() throws Exception {
        ResultSet rs = dmd.getVersionColumns(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getVersionColumns", rs.next());
    }

    public void testGetImportedKeys_NoForeignKeys() throws Exception {
        createTable("table1");

        ResultSet rs = dmd.getImportedKeys(null, null, "TABLE1");

        assertFalse("Expected no imported keys for table without foreign key", rs.next());
    }

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

    public void testGetExportedKeys_NoForeignKeys() throws Exception {
        createTable("table1");

        ResultSet rs = dmd.getExportedKeys(null, null, "TABLE1");

        assertFalse("Expected no exported keys for table without foreign key references", rs.next());
    }

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

    public void testGetCrossReference_NoForeignKeys() throws Exception {
        createTable("table1");
        createTable("table2");

        ResultSet rs = dmd.getCrossReference(null, null, "TABLE1", null, null, "TABLE2");

        assertFalse("Expected no cross reference for tables without foreign key references", rs.next());
    }

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

    public void testGetSuperTypes() throws Exception {
        ResultSet rs = dmd.getSuperTypes(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getSuperTypes", rs.next());
    }

    public void testGetSuperTables() throws Exception {
        ResultSet rs = dmd.getSuperTables(null, null, null);

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getSuperTables", rs.next());
    }

    public void testGetClientInfoProperties() throws Exception {
        ResultSet rs = ((FBDatabaseMetaData)dmd).getClientInfoProperties();

        // TODO Extend to verify columns as defined in JDBC
        assertFalse("Expected no results for getClientInfoProperties", rs.next());
    }
}
