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
package org.firebirdsql.gds.ng.tz;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.*;

import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TimeZoneDatatypeCoderTest {

    private static final String TIMESTAMPTZ = "2019-03-09T07:45:51+01:00";
    private static final OffsetDateTime TIMESTAMPTZ_OFFSETDATETIME = OffsetDateTime.parse(TIMESTAMPTZ);
    // Defined using offset
    private static final String TIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB";
    private static final String TIMESTAMPTZ_OFFSET_LE_HEX = "B7E40000F0AA830EDB050000";
    private static final String TIMESTAMPTZ_OFFSET_BE_HEX = "0000E4B70E83AAF005DB0000";
    // Defined using Europe/Amsterdam
    private static final String TIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE49";
    private static final String TIMESTAMPTZ_ZONE_LE_HEX = "B7E40000F0AA830E49FE0000";
    private static final String TIMESTAMPTZ_ZONE_BE_HEX = "0000E4B70E83AAF0FE490000";
    private static final String TIMETZ = "07:45:51+01:00";
    private static final OffsetTime TIMETZ_OFFSETTIME = OffsetTime.parse(TIMETZ);
    // Defined using offset
    private static final String TIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB";
    private static final String TIMETZ_OFFSET_LE_HEX = "F0AA830EDB050000";
    private static final String TIMETZ_OFFSET_BE_HEX = "0E83AAF005DB0000";
    // Defined using Europe/Amsterdam (note: offset is date-sensitive)
    private static final String TIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE49";
    private static final String TIMETZ_ZONE_LE_HEX = "F0AA830E49FE0000";
    private static final String TIMETZ_ZONE_BE_HEX = "0E83AAF0FE490000";

    @Test
    public void decodeTimestampTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeTimestampTz_zone_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_ZONE_NETWORK_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeTimestampTz_offset_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeTimestampTz_zone_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_ZONE_LE_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeTimestampTz_offset_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeTimestampTz_zone_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_ZONE_BE_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void encodeTimestampTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX), encoded);
    }

    @Test
    public void encodeTimestampTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeTimestampTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX), encoded);
    }

    @Test
    public void decodeTimeTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();
        OffsetTime expectedOffsetTime = getExpectedOffsetTimeForZone();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_NETWORK_HEX));

        assertEquals(expectedOffsetTime, offsetTime);
    }

    @Test
    public void decodeTimeTz_offset_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_LE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();
        OffsetTime expectedOffsetTime = getExpectedOffsetTimeForZone();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_LE_HEX));

        assertEquals(expectedOffsetTime, offsetTime);
    }

    @Test
    public void decodeTimeTz_offset_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_BE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();
        OffsetTime expectedOffsetTime = getExpectedOffsetTimeForZone();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_BE_HEX));

        assertEquals(expectedOffsetTime, offsetTime);
    }

    @Test
    public void encodeTimeTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_NETWORK_HEX), encoded);
    }

    @Test
    public void encodeTimeTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeTimeTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_BE_HEX), encoded);
    }

    private TimeZoneDatatypeCoder getDefaultTzCoder() {
        DatatypeCoder datatypeCoder = DefaultDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    private TimeZoneDatatypeCoder getLittleEndianTzCoder() {
        DatatypeCoder datatypeCoder = LittleEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    private TimeZoneDatatypeCoder getBigEndianTzCoder() {
        DatatypeCoder datatypeCoder = BigEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    private OffsetTime getExpectedOffsetTimeForZone() {
        LocalTime expectedTime = TIMETZ_OFFSETTIME.toLocalTime();
        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("Europe/Amsterdam"));
        return ZonedDateTime
                .of(today.toLocalDate(), expectedTime, ZoneId.of("Europe/Amsterdam"))
                .toOffsetDateTime()
                .toOffsetTime();
    }

}