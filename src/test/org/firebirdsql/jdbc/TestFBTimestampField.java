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


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Describe class <code>TestFBTimestampField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBTimestampField extends BaseTestFBField {
	public TestFBTimestampField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBTimestampField.class);
	}
	protected void setUp() throws SQLException{
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_TIMESTAMP;
        Object[] row = new byte[1][];
        ArrayList rows = new ArrayList();
        rows.add(row);		  
        FBResultSet rs = new FBResultSet(xsqlvars,rows);
		  rs.next();
//		stringField.sqldata = TEST_TIMESTAMP;
//		stringField.sqlind = 0;

		field = FBField.createField(xsqlvars[0],rs,0, false);
	}
	protected void tearDown() {
	}
	public void testShort() throws SQLException {
		try {
			super.testShort();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testDouble() throws SQLException {
		try {
			super.testDouble();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testLong() throws SQLException {
		try {
			super.testLong();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
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
		try {
			super.testByte();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBoolean() throws SQLException {
		try {
			super.testBoolean();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBinaryStream() throws SQLException {
		try {
			super.testBinaryStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testFloat() throws SQLException {
		try {
			super.testFloat();
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
	public void testAsciiStream() throws SQLException {
		try {
			super.testAsciiStream();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testInteger() throws SQLException {
		try {
			super.testInteger();
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}
	public void testBigDecimal() throws SQLException {
		try {
			field.setBigDecimal(new BigDecimal(TEST_DOUBLE));
			assertTrue("This method should fail.", false);
		} catch(SQLException ex) {
			//everything is ok :)
		}
	}

	//--- real test methods

	public void testString() throws SQLException {
		field.setString(TEST_TIMESTAMP.toString());
		field.copyOI();
		assertTrue("String value test failure: expected: " + TEST_TIMESTAMP + ", actual: " + field.getTimestamp(),
			field.getTimestamp().equals(TEST_TIMESTAMP));
	}
	public void testObject() throws SQLException {
		field.setObject(TEST_TIMESTAMP);
		field.copyOI();
		assertTrue("Object value test failure: expected: " + TEST_TIMESTAMP + ", actual: " + field.getTimestamp(),
			field.getTimestamp().equals(TEST_TIMESTAMP));
	}

}
