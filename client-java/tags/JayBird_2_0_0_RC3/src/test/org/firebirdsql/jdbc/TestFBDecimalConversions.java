/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Describe class <code>TestFBDecimalConversions</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBDecimalConversions extends FBTestBase {
    public static final String CREATE_TABLE =
        "CREATE TABLE decimal_test (" +
        "  id INT, " +
        "  col_64bit DECIMAL(18,2), " +
        "  col_32bit NUMERIC(8,2), " +
        "  col_16bit NUMERIC(2,2), " +
        "  col_double_precision DOUBLE PRECISION, " +
        "  col_float FLOAT " +
        ")";

    public static final String DROP_TABLE =
        "DROP TABLE decimal_test";

    public static final String INSERT_RECORD_1 =
        "INSERT INTO decimal_test VALUES (1, 10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0)";

    public static final String SELECT_RECORD_1 =
        "SELECT * FROM decimal_test where id = 1";

    public static final String INSERT_RECORD_2 =
        "INSERT INTO decimal_test VALUES (2, 5840813343806525.49, -16.92, -16.92, -16.92, -16.92)";

    public static final String SELECT_RECORD_2 =
        "SELECT * FROM decimal_test where id = 2";

    public static final String UPDATE_RECORD = 
        "UPDATE decimal_test SET col_64bit = ? where id = 2";

    public TestFBDecimalConversions(String testName) {
        super(testName);
    }

    private Connection connection;

    protected void setUp() throws Exception {
        super.setUp();

        Class.forName(FBDriver.class.getName());
        connection = getConnectionViaDriverManager();

        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        stmt.executeUpdate(CREATE_TABLE);
        stmt.executeUpdate(INSERT_RECORD_1);
        stmt.executeUpdate(INSERT_RECORD_2);

        stmt.close();

    }

    protected void tearDown() throws Exception {
        /*
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        */

        connection.close();

        super.tearDown();
    }

    public void testFloat() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_RECORD_1);

        assertTrue("ResultSet should not be empty.", rs.next());

        float decimalFloat = rs.getFloat(2);
        assertTrue("DECIMAL(18,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            decimalFloat, decimalFloat == (float)3.33);

        float numeric32Float = rs.getFloat(3);
        assertTrue("NUMERIC(8,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric32Float, numeric32Float == (float)3.33);

        float numeric16Float = rs.getFloat(4);
        assertTrue("NUMERIC(2,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric16Float, numeric16Float == (float)3.33);

        float floatFloat = rs.getFloat(5);
        assertTrue("FLOAT of 10.0/3.0 should be the same to 3.33 instead of " +
            floatFloat, floatFloat == (float)3.33);

        float doubleFloat = rs.getFloat(6);
        assertTrue("DOUBLE PRECISION of 10.0/3.0 should be the same to 3.33 instead of " +
            doubleFloat, doubleFloat == (float)3.33);

    }

    public void testDecimal() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_RECORD_2);

        assertTrue("ResultSet should not be empty.", rs.next());

        BigDecimal bigDecimal = rs.getBigDecimal(2);
        assertTrue("DECIMAL(18,2) of 5840813343806525.49 should be exact instead of " +
            bigDecimal, bigDecimal.equals(new BigDecimal("5840813343806525.49")));
        /*

        float numeric32Float = rs.getFloat(3);
        assertTrue("NUMERIC(8,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric32Float, numeric32Float == (float)3.33);

        float numeric16Float = rs.getFloat(4);
        assertTrue("NUMERIC(2,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric16Float, numeric16Float == (float)3.33);

        float floatFloat = rs.getFloat(5);
        assertTrue("FLOAT of 10.0/3.0 should be the same to 3.33 instead of " +
            floatFloat, floatFloat == (float)3.33);

        float doubleFloat = rs.getFloat(6);
        assertTrue("DOUBLE PRECISION of 10.0/3.0 should be the same to 3.33 instead of " +
            doubleFloat, doubleFloat == (float)3.33);
        */
    }

    public void testSetDecimal() throws Exception {
        BigDecimal test = new BigDecimal("5840813343806525.49");
        PreparedStatement ps = connection.prepareStatement(UPDATE_RECORD);
        ps.setBigDecimal(1, test);
        ps.execute();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_RECORD_2);

        assertTrue("ResultSet should not be empty.", rs.next());

        BigDecimal bigDecimal = rs.getBigDecimal(2);
        assertTrue("DECIMAL(18,2) of " + test + " should be exact instead of " +
            bigDecimal, bigDecimal.equals(test));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestFBDecimalConversions.class);
    }
}
