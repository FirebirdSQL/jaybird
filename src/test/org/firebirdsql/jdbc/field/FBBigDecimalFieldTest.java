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
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FBBigDecimalField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBBigDecimalFieldTest extends BaseJUnit5TestFBField<FBBigDecimalField, BigDecimal> {
    
    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();
        // NOTE This definition is necessary for the tests in the superclass, this is overridden by most tests in the class itself
        rowDescriptorBuilder.setType(ISCConstants.SQL_LONG)
                .setScale(-2);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
    }
    
    // TODO Add set<PrimitiveNumber> tests for out of range condition; in current implementation duplicates setBigDecimal out of range tests
    // TODO Add set/getObject test

    @Test
    void getBigDecimalNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertNull(field.getBigDecimal(), "Expected null result");
    }

    @Test
    void getObject_BigDecimalNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(BigDecimal.class), "Expected null result for getObject(BigDecimal.class)");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-4);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(37);

        BigDecimal expectedValue = new BigDecimal("0.0037");
        assertEquals(expectedValue, field.getObject(BigDecimal.class), "Unexpected value for integer BigDecimal");
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
    void getBigDecimalShort() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnShortExpectations((short) 231);
        
        BigDecimal expectedValue = new BigDecimal("23.1");
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for short BigDecimal");
    }
    
    @Test 
    void getBigDecimalInteger() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-4);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(34);
        
        BigDecimal expectedValue = new BigDecimal("0.0034");
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for integer BigDecimal");
    }
    
    @Test 
    void getBigDecimalLong() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-8);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(51300000000L);
        
        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for long BigDecimal");
    }

    @Test
    void getBigDecimalInt128() throws SQLException {
        fieldDescriptor = createInt128FieldDescriptor(-8);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnInt128Expectations(new BigInteger("4724323329251300020001"));

        final String value = "47243233292513.00020001";
        BigDecimal expectedValue = new BigDecimal(value);
        assertEquals(expectedValue, field.getBigDecimal(), "Unexpected value for Int128 BigDecimal");
    }
    
    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void setBigDecimalNonNull() throws SQLException {
    }
    
    @Test
    void setBigDecimalShort() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        // TODO Might need to add separate test for the rescaling applied here
        field.setBigDecimal(new BigDecimal("43.2"));
        
        verifySetShort((short) 4320);
    }
    
    @Test
    void setBigDecimalShortTooHigh() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(BigDecimal.valueOf(Short.MAX_VALUE + 1, 2)));
    }
    
    @Test
    void setBigDecimalShortTooLow() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(BigDecimal.valueOf(Short.MIN_VALUE - 1, 2)));
    }
    
    @Test
    void setBigDecimalShortNull() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(null);

        verifySetNull();
    }
    
    @Test
    void setBigDecimalInteger() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-3);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(new BigDecimal("1234.567"));

        verifySetInteger(1234567);
    }
    
    @Test
    void setBigDecimalIntegerTooHigh() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(BigDecimal.valueOf(Integer.MAX_VALUE + 1L, 2)));
    }
    
    @Test
    void setBigDecimalIntegerTooLow() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(BigDecimal.valueOf(Integer.MIN_VALUE - 1L, 2)));
    }
    
    @Test
    void setBigDecimalIntegerNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(null);

        verifySetNull();
    }
    
    @Test
    void setBigDecimalLong() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-5);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(new BigDecimal("12345678.90123"));

        verifySetLong(1234567890123L);
    }
    
    @Test
    void setBigDecimalLongTooHigh() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(new BigDecimal(value, 2)));
    }
    
    @Test
    void setBigDecimalLongTooLow() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        
        BigInteger value = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(new BigDecimal(value, 2)));
    }
    
    @Test
    void setBigDecimalLongNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(null);

        verifySetNull();
    }

    @Test
    void setBigDecimalInt128() throws SQLException {
        fieldDescriptor = createInt128FieldDescriptor(-3);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        final String value = "1234.567";
        field.setBigDecimal(new BigDecimal(value));

        verifySetInt128(new BigInteger("1234567"));
    }

    @Test
    void setBigDecimalInt128TooHigh() throws SQLException {
        fieldDescriptor = createInt128FieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        final String maxValue = "9999999999999999999999999999999999999.99";

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(new BigDecimal(maxValue).add(new BigDecimal("0.01"))));
    }

    @Test
    void setBigDecimalInt128TooLow() throws SQLException {
        fieldDescriptor = createInt128FieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        final String minValue = "-9999999999999999999999999999999999999.99";

        assertThrows(TypeConversionException.class,
                () -> field.setBigDecimal(new BigDecimal(minValue).subtract(new BigDecimal("0.01"))));
    }

    @Test
    void setBigDecimalInt128Null() throws SQLException {
        fieldDescriptor = createInt128FieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigDecimal(null);

        verifySetNull();
    }
    
    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void getBooleanNonNull() throws SQLException {
    }

    @Test
    void getObject_BooleanNull() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Boolean.class), "Expected null for getObject(Boolean.class)");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have created a FBBigDecimalField
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnShortExpectations((short)10);

        assertTrue(field.getObject(Boolean.class), "Expected true from getBoolean");
    }
    
    @Test
    void getBooleanTrue() throws SQLException {
        // NOTE: We could use 0 for the test, but in that case Jaybird would not have created a FBBigDecimalField
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnShortExpectations((short)10);
        
        assertTrue(field.getBoolean(), "Expected true from getBoolean");
    }
    
    @Test
    void getBooleanFalse() throws SQLException {
        // NOTE: We could use scale 0 for the test, but in that case Jaybird would not have created a FBBigDecimalField
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        // NOTE Any value other than 10 would do
        toReturnShortExpectations((short)0);
        
        assertFalse(field.getBoolean(), "Expected false from getBoolean");
    }
    
    @Test
    @Disabled("Ignored in favor of more specific tests")
    @Override
    void setBoolean() throws SQLException {
    }
    
    @Test
    void setBooleanTrue() throws SQLException {
        // NOTE: We could use scale 0 for the test, but in that case Jaybird would not have created a FBBigDecimalField
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBoolean(true);

        verifySetShort((short) 10);
    }
    
    @Test
    void setBooleanFalse() throws SQLException {
        // NOTE: We could use scale 0 for the test, but in that case Jaybird would not have create a FBBigDecimalField
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBoolean(false);

        verifySetShort((short) 0);
    }
    
    @Test
    void getByteNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0, field.getByte(), "Expected getByte() to return 0 for NULL value");
    }

    @Test
    void getObject_ByteNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Byte.class), "Expected getObject(Byte.class) to return null for NULL value");
    }

    @Test
    @Override
    void getByteNonNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(Byte.MIN_VALUE * 100);
        
        assertEquals(Byte.MIN_VALUE, field.getByte(), "Unexpected value for getByte()");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(Byte.MIN_VALUE * 100);

        assertEquals(Byte.valueOf(Byte.MIN_VALUE), field.getObject(Byte.class),
                "Unexpected value for getObject(Byte.class)");
    }
    
    @Test
    void getByteTooHigh() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations((Byte.MAX_VALUE + 1)* 100);

        assertThrows(TypeConversionException.class, field::getByte);
    }
    
    @Test
    void getByteTooLow() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations((Byte.MIN_VALUE - 1)* 100);

        assertThrows(TypeConversionException.class, field::getByte);
    }
    
    @Test
    void setByte() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-7);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setByte((byte) -34);

        verifySetLong(-340000000L);
    }
    
    @Test
    void getDoubleNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0.0, field.getDouble(), 0.0, "Expected getDouble() to return 0.0 for NULL value");
    }

    @Test
    void getObject_DoubleNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Double.class), "Expected getObject(Double.class) to return null for NUL value");
    }
    
    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MIN_VALUE);
        
        assertEquals(Long.MIN_VALUE / 100.0, field.getDouble(), 0.0, "Unexpected value for getDouble()");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MIN_VALUE);

        assertEquals(Long.MIN_VALUE / 100.0, field.getObject(Double.class), 0.0,
                "Unexpected value for getObject(Double.class)");
    }
    
    @Test
    void setDouble() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setDouble(469.1234567);

        verifySetShort((short) 4691);
    }
    
    @Test
    void getFloatNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0.0, field.getFloat(), 0.0, "Expected getFloat() to return 0.0 for NULL value");
    }

    @Test
    void getObject_FloatNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Float.class), "Expected getObject(Float.class) to return null for NUL value");
    }
    
    @Test
    @Override
    void getFloatNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MAX_VALUE);
        
        assertEquals(Long.MAX_VALUE / 100.0f, field.getFloat(), 0.0, "Unexpected value for getFloat()");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE / 100.0f, field.getObject(Float.class), 0.0,
                "Unexpected value for getObject(Float.class)");
    }
    
    @Test
    @Override
    void setFloat() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-5);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setFloat(469.1234567f);

        verifySetLong(46912344L);
    }
    
    @Test
    void getIntNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0, field.getInt(), "Expected getInt() to return 0 for NULL value");
    }

    @Test
    void getObject_IntegerNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Integer.class), "Expected getObject(Integer.class) to return null for NUL value");
    }
    
    @Test
    @Override
    void getIntNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(987654321098765L);
        
        assertEquals(987654321, field.getInt(), "Unexpected value from getInt()");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-6);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(987654321098765L);

        assertEquals(987654321, (int) field.getObject(Integer.class), "Unexpected value from getInt()");
    }
    
    @Test
    void getIntTooHigh() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations((Integer.MAX_VALUE + 1L) * 100);

        assertThrows(TypeConversionException.class, field::getInt);
    }
    
    @Test
    void getIntTooLow() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations((Integer.MIN_VALUE - 1L) * 100);

        assertThrows(TypeConversionException.class, () -> field.getInt());
    }
    
    @Test
    @Override
    void setInteger() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setInteger(123456);

        verifySetInteger(1234560);
    }
    
    @Test
    void getLongNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0, field.getLong(), "Expected getLong() to return 0 for NULL value");
    }

    @Test
    void getObject_LongNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Long.class), "Expected getObject(Long.class) to return null for NUL value");
    }
    
    @Test
    @Override
    void getLongNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MAX_VALUE);
        
        assertEquals(Long.MAX_VALUE / 100, field.getLong(), "Unexpected value from getLong()");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE / 100, (long) field.getObject(Long.class), "Unexpected value from getLong()");
    }
    
    @Test
    @Override
    void setLong() throws SQLException {
        fieldDescriptor = createShortFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setLong(35);

        verifySetShort((short) 3500);
    }
    
    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-8);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(51300000000L);
        
        BigDecimal expectedValue = new BigDecimal("513.00000000");
        assertEquals(expectedValue, field.getObject(), "Unexpected value for long BigDecimal");
    }
    
    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-3);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setObject(new BigDecimal("1234.567"));

        verifySetInteger(1234567);
    }
    
    // TODO Add tests for other object types
    
    @Test
    void getShortNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertEquals(0, field.getShort(), "Expected getShort() to return 0 for NULL value");
    }

    @Test
    void getObject_ShortNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(Short.class), "Expected getObject(Short.class) to return null for NUL value");
    }
    
    @Test
    @Override
    void getShortNonNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-4);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(123456789);
        
        assertEquals(12345, field.getShort(), "Unexpected value from getShort()");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-4);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations(123456789);

        assertEquals(12345, (short) field.getObject(Short.class), "Unexpected value from getShort()");
    }
    
    @Test
    void getShortTooHigh() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations((Short.MAX_VALUE + 1) * 100);

        assertThrows(TypeConversionException.class, field::getShort);
    }
    
    @Test
    void getShortTooLow() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnIntegerExpectations((Short.MIN_VALUE - 1) * 100);

        assertThrows(TypeConversionException.class, field::getShort);
    }
    
    @Test
    @Override
    void setShort() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-3);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setShort(Short.MIN_VALUE);

        verifySetLong(Short.MIN_VALUE * 1000L);
    }
    
    @Test
    void getStringNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();
        
        assertNull(field.getString());
    }

    @Test
    void getObject_StringNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getObject(String.class));
    }
    
    @Test
    @Override
    void getStringNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(456789123);
        
        assertEquals("4567891.23", field.getString(), "Unexpected value from getString()");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(456789123);

        assertEquals("4567891.23", field.getObject(String.class), "Unexpected value from getString()");
    }
    
    @Test
    @Override
    void setStringNonNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setString("78912.3456");

        verifySetInteger(789123);
    }
    
    @Test
    void setStringNull() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        
        field.setString(null);

        verifySetNull();
    }
    
    @Test
    void setStringNonNumber() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-1);
        FBBigDecimalField field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class, () -> field.setString("NotANumber"));
    }

    @Test
    @Override
    void getObject_BigInteger() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(456789123);

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
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setObject(BigInteger.TEN);

        verifySetLong(10);
    }

    @Test
    void setObject_BigInteger_MAX() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setObject(BigInteger.valueOf(Long.MAX_VALUE));

        verifySetLong(Long.MAX_VALUE);
    }

    @Test
    void setObject_BigInteger_MAX_plus_1() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
    }

    @Test
    void setObject_BigInteger_MIN() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setObject(BigInteger.valueOf(Long.MIN_VALUE));

        verifySetLong(Long.MIN_VALUE);
    }

    @Test
    void setObject_BigInteger_MIN_minus_1() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        assertThrows(TypeConversionException.class,
                () -> field.setObject(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    @Test
    void setBigInteger_null() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(0);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setBigInteger(null);

        verifySetNull();
    }

    @Test
    void getDecimal_null() throws SQLException {
        fieldDescriptor = createIntegerFieldDescriptor(-2);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnNullExpectations();

        assertNull(field.getDecimal(), "Expected null result");
    }

    @Test
    @Override
    void getDecimalNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-8);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);
        toReturnLongExpectations(51300000000L);

        Decimal128 expectedValue = Decimal128.valueOf("513.00000000");
        assertEquals(expectedValue, field.getDecimal(), "Unexpected value for long Decimal");
    }

    @Test
    void setDecimal_null() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-8);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setDecimal(null);

        verifySetNull();
    }

    @Test
    @Override
    void setDecimalNonNull() throws SQLException {
        fieldDescriptor = createLongFieldDescriptor(-5);
        field = new FBBigDecimalField(fieldDescriptor, fieldData, Types.NUMERIC);

        field.setDecimal(Decimal128.valueOf("12345678.90123"));

        verifySetLong(1234567890123L);
    }
    
    @Test
    void constructWithUnsupportedSqlType() {
        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        assertThrows(SQLException.class, () -> new FBBigDecimalField(fieldDescriptor, fieldData, Types.VARCHAR));
    }

    private FieldDescriptor createShortFieldDescriptor(int scale) {
        return new RowDescriptorBuilder(1, datatypeCoder)
                .setType(ISCConstants.SQL_SHORT)
                .setScale(scale)
                .toFieldDescriptor();
    }

    private FieldDescriptor createIntegerFieldDescriptor(int scale) {
        return new RowDescriptorBuilder(1, datatypeCoder)
                .setType(ISCConstants.SQL_LONG)
                .setScale(scale)
                .toFieldDescriptor();
    }

    private FieldDescriptor createLongFieldDescriptor(int scale) {
        return new RowDescriptorBuilder(1, datatypeCoder)
                .setType(ISCConstants.SQL_INT64)
                .setScale(scale)
                .toFieldDescriptor();
    }

    private FieldDescriptor createInt128FieldDescriptor(int scale) {
        return new RowDescriptorBuilder(1, datatypeCoder)
                .setType(ISCConstants.SQL_INT128)
                .setScale(scale)
                .toFieldDescriptor();
    }

    @Override
    BigDecimal getNonNullObject() {
        return BigDecimal.ONE;
    }
}
