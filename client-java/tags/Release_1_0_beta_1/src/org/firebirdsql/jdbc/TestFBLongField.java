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

import java.sql.SQLException;


public class TestFBLongField extends BaseTestFBField {
	public TestFBLongField(String testName) {
		super(testName);
	}
	public static Test suite() {
		return new TestSuite(TestFBLongField.class);
	}
	protected void setUp() throws SQLException{
		XSQLVAR longField = new XSQLVAR();
		longField.sqldata = new Long(TEST_LONG);
		longField.sqlind = 0;
		longField.sqltype = GDS.SQL_INT64;

		field = FBField.createField(longField);
	}
	protected void tearDown() {
	}

	public void testObject() throws java.sql.SQLException {
		field.setObject(new Long(TEST_LONG));
		assertTrue(field.getObject().equals(new Long(TEST_LONG)));
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
		field.setString(Long.toString(TEST_LONG));
		assertTrue(field.getString().equals(Long.toString(TEST_LONG)));
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
		//unfortunatelly we loose some digits while converting
		// between BigDecimal and long, so we have to test long values
		java.math.BigDecimal testBigDecimal =
			new java.math.BigDecimal((double)TEST_LONG);
		field.setBigDecimal(testBigDecimal);
		assertTrue(field.getLong() == testBigDecimal.longValue());
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
	public void testFloat() throws java.sql.SQLException {
		//unfortunatelly we loose some digits while converting
		// between float and long, so we have to test long values
		field.setFloat(TEST_FLOAT);
		assertTrue("Float values test failure", field.getLong() == (long)TEST_FLOAT);
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