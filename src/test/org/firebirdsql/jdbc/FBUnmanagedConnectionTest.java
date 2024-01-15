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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FBUnmanagedConnectionTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    //@formatter:off
    private static final String CREATE_TEST_TABLE =
        "CREATE TABLE connection_test (" +
        "  test_int INTEGER" +
        ")";

    private static final String INSERT_TEST_TABLE = "INSERT INTO connection_test(test_int) VALUES(1)";

    private static final String SELECT_TEST_TABLE = "SELECT test_int FROM connection_test";
    //@formatter:on

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeQuietly(connection);
    }

    @Test
    void testCommit() throws Exception {
        connection.setAutoCommit(false);
        executeCreateTable(connection, CREATE_TEST_TABLE);
        connection.commit();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(INSERT_TEST_TABLE);
            connection.commit();
            try (ResultSet rs = statement.executeQuery(SELECT_TEST_TABLE)) {
                assertTrue(rs.next(), "ResultSet is empty");
                int value = rs.getInt(1);
                assertEquals(1, value, "Commit failed");
            }
        }
        connection.commit();
    }

    @Test
    void testCreateStatement() throws Exception {
        Statement statement = connection.createStatement();
        assertNotNull(statement, "Statement is null");
    }

    @Test
    void testGetAutoCommit() throws Exception {
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit(), "AutoCommit is false");
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit(), "AutoCommit is true");
    }

    @Test
    void testGetMetaData() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        assertNotNull(metaData, "Metadata is null");
    }

    @Test
    void testGetTypeMap() throws Exception {
        Map<String, Class<?>> typeMap = connection.getTypeMap();
        assertNotNull(typeMap, "TypeMap is null");
    }

    @Test
    void testNativeSQL() throws Exception {
        String nativeSQL = connection.nativeSQL("SELECT * FROM RDB$DATABASE");
        assertNotNull(nativeSQL, "NativeSQL is null");
    }

    /**
     * Make sure commit can be called repeatedly with no work done.
     */
    @Test
    void testCommitsWithNoWork() throws Exception {
        connection.setAutoCommit(false);
        assertDoesNotThrow(connection::commit);
        assertDoesNotThrow(connection::commit);
        assertDoesNotThrow(connection::commit);
    }

}
