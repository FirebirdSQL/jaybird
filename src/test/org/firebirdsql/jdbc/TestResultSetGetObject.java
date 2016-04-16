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
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.util.SQLExceptionChainBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link FBResultSet#getObject(int)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("UnnecessaryBoxing")
public class TestResultSetGetObject {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private static Connection connection;
    private static Connection octetsConnection;
    private static Statement statement;
    private static Statement octetsStatement;
    private static ResultSet queryResult;
    private static ResultSet octetsQueryResult;
    private static ResultSetMetaData queryResultMd;
    private static ResultSetMetaData octetsQueryResultMd;

    @BeforeClass
    public static void initDatabase() throws Exception {
        connection = getConnectionViaDriverManager();
        boolean supportsBoolean = connection.getMetaData().getDatabaseMajorVersion() >= 3;
        DdlHelper.executeCreateTable(connection, "create table alltypes ("
                + "smallintcolumn smallint, integercolumn integer, bigintcolumn bigint,"
                + "numericcolumn numeric(18,2), decimalcolumn decimal(18,2),"
                + "doublecolumn double precision, floatcolumn float,"
                + "timecolumn time, datecolumn date, timestampcolumn timestamp,"
                + "varcharcolumn varchar(10), charcolumn char(10), textblobcolumn blob sub_type text,"
                + "blobcolumn blob sub_type binary, varbinarycolumn varchar(10) character set octets, binarycolumn char(10) character set octets"
                + (supportsBoolean ? ", booleancolumn boolean" : "") +")");
        statement = connection.createStatement();
        statement.executeUpdate("insert into alltypes values ("
                + "1, 2, 3, "
                + "4.21, 5.21, "
                + "6.31, 7.89, "
                + "'08:00', '2016-04-09', '2016-04-10 10:31', "
                + "'eleven', 'twelve', 'thirteen', "
                + "'fourteen', 'fifteen', 'sixteen'"
                + (supportsBoolean ? ", true" : "") + ")");
        queryResult = statement.executeQuery("select * from alltypes");
        queryResultMd = queryResult.getMetaData();
        queryResult.next();

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("octetsAsBytes", "true");
        octetsConnection = DriverManager.getConnection(getUrl(), props);
        octetsStatement = octetsConnection.createStatement();
        octetsQueryResult = octetsStatement.executeQuery("select * from alltypes");
        octetsQueryResultMd = octetsQueryResult.getMetaData();
        octetsQueryResult.next();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();
        try {
            connection.close();
        } catch (SQLException e) {
            chain.append(e);
        }
        try {
            octetsConnection.close();
        } catch (SQLException e) {
            chain.append(e);
        }
        if (chain.hasException()) {
            throw chain.getException();
        }
    }

    @Test
    public void testSmallintColumn() throws Exception {
        checkColumn(1, Integer.class, Types.SMALLINT, Integer.valueOf(1));
    }

    @Test
    public void testIntegerColumn() throws Exception {
        checkColumn(2, Integer.class, Types.INTEGER, Integer.valueOf(2));
    }

    @Test
    public void testBigintColumn() throws Exception {
        checkColumn(3, Long.class, Types.BIGINT, Long.valueOf(3));
    }

    @Test
    public void testNumericColumn() throws Exception {
        checkColumn(4, BigDecimal.class, Types.NUMERIC, new BigDecimal("4.21"));
    }

    @Test
    public void testDecimalColumn() throws Exception {
        checkColumn(5, BigDecimal.class, Types.DECIMAL, new BigDecimal("5.21"));
    }

    @Test
    public void testDoubleColumn() throws Exception {
        checkColumn(6, Double.class, Types.DOUBLE, Double.valueOf("6.31"));
    }

    @Test
    public void testFloatColumn() throws Exception {
        checkColumn(7, Double.class, Types.FLOAT, Double.valueOf(7.89f));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTimeColumn() throws Exception {
        checkColumn(8, java.sql.Time.class, Types.TIME, new Time(8, 0, 0));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDateColumn() throws Exception {
        checkColumn(9, java.sql.Date.class, Types.DATE, new Date(116, 3, 9));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTimestampColumn() throws Exception {
        checkColumn(10, java.sql.Timestamp.class, Types.TIMESTAMP, new Timestamp(116, 3, 10, 10, 31, 0, 0));
    }

    @Test
    public void testVarcharColumn() throws Exception {
        checkColumn(11, String.class, Types.VARCHAR, "eleven");
    }

    @Test
    public void testCharColumn() throws Exception {
        checkColumn(12, String.class, Types.CHAR, "twelve    ");
    }

    @Test
    public void testTextblobColumn() throws Exception {
        checkColumn(13, String.class, Types.LONGVARCHAR, "thirteen");
    }

    @Test
    public void testBlobColumn() throws Exception {
        checkColumn(14, byte[].class, Types.LONGVARBINARY, "fourteen".getBytes("US-ASCII"));
    }

    @Test
    public void testVarbinaryColumn() throws Exception {
        checkNormalColumn(15, String.class, Types.VARCHAR, "fifteen");
        checkOctetsColumn(15, byte[].class, Types.VARBINARY, "fifteen".getBytes("US-ASCII"));
    }

    @Test
    public void testBinaryColumn() throws Exception {
        checkNormalColumn(16, String.class, Types.CHAR, "sixteen\0\0\0");
        checkOctetsColumn(16, byte[].class, Types.BINARY, "sixteen\0\0\0".getBytes("US-ASCII"));
    }

    @Test
    public void testBooleanColumn() throws Exception {
        assumeTrue("Test only works on Firebird 3 or higher", connection.getMetaData().getDatabaseMajorVersion() >= 3);
        checkColumn(17, Boolean.class, Types.BOOLEAN, Boolean.TRUE);
    }

    private void checkColumn(int columnIndex, Class<?> expectedType, int expectedJdbcType, Object expectedValue) throws Exception {
        checkNormalColumn(columnIndex, expectedType, expectedJdbcType, expectedValue);
        checkOctetsColumn(columnIndex, expectedType, expectedJdbcType, expectedValue);
    }

    private void checkNormalColumn(int columnIndex, Class<?> expectedType, int expectedJdbcType, Object expectedValue) throws Exception {
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

    private void checkOctetsColumn(int columnIndex, Class<?> expectedType, int expectedJdbcType, Object expectedValue) throws Exception {
        assertEquals(expectedType.getName(), octetsQueryResultMd.getColumnClassName(columnIndex));
        assertEquals(expectedJdbcType, octetsQueryResultMd.getColumnType(columnIndex));
        Object octetsResult = octetsQueryResult.getObject(columnIndex);
        assertEquals(expectedType, octetsResult.getClass());
        if (expectedType.equals(byte[].class)) {
            assertArrayEquals((byte[]) expectedValue, (byte[]) octetsResult);
        } else {
            assertEquals(expectedValue, octetsResult);
        }
    }
}
