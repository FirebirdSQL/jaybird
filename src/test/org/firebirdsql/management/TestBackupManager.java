package org.firebirdsql.management;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.GDSType;


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
        fbManager.setServer("localhost");
        fbManager.start();

        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        GDSType gdsType = GDSType.getType(System.getProperty("test.gds_type"));
        backupManager = new FBBackupManager(gdsType);
        backupManager.setHost("localhost");
        backupManager.setUser("SYSDBA");
        backupManager.setPassword("masterkey");
        backupManager.setDatabase(getDatabasePath());
        backupManager.setBackupPath(getBackupPath());
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    private String getDatabasePath() {
        return  DB_PATH + "/" + DB_NAME;
    }
    
    private String getBackupPath() {
        return DB_PATH + "/" + DB_NAME + ".fbk";
    }
    
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            return DriverManager.getConnection(
                "jdbc:firebirdsql:localhost:" + getDatabasePath(), 
                DB_USER, DB_PASSWORD);
        } catch(ClassNotFoundException ex) {
            throw new SQLException("JayBird not found.");
        }
    }

    private void createTestTable() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(TEST_TABLE);
        stmt.close();
        conn.close();
    }

    protected void tearDown() throws Exception {
        try { 
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
            fbManager.stop();
        } catch (Exception e){
            e.printStackTrace();
        }
        
        super.tearDown();
    }
    
    public void testBackup() throws Exception {
        
        backupManager.backupDatabase();
        
        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        
        try {
            Connection c = getConnection();
            c.close();
            fail("Should not be able to connect to a dropped database");
        } catch(SQLException ex) {
            // ignore
        }

        System.out.println();
        backupManager.restoreDatabase();
        
        Connection c = getConnection();
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
            conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
            conn.close();

            backupManager.backupDatabase();
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
            backupManager.setRestoreReadOnly(true);
            backupManager.restoreDatabase();

            conn = getConnection();
            stmt = conn.createStatement();
            try {
                stmt.executeUpdate("INSERT INTO TEST VALUES (2)");
                fail("Not possible to insert data in a read-only database");
            } catch (SQLException e){
                // Ignore
            }

            conn.close();

            backupManager.setRestoreReadOnly(false);
            backupManager.setRestoreCreate(false);
            backupManager.restoreDatabase();
            conn = getConnection();
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
        backupManager.setRestoreCreate(true);
        try {
            backupManager.restoreDatabase();
            fail("Can't restore-create an existing database");
        } catch (SQLException e){
            // Ignore
        }
        
        backupManager.setRestoreCreate(false);
        backupManager.restoreDatabase();
    }

}
