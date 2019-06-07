/*
 * $Id$
 *
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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * This test case checks callable statements by executing procedure through
 * {@link java.sql.CallableStatement} and {@link java.sql.PreparedStatement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBCallableStatement extends FBJUnit4TestBase {
    //@formatter:off
    private static final String CREATE_PROCEDURE =
            "CREATE PROCEDURE factorial( "
            + "  max_rows INTEGER, "
            + "  mode INTEGER "
            + ") RETURNS ( "
            + "  row_num INTEGER, "
            + "  factorial INTEGER "
            + ") AS "
            + "  DECLARE VARIABLE temp INTEGER; "
            + "  DECLARE VARIABLE counter INTEGER; "
            + "BEGIN "
            + "  counter = 0; "
            + "  temp = 1; "
            + "  WHILE (counter <= max_rows) DO BEGIN "
            + "    row_num = counter; "
            + "    IF (row_num = 0) THEN "
            + "      temp = 1; "
            + "    ELSE "
            + "      temp = temp * row_num; "
            + "    factorial = temp; "
            + "    counter = counter + 1; "
            + "    IF (mode = 1) THEN "
            + "      SUSPEND; "
            + "  END "
            + "  IF (mode = 2) THEN "
            + "    SUSPEND; "
            + "END ";

    private static final String SELECT_PROCEDURE = "SELECT * FROM factorial(?, 2)";
    private static final String CALL_SELECT_PROCEDURE = "{call factorial(?, 1, ?, ?)}";
    private static final String EXECUTE_PROCEDURE = "{call factorial(?, ?, ?, ?)}";
    private static final String EXECUTE_PROCEDURE_AS_STMT = "{call factorial(?, 0)}";

    private static final String CREATE_PROCEDURE_EMP_SELECT =
            "CREATE PROCEDURE get_emp_proj(emp_no SMALLINT) "
            + " RETURNS (proj_id VARCHAR(25)) AS "
            + " BEGIN "
            + "    FOR SELECT PROJ_ID "
            + "        FROM employee_project "
            + "        WHERE emp_no = :emp_no ORDER BY proj_id "
            + "        INTO :proj_id "
            + "    DO "
            + "        SUSPEND; "
            + "END";

    private static final String SELECT_PROCEDURE_EMP_SELECT = "SELECT * FROM get_emp_proj(?)";
    private static final String EXECUTE_PROCEDURE_EMP_SELECT = "{call get_emp_proj(?)}";

    private static final String CREATE_PROCEDURE_EMP_INSERT =
            "CREATE PROCEDURE set_emp_proj(emp_no SMALLINT, proj_id VARCHAR(10)"
            + " , last_name VARCHAR(10), proj_name VARCHAR(25)) "
            + " AS "
            + " BEGIN "
            + "    INSERT INTO employee_project (emp_no, proj_id, last_name, proj_name) "
            + "    VALUES (:emp_no, :proj_id, :last_name, :proj_name); "
            + "END";

    private static final String EXECUTE_PROCEDURE_EMP_INSERT = "{call set_emp_proj (?,?,?,?)}";
    private static final String EXECUTE_PROCEDURE_EMP_INSERT_1 = "EXECUTE PROCEDURE set_emp_proj (?,?,?,?)";
    private static final String EXECUTE_PROCEDURE_EMP_INSERT_SPACES = "EXECUTE PROCEDURE \nset_emp_proj\t   ( ?,?\t,?\n  ,?)";

    private static final String CREATE_EMPLOYEE_PROJECT =
            "CREATE TABLE employee_project( "
            + " emp_no INTEGER NOT NULL, "
            + " proj_id VARCHAR(10) NOT NULL, "
            + " last_name VARCHAR(10) NOT NULL, "
            + " proj_name VARCHAR(25) NOT NULL, "
            + " proj_desc BLOB SUB_TYPE 1, "
            + " product VARCHAR(25) )";

    private static final String CREATE_SIMPLE_OUT_PROC =
            "CREATE PROCEDURE test_out (inParam VARCHAR(10)) RETURNS (outParam VARCHAR(10)) "
            + "AS BEGIN "
            + "    outParam = inParam; "
            + "END";

    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE = "{call test_out ?, ? }";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_1 = "{?=CALL test_out(?)}";
    private static final String EXECUTE_IN_OUT_PROCEDURE = "{call test_out ?}";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST = "EXECUTE PROCEDURE test_out 'test'";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION = "EXECUTE PROCEDURE test_out 'test?'";
    private static final String EXECUTE_SIMPLE_OUT_WITH_OUT_PARAM = "EXECUTE PROCEDURE test_out(?, ?)";

    private static final String CREATE_PROCEDURE_WITHOUT_PARAMS =
            "CREATE PROCEDURE test_no_params "
            + "AS BEGIN "
            + "    exit;"
            + "END";

    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS = "{call test_no_params}";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_1 = "{call test_no_params()}";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_2 = "{call test_no_params () }";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_3 = "EXECUTE PROCEDURE test_no_params ()";

    private static final String CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS =
            "CREATE PROCEDURE select_no_params "
            + " RETURNS (proj_id VARCHAR(25)) "
            + "AS BEGIN "
            + "    proj_id = 'abc'; "
            + "    SUSPEND;"
            + "END";

    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS = "{call select_no_params}";
    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_1 = "{call select_no_params()}";
    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_2 = "{call select_no_params () }";

    private static final String CREATE_PROCEDURE_BLOB_RESULT =
            "CREATE PROCEDURE blob_result\n" +
            "RETURNS (\n" +
            "    SQL_SELECT BLOB SUB_TYPE 1 )\n" +
            "AS\n" +
            "BEGIN\n" +
            "    sql_select = '\n" +
            "        EXECUTE BLOCK\n" +
            "        RETURNS(\n" +
            "            column_info_column_name VARCHAR(30),\n" +
            "            column_value VARCHAR(100),\n" +
            "            column_alias VARCHAR(20))\n" +
            "        AS\n" +
            "        DECLARE VARIABLE i INTEGER;\n" +
            "        BEGIN\n" +
            "          i = 0;\n" +
            "          WHILE (i < 10)\n" +
            "          DO BEGIN\n" +
            "            column_info_column_name = ''FK_TETEL__DYN'';\n" +
            "            column_value = ascii_char(ascii_val(''a'') + i);\n" +
            "            column_alias = UPPER(column_value);\n" +
            "            SUSPEND;\n" +
            "\n" +
            "            i = i + 1;\n" +
            "          END\n" +
            "        END';\n" +
            "END";

    private static final String EXECUTE_PROCEDURE_BLOB_RESULT = "EXECUTE PROCEDURE blob_result";
    //@formatter:on

    private Connection con;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        con = getConnectionViaDriverManager();
    }

    @After
    public void tearDown() throws Exception {
        closeQuietly(con);
    }

    @Test
    public void testRun() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE);
        try {
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.registerOutParameter(4, Types.INTEGER);
            ((FirebirdCallableStatement) cstmt).setSelectableProcedure(false);
            cstmt.setInt(1, 5);
            cstmt.setInt(2, 0);
            cstmt.execute();
            int ans = cstmt.getInt(4);
            assertEquals("Wrong answer", 120, ans);
        } finally {
            cstmt.close();
        }

        PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE);
        try {
            stmt.setInt(1, 5);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            int result = rs.getInt(2);
            assertEquals("Wrong result", 120, result);

            assertFalse("Should have exactly one row.", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }

        CallableStatement cs = con.prepareCall(CALL_SELECT_PROCEDURE);
        try {
            ((FirebirdCallableStatement) cs).setSelectableProcedure(true);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.setInt(1, 5);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            assertTrue("Should have at least one row", rs.next());
            int result = cs.getInt(3);
            assertEquals("Wrong result", 1, result);

            int counter = 1;
            while (rs.next()) {
                assertEquals(rs.getInt(2), cs.getInt(3));
                counter++;
            }

            assertEquals("Should have 6 rows", 6, counter);
            rs.close();
        } finally {
            cs.close();
        }
    }

    @Test
    public void testRun_emp_cs() throws Exception {
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);

        CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        } finally {
            cstmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
            cstmt.setInt(1, 44);
            ResultSet rs = cstmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            assertEquals("First row value must be DGPII", "DGPII", rs.getString(1));

            cstmt.setInt(1, 22);
            rs = cstmt.executeQuery();
            assertTrue("Should have one row", rs.next());
            assertEquals("First row value must be OTHER", "OTHER", rs.getString(1));
            assertFalse("Should have one row", rs.next());

            rs.close();
        } finally {
            cstmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
            cstmt.setInt(1, 44);
            cstmt.execute();
            assertEquals("First row value must be DGPII", "DGPII", cstmt.getString(1));

            cstmt.setInt(1, 22);
            cstmt.execute();
            assertEquals("First row value must be OTHER", "OTHER", cstmt.getString(1));

        } finally {
            cstmt.close();
        }

        con.setAutoCommit(true);
        PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE_EMP_SELECT);
        try {
            stmt.setInt(1, 44);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            int count = 0;
            for (String expectedValue : new String[] {"DGPII", "HWRII", "VBASE"}) {
                count++;
                assertTrue(String.format("Expected row %d", count), rs.next());
                assertEquals(String.format("Unexpected value for row %d", count), expectedValue, rs.getString(1));
            }
            assertFalse("Should have no more rows", rs.next());

            stmt.setInt(1, 22);
            rs = stmt.executeQuery();
            assertTrue("Should have one row", rs.next());
            assertEquals("First row value must be OTHER", "OTHER", rs.getString(1));
            assertFalse("Should have one row", rs.next());

            rs.close();
        } finally {
            stmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT_1);
        try {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        } finally {
            cstmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT_SPACES);
        try {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
        } finally {
            cstmt.close();
        }
    }

    @Test
    public void testFatalError() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        PreparedStatement stmt = con.prepareStatement(EXECUTE_PROCEDURE_AS_STMT);
        try {
            stmt.setInt(1, 5);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            int result = rs.getInt(2);
            assertEquals("Wrong result", 120, result);

            assertFalse("Should have exactly one row.", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testOutProcedure() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertEquals("Should return correct value", 1, stmt.getInt(2));
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testOutProcedure1() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_1);
        try {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, 1);
            stmt.execute();
            assertEquals("Should return correct value", 1, stmt.getInt(1));
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testOutProcedureWithConst() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST);
        try {
            stmt.execute();
            assertEquals("Should return correct value", "test", stmt.getString(1));
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testOutProcedureWithConstWithQuestionMark() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION);
        try {
            stmt.execute();
            assertEquals("Should return correct value", "test?", stmt.getString(1));
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testInOutProcedure() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement stmt = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
        try {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertEquals("Should return correct value", 1, stmt.getInt(1));

            stmt.clearParameters();
            stmt.setInt(1, 2);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertEquals("Should return correct value", 2, stmt.getInt(1));
        } finally {
            stmt.close();
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters. Bug found and reported by Stanislav Bernatsky.
     */
    @Test
    public void testProcedureWithoutParams() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters but with braces in call. Reported by Ben (vmdd_tech).
     */
    @Test
    public void testProcedureWithoutParams1() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_1);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters, with braces in call, but with space between procedure
     * name and braces. Reported by Ben (vmdd_tech).
     */
    @Test
    public void testProcedureWithoutParams2() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_2);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }

        // and now test EXECUTE PROCEDURE syntax
        stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_3);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }

    }

    @Test
    public void testBatch() throws Exception {
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.addBatch();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();

            cstmt.executeBatch();

            try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM employee_project");
                rs.last();
                assertEquals("Should find 4 records.", 4, rs.getRow());

                cstmt.setInt(1, 22);
                cstmt.setString(2, "VBASE");
                cstmt.setString(3, "Stevens");
                cstmt.setString(4, "Translator upgrade");
                cstmt.addBatch();

                cstmt.setInt(1, 22);
                cstmt.setNull(2, Types.CHAR);
                cstmt.setString(3, "Roman");
                cstmt.setString(4, "Failure upgrade");
                cstmt.addBatch();

                try {
                    cstmt.executeBatch();
                    fail("Should throw an error.");
                } catch (SQLException ex) {
                    // everything is ok
                }

                rs = stmt.executeQuery("SELECT * FROM employee_project");
                rs.last();
                if (((FirebirdConnection) con).isUseFirebirdAutoCommit()) {
                    assertEquals("Should find 5 records.", 5, rs.getRow());
                } else {
                    assertEquals("Should find 4 records.", 4, rs.getRow());
                }
            }
        }
    }

    @Test
    public void testBatchResultSet() throws Exception {
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);

        CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        } finally {
            cstmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
            cstmt.setInt(1, 44);
            cstmt.addBatch();
            cstmt.setInt(1, 22);
            cstmt.addBatch();
            cstmt.executeBatch();
            fail("Result sets not allowed in batch execution.");
        } catch (BatchUpdateException e) {
            //Do nothing.  Exception should be thrown.
        } finally {
            cstmt.close();
        }
    }

    /**
     * Test Batch.  IN-OUT parameters are prohibited in batch execution.
     */
    @Test
    public void testBatchInOut() throws Exception {
        CallableStatement stmt = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
        try {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.addBatch();

            expectedException.expect(BatchUpdateException.class);

            stmt.executeBatch();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test Batch.  OUT parameters are prohibited in batch execution.
     */
    @Test
    public void testBatchOut() throws Exception {
        CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.addBatch();

            expectedException.expect(BatchUpdateException.class);

            stmt.executeBatch();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     */
    @Test
    public void testAutomaticSetSelectableProcedure_Selectable() throws SQLException {
        assumeTrue("Firebird version does not support RDB$PROCEDURE_TYPE", databaseEngineHasSelectabilityInfo());
        executeDDL(con, CREATE_PROCEDURE);

        FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall(CALL_SELECT_PROCEDURE);
        try {
            assertTrue("Expected selectable procedure", cs.isSelectableProcedure());
        } finally {
            cs.close();
        }
    }

    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     */
    @Test
    public void testAutomaticSetSelectableProcedure_Executable() throws SQLException {
        assumeTrue("Firebird version does not support RDB$PROCEDURE_TYPE", databaseEngineHasSelectabilityInfo());

        FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
            assertFalse("Expected executable procedure (not-selectable)", cs.isSelectableProcedure());
        } finally {
            cs.close();
        }
    }

    @Test
    public void testAutomaticSetSelectableProcedureAfterMetaUpdate() throws SQLException {
        assumeTrue("Firebird version does not support RDB$PROCEDURE_TYPE", databaseEngineHasSelectabilityInfo());

        final String CREATE_SIMPLE_PROC =
                "CREATE PROCEDURE MULT (A INTEGER, B INTEGER) RETURNS (C INTEGER)"
                + "AS BEGIN "
                + "    C = A * B;"
                + "    SUSPEND;"
                + "END";

        con.setAutoCommit(false);
        CallableStatement callableStatement = con.prepareCall(CALL_SELECT_PROCEDURE);
        callableStatement.close();

        Statement stmt = con.createStatement();

        stmt.execute(CREATE_SIMPLE_PROC);
        con.commit();

        try {
            FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall("{call mult(?, ?)}");
            try {
                assertTrue("Expected selectable procedure", cs.isSelectableProcedure());
            } finally {
                cs.close();
            }
        } finally {
            stmt.close();
        }
    }

    private boolean databaseEngineHasSelectabilityInfo() throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        int majorVersion = metaData.getDatabaseMajorVersion();
        int minorVersion = metaData.getDatabaseMinorVersion();

        return majorVersion > 2 || majorVersion == 2 && minorVersion >= 1;
    }

    @Test
    public void testJdbc181() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        CallableStatement cs = con.prepareCall("{call factorial(?, ?)}"); //con.prepareStatement("EXECUTE PROCEDURE factorial(?, ?)");
        try {
            cs.setInt(1, 5);
            cs.setInt(2, 1);
            ResultSet rs = cs.executeQuery();
            int counter = 0;
            int factorial = 1;
            while (rs.next()) {
                assertEquals(counter, rs.getInt(1));
                assertEquals(factorial, rs.getInt(2));
                counter++;
                if (counter > 0)
                    factorial *= counter;
            }
        } finally {
            cs.close();
        }
    }

    /**
     * Closing a statement twice should not result in an Exception.
     *
     * @throws SQLException
     */
    @Test
    public void testDoubleClose() throws SQLException {
        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        stmt.close();
        stmt.close();
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     * <p>
     * JDBC 4.1 feature
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        executeDDL(con, CREATE_PROCEDURE);

        FBCallableStatement stmt = (FBCallableStatement) con.prepareCall("{call factorial(?, ?)}");
        try {
            stmt.closeOnCompletion();
            stmt.setInt(1, 5);
            stmt.setInt(2, 1);
            stmt.execute();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertTrue("Resultset should be closed (automatically closed after last result read)", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    // Other closeOnCompletion behavior considered to be sufficiently tested in TestFBStatement

    /**
     * The method {@link java.sql.Statement#executeQuery(String)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecuteQuery_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.executeQuery("SELECT * FROM test_blob");
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecuteUpdate_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.executeUpdate("SELECT * FROM test_blob");
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecute_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.execute("SELECT * FROM test_blob");
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#addBatch(String)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedAddBatch_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.addBatch("SELECT * FROM test_blob");
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, int)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecuteUpdate_String_int() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.executeUpdate("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS);
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecuteUpdate_String_intArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.executeUpdate("SELECT * FROM test_blob", new int[] { 1 });
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, String[])} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecuteUpdate_String_StringArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.executeUpdate("SELECT * FROM test_blob", new String[] { "col" });
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int)} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecute_String_int() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.execute("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS);
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallabeStatement.
     */
    @Test
    public void testUnsupportedExecute_String_intArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.execute("SELECT * FROM test_blob", new int[] { 1 });
        } finally {
            cs.close();
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, String[])} should not work on CallableStatement.
     */
    @Test
    public void testUnsupportedExecute_String_StringArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            expectedException.expect(fbStatementOnlyMethodException());

            cs.execute("SELECT * FROM test_blob", new String[] { "col" });
        } finally {
            cs.close();
        }
    }

    /**
     * Basic test of {@link FBCallableStatement#getMetaData()}.
     */
    @Test
    public void testGetMetaData() throws Exception {
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);

        CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
            ResultSetMetaData metaData = cs.getMetaData();

            assertEquals("columnCount", 1, metaData.getColumnCount());
            assertEquals("columnLabel", "PROJ_ID", metaData.getColumnLabel(1));
            assertEquals("columnName", "PROJ_ID", metaData.getColumnName(1));
            // Basic checking, rest should be covered by TestFBResultSetMetaData.
        } finally {
            cs.close();
        }
    }

    /**
     * Calling {@link java.sql.CallableStatement#getMetaData()} on a closed statement should throw an exception.
     */
    @Test
    public void testGetMetaData_statementClosed() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        cs.close();

        expectedException.expect(fbStatementClosedException());

        cs.getMetaData();
    }

    @Test
    public void testExecuteSelectableProcedureNoParameters_call_noBraces() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS);
        try {
            assertTrue("Expected ResultSet", cs.execute());
            ResultSet rs = cs.getResultSet();
            assertNotNull("Expected ResultSet", rs);
            assertTrue("Expected at least one row", rs.next());
            assertEquals("abc", rs.getString("proj_id"));
        } finally {
            cs.close();
        }
    }

    @Test
    public void testExecuteSelectableProcedureNoParameters_call_emptyBraces() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_1);
        try {
            assertTrue("Expected ResultSet", cs.execute());
            ResultSet rs = cs.getResultSet();
            assertNotNull("Expected ResultSet", rs);
            assertTrue("Expected at least one row", rs.next());
            assertEquals("abc", rs.getString("proj_id"));
        } finally {
            cs.close();
        }
    }

    @Test
     public void testExecuteSelectableProcedureNoParameters_call_emptyBraces_withWhitespace() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_2);
        try {
            assertTrue("Expected ResultSet", cs.execute());
            ResultSet rs = cs.getResultSet();
            assertNotNull("Expected ResultSet", rs);
            assertTrue("Expected at least one row", rs.next());
            assertEquals("abc", rs.getString("proj_id"));
        } finally {
            cs.close();
        }
    }

    /**
     * Test if first accessing and processing the result set from an <b>executable</b> procedure,
     * and then accessing the getters works.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-350">JDBC-350</a>,
     * </p>
     * <p>
     * NOTE: This tests behavior we maintain for compatibility with previous versions; a correct implementation
     * shouldn't return a result set at all.
     * </p>
     */
    @Test
    public void testExecutableProcedureAccessResultSetFirst_thenGetter_shouldWork() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        CallableStatement cs = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
        try {
            cs.setString(1, "paramvalue");
            assertTrue("Expected ResultSet", cs.execute());
            ResultSet rs = cs.getResultSet();
            assertNotNull("Expected ResultSet", rs);
            assertTrue("Expected at least one row", rs.next());
            assertEquals("paramvalue", rs.getString("outParam"));
            rs.close();

            assertEquals("paramvalue", cs.getString(1));
        } finally {
            cs.close();
        }
    }

    /**
     * Tests if the blob result of an executable procedure can be retrieved and is non-empty.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-381">JDBC-381</a>.
     * </p>
     */
    @Test
    public void testExecutableProcedureBlobResult_shouldGetNonEmptyValue() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_BLOB_RESULT);

        CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_BLOB_RESULT);
        try {
            cs.execute();
            String value = cs.getString(1);
            assertNotNull("Expected non-null value", value);
            assertTrue("Expected non-empty value", value.trim().length() > 0);
        } finally {
            cs.close();
        }
    }

    @Test
    public void testExecutableProcedureGetMetaDataOutParameterSpecifiedValueNotSet() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_WITH_OUT_PARAM)) {
            cs.registerOutParameter(2, Types.VARCHAR);

            // Calling getParameterMetaData and getMetaData should not throw an exception
            cs.getParameterMetaData();
            cs.getMetaData();
        }
    }

    @Test
    public void testIgnoreProcedureType() throws Exception{
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.addBatch();
            cstmt.executeBatch();
        }

        // First verify with default connection that procedure is selectable
        try (CallableStatement cstmt = con.prepareCall("{call get_emp_proj(?)}")) {
            assertTrue("Expected procedure inferred as selectable",
                    cstmt.unwrap(FirebirdCallableStatement.class).isSelectableProcedure());
            cstmt.setInt(1, 44);
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue("Should have at least one row", rs.next());
                assertEquals("First row value must be DGPII", "DGPII", rs.getString(1));
                assertTrue("Should have at least a second row", rs.next());
                assertEquals("Second row value must be VBASE", "VBASE", rs.getString(1));
            }
        }

        // Then check with ignoreProcedureType = true
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("ignoreProcedureType", "true");
        try (Connection conn2 = DriverManager.getConnection(getUrl(), props);
             CallableStatement cstmt = conn2.prepareCall("{call get_emp_proj(?)}")) {
            assertFalse("Expected procedure inferred as executable",
                    cstmt.unwrap(FirebirdCallableStatement.class).isSelectableProcedure());
            cstmt.setInt(1, 44);
            cstmt.execute();
            assertEquals("Value must be DGPII", "DGPII", cstmt.getString(1));

            // re-check via non-standard result set if it has a single row (executable should end after first result)
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue("Should have at least one row", rs.next());
                assertEquals("First row value must be DGPII", "DGPII", rs.getString(1));
                assertFalse("Should not have a second row", rs.next());
            }
        }
    }
}
