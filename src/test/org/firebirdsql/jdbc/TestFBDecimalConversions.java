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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFBDecimalConversions extends FBJUnit4TestBase {
    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE decimal_test (" +
            "  id INT, " +
            "  col_64bit DECIMAL(18,2), " +
            "  col_32bit NUMERIC(8,2), " +
            "  col_16bit NUMERIC(2,2), " +
            "  col_double_precision DOUBLE PRECISION, " +
            "  col_float FLOAT " +
            ")";
    //@formatter:on

    private static final String INSERT_RECORD_1 =
            "INSERT INTO decimal_test VALUES (1, 10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0)";

    private static final String SELECT_RECORD_1 =
            "SELECT * FROM decimal_test where id = 1";

    private static final String INSERT_RECORD_2 =
            "INSERT INTO decimal_test VALUES (2, 5840813343806525.49, -16.92, -16.92, -16.92, -16.92)";

    private static final String SELECT_RECORD_2 =
            "SELECT * FROM decimal_test where id = 2";

    private static final String UPDATE_RECORD =
            "UPDATE decimal_test SET col_64bit = ? where id = 2";

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
            stmt.executeUpdate(INSERT_RECORD_1);
            stmt.executeUpdate(INSERT_RECORD_2);
        }
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testFloat() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_RECORD_1)) {

            assertTrue("ResultSet should not be empty.", rs.next());

            float decimalFloat = rs.getFloat(2);
            assertEquals("DECIMAL(18,2) of 10.0/3.0 should be exactly 3.33 instead of " + decimalFloat,
                    (float) 3.33, decimalFloat, 0.0);

            float numeric32Float = rs.getFloat(3);
            assertEquals("NUMERIC(8,2) of 10.0/3.0 should be exactly 3.33 instead of " + numeric32Float,
                    (float) 3.33, numeric32Float, 0.0);

            float numeric16Float = rs.getFloat(4);
            assertEquals("NUMERIC(2,2) of 10.0/3.0 should be exactly 3.33 instead of " + numeric16Float,
                    (float) 3.33, numeric16Float, 0.0);

            float floatFloat = rs.getFloat(5);
            assertEquals("FLOAT of 10.0/3.0 should be the same to 3.33 instead of " + floatFloat,
                    (float) 3.33, floatFloat, 0.0);

            float doubleFloat = rs.getFloat(6);
            assertEquals("DOUBLE PRECISION of 10.0/3.0 should be the same to 3.33 instead of " + doubleFloat,
                    (float) 3.33, doubleFloat, 0.0);
        }
    }

    @Test
    public void testDecimal() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_RECORD_2)) {
            assertTrue("ResultSet should not be empty.", rs.next());

            BigDecimal bigDecimal = rs.getBigDecimal(2);
            assertEquals("DECIMAL(18,2) of 5840813343806525.49 should be exact instead of " + bigDecimal,
                    new BigDecimal("5840813343806525.49"), bigDecimal);
        }
    }

    @Test
    public void testSetDecimal() throws Exception {
        BigDecimal test = new BigDecimal("5840813343806525.49");
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_RECORD)) {
            ps.setBigDecimal(1, test);
            ps.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_RECORD_2)) {
            assertTrue("ResultSet should not be empty.", rs.next());

            BigDecimal bigDecimal = rs.getBigDecimal(2);
            assertEquals("DECIMAL(18,2) of " + test + " should be exact instead of " + bigDecimal, bigDecimal, test);
        }
    }

}
