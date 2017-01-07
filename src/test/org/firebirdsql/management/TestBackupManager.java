/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * This test assumes it is run against localhost
 */
public class TestBackupManager {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final UsesDatabase usesDatabase = UsesDatabase.noDatabase();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final RuleChain ruleChain = RuleChain.outerRule(temporaryFolder)
            .around(usesDatabase);

    private BackupManager backupManager;
    private File tempFolder;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    @Before
    public void setUp() throws Exception {
        tempFolder = temporaryFolder.newFolder();
        System.out.println(tempFolder);
        backupManager = new FBBackupManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            assumeTrue("Test needs to run on localhost for proper clean up", isLocalHost(DB_SERVER_URL));
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
        final Path backupPath = Paths.get(tempFolder.getAbsolutePath(), "testbackup.fbk");
        return backupPath.toString();
    }

    private void createTestTable() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(TEST_TABLE);
        }
    }

    @Test
    public void testBackup() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();

        final Path restorePath = Paths.get(tempFolder.getAbsolutePath(), "testrestore.fdb");

        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath.toString());
        backupManager.setDatabase(restorePath.toString());
        backupManager.restoreDatabase();

        try (Connection c = DriverManager.getConnection(getUrl(restorePath.toString()), getDefaultPropertiesForConnection())) {
            assertTrue(c.isValid(0));
        }
    }

    @Test
    public void testSetBadBufferCount() {
        expectedException.reportMissingExceptionWithMessage("Page buffer count must be a positive value")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageBufferCount(-1);
    }

    @Test
    public void testSetBadPageSize() {
        expectedException.reportMissingExceptionWithMessage("Page size must be one of 1024, 2048, 4196, 8192, 16384 or 32768)")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageSize(4000);
    }

    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    @Test
    public void testValidPageSizes() {
        final int[] pageSizes = { PageSizeConstants.SIZE_1K, PageSizeConstants.SIZE_2K, PageSizeConstants.SIZE_4K,
                PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K, PageSizeConstants.SIZE_32K };
        for (int pageSize : pageSizes) {
            backupManager.setRestorePageSize(pageSize);
        }
    }

    @Test
    public void testRestoreReadOnly() throws Exception {
        usesDatabase.createDefaultDatabase();
        createTestTable();
        try (Connection conn = getConnectionViaDriverManager();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST VALUES (1)");
        }

        backupManager.backupDatabase();
        final Path restorePath1 = Paths.get(tempFolder.getAbsolutePath(), "testrestore1.fdb");
        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath1.toString());
        backupManager.setDatabase(restorePath1.toString());
        backupManager.setRestoreReadOnly(true);
        backupManager.restoreDatabase();

        try (Connection conn = DriverManager.getConnection(getUrl(restorePath1.toString()), getDefaultPropertiesForConnection());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST VALUES (2)");
            fail("Not possible to insert data in a read-only database");
        } catch (SQLException e) {
            // Ignore
        }

        final Path restorePath2 = Paths.get(tempFolder.getAbsolutePath(), "testrestore2.fdb");
        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath2.toString());
        backupManager.setDatabase(restorePath2.toString());
        backupManager.setRestoreReadOnly(false);
        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();

        try (Connection conn = DriverManager.getConnection(getUrl(restorePath2.toString()), getDefaultPropertiesForConnection());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST VALUES (3)");
        }
    }

    @Test
    public void testBackupReplace() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.backupDatabase();
        backupManager.setRestoreReplace(false);
        try {
            backupManager.restoreDatabase();
            fail("Can't restore-create an existing database");
        } catch (SQLException e) {
            // Ignore
        }

        backupManager.setRestoreReplace(true);
        backupManager.restoreDatabase();
    }

    /**
     * Test if restoring a database to page size 16384 works.
     */
    @Test
    public void testRestorePageSize16384() throws Exception {
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
            assertEquals("Unexpected info item", ISCConstants.isc_info_page_size, databaseInfo[0]);
            int length = iscVaxInteger2(databaseInfo, 1);
            int pageSize = iscVaxInteger(databaseInfo, 3, length);
            assertEquals("Unexpected page size", 16384, pageSize);
        }
    }

    /**
     * Test if restoring a database to page size 32768 works.
     */
    @Test
    public void testRestorePageSize32768() throws Exception {
        assumeTrue("Test requires 32K page size support",
                getDefaultSupportInfo().supportsPageSize(PageSizeConstants.SIZE_32K));
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
            assertEquals("Unexpected info item", ISCConstants.isc_info_page_size, databaseInfo[0]);
            int length = iscVaxInteger2(databaseInfo, 1);
            int pageSize = iscVaxInteger(databaseInfo, 3, length);
            assertEquals("Unexpected page size", 32768, pageSize);
        }
    }

    @Test
    public void testBackupMultiple() throws Exception {
        usesDatabase.createDefaultDatabase();
        backupManager.clearBackupPaths();
        backupManager.clearRestorePaths();


        final Path backupPath1Path = Paths.get(tempFolder.getAbsolutePath(), "testbackup1.fbk");
        String backupPath1 = backupPath1Path.toString();
        final Path backupPath2Path = Paths.get(tempFolder.getAbsolutePath(), "testbackup2.fbk");
        String backupPath2 = backupPath2Path.toString();

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


        final Path restorePath1Path = Paths.get(tempFolder.getAbsolutePath(), "testrestore1.fdb");
        String restorePath1 = restorePath1Path.toString();
        final Path restorePath2Path = Paths.get(tempFolder.getAbsolutePath(), "testrestore2.fdb");
        String restorePath2 = restorePath2Path.toString();

        backupManager.addRestorePath(restorePath1, 10);
        backupManager.addRestorePath(restorePath2, 100);

        backupManager.restoreDatabase();

        //Remove test files from filesystem.
        File file3 = new File(restorePath1);
        assertTrue("File " + restorePath1 + " should exist.", file3.exists());
        File file4 = new File(restorePath2);
        assertTrue("File " + restorePath2 + " should exist.", file4.exists());
   }

    private static boolean isLocalHost(String hostName) {
        return "localhost".equalsIgnoreCase(hostName)
                || "::1".equals(hostName)
                // Ignoring other 127.* possibilities
                || "127.0.0.1".equals(hostName);
    }
}
