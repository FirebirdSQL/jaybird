// SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;
import static org.firebirdsql.common.FBTestProperties.resolveSchema;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBDatabaseMetaData} for table related metadata.
 * 
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataTablesTest {

    // Valid values for TABLE_TYPE (separate from those defined in FBDatabaseMetaData for testing)
    private static final String VIEW = "VIEW";
    private static final String TABLE = "TABLE";
    private static final String SYSTEM_TABLE = "SYSTEM TABLE";
    private static final String GLOBAL_TEMPORARY = "GLOBAL TEMPORARY";

    private static final String CREATE_NORMAL_TABLE = """
            CREATE TABLE test_normal_table (
                id INTEGER PRIMARY KEY,
                varchar_field VARCHAR(100)
            )""";

    private static final String CREATE_QUOTED_NORMAL_TABLE = """
            CREATE TABLE "test_quoted_normal_table" (
                id INTEGER PRIMARY KEY,
                varchar_field VARCHAR(100)
            )""";

    private static final String CREATE_QUOTED_WITH_SLASH_NORMAL_TABLE = """
            CREATE TABLE "testquotedwith\\table" (
                id INTEGER PRIMARY KEY,
                varchar_field VARCHAR(100)
            )""";

    private static final String CREATE_NORMAL_VIEW = """
            CREATE VIEW test_normal_view (id, varchar_1, varchar_2)
            AS
            SELECT t1.id, t1.varchar_field, t2.varchar_field
            FROM test_normal_table t1
            INNER JOIN "test_quoted_normal_table" t2 ON t1.id = t2.id""";

    private static final String CREATE_QUOTED_NORMAL_VIEW = """
            CREATE VIEW "test_quoted_normal_view" (id, varchar_1, varchar_2)
            AS
            SELECT t1.id, t1.varchar_field, t2.varchar_field
            FROM test_normal_table t1
            INNER JOIN "test_quoted_normal_table" t2 ON t1.id = t2.id""";

    private static final String CREATE_GTT_ON_COMMIT_DELETE = """
            create global temporary table test_gtt_on_commit_delete (
                id INTEGER PRIMARY KEY,
                varchar_field VARCHAR(100)
            ) on commit delete rows""";

    private static final String CREATE_GTT_ON_COMMIT_PRESERVE = """
            create global temporary table test_gtt_on_commit_preserve (
                id INTEGER PRIMARY KEY,
                varchar_field VARCHAR(100)
            ) on commit delete rows""";

    private static final String CREATE_OTHER_SCHEMA = "create schema OTHER_SCHEMA";

    private static final String CREATE_OTHER_SCHEMA_NORMAL_TABLE2 = """
            create table OTHER_SCHEMA.TEST_NORMAL_TABLE2 (
                ID integer primary key,
                VARCHAR_FIELD varchar(100)
            )""";

    private static final MetadataResultSetDefinition getTablesDefinition =
            new MetadataResultSetDefinition(TableMetaData.class);

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
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
    
    private static List<String> getCreateStatements() {
        List<String> createStatements = new ArrayList<>(Arrays.asList(
                CREATE_NORMAL_TABLE,
                CREATE_QUOTED_NORMAL_TABLE,
                CREATE_QUOTED_WITH_SLASH_NORMAL_TABLE,
                CREATE_NORMAL_VIEW,
                CREATE_QUOTED_NORMAL_VIEW));
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsGlobalTemporaryTables()) {
            createStatements.add(CREATE_GTT_ON_COMMIT_DELETE);
            createStatements.add(CREATE_GTT_ON_COMMIT_PRESERVE);
        }
        if (supportInfo.supportsSchemas()) {
            createStatements.addAll(List.of(
                    CREATE_OTHER_SCHEMA,
                    CREATE_OTHER_SCHEMA_NORMAL_TABLE2));
        }
        return createStatements;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getTables().
     */
    @Test
    void testTableMetaDataColumns() throws Exception {
        try (ResultSet tables = dbmd.getTables(null, null, "doesnotexist", null)) {
            getTablesDefinition.validateResultSetColumns(tables);
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            <NIL>,         <NIL>
            %,             <NIL>
            <NIL>,         %
            %,             %
            """)
    void testTableMetaData_everything_types_null(String schemaPattern, String tableNamePattern) throws Exception {
        validateTableMetaData_everything(schemaPattern, tableNamePattern, null);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            <NIL>,         <NIL>
            %,             <NIL>
            <NIL>,         %
            %,             %
            """)
    void testTableMetaData_everything_allTypes(String schemaPattern, String tableNamePattern) throws Exception {
        validateTableMetaData_everything(schemaPattern, tableNamePattern,
                new String[] { SYSTEM_TABLE, TABLE, VIEW, GLOBAL_TEMPORARY });
    }

    private void validateTableMetaData_everything(String schemaPattern, String tableNamePattern, String[] types)
            throws Exception {
        // Expected user tables + a selection of expected system tables (some that existed in Firebird 1.0)
        // TODO Add test for order?
        Set<String> expectedTables = new HashSet<>(Arrays.asList("TEST_NORMAL_TABLE",
                "test_quoted_normal_table", "testquotedwith\\table", "TEST_NORMAL_VIEW", "test_quoted_normal_view",
                "RDB$FIELDS", "RDB$GENERATORS", "RDB$ROLES", "RDB$DATABASE", "RDB$TRIGGERS"));
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsGlobalTemporaryTables()) {
            expectedTables.add("TEST_GTT_ON_COMMIT_DELETE");
            expectedTables.add("TEST_GTT_ON_COMMIT_PRESERVE");
        }
        if (supportInfo.supportsSchemas()) {
            expectedTables.add("TEST_NORMAL_TABLE2");
        }
        try (ResultSet tables = dbmd.getTables(null, schemaPattern, tableNamePattern, types)) {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
                assertThat("TABLE_NAME is not allowed to be null or empty", tableName, not(emptyString()));
                expectedTables.remove(tableName);

                updateTableRules(tableName, rules);

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertThat("getTables() did not return some expected tables", expectedTables, is(empty()));
        }
    }

    /**
     * Tests getTables with a tableName all-pattern and types SYSTEM TABLES, expecting only system tables to be
     * returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            <NIL>,         <NIL>
            %,             %
            SYSTEM,        <NIL>
            SYSTEM,        %
            """)
    void testTableMetaData_allSystemTables_tableName_null(String schemaPattern, String tableNamePattern)
            throws Exception {
        validateTableMetaData_allSystemTables(schemaPattern, tableNamePattern);
    }

    /**
     * Helper method for test methods that retrieve table metadata of all system tables.
     * 
     * @param tableNamePattern
     *            Pattern for the tableName (should be null or "%" only for this test)
     */
    private void validateTableMetaData_allSystemTables(String schemaPattern, String tableNamePattern) throws Exception {
        // Expected selection of expected system tables (some that existed in Firebird 1.0); we don't check all system tables
        Set<String> expectedTables = new HashSet<>(Arrays.asList("RDB$FIELDS", "RDB$GENERATORS",
                "RDB$ROLES", "RDB$DATABASE", "RDB$TRIGGERS"));
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_SCHEM, ifSchemaElse("SYSTEM", null));
        rules.put(TableMetaData.TABLE_TYPE, SYSTEM_TABLE);
        try (ResultSet tables = dbmd.getTables(null, resolveSchema(schemaPattern), tableNamePattern,
                new String[] { SYSTEM_TABLE })) {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertThat("TABLE_NAME is not allowed to be null or empty", tableName, not(emptyString()));
                expectedTables.remove(tableName);

                assertThat("Only expect table names starting with RDB$, MON$ or SEC$",
                        tableName, anyOf(startsWith("RDB$"), startsWith("MON$"), startsWith("SEC$")));

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertThat("getTables() did not return some expected tables", expectedTables, is(empty()));
        }
    }

    /**
     * Tests getTables with tableName all-pattern and types TABLE, expecting only normal tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the normal tables
     * <p>
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "%")
    void testTableMetaData_allNormalTables(String tableNamePattern) throws Exception {
        Set<String> expectedNormalTables = new HashSet<>(Arrays.asList("TEST_NORMAL_TABLE",
                "test_quoted_normal_table", "testquotedwith\\table"));
        if (getDefaultSupportInfo().supportsSchemas()) {
            expectedNormalTables.add("TEST_NORMAL_TABLE2");
        }
        Set<String> retrievedTables = new HashSet<>();
        try (ResultSet tables = dbmd.getTables(null, null, tableNamePattern, new String[] { TABLE })) {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertThat("TABLE_NAME is not allowed to be null or empty", tableName, not(emptyString()));
                retrievedTables.add(tableName);

                assertThat("Only expect normal tables, not starting with RDB$, MON$ or SEC$",
                        tableName, not(anyOf(startsWith("RDB$"), startsWith("MON$"), startsWith("SEC$"))));
                Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
                updateTableRules(tableName, rules);

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertEquals(expectedNormalTables, retrievedTables, "getTables() did not return expected tables");
        }
    }

    /**
     * Tests getTables with tableName all-pattern and types VIEW, expecting only views to be returned.
     * <p>
     * This method only checks the existence of a subset of the views
     * <p>
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "%")
    void testTableMetaData_allViews_tableName_null(String tableNamePattern) throws Exception {
        Set<String> expectedViews = new HashSet<>(Arrays.asList("TEST_NORMAL_VIEW", "test_quoted_normal_view"));
        Set<String> retrievedTables = new HashSet<>();
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_TYPE, VIEW);
        try (ResultSet tables = dbmd.getTables(null, null, tableNamePattern, new String[] { VIEW })) {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertThat("TABLE_NAME is not allowed to be null or empty", tableName, not(emptyString()));
                retrievedTables.add(tableName);

                assertThat("Only expect views, not starting with RDB$, MON$ or SEC$",
                        tableName, not(anyOf(startsWith("RDB$"), startsWith("MON$"), startsWith("SEC$"))));

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertEquals(expectedViews, retrievedTables, "getTables() did not return expected tables");
        }
    }

    /**
     * Tests getTables retrieving normal table that was created unquoted using
     * its lower case name with types null (should produce no rows)
     */
    @Test
    void testTableMetaData_NormalUnquotedTable_lowercase_typesNull() throws Exception {
        validateTableMetaDataNoRow("test_normal_table", null);
    }

    @Test
    void testTableMetaData_QuotedWithSlash_unescaped() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "testquotedwith\\table");

        validateTableMetaDataSingleRow("testquotedwith\\table", null, validationRules);
    }

    @Test
    void testTableMetaData_QuotedWithSlash_escaped() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "testquotedwith\\table");

        validateTableMetaDataSingleRow("testquotedwith\\\\table", null, validationRules);
    }

    @Test
    void testTableMetaData_NormalUnquotedTable_uppercase_typesNull() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("TEST_NORMAL_TABLE", null, validationRules);
    }

    @Test
    void testTableMetaData_NormalUnquotedTable_uppercase_escaped_typesNull() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("TEST\\_NORMAL\\_TABLE", null, validationRules);
    }

    @Test
    void testTableMetaData_NormalUnquotedTable_uppercase_wildcard_typesNull() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("TEST\\_NORMAL\\_T%", null, validationRules);
    }

    /**
     * Tests getTables retrieving normal table that was created unquoted using
     * its upper case name with types TABLE.
     */
    @Test
    void testTableMetaData_NormalUnquotedTable_uppercase_typesTABLE() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("TEST_NORMAL_TABLE", new String[] { TABLE }, validationRules);
    }

    /**
     * Tests getTables retrieving normal table that was created quoted using its
     * normal case name with all types.
     */
    @Test
    void testTableMetaData_NormalQuotedTable_AllTypes() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "test_quoted_normal_table");

        validateTableMetaDataSingleRow("test_quoted_normal_table", new String[] { SYSTEM_TABLE, TABLE, VIEW }, validationRules);
    }

    /**
     * Tests getTables retrieving normal table that was created unquoted using
     * its upper case name with types TABLE.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "%", "OTHER_SCHEMA", "OTHER\\_SCHEMA", "OTHER%" })
    void testTableMetaData_OtherSchemaNormalTable_typesTABLE(String schemaPattern) throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsSchemas, "Test requires schema support");
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_SCHEM, "OTHER_SCHEMA");
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE2");

        validateTableMetaDataSingleRow(schemaPattern, "TEST_NORMAL_TABLE2", new String[] { TABLE }, validationRules);
    }

    private void validateTableMetaDataSingleRow(String tableNamePattern, String[] types,
            Map<TableMetaData, Object> validationRules) throws Exception {
        validateTableMetaDataSingleRow(ifSchemaElse("PUBLIC", ""), tableNamePattern, types, validationRules);
    }

    private void validateTableMetaDataSingleRow(String schemaPattern, String tableNamePattern, String[] types,
            Map<TableMetaData, Object> validationRules) throws Exception {
        getTablesDefinition.checkValidationRulesComplete(validationRules);
        try (ResultSet tables = dbmd.getTables(null, schemaPattern, tableNamePattern, types)) {
            assertTrue(tables.next(), "Expected row in table metadata");
            getTablesDefinition.validateRowValues(tables, validationRules);
            assertFalse(tables.next(), "Expected only one row in result set");
        }
    }

    /**
     * Tests getTables retrieving views, specifying the name of a normal table:
     * expecting empty resultset
     */
    @Test
    void testTableMetaData_NormalUnquotedTable_typesVIEW() throws Exception {
        validateTableMetaDataNoRow("test_normal_table", new String[] { VIEW });
    }

    /**
     * Tests getTables retrieving normal tables, specifying the name of a system
     * table: expecting empty resultset
     */
    @Test
    void testTableMetaData_SystemTable_typesTABLE() throws Exception {
        validateTableMetaDataNoRow("RDB$DATABASE", new String[] { TABLE });
    }

    /**
     * Tests getTables retrieving system tables, specifying the name of a view:
     * expecting empty resultset
     */
    @Test
    void testTableMetaData_UnquotedView_typesSYSTEM() throws Exception {
        validateTableMetaDataNoRow("test_normal_view", new String[] { SYSTEM_TABLE });
    }

    @Test
    void testTableMetaData_globalTemporaryTables() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsGlobalTemporaryTables(), "Requires global temporary table support");

        Set<String> expectedGtt = new HashSet<>(Arrays.asList("TEST_GTT_ON_COMMIT_DELETE", "TEST_GTT_ON_COMMIT_PRESERVE"));
        Set<String> retrievedTables = new HashSet<>();
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_TYPE, GLOBAL_TEMPORARY);
        try (ResultSet tables = dbmd.getTables(null, null, null, new String[] { GLOBAL_TEMPORARY })) {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertThat("TABLE_NAME is not allowed to be null or empty", tableName, not(emptyString()));
                retrievedTables.add(tableName);

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertEquals(expectedGtt, retrievedTables, "getTables() did not return expected tables");
        }
    }

    @Test
    void testTableMetaData_exceptSystemTable_sorted() throws Exception {
        List<String> expectedTables = new ArrayList<>();
        if (getDefaultSupportInfo().supportsGlobalTemporaryTables()) {
            expectedTables.add("TEST_GTT_ON_COMMIT_DELETE");
            expectedTables.add("TEST_GTT_ON_COMMIT_PRESERVE");
        }
        if (getDefaultSupportInfo().supportsSchemas()) {
            expectedTables.add("TEST_NORMAL_TABLE2");
        }
        expectedTables.add("TEST_NORMAL_TABLE");
        expectedTables.add("test_quoted_normal_table");
        expectedTables.add("testquotedwith\\table");
        expectedTables.add("TEST_NORMAL_VIEW");
        expectedTables.add("test_quoted_normal_view");
        int indexExpected = 0;
        try (ResultSet tables = dbmd.getTables(null, null, "%", new String[] { TABLE, VIEW, GLOBAL_TEMPORARY })) {
            while (tables.next()) {
                assertThat("More tables than expected", indexExpected, lessThan(expectedTables.size()));
                String expectedTableName = expectedTables.get(indexExpected++);

                Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
                updateTableRules(expectedTableName, rules);

                getTablesDefinition.validateRowValues(tables, rules);
            }

            assertEquals(expectedTables.size(), indexExpected, "getTables() did not return some expected tables");
        }
    }

    /**
     * Helper method for test methods that retrieve metadata expecting no
     * results.
     * 
     * @param tableNamePattern
     *            Pattern of the tablename
     * @param types
     *            Table types to request
     */
    private void validateTableMetaDataNoRow(String tableNamePattern, String[] types) throws Exception {
        try (ResultSet tables = dbmd.getTables(null, null, tableNamePattern, types)) {
            assertFalse(tables.next(), () -> format("Expected empty result set for requesting %s with types %s",
                    tableNamePattern, Arrays.toString(types)));
        }
    }

    private void updateTableRules(String tableName, Map<TableMetaData, Object> rules) {
        rules.put(TableMetaData.TABLE_NAME, tableName);
        if (tableName.startsWith("RDB$") || tableName.startsWith("MON$") || tableName.startsWith("SEC$")) {
            rules.put(TableMetaData.TABLE_SCHEM, ifSchemaElse("SYSTEM", null));
            rules.put(TableMetaData.TABLE_TYPE, SYSTEM_TABLE);
        } else if (tableName.equals("TEST_NORMAL_TABLE") || tableName.equals("test_quoted_normal_table")
                || tableName.equals("testquotedwith\\table")) {
            rules.put(TableMetaData.TABLE_TYPE, TABLE);
        } else if (tableName.equals("TEST_NORMAL_VIEW") || tableName.equals("test_quoted_normal_view")) {
            rules.put(TableMetaData.TABLE_TYPE, VIEW);
        } else if (tableName.startsWith("TEST_GTT")) {
            rules.put(TableMetaData.TABLE_TYPE, GLOBAL_TEMPORARY);
        } else if (tableName.equals("TEST_NORMAL_TABLE2")) {
            rules.put(TableMetaData.TABLE_SCHEM, "OTHER_SCHEMA");
            rules.put(TableMetaData.TABLE_TYPE, TABLE);
        } else {
            // Make sure we don't accidentally miss a table
            fail("Unexpected TABLE_NAME: " + tableName);
        }
    }
    
    // TODO: Add more extensive tests of patterns and combination of table types

    private static final Map<TableMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<TableMetaData, Object> defaults = new EnumMap<>(TableMetaData.class);
        defaults.put(TableMetaData.TABLE_CAT, null);
        defaults.put(TableMetaData.TABLE_SCHEM, ifSchemaElse("PUBLIC", null));
        defaults.put(TableMetaData.REMARKS, null);
        defaults.put(TableMetaData.TYPE_CAT, null);
        defaults.put(TableMetaData.TYPE_SCHEM, null);
        defaults.put(TableMetaData.TYPE_NAME, null);
        // NOTE Self-referencing is for structured types, which Firebird does not provide (See SQL2003 4.14.5)
        defaults.put(TableMetaData.SELF_REFERENCING_COL_NAME, null);
        defaults.put(TableMetaData.REF_GENERATION, null);
        defaults.put(TableMetaData.OWNER_NAME, "SYSDBA");
        defaults.put(TableMetaData.JB_RELATION_ID, MetaDataInfo.IGNORE_DURING_VALIDATION);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<TableMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getTables() metadata.
     */
    private enum TableMetaData implements MetaDataInfo {
        TABLE_CAT(1, String.class), 
        TABLE_SCHEM(2, String.class), 
        TABLE_NAME(3, String.class),
        TABLE_TYPE(4, String.class),
        REMARKS(5, String.class),
        TYPE_CAT(6, String.class),
        TYPE_SCHEM(7, String.class),
        TYPE_NAME(8, String.class),
        SELF_REFERENCING_COL_NAME(9, String.class),
        REF_GENERATION(10, String.class),
        OWNER_NAME(11, String.class),
        JB_RELATION_ID(12, Short.class),
        ;

        private final int position;
        private final Class<?> columnClass;

        TableMetaData(int position, Class<?> columnClass) {
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
