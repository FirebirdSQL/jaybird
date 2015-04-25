/*
 * $Id$
 *
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Test for boolean fields. Note that boolean fields are only supported in Firebird 3.0 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBBooleanField extends BaseTestFBField {

    public TestFBBooleanField(String testName) {
        super(testName);
    }

    protected void setUp() throws SQLException{
        final XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_BOOLEAN;

        field = FBField.createField(xsqlvars[0], createDataProvider(xsqlvars), null, false);
    }

    public void testObject() throws SQLException {
        field.setObject(true);
        field.copyOI();
        assertEquals(true, field.getObject());
    }

    public void testBigDecimal() throws SQLException {
        field.setBigDecimal(BigDecimal.valueOf(TEST_INT));
        field.copyOI();
        assertEquals(BigDecimal.ONE, field.getBigDecimal());
    }

    public void testString() throws SQLException {
        field.setString("true");
        field.copyOI();
        assertEquals("true", field.getString());
    }

    public void testByte() throws SQLException {
        field.setByte(TEST_BYTE);
        field.copyOI();
        assertEquals(1, field.getByte());
    }

    public void testLong() throws SQLException {
        field.setLong(TEST_LONG);
        field.copyOI();
        assertEquals(1, field.getLong());
    }

    public void testFloat() throws SQLException {
        field.setFloat(TEST_FLOAT);
        field.copyOI();
        assertEquals(1, field.getFloat(), 0);
    }

    public void testInteger() throws SQLException {
        field.setInteger(TEST_INT);
        field.copyOI();
        assertEquals(1, field.getInt());
    }

    public void testShort() throws SQLException {
        field.setShort(TEST_SHORT);
        field.copyOI();
        assertEquals(1, field.getShort());
    }

    public void testDouble() throws SQLException {
        field.setDouble(TEST_DOUBLE);
        field.copyOI();
        assertEquals(1, field.getDouble(), 0);
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
}
