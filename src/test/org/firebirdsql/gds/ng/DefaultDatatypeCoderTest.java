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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.firebirdsql.jaybird.util.FbDatetimeConversion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for internal consistency of encoding and decoding provided by {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * <p>
 * These tests do not contact a Firebird server (so they don't test the actual correctness/compatibility with Firebird).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class DefaultDatatypeCoderTest {

    private final DefaultDatatypeCoder datatypeCoder =
            new DefaultDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Test
    void testShort() {
        final short testShort = 513;
        final byte[] shortBytes = datatypeCoder.encodeShort(testShort);

        final short result = datatypeCoder.decodeShort(shortBytes);

        assertEquals(testShort, result, "Unexpected short");
    }

    @Test
    void testDecodeShort_null() {
        assertEquals(0, datatypeCoder.decodeShort(null));
    }

    @Test
    void testShortWithOffset() {
        final short testShort = 513;
        byte[] target = new byte[8];

        datatypeCoder.encodeShort(testShort, target, 2);

        final short result = datatypeCoder.decodeShort(target, 2);

        assertEquals(testShort, result, "Unexpected short");
        assertEquals(0, target[0], "First byte should not be set");
        assertEquals(0, target[1], "Second byte should not be set");
    }

    @Test
    void testInt() {
        final int testInt = -1405525771;
        final byte[] intBytes = datatypeCoder.encodeInt(testInt);

        final int result = datatypeCoder.decodeInt(intBytes);

        assertEquals(testInt, result, "Unexpected int");
    }

    @Test
    void testDecodeInt_null() {
        assertEquals(0, datatypeCoder.decodeInt(null));
    }

    @Test
    void testIntWithOffset() {
        final int testInt = -1405525771;
        byte[] target = new byte[8];

        datatypeCoder.encodeInt(testInt, target, 2);

        final int result = datatypeCoder.decodeInt(target, 2);

        assertEquals(testInt, result, "Unexpected short");
        assertEquals(0, target[0], "First byte should not be set");
        assertEquals(0, target[1], "Second byte should not be set");
    }

    @Test
    void testLong() {
        final long testLong = Long.MAX_VALUE ^ ((132L << 56) + 513);
        final byte[] longBytes = datatypeCoder.encodeLong(testLong);

        final long result = datatypeCoder.decodeLong(longBytes);

        assertEquals(testLong, result, "Unexpected long");
    }

    @Test
    void testDecodeLong_null() {
        assertEquals(0L, datatypeCoder.decodeLong(null));
    }

    // Skip testing encode/decodeFloat as it is same as testing encode/decodeInt + JDK implementation of Float.floatToIntBits/intBitsToFloat

    @Test
    void testDecodeFloat_null() {
        assertEquals(0f, datatypeCoder.decodeFloat(null));
    }

    // Skip testing encode/decodeDouble as it is same as testing encode/decodeLong + JDK implementation of Double.doubleToLongBits/longBitsToDouble

    @Test
    void testDecodeDouble_null() {
        assertEquals(0d, datatypeCoder.decodeDouble(null));
    }

    // Skipping string encoding

    @Test
    void testEncodeString_null() {
        assertNull(datatypeCoder.encodeString(null));
    }

    @Test
    void testDecodeString_null() {
        assertNull(datatypeCoder.decodeString(null));
    }

    @Test
    void testLocalDateTime() {
        LocalDateTime testTimestamp = LocalDateTime.parse("2021-10-31T17:23:01.1234");
        byte[] timestampBytes = datatypeCoder.encodeLocalDateTime(testTimestamp);

        LocalDateTime result = datatypeCoder.decodeLocalDateTime(timestampBytes);

        assertEquals(testTimestamp, result, "Unexpected timestamp");
    }

    @Test
    void testEncodeLocalDateTime_null() {
        assertNull(datatypeCoder.encodeLocalDateTime(null));
    }

    @Test
    void testDecodeLocalDateTime_null() {
        assertNull(datatypeCoder.decodeLocalDateTime(null));
    }

    @Test
    void testLocalTime() {
        LocalTime testTime = LocalTime.parse("17:23:01");
        byte[] timeBytes = datatypeCoder.encodeLocalTime(testTime);

        LocalTime result = datatypeCoder.decodeLocalTime(timeBytes);

        assertEquals(testTime, result, "Unexpected time");
    }

    @Test
    void testEncodeLocalTime_null() {
        assertNull(datatypeCoder.encodeLocalTime(null));
    }

    @Test
    void testDecodeLocalTime_null() {
        assertNull(datatypeCoder.decodeLocalTime(null));
    }

    @Test
    void testLocalDate() {
        LocalDate testDate = LocalDate.parse("2014-03-29");
        byte[] dateBytes = datatypeCoder.encodeLocalDate(testDate);

        LocalDate result = datatypeCoder.decodeLocalDate(dateBytes);

        assertEquals(testDate, result, "Unexpected date");
    }

    @Test
    void testEncodeLocalDate_null() {
        assertNull(datatypeCoder.encodeLocalDate(null));
    }

    @Test
    void testDecodeLocalDate_null() {
        assertNull(datatypeCoder.decodeLocalDate(null));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testBoolean(final boolean testBoolean) {
        final byte[] booleanBytes = datatypeCoder.encodeBoolean(testBoolean);

        final boolean result = datatypeCoder.decodeBoolean(booleanBytes);

        assertEquals(testBoolean, result, "Unexpected boolean");
    }

    @Test
    void testDecodeBoolean_null() {
        assertFalse(datatypeCoder.decodeBoolean(null));
    }
    
    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalDateTime(LocalDateTime)} using
     * {@link DefaultDatatypeCoder#decodeLocalDateTime(byte[])}.
     */
    @Test
    void testLocalDateTimeToTimestamp() {
        final LocalDateTime expected = FbDatetimeConversion.parseSqlTimestamp("2013-03-29 17:43:01.9751");
        final byte[] localDateTimeBytes = datatypeCoder.encodeLocalDateTime(expected);

        final LocalDateTime result = datatypeCoder.decodeLocalDateTime(localDateTimeBytes);

        assertEquals(expected, result, "Unexpected local date time");
    }

    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalDate(LocalDate)} using
     * {@link DefaultDatatypeCoder#decodeLocalDate(byte[])}.
     */
    @Test
    void testLocalDateToDate() {
        final LocalDate expected = LocalDate.parse("2014-03-29");
        final byte[] localDateBytes = datatypeCoder.encodeLocalDate(expected);

        final LocalDate result = datatypeCoder.decodeLocalDate(localDateBytes);

        assertEquals(expected, result, "Unexpected local date");
    }

    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalTime(LocalTime) using
     * {@link DefaultDatatypeCoder#decodeLocalTime(byte[])}
     */
    @Test
    void testLocalTimeToTimestamp() {
        final LocalTime expected = LocalTime.parse("17:43:01.9751");
        final byte[] localTimeBytes = datatypeCoder.encodeLocalTime(expected);

        final LocalTime result = datatypeCoder.decodeLocalTime(localTimeBytes);

        assertEquals(expected, result, "Unexpected local time");
    }

    /**
     * Checks cache maintenance implementation (warning: reflection ties this to the implementation)
     */
    @Test
    public void testCacheOfEncodingSpecificDatatypeCoders() throws Exception {
        Field cacheField = DefaultDatatypeCoder.class.getDeclaredField("encodingSpecificDatatypeCoders");
        cacheField.setAccessible(true);

        assertEquals(0, ((Map<?, ?>) cacheField.get(datatypeCoder)).size(), "Cache size at start");

        List<String> encodingsToTry = Arrays.asList("ISO8859_1", "ISO8859_2", "ISO8859_3", "ISO8859_4", "ISO8859_5",
                "ISO8859_6", "ISO8859_7", "ISO8859_8", "ISO8859_9", "DOS437");
        String additionalEncoding = "DOS850";
        Map<String, DatatypeCoder> retrievedDatatypeCoders = new HashMap<>(encodingsToTry.size() + 1);

        IEncodingFactory encodingFactory = datatypeCoder.getEncodingFactory();
        // prime cache
        for (String encoding : encodingsToTry) {
            DatatypeCoder encodingSpecificDatatypeCoder =
                    datatypeCoder.forEncodingDefinition(encodingFactory.getEncodingDefinitionByFirebirdName(encoding));
            retrievedDatatypeCoders.put(encoding, encodingSpecificDatatypeCoder);
        }

        assertEquals(encodingsToTry.size(), ((Map<?, ?>) cacheField.get(datatypeCoder)).size(),
                "Cache size after adding items");

        // check cache
        for (String encoding : encodingsToTry) {
            DatatypeCoder encodingSpecificDatatypeCoder =
                    datatypeCoder.forEncodingDefinition(encodingFactory.getEncodingDefinitionByFirebirdName(encoding));
            assertSame(retrievedDatatypeCoders.get(encoding), encodingSpecificDatatypeCoder,
                    "Unexpected instance for " + encoding);
        }

        // Overflow cache to trigger clean up
        DatatypeCoder additionalDatatypeCoder = datatypeCoder.forEncodingDefinition(
                encodingFactory.getEncodingDefinitionByFirebirdName(additionalEncoding));
        assertNotNull(additionalDatatypeCoder);

        assertEquals(0, ((Map<?, ?>) cacheField.get(datatypeCoder)).size(), "Cache size after overflow");
    }

    @Test
    void decodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertEquals(decimal64, datatypeCoder.decodeDecimal64(bytes));
    }

    @Test
    void decodeDecimal64_null() {
        assertNull(datatypeCoder.decodeDecimal64(null));
    }

    @Test
    void encodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal64(decimal64));
    }

    @Test
    void encodeDecimal64_null() {
        assertNull(datatypeCoder.encodeDecimal64(null));
    }

    @Test
    void decodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertEquals(decimal128, datatypeCoder.decodeDecimal128(bytes));
    }

    @Test
    void decodeDecimal128_null() {
        assertNull(datatypeCoder.decodeDecimal128(null));
    }

    @Test
    void encodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal128(decimal128));
    }

    @Test
    void encodeDecimal128_null() {
        assertNull(datatypeCoder.encodeDecimal128(null));
    }

    @Test
    void decodeInt128_null() {
        assertNull(datatypeCoder.decodeInt128(null));
    }

    @Test
    void encodeInt128_null() {
        assertNull(datatypeCoder.encodeInt128(null));
    }

}
