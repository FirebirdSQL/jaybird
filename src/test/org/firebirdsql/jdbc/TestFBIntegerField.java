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
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

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
	protected void setUp() throws SQLException{
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_LONG;
        byte[][] row = new byte[1][];
        java.util.ArrayList rows = new java.util.ArrayList();
        rows.add(row);		  
        FBResultSet rs = new FBResultSet(xsqlvars,rows);
		  rs.next();
//		intField.sqldata = new Integer(TEST_INT);
//		intField.sqlind = 0;

		field = FBField.createField(xsqlvars[0],rs,0,false);
	}
	protected void tearDown() {
	}

	public void testObject() throws java.sql.SQLException {
		field.setObject(new Integer(TEST_INT));
		field.copyOI();
		assertTrue(field.getObject().equals(new Integer(TEST_INT)));
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
		field.setString(Integer.toString(TEST_INT));
		field.copyOI();
		assertTrue(field.getString().equals(Integer.toString(TEST_INT)));
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
			java.math.BigDecimal.valueOf((long)TEST_INT);
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
	public void testLong() throws java.sql.SQLException {
		try {
			super.testLong();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testFloat() throws java.sql.SQLException {
		try {
			super.testFloat();
			assertTrue("This method should fail.", false);
		} catch(SQLException sqlex) {
			//everything is ok :)
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
