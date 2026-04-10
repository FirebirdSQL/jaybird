// SPDX-FileCopyrightText: Copyright 2016-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.common.MappedPath;
import org.firebirdsql.common.MappedTempDirFactory;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link FBStreamingBackupManager}.
 *
 * @author Mark Rotteveel
 */
class FBStreamingBackupManagerTest {

    @RegisterExtension
    static final RequireProtocolExtension requireProtocol = RequireProtocolExtension.requireProtocolVersion(12);

    @RegisterExtension
    static final RunEnvironmentExtension runEnvironment = RunEnvironmentExtension.builder()
            .requiresDbLocallyMapped()
            .build();

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();

    @TempDir(factory = MappedTempDirFactory.class)
    Path tempFolder;
    private FBStreamingBackupManager backupManager;

    @BeforeEach
    void setUp() {
        backupManager = configureDefaultServiceProperties(new FBStreamingBackupManager(getGdsType()));
        backupManager.setDatabase(getDatabasePath());
        /* NOTE:
         1) Setting parallel workers unconditionally, but actual support was introduced in Firebird 5.0;
         2) It is only possible to verify for restore if it was set (if a too high value was used), we're more testing
            that the implementation doesn't set it for versions which don't support it, than testing if it gets set
        */
        backupManager.setParallelWorkers(2);
        backupManager.setLogger(System.out);
        backupManager.setVerbose(true);
    }

    @Test
    void testStreamingBackupAndRestore() throws Exception {
        usesDatabase.createDefaultDatabase();
        MappedPath backupPath = getTempPath("testbackup.fbk");
        try (var backupOutputStream = Files.newOutputStream(backupPath.local())) {
            backupManager.setBackupOutputStream(backupOutputStream);
            backupManager.backupDatabase();
        }
        assertTrue(Files.isRegularFile(backupPath.local()),
                () -> "Expected backup file %s to exist".formatted(backupPath));

        MappedPath restorePath = getTempPath("testrestore.fdb");
        backupManager.clearRestorePaths();
        usesDatabase.addDatabase(restorePath.toServerPath());
        backupManager.setDatabase(restorePath.toServerPath());
        try (var restoreInputStream = Files.newInputStream(backupPath.local())) {
            backupManager.setRestoreInputStream(restoreInputStream);
            backupManager.restoreDatabase();
        }
        assertTrue(Files.isRegularFile(restorePath.local()),
                () -> "Expected database file %s to exist".formatted(backupPath));
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
        assertDoesNotThrow(() -> backupManager.setRestorePageSize(pageSize));
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

    @ParameterizedTest
    @ValueSource(ints = { 1, 1024, 30 * 1024, 512 * 1024, Integer.MAX_VALUE })
    void setBackupBufferSize_validSizes(int bufferSize) {
        assertDoesNotThrow(() -> backupManager.setBackupBufferSize(bufferSize));
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0 })
    void setBackupBufferSize_invalidSizes(int bufferSize) {
        assertThrows(IllegalArgumentException.class, () -> backupManager.setBackupBufferSize(bufferSize));
    }

    private MappedPath getTempPath(String name) {
        Path localPath = tempFolder.resolve(name);
        Path serverPath = transformMappedToDatabasePath(localPath).orElseThrow();
        return new MappedPath(localPath, serverPath);
    }

}
