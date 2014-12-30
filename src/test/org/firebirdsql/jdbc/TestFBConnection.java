/*
 * $Id$
 * 
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
import java.util.Properties;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.*;
import org.firebirdsql.jca.FBManagedConnection;

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
    @SuppressWarnings("deprecation")
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

                /*
                
                // This is correct way to set transaction parameters
                // However, we use deprecated methods to check the
                // backward compatibility
                
                TransactionParameterBuffer tpb = ((FirebirdConnection)conB).createTransactionParameterBuffer();
                tpb.addArgument(TransactionParameterBuffer.READ_COMMITTED);
                tpb.addArgument(TransactionParameterBuffer.REC_VERSION);
                tpb.addArgument(TransactionParameterBuffer.WRITE);
                tpb.addArgument(TransactionParameterBuffer.NOWAIT);
                
                ((FirebirdConnection)conB).setTransactionParameters(tpb);
                */
                
                ((FirebirdConnection)conB).setTransactionParameters(
                        Connection.TRANSACTION_READ_COMMITTED,
                        new int[] {
                        	FirebirdConnection.TPB_READ_COMMITTED,
                            FirebirdConnection.TPB_REC_VERSION,
                            FirebirdConnection.TPB_WRITE,
                            FirebirdConnection.TPB_NOWAIT
                        });
                conB.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                
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
    

    public void testStatementCompletion() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            try {
                stmt.executeQuery("SELECT * FROM rdb$database");
                connection.rollback();
                connection.setAutoCommit(true);
            } finally {
                stmt.close();
            }
            
            stmt = connection.createStatement();
            try {
                stmt.executeQuery("SELECT * FROM rdb$database");
                stmt.executeQuery("SELECT * FROM rdb$database");
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
    }
    
    public void testExecuteStatementTwice() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        
        Statement ddlStmt = connection.createStatement();
        try {
            try {
                ddlStmt.execute("DROP TABLE test_exec_twice");
            } catch(SQLException ex) {
                // ignore
            }
            
            ddlStmt.execute("CREATE TABLE test_exec_twice(col1 VARCHAR(100))");
        } finally {
            ddlStmt.close();
        }
        
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            String select1 = "SELECT * FROM test_exec_twice";
            String select2 = select1 + " WHERE col1 > ? ORDER BY col1";

            PreparedStatement pstmt = connection.prepareStatement(select2);

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(select1);  // throws Exception on the 2nd call
            rs.close();

            pstmt.setString(1, "ABC");
            rs = pstmt.executeQuery();
            for (int i = 0; i < 10 && rs.next(); i++)
                ;   // do something
            rs.close();

            // on the following 2nd call the exception gets thrown
            rs = stmt.executeQuery(select1);  // throws Exception on the 2nd call
            rs.close();

            pstmt.setString(1, "ABC");
            rs = pstmt.executeQuery();
            for (int i = 0; i < 10 && rs.next(); i++)
                ;   // do something
            rs.close();

            
        } finally {
            connection.commit();
            connection.close();
        }
    }
    
    public void testLockTable() throws Exception {
        FirebirdConnection connection = 
            (FirebirdConnection)getConnectionViaDriverManager();
        
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute("CREATE TABLE test_lock(col1 INTEGER)");
            } catch(SQLException ex) {
                // ignore
            }
        } finally {
            connection.close();
        }

        connection = (FirebirdConnection)getConnectionViaDriverManager();
        try {
            
            Statement stmt = connection.createStatement();
            try {
            
                TransactionParameterBuffer tpb = 
                    connection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);
                
                if (tpb.hasArgument(TransactionParameterBuffer.WAIT)) {
                tpb.removeArgument(TransactionParameterBuffer.WAIT);
                tpb.addArgument(TransactionParameterBuffer.NOWAIT);
                }
                
                connection.setTransactionParameters(Connection.TRANSACTION_READ_COMMITTED, tpb);
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                
                connection.setAutoCommit(false);
                
                FirebirdConnection anotherConnection = 
                    (FirebirdConnection)getConnectionViaDriverManager();
                anotherConnection.setAutoCommit(false);
                
                try {
                    TransactionParameterBuffer anotherTpb = 
                        anotherConnection.createTransactionParameterBuffer();
                    
                    anotherTpb.addArgument(TransactionParameterBuffer.CONSISTENCY);
                    anotherTpb.addArgument(TransactionParameterBuffer.WRITE);
                    anotherTpb.addArgument(TransactionParameterBuffer.NOWAIT);
                    
                    anotherTpb.addArgument(TransactionParameterBuffer.LOCK_WRITE, "TEST_LOCK");
                    anotherTpb.addArgument(TransactionParameterBuffer.PROTECTED);
                    
                    anotherConnection.setTransactionParameters(anotherTpb);
                    
                    Statement anotherStmt = anotherConnection.createStatement();
                    try {
                        anotherStmt.execute("INSERT INTO test_lock VALUES(1)");
                    } finally {
                        anotherStmt.close();
                    }
                    
                    try {
                        stmt.execute("INSERT INTO test_lock VALUES(2)");
                        fail("Should throw an error because of lock conflict.");
                    } catch(SQLException ex) {
                        assertEquals(ISCConstants.isc_lock_conflict, ex.getErrorCode());
                    }
                    
                } finally {
                    anotherConnection.close();
                }
            
            } finally {
                stmt.close();
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            connection.close();
        }
    }
    
    public void testMetaDataTransaction() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            connection.setAutoCommit(true);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "RDB$DATABASE", null);
            rs.close();
        } finally {
            connection.close();
        }
    }
    
    public void testTransactionCoordinatorAutoCommitChange() throws Exception {
        
        Connection connection = getConnectionViaDriverManager();
        try {
            
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM rdb$database");
            
            try {
                connection.setAutoCommit(false);
            } finally {
                ps.close();
            }
            
        } finally {
            connection.close();
        }
        
    }
    
    public void testDefaultHoldableResultSet() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("defaultHoldable", "");
        
        Class.forName(FBDriver.class.getName());
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            
            AbstractStatement stmt1 = (AbstractStatement)connection.createStatement();
            assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, stmt1.getResultSetHoldability());
            
            Statement stmt2 = connection.createStatement();
            
            ResultSet rs1 = stmt1.executeQuery(
                "SELECT rdb$collation_name, rdb$character_set_id FROM rdb$collations");
            while(rs1.next()) {
                ResultSet rs2 = stmt2.executeQuery(
                    "SELECT rdb$character_set_name FROM rdb$character_sets " +
                    "WHERE rdb$character_set_id = " + rs1.getInt(2));
                
                assertTrue("Should find corresponding charset.", rs2.next());
            }
            
        } finally {
            connection.close();
        }
    }
    
    public void testGetAttachments() throws Exception {
        FirebirdConnection connection = getConnectionViaDriverManager();
        try {
            AbstractConnection abstractConnection = (AbstractConnection)connection;
            
            GDS gds = (abstractConnection).getInternalAPIHandler();
            
            byte[] infoRequest = new byte[] {ISCConstants.isc_info_user_names, ISCConstants.isc_info_end};
            byte[] reply = gds.iscDatabaseInfo(
                abstractConnection.getIscDBHandle(), infoRequest, 1024);
            
            int i = 0;
            
            while(reply[i] != ISCConstants.isc_info_end) {
                switch(reply[i++]) {
                    case ISCConstants.isc_info_user_names :
                        gds.iscVaxInteger(reply, i, 2); // can be ignored
                        i += 2;
                        int strLen = reply[i] & 0xff;
                        i += 1;
                        String userName = new String(reply, i, strLen);
                        i += strLen;
                        System.out.println(userName);
                        break;
                    default :
                        break;
                }
            }
            
        } finally {
            connection.close();
        }
    }
    
    public void testWireProtocolCompatibility() throws Exception {
        String sql = "SELECT max(rdb$format) FROM rdb$formats";
        
        FirebirdConnection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(sql);
                assertTrue("Should fetch some rows.", rs.next());
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
    }
    
    /**
     * Test if not explicitly specifying a connection characterset results in a warning on the connection.
     */
    public void testNoCharactersetWarning() throws Exception {
        try {
            Class.forName(FBDriver.class.getName());
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No suitable driver.");
        }

        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        Connection con = null;
        try {
            con = DriverManager.getConnection(getUrl(), props);
            SQLWarning warnings = con.getWarnings();
            assertNotNull("Expected a warning for not specifying connection characterset", warnings);
            assertEquals("Unexpected warning message for not specifying connection characterset", FBManagedConnection.WARNING_NO_CHARSET, warnings.getMessage());
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test if explicitly specifying a connection characterset does not add a warning on the connection.
     */
    public void testCharactersetNoWarning() throws Exception {
        try {
            Class.forName(FBDriver.class.getName());
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No suitable driver.");
        }

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "UTF8");
        
        Connection con = null;
        try {
            con = DriverManager.getConnection(getUrl(), props);
            SQLWarning warnings = con.getWarnings();
            assertNull("Expected no warning when specifying connection characterset", warnings);
        } finally {
            closeQuietly(con);
        }
    }
}
