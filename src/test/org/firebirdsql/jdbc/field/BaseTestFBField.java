/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Describe class <code>BaseTestFBField</code> here.
 * <p>
 * TODO Merge with BaseJUnit4TestFBField.
 * </p>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public abstract class BaseTestFBField {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected static final DatatypeCoder datatypeCoder =
            new DefaultDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    protected FBField field;
    static byte TEST_BYTE = Byte.MAX_VALUE;
    static short TEST_SHORT = Short.MAX_VALUE;
    static int TEST_INT = Integer.MAX_VALUE;
    static long TEST_LONG = Long.MAX_VALUE;
    static float TEST_FLOAT = Float.MAX_VALUE;
    static double TEST_DOUBLE = Double.MAX_VALUE;
    static byte[] TEST_BYTES = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    static Date TEST_DATE = new Date(System.currentTimeMillis());

    static Time TEST_TIME = new Time(System.currentTimeMillis());

    static Timestamp TEST_TIMESTAMP = new Timestamp(System.currentTimeMillis());

    protected FieldDataProvider createDataProvider(RowDescriptor rowDescriptor) throws SQLException {
        assert rowDescriptor.getCount() == 1 : "Test should use a single column";
        RowValue row = rowDescriptor.createDefaultFieldValues();
        return row.getFieldValue(0);
    }

    @Test
    public void testByte() throws SQLException {
        field.setByte(TEST_BYTE);
        assertEquals("Byte values test failure", TEST_BYTE, field.getByte());
    }

    @Test
    public void testShort() throws SQLException {
        field.setShort(TEST_SHORT);
        assertEquals("Short values test failure", TEST_SHORT, field.getShort());
    }

    @Test
    public void testInteger() throws SQLException {
        field.setInteger(TEST_INT);
        assertEquals("Integer values test failure", TEST_INT, field.getInt());
    }

    @Test
    public void testLong() throws SQLException {
        field.setLong(TEST_LONG);
        assertEquals("Long values test failure", TEST_LONG, field.getLong());
    }

    @Test
    public void testFloat() throws SQLException {
        field.setFloat(TEST_FLOAT);
        assertEquals("Float values test failure", TEST_FLOAT, field.getFloat(), 0.0);
    }

    @Test
    public void testDouble() throws SQLException {
        field.setDouble(TEST_DOUBLE);
        assertEquals("Double values test failure", TEST_DOUBLE, field.getDouble(), 0.0);
    }

    @Test
    public abstract void testBigDecimal() throws SQLException;

    @Test
    public void testBoolean() throws SQLException {
        field.setBoolean(true);
        assertTrue("Boolean values test failure", field.getBoolean());
    }

    @Test
    public abstract void testObject() throws SQLException;

    @Test
    public abstract void testString() throws SQLException;

    @Test
    public void testAsciiStream() throws SQLException {
        field.setAsciiStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        assertTrue("ASCII stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getAsciiStream())));
    }

    @Test
    public void testBinaryStream() throws SQLException {
        field.setBinaryStream(new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
        assertTrue("Binary stream values test failure", Arrays.equals(TEST_BYTES, readInputStream(field.getBinaryStream())));
    }

    @Test
    public void testBytes() throws SQLException {
        field.setBytes(TEST_BYTES);
        assertTrue("Byte array values test failure", Arrays.equals(TEST_BYTES, field.getBytes()));
    }

    @Test
    public void testDate() throws SQLException {
        field.setDate(TEST_DATE);
        assertEquals("Date values test failure", TEST_DATE.toString(), field.getDate().toString());
    }

    @Test
    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        assertEquals("Time values test failure", TEST_TIME.toString(), field.getTime().toString());
    }

    @Test
    public void testTimestamp() throws SQLException {
        field.setTimestamp(TEST_TIMESTAMP);
        assertEquals("Timestamp values test failure", TEST_TIMESTAMP, field.getTimestamp());
    }

    protected byte[] readInputStream(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int counter;
            while ((counter = in.read(buff)) != -1)
                out.write(buff, 0, counter);
            return out.toByteArray();
        } catch (IOException ioex) {
            fail("IOException happened during processing.");
            // lets make compiler happy! :)
            return null;
        }
    }
}
