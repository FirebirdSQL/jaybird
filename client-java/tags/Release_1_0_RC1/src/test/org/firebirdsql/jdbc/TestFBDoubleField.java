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
 * Describe class <code>TestFBDoubleField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBDoubleField extends BaseTestFBField {
	public TestFBDoubleField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBDoubleField.class);
	}
	protected void setUp() throws SQLException{
		XSQLVAR doubleField = new XSQLVAR();
		doubleField.sqldata = new Double(TEST_DOUBLE);
		doubleField.sqlind = 0;
		doubleField.sqltype = GDS.SQL_DOUBLE;

		field = FBField.createField(doubleField);
	}
	protected void tearDown() {
	}

	public void testObject() throws java.sql.SQLException {
		field.setObject(new Double(TEST_DOUBLE));
		assertTrue(field.getObject().equals(new Double(TEST_DOUBLE)));
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
			new java.math.BigDecimal((double)TEST_DOUBLE);
		field.setBigDecimal(testBigDecimal);
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
	public void testLong() throws java.sql.SQLException {
		// unfortunately (long)((double)myLong) != myLong....
		// so we can test only float values...
		field.setLong(TEST_LONG);
		assertTrue(field.getFloat() == (float)TEST_LONG);
	}
}
