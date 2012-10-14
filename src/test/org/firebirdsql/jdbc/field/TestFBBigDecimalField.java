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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link FBBigDecimalField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBBigDecimalField extends BaseJUnit4TestFBField<FBBigDecimalField, BigDecimal> {
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // NOTE This definition is necessary for the tests in the superclass, this is overridden by most tests in the class itself
        xsqlvar.sqltype = ISCConstants.SQL_LONG;
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
    }
    
    // TODO Add set<PrimitiveNumber> tests for out of range condition; in current implementation duplicates setBigDecimal out of range tests
    // TODO Add set/getObject test

    @Test
    public void getBigDecimalNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertNull("Expected null result", field.getBigDecimal());
    }
    
    @Test
    @Ignore
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        // Ignore in favor of more specific tests
    }
    
    @Test
    @Ignore
    @Override
    public void getBigDecimalIntNonNull() throws SQLException {
        // TODO: Implement test for getBigDecimal(int)
    }
    
    @Test 
    public void getBigDecimalShort() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short) 231)));
        }});
        
        BigDecimal expectedValue = new BigDecimal("23.1");
        assertEquals("Unexpected value for short BigDecimal", expectedValue, field.getBigDecimal());
    }
    
    @Test 
    public void getBigDecimalInteger() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -4;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(34)));
        }});
        
        BigDecimal expectedValue = new BigDecimal("0.0034");
        assertEquals("Unexpected value for integer BigDecimal", expectedValue, field.getBigDecimal());
    }
    
    @Test 
    public void getBigDecimalLong() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -8;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(51300000000L)));
        }});
        
        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals("Unexpected value for long BigDecimal", expectedValue, field.getBigDecimal());
    }
    
    @Test
    @Ignore
    @Override
    public void setBigDecimalNonNull() throws SQLException {
        // Ignore in favor of more specific tests
    }
    
    @Test
    public void setBigDecimalShort() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)4320));
        }});
        
        // TODO Might need to add separate test for the rescaling applied here
        field.setBigDecimal(new BigDecimal("43.2"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalShortTooHigh() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Short.MAX_VALUE + 1, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalShortTooLow() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Short.MIN_VALUE - 1, 2));
    }
    
    @Test
    public void setBigDecimalShortNull() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        setNullExpectations();
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void setBigDecimalInteger() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -3;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(1234567));
        }});
        
        field.setBigDecimal(new BigDecimal("1234.567"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalIntegerTooHigh() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Integer.MAX_VALUE + 1L, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalIntegerTooLow() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(BigDecimal.valueOf(Integer.MIN_VALUE - 1L, 2));
    }
    
    @Test
    public void setBigDecimalIntegerNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        setNullExpectations();
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void setBigDecimalLong() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -5;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(1234567890123L));
        }});
        
        field.setBigDecimal(new BigDecimal("12345678.90123"));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalLongTooHigh() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        field.setBigDecimal(new BigDecimal(value, 2));
    }
    
    @Test(expected = TypeConversionException.class) 
    public void setBigDecimalLongTooLow() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        field.setBigDecimal(new BigDecimal(value, 2));
    }
    
    @Test
    public void setBigDecimalLongNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        setNullExpectations();
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        field.setBigDecimal(null);
    }
    
    @Test
    @Ignore
    @Override
    public void getBooleanNonNull() throws SQLException {
        // Ignore in favor of more specific tests
    }
    
    @Test
    public void getBooleanTrue() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short)10)));
        }});
        
        assertTrue("Expected true from getBoolean", field.getBoolean());
    }
    
    @Test
    public void getBooleanFalse() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            // NOTE Any value other than 10 would do
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeShort((short)0)));
        }});
        
        assertFalse("Expected false from getBoolean", field.getBoolean());
    }
    
    @Test
    @Ignore
    @Override
    public void setBoolean() throws SQLException {
        // Ignore in favor of more specific tests
    }
    
    @Test
    public void setBooleanTrue() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)10));
        }});
        
        field.setBoolean(true);
    }
    
    @Test
    public void setBooleanFalse() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)0));
        }});
        
        field.setBoolean(false);
    }
    
    @Test
    public void getByteNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getByte() to return 0 for NULL value", 0, field.getByte());
    }
    
    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(Byte.MIN_VALUE * 100)));
        }});
        
        assertEquals("Unexpected value for getByte()", Byte.MIN_VALUE, field.getByte());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getByteTooHigh() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Byte.MAX_VALUE + 1)* 100)));
        }});
        
        field.getByte();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getByteTooLow() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Byte.MIN_VALUE - 1)* 100)));
        }});
        
        field.getByte();
    }
    
    @Test
    public void setByte() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -7;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(-340000000L));
        }});
        
        field.setByte((byte) -34);
    }
    
    @Test
    public void getDoubleNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getDouble() to return 0.0 for NULL value", 0.0, field.getDouble(), 0.0);
    }
    
    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MIN_VALUE)));
        }});
        
        assertEquals("Unexpected value for getDouble()", Long.MIN_VALUE / 100.0, field.getDouble(), 0.0);
    }
    
    @Test
    public void setDouble() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)4691));
        }});
        
        field.setDouble(469.1234567);
    }
    
    @Test
    public void getFloatNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -6;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getFloat() to return 0.0 for NULL value", 0.0, field.getFloat(), 0.0);
    }
    
    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MAX_VALUE)));
        }});
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        
        assertEquals("Unexpected value for getFloat()", Long.MAX_VALUE / 100.0f, field.getFloat(), 0.0);
    }
    
    @Test
    @Override
    public void setFloat() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -5;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(46912344L));
        }});

        field.setFloat(469.1234567f);
    }
    
    @Test
    public void getIntNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getInt() to return 0 for NULL value", 0, field.getInt());
    }
    
    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -6;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(987654321098765L)));
        }});
        
        assertEquals("Unexpected value from getInt()", 987654321, field.getInt());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getIntTooHigh() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong((Integer.MAX_VALUE + 1L) * 100)));
        }});
        
        field.getInt();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getIntTooLow() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong((Integer.MIN_VALUE - 1L) * 100)));
        }});
        
        field.getInt();
    }
    
    @Test
    @Override
    public void setInteger() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(1234560));
        }});
        
        field.setInteger(123456);
    }
    
    @Test
    public void getLongNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getLong() to return 0 for NULL value", 0, field.getLong());
    }
    
    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(Long.MAX_VALUE)));
        }});
        
        assertEquals("Unexpected value from getLong()", Long.MAX_VALUE / 100, field.getLong());
    }
    
    @Test
    @Override
    public void setLong() throws SQLException {
        xsqlvar = createShortXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeShort((short)3500));
        }});
        
        field.setLong(35);
    }
    
    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -8;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(51300000000L)));
        }});
        
        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals("Unexpected value for long BigDecimal", expectedValue, field.getObject());
    }
    
    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -3;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(1234567));
        }});
        
        field.setObject(new BigDecimal("1234.567"));
    }
    
    // TODO Add tests for other object types
    
    @Test
    public void getShortNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals("Expected getShort() to return 0 for NULL value", 0, field.getShort());
    }
    
    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -4;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt(123456789)));
        }});
        
        assertEquals("Unexpected value from getShort()", 12345, field.getShort());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getShortTooHigh() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Short.MAX_VALUE + 1) * 100)));
        }});
        
        field.getShort();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getShortTooLow() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeInt((Short.MIN_VALUE - 1) * 100)));
        }});
        
        field.getShort();
    }
    
    @Test
    @Override
    public void setShort() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -3;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeLong(Short.MIN_VALUE * 1000L));
        }});
        
        field.setShort(Short.MIN_VALUE);
    }
    
    @Test
    public void getStringNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertNull(field.getString());
    }
    
    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        xsqlvar = createLongXSQLVAR();
        xsqlvar.sqlscale = -2;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeLong(456789123)));
        }});
        
        assertEquals("Unexpected value from getString()", "4567891.23", field.getString());
    }
    
    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeInt(789123));
        }});
        
        field.setString("78912.3456");
    }
    
    @Test
    public void setStringNull() throws SQLException {
        xsqlvar = createIntegerXSQLVAR();
        xsqlvar.sqlscale = -1;
        field = new FBBigDecimalField(xsqlvar, fieldData, Types.NUMERIC);
        setNullExpectations();
        
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
        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_VARYING;
        
        new FBBigDecimalField(xsqlvar, fieldData, Types.VARCHAR);
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

    @Override
    protected BigDecimal getNonNullObject() {
        return BigDecimal.ONE;
    }
}
