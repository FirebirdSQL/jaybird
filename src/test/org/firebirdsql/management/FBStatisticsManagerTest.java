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
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_PORT;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_URL;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getGdsType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the FBStatisticsManager class
 */
class FBStatisticsManagerTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private FBStatisticsManager statManager;
    private OutputStream loggingStream;

    private static final String DEFAULT_TABLE = ""
        + "CREATE TABLE TEST ("
        + "     TESTVAL INTEGER NOT NULL"
        + ")";

    @BeforeEach
    void setUp() {
        loggingStream = new ByteArrayOutputStream();
    
        statManager = new FBStatisticsManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            statManager.setServerName(DB_SERVER_URL);
            statManager.setPortNumber(DB_SERVER_PORT);
        }
        statManager.setUser(DB_USER);
        statManager.setPassword(DB_PASSWORD);
        statManager.setDatabase(getDatabasePath());
        statManager.setLogger(loggingStream);
    }

    private void createTestTable() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.execute(DEFAULT_TABLE);
        }
    }

    @Test
    void testGetHeaderPage() throws SQLException {
        statManager.getHeaderPage();
        String headerPage = loggingStream.toString();

        // Not a lot more we can really do to ensure that it's a real
        // header page, unfortunately :(
        assertThat(headerPage)
                .describedAs("The header page must include 'Database header page information'")
                .contains("Database header page information")
                .describedAs("The statistics must not include data table info").doesNotContain("Data pages");
    }

    @Test
    void testGetDatabaseStatistics() throws SQLException {
        createTestTable();
        statManager.getDatabaseStatistics();
        String statistics = loggingStream.toString();

        assertThat(statistics)
                .describedAs("The database page analysis must be in the statistics").contains("Data pages")
                .describedAs("System table information must not be in basic statistics").doesNotContain("RDB$DATABASE");
    }

    @Test
    void testGetStatsWithBadOptions() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                statManager.getDatabaseStatistics(
                        (StatisticsManager.DATA_TABLE_STATISTICS
                                | StatisticsManager.SYSTEM_TABLE_STATISTICS
                                | StatisticsManager.INDEX_STATISTICS) * 2));
    }

    @Test
    void testGetSystemStats() throws SQLException {
        statManager.getDatabaseStatistics(
                StatisticsManager.SYSTEM_TABLE_STATISTICS);
        String statistics = loggingStream.toString();

        assertThat(statistics)
                .describedAs("Statistics with SYSTEM_TABLE_STATISTICS option must include system table info")
                .contains("RDB$DATABASE");
    }

    @Test
    void testGetTableStatistics() throws SQLException {
        createTestTable();
        statManager.getTableStatistics(new String[] { "TEST" });
        String statistics = loggingStream.toString();

        assertThat(statistics)
                .describedAs("The database page analysis must be in the statistics").contains("Data pages")
                .describedAs("The table name must be in the statistics").contains("TEST");
    }

    @Test
    void testGetDatabaseTransactionInfo_usingServiceConfig() throws SQLException {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        int oldest = getExpectedOldest(supportInfo);
        int expectedNextOffset = supportInfo.isVersionEqualOrAbove(3, 0) ? 1 : 2;
        createTestTable();
        
        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("select * from rdb$database");
            FBStatisticsManager.DatabaseTransactionInfo databaseTransactionInfo =
                    statManager.getDatabaseTransactionInfo();
            // The transaction values checked here might be implementation dependent
            assertEquals(oldest, databaseTransactionInfo.getOldestTransaction(), "oldest");
            assertEquals(oldest + 1, databaseTransactionInfo.getOldestActiveTransaction(), "oldest active");
            assertEquals(oldest + 1, databaseTransactionInfo.getOldestSnapshotTransaction(), "oldest snapshot");
            assertEquals(oldest + expectedNextOffset, databaseTransactionInfo.getNextTransaction(), "next");
            assertEquals(1, databaseTransactionInfo.getActiveTransactionCount(), "active");
        }
    }

    private int getExpectedOldest(FirebirdSupportInfo supportInfo) {
        if (supportInfo.isVersionEqualOrAbove(5, 0)) {
            return 2;
        } else if (supportInfo.isVersionEqualOrAbove(4, 0)) {
            return 1;
        } else if (supportInfo.isVersionEqualOrAbove(3, 0, 10)) {
            return 2;
        } else if (supportInfo.isVersionEqualOrAbove(2, 5)) {
            return 1;
        } else {
            return 5;
        }
    }

    @Test
    void testGetDatabaseTransactionInfo_noDatabaseNameSpecified() {
        statManager.setDatabase(null);
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> statManager.getDatabaseTransactionInfo());
    }

    @Test
    void testGetDatabaseTransactionInfo_usingConnection() throws SQLException {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        int oldest = getExpectedOldest(supportInfo);
        int expectedNextOffset;
        if (supportInfo.isVersionEqualOrAbove(3, 0)) {
            expectedNextOffset = 1;
        } else if (supportInfo.isVersionEqualOrAbove(2, 5)) {
            expectedNextOffset = 2;
        } else {
            expectedNextOffset = 1;
        }
        createTestTable();

        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("select * from rdb$database");

            FBStatisticsManager.DatabaseTransactionInfo databaseTransactionInfo =
                    FBStatisticsManager.getDatabaseTransactionInfo(conn);
            // The transaction values checked here might be implementation dependent
            assertEquals(oldest, databaseTransactionInfo.getOldestTransaction(), "oldest");
            assertEquals(oldest + 1, databaseTransactionInfo.getOldestActiveTransaction(), "oldest active");
            assertEquals(oldest + 1, databaseTransactionInfo.getOldestSnapshotTransaction(), "oldest snapshot");
            assertEquals(oldest + expectedNextOffset, databaseTransactionInfo.getNextTransaction(), "next");
            assertEquals(1, databaseTransactionInfo.getActiveTransactionCount(), "active");
        }
    }
}
