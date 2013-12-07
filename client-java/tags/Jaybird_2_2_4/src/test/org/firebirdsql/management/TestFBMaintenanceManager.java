package org.firebirdsql.management;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.jdbc.AbstractConnection;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscTrHandle;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSType;

/** 
 * Test the FBMaintenanceManager class
 */
public class TestFBMaintenanceManager extends FBTestBase {

//    private FBManager fbManager;

    private FBMaintenanceManager maintenanceManager;

    public static final String DEFAULT_TABLE = ""
        + "CREATE TABLE TEST ("
        + "     TESTVAL INTEGER NOT NULL"
        + ")";

    public static final String DIALECT3_TABLE = ""
        + "CREATE TABLE DIALECTTHREE ("
        + "     TESTVAL TIME NOT NULL"
        + ")";

    public TestFBMaintenanceManager(String name) throws Exception {
        super(name);
        Class.forName("org.firebirdsql.jdbc.FBDriver");
    }

    protected void setUp() throws Exception {
        super.setUp();
        
//        fbManager = createFBManager();
//        
//        String gdsType = getProperty("test.gds_type", "PURE_JAVA");
//        
//        if (!"EMBEDDED".equalsIgnoreCase(gdsType) && !"LOCAL".equalsIgnoreCase(gdsType)) {
//            fbManager.setServer(DB_SERVER_URL);
//            fbManager.setPort(DB_SERVER_PORT);
//        }
//
//        fbManager.start();
//
//        fbManager.setForceCreate(true);
//        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        maintenanceManager = new FBMaintenanceManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            maintenanceManager.setHost(DB_SERVER_URL);
            maintenanceManager.setPort(DB_SERVER_PORT);
        }
        
        maintenanceManager.setUser(DB_USER);
        maintenanceManager.setPassword(DB_PASSWORD);
        maintenanceManager.setDatabase(getDatabasePath());
        maintenanceManager.setLogger(System.out);
    }

    private void createTestTable() throws SQLException {
        createTestTable(DEFAULT_TABLE);
    }

    private void createTestTable(String tableDef) throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(tableDef);
        } finally {
            conn.close();
        }
    }

    protected void tearDown() throws Exception {
//        fbManager.stop();
        super.tearDown();
    }
    
    public void testSetModeReadOnly() throws Exception {
        createTestTable();
        Connection conn = getConnectionViaDriverManager();
        try {
            Statement stmt = conn.createStatement();

            // In read-write mode by default
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
            conn.close();

            // Try read-only mode
            maintenanceManager.setDatabaseAccessMode(
                    MaintenanceManager.ACCESS_MODE_READ_ONLY);
            
            conn = getConnectionViaDriverManager();
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM TEST");
            assertTrue("SELECT should succeed while in read-only mode", 
                    resultSet.next());
            try {
                stmt.executeUpdate("INSERT INTO TEST VALUES (2)");
                fail("INSERT should fail when database is in read-only mode");
            } catch (SQLException e1){ 
                // Ignore
            }
        } finally {
            conn.close();
        }
    }

    public void testSetModeReadWrite() throws Exception {
        createTestTable();
        
        Connection conn = null;
        try {
            maintenanceManager.setDatabaseAccessMode(
                    MaintenanceManager.ACCESS_MODE_READ_ONLY);

            maintenanceManager.setDatabaseAccessMode(
                    MaintenanceManager.ACCESS_MODE_READ_WRITE);

            conn = getConnectionViaDriverManager();
            Statement stmt = conn.createStatement();

            // This has to fail unless the db is read-write
            stmt.executeUpdate("INSERT INTO TEST VALUES (3)");

        } finally {
            if (conn != null){
                conn.close();
            }
        }
    }

    public void testSetAccessModeWithBadMode() throws Exception {
        try {
            maintenanceManager.setDatabaseAccessMode(
                    MaintenanceManager.ACCESS_MODE_READ_ONLY 
                        | MaintenanceManager.ACCESS_MODE_READ_WRITE);
            fail("Access mode must be either read-only or read-write");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }
   
    public void testSetDialectOne() throws Exception {
        createTestTable();
        maintenanceManager.setDatabaseDialect(1);
        try {
            createTestTable(DIALECT3_TABLE);
            fail("Dialect-3 table must fail if the dialect is 1");
        } catch (SQLException e){
            // Ignore
        }
    }

    public void testSetDialectThree() throws Exception {
        maintenanceManager.setDatabaseDialect(1);
        maintenanceManager.setDatabaseDialect(3);

        // Database has to be in dialect 3 to do this
        createTestTable(DIALECT3_TABLE);
    }

    public void testSetBadDialect() throws Exception {
        try {
            maintenanceManager.setDatabaseDialect(5);
            fail("Database dialect must be either 1 or 3");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }

    public void testForcedShutdown() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        String sql = "SELECT * FROM TEST";
        createTestTable();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeQuery(sql);
            maintenanceManager.shutdownDatabase(
                    MaintenanceManager.SHUTDOWN_FORCE, 0);
            try {
                stmt.executeQuery(sql);
                fail("Query must fail on an offline database");
            } catch (SQLException e){
                // Ignore
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e2){
                // Ignore this exception, which will always be thrown due 
                // to the database being shutdown
            } catch(IllegalStateException e2) {
                // Ignore this exception, which will always be thrown due 
                // to the database being shutdown
            }
        }
    }

    public void testTransactionalShutdown() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        String sql = "UPDATE TEST SET TESTVAL = 5";
        createTestTable();
        try {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.commit();
            conn.close();

            // Shutting down when no transactions are active should work
            maintenanceManager.shutdownDatabase(
                    MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
            Thread.sleep(100);
            maintenanceManager.bringDatabaseOnline();
            Thread.sleep(100);
            conn = getConnectionViaDriverManager();
            conn.setAutoCommit(false);

            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                maintenanceManager.shutdownDatabase(
                    MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
                fail("A transaction shutdown fails with open transactions "
                        + "at the end of the timeout");
            } catch (SQLException se){
                // Ignore
            }
        } finally {
            try {
                conn.close();
            } catch(SQLException ex) {
                // empty
            }
        }
    }

    public void testShutdownWithBadMode() throws Exception {
        try {
            maintenanceManager.shutdownDatabase(
                    MaintenanceManager.SHUTDOWN_ATTACH 
                        | MaintenanceManager.SHUTDOWN_TRANSACTIONAL 
                        | MaintenanceManager.SHUTDOWN_FORCE,
                    0);
            fail("Shutdown mode must be one of: SHUTDOWN_ATTACH, "
                    + "SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE");
        } catch (IllegalArgumentException e1){
            // Ignore
        }

        try {
            maintenanceManager.shutdownDatabase(0, 0);
            fail("Shutdown mode must be one of: SHUTDOWN_ATTACH, "
                    + "SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE");
        } catch (IllegalArgumentException e2){
            // Ignore
        }
    }

    public void testShutdownWithBadTimeout() throws Exception {
        try {
            maintenanceManager.shutdownDatabase(
                    MaintenanceManager.SHUTDOWN_FORCE, -1);
            fail("Shutdown timeout must be >= 0");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }

    public void testSetDefaultCacheBufferBadCount() throws Exception {
        try {
            maintenanceManager.setDefaultCacheBuffer(-1);
            fail("Default cache buffer must be a positive integer");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }

 
    public void testSetDefaultCacheBuffer() throws Exception {
        // Unfortunately, we can really just run it and see if it fails...
        maintenanceManager.setDefaultCacheBuffer(2000);
    }

    public void testSetForcedWrites() throws Exception {
        // No test we can really do other than make sure it doesn't just fail
        maintenanceManager.setForcedWrites(true);
        maintenanceManager.setForcedWrites(false);
    }

    public void testSetPageFillBadParam() throws Exception {
        try {
            maintenanceManager.setPageFill(
                    MaintenanceManager.PAGE_FILL_FULL 
                    | MaintenanceManager.PAGE_FILL_RESERVE);
            fail("page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE");
        } catch (IllegalArgumentException e1){
            // Ignore
        }

        try {
            maintenanceManager.setPageFill(
                    Math.min(MaintenanceManager.PAGE_FILL_FULL,
                        MaintenanceManager.PAGE_FILL_RESERVE) - 1);
            fail("page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE");
        } catch  (IllegalArgumentException e2){
            // Ignore
        }
    }

    public void testSetPageFill() throws Exception {
        // Just make sure it runs without an exception
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_FULL);
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_RESERVE);
    }

    public void testMarkCorruptRecords() throws Exception {
        // ensure that our maintenance manager has exclusive connection
        fbManager.stop();
        try {
            // Just make sure it runs without an exception
            maintenanceManager.markCorruptRecords();
        } finally {
            fbManager.start();
        }
    }

    public void testValidateDatabase() throws Exception {
        // ensure that our maintenance manager has exclusive connection
        fbManager.stop();
        try {
            // Just make sure it runs without an exception
            maintenanceManager.validateDatabase();
        } finally {
            fbManager.start();
        }
    }

    public void testValidateDatabaseBadParam() throws Exception {
        try {
            maintenanceManager.validateDatabase(
                    (MaintenanceManager.VALIDATE_READ_ONLY
                    | MaintenanceManager.VALIDATE_FULL
                    | MaintenanceManager.VALIDATE_IGNORE_CHECKSUM) * 2);
            fail("Validation options must be either 0, read-only, or full");
        } catch (IllegalArgumentException e1){
            // Ignore
        }

        try {
            maintenanceManager.validateDatabase(
                    MaintenanceManager.VALIDATE_READ_ONLY 
                    | MaintenanceManager.VALIDATE_FULL);
            fail("Validation must be either 0, read-only, or full");

        } catch (IllegalArgumentException e2){
            // Ignore
        }

        try {
            maintenanceManager.validateDatabase(
                    MaintenanceManager.VALIDATE_FULL / 2);
            fail("Validation must be either 0, read-only, or full");
        } catch (IllegalArgumentException e3){
            // Ignore
        }

        try {
            maintenanceManager.validateDatabase(-1);
            fail("Validation must be either 0, read-only, or full");
        } catch (IllegalArgumentException e4){
            // Ignore
        }
    }

    public void testValidateDatabaseFull() throws Exception {
        // ensure that our maintenance manager has exclusive connection
        fbManager.stop();
        try {
            // Just run to make sure it doesn't fail
            maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_FULL);
        } finally {
            fbManager.start();
        }
    }

    public void testSetSweepThresholdBadParams() throws Exception {
        try {
            maintenanceManager.setSweepThreshold(-1);
            fail("Sweep threshold must be positive");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }
    public void testSetSweepThreshold() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.setSweepThreshold(0);
        maintenanceManager.setSweepThreshold(2000);
    }

    public void testSweepDatabase() throws Exception {
        // Just run it to see if it throws an exception 
        maintenanceManager.sweepDatabase();
    }

    public void testActivateShadowFile() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.activateShadowFile();
    }

    public void testKillUnavailableShadows() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.killUnavailableShadows();
    }

    public void testListLimboTransactions() throws Exception {
        final int COUNT_LIMBO = 5;
        createLimboTransaction(COUNT_LIMBO);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        maintenanceManager.setLogger(byteOut);
        maintenanceManager.listLimboTransactions();
        
        StringTokenizer limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(COUNT_LIMBO, limboTransactions.countTokens());
    }

    public void testGetLimboTransactions() throws Exception {
        final int COUNT_LIMBO = 5;
        createLimboTransaction(COUNT_LIMBO);
        int[] limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(COUNT_LIMBO, limboTransactions.length);
    }

    
    public void testRollbackLimboTransaction() throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        maintenanceManager.setLogger(byteOut);
        maintenanceManager.listLimboTransactions();
        StringTokenizer limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(0, limboTransactions.countTokens());
        createLimboTransaction(3);
        byteOut.reset();
        maintenanceManager.listLimboTransactions();
        limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(3, limboTransactions.countTokens());
        if (limboTransactions.hasMoreTokens()) {
            int trId = Integer.parseInt(limboTransactions.nextToken());
            maintenanceManager.rollbackTransaction(trId);
        }
        else fail("There should be 3 limbo transactions.");
        byteOut.reset();
        maintenanceManager.listLimboTransactions();
        limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(2, limboTransactions.countTokens());
    }

    public void testCommitLimboTransaction() throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        maintenanceManager.setLogger(byteOut);
        maintenanceManager.listLimboTransactions();
        StringTokenizer limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(0, limboTransactions.countTokens());
        createLimboTransaction(3);
        byteOut.reset();
        maintenanceManager.listLimboTransactions();
        limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(3, limboTransactions.countTokens());
        if (limboTransactions.hasMoreTokens()) {
            int trId = Integer.parseInt(limboTransactions.nextToken());
            maintenanceManager.commitTransaction(trId);
        }
        else fail("There should be 3 limbo transactions.");
        byteOut.reset();
        maintenanceManager.listLimboTransactions();
        limboTransactions = new StringTokenizer(byteOut.toString(),"\n");
        assertEquals(2, limboTransactions.countTokens());
    }


    private void createLimboTransaction(int count) throws Exception {
        AbstractConnection conn = (AbstractConnection)getConnectionViaDriverManager();
        try {
            GDS gds = conn.getInternalAPIHandler();
            DatabaseParameterBuffer dpb = gds.createDatabaseParameterBuffer();
            dpb.addArgument(DatabaseParameterBuffer.USER, DB_USER);
            dpb.addArgument(DatabaseParameterBuffer.PASSWORD, DB_PASSWORD);
            IscDbHandle dbh = gds.createIscDbHandle();
            gds.iscAttachDatabase(getdbpath(DB_NAME), dbh, dpb);
            for (int i = 0; i < count; i++){
                TransactionParameterBuffer tpBuf = 
                    gds.newTransactionParameterBuffer();
                IscTrHandle trh = gds.createIscTrHandle();
                gds.iscStartTransaction(trh, dbh, tpBuf);
                gds.iscPrepareTransaction(trh);
            }
            gds.iscDetachDatabase(dbh);
        } finally {
            conn.close();
        }
    }

}
