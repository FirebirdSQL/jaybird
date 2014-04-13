package org.firebirdsql.management;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.StringTokenizer;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.*;

/**
 * Test the FBMaintenanceManager class
 */
public class TestFBMaintenanceManager extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FBMaintenanceManager maintenanceManager;

    public static final String DEFAULT_TABLE =
              "CREATE TABLE TEST ("
            + "     TESTVAL INTEGER NOT NULL"
            + ")";

    public static final String DIALECT3_TABLE =
              "CREATE TABLE DIALECTTHREE ("
            + "     TESTVAL TIME NOT NULL"
            + ")";

    @Before
    public void setUp() throws Exception {
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

    @Test
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

            expectedException.expect(SQLException.class);

            stmt.executeUpdate("INSERT INTO TEST VALUES (2)");
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
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
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetAccessModeWithBadMode() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setDatabaseAccessMode(
                MaintenanceManager.ACCESS_MODE_READ_ONLY
                    | MaintenanceManager.ACCESS_MODE_READ_WRITE);
    }

    /**
     * Dialect-3 table must fail if the dialect is 1
     */
    @Test
    public void testSetDialectOne() throws Exception {
        createTestTable();
        maintenanceManager.setDatabaseDialect(1);

        expectedException.expect(SQLException.class);

        createTestTable(DIALECT3_TABLE);
    }

    @Test
    public void testSetDialectThree() throws Exception {
        maintenanceManager.setDatabaseDialect(1);
        maintenanceManager.setDatabaseDialect(3);

        // Database has to be in dialect 3 to do this
        createTestTable(DIALECT3_TABLE);
    }

    /**
     * Database dialect must be either 1 or 3
     */
    @Test
    public void testSetBadDialect() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setDatabaseDialect(5);
    }

    /**
     * Query must fail on an offline database
     */
    @Test
    public void testForcedShutdown() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            String sql = "SELECT * FROM TEST";
            createTestTable();

            Statement stmt = conn.createStatement();
            stmt.executeQuery(sql);
            maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_FORCE, 0);

            expectedException.expect(SQLException.class);

            stmt.executeQuery(sql);
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * A transaction shutdown fails with open transactions at the end of the timeout
     */
    @Test
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
            maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
            Thread.sleep(100);
            maintenanceManager.bringDatabaseOnline();
            Thread.sleep(100);
            conn = getConnectionViaDriverManager();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            expectedException.expect(SQLException.class);

            maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Shutdown mode must be one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE
     */
    @Test
    public void testShutdownWithBadMode_1() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.shutdownDatabase(
                MaintenanceManager.SHUTDOWN_ATTACH
                    | MaintenanceManager.SHUTDOWN_TRANSACTIONAL
                    | MaintenanceManager.SHUTDOWN_FORCE,
                0);
    }

    /**
     * Shutdown mode must be one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE
     */
    @Test
    public void testShutdownWithBadMode_2() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.shutdownDatabase(0, 0);
    }

    /**
     * Shutdown timeout must be >= 0
     */
    @Test
    public void testShutdownWithBadTimeout() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_FORCE, -1);
    }

    /**
     * Default cache buffer must be a positive integer
     */
    @Test
    public void testSetDefaultCacheBufferBadCount() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setDefaultCacheBuffer(-1);
    }

    @Test
    public void testSetDefaultCacheBuffer() throws Exception {
        // Unfortunately, we can really just run it and see if it fails...
        maintenanceManager.setDefaultCacheBuffer(2000);
    }

    @Test
    public void testSetForcedWrites() throws Exception {
        // No test we can really do other than make sure it doesn't just fail
        maintenanceManager.setForcedWrites(true);
        maintenanceManager.setForcedWrites(false);
    }

    /**
     * page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE
     */
    @Test
    public void testSetPageFillBadParam_1() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setPageFill(
                MaintenanceManager.PAGE_FILL_FULL
                    | MaintenanceManager.PAGE_FILL_RESERVE);
    }

    /**
     * page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE
     */
    @Test
    public void testSetPageFillBadParam_2() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setPageFill(
                Math.min(MaintenanceManager.PAGE_FILL_FULL,
                        MaintenanceManager.PAGE_FILL_RESERVE) - 1);
    }

    @Test
    public void testSetPageFill() throws Exception {
        // Just make sure it runs without an exception
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_FULL);
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_RESERVE);
    }

    @Test
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

    @Test
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

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    public void testValidateDatabaseBadParam_1() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.validateDatabase(
                (MaintenanceManager.VALIDATE_READ_ONLY
                    | MaintenanceManager.VALIDATE_FULL
                    | MaintenanceManager.VALIDATE_IGNORE_CHECKSUM) * 2);
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    public void testValidateDatabaseBadParam_2() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.validateDatabase(
                MaintenanceManager.VALIDATE_READ_ONLY
                    | MaintenanceManager.VALIDATE_FULL);
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    public void testValidateDatabaseBadParam_3() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_FULL / 2);
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    public void testValidateDatabaseBadParam_4() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.validateDatabase(-1);
    }

    @Test
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

    /**
     * Sweep threshold must be positive
     */
    @Test
    public void testSetSweepThresholdBadParams() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        maintenanceManager.setSweepThreshold(-1);
    }

    @Test
    public void testSetSweepThreshold() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.setSweepThreshold(0);
        maintenanceManager.setSweepThreshold(2000);
    }

    @Test
    public void testSweepDatabase() throws Exception {
        // Just run it to see if it throws an exception 
        maintenanceManager.sweepDatabase();
    }

    @Test
    public void testActivateShadowFile() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.activateShadowFile();
    }

    @Test
    public void testKillUnavailableShadows() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.killUnavailableShadows();
    }

    @Test
    public void testListLimboTransactions() throws Exception {
        final int COUNT_LIMBO = 5;
        createLimboTransaction(COUNT_LIMBO);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        maintenanceManager.setLogger(byteOut);
        maintenanceManager.listLimboTransactions();

        StringTokenizer limboTransactions = new StringTokenizer(byteOut.toString(), "\n");
        assertEquals(COUNT_LIMBO, limboTransactions.countTokens());
    }

    @Test
    public void testGetLimboTransactions() throws Exception {
        final int COUNT_LIMBO = 5;
        createLimboTransaction(COUNT_LIMBO);
        int[] limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(COUNT_LIMBO, limboTransactions.length);
    }

    @Test
    public void testRollbackLimboTransaction() throws Exception {
        List<Integer> limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(0, limboTransactions.size());

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(3, limboTransactions.size());

        int trId = limboTransactions.get(0);
        maintenanceManager.rollbackTransaction(trId);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(2, limboTransactions.size());
    }

    @Test
    public void testCommitLimboTransaction() throws Exception {
        List<Integer> limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(0, limboTransactions.size());

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(3, limboTransactions.size());

        int trId = limboTransactions.get(0);
        maintenanceManager.commitTransaction(trId);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(2, limboTransactions.size());
    }

    private void createLimboTransaction(int count) throws Exception {
        FBConnection conn = (FBConnection) getConnectionViaDriverManager();
        try {
            GDS gds = conn.getInternalAPIHandler();
            DatabaseParameterBuffer dpb = gds.createDatabaseParameterBuffer();
            dpb.addArgument(DatabaseParameterBuffer.USER, DB_USER);
            dpb.addArgument(DatabaseParameterBuffer.PASSWORD, DB_PASSWORD);
            IscDbHandle dbh = gds.createIscDbHandle();
            gds.iscAttachDatabase(getdbpath(DB_NAME), dbh, dpb);
            for (int i = 0; i < count; i++) {
                TransactionParameterBuffer tpBuf = gds.newTransactionParameterBuffer();
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
