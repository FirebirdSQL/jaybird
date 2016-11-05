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
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.Assert.*;

/**
 * Tests for {@link FBShortField}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBShortField extends BaseJUnit4TestFBField<FBShortField, Short> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_SHORT);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBShortField(fieldDescriptor, fieldData, Types.SMALLINT);
    }

    @Test
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        toReturnShortExpectations(4);

        BigDecimal expectedValue = new BigDecimal(4);
        assertEquals("Unexpected value for getBigDecimal", expectedValue, field.getBigDecimal());
    }

    @Test
    @Override
    public void getObject_BigDecimal() throws SQLException {
        toReturnShortExpectations(4);

        BigDecimal expectedValue = new BigDecimal(4);
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
        setShortExpectations(10);

        field.setBigDecimal(BigDecimal.TEN);
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE)
     */
    @Test
    public void setBigDecimal_MAX() throws SQLException {
        setShortExpectations(Short.MAX_VALUE);

        field.setBigDecimal(new BigDecimal(Short.MAX_VALUE));
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE) plus fraction
     */
    @Test
    public void setBigDecimal_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        BigDecimal testValue = new BigDecimal(Short.MAX_VALUE);
        testValue = testValue.add(testValue.ulp());

        field.setBigDecimal(testValue);
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE)
     */
    @Test
    public void setBigDecimal_MIN() throws SQLException {
        setShortExpectations(Short.MIN_VALUE);

        field.setBigDecimal(new BigDecimal(Short.MIN_VALUE));
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE) minus fraction
     */
    @Test
    public void setBigDecimal_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        BigDecimal testValue = new BigDecimal(Short.MIN_VALUE);
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
        toReturnShortExpectations(1);

        assertTrue("Expected true for getBoolean with field value 1", field.getBoolean());
    }

    @Test
    @Override
    public void getObject_Boolean() throws SQLException {
        toReturnShortExpectations(1);

        assertTrue("Expected true for getObject(Boolean.class) with field value 1", field.getObject(Boolean.class));
    }

    /**
     * Test for value for false with value zero
     */
    @Test
    public void getBooleanNonNull_false_zero() throws SQLException {
        toReturnShortExpectations(0);

        assertFalse("Expected false for getBoolean with field value 0", field.getBoolean());
    }

    /**
     * Test for value for false with value other than 1 or zero
     * TODO: Check if this is according to spec
     */
    @Test
    public void getBooleanNonNull_false_other() throws SQLException {
        toReturnShortExpectations(513);

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
        setShortExpectations(1);

        field.setBoolean(true);
    }

    /**
     * Tests with setting false
     */
    @Test
    public void setBoolean_false() throws SQLException {
        setShortExpectations(0);

        field.setBoolean(false);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        toReturnShortExpectations(114);

        assertEquals("Unexpected value for getByte", 114, field.getByte());
    }

    @Test
    @Override
    public void getObject_Byte() throws SQLException {
        toReturnShortExpectations(114);

        assertEquals("Unexpected value for getObject(Byte.class)", 114, (byte) field.getObject(Byte.class));
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE).
     */
    @Test
    public void getByte_MAX() throws SQLException {
        toReturnShortExpectations(Byte.MAX_VALUE);

        assertEquals("Unexpected value for getByte", 127, field.getByte());
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE) plus one.
     */
    @Test
    public void getByte_MAX_plus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnShortExpectations(Byte.MAX_VALUE + 1);

        field.getByte();
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE).
     */
    @Test
    public void getByte_MIN() throws SQLException {
        toReturnShortExpectations(Byte.MIN_VALUE);

        assertEquals("Unexpected value for getByte", -128, field.getByte());
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE) minus one.
     */
    @Test
    public void getByte_MIN_minus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnShortExpectations(Byte.MIN_VALUE - 1);

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
        setShortExpectations(-54);

        field.setByte((byte)-54);
    }

    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        toReturnShortExpectations(2);

        assertEquals("Unexpected value for getDouble", 2.0, field.getDouble(), 0);
    }

    @Test
    @Override
    public void getObject_Double() throws SQLException {
        toReturnShortExpectations(2);

        assertEquals("Unexpected value for getObject(Double.class)", 2.0, field.getObject(Double.class), 0);
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
        setShortExpectations(915);

        field.setDouble(915.1245785);
    }

    @Test
    public void setDouble_MAX() throws SQLException {
        setShortExpectations(Short.MAX_VALUE);

        field.setDouble(Short.MAX_VALUE);
    }

    @Test
    public void setDouble_MIN() throws SQLException {
        setShortExpectations(Short.MIN_VALUE);

        field.setDouble(Short.MIN_VALUE);
    }

    @Test
    public void setDouble_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setDouble(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE));
    }

    @Test
    public void setDouble_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setDouble(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE));
    }

    @Test
    public void setDouble_NaN() throws SQLException {
        setShortExpectations(0);

        field.setDouble(Double.NaN);
    }

    @Test
    public void setDouble_posInf() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setDouble(Double.POSITIVE_INFINITY);
    }

    @Test
    public void setDouble_negInf() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setDouble(Double.NEGATIVE_INFINITY);
    }

    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        toReturnShortExpectations(1247);

        assertEquals("Unexpected value for getFloat", 1247f, field.getFloat(), 0);
    }

    @Test
    @Override
    public void getObject_Float() throws SQLException {
        toReturnShortExpectations(1247);

        assertEquals("Unexpected value for getObject(Float.class)", 1247f, field.getObject(Float.class), 0);
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
        setShortExpectations(15);

        field.setFloat(15f);
    }

    @Test
    public void setFloat_MAX() throws SQLException {
        setShortExpectations(Short.MAX_VALUE);

        field.setFloat(Short.MAX_VALUE);
    }

    @Test
    public void setFloat_MIN() throws SQLException {
        setShortExpectations(Short.MIN_VALUE);

        field.setFloat(Short.MIN_VALUE);
    }

    @Test
    public void setFloat_MAX_plus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setFloat(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE));
    }

    @Test
    public void setFloat_MIN_minus_fraction() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setFloat(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE));
    }

    @Test
    public void setFloat_NaN() throws SQLException {
        setShortExpectations(0);

        field.setFloat(Float.NaN);
    }

    @Test
    public void setFloat_posInf() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setFloat(Float.POSITIVE_INFINITY);
    }

    @Test
    public void setFloat_negInf() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setFloat(Float.NEGATIVE_INFINITY);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        toReturnShortExpectations(12457);

        assertEquals("Unexpected value for getInt", 12457, field.getInt());
    }

    @Test
    @Override
    public void getObject_Integer() throws SQLException {
        toReturnShortExpectations(12457);

        assertEquals("Unexpected value for getObject(Integer.class)", 12457, (int) field.getObject(Integer.class));
    }

    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE).
     */
    @Test
    public void getInt_MAX() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        assertEquals("Unexpected value for getInt", Short.MAX_VALUE, field.getInt());
    }

    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE).
     */
    @Test
    public void getInt_MIN() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        assertEquals("Unexpected value for getInt", Short.MIN_VALUE, field.getInt());
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
        setShortExpectations(5323);

        field.setInteger(5323);
    }

    @Test
    public void setInteger_MAX() throws SQLException {
        setShortExpectations(Short.MAX_VALUE);

        field.setInteger(Short.MAX_VALUE);
    }

    @Test
    public void setInteger_MAX_plus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setInteger(Short.MAX_VALUE + 1);
    }

    @Test
    public void setInteger_MIN_minus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setInteger(Short.MIN_VALUE - 1);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        long expectedValue = Short.MAX_VALUE;
        assertEquals("Unexpected value for getLong", expectedValue, field.getLong());
    }

    @Test
    @Override
    public void getObject_Long() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        long expectedValue = Short.MAX_VALUE;
        assertEquals("Unexpected value for getLong", expectedValue, (long) field.getObject(Long.class));
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
        setShortExpectations(Short.MAX_VALUE);

        field.setLong(Short.MAX_VALUE);
    }

    @Test
    public void setLong_MAX_plus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setLong(Short.MAX_VALUE + 1L);
    }

    @Test
    public void setLong_MIN_minus_one() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setLong(Short.MIN_VALUE - 1L);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        // JDBC expects type Integer for Types.SMALLINT
        Integer expectedValue = (int) Short.MIN_VALUE;
        assertEquals("Unexpected value for getObject", expectedValue, field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setShortExpectations(4534);
        Integer setValue = 4534;

        field.setObject(setValue);
    }

    // TODO Add additional object tests (eg with Integer, Long, Float, BigDecimal etc objects).

    /**
     * Tests value in range
     */
    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        toReturnShortExpectations(-14578);

        assertEquals("Unexpected value for getShort", -14578, field.getShort());
    }

    @Test
    @Override
    public void getObject_Short() throws SQLException {
        toReturnShortExpectations(-14578);

        assertEquals("Unexpected value for getShort", -14578, (short) field.getObject(Short.class));
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE).
     */
    @Test
    public void getShort_MAX() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        assertEquals("Unexpected value for getShort", Short.MAX_VALUE, field.getShort());
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE).
     */
    @Test
    public void getShort_MIN() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        assertEquals("Unexpected value for getShort", Short.MIN_VALUE, field.getShort());
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
        setShortExpectations(Short.MIN_VALUE);

        field.setShort(Short.MIN_VALUE);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnShortExpectations(-14578);

        String expectedValue = String.valueOf(-14578);
        assertEquals("Unexpected value for getString", expectedValue, field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnShortExpectations(-14578);

        String expectedValue = String.valueOf(-14578);
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
        setShortExpectations(5145);

        field.setString("5145");
    }

    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();

        field.setString(null);
    }

    @Test
    public void setString_noShort() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setString("no short");
    }

    @Test
    @Override
    public void getObject_BigInteger() throws SQLException {
        final short testValue = -14578;
        toReturnShortExpectations(testValue);

        assertEquals("Unexpected value for getObject(BigInteger.class)",
                BigInteger.valueOf(testValue), field.getObject(BigInteger.class));
    }

    @Test
    public void getObject_BigInteger_null() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(BigInteger.class)", field.getObject(BigInteger.class));
    }

    @Test
    @Override
    public void setObject_BigInteger() throws SQLException {
        setShortExpectations(10);

        field.setObject(BigInteger.TEN);
    }

    @Test
    public void setObject_BigInteger_MAX() throws SQLException {
        setShortExpectations(Short.MAX_VALUE);

        field.setObject(BigInteger.valueOf(Short.MAX_VALUE));
    }

    @Test
    public void setObject_BigInteger_MAX_plus_1() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setObject(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
    }

    @Test
    public void setObject_BigInteger_MIN() throws SQLException {
        setShortExpectations(Short.MIN_VALUE);

        field.setObject(BigInteger.valueOf(Short.MIN_VALUE));
    }

    @Test
    public void setObject_BigInteger_MIN_minus_1() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setObject(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE));
    }

    @Test
    public void setBigInteger_null() throws SQLException {
        setNullExpectations();

        field.setBigInteger(null);
    }

    @Override
    protected Short getNonNullObject() {
        return 1;
    }
}
