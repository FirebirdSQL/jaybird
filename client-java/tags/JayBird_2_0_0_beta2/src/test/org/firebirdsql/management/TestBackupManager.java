package org.firebirdsql.management;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;


/**
 * 
 */
public class TestBackupManager extends FBTestBase {

        
    private BackupManager backupManager;

    private FBManager fbManager;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    /**
     * @param name
     */
    public TestBackupManager(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        fbManager = createFBManager();
        fbManager.setServer(DB_SERVER_URL);
        fbManager.setPort(DB_SERVER_PORT);
        fbManager.start();

        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        backupManager = new FBBackupManager();
        backupManager.setHost(DB_SERVER_URL);
        backupManager.setUser(DB_USER);
        backupManager.setPassword(DB_PASSWORD);
        backupManager.setDatabase(getDatabasePath());
        backupManager.setBackupPath(getBackupPath());
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    private String getBackupPath() {
        return DB_PATH + "/" + DB_NAME + ".fbk";
    }
    
    private void createTestTable() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(TEST_TABLE);
        stmt.close();
        conn.close();
    }

    protected void tearDown() throws Exception {
        try {
        	//Drop database.
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
            fbManager.stop();            
            //Delete backup file.            
            File file = new File(getBackupPath());
            file.delete();
            
        } catch (Exception e){
            e.printStackTrace();
        }
        super.tearDown();
    }
    
    public void testBackup() throws Exception {
        
        backupManager.backupDatabase();
        
        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        
        try {
            Connection c = getConnectionViaDriverManager();
            c.close();
            fail("Should not be able to connect to a dropped database");
        } catch(SQLException ex) {
            // ignore
        }

        System.out.println();
        backupManager.restoreDatabase();
        
        Connection c = getConnectionViaDriverManager();
        c.close();
        
        
    }

    public void testSetBadBufferCount() {
        try {
            backupManager.setRestorePageBufferCount(-1);
            fail("Page buffer count must be a positive value");
        } catch (IllegalArgumentException e){
            // Ignore
        }
        backupManager.setRestorePageBufferCount(500);
    }

    public void testSetBadPageSize() {
        try {
            backupManager.setRestorePageSize(4000);
            fail("Page size must be one of 1024, 2048, 4196 or 8192)");
        } catch (IllegalArgumentException e) {
            // Ignore
        }
        backupManager.setRestorePageSize(4196);
    }

    public void testRestoreReadOnly() throws Exception {
        createTestTable();
        Connection conn = null;
        try {
            conn = getConnectionViaDriverManager();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
            conn.close();

            backupManager.backupDatabase();
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
            backupManager.setRestoreReadOnly(true);
            backupManager.restoreDatabase();

            conn = getConnectionViaDriverManager();
            stmt = conn.createStatement();
            try {
                stmt.executeUpdate("INSERT INTO TEST VALUES (2)");
                fail("Not possible to insert data in a read-only database");
            } catch (SQLException e){
                // Ignore
            }

            conn.close();

            backupManager.setRestoreReadOnly(false);
            backupManager.setRestoreReplace(true);
            backupManager.restoreDatabase();
            conn = getConnectionViaDriverManager();
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO TEST VALUES (3)");
        } finally {
            if (conn != null){
                conn.close();
            }
        }
     }

    public void testBackupReplace() throws Exception {
        backupManager.backupDatabase();
        backupManager.setRestoreReplace(false);
        try {
            backupManager.restoreDatabase();
            fail("Can't restore-create an existing database");
        } catch (SQLException e){
            // Ignore
        }
        
        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();
    }

    public void testBackupMultiple() throws Exception {
        backupManager.clearBackupPaths();
        backupManager.clearRestorePaths();
        
        String backupPath1 =  getDatabasePath() + "-1.fbk";
        String backupPath2 =  getDatabasePath() + "-2.fbk";

        backupManager.addBackupPath(backupPath1, 2048);
        backupManager.addBackupPath(backupPath2);
        
        backupManager.backupDatabase();
        
        File file1 = new File(backupPath1);
        assertTrue("File " + backupPath1 + " should exist.", file1.exists());
        
        File file2 = new File(backupPath2);
        assertTrue("File " + backupPath2 + " should exist.", file2.exists());

        backupManager.clearBackupPaths();
        
        backupManager.addBackupPath(backupPath1);
        backupManager.addBackupPath(backupPath2);
        
        String restorePath1 = getDatabasePath() + "-1.fdb";
        String restorePath2 = getDatabasePath() + "-2.fdb";
        
        backupManager.addRestorePath(restorePath1, 10);
        backupManager.addRestorePath(restorePath2, 100);
        
        backupManager.restoreDatabase();
        
        //Remove test files from filesystem.
        File file3 = new File(restorePath1);
        assertTrue("File " + restorePath1 + " should exist.", file1.exists());
        file1.delete();
        file3.delete();        
        File file4 = new File(restorePath2);
        assertTrue("File " + restorePath2 + " should exist.", file2.exists());
        file2.delete();
        file4.delete();
    }
}
