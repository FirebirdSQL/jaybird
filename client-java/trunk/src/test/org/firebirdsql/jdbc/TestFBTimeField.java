/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import junit.framework.*;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.GDS;

import java.sql.*;
import java.io.*;

public class TestFBTimeField extends BaseTestFBField {
	public TestFBTimeField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBTimeField.class);
	}
	protected void setUp() throws SQLException{
		XSQLVAR stringField = new XSQLVAR();
		stringField.sqldata = TEST_TIME;
		stringField.sqlind = 0;
		stringField.sqltype = GDS.SQL_TYPE_TIME;

		field = FBField.createField(stringField);
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
		assertTrue("Object value test failure",
			field.getString().equals(TEST_TIME.toString()));
	}

}