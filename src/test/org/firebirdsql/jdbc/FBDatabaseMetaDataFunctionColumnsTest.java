// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.jdbc.FBDatabaseMetaDataFunctionsTest.isIgnoredFunction;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataFunctionColumnsTest {

    private static final String PSQL_EXAMPLE_1 = "PSQL$EXAMPLE$1";
    private static final String PSQL_EXAMPLE_2 = "PSQL$EXAMPLE$2";
    private static final String UDF_EXAMPLE_1 = "UDF$EXAMPLE$1";
    private static final String UDF_EXAMPLE_2 = "UDF$EXAMPLE$2";
    private static final String UDF_EXAMPLE_3 = "UDF$EXAMPLE$3";

    private static final String CREATE_DOMAIN_D_INTEGER_NOT_NULL =
            "create domain D_INTEGER_NOT_NULL as integer not null";

    private static final String CREATE_DOMAIN_D_INTEGER =
            "create domain D_INTEGER as integer";

    private static final String CREATE_PSQL_EXAMPLE_1 = "create function " + PSQL_EXAMPLE_1 + "("
            + "C$01$FLOAT float not null,"
            + "C$02$DOUBLE double precision,"
            + "C$03$CHAR10 char(10),"
            + "C$04$VARCHAR15 varchar(15) not null,"
            + "C$05$BINARY20 char(20) character set octets,"
            + "C$06$VARBINARY25 varchar(25) character set octets,"
            + "C$07$BIGINT bigint,"
            + "C$08$INTEGER integer,"
            + "C$09$SMALLINT smallint,"
            + "C$10$NUMERIC18$3 numeric(18,3),"
            + "C$11$NUMERIC9$3 numeric(9,3),"
            + "C$12$NUMERIC4$3 numeric(4,3),"
            + "C$13$DECIMAL18$2 decimal(18,2),"
            + "C$14$DECIMAL9$2 decimal(9,2),"
            + "C$15$DECIMAL4$2 decimal(4,2),"
            + "C$16$DATE date,"
            + "C$17$TIME time,"
            + "C$18$TIMESTAMP timestamp,"
            + "C$19$BOOLEAN boolean,"
            + "C$20$D_INTEGER_NOT_NULL D_INTEGER_NOT_NULL,"
            + "C$21$D_INTEGER_WITH_NOT_NULL D_INTEGER NOT NULL) "
            + "returns varchar(100) "
            + "as "
            + "begin"
            + "  return 'a';"
            + "end";

    private static final String CREATE_PSQL_EXAMPLE_2 = "create function " + PSQL_EXAMPLE_2 + "("
            + "C$01$TIME_WITH_TIME_ZONE time with time zone,"
            + "C$02$TIMESTAMP_WITH_TIME_ZONE timestamp with time zone,"
            + "C$03$DECFLOAT decfloat,"
            + "C$04$DECFLOAT16 decfloat(16),"
            + "C$05$DECFLOAT34 decfloat(34),"
            + "C$06$NUMERIC21$5 numeric(21,5),"
            + "C$07$DECIMAL34$19 decimal(34,19)) "
            + "returns varchar(100) not null "
            + "as "
            + "begin"
            + "  return 'a';"
            + "end";

    private static final String CREATE_UDF_EXAMPLE_1 = "declare external function " + UDF_EXAMPLE_1
            + "/* 1*/ float by descriptor,"
            + "/* 2*/ double precision,"
            + "/* 3*/ char(10),"
            + "/* 4*/ varchar(15) by descriptor,"
            + "/* 5*/ char(20) character set octets,"
            + "/* 6*/ varchar(25) character set octets,"
            + "/* 7*/ bigint,"
            + "/* 8*/ integer,"
            + "/* 9*/ smallint,"
            + "/*10*/ numeric(18,3) "
            + "returns varchar(100) "
            + "entry_point 'UDF$EXAMPLE$1' module_name 'module_1'";

    private static final String CREATE_UDF_EXAMPLE_2 = "declare external function " + UDF_EXAMPLE_2
            + "/* 1*/ numeric(9,3),"
            + "/* 2*/ numeric(4,3),"
            + "/* 3*/ decimal(18,2),"
            + "/* 4*/ decimal(9,2),"
            + "/* 5*/ decimal(4,2),"
            + "/* 6*/ date,"
            + "/* 7*/ time,"
            + "/* 8*/ timestamp "
            + "returns varchar(100) by descriptor "
            + "entry_point 'UDF$EXAMPLE$2' module_name 'module_1'";

    private static final String CREATE_UDF_EXAMPLE_3 = "declare external function " + UDF_EXAMPLE_3
            + " returns cstring(100)"
            + "entry_point 'UDF$EXAMPLE$3' module_name 'module_1'";

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

    private static final MetadataResultSetDefinition getFunctionColumnsDefinition =
            new MetadataResultSetDefinition(FunctionColumnMetaData.class);

    private static Connection con;
    private DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setup() throws SQLException {
        dbmd = con.getMetaData();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
        }
    }

    private static List<String> getCreateStatements() {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        List<String> statements = new ArrayList<>();
        statements.add(CREATE_DOMAIN_D_INTEGER_NOT_NULL);
        statements.add(CREATE_DOMAIN_D_INTEGER);
        if (supportInfo.supportsPsqlFunctions()) {
            statements.add(CREATE_PSQL_EXAMPLE_1);

            if (supportInfo.isVersionEqualOrAbove(4, 0)) {
                statements.add(CREATE_PSQL_EXAMPLE_2);
            }

            if (supportInfo.supportsPackages()) {
                statements.add(CREATE_PACKAGE_WITH_FUNCTION);
                statements.add(CREATE_PACKAGE_BODY_WITH_FUNCTION);
            }
        }

        statements.add(CREATE_UDF_EXAMPLE_1);
        statements.add(CREATE_UDF_EXAMPLE_2);
        statements.add(CREATE_UDF_EXAMPLE_3);

        return statements;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getFunctions().
     */
    @Test
    void testFunctionColumnMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getFunctionColumns(null, null, "doesnotexist", "doesnotexist")) {
            getFunctionColumnsDefinition.validateResultSetColumns(columns);
        }
    }

    @Test
    void testFunctionColumnMetaData_everything_functionNamePattern_null() throws Exception {
        validateFunctionColumnMetaData_everything(null);
    }

    @Test
    void testFunctionColumnMetaData_everything_functionNamePattern_allPattern() throws Exception {
        validateFunctionColumnMetaData_everything("%");
    }

    private void validateFunctionColumnMetaData_everything(String functionNamePattern)
            throws Exception {
        FirebirdSupportInfo defaultSupportInfo = getDefaultSupportInfo();
        List<Map<FunctionColumnMetaData, Object>> expectedColumns = new ArrayList<>();
        if (defaultSupportInfo.supportsPsqlFunctions()) {
            expectedColumns.addAll(getPsqlExample1Columns());
            if (defaultSupportInfo.isVersionEqualOrAbove(4, 0)) {
                expectedColumns.addAll(getPsqlExample2Columns());
            }
        }
        expectedColumns.addAll(getUdfExample1Columns());
        expectedColumns.addAll(getUdfExample2Columns());
        expectedColumns.add(createStringType(Types.VARCHAR, UDF_EXAMPLE_3, "PARAM_0", 0, 100, false));
        validateExpectedFunctionColumns(functionNamePattern, null, expectedColumns);
    }

    @Test
    void testFunctionColumnMetaData_functionNamePattern_emptyString_noResults() throws Exception {
        validateNoRows("", "%");
    }

    @Test
    void testFunctionColumnMetaData_columnNamePattern_emptyString_noResults() throws Exception {
        validateNoRows("%", "");
    }

    private void validateNoRows(String functionNamePattern, String columnNamePattern) throws Exception {
        validateExpectedFunctionColumns(functionNamePattern, columnNamePattern, Collections.emptyList());
    }

    @Test
    void testFunctionColumnMetaData_PsqlExample1() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        validateExpectedFunctionColumns(PSQL_EXAMPLE_1, null, getPsqlExample1Columns());
    }

    @Test
    void testFunctionColumnMetaData_byColumnNameOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        validateExpectedFunctionColumns(null, "C$19$BOOLEAN",
                singletonList(createBoolean(PSQL_EXAMPLE_1, "C$19$BOOLEAN", 19, true)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "C$04$VARCHAR15", "C$04%", "C$04$VARCHAR__" })
    void testFunctionColumnMetaData_PsqlExample1_onlyParam4(String columnNamePattern) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        validateExpectedFunctionColumns(PSQL_EXAMPLE_1, columnNamePattern,
                singletonList(createStringType(Types.VARCHAR, PSQL_EXAMPLE_1, "C$04$VARCHAR15", 4, 15, false)));
    }

    @Test
    void testFunctionColumnMetaData_PsqlExample2() throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(4, 0), "Requires Firebird 4");
        validateExpectedFunctionColumns(PSQL_EXAMPLE_2, null, getPsqlExample2Columns());
    }

    @Test
    void testFunctionColumnMetaData_UdfExample1() throws Exception {
        validateExpectedFunctionColumns(UDF_EXAMPLE_1, null, getUdfExample1Columns());
    }

    @ParameterizedTest
    @ValueSource(strings = { "PARAM\\_4", "%\\_4", "PARAM_4" })
    void testFunctionColumnMetaData_UdfExample1_onlyParam4(String columnNamePattern) throws Exception {
        validateExpectedFunctionColumns(UDF_EXAMPLE_1, columnNamePattern,
                singletonList(createStringType(Types.VARCHAR, UDF_EXAMPLE_1, "PARAM_4", 4, 15, true)));
    }

    @Test
    void testFunctionColumnMetaData_UdfExample2() throws Exception {
        validateExpectedFunctionColumns(UDF_EXAMPLE_2, "%", getUdfExample2Columns());
    }

    @Test
    void testFunctionColumnMetaData_functionInPackageNotFound() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Requires package support");
        validateNoRows("IN$PACKAGE", "%");
        validateNoRows("%IN$PACKAGE%", "%");
    }

    @Test
    void testFunctionColumnMetaData_useCatalogAsPackage_everything() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();
            var expectedColumns = new ArrayList<>(getPsqlExample1Columns());
            if (supportInfo.isVersionEqualOrAbove(4)) {
                expectedColumns.addAll(getPsqlExample2Columns());
            }
            expectedColumns.addAll(getUdfExample1Columns());
            expectedColumns.addAll(getUdfExample2Columns());
            expectedColumns.add(createStringType(Types.VARCHAR, UDF_EXAMPLE_3, "PARAM_0", 0, 100, false));
            withCatalog("", expectedColumns);
            expectedColumns.addAll(getWithFunctionInPackageColumns());
            validateExpectedFunctionColumns(null, null, null, expectedColumns);
        }
    }

    @Test
    void testFunctionColumnMetaData_useCatalogAsPackage_specificPackage() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();
            List<Map<FunctionColumnMetaData, Object>> expectedColumns = getWithFunctionInPackageColumns();
            validateExpectedFunctionColumns("WITH$FUNCTION", null, null, expectedColumns);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$FUNCTION")
    void testFunctionColumnMetaData_useCatalogAsPackage_specificPackageProcedure(String catalog) throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();
            List<Map<FunctionColumnMetaData, Object>> expectedColumns = getWithFunctionInPackageColumns();
            validateExpectedFunctionColumns(catalog, "IN$PACKAGE", null, expectedColumns);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$FUNCTION")
    void testFunctionColumnMetaData_useCatalogAsPackage_specificPackageProcedureColumn(String catalog)
            throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();
            List<Map<FunctionColumnMetaData, Object>> expectedColumns = withCatalog("WITH$FUNCTION",
                    withSpecificName("\"WITH$FUNCTION\".\"IN$PACKAGE\"",
                            List.of(createNumericalType(Types.INTEGER, "IN$PACKAGE", "PARAM1", 1, 10, 0, true))));
            validateExpectedFunctionColumns(catalog, "IN$PACKAGE", "PARAM1", expectedColumns);
        }
    }

    @Test
    void testFunctionColumnMetaData_useCatalogAsPackage_nonPackagedOnly() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();
            var expectedColumns = new ArrayList<>(getPsqlExample1Columns());
            if (supportInfo.isVersionEqualOrAbove(4)) {
                expectedColumns.addAll(getPsqlExample2Columns());
            }
            expectedColumns.addAll(getUdfExample1Columns());
            expectedColumns.addAll(getUdfExample2Columns());
            expectedColumns.add(createStringType(Types.VARCHAR, UDF_EXAMPLE_3, "PARAM_0", 0, 100, false));
            withCatalog("", expectedColumns);
            validateExpectedFunctionColumns("", null, null, expectedColumns);
        }
    }

    private void validateExpectedFunctionColumns(String functionNamePattern, String columnNamePattern,
            List<Map<FunctionColumnMetaData, Object>> expectedColumns) throws Exception {
        validateExpectedFunctionColumns(null, functionNamePattern, columnNamePattern, expectedColumns);
    }

    private void validateExpectedFunctionColumns(String catalog, String functionNamePattern, String columnNamePattern,
            List<Map<FunctionColumnMetaData, Object>> expectedColumns) throws Exception {
        try (ResultSet columns = dbmd.getFunctionColumns(catalog, null, functionNamePattern, columnNamePattern)) {
            for (Map<FunctionColumnMetaData, Object> expectedColumn : expectedColumns) {
                expectNextFunctionColumn(columns);
                getFunctionColumnsDefinition.validateRowValues(columns, expectedColumn);
            }
            expectNoMoreRows(columns);
        }
    }

    private static void expectNextFunctionColumn(ResultSet rs) throws SQLException {
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

    private static List<Map<FunctionColumnMetaData, Object>> getPsqlExample1Columns() {
        return List.of(
                withColumnTypeFunctionReturn(createStringType(Types.VARCHAR, PSQL_EXAMPLE_1, "PARAM_0", 0, 100, true)),
                createFloat(PSQL_EXAMPLE_1, "C$01$FLOAT", 1, false),
                createDouble(PSQL_EXAMPLE_1, "C$02$DOUBLE", 2, true),
                createStringType(Types.CHAR, PSQL_EXAMPLE_1, "C$03$CHAR10", 3, 10, true),
                createStringType(Types.VARCHAR, PSQL_EXAMPLE_1, "C$04$VARCHAR15", 4, 15, false),
                createStringType(Types.BINARY, PSQL_EXAMPLE_1, "C$05$BINARY20", 5, 20, true),
                createStringType(Types.VARBINARY, PSQL_EXAMPLE_1, "C$06$VARBINARY25", 6, 25, true),
                createNumericalType(Types.BIGINT, PSQL_EXAMPLE_1, "C$07$BIGINT", 7, 19, 0, true),
                createNumericalType(Types.INTEGER, PSQL_EXAMPLE_1, "C$08$INTEGER", 8, 10, 0, true),
                createNumericalType(Types.SMALLINT, PSQL_EXAMPLE_1, "C$09$SMALLINT", 9, 5, 0, true),
                createNumericalType(Types.NUMERIC, PSQL_EXAMPLE_1, "C$10$NUMERIC18$3", 10, 18, 3, true),
                createNumericalType(Types.NUMERIC, PSQL_EXAMPLE_1, "C$11$NUMERIC9$3", 11, 9, 3, true),
                createNumericalType(Types.NUMERIC, PSQL_EXAMPLE_1, "C$12$NUMERIC4$3", 12, 4, 3, true),
                createNumericalType(Types.DECIMAL, PSQL_EXAMPLE_1, "C$13$DECIMAL18$2", 13, 18, 2, true),
                createNumericalType(Types.DECIMAL, PSQL_EXAMPLE_1, "C$14$DECIMAL9$2", 14, 9, 2, true),
                createNumericalType(Types.DECIMAL, PSQL_EXAMPLE_1, "C$15$DECIMAL4$2", 15, 4, 2, true),
                createDateTime(Types.DATE, PSQL_EXAMPLE_1, "C$16$DATE", 16, true),
                createDateTime(Types.TIME, PSQL_EXAMPLE_1, "C$17$TIME", 17, true),
                createDateTime(Types.TIMESTAMP, PSQL_EXAMPLE_1, "C$18$TIMESTAMP", 18, true),
                createBoolean(PSQL_EXAMPLE_1, "C$19$BOOLEAN", 19, true),
                createNumericalType(Types.INTEGER, PSQL_EXAMPLE_1, "C$20$D_INTEGER_NOT_NULL", 20, 10, 0, false),
                createNumericalType(Types.INTEGER, PSQL_EXAMPLE_1, "C$21$D_INTEGER_WITH_NOT_NULL", 21, 10, 0, false));
    }

    private static List<Map<FunctionColumnMetaData, Object>> getPsqlExample2Columns() {
        return List.of(
                withColumnTypeFunctionReturn(createStringType(Types.VARCHAR, PSQL_EXAMPLE_2, "PARAM_0", 0, 100, false)),
                createDateTime(Types.TIME_WITH_TIMEZONE, PSQL_EXAMPLE_2, "C$01$TIME_WITH_TIME_ZONE", 1, true),
                createDateTime(Types.TIMESTAMP_WITH_TIMEZONE, PSQL_EXAMPLE_2, "C$02$TIMESTAMP_WITH_TIME_ZONE", 2, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$03$DECFLOAT", 3, 34, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$04$DECFLOAT16", 4, 16, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$05$DECFLOAT34", 5, 34, true),
                createNumericalType(Types.NUMERIC, PSQL_EXAMPLE_2, "C$06$NUMERIC21$5", 6, 21, 5, true),
                createNumericalType(Types.DECIMAL, PSQL_EXAMPLE_2, "C$07$DECIMAL34$19", 7, 34, 19, true));
    }

    private static List<Map<FunctionColumnMetaData, Object>> getUdfExample1Columns() {
        return List.of(
                withColumnTypeFunctionReturn(createStringType(Types.VARCHAR, UDF_EXAMPLE_1, "PARAM_0", 0, 100, false)),
                createFloat(UDF_EXAMPLE_1, "PARAM_1", 1, true),
                createDouble(UDF_EXAMPLE_1, "PARAM_2", 2, false),
                createStringType(Types.CHAR, UDF_EXAMPLE_1, "PARAM_3", 3, 10, false),
                createStringType(Types.VARCHAR, UDF_EXAMPLE_1, "PARAM_4", 4, 15, true),
                createStringType(Types.BINARY, UDF_EXAMPLE_1, "PARAM_5", 5, 20, false),
                createStringType(Types.VARBINARY, UDF_EXAMPLE_1, "PARAM_6", 6, 25, false),
                createNumericalType(Types.BIGINT, UDF_EXAMPLE_1, "PARAM_7", 7, 19, 0, false),
                createNumericalType(Types.INTEGER, UDF_EXAMPLE_1, "PARAM_8", 8, 10, 0, false),
                createNumericalType(Types.SMALLINT, UDF_EXAMPLE_1, "PARAM_9", 9, 5, 0, false),
                createNumericalType(Types.NUMERIC, UDF_EXAMPLE_1, "PARAM_10", 10, 18, 3, false));
    }

    private static List<Map<FunctionColumnMetaData, Object>> getUdfExample2Columns() {
        return List.of(
                withColumnTypeFunctionReturn(createStringType(Types.VARCHAR, UDF_EXAMPLE_2, "PARAM_0", 0, 100, true)),
                createNumericalType(Types.NUMERIC, UDF_EXAMPLE_2, "PARAM_1", 1, 9, 3, false),
                createNumericalType(Types.NUMERIC, UDF_EXAMPLE_2, "PARAM_2", 2, 4, 3, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_3", 3, 18, 2, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_4", 4, 9, 2, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_5", 5, 4, 2, false),
                createDateTime(Types.DATE, UDF_EXAMPLE_2, "PARAM_6", 6, false),
                createDateTime(Types.TIME, UDF_EXAMPLE_2, "PARAM_7", 7, false),
                createDateTime(Types.TIMESTAMP, UDF_EXAMPLE_2, "PARAM_8", 8, false));
    }

    private static List<Map<FunctionColumnMetaData, Object>> getWithFunctionInPackageColumns() {
        return withCatalog("WITH$FUNCTION",
                withSpecificName("\"WITH$FUNCTION\".\"IN$PACKAGE\"",
                        List.of(
                                withColumnTypeFunctionReturn(
                                        createNumericalType(Types.INTEGER, "IN$PACKAGE", "PARAM_0", 0, 10, 0, true)),
                                createNumericalType(Types.INTEGER, "IN$PACKAGE", "PARAM1", 1, 10, 0, true))));
    }

    private static Map<FunctionColumnMetaData, Object> withColumnTypeFunctionReturn(
            Map<FunctionColumnMetaData, Object> rules) {
        rules.put(FunctionColumnMetaData.COLUMN_TYPE, DatabaseMetaData.functionReturn);
        return rules;
    }

    private static List<Map<FunctionColumnMetaData, Object>> withCatalog(String catalog,
            List<Map<FunctionColumnMetaData, Object>> rules) {
        for (Map<FunctionColumnMetaData, Object> rowRule : rules) {
            rowRule.put(FunctionColumnMetaData.FUNCTION_CAT, catalog);
        }
        return rules;
    }

    private static List<Map<FunctionColumnMetaData, Object>> withSpecificName(String specificName,
            List<Map<FunctionColumnMetaData, Object>> rules) {
        for (Map<FunctionColumnMetaData, Object> rowRule : rules) {
            rowRule.put(FunctionColumnMetaData.SPECIFIC_NAME, specificName);
        }
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createColumn(String functionName, String columnName,
            int ordinalPosition, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = getDefaultValidationRules();
        rules.put(FunctionColumnMetaData.FUNCTION_NAME, functionName);
        rules.put(FunctionColumnMetaData.SPECIFIC_NAME, functionName);
        rules.put(FunctionColumnMetaData.COLUMN_NAME, columnName);
        rules.put(FunctionColumnMetaData.ORDINAL_POSITION, ordinalPosition);
        if (nullable) {
            rules.put(FunctionColumnMetaData.NULLABLE, DatabaseMetaData.functionNullable);
            rules.put(FunctionColumnMetaData.IS_NULLABLE, "YES");
        }
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createStringType(int jdbcType, String functionName,
            String columnName, int ordinalPosition, int length, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, jdbcType);
        String typeName = switch (jdbcType) {
            case Types.CHAR, Types.BINARY -> "CHAR";
            case Types.VARCHAR, Types.VARBINARY -> "VARCHAR";
            default -> throw new IllegalArgumentException("Wrong type code for createStringType: " + jdbcType);
        };
        rules.put(FunctionColumnMetaData.TYPE_NAME, typeName);
        rules.put(FunctionColumnMetaData.PRECISION, length);
        rules.put(FunctionColumnMetaData.LENGTH, length);
        rules.put(FunctionColumnMetaData.CHAR_OCTET_LENGTH, length);
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createNumericalType(int jdbcType, String functionName,
            String columnName, int ordinalPosition, int precision, int scale, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, jdbcType);
        String typeName;
        int length;
        switch (jdbcType) {
        case Types.BIGINT -> {
            typeName = "BIGINT";
            length = 8;
        }
        case Types.INTEGER -> {
            typeName = "INTEGER";
            length = 4;
        }
        case Types.SMALLINT -> {
            typeName = "SMALLINT";
            length = 2;
        }
        case Types.NUMERIC -> {
            typeName = "NUMERIC";
            length = precision > 5 ? (precision > 9 ? (precision > 18 ? 16 : 8) : 4) : 2;
        }
        case Types.DECIMAL -> {
            typeName = "DECIMAL";
            length = precision > 9 ? (precision > 18 ? 16 : 8) : 4;
        }
        default -> throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
        }
        rules.put(FunctionColumnMetaData.TYPE_NAME, typeName);
        rules.put(FunctionColumnMetaData.PRECISION, precision);
        rules.put(FunctionColumnMetaData.SCALE, scale);
        rules.put(FunctionColumnMetaData.LENGTH, length);
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createDateTime(int jdbcType, String functionName,
            String columnName, int ordinalPosition, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, jdbcType);
        String typeName;
        int precision;
        int length;
        switch (jdbcType) {
        case Types.DATE -> {
            typeName = "DATE";
            precision = DATE_PRECISION;
            length = 4;
        }
        case Types.TIME -> {
            typeName = "TIME";
            precision = TIME_PRECISION;
            length = 4;
        }
        case Types.TIMESTAMP -> {
            typeName = "TIMESTAMP";
            precision = TIMESTAMP_PRECISION;
            length = 8;
        }
        case Types.TIME_WITH_TIMEZONE -> {
            typeName = "TIME WITH TIME ZONE";
            precision = TIME_WITH_TIMEZONE_PRECISION;
            length = 8; // TODO Possibly 6
        }
        case Types.TIMESTAMP_WITH_TIMEZONE -> {
            typeName = "TIMESTAMP WITH TIME ZONE";
            precision = TIMESTAMP_WITH_TIMEZONE_PRECISION;
            length = 12; // TODO Possibly 10
        }
        default -> throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
        }
        rules.put(FunctionColumnMetaData.TYPE_NAME, typeName);
        rules.put(FunctionColumnMetaData.PRECISION, precision);
        rules.put(FunctionColumnMetaData.LENGTH, length);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<FunctionColumnMetaData, Object> createFloat(String functionName, String columnName,
            int ordinalPosition, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, Types.FLOAT);
        rules.put(FunctionColumnMetaData.TYPE_NAME, "FLOAT");
        if (getDefaultSupportInfo().supportsFloatBinaryPrecision()) {
            rules.put(FunctionColumnMetaData.PRECISION, 24);
            rules.put(FunctionColumnMetaData.RADIX, 2);
        } else {
            rules.put(FunctionColumnMetaData.PRECISION, 7);
        }
        rules.put(FunctionColumnMetaData.LENGTH, 4);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<FunctionColumnMetaData, Object> createDouble(String functionName, String columnName,
            int ordinalPosition, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, Types.DOUBLE);
        rules.put(FunctionColumnMetaData.TYPE_NAME, "DOUBLE PRECISION");
        if (getDefaultSupportInfo().supportsFloatBinaryPrecision()) {
            rules.put(FunctionColumnMetaData.PRECISION, 53);
            rules.put(FunctionColumnMetaData.RADIX, 2);
        } else {
            rules.put(FunctionColumnMetaData.PRECISION, 15);
        }
        rules.put(FunctionColumnMetaData.LENGTH, 8);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<FunctionColumnMetaData, Object> createBoolean(String functionName, String columnName,
            int ordinalPosition, boolean nullable) {
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, Types.BOOLEAN);
        rules.put(FunctionColumnMetaData.TYPE_NAME, "BOOLEAN");
        rules.put(FunctionColumnMetaData.PRECISION, 1);
        rules.put(FunctionColumnMetaData.RADIX, 2);
        rules.put(FunctionColumnMetaData.LENGTH, 1);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<FunctionColumnMetaData, Object> createDecfloat(String functionName, String columnName,
            int ordinalPosition, int precision, boolean nullable) {
        assert precision == 16 || precision == 34 : "Decfloat requires precision 16 or 34";
        Map<FunctionColumnMetaData, Object> rules = createColumn(functionName, columnName, ordinalPosition, nullable);
        rules.put(FunctionColumnMetaData.DATA_TYPE, JaybirdTypeCodes.DECFLOAT);
        rules.put(FunctionColumnMetaData.TYPE_NAME, "DECFLOAT");
        rules.put(FunctionColumnMetaData.PRECISION, precision);
        rules.put(FunctionColumnMetaData.LENGTH, precision == 16 ? 8 : 16);
        return rules;
    }

    private static final Map<FunctionColumnMetaData, Object> DEFAULT_COLUMN_VALUES;

    static {
        Map<FunctionColumnMetaData, Object> defaults = new EnumMap<>(FunctionColumnMetaData.class);
        defaults.put(FunctionColumnMetaData.FUNCTION_CAT, null);
        defaults.put(FunctionColumnMetaData.FUNCTION_SCHEM, null);
        defaults.put(FunctionColumnMetaData.PRECISION, null);
        defaults.put(FunctionColumnMetaData.SCALE, null);
        defaults.put(FunctionColumnMetaData.RADIX, 10);
        defaults.put(FunctionColumnMetaData.NULLABLE, DatabaseMetaData.functionNoNulls);
        defaults.put(FunctionColumnMetaData.REMARKS, null);
        defaults.put(FunctionColumnMetaData.CHAR_OCTET_LENGTH, null);
        defaults.put(FunctionColumnMetaData.IS_NULLABLE, "NO");

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<FunctionColumnMetaData, Object> getDefaultValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private enum FunctionColumnMetaData implements MetaDataInfo {
        FUNCTION_CAT(1, String.class),
        FUNCTION_SCHEM(2, String.class),
        FUNCTION_NAME(3, String.class),
        COLUMN_NAME(4, String.class),
        COLUMN_TYPE(5, Short.class),
        DATA_TYPE(6, Integer.class),
        TYPE_NAME(7, String.class),
        PRECISION(8, Integer.class),
        LENGTH(9, Integer.class),
        SCALE(10, Short.class),
        RADIX(11, Short.class),
        NULLABLE(12, Short.class),
        REMARKS(13, String.class),
        CHAR_OCTET_LENGTH(14, Integer.class),
        ORDINAL_POSITION(15, Integer.class),
        IS_NULLABLE(16, String.class),
        SPECIFIC_NAME(17, String.class);

        private final int position;
        private final Class<?> columnClass;

        FunctionColumnMetaData(int position, Class<?> columnClass) {
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
