/*
 SPDX-FileCopyrightText: Copyright 2004-2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2005-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the FBStatisticsManager class
 */
class FBStatisticsManagerTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private FBStatisticsManager statManager;
    private OutputStream loggingStream;

    private static final String DEFAULT_TABLE = """
            create table TEST (
                 TESTVAL integer constraint PK_TEST primary key
            )""";

    @BeforeEach
    void setUp() {
        loggingStream = new ByteArrayOutputStream();
        statManager = configureDefaultServiceProperties(new FBStatisticsManager(getGdsType()));
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
        statManager.getTableStatistics("TEST");
        String statistics = loggingStream.toString();

        assertThat(statistics)
                .describedAs("The database page analysis must be in the statistics").contains("Data pages")
                .describedAs("The table name must be in the statistics").contains("TEST")
                .describedAs("The (primary key) index must be in the statistics").contains("PK_TEST");
    }

    @Test
    void testGetDatabaseTransactionInfo_usingServiceConfig() throws SQLException {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        int oldest = getExpectedOldest(supportInfo);
        int expectedNextOffset = supportInfo.isVersionEqualOrAbove(3) ? 1 : 2;
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

    @ParameterizedTest
    @MethodSource
    void testGetTableStatistics_limitBySchema(List<String> schemas, List<String> tables, List<String> expectedTables,
            List<String> unexpectedTables) throws Exception {
        assumeSchemaSupport();
        schemaTestSetup();
        statManager.getTableStatistics(schemas, tables);
        String statistics = loggingStream.toString();

        assertThat(statistics)
                .describedAs("The database page analysis must be in the statistics").contains("Data pages");
        if (!expectedTables.isEmpty()) {
            assertThat(statistics)
                .describedAs("These table names must be in the statistics").contains(expectedTables);
        }
        if (!unexpectedTables.isEmpty()) {
            assertThat(statistics)
                .describedAs("These table names must not be in the statistics").doesNotContain(unexpectedTables);
        }
    }

    static Stream<Arguments> testGetTableStatistics_limitBySchema() {
        final List<String> empty = List.of();
        return Stream.of(
                Arguments.of(empty, empty,
                        List.of("\"PUBLIC\".\"TBL1\"", "\"SCH1\".\"TBL1\"", "\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\"",
                                "\"SCH2\".\"TBL2\""),
                        empty),
                Arguments.of(List.of("PUBLIC"), empty, List.of("\"PUBLIC\".\"TBL1\""), List.of("\"SCH1\".\"TBL1\"",
                        "\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\"", "\"SCH2\".\"TBL2\"")),
                Arguments.of(List.of("PUBLIC", "SCH2"), empty,
                        List.of("\"PUBLIC\".\"TBL1\"", "\"SCH2\".\"TBL2\"", "\"SCH2\".\"TBL2\""),
                        List.of("\"SCH1\".\"TBL1\"", "\"SCH1\".\"TBL2\"")),
                Arguments.of(empty, List.of("TBL1"), List.of("\"PUBLIC\".\"TBL1\"", "\"SCH1\".\"TBL1\""),
                        List.of("\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\"", "\"SCH2\".\"TBL2\"")),
                Arguments.of(List.of("SCH1"), List.of("TBL1"), List.of("\"SCH1\".\"TBL1\""),
                        List.of("\"PUBLIC\".\"TBL1\"", "\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\"", "\"SCH2\".\"TBL3\"")),
                Arguments.of(List.of("SCH1", "SCH2"), List.of("TBL1"), List.of("\"SCH1\".\"TBL1\""),
                        List.of("\"PUBLIC\".\"TBL1\"", "\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\"", "\"SCH2\".\"TBL3\"")),
                Arguments.of(List.of("SCH1", "SCH2"), List.of("TBL1", "TBL3"),
                        List.of("\"SCH1\".\"TBL1\"", "\"SCH2\".\"TBL3\""),
                        List.of("\"PUBLIC\".\"TBL1\"", "\"SCH1\".\"TBL2\"", "\"SCH2\".\"TBL2\""))
        );
    }

    private static void schemaTestSetup() throws SQLException {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            for (String sql : List.of(
                    "create schema SCH1",
                    "create schema SCH2",
                    "create table PUBLIC.TBL1 (ID integer)",
                    "create table SCH1.TBL1 (ID integer)",
                    "create table SCH1.TBL2 (ID integer)",
                    "create table SCH2.TBL2 (ID integer)",
                    "create table SCH2.TBL3 (ID integer)")) {
                stmt.execute(sql);
            }
            connection.commit();
        }
    }

    private int getExpectedOldest(FirebirdSupportInfo supportInfo) {
        if (supportInfo.isVersionEqualOrAbove(4, 0, 2)) {
            return isEmbeddedType().matches(GDS_TYPE) ? 1 : 2;
        } else if (supportInfo.isVersionEqualOrAbove(4, 0)) {
            return 1;
        } else if (supportInfo.isVersionEqualOrAbove(3, 0, 10)) {
            return isEmbeddedType().matches(GDS_TYPE) ? 1 : 2;
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
        int expectedNextOffset = supportInfo.isVersionEqualOrAbove(2, 5) && supportInfo.isVersionBelow(3) ? 2 : 1;
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
