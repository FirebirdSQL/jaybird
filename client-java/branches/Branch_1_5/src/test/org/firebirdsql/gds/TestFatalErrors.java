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
package org.firebirdsql.gds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FirebirdConnection;


/**
 * Test cases to check driver's bahviour in case of fatal errors. 
 */
public class TestFatalErrors extends FBTestBase {

    /**
     * Create instance of this class for the specified test case.
     * 
     * @param name name of the test case.
     */
    public TestFatalErrors(String name) {
        super(name);
    }
    
    public static final String CREATE_TABLE = ""
        + "CREATE TABLE test_fatal ("
        + "    id INTEGER NOT NULL "
        + ")"
        ;
    
    public static final String DROP_TABLE = ""
        + "DROP TABLE test_fatal"
        ;
    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            try {
                try {
                    stmt.execute(CREATE_TABLE);
                } catch(SQLException ex) {
                    // ignore
                }
                
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
    }

    protected void tearDown() throws Exception {
        
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            try {
                
                try {
                    stmt.execute(DROP_TABLE);
                } catch(SQLException ex) {
                    // ignore
                }
                
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }

        super.tearDown();
    }
    
    /**
     * Test driver behaviour when fatal error happens in the middle of 
     * transaction.
     * 
     * @throws Exception
     */
    public void testFatalInTransaction() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        
        int[] savedFatalErrors = ISCConstants.FATAL_ERRORS;

        try {
            Statement stmt = connection.createStatement();
            
            stmt.execute("ALTER TABLE test_fatal ADD CONSTRAINT test_fatal_unique UNIQUE(id)");

            connection.setAutoCommit(false);

            try {
                // initiate transaction by an insert
                stmt.execute("INSERT INTO test_fatal VALUES (1)");
                
                // next call will generate an error 335544569
                // which for the time being is registered as fatal
                
                int[] newFatalErrors = new int[savedFatalErrors.length + 1];
                
                // we temporarily mark isc_dsql_error as fatal
                newFatalErrors[0] = ISCConstants.isc_dsql_error;
                System.arraycopy(savedFatalErrors, 0, newFatalErrors, 1, savedFatalErrors.length);
                ISCConstants.FATAL_ERRORS = newFatalErrors;
                
                try {
                    stmt.execute("bla-bla-bla");
                } catch(SQLException ex) {
                    if (ex.getErrorCode() != ISCConstants.isc_dsql_error)
                        throw ex;
                }
                
                // any subsequent call to the same statement should fail
                try {
                    stmt.execute("INSERT INTO test_fatal VALUES (2)");
                    fail("This statement execution should fail.");
                } catch(SQLException ex) {
                    // everything is ok
                }
                
                // forget the previous transaction
                connection.rollback();
                
                // but it should be possible to create new statement
                // and use it without any problem
                
                Statement anotherStmt = connection.createStatement();
                try {
                    anotherStmt.execute("INSERT INTO test_fatal VALUES (2)");
                } finally {
                    anotherStmt.close();
                }
                
            } finally {
                
                ISCConstants.FATAL_ERRORS = savedFatalErrors;
                
                try {
                    stmt.close();
                } catch(SQLException ex) {
                    ex.printStackTrace();
                    fail("No exception should have been thrown here");
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                connection.close();
            } catch(SQLException ex) {
                fail("No exception should have been thrown here");
            }
        }
    }
    
    /**
     * Test driver's behaviour when using JNI-bridge.
     * 
     * @throws Exception if something went wrong.
     */
    public void testNativeFatalInTransaction() throws Exception {
        String savedProperty = System.getProperty("test.gds_type");
        System.setProperty("test.gds_type", "NATIVE");
        try {
            testFatalInTransaction();
        } finally {
            if (savedProperty == null)
                savedProperty = "PURE_JAVA";
            System.setProperty("test.gds_type", savedProperty);
        }
    }
    
    public void testFatalOnCommit() throws Exception {
        FirebirdConnection connection = (FirebirdConnection)getConnectionViaDriverManager();
        
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        connection.setAutoCommit(false);

        int[] savedFatalErrors = ISCConstants.FATAL_ERRORS;

        try {
            Statement stmt = connection.createStatement();
            
            try {
                // initiate transaction by an insert
                stmt.execute("INSERT INTO test_fatal VALUES (1)");
                
                // next call will generate an error 335544569
                // which for the time being is registered as fatal
                
                int[] newFatalErrors = new int[savedFatalErrors.length + 1];
                
                // we temporarily mark isc_no_dup as fatal
                newFatalErrors[0] = ISCConstants.isc_no_dup;
                System.arraycopy(savedFatalErrors, 0, newFatalErrors, 1, savedFatalErrors.length);
                ISCConstants.FATAL_ERRORS = newFatalErrors;

                // insert duplicate (no constraint yet)
                stmt.execute("INSERT INTO test_fatal VALUES (1)");
                
                // add constraint (in theory should fail)
                stmt.execute("ALTER TABLE test_fatal ADD CONSTRAINT test_fatal_unique UNIQUE(id)");
                
                try {
                    connection.commit();
                    fail("Should throw exception due to constraint violation");
                } catch(SQLException ex) {
                    
                    try {
                        stmt.execute("INSERT INTO test_fatal VALUES (2)");
                        fail("It should not be possible to do anything in " +
                                "transaction in which fatal error occured.");
                    } catch(SQLException ex1) {
                        // correct
                    }
                }
                
                // forget the previous transaction
                connection.rollback();
                
                // but it should be possible to create new statement
                // and use it without any problem
                
                Statement anotherStmt = connection.createStatement();
                try {
                    anotherStmt.execute("INSERT INTO test_fatal VALUES (1)");
                } finally {
                    anotherStmt.close();
                }
                
            } finally {
                
                ISCConstants.FATAL_ERRORS = savedFatalErrors;
                
                try {
                    stmt.close();
                } catch(SQLException ex) {
                    ex.printStackTrace();
                    fail("No exception should have been thrown here");
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                connection.close();
            } catch(SQLException ex) {
                fail("No exception should have been thrown here");
            }
        }
    }
    
    public void testNativeFatalOnCommit() throws Exception {
        String savedProperty = System.getProperty("test.gds_type");
        System.setProperty("test.gds_type", "NATIVE");
        try {
            testFatalOnCommit();
        } finally {
            if (savedProperty == null)
                savedProperty = "PURE_JAVA";
            System.setProperty("test.gds_type", savedProperty);
        }
    }
}
