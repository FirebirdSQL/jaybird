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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This test case checks callable statements by executing procedure through
 * {@link java.sql.CallableStatement} and {@link java.sql.PreparedStatement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBCallableStatement extends FBTestBase {
    public static final String CREATE_PROCEDURE = ""
        + "CREATE PROCEDURE factorial(number INTEGER, mode INTEGER) RETURNS (result INTEGER) " 
        + "AS " 
        + "  DECLARE VARIABLE temp INTEGER; " 
        + "BEGIN " 
        + "  temp = number - 1; " 
        + "  IF (NOT temp IS NULL) THEN BEGIN " 
        + "    IF (temp > 0) THEN " 
        + "      EXECUTE PROCEDURE factorial(:temp, 0) RETURNING_VALUES :temp; " 
        + "    ELSE " 
        + "      temp = 1; " 
        + "    result = number * temp; " 
        + "  END "
        + "  IF (mode = 1) THEN "
        + "    SUSPEND; "
        + "END"
        ;

    public static final String DROP_PROCEDURE =
        "DROP PROCEDURE factorial;";

    public static final String SELECT_PROCEDURE =
        "SELECT * FROM factorial(?, 1)";

    public static final String EXECUTE_PROCEDURE =
        "{call factorial(?, ?, ?)}";
    
    public static final String EXECUTE_PROCEDURE_AS_STMT =
        "{call factorial(?, 0)}";
    
	 public static final String CREATE_PROCEDURE_EMP_SELECT = ""
	     + "CREATE PROCEDURE get_emp_proj(emp_no SMALLINT) "
		  + " RETURNS (proj_id VARCHAR(25)) AS "
		  + " BEGIN "
		  + "    FOR SELECT PROJ_ID "
		  + "        FROM employee_project "
		  + "        WHERE emp_no = :emp_no ORDER BY proj_id "
		  + "        INTO :proj_id "
		  + "    DO "
		  + "        SUSPEND; "
		  + "END";

    public static final String DROP_PROCEDURE_EMP_SELECT =
        "DROP PROCEDURE get_emp_proj;";
    public static final String SELECT_PROCEDURE_EMP_SELECT =
        "SELECT * FROM get_emp_proj(?)";

    public static final String EXECUTE_PROCEDURE_EMP_SELECT =
        "{call get_emp_proj(?)}";

	 public static final String CREATE_PROCEDURE_EMP_INSERT = ""
	     + "CREATE PROCEDURE set_emp_proj(emp_no SMALLINT, proj_id VARCHAR(10)"
		  + " , last_name VARCHAR(10), proj_name VARCHAR(25)) "
		  + " AS "
		  + " BEGIN "
        + "    INSERT INTO employee_project (emp_no, proj_id, last_name, proj_name) "
		  + "    VALUES (:emp_no, :proj_id, :last_name, :proj_name); "
		  + "END";

    public static final String DROP_PROCEDURE_EMP_INSERT =
        "DROP PROCEDURE set_emp_proj;";

    public static final String EXECUTE_PROCEDURE_EMP_INSERT =
        "{call set_emp_proj(?, ?, ? ,?)}";

	 public static final String CREATE_EMPLOYEE_PROJECT = ""
	     + "CREATE TABLE employee_project( "
		  + " emp_no INTEGER NOT NULL, "
		  + " proj_id VARCHAR(10) NOT NULL, "
		  + " last_name VARCHAR(10) NOT NULL, "
		  + " proj_name VARCHAR(25) NOT NULL, "
		  + " proj_desc BLOB SUB_TYPE 1, "
		  + " product VARCHAR(25) )";

	 public static final String DROP_EMPLOYEE_PROJECT = 
	     "DROP TABLE employee_project;";
     
     public static final String CREATE_SIMPLE_OUT_PROC = ""
         + "CREATE PROCEDURE test_out (inParam VARCHAR(10)) RETURNS (outParam VARCHAR(10)) "
         + "AS BEGIN "
         + "    outParam = inParam; "
         + "END"
         ;
     
     public static final String DROP_SIMPLE_OUT_PROC = ""
         + "DROP PROCEDURE test_out"
         ;
     
     public static final String EXECUTE_SIMPLE_OUT_PROCEDURE = ""
         + "{call test_out ?, ? }"
         ;
         
     public static final String EXECUTE_IN_OUT_PROCEDURE = ""
         + "{call test_out ?}"
         ;
     
     public static final String CREATE_PROCEDURE_WITHOUT_PARAMS = ""
         + "CREATE PROCEDURE test_no_params "
         + "AS BEGIN "
         + "    exit; "
         + "END"
         ;
     
     public static final String DROP_PROCEDURE_WITHOUT_PARAMS = ""
         + "DROP PROCEDURE test_no_params"
         ;
     
     public static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS = ""
         + "{call test_no_params}"
         ;
     
     public static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_1 = ""
         + "{call test_no_params()}"
         ;
     
     public static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_2 = ""
         + "{call test_no_params () }"
         ;
     
     public static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_3 = ""
         + "EXECUTE PROCEDURE test_no_params ()"
         ;
     
     public static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST = ""
         + "EXECUTE PROCEDURE test_out 'test'";

    private Connection connection;

    public TestFBCallableStatement(String testName) {
        super(testName);
    }


    protected void setUp() throws Exception {
       super.setUp();
        Class.forName(FBDriver.class.getName());
        connection = getConnectionViaDriverManager();
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_PROCEDURE);
        }
        catch (Exception e) {}
        try {
            stmt.executeUpdate(DROP_PROCEDURE_EMP_SELECT);
        }
        catch (Exception e) {}
        try {
            stmt.executeUpdate(DROP_PROCEDURE_EMP_INSERT);
        }
        catch (Exception e) {}
        try {
            stmt.executeUpdate(DROP_EMPLOYEE_PROJECT);
        }
        catch (Exception e) {}
        try {
            stmt.executeUpdate(DROP_SIMPLE_OUT_PROC);
        }
        catch (Exception e) {}
        try {
            stmt.executeUpdate(DROP_PROCEDURE_WITHOUT_PARAMS);
        }
        catch (Exception e) {}
        

        stmt.executeUpdate(CREATE_PROCEDURE);
        stmt.executeUpdate(CREATE_EMPLOYEE_PROJECT);
        stmt.executeUpdate(CREATE_PROCEDURE_EMP_SELECT);
        stmt.executeUpdate(CREATE_PROCEDURE_EMP_INSERT);
        stmt.executeUpdate(CREATE_SIMPLE_OUT_PROC);
        stmt.executeUpdate(CREATE_PROCEDURE_WITHOUT_PARAMS);
        
        stmt.close();
    }
    protected void tearDown() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_PROCEDURE);
        stmt.executeUpdate(DROP_PROCEDURE_EMP_SELECT);
        stmt.executeUpdate(DROP_PROCEDURE_EMP_INSERT);
        stmt.executeUpdate(DROP_EMPLOYEE_PROJECT);
        stmt.executeUpdate(DROP_SIMPLE_OUT_PROC);
        stmt.executeUpdate(DROP_PROCEDURE_WITHOUT_PARAMS);
        stmt.close();
        connection.close();
        super.tearDown();
    }

    public void testRun() throws Exception {
        CallableStatement cstmt = connection.prepareCall(EXECUTE_PROCEDURE);
        try {
          cstmt.registerOutParameter(2, Types.INTEGER);
          cstmt.setInt(1, 5);
          cstmt.setInt(3, 0);
          cstmt.execute();
          int ans = cstmt.getInt(2);
          assertTrue("got wrong answer, expected 120: " + ans, ans == 120);
        } finally {
          cstmt.close();
        }
        
        PreparedStatement stmt = connection.prepareStatement(SELECT_PROCEDURE);
        try {
          stmt.setInt(1, 5);
          ResultSet rs = stmt.executeQuery();
          assertTrue("Should have at least one row", rs.next());
          int result = rs.getInt(1);
          assertTrue("Wrong result: expecting 120, received " + result, result == 120);
                
          assertTrue("Should have exactly one row.", !rs.next());
          rs.close();
        } finally {
          stmt.close();
        }
    }

    public void testRun_emp_cs() throws Exception {
        //
        // Insert and select with callable statement
        // 		 
        CallableStatement cstmt = connection.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
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
        
        cstmt = connection.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
        try {
          cstmt.setInt(1, 44);
          ResultSet rs = cstmt.executeQuery();
          assertTrue("Should have three rows", rs.next());
			 assertTrue("First row value must be DGPII", rs.getString(1).equals("DGPII"));
          assertTrue("Should have three rows", !rs.next());
			 
          cstmt.setInt(1, 22);			 
          rs = cstmt.executeQuery();
          assertTrue("Should have one row", rs.next());
			 assertTrue("First row value must be OTHER", rs.getString(1).equals("OTHER"));
          assertTrue("Should have one row", !rs.next());
			 
          rs.close();
        } finally {
          cstmt.close();
        }

        cstmt = connection.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT);
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
		  
        connection.setAutoCommit(true);
        PreparedStatement stmt = connection.prepareStatement(SELECT_PROCEDURE_EMP_SELECT);
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
          connection.setAutoCommit(false);
        }
    }

    public void testFatalError() throws Exception {
        PreparedStatement stmt = connection.prepareStatement(EXECUTE_PROCEDURE_AS_STMT);
        try {
          stmt.setInt(1, 5);
          ResultSet rs = stmt.executeQuery();
          assertTrue("Should have at least one row", rs.next());
          int result = rs.getInt(1);
          assertTrue("Wrong result: expecting 120, received " + result, result == 120);

          assertTrue("Should have exactly one row.", !rs.next());
          rs.close();
        } finally {
          stmt.close();
        }
    }
	 
    public void testOutProcedure() throws Exception {
        CallableStatement stmt = 
            connection.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", stmt.getInt(2) == 1);
        } finally {
            stmt.close();
        }
        
    }

    public void testOutProcedureWithConst() throws Exception {
        CallableStatement stmt = 
            connection.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST);
        try {
            //stmt.setInt(1, 1);
            //stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertTrue("Should return correct value", "test".equals(stmt.getString(1)));
        } finally {
            stmt.close();
        }
        
    }
    
    public void testInOutProcedure() throws Exception {
        CallableStatement stmt = 
            connection.prepareCall(EXECUTE_IN_OUT_PROCEDURE);
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
            connection.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS);
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
            connection.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_1);
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
            connection.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_2);
        try {
            stmt.execute();
            // assertTrue("Should return correct value", stmt.getInt(1) == 1);
        } finally {
            stmt.close();
        }
        
        // and now test EXECUTE PROCEDURE syntax
        stmt = connection.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_3);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }
        
    }

}
