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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

public abstract class BaseTestFBField extends TestCase {
    protected FBField field;
    static byte TEST_BYTE = Byte.MAX_VALUE - 1;
    static short TEST_SHORT = Short.MAX_VALUE - 1;
    static int TEST_INT = Integer.MAX_VALUE - 1;
    static long TEST_LONG = Long.MAX_VALUE - 1;
    static float TEST_FLOAT = Float.MAX_VALUE - 1;
    static double TEST_DOUBLE = Double.MAX_VALUE - 1;
    static int TEST_BOOLEAN_INT = 1;
    static byte[] TEST_BYTES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    static long TEST_TIME_LONG = System.currentTimeMillis();

    static java.sql.Date TEST_DATE =
        new java.sql.Date(System.currentTimeMillis());

    static java.sql.Time TEST_TIME =
        new java.sql.Time(System.currentTimeMillis());

    static java.sql.Timestamp TEST_TIMESTAMP =
        new java.sql.Timestamp(System.currentTimeMillis());


    public BaseTestFBField(String testName) {
        super(testName);
    }

    public void testByte() throws SQLException {
        field.setByte(TEST_BYTE);
        assertTrue("Byte values test failure", field.getByte() == TEST_BYTE);
    }

    public void testShort() throws SQLException {
        field.setShort(TEST_SHORT);
        assertTrue("Short values test failure", field.getShort() == TEST_SHORT);
    }

    public void testInteger() throws SQLException {
        field.setInteger(TEST_INT);
        assertTrue("Integer values test failure", field.getInt() == TEST_INT);
    }

    public void testLong() throws SQLException {
        field.setLong(TEST_LONG);
        assertTrue("Long values test failure", field.getLong() == TEST_LONG);
    }

    public void testFloat() throws SQLException {
        field.setFloat(TEST_FLOAT);
        assertTrue("Float values test failure", field.getFloat() == TEST_FLOAT);
    }

    public void testDouble() throws SQLException {
        field.setDouble(TEST_DOUBLE);
        assertTrue("Double values test failure", field.getDouble() == TEST_DOUBLE);
    }

    public abstract void testBigDecimal() throws SQLException;

    public void testBoolean() throws SQLException {
        field.setBoolean(true);
        assertTrue("Boolean values test failure", field.getBoolean());
    }

    public abstract void testObject() throws SQLException;

    public abstract void testString() throws SQLException;

    public void testAsciiStream() throws SQLException {
        field.setAsciiStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        assertTrue("ASCII stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getAsciiStream())));
    }
    public void testUnicodeStream() throws SQLException {
        field.setUnicodeStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        assertTrue("Unicode stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getUnicodeStream())));
    }
    public void testBinaryStream() throws SQLException {
        field.setBinaryStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        assertTrue("Binary stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getBinaryStream())));
    }
    public void testBytes() throws SQLException {
        field.setBytes(TEST_BYTES);
        assertTrue("Byte array values test failure", Arrays.equals(TEST_BYTES, field.getBytes()));
    }
    public void testDate() throws SQLException {
        field.setDate(TEST_DATE);
        assertTrue("Date values test failure", field.getDate().toString().equals(TEST_DATE.toString()));
    }
    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        assertTrue("Time values test failure", field.getTime().toString().equals(TEST_TIME.toString()));
    }
    public void testTimestamp() throws SQLException {
        field.setTimestamp(TEST_TIMESTAMP);
        assertTrue("Timestamp values test failure", field.getTimestamp().equals(TEST_TIMESTAMP));
    }

    protected byte[] readInputStream(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int counter = 0;
            while ((counter = in.read(buff)) != -1)
                out.write(buff, 0, counter);
            return out.toByteArray();
        } catch(IOException ioex) {
            assertTrue("IOException happened during processing.", false);
            // lets make compiler happy! :)
            return null;
        }
    }

}
