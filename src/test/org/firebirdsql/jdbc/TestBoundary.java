package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;

public class TestBoundary extends FBTestBase {

    private static final String CREATE_META_ONE = 
            "CREATE TABLE COMMUNICATIONS_FIT ( \n"
            + "ID INTEGER NOT NULL, \n" 
            + "GUIDID CHAR(16), \n"
            + "NAME VARCHAR(64) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n"
            + "SDESC VARCHAR(256) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n"
            + "LDESC BLOB SUB_TYPE 1, \n" 
            + "STATUS INTEGER, \n" 
            + "PRIMARY KEY (ID) \n" 
            + ") \n";

    private static final String DROP_META_ONE = "DROP TABLE COMMUNICATIONS_FIT";

    public TestBoundary(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Connection connection = getConnectionViaDriverManager();
        try {
            executeDropTable(connection, DROP_META_ONE);
            executeCreateTable(connection, CREATE_META_ONE);
        } finally {
            closeQuietly(connection);
        }
    }
    
    protected void tearDown() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            executeDropTable(connection, DROP_META_ONE);
        } finally {
            closeQuietly(connection);
            super.tearDown();
        }
    }

    public void testLockUp() throws Exception {
        final Results results = createResults();
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    performLockupSequence();
                    results.setIsOk();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, "Lockup test thread.");
        thread.start();
        
        assertTrue("Operation should have completed by now", results.waitForCompletionOrTimeout());
    }

    public void testNoLockUp() throws Exception {
        final Results results = createResults();
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    performSimilarButNoLockupSequence();
                    results.setIsOk();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, "Lockup test thread.");
        thread.start();

        assertTrue("Operation should have completed by now", results.waitForCompletionOrTimeout());
    }

    private synchronized Results createResults() {
        return new Results();
    }

    private static class Results {
        
        private boolean isOk = false;

        private synchronized void setIsOk() {
            isOk = true;
            notifyAll();
        }

        private synchronized boolean waitForCompletionOrTimeout() {
            final long startTime = System.currentTimeMillis();

            while (isOk == false && (System.currentTimeMillis() - startTime) < 10000) {
                try {
                    wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return isOk;
        }
    }

    private void performLockupSequence() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            final PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO COMMUNICATIONS_FIT ( \n " 
                    + "GUIDID, \n" 
                    + "NAME, \n"
                    + "SDESC, \n" 
                    + "LDESC, \n"
                    + "STATUS, \n"
                    + "ID \n" 
                    + ") \n" 
                    + "VALUES ( ?, ?, ?, ?, ?, ? ) \n");
            statement.clearParameters();
            
            byte[] guid = new byte[16];
            for (int i = 0; i < guid.length; i++) {
                guid[i] = (byte) (i + 65);
            }
            statement.setBytes(1, guid);
            statement.setString(2, "Further");
            statement.setString(3, "Further infomation field");
            statement.setString(4, "Field to provide Further infomation capture");
            statement.setInt(5, 2);
            statement.setInt(6, 1);
            statement.executeUpdate(); // <---- WE WILL LOCK
        } finally {
            closeQuietly(conn);
        }
    }

    private void performSimilarButNoLockupSequence() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            final PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO COMMUNICATIONS_FIT ( \n "
                  //+ "GUIDID, \n" 
                    + "NAME, \n"
                    + "SDESC, \n"
                    + "LDESC, \n" 
                    + "STATUS, \n"
                    + "ID \n" 
                    + ") \n" 
                    + "VALUES ( ?, ?, ?, ?, ? ) \n");
    
            statement.clearParameters();
            // statement.setBytes( 1, new byte[16] );
            statement.setString(1, "Further");
            statement.setString(2, "Further infomation field");
            statement.setString(3, "Field to provide Further infomation capture");
            statement.setInt(4, 2);
            statement.setInt(5, 1);
            statement.executeUpdate(); // <---- WE WILL LOCK
        } finally {
            closeQuietly(conn);
        }
    }

}
