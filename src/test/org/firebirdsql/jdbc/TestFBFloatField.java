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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_FLOAT;
        byte[][] row = new byte[1][];
        ArrayList rows = new ArrayList();
        rows.add(row);		  
        FBResultSet rs = new FBResultSet(xsqlvars,rows);
		  rs.next();
//		floatField.sqldata = new Float(TEST_FLOAT);
//		floatField.sqlind = 0;

		field = FBField.createField(xsqlvars[0], rs, 0, false);
	}
	protected void tearDown() {
	}

	public void testObject() throws SQLException {
		field.setObject(new Float(TEST_FLOAT));
		field.copyOI();
		assertTrue(field.getObject().equals(new Float(TEST_FLOAT)));
	}
	public void testUnicodeStream() throws SQLException {
		try {
			super.testUnicodeStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testByte() throws SQLException {
		super.testByte();
	}
	public void testBinaryStream() throws SQLException {
		try {
			super.testBinaryStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}

	}
	public void testString() throws SQLException {
		field.setString(Float.toString(TEST_FLOAT));
		field.copyOI();		
		assertTrue(field.getString().equals(Float.toString(TEST_FLOAT)));
	}
	public void testAsciiStream() throws SQLException {
		try {
			super.testAsciiStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testTimestamp() throws SQLException {
		try {
			super.testTimestamp();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBigDecimal() throws SQLException {
		BigDecimal testBigDecimal =	new BigDecimal((double)TEST_FLOAT);
		field.setBigDecimal(testBigDecimal);
		field.copyOI();		
		assertTrue(field.getBigDecimal().equals(testBigDecimal));
	}
	public void testDate() throws SQLException {
		try {
			super.testDate();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testTime() throws SQLException {
		try {
			super.testTime();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBytes() throws SQLException {
		try {
			super.testBytes();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testInteger() throws SQLException {
		// unfortunately (long)((float)myLong) != myLong....
		// so we can test only float values...
		field.setLong(TEST_INT);
		field.copyOI();		
		assertTrue(field.getFloat() == (float)TEST_INT);
	}
	public void testLong() throws SQLException {
		// unfortunately (long)((float)myLong) != myLong....
		// so we can test only float values...
		field.setLong(TEST_LONG);
		field.copyOI();		
		assertTrue(field.getFloat() == (float)TEST_LONG);
	}
	public void testDouble() throws SQLException {
		try {
			super.testDouble();
			assertTrue("This method should fail.", false);
		} catch(SQLException sqlex) {
			//everything is ok :)
		}
	}
}
