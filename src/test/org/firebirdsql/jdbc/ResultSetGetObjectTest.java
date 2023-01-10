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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBResultSet#getObject(int)}.
 *
 * @author Mark Rotteveel
 */
class ResultSetGetObjectTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection connection;
    @SuppressWarnings("FieldCanBeLocal")
    private static Statement statement;
    private static ResultSet queryResult;
    private static ResultSetMetaData queryResultMd;

    @BeforeAll
    static void initDatabase() throws Exception {
        connection = getConnectionViaDriverManager();
        DdlHelper.executeCreateTable(connection, "create table alltypes ("
                + "smallintcolumn smallint, integercolumn integer, bigintcolumn bigint,"
                + "numericcolumn numeric(18,2), decimalcolumn decimal(18,2),"
                + "doublecolumn double precision, floatcolumn float,"
                + "timecolumn time, datecolumn date, timestampcolumn timestamp,"
                + "varcharcolumn varchar(10), charcolumn char(10), textblobcolumn blob sub_type text,"
                + "blobcolumn blob sub_type binary, varbinarycolumn varchar(10) character set octets, binarycolumn char(10) character set octets"
                + (getDefaultSupportInfo().supportsBoolean() ? ", booleancolumn boolean" : "") +")");
        statement = connection.createStatement();
        statement.executeUpdate("insert into alltypes values ("
                + "1, 2, 3, "
                + "4.21, 5.21, "
                + "6.31, 7.89, "
                + "'08:00', '2016-04-09', '2016-04-10 10:31', "
                + "'eleven', 'twelve', 'thirteen', "
                + "'fourteen', 'fifteen', 'sixteen'"
                + (getDefaultSupportInfo().supportsBoolean() ? ", true" : "") + ")");
        queryResult = statement.executeQuery("select * from alltypes");
        queryResultMd = queryResult.getMetaData();
        queryResult.next();
    }

    @AfterAll
    static void cleanup() throws Exception {
        connection.close();
    }

    @Test
    void testSmallintColumn() throws Exception {
        checkColumn(1, Integer.class, Types.SMALLINT, 1);
    }

    @Test
    void testIntegerColumn() throws Exception {
        checkColumn(2, Integer.class, Types.INTEGER, 2);
    }

    @Test
    void testBigintColumn() throws Exception {
        checkColumn(3, Long.class, Types.BIGINT, 3L);
    }

    @Test
    void testNumericColumn() throws Exception {
        checkColumn(4, BigDecimal.class, Types.NUMERIC, new BigDecimal("4.21"));
    }

    @Test
    void testDecimalColumn() throws Exception {
        checkColumn(5, BigDecimal.class, Types.DECIMAL, new BigDecimal("5.21"));
    }

    @Test
    void testDoubleColumn() throws Exception {
        checkColumn(6, Double.class, Types.DOUBLE, Double.valueOf("6.31"));
    }

    @SuppressWarnings("UnnecessaryBoxing") // it is actually necessary because of float -> double conversion
    @Test
    void testFloatColumn() throws Exception {
        checkColumn(7, Double.class, Types.FLOAT, Double.valueOf(7.89f));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testTimeColumn() throws Exception {
        checkColumn(8, java.sql.Time.class, Types.TIME, new java.sql.Time(8, 0, 0));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDateColumn() throws Exception {
        checkColumn(9, java.sql.Date.class, Types.DATE, new java.sql.Date(116, 3, 9));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testTimestampColumn() throws Exception {
        checkColumn(10, java.sql.Timestamp.class, Types.TIMESTAMP, new java.sql.Timestamp(116, 3, 10, 10, 31, 0, 0));
    }

    @Test
    void testVarcharColumn() throws Exception {
        checkColumn(11, String.class, Types.VARCHAR, "eleven");
    }

    @Test
    void testCharColumn() throws Exception {
        checkColumn(12, String.class, Types.CHAR, "twelve    ");
    }

    @Test
    void testTextblobColumn() throws Exception {
        checkColumn(13, String.class, Types.LONGVARCHAR, "thirteen");
    }

    @Test
    void testBlobColumn() throws Exception {
        checkColumn(14, byte[].class, Types.LONGVARBINARY, "fourteen".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void testVarbinaryColumn() throws Exception {
        checkColumn(15, byte[].class, Types.VARBINARY, "fifteen".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void testBinaryColumn() throws Exception {
        checkColumn(16, byte[].class, Types.BINARY, "sixteen\0\0\0".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void testBooleanColumn() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsBoolean(), "test requires support for BOOLEAN datatype");
        checkColumn(17, Boolean.class, Types.BOOLEAN, Boolean.TRUE);
    }

    private <T> void checkColumn(int columnIndex, Class<T> expectedType, int expectedJdbcType, T expectedValue) throws Exception {
        assertEquals(expectedType.getName(), queryResultMd.getColumnClassName(columnIndex));
        assertEquals(expectedJdbcType, queryResultMd.getColumnType(columnIndex));
        Object result = queryResult.getObject(columnIndex);
        assertEquals(expectedType, result.getClass());
        if (expectedType.equals(byte[].class)) {
            assertArrayEquals((byte[]) expectedValue, (byte[]) result);
        } else {
            assertEquals(expectedValue, result);
        }
    }
}
