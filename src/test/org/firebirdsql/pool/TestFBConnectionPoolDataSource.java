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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import javax.sql.PooledConnection;


import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.ISCConstants;

/**
 * Test suite for JDBC connection pool.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBConnectionPoolDataSource extends FBTestBase {
    
    protected static final int DEFAULT_MIN_CONNECTIONS = 0;
    protected static final int DEFAULT_MAX_CONNECTIONS = 2;
    protected static final int DEFAULT_PING_INTERVAL = 5000;
    
    public TestFBConnectionPoolDataSource(String name) {
        super(name);
    }

    protected BasicAbstractConnectionPool pool;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        FBConnectionPoolDataSource connectionPool = new FBConnectionPoolDataSource();

        connectionPool.setType(getGdsType().toString());
        
        connectionPool.setDatabase(DB_DATASOURCE_URL);
        connectionPool.setMinPoolSize(DEFAULT_MIN_CONNECTIONS);
        connectionPool.setMaxPoolSize(DEFAULT_MAX_CONNECTIONS);
        connectionPool.setPingInterval(DEFAULT_PING_INTERVAL);
        
        connectionPool.setProperties(getDefaultPropertiesForConnection());
        
        this.pool = connectionPool;
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
     * Test correctness of JNDI serialization/deserialization.
     * 
     * @throws Exception if something went wrong.
     */
    public void testJNDI() throws Exception {
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, FBConnectionPoolDataSource.class.getName());
        
        checkJNDI(props);
    }
    
    /**
     * Perform JNDI test case. This method is separated from {@link #testJNDI()}
     * during refactoring, since it is used by {@link TestDriverConnectionPoolDataSource}
     * test case too.
     * 
     * @param env environment for JNDI context.
     * 
     * @throws Exception if something went wrong.
     */
    protected void checkJNDI(Properties env) throws Exception {
        Context context = new InitialContext(env);
        try {
            context.bind("jdbc/test", pool);
            
            BasicAbstractConnectionPool testPool = 
                (BasicAbstractConnectionPool)context.lookup("jdbc/test");

            Connection testConnection = 
                testPool.getPooledConnection().getConnection();
            try {
                Statement stmt = testConnection.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery("SELECT 1 FROM rdb$database");
                    assertTrue("Result set should have at least one row.", rs.next());
                    assertTrue("Should return correct value", rs.getInt(1) == 1);
                    assertTrue("Result set should have only one row.", !rs.next());
                } finally {
                    stmt.close();
                }
            } finally {
                testConnection.close();
            }
            
        } finally {
            context.unbind("jdbc/test");
        }
    }
    
    public void testReferenceSupportWrapping() throws Exception {
        Reference ref = new Reference(FBWrappingDataSource.class.getName());
        
        fillReference(ref);
        
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, FBWrappingDataSource.class.getName());
        
        Context ctx = new InitialContext(props);
        try {
            ctx.bind("jdbc/test", ref);
            
            Object obj = ctx.lookup("jdbc/test");
            
            assertTrue("Should provide correct data source", obj instanceof FBWrappingDataSource);
            
            FBWrappingDataSource ds = (FBWrappingDataSource)obj;
            
            assertPoolConfiguration(ds);
        } finally {
            ctx.unbind("jdbc/test");
        }
    }
    
    public void testReferenceSupport() throws Exception {
        Reference ref = new Reference(FBConnectionPoolDataSource.class.getName());
        
        fillReference(ref);
        
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, FBConnectionPoolDataSource.class.getName());
        
        Context ctx = new InitialContext(props);
        try {
            ctx.bind("jdbc/test", ref);
            
            Object obj = ctx.lookup("jdbc/test");
            
            assertTrue("Should provide correct data source", obj instanceof FBConnectionPoolDataSource);
            
            FBConnectionPoolDataSource ds = (FBConnectionPoolDataSource)obj;
            
            assertPoolConfiguration(ds);
        } finally {
            ctx.unbind("jdbc/test");
        }
    }    
    
    /**
     * Assert that datasource has correct propeties.
     * 
     * @param ds data source to check
     */
    private void assertPoolConfiguration(FirebirdPool ds) {
        assertEquals(DB_DATASOURCE_URL, ds.getDatabase());
        assertEquals(DB_USER, ds.getUserName());
        assertEquals(DB_PASSWORD, ds.getPassword());
        assertEquals(getGdsType().toString(), ds.getType());
        assertEquals("USER", ds.getRoleName());
        assertEquals(32767, ds.getBlobBufferSize());
        assertEquals(8192, ds.getSocketBufferSize());
        
        assertEquals(1000, ds.getBlockingTimeout());
        assertEquals(1000, ds.getMaxIdleTime());
        assertEquals(5, ds.getMaxPoolSize());
        assertEquals(2, ds.getMinPoolSize());
        assertEquals(12000, ds.getPingInterval());
        assertEquals("TRANSACTION_REPEATABLE_READ", ds.getIsolation());

        // These properties are not avaiable via FBWrappingDataSource interface
        //
        //assertEquals(100, ds.getRetryInterval());
        //assertEquals(false, ds.getPooling());
        //assertEquals(false, ds.getStatementPooling());
        //assertEquals("SELECT CAST(2 AS INTEGER) FROM RDB$DATABASE", ds.getPingStatement());

        assertEquals("WIN1251", ds.getNonStandardProperty("isc_dpb_set_db_charset"));
        assertEquals("2048", ds.getNonStandardProperty("isc_dpb_num_buffers"));
        assertEquals("100", ds.getNonStandardProperty("isc_dpb_sweep_interval"));
    }

    /**
     * Fill the refrence.
     * 
     * @param ref instance of {@link Reference} to fill.
     */
    private void fillReference(Reference ref) {
        // Firebird standard properties
        ref.add(new StringRefAddr("database", DB_DATASOURCE_URL));
        ref.add(new StringRefAddr("userName", DB_USER));
        ref.add(new StringRefAddr("password", DB_PASSWORD));
        ref.add(new StringRefAddr("type", getGdsType().toString()));
        ref.add(new StringRefAddr("sqlRole", "USER"));
        ref.add(new StringRefAddr("blobBufferSize", "32767"));
        ref.add(new StringRefAddr("socketBufferSize", "8192"));
        
        // pool properties
        ref.add(new StringRefAddr("blockingTimeout", "1000"));
        ref.add(new StringRefAddr("maxIdleTime", "1000"));
        ref.add(new StringRefAddr("retryInterval", "100"));
        ref.add(new StringRefAddr("maxPoolSize", "5"));
        ref.add(new StringRefAddr("minPoolSize", "2"));
        ref.add(new StringRefAddr("pooling", "false"));
        ref.add(new StringRefAddr("statementPooling", "false"));
        ref.add(new StringRefAddr("pingStatement", "SELECT CAST(2 AS INTEGER) FROM RDB$DATABASE"));
        ref.add(new StringRefAddr("pingInterval", "12000"));
        ref.add(new StringRefAddr("isolation", "TRANSACTION_REPEATABLE_READ"));
        
        // non-standard properties
        ref.add(new StringRefAddr("nonStandard", "isc_dpb_set_db_charset : WIN1251"));
        ref.add(new StringRefAddr("nonStandard", "isc_dpb_num_buffers 2048"));
        ref.add(new StringRefAddr("isc_dpb_sweep_interval", "100"));
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
            
            assertTrue("Autocommit should be true.", con.getAutoCommit());
            assertTrue("Read-only should be false.", !con.isReadOnly());
            assertTrue("Tx isolation level should be read committed.",
                    con.getTransactionIsolation() == Connection.TRANSACTION_READ_COMMITTED);
            
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
                try {
                    stmt.executeUpdate("CREATE TABLE test(a INTEGER)");
                } catch(SQLException ex) {
                    if (ex.getErrorCode() != ISCConstants.isc_no_meta_update)
                        throw ex;
                }
                
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
                    
                    assertTrue("Statement that created result set should be " + 
                        "same as statement returned by ResultSet.getStatement().",
                        rs.getStatement() == ps);
                    
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
            con.close();
            
            pool.shutdown();
        }
    }
    
    /**
     * Test if statement leaks are correctly prevented.
     * 
     * @throws Exception if something went wrong.
     */
    public void testStatementLeaking() throws Exception {
        DataSource dataSource = new SimpleDataSource(pool);
        
        Connection con = dataSource.getConnection();
        
        try {
            Statement stmt = con.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM rdb$database");
            assertTrue("Should select at least one row.", rs.next());
            assertTrue("Should have correct statememnt.", rs.getStatement() == stmt);
            assertTrue("Should select exactly one row.", !rs.next());
            
            // close connection, according to specification
            // it must close also corresponding statement
            con.close();
            
            try {
                rs = stmt.executeQuery("SELECT * FROM rdb$database");
                fail("Should throw exception that statement is closed.");
            } catch(SQLException ex) {
                // everything is fine
            }
            
            // this block checks if statements are correctly
            // removed from a collection for automatic cleanup
            // we rely on tha fact, that calling Statement.close()
            // twice would cause SQLException in the second case.
            con = dataSource.getConnection();
            stmt = con.createStatement();
            stmt.close();
            con.close();
            
            // get connection from the pool, so finally clause works correctly
            con = dataSource.getConnection();
            
            assertTrue("Pool should have only one connection open.", 
                    pool.getTotalSize() == 1);

        } finally {
            con.close();
            
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
                new PooledConnection[pool.getMaxPoolSize()];
                
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
            //Thread.sleep(pool.getBlockingTimeout() + 1000);
            t.join();
            
            assertTrue("Blocked thread should have failed on timeout.", 
                tester.failedOnTimeout);
                
            assertTrue("Exception should not have been thrown too early.", 
                tester.duration >= pool.getBlockingTimeout());
        } finally {
            pool.shutdown();    
        }
    }
    
    public void testIdleRemover() throws Exception {
        pool.setMaxIdleTime(1 * 1000);
        
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

    public void testIdleRemoverAndMinPoolSize() throws Exception {
        pool.setMaxIdleTime(1 * 1000);
        pool.setMinPoolSize(1);
        
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
            
            assertTrue("Total size should be 1",
                pool.getTotalSize() == 1);
            
        } finally {
            pool.shutdown();
        }
    }

    /**
     * This test check if closing a physical connection is correctly detected
     * and pool sizes are correctly adjusted.
     * 
     * @throws Exception if something went wrong.
     */
    public void testClosePhysicalConnection() throws Exception {
        pool.setBlockingTimeout(1 * 1000);
        pool.setMaxPoolSize(1);
        
        try {
            PooledConnection physConnection = pool.getPooledConnection();
            
            assertTrue("Should have totally 1 connection", 
                pool.getTotalSize() == 1);
                
            assertTrue("Should have 1 working connection", 
                pool.getWorkingSize() == 1);
            
            physConnection.close();
            
            assertTrue("Working size should be 0", 
                pool.getWorkingSize() == 0);

            assertTrue("Total size should be 0",
                pool.getTotalSize() == 0);
            
            try {
                physConnection = pool.getPooledConnection();
            } catch(SQLException ex) {
                fail("Should get the connection without any problem. " + ex.toString());
            }
           
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            pool.shutdown();
        }
    }
}
