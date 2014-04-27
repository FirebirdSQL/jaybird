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

package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;

import java.math.BigDecimal;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Describe class <code>TestFBIntegerField</code> here.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBIntegerField extends BaseTestFBField {
    
    public TestFBIntegerField(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFBIntegerField.class);
    }

    protected void setUp() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_LONG;
        field = FBField.createField(xsqlvars[0], createDataProvider(xsqlvars), null, false);
    }

    public void testObject() throws SQLException {
        field.setObject(new Integer(TEST_INT));
        field.copyOI();
        assertEquals(Integer.valueOf(TEST_INT), field.getObject());
    }

    public void testUnicodeStream() throws SQLException {
        try {
            super.testUnicodeStream();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testBinaryStream() throws SQLException {
        try {
            super.testBinaryStream();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }

    }

    public void testString() throws SQLException {
        field.setString(Integer.toString(TEST_INT));
        field.copyOI();
        assertEquals(Integer.toString(TEST_INT), field.getString());
    }

    public void testAsciiStream() throws SQLException {
        try {
            super.testAsciiStream();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testTimestamp() throws SQLException {
        try {
            super.testTimestamp();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testBigDecimal() throws SQLException {
        BigDecimal testBigDecimal = BigDecimal.valueOf(TEST_INT);
        field.setBigDecimal(testBigDecimal);
        field.copyOI();
        assertEquals(testBigDecimal, field.getBigDecimal());
    }

    public void testDate() throws SQLException {
        try {
            super.testDate();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testTime() throws SQLException {
        try {
            super.testTime();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testBytes() throws SQLException {
        try {
            super.testBytes();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }

    }

    public void testLong() throws SQLException {
        try {
            super.testLong();
            fail("This method should fail.");
        } catch (SQLException ex) {
            // everything is ok :)
        }
    }

    public void testFloat() throws SQLException {
        try {
            super.testFloat();
            fail("This method should fail.");
        } catch (SQLException sqlex) {
            // everything is ok :)
        }
    }

    public void testDouble() throws SQLException {
        try {
            super.testDouble();
            fail("This method should fail.");
        } catch (SQLException sqlex) {
            // everything is ok :)
        }
    }
}
