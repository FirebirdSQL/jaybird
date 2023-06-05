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
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
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

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static final MetadataResultSetDefinition getFunctionsDefinition =
            new MetadataResultSetDefinition(FunctionMetaData.class);

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
    @ValueSource(strings = "%")
    @NullSource
    void testFunctionMetadata_everything_functionNamePattern(String functionNamePattern) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, functionNamePattern)) {
            if (getDefaultSupportInfo().supportsPsqlFunctions()) {
                expectNextFunction(functions);
                validatePsqlExample(functions);
            }

            // Verify UDF$EXAMPLE
            expectNextFunction(functions);
            validateUdfExample(functions);

            expectNoMoreRows(functions);
        }
    }

    @Test
    void testFunctionMetaData_udfExampleOnly() throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, "UDF$EXAMPLE")) {
            assertTrue(functions.next(), "Expected a row");
            validateUdfExample(functions);
            assertFalse(functions.next(), "Expected no more rows");
        }
    }

    @Test
    void testFunctionMetaData_psqlExampleOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        try (ResultSet functions = dbmd.getFunctions(null, null, "PSQL$EXAMPLE")) {
            assertTrue(functions.next(), "Expected a row");
            validatePsqlExample(functions);
            assertFalse(functions.next(), "Expected no more rows");
        }
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
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var functions = connection.getMetaData().getFunctions(null, null, "%")) {
            expectNextFunction(functions);
            validatePsqlExample(functions, true);

            // Verify UDF$EXAMPLE
            expectNextFunction(functions);
            validateUdfExample(functions, true);

            // Verify packaged function WITH$FUNCTION.IN$PACKAGE
            expectNextFunction(functions);
            validatePackageFunctionExample(functions);

            expectNoMoreRows(functions);
        }
    }

    @Test
    void testFunctionMetadata_useCatalogAsPackage_specificPackage() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var functions = connection.getMetaData().getFunctions("WITH$FUNCTION", null, "%")) {
            // Verify packaged function WITH$FUNCTION.IN$PACKAGE
            expectNextFunction(functions);
            validatePackageFunctionExample(functions);

            expectNoMoreRows(functions);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$FUNCTION")
    void testFunctionMetadata_useCatalogAsPackage_specificPackageFunction(String catalog) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var functions = connection.getMetaData().getFunctions(catalog, null, "IN$PACKAGE")) {
            // Verify packaged function WITH$FUNCTION.IN$PACKAGE
            expectNextFunction(functions);
            validatePackageFunctionExample(functions);

            expectNoMoreRows(functions);
        }
    }

    @Test
    void testFunctionMetadata_useCatalogAsPackage_nonPackagedOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var functions = connection.getMetaData().getFunctions("", null, "%")) {
            expectNextFunction(functions);
            validatePsqlExample(functions, true);

            // Verify UDF$EXAMPLE
            expectNextFunction(functions);
            validateUdfExample(functions, true);

            expectNoMoreRows(functions);
        }
    }

    private void validateNoRows(String functionNamePattern) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, functionNamePattern)) {
            assertFalse(functions.next(), "Expected no rows");
        }
    }

    private void validatePsqlExample(ResultSet functions) throws SQLException {
        validatePsqlExample(functions, false);
    }

    private void validatePsqlExample(ResultSet functions, boolean useCatalogAsPackage) throws SQLException {
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

        getFunctionsDefinition.validateRowValues(functions, rules);
    }

    private void validateUdfExample(ResultSet functions) throws SQLException {
        validateUdfExample(functions, false);
    }

    private void validateUdfExample(ResultSet functions, boolean useCatalogAsPackage) throws SQLException {
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
        getFunctionsDefinition.validateRowValues(functions, rules);
    }
    
    private void validatePackageFunctionExample(ResultSet functions) throws SQLException {
        Map<FunctionMetaData, Object> rules = getDefaultValidationRules();
        rules.put(FunctionMetaData.FUNCTION_CAT, "WITH$FUNCTION");
        rules.put(FunctionMetaData.FUNCTION_NAME, "IN$PACKAGE");
        rules.put(FunctionMetaData.SPECIFIC_NAME, "\"WITH$FUNCTION\".\"IN$PACKAGE\"");
        // Stored with package
        rules.put(FunctionMetaData.JB_FUNCTION_SOURCE, null);
        rules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");
        getFunctionsDefinition.validateRowValues(functions, rules);
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
            // Also skipping functions from system packages (when testing with useCatalogAsPackage=true)
            private static final List<String> PREFIXES_TO_IGNORE =
                    List.of("\"RDB$BLOB_UTIL\".", "\"RDB$PROFILER\".", "\"RDB$TIME_ZONE_UTIL\".");
        }
        if (Ignored.FUNCTIONS_TO_IGNORE.contains(specificName)) return true;
        return Ignored.PREFIXES_TO_IGNORE.stream().anyMatch(specificName::startsWith);
    }

    private static final Map<FunctionMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<FunctionMetaData, Object> defaults = new EnumMap<>(FunctionMetaData.class);
        defaults.put(FunctionMetaData.FUNCTION_CAT, null);
        defaults.put(FunctionMetaData.FUNCTION_SCHEM, null);
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
