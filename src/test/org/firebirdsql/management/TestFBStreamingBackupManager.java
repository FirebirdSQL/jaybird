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

import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link FBStreamingBackupManager}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBStreamingBackupManager {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final UsesDatabase usesDatabase = UsesDatabase.noDatabase();

    @Rule
    public final RuleChain ruleChain = RuleChain.outerRule(RequireProtocol.requireProtocolVersion(12))
            .around(temporaryFolder)
            .around(usesDatabase);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private File tempFolder;
    private FBStreamingBackupManager backupManager;

    private static final String TEST_TABLE = "CREATE TABLE TEST (A INT)";

    @Before
    public void setUp() throws Exception {
        tempFolder = temporaryFolder.newFolder();
        System.out.println(tempFolder);
        backupManager = new FBStreamingBackupManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            backupManager.setHost(DB_SERVER_URL);
            backupManager.setPort(DB_SERVER_PORT);
        }
        backupManager.setUser(DB_USER);
        backupManager.setPassword(DB_PASSWORD);
        backupManager.setDatabase(getDatabasePath());
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    @Test
    public void testStreamingBackupAndRestore() throws Exception {
        usesDatabase.createDefaultDatabase();
        final Path backupPath = Paths.get(tempFolder.getAbsolutePath(), "testbackup.fbk");
        try (OutputStream backupOutputStream = new FileOutputStream(backupPath.toFile())) {
            backupManager.setBackupOutputStream(backupOutputStream);
            backupManager.backupDatabase();
        }
        assertTrue(String.format("Expected backup file %s to exist", backupPath), Files.exists(backupPath));

        final Path restorePath = Paths.get(tempFolder.getAbsolutePath(), "testrestore.fdb");
        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath.toString());
        backupManager.setDatabase(restorePath.toString());
        try (InputStream restoreInputStream = new FileInputStream(backupPath.toFile())) {
            backupManager.setRestoreInputStream(restoreInputStream);
            backupManager.restoreDatabase();
        }
        assertTrue(String.format("Expected database file %s to exist", backupPath), Files.exists(backupPath));
    }

    @Test
    public void testSetBadBufferCount() {
        expectedException.reportMissingExceptionWithMessage("Page buffer count must be a positive value")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageBufferCount(-1);
    }

    @Test
    public void testSetBadPageSize() {
        expectedException.reportMissingExceptionWithMessage("Page size must be one of 4196, 8192 or 16384)")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageSize(4000);
    }

    @Test
    public void testSetBadPageSize_1K_notSupported() {
        expectedException.reportMissingExceptionWithMessage("Page size must be one of 4196, 8192 or 16384)")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageSize(PageSizeConstants.SIZE_1K);
    }

    @Test
    public void testSetBadPageSize_2K_notSupported() {
        expectedException.reportMissingExceptionWithMessage("Page size must be one of 4196, 8192 or 16384)")
                .expect(IllegalArgumentException.class);
        backupManager.setRestorePageSize(PageSizeConstants.SIZE_2K);
    }

    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    @Test
    public void testValidPageSizes() {
        final int[] pageSizes = { PageSizeConstants.SIZE_4K, PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K };
        for (int pageSize : pageSizes) {
            backupManager.setRestorePageSize(pageSize);
        }
    }

    @Test
    public void testSetBackupPathNotSupported() {
        expectedException.reportMissingExceptionWithMessage("setBackupPath not allowed")
                .expect(IllegalArgumentException.class);
        backupManager.setBackupPath("Some path");
    }

    @Test
    public void testAddBackupPathNotSupported() {
        expectedException.reportMissingExceptionWithMessage("addBackupPath not allowed")
                .expect(IllegalArgumentException.class);
        backupManager.addBackupPath("Some path");
    }
}
