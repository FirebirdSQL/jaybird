/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FBIntegerField}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBIntegerFieldTest extends BaseJUnit5TestFBField<FBIntegerField, Integer> {

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_LONG);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBIntegerField(fieldDescriptor, fieldData, Types.INTEGER);
    }

    @Test
    @Override
    void getBigDecimalNonNull() throws SQLException {
        toReturnIntegerExpectations(4);

        BigDecimal expectedValue = new BigDecimal(4);
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        toReturnIntegerExpectations(4);

        BigDecimal expectedValue = new BigDecimal(4);
        assertEquals(expectedValue, field.getObject(BigDecimal.class),
                "Unexpected value for getObject(BigDecimal.class)");
    }

    @Test
    void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getBigDecimal(), "Expected null result");
    }

    @Test
    void getObject_BigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(BigDecimal.class), "Expected null result for getObject(BigDecimal.class)");
    }

    @Test
    @Override
    void setBigDecimalNonNull() throws SQLException {
        field.setBigDecimal(BigDecimal.TEN);

        verifySetInteger(10);
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE)
     */
    @Test
    void setBigDecimal_MAX() throws SQLException {
        field.setBigDecimal(new BigDecimal(Integer.MAX_VALUE));

        verifySetInteger(Integer.MAX_VALUE);
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE) plus fraction
     */
    @Test
    void setBigDecimal_MAX_plus_fraction() {
        BigDecimal maxValue = new BigDecimal(Integer.MAX_VALUE);
        BigDecimal testValue = maxValue.add(maxValue.ulp());

        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(testValue));
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE)
     */
    @Test
    void setBigDecimal_MIN() throws SQLException {
        field.setBigDecimal(new BigDecimal(Integer.MIN_VALUE));

        verifySetInteger(Integer.MIN_VALUE);
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE) minus fraction
     */
    @Test
    void setBigDecimal_MIN_minus_fraction() {
        BigDecimal maxValue = new BigDecimal(Integer.MIN_VALUE);
        BigDecimal testValue = maxValue.subtract(maxValue.ulp());

        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(testValue));
    }

    @Test
    @Disabled
    @Override
    void getBigDecimalIntNonNull() throws SQLException {
        // TODO: Implement test for getBigDecimal(int)
    }

    @Test
    void setBigDecimalNull() throws SQLException {
        field.setBigDecimal(null);

        verifySetNull();
    }

    /**
     * Test for value for true.
     */
    @Test
    @Override
    void getBooleanNonNull() throws SQLException {
        toReturnIntegerExpectations(1);

        assertTrue(field.getBoolean(), "Expected true for getBoolean with field value 1");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        toReturnIntegerExpectations(1);

        assertTrue(field.getObject(Boolean.class), "Expected true for getObject(Boolean.class) with field value 1");
    }

    /**
     * Test for value for false with value zero
     */
    @Test
    void getBooleanNonNull_false_zero() throws SQLException {
        toReturnIntegerExpectations(0);

        assertFalse(field.getBoolean(), "Expected false for getBoolean with field value 0");
    }

    /**
     * Test for value for false with value other than 1 or zero
     * TODO: Check if this is according to spec
     */
    @Test
    void getBooleanNonNull_false_other() throws SQLException {
        toReturnIntegerExpectations(513);

        assertFalse(field.getBoolean(), "Expected false for getBoolean with field value other than 1 or 0");
    }

    @Test
    void getBooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertFalse(field.getBoolean(), "Expected false for getBoolean with field value null");
    }

    @Test
    void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Boolean.class), "Expected null for getObject(Boolean.class)");
    }

    /**
     * Tests with setting true
     */
    @Test
    @Override
    void setBoolean() throws SQLException {
        field.setBoolean(true);

        verifySetInteger(1);
    }

    /**
     * Tests with setting false
     */
    @Test
    void setBoolean_false() throws SQLException {
        field.setBoolean(false);

        verifySetInteger(0);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getByteNonNull() throws SQLException {
        toReturnIntegerExpectations(114);

        assertEquals(114, field.getByte(), "Unexpected value for getByte");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        toReturnIntegerExpectations(114);

        assertEquals(114, (byte) field.getObject(Byte.class), "Unexpected value for getObject(Byte.class)");
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE).
     */
    @Test
    void getByte_MAX() throws SQLException {
        toReturnIntegerExpectations(Byte.MAX_VALUE);

        assertEquals(127, field.getByte(), "Unexpected value for getByte");
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE) plus one.
     */
    @Test
    void getByte_MAX_plus_one() {
        toReturnIntegerExpectations(Byte.MAX_VALUE + 1);

        assertThrows(TypeConversionException.class, field::getByte);
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE).
     */
    @Test
    void getByte_MIN() throws SQLException {
        toReturnIntegerExpectations(Byte.MIN_VALUE);

        assertEquals(-128, field.getByte(), "Unexpected value for getByte");
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE) minus one.
     */
    @Test
    void getByte_MIN_minus_one() {
        toReturnIntegerExpectations(Byte.MIN_VALUE - 1);

        assertThrows(TypeConversionException.class, field::getByte);
    }

    @Test
    void getByteNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getByte(), "Unexpected value for getByte for null");
    }

    @Test
    void getObject_ByteNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Byte.class), "Expected null for getObject(Byte.class)");
    }

    @Test
    @Override
    void setByte() throws SQLException {
        field.setByte((byte)-54);

        verifySetInteger(-54);
    }

    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        toReturnIntegerExpectations(2);

        assertEquals(2.0, field.getDouble(), 0, "Unexpected value for getDouble");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        toReturnIntegerExpectations(2);

        assertEquals(2.0, field.getObject(Double.class), 0, "Unexpected value for getObject(Double.class)");
    }

    @Test
    void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getDouble(), 0, "Unexpected value for getDouble for null");
    }

    @Test
    void getObject_DoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Double.class), "Expected null for getObject(Double.class)");
    }

    @Test
    @Override
    void setDouble() throws SQLException {
        field.setDouble(9157824.1245785);

        verifySetInteger(9157824);
    }

    @Test
    void setDouble_MAX() throws SQLException {
        field.setDouble(Integer.MAX_VALUE);

        verifySetInteger(Integer.MAX_VALUE);
    }

    @Test
    void setDouble_MIN() throws SQLException {
        field.setDouble(Integer.MIN_VALUE);

        verifySetInteger(Integer.MIN_VALUE);
    }

    @Test
    void setDouble_MAX_plus_fraction() {
        assertThrows(TypeConversionException.class,
                () -> field.setDouble(Integer.MAX_VALUE + Math.ulp(Integer.MAX_VALUE)));
    }

    @Test
    void setDouble_MIN_minus_fraction() {
        assertThrows(TypeConversionException.class,
                () -> field.setDouble(Integer.MIN_VALUE - Math.ulp(Integer.MIN_VALUE)));
    }

    @Test
    void setDouble_NaN() throws SQLException {
        field.setDouble(Double.NaN);

        verifySetInteger(0);
    }

    @Test
    void setDouble_posInf() {
        assertThrows(TypeConversionException.class, () -> field.setDouble(Double.POSITIVE_INFINITY));
    }

    @Test
    void setDouble_negInf() {
        assertThrows(TypeConversionException.class, () -> field.setDouble(Double.NEGATIVE_INFINITY));
    }

    @Test
    @Override
    void getFloatNonNull() throws SQLException {
        toReturnIntegerExpectations(1247);

        assertEquals(1247f, field.getFloat(), 0, "Unexpected value for getFloat");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        toReturnIntegerExpectations(1247);

        assertEquals(1247f, field.getObject(Float.class), 0, "Unexpected value for getObject(Float.class)");
    }

    @Test
    void getFloatNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getFloat(), 0, "Unexpected value for getFloat for null");
    }

    @Test
    void getObject_FloatNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Float.class), "Expected null for getObject(Float.class)");
    }

    @Test
    @Override
    void setFloat() throws SQLException {
        field.setFloat(15f);

        verifySetInteger(15);
    }

    @Test
    void setFloat_MAX() throws SQLException {
        field.setFloat(Integer.MAX_VALUE);

        verifySetInteger(Integer.MAX_VALUE);
    }

    @Test
    void setFloat_MIN() throws SQLException {
        field.setFloat(Integer.MIN_VALUE);

        verifySetInteger(Integer.MIN_VALUE);
    }

    @Test
    void setFloat_MAX_plus_fraction() {
        assertThrows(TypeConversionException.class,
                () -> field.setFloat(Integer.MAX_VALUE + Math.ulp(Integer.MAX_VALUE)));
    }

    @Test
    void setFloat_MIN_minus_fraction() {
        assertThrows(TypeConversionException.class,
                () -> field.setFloat(Integer.MIN_VALUE - Math.ulp(Integer.MIN_VALUE)));
    }

    @Test
    void setFloat_NaN() throws SQLException {
        field.setFloat(Float.NaN);

        verifySetInteger(0);
    }

    @Test
    void setFloat_posInf() {
        assertThrows(TypeConversionException.class, () -> field.setFloat(Float.POSITIVE_INFINITY));
    }

    @Test
    void setFloat_negInf() {
        assertThrows(TypeConversionException.class, () -> field.setFloat(Float.NEGATIVE_INFINITY));
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getIntNonNull() throws SQLException {
        toReturnIntegerExpectations(124578);

        assertEquals(124578, field.getInt(), "Unexpected value for getInt");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        toReturnIntegerExpectations(124578);

        assertEquals(124578, (int) field.getObject(Integer.class), "Unexpected value for getObject(Integer.class)");
    }

    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE).
     */
    @Test
    void getInt_MAX() throws SQLException {
        toReturnIntegerExpectations(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, field.getInt(), "Unexpected value for getInt");
    }

    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE).
     */
    @Test
    void getInt_MIN() throws SQLException {
        toReturnIntegerExpectations(Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, field.getInt(), "Unexpected value for getInt");
    }

    @Test
    void getIntNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getInt(), "Unexpected value for getInt for null");
    }

    @Test
    void getObject_IntegerNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Integer.class), "Expected null for getObject(Integer.class)");
    }

    @Test
    @Override
    void setInteger() throws SQLException {
        field.setInteger(Integer.MAX_VALUE);

        verifySetInteger(Integer.MAX_VALUE);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getLongNonNull() throws SQLException {
        toReturnIntegerExpectations(Integer.MAX_VALUE);

        long expectedValue = Integer.MAX_VALUE;
        assertEquals(expectedValue, field.getLong(), "Unexpected value for getLong");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        toReturnIntegerExpectations(Integer.MAX_VALUE);

        long expectedValue = Integer.MAX_VALUE;
        assertEquals(expectedValue, (long) field.getObject(Long.class), "Unexpected value for getLong");
    }

    @Test
    void getLongNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getLong(), "Unexpected value for getLong for null");
    }

    @Test
    void getObject_LongNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Long.class), "Expected null for getObject(Long.class)");
    }

    @Test
    @Override
    void setLong() throws SQLException {
        field.setLong(Integer.MAX_VALUE);

        verifySetInteger(Integer.MAX_VALUE);
    }

    @Test
    void setLong_MAX_plus_one() {
        assertThrows(TypeConversionException.class, () -> field.setLong(Integer.MAX_VALUE + 1L));
    }

    @Test
    void setLong_MIN_minus_one() {
        assertThrows(TypeConversionException.class, () -> field.setLong(Integer.MIN_VALUE - 1L));
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnIntegerExpectations(Integer.MIN_VALUE);

        Integer expectedValue = Integer.MIN_VALUE;
        assertEquals(expectedValue, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        Integer setValue = 4534;

        field.setObject(setValue);

        verifySetInteger(4534);
    }

    // TODO Add additional object tests (eg with Integer, Long, Float, BigDecimal etc objects).

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getShortNonNull() throws SQLException {
        toReturnIntegerExpectations(-14578);

        assertEquals(-14578, field.getShort(), "Unexpected value for getShort");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        toReturnIntegerExpectations(-14578);

        assertEquals(-14578, (short) field.getObject(Short.class), "Unexpected value for getShort");
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE).
     */
    @Test
    void getShort_MAX() throws SQLException {
        toReturnIntegerExpectations(Short.MAX_VALUE);

        assertEquals(Short.MAX_VALUE, field.getShort(), "Unexpected value for getShort");
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE) plus one
     */
    @Test
    void getShort_MAX_plus_one() {
        toReturnIntegerExpectations(Short.MAX_VALUE + 1);

        assertThrows(TypeConversionException.class, field::getShort);
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE).
     */
    @Test
    void getShort_MIN() throws SQLException {
        toReturnIntegerExpectations(Short.MIN_VALUE);

        assertEquals(Short.MIN_VALUE, field.getShort(), "Unexpected value for getShort");
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE) minus one
     */
    @Test
    void getShort_MIN_minus_one() {
        toReturnIntegerExpectations(Short.MIN_VALUE - 1);

        assertThrows(TypeConversionException.class, field::getShort);
    }

    @Test
    void getShortNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getShort(), "Unexpected value for getShort for null");
    }

    @Test
    void getObject_ShortNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Short.class), "Expected null for getObject(Short.class)");
    }

    @Test
    @Override
    void setShort() throws SQLException {
        field.setShort(Short.MIN_VALUE);

        verifySetInteger(Short.MIN_VALUE);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnIntegerExpectations(-14578);

        String expectedValue = String.valueOf(-14578);
        assertEquals(expectedValue, field.getString(), "Unexpected value for getString");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnIntegerExpectations(-14578);

        String expectedValue = String.valueOf(-14578);
        assertEquals(expectedValue, field.getObject(String.class), "Unexpected value for getString");
    }

    @Test
    void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getString(), "Unexpected value for getString for null");
    }

    @Test
    void getObject_StringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(String.class), "Expected null for getObject(String.class)");
    }

    /**
     * Tests with a valid double in string
     */
    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString("5145789");

        verifySetInteger(5145789);
    }

    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }

    @Test
    void setString_noInteger() {
        assertThrows(TypeConversionException.class, () -> field.setString("no integer"));
    }

    @Test
    @Override
    void getObject_BigInteger() throws SQLException {
        final int testValue = -14578;
        toReturnIntegerExpectations(testValue);

        assertEquals(BigInteger.valueOf(testValue), field.getObject(BigInteger.class),
                "Unexpected value for getObject(BigInteger.class)");
    }

    @Test
    void getObject_BigInteger_null() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(BigInteger.class), "Unexpected value for getObject(BigInteger.class)");
    }

    @Test
    @Override
    void setObject_BigInteger() throws SQLException {
        field.setObject(BigInteger.TEN);

        verifySetInteger(10);
    }

    @Test
    void setObject_BigInteger_MAX() throws SQLException {
        field.setObject(BigInteger.valueOf(Integer.MAX_VALUE));

        verifySetInteger(Integer.MAX_VALUE);
    }

    @Test
    void setObject_BigInteger_MAX_plus_1() {
        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE)));
    }

    @Test
    void setObject_BigInteger_MIN() throws SQLException {
        field.setObject(BigInteger.valueOf(Integer.MIN_VALUE));

        verifySetInteger(Integer.MIN_VALUE);
    }

    @Test
    void setObject_BigInteger_MIN_minus_1() {
        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    @Test
    void setBigInteger_null() throws SQLException {
        field.setBigInteger(null);

        verifySetNull();
    }

    @Test
    @Override
    void getDecimalNonNull() throws SQLException {
        toReturnIntegerExpectations(134578);

        Decimal128 expectedValue = Decimal128.valueOf("134578");
        assertEquals(expectedValue, field.getDecimal(), "Unexpected value for getDecimal");
    }

    @Test
    void getDecimal_null() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDecimal(), "expected null for getDecimal");
    }

    @Test
    @Override
    void setDecimalNonNull() throws SQLException {
        field.setDecimal(Decimal128.valueOf("10"));

        verifySetInteger(10);
    }

    @Test
    void setDecimalNull() throws SQLException {
        field.setDecimal(null);

        verifySetNull();
    }

    @Override
    Integer getNonNullObject() {
        return 1;
    }
}
