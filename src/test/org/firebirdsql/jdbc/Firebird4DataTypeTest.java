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

import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the limited support for Firebird 4 datatypes.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class Firebird4DataTypeTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @BeforeClass
    public static void requireFirebird4() {
        assumeTrue("Test requires Firebird 4 or higher", getDefaultSupportInfo().isVersionEqualOrAbove(4, 0));
    }

    @Test
    public void testTimeZoneBind() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("timeZoneBind", "legacy");
        // Ensure consistent value
        props.setProperty("sessionTimeZone", TimeZone.getDefault().getID());

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             // NOTE: CURRENT_TIMESTAMP produces a TIMESTAMP WITH TIME ZONE
             ResultSet rs = stmt.executeQuery("select current_timestamp from rdb$database")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected Types.TIMESTAMP", Types.TIMESTAMP, rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            Timestamp timestamp = rs.getTimestamp(1);
            Date currentDate = new Date();
            long difference = currentDate.getTime() - timestamp.getTime();

            assertTrue("Difference between " + currentDate + " and " + timestamp + " too large",
                    Math.abs(difference) < 1000);
        }
    }

    @Test
    public void testDefloatBindCharOnDecfloat() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("decfloatBind", "char");

        final String testValue = "9.999999999999999999999999999999999E+6144";
        final BigDecimal expectedBigDecimal = new BigDecimal(testValue);

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select cast('" + testValue + "' as DECFLOAT(34)) from rdb$database")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected Types.CHAR", Types.CHAR, rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            assertEquals("Unexpected string value", testValue, rs.getString(1).trim());
            assertEquals("Unexpected big decimal value", expectedBigDecimal, rs.getBigDecimal(1));
        }
    }

    @Test
    public void testDefloatBindDoublePrecisionOnDecfloat() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("decfloatBind", "double precision");

        // Max value representable; larger DECFLOAT values will yield an error with default trap settings
        final String testValue = String.valueOf(Double.MAX_VALUE);

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select cast('" + testValue + "' as DECFLOAT(34)) from rdb$database")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected Types.DOUBLE", Types.DOUBLE, rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            assertEquals("Unexpected double value", Double.MAX_VALUE, rs.getDouble(1), Math.ulp(Double.MAX_VALUE));
        }
    }

    @Test
    public void testDefloatBindBigint3OnDecfloat() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("decfloatBind", "bigint,3");

        // Max value representable; larger DECFLOAT values will yield an error with default trap settings
        BigDecimal testValue = BigDecimal.valueOf(Long.MAX_VALUE, 3);

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select cast('" + testValue + "' as DECFLOAT(34)) from rdb$database")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected Types.NUMERIC", Types.NUMERIC, rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            assertEquals("Unexpected big decimal value", testValue, rs.getBigDecimal(1));
        }
    }
}
