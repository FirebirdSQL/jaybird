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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.sql.ResultSetMetaData.columnNullable;
import static java.sql.Types.*;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * Test for {@link org.firebirdsql.jdbc.FBResultSetMetaData}.
 * <p>
 * This is a parametrized test that test all columns in a result set and the (expected) metadata.
 * </p>
 * <p>
 * This test is similar to {@link org.firebirdsql.jdbc.TestFBPreparedStatementMetaData}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@RunWith(Parameterized.class)
public class TestFBResultSetMetaDataParametrized {

    public static final String TABLE_NAME = "TEST_P_METADATA";
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
        "  /* time zone */ " +
        ")";

    public static final String TEST_QUERY =
            "SELECT " +
            "simple_field, two_byte_field, three_byte_field, long_field, int_field, short_field," +
            "float_field, double_field, smallint_numeric, integer_decimal_1, integer_numeric," +
            "integer_decimal_2, bigint_numeric, bigint_decimal, date_field, time_field," +
            "timestamp_field, blob_field, blob_text_field, blob_minus_one " +
            "/* boolean */ " +
            "/* decfloat */ " +
            "/* extended numerics */ " +
            "/* time zone */ " +
            "FROM test_p_metadata";
    //@formatter:on

    private static FBManager fbManager;
    private static Connection connection;
    private static PreparedStatement pstmt;
    private static ResultSetMetaData rsmd;
    private static FirebirdSupportInfo supportInfo;

    private final Integer columnIndex;
    private final ResultSetMetaDataInfo expectedMetaData;

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
            createTable = CREATE_TABLE.replace("long_field BIGINT,", "long_field DOUBLE PRECISION,");
        }
        if (supportInfo.supportsBoolean()) {
            createTable = createTable.replace("/* boolean */", ", boolean_field BOOLEAN");
            testQuery = testQuery.replace("/* boolean */", ", boolean_field");
        }
        if (supportInfo.supportsDecfloat()) {
            createTable = createTable.replace("/* decfloat */",
                    ", decfloat16_field DECFLOAT(16), decfloat34_field DECFLOAT(34)");
            testQuery = testQuery.replace("/* decfloat */", ", decfloat16_field, decfloat34_field");
        }
        if (supportInfo.supportsDecimalPrecision(34)) {
            createTable = createTable.replace("/* extended numerics */",
                    ", col_numeric25_20 NUMERIC(25, 20), col_decimal30_5 DECIMAL(30,5)");
            testQuery = testQuery.replace("/* extended numerics */", ", col_numeric25_20, col_decimal30_5");
        }
        if (supportInfo.supportsTimeZones()) {
            createTable = createTable.replace("/* time zone */",
                    ", col_timetz TIME WITH TIME ZONE, col_timestamptz TIMESTAMP WITH TIME ZONE");
            testQuery = testQuery.replace("/* time zone */", ", col_timetz, col_timestamptz");
        }

        DdlHelper.executeCreateTable(connection, createTable);

        pstmt = connection.prepareStatement(testQuery);
        rsmd = pstmt.getMetaData();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            closeQuietly(pstmt);
            closeQuietly(connection);
            defaultDatabaseTearDown(fbManager);
        } finally {
            rsmd = null;
            pstmt = null;
            connection = null;
            fbManager = null;
            supportInfo = null;
        }
    }

    public TestFBResultSetMetaDataParametrized(Integer columnIndex, ResultSetMetaDataInfo expectedMetaData, @SuppressWarnings("UnusedParameters") String descriptiveName) {
        this.columnIndex = columnIndex;
        this.expectedMetaData = expectedMetaData;
    }

    @Parameterized.Parameters(name = "Index {0} ({2})")
    public static Collection<Object[]> testData() {
        List<Object[]> testData = new ArrayList<>(Arrays.asList(
                create(1, "java.lang.String", 60, "SIMPLE_FIELD", "SIMPLE_FIELD", VARCHAR, "VARCHAR", 60, 0, TABLE_NAME, columnNullable, true, false),
                create(2, "java.lang.String", 60, "TWO_BYTE_FIELD", "TWO_BYTE_FIELD", VARCHAR, "VARCHAR", 60, 0, TABLE_NAME, columnNullable, true, false),
                create(3, "java.lang.String", 60, "THREE_BYTE_FIELD", "THREE_BYTE_FIELD", VARCHAR, "VARCHAR", 60, 0, TABLE_NAME, columnNullable, true, false),
                create(4, "java.lang.Long", 20, "LONG_FIELD", "LONG_FIELD", BIGINT, "BIGINT", 19, 0, TABLE_NAME, columnNullable, true, true),
                create(5, "java.lang.Integer", 11, "INT_FIELD", "INT_FIELD", INTEGER, "INTEGER", 10, 0, TABLE_NAME, columnNullable, true, true),
                create(6, "java.lang.Integer", 6, "SHORT_FIELD", "SHORT_FIELD", SMALLINT, "SMALLINT", 5, 0, TABLE_NAME, columnNullable, true, true),
                create(7, "java.lang.Double", 13, "FLOAT_FIELD", "FLOAT_FIELD", FLOAT, "FLOAT", 7, 0, TABLE_NAME, columnNullable, true, true),
                create(8, "java.lang.Double", 22, "DOUBLE_FIELD", "DOUBLE_FIELD", DOUBLE, "DOUBLE PRECISION", 15, 0, TABLE_NAME, columnNullable, true, true),
                create(9, "java.math.BigDecimal", 5, "SMALLINT_NUMERIC", "SMALLINT_NUMERIC", NUMERIC, "NUMERIC", 3, 1, TABLE_NAME, columnNullable, true, true),
                create(10, "java.math.BigDecimal", 5, "INTEGER_DECIMAL_1", "INTEGER_DECIMAL_1", DECIMAL, "DECIMAL", 3, 1, TABLE_NAME, columnNullable, true, true),
                create(11, "java.math.BigDecimal", 7, "INTEGER_NUMERIC", "INTEGER_NUMERIC", NUMERIC, "NUMERIC", 5, 2, TABLE_NAME, columnNullable, true, true),
                create(12, "java.math.BigDecimal", 11, "INTEGER_DECIMAL_2", "INTEGER_DECIMAL_2", DECIMAL, "DECIMAL", 9, 3, TABLE_NAME, columnNullable, true, true),
                create(13, "java.math.BigDecimal", 12, "BIGINT_NUMERIC", "BIGINT_NUMERIC", NUMERIC, "NUMERIC", 10, 4, TABLE_NAME, columnNullable, true, true),
                create(14, "java.math.BigDecimal", 20, "BIGINT_DECIMAL", "BIGINT_DECIMAL", DECIMAL, "DECIMAL", 18, 9, TABLE_NAME, columnNullable, true, true),
                create(15, "java.sql.Date", 10, "DATE_FIELD", "DATE_FIELD", DATE, "DATE", 10, 0, TABLE_NAME, columnNullable, true, false),
                create(16, "java.sql.Time", 8, "TIME_FIELD", "TIME_FIELD", TIME, "TIME", 8, 0, TABLE_NAME, columnNullable, true, false),
                create(17, "java.sql.Timestamp", 19, "TIMESTAMP_FIELD", "TIMESTAMP_FIELD", TIMESTAMP, "TIMESTAMP", 19, 0, TABLE_NAME, columnNullable, true, false),
                create(18, "[B", 0, "BLOB_FIELD", "BLOB_FIELD", LONGVARBINARY, "BLOB SUB_TYPE 0", 0, 0, TABLE_NAME, columnNullable, false, false),
                create(19, "java.lang.String", 0, "BLOB_TEXT_FIELD", "BLOB_TEXT_FIELD", LONGVARCHAR, "BLOB SUB_TYPE 1", 0, 0, TABLE_NAME, columnNullable, false, false),
                // TODO Report actual subtype value
                create(20, "java.sql.Blob", 0, "BLOB_MINUS_ONE", "BLOB_MINUS_ONE", BLOB, "BLOB SUB_TYPE <0", 0, 0, TABLE_NAME, columnNullable, false, false)
        ));
        final FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsBoolean()) {
            testData.add(create(testData.size() + 1, "java.lang.Boolean", 5, "BOOLEAN_FIELD", "BOOLEAN_FIELD", BOOLEAN, "BOOLEAN", 1, 0, TABLE_NAME, columnNullable, true, false));
        }
        if (supportInfo.supportsDecfloat()) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", 23, "DECFLOAT16_FIELD", "DECFLOAT16_FIELD", JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 16, 0, TABLE_NAME, columnNullable, true, true));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", 42, "DECFLOAT34_FIELD", "DECFLOAT34_FIELD", JaybirdTypeCodes.DECFLOAT, "DECFLOAT", 34, 0, TABLE_NAME, columnNullable, true, true));
        }
        if (supportInfo.supportsDecimalPrecision(34)) {
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", 27, "COL_NUMERIC25_20", "COL_NUMERIC25_20", NUMERIC, "NUMERIC", 25, 20, TABLE_NAME, columnNullable, true, true));
            testData.add(create(testData.size() + 1, "java.math.BigDecimal", 32, "COL_DECIMAL30_5", "COL_DECIMAL30_5", DECIMAL, "DECIMAL", 30, 5, TABLE_NAME, columnNullable, true, true));
        }
        if (supportInfo.supportsTimeZones()) {
            testData.add(create(testData.size() + 1, "java.time.OffsetTime", 19, "COL_TIMETZ", "COL_TIMETZ", JaybirdTypeCodes.TIME_WITH_TIMEZONE, "TIME WITH TIME ZONE", 19, 0, TABLE_NAME, columnNullable, true, false));
            testData.add(create(testData.size() + 1, "java.time.OffsetDateTime", 30, "COL_TIMESTAMPTZ", "COL_TIMESTAMPTZ", JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP WITH TIME ZONE", 30, 0, TABLE_NAME, columnNullable, true, false));
        }

        return testData;
    }

    @Before
    public void checkAssumptions() {
        assumeFalse("Test requires BIGINT support", expectedMetaData.getType() == BIGINT && !supportInfo.supportsBigint());
    }

    @Test
    public void testGetCatalogName() throws Exception {
        assertEquals("getCatalogName", "", rsmd.getCatalogName(columnIndex));
    }

    @Test
    public void testGetColumnClassName() throws Exception {
        assertEquals("getColumnClassName", expectedMetaData.getClassName(), rsmd.getColumnClassName(columnIndex));
    }

    @Test
    public void testGetColumnDisplaySize() throws Exception {
        assertEquals("getColumnDisplaySize", expectedMetaData.getDisplaySize(), rsmd.getColumnDisplaySize(columnIndex));
    }

    @Test
    public void testGetColumnLabel() throws Exception {
        assertEquals("getColumnLabel", expectedMetaData.getLabel(), rsmd.getColumnLabel(columnIndex));
    }

    @Test
    public void testGetColumnName() throws Exception {
        assertEquals("getColumnName", expectedMetaData.getName(), rsmd.getColumnName(columnIndex));
    }

    @Test
    public void testGetColumnType() throws Exception {
        assertEquals("getColumnType", expectedMetaData.getType(), rsmd.getColumnType(columnIndex));
    }

    @Test
    public void testGetColumnTypeName() throws Exception {
        assertEquals("getColumnTypeName", expectedMetaData.getTypeName(), rsmd.getColumnTypeName(columnIndex));
    }

    @Test
    public void testGetPrecision() throws Exception {
        assertEquals("getPrecision", expectedMetaData.getPrecision(), rsmd.getPrecision(columnIndex));
    }

    @Test
    public void testGetScale() throws Exception {
        assertEquals("getScale", expectedMetaData.getScale(), rsmd.getScale(columnIndex));
    }

    @Test
    public void testGetSchemaName() throws Exception {
        assertEquals("getSchemaName", "", rsmd.getSchemaName(columnIndex));
    }

    @Test
    public void testGetTableName() throws Exception {
        assertEquals("getTableName", expectedMetaData.getTableName(), rsmd.getTableName(columnIndex));
    }

    @Test
    public void testIsAutoIncrement() throws Exception {
        assertFalse("isAutoIncrement", rsmd.isAutoIncrement(columnIndex));
    }

    @Test
    public void testIsCaseSensitive() throws Exception {
        assertTrue("isCaseSensitive", rsmd.isCaseSensitive(columnIndex));
    }

    @Test
    public void testIsCurrency() throws Exception {
        assertFalse("isCurrency", rsmd.isCurrency(columnIndex));
    }

    @Test
    public void testIsDefinitelyWritable() throws Exception {
        assertTrue("isDefiniteyWritable", rsmd.isDefinitelyWritable(columnIndex));
    }

    @Test
    public void testIsNullable() throws Exception {
        assertEquals("isNullable", expectedMetaData.getNullable(), rsmd.isNullable(columnIndex));
    }

    @Test
    public void testIsReadOnly() throws Exception {
        assertFalse("isReadOnly", rsmd.isReadOnly(columnIndex));
    }

    @Test
    public void testIsSearchable() throws Exception {
        assertEquals("isSearchable", expectedMetaData.isSearchable(), rsmd.isSearchable(columnIndex));
    }

    @Test
    public void testIsSigned() throws Exception {
        assertEquals("isSigned", expectedMetaData.isSigned(), rsmd.isSigned(columnIndex));
    }

    @Test
    public void testIsWritable() throws Exception {
        assertTrue("isWritable", rsmd.isWritable(columnIndex));
    }

    private static Object[] create(int index, String className, int displaySize, String label, String name, int type,
            String typeName, int precision, int scale, String tableName, int nullable, boolean searchable,
            boolean signed) {
        return new Object[] { index,
                new ResultSetMetaDataInfo(className, displaySize, label, name, type, typeName, precision, scale,
                        tableName, nullable, searchable, signed),
                label };
    }

    private static class ResultSetMetaDataInfo {
        private final String className;
        private final int displaySize;
        private final String label;
        private final String name;
        private final int type;
        private final String typeName;
        private final int precision;
        private final int scale;
        private final String tableName;
        private final int nullable;
        private final boolean searchable;
        private final boolean signed;

        private ResultSetMetaDataInfo(String className, int displaySize, String label, String name, int type,
                String typeName, int precision, int scale, String tableName, int nullable, boolean searchable,
                boolean signed) {

            this.className = className;
            this.displaySize = displaySize;
            this.label = label;
            this.name = name;
            this.type = type;
            this.typeName = typeName;
            this.precision = precision;
            this.scale = scale;
            this.tableName = tableName;
            this.nullable = nullable;
            this.searchable = searchable;
            this.signed = signed;
        }

        private String getClassName() {
            return className;
        }

        private int getDisplaySize() {
            return displaySize;
        }

        private String getLabel() {
            return label;
        }

        private String getName() {
            return name;
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

        private String getTableName() {
            return tableName;
        }

        private int getNullable() {
            return nullable;
        }

        private boolean isSearchable() {
            return searchable;
        }

        private boolean isSigned() {
            return signed;
        }
    }
}
