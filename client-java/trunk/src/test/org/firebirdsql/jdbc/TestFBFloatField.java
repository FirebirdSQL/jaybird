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
 * Describe class <code>TestFBFloatField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBFloatField extends BaseTestFBField {
	public TestFBFloatField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBFloatField.class);
	}
	protected void setUp() throws SQLException{
		XSQLVAR floatField = new XSQLVAR();
		Object[] row = new Object[1];
//		floatField.sqldata = new Float(TEST_FLOAT);
//		floatField.sqlind = 0;
		floatField.sqltype = GDS.SQL_FLOAT;

		field = FBField.createField(floatField, row, 0);
	}
	protected void tearDown() {
	}

	public void testObject() throws java.sql.SQLException {
		field.setObject(new Float(TEST_FLOAT));
		field.copyOI();
		assertTrue(field.getObject().equals(new Float(TEST_FLOAT)));
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
		field.setString(Float.toString(TEST_FLOAT));
		field.copyOI();		
		assertTrue(field.getString().equals(Float.toString(TEST_FLOAT)));
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
		java.math.BigDecimal testBigDecimal =
			new java.math.BigDecimal((double)TEST_FLOAT);
		field.setBigDecimal(testBigDecimal);
		field.copyOI();		
		assertTrue(field.getBigDecimal().equals(testBigDecimal));
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
	public void testInteger() throws java.sql.SQLException {
		// unfortunately (long)((float)myLong) != myLong....
		// so we can test only float values...
		field.setLong(TEST_INT);
		field.copyOI();		
		assertTrue(field.getFloat() == (float)TEST_INT);
	}
	public void testLong() throws java.sql.SQLException {
		// unfortunately (long)((float)myLong) != myLong....
		// so we can test only float values...
		field.setLong(TEST_LONG);
		field.copyOI();		
		assertTrue(field.getFloat() == (float)TEST_LONG);
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
