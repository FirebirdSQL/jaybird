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


/**
 * Describe class <code>TestFBLongField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBLongField extends BaseTestFBField {
    public TestFBLongField(String testName) {
        super(testName);
    }
    public static Test suite() {
        return new TestSuite(TestFBLongField.class);
    }
    protected void setUp() throws SQLException{
		 
        XSQLVAR longField = new XSQLVAR();
        Object[] row = new Object[1];
//        longField.sqldata = new Long(TEST_LONG);
//        longField.sqlind = 0;
        longField.sqltype = GDS.SQL_INT64;

        field = FBField.createField(longField,row,0);
    }
    protected void tearDown() {
    }

    public void testObject() throws java.sql.SQLException {
        field.setObject(new Long(TEST_LONG));
        field.copyOI();
        assertTrue(field.getObject().equals(new Long(TEST_LONG)));
    }
    public void testUnicodeStream() throws java.sql.SQLException {
        try {
            super.testUnicodeStream();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }
    }
    public void testByte() throws java.sql.SQLException {
        super.testByte();
    }
    public void testBinaryStream() throws java.sql.SQLException {
        try {
            super.testBinaryStream();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }

    }
    public void testString() throws java.sql.SQLException {
        field.setString(Long.toString(TEST_LONG));
        field.copyOI();
        assertTrue(field.getString().equals(Long.toString(TEST_LONG)));
    }
    public void testAsciiStream() throws java.sql.SQLException {
        try {
            super.testAsciiStream();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }
    }
    public void testTimestamp() throws java.sql.SQLException {
        try {
            super.testTimestamp();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }
    }
    public void testBigDecimal() throws java.sql.SQLException {
        //unfortunatelly we loose some digits while converting
        // between BigDecimal and long, so we have to test long values
        java.math.BigDecimal testBigDecimal =
            java.math.BigDecimal.valueOf(TEST_LONG);
        field.setBigDecimal(testBigDecimal);
        field.copyOI();
        assertTrue(field.getLong() == testBigDecimal.longValue());
    }
    public void testDate() throws java.sql.SQLException {
        try {
            super.testDate();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }
    }
    public void testTime() throws java.sql.SQLException {
        try {
            super.testTime();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }
    }
    public void testBytes() throws java.sql.SQLException {
        try {
            super.testBytes();
            assertTrue("This method should fail.", false);
        } catch(SQLException ex) {
            //everything is ok :)
        }

    }
    public void testFloat() throws java.sql.SQLException {
        //unfortunatelly we loose some digits while converting
        // between float and long, so we have to test long values
        try {
            field.setFloat(TEST_FLOAT);
            field.copyOI();
            assertTrue("Float values test failure", field.getLong() == (long)TEST_FLOAT);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testDouble() throws java.sql.SQLException {
        try {
            super.testDouble();
            assertTrue("This method should fail.", false);
        } catch(SQLException sqlex) {
            //everything is ok :)
        }
    }
}
