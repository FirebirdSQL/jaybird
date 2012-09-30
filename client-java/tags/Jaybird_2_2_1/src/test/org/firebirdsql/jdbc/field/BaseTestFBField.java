/*
 * $Id$
 * 
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.jni.XSQLVARLittleEndianImpl;
import org.firebirdsql.jdbc.FBResultSet;

/**
 * Describe class <code>BaseTestFBField</code> here.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public abstract class BaseTestFBField extends TestCase {
    protected FBField field;
    static byte TEST_BYTE = Byte.MAX_VALUE;
    static short TEST_SHORT = Short.MAX_VALUE;
    static int TEST_INT = Integer.MAX_VALUE;
    static long TEST_LONG = Long.MAX_VALUE;
    static float TEST_FLOAT = Float.MAX_VALUE;
    static double TEST_DOUBLE = Double.MAX_VALUE;
    static int TEST_BOOLEAN_INT = 1;
    static byte[] TEST_BYTES = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    static long TEST_TIME_LONG = System.currentTimeMillis();

    static Date TEST_DATE =
    		new Date(System.currentTimeMillis());

    static Time TEST_TIME =
    		new Time(System.currentTimeMillis());

    static Timestamp TEST_TIMESTAMP =
    		new Timestamp(System.currentTimeMillis());

    public BaseTestFBField(String testName) {
        super(testName);
    }

    protected FieldDataProvider createDataProvider(XSQLVAR[] xsqlvars) throws SQLException {
        byte[][] row = new byte[1][];
        ArrayList rows = new ArrayList();
        rows.add(row);
        final FBResultSet rs = new FBFieldResultSet(xsqlvars, rows);
        rs.next();
        // anonymous implementation of the FieldDataProvider interface
        FieldDataProvider dataProvider = new FieldDataProvider() {

            public byte[] getFieldData() {
                return rs.row[0];
            }

            public void setFieldData(byte[] data) {
                rs.row[0] = data;
            }
        };

        return dataProvider;
    }

    public void testByte() throws SQLException {
        field.setByte(TEST_BYTE);
        field.copyOI();
        assertEquals("Byte values test failure", TEST_BYTE, field.getByte());
    }

    public void testShort() throws SQLException {
        field.setShort(TEST_SHORT);
        field.copyOI();
        assertEquals("Short values test failure", TEST_SHORT, field.getShort());
    }

    public void testInteger() throws SQLException {
        field.setInteger(TEST_INT);
        field.copyOI();
        assertEquals("Integer values test failure", TEST_INT, field.getInt());
    }

    public void testLong() throws SQLException {
        field.setLong(TEST_LONG);
        field.copyOI();
        assertEquals("Long values test failure", TEST_LONG, field.getLong());
    }

    public void testFloat() throws SQLException {
        field.setFloat(TEST_FLOAT);
        field.copyOI();
        assertEquals("Float values test failure", TEST_FLOAT, field.getFloat());
    }

    public void testDouble() throws SQLException {
        field.setDouble(TEST_DOUBLE);
        field.copyOI();
        assertEquals("Double values test failure", TEST_DOUBLE, field.getDouble(), 0.0);
    }

    public abstract void testBigDecimal() throws SQLException;

    public void testBoolean() throws SQLException {
        field.setBoolean(true);
        field.copyOI();
        assertTrue("Boolean values test failure", field.getBoolean());
    }

    public abstract void testObject() throws SQLException;

    public abstract void testString() throws SQLException;

    public void testAsciiStream() throws SQLException {
        field.setAsciiStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        field.copyOI();
        assertTrue("ASCII stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getAsciiStream())));
    }

    public void testUnicodeStream() throws SQLException {
        field.setUnicodeStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        field.copyOI();
        assertTrue("Unicode stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getUnicodeStream())));
    }

    public void testBinaryStream() throws SQLException {
        field.setBinaryStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        field.copyOI();
        assertTrue("Binary stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getBinaryStream())));
    }

    public void testBytes() throws SQLException {
        field.setBytes(TEST_BYTES);
        field.copyOI();
        assertTrue("Byte array values test failure", Arrays.equals(TEST_BYTES, field.getBytes()));
    }

    public void testDate() throws SQLException {
        field.setDate(TEST_DATE);
        field.copyOI();
        assertEquals("Date values test failure", TEST_DATE.toString(), field.getDate().toString());
    }

    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        field.copyOI();
        assertEquals("Time values test failure", TEST_TIME.toString(), field.getTime().toString());
    }

    public void testTimestamp() throws SQLException {
        field.setTimestamp(TEST_TIMESTAMP);
        field.copyOI();
        assertEquals("Timestamp values test failure", TEST_TIMESTAMP, field.getTimestamp());
    }

    protected byte[] readInputStream(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int counter = 0;
            while ((counter = in.read(buff)) != -1)
                out.write(buff, 0, counter);
            return out.toByteArray();
        } catch (IOException ioex) {
            assertTrue("IOException happened during processing.", false);
            // lets make compiler happy! :)
            return null;
        }
    }

    protected XSQLVAR createXSQLVAR() {
        if (getGdsType() == GDSType.getType("PURE_JAVA"))
            return new org.firebirdsql.gds.XSQLVAR();
        else if (getGdsType() == GDSType.getType("NATIVE")
                || getGdsType() == GDSType.getType("EMBEDDED"))
            return new XSQLVARLittleEndianImpl();
        else
            throw new RuntimeException("Unrecognised GDSType");
    }

    protected GDSType getGdsType() {
        return GDSType.getType("PURE_JAVA");
    }
}
