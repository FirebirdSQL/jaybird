// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension.UsesDatabaseForAll;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.util.SearchPathHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeNoSchemaSupport;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FBConnection#setSchema(String)} and {@link FBConnection#getSchema()}, and other schema-specific
 * tests of {@link FBConnection}.
 */
class FBConnectionSchemaTest {

    @RegisterExtension
    static UsesDatabaseForAll usesDatabaseForAll = UsesDatabaseExtension.usesDatabaseForAll(dbInitStatements());

    private static List<String> dbInitStatements() {
        if (getDefaultSupportInfo().supportsSchemas()) {
            return List.of(
                    "create schema SCHEMA_1",
                    "create schema \"case_sensitive\"",
                    "create table PUBLIC.TABLE_ONE (IN_PUBLIC integer)",
                    "create table SCHEMA_1.TABLE_ONE (IN_SCHEMA_1 integer)",
                    "create table \"case_sensitive\".TABLE_ONE (\"IN_case_sensitive\" integer)");
        }
        return List.of(
                "create table TABLE_ONE (IN_ integer)");
    }

    @Test
    void getSchema_noSupport_returnsNull() throws Exception {
        assumeNoSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            assertNull(connection.getSchema(), "schema");
            checkSchemaResolution(connection, "");
            assertNull(connection.getSearchPath(), "searchPath");
            assertThat("searchPathList", connection.getSearchPathList(), is(empty()));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "PUBLIC", "SYSTEM" })
    void setSchema_noSupport_ignoresValue(String schemaName) throws Exception {
        assumeNoSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            assertDoesNotThrow(() -> connection.setSchema(schemaName));
            assertNull(connection.getSchema(), "schema always null");
            checkSchemaResolution(connection, "");
            assertNull(connection.getSearchPath(), "searchPath");
            assertThat("searchPathList", connection.getSearchPathList(), is(empty()));
        }
    }

    @Test
    void getSchema_default_PUBLIC() throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            assertEquals("PUBLIC", connection.getSchema(), "schema");
            checkSchemaResolution(connection, "PUBLIC");
            assertEquals(List.of("PUBLIC", "SYSTEM"), connection.getSearchPathList(), "searchPathList");
            assertEquals("\"PUBLIC\", \"SYSTEM\"", connection.getSearchPath(), "searchPath");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void setSchema_nullOrBlank_notAccepted(String schemaName) throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            var exception = assertThrows(SQLDataException.class, () -> connection.setSchema(schemaName));
            assertEquals("schema must be non-null and not blank", exception.getMessage(), "message");
            assertEquals(SQLStateConstants.SQL_STATE_INVALID_USE_NULL, exception.getSQLState(), "SQLState");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "PUBLIC", "SCHEMA_1", "case_sensitive" })
    void setSchema_exists(String schemaName) throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            connection.setSchema(schemaName);
            assertEquals(schemaName, connection.getSchema(), "schema after schema change");
            checkSchemaResolution(connection, schemaName);
            // We leave the search path unmodified if the schema is already the first
            final var expectedSearchPathList = "PUBLIC".equals(schemaName)
                    ? List.of("PUBLIC", "SYSTEM")
                    : List.of(schemaName, "PUBLIC", "SYSTEM");
            assertEquals(expectedSearchPathList, connection.getSearchPathList(), "searchPathList");
            final String expectedSearchPath =
                    SearchPathHelper.toSearchPath(expectedSearchPathList, QuoteStrategy.DIALECT_3);
            assertEquals(expectedSearchPath, connection.getSearchPath(), "searchPath");
        }
    }

    @Test
    void setSchema_SYSTEM_first() throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            connection.setSchema("SYSTEM");
            assertEquals("SYSTEM", connection.getSchema(), "schema after schema change");
            // We're prepending the schema, leaving schemas later in the list untouched
            final var expectedSearchPathList = List.of("SYSTEM", "PUBLIC", "SYSTEM");
            assertEquals(expectedSearchPathList, connection.getSearchPathList(), "searchPathList");
            final String expectedSearchPath = "\"SYSTEM\", \"PUBLIC\", \"SYSTEM\"";
            assertEquals(expectedSearchPath, connection.getSearchPath(), "searchPath");
        }
    }

    @Test
    void setSchema_doesNotExist() throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            assertDoesNotThrow(() -> connection.setSchema("DOES_NOT_EXIST"));
            assertEquals("PUBLIC", connection.getSchema(),
                    "current schema not changed after setting non-existent schema");
            // non-existent schema is included in the search path
            final var expectedSearchPathList = List.of("DOES_NOT_EXIST", "PUBLIC", "SYSTEM");
            assertEquals(expectedSearchPathList, connection.getSearchPathList(), "searchPathList");
            final String expectedSearchPath = "\"DOES_NOT_EXIST\", \"PUBLIC\", \"SYSTEM\"";
            assertEquals(expectedSearchPath, connection.getSearchPath(), "searchPath");
        }
    }

    @Test
    void setSchema_sequenceOfInvocations() throws Exception {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager()) {
            connection.setSchema("SCHEMA_1");
            assertEquals("SCHEMA_1", connection.getSchema(), "schema after SCHEMA_1");
            assertEquals(List.of("SCHEMA_1", "PUBLIC", "SYSTEM"), connection.getSearchPathList(),
                    "searchPathList after SCHEMA_1");
            checkSchemaResolution(connection, "SCHEMA_1");

            connection.setSchema("DOES_NOT_EXIST");
            assertEquals("PUBLIC", connection.getSchema(), "schema after DOES_NOT_EXIST");
            assertEquals(List.of("DOES_NOT_EXIST", "PUBLIC", "SYSTEM"), connection.getSearchPathList(),
                    "searchPathList after DOES_NOT_EXIST");
            checkSchemaResolution(connection, "PUBLIC");

            connection.setSchema("case_sensitive");
            assertEquals("case_sensitive", connection.getSchema(), "schema after case_sensitive");
            assertEquals(List.of("case_sensitive", "PUBLIC", "SYSTEM"), connection.getSearchPathList(),
                    "searchPathList after case_sensitive");
            checkSchemaResolution(connection, "case_sensitive");

            connection.setSchema("PUBLIC");
            assertEquals("PUBLIC", connection.getSchema(), "schema after PUBLIC");
            assertEquals(List.of("PUBLIC", "PUBLIC", "SYSTEM"), connection.getSearchPathList(),
                    "searchPathList after PUBLIC");
            checkSchemaResolution(connection, "PUBLIC");
        }
    }

    // There is some overlap with the searchPath tests in ConnectionPropertiesTest
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            searchPath,       expectedSchema, expectedSearchPath
            PUBLIC,           PUBLIC,         '"PUBLIC", "SYSTEM"'
            'PUBLIC, SYSTEM', PUBLIC,         '"PUBLIC", "SYSTEM"'
            public,           PUBLIC,         '"PUBLIC", "SYSTEM"'
            "public",         SYSTEM,         '"public", "SYSTEM"'
            SCHEMA_1,         SCHEMA_1,       '"SCHEMA_1", "SYSTEM"'
            "case_sensitive", case_sensitive, '"case_sensitive", "SYSTEM"'
            # NOTE Unquoted!
            case_sensitive,   SYSTEM,         '"CASE_SENSITIVE", "SYSTEM"'
            'SCHEMA_1, "case_sensitive", SYSTEM, PUBLIC', SCHEMA_1, '"SCHEMA_1", "case_sensitive", "SYSTEM", "PUBLIC"'
            """)
    void connectionSearchPath(String searchPath, String expectedSchema, String expectedSearchPath) throws SQLException {
        assumeSchemaSupport();
        try (var connection = getConnectionViaDriverManager(PropertyNames.searchPath, searchPath)) {
            assertEquals(expectedSchema, connection.getSchema(), "schema");
            assertEquals(expectedSearchPath, connection.getSearchPath(), "searchPath");
            assertEquals(SearchPathHelper.parseSearchPath(expectedSearchPath), connection.getSearchPathList(),
                    "searchPathList");
        }
    }

    private static void checkSchemaResolution(Connection connection, String expectedSchema) throws SQLException {
        try (var pstmt = connection.prepareStatement("select * from TABLE_ONE")) {
            ResultSetMetaData rsmd = pstmt.getMetaData();
            assertEquals(expectedSchema, rsmd.getSchemaName(1), "column schemaName");
            assertEquals("TABLE_ONE", rsmd.getTableName(1), "column tableName");
            assertEquals("IN_" + expectedSchema, rsmd.getColumnName(1), "column name");
        }
    }

}
