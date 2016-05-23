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

import org.firebirdsql.gds.ISCConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.Assert.*;

/**
 * Tests for {@link FBFloatField}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBFloatField extends BaseJUnit4TestFBField<FBFloatField, Float> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_FLOAT);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBFloatField(fieldDescriptor, fieldData, Types.FLOAT);
    }

    @Test
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        toReturnFloatExpectations(1.34578f);

        BigDecimal expectedValue = new BigDecimal(1.34578f);
        assertEquals("Unexpected value for getBigDecimal", expectedValue, field.getBigDecimal());
    }

    @Test
    @Override
    public void getObject_BigDecimal() throws SQLException {
        toReturnFloatExpectations(1.34578f);

        BigDecimal expectedValue = new BigDecimal(1.34578f);
        assertEquals("Unexpected value for getObject(BigDecimal.class)",
                expectedValue, field.getObject(BigDecimal.class));
    }

    @Test
    public void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result", field.getBigDecimal());
    }

    @Test
    public void getObject_BigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result for getObject(BigDecimal.class)", field.getObject(BigDecimal.class));
    }

    @Test
    @Override
    public void setBigDecimalNonNull() throws SQLException {
        setFloatExpectations(10);

        field.setBigDecimal(BigDecimal.TEN);
    }

    /**
     * Test at maximum allowed value (Float.MAX_VALUE)
     */
    @Test
    public void setBigDecimal_MAX() throws SQLException {
        setFloatExpectations(Float.MAX_VALUE);

        field.setBigDecimal(new BigDecimal(Float.MAX_VALUE));
    }

    /**
     * Test at maximum allowed value (Float.MAX_VALUE) plus fraction
     */
    @Test
    public void setBigDecimal_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        BigDecimal testValue = new BigDecimal(Float.MAX_VALUE);
        testValue = testValue.add(testValue.ulp());

        field.setBigDecimal(testValue);
    }

    /**
     * Test at minimum allowed value (-1 * Float.MAX_VALUE)
     */
    @Test
    public void setBigDecimal_MIN() throws SQLException {
        setFloatExpectations(-1 * Float.MAX_VALUE);

        field.setBigDecimal(new BigDecimal(-1* Float.MAX_VALUE));
    }

    /**
     * Test at minimum allowed value (-1 * Float.MAX_VALUE) minus fraction
     */
    @Test
    public void setBigDecimal_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        BigDecimal testValue = new BigDecimal(-1 * Float.MAX_VALUE);
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
        toReturnFloatExpectations(1);

        assertTrue("Expected true for getBoolean with field value 1", field.getBoolean());
    }

    @Test
    @Override
    public void getObject_Boolean() throws SQLException {
        toReturnFloatExpectations(1);

        assertTrue("Expected true for getObject(Boolean.class) with field value 1", field.getObject(Boolean.class));
    }

    /**
     * Test for value for false with value zero
     */
    @Test
    public void getBooleanNonNull_false_zero() throws SQLException {
        toReturnFloatExpectations(0);

        assertFalse("Expected false for getBoolean with field value 0", field.getBoolean());
    }

    /**
     * Test for value for false with value other than 1 or zero
     * TODO: Check if this is according to spec
     */
    @Test
    public void getBooleanNonNull_false_other() throws SQLException {
        toReturnFloatExpectations(513);

        assertFalse("Expected false for getBoolean with field value other than 1 or 0", field.getBoolean());
    }

    @Test
    public void getBooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertFalse("Expected false for getBoolean with field value null", field.getBoolean());
    }

    @Test
    public void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Boolean.class)", field.getObject(Boolean.class));
    }

    /**
     * Tests with setting true
     */
    @Test
    @Override
    public void setBoolean() throws SQLException {
        setFloatExpectations(1);

        field.setBoolean(true);
    }

    /**
     * Tests with setting false
     */
    @Test
    public void setBoolean_false() throws SQLException {
        setFloatExpectations(0);

        field.setBoolean(false);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        toReturnFloatExpectations(114.123f);

        assertEquals("Unexpected value for getByte", 114, field.getByte());
    }

    @Test
    @Override
    public void getObject_Byte() throws SQLException {
        toReturnFloatExpectations(114.123f);

        assertEquals("Unexpected value for getObject(Byte.class)", 114, (byte) field.getObject(Byte.class));
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE).
     */
    @Test
    public void getByte_MAX() throws SQLException {
        toReturnFloatExpectations(Byte.MAX_VALUE);

        assertEquals("Unexpected value for getByte", 127, field.getByte());
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE) plus a fraction.
     */
    @Test
    public void getByte_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Byte.MAX_VALUE + Math.ulp(Byte.MAX_VALUE));

        field.getByte();
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE).
     */
    @Test
    public void getByte_MIN() throws SQLException {
        toReturnFloatExpectations(Byte.MIN_VALUE);

        assertEquals("Unexpected value for getByte", -128, field.getByte());
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE) minus a fraction.
     */
    @Test
    public void getByte_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Byte.MIN_VALUE - Math.ulp(Byte.MIN_VALUE));

        field.getByte();
    }

    @Test
    public void getByteNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getByte for null", 0, field.getByte());
    }

    @Test
    public void getObject_ByteNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Byte.class)", field.getObject(Byte.class));
    }

    @Test
    @Override
    public void setByte() throws SQLException {
        setFloatExpectations(-54);

        field.setByte((byte)-54);
    }

    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        toReturnFloatExpectations(1.34578f);

        assertEquals("Unexpected value for getDouble", 1.34578f, field.getDouble(), 0);
    }

    @Test
    @Override
    public void getObject_Double() throws SQLException {
        toReturnFloatExpectations(1.34578f);

        assertEquals("Unexpected value for getObject(Double.class)", 1.34578f, field.getObject(Double.class), 0);
    }

    @Test
    public void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getDouble for null", 0, field.getDouble(), 0);
    }

    @Test
    public void getObject_DoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Double.class)", field.getObject(Double.class));
    }

    @Test
    @Override
    public void setDouble() throws SQLException {
        setFloatExpectations(9157824.1245785f);

        field.setDouble(9157824.1245785f);
    }

    @Test
    public void setDouble_NaN() throws SQLException {
        setFloatExpectations(Float.NaN);

        field.setDouble(Double.NaN);
    }

    @Test
    public void setDouble_posInf() throws SQLException {
        setFloatExpectations(Float.POSITIVE_INFINITY);

        field.setDouble(Double.POSITIVE_INFINITY);
    }

    @Test
    public void setDouble_negInf() throws SQLException {
        setFloatExpectations(Float.NEGATIVE_INFINITY);

        field.setDouble(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void setDouble_OutOfRange_high() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setDouble(Float.MAX_VALUE + (double)Math.ulp(Float.MAX_VALUE));
    }

    @Test
    public void setDouble_OutOfRange_low() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setDouble(-1 * Float.MAX_VALUE - (double)Math.ulp(Float.MAX_VALUE));
    }

    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        toReturnFloatExpectations(1247.25898f);

        assertEquals("Unexpected value for getFloat", 1247.25898f, field.getFloat(), 0);
    }

    @Test
    @Override
    public void getObject_Float() throws SQLException {
        toReturnFloatExpectations(1247.25898f);

        assertEquals("Unexpected value for getObject(Float.class)", 1247.25898f, field.getObject(Float.class), 0);
    }

    @Test
    public void getFloatNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getFloat for null", 0, field.getFloat(), 0);
    }

    @Test
    public void getObject_FloatNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Float.class)", field.getObject(Float.class));
    }

    @Test
    @Override
    public void setFloat() throws SQLException {
        setFloatExpectations(Float.MAX_VALUE);

        field.setFloat(Float.MAX_VALUE);
    }

    @Test
    public void setFloat_NaN() throws SQLException {
        setFloatExpectations(Float.NaN);

        field.setFloat(Float.NaN);
    }

    @Test
    public void setFloat_posInf() throws SQLException {
        setFloatExpectations(Float.POSITIVE_INFINITY);

        field.setFloat(Float.POSITIVE_INFINITY);
    }

    @Test
    public void setFloat_negInf() throws SQLException {
        setFloatExpectations(Float.NEGATIVE_INFINITY);

        field.setFloat(Float.NEGATIVE_INFINITY);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        toReturnFloatExpectations(124578.124578f);

        assertEquals("Unexpected value for getInt", 124578, field.getInt());
    }

    @Test
    @Override
    public void getObject_Integer() throws SQLException {
        toReturnFloatExpectations(124578.124578f);

        assertEquals("Unexpected value for getObject(Integer.class)", 124578, (int) field.getObject(Integer.class));
    }

    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE).
     */
    @Test
    public void getInt_MAX() throws SQLException {
        toReturnFloatExpectations(Integer.MAX_VALUE);

        assertEquals("Unexpected value for getInt", Integer.MAX_VALUE, field.getInt());
    }

    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE) plus a fraction
     */
    @Test
    public void getInt_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Integer.MAX_VALUE + Math.ulp(Integer.MAX_VALUE));

        field.getInt();
    }

    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE).
     */
    @Test
    public void getInt_MIN() throws SQLException {
        toReturnFloatExpectations(Integer.MIN_VALUE);

        assertEquals("Unexpected value for getInt", Integer.MIN_VALUE, field.getInt());
    }

    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE) minus a fraction
     */
    @Test
    public void getInt_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Integer.MIN_VALUE - Math.ulp(Integer.MIN_VALUE));

        field.getInt();
    }

    @Test
    public void getIntNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getInt for null", 0, field.getInt());
    }

    @Test
    public void getObject_IntegerNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Integer.class)", field.getObject(Integer.class));
    }

    @Test
    @Override
    public void setInteger() throws SQLException {
        setFloatExpectations(Integer.MAX_VALUE);

        field.setInteger(Integer.MAX_VALUE);
    }


    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        toReturnFloatExpectations(2.132457f * Integer.MAX_VALUE);

        long expectedValue = (long)(2.132457f * Integer.MAX_VALUE);
        assertEquals("Unexpected value for getLong", expectedValue, field.getLong());
    }

    @Test
    @Override
    public void getObject_Long() throws SQLException {
        toReturnFloatExpectations(2.132457f * Integer.MAX_VALUE);

        long expectedValue = (long)(2.132457f * Integer.MAX_VALUE);
        assertEquals("Unexpected value for getLong", expectedValue, (long) field.getObject(Long.class));
    }

    /**
     * Tests value at maximum allowed (Long.MAX_VALUE).
     */
    @Test
    public void getLong_MAX() throws SQLException {
        toReturnFloatExpectations(Long.MAX_VALUE);

        assertEquals("Unexpected value for getLong", Long.MAX_VALUE, field.getLong());
    }

    /**
     * Tests value at maximum allowed (Long.MAX_VALUE) plus a fraction.
     */
    @Test
    public void getLong_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Long.MAX_VALUE + Math.ulp(Long.MAX_VALUE));

        field.getLong();
    }

    /**
     * Tests value at minimum allowed (Long.MIN_VALUE).
     */
    @Test
    public void getLong_MIN() throws SQLException {
        toReturnFloatExpectations(Long.MIN_VALUE);

        assertEquals("Unexpected value for getLong", Long.MIN_VALUE, field.getLong());
    }

    /**
     * Tests value at minimum allowed (Long.MIN_VALUE) minus a fraction.
     */
    @Test
    public void getLong_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Long.MIN_VALUE - Math.ulp(Long.MIN_VALUE));

        field.getLong();
    }

    @Test
    public void getLongNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getLong for null", 0, field.getLong());
    }

    @Test
    public void getObject_LongNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Long.class)", field.getObject(Long.class));
    }

    @Test
    @Override
    public void setLong() throws SQLException {
        setFloatExpectations(Long.MAX_VALUE);

        field.setLong(Long.MAX_VALUE);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnFloatExpectations(-1 * Float.MAX_VALUE);

        // JDBC expected return type for getObject on Types.FLOAT is java.lang.Double
        Double expectedValue = (double) (-1 * Float.MAX_VALUE);
        assertEquals("Unexpected value for getObject", expectedValue, field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setFloatExpectations(-1 * Float.MAX_VALUE);
        Float setValue = -1 * Float.MAX_VALUE;

        field.setObject(setValue);
    }

    // TODO Add additional object tests (eg with Integer, Long, Float, BigDecimal etc objects).

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        toReturnFloatExpectations(-14578.124f);

        assertEquals("Unexpected value for getShort", -14578, field.getShort());
    }

    @Test
    @Override
    public void getObject_Short() throws SQLException {
        toReturnFloatExpectations(-14578.124f);

        assertEquals("Unexpected value for getShort", -14578, (short) field.getObject(Short.class));
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE).
     * @throws SQLException
     */
    @Test
    public void getShort_MAX() throws SQLException {
        toReturnFloatExpectations(Short.MAX_VALUE);

        assertEquals("Unexpected value for getShort", Short.MAX_VALUE, field.getShort());
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE) plus a fraction
     * @throws SQLException
     */
    @Test
    public void getShort_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE));

        field.getShort();
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE).
     * @throws SQLException
     */
    @Test
    public void getShort_MIN() throws SQLException {
        toReturnFloatExpectations(Short.MIN_VALUE);

        assertEquals("Unexpected value for getShort", Short.MIN_VALUE, field.getShort());
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE) minus a fraction
     * @throws SQLException
     */
    @Test
    public void getShort_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnFloatExpectations(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE));

        field.getShort();
    }

    @Test
    public void getShortNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Unexpected value for getShort for null", 0, field.getShort());
    }

    @Test
    public void getObject_ShortNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Short.class)", field.getObject(Short.class));
    }

    @Test
    @Override
    public void setShort() throws SQLException {
        setFloatExpectations(Short.MIN_VALUE);

        field.setShort(Short.MIN_VALUE);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnFloatExpectations(-14578.124f);

        String expectedValue = String.valueOf(-14578.124f);
        assertEquals("Unexpected value for getString", expectedValue, field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnFloatExpectations(-14578.124f);

        String expectedValue = String.valueOf(-14578.124f);
        assertEquals("Unexpected value for getString", expectedValue, field.getObject(String.class));
    }

    @Test
    public void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getString for null", field.getString());
    }

    @Test
    public void getObject_StringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(String.class)", field.getObject(String.class));
    }

    /**
     * Tests with a valid double in string
     */
    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setFloatExpectations(5145789.12457f);

        field.setString("5145789.12457");
    }

    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();

        field.setString(null);
    }

    @Test
    public void setString_noFloat() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setString("no float");
    }


    @Override
    protected Float getNonNullObject() {
        return 1f;
    }
}
