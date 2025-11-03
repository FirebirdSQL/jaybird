// SPDX-FileCopyrightText: Copyright 2019-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getFunctions(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataFunctionsTest {

    private static final String CREATE_UDF_EXAMPLE = """
            declare external function UDF$EXAMPLE
            int by descriptor, int by descriptor
            returns int by descriptor
            entry_point 'idNvl' module_name 'fbudf'""";

    private static final String ADD_COMMENT_ON_UDF_EXAMPLE =
            "comment on external function UDF$EXAMPLE is 'Comment on UDF$EXAMPLE'";

    private static final String CREATE_PSQL_EXAMPLE = """
            create function PSQL$EXAMPLE(X int) returns int
            as
            begin
              return X+1;
            end""";

    // Same name, different schema as CREATE_PSQL_EXAMPLE
    private static final String CREATE_OTHER_SCHEMA_PSQL_EXAMPLE = """
            create function OTHER_SCHEMA.PSQL$EXAMPLE(X double precision) returns varchar(50)
            as
            begin
              return cast(x as varchar(50));
            end""";

    private static final String CREATE_OTHER_SCHEMA_PSQL_EXAMPLE2 = """
            create function OTHER_SCHEMA.PSQL$EXAMPLE2(X int) returns int
            as
            begin
              return X+1;
            end""";

    private static final String ADD_COMMENT_ON_PSQL_EXAMPLE =
            "comment on function PSQL$EXAMPLE is 'Comment on PSQL$EXAMPLE'";

    private static final String CREATE_PACKAGE_WITH_FUNCTION = """
            create package WITH$FUNCTION
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER;
            end""";

    private static final String CREATE_PACKAGE_BODY_WITH_FUNCTION = """
            create package body WITH$FUNCTION
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER
              as
              begin
                return PARAM1 + 1;
              end
            end""";

    private static final String CREATE_OTHER_SCHEMA = "create schema OTHER_SCHEMA";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static final MetadataResultSetDefinition getFunctionsDefinition =
            new MetadataResultSetDefinition(FunctionMetaData.class);

    private static Connection con;
    private static DatabaseMetaData originalDbmd;
    // may get replaced during a test
    private DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        originalDbmd = con.getMetaData();
    }

    @BeforeEach
    void setup() {
        dbmd = originalDbmd;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            originalDbmd = null;
        }
    }

    private static List<String> getCreateStatements() {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        List<String> statements = new ArrayList<>();
        statements.add(CREATE_UDF_EXAMPLE);
        if (supportInfo.supportsComment()) {
            statements.add(ADD_COMMENT_ON_UDF_EXAMPLE);
        }
        if (supportInfo.supportsPsqlFunctions()) {
            statements.add(CREATE_PSQL_EXAMPLE);
            if (supportInfo.supportsComment()) {
                statements.add(ADD_COMMENT_ON_PSQL_EXAMPLE);
            }
            if (supportInfo.supportsPackages()) {
                // Functions in packages should not show up in results
                statements.add(CREATE_PACKAGE_WITH_FUNCTION);
                statements.add(CREATE_PACKAGE_BODY_WITH_FUNCTION);
            }
        }
        if (supportInfo.supportsSchemas()) {
            statements.add(CREATE_OTHER_SCHEMA);
            statements.add(CREATE_OTHER_SCHEMA_PSQL_EXAMPLE);
            statements.add(CREATE_OTHER_SCHEMA_PSQL_EXAMPLE2);
        }

        // TODO See if we can add a UDR example as well.
        return statements;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getFunctions().
     */
    @Test
    void testFunctionMetaDataColumns() throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, "doesnotexist")) {
            getFunctionsDefinition.validateResultSetColumns(functions);
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = { "<NIL>" }, textBlock = """
            schemaPattern, functionNamePattern
            <NIL>,         <NIL>
            <NIL>,         %
            %,             <NIL>
            %,             %
            """)
    void testFunctionMetadata_everything_functionNamePattern(String schemaPattern, String functionNamePattern)
            throws Exception {
        validateExpectedFunctions(null, schemaPattern, functionNamePattern, getAllFunctionsNonPackaged());
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = { "<NIL>" }, textBlock = """
            schemaPattern, functionNamePattern
            OTHER_SCHEMA,  <NIL>
            OTHER_SCHEMA,  %
            OTHER_SCHEMA,  PSQL$%
            OTHER_%,       <NIL>
            OTHER_%,       %
            OTHER_%,       PSQL$%
            """)
    void testFunctionMetadata_everything_ofOtherSchema(String schemaPattern, String functionNamePattern)
            throws Exception {
        assumeSchemaSupport();
        var expectedFunctions = List.of(getOtherSchemaPsqlExample(), getOtherSchemaPsqlExample2());
        validateExpectedFunctions(null, schemaPattern, functionNamePattern, expectedFunctions);
    }

    @Test
    void testFunctionMetaData_udfExampleOnly() throws Exception {
        validateExpectedFunctions(null, null, "UDF$EXAMPLE", List.of(getUdfExample()));
    }

    @Test
    void testFunctionMetaData_defaultSchema_psqlExampleOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        validateExpectedFunctions(null, ifSchemaElse("PUBLIC", ""), "PSQL$EXAMPLE", List.of(getPsqlExample()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "%")
    void testFunctionMetaData_allSchema_psqlExampleOnly(String schemaPattern) throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPsqlFunctions(), "Requires PSQL function support");
        var expectedFunctions = new ArrayList<Map<FunctionMetaData, Object>>();
        if (supportInfo.supportsSchemas()) {
            expectedFunctions.add(getOtherSchemaPsqlExample());
        }
        expectedFunctions.add(getPsqlExample());
        validateExpectedFunctions(null, schemaPattern, "PSQL$EXAMPLE", expectedFunctions);
    }

    @Test
    void testFunctionMetaData_otherSchema_psqlExampleOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsSchemas(), "Requires schema support");
        validateExpectedFunctions(null, "OTHER_SCHEMA", "PSQL$EXAMPLE", List.of(getOtherSchemaPsqlExample()));
    }

    @Test
    void testFunctionMetaData_caseSensitivity_udfExampleNotFound_with_lowercase() throws Exception {
        validateNoRows("udf$example");
    }

    @Test
    void testFunctionMetaData_functionInPackageNotFound() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Requires package support");
        validateNoRows("IN$PACKAGE");
        validateNoRows("%IN$PACKAGE%");
    }

    @Test
    void testFunctionMetaData_emptyString_noResults() throws Exception {
        validateNoRows("");
    }

    @Test
    void testFunctionMetadata_useCatalogAsPackage_everything() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        List<Map<FunctionMetaData, Object>> expectedFunctions = getAllFunctionsNonPackaged(true);
        expectedFunctions.add(getPackageFunctionExample());

        try (var connection = getConnectionViaDriverManager(PropertyNames.useCatalogAsPackage, "true")) {
            dbmd = connection.getMetaData();
            validateExpectedFunctions(null, null, "%", expectedFunctions);
        }
    }

    @Test
    void testFunctionMetadata_useCatalogAsPackage_specificPackage() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        try (var connection = getConnectionViaDriverManager(PropertyNames.useCatalogAsPackage, "true")) {
            dbmd = connection.getMetaData();
            validateExpectedFunctions("WITH$FUNCTION", null, "%", List.of(getPackageFunctionExample()));
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$FUNCTION")
    void testFunctionMetadata_useCatalogAsPackage_specificPackageFunction(String catalog) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        try (var connection = getConnectionViaDriverManager(PropertyNames.useCatalogAsPackage, "true")) {
            dbmd = connection.getMetaData();
            validateExpectedFunctions(catalog, null, "IN$PACKAGE", List.of(getPackageFunctionExample()));
        }
    }

    @Test
    void testFunctionMetadata_useCatalogAsPackage_nonPackagedOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        try (var connection = getConnectionViaDriverManager(PropertyNames.useCatalogAsPackage, "true")) {
            dbmd = connection.getMetaData();
            validateExpectedFunctions("", null, "%", getAllFunctionsNonPackaged(true));
        }
    }

    private void validateExpectedFunctions(String catalog, String schemaPattern, String functionNamePattern,
            List<Map<FunctionMetaData, Object>> expectedColumns) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(catalog, schemaPattern, functionNamePattern)) {
            validateFunctions(functions, expectedColumns);
        }
    }

    private static void validateFunctions(ResultSet functions,
            List<Map<FunctionMetaData, Object>> expectedColumns) throws SQLException {
        for (Map<FunctionMetaData, Object> expectedColumn : expectedColumns) {
            expectNextFunction(functions);
            getFunctionsDefinition.validateRowValues(functions, expectedColumn);
        }
        expectNoMoreRows(functions);
    }

    private void validateNoRows(String functionNamePattern) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, functionNamePattern)) {
            assertFalse(functions.next(), "Expected no rows");
        }
    }

    private static List<Map<FunctionMetaData, Object>> getAllFunctionsNonPackaged() {
        return getAllFunctionsNonPackaged(false);
    }

    private static List<Map<FunctionMetaData, Object>> getAllFunctionsNonPackaged(boolean useCatalogAsPackage) {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        var expectedFunctions = new ArrayList<Map<FunctionMetaData, Object>>();
        if (supportInfo.supportsSchemas()) {
            expectedFunctions.add(getOtherSchemaPsqlExample(useCatalogAsPackage));
            expectedFunctions.add(getOtherSchemaPsqlExample2(useCatalogAsPackage));
        }
        if (supportInfo.supportsPsqlFunctions()) {
            expectedFunctions.add(getPsqlExample(useCatalogAsPackage));
        }
        expectedFunctions.add(getUdfExample(useCatalogAsPackage));
        return expectedFunctions;
    }



    private static Map<FunctionMetaData, Object> getPsqlExample() {
        return getPsqlExample(false);
    }

    private static Map<FunctionMetaData, Object> getPsqlExample(boolean useCatalogAsPackage) {
        final boolean supportsComments = getDefaultSupportInfo().supportsComment();
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        if (useCatalogAsPackage) {
            rules.put(FunctionMetaData.FUNCTION_CAT, "");
        }
        rules.put(FunctionMetaData.FUNCTION_NAME, "PSQL$EXAMPLE");
        rules.put(FunctionMetaData.SPECIFIC_NAME, "PSQL$EXAMPLE");
        if (supportsComments) {
            rules.put(FunctionMetaData.REMARKS, "Comment on PSQL$EXAMPLE");
        }
        rules.put(FunctionMetaData.JB_FUNCTION_SOURCE, """
                begin
                  return X+1;
                end""");
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");
        return rules;
    }

    private static Map<FunctionMetaData, Object> getOtherSchemaPsqlExample() {
        return getOtherSchemaPsqlExample(false);
    }

    private static Map<FunctionMetaData, Object> getOtherSchemaPsqlExample(boolean useCatalogAsPackage) {
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        if (useCatalogAsPackage) {
            rules.put(FunctionMetaData.FUNCTION_CAT, "");
        }
        rules.put(FunctionMetaData.FUNCTION_SCHEM, "OTHER_SCHEMA");
        rules.put(FunctionMetaData.FUNCTION_NAME, "PSQL$EXAMPLE");
        rules.put(FunctionMetaData.SPECIFIC_NAME, "PSQL$EXAMPLE");
        rules.put(FunctionMetaData.JB_FUNCTION_SOURCE, """
                begin
                  return cast(x as varchar(50));
                end""");
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");
        return rules;
    }

    private static Map<FunctionMetaData, Object> getOtherSchemaPsqlExample2() {
        return getOtherSchemaPsqlExample2(false);
    }

    private static Map<FunctionMetaData, Object> getOtherSchemaPsqlExample2(boolean useCatalogAsPackage) {
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        if (useCatalogAsPackage) {
            rules.put(FunctionMetaData.FUNCTION_CAT, "");
        }
        rules.put(FunctionMetaData.FUNCTION_SCHEM, "OTHER_SCHEMA");
        rules.put(FunctionMetaData.FUNCTION_NAME, "PSQL$EXAMPLE2");
        rules.put(FunctionMetaData.SPECIFIC_NAME, "PSQL$EXAMPLE2");
        rules.put(FunctionMetaData.JB_FUNCTION_SOURCE, """
                begin
                  return X+1;
                end""");
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");
        return rules;
    }

    private static Map<FunctionMetaData, Object> getUdfExample() {
        return getUdfExample(false);
    }

    private static Map<FunctionMetaData, Object> getUdfExample(boolean useCatalogAsPackage) {
        final boolean supportsComments = getDefaultSupportInfo().supportsComment();
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        if (useCatalogAsPackage) {
            rules.put(FunctionMetaData.FUNCTION_CAT, "");
        }
        rules.put(FunctionMetaData.FUNCTION_NAME, "UDF$EXAMPLE");
        rules.put(FunctionMetaData.SPECIFIC_NAME, "UDF$EXAMPLE");
        if (supportsComments) {
            rules.put(FunctionMetaData.REMARKS, "Comment on UDF$EXAMPLE");
        }
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "UDF");
        rules.put(FunctionMetaData.JB_MODULE_NAME, "fbudf");
        rules.put(FunctionMetaData.JB_ENTRYPOINT, "idNvl");
        return rules;
    }
    
    private static Map<FunctionMetaData, Object> getPackageFunctionExample() {
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        rules.put(FunctionMetaData.FUNCTION_CAT, "WITH$FUNCTION");
        rules.put(FunctionMetaData.FUNCTION_NAME, "IN$PACKAGE");
        rules.put(FunctionMetaData.SPECIFIC_NAME, ObjectReference.of("WITH$FUNCTION", "IN$PACKAGE").toString());
        // Stored with package
        rules.put(FunctionMetaData.JB_FUNCTION_SOURCE, null);
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");

        return rules;
    }

    private static void expectNextFunction(ResultSet rs) throws SQLException {
        do {
            assertTrue(rs.next(), "Expected a row");
        } while (isIgnoredFunction(rs.getString("SPECIFIC_NAME")));
    }

    private static void expectNoMoreRows(ResultSet rs) throws SQLException {
        while (rs.next()) {
            if (!isIgnoredFunction(rs.getString("SPECIFIC_NAME"))) {
                fail("Expected no more rows");
            }
        }
    }

    static boolean isIgnoredFunction(String specificName) {
        class Ignored {
            // Skipping RDB$GET_CONTEXT and RDB$SET_CONTEXT as that seems to be an implementation artifact:
            // present in FB 2.5, absent in FB 3.0
            private static final Set<String> FUNCTIONS_TO_IGNORE = Set.of("RDB$GET_CONTEXT", "RDB$SET_CONTEXT");
            // Also skipping functions from system packages (when testing with useCatalogAsPackage=true),
            // and schema SYSTEM (Firebird 6+)
            private static final List<String> PREFIXES_TO_IGNORE =
                    List.of("\"SYSTEM\".\"RDB$", "\"RDB$BLOB_UTIL\".", "\"RDB$PROFILER\".", "\"RDB$TIME_ZONE_UTIL\".");
        }
        if (Ignored.FUNCTIONS_TO_IGNORE.contains(specificName)) return true;
        return Ignored.PREFIXES_TO_IGNORE.stream().anyMatch(specificName::startsWith);
    }

    private static final Map<FunctionMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<FunctionMetaData, Object> defaults = new EnumMap<>(FunctionMetaData.class);
        defaults.put(FunctionMetaData.FUNCTION_CAT, null);
        defaults.put(FunctionMetaData.FUNCTION_SCHEM, ifSchemaElse("PUBLIC", null));
        defaults.put(FunctionMetaData.REMARKS, null);
        defaults.put(FunctionMetaData.FUNCTION_TYPE, (short) DatabaseMetaData.functionNoTable);
        defaults.put(FunctionMetaData.JB_FUNCTION_SOURCE, null);
        defaults.put(FunctionMetaData.JB_MODULE_NAME, null);
        defaults.put(FunctionMetaData.JB_ENTRYPOINT, null);
        defaults.put(FunctionMetaData.JB_ENGINE_NAME, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<FunctionMetaData, Object> getDefaultValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private enum FunctionMetaData implements MetaDataInfo {
        FUNCTION_CAT(1, String.class),
        FUNCTION_SCHEM(2, String.class),
        FUNCTION_NAME(3, String.class),
        REMARKS(4, String.class),
        FUNCTION_TYPE(5, Short.class),
        SPECIFIC_NAME(6, String.class),
        JB_FUNCTION_SOURCE(7, String.class),
        JB_FUNCTION_KIND(8, String.class),
        JB_MODULE_NAME(9, String.class),
        JB_ENTRYPOINT(10, String.class),
        JB_ENGINE_NAME(11, String.class);

        private final int position;
        private final Class<?> columnClass;

        FunctionMetaData(int position, Class<?> columnClass) {
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
