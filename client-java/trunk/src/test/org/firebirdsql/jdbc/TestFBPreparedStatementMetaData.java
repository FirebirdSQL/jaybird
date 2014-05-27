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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.management.FBManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;

import static java.sql.ParameterMetaData.parameterModeIn;
import static java.sql.ParameterMetaData.parameterNullable;
import static java.sql.Types.*;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link org.firebirdsql.jdbc.FBParameterMetaData} for a {@link org.firebirdsql.jdbc.FBPreparedStatement}.
 * <p>
 * This is a parametrized test that test all columns in a statement and the (expected) metadata.
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
        "  blob_text_field BLOB SUB_TYPE TEXT" +
        ")";

    public static final String TEST_QUERY =
            "insert into test_p_metadata(" +
            "simple_field, two_byte_field, three_byte_field, long_field, int_field, short_field," +
            "float_field, double_field, smallint_numeric, integer_decimal_1, integer_numeric," +
            "integer_decimal_2, bigint_numeric, bigint_decimal, date_field, time_field," +
            "timestamp_field, blob_field, blob_text_field) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    //@formatter:on

    private static FBManager fbManager;
    private static Connection connection;
    private static PreparedStatement pstmt;
    private static ParameterMetaData parameterMetaData;

    private final Integer parameterIndex;
    private final ParameterMetaDataInfo expectedMetaData;

    @BeforeClass
    public static void setUp() throws Exception {
        // Contrary to other tests we create the database, connection, statement etc only once
        fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);

        connection = getConnectionViaDriverManager();

        DdlHelper.executeCreateTable(connection, CREATE_TABLE);

        pstmt = connection.prepareStatement(TEST_QUERY);
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
        }
    }

    public TestFBPreparedStatementMetaData(Integer parameterIndex, ParameterMetaDataInfo expectedMetaData) {
        this.parameterIndex = parameterIndex;
        this.expectedMetaData = expectedMetaData;
    }

    @Parameterized.Parameters(name="Column Index {0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(
                create(1, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false),
                create(2, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false),
                create(3, "java.lang.String", parameterModeIn, VARCHAR, "VARCHAR", 60, 0, parameterNullable, false),
                create(4, "java.lang.Long", parameterModeIn, BIGINT, "BIGINT", 19, 0, parameterNullable, true),
                create(5, "java.lang.Integer", parameterModeIn, INTEGER, "INTEGER", 10, 0, parameterNullable, true),
                create(6, "java.lang.Integer", parameterModeIn, SMALLINT, "SMALLINT", 5, 0, parameterNullable, true),
                create(7, "java.lang.Double", parameterModeIn, FLOAT, "FLOAT", 7, 0, parameterNullable, true),
                create(8, "java.lang.Double", parameterModeIn, DOUBLE, "DOUBLE PRECISION", 15, 0, parameterNullable, true),
                create(9, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 4, 1, parameterNullable, true),
                create(10, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 1, parameterNullable, true),
                create(11, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 9, 2, parameterNullable, true),
                create(12, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 9, 3, parameterNullable, true),
                create(13, "java.math.BigDecimal", parameterModeIn, NUMERIC, "NUMERIC", 18, 4, parameterNullable, true),
                create(14, "java.math.BigDecimal", parameterModeIn, DECIMAL, "DECIMAL", 18, 9, parameterNullable, true),
                create(15, "java.sql.Date", parameterModeIn, DATE, "DATE", 10, 0, parameterNullable, false),
                create(16, "java.sql.Time", parameterModeIn, TIME, "TIME", 8, 0, parameterNullable, false),
                create(17, "java.sql.Timestamp", parameterModeIn, TIMESTAMP, "TIMESTAMP", 19, 0, parameterNullable, false),
                create(18, "[B", parameterModeIn, LONGVARBINARY, "BLOB SUB_TYPE 0", 0, 0, parameterNullable, false),
                create(19, "java.lang.String", parameterModeIn, LONGVARCHAR, "BLOB SUB_TYPE 1", 0, 0, parameterNullable, false)
        );
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

    private static Object[] create(int index, String className, int mode, int type, String typeName, int precision,
                                   int scale, int nullable, boolean signed) {
        return new Object[] { index, new ParameterMetaDataInfo(className, mode, type, typeName, precision, scale, nullable, signed) };
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

        public String getClassName() {
            return className;
        }

        public int getMode() {
            return mode;
        }

        public int getType() {
            return type;
        }

        public String getTypeName() {
            return typeName;
        }

        public int getPrecision() {
            return precision;
        }

        public int getScale() {
            return scale;
        }

        public int isNullable() {
            return nullable;
        }

        public boolean isSigned() {
            return signed;
        }
    }
}
