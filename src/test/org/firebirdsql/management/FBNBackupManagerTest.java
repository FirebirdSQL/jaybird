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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FBNBackupManagerTest {

    @TempDir
    Path tempDir;

    @RegisterExtension
    UsesDatabaseExtension usesDatabase = UsesDatabaseExtension.usesDatabase(
            "create table data (id integer, val varchar(50))",
            "commit retain",
            "insert into data (id, val) values (1, 'first')");

    private final FBNBackupManager manager = new FBNBackupManager(getGdsType());

    @BeforeEach
    void configureFBNBackupManager() {
        manager.setHost(DB_SERVER_URL);
        manager.setPort(DB_SERVER_PORT);
        manager.setUser(DB_USER);
        manager.setPassword(DB_PASSWORD);
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

}