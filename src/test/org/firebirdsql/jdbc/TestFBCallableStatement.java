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

import org.firebirdsql.common.FBTestBase;

import java.sql.*;

/**
 * This test case checks callable statements by executing procedure through
 * {@link java.sql.CallableStatement} and {@link java.sql.PreparedStatement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBCallableStatement extends FBTestBase {
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
        + "END " 
        ;

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

    private static final String CREATE_PROCEDURE_WITHOUT_PARAMS =
         "CREATE PROCEDURE test_no_params "
         + "AS BEGIN "
         + "    exit; "
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

    public TestFBCallableStatement(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        con = getConnectionViaDriverManager();
        Statement stmt = con.createStatement();
        try {
            stmt.execute(CREATE_PROCEDURE);
            stmt.execute(CREATE_EMPLOYEE_PROJECT);
            stmt.execute(CREATE_PROCEDURE_EMP_SELECT);
            stmt.execute(CREATE_PROCEDURE_EMP_INSERT);
            stmt.execute(CREATE_SIMPLE_OUT_PROC);
            stmt.execute(CREATE_PROCEDURE_WITHOUT_PARAMS);
            stmt.execute(CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);
            stmt.execute(CREATE_PROCEDURE_BLOB_RESULT);
        } finally {
            closeQuietly(stmt);
        }
    }

    protected void tearDown() throws Exception {
        try {
            closeQuietly(con);
        } finally {
            super.tearDown();
        }
    }

    public void testRun() throws Exception {
        CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE);
        try {
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.registerOutParameter(4, Types.INTEGER);
            ((FirebirdCallableStatement) cstmt).setSelectableProcedure(false);
            cstmt.setInt(1, 5);
            cstmt.setInt(2, 0);
            cstmt.execute();
            int ans = cstmt.getInt(4);
            assertTrue("got wrong answer, expected 120: " + ans, ans == 120);
        } finally {
            cstmt.close();
        }

        PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE);
        try {
            stmt.setInt(1, 5);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            int result = rs.getInt(2);
            assertTrue("Wrong result: expecting 120, received " + result, result == 120);

            assertTrue("Should have exactly one row.", !rs.next());
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
            assertTrue("Wrong result: expecting 120, received " + result, result == 1);

            int counter = 1;
            while (rs.next()) {
                assertTrue(rs.getInt(2) == cs.getInt(3));
                counter++;
            }

            assertTrue("Should have 6 rows", counter == 6);
            rs.close();
        } finally {
            cs.close();
        }
    }

    public void testRun_emp_cs() throws Exception {
        //
        // Insert and select with callable statement
        // 		 
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
            assertTrue("First row value must be DGPII", rs.getString(1).equals("DGPII"));
            //assertTrue("Should have three rows", !rs.next());

            cstmt.setInt(1, 22);
            rs = cstmt.executeQuery();
            assertTrue("Should have one row", rs.next());
            assertTrue("First row value must be OTHER", rs.getString(1).equals("OTHER"));
            assertTrue("Should have one row", !rs.next());

            rs.close();
        } finally {
            cstmt.close();
        }

        cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
            cstmt.setInt(1, 44);
            cstmt.execute();
            assertTrue("First row value must be DGPII", cstmt.getString(1).equals("DGPII"));

            cstmt.setInt(1, 22);
            cstmt.execute();
            assertTrue("First row value must be OTHER, is " +
                    cstmt.getString(1), cstmt.getString(1).equals("OTHER"));

        } finally {
            cstmt.close();
        }

        con.setAutoCommit(true);
        PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE_EMP_SELECT);
        try {
            stmt.setInt(1, 44);
            stmt.execute();
            //ResultSet rs = stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            assertTrue("Should have three rows", rs.next());
            assertTrue("First row value must be DGPII", rs.getString(1).equals("DGPII"));
            assertTrue("Should have three rows", rs.next());
            assertTrue("Second row value must be HWRII", rs.getString(1).equals("HWRII"));
            assertTrue("Should have three rows", rs.next());
            assertTrue("First row value must be VBASE", rs.getString(1).equals("VBASE"));
            assertTrue("Should have three rows", !rs.next());

            stmt.setInt(1, 22);
            rs = stmt.executeQuery();
            assertTrue("Should have one row", rs.next());
            assertTrue("First row value must be OTHER", rs.getString(1).equals("OTHER"));
            assertTrue("Should have one row", !rs.next());

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

    public void testFatalError() throws Exception {
        PreparedStatement stmt = con.prepareStatement(EXECUTE_PROCEDURE_AS_STMT);
        try {
            stmt.setInt(1, 5);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            int result = rs.getInt(2);
            assertTrue("Wrong result: expecting 120, received " + result, result == 120);

            assertTrue("Should have exactly one row.", !rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }

    public void testOutProcedure() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", stmt.getInt(2) == 1);
        } finally {
            stmt.close();
        }

    }

    public void testOutProcedure1() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_1);
        try {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, 1);
            stmt.execute();
            assertTrue("Should return correct value", stmt.getInt(1) == 1);
        } finally {
            stmt.close();
        }

    }

    public void testOutProcedureWithConst() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST);
        try {
            //stmt.setInt(1, 1);
            //stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", "test".equals(stmt.getString(1)));
        } finally {
            stmt.close();
        }

    }

    public void testOutProcedureWithConstWithQuestionMart() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION);
        try {
            //stmt.setInt(1, 1);
            //stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", "test?".equals(stmt.getString(1)));
        } finally {
            stmt.close();
        }
    }

    public void testInOutProcedure() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
        try {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", stmt.getInt(1) == 1);
            stmt.clearParameters();
            stmt.setInt(1, 2);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", stmt.getInt(1) == 2);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters. Bug found and reported by Stanislav Bernatsky.
     *
     * @throws Exception if something went wrong.
     */
    public void testProcedureWithoutParams() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters but with braces in call. Reported by Ben (vmdd_tech).
     *
     * @throws Exception if something went wrong.
     */
    public void testProcedureWithoutParams1() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_1);
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
     *
     * @throws Exception if something went wrong.
     */
    public void testProcedureWithoutParams2() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_2);
        try {
            stmt.execute();
            // assertTrue("Should return correct value", stmt.getInt(1) == 1);
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

    public void testBatch() throws Exception {
        CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
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

            Statement stmt = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            try {
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

            } finally {
                stmt.close();
            }

        } finally {
            cstmt.close();
        }
    }

    public void testBatchResultSet() throws Exception {
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
     *
     * @throws Exception if something went wrong.
     */
    public void testBatchInOut() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
        try {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.addBatch();
            stmt.clearParameters();
            stmt.setInt(1, 2);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.addBatch();
            stmt.executeBatch();
            fail("IN-OUT parameters not allowed in batch execution");
        } catch (BatchUpdateException e) {
            //Expected exception
        } finally {
            stmt.close();
        }
    }

    /**
     * Test Batch.  OUT parameters are prohibited in batch execution.
     *
     * @throws Exception if something went wrong.
     */
    public void testBatchOut() throws Exception {
        CallableStatement stmt =
                con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.addBatch();

            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.addBatch();

            stmt.executeBatch();

            fail("OUT parameters not allowed in batch execution");
        } catch (BatchUpdateException e) {
            // Expected exception
        } finally {
            stmt.close();
        }
    }

    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     * @throws SQLException
     */
    public void testAutomaticSetSelectableProcedure() throws SQLException {
        if (!databaseEngineHasSelectabilityInfo()) {
            return;
        }

        FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall(CALL_SELECT_PROCEDURE);
        try {
            assertTrue(cs.isSelectableProcedure());
        } finally {
            cs.close();
        }

        cs = (FirebirdCallableStatement) con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
            assertFalse(cs.isSelectableProcedure());
        } finally {
            cs.close();
        }
    }

    public void testAutomaticSetSelectableProcedureAfterMetaUpdate() throws SQLException {
        if (!databaseEngineHasSelectabilityInfo()) {
            return;
        }

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
                assertTrue(cs.isSelectableProcedure());
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

        if (majorVersion > 2) {
            return true;
        }
        if (majorVersion == 2 && minorVersion >= 1) {
            return true;
        }
        return false;
    }

    public void testJdbc181() throws Exception {
        PreparedStatement ps = con.prepareCall("{call factorial(?, ?)}"); //con.prepareStatement("EXECUTE PROCEDURE factorial(?, ?)");
        try {
            ps.setInt(1, 5);
            ps.setInt(2, 1);
            ResultSet rs = ps.executeQuery();
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
            closeQuietly(ps);
        }
    }

    /**
     * Closing a statement twice should not result in an Exception.
     *
     * @throws SQLException
     */
    public void testDoubleClose() throws SQLException {
        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        try {
            stmt.close();
            stmt.close();
        } finally {
            closeQuietly(stmt);
        }
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
    public void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
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

    public void testExecuteSelectableProcedureNoParameters_call_noBraces() throws Exception {
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

    public void testExecuteSelectableProcedureNoParameters_call_emptyBraces() throws Exception {
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

    public void testExecuteSelectableProcedureNoParameters_call_emptyBraces_withWhitespace() throws Exception {
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
    public void testExecutableProcedureAccessResultSetFirst_thenGetter_shouldWork() throws Exception {
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
    public void testExecutableProcedureBlobResult_shouldGetNonEmptyValue() throws Exception {
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
}
