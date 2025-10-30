// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeNoSchemaSupport;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;

/**
 * Tests for {@link FBDatabaseMetaData} for schema related metadata.
 */
class FBDatabaseMetaDataSchemasTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static final MetadataResultSetDefinition getSchemasDefinition =
            new MetadataResultSetDefinition(SchemaMetaData.class);
    public static final List<String> DEFAULT_SCHEMAS = List.of("PUBLIC", "SYSTEM");

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterEach
    void cleanupAdditionalSchemas() throws SQLException {
        if (!getDefaultSupportInfo().supportsSchemas()) return;
        var schemasToDrop = new HashSet<String>();
        try (ResultSet schemas = dbmd.getSchemas()) {
            while (schemas.next()) {
                String schemaName = schemas.getString("TABLE_SCHEM");
                if (DEFAULT_SCHEMAS.contains(schemaName)) continue;
                schemasToDrop.add(schemaName);
            }
        }
        if (schemasToDrop.isEmpty()) return;
        try (var stmt = con.createStatement()) {
            con.setAutoCommit(false);
            for (String schemaName : schemasToDrop) {
                stmt.addBatch("drop schema " + stmt.enquoteIdentifier(schemaName, false));
            }
            stmt.executeBatch();
        } finally {
            con.setAutoCommit(true);
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            dbmd = null;
        }
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getSchemas(...).
     */
    @Test
    void testSchemaMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getSchemas(null, "doesnotexist")) {
            getSchemasDefinition.validateResultSetColumns(columns);
        }
    }

    @Test
    void getSchemas_noSchemaSupport_noRows() throws Exception {
        assumeNoSchemaSupport();
        ResultSet schemas = dbmd.getSchemas();
        assertNoNextRow(schemas);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "%")
    void getSchemas_string_string_noSchemaSupport_noRows(String schemaPattern) throws Exception {
        assumeNoSchemaSupport();
        validateSchemaMetaDataNoRow(null, schemaPattern);
    }

    @Test
    void getSchemas_string_string_emptySchemaPattern_noRows() throws Exception {
        // No rows expected with and without schema support
        validateSchemaMetaDataNoRow(null, "");
    }

    @Test
    void getSchemas_schemaSupport_defaults() throws Exception {
        assumeSchemaSupport();
        try (ResultSet schemas = dbmd.getSchemas()) {
            // calling getSchemas() is equivalent to calling getSchemas(null, null)
            validateSchemaMetaData(null, schemas, DEFAULT_SCHEMAS);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "%")
    void getSchemas_string_string_schemaSupport_defaults(String schemaPattern) throws Exception {
        assumeSchemaSupport();
        validateSchemaMetaData(null, schemaPattern, List.of("PUBLIC", "SYSTEM"));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            schemaPattern, expectedSchema
            PUBLIC,        PUBLIC
            SYSTEM,        SYSTEM
            PUB%,          PUBLIC
            SYS%,          SYSTEM
            PUBL_C,        PUBLIC
            S_STEM,        SYSTEM
            """)
    void getSchemas_string_string_schemaSupport_singleSchemaExpected(String schemaPattern, String expectedSchemaName)
            throws Exception {
        assumeSchemaSupport();
        validateSchemaMetaData(null, schemaPattern, List.of(expectedSchemaName));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getSchemas_string_string_schemaSupport_catalogNullOrEmpty_defaults(String catalog) throws Exception {
        assumeSchemaSupport();
        validateSchemaMetaData(catalog, "%", DEFAULT_SCHEMAS);
    }

    @Test
    void getSchemas_string_string_catalogNonEmpty_noRows() throws Exception {
        // Should return no rows with or without schema support
        validateSchemaMetaDataNoRow("NON_EMPTY", null);
    }

    @Test
    void getSchema_string_string_schemaSupport_returnsUserDefinedSchemas() throws Exception {
        assumeSchemaSupport();
        try (var stmt = con.createStatement()) {
            con.setAutoCommit(false);
            for (String schema : List.of("ABC", "QRS", "TUV")) {
                stmt.execute("create schema " + schema);
            }
        } finally {
            con.setAutoCommit(true);
        }
        validateSchemaMetaData("", "%", List.of("ABC", "PUBLIC", "QRS", "SYSTEM", "TUV"));
    }

    /**
     * Helper method for test methods that retrieve metadata expecting no results.
     *
     * @param schemaPattern
     *         pattern of the schema name
     */
    private void validateSchemaMetaDataNoRow(String catalog, String schemaPattern) throws SQLException {
        try (ResultSet schemas = dbmd.getSchemas(catalog, schemaPattern)) {
            assertNoNextRow(schemas, "Expected empty result set for requesting " + schemaPattern);
        }
    }

    /**
     * Helper method for test methods that retrieve metadata expecting schemas.
     *
     * @param schemaPattern
     *         pattern of the schema name
     * @param expectedSchemaNames
     *         expected schema names in order of appearance
     */
    private void validateSchemaMetaData(String catalog, String schemaPattern, List<String> expectedSchemaNames)
            throws SQLException {
        try (ResultSet schemas = dbmd.getSchemas(catalog, schemaPattern)) {
            validateSchemaMetaData(schemaPattern, schemas, expectedSchemaNames);
        }
    }

    /**
     * Helper method for test methods that retrieve metadata expecting schemas.
     *
     * @param schemaPattern
     *         pattern of the schema name (for diagnostics only)
     * @param schemas
     *         schema result set as returned by one of the database metadata {@code getSchema} methods
     * @param expectedSchemaNames
     *         expected schema names in order of appearance
     */
    private static void validateSchemaMetaData(String schemaPattern, ResultSet schemas,
            List<String> expectedSchemaNames) throws SQLException {
        for (String expectedSchemaName : expectedSchemaNames) {
            assertNextRow(schemas,
                    "Pattern '%s', expected row for schema name %s".formatted(schemaPattern, expectedSchemaName));
            Map<SchemaMetaData, Object> valueRules = getDefaultValueValidationRules();
            valueRules.put(SchemaMetaData.TABLE_SCHEM, expectedSchemaName);
            getSchemasDefinition.validateRowValues(schemas, valueRules);
        }
        assertNoNextRow(schemas, "Expected no more schema names for pattern " + schemaPattern);
    }

    private static final Map<SchemaMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<SchemaMetaData, Object> defaults = new EnumMap<>(SchemaMetaData.class);
        defaults.put(SchemaMetaData.TABLE_CATALOG, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<SchemaMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getTables() metadata.
     */
    private enum SchemaMetaData implements MetaDataInfo {
        TABLE_SCHEM(1, String.class),
        TABLE_CATALOG(2, String.class),
        ;

        private final int position;
        private final Class<?> columnClass;

        SchemaMetaData(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public Class<?> getColumnClass() {
            return columnClass;
        }
    }
}
