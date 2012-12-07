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

import static org.firebirdsql.common.JdbcResourceHelper.*;

/**
 * This test case checks callable statements by executing procedure through
 * {@link java.sql.CallableStatement} and {@link java.sql.PreparedStatement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBCallableStatement extends FBTestBase {
    public static final String CREATE_PROCEDURE =
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

    public static final String DROP_PROCEDURE =
        "DROP PROCEDURE factorial;";

    public static final String SELECT_PROCEDURE =
        "SELECT * FROM factorial(?, 2)";
    
    public static final String CALL_SELECT_PROCEDURE =
        "{call factorial(?, 1, ?, ?)}";

    public static final String EXECUTE_PROCEDURE =
        "{call factorial(?, ?, ?, ?)}";
    
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
        "{call set_emp_proj (?,?,?,?)}";

    public static final String EXECUTE_PROCEDURE_EMP_INSERT_1 =
        "EXECUTE PROCEDURE set_emp_proj (?,?,?,?)";

    public static final String EXECUTE_PROCEDURE_EMP_INSERT_SPACES =
        "EXECUTE PROCEDURE \nset_emp_proj\t   ( ?,?\t,?\n  ,?)";

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
         
     public static final String EXECUTE_SIMPLE_OUT_PROCEDURE_1 = ""
         + "{?=CALL test_out(?)}"
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
     
     public static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION = ""
         + "EXECUTE PROCEDURE test_out 'test?'";

    private Connection con;

    public TestFBCallableStatement(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        con = getConnectionViaDriverManager();
        Statement stmt = con.createStatement();
        try {
            try {
                stmt.executeUpdate(DROP_PROCEDURE);
            } catch (Exception e) {}
            try {
                stmt.executeUpdate(DROP_PROCEDURE_EMP_SELECT);
            } catch (Exception e) {}
            try {
                stmt.executeUpdate(DROP_PROCEDURE_EMP_INSERT);
            } catch (Exception e) {}
            try {
                stmt.executeUpdate(DROP_EMPLOYEE_PROJECT);
            } catch (Exception e) {}
            try {
                stmt.executeUpdate(DROP_SIMPLE_OUT_PROC);
            } catch (Exception e) {}
            try {
                stmt.executeUpdate(DROP_PROCEDURE_WITHOUT_PARAMS);
            } catch (Exception e) {}

            stmt.executeUpdate(CREATE_PROCEDURE);
            stmt.executeUpdate(CREATE_EMPLOYEE_PROJECT);
            stmt.executeUpdate(CREATE_PROCEDURE_EMP_SELECT);
            stmt.executeUpdate(CREATE_PROCEDURE_EMP_INSERT);
            stmt.executeUpdate(CREATE_SIMPLE_OUT_PROC);
            stmt.executeUpdate(CREATE_PROCEDURE_WITHOUT_PARAMS);

        } finally {
            closeQuietly(stmt);
        }
    }
    
    protected void tearDown() throws Exception {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(DROP_PROCEDURE);
            stmt.executeUpdate(DROP_PROCEDURE_EMP_SELECT);
            stmt.executeUpdate(DROP_PROCEDURE_EMP_INSERT);
            stmt.executeUpdate(DROP_EMPLOYEE_PROJECT);
            stmt.executeUpdate(DROP_SIMPLE_OUT_PROC);
            stmt.executeUpdate(DROP_PROCEDURE_WITHOUT_PARAMS);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
            super.tearDown();
        }
    }

    public void testRun() throws Exception {
    	CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE);
        try {
          cstmt.registerOutParameter(3, Types.INTEGER);
          cstmt.registerOutParameter(4, Types.INTEGER);
          ((FirebirdCallableStatement)cstmt).setSelectableProcedure(false);
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
          ((FirebirdCallableStatement)cs).setSelectableProcedure(true);
          cs.registerOutParameter(2, Types.INTEGER);
          cs.registerOutParameter(3, Types.INTEGER);
          cs.setInt(1, 5);
          cs.execute();
          ResultSet rs = cs.getResultSet();
          assertTrue("Should have at least one row", rs.next());
          int result = cs.getInt(3);
          assertTrue("Wrong result: expecting 120, received " + result, result == 1);
                
          int counter = 1;
          while(rs.next()) {
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
              } catch(SQLException ex) {
                  // everything is ok
              }


              rs = stmt.executeQuery("SELECT * FROM employee_project");
              rs.last();
              assertEquals("Should find 4 records.", 4, rs.getRow());

          } finally {
              stmt.close();
          }
          
        } finally {
          cstmt.close();
        }
    }
    
    public void testBatchResultSet() throws Exception
    {
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
        }
        catch (BatchUpdateException e)
        {
        	
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
        }
        catch (BatchUpdateException e){}
        finally {
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
        }
        catch (BatchUpdateException e){}
        finally {
            stmt.close();
        }
    }
    
    
    
    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     * @throws SQLException 
     */
    public void testAutomaticSetSelectableProcedure() throws SQLException{
    	if (!databaseEngineHasSelectabilityInfo()){
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
    	if (!databaseEngineHasSelectabilityInfo()){
    		return;
    	}
    	
    	final String CREATE_SIMPLE_PROC = ""
    		+ "CREATE PROCEDURE MULT (A INTEGER, B INTEGER) RETURNS (C INTEGER)"
    		+ "AS BEGIN "
    		+ "    C = A * B;"
    		+ "    SUSPEND;"
    		+ "END";
    	
    	final String DROP_SIMPLE_PROC = "DROP PROCEDURE MULT";
    	
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
    		stmt.execute(DROP_SIMPLE_PROC);
    		stmt.close();
    	}
    }
    
    private boolean databaseEngineHasSelectabilityInfo() throws SQLException {
    	DatabaseMetaData metaData = con.getMetaData();
    	int majorVersion = metaData.getDatabaseMajorVersion();
    	int minorVersion = metaData.getDatabaseMinorVersion();
    	
    	if (majorVersion > 2){
    		return true;
    	}
    	if (majorVersion == 2 && minorVersion >= 1){
    		return true;
    	}
    	return false;
    }
    
    public void testJdbc181() throws Exception {
        CallableStatement cs = con.prepareCall("{call factorial(?, ?)}"); //con.prepareStatement("EXECUTE PROCEDURE factorial(?, ?)");
        try {
            cs.setInt(1, 5);
            cs.setInt(2, 1);
            ResultSet rs = cs.executeQuery();
            int counter = 0; 
            int factorial = 1;
            while(rs.next()) {
                assertEquals(counter, rs.getInt(1));
                assertEquals(factorial, rs.getInt(2));
                counter++;
                if (counter > 0)
                    factorial *= counter;
            }
        } finally {
            closeQuietly(cs);
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
        FBCallableStatement stmt = (FBCallableStatement)con.prepareCall("{call factorial(?, ?)}");
        try {
            stmt.closeOnCompletion();
            stmt.setInt(1, 5);
            stmt.setInt(2, 1);
            stmt.execute();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
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
    public void testUnsupportedExecuteQuery_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.executeQuery("SELECT * FROM test_blob");
            fail("Expected SQLException when executing executeQuery(String) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#executeUpdate(String)} should not work on CallabeStatement.
     */
    public void testUnsupportedExecuteUpdate_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.executeUpdate("SELECT * FROM test_blob");
            fail("Expected SQLException when executing executeUpdate(String) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#execute(String)} should not work on CallabeStatement.
     */
    public void testUnsupportedExecute_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.execute("SELECT * FROM test_blob");
            fail("Expected SQLException when executing execute(String) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#addBatch(String)} should not work on CallabeStatement.
     */
    public void testUnsupportedAddBatch_String() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.addBatch("SELECT * FROM test_blob");
            fail("Expected SQLException when executing addBatch(String) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#executeUpdate(String, int)} should not work on CallabeStatement.
     */
    public void testUnsupportedExecuteUpdate_String_int() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.executeUpdate("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS);
            fail("Expected SQLException when executing executeUpdate(String, int) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallabeStatement.
     */
    public void testUnsupportedExecuteUpdate_String_intArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.executeUpdate("SELECT * FROM test_blob", new int[] { 1 });
            fail("Expected SQLException when executing executeUpdate(String, int[]) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#executeUpdate(String, String[])} should not work on CallabeStatement.
     */
    public void testUnsupportedExecuteUpdate_String_StringArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.executeUpdate("SELECT * FROM test_blob", new String[] { "col" });
            fail("Expected SQLException when executing executeUpdate(String, String[]) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#execute(String, int)} should not work on CallabeStatement.
     */
    public void testUnsupportedExecute_String_int() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.execute("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS);
            fail("Expected SQLException when executing execute(String, int) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallabeStatement.
     */
    public void testUnsupportedExecute_String_intArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.execute("SELECT * FROM test_blob", new int[] { 1 });
            fail("Expected SQLException when executing execute(String, int[]) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }
    
    /**
     * The method {@link java.sql.Statement#execute(String, String[])} should not work on CallabeStatement.
     */
    public void testUnsupportedExecute_String_StringArr() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        try {
            cs.execute("SELECT * FROM test_blob", new String[] { "col" });
            fail("Expected SQLException when executing execute(String, String[]) on CallabeStatement");
        } catch (SQLException ex) {
            assertStatementOnlyException(ex);
        } finally {
            closeQuietly(cs);
        }
    }

    private void assertStatementOnlyException(SQLException ex) {
        assertEquals("Unexpected SQLState for statement only method called on FBCallableStatement", 
                FBSQLException.SQL_STATE_GENERAL_ERROR, ex.getSQLState());
        assertEquals("Unexpected exception message for statement only method called on FBCallableStatement", 
                FBPreparedStatement.METHOD_NOT_SUPPORTED, ex.getMessage());
    }
}
