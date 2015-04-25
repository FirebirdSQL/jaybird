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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Random;

import org.firebirdsql.common.FBTestBase;


/**
 * Test case for multi-threaded access. Due to a long duration of the test and
 * requirement to check results by human, all test cases except dummy one are 
 * excluded from run during normal build process. They should be run manually. 
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestMultithreadedAccess extends FBTestBase {

    /**
     * Create instance of this class.
     * 
     * @param name name of the test case.
     */
    public TestMultithreadedAccess(String name) {
        super(name);
    }
    
    public static final String CREATE_TABLE_1 = ""
        + "CREATE TABLE table1 ("
        + "  id INTEGER, "
        + "  charValue VARCHAR(255)"
        + ")";
    
    public static final String CREATE_TABLE_2 = ""
        + "CREATE TABLE table2 ("
        + "  id INTEGER, "
        + "  blobValue BLOB"
        + ")";
    
    public static final String DROP_TABLE_1 = ""
        + "DROP TABLE table1";
    
    public static final String DROP_TABLE_2 = ""
        + "DROP TABLE table2";
    
    public static final String INSERT_TABLE_1 = ""
        + "INSERT INTO table1 (id, charValue) VALUES (?, ?)";
    
    public static final String INSERT_TABLE_2 = ""
        + "INSERT INTO table2 (id, blobValue) VALUES (?, ?)";
    
    public static final String SELECT_TABLE_1 = ""
        + "SELECT id, charValue FROM table1 WHERE id BETWEEN 0 and {0}";
    
    public static final String SELECT_TABLE_2 = ""
        + "SELECT id, blobValue FROM table2 WHERE id = {0}";
    
    public static final int RECORD_COUNT = 100;
    public static final int TEST_TIME_SECONDS = 120;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                
                executeDDL(stmt, DROP_TABLE_1, true);
                executeDDL(stmt, DROP_TABLE_2, true);
                
                executeDDL(stmt, CREATE_TABLE_1, false);
                executeDDL(stmt, CREATE_TABLE_2, false);
                
            } finally {
                stmt.close();
            }

            connection.setAutoCommit(false);
            
            populateTable(connection, INSERT_TABLE_1, 255);
            populateTable(connection, INSERT_TABLE_2, 8192);
            
            connection.commit();

        } finally {
            connection.close();
        }
    }
    
    
    protected void tearDown() throws Exception {
        
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                executeDDL(stmt, DROP_TABLE_1, true);
                executeDDL(stmt, DROP_TABLE_2, true);
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
        
        super.tearDown();
    }
    
    /**
     * Execute DDL statement and ignore the exception if needed.
     * 
     * @param statement statement that will be used to execute DDL.
     * @param sql DDL code.
     * @param ignoreExceptions <code>true</code> if exceptions should be ignred.
     * 
     * @throws SQLException if exception happened during execution and
     * <code>ignoreExceptions</code> is <code>false</code>.
     */
    private void executeDDL(Statement statement, String sql, 
            boolean ignoreExceptions) throws SQLException 
    {
        try {
            statement.executeUpdate(sql);
        } catch(SQLException ex) {
            if (!ignoreExceptions)
                throw ex;
        }
    }
    
    /**
     * Populate table with random data.
     * 
     * @param connection connection that will be used.
     * @param sql INSERT statement.
     * @param maxLength maximum length of the second param in bytes.
     * 
     * @throws SQLException if something went wrong.
     */
    private void populateTable(Connection connection, String sql, int maxLength) 
        throws SQLException 
    {
        Random rnd = new Random();
        
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            for(int i = 0; i < RECORD_COUNT; i++) {
                int length = rnd.nextInt(maxLength);
                byte[] data = new byte[length];
                
                rnd.nextBytes(data);
                
                ps.setInt(1, i);
                ps.setBytes(2, data);
                
                ps.execute();
            }
        } finally {
            ps.close();
        }
    }
    
    /**
     * Dummy test case. It is present only to make JUnit happy.
     * 
     * @throws Exception never happens
     */
    public void testDummy() throws Exception {
        // empty
    }
    
    
    /**
     * Implementation of the test case that will execute some SQL statement
     * and fetch all records from the result set in a separate thread.
     */
    private static class RandomSelector implements Runnable {
    
        private int maxId;
        
        private Statement stmt;
        private String sqlTemplate;
        
        private boolean stopped;
        
        protected RandomSelector(int maxId) {
            this.maxId = maxId;
        }
        
        protected RandomSelector(Statement stmt, String sqlTemplate, int maxId) {
            this(maxId);
            this.sqlTemplate = sqlTemplate;
            this.stmt = stmt;
        }
        
        protected Statement getStatement() {
            return stmt;
        }
        
        public void run() {

            try {
                Random rnd = new Random();
                
                while(!stopped) {
                    ResultSet rs = executeQuery(rnd.nextInt(maxId));
                    try {
                        while(rs.next()) {
                            rs.getInt(1);
                            rs.getBytes(2);
                        }
                    } catch(SQLException ex) {
                        // ignore it here
                        ex.printStackTrace();
                    }
                }
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        protected ResultSet executeQuery(int id) throws SQLException {
            String sql = MessageFormat.format(
                    sqlTemplate, 
                    new Object[]{
                            new Integer(id)
                    });
            
            System.out.println(sql);
            
            return getStatement().executeQuery(sql);
        }
        
        public void stop() {
            stopped = true;
        }
    }
    
    /**
     * Test if single statement can be safely used in multiple transactions.
     * The only visible outcome of this test case are messages in stderr that
     * result set was closed. Any other error message means test case failure.
     * 
     * @throws Exception if something went wrong.
     */
    public void _testStatements() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        connection.setAutoCommit(false);
        try {
        
            Statement stmt = connection.createStatement();
            //stmt.setCursorName("test");
            
            // this is needed to initiate a transaction
            ResultSet rs = stmt.executeQuery("SELECT * FROM rdb$database");
            rs.close();
            
            RandomSelector selector1 = 
                new RandomSelector(stmt, SELECT_TABLE_1, RECORD_COUNT);
            
            RandomSelector selector2 = 
                new RandomSelector(stmt, SELECT_TABLE_2, RECORD_COUNT);
            
            Thread thread1 = new Thread(selector1, "Selector 1");
            Thread thread2 = new Thread(selector2, "Selector 2");
            
            thread1.start();
            thread2.start();
            
            Thread.sleep(TEST_TIME_SECONDS * 1000);
            
            selector1.stop();
            selector2.stop();
            
            thread1.join();
            thread2.join();
            
            stmt.close();
            
        } finally {
            connection.close();
        }
    }
    
    /**
     * Extension of the {@link RandomSelector} for the case of 
     * {@link PreparedStatement}.
     */
    private static class RandomPreparedSelector extends RandomSelector {
        
        private Connection connection;
        private PreparedStatement stmt;
        private String sql;
        
        public RandomPreparedSelector(Connection connection, String sql, int maxId) {
            super(maxId);
            this.connection = connection;
            this.sql = sql;
        }
        
        
        public ResultSet executeQuery(int id) throws SQLException {
            
            // close previously executed statement
            if (stmt != null)
                stmt.close();
            
            stmt = connection.prepareStatement(
                    sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            stmt.setInt(1, id);

            return stmt.executeQuery();
        }
    }

    /**
     * Test if prepared statements can be used in multiple transactions. The only
     * visible outcome of this test case is messages in stderr that result set
     * was already closed. Any other error message means test case failure.
     * 
     * @throws Exception if something went wrong.
     */
    public void _testPreparedStatements() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        connection.setAutoCommit(false);
        try {
        
            String sql1 = MessageFormat.format(
                    SELECT_TABLE_1, new Object[]{"?"});
            
            String sql2 = MessageFormat.format(
                    SELECT_TABLE_2, new Object[]{"?"});
            
            RandomSelector selector1 = 
                new RandomPreparedSelector(connection, sql1, RECORD_COUNT);
            
            RandomSelector selector2 = 
                new RandomPreparedSelector(connection, sql2, RECORD_COUNT);
            
            Thread thread1 = new Thread(selector1, "Selector 1");
            Thread thread2 = new Thread(selector2, "Selector 2");
            
            thread1.start();
            thread2.start();
            
            Thread.sleep(TEST_TIME_SECONDS * 1000);
            
            selector1.stop();
            selector2.stop();
            
            thread1.join();
            thread2.join();
            
        } finally {
            connection.close();
        }
    }
}
