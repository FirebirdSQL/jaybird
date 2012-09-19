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
 * Describe class <code>TestFBShortField</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBShortField extends BaseTestFBField {
	public TestFBShortField(String testName) {
		super(testName);
	}

	protected void setUp() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[1];
        xsqlvars[0] = createXSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_SHORT;
        field = FBField.createField(xsqlvars[0], createDataProvider(xsqlvars), null, false);
	}

	protected void tearDown() {
	}

	public static Test suite() {
		return new TestSuite(TestFBShortField.class);
	}



	public void testObject() throws SQLException {
		field.setObject(new Short(TEST_SHORT));
		field.copyOI();
		assertTrue(field.getObject().equals(new Integer(TEST_SHORT)));
	}
	public void testUnicodeStream() throws SQLException {
		try {
			super.testUnicodeStream();
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
	public void testString() throws SQLException {
		field.setString(Short.toString(TEST_SHORT));
		field.copyOI();
		assertTrue(field.getString().equals(Short.toString(TEST_SHORT)));
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
		BigDecimal testBigDecimal =	BigDecimal.valueOf(TEST_SHORT);
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
		try {
			super.testInteger();
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
	public void testFloat() throws SQLException {
		try {
			super.testFloat();
			assertTrue("This method should fail.", false);
		} catch(SQLException sqlex) {
			//everything is ok :)
		}
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
