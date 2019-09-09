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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.assertTrue;

public class TestFBDatabaseMetaDataNulls extends FBJUnit4TestBase {

    private static final String CREATE_TABLE = ""
            + "CREATE TABLE test_nulls("
            + "  id INTEGER NOT NULL PRIMARY KEY, "
            + "  char_value VARCHAR(20)"
            + ")";

    private static final String INSERT_VALUES =
            "INSERT INTO test_nulls VALUES(?, ?)";

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        executeCreateTable(connection, CREATE_TABLE);

        try (PreparedStatement ps = connection.prepareStatement(INSERT_VALUES)) {
            ps.setInt(1, 1);
            ps.setString(2, "a");
            ps.execute();

            ps.setInt(1, 2);
            ps.setNull(2, Types.VARCHAR);
            ps.execute();
        }
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testNullAreSortedAtStartEnd() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (Statement stmt = connection.createStatement()) {
            boolean[][] sorting = new boolean[2][2];

            ResultSet rs;

            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 ASC");
            assertTrue("Should select a record", rs.next());
            sorting[0][0] = rs.getString(1) == null;
            assertTrue("Should select a record", rs.next());
            sorting[0][1] = rs.getString(1) == null;

            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 DESC");
            assertTrue("Should select a record", rs.next());
            sorting[1][0] = rs.getString(1) == null;
            assertTrue("Should select a record", rs.next());
            sorting[1][1] = rs.getString(1) == null;

            assertTrue("nullsAreSortedAtEnd is not correct.",
                    metaData.nullsAreSortedAtEnd() && (sorting[0][1] && sorting[1][1]) ||
                            !metaData.nullsAreSortedAtEnd() && !(sorting[0][1] && sorting[1][1]));

            assertTrue("nullsAreSortedAtStart is not correct.",
                    metaData.nullsAreSortedAtStart() && (sorting[0][0] && sorting[1][0]) ||
                            !metaData.nullsAreSortedAtStart() && !(sorting[0][0] && sorting[1][0]));

            assertTrue("nullsAreSortedHigh is not correct.",
                    metaData.nullsAreSortedHigh() && (sorting[0][1] && sorting[1][0]) ||
                            !metaData.nullsAreSortedHigh() && (sorting[0][0] && sorting[1][1]) ||
                            (!metaData.nullsAreSortedHigh() && (metaData.nullsAreSortedAtEnd()
                                    || metaData.nullsAreSortedAtStart()))
            );

            assertTrue("nullsAreSortedLow is not correct.",
                    metaData.nullsAreSortedLow() && (sorting[0][0] && sorting[1][1]) ||
                            !metaData.nullsAreSortedLow() && (metaData.nullsAreSortedAtEnd()
                                    || metaData.nullsAreSortedAtStart()));
        }
    }
}
