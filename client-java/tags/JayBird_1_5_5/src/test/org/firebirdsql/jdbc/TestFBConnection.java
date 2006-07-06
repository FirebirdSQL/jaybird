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

import java.sql.*;

import org.firebirdsql.common.FBTestBase;

/**
 * Test cases for FirebirdConnection interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBConnection extends FBTestBase {
    
    public static final String CREATE_TABLE = ""
        + "CREATE TABLE test ("
        + "  col1 INTEGER"
        + ")"
        ;
    
    public static final String DROP_TABLE = ""
        + "DROP TABLE test"
        ;
    
    public static final String INSERT_DATA = ""
        + "INSERT INTO test(col1) VALUES(?)"
        ;

	public TestFBConnection(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
        
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement ddlStmt = connection.createStatement();
            try {
                try {
                    ddlStmt.execute(DROP_TABLE);
                } catch(SQLException ex) {
                    // ignore
                }
                
                ddlStmt.execute(CREATE_TABLE);
                
            } finally {
                ddlStmt.close();
            }
            
        } finally {
        	connection.close();
        }
        
	}

	protected void tearDown() throws Exception {
        
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement ddlStmt = connection.createStatement();
            try {
                ddlStmt.execute(DROP_TABLE);
            } finally {
                ddlStmt.close();
            }
        } finally {
            connection.close();
        }
        
        super.tearDown();
    }
    
    /**
     * Test if {@link FirebirdConnection#setTransactionParameters(int, int[])}
     * method works correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testTpbMapping() throws Exception {
        
        Connection conA = getConnectionViaDriverManager();
        try {
            conA.setAutoCommit(false);
            
            PreparedStatement ps = conA.prepareStatement(INSERT_DATA);
            ps.setInt(1, 1);
            ps.execute();
            ps.close();
            
            conA.commit();
            
            Connection conB = getConnectionViaDriverManager();
            try {
                conB.setAutoCommit(false);
                ((FirebirdConnection)conB).setTransactionParameters(
                        Connection.TRANSACTION_READ_COMMITTED,
                        new int[] {
                        	FirebirdConnection.TPB_READ_COMMITTED,
                            FirebirdConnection.TPB_REC_VERSION,
                            FirebirdConnection.TPB_WRITE,
                            FirebirdConnection.TPB_NOWAIT
                        });
                
                Statement stmtA = conA.createStatement();
                Statement stmtB = conB.createStatement();
                
                stmtA.execute("UPDATE test SET col1 = 2");
                try {
                	stmtB.execute("UPDATE test SET col1 = 3");
                    fail("Should notify about a deadlock.");
                } catch(SQLException ex) {
                	// everything is fine
                }
                
                stmtA.close();
                stmtB.close();
                
            } finally {
                conB.close();
            }
            
        } finally {
            conA.close();
        }
    }
    

}