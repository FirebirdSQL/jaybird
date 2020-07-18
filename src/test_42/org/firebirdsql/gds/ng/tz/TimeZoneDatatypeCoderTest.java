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
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.Assert.*;

public class TimeZoneDatatypeCoderTest {

    private static final String TIMESTAMPTZ = "2019-03-09T07:45:51+01:00";
    private static final OffsetDateTime TIMESTAMPTZ_OFFSETDATETIME = OffsetDateTime.parse(TIMESTAMPTZ);
    // Defined using offset
    private static final String TIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB";
    private static final String TIMESTAMPTZ_OFFSET_LE_HEX = "B7E40000F0AA830EDB050000";
    private static final String TIMESTAMPTZ_OFFSET_BE_HEX = "0000E4B70E83AAF005DB0000";
    private static final String EXTIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB0000003C";
    private static final String EXTIMESTAMPTZ_OFFSET_NETWORK_HEX_ENCODED = "0000E4B70E83AAF0000005DB00000000";
    private static final String EXTIMESTAMPTZ_OFFSET_LE_HEX = "B7E40000F0AA830EDB053C00";
    private static final String EXTIMESTAMPTZ_OFFSET_BE_HEX = "0000E4B70E83AAF005DB003C";
    // Defined using Europe/Amsterdam
    private static final String TIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE49";
    private static final String TIMESTAMPTZ_ZONE_LE_HEX = "B7E40000F0AA830E49FE0000";
    private static final String TIMESTAMPTZ_ZONE_BE_HEX = "0000E4B70E83AAF0FE490000";
    private static final String EXTIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE490000003C";
    private static final String EXTIMESTAMPTZ_ZONE_LE_HEX = "B7E40000F0AA830E49FE3C00";
    private static final String EXTIMESTAMPTZ_ZONE_BE_HEX = "0000E4B70E83AAF0FE49003C";
    private static final String TIMETZ = "07:45:51+01:00";
    private static final OffsetTime TIMETZ_OFFSETTIME = OffsetTime.parse(TIMETZ);
    // Defined using offset
    private static final String TIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB";
    private static final String TIMETZ_OFFSET_LE_HEX = "F0AA830EDB050000";
    private static final String TIMETZ_OFFSET_BE_HEX = "0E83AAF005DB0000";
    private static final String EXTIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB0000003C";
    private static final String EXTIMETZ_OFFSET_NETWORK_HEX_ENCODED = "0E83AAF0000005DB00000000";
    private static final String EXTIMETZ_OFFSET_LE_HEX = "F0AA830EDB053C00";
    private static final String EXTIMETZ_OFFSET_BE_HEX = "0E83AAF005DB003C";
    // Defined using Europe/Amsterdam (note: offset is date-sensitive)
    private static final String TIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE49";
    private static final String TIMETZ_ZONE_LE_HEX = "F0AA830E49FE0000";
    private static final String TIMETZ_ZONE_BE_HEX = "0E83AAF0FE490000";
    private static final String EXTIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE490000003C";
    private static final String EXTIMETZ_ZONE_LE_HEX = "F0AA830E49FE3C00";
    private static final String EXTIMETZ_ZONE_BE_HEX = "0E83AAF0FE49003C";

    @Test
    public void decodeTimestampTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeTimestampTz(
                fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void decodeExTimestampTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX));

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
    public void decodeExTimestampTz_zone_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_ZONE_NETWORK_HEX));

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
    public void decodeExTimestampTz_offset_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_OFFSET_LE_HEX));

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
    public void decodeExTimestampTz_zone_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_ZONE_LE_HEX));

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
    public void decodeExTimestampTz_offset_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_OFFSET_BE_HEX));

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
    public void decodeExTimestampTz_zone_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetDateTime offsetDateTime = timeZoneDatatypeCoder.decodeExTimestampTz(
                fromHexString(EXTIMESTAMPTZ_ZONE_BE_HEX));

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public void encodeTimestampTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX), encoded);
    }

    @Test
    public void encodeExTimestampTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX_ENCODED), encoded);
    }

    @Test
    public void encodeTimestampTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeExTimestampTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        // Encoded result is identical to that for encodeTimestampTz (secondary offset is zeroed)
        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeTimestampTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX), encoded);
    }

    @Test
    public void encodeExTimestampTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimestampTz(TIMESTAMPTZ_OFFSETDATETIME);

        // Encoded result is identical to that for encodeTimestampTz (secondary offset is zeroed)
        assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX), encoded);
    }

    @Test
    public void decodeTimeTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_offset_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_NETWORK_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_zone_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_ZONE_NETWORK_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_offset_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_LE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_offset_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_OFFSET_LE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_LE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_zone_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_ZONE_LE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_offset_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_OFFSET_BE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_offset_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_OFFSET_BE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeTimeTz_zone_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeTimeTz(fromHexString(TIMETZ_ZONE_BE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void decodeExTimeTz_zone_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        OffsetTime offsetTime = timeZoneDatatypeCoder.decodeExTimeTz(fromHexString(EXTIMETZ_ZONE_BE_HEX));

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void encodeTimeTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_NETWORK_HEX), encoded);
    }

    @Test
    public void encodeExTimeTz_network() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getDefaultTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX_ENCODED), encoded);
    }

    @Test
    public void encodeTimeTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeExTimeTz_littleEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getLittleEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimeTz(TIMETZ_OFFSETTIME);

        // Encoded result is identical to that for encodeTimestampTz (secondary offset is zeroed)
        assertArrayEquals(fromHexString(TIMETZ_OFFSET_LE_HEX), encoded);
    }

    @Test
    public void encodeTimeTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeTimeTz(TIMETZ_OFFSETTIME);

        assertArrayEquals(fromHexString(TIMETZ_OFFSET_BE_HEX), encoded);
    }

    @Test
    public void encodeExTimeTz_bigEndian() {
        TimeZoneDatatypeCoder timeZoneDatatypeCoder = getBigEndianTzCoder();

        byte[] encoded = timeZoneDatatypeCoder.encodeExTimeTz(TIMETZ_OFFSETTIME);

        // Encoded result is identical to that for encodeTimestampTz (secondary offset is zeroed)
        assertArrayEquals(fromHexString(TIMETZ_OFFSET_BE_HEX), encoded);
    }

    @Test
    public void getTimeZoneCodec_default() throws Exception {
        // NOTE Only testing for default datatype coder, this is about testing selection of right type
        for (int baseType : new int[] { SQL_TIMESTAMP_TZ, SQL_TIME_TZ }) {
            for (int type : new int[] { baseType, baseType | 1 }) {
                FieldDescriptor descriptor =
                        new FieldDescriptor(0, getDefaultDataTypeCoder(), type, 0, 0, 0, null, null, null, null, null);
                TimeZoneDatatypeCoder.TimeZoneCodec codec = getDefaultTzCoder().getTimeZoneCodecFor(descriptor);

                assertEquals(TIMESTAMPTZ_OFFSETDATETIME,
                        codec.decodeOffsetDateTime(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX)));
                assertEquals(TIMETZ_OFFSETTIME, codec.decodeOffsetTime(fromHexString(TIMETZ_OFFSET_NETWORK_HEX)));
                assertArrayEquals(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX),
                        codec.encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME));
                assertArrayEquals(fromHexString(TIMETZ_OFFSET_NETWORK_HEX), codec.encodeOffsetTime(TIMETZ_OFFSETTIME));
            }
        }
    }

    @Test
    public void getTimeZoneCodec_extended() throws Exception {
        // NOTE Only testing for default datatype coder, this is about testing selection of right type
        for (int baseType : new int[] { SQL_TIMESTAMP_TZ_EX, SQL_TIME_TZ_EX }) {
            for (int type : new int[] { baseType, baseType | 1 }) {
                FieldDescriptor descriptor =
                        new FieldDescriptor(0, getDefaultDataTypeCoder(), type, 0, 0, 0, null, null, null, null, null);
                TimeZoneDatatypeCoder.TimeZoneCodec codec = getDefaultTzCoder().getTimeZoneCodecFor(descriptor);

                assertEquals(TIMESTAMPTZ_OFFSETDATETIME,
                        codec.decodeOffsetDateTime(fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX)));
                assertEquals(TIMETZ_OFFSETTIME, codec.decodeOffsetTime(fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX)));
                assertArrayEquals(fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX_ENCODED),
                        codec.encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME));
                assertArrayEquals(fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX_ENCODED),
                        codec.encodeOffsetTime(TIMETZ_OFFSETTIME));
            }
        }
    }

    @Test
    public void getTimeZoneCode_nonTimeZoneType_throwsSQLException() {
        for (int baseType : new int[] { SQL_TEXT, SQL_VARYING, SQL_SHORT, SQL_LONG, SQL_FLOAT, SQL_DOUBLE,
                SQL_TIMESTAMP, SQL_BLOB, SQL_TYPE_TIME, SQL_TYPE_DATE, SQL_INT64, SQL_INT128, SQL_DEC16, SQL_DEC34,
                SQL_BOOLEAN }) {
            for (int type : new int[] { baseType, baseType | 1 }) {
                FieldDescriptor descriptor =
                        new FieldDescriptor(0, getDefaultDataTypeCoder(), type, 0, 0, 0, null, null, null, null, null);
                try {
                    getDefaultTzCoder().getTimeZoneCodecFor(descriptor);
                    fail("Should have thrown SQLException for type " + type);
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    private TimeZoneDatatypeCoder getDefaultTzCoder() {
        DatatypeCoder datatypeCoder = getDefaultDataTypeCoder();
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    private DefaultDatatypeCoder getDefaultDataTypeCoder() {
        return DefaultDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
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

}