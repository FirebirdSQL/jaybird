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
import java.io.*;

public class TestFBStringField extends BaseTestFBField {
    static short TEST_STRING_SIZE = 40;
    // TEST_STRING_LONG should be shorter than TEST_STRING_SIZE
    static String TEST_STRING_SHORT = "This is short string.";
    // TEST_STRING_LONG should be longer than TEST_STRING_SIZE
    static String TEST_STRING_LONG =
        "And this string should be longer than short one.";

    public TestFBStringField(String testName) {
        super(testName);
    }
    public static Test suite() {
        return new TestSuite(TestFBStringField.class);
    }
    protected void setUp() throws SQLException{
        XSQLVAR stringField = new XSQLVAR();
        stringField.sqldata = new byte[TEST_STRING_SIZE];
        stringField.sqlind = 0;
        stringField.sqllen = TEST_STRING_SIZE;
        stringField.sqltype = GDS.SQL_TEXT;

        field = FBField.createField(stringField);
    }
    protected void tearDown() {
    }
    public void testBigDecimal() throws java.sql.SQLException {
        java.math.BigDecimal testBigDecimal =
            new java.math.BigDecimal((double)TEST_LONG);
        field.setBigDecimal(testBigDecimal);
        assertTrue(field.getBigDecimal().equals(testBigDecimal));
    }
    public void testString() throws java.sql.SQLException {
        field.setString(TEST_STRING_SHORT);
        assertTrue("String was not completed with spaces or is longer.",
            field.getString().length() == TEST_STRING_SIZE);
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
        try {
            field.setString(TEST_STRING_LONG);
            assertTrue("String longer than available space should not be allowed", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testObject() throws java.sql.SQLException {
        field.setObject(TEST_STRING_SHORT);
        assertTrue("String was not completed with spaces or is longer.",
            field.getString().length() == TEST_STRING_SIZE);
        assertTrue("String does not equal to assigned one.",
            field.getString().trim().equals(TEST_STRING_SHORT));
    }
    public void testUnicodeStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setUnicodeStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getUnicodeStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();            
            field.setUnicodeStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testBinaryStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getBinaryStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();            
            field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testAsciiStream() throws java.sql.SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getAsciiStream()));
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromStream.trim()));
        try {
            bytes = TEST_STRING_LONG.getBytes();
            field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testBytes() throws java.sql.SQLException {
        field.setBytes(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getBytes());
        assertTrue("ASCII stream values test failure",
            TEST_STRING_SHORT.equals(fromBytes.trim()));
        try {
            field.setBytes(TEST_STRING_LONG.getBytes());
            assertTrue("Should fail when the sting is longer than available space", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
    }
    public void testDate() throws java.sql.SQLException {
        field.setDate(TEST_DATE);
        // because of the date-string-date conversion we loose the
        // time part of java.util.Date, and strictly speaking
        // TEST_DATE and field.getDate() objects are not equal.
        assertTrue("Date values test failure",
            field.getDate().toString().equals(TEST_DATE.toString()));
    }
    public void testTime() throws java.sql.SQLException {
        field.setTime(TEST_TIME);
        // because of the time-string-time conversion we loose the
        // date part of java.util.Date, and strictly speaking
        // TEST_TIME and field.getTime() objects are not equal.
        assertTrue("Time values test failure",
            field.getTime().toString().equals(TEST_TIME.toString()));
    }
}