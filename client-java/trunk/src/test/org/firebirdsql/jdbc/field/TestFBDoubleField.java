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
import java.sql.SQLException;
import java.sql.Types;

import org.firebirdsql.gds.ISCConstants;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link FBDoubleField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDoubleField extends BaseJUnit4TestFBField<FBDoubleField, Double> {
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        xsqlvar.sqltype = ISCConstants.SQL_DOUBLE;
        field = new FBDoubleField(xsqlvar, fieldData, Types.DOUBLE);
    }
    
    @Test
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(1.34578)));
        }});
        
        BigDecimal expectedValue = new BigDecimal(1.34578);
        assertEquals("Unexpected value for getBigDecimal", expectedValue, field.getBigDecimal());
    }
    
    @Test
    public void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result", field.getBigDecimal());
    }
    
    @Test
    @Override
    public void setBigDecimalNonNull() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(10));
        }});
        
        field.setBigDecimal(BigDecimal.TEN);
    }
    
    /**
     * Test at maximum allowed value (Double.MAX_VALUE)
     */
    @Test
    public void setBigDecimal_MAX() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.MAX_VALUE));
        }});
        
        field.setBigDecimal(new BigDecimal(Double.MAX_VALUE));
    }
    
    /**
     * Test at maximum allowed value (Double.MAX_VALUE) plus fraction
     */
    @Test(expected = TypeConversionException.class)
    public void setBigDecimal_MAX_plus_fraction() throws SQLException {
        BigDecimal testValue = new BigDecimal(Double.MAX_VALUE);
        testValue = testValue.add(testValue.ulp());
        
        field.setBigDecimal(testValue);
    }
    
    /**
     * Test at minimum allowed value (-1 * Double.MAX_VALUE)
     */
    @Test
    public void setBigDecimal_MIN() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(-1 * Double.MAX_VALUE));
        }});
        
        field.setBigDecimal(new BigDecimal(-1* Double.MAX_VALUE));
    }
    
    /**
     * Test at minimum allowed value (-1 * Double.MAX_VALUE) minus fraction
     */
    @Test(expected = TypeConversionException.class)
    public void setBigDecimal_MIN_minus_fraction() throws SQLException {
        BigDecimal testValue = new BigDecimal(-1 * Double.MAX_VALUE);
        testValue = testValue.subtract(testValue.ulp());
        
        field.setBigDecimal(testValue);
    }
    
    @Test
    @Ignore
    @Override
    public void getBigDecimalIntNonNull() throws SQLException {
        // TODO: Implement test for getBigDecimal(int)
    }
    
    @Test
    public void setBigDecimalNull() throws SQLException {
        setNullExpectations();
        
        field.setBigDecimal(null);
    }
    
    /**
     * Test for value for true.
     */
    @Test
    @Override
    public void getBooleanNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(1)));
        }});
        
        assertTrue("Expected true for getBoolean with field value 1", field.getBoolean());
    }
    
    /**
     * Test for value for false with value zero
     */
    @Test
    public void getBooleanNonNull_false_zero() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(0)));
        }});
        
        assertFalse("Expected false for getBoolean with field value 0", field.getBoolean());
    }
    
    /**
     * Test for value for false with value other than 1 or zero
     * TODO: Check if this is according to spec
     */
    @Test
    public void getBooleanNonNull_false_other() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(513)));
        }});
        
        assertFalse("Expected false for getBoolean with field value other than 1 or 0", field.getBoolean());
    }
    
    @Test
    public void getBooleanNull() throws SQLException {
        toReturnNullExpectations();
        
        assertFalse("Expected false for getBoolean with field value null", field.getBoolean());
    }
    
    /**
     * Tests with setting true
     */
    @Test
    @Override
    public void setBoolean() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(1));
        }});
        
        field.setBoolean(true);
    }
    
    /**
     * Tests with setting false
     */
    @Test
    public void setBoolean_false() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(0));
        }});
        
        field.setBoolean(false);
    }
    
    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(114.123)));
        }});
        
        assertEquals("Unexpected value for getByte", 114, field.getByte());
    }
    
    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE).
     */
    @Test
    public void getByte_MAX() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Byte.MAX_VALUE)));
        }});
        
        assertEquals("Unexpected value for getByte", 127, field.getByte());
    }
    
    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE) plus a fraction.
     */
    @Test(expected = TypeConversionException.class)
    public void getByte_MAX_plus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Byte.MAX_VALUE + Math.ulp(Byte.MAX_VALUE))));
        }});
        
        field.getByte();
    }
    
    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE).
     */
    @Test
    public void getByte_MIN() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Byte.MIN_VALUE)));
        }});
        
        assertEquals("Unexpected value for getByte", -128, field.getByte());
    }
    
    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE) minus a fraction.
     */
    @Test(expected = TypeConversionException.class)
    public void getByte_MIN_minus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Byte.MIN_VALUE - Math.ulp(Byte.MIN_VALUE))));
        }});
        
        field.getByte();
    }
    
    @Test
    public void getByteNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexpected value for getByte for null", 0, field.getByte());
    }
    
    @Test
    @Override
    public void setByte() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(-54));
        }});
        
        field.setByte((byte)-54);
    }
    
    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(1.34578)));
        }});
        
        assertEquals("Unexpected value for getDouble", 1.34578, field.getDouble(), 0);
    }
    
    @Test
    public void getDoubleNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexpected value for getDouble for null", 0, field.getDouble(), 0);
    }
    
    @Test
    @Override
    public void setDouble() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(9157824.1245785));
        }});
        
        field.setDouble(9157824.1245785);
    }
    
    @Test
    public void setDouble_NaN() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.NaN));
        }});
        
        field.setDouble(Double.NaN);
    }
    
    @Test
    public void setDouble_posInf() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.POSITIVE_INFINITY));
        }});
        
        field.setDouble(Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void setDouble_negInf() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.NEGATIVE_INFINITY));
        }});
        
        field.setDouble(Double.NEGATIVE_INFINITY);
    }
    
    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(1247.25898)));
        }});
        
        assertEquals("Unexpected value for getFloat", 1247.25898f, field.getFloat(), 0);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getFloat_OutOfRange_high() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Float.MAX_VALUE + (double)Math.ulp(Float.MAX_VALUE))));
        }});
        
        field.getFloat();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getFloat_OutOfRange_low() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(-1 * Float.MAX_VALUE - (double)Math.ulp(Float.MAX_VALUE))));
        }});
        
        field.getFloat();
    }
    
    @Test
    public void getFloatNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexpected value for getFloat for null", 0, field.getFloat(), 0);
    }
    
    @Test
    @Override
    public void setFloat() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Float.MAX_VALUE));
        }});
        
        field.setFloat(Float.MAX_VALUE);
    }
    
    @Test
    public void setFloat_NaN() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.NaN));
        }});
        
        field.setDouble(Float.NaN);
    }
    
    @Test
    public void setFloat_posInf() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.POSITIVE_INFINITY));
        }});
        
        field.setFloat(Float.POSITIVE_INFINITY);
    }
    
    @Test
    public void setFloat_negInf() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Double.NEGATIVE_INFINITY));
        }});
        
        field.setFloat(Float.NEGATIVE_INFINITY);
    }
    
    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(124578.124578)));
        }});
        
        assertEquals("Unexpected value for getInt", 124578, field.getInt());
    }
    
    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE).
     */
    @Test
    public void getInt_MAX() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Integer.MAX_VALUE)));
        }});
        
        assertEquals("Unexpected value for getInt", Integer.MAX_VALUE, field.getInt());
    }
    
    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE) plus a fraction
     */
    @Test(expected = TypeConversionException.class)
    public void getInt_MAX_plus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Integer.MAX_VALUE + Math.ulp(Integer.MAX_VALUE))));
        }});
        
        field.getInt();
    }
    
    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE).
     */
    @Test
    public void getInt_MIN() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Integer.MIN_VALUE)));
        }});
        
        assertEquals("Unexpected value for getInt", Integer.MIN_VALUE, field.getInt());
    }
    
    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE) minus a fraction
     */
    @Test(expected = TypeConversionException.class)
    public void getInt_MIN_minus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Integer.MIN_VALUE - Math.ulp(Integer.MIN_VALUE))));
        }});
        
        field.getInt();
    }
    
    @Test
    public void getIntNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexepected value for getInt for null", 0, field.getInt());
    }
    
    @Test
    @Override
    public void setInteger() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Integer.MAX_VALUE));
        }});
        
        field.setInteger(Integer.MAX_VALUE);
    }
    
    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(2.132457 * Integer.MAX_VALUE)));
        }});
        
        long expectedValue = (long)(2.132457 * Integer.MAX_VALUE);
        assertEquals("Unexpected value for getLong", expectedValue, field.getLong());
    }
    
    /**
     * Tests value at maximum allowed (Long.MAX_VALUE).
     */
    @Test
    public void getLong_MAX() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Long.MAX_VALUE)));
        }});
        
        assertEquals("Unexpected value for getLong", Long.MAX_VALUE, field.getLong());
    }
    
    /**
     * Tests value at maximum allowed (Long.MAX_VALUE) plus a fraction.
     */
    @Test(expected = TypeConversionException.class)
    public void getLong_MAX_plus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Long.MAX_VALUE + Math.ulp(Long.MAX_VALUE))));
        }});
        
        field.getLong();
    }
    
    /**
     * Tests value at minimum allowed (Long.MIN_VALUE).
     */
    @Test
    public void getLong_MIN() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Long.MIN_VALUE)));
        }});
        
        assertEquals("Unexpected value for getLong", Long.MIN_VALUE, field.getLong());
    }
    
    /**
     * Tests value at minimum allowed (Long.MIN_VALUE) minus a fraction.
     */
    @Test(expected = TypeConversionException.class)
    public void getLong_MIN_minus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Long.MIN_VALUE - Math.ulp(Long.MIN_VALUE))));
        }});
        
        field.getLong();
    }
    
    @Test
    public void getLongNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexpected value for getLong for null", 0, field.getLong());
    }
    
    @Test
    @Override
    public void setLong() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Long.MAX_VALUE));
        }});
        
        field.setLong(Long.MAX_VALUE);
    }
    
    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(-1 * Double.MAX_VALUE)));
        }});
        
        Double expectedValue = Double.valueOf(-1 * Double.MAX_VALUE);
        assertEquals("Unexpected value for getObject", expectedValue, field.getObject());
    }
    
    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(-1 * Double.MAX_VALUE));
        }});
        Double setValue = Double.valueOf(-1 * Double.MAX_VALUE);
        
        field.setObject(setValue);
    }
    
    // TODO Add additional object tests (eg with Integer, Long, Float, BigDecimal etc objects).
    
    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(-14578.124)));
        }});
        
        assertEquals("Unexpected value for getShort", -14578, field.getShort());
    }
    
    /**
     * Tests value at maximum allowed (Short.MAX_VALUE).
     * @throws SQLException
     */
    @Test
    public void getShort_MAX() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Short.MAX_VALUE)));
        }});
        
        assertEquals("Unexpected value for getShort", Short.MAX_VALUE, field.getShort());
    }
    
    /**
     * Tests value at maximum allowed (Short.MAX_VALUE) plus a fraction
     * @throws SQLException
     */
    @Test(expected = TypeConversionException.class)
    public void getShort_MAX_plus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE))));
        }});
        
        field.getShort();
    }
    
    /**
     * Tests value at minimum allowed (Short.MIN_VALUE).
     * @throws SQLException
     */
    @Test
    public void getShort_MIN() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Short.MIN_VALUE)));
        }});
        
        assertEquals("Unexpected value for getShort", Short.MIN_VALUE, field.getShort());
    }
    
    /**
     * Tests value at minimum allowed (Short.MIN_VALUE) minus a fraction
     * @throws SQLException
     */
    @Test(expected = TypeConversionException.class)
    public void getShort_MIN_minus_fraction() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE))));
        }});
        
        field.getShort();
    }
    
    @Test
    public void getShortNull() throws SQLException {
        toReturnNullExpectations();
        
        assertEquals("Unexpected value for getShort for null", 0, field.getShort());
    }
    
    @Test
    @Override
    public void setShort() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(Short.MIN_VALUE));
        }});
        
        field.setShort(Short.MIN_VALUE);
    }
    
    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(xsqlvar.encodeDouble(-14578.124)));
        }});
        
        String expectedValue = String.valueOf(-14578.124);
        assertEquals("Unexpected value for getString", expectedValue, field.getString());
    }
    
    @Test
    public void getStringNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull("Unexpected value for getString for null", field.getString());
    }
    
    /**
     * Tests with a valid double in string
     */
    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(xsqlvar.encodeDouble(5145789.12457));
        }});
        
        field.setString("5145789.12457");
    }
    
    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();
        
        field.setString(null);
    }
    
    @Test(expected = TypeConversionException.class)
    public void setString_noDouble() throws SQLException {
        field.setString("no double");
    }
    
    @Override
    protected Double getNonNullObject() {
        return Double.valueOf(1);
    }
}
