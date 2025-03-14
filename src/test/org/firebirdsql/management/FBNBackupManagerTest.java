// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FBNBackupManagerTest {

    private static final Pattern DATABASE_GUID_PATTERN = Pattern.compile("Database GUID:\\s+(\\{[^}]+})");

    @TempDir
    Path tempDir;

    @RegisterExtension
    @Order(1)
    static RunEnvironmentExtension runEnvironmentExtension = RunEnvironmentExtension.builder()
            .requiresDbOnLocalFileSystem()
            .build();

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase(
            "create table data (id integer, val varchar(50))",
            "commit retain",
            "insert into data (id, val) values (1, 'first')");

    private final FBNBackupManager manager = configureServiceManager(new FBNBackupManager(getGdsType()));

    @AfterEach
    void cleanupDeltaFile() throws Exception {
        Files.deleteIfExists(Paths.get(getDatabasePath() + ".delta"));
    }

    @Test
    void backupWithGuid() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupWithGuid(), "Requires NBackup GUID backup support");
        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        String guid;
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                    "select first 1 RDB$GUID from RDB$BACKUP_HISTORY order by RDB$TIMESTAMP desc")) {
                assertTrue(rs.next(), "expected a row");
                guid = rs.getString(1);
            }
            statement.execute("insert into data (id, val) values (2, 'second')");
        }

        manager.clearBackupFiles();
        String backup2 = tempDir.resolve("backup2.nbk").toString();
        manager.setBackupFile(backup2);
        manager.setBackupGuid(guid);
        manager.backupDatabase();

        manager.clearBackupFiles();
        String restoredDb = tempDir.resolve("restored.fdb").toString();
        manager.setDatabase(restoredDb);
        manager.addBackupFile(backup1);
        manager.addBackupFile(backup2);
        manager.restoreDatabase();
        usesDatabase.addDatabase(restoredDb);

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue(rs.next(), "expected first row");
            assertEquals("first", rs.getString(1), "first row");
            assertTrue(rs.next(), "expected second row");
            assertEquals("second", rs.getString(1), "second row");
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void inPlaceRestore() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupWithGuid()
                && getDefaultSupportInfo().supportsNBackupInPlaceRestore(),
                "Requires NBackup GUID backup support and in-place restore support");
        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        String guid;
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                    "select first 1 RDB$GUID from RDB$BACKUP_HISTORY order by RDB$TIMESTAMP desc")) {
                assertTrue(rs.next(), "expected a row");
                guid = rs.getString(1);
            }
            statement.execute("insert into data (id, val) values (2, 'second')");
        }

        manager.clearBackupFiles();
        String backup2 = tempDir.resolve("backup2.nbk").toString();
        manager.setBackupFile(backup2);
        manager.setBackupGuid(guid);
        manager.backupDatabase();

        manager.clearBackupFiles();
        String restoredDb = tempDir.resolve("restored.fdb").toString();
        manager.setDatabase(restoredDb);
        manager.addBackupFile(backup1);
        manager.restoreDatabase();
        usesDatabase.addDatabase(restoredDb);

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue(rs.next(), "expected first row");
            assertEquals("first", rs.getString(1), "first row");
            assertFalse(rs.next(), "expected no more rows");
        }

        manager.clearBackupFiles();
        manager.setInPlaceRestore(true);
        manager.addBackupFile(backup2);
        manager.restoreDatabase();

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue(rs.next(), "expected first row");
            assertEquals("first", rs.getString(1), "first row");
            assertTrue(rs.next(), "expected second row");
            assertEquals("second", rs.getString(1), "second row");
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void fixup() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupFixup(), "Requires NBackup fixup support");
        final String initialDbGuid = getCurrentDbGuid();
        assertNotNull(initialDbGuid, "Initial database GUID not found");

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.execute("alter database begin backup");
        }

        manager.setDatabase(getDatabasePath());
        manager.fixupDatabase();

        final String afterFixupDbGuid = getCurrentDbGuid();
        assertNotEquals(initialDbGuid, afterFixupDbGuid, "Normal fixup should change database GUID");
    }

    @Test
    void fixup_withPreserveSequence() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupFixup()
                && getDefaultSupportInfo().supportsNBackupPreserveSequence(),
                "Requires NBackup fixup and preserve sequence support");
        final String initialDbGuid = getCurrentDbGuid();
        assertNotNull(initialDbGuid, "Initial database GUID not found");

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.execute("alter database begin backup");
        }

        manager.setDatabase(getDatabasePath());
        manager.setPreserveSequence(true);
        manager.fixupDatabase();

        final String afterFixupDbGuid = getCurrentDbGuid();
        assertEquals(initialDbGuid, afterFixupDbGuid,
                "Fixup with preserve sequence should retain original database GUID");
    }

    @Test
    void restore_withPreserveSequence() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupPreserveSequence(),
                "Requires NBackup preserve sequence support");
        final String initialDbGuid = getCurrentDbGuid();
        assertNotNull(initialDbGuid, "Initial database GUID not found");

        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        manager.clearBackupFiles();
        String restoredDb = tempDir.resolve("restored.fdb").toString();
        manager.setDatabase(restoredDb);
        manager.addBackupFile(backup1);
        manager.setPreserveSequence(true);
        manager.restoreDatabase();
        usesDatabase.addDatabase(restoredDb);

        final String afterFixupDbGuid = getCurrentDbGuid(restoredDb);
        assertEquals(initialDbGuid, afterFixupDbGuid,
                "Restore with preserve sequence should retain original database GUID");
    }

    @Test
    void backupCleanHistoryWithKeepDays() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupCleanHistory(), "Requires NBackup clean history support");
        manager.setCleanHistory(true);
        manager.setKeepDays(5);

        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        assertDoesNotThrow(manager::backupDatabase);

        // NOTE: We are not checking if the server actually cleaned history
    }

    @Test
    void backupCleanHistoryWithKeepRows() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsNBackupCleanHistory(), "Requires NBackup clean history support");
        manager.setCleanHistory(true);
        manager.setKeepRows(5);

        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        assertDoesNotThrow(manager::backupDatabase);

        // NOTE: We are not checking if the server actually cleaned history
    }

    @Test
    void backupCleanHistoryWithoutKeep() {
        assumeTrue(getDefaultSupportInfo().supportsNBackupCleanHistory(), "Requires NBackup clean history support");
        manager.setCleanHistory(true);

        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        SQLException exception = assertThrows(SQLException.class, manager::backupDatabase);
        assertThat(exception, fbMessageStartsWith(
                ISCConstants.isc_missing_required_spb, "isc_spb_nbk_keep_days or isc_spb_nbk_keep_rows"));
    }

    @Test
    void backupCleanHistoryBothKeeps() {
        assumeTrue(getDefaultSupportInfo().supportsNBackupCleanHistory(), "Requires NBackup clean history support");
        manager.setCleanHistory(true);
        manager.setKeepDays(5);
        manager.setKeepRows(5);

        String backup1 = tempDir.resolve("backup1.nbk").toString();
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        SQLException exception = assertThrows(SQLException.class, manager::backupDatabase);
        assertThat(exception, fbMessageStartsWith(
                ISCConstants.isc_unexp_spb_form, "only one isc_spb_nbk_keep_days or isc_spb_nbk_keep_rows"));
    }

    private String getCurrentDbGuid() throws SQLException {
        return getCurrentDbGuid(getDatabasePath());
    }

    private String getCurrentDbGuid(String databasePath) throws SQLException {
        FBStatisticsManager statsMan = new FBStatisticsManager(getGdsType());
        configureServiceManager(statsMan);
        statsMan.setDatabase(databasePath);
        ByteArrayOutputStream loggingStream = new ByteArrayOutputStream();
        statsMan.setLogger(loggingStream);
        statsMan.getHeaderPage();
        String headerPage = loggingStream.toString();
        Matcher matcher = DATABASE_GUID_PATTERN.matcher(headerPage);
        return matcher.find() ? matcher.group(1) : null;
    }

}