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
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.ngds.XSQLVARLittleEndianImpl;

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
    static byte[] TEST_BYTES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

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
        assertTrue("Byte values test failure", field.getByte() == TEST_BYTE);
    }

    public void testShort() throws SQLException {
        field.setShort(TEST_SHORT);
        field.copyOI();		  
        assertTrue("Short values test failure", field.getShort() == TEST_SHORT);
    }

    public void testInteger() throws SQLException {
        field.setInteger(TEST_INT);
        field.copyOI();		  
        assertTrue("Integer values test failure", field.getInt() == TEST_INT);
    }

    public void testLong() throws SQLException {
        field.setLong(TEST_LONG);
        field.copyOI();		  
        assertTrue("Long values test failure", field.getLong() == TEST_LONG);
    }

    public void testFloat() throws SQLException {
        field.setFloat(TEST_FLOAT);
        field.copyOI();		  
        assertTrue("Float values test failure", field.getFloat() == TEST_FLOAT);
    }

    public void testDouble() throws SQLException {
        field.setDouble(TEST_DOUBLE);
        field.copyOI();		  
        assertTrue("Double values test failure", field.getDouble() == TEST_DOUBLE);
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
        assertTrue("Date values test failure", field.getDate().toString().equals(TEST_DATE.toString()));
    }
    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        field.copyOI();		  
        assertTrue("Time values test failure", field.getTime().toString().equals(TEST_TIME.toString()));
    }
    public void testTimestamp() throws SQLException {
        field.setTimestamp(TEST_TIMESTAMP);
        field.copyOI();		  
        assertTrue("Timestamp values test failure: expected: " + TEST_TIMESTAMP + ", actual: " + field.getTimestamp(), field.getTimestamp().equals(TEST_TIMESTAMP));
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

    protected XSQLVAR createXSQLVAR()
        {
        if(getGdsType() == GDSType.PURE_JAVA)
            return new org.firebirdsql.gds.XSQLVAR();
        else
        if(getGdsType() == GDSType.NATIVE ||
           getGdsType() == GDSType.NATIVE_EMBEDDED)
            return new XSQLVARLittleEndianImpl();
        else
            throw new RuntimeException("Unrecognised GDSType");
        }


    private GDSType getGdsType()
        {
        return GDSType.PURE_JAVA;
        }



}
