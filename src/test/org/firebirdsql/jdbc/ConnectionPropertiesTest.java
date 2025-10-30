// SPDX-FileCopyrightText: Copyright 2019-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for connection properties (does not cover all properties for now)
 *
 * @author Mark Rotteveel
 */
class ConnectionPropertiesTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase =
            UsesDatabaseExtension.usesDatabaseForAll(getDbInitStatements());

    private static List<String> getDbInitStatements() {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsSchemas()) {
            return List.of(
                    "create schema OTHER_SCHEMA",
                    "create table PUBLIC.TEST_TABLE (ID integer)",
                    "create table OTHER_SCHEMA.TEST_TABLE (OTHER_ID bigint)");
        }
        return List.of();
    }

    @Test
    void testProperty_defaultIsolation_onDataSource() throws Exception {
        FBSimpleDataSource ds = createDataSource();

        ds.setDefaultIsolation("TRANSACTION_SERIALIZABLE");

        try (Connection connection = ds.getConnection()) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    @Test
    void testProperty_defaultIsolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("defaultIsolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    @Test
    void testProperty_isolation_onDriverManager() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // alias for defaultIsolation
        props.setProperty("isolation", "TRANSACTION_SERIALIZABLE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation(),
                    "Unexpected isolation level");
        }
    }

    @ParameterizedTest
    @MethodSource("searchPathTestCases")
    void testProperty_searchPath_onDriverManager(String searchPath, String expectedSearchPath, String expectedSchema,
            String expectedColumn) throws Exception {
        assumeSchemaSupport();
        try (Connection connection = getConnectionViaDriverManager(PropertyNames.searchPath, searchPath)) {
            verifySearchPath(connection, expectedSearchPath, expectedSchema, expectedColumn);
        }
    }

    @ParameterizedTest
    @MethodSource("searchPathTestCases")
    void testProperty_searchPath_onataSource(String searchPath, String expectedSearchPath, String expectedSchema,
            String expectedColumn) throws Exception {
        assumeSchemaSupport();
        FBSimpleDataSource ds = createDataSource();

        ds.setSearchPath(searchPath);

        try (var connection = ds.getConnection()) {
            verifySearchPath(connection, expectedSearchPath, expectedSchema, expectedColumn);
        }
    }

    private static void verifySearchPath(Connection connection, String expectedSearchPath, String expectedSchema,
            String expectedColumn) throws Exception {
        connection.setAutoCommit(false);
        try (var stmt = connection.createStatement()) {
            try (var rs = stmt.executeQuery(
                    "select rdb$get_context('SYSTEM', 'SEARCH_PATH') from SYSTEM.RDB$DATABASE")) {
                assertNextRow(rs);
                assertEquals(expectedSearchPath, rs.getString(1), "unexpected search path");
            }
        }
        try (var pstmt = connection.prepareStatement("select * from TEST_TABLE")) {
            ResultSetMetaData metaData = pstmt.getMetaData();
            assertEquals("TEST_TABLE", metaData.getTableName(1), "tableName");
            assertEquals(expectedSchema, metaData.getSchemaName(1), "schemaName");
            assertEquals(expectedColumn, metaData.getColumnName(1), "columnName");
        }
    }

    static Stream<Arguments> searchPathTestCases() {
        return Stream.of(
                Arguments.arguments(null, "\"PUBLIC\", \"SYSTEM\"", "PUBLIC", "ID"),
                Arguments.arguments("PUBLIC", "\"PUBLIC\", \"SYSTEM\"", "PUBLIC", "ID"),
                Arguments.arguments("OTHER_SCHEMA", "\"OTHER_SCHEMA\", \"SYSTEM\"", "OTHER_SCHEMA", "OTHER_ID"),
                Arguments.arguments("PUBLIC, OTHER_SCHEMA", "\"PUBLIC\", \"OTHER_SCHEMA\", \"SYSTEM\"", "PUBLIC", "ID"),
                Arguments.arguments("OTHER_SCHEMA, PUBLIC", "\"OTHER_SCHEMA\", \"PUBLIC\", \"SYSTEM\"", "OTHER_SCHEMA",
                        "OTHER_ID"));
    }


    private FBSimpleDataSource createDataSource() {
        return configureDefaultDbProperties(new FBSimpleDataSource());
    }
}
