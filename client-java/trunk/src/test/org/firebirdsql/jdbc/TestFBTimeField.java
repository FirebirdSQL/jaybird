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

import java.sql.*;
import java.io.*;

/**
 * Describe class <code>TestFBTimeField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBTimeField extends BaseTestFBField {
	public TestFBTimeField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBTimeField.class);
	}
	protected void setUp() throws SQLException{
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = GDS.SQL_TYPE_TIME;
        byte[][] row = new byte[1][];
        java.util.ArrayList rows = new java.util.ArrayList();
        rows.add(row);		  
        FBResultSet rs = new FBResultSet(xsqlvars,rows);
		  rs.next();
//		stringField.sqldata = TEST_TIME;
//		stringField.sqlind = 0;

		field = FBField.createField(xsqlvars[0],rs,0, false);
	}
	protected void tearDown() {
	}
	public void testShort() throws java.sql.SQLException {
		try {
			super.testShort();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testDouble() throws java.sql.SQLException {
		try {
			super.testDouble();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testLong() throws java.sql.SQLException {
		try {
			super.testLong();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
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
		try {
			super.testByte();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBoolean() throws java.sql.SQLException {
		try {
			super.testBoolean();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBinaryStream() throws java.sql.SQLException {
		try {
			super.testBinaryStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testFloat() throws java.sql.SQLException {
		try {
			super.testFloat();
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
	public void testAsciiStream() throws java.sql.SQLException {
		try {
			super.testAsciiStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testInteger() throws java.sql.SQLException {
		try {
			super.testInteger();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBigDecimal() throws java.sql.SQLException {
		try {
			field.setBigDecimal(new java.math.BigDecimal(TEST_DOUBLE));
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}

	//--- real test methods

	public void testDate() throws java.sql.SQLException {
		try {
			super.testDate();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testTimestamp() throws java.sql.SQLException {
		String timeStr = new Time(TEST_TIMESTAMP.getTime()).toString();
		field.setTimestamp(TEST_TIMESTAMP);
		field.copyOI();
		// we have to test string representation, because of conversion problem
		assertTrue("Timestamp value test failure.",
			field.getTime().toString().equals(timeStr));
	}
	public void testString() throws java.sql.SQLException {
		field.setString(TEST_TIME.toString());
		// we have to test string representation, because java.sql.Date
		// keeps the time part of the timestamp after creation, but
		// usually loses it after some conversions. So, date might
		// be the same, by object will differ. String comparison produces
		// stable results.
		field.copyOI();
		assertTrue("String value test failure",
			field.getString().equals(TEST_TIME.toString()));
	}
	public void testObject() throws java.sql.SQLException {
		field.setObject(TEST_TIME);
		// we have to test string representation, because java.sql.Date
		// keeps the time part of the timestamp after creation, but
		// usually loses it after some conversions. So, date might
		// be the same, by object will differ. String comparison produces
		// stable results.
		field.copyOI();
		assertTrue("Object value test failure",
			field.getString().equals(TEST_TIME.toString()));
	}

}
