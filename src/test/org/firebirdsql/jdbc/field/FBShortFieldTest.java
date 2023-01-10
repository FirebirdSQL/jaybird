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
 * Tests for {@link FBShortField}.
 *
 * @author Mark Rotteveel
 */
class FBShortFieldTest extends BaseJUnit5TestFBField<FBShortField, Short> {

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_SHORT);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBShortField(fieldDescriptor, fieldData, Types.SMALLINT);
    }

    @Test
    @Override
    void getBigDecimalNonNull() throws SQLException {
        toReturnShortExpectations(4);

        BigDecimal expectedValue = new BigDecimal(4);
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        toReturnShortExpectations(4);

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

        verifySetShort(10);
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE)
     */
    @Test
    void setBigDecimal_MAX() throws SQLException {
        field.setBigDecimal(new BigDecimal(Short.MAX_VALUE));

        verifySetShort(Short.MAX_VALUE);
    }

    /**
     * Test at maximum allowed value (Integer.MAX_VALUE) plus fraction
     */
    @Test
    void setBigDecimal_MAX_plus_fraction() {
        BigDecimal maxValue = new BigDecimal(Short.MAX_VALUE);
        BigDecimal testValue = maxValue.add(maxValue.ulp());

        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(testValue));
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE)
     */
    @Test
    void setBigDecimal_MIN() throws SQLException {
        field.setBigDecimal(new BigDecimal(Short.MIN_VALUE));

        verifySetShort(Short.MIN_VALUE);
    }

    /**
     * Test at minimum allowed value (Integer.MIN_VALUE) minus fraction
     */
    @Test
    void setBigDecimal_MIN_minus_fraction() {
        BigDecimal minValue = new BigDecimal(Short.MIN_VALUE);
        BigDecimal testValue = minValue.subtract(minValue.ulp());

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
        toReturnShortExpectations(1);

        assertTrue(field.getBoolean(), "Expected true for getBoolean with field value 1");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        toReturnShortExpectations(1);

        assertTrue(field.getObject(Boolean.class), "Expected true for getObject(Boolean.class) with field value 1");
    }

    /**
     * Test for value for false with value zero
     */
    @Test
    void getBooleanNonNull_false_zero() throws SQLException {
        toReturnShortExpectations(0);

        assertFalse(field.getBoolean(), "Expected false for getBoolean with field value 0");
    }

    /**
     * Test for value for false with value other than 1 or zero
     * TODO: Check if this is according to spec
     */
    @Test
    void getBooleanNonNull_false_other() throws SQLException {
        toReturnShortExpectations(513);

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

        verifySetShort(1);
    }

    /**
     * Tests with setting false
     */
    @Test
    void setBoolean_false() throws SQLException {
        field.setBoolean(false);

        verifySetShort(0);
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getByteNonNull() throws SQLException {
        toReturnShortExpectations(114);

        assertEquals(114, field.getByte(), "Unexpected value for getByte");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        toReturnShortExpectations(114);

        assertEquals(114, (byte) field.getObject(Byte.class), "Unexpected value for getObject(Byte.class)");
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE).
     */
    @Test
    void getByte_MAX() throws SQLException {
        toReturnShortExpectations(Byte.MAX_VALUE);

        assertEquals(127, field.getByte(), "Unexpected value for getByte");
    }

    /**
     * Tests getByte with maximum value allowed (Byte.MAX_VALUE) plus one.
     */
    @Test
    void getByte_MAX_plus_one() {
        toReturnShortExpectations(Byte.MAX_VALUE + 1);

        assertThrows(TypeConversionException.class, field::getByte);
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE).
     */
    @Test
    void getByte_MIN() throws SQLException {
        toReturnShortExpectations(Byte.MIN_VALUE);

        assertEquals(-128, field.getByte(), "Unexpected value for getByte");
    }

    /**
     * Tests getByte with minimum value allowed (Byte.MIN_VALUE) minus one.
     */
    @Test
    void getByte_MIN_minus_one() {
        toReturnShortExpectations(Byte.MIN_VALUE - 1);

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

        verifySetShort(-54);
    }

    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        toReturnShortExpectations(2);

        assertEquals(2.0, field.getDouble(), 0, "Unexpected value for getDouble");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        toReturnShortExpectations(2);

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
        field.setDouble(915.1245785);

        verifySetShort(915);
    }

    @Test
    void setDouble_MAX() throws SQLException {
        field.setDouble(Short.MAX_VALUE);

        verifySetShort(Short.MAX_VALUE);
    }

    @Test
    void setDouble_MIN() throws SQLException {
        field.setDouble(Short.MIN_VALUE);

        verifySetShort(Short.MIN_VALUE);
    }

    @Test
    void setDouble_MAX_plus_fraction() {
        assertThrows(TypeConversionException.class, () -> field.setDouble(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE)));
    }

    @Test
    void setDouble_MIN_minus_fraction() {
        assertThrows(TypeConversionException.class, () -> field.setDouble(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE)));
    }

    @Test
    void setDouble_NaN() throws SQLException {
        field.setDouble(Double.NaN);

        verifySetShort(0);
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
        toReturnShortExpectations(1247);

        assertEquals(1247f, field.getFloat(), 0, "Unexpected value for getFloat");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        toReturnShortExpectations(1247);

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

        verifySetShort(15);
    }

    @Test
    void setFloat_MAX() throws SQLException {
        field.setFloat(Short.MAX_VALUE);

        verifySetShort(Short.MAX_VALUE);
    }

    @Test
    void setFloat_MIN() throws SQLException {
        field.setFloat(Short.MIN_VALUE);

        verifySetShort(Short.MIN_VALUE);
    }

    @Test
    void setFloat_MAX_plus_fraction() {
        assertThrows(TypeConversionException.class, () -> field.setFloat(Short.MAX_VALUE + Math.ulp(Short.MAX_VALUE)));
    }

    @Test
    void setFloat_MIN_minus_fraction() {
        assertThrows(TypeConversionException.class, () -> field.setFloat(Short.MIN_VALUE - Math.ulp(Short.MIN_VALUE)));
    }

    @Test
    void setFloat_NaN() throws SQLException {
        field.setFloat(Float.NaN);

        verifySetShort(0);
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
        toReturnShortExpectations(12457);

        assertEquals(12457, field.getInt(), "Unexpected value for getInt");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        toReturnShortExpectations(12457);

        assertEquals(12457, (int) field.getObject(Integer.class), "Unexpected value for getObject(Integer.class)");
    }

    /**
     * Tests value at maximum allowed (Integer.MAX_VALUE).
     */
    @Test
    void getInt_MAX() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        assertEquals(Short.MAX_VALUE, field.getInt(), "Unexpected value for getInt");
    }

    /**
     * Tests value at minimum allowed (Integer.MIN_VALUE).
     */
    @Test
    void getInt_MIN() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        assertEquals(Short.MIN_VALUE, field.getInt(), "Unexpected value for getInt");
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
        field.setInteger(5323);

        verifySetShort(5323);
    }

    @Test
    void setInteger_MAX() throws SQLException {
        field.setInteger(Short.MAX_VALUE);

        verifySetShort(Short.MAX_VALUE);
    }

    @Test
    void setInteger_MAX_plus_one() {
        assertThrows(TypeConversionException.class, () -> field.setInteger(Short.MAX_VALUE + 1));
    }

    @Test
    void setInteger_MIN_minus_one() {
        assertThrows(TypeConversionException.class, () -> field.setInteger(Short.MIN_VALUE - 1));
    }

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getLongNonNull() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        long expectedValue = Short.MAX_VALUE;
        assertEquals(expectedValue, field.getLong(), "Unexpected value for getLong");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        long expectedValue = Short.MAX_VALUE;
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
        field.setLong(Short.MAX_VALUE);

        verifySetShort(Short.MAX_VALUE);
    }

    @Test
    void setLong_MAX_plus_one() {
        assertThrows(TypeConversionException.class, () -> field.setLong(Short.MAX_VALUE + 1L));
    }

    @Test
    void setLong_MIN_minus_one() {
        assertThrows(TypeConversionException.class, () -> field.setLong(Short.MIN_VALUE - 1L));
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        // JDBC expects type Integer for Types.SMALLINT
        Integer expectedValue = (int) Short.MIN_VALUE;
        assertEquals(expectedValue, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        Integer setValue = 4534;

        field.setObject(setValue);
        
        verifySetShort(4534);
    }

    // TODO Add additional object tests (eg with Integer, Long, Float, BigDecimal etc objects).

    /**
     * Tests value in range
     */
    @Test
    @Override
    void getShortNonNull() throws SQLException {
        toReturnShortExpectations(-14578);

        assertEquals(-14578, field.getShort(), "Unexpected value for getShort");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        toReturnShortExpectations(-14578);

        assertEquals(-14578, (short) field.getObject(Short.class), "Unexpected value for getShort");
    }

    /**
     * Tests value at maximum allowed (Short.MAX_VALUE).
     */
    @Test
    void getShort_MAX() throws SQLException {
        toReturnShortExpectations(Short.MAX_VALUE);

        assertEquals(Short.MAX_VALUE, field.getShort(), "Unexpected value for getShort");
    }

    /**
     * Tests value at minimum allowed (Short.MIN_VALUE).
     */
    @Test
    void getShort_MIN() throws SQLException {
        toReturnShortExpectations(Short.MIN_VALUE);

        assertEquals(Short.MIN_VALUE, field.getShort(), "Unexpected value for getShort");
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

        verifySetShort(Short.MIN_VALUE);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnShortExpectations(-14578);

        String expectedValue = String.valueOf(-14578);
        assertEquals(expectedValue, field.getString(), "Unexpected value for getString");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnShortExpectations(-14578);

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
        field.setString("5145");

        verifySetShort(5145);
    }

    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }

    @Test
    void setString_noShort() {
        assertThrows(TypeConversionException.class, () -> field.setString("no short"));
    }

    @Test
    @Override
    void getObject_BigInteger() throws SQLException {
        final short testValue = -14578;
        toReturnShortExpectations(testValue);

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

        verifySetShort(10);
    }

    @Test
    void setObject_BigInteger_MAX() throws SQLException {
        field.setObject(BigInteger.valueOf(Short.MAX_VALUE));

        verifySetShort(Short.MAX_VALUE);
    }

    @Test
    void setObject_BigInteger_MAX_plus_1() {
        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
    }

    @Test
    void setObject_BigInteger_MIN() throws SQLException {
        field.setObject(BigInteger.valueOf(Short.MIN_VALUE));

        verifySetShort(Short.MIN_VALUE);
    }

    @Test
    void setObject_BigInteger_MIN_minus_1() {
        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    @Test
    void setBigInteger_null() throws SQLException {
        field.setBigInteger(null);

        verifySetNull();
    }

    @Test
    @Override
    void getDecimalNonNull() throws SQLException {
        toReturnShortExpectations(14278);

        Decimal128 expectedValue = Decimal128.valueOf("14278");
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

        verifySetShort(10);
    }

    @Test
    void setDecimalNull() throws SQLException {
        field.setDecimal(null);

        verifySetNull();
    }

    @Override
    Short getNonNullObject() {
        return 1;
    }
}
