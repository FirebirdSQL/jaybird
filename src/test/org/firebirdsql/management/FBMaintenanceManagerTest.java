/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCode;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.oneOf;
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
        maintenanceManager = new FBMaintenanceManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            maintenanceManager.setServerName(DB_SERVER_URL);
            maintenanceManager.setPortNumber(DB_SERVER_PORT);
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
            stmt.executeUpdate("INSERT INTO TEST VALUES (3)");
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
        createTestTable(DIALECT3_TABLE);
    }

    /**
     * Database dialect must be either 1 or 3
     */
    @Test
    void testSetBadDialect() {
        assertThrows(IllegalArgumentException.class, () -> maintenanceManager.setDatabaseDialect(5));
    }

    /**
     * Query must fail on an offline database
     */
    @Test
    void testForcedShutdown() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            createTestTable();

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
        try (Connection conn = getConnectionViaDriverManager()) {
            createTestTable();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.commit();
        }

        // Shutting down when no transactions are active should work
        maintenanceManager.shutdownDatabase(MaintenanceManager.SHUTDOWN_TRANSACTIONAL, 0);
        Thread.sleep(100);
        maintenanceManager.bringDatabaseOnline();
        Thread.sleep(100);
        
        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            SQLException exception = assertThrows(SQLException.class,
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
    public void testSetDefaultCacheBufferTooLow() {
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
    void testSetForcedWrites() throws Exception {
        // No test we can really do other than make sure it doesn't just fail
        maintenanceManager.setForcedWrites(true);
        maintenanceManager.setForcedWrites(false);
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
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceManager.setPageFill(
                        Math.min(MaintenanceManager.PAGE_FILL_FULL, MaintenanceManager.PAGE_FILL_RESERVE) - 1));
    }

    @Test
    void testSetPageFill() throws Exception {
        // Just make sure it runs without an exception
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_FULL);
        maintenanceManager.setPageFill(MaintenanceManager.PAGE_FILL_RESERVE);
    }

    @Test
    void testMarkCorruptRecords() throws Exception {
        // Just make sure it runs without an exception
        maintenanceManager.markCorruptRecords();
    }

    @Test
    void testValidateDatabase() throws Exception {
        // Just make sure it runs without an exception
        maintenanceManager.validateDatabase();
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
    void testSetSweepThreshold() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.setSweepThreshold(0);
        maintenanceManager.setSweepThreshold(2000);
    }

    @Test
    void testSweepDatabase() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.sweepDatabase();
    }

    @Test
    void testActivateShadowFile() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.activateShadowFile();
    }

    @Test
    void testKillUnavailableShadows() throws Exception {
        // Just run it to see if it throws an exception
        maintenanceManager.killUnavailableShadows();
    }

    @Test
    public void testGetLimboTransactions() throws Exception {
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
}
