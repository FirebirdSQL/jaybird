package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestBoundary
    extends BaseFBTest {
    final String CREATE_META_ONE = "CREATE TABLE COMMUNICATIONS_FIT ( \n" +
        "ID INTEGER NOT NULL, \n" +
        "GUIDID CHAR(16), \n" +
        "NAME VARCHAR(64) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n" +
        "SDESC VARCHAR(256) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n" +
        "LDESC BLOB SUB_TYPE 1, \n" +
        "STATUS INTEGER, \n" +
        "PRIMARY KEY (ID) \n" +
        ") \n";

    final String DROP_META_ONE = "DROP TABLE COMMUNICATIONS_FIT";

    public TestBoundary(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(FBDriver.class.getName());
    }

    protected void tearDown() throws Exception {
        // super.tearDown();
    }

    public void testLockUp() throws Exception {
        Connection connection =
            DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);

        connection.setAutoCommit(false);

        tryDrop(connection, DROP_META_ONE);

        connection.commit();

        final Statement stmnt =
            connection.createStatement();
        try {
            stmnt.executeUpdate(CREATE_META_ONE);
            connection.commit();

        }
        finally {
            stmnt.close();
            connection.close();
        }

        final Results results = createResults();

        final Thread thread = new Thread(
            new Runnable() {
                public void run() {
                    try {
                        performLockupSequence();
    
                        results.setIsOk();
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, 
            "Lockup test thread."
        );

        thread.start();
        //thread.run();

        assertTrue("Operation should have completed by now",
                   results.waitForCompletionOrTimeout());
    }

    public void testNoLockUp() throws Exception {
        Connection connection =
            DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);

        connection.setAutoCommit(false);

        tryDrop(connection, DROP_META_ONE);

        connection.commit();

        final Statement stmnt =
            connection.createStatement();
        try {
            stmnt.executeUpdate(CREATE_META_ONE);
            connection.commit();

        }
        finally {
            stmnt.close();
            connection.close();
        }

        final Results results = createResults();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    performSimilarButNoLockupSequence();

                    results.setIsOk();
                }
                catch (SQLException e) {
                    e.printStackTrace(); //To change body of catch statement use Options | File Templates.
                }
            }
        }

        , "Lockup test thread.");

        thread.start();

        assertTrue("Operation should have completed by now",
                   results.waitForCompletionOrTimeout());
    }

    private synchronized Results createResults() {
        return new Results();
    }

    class Results {
        synchronized void setIsOk() {
            isOk = true;
            notifyAll();
        }

        synchronized boolean waitForCompletionOrTimeout() {
            final long startTime =
                System.currentTimeMillis();

            while (isOk == false &&
                   (System.currentTimeMillis() - startTime) < 10000) {
                try {
                    wait(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace(); //To change body of catch statement use Options | File Templates.
                }
            }
            return isOk;
        }

        private boolean isOk = false;
    }

    private void performLockupSequence() throws
        SQLException {
        Connection conn =
            DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);

        final PreparedStatement statement =
            conn.prepareStatement("INSERT INTO COMMUNICATIONS_FIT ( \n " +
                                  "GUIDID, \n" +
                                  "NAME, \n" +
                                  "SDESC, \n" +
                                  "LDESC, \n" +
                                  "STATUS, \n" +
                                  "ID \n" +
                                  ") \n" +
                                  "VALUES ( ?, ?, ?, ?, ?, ? ) \n");
        statement.clearParameters();
        
        byte[] guid = new byte[16];
        
        for (int i = 0; i < guid.length; i++) {
            guid[i] = (byte)(i + 65);
        }
        

        statement.setBytes(1, guid);
        statement.setString(2, "Further");
        statement.setString(3, "Further infomation field");
        statement.setString(4, "Field to provide Further infomation capture");
        statement.setInt(5, 2);
        statement.setInt(6, 1);

        statement.executeUpdate(); // <---- WE WILL LOCKL

        conn.close();
    }

    private void performSimilarButNoLockupSequence() throws SQLException {
        Connection conn =
            DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);

        final PreparedStatement statement =
            conn.prepareStatement("INSERT INTO COMMUNICATIONS_FIT ( \n " +
                                 // "GUIDID, \n" +
                                  "NAME, \n" +
                                  "SDESC, \n" +
                                  "LDESC, \n" +
                                  "STATUS, \n" +
                                  "ID \n" +
                                  ") \n" +

                                  "VALUES ( ?, ?, ?, ?, ? ) \n");
        statement.clearParameters();

        // statement.setBytes( 1, new byte[16] );
        statement.setString(1, "Further");
        statement.setString(2, "Further infomation field");
        statement.setString(3, "Field to provide Further infomation capture");
        statement.setInt(4, 2);
        statement.setInt(5, 1);

        statement.executeUpdate(); // <---- WE WILL LOCKL

        conn.close();
    }

    private void tryDrop(Connection connection, String
                         sql) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        catch (SQLException sqlex) {
// do nothing
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException sqlex) {
// do nothing
                }
            }
        }
    }
}
