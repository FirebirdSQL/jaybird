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

import java.sql.*;

/**
 * Describe class <code>TestFBDecimalConversions</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBDecimalConversions extends BaseFBTest {
    public static final String CREATE_TABLE =
        "CREATE TABLE decimal_test (" +
        "  col_64bit DECIMAL(18,2), " +
        "  col_32bit NUMERIC(8,2), " +
        "  col_16bit NUMERIC(2,2), " +
        "  col_double_precision DOUBLE PRECISION, " +
        "  col_float FLOAT " +
        ")";

    public static final String DROP_TABLE =
        "DROP TABLE decimal_test";

    public static final String INSERT_RECORD =
        "INSERT INTO decimal_test VALUES (10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0, 10.0/3.0)";

    public static final String SELECT_RECORD =
        "SELECT * FROM decimal_test";

    public TestFBDecimalConversions(String testName) {
        super(testName);
    }

    private java.sql.Connection connection;

    protected void setUp() throws Exception {
        super.setUp();

        Class.forName(FBDriver.class.getName());
        connection =
            java.sql.DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);

        java.sql.Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        stmt.executeUpdate(CREATE_TABLE);
        stmt.executeUpdate(INSERT_RECORD);

        stmt.close();

    }

    protected void tearDown() throws Exception {
        /*
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        */

        connection.close();

        super.tearDown();
    }

    public void testFloat() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_RECORD);

        assertTrue("ResultSet should not be empty.", rs.next());

        float decimalFloat = rs.getFloat(1);
        assertTrue("DECIMAL(18,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            decimalFloat, decimalFloat == (float)3.33);

        float numeric32Float = rs.getFloat(2);
        assertTrue("NUMERIC(8,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric32Float, numeric32Float == (float)3.33);

        float numeric16Float = rs.getFloat(3);
        assertTrue("NUMERIC(2,2) of 10.0/3.0 should be exactly 3.33 instead of " +
            numeric16Float, numeric16Float == (float)3.33);

        float floatFloat = rs.getFloat(4);
        assertTrue("FLOAT of 10.0/3.0 should be the same to 3.33 instead of " +
            floatFloat, floatFloat == (float)3.33);

        float doubleFloat = rs.getFloat(5);
        assertTrue("DOUBLE PRECISION of 10.0/3.0 should be the same to 3.33 instead of " +
            doubleFloat, doubleFloat == (float)3.33);

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestFBDecimalConversions.class);
    }
}
