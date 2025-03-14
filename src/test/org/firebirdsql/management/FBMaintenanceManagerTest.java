/*
 SPDX-FileCopyrightText: Copyright 2004-2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2005-2011 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.common.extension.RunEnvironmentExtension.EnvironmentRequirement;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.OdsVersion;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FirebirdDatabaseMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.TestAbortedException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCode;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test the FBMaintenanceManager class
 */
class FBMaintenanceManagerTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private FBMaintenanceManager maintenanceManager;

    //@formatter:off
    private static final String DEFAULT_TABLE =
              "CREATE TABLE TEST ("
            + "     TESTVAL INTEGER NOT NULL"
            + ")";

    private static final String DIALECT3_TABLE =
              "CREATE TABLE DIALECTTHREE ("
            + "     TESTVAL TIME NOT NULL"
            + ")";
    // @formatter:on

    @BeforeEach
    void setUp() {
        maintenanceManager = configureDefaultServiceProperties(new FBMaintenanceManager(getGdsType()));
        maintenanceManager.setDatabase(getDatabasePath());
        /* NOTE:
         1) Setting parallel workers unconditionally, but actual support was introduced in Firebird 5.0;
         2) There is no way to verify if we're actually setting it (we're more testing that the implementation doesn't
            set it for options or versions which don't support it, than testing if it gets set);
         3) The actual usage of the configured value is determined per option
        */
        maintenanceManager.setParallelWorkers(2);
        maintenanceManager.setLogger(System.out);
    }

    private void createTestTable() throws SQLException {
        createTestTable(DEFAULT_TABLE);
    }

    private void createTestTable(String tableDef) throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(tableDef);
        }
    }

    @Test
    void testSetModeReadOnly() throws Exception {
        createTestTable();

        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            // In read-write mode by default
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
        }

        // Try read-only mode
        maintenanceManager.setDatabaseAccessMode(MaintenanceManager.ACCESS_MODE_READ_ONLY);

        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM TEST");
            assertTrue(resultSet.next(), "SELECT should succeed while in read-only mode");

            SQLException exception = assertThrows(SQLException.class,
                    () -> stmt.executeUpdate("INSERT INTO TEST VALUES (2)"));
            assertThat(exception, errorCodeEquals(ISCConstants.isc_read_only_database));
        }
    }

    @Test
    void testSetModeReadWrite() throws Exception {
        createTestTable();

        maintenanceManager.setDatabaseAccessMode(MaintenanceManager.ACCESS_MODE_READ_ONLY);
        maintenanceManager.setDatabaseAccessMode(MaintenanceManager.ACCESS_MODE_READ_WRITE);

        try(Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();

            // This has to fail unless the db is read-write
            assertDoesNotThrow(() -> stmt.executeUpdate("INSERT INTO TEST VALUES (3)"));
        }
    }

    @Test
    void testSetAccessModeWithBadMode() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceManager.setDatabaseAccessMode(
                        MaintenanceManager.ACCESS_MODE_READ_ONLY | MaintenanceManager.ACCESS_MODE_READ_WRITE));
    }

    /**
     * Dialect-3 table must fail if the dialect is 1
     */
    @Test
    void testSetDialectOne() throws Exception {
        createTestTable();
        maintenanceManager.setDatabaseDialect(1);

        SQLException exception = assertThrows(SQLException.class, () -> createTestTable(DIALECT3_TABLE));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_sql_db_dialect_dtype_unsupport));
    }

    @Test
    void testSetDialectThree() throws Exception {
        maintenanceManager.setDatabaseDialect(1);
        maintenanceManager.setDatabaseDialect(3);

        // Database has to be in dialect 3 to do this
        assertDoesNotThrow(() -> createTestTable(DIALECT3_TABLE));
    }

    /**
     * Database dialect must be either 1 or 3
     */
    @Test
    void testSetBadDialect() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setDatabaseDialect(5));
    }

    /**
     * Query must fail on an offline database.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testForcedShutdown(boolean autoCommit) throws Exception {
        assumeThat("Test doesn't work correctly under embedded", GDS_TYPE, not(isEmbeddedType()));
        try (Connection conn = getConnectionViaDriverManager()) {
            createTestTable();

            conn.setAutoCommit(autoCommit);
            Statement stmt = conn.createStatement();
            try {
                String sql = "SELECT * FROM TEST";
                stmt.executeQuery(sql);
                maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_FORCE, 0);

                SQLException exception = assertThrows(SQLException.class, () -> stmt.executeQuery(sql));
                assertThat(exception, errorCode(oneOf(
                        ISCConstants.isc_shutdown, ISCConstants.isc_att_shutdown, ISCConstants.isc_net_read_err)));
            } finally {
                closeQuietly(stmt);
            }
        }
    }

    /**
     * A transaction shutdown fails with open transactions at the end of the timeout
     */
    @Test
    void testTransactionalShutdown() throws Exception {
        String sql = "UPDATE TEST SET TESTVAL = 5";
        try (var conn = getConnectionViaDriverManager()) {
            createTestTable();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.commit();
        }

        // Shutting down when no transactions are active should work
        maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
        Thread.yield();
        maintenanceManager.bringDatabaseOnline();
        Thread.yield();

        try (var conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);

            var stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            var exception = assertThrows(SQLException.class,
                    () -> maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0));
            assertThat(exception, errorCodeEquals(ISCConstants.isc_shutfail));
        }
    }

    /**
     * Shutdown mode must be one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE
     */
    @Test
    void testShutdownWithBadMode_1() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceManager.shutdownDatabase(
                        MaintenanceManager.SHUTDOWN_ATTACH
                                | MaintenanceManager.SHUTDOWN_TRANSACTIONAL
                                | MaintenanceManager.SHUTDOWN_FORCE,
                        0));
    }

    /**
     * Shutdown mode must be one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE
     */
    @Test
    void testShutdownWithBadMode_2() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.shutdownDatabase(0, 0));
    }

    /**
     * Shutdown timeout must be >= 0
     */
    @Test
    void testShutdownWithBadTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_FORCE, -1));
    }

    /**
     * Default cache buffer must not be a negative integer
     */
    @Test
    void testSetDefaultCacheBufferNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setDefaultCacheBuffer(-1));
    }

    @Test
    void testSetDefaultCacheBufferTooLow() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setDefaultCacheBuffer(49));
    }

    @Test
    void testSetDefaultCacheBuffer() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test needs access to monitoring tables");

        final int bufferSize = 50;
        maintenanceManager.setDefaultCacheBuffer(bufferSize);

        assertBufferSize(bufferSize);
    }

    @Test
    void testSetDefaultCacheBufferZeroResetsValue() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test needs access to monitoring tables");

        final int systemDefaultBuffer;
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select MON$PAGE_BUFFERS from MON$DATABASE")) {
            assertTrue(rs.next());
            systemDefaultBuffer = rs.getInt(1);
        }

        final int bufferSize = systemDefaultBuffer + 50;
        maintenanceManager.setDefaultCacheBuffer(bufferSize);
        assertBufferSize(bufferSize);

        maintenanceManager.setDefaultCacheBuffer(0);
        assertBufferSize(systemDefaultBuffer);
    }

    private void assertBufferSize(int bufferSize) throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select MON$PAGE_BUFFERS from MON$DATABASE")) {
            assertTrue(rs.next());
            assertEquals(bufferSize, rs.getInt(1));
        }
    }

    @Test
    void testSetForcedWrites() {
        // No test we can really do other than make sure it doesn't just fail
        assertDoesNotThrow(() -> maintenanceManager.setForcedWrites(true));
        assertDoesNotThrow(() -> maintenanceManager.setForcedWrites(false));
    }

    /**
     * page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE
     */
    @Test
    void testSetPageFillBadParam_1() {
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceManager.setPageFill(
                        MaintenanceManager.PAGE_FILL_FULL | MaintenanceManager.PAGE_FILL_RESERVE));
    }

    /**
     * page fill must be PAGE_FILL_FULL or PAGE_FILL_RESERVE
     */
    @Test
    void testSetPageFillBadParam_2() {
        final int notPageFillFullOrReserve = 34;
        assert notPageFillFullOrReserve != MaintenanceManager.PAGE_FILL_FULL
                && notPageFillFullOrReserve != MaintenanceManager.PAGE_FILL_RESERVE;
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setPageFill(notPageFillFullOrReserve));
    }

    @Test
    void testSetPageFill() {
        // Just make sure it runs without an exception
        assertDoesNotThrow(() -> maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_FULL));
        assertDoesNotThrow(() -> maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_RESERVE));
    }

    @Test
    void testMarkCorruptRecords() {
        // Just make sure it runs without an exception
        assertDoesNotThrow(() -> maintenanceManager.markCorruptRecords());
    }

    @Test
    void testValidateDatabase() {
        // Just make sure it runs without an exception
        assertDoesNotThrow(() -> maintenanceManager.validateDatabase());
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    void testValidateDatabaseBadParam_1() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.validateDatabase(
                (MaintenanceManager.VALIDATE_READ_ONLY
                        | MaintenanceManager.VALIDATE_FULL
                        | MaintenanceManager.VALIDATE_IGNORE_CHECKSUM) * 2));
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    void testValidateDatabaseBadParam_2() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.validateDatabase(
                MaintenanceManager.VALIDATE_READ_ONLY | MaintenanceManager.VALIDATE_FULL));
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    void testValidateDatabaseBadParam_3() {
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_FULL / 2));
    }

    /**
     * Validation must be either 0, read-only, or full
     */
    @Test
    void testValidateDatabaseBadParam_4() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.validateDatabase(-1));
    }

    @Test
    void testValidateDatabaseFull() throws Exception {
        // Just run to make sure it doesn't fail
        maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_FULL);
    }

    /**
     * Sweep threshold must be positive
     */
    @Test
    void testSetSweepThresholdBadParams() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setSweepThreshold(-1));
    }

    @Test
    void testSetSweepThreshold() {
        // Just run it to see if it throws an exception
        assertDoesNotThrow(() -> maintenanceManager.setSweepThreshold(0));
        assertDoesNotThrow(() -> maintenanceManager.setSweepThreshold(2000));
    }

    @Test
    void testSweepDatabase() {
        // Just run it to see if it throws an exception
        assertDoesNotThrow(() -> maintenanceManager.sweepDatabase());
    }

    @Test
    void testActivateShadowFile() {
        // Just run it to see if it throws an exception
        assertDoesNotThrow(() -> maintenanceManager.activateShadowFile());
    }

    @Test
    void testKillUnavailableShadows() {
        // Just run it to see if it throws an exception
        assertDoesNotThrow(() -> maintenanceManager.killUnavailableShadows());
    }

    @Test
    void testGetLimboTransactions() throws Exception {
        final int COUNT_LIMBO = 5;
        createLimboTransaction(COUNT_LIMBO);
        long[] limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(COUNT_LIMBO, limboTransactions.length);
    }

    @Test
    void testRollbackLimboTransaction() throws Exception {
        List<Long> limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(0, limboTransactions.size());

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(3, limboTransactions.size());

        long trId = limboTransactions.get(0);
        maintenanceManager.rollbackTransaction(trId);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(2, limboTransactions.size());
    }

    @Test
    void testRollbackLimboTransactionAsInt() throws Exception {
        long[] limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(0, limboTransactions.length);

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(3, limboTransactions.length);

        int trId = (int) limboTransactions[0];
        maintenanceManager.rollbackTransaction(trId);

        limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(2, limboTransactions.length);
    }

    @Test
    void testCommitLimboTransaction() throws Exception {
        List<Long> limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(0, limboTransactions.size());

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(3, limboTransactions.size());

        long trId = limboTransactions.get(0);
        maintenanceManager.commitTransaction(trId);

        limboTransactions = maintenanceManager.limboTransactionsAsList();
        assertEquals(2, limboTransactions.size());
    }

    @Test
    void testCommitLimboTransactionAsInt() throws Exception {
        long[] limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(0, limboTransactions.length);

        createLimboTransaction(3);

        limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(3, limboTransactions.length);

        int trId = (int)limboTransactions[0];
        maintenanceManager.commitTransaction(trId);

        limboTransactions = maintenanceManager.getLimboTransactions();
        assertEquals(2, limboTransactions.length);
    }

    private void createLimboTransaction(int count) throws Exception {
        try (FBConnection conn = (FBConnection) getConnectionViaDriverManager()) {
            final FbDatabase fbDatabase = conn.getFbDatabase();
            for (int i = 0; i < count; i++) {
                TransactionParameterBuffer tpBuf = conn.createTransactionParameterBuffer();
                FbTransaction transaction = fbDatabase.startTransaction(tpBuf);
                transaction.prepare(null);
            }
        }
    }

    // NOTE: this test only confirms that the repair option can be run, given we can't (easily) create databases with
    // a previous ODS, we can't test if it is really run, see next test for an environment-specific test
    @Test
    void testUpgradeOds() {
        assumeTrue(getDefaultSupportInfo().supportsUpgradeOds(), "test requires upgrade ODS support");
        assertDoesNotThrow(() -> maintenanceManager.upgradeOds());
    }

    /**
     * Test upgrade ODS, with a specific Firebird 4.0 database file.
     * <p>
     * This test is machine specific (or at least, environment-specific), as it requires a Firebird database with
     * the path {@code E:\DB\FB4\FB4TESTDATABASE.FDB}.
     * </p>
     */
    @Test
    void testUpgradeOds_machineSpecific(@TempDir Path tempDir) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsUpgradeOds(), "test requires upgrade ODS support");
        assumeTrue(getDefaultSupportInfo().supportsOds(13, 0), "Test requires ODS 13.0 support (DB to be upgraded)");
        assumeTrue(EnvironmentRequirement.DB_LOCAL_FS.isMet(), "Requires DB on local file system");
        // In the future, this may need to declare an upper version limit, or select DB based on the actual version
        Path fb4DbPath;
        try {
            fb4DbPath = Path.of("E:/DB/FB4/FB4TESTDATABASE.FDB");
        } catch (InvalidPathException e) {
            throw new TestAbortedException("Database path is invalid on this system", e);
        }
        assumeTrue(Files.exists(fb4DbPath), "Expected database does not exist");

        var testDb = tempDir.resolve("tempdb.fdb").toAbsolutePath();
        Files.copy(fb4DbPath, testDb);
        maintenanceManager.setDatabase(testDb.toString());
        String jdbcUrl = getUrl(testDb);
        Properties props = getDefaultPropertiesForConnection();

        // Verify ODS before upgrade
        try (var connection = DriverManager.getConnection(jdbcUrl, props)) {
            var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
            assertEquals(OdsVersion.of(13, 0), OdsVersion.of(dbmd.getOdsMajorVersion(), dbmd.getOdsMinorVersion()),
                    "ODS before upgrade");
        }

        maintenanceManager.upgradeOds();

        // Verify ODS after upgrade
        try (var connection = DriverManager.getConnection(jdbcUrl, props)) {
            var dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
            assertEquals(getDefaultSupportInfo().getDefaultOdsVersion(),
                    OdsVersion.of(dbmd.getOdsMajorVersion(), dbmd.getOdsMinorVersion()), "ODS after upgrade");
        }
    }

    // NOTE: this test only confirms that the repair option can be run
    @Test
    void testFixIcu() {
        assumeTrue(getDefaultSupportInfo().supportsFixIcu(), "test requires fix ICU support");
        assertDoesNotThrow(maintenanceManager::fixIcu);
    }

}
