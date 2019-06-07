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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for internal consistency of encoding and decoding provided by {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * <p>
 * These tests do not contact a Firebird server (so they don't test the actual correctness/compatibility with Firebird).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DefaultDatatypeCoderTest {

    private final DefaultDatatypeCoder datatypeCoder =
            new DefaultDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Test
    public void testShort() {
        final short testShort = 513;
        final byte[] shortBytes = datatypeCoder.encodeShort(testShort);

        final short result = datatypeCoder.decodeShort(shortBytes);

        assertEquals("Unexpected short", testShort, result);
    }

    @Test
    public void testShortWithOffset() {
        final short testShort = 513;
        byte[] target = new byte[8];

        datatypeCoder.encodeShort(testShort, target, 2);

        final short result = datatypeCoder.decodeShort(target, 2);

        assertEquals("Unexpected short", testShort, result);
        assertEquals("First byte should not be set", 0, target[0]);
        assertEquals("Second byte should not be set", 0, target[0]);
    }

    @Test
    public void testInt() {
        final int testInt = -1405525771;
        final byte[] intBytes = datatypeCoder.encodeInt(testInt);

        final int result = datatypeCoder.decodeInt(intBytes);

        assertEquals("Unexpected int", testInt, result);
    }

    @Test
    public void testIntWithOffset() {
        final int testInt = -1405525771;
        byte[] target = new byte[8];

        datatypeCoder.encodeInt(testInt, target, 2);

        final int result = datatypeCoder.decodeInt(target, 2);

        assertEquals("Unexpected short", testInt, result);
        assertEquals("First byte should not be set", 0, target[0]);
        assertEquals("Second byte should not be set", 0, target[0]);
    }

    @Test
    public void testLong() {
        final long testLong = Long.MAX_VALUE ^ ((132L << 56) + 513);
        final byte[] longBytes = datatypeCoder.encodeLong(testLong);

        final long result = datatypeCoder.decodeLong(longBytes);

        assertEquals("Unexpected long", testLong, result);
    }

    // Skip testing encode/decodeFloat as it is same as testing encode/decodeInt + JDK implementation of Float.floatToIntBits/intBitsToFloat

    // Skip testing encode/decodeDouble as it is same as  testing encode/decodeLong + JDK implementation of Double.doubleToLongBits/longBitsToDouble

    // Skipping string encoding

    @Test
    public void testTimestamp() {
        final java.util.Date date = new java.util.Date();
        final java.sql.Timestamp testTimestamp = new java.sql.Timestamp(date.getTime());
        // Make sure minimum Firebird precision (100 microseconds) is set
        testTimestamp.setNanos((int) TimeUnit.MICROSECONDS.toNanos(975100));
        final byte[] timestampBytes = datatypeCoder.encodeTimestamp(testTimestamp);

        final java.sql.Timestamp result = datatypeCoder.decodeTimestamp(timestampBytes);

        assertEquals("Unexpected timestamp", testTimestamp, result);
    }

    // TODO Tests for various Timestamp methods taking a Calendar

    /**
     * NOTE: {@link java.sql.Time} only supports second precision!
     */
    @Test
    public void testTime() {
        final java.sql.Time testTime = java.sql.Time.valueOf("17:23:01");
        final byte[] timeBytes = datatypeCoder.encodeTime(testTime);

        final java.sql.Time result = datatypeCoder.decodeTime(timeBytes);

        assertEquals("Unexpected time", testTime, result);
    }

    @Test
    public void testDate() {
        final java.sql.Date testDate = java.sql.Date.valueOf("2014-03-29");
        final byte[] dateBytes = datatypeCoder.encodeDate(testDate);

        final java.sql.Date result = datatypeCoder.decodeDate(dateBytes);

        assertEquals("Unexpected date", testDate, result);
    }

    @Test
    public void testBooleanTrue() {
        checkBoolean(true);
    }

    @Test
    public void testBooleanFalse() {
        checkBoolean(false);
    }

    private void checkBoolean(final boolean testBoolean) {
        final byte[] booleanBytes = datatypeCoder.encodeBoolean(testBoolean);

        final boolean result = datatypeCoder.decodeBoolean(booleanBytes);

        assertEquals("Unexpected boolean", testBoolean, result);
    }

    // TODO java.time roundtrip tests

    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalDateTime(int, int, int, int, int, int, int)} using {@link DefaultDatatypeCoder#decodeTimestamp(byte[])}.
     */
    @Test
    public void testLocalDateTimeToTimestamp() {
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2013-03-29 17:43:01.9751");
        final byte[] localDateTimeBytes = datatypeCoder.encodeLocalDateTime(2013, 3, 29, 17, 43, 1, (int) TimeUnit.MICROSECONDS.toNanos(975100));

        final java.sql.Timestamp result = datatypeCoder.decodeTimestamp(localDateTimeBytes);

        assertEquals("Unexpected timestamp", expected, result);
    }

    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalDate(int, int, int)} using {@link DefaultDatatypeCoder#decodeDate(byte[])}.
     */
    @Test
    public void testLocalDateToDate() {
        final java.sql.Date expected = java.sql.Date.valueOf("2014-03-29");
        final byte[] localDateBytes = datatypeCoder.encodeLocalDate(2014, 3, 29);

        final java.sql.Date result = datatypeCoder.decodeDate(localDateBytes);

        assertEquals("Unexpected date", expected, result);
    }

    /**
     * Test round trip for {@link DefaultDatatypeCoder#encodeLocalTime(int, int, int, int) using {@link DefaultDatatypeCoder#decodeTimestamp(byte[])}
     * <p>
     * We test using java.sql.Timestamp so we can check the maximum precision (which is not available through java.sql.Time)
     * </p>
     */
    @Test
    public void testLocalTimeToTimestamp() {
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2014-03-29 17:43:01.9751");
        // We need a date part as well to construct a valid timestamp
        final byte[] localDateBytes = datatypeCoder.encodeLocalDate(2014, 3, 29);
        final byte[] localTimeBytes = datatypeCoder.encodeLocalTime(17, 43, 1, (int) TimeUnit.MICROSECONDS.toNanos(975100));

        final byte[] combinedDateTime = new byte[8];
        System.arraycopy(localDateBytes, 0, combinedDateTime, 0, 4);
        System.arraycopy(localTimeBytes, 0, combinedDateTime, 4, 4);

        final java.sql.Timestamp result = datatypeCoder.decodeTimestamp(combinedDateTime);

        assertEquals("Unexpected timestamp", expected, result);
    }

    /**
     * Test round trip for timestamp conversion timezone.
     */
    @Test
    public void testTimestampRoundtrip() {
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2013-03-29 17:43:01.9751");
        final byte[] dateTimeBytes = datatypeCoder.encodeTimestamp(expected);

        final java.sql.Timestamp result = datatypeCoder.decodeTimestamp(dateTimeBytes);

        assertEquals("Unexpected timestamp", expected, result);
    }

    /**
     * Test round trip for timestamp conversion with timezone.
     */
    @Test
    public void testTimestampRoundtripWithCalendar() {
        // Note we test with the assumption that we are not in timezone America/New_York
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        Calendar calendar = Calendar.getInstance(timeZone);
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2013-03-29 17:43:01.9751");
        final byte[] dateTimeBytes = datatypeCoder.encodeTimestampCalendar(expected, calendar);

        final java.sql.Timestamp result = datatypeCoder.decodeTimestampCalendar(dateTimeBytes, calendar);

        assertEquals("Unexpected timestamp", expected, result);
    }

    @Test
    public void testTimeRoundtrip() {
        final java.sql.Time expected = java.sql.Time.valueOf("17:43:01");
        final byte[] timeBytes = datatypeCoder.encodeTime(expected);

        final java.sql.Time result = datatypeCoder.decodeTime(timeBytes);

        assertEquals("Unexpected timestamp", expected, result);
    }

    @Test
    public void testTimeRoundtripWithCalendar() {
        // Note we test with the assumption that we are not in timezone America/New_York
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        Calendar calendar = Calendar.getInstance(timeZone);
        final java.sql.Time expected = java.sql.Time.valueOf("17:43:01");
        final byte[] timeBytes = datatypeCoder.encodeTimeCalendar(expected, calendar);

        final java.sql.Time result = datatypeCoder.decodeTimeCalendar(timeBytes, calendar);

        assertEquals("Unexpected timestamp", expected, result);
    }

    /**
     * Checks cache maintenance implementation (warning: reflection ties this to the implementation)
     */
    @Test
    public void testCacheOfEncodingSpecificDatatypeCoders() throws Exception {
        Field cacheField = DefaultDatatypeCoder.class.getDeclaredField("encodingSpecificDatatypeCoders");
        cacheField.setAccessible(true);

        assertEquals("Cache size at start", 0, ((Map<?, ?>) cacheField.get(datatypeCoder)).size());

        List<String> encodingsToTry = Arrays.asList("ISO8859_1", "ISO8859_2", "ISO8859_3", "ISO8859_4", "ISO8859_5",
                "ISO8859_6", "ISO8859_7", "ISO8859_8", "ISO8859_9", "DOS437");
        String additionalEncoding = "DOS850";
        assert encodingsToTry.size() == 10 : "Unexected number of encodings";
        Map<String, DatatypeCoder> retrievedDatatypeCoders = new HashMap<>(encodingsToTry.size() + 1);

        IEncodingFactory encodingFactory = datatypeCoder.getEncodingFactory();
        // prime cache
        for (String encoding : encodingsToTry) {
            DatatypeCoder encodingSpecificDatatypeCoder =
                    datatypeCoder.forEncodingDefinition(encodingFactory.getEncodingDefinitionByFirebirdName(encoding));
            retrievedDatatypeCoders.put(encoding, encodingSpecificDatatypeCoder);
        }

        assertEquals("Cache size after adding items", encodingsToTry.size(), ((Map<?, ?>) cacheField.get(datatypeCoder)).size());

        // check cache
        for (String encoding : encodingsToTry) {
            DatatypeCoder encodingSpecificDatatypeCoder =
                    datatypeCoder.forEncodingDefinition(encodingFactory.getEncodingDefinitionByFirebirdName(encoding));
            assertSame("Unexpected instance for " + encoding,
                    retrievedDatatypeCoders.get(encoding), encodingSpecificDatatypeCoder);
        }

        // Overflow cache to trigger clean up
        DatatypeCoder additionalDatatypeCoder = datatypeCoder.forEncodingDefinition(
                encodingFactory.getEncodingDefinitionByFirebirdName(additionalEncoding));
        assertNotNull(additionalDatatypeCoder);

        assertEquals("Cache size after overflow", 0, ((Map<?, ?>) cacheField.get(datatypeCoder)).size());
    }

    @Test
    public void decodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertEquals(decimal64, datatypeCoder.decodeDecimal64(bytes));
    }

    @Test
    public void encodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal64(decimal64));
    }

    @Test
    public void decodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertEquals(decimal128, datatypeCoder.decodeDecimal128(bytes));
    }

    @Test
    public void encodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal128(decimal128));
    }

}
