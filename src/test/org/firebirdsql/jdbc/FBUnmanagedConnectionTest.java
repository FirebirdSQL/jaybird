/*
 SPDX-FileCopyrightText: Copyright 2001-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
