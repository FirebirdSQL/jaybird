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

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Firebird 4 extended precision of 38 for decimal (and numeric).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class DecimalPrecision38SupportTest {

    private static final String CREATE_TABLE =
            "create table extendeddecimal ("
                    + "id integer primary key,"
                    + "decimal19_2 decimal(19, 2),"
                    + "numeric19_2 numeric(19, 2),"
                    + "decimal38_4 numeric(38, 4),"
                    + "numeric38_4 numeric(38, 4)"
                    + ")";

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(supportInfo -> supportInfo.supportsDecimalPrecision(38),
                    "Requires DECIMAL/NUMERIC precision 38 support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static Connection connection;

    @BeforeAll
    static void setupAll() throws Exception{
        connection = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setUp() throws Exception {
        connection.setAutoCommit(true);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from extendeddecimal");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void simpleValuesSelect() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute("insert into extendeddecimal (id, decimal19_2, numeric19_2) "
                    + "values (1, 12345678901234567.89, 12345678901234567.89)");
            stmt.execute("insert into extendeddecimal (id, decimal19_2, numeric19_2) "
                    + "values (2, -12345678901234567.89, -12345678901234567.89)");

            try (ResultSet rs = stmt.executeQuery(
                    "select id, "
                            + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                            + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                            + "from extendeddecimal order by id")) {
                String row1ExpectedString = "12345678901234567.89";
                BigDecimal row1Expected = new BigDecimal(row1ExpectedString);
                assertRow(rs, 1, row1Expected, row1ExpectedString, row1Expected, row1ExpectedString);

                String row2ExpectedString = "-12345678901234567.89";
                BigDecimal row2Expected = new BigDecimal(row2ExpectedString);
                assertRow(rs, 2, row2Expected, row2ExpectedString, row2Expected, row2ExpectedString);
            }
        }
    }

    @Test
    void simpleInsert() throws Exception {
        final String stringValue = "12345678901234567.89";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        final String stringValue2 = "-12345678901234567.89";
        final BigDecimal bdValue2 = new BigDecimal(stringValue2);
        try (PreparedStatement pstmt = connection.prepareStatement(
                "insert into extendeddecimal(id, decimal19_2, numeric19_2) values (?, ?, ?)")) {
            execute(pstmt, 1, bdValue, bdValue);
            execute(pstmt, 2, bdValue2, bdValue2);
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "select id, "
                             + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                             + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                             + "from extendeddecimal order by id")) {
            assertRow(rs, 1, bdValue, stringValue, bdValue, stringValue);
            assertRow(rs, 2, bdValue2, stringValue2, bdValue2, stringValue2);
        }
    }

    /**
     * Both DECIMAL and NUMERIC with precision >= 19 will actually have precision 38.
     */
    @Test
    void insert_actualPrecisionIs38() throws Exception {
        final String stringValue = "123456789012345678901234567890123456.78";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        connection.setAutoCommit(false);
        try (PreparedStatement pstmt = connection.prepareStatement(
                "insert into extendeddecimal(id, decimal19_2, numeric19_2) values (?, ?, ?)")) {
            execute(pstmt, 1, bdValue, bdValue);
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "select id, "
                             + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                             + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                             + "from extendeddecimal order by id")) {
            assertRow(rs, 1, bdValue, stringValue, bdValue, stringValue);
        }
    }

    @Test
    void select_decimal19_2_ResultSetMetaData() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decimal19_2 from extendeddecimal")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("DECIMAL", rsmd.getColumnTypeName(1), "typeName");
            assertEquals(Types.DECIMAL, rsmd.getColumnType(1), "type");
            assertEquals("java.math.BigDecimal", rsmd.getColumnClassName(1), "className");
            assertEquals(19, rsmd.getPrecision(1), "precision");
            assertEquals(2, rsmd.getScale(1), "scale");
            assertEquals(21, rsmd.getColumnDisplaySize(1), "displaySize");
            assertTrue(rsmd.isSearchable(1), "searchable");
            assertTrue(rsmd.isSigned(1), "signed");
        }
    }

    @Test
    void select_numeric19_2_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select numeric19_2 from extendeddecimal")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("NUMERIC", rsmd.getColumnTypeName(1), "typeName");
            assertEquals(Types.NUMERIC, rsmd.getColumnType(1), "type");
            assertEquals("java.math.BigDecimal", rsmd.getColumnClassName(1), "className");
            assertEquals(19, rsmd.getPrecision(1), "precision");
            assertEquals(2, rsmd.getScale(1), "scale");
            assertEquals(21, rsmd.getColumnDisplaySize(1), "displaySize");
            assertTrue(rsmd.isSearchable(1), "searchable");
            assertTrue(rsmd.isSigned(1), "signed");
        }
    }

    @Test
    void insertAndSelectMinAndMaxValues() throws Exception {
        final String stringValue = "9999999999999999999999999999999999.9999";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        final String stringValue2 = "-9999999999999999999999999999999999.9999";
        final BigDecimal bdValue2 = new BigDecimal(stringValue2);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into extendeddecimal(id, decimal38_4, numeric38_4) values (?, ?, ?)")) {
                execute(pstmt, 1, bdValue, bdValue);
                execute(pstmt, 2, bdValue2, bdValue2);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "decimal38_4, cast(decimal38_4 as varchar(50)) str_decimal38_4, "
                                 + "numeric38_4, cast(numeric38_4 as varchar(50)) str_numeric38_4 "
                                 + "from extendeddecimal order by id")) {
                assertRow38_4(rs, 1, bdValue, stringValue, bdValue, stringValue);
                assertRow38_4(rs, 2, bdValue2, stringValue2, bdValue2, stringValue2);
            }
        }
    }

    private static void execute(PreparedStatement insert, int index, BigDecimal decimal2, BigDecimal numeric3)
            throws Exception {
        insert.setInt(1, index);
        insert.setBigDecimal(2, decimal2);
        insert.setBigDecimal(3, numeric3);
        insert.execute();
    }

    private static void assertRow(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecimal19_2, String expectedDecimal19_2String,
            BigDecimal expectedNumeric19_2, String expectedNumeric19_2String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(resultSet.next(), prefix + " expected row");
        assertEquals(expectedId, resultSet.getInt("id"), prefix);
        assertEquals(expectedDecimal19_2, resultSet.getBigDecimal("decimal19_2"), prefix + " decimal19_2");
        assertEquals(expectedNumeric19_2, resultSet.getBigDecimal("numeric19_2"), prefix + " numeric19_2");
        assertEquals(expectedDecimal19_2String, resultSet.getString("str_decimal19_2"), prefix + " str_decimal19_2");
        assertEquals(expectedNumeric19_2String, resultSet.getString("str_numeric19_2"), prefix + " str_numeric19_2");
    }

    private static void assertRow38_4(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecimal19_2, String expectedDecimal19_2String,
            BigDecimal expectedNumeric19_2, String expectedNumeric19_2String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(resultSet.next(), prefix + " expected row");
        assertEquals(expectedId, resultSet.getInt("id"), prefix);
        assertEquals(expectedDecimal19_2, resultSet.getBigDecimal("decimal38_4"), prefix + " decimal38_4");
        assertEquals(expectedNumeric19_2, resultSet.getBigDecimal("numeric38_4"), prefix + " numeric38_4");
        assertEquals(expectedDecimal19_2String, resultSet.getString("str_decimal38_4"), prefix + " str_decimal38_4");
        assertEquals(expectedNumeric19_2String, resultSet.getString("str_numeric38_4"), prefix + " str_numeric38_4");
    }

}
