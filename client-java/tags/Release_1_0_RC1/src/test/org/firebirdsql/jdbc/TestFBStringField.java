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

import junit.framework.*;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.GDS;

import java.sql.SQLException;
import java.io.*;

/**
 * Describe class <code>TestFBStringField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBStringField extends BaseTestFBField {
    static short TEST_STRING_SIZE = 40;
    // TEST_STRING_LONG should be shorter than TEST_STRING_SIZE
    static String TEST_STRING_SHORT = "This is short string.";
    // TEST_STRING_LONG should be longer than TEST_STRING_SIZE
    static String TEST_STRING_LONG =
        "And this string should be longer than short one.";

    public TestFBStringField(String testName) {
        super(testName);
    }
    public static Test suite() {
        return new TestSuite(TestFBStringField.class);
    }
    protected void setUp() throws SQLException{
        XSQLVAR stringField = new XSQLVAR();
        stringField.sqldata = new byte[TEST_STRING_SIZE];
        stringField.sqlind = 0;
        stringField.sqllen = TEST_STRING_SIZE;
        stringField.sqltype = GDS.SQL_TEXT;

        field = FBField.createField(stringField);
    }
    protected void tearDown() {
    }
    public void testBigDecimal() throws java.sql.SQLException {
        java.math.BigDecimal testBigDecimal =
            new java.math.BigDecimal((double)TEST_LONG);
        field.setBigDecimal(testBigDecimal);
        assertTrue(field.getBigDecimal().equals(testBigDecimal));
    }
    public void testString() throws java.sql.SQLException {
        field.setString(TEST_STRING_SHORT);
        assertTrue("String was not completed with spaces or is longer.",
            field.getString().length() == TEST_STRING_SIZE);
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
        try {
            field.setString(TEST_STRING_LONG);
            assertTrue("String longer than available space should not be allowed", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testObject() throws java.sql.SQLException {
        field.setObject(TEST_STRING_SHORT);
        assertTrue("String was not completed with spaces or is longer.",
            field.getString().length() == TEST_STRING_SIZE);
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
    }
    public void testUnicodeStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setUnicodeStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getUnicodeStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();            
            field.setUnicodeStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testBinaryStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getBinaryStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();            
            field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testAsciiStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getAsciiStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();
            field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testBytes() throws java.sql.SQLException {
        field.setBytes(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getBytes());
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromBytes.trim()));
        try {
            field.setBytes(TEST_STRING_LONG.getBytes());
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testDate() throws java.sql.SQLException {
        field.setDate(TEST_DATE);
        // because of the date-string-date conversion we loose the
        // time part of java.util.Date, and strictly speaking
        // TEST_DATE and field.getDate() objects are not equal.
        assertTrue("Date values test failure",
            field.getDate().toString().equals(TEST_DATE.toString()));
    }
    public void testTime() throws java.sql.SQLException {
        field.setTime(TEST_TIME);
        // because of the time-string-time conversion we loose the
        // date part of java.util.Date, and strictly speaking
        // TEST_TIME and field.getTime() objects are not equal.
        assertTrue("Time values test failure",
            field.getTime().toString().equals(TEST_TIME.toString()));
    }
}
