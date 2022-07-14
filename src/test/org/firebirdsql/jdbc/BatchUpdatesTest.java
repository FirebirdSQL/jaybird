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

import java.io.StringReader;
import java.sql.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.*;

class BatchUpdatesTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static final String CREATE_TABLE = ""
            + "RECREATE TABLE batch_updates("
            + "  id INTEGER, "
            + "  str_value BLOB, "
            + "  clob_value BLOB SUB_TYPE 1"
            + ")";

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        executeCreateTable(connection, CREATE_TABLE);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if batch updates in {@link Statement} implementation works correctly.
     */
    @Test
    void testStatementBatch() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.addBatch("INSERT INTO batch_updates(id, str_value) VALUES (1, 'test')");
            stmt.addBatch("INSERT INTO batch_updates(id, str_value) VALUES (2, 'another')");
            stmt.addBatch("UPDATE batch_updates SET id = 3 WHERE id = 2");

            int[] updates = stmt.executeBatch();

            assertEquals(3, updates.length, "Should contain 3 results");
            assertArrayEquals(new int[] { 1, 1, 1 }, updates, "Should update one row each time");

            ResultSet rs = stmt.executeQuery("SELECT * FROM batch_updates");
            int counter = 0;
            while (rs.next())
                counter++;

            assertEquals(2, counter, "Should insert 2 rows");

            stmt.addBatch("DELETE FROM batch_updates");
            stmt.clearBatch();
            updates = stmt.executeBatch();

            assertEquals(0, updates.length, "No updates should have been made");
        }
    }

    /**
     * Test if batch updates work correctly with prepared statement.
     */
    @Test
    void testPreparedStatementBatch() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO batch_updates(id, str_value, clob_value) VALUES (?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "test");
            ps.setNull(3, Types.LONGVARBINARY);
            ps.addBatch();

            ps.setInt(1, 3);
            ps.setCharacterStream(2, new StringReader("stream"), 11);
            ps.setString(3, "string");
            ps.addBatch();

            ps.setInt(1, 2);
            ps.setString(2, "another");
            ps.setNull(3, Types.LONGVARBINARY);
            ps.addBatch();

            ps.executeBatch();

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM batch_updates order by id");
                assertTrue(rs.next(), "expected row 1");
                assertEquals(rs.getInt(1), 1, "id=1");
                assertEquals("test", rs.getString(2), "id=1 str_value");
                assertTrue(rs.next(), "expected row 2");
                assertEquals(rs.getInt(1), 2, "id=2");
                assertEquals("another", rs.getString(2), "id=2 str_value");
                assertTrue(rs.next(), "expected row 3");
                assertEquals(rs.getInt(1), 3, "id=3");
                assertEquals("stream", rs.getString(2), "id=3 str_value");
                assertEquals("string", rs.getString(3), "id=3 clob_value");
                assertFalse(rs.next(), "no more rows");
            }
        }
    }
}
