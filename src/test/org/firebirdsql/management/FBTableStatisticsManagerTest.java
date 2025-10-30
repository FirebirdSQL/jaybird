// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FBTableStatisticsManager}.
 */
class FBTableStatisticsManagerTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = getConnectionViaDriverManager();
        DdlHelper.executeCreateTable(connection, "recreate table TEST_TABLE(INT_VAL integer)");
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void testTableStatistics() throws SQLException {
        try (FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection);
             Statement stmt = connection.createStatement()) {
            final String key = ifSchemaElse("\"PUBLIC\".\"TEST_TABLE\"", "TEST_TABLE");
            final String tableReference = ifSchemaElse("\"PUBLIC\".\"TEST_TABLE\"", "\"TEST_TABLE\"");
            assertThat("Expected no statistics for TEST_TABLE",
                    statsMan.getTableStatistics(), not(hasKey(key)));

            stmt.execute("insert into TEST_TABLE(INT_VAL) values (1)");

            Map<String, TableStatistics> statsAfterInsert = statsMan.getTableStatistics();
            assertThat("Expected statistics for TEST_TABLE", statsAfterInsert, hasKey(key));
            TableStatistics testTableAfterInsert = statsAfterInsert.get(key);
            assertEquals("TEST_TABLE", testTableAfterInsert.tableName(), "tableName");
            assertEquals(ifSchemaElse("PUBLIC", ""), testTableAfterInsert.schema(), "schema");
            assertEquals(tableReference, testTableAfterInsert.tableReference(), "tableReference");
            assertEquals(1, testTableAfterInsert.insertCount(), "Expected one insert");

            try (ResultSet rs = stmt.executeQuery("select * from TEST_TABLE")) {
                assertTrue(rs.next());
            }

            Map<String, TableStatistics> statsAfterSelect = statsMan.getTableStatistics();
            TableStatistics testTableAfterSelect = statsAfterSelect.get(key);
            assertEquals("TEST_TABLE", testTableAfterSelect.tableName(), "tableName");
            assertEquals(ifSchemaElse("PUBLIC", ""), testTableAfterInsert.schema(), "schema");
            assertEquals(tableReference, testTableAfterInsert.tableReference(), "tableReference");
            assertEquals(1, testTableAfterSelect.insertCount(), "Expected one insert");
            assertEquals(1, testTableAfterSelect.readSeqCount(), "Expected one sequential read");
        }
    }

    @Test
    void cannotCreateTableStatisticsManagerIfConnectionIsClosed() throws Exception {
        connection.close();

        SQLNonTransientConnectionException exception = assertThrows(SQLNonTransientConnectionException.class,
                () -> FBTableStatisticsManager.of(connection));
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_connectionClosed));
    }

    @Test
    void cannotGetTableStatisticsAfterConnectionClose() throws Exception {
        FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection);

        connection.close();

        SQLNonTransientException exception = assertThrows(SQLNonTransientException.class,
                statsMan::getTableStatistics);
        assertThat(exception, message(containsString("statistics manager is closed")));
    }

    @Test
    void cannotGetTableStatisticsAfterStatisticsManagerClose() throws Exception {
        FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection);

        statsMan.close();

        SQLNonTransientException exception = assertThrows(SQLNonTransientException.class,
                statsMan::getTableStatistics);
        assertThat(exception, message(containsString("statistics manager is closed")));

        assertFalse(connection.isClosed(), "expected connection not closed");
    }

    /**
     * Test for <a href="https://github.com/FirebirdSQL/jaybird/issues/747">jaybird#747</a>.
     */
    @Test
    void testTableStatistics_reduceTableCount_multipleInstances() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            final String key = ifSchemaElse("\"PUBLIC\".\"TEST_TABLE\"", "TEST_TABLE");
            try (FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection)) {
                assertThat("Expected no statistics for TEST_TABLE",
                        statsMan.getTableStatistics(), not(hasKey(key)));

                stmt.execute("insert into TEST_TABLE(INT_VAL) values (1)");

                try (ResultSet rs = stmt.executeQuery("select * from TEST_TABLE")) {
                    assertTrue(rs.next());
                }

                Map<String, TableStatistics> statsAfterSelect = statsMan.getTableStatistics();
                assertNotNull(statsAfterSelect.get(key), "statsAfterSelect for TEST_TABLE");
            }

            stmt.execute("drop table TEST_TABLE");

            try (FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection)) {
                Map<String, TableStatistics> statsAfterDrop = statsMan.getTableStatistics();
                assertThat("Expected stats on dropped table",
                        statsAfterDrop, hasKey(startsWith("UNKNOWN_TABLE_ID_")));
            }
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            <NIL>,   TABLE1, TABLE1
            '',      TABLE1, TABLE1
            SCHEMA1, TABLE1, "SCHEMA1"."TABLE1"
            """)
    void testToKey(String schema, String tableName, String expectedTableReference) {
        assertEquals(expectedTableReference, FBTableStatisticsManager.toKey(schema, tableName),
                "tableReference");
    }

}