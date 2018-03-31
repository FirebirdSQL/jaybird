/*
 * $Id$
 *
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

import org.firebirdsql.management.FBManager;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.sql.ParameterMetaData.parameterModeIn;
import static java.sql.ParameterMetaData.parameterNullable;
import static java.sql.Types.*;
import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

/**
 * Tests {@link org.firebirdsql.jdbc.FBParameterMetaData} for a {@link org.firebirdsql.jdbc.FBPreparedStatement}.
 * <p>
 * This is a parametrized test that test all parameters in a statement and the (expected) metadata.
 * </p>
 * <p>
 * This test is similar to {@link TestFBResultSetMetaDataParametrized}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@RunWith(Parameterized.class)
public class TestFBPreparedStatementMetaData {

    //@formatter:off
    public static String CREATE_TABLE =
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
        ")";

    public static final String TEST_QUERY =
            "insert into test_p_metadata(" +
            "simple_field, two_byte_field, three_byte_field, long_field, int_field, short_field," +
            "float_field, double_field, smallint_numeric, integer_decimal_1, integer_numeric," +
            "integer_decimal_2, bigint_numeric, bigint_decimal, date_field, time_field," +
            "timestamp_field, blob_field, blob_text_field, blob_minus_one" +
            "  /* boolean */ " +
            "  /* decfloat */ " +
            "  /* extended numerics */ " +
            ") " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
                    + "/* boolean-param *//* decfloat-param *//* extended-num-param*/)";
    //@formatter:on

    private static FBManager fbManager;
    private static Connection connection;
    private static PreparedStatement pstmt;
    private static ParameterMetaData parameterMetaData;
    private static FirebirdSupportInfo supportInfo;

    private final Integer parameterIndex;
    private final ParameterMetaDataInfo expectedMetaData;

    @BeforeClass
    public static void setUp() throws Exception {
        // Contrary to other tests we create the database, connection, statement etc only once
        fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);

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
        if (supportInfo.supportsDecimalPrecision(34)) {
            createTable = createTable.replace("/* extended numerics */",
                    ", col_numeric25_20 NUMERIC(25, 20), col_decimal30_5 DECIMAL(30,5)");
            testQuery = testQuery.replace("/* extended numerics */", ", col_numeric25_20, col_decimal30_5")
                    .replace("/* extended-num-param*/", ", ?, ?");
        }
        executeCreateTable(connection, createTable);

        pstmt = connection.prepareStatement(testQuery);
        parameterMetaData = pstmt.getParameterMetaData();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            closeQuietly(pstmt);
            closeQuietly(connection);
            defaultDatabaseTearDown(fbManager);
        } finally {
            parameterMetaData = null;
            pstmt = null;
            connection = null;
            fbManager = null;
            supportInfo = null;
        }
    }

    public TestFBPreparedStatementMetaData(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData, @SuppressWarnings("UnusedParameters") String descriptiveName) {
        this.parameterIndex = parameterIndex;
        this.expectedMetaData = expectedMetaData;
    }

    @Parameterized.Parameters(name = "Column {0} ({2})")
    public static Collection<Object[]> testData() {
        List<Object[]> testData = new ArrayList<>(Arrays.asList(
                create(1, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "simple_field"),
                create(2, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "two_byte_field"),
                create(3, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false, "three_byte_field"),
                create(4, "java.lang.Long", parameterModeIn, BIGINT, "BIGINT", 19, 0, parameterNullable, true, "long_field"),
                create(5, "java.lang.Integer", parameterModeIn, INTEGER, "INTEGER", 10, 0, parameterNullable, true, "int_field"),
                create(6, "java.lang.Integer", parameterModeIn, SMALLINT, "SMALLINT", 5, 0, parameterNullable, true, "short_field"),
                create(7, "java.lang.Double", parameterModeIn, FLOAT, "FLOAT", 7, 0, parameterNullable, true, "float_field"),
                create(8, "java.lang.Double", parameterModeIn, DOUBLE, "DOUBLE PRECISION", 15, 0, parameterNullable, true, "double_field"),
                create(9, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 4, 1, parameterNullable, true, "smallint_numeric"),
                create(10, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 1, parameterNullable, true, "integer_decimal_1"),
                create(11, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 9, 2, parameterNullable, true, "integer_numeric"),
                create(12, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 3, parameterNullable, true, "integer_decimal_2"),
                create(13, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 18, 4, parameterNullable, true, "bigint_numeric"),
                create(14, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 18, 9, parameterNullable, true, "bigint_decimal"),
                create(15, "java.sql.Date", parameterModeIn, DATE, "DATE", 10, 0, parameterNullable, false, "date_field"),
                create(16, "java.sql.Time", parameterModeIn, TIME, "TIME", 8, 0, parameterNullable, false, "time_field"),
                create(17, "java.sql.Timestamp", parameterModeIn, TIMESTAMP, "TIMESTAMP", 19, 0, parameterNullable, false, "timestamp_field"),
                create(18, "[B", parameterModeIn, LONGVARBINARY, "BLOB SUB_TYPE 0", 0, 0, parameterNullable, false, "blob_field"),
                create(19, "java.lang.String", parameterModeIn, LONGVARCHAR, "BLOB SUB_TYPE 1", 0, 0, parameterNullable, false, "blob_text_field"),
                // TODO Report actual subtype value
                create(20, "java.sql.Blob", parameterModeIn, BLOB, "BLOB SUB_TYPE <0", 0, 0, parameterNullable, false, "blob_minus_one")
        ));
        final FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsBoolean()) {
            testData.add(create(testData.size() + 1, "java.lang.Boolean", parameterModeIn, BOOLEAN, "BOOLEAN", 1, 0, parameterNullable, false, "boolean_field"));
        }
        if (supportInfo.supportsDecfloat()) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 16, 0, parameterNullable, true, "decfloat16_field"));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 34, 0, parameterNullable, true, "decfloat34_field"));
        }
        if (supportInfo.supportsDecimalPrecision(34)) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 34, 20, parameterNullable, true, "col_numeric25_20"));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 34, 5, parameterNullable, true, "col_decimal30_5"));
        }

        return testData;
    }

    @Before
    public void checkAssumptions() {
        assumeFalse("Test requires BIGINT support", expectedMetaData.getType() == BIGINT && !supportInfo.supportsBigint());
    }

    @Test
    public void testGetParameterClassName() throws Exception {
        assertEquals("getParameterClassName",
                expectedMetaData.getClassName(), parameterMetaData.getParameterClassName(parameterIndex));
    }

    @Test
    public void testGetParameterMode() throws Exception {
        assertEquals("getParameterMode",
                expectedMetaData.getMode(), parameterMetaData.getParameterMode(parameterIndex));
    }

    @Test
    public void testGetParameterType() throws Exception {
        assertEquals("getParameterType",
                expectedMetaData.getType(), parameterMetaData.getParameterType(parameterIndex));
    }

    @Test
    public void testGetParameterTypeName() throws Exception {
        assertEquals("getParameterTypeName",
                expectedMetaData.getTypeName(), parameterMetaData.getParameterTypeName(parameterIndex));
    }

    @Test
    public void testGetPrecision() throws Exception {
        assertEquals("getPrecision",
                expectedMetaData.getPrecision(), parameterMetaData.getPrecision(parameterIndex));
    }

    @Test
    public void testGetScale() throws Exception {
        assertEquals("getScale",
                expectedMetaData.getScale(), parameterMetaData.getScale(parameterIndex));
    }

    @Test
    public void testIsNullable() throws Exception {
        assertEquals("isNullable",
                expectedMetaData.isNullable(), parameterMetaData.isNullable(parameterIndex));
    }

    @Test
    public void testIsSigned() throws Exception {
        assertEquals("isSigned",
                expectedMetaData.isSigned(), parameterMetaData.isSigned(parameterIndex));
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
    private static Object[] create(int index, String className, int mode, int type, String typeName, int precision,
                                   int scale, int nullable, boolean signed, String descriptiveName) {
        return new Object[] { index,
                new ParameterMetaDataInfo(className, mode, type, typeName, precision, scale, nullable, signed),
                descriptiveName };
    }

    /**
     * Simple bean with the expected meta data information.
     */
    private static class ParameterMetaDataInfo {
        private final String className;
        private final int mode;
        private final int type;
        private final String typeName;
        private final int precision;
        private final int scale;
        private final int nullable;
        private final boolean signed;

        private ParameterMetaDataInfo(String className, int mode, int type, String typeName, int precision, int scale,
                                      int nullable, boolean signed) {

            this.className = className;
            this.mode = mode;
            this.type = type;
            this.typeName = typeName;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.signed = signed;
        }

        private String getClassName() {
            return className;
        }

        private int getMode() {
            return mode;
        }

        private int getType() {
            return type;
        }

        private String getTypeName() {
            return typeName;
        }

        private int getPrecision() {
            return precision;
        }

        private int getScale() {
            return scale;
        }

        private int isNullable() {
            return nullable;
        }

        private boolean isSigned() {
            return signed;
        }
    }
}
