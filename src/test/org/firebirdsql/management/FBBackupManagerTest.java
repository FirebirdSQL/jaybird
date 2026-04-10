/*
 SPDX-FileCopyrightText: Copyright 2004-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004-2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.common.ConfigHelper;
import org.firebirdsql.common.MappedTempDirFactory;
import org.firebirdsql.common.MappedPath;
import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This test assumes it is run against localhost
 */
class FBBackupManagerTest {

    @RegisterExtension
    static final RunEnvironmentExtension runEnvironment = RunEnvironmentExtension.builder()
            .requiresDbLocallyMapped()
            .build();

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();

    private BackupManager backupManager;
    @TempDir(factory = MappedTempDirFactory.class)
    Path tempFolder;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    @BeforeEach
    void setUp() {
        backupManager = configureDefaultServiceProperties(new FBBackupManager(getGdsType()));
        backupManager.setDatabase(getDatabasePath());
        backupManager.setBackupPath(getTempPath("testbackup.fbk").toServerPath());
        /* NOTE:
         1) Setting parallel workers unconditionally, but actual support was introduced in Firebird 5.0;
         2) It is only possible to verify for restore if it was set (if a too high value was used), we're more testing
            that the implementation doesn't set it for versions which don't support it, than testing if it gets set
        */
        backupManager.setParallelWorkers(2);
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    private void createTestTable() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(TEST_TABLE);
        }
    }

    @Test
    void testBackup() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();

        MappedPath restorePath = getTempPath("testrestore.fdb");

        backupManager.clearRestorePaths();
        String restorePathString = restorePath.toServerPath();
        usesDatabase.addDatabase(restorePathString);
        backupManager.setDatabase(restorePathString);
        backupManager.restoreDatabase();

        try (var c = DriverManager.getConnection(getUrl(restorePathString), getDefaultPropertiesForConnection())) {
            assertTrue(c.isValid(0));
        }
    }

    @Test
    void testSetBadBufferCount() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageBufferCount(-1),
                "Page buffer count must be a positive value");
    }

    @Test
    void testSetBadPageSize() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageSize(4000),
                "Page size must be one of 1024, 2048, 4196, 8192, 16384 or 32768)");
    }

    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    @ParameterizedTest
    @ValueSource(ints = { PageSizeConstants.SIZE_1K, PageSizeConstants.SIZE_2K, PageSizeConstants.SIZE_4K,
            PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K, PageSizeConstants.SIZE_32K })
    void testValidPageSizes(int pageSize) {
        assertDoesNotThrow(() -> backupManager.setRestorePageSize(pageSize));
    }

    @Test
    void testRestoreReadOnly() throws Exception {
        usesDatabase.createDefaultDatabase();
        createTestTable();
        try (Connection conn = getConnectionViaDriverManager();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
        }

        backupManager.backupDatabase();
        MappedPath restorePath1 = getTempPath("testrestore1.fdb");
        backupManager.clearRestorePaths();
        String serverRestorePath1 = restorePath1.toServerPath();
        usesDatabase.addDatabase(serverRestorePath1);
        backupManager.setDatabase(serverRestorePath1);
        backupManager.setRestoreReadOnly(true);
        backupManager.restoreDatabase();

        try (var conn = DriverManager.getConnection(getUrl(serverRestorePath1), getDefaultPropertiesForConnection());
             Statement stmt = conn.createStatement()) {
            SQLException exception = assertThrows(SQLException.class,
                    () -> stmt.executeUpdate("INSERT INTO TEST VALUES (2)"),
                    "Not possible to insert data in a read-only database");
            assertThat(exception, errorCodeEquals(ISCConstants.isc_read_only_database));
        }

        MappedPath restorePath2 = getTempPath("testrestore2.fdb");
        backupManager.clearRestorePaths();
        String serverRestorePath2 = restorePath2.toServerPath();
        usesDatabase.addDatabase(serverRestorePath2);
        backupManager.setDatabase(serverRestorePath2);
        backupManager.setRestoreReadOnly(false);
        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();

        try (var conn = DriverManager.getConnection(getUrl(serverRestorePath2), getDefaultPropertiesForConnection());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST VALUES (3)");
        }
    }

    @Test
    void testBackupReplace() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();
        backupManager.setRestoreReplace(false);
        assertThrows(SQLException.class, backupManager::restoreDatabase, "Can't restore-create an existing database");

        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();
    }

    /**
     * Test if restoring a database to page size 16384 works.
     */
    @Test
    void testRestorePageSize16384() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();

        backupManager.setRestoreReplace(true);
        backupManager.setRestorePageSize(PageSizeConstants.SIZE_16K);
        backupManager.restoreDatabase();

        try (Connection con = getConnectionViaDriverManager()) {
            GDSHelper gdsHelper = ((FBConnection) con).getGDSHelper();
            final FbDatabase currentDatabase = gdsHelper.getCurrentDatabase();
            final byte[] databaseInfo = currentDatabase.getDatabaseInfo(
                    new byte[] { ISCConstants.isc_info_page_size }, 10);
            assertEquals(ISCConstants.isc_info_page_size, databaseInfo[0], "Unexpected info item");
            int length = iscVaxInteger2(databaseInfo, 1);
            int pageSize = iscVaxInteger(databaseInfo, 3, length);
            assertEquals(16384, pageSize, "Unexpected page size");
        }
    }

    /**
     * Test if restoring a database to page size 32768 works.
     */
    @Test
    void testRestorePageSize32768() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPageSize(PageSizeConstants.SIZE_32K),
                "Test requires 32K page size support");
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();

        backupManager.setRestoreReplace(true);
        backupManager.setRestorePageSize(PageSizeConstants.SIZE_32K);
        backupManager.restoreDatabase();

        try (Connection con = getConnectionViaDriverManager()) {
            GDSHelper gdsHelper = ((FBConnection) con).getGDSHelper();
            final FbDatabase currentDatabase = gdsHelper.getCurrentDatabase();
            final byte[] databaseInfo = currentDatabase.getDatabaseInfo(
                    new byte[] { ISCConstants.isc_info_page_size }, 10);
            assertEquals(ISCConstants.isc_info_page_size, databaseInfo[0], "Unexpected info item");
            int length = iscVaxInteger2(databaseInfo, 1);
            int pageSize = iscVaxInteger(databaseInfo, 3, length);
            assertEquals(32768, pageSize, "Unexpected page size");
        }
    }

    @Test
    void testBackupMultiple() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsLegacyMultiFileDatabase,
                "Test requires legacy multi-file database support");
        usesDatabase.createDefaultDatabase();
        backupManager.clearBackupPaths();
        backupManager.clearRestorePaths();

        MappedPath backupPath1 = getTempPath("testbackup1.fbk");
        String serverBackupPath1 = backupPath1.toServerPath();
        MappedPath backupPath2 = getTempPath("testbackup2.fbk");
        String serverBackupPath2 = backupPath2.toServerPath();

        backupManager.addBackupPath(serverBackupPath1, 2048);
        backupManager.addBackupPath(serverBackupPath2);

        backupManager.backupDatabase();

        assertTrue(Files.exists(backupPath1.local()), () -> "File %s should exist".formatted(backupPath1));
        assertTrue(Files.exists(backupPath2.local()), () -> "File %s should exist".formatted(backupPath2));

        backupManager.clearBackupPaths();

        backupManager.addBackupPath(serverBackupPath1);
        backupManager.addBackupPath(serverBackupPath2);

        MappedPath restorePath1 = getTempPath("testrestore1.fdb");
        String serverRestorePath1 = restorePath1.toServerPath();
        MappedPath restorePath2 = getTempPath("testrestore2.fdb");
        String serverRestorePath2 = restorePath2.toServerPath();

        backupManager.addRestorePath(serverRestorePath1, 10);
        backupManager.addRestorePath(serverRestorePath2, 100);

        backupManager.restoreDatabase();

        //Remove test files from filesystem.
        assertTrue(Files.exists(restorePath1.local()), () -> "File %s should exist".formatted(restorePath1));
        assertTrue(Files.exists(restorePath2.local()), () -> "File %s should exist".formatted(restorePath2));
    }

    /*
     The following test requires a custom security database configured for the alias test_with_custom_sec_db, where
     the custom security database was initialized with:
     create user sysdba password 'masterkey' using plugin srp;
     create user custom_sec password 'custom_sec' grant admin role using plugin srp;

     The SYSDBA password must match the value of the system property test.password (or masterkey if not set).

     These tests are ignored (if possible) or fail when this hasn't been set up.
     */

    @Test
    void testBackupReplace_customSecurityDb() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsCustomSecurityDb(), "Requires custom security DB support");
        try (FBManager mgr = configureFBManager(createFBManager())) {
            mgr.createDatabase("test_with_custom_sec_db", "custom_sec", "custom_sec", "RDB$ADMIN");
        } catch (SQLException e) {
            // We assume that the exception occurred because the custom security database wasn't set up
            assumeTrue(false, "Test requires custom security database for alias test_with_custom_sec_db with admin user custom_sec: " + e);
        }
        usesDatabase.addDatabase("test_with_custom_sec_db");

        backupManager.clearRestorePaths();
        backupManager.setDatabase("test_with_custom_sec_db");
        backupManager.setUser("custom_sec");
        backupManager.setPassword("custom_sec");
        if (supportInfo.isVersionEqualOrAbove(3) && supportInfo.isVersionBelow(4)) {
            backupManager.setRoleName("RDB$ADMIN");
        }
        backupManager.backupDatabase();

        backupManager.setRestoreReplace(true);
        assertDoesNotThrow(() -> backupManager.restoreDatabase());
    }

    @Test
    void testBackupRestore_parallel() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsParallelWorkers(), "test requires support for parallel workers");
        usesDatabase.createDefaultDatabase();
        final int maxParallelWorkers;
        try (var connection = getConnectionViaDriverManager()) {
            maxParallelWorkers = ConfigHelper.getIntConfigValue(connection, "MaxParallelWorkers").orElse(1);
        }

        // set 1 higher to trigger warning
        backupManager.setParallelWorkers(maxParallelWorkers + 1);

        backupManager.setVerbose(true);
        backupManager.backupDatabase();

        // Use of parallel workers with backup is currently not assertable as the warning does not appear in the output

        var loggingStream = new ByteArrayOutputStream(1024);
        backupManager.setLogger(loggingStream);

        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();

        var logOutput = loggingStream.toString(StandardCharsets.ISO_8859_1);
        assertThat(logOutput, containsString(
                "gbak: WARNING:Wrong parallel workers value %d, valid range are from 1 to %d"
                        .formatted(maxParallelWorkers + 1, maxParallelWorkers)));
        System.out.println(logOutput);
    }

    private MappedPath getTempPath(String name) {
        Path localPath = tempFolder.resolve(name);
        Path serverPath = transformMappedToDatabasePath(localPath).orElseThrow();
        return new MappedPath(localPath, serverPath);
    }

}
