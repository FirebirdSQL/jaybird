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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FBDatabaseMetaDataNullsTest {

    private static final String CREATE_TABLE = ""
            + "CREATE TABLE test_nulls("
            + "  id INTEGER NOT NULL PRIMARY KEY, "
            + "  char_value VARCHAR(20)"
            + ")";

    private static final String INSERT_VALUES =
            "INSERT INTO test_nulls VALUES(?, ?)";

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase(
            CREATE_TABLE);

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();

        try (PreparedStatement ps = connection.prepareStatement(INSERT_VALUES)) {
            ps.setInt(1, 1);
            ps.setString(2, "a");
            ps.execute();

            ps.setInt(1, 2);
            ps.setNull(2, Types.VARCHAR);
            ps.execute();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testNullAreSortedAtStartEnd() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (Statement stmt = connection.createStatement()) {
            boolean[][] sorting = new boolean[2][2];

            ResultSet rs;

            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 ASC");
            assertTrue(rs.next(), "Should select a record");
            sorting[0][0] = rs.getString(1) == null;
            assertTrue(rs.next(), "Should select a record");
            sorting[0][1] = rs.getString(1) == null;

            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 DESC");
            assertTrue(rs.next(), "Should select a record");
            sorting[1][0] = rs.getString(1) == null;
            assertTrue(rs.next(), "Should select a record");
            sorting[1][1] = rs.getString(1) == null;

            assertTrue(metaData.nullsAreSortedAtEnd() && (sorting[0][1] && sorting[1][1]) ||
                            !metaData.nullsAreSortedAtEnd() && !(sorting[0][1] && sorting[1][1]),
                    "nullsAreSortedAtEnd is not correct");

            assertTrue(metaData.nullsAreSortedAtStart() && (sorting[0][0] && sorting[1][0]) ||
                            !metaData.nullsAreSortedAtStart() && !(sorting[0][0] && sorting[1][0]),
                    "nullsAreSortedAtStart is not correct");

            assertTrue(metaData.nullsAreSortedHigh() && (sorting[0][1] && sorting[1][0]) ||
                            !metaData.nullsAreSortedHigh() && (sorting[0][0] && sorting[1][1]) ||
                            (!metaData.nullsAreSortedHigh() && (metaData.nullsAreSortedAtEnd()
                                    || metaData.nullsAreSortedAtStart())),
                    "nullsAreSortedHigh is not correct");

            assertTrue(metaData.nullsAreSortedLow() && (sorting[0][0] && sorting[1][1]) ||
                            !metaData.nullsAreSortedLow() && (metaData.nullsAreSortedAtEnd()
                                    || metaData.nullsAreSortedAtStart()),
                    "nullsAreSortedLow is not correct");
        }
    }
}
