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


import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[0].sqllen = TEST_STRING_SIZE;
        Object[] row = new byte[1][];
        ArrayList rows = new ArrayList();
        rows.add(row);		  
        FBResultSet rs = new FBResultSet(xsqlvars,rows);
		  rs.next();
//        stringField.sqldata = new byte[TEST_STRING_SIZE];
//        stringField.sqlind = 0;

        field = FBField.createField(xsqlvars[0],rs,0, false);
    }
    protected void tearDown() {
    }
    public void testBigDecimal() throws SQLException {
        BigDecimal testBigDecimal = new BigDecimal((double)TEST_LONG);
        field.setBigDecimal(testBigDecimal);
        field.copyOI();
        assertTrue(field.getBigDecimal().equals(testBigDecimal));
    }

    public void testString() throws SQLException {
        field.setString(TEST_STRING_SHORT);		  
//        assertTrue("String was not completed with spaces or is longer.",
//            field.getString().length() == TEST_STRING_SIZE);
        field.copyOI();
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
        try {
            field.setString(TEST_STRING_LONG);
            assertTrue("String longer than available space should not be allowed", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testObject() throws SQLException {
        field.setObject(TEST_STRING_SHORT);
//        assertTrue("String was not completed with spaces or is longer.",
//            field.getString().length() == TEST_STRING_SIZE);
        field.copyOI();
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
    }

    public void testUnicodeStream() throws SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setUnicodeStream(new ByteArrayInputStream(bytes), bytes.length);
        field.copyOI();
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
    public void testBinaryStream() throws SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
        field.copyOI();
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
    public void testAsciiStream() throws SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
        field.copyOI();
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
    public void testBytes() throws SQLException {
        field.setBytes(TEST_STRING_SHORT.getBytes());
        field.copyOI();
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
    public void testDate() throws SQLException {
        field.setDate(TEST_DATE);
        // because of the date-string-date conversion we loose the
        // time part of java.util.Date, and strictly speaking
        // TEST_DATE and field.getDate() objects are not equal.
        field.copyOI();
        assertTrue("Date values test failure",
            field.getDate().toString().equals(TEST_DATE.toString()));
    }
    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        // because of the time-string-time conversion we loose the
        // date part of java.util.Date, and strictly speaking
        // TEST_TIME and field.getTime() objects are not equal.
        field.copyOI();
        assertTrue("Time values test failure",
            field.getTime().toString().equals(TEST_TIME.toString()));
    }
}
