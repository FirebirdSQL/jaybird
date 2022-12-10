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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FBTableStatisticsManager}.
 */
class FBTableStatisticsManagerTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            "create table TEST_TABLE(INT_VAL integer)"
    );

    @Test
    void testTableStatistics() throws SQLException {
        try (Connection con = getConnectionViaDriverManager();
             FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(con);
             Statement stmt = con.createStatement()) {
            assertThat("Expected no statistics for TEST_TABLE",
                    statsMan.getTableStatistics(), not(hasKey("TEST_TABLE")));

            stmt.execute("insert into TEST_TABLE(INT_VAL) values (1)");

            Map<String, TableStatistics> statsAfterInsert = statsMan.getTableStatistics();
            assertThat("Expected statistics for TEST_TABLE", statsAfterInsert, hasKey("TEST_TABLE"));
            TableStatistics testTableAfterInsert = statsAfterInsert.get("TEST_TABLE");
            assertEquals("TEST_TABLE", testTableAfterInsert.tableName(), "tableName");
            assertEquals(1, testTableAfterInsert.insertCount(), "Expected one insert");

            try (ResultSet rs = stmt.executeQuery("select * from TEST_TABLE")) {
                assertTrue(rs.next());
            }

            Map<String, TableStatistics> statsAfterSelect = statsMan.getTableStatistics();
            TableStatistics testTableAfterSelect = statsAfterSelect.get("TEST_TABLE");
            assertEquals("TEST_TABLE", testTableAfterSelect.tableName(), "tableName");
            assertEquals(1, testTableAfterSelect.insertCount(), "Expected one insert");
            System.out.println(testTableAfterSelect);
            assertEquals(1, testTableAfterSelect.readSeqCount(), "Expected one sequential read");
        }
    }

    @Test
    void cannotCreateTableStatisticsManagerIfConnectionIsClosed() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        connection.close();

        SQLNonTransientConnectionException exception = assertThrows(SQLNonTransientConnectionException.class,
                () -> FBTableStatisticsManager.of(connection));
        assertThat(exception, message(containsString("connection is closed")));
    }

    @Test
    void cannotGetTableStatisticsAfterConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection);

            connection.close();

            SQLNonTransientException exception = assertThrows(SQLNonTransientException.class,
                    statsMan::getTableStatistics);
            assertThat(exception, message(containsString("statistics manager is closed")));
        }
    }

    @Test
    void cannotGetTableStatisticsAfterStatisticsManagerClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            FBTableStatisticsManager statsMan = FBTableStatisticsManager.of(connection);

            statsMan.close();

            SQLNonTransientException exception = assertThrows(SQLNonTransientException.class,
                    statsMan::getTableStatistics);
            assertThat(exception, message(containsString("statistics manager is closed")));

            assertFalse(connection.isClosed(), "expected connection not closed");
        }
    }
}