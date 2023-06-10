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
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.sql.ParameterMetaData.parameterModeIn;
import static java.sql.ParameterMetaData.parameterNullable;
import static java.sql.Types.*;
import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link org.firebirdsql.jdbc.FBParameterMetaData} for a {@link org.firebirdsql.jdbc.FBPreparedStatement}.
 * <p>
 * This is a parametrized test that test all parameters in a statement and the (expected) metadata.
 * </p>
 * <p>
 * This test is similar to {@link FBResultSetMetaDataParametrizedTest}
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBPreparedStatementMetaDataTest {

    //@formatter:off
    private static final String CREATE_TABLE =
        "CREATE TABLE test_p_metadata (" +
        "  id INTEGER, " +
        "  simple_field VARCHAR(60) CHARACTER SET WIN1251 COLLATE PXW_CYRL, " +
        "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
        "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
        "  long_field BIGINT, " +
        "  int_field INTEGER, " +
        "  short_field SMALLINT, " +
        "  float_field FLOAT, " +
        "  double_field DOUBLE PRECISION, " +
        "  smallint_numeric NUMERIC(3,1), " +
        "  integer_decimal_1 DECIMAL(3,1), " +
        "  integer_numeric NUMERIC(5,2), " +
        "  integer_decimal_2 DECIMAL(9,3), " +
        "  bigint_numeric NUMERIC(10,4), " +
        "  bigint_decimal DECIMAL(18,9), " +
        "  date_field DATE, " +
        "  time_field TIME, " +
        "  timestamp_field TIMESTAMP, " +
        "  blob_field BLOB, " +
        "  blob_text_field BLOB SUB_TYPE TEXT, " +
        "  blob_minus_one BLOB SUB_TYPE -1 " +
        "  /* boolean */ " +
        "  /* decfloat */ " +
        "  /* extended numerics */ " +
        "  /* time zone */ " +
        "  /* int128 */ " +
        ")";

    private static final String TEST_QUERY =
            "insert into test_p_metadata(" +
            "simple_field, two_byte_field, three_byte_field, long_field, int_field, short_field," +
            "float_field, double_field, smallint_numeric, integer_decimal_1, integer_numeric," +
            "integer_decimal_2, bigint_numeric, bigint_decimal, date_field, time_field," +
            "timestamp_field, blob_field, blob_text_field, blob_minus_one" +
            "  /* boolean */ " +
            "  /* decfloat */ " +
            "  /* extended numerics */ " +
            "  /* time zone */ " +
            "  /* int128 */ " +
            ") " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? /* boolean-param */" +
            "/* decfloat-param *//* extended-num-param *//* time-zone-param *//* int128-param */)";
    //@formatter:on

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection connection;
    private static PreparedStatement pstmt;
    private static ParameterMetaData parameterMetaData;
    private static FirebirdSupportInfo supportInfo;

    @BeforeAll
    static void setupAll() throws Exception {
        connection = getConnectionViaDriverManager();
        supportInfo = supportInfoFor(connection);

        String createTable = CREATE_TABLE;
        String testQuery = TEST_QUERY;
        if (!supportInfo.supportsBigint()) {
            // No BIGINT support, replacing type so number of columns remain the same
            createTable = CREATE_TABLE.replace("long_field BIGINT,", "long field DOUBLE PRECISION,");
        }
        if (supportInfo.supportsBoolean()) {
            createTable = createTable.replace("/* boolean */", ", boolean_field BOOLEAN");
            testQuery = testQuery.replace("/* boolean */", ", boolean_field").replace("/* boolean-param */", ", ?");
        }
        if (supportInfo.supportsDecfloat()) {
            createTable = createTable.replace("/* decfloat */",
                    ", decfloat16_field DECFLOAT(16), decfloat34_field DECFLOAT(34)");
            testQuery = testQuery.replace("/* decfloat */", ", decfloat16_field, decfloat34_field")
                    .replace("/* decfloat-param */", ", ?, ?");
        }
        if (supportInfo.supportsDecimalPrecision(38)) {
            createTable = createTable.replace("/* extended numerics */",
                    ", col_numeric25_20 NUMERIC(25, 20), col_decimal30_5 DECIMAL(30,5)");
            testQuery = testQuery.replace("/* extended numerics */", ", col_numeric25_20, col_decimal30_5")
                    .replace("/* extended-num-param */", ", ?, ?");
        }
        if (getDefaultSupportInfo().supportsTimeZones()) {
            createTable = createTable.replace("/* time zone */",
                    ", col_timetz TIME WITH TIME ZONE, col_timestamptz TIMESTAMP WITH TIME ZONE");
            testQuery = testQuery.replace("/* time zone */", ", col_timetz, col_timestamptz")
                    .replace("/* time-zone-param */", ", ?, ?");
        }
        if (supportInfo.supportsInt128()) {
            createTable = createTable.replace("/* int128 */", ", col_int128 INT128");
            testQuery = testQuery.replace("/* int128 */", ", col_int128").replace("/* int128-param */", ", ?");
        }
        executeCreateTable(connection, createTable);

        pstmt = connection.prepareStatement(testQuery);
        parameterMetaData = pstmt.getParameterMetaData();
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            closeQuietly(pstmt, connection);
        } finally {
            parameterMetaData = null;
            pstmt = null;
            connection = null;
            supportInfo = null;
        }
    }

    static Stream<Arguments> testData() {
        final boolean supportsFloatBinaryPrecision = getDefaultSupportInfo().supportsFloatBinaryPrecision();
        List<Arguments> testData = new ArrayList<>(Arrays.asList(
                create(1, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "simple_field"),
                create(2, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "two_byte_field"),
                create(3, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "three_byte_field"),
                create(4, "java.lang.Long", parameterModeIn, BIGINT, "BIGINT", 19, 0, parameterNullable, true, "long_field"),
                create(5, "java.lang.Integer", parameterModeIn, INTEGER, "INTEGER", 10, 0, parameterNullable, true, "int_field"),
                create(6, "java.lang.Integer", parameterModeIn, SMALLINT, "SMALLINT", 5, 0, parameterNullable, true, "short_field"),
                create(7, "java.lang.Double", parameterModeIn, FLOAT, "FLOAT", supportsFloatBinaryPrecision ? 24 : 7, 0, parameterNullable, true, "float_field"),
                create(8, "java.lang.Double", parameterModeIn, DOUBLE, "DOUBLE PRECISION", supportsFloatBinaryPrecision ? 53 : 15, 0, parameterNullable, true, "double_field"),
                create(9, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 4, 1, parameterNullable, true, "smallint_numeric"),
                create(10, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 1, parameterNullable, true, "integer_decimal_1"),
                create(11, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 9, 2, parameterNullable, true, "integer_numeric"),
                create(12, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 3, parameterNullable, true, "integer_decimal_2"),
                create(13, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 18, 4, parameterNullable, true, "bigint_numeric"),
                create(14, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 18, 9, parameterNullable, true, "bigint_decimal"),
                create(15, "java.sql.Date", parameterModeIn, DATE, "DATE", 10, 0, parameterNullable, false, "date_field"),
                create(16, "java.sql.Time", parameterModeIn, TIME, "TIME", 8, 0, parameterNullable, false, "time_field"),
                create(17, "java.sql.Timestamp", parameterModeIn, TIMESTAMP, "TIMESTAMP", 19, 0, parameterNullable, false, "timestamp_field"),
                create(18, "[B", parameterModeIn, LONGVARBINARY, "BLOB SUB_TYPE BINARY", 0, 0, parameterNullable, false, "blob_field"),
                create(19, "java.lang.String", parameterModeIn, LONGVARCHAR, "BLOB SUB_TYPE TEXT", 0, 0, parameterNullable, false, "blob_text_field"),
                create(20, "java.sql.Blob", parameterModeIn, BLOB, "BLOB SUB_TYPE -1", 0, 0, parameterNullable, false, "blob_minus_one")
        ));
        final FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsBoolean()) {
            testData.add(create(testData.size() + 1, "java.lang.Boolean", parameterModeIn, BOOLEAN, "BOOLEAN", 1, 0, parameterNullable, false, "boolean_field"));
        }
        if (supportInfo.supportsDecfloat()) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 16, 0, parameterNullable, true, "decfloat16_field"));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 34, 0, parameterNullable, true, "decfloat34_field"));
        }
        if (supportInfo.supportsDecimalPrecision(38)) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 38, 20, parameterNullable, true, "col_numeric25_20"));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 38, 5, parameterNullable, true, "col_decimal30_5"));
        }
        if (getDefaultSupportInfo().supportsTimeZones()) {
            testData.add(create(testData.size() + 1, "java.time.OffsetTime", parameterModeIn, TIME_WITH_TIMEZONE, "TIME WITH TIME ZONE", 19, 0, parameterNullable, false, "col_timetz"));
            testData.add(create(testData.size() + 1, "java.time.OffsetDateTime", parameterModeIn, TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP WITH TIME ZONE", 30, 0, parameterNullable, false, "col_timestamptz"));
        }
        if (supportInfo.supportsInt128()) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, NUMERIC, "INT128", 38, 0, parameterNullable, true, "col_int128"));
        }

        return testData.stream();
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetParameterClassName(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.className(), parameterMetaData.getParameterClassName(parameterIndex),
                "getParameterClassName");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetParameterMode(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.mode(), parameterMetaData.getParameterMode(parameterIndex),
                "getParameterMode");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetParameterType(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.type(), parameterMetaData.getParameterType(parameterIndex),
                "getParameterType");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetParameterTypeName(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.typeName(), parameterMetaData.getParameterTypeName(parameterIndex),
                "getParameterTypeName");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetPrecision(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.precision(), parameterMetaData.getPrecision(parameterIndex), "getPrecision");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testGetScale(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored) throws Exception {
        assertEquals(expectedMetaData.scale(), parameterMetaData.getScale(parameterIndex), "getScale");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testIsNullable(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored)
            throws Exception {
        assertEquals(expectedMetaData.nullable(), parameterMetaData.isNullable(parameterIndex), "isNullable");
    }

    @ParameterizedTest(name = "Column {0} ({2})")
    @MethodSource("testData")
    void testIsSigned(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, String ignored) throws Exception {
        assertEquals(expectedMetaData.signed(), parameterMetaData.isSigned(parameterIndex), "isSigned");
    }

    /**
     * Creates a parameter array for a set of tests for a single column.
     *
     * @param index
     *         Parameter index (1-based)
     * @param className
     *         Expected java class name
     * @param mode
     *         Expected parameter mode
     * @param type
     *         Expected parameter type
     * @param typeName
     *         Expected parameter type name
     * @param precision
     *         Expected precision
     * @param scale
     *         Expected scale
     * @param nullable
     *         Expected nullability
     * @param signed
     *         Expected value for is signed
     * @param descriptiveName
     *         Descriptive name (eg the column name) for logging purposes
     * @return Test parameter data
     */
    @SuppressWarnings("SameParameterValue")
    private static Arguments create(int index, String className, int mode, int type, String typeName, int precision,
            int scale, int nullable, boolean signed, String descriptiveName) {
        return Arguments.of(index,
                new ParameterMetaDataInfo(className, mode, type, typeName, precision, scale, nullable, signed),
                descriptiveName);
    }

    /**
     * Simple record with the expected metadata information.
     */
    private record ParameterMetaDataInfo(
            String className, int mode, int type, String typeName, int precision, int scale, int nullable,
            boolean signed) {
    }
}
