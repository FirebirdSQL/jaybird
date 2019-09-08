/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDatabaseMetaDataFunctionColumnsTest {

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

    private static final String CREATE_PACKAGE_WITH_FUNCTION = "create package WITH$FUNCTION\n"
            + "as\n"
            + "begin\n"
            + "  function IN$PACKAGE(PARAM1 integer) returns INTEGER;\n"
            + "end";

    private static final String CREATE_PACKAGE_BODY_WITH_FUNCTION = "create package body WITH$FUNCTION\n"
            + "as\n"
            + "begin\n"
            + "  function IN$PACKAGE(PARAM1 integer) returns INTEGER\n"
            + "  as\n"
            + "  begin\n"
            + "    return PARAM1 + 1;\n"
            + "  end\n"
            + "end";

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(getCreateStatements());

    private static final MetaDataTestSupport<FunctionColumnMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(FunctionColumnMetaData.class, EnumSet.allOf(FunctionColumnMetaData.class));
    // Skipping RDB$GET_CONTEXT and RDB$SET_CONTEXT as that seems to be an implementation artifact:
    // present in FB 2.5, absent in FB 3.0
    private static final Set<String> FUNCTIONS_TO_IGNORE = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("RDB$GET_CONTEXT", "RDB$SET_CONTEXT")));

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeClass
    public static void setUp() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
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
    public void testFunctionColumnMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getFunctionColumns(null, null, "doesnotexist", "doesnotexist")) {
            metaDataTestSupport.validateResultSetColumns(columns);
        }
    }

    @Test
    public void testFunctionColumnMetaData_everything_functionNamePattern_null() throws Exception {
        validateFunctionColumnMetaData_everything(null);
    }

    @Test
    public void testFunctionColumnMetaData_everything_functionNamePattern_allPattern() throws Exception {
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
    public void testFunctionColumnMetaData_functionNamePattern_emptyString_noResults() throws Exception {
        validateNoRows("", "%");
    }

    @Test
    public void testFunctionColumnMetaData_columnNamePattern_emptyString_noResults() throws Exception {
        validateNoRows("%", "");
    }

    public void validateNoRows(String functionNamePattern, String columnNamePattern) throws Exception {
        validateExpectedFunctionColumns(functionNamePattern, columnNamePattern,
                Collections.<Map<FunctionColumnMetaData, Object>>emptyList());
    }

    @Test
    public void testFunctionColumnMetaData_PsqlExample1() throws Exception {
        assumeTrue("Requires PSQL function support", getDefaultSupportInfo().supportsPsqlFunctions());
        validateExpectedFunctionColumns(PSQL_EXAMPLE_1, null, getPsqlExample1Columns());
    }

    @Test
    public void testFunctionColumnMetaData_PsqlExample2() throws Exception {
        assumeTrue("Requires Firebird 4", getDefaultSupportInfo().isVersionEqualOrAbove(4, 0));
        validateExpectedFunctionColumns(PSQL_EXAMPLE_2, null, getPsqlExample2Columns());
    }

    @Test
    public void testFunctionColumnMetaData_UdfExample1() throws Exception {
        validateExpectedFunctionColumns(UDF_EXAMPLE_1, null, getUdfExample1Columns());
    }

    @Test
    public void testFunctionColumnMetaData_UdfExample2() throws Exception {
        validateExpectedFunctionColumns(UDF_EXAMPLE_2, "%", getUdfExample2Columns());
    }

    @Test
    public void testFunctionColumnMetaData_functionInPackageNotFound() throws Exception {
        assumeTrue("Requires package support", getDefaultSupportInfo().supportsPackages());
        validateNoRows("IN$PACKAGE", "%");
        validateNoRows("%IN$PACKAGE%", "%");
    }

    public void validateExpectedFunctionColumns(String functionNamePattern, String columnNamePattern,
            List<Map<FunctionColumnMetaData, Object>> expectedColumns) throws Exception {
        try (ResultSet columns = dbmd.getFunctionColumns(null, null, functionNamePattern, columnNamePattern)) {
            for (int i = 0; i < expectedColumns.size(); i++) {
                expectNextFunction(columns);
                System.out.println("Position: " + i);
                Map<FunctionColumnMetaData, Object> expectedColumn = expectedColumns.get(i);
                metaDataTestSupport.validateRowValues(columns, expectedColumn);
            }
            expectNoMoreRows(columns);
        }
    }

    private void expectNextFunction(ResultSet rs) throws SQLException {
        assertTrue("Expected a row", rs.next());
        while (FUNCTIONS_TO_IGNORE.contains(rs.getString("FUNCTION_NAME"))) {
            assertTrue("Expected a row", rs.next());
        }
    }

    private void expectNoMoreRows(ResultSet rs) throws SQLException {
        boolean hasRow;
        while ((hasRow = rs.next())) {
            if (!FUNCTIONS_TO_IGNORE.contains(rs.getString("FUNCTION_NAME"))) {
                break;
            }
        }
        assertFalse("Expected no more rows", hasRow);
    }

    private static List<Map<FunctionColumnMetaData, Object>> getPsqlExample1Columns() {
        return Arrays.asList(
                createReturnColumnPsqlExample1(),
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
        return Arrays.asList(
                createReturnColumnPsqlExample2(),
                createDateTime(JaybirdTypeCodes.TIME_WITH_TIMEZONE, PSQL_EXAMPLE_2, "C$01$TIME_WITH_TIME_ZONE", 1,
                        true),
                createDateTime(JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE, PSQL_EXAMPLE_2,
                        "C$02$TIMESTAMP_WITH_TIME_ZONE", 2, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$03$DECFLOAT", 3, 34, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$04$DECFLOAT16", 4, 16, true),
                createDecfloat(PSQL_EXAMPLE_2, "C$05$DECFLOAT34", 5, 34, true),
                createNumericalType(Types.NUMERIC, PSQL_EXAMPLE_2, "C$06$NUMERIC21$5", 6, 21, 5, true),
                createNumericalType(Types.DECIMAL, PSQL_EXAMPLE_2, "C$07$DECIMAL34$19", 7, 34, 19, true));
    }

    private static List<Map<FunctionColumnMetaData, Object>> getUdfExample1Columns() {
        return Arrays.asList(
                createReturnColumnUdfExample1(),
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
        return Arrays.asList(
                createReturnColumnUdfExample2(),
                createNumericalType(Types.NUMERIC, UDF_EXAMPLE_2, "PARAM_1", 1, 9, 3, false),
                createNumericalType(Types.NUMERIC, UDF_EXAMPLE_2, "PARAM_2", 2, 4, 3, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_3", 3, 18, 2, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_4", 4, 9, 2, false),
                createNumericalType(Types.DECIMAL, UDF_EXAMPLE_2, "PARAM_5", 5, 4, 2, false),
                createDateTime(Types.DATE, UDF_EXAMPLE_2, "PARAM_6", 6, false),
                createDateTime(Types.TIME, UDF_EXAMPLE_2, "PARAM_7", 7, false),
                createDateTime(Types.TIMESTAMP, UDF_EXAMPLE_2, "PARAM_8", 8, false));
    }

    private static Map<FunctionColumnMetaData, Object> createReturnColumnPsqlExample1() {
        Map<FunctionColumnMetaData, Object> rules =
                createStringType(Types.VARCHAR, PSQL_EXAMPLE_1, "PARAM_0", 0, 100, true);
        rules.put(FunctionColumnMetaData.COLUMN_TYPE, DatabaseMetaData.functionReturn);
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createReturnColumnPsqlExample2() {
        Map<FunctionColumnMetaData, Object> rules =
                createStringType(Types.VARCHAR, PSQL_EXAMPLE_2, "PARAM_0", 0, 100, false);
        rules.put(FunctionColumnMetaData.COLUMN_TYPE, DatabaseMetaData.functionReturn);
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createReturnColumnUdfExample1() {
        Map<FunctionColumnMetaData, Object> rules =
                createStringType(Types.VARCHAR, UDF_EXAMPLE_1, "PARAM_0", 0, 100, false);
        rules.put(FunctionColumnMetaData.COLUMN_TYPE, DatabaseMetaData.functionReturn);
        return rules;
    }

    private static Map<FunctionColumnMetaData, Object> createReturnColumnUdfExample2() {
        Map<FunctionColumnMetaData, Object> rules =
                createStringType(Types.VARCHAR, UDF_EXAMPLE_2, "PARAM_0", 0, 100, true);
        rules.put(FunctionColumnMetaData.COLUMN_TYPE, DatabaseMetaData.functionReturn);
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
        String typeName;
        switch (jdbcType) {
        case Types.CHAR:
        case Types.BINARY:
            typeName = "CHAR";
            break;
        case Types.VARCHAR:
        case Types.VARBINARY:
            typeName = "VARCHAR";
            break;
        default:
            throw new IllegalArgumentException("Wrong type code for createStringType: " + jdbcType);
        }
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
        case Types.BIGINT:
            typeName = "BIGINT";
            length = 8;
            break;
        case Types.INTEGER:
            typeName = "INTEGER";
            length = 4;
            break;
        case Types.SMALLINT:
            typeName = "SMALLINT";
            length = 2;
            break;
        case Types.NUMERIC:
            typeName = "NUMERIC";
            length = precision > 5 ? (precision > 9 ? (precision > 18 ? 16 : 8) : 4) : 2;
            break;
        case Types.DECIMAL:
            typeName = "DECIMAL";
            length = precision > 9 ? (precision > 18 ? 16 : 8) : 4;
            break;
        default:
            throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
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
        case Types.DATE:
            typeName = "DATE";
            precision = DATE_PRECISION;
            length = 4;
            break;
        case Types.TIME:
            typeName = "TIME";
            precision = TIME_PRECISION;
            length = 4;
            break;
        case Types.TIMESTAMP:
            typeName = "TIMESTAMP";
            precision = TIMESTAMP_PRECISION;
            length = 8;
            break;
        case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
            typeName = "TIME WITH TIME ZONE";
            precision = TIME_WITH_TIMEZONE_PRECISION;
            length = 8; // TODO Possibly 6
            break;
        case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
            typeName = "TIMESTAMP WITH TIME ZONE";
            precision = TIMESTAMP_WITH_TIMEZONE_PRECISION;
            length = 12; // TODO Possibly 10
            break;
        default:
            throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
        }
        rules.put(FunctionColumnMetaData.TYPE_NAME, typeName);
        rules.put(FunctionColumnMetaData.PRECISION, precision);
        rules.put(FunctionColumnMetaData.LENGTH, length);
        return rules;
    }

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

        @Override
        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<>(this);
        }
    }

}
