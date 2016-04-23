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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.junit.Assert.*;

/**
 * This test assumes it is run against localhost
 */
public class TestBackupManager extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private BackupManager backupManager;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    @Before
    public void setUp() throws Exception {
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

    @After
    public void basicTearDown() throws Exception {
        try {
            try {
                // Drop database.
                super.basicTearDown();
            } finally {
                // Delete backup file.
                File file = new File(getBackupPath());
                file.delete();
            }
        } catch (FileNotFoundException e) {
            // ignore
        }
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

    @Test
    public void testBackup() throws Exception {
        backupManager.backupDatabase();

        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        try {
            Connection c = getConnectionViaDriverManager();
            c.close();
            fail("Should not be able to connect to a dropped database");
        } catch (SQLException ex) {
            // ignore
        }

        System.out.println();
        backupManager.restoreDatabase();

        Connection c = getConnectionViaDriverManager();
        c.close();
    }

    @Test
    public void testSetBadBufferCount() {
        expectedException.reportMissingExceptionWithMessage("Page buffer count must be a positive value")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageBufferCount(-1);
    }

    @Test
    public void testSetBadPageSize() {
        expectedException.reportMissingExceptionWithMessage("Page size must be one of 1024, 2048, 4196, 8192 or 16384)")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageSize(4000);
    }

    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    @Test
    public void testValidPageSizes() {
        final int[] pageSizes = { PageSizeConstants.SIZE_1K, PageSizeConstants.SIZE_2K, PageSizeConstants.SIZE_4K,
                PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K };
        for (int pageSize : pageSizes) {
            backupManager.setRestorePageSize(pageSize);
        }
    }

    @Test
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
            } catch (SQLException e) {
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

    @Test
    public void testBackupReplace() throws Exception {
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
        backupManager.backupDatabase();

        backupManager.setRestoreReplace(true);
        backupManager.setRestorePageSize(16384);
        backupManager.restoreDatabase();

        Connection con = null;
        try {
            con = getConnectionViaDriverManager();
            GDSHelper gdsHelper = ((FBConnection) con).getGDSHelper();
            final FbDatabase currentDatabase = gdsHelper.getCurrentDatabase();
            final byte[] databaseInfo = currentDatabase.getDatabaseInfo(
                    new byte[] { ISCConstants.isc_info_page_size }, 10);
            assertEquals("Unexpected info item", ISCConstants.isc_info_page_size, databaseInfo[0]);
            int length = iscVaxInteger2(databaseInfo, 1);
            int pageSize = iscVaxInteger(databaseInfo, 3, length);
            assertEquals("Unexpected page size", 16384, pageSize);
        } finally {
            closeQuietly(con);
        }
    }

    @Test
    public void testBackupMultiple() throws Exception {
        backupManager.clearBackupPaths();
        backupManager.clearRestorePaths();

        String backupPath1 = getDatabasePath() + "-1.fbk";
        String backupPath2 = getDatabasePath() + "-2.fbk";

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
