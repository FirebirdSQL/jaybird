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

import java.sql.SQLException;

import javax.sql.PooledConnection;

import org.firebirdsql.jdbc.BaseFBTest;

/**
 * Test suite for JDBC connection pool.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBConnectionPoolDataSource extends BaseFBTest {
    
    private static final int DEFAULT_MIN_CONNECTIONS = 0;
    private static final int DEFAULT_MAX_CONNECTIONS = 2;
    private static final int DEFAULT_PING_INTERVAL = 5000;
    
    public TestFBConnectionPoolDataSource(String name) {
        super(name);
    }

    private FBConnectionPoolConfiguration config;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        config = new FBConnectionPoolConfiguration();
        
        config.setJdbcUrl(DB_DRIVER_URL);
        config.setMinConnections(DEFAULT_MIN_CONNECTIONS);
        config.setMaxConnections(DEFAULT_MAX_CONNECTIONS);
        config.setPingInterval(DEFAULT_PING_INTERVAL);
        
        config.setProperties(DB_INFO);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPoolStart() throws Exception {
        FBConnectionPoolDataSource pool = new FBConnectionPoolDataSource(config);
        
        try {
            pool.start();
        } catch(SQLException ex) {
            fail("Pool should have been started.");
        } finally {
            pool.shutdown();
        }
    }
    
    public void testBlocking() throws Exception {
        
        config.setBlockingTimeout(2 * 1000);
        
        final FBConnectionPoolDataSource pool = new FBConnectionPoolDataSource(config);
        
        try {
            pool.start();
            
            PooledConnection[] connections = 
                new PooledConnection[config.getMaxConnections()];
                
            // take all connections, so next access will block
            for (int i = 0; i < connections.length; i++) {
    			connections[i] = pool.getPooledConnection();
    		}
            
            class BlockingTester implements Runnable {
                private boolean failedOnTimeout;
                
                public void run() {
                    PooledConnection connection = null;
                    try {
                        connection = pool.getPooledConnection();
                    } catch(SQLException ex) {
                        //everything is fine
                        failedOnTimeout = true;
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
            Thread.sleep(config.getBlockingTimeout() + 1000);
            
            assertTrue("Blocked thread should have failed on timeout", 
                tester.failedOnTimeout);
        } finally {
            pool.shutdown();    
        }
    }
}
