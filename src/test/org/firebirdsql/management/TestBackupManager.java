package org.firebirdsql.management;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBConnection;

/**
 * TODO: This test assumes it is run against localhost 
 */
public class TestBackupManager extends FBTestBase {
        
    private BackupManager backupManager;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    /**
     * @param name
     */
    public TestBackupManager(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        backupManager = new FBBackupManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            backupManager.setHost(DB_SERVER_URL);
            backupManager.setPort(DB_SERVER_PORT);
        }
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
            try {
                // Drop database.
                super.tearDown();
            } finally {
                // Delete backup file.
                File file = new File(getBackupPath());
                file.delete();
            }

        } catch (FileNotFoundException e) {
            // ignore
        }
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
            fail("Page size must be one of 1024, 2048, 4196, 8192 or 16384)");
        } catch (IllegalArgumentException e) {
            // Ignore
        }
        backupManager.setRestorePageSize(4096);
    }
    
    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    public void testValidPageSizes() {
        final int[] pageSizes = {1024, 2048, 4096, 8192, 16384};
        for (int pageSize : pageSizes) {
            backupManager.setRestorePageSize(pageSize);
        }
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
            closeQuietly(conn);
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
    
    /**
     * Test if restoring a database to page size 16384 works.
     */
    public void testRestorePageSize16384() throws Exception {
        backupManager.backupDatabase();
        
        backupManager.setRestoreReplace(true);
        backupManager.setRestorePageSize(16384);
        backupManager.restoreDatabase();
        
        Connection con = null;
        try {
            con = getConnectionViaDriverManager();
            GDSHelper gdsHelper = ((FBConnection)con).getGDSHelper();
            if (gdsHelper.compareToVersion(2, 1) < 0) {
                // No support for MON$DATABASE, skipping test for actual page size
                return;
            }
            
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MON$PAGE_SIZE FROM MON$DATABASE");
            if (rs.next()) {
                assertEquals("Expected restored database to have pagesize 16384", 16384, rs.getInt(1));
            } else {
                fail("Expected at least one record");
            }
        } finally {
            closeQuietly(con);
        }
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
