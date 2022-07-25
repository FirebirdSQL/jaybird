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

import org.firebirdsql.extern.decimal.*;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBDecfloatFieldTest extends BaseJUnit5TestFBField<FBDecfloatField<?>, BigDecimal> {

    // TODO Handling infinity, and nan subject to discussion; add tests after this has been settled

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();
        // NOTE Definition necessary for tests defined in superclass, decfloat(34) used as default
        setupDecfloat34Field();
    }

    @Test
    @Disabled("Ignored in favour of more specific tests")
    void getDecimalNonNull() throws SQLException {
    }

    @Test
    @Disabled("Ignored in favour of more specific tests")
    void setDecimalNonNull() throws SQLException {
    }

    @Test
    void getDecimal_matchesType_Decimal128() throws SQLException {
        FBDecfloatField<Decimal128> field = setupDecfloat34Field();
        final String stringValue = "1.2334E6000";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getDecimal();
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    void getDecimal_Decimal64_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456789012345678901234E300";
        toReturnDecfloat34Expectations(stringValue);

        Decimal64 value = field.getDecimal(Decimal64.class);
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf("1.234567890123457E300"))));
    }

    @Test
    void getDecimal_Decimal64_decfloat34_overflow() {
        final String stringValue = "1.234567890123456789012345678901234E385";
        toReturnDecfloat34Expectations(stringValue);
        
        SQLException exception = assertThrows(TypeConversionException.class, () -> field.getDecimal(Decimal64.class));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void getDecimal_Decimal128_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456789012345678901234E300";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getDecimal(Decimal128.class);
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    void getDecimal_matchesType_Decimal64() throws SQLException {
        FBDecfloatField<Decimal64> field = setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal64 value = field.getDecimal();
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf(stringValue))));
    }

    @Test
    void getDecimal_Decimal64_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal64 value = field.getDecimal(Decimal64.class);
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf(stringValue))));
    }

    @Test
    void getDecimal_Decimal128_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal128 value = field.getDecimal(Decimal128.class);
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    void setDecimal_null() throws SQLException {
        field.setDecimal(null);

        verifySetNull();
    }

    @Test
    void setDecimal_decfloat16_Decimal64() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.234567890123456";

        field.setDecimal(Decimal64.valueOf(stringValue));

        verifySetDecfloat16(stringValue);
    }

    @Test
    void setDecimal_decfloat16_Decimal128() throws SQLException {
        setupDecfloat16Field();

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234"));

        verifySetDecfloat16("1.234567890123457");
    }

    @Test
    void setDecimal_decfloat16_Decimal128_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();

        SQLException exception = assertThrows(TypeConversionException.class,
                () -> field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234E385")));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void setDecimal_decfloat16_Decimal128_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234E-399"));

        verifySetDecfloat16("0E-398");
    }

    @Test
    void setDecimal_decfloat34_Decimal64() throws SQLException {
        final String stringValue = "1.234567890123456";

        field.setDecimal(Decimal64.valueOf(stringValue));

        verifySetDecfloat34(stringValue);
    }

    @Test
    void setDecimal_decfloat34_Decimal128() throws SQLException {
        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234"));

        verifySetDecfloat34("1.234567890123456789012345679901234");
    }

    @Test
    void getObject_Decimal_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal<?> value = field.getObject(Decimal.class);
        assertTrue(value instanceof Decimal128, "Expected Decimal128");
        assertEquals(Decimal128.valueOf(stringValue), value, "Unexpected value");
    }

    @Test
    void getObject_Decimal_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.234567890123456";
        toReturnDecfloat16Expectations(stringValue);

        Decimal<?> value = field.getObject(Decimal.class);
        assertTrue(value instanceof Decimal64, "Expected Decimal64");
        assertEquals(Decimal64.valueOf(stringValue), value, "Unexpected value");
    }

    @Test
    void getObject_Decimal32_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal32 value = field.getObject(Decimal32.class);
        assertEquals(Decimal32.valueOf(stringValue), value, "Unexpected value");
    }

    @Test
    void getObject_Decimal64_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal64 value = field.getObject(Decimal64.class);
        assertEquals(Decimal64.valueOf(stringValue), value, "Unexpected value");
    }

    @Test
    void getObject_Decimal128_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getObject(Decimal128.class);
        assertEquals(Decimal128.valueOf(stringValue), value, "Unexpected value");
    }

    @Test
    void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getBigDecimal(), "Expected null result");
    }

    @Test
    void getDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDecimal(), "Expected null result");
    }

    @Test
    void getObject_BigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(BigDecimal.class), "Expected null result for getObject(BigDecimal.class)");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        toReturnDecfloat34Expectations("0.0037");

        BigDecimal expectedValue = new BigDecimal("0.0037");
        assertEquals(expectedValue, field.getObject(BigDecimal.class), "Unexpected value for BigDecimal");
    }

    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void getBigDecimalNonNull() throws SQLException {
    }

    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void getBigDecimalIntNonNull() throws SQLException {
    }

    @Test
    void getBigDecimalDecfloat16() throws SQLException {
        setupDecfloat16Field();
        toReturnDecfloat16Expectations("9.999999999999999E+384");

        BigDecimal expectedValue = new BigDecimal("9.999999999999999E+384");
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for Decimal64 BigDecimal");
    }

    @Test
    void getBigDecimalDecfloat34() throws SQLException {
        toReturnDecfloat34Expectations("9.999999999999999999999999999999999E+6144");

        BigDecimal expectedValue = new BigDecimal("9.999999999999999999999999999999999E+6144");
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for Decimal64 BigDecimal");
    }

    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void setBigDecimalNonNull() throws SQLException {
    }

    @Test
    void setBigDecimal_decfloat16() throws SQLException {
        setupDecfloat16Field();

        field.setBigDecimal(new BigDecimal("43.12345678901234"));

        verifySetDecfloat16("43.12345678901234");
    }

    @Test
    void setBigDecimal_decfloat16_null() throws SQLException {
        setupDecfloat16Field();

        field.setBigDecimal(null);

        verifySetNull();
    }

    @Test
    void setBigDecimal_decfloat16_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();

        SQLException exception = assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(new BigDecimal("1.234567890123456E385")));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void setBigDecimal_decfloat16_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();
        // value too small
        final String stringValue = "1.234567890123456E-399";

        field.setBigDecimal(new BigDecimal(stringValue));

        verifySetDecfloat16("0E-398");
    }

    @Test
    void setBigDecimal_decfloat34() throws SQLException {
        field.setBigDecimal(new BigDecimal("43.123456789012345678901234567890"));

        verifySetDecfloat34("43.123456789012345678901234567890");
    }

    @Test
    void setBigDecimal_decfloat34Null() throws SQLException {
        field.setBigDecimal(null);

        verifySetNull();
    }

    @Test
    void setBigDecimal_decfloat34_exception_onOverflow() {
        // value too big to fit
        final String stringValue = "1.234567890123456789012345678901234E+6145";

        SQLException exception = assertThrows(SQLException.class,
                () -> field.setBigDecimal(new BigDecimal(stringValue)));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void setBigDecimal_decfloat34_roundToZero_onUnderflow() throws SQLException {
        // value too small
        final String stringValue = "1.234567890123456789012345678901234E-6177";

        field.setBigDecimal(new BigDecimal(stringValue));

        verifySetDecfloat34("0E-6176");
    }

    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void getBooleanNonNull() throws SQLException {
    }

    @Test
    void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Boolean.class), "Expected null for getObject(Boolean.class)");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        toReturnDecfloat34Expectations("1");

        assertTrue(field.getObject(Boolean.class), "Expected true from getBoolean");
    }

    @Test
    void getBooleanTrue() throws SQLException {
        toReturnDecfloat34Expectations("1");

        assertTrue(field.getBoolean(), "Expected true from getBoolean");
    }

    @Test
    void getBooleanFalse() throws SQLException {
        // NOTE Any value other than 1 would do
        toReturnDecfloat34Expectations("0");

        assertFalse(field.getBoolean(), "Expected false from getBoolean");
    }

    @Test
    void getBoolean_oneWithPrecision2() throws SQLException {
        toReturnDecfloat34Expectations("1.0");

        //TODO See also DECIMAL/NUMERIC behavior?
        // assertTrue(field.getBoolean(), "Expected true from getBoolean");
        assertFalse(field.getBoolean(), "Expected true from getBoolean");
    }

    @Test
    void getBoolean_notExactlyOne() throws SQLException {
        toReturnDecfloat34Expectations("1.1");

        //TODO See also DECIMAL/NUMERIC behavior?
        // assertTrue(field.getBoolean(), "Expected true from getBoolean");
        assertFalse(field.getBoolean(), "Expected true from getBoolean");
    }

    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void setBoolean() throws SQLException {
    }

    @Test
    void setBooleanTrue() throws SQLException {
        field.setBoolean(true);

        verifySetDecfloat34("1");
    }

    @Test
    void setBooleanFalse() throws SQLException {
        field.setBoolean(false);

        verifySetDecfloat34("0");
    }

    @Test
    void getByteNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getByte(), "Expected getByte() to return 0 for NULL value");
    }

    @Test
    void getObject_ByteNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Byte.class), "Expected getObject(Byte.class) to return null for NULL value");
    }

    @Test
    @Override
    void getByteNonNull() throws SQLException {
        toReturnDecfloat34Expectations("-128");

        assertEquals(Byte.MIN_VALUE, field.getByte(), "Unexpected value for getByte()");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        toReturnDecfloat34Expectations("-128");

        assertEquals(Byte.valueOf(Byte.MIN_VALUE), field.getObject(Byte.class),
                "Unexpected value for getObject(Byte.class)");
    }

    @Test
    void getByteTooHigh() {
        toReturnDecfloat34Expectations("128");

        assertThrows(TypeConversionException.class, field::getByte);
    }

    @Test
    void getByteTooLow() {
        toReturnDecfloat34Expectations("-129");

        assertThrows(TypeConversionException.class, field::getByte);
    }

    @Test
    @Override
    void setByte() throws SQLException {
        field.setByte((byte) -34);

        verifySetDecfloat34("-34");
    }

    @Test
    void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0.0, field.getDouble(), 0.0, "Expected getDouble() to return 0.0 for NULL value");
    }

    @Test
    void getObject_DoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Double.class), "Expected getObject(Double.class) to return null for NUL value");
    }

    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        toReturnDecfloat34Expectations("8.938297342");

        assertEquals(8.938297342, field.getDouble(), 0.0, "Unexpected value for getDouble()");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        toReturnDecfloat34Expectations("8.938297342");

        assertEquals(8.938297342, field.getObject(Double.class), 0.0, "Unexpected value for getObject(Double.class)");
    }

    @Test
    void setDouble() throws SQLException {
        field.setDouble(469.1234567);

        verifySetDecfloat34("469.1234567");
    }

    @Test
    void setDouble_decfloat16() throws SQLException {
        setupDecfloat16Field();

        field.setDouble(469.1234567);

        verifySetDecfloat16("469.1234567");
    }

    @Test
    void getFloatNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0.0, field.getFloat(), 0.0, "Expected getFloat() to return 0.0 for NULL value");
    }

    @Test
    void getObject_FloatNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Float.class), "Expected getObject(Float.class) to return null for NUL value");
    }

    @Test
    @Override
    void getFloatNonNull() throws SQLException {
        toReturnDecfloat34Expectations("469.12344");

        assertEquals(469.12344f, field.getFloat(), 0.0, "Unexpected value for getFloat()");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        toReturnDecfloat34Expectations("469.12344");

        assertEquals(469.12344f, field.getObject(Float.class), 0.0, "Unexpected value for getObject(Float.class)");
    }

    @Test
    @Override
    void setFloat() throws SQLException {
        field.setFloat(469.12344f);

        // artifact of float -> double -> decimal
        verifySetDecfloat34("469.1234436035156");
    }

    @Test
    void getIntNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getInt(), "Expected getInt() to return 0 for NULL value");
    }

    @Test
    void getObject_IntegerNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Integer.class), "Expected getObject(Integer.class) to return null for NUL value");
    }

    @Test
    @Override
    void getIntNonNull() throws SQLException {
        final int expectedValue = 987654321;
        toReturnDecfloat34Expectations(String.valueOf(expectedValue));

        assertEquals(expectedValue, field.getInt(), "Unexpected value from getInt()");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        final int expectedValue = 987654321;
        toReturnDecfloat34Expectations(String.valueOf(expectedValue));

        assertEquals(987654321, (int) field.getObject(Integer.class), "Unexpected value from getInt()");
    }

    @Test
    void getIntTooHigh() {
        toReturnDecfloat34Expectations(String.valueOf(Integer.MAX_VALUE + 1L));

        assertThrows(TypeConversionException.class, field::getInt);
    }

    @Test
    void getIntTooLow() {
        toReturnDecfloat34Expectations(String.valueOf(Integer.MIN_VALUE - 1L));

        assertThrows(TypeConversionException.class, field::getInt);
    }

    @Test
    @Override
    void setInteger() throws SQLException {
        final int value = 123456;

        field.setInteger(value);

        verifySetDecfloat34(String.valueOf(value));
    }

    @Test
    void getLongNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getLong(), "Expected getLong() to return 0 for NULL value");
    }

    @Test
    void getObject_LongNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Long.class), "Expected getObject(Long.class) to return null for NUL value");
    }

    @Test
    @Override
    void getLongNonNull() throws SQLException {
        toReturnDecfloat34Expectations(String.valueOf(Long.MAX_VALUE));

        assertEquals(Long.MAX_VALUE, field.getLong(), "Unexpected value from getLong()");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        toReturnDecfloat34Expectations(String.valueOf(Long.MAX_VALUE));

        assertEquals(Long.MAX_VALUE, (long) field.getObject(Long.class), "Unexpected value from getLong()");
    }

    @Test
    void getLongTooHigh() {
        toReturnDecfloat34Expectations(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString());

        assertThrows(TypeConversionException.class, field::getLong);
    }

    @Test
    void getLongTooLow() {
        toReturnDecfloat34Expectations(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE).toString());

        assertThrows(TypeConversionException.class, field::getLong);
    }

    @Test
    @Override
    void setLong() throws SQLException {
        field.setLong(35);

        verifySetDecfloat34("35");
    }

    @Test
    void setLong_decfloat16_atMaxPrecision() throws SQLException {
        setupDecfloat16Field();

        field.setLong(9999999999999999L);

        verifySetDecfloat16("9999999999999999");
    }

    @Test
    void setLong_decfloat16_1OverMaxPrecision() throws SQLException {
        setupDecfloat16Field();

        field.setLong(10000000000000000L);

        verifySetDecfloat16("1000000000000000E+1");
    }

    @Test
    void setLong_decfloat16_exceedsPrecision_maxValue() throws SQLException {
        setupDecfloat16Field();

        field.setLong(Long.MAX_VALUE);

        verifySetDecfloat16("9223372036854776E+3");
    }

    @Test
    void setLong_decfloat16_overMaxPrecision_roundingHalfEven_resultDown() throws SQLException {
        setupDecfloat16Field();

        field.setLong(10000000000000005L);

        verifySetDecfloat16("1000000000000000E+1");
    }

    @Test
    void setLong_decfloat16_overMaxPrecision_roundingHalfEven_resultUp() throws SQLException {
        setupDecfloat16Field();

        field.setLong(10000000000000015L);

        verifySetDecfloat16("1000000000000002E+1");
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnDecfloat34Expectations("513.00000000");

        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals(expectedValue, field.getObject(), "Unexpected value for long BigDecimal");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(new BigDecimal("1234.567"));

        verifySetDecfloat34("1234.567");
    }

    // TODO Add tests for other object types

    @Test
    @Override
    void getShortNonNull() throws SQLException {
        toReturnDecfloat34Expectations("12345.6789");

        assertEquals(12345, field.getShort(), "Unexpected value from getShort()");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        toReturnDecfloat34Expectations("12345.6789");

        assertEquals(12345, (short) field.getObject(Short.class), "Unexpected value from getShort()");
    }

    @Test
    void getShortTooHigh() {
        toReturnDecfloat34Expectations(String.valueOf(Short.MAX_VALUE + 1));

        assertThrows(TypeConversionException.class, field::getShort);
    }

    @Test
    void getShortTooLow() {
        toReturnDecfloat34Expectations(String.valueOf(Short.MIN_VALUE - 1));

        assertThrows(TypeConversionException.class, field::getShort);
    }

    @Test
    @Override
    void setShort() throws SQLException {
        field.setShort(Short.MIN_VALUE);

        verifySetDecfloat34(String.valueOf(Short.MIN_VALUE));
    }

    @Test
    void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getString());
    }

    @Test
    void getObject_StringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(String.class));
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals("4567891.23", field.getString(), "Unexpected value from getString()");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals("4567891.23", field.getObject(String.class), "Unexpected value from getString()");
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        setupDecfloat16Field();

        field.setString("1234567890123.456789");

        verifySetDecfloat16("1234567890123.457");
    }

    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }

    @Test
    void setStringNonNumber() {
        assertThrows(TypeConversionException.class, () -> field.setString("NotANumber"));
    }

    @Test
    void setString_decfloat16_rounding_fitPrecision() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.2345678901234567";

        field.setString(stringValue);

        // Precision 17 > 16
        verifySetDecfloat16("1.234567890123457");
    }

    @Test
    void setString_decfloat34_rounding_fitPrecision() throws SQLException {
        final String stringValue = "1.2345678901234567890123456789012345";

        field.setString(stringValue);

        // Precision 35 > 34
        verifySetDecfloat34("1.234567890123456789012345678901234");
    }

    @Test
    void setString_decfloat16_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();
        // value too big to fit
        final String stringValue = "1.234567890123456E385";

        SQLException exception = assertThrows(TypeConversionException.class, () -> field.setString(stringValue));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void setString_decfloat16_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();
        // value too small
        final String stringValue = "1.234567890123456E-399";

        field.setString(stringValue);

        verifySetDecfloat16("0E-398");
    }

    @Test
    void setString_decfloat34_exception_onOverflow() {
        // value too big to fit
        final String stringValue = "1.234567890123456789012345678901234E+6145";

        SQLException exception = assertThrows(TypeConversionException.class, () -> field.setString(stringValue));
        assertThat(exception, message(containsString("out of range")));
    }

    @Test
    void setString_decfloat34_roundToZero_onUnderflow() throws SQLException {
        // value too small
        final String stringValue = "1.234567890123456789012345678901234E-6177";

        field.setString(stringValue);

        verifySetDecfloat34("0E-6176");
    }

    @Test
    @Override
    void getObject_BigInteger() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals(BigInteger.valueOf(4567891), field.getObject(BigInteger.class),
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

        verifySetDecfloat34("10");
    }

    @Test
    void setObject_BigInteger_Long_MAX() throws SQLException {
        field.setObject(BigInteger.valueOf(Long.MAX_VALUE));

        verifySetDecfloat34(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    void setObject_BigInteger_Long_MAX_plus_1() throws SQLException {
        final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

        field.setObject(value);

        verifySetDecfloat34(value.toString());
    }

    @Test
    void setObject_BigInteger_Long_MIN() throws SQLException {
        field.setObject(BigInteger.valueOf(Long.MIN_VALUE));

        verifySetDecfloat34(String.valueOf(Long.MIN_VALUE));
    }

    @Test
    void setObject_BigInteger_Long_MIN_minus_1() throws SQLException {
        final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).subtract(BigInteger.ONE);

        field.setObject(value);

        verifySetDecfloat34(value.toString());
    }

    @Test
    void setBigInteger_null() throws SQLException {
        field.setBigInteger(null);

        verifySetNull();
    }

    @SuppressWarnings("unused")
    @Test
    void constructWithUnsupportedSqlType() {
        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        assertThrows(SQLException.class,
                () -> new FBDecfloatField<>(fieldDescriptor, fieldData, Types.VARCHAR, Decimal128.class));
    }

    @Test
    void constructWithUnsupportedDecimal32() {
        SQLException exception = assertThrows(FBDriverNotCapableException.class,
                () -> new FBDecfloatField<>(fieldDescriptor, fieldData, JaybirdTypeCodes.DECFLOAT, Decimal32.class));
        assertThat(exception, message(equalTo(
                "Unsupported type org.firebirdsql.extern.decimal.Decimal32 and/or field type 32762")));
    }

    private FBDecfloatField<Decimal64> setupDecfloat16Field() throws SQLException {
        fieldDescriptor = rowDescriptorBuilder
                .setType(ISCConstants.SQL_DEC16)
                .toFieldDescriptor();
        FBDecfloatField<Decimal64> tempField =
                new FBDecfloatField<>(fieldDescriptor, fieldData, JaybirdTypeCodes.DECFLOAT, Decimal64.class);
        field = tempField;
        return tempField;
    }

    private FBDecfloatField<Decimal128> setupDecfloat34Field() throws SQLException {
        fieldDescriptor = rowDescriptorBuilder
                .setType(ISCConstants.SQL_DEC34)
                .toFieldDescriptor();
        FBDecfloatField<Decimal128> tempField =
                new FBDecfloatField<>(fieldDescriptor, fieldData, JaybirdTypeCodes.DECFLOAT, Decimal128.class);
        field = tempField;
        return tempField;
    }

    @Override
    BigDecimal getNonNullObject() {
        return BigDecimal.ONE;
    }
}