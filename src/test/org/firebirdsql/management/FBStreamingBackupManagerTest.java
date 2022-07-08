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

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link FBStreamingBackupManager}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBStreamingBackupManagerTest {

    @RegisterExtension
    static final RequireProtocolExtension requireProtocol = RequireProtocolExtension.requireProtocolVersion(12);

    @RegisterExtension
    static final RunEnvironmentExtension runEnvironment = RunEnvironmentExtension.builder()
            .requiresDbOnLocalFileSystem()
            .build();

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();

    @TempDir
    Path tempFolder;
    private FBStreamingBackupManager backupManager;

    @BeforeEach
    void setUp() {
        backupManager = new FBStreamingBackupManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            backupManager.setServerName(DB_SERVER_URL);
            backupManager.setPortNumber(DB_SERVER_PORT);
        }
        backupManager.setUser(DB_USER);
        backupManager.setPassword(DB_PASSWORD);
        backupManager.setDatabase(getDatabasePath());
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    @Test
    void testStreamingBackupAndRestore() throws Exception {
        usesDatabase.createDefaultDatabase();
        Path backupPath = tempFolder.resolve("testbackup.fbk");
        try (OutputStream backupOutputStream = new FileOutputStream(backupPath.toFile())) {
            backupManager.setBackupOutputStream(backupOutputStream);
            backupManager.backupDatabase();
        }
        assertTrue(Files.exists(backupPath), () -> format("Expected backup file %s to exist", backupPath));

        Path restorePath = tempFolder.resolve("testrestore.fdb");
        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath.toString());
        backupManager.setDatabase(restorePath.toString());
        try (InputStream restoreInputStream = new FileInputStream(backupPath.toFile())) {
            backupManager.setRestoreInputStream(restoreInputStream);
            backupManager.restoreDatabase();
        }
        assertTrue(Files.exists(backupPath), () -> format("Expected database file %s to exist", backupPath));
    }

    @Test
    void testSetBadBufferCount() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageBufferCount(-1),
                "Page buffer count must be a positive value");
    }

    @Test
    void testSetBadPageSize() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageSize(4000),
                "Page size must be one of 4196, 8192 or 16384)");
    }

    @Test
    void testSetBadPageSize_1K_notSupported() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageSize(PageSizeConstants.SIZE_1K),
                "Page size must be one of 4196, 8192 or 16384)");
    }

    @Test
    void testSetBadPageSize_2K_notSupported() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setRestorePageSize(PageSizeConstants.SIZE_2K),
                "Page size must be one of 4196, 8192 or 16384)");
    }

    /**
     * Tests the valid page sizes expected to be accepted by the BackupManager
     */
    @ParameterizedTest
    @ValueSource(ints = { PageSizeConstants.SIZE_4K, PageSizeConstants.SIZE_8K, PageSizeConstants.SIZE_16K })
    void testValidPageSizes(int pageSize) {
        backupManager.setRestorePageSize(pageSize);
    }

    @Test
    void testSetBackupPathNotSupported() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setBackupPath("Some path"),
                "setBackupPath not allowed");
    }

    @Test
    void testAddBackupPathNotSupported() {
        assertThrows(IllegalArgumentException.class, () -> backupManager.addBackupPath("Some path"),
                "addBackupPath not allowed");
    }
}
