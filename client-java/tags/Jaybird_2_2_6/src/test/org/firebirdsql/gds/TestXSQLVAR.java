/*
 * $Id$
 * 
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
package org.firebirdsql.gds;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

/**
 * Test for internal consistency of encoding and decoding provided by {@link org.firebirdsql.gds.XSQLVAR}.
 * <p>
 * These tests do not contact a Firebird server (so they don't test the actual correctness/compatibility with Firebird).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2.5
 */
public class TestXSQLVAR extends TestCase {

    private final XSQLVAR xsqlvar = new XSQLVAR();

    public void testShort() {
        final short testShort = 513;
        final byte[] shortBytes = xsqlvar.encodeShort(testShort);

        final short result = xsqlvar.decodeShort(shortBytes);

        assertEquals("Unexpected short", testShort, result);
    }

    public void testInt() {
        final int testInt = -1405525771;
        final byte[] intBytes = xsqlvar.encodeInt(testInt);

        final int result = xsqlvar.decodeInt(intBytes);

        assertEquals("Unexpected int", testInt, result);
    }

    public void testLong() {
        final long testLong = Long.MAX_VALUE ^ ((132L << 56) + 513);
        final byte[] longBytes = xsqlvar.encodeLong(testLong);

        final long result = xsqlvar.decodeLong(longBytes);

        assertEquals("Unexpected long", testLong, result);
    }

    // Skip testing encode/decodeFloat as it is same as testing encode/decodeInt + JDK implementation of Float.floatToIntBits/intBitsToFloat

    // Skip testing encode/decodeDouble as it is same as  testing encode/decodeLong + JDK implementation of Double.doubleToLongBits/longBitsToDouble

    // Skipping string encoding

    public void testTimestamp() {
        final java.util.Date date = new java.util.Date();
        final java.sql.Timestamp testTimestamp = new java.sql.Timestamp(date.getTime());
        // Make sure minimum Firebird precision (100 microseconds) is set
        testTimestamp.setNanos((int) TimeUnit.MICROSECONDS.toNanos(975100));
        final byte[] timestampBytes = xsqlvar.encodeTimestamp(testTimestamp);

        final java.sql.Timestamp result = xsqlvar.decodeTimestamp(timestampBytes);

        assertEquals("Unexpected timestamp", testTimestamp, result);
    }

    // TODO Tests for various Timestamp methods taking a Calendar

    /**
     * NOTE: {@link java.sql.Time} only supports second precision!
     */
    public void testTime() {
        final java.sql.Time testTime = java.sql.Time.valueOf("17:23:01");
        final byte[] timeBytes = xsqlvar.encodeTime(testTime);

        final java.sql.Time result = xsqlvar.decodeTime(timeBytes);

        assertEquals("Unexpected time", testTime, result);
    }

    public void testDate() {
        final java.sql.Date testDate = java.sql.Date.valueOf("2014-03-29");
        final byte[] dateBytes = xsqlvar.encodeDate(testDate);

        final java.sql.Date result = xsqlvar.decodeDate(dateBytes);

        assertEquals("Unexpected date", testDate, result);
    }

    public void testBooleanTrue() {
        checkBoolean(true);
    }

    public void testBooleanFalse() {
        checkBoolean(false);
    }

    private void checkBoolean(final boolean testBoolean) {
        final byte[] booleanBytes = xsqlvar.encodeBoolean(testBoolean);

        final boolean result = xsqlvar.decodeBoolean(booleanBytes);

        assertEquals("Unexpected boolean", testBoolean, result);
    }

    // TODO java.time roundtrip tests

    /**
     * Test roundtrip for {@link XSQLVAR#encodeLocalDateTime(int, int, int, int, int, int, int)} using {@link XSQLVAR#decodeTimestamp(byte[])}
     */
    public void testLocalDateTimeToTimestamp() {
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2013-03-29 17:43:01.9751");
        final byte[] localDateTimeBytes = xsqlvar.encodeLocalDateTime(2013, 3, 29, 17, 43, 1, (int) TimeUnit.MICROSECONDS.toNanos(975100));

        final java.sql.Timestamp result = xsqlvar.decodeTimestamp(localDateTimeBytes);

        assertEquals("Unexpected timestamp", expected, result);
    }

    /**
     * Test round trip for {@link XSQLVAR#encodeLocalDate(int, int, int)} using {@link XSQLVAR#decodeDate(byte[])}.
     */
    public void testLocalDateToDate() {
        final java.sql.Date expected = java.sql.Date.valueOf("2014-03-29");
        final byte[] localDateBytes = xsqlvar.encodeLocalDate(2014, 3, 29);

        final java.sql.Date result = xsqlvar.decodeDate(localDateBytes);

        assertEquals("Unexpected date", expected, result);
    }

    /**
     * Test round trip for {@link XSQLVAR#encodeLocalTime(int, int, int, int) using {@link XSQLVAR#decodeTimestamp(byte[])}
     * <p>
     * We test using java.sql.Timestamp so we can check the maximum precision (which is not available through java.sql.Time)
     * </p>
     */
    public void testLocalTimeToTimestamp() {
        final java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2014-03-29 17:43:01.9751");
        // We need a date part as well to construct a valid timestamp
        final byte[] localDateBytes = xsqlvar.encodeLocalDate(2014, 3, 29);
        final byte[] localTimeBytes = xsqlvar.encodeLocalTime(17, 43, 1, (int) TimeUnit.MICROSECONDS.toNanos(975100));

        final byte[] combinedDateTime = new byte[8];
        System.arraycopy(localDateBytes, 0, combinedDateTime, 0, 4);
        System.arraycopy(localTimeBytes, 0, combinedDateTime, 4, 4);

        final java.sql.Timestamp result = xsqlvar.decodeTimestamp(combinedDateTime);

        assertEquals("Unexpected timestamp", expected, result);
    }
}
