package org.firebirdsql.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.GDSType;


/**
 * 
 */
public class TestBackupManager extends FBTestBase {

    /**
     * @param name
     */
    public TestBackupManager(String name) {
        super(name);
    }

    private FBManager fbManager;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        fbManager = createFBManager();
        fbManager.setServer("localhost");
//        fbManager.setPort(3060);
        fbManager.start();

        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        
    }
    private String getDatabasePath() {
        return DB_PATH + "/" + DB_NAME + ".fdb";
    }
    
    private String getBackupPath() {
        return DB_PATH + "/" + DB_NAME + ".fbk";
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:firebirdsql:localhost:" + getDatabasePath(), 
            DB_USER, DB_PASSWORD);
    }

    protected void tearDown() throws Exception {
        
//        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        fbManager.stop();
        
        super.tearDown();
    }
    
    public void testBla() {
        
    }
    
    public void testBackup() throws Exception {
        BackupManager backupManager = new BackupManager(GDSType.PURE_JAVA);
        backupManager.setHost("localhost");
//        backupManager.setPort(3060);
        backupManager.setUser("SYSDBA");
        backupManager.setPassword("masterkey");
        
        
        backupManager.setDatabase(getDatabasePath());
        backupManager.setBackupPath(getBackupPath());
//        backupManager.setDatabase("c:/database/employee.fdb");
//        backupManager.setBackupPath("c:/testdb.gbk");
        backupManager.setLogger(System.out);
        backupManager.backupDatabase(true);
        
        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        
        try {
            Connection c = getConnection();
            c.close();
            fail("Should not be able to connect to a dropped database");
        } catch(SQLException ex) {
            // ignore
        }
        
        System.out.println();
        
        backupManager.restoreDatabase(true);
        
        Connection c = getConnection();
        c.close();
    }
}
