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
package org.firebirdsql.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.sql.PooledConnection;


import org.firebirdsql.common.FBTestBase;

/**
 * Test suite for JDBC connection pool.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBConnectionPoolDataSource extends FBTestBase {
    
    private static final int DEFAULT_MIN_CONNECTIONS = 0;
    private static final int DEFAULT_MAX_CONNECTIONS = 2;
    private static final int DEFAULT_PING_INTERVAL = 5000;
    
    public TestFBConnectionPoolDataSource(String name) {
        super(name);
    }

    private FBConnectionPoolDataSource pool;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        pool = createFBConnectionPoolDataSource();
        
        pool.setDatabase(DB_DATASOURCE_URL);
        pool.setMinConnections(DEFAULT_MIN_CONNECTIONS);
        pool.setMaxConnections(DEFAULT_MAX_CONNECTIONS);
        pool.setPingInterval(DEFAULT_PING_INTERVAL);
        
        pool.setProperties(getDefaultPropertiesForConnection());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test if pool is started correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testPoolStart() throws Exception {
        try {
            PooledConnection pooledConnection = pool.getPooledConnection();
        } catch(SQLException ex) {
            fail("Pool should have been started.");
        } finally {
            pool.shutdown();
        }
    }
    
    /**
     * Test if connection we obtained is ok.
     * 
     * @throws Exception if something is wrong.
     */
    public void testConnection() throws Exception {
        DataSource dataSource = new SimpleDataSource(pool);
        
        Connection con = dataSource.getConnection();
        
        try {
            
            Statement stmt = con.createStatement();
            
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT cast(1 AS INTEGER) FROM rdb$database");
                    
                assertTrue("Should select one row", rs.next());
                assertTrue("Selected value should be 1.", rs.getInt(1) == 1);
            } finally {            
                stmt.close();
            }
            
        } catch(SQLException ex) {
            ex.printStackTrace();
            fail("No exception should be thrown.");
        } finally {
            con.close();
            
            pool.shutdown();
        }
    }
    
    /**
     * Test if we can call some methods on closed connection.
     * 
     * @throws Exception if something went wrong.
     */
    public void testFalseConnectionUsage() throws Exception {
        DataSource dataSource = new SimpleDataSource(pool);
        
        Connection con = dataSource.getConnection();
        try {
        
            // release connection
            con.close();
            
            try {
                Statement stmt = con.createStatement();
                
                fail("Should not be able to create statement with closed connection.");
            } catch(SQLException ex) {
                // everything is ok
            } 
        }finally {
            pool.shutdown();
        }
    }
    
    /**
     * Check if prepared statements function correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testPreparedStatement() throws Exception {
        DataSource dataSource = new SimpleDataSource(pool);
        
        Connection con = dataSource.getConnection();
        
        try {
            Statement stmt = con.createStatement();
            
            try {
                stmt.executeUpdate("CREATE TABLE test(a INTEGER)");
                stmt.executeUpdate("INSERT INTO test VALUES(1)");
                
                String sql = "SELECT * FROM test WHERE a = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                
                PreparedStatement original = 
                    ((XCachablePreparedStatement)ps).getOriginal();
                    
                try {
                    ps.setInt(1, 1);
                    
                    ResultSet rs = ps.executeQuery();
                    
                    assertTrue("Result set should not be empty.", rs.next());
                    assertTrue("Correct data should be selected", rs.getInt(1) == 1);
                    
                    ps.setInt(1, 0);
                    rs = ps.executeQuery();
                    
                    assertTrue("Result set should be empty", !rs.next());
                } finally {
                    ps.close();
                }
                
                ps = con.prepareStatement(sql);
                try {
                    PreparedStatement anotherOriginal = 
                        ((XCachablePreparedStatement)ps).getOriginal();
                        
                    assertTrue("Original statemenets should be identical.", 
                        original == anotherOriginal);
                } finally {
                    ps.close();
                }
                
            } finally {
                stmt.close();
            }
        } finally{
            // we must close the connection because prepared statement
            // will be alive and it will prevent us from dropping a table
            con.close();
            
            con = dataSource.getConnection();
            try {
                Statement stmt = con.createStatement();
                try {
                    stmt.executeUpdate("DROP TABLE test");
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
            
            pool.shutdown();
        }
    }
    
    /**
     * Test if blocking works correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testBlocking() throws Exception {
        
        pool.setBlockingTimeout(2 * 1000);
        
        try {
            PooledConnection[] connections = 
                new PooledConnection[pool.getMaxConnections()];
                
            // take all connections, so next access will block
            for (int i = 0; i < connections.length; i++) {
    			connections[i] = pool.getPooledConnection();
    		}
            
            class BlockingTester implements Runnable {
                private boolean failedOnTimeout;
                private long duration;
                
                public void run() {
                    PooledConnection connection = null;
                    long start = System.currentTimeMillis();
                    try {
                        
                        connection = pool.getPooledConnection();
                        
                        // set duration to zero, to indicate incorrect behavior
                        duration = 0;
                    } catch(SQLException ex) {
                        
                        //everything is fine
                        failedOnTimeout = true;
                        duration = System.currentTimeMillis() - start;
                        
                    } finally {
                        try {
                            if (connection != null)
                                connection.close();
                        } catch(SQLException ex) {
                            // do nothing
                        }
                    }
                }
            };
            
            BlockingTester tester = new BlockingTester();
            
            Thread t = new Thread(tester, "BlockingTester");
            t.start();
            
            // sleep for blocking timeout + 1 sec.
            Thread.sleep(pool.getBlockingTimeout() + 1000);
            
            assertTrue("Blocked thread should have failed on timeout.", 
                tester.failedOnTimeout);
                
            assertTrue("Exception should not have been thrown too early.", 
                tester.duration >= pool.getBlockingTimeout());
        } finally {
            pool.shutdown();    
        }
    }
    
    public void testIdleRemover() throws Exception {
        pool.setIdleTimeout(1 * 1000);
        
        try {
            Connection con = pool.getPooledConnection().getConnection();
            
            assertTrue("Should have totally 1 connection", 
                pool.getTotalSize() == 1);
                
            assertTrue("Should have 1 working connection", 
                pool.getWorkingSize() == 1);
            
            con.close();
            
            assertTrue("Working size should be 0", 
                pool.getWorkingSize() == 0);
            
            Thread.sleep(10 * 1000 + 5 * 1000);
            
            assertTrue("Total size should be 0",
                pool.getTotalSize() == 0);
            
        } finally {
            pool.shutdown();
        }
    }
}
