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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link FBBigDecimalField}
 */
@RunWith(JMock.class)
public class TestFBBigDecimalField {
    Mockery context = new JUnit4Mockery();
    
    private FieldDataProvider fieldData;
    
    @Before
    public void setUp() {
        fieldData = context.mock(FieldDataProvider.class);
    }
    
    // TODO Add set<PrimitiveNumber> tests for out of range condition; in current implementation duplicates setBigDecimal out of range tests
    // TODO Add tests for unsupported conversions
    // TODO Add set/getObject test

    @Test
    public void getBigDecimalNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertNull("Expected null result", field.getBigDecimal());
    }
    
    @Test 
    public void getBigDecimalShort() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short) 231)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigDecimal expectedValue = new BigDecimal("23.1");
        assertEquals(expectedValue, field.getBigDecimal());
    }
    
    @Test 
    public void getBigDecimalInteger() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -4;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(34)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigDecimal expectedValue = new BigDecimal("0.0034");
        assertEquals(expectedValue, field.getBigDecimal());
    }
    
    @Test 
    public void getBigDecimalLong() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -8;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(51300000000L)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals(expectedValue, field.getBigDecimal());
    }
    
    @Test
    public void setBigDecimalShort() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)4320));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        // TODO Might need to add separate test for the rescaling applied here
        field.setBigDecimal(new BigDecimal("43.2"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalShortTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Short.MAX_VALUE + 1, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalShortTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Short.MIN_VALUE - 1, 2));
    }
    
    @Test
    public void setBigDecimalShortNull() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void setBigDecimalInteger() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -3;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(1234567));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(new BigDecimal("1234.567"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalIntegerTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Integer.MAX_VALUE + 1L, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalIntegerTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Integer.MIN_VALUE - 1L, 2));
    }
    
    @Test
    public void setBigDecimalIntegerNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void setBigDecimalLong() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -5;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(1234567890123L));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(new BigDecimal("12345678.90123"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalLongTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        field.setBigDecimal(new BigDecimal(value, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalLongTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        field.setBigDecimal(new BigDecimal(value, 2));
    }
    
    @Test
    public void setBigDecimalLongNull() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void getBooleanTrue() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short)10)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertTrue("Expected true from getBoolean", field.getBoolean());
    }
    
    @Test
    public void getBooleanFalse() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            // NOTE Any value other than 10 would do
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short)0)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertFalse("Expected false from getBoolean", field.getBoolean());
    }
    
    @Test
    public void setBooleanTrue() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)10));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBoolean(true);
    }
    
    @Test
    public void setBooleanFalse() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)0));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBoolean(false);
    }
    
    @Test
    public void getByteNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getByte() to return 0 for NULL value", 0, field.getByte());
    }
    
    @Test
    public void getByte() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(Byte.MIN_VALUE * 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value for getByte()", Byte.MIN_VALUE, field.getByte());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getByteTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Byte.MAX_VALUE + 1)* 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getByte();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getByteTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Byte.MIN_VALUE - 1)* 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getByte();
    }
    
    @Test
    public void setByte() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -7;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(-340000000L));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setByte((byte) -34);
    }
    
    @Test
    public void getDoubleNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getDouble() to return 0.0 for NULL value", 0.0, field.getDouble(), 0.0);
    }
    
    @Test
    public void getDouble() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MIN_VALUE)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value for getDouble()", Long.MIN_VALUE / 100.0, field.getDouble(), 0.0);
    }
    
    @Test
    public void setDouble() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)4691));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setDouble(469.1234567);
    }
    
    @Test
    public void getFloatNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getFloat() to return 0.0 for NULL value", 0.0, field.getFloat(), 0.0);
    }
    
    @Test
    public void getFloat() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MAX_VALUE)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value for getFloat()", Long.MAX_VALUE / 100.0f, field.getFloat(), 0.0);
    }
    
    @Test
    public void setFloat() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -5;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(46912344L));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);

        field.setFloat(469.1234567f);
    }
    
    @Test
    public void getIntNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getInt() to return 0 for NULL value", 0, field.getInt());
    }
    
    @Test
    public void getInt() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -6;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(987654321098765L)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value from getInt()", 987654321, field.getInt());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getIntTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong((Integer.MAX_VALUE + 1L) * 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getInt();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getIntTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong((Integer.MIN_VALUE - 1L) * 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getInt();
    }
    
    @Test
    public void setInteger() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(1234560));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setInteger(123456);
    }
    
    @Test
    public void getLongNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getLong() to return 0 for NULL value", 0, field.getLong());
    }
    
    @Test
    public void getLong() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MAX_VALUE)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value from getLong()", Long.MAX_VALUE / 100, field.getLong());
    }
    
    @Test
    public void setLong() throws SQLException {
        final XSQLVAR xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)3500));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setLong(35);
    }
    
    @Test
    public void setNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setNull();
    }
    
    @Test
    public void getShortNull() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Expected getShort() to return 0 for NULL value", 0, field.getShort());
    }
    
    @Test
    public void getShort() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -4;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(123456789)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value from getShort()", 12345, field.getShort());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getShortTooHigh() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Short.MAX_VALUE + 1) * 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getShort();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getShortTooLow() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Short.MIN_VALUE - 1) * 100)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.getShort();
    }
    
    @Test
    public void setShort() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -3;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(Short.MIN_VALUE * 1000L));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setShort(Short.MIN_VALUE);
    }
    
    @Test
    public void getStringNull() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        toReturnNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertNull(field.getString());
    }
    
    @Test
    public void getString() throws SQLException {
        final XSQLVAR xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(456789123)));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value from getString()", "4567891.23", field.getString());
    }
    
    @Test
    public void setString() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(789123));
        }});
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setString("78912.3456");
    }
    
    @Test
    public void setStringNull() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setString(null);
    }
    
    @Test(expected = TypeConversionException.class)
    public void setStringNonNumber() throws SQLException {
        final XSQLVAR xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        FBBigDecimalField field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setString("NotANumber");
    }
    
    @Test(expected = SQLException.class)
    public void constructWithUnsupportedSqlType() throws SQLException {
        final XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_VARYING;
        
        new FBBigDecimalField(xsqlvar, fieldData, Types.VARCHAR);
    }
    
    /**
     * Expectations to return null from fieldData.
     */
    private void toReturnNullExpectations() {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(null));
        }});
    }
    
    /**
     * Expectations for setting field to null
     */
    private void setNullExpectations() {
        context.checking(new Expectations() {{
            one(fieldData).setFieldData(null);
        }});
    }
    
    private static XSQLVAR createShortXSQLVAR() {
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        return xsqlvar;
    }
    
    private static XSQLVAR createIntegerXSQLVAR() {
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_LONG;
        return xsqlvar;
    }
    
    private static XSQLVAR createLongXSQLVAR() {
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_INT64;
        return xsqlvar;
    }
}
