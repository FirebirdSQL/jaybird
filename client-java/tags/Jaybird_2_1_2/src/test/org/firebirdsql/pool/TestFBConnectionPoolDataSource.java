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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.*;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.jdbc.FirebirdPreparedStatement;

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
        
        AbstractFBConnectionPoolDataSource connectionPool = FBPooledDataSourceFactory.createFBConnectionPoolDataSource();

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
        if (getGdsType() != GDSType.getType("PURE_JAVA"))
            fail("This test case does not work with JNI connections.");
        
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, ClassFactory.get(ClassFactory.FBConnectionPoolDataSource).getName());
        props.put(Context.PROVIDER_URL, "file:.");
        
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

            try {
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
                testPool.shutdown();
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
        Reference ref = new Reference(ClassFactory.get(ClassFactory.FBConnectionPoolDataSource).getName());
        
        fillReference(ref);
        
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES,ClassFactory.get(ClassFactory.FBConnectionPoolDataSource).getName());
        
        Context ctx = new InitialContext(props);
        try {
            ctx.bind("jdbc/test", ref);
            
            Object obj = ctx.lookup("jdbc/test");
            
            assertTrue("Should provide correct data source", obj instanceof AbstractFBConnectionPoolDataSource);
            
            AbstractFBConnectionPoolDataSource ds = (AbstractFBConnectionPoolDataSource)obj;
            
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
        assertEquals("TRANSACTION_REPEATABLE_READ", ds.getDefaultIsolation());

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
                    stmt.executeUpdate("DROP TABLE test");
                } catch(SQLException ex) {
                    if (ex.getErrorCode() != ISCConstants.isc_dsql_error)
                        throw ex;
                }
                
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
    
    /**
     * Tests restart functionality.
     * 
     * @throws Exception
     */
    public void testRestart() throws Exception {
        pool.setMinPoolSize(0);
        pool.setMaxPoolSize(5);
    	
    	DataSource datasource = new SimpleDataSource(pool);
    	
    	assertTrue("Total size should equal MinPoolSize", pool.getTotalSize() == pool.getMinPoolSize());
    	
    	ArrayList connections = new ArrayList(pool.getMaxPoolSize());
    	while (connections.size() < pool.getMaxPoolSize())
    		connections.add(datasource.getConnection());

    	assertTrue("Total size should equal MaxPoolSize", pool.getTotalSize() == pool.getMaxPoolSize());
    	
    	Iterator iter = connections.iterator();
    	while (iter.hasNext())
          ((Connection)iter.next()).close();
    	connections.clear();
    	
    	assertTrue("Total size should still equal MaxPoolSize", pool.getTotalSize() == pool.getMaxPoolSize());
    	
    	pool.restart();
    	
    	assertTrue("Total size should equal MinPoolSize", pool.getTotalSize() == pool.getMinPoolSize());
    }
    
    /**
     * Test whether shutdown works correctly with one open connection.
     * 
     * @throws Exception if something went wrong.
     */
    public void testShutdown() throws Exception {
        Connection con = pool.getPooledConnection().getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.execute("SELECT * FROM rdb$database");
        } finally {
            stmt.close();
        }
        
        pool.shutdown();
        
        try {
            Statement test = con.createStatement();
            try {
                test.close();
            } finally {
                fail("Should not happen.");
            }
        } catch(SQLException ex) {
            // everything is fine
        }
    }
    
    /**
     * Test whether shutdown works correctly with multiple open connections.
     * 
     * @throws Exception if something went wrong.
     */
    public void testShutdownMultiple() throws Exception {
        Connection con1 = pool.getPooledConnection().getConnection();
        Statement stmt = con1.createStatement();
        try {
            stmt.execute("SELECT * FROM rdb$database");
        } finally {
            stmt.close();
        }
        
        Connection con2 = pool.getPooledConnection().getConnection();
        
        pool.shutdown();
        
        try {
            Statement test = con2.createStatement();
            try {
                test.close();
            } finally {
                fail("Should not happen.");
            }
        } catch(SQLException ex) {
            // everything is fine
        }
    }
    
    public void testPrepareWithError() throws Exception {
        try {
            Connection con = pool.getPooledConnection().getConnection();
            con.setAutoCommit(false);
            try {
                PreparedStatement stmt = con.prepareStatement("bla");
                fail("Should not enter here.");
            } catch(SQLException ex) {
                // everything is fine
            } finally {
                con.close();
            }
        } finally {
            pool.shutdown();
        }
    }
    
    /**
     * Test whether role name is correctly passed to the server. Currently 
     * disabled because requires adding new user to the server manually.
     * 
     * @throws Exception if something went wrong.
     */
    public void _testSqlRole() throws Exception {
        
        Connection ddlConnection = getConnectionViaDriverManager();
        try {
            Statement stmt = ddlConnection.createStatement();
            
            try {
                try {
                    stmt.execute("CREATE TABLE test_role_table (id INTEGER)");
                } catch(SQLException ex) {
                    // ignore
                }
                
                try {
                    stmt.execute("CREATE ROLE testRole");
                } catch(SQLException ex) {
                    // ignore
                }
                
                stmt.execute("GRANT SELECT, INSERT, UPDATE ON test_role_table TO testRole");
                
                stmt.execute("GRANT testRole TO testUser");
                
            } finally {
                stmt.close();
            }
            
        } finally {
            ddlConnection.close();
        }
        
        try {
            ((FirebirdPool)pool).setUserName("testUser");
            ((FirebirdPool)pool).setRoleName("testRole");
            pool.setLoginTimeout(1);
            
            Connection connection = pool.getPooledConnection().getConnection();
            
            try {
                Statement stmt = connection.createStatement();
                
                try {
                    ResultSet rs = stmt.executeQuery("SELECT current_role FROM rdb$database");
                    rs.next();
                    
                    assertTrue("Role should be correct : " + rs.getString(1), "TESTROLE".equalsIgnoreCase(rs.getString(1)));
                    
                    stmt.execute("INSERT INTO test_role_table VALUES(1)");
                    
                    rs = stmt.executeQuery("SELECT * FROM test_role_table");
                    
                    assertTrue("Result set should not be empty.", rs.next());
                    assertTrue("First column in first row should be equal 1.", rs.getInt(1) == 1);
                    assertTrue("Result set should contain only one row", !rs.next());
                } finally {
                    stmt.close();
                }
                
            } finally {
                connection.close();
                pool.shutdown();
            }
                
        } finally {
            ddlConnection = getConnectionViaDriverManager();
            try {
                Statement ddlStatement = ddlConnection.createStatement();
                try {
                    ddlStatement.execute("DROP ROLE testRole");
                    ddlStatement.execute("DROP TABLE test_role_table");
                } finally {
                    ddlStatement.close();
                }
            } finally {
                ddlConnection.close();
            }
        }
    }
    
    public void testNoPooling() throws Exception {
        pool.setPooling(false);
        pool.setMinPoolSize(0);
        pool.setMaxPoolSize(5);

        try {
            Connection connection1 = pool.getPooledConnection().getConnection();
            assertTrue(pool.getTotalSize() == 1);
            
            connection1.close();
            assertTrue(pool.getTotalSize() == 0);
            
            Connection connection2 = pool.getPooledConnection().getConnection();
            assertTrue(pool.getTotalSize() == 1);
        } finally {
            pool.shutdown();
        }
    }
    
    public void testReleaseResultSet() throws Exception {
        try {
            Connection connection = pool.getPooledConnection().getConnection();
            connection.setAutoCommit(false);
            try {
                String sql = "SELECT * FROM rdb$database";
                
                FirebirdPreparedStatement ps = (FirebirdPreparedStatement) 
                    connection.prepareStatement(sql);
                
                ResultSet rs = ps.executeQuery();

                assertTrue("Statement should have open result set.", 
                    ps.hasOpenResultSet());
                
                ps.close();

                // strictly speaking we cannot call ps.hasOpenResultSet() method
                // now, because statement is in pool and might throw appropriate
                // exception. So we prepare another statement relying on the fact
                // that we get the same statement object back.
                ps = (FirebirdPreparedStatement)connection.prepareStatement(sql);
                
                assertTrue("Result set should be closed now.",
                    !ps.hasOpenResultSet());
                
            } finally {
                connection.close();
            }
        } finally {
            pool.shutdown();
        }
    }
    
    // "test string" in Ukrainian ("тестова стрічка")
    public static String UKRAINIAN_TEST_STRING = 
        //"\u00f2\u00e5\u00f1\u00f2\u00ee\u00e2\u00e0 " +
        "\u0442\u0435\u0441\u0442\u043e\u0432\u0430 " + 
        //"\u00f1\u00f2\u00f0\u00b3\u00f7\u00ea\u00e0";
        "\u0441\u0442\u0440\u0456\u0447\u043a\u0430";
        
    public static byte[] UKRAINIAN_TEST_BYTES = new byte[] {
        (byte)0xf2, (byte)0xe5, (byte)0xf1, (byte)0xf2, 
        (byte)0xee, (byte)0xe2, (byte)0xe0, (byte)0x20,
        (byte)0xf1, (byte)0xf2, (byte)0xf0, (byte)0xb3, 
        (byte)0xf7, (byte)0xea, (byte)0xe0
    };

    public void testEncoding() throws Exception {
    	AbstractFBConnectionPoolDataSource fbPool = (AbstractFBConnectionPoolDataSource)pool;
        
        fbPool.setEncoding("WIN1251");
        
        try {
            Connection connection = pool.getPooledConnection().getConnection();
            
            try {
                
                Statement stmt = connection.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT '" + UKRAINIAN_TEST_STRING + "' FROM rdb$database");
                    
                    assertTrue("Should select at least one row.", rs.next());
                    assertTrue("Should select correct values", 
                        Arrays.equals(rs.getBytes(1), UKRAINIAN_TEST_BYTES));
                    assertTrue("Should select correct values", 
                        UKRAINIAN_TEST_STRING.equals(rs.getString(1)));
                    assertTrue("Should select only one row.", !rs.next());
                    
                } finally {
                    stmt.close();
                }
                
            } finally {
                connection.close();
            }
        } finally {
            pool.shutdown();
        }
    }

    /**
     * It is not allowed to call "getConnection()" method on a connection that
     * is in pool.
     * 
     * @throws Exception if test did not suceed.
     */
    public void testConnectionInLoop() throws Exception {
        try {
            PooledConnection xac = ((ConnectionPoolDataSource) pool).getPooledConnection();
            
            try {
                Connection c = xac.getConnection(); 
                c.close();
                
                try {
                    Connection c2 = xac.getConnection();
                    c2.close();
                    fail("Should not obtain logical connection.");
                } catch(SQLException ex) {
                    // everything is fine
                }
            } finally {        
                xac.close();
            }
        } finally {
            pool.shutdown();
        }
    }
    
    /**
     * Check whether pool can load custom TPB mapping.
     * 
     * @throws Exception if something went wrong.
     */
    public void testCustomTpbMapping() throws Exception {
        AbstractFBConnectionPoolDataSource fbPool = (AbstractFBConnectionPoolDataSource)pool;
        fbPool.setTpbMapping("org.firebirdsql.pool.pool_tpb_mapping");
        
        try {
            FirebirdConnection connection1 = 
                (FirebirdConnection)fbPool.getPooledConnection().getConnection();
            
            try {
                TransactionParameterBuffer tpb = connection1.getTransactionParameters(
                    Connection.TRANSACTION_READ_COMMITTED);

                assertTrue(tpb.hasArgument(TransactionParameterBuffer.NOWAIT));
                
            } finally {
                connection1.close();
            }
        } finally {
            pool.shutdown();
        }
    }
}
