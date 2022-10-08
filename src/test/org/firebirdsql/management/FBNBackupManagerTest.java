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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBNBackupManagerTest {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(
            "create table data (id integer, val varchar(50))",
            "commit retain",
            "insert into data (id, val) values (1, 'first')");

    @Rule
    public final RuleChain ruleChain = RuleChain.outerRule(temporaryFolder)
            .around(usesDatabase);

    private final FBNBackupManager manager = new FBNBackupManager(getGdsType());

    @Before
    public void configureFBNBackupManager() {
        manager.setHost(DB_SERVER_URL);
        manager.setPort(DB_SERVER_PORT);
        manager.setUser(DB_USER);
        manager.setPassword(DB_PASSWORD);
    }

    @Test
    public void backupWithGuid() throws Exception {
        assumeTrue("Requires NBackup GUID backup support", getDefaultSupportInfo().supportsNBackupWithGuid());
        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        String guid;
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                    "select first 1 RDB$GUID from RDB$BACKUP_HISTORY order by RDB$TIMESTAMP desc")) {
                assertTrue("expected a row", rs.next());
                guid = rs.getString(1);
            }
            statement.execute("insert into data (id, val) values (2, 'second')");
        }

        manager.clearBackupFiles();
        String backup2 = tempFile("backup2.nbk");
        manager.setBackupFile(backup2);
        manager.setBackupGuid(guid);
        manager.backupDatabase();

        manager.clearBackupFiles();
        String restoredDb = tempFile("restored.fdb");
        manager.setDatabase(restoredDb);
        manager.addBackupFile(backup1);
        manager.addBackupFile(backup2);
        manager.restoreDatabase();
        usesDatabase.addDatabase(restoredDb);

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue("expected first row", rs.next());
            assertEquals("first row", "first", rs.getString(1));
            assertTrue("expected second row", rs.next());
            assertEquals("second row", "second", rs.getString(1));
            assertFalse("expected no more rows", rs.next());
        }
    }

    @Test
    public void inPlaceRestore() throws Exception {
        assumeTrue("Requires NBackup GUID backup support and in-place restore support",
                getDefaultSupportInfo().supportsNBackupWithGuid()
                        && getDefaultSupportInfo().supportsNBackupInPlaceRestore());
        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        String guid;
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                    "select first 1 RDB$GUID from RDB$BACKUP_HISTORY order by RDB$TIMESTAMP desc")) {
                assertTrue("expected a row", rs.next());
                guid = rs.getString(1);
            }
            statement.execute("insert into data (id, val) values (2, 'second')");
        }

        manager.clearBackupFiles();
        String backup2 = tempFile("backup2.nbk");
        manager.setBackupFile(backup2);
        manager.setBackupGuid(guid);
        manager.backupDatabase();

        manager.clearBackupFiles();
        String restoredDb = tempFile("restored.fdb");
        manager.setDatabase(restoredDb);
        manager.addBackupFile(backup1);
        manager.restoreDatabase();
        usesDatabase.addDatabase(restoredDb);

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue("expected first row", rs.next());
            assertEquals("first row", "first", rs.getString(1));
            assertFalse("expected no more rows", rs.next());
        }

        manager.clearBackupFiles();
        manager.setInPlaceRestore(true);
        manager.addBackupFile(backup2);
        manager.restoreDatabase();

        try (Connection connection = DriverManager.getConnection(getUrl(restoredDb), getDefaultPropertiesForConnection());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select val from data order by id")) {
            assertTrue("expected first row", rs.next());
            assertEquals("first row", "first", rs.getString(1));
            assertTrue("expected second row", rs.next());
            assertEquals("second row", "second", rs.getString(1));
            assertFalse("expected no more rows", rs.next());
        }
    }

    @Test
    public void backupCleanHistoryWithKeepDays() throws Exception {
        assumeTrue("Requires NBackup clean history support", getDefaultSupportInfo().supportsNBackupCleanHistory());
        manager.setCleanHistory(true);
        manager.setKeepDays(5);

        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        // NOTE: We are not checking if the server actually cleaned history
    }

    @Test
    public void backupCleanHistoryWithKeepRows() throws Exception {
        assumeTrue("Requires NBackup clean history support", getDefaultSupportInfo().supportsNBackupCleanHistory());
        manager.setCleanHistory(true);
        manager.setKeepRows(5);

        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        manager.backupDatabase();

        // NOTE: We are not checking if the server actually cleaned history
    }

    @Test
    public void backupCleanHistoryWithoutKeep() {
        assumeTrue("Requires NBackup clean history support", getDefaultSupportInfo().supportsNBackupCleanHistory());
        manager.setCleanHistory(true);

        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        try {
            manager.backupDatabase();
            fail("expected exception to be thrown");
        } catch (SQLException e) {
            assertThat(e, fbMessageStartsWith(
                    ISCConstants.isc_missing_required_spb, "isc_spb_nbk_keep_days or isc_spb_nbk_keep_rows"));
        }
    }

    @Test
    public void backupCleanHistoryBothKeeps() {
        assumeTrue("Requires NBackup clean history support", getDefaultSupportInfo().supportsNBackupCleanHistory());
        manager.setCleanHistory(true);
        manager.setKeepDays(5);
        manager.setKeepRows(5);

        String backup1 = tempFile("backup1.nbk");
        manager.setBackupFile(backup1);
        manager.setDatabase(getDatabasePath());
        try {
            manager.backupDatabase();
            fail("expected exception to be thrown");
        } catch (SQLException e) {
            assertThat(e, fbMessageStartsWith(
                    ISCConstants.isc_unexp_spb_form, "only one isc_spb_nbk_keep_days or isc_spb_nbk_keep_rows"));
        }
    }

    private String tempFile(String filename) {
        return Paths.get(temporaryFolder.getRoot().getAbsolutePath(), filename).toString();
    }
}