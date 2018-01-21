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

import org.firebirdsql.extern.decimal.*;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDecfloatFieldTest extends BaseJUnit4TestFBField<FBDecfloatField<?>, BigDecimal> {

    // TODO Handling infinity, and nan subject to discussion; add tests after this has been settled

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // NOTE Definition necessary for tests defined in superclass, decfloat(34) used as default
        setupDecfloat34Field();
    }

    @Test
    @Ignore("Ignored in favour of more specific tests")
    public void getDecimalNonNull() throws SQLException {
    }

    @Test
    @Ignore("Ignored in favour of more specific tests")
    public void setDecimalNonNull() throws SQLException {
    }

    @Test
    public void getDecimal_matchesType_Decimal128() throws SQLException {
        FBDecfloatField<Decimal128> field = setupDecfloat34Field();
        final String stringValue = "1.2334E6000";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getDecimal();
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    public void getDecimal_Decimal64_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456789012345678901234E300";
        toReturnDecfloat34Expectations(stringValue);

        Decimal64 value = field.getDecimal(Decimal64.class);
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf("1.234567890123457E300"))));
    }

    @Test
    public void getDecimal_Decimal64_decfloat34_overflow() throws SQLException {
        final String stringValue = "1.234567890123456789012345678901234E385";
        toReturnDecfloat34Expectations(stringValue);
        
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.getDecimal(Decimal64.class);
    }

    @Test
    public void getDecimal_Decimal128_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456789012345678901234E300";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getDecimal(Decimal128.class);
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    public void getDecimal_matchesType_Decimal64() throws SQLException {
        FBDecfloatField<Decimal64> field = setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal64 value = field.getDecimal();
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf(stringValue))));
    }

    @Test
    public void getDecimal_Decimal64_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal64 value = field.getDecimal(Decimal64.class);
        assertThat(value, allOf(
                instanceOf(Decimal64.class),
                equalTo(Decimal64.valueOf(stringValue))));
    }

    @Test
    public void getDecimal_Decimal128_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.2334E300";
        toReturnDecfloat16Expectations(stringValue);

        Decimal128 value = field.getDecimal(Decimal128.class);
        assertThat(value, allOf(
                instanceOf(Decimal128.class),
                equalTo(Decimal128.valueOf(stringValue))));
    }

    @Test
    public void setDecimal_null() throws SQLException {
        setNullExpectations();

        field.setDecimal(null);
    }

    @Test
    public void setDecimal_decfloat16_Decimal64() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.234567890123456";
        setDecfloat16Expectations(stringValue);

        field.setDecimal(Decimal64.valueOf(stringValue));
    }

    @Test
    public void setDecimal_decfloat16_Decimal128() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("1.234567890123457");

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234"));
    }

    @Test
    public void setDecimal_decfloat16_Decimal128_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234E385"));
    }

    @Test
    public void setDecimal_decfloat16_Decimal128_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("0E-398");

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234E-399"));
    }

    @Test
    public void setDecimal_decfloat34_Decimal64() throws SQLException {
        final String stringValue = "1.234567890123456";
        setDecfloat34Expectations(stringValue);

        field.setDecimal(Decimal64.valueOf(stringValue));
    }

    @Test
    public void setDecimal_decfloat34_Decimal128() throws SQLException {
        setDecfloat34Expectations("1.234567890123456789012345679901234");

        field.setDecimal(Decimal128.valueOf("1.234567890123456789012345679901234"));
    }

    @Test
    public void getObject_Decimal_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal<?> value = field.getObject(Decimal.class);
        assertTrue("Expected Decimal128", value instanceof Decimal128);
        assertEquals("Unexpected value", Decimal128.valueOf(stringValue), value);
    }

    @Test
    public void getObject_Decimal_decfloat16() throws SQLException {
        setupDecfloat16Field();
        final String stringValue = "1.234567890123456";
        toReturnDecfloat16Expectations(stringValue);

        Decimal<?> value = field.getObject(Decimal.class);
        assertTrue("Expected Decimal64", value instanceof Decimal64);
        assertEquals("Unexpected value", Decimal64.valueOf(stringValue), value);
    }

    @Test
    public void getObject_Decimal32_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal32 value = field.getObject(Decimal32.class);
        assertEquals("Unexpected value", Decimal32.valueOf(stringValue), value);
    }

    @Test
    public void getObject_Decimal64_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal64 value = field.getObject(Decimal64.class);
        assertEquals("Unexpected value", Decimal64.valueOf(stringValue), value);
    }

    @Test
    public void getObject_Decimal128_decfloat34() throws SQLException {
        final String stringValue = "1.234567890123456";
        toReturnDecfloat34Expectations(stringValue);

        Decimal128 value = field.getObject(Decimal128.class);
        assertEquals("Unexpected value", Decimal128.valueOf(stringValue), value);
    }

    @Test
    public void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result", field.getBigDecimal());
    }

    @Test
    public void getDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result", field.getDecimal());
    }

    @Test
    public void getObject_BigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null result for getObject(BigDecimal.class)", field.getObject(BigDecimal.class));
    }

    @Test
    @Override
    public void getObject_BigDecimal() throws SQLException {
        toReturnDecfloat34Expectations("0.0037");

        BigDecimal expectedValue = new BigDecimal("0.0037");
        assertEquals("Unexpected value for BigDecimal", expectedValue, field.getObject(BigDecimal.class));
    }

    @Test
    @Ignore("Ignored in favor of more specific tests")
    @Override
    public void getBigDecimalNonNull() throws SQLException {
    }

    @Test
    @Ignore("Ignored in favor of more specific tests")
    @Override
    public void getBigDecimalIntNonNull() throws SQLException {
    }

    @Test
    public void getBigDecimalDecfloat16() throws SQLException {
        setupDecfloat16Field();
        toReturnDecfloat16Expectations("9.999999999999999E+384");

        BigDecimal expectedValue = new BigDecimal("9.999999999999999E+384");
        assertEquals("Unexpected value for Decimal64 BigDecimal", expectedValue, field.getBigDecimal());
    }

    @Test
    public void getBigDecimalDecfloat34() throws SQLException {
        toReturnDecfloat34Expectations("9.999999999999999999999999999999999E+6144");

        BigDecimal expectedValue = new BigDecimal("9.999999999999999999999999999999999E+6144");
        assertEquals("Unexpected value for Decimal64 BigDecimal", expectedValue, field.getBigDecimal());
    }

    @Test
    @Ignore("Ignored in favor of more specific tests")
    @Override
    public void setBigDecimalNonNull() throws SQLException {
    }

    @Test
    public void setBigDecimal_decfloat16() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("43.12345678901234");

        field.setBigDecimal(new BigDecimal("43.12345678901234"));
    }

    @Test
    public void setBigDecimal_decfloat16_null() throws SQLException {
        setupDecfloat16Field();
        setNullExpectations();

        field.setBigDecimal(null);
    }

    @Test
    public void setBigDecimal_decfloat16_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.setBigDecimal(new BigDecimal("1.234567890123456E385"));
    }

    @Test
    public void setBigDecimal_decfloat16_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();
        // value too small
        final String stringValue = "1.234567890123456E-399";
        setDecfloat16Expectations("0E-398");

        field.setBigDecimal(new BigDecimal(stringValue));
    }

    @Test
    public void setBigDecimal_decfloat34() throws SQLException {
        setDecfloat34Expectations("43.123456789012345678901234567890");

        field.setBigDecimal(new BigDecimal("43.123456789012345678901234567890"));
    }

    @Test
    public void setBigDecimal_decfloat34Null() throws SQLException {
        setNullExpectations();

        field.setBigDecimal(null);
    }

    @Test
    public void setBigDecimal_decfloat34_exception_onOverflow() throws SQLException {
        // value too big to fit
        final String stringValue = "1.234567890123456789012345678901234E+6145";
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.setBigDecimal(new BigDecimal(stringValue));
    }

    @Test
    public void setBigDecimal_decfloat34_roundToZero_onUnderflow() throws SQLException {
        // value too small
        final String stringValue = "1.234567890123456789012345678901234E-6177";
        setDecfloat34Expectations("0E-6176");

        field.setBigDecimal(new BigDecimal(stringValue));
    }

    @Test
    @Ignore("Ignored in favor of more specific tests")
    @Override
    public void getBooleanNonNull() throws SQLException {
    }

    @Test
    public void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Boolean.class)", field.getObject(Boolean.class));
    }

    @Test
    @Override
    public void getObject_Boolean() throws SQLException {
        toReturnDecfloat34Expectations("1");

        assertTrue("Expected true from getBoolean", field.getObject(Boolean.class));
    }

    @Test
    public void getBooleanTrue() throws SQLException {
        toReturnDecfloat34Expectations("1");

        assertTrue("Expected true from getBoolean", field.getBoolean());
    }

    @Test
    public void getBooleanFalse() throws SQLException {
        // NOTE Any value other than 1 would do
        toReturnDecfloat34Expectations("0");

        assertFalse("Expected false from getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_oneWithPrecision2() throws SQLException {
        toReturnDecfloat34Expectations("1.0");

        //TODO See also DECIMAL/NUMERIC behavior?
        // assertTrue("Expected true from getBoolean", field.getBoolean());
        assertFalse("Expected true from getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_notExactlyOne() throws SQLException {
        toReturnDecfloat34Expectations("1.1");

        //TODO See also DECIMAL/NUMERIC behavior?
        // assertTrue("Expected true from getBoolean", field.getBoolean());
        assertFalse("Expected true from getBoolean", field.getBoolean());
    }

    @Test
    @Ignore("Ignored in favor of more specific tests")
    @Override
    public void setBoolean() throws SQLException {
    }

    @Test
    public void setBooleanTrue() throws SQLException {
        setDecfloat34Expectations("1");

        field.setBoolean(true);
    }

    @Test
    public void setBooleanFalse() throws SQLException {
        setDecfloat34Expectations("0");

        field.setBoolean(false);
    }

    @Test
    public void getByteNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected getByte() to return 0 for NULL value", 0, field.getByte());
    }

    @Test
    public void getObject_ByteNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected getObject(Byte.class) to return null for NULL value", field.getObject(Byte.class));
    }

    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        toReturnDecfloat34Expectations("-128");

        assertEquals("Unexpected value for getByte()", Byte.MIN_VALUE, field.getByte());
    }

    @Test
    @Override
    public void getObject_Byte() throws SQLException {
        toReturnDecfloat34Expectations("-128");

        assertEquals("Unexpected value for getObject(Byte.class)",
                Byte.valueOf(Byte.MIN_VALUE), field.getObject(Byte.class));
    }

    @Test
    public void getByteTooHigh() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations("128");

        field.getByte();
    }

    @Test
    public void getByteTooLow() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations("-129");

        field.getByte();
    }

    @Test
    @Override
    public void setByte() throws SQLException {
        setDecfloat34Expectations("-34");

        field.setByte((byte) -34);
    }

    @Test
    public void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected getDouble() to return 0.0 for NULL value", 0.0, field.getDouble(), 0.0);
    }

    @Test
    public void getObject_DoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected getObject(Double.class) to return null for NUL value", field.getObject(Double.class));
    }

    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        toReturnDecfloat34Expectations("8.938297342");

        assertEquals("Unexpected value for getDouble()", 8.938297342, field.getDouble(), 0.0);
    }

    @Test
    @Override
    public void getObject_Double() throws SQLException {
        toReturnDecfloat34Expectations("8.938297342");

        assertEquals("Unexpected value for getObject(Double.class)",
                8.938297342, field.getObject(Double.class), 0.0);
    }

    @Test
    public void setDouble() throws SQLException {
        setDecfloat34Expectations("469.1234567");

        field.setDouble(469.1234567);
    }

    @Test
    public void setDouble_decfloat16() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("469.1234567");

        field.setDouble(469.1234567);
    }

    @Test
    public void getFloatNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected getFloat() to return 0.0 for NULL value", 0.0, field.getFloat(), 0.0);
    }

    @Test
    public void getObject_FloatNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected getObject(Float.class) to return null for NUL value", field.getObject(Float.class));
    }

    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        toReturnDecfloat34Expectations("469.12344");

        assertEquals("Unexpected value for getFloat()", 469.12344f, field.getFloat(), 0.0);
    }

    @Test
    @Override
    public void getObject_Float() throws SQLException {
        toReturnDecfloat34Expectations("469.12344");

        assertEquals("Unexpected value for getObject(Float.class)",
                469.12344f, field.getObject(Float.class), 0.0);
    }

    @Test
    @Override
    public void setFloat() throws SQLException {
        // artifact of float -> double -> decimal
        setDecfloat34Expectations("469.1234436035156");

        field.setFloat(469.12344f);
    }

    @Test
    public void getIntNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected getInt() to return 0 for NULL value", 0, field.getInt());
    }

    @Test
    public void getObject_IntegerNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected getObject(Integer.class) to return null for NUL value", field.getObject(Integer.class));
    }

    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        final int expectedValue = 987654321;
        toReturnDecfloat34Expectations(String.valueOf(expectedValue));

        assertEquals("Unexpected value from getInt()", expectedValue, field.getInt());
    }

    @Test
    @Override
    public void getObject_Integer() throws SQLException {
        final int expectedValue = 987654321;
        toReturnDecfloat34Expectations(String.valueOf(expectedValue));

        assertEquals("Unexpected value from getInt()", 987654321, (int) field.getObject(Integer.class));
    }

    @Test
    public void getIntTooHigh() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(String.valueOf(Integer.MAX_VALUE + 1L));

        field.getInt();
    }

    @Test
    public void getIntTooLow() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(String.valueOf(Integer.MIN_VALUE - 1L));

        field.getInt();
    }

    @Test
    @Override
    public void setInteger() throws SQLException {
        final int value = 123456;
        setDecfloat34Expectations(String.valueOf(value));

        field.setInteger(value);
    }

    @Test
    public void getLongNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected getLong() to return 0 for NULL value", 0, field.getLong());
    }

    @Test
    public void getObject_LongNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected getObject(Long.class) to return null for NUL value", field.getObject(Long.class));
    }

    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        toReturnDecfloat34Expectations(String.valueOf(Long.MAX_VALUE));

        assertEquals("Unexpected value from getLong()", Long.MAX_VALUE, field.getLong());
    }

    @Test
    @Override
    public void getObject_Long() throws SQLException {
        toReturnDecfloat34Expectations(String.valueOf(Long.MAX_VALUE));

        assertEquals("Unexpected value from getLong()", Long.MAX_VALUE, (long) field.getObject(Long.class));
    }

    @Test
    public void getLongTooHigh() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString());

        field.getLong();
    }

    @Test
    public void getLongTooLow() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE).toString());

        field.getLong();
    }

    @Test
    @Override
    public void setLong() throws SQLException {
        setDecfloat34Expectations("35");

        field.setLong(35);
    }

    @Test
    public void setLong_decfloat16_atMaxPrecision() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("9999999999999999");

        field.setLong(9999999999999999L);
    }

    @Test
    public void setLong_decfloat16_1OverMaxPrecision() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("1000000000000000E+1");

        field.setLong(10000000000000000L);
    }

    @Test
    public void setLong_decfloat16_exceedsPrecision_maxValue() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("9223372036854776E+3");

        field.setLong(Long.MAX_VALUE);
    }

    @Test
    public void setLong_decfloat16_overMaxPrecision_roundingHalfEven_resultDown() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("1000000000000000E+1");

        field.setLong(10000000000000005L);
    }

    @Test
    public void setLong_decfloat16_overMaxPrecision_roundingHalfEven_resultUp() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("1000000000000002E+1");

        field.setLong(10000000000000015L);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnDecfloat34Expectations("513.00000000");

        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals("Unexpected value for long BigDecimal", expectedValue, field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setDecfloat34Expectations("1234.567");

        field.setObject(new BigDecimal("1234.567"));
    }

    // TODO Add tests for other object types

    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        toReturnDecfloat34Expectations("12345.6789");

        assertEquals("Unexpected value from getShort()", 12345, field.getShort());
    }

    @Test
    @Override
    public void getObject_Short() throws SQLException {
        toReturnDecfloat34Expectations("12345.6789");

        assertEquals("Unexpected value from getShort()", 12345, (short) field.getObject(Short.class));
    }

    @Test
    public void getShortTooHigh() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(String.valueOf(Short.MAX_VALUE + 1));

        field.getShort();
    }

    @Test
    public void getShortTooLow() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        toReturnDecfloat34Expectations(String.valueOf(Short.MIN_VALUE - 1));

        field.getShort();
    }

    @Test
    @Override
    public void setShort() throws SQLException {
        setDecfloat34Expectations(String.valueOf(Short.MIN_VALUE));

        field.setShort(Short.MIN_VALUE);
    }

    @Test
    public void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getString());
    }

    @Test
    public void getObject_StringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(String.class));
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals("Unexpected value from getString()", "4567891.23", field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals("Unexpected value from getString()", "4567891.23", field.getObject(String.class));
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setupDecfloat16Field();
        setDecfloat16Expectations("1234567890123.457");

        field.setString("1234567890123.456789");
    }

    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();

        field.setString(null);
    }

    @Test
    public void setStringNonNumber() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setString("NotANumber");
    }

    @Test
    public void setString_decfloat16_rounding_fitPrecision() throws SQLException {
        setupDecfloat16Field();
        // Precision 17 > 16
        final String stringValue = "1.2345678901234567";
        setDecfloat16Expectations("1.234567890123457");

        field.setString(stringValue);
    }

    @Test
    public void setString_decfloat34_rounding_fitPrecision() throws SQLException {
        // Precision 35 > 34
        final String stringValue = "1.2345678901234567890123456789012345";
        setDecfloat34Expectations("1.234567890123456789012345678901234");

        field.setString(stringValue);
    }

    @Test
    public void setString_decfloat16_exception_onOverflow() throws SQLException {
        setupDecfloat16Field();
        // value too big to fit
        final String stringValue = "1.234567890123456E385";
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.setString(stringValue);
    }

    @Test
    public void setString_decfloat16_roundToZero_onUnderflow() throws SQLException {
        setupDecfloat16Field();
        // value too small
        final String stringValue = "1.234567890123456E-399";
        setDecfloat16Expectations("0E-398");

        field.setString(stringValue);
    }

    @Test
    public void setString_decfloat34_exception_onOverflow() throws SQLException {
        // value too big to fit
        final String stringValue = "1.234567890123456789012345678901234E+6145";
        expectedException.expect(TypeConversionException.class);
        expectedException.expectMessage(FBField.OVERFLOW_ERROR);

        field.setString(stringValue);
    }

    @Test
    public void setString_decfloat34_roundToZero_onUnderflow() throws SQLException {
        // value too small
        final String stringValue = "1.234567890123456789012345678901234E-6177";
        setDecfloat34Expectations("0E-6176");

        field.setString(stringValue);
    }

    @Test
    @Override
    public void getObject_BigInteger() throws SQLException {
        toReturnDecfloat34Expectations("4567891.23");

        assertEquals("Unexpected value for getObject(BigInteger.class)",
                BigInteger.valueOf(4567891), field.getObject(BigInteger.class));
    }

    @Test
    public void getObject_BigInteger_null() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(BigInteger.class)", field.getObject(BigInteger.class));
    }

    @Test
    @Override
    public void setObject_BigInteger() throws SQLException {
        setDecfloat34Expectations("10");

        field.setObject(BigInteger.TEN);
    }

    @Test
    public void setObject_BigInteger_Long_MAX() throws SQLException {
        setDecfloat34Expectations(String.valueOf(Long.MAX_VALUE));

        field.setObject(BigInteger.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void setObject_BigInteger_Long_MAX_plus_1() throws SQLException {
        final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        setDecfloat34Expectations(value.toString());

        field.setObject(value);
    }

    @Test
    public void setObject_BigInteger_Long_MIN() throws SQLException {
        setDecfloat34Expectations(String.valueOf(Long.MIN_VALUE));

        field.setObject(BigInteger.valueOf(Long.MIN_VALUE));
    }

    @Test
    public void setObject_BigInteger_Long_MIN_minus_1() throws SQLException {
        final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).subtract(BigInteger.ONE);
        setDecfloat34Expectations(value.toString());

        field.setObject(value);
    }

    @Test
    public void setBigInteger_null() throws SQLException {
        setNullExpectations();

        field.setBigInteger(null);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructWithUnsupportedSqlType() throws SQLException {
        expectedException.expect(SQLException.class);
        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        new FBDecfloatField<>(fieldDescriptor, fieldData, Types.VARCHAR, Decimal128.class);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructWithUnsupportedDecimal32() throws SQLException {
        expectedException.expect(FBDriverNotCapableException.class);
        expectedException.expectMessage(
                "Unsupported type org.firebirdsql.extern.decimal.Decimal32 and/or field type 32762");
        new FBDecfloatField<>(fieldDescriptor, fieldData, JaybirdTypeCodes.DECFLOAT, Decimal32.class);
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

    /**
     * Expectations to return a specific decfloat(16) (Decimal64) value from fieldData.
     *
     * @param value
     *         String representation of the value to return
     */
    protected final void toReturnDecfloat16Expectations(final String value) {
        Decimal64 parsedValue = Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        toReturnValueExpectations(parsedValue.toBytes());
    }

    /**
     * Expectations to return a specific decfloat(34) (Decimal128) value from fieldData.
     *
     * @param value
     *         String representation of the value to return
     */
    protected final void toReturnDecfloat34Expectations(final String value) {
        Decimal128 parsedValue = Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        toReturnValueExpectations(parsedValue.toBytes());
    }

    /**
     * Expectations for setting fieldData to a specific decfloat(16) (Decimal64) value.
     *
     * @param value
     *         String representation of the value to be set
     */
    protected final void setDecfloat16Expectations(final String value) {
        Decimal64 parsedValue = Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        setValueExpectations(parsedValue.toBytes());
    }

    /**
     * Expectations for setting fieldData to a specific decfloat(34) (Decimal128) value.
     *
     * @param value
     *         String representation of the value to be set
     */
    protected final void setDecfloat34Expectations(final String value) {
        Decimal128 parsedValue = Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        setValueExpectations(parsedValue.toBytes());
    }

    @Override
    protected BigDecimal getNonNullObject() {
        return BigDecimal.ONE;
    }
}