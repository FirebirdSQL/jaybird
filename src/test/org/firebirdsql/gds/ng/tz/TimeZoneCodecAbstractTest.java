// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.tz;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder.TimeZoneCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Common test behaviour for {@link TimeZoneCodec} implementation tests.
 *
 * @author Mark Rotteveel
 */
abstract class TimeZoneCodecAbstractTest {

    static final OffsetDateTime OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09 =
            OffsetDateTime.parse("2019-03-09T07:45:51+01:00");
    static final ZonedDateTime OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09 =
            ZonedDateTime.parse("2019-03-09T07:45:51+01:00");
    static final OffsetTime OFFSET_1_OFFSET_TIME = OffsetTime.parse("07:45:51+01:00");
    static final OffsetDateTime OFFSET_1_OFFSET_DATE_TIME_AT_2019_07_01 =
            OffsetDateTime.parse("2019-07-01T07:45:51+01:00");
    static final ZonedDateTime OFFSET_1_ZONED_DATE_TIME_AT_2019_07_01 =
            ZonedDateTime.parse("2019-07-01T07:45:51+01:00");
    static final OffsetDateTime OFFSET_2_OFFSET_DATE_TIME_AT_2019_07_01 =
            OffsetDateTime.parse("2019-07-01T07:45:51+02:00");
    static final OffsetTime OFFSET_2_OFFSET_TIME = OffsetTime.parse("07:45:51+02:00");
    static final ZonedDateTime ZONE_ZONED_DATE_TIME_AT_2019_03_09 =
            ZonedDateTime.parse("2019-03-09T07:45:51+01:00[Europe/Amsterdam]");
    static final ZonedDateTime ZONE_ZONED_DATE_TIME_AT_2019_07_01 =
            ZonedDateTime.parse("2019-07-01T07:45:51+02:00[Europe/Amsterdam]");

    static final Clock FIXED_AT_2019_03_09 = Clock.fixed(Instant.parse("2019-03-09T12:00:00Z"), ZoneOffset.UTC);
    static final Clock FIXED_AT_2019_07_01 = Clock.fixed(Instant.parse("2019-07-01T12:00:00Z"), ZoneOffset.UTC);

    final int tzType = getTzType();

    abstract int getTzType();

    @Test
    final void decodeOffsetDateTime_offset_network_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getOffsetNetworkAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetDateTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).decodeOffsetDateTime(null));
    }

    @Test
    final void decodeOffsetTime_offset_network_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getOffsetNetworkAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeOffsetTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).decodeOffsetTime(null));
    }

    @Test
    final void decodeZonedDateTime_offset_network_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getOffsetNetworkAt2019_03_09Input());

        assertEquals(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void decodeZonedDateTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).decodeZonedDateTime(null));
    }

    @Test
    final void decodeOffsetDateTime_offset_network_at2019_07_01() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeOffsetDateTime(getOffsetNetworkAt2019_07_01Input());

        assertEquals(getOffsetExpectedOffsetDateTimeAt2019_07_01(), offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_offset_network_at2019_07_01() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeOffsetTime(getOffsetNetworkAt2019_07_01Input());

        assertEquals(getOffsetExpectedOffsetTimeAt2019_07_01(), offsetTime);
    }

    @Test
    final void decodeZonedDateTime_offset_network_at2019_07_01() throws Exception {
        ZonedDateTime zonedDateTimeDateTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeZonedDateTime(getOffsetNetworkAt2019_07_01Input());

        assertEquals(OFFSET_1_ZONED_DATE_TIME_AT_2019_07_01, zonedDateTimeDateTime);
    }

    @Test
    final void decodeOffsetDateTime_zone_network_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getZoneNetworkAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_zone_network_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getZoneNetworkAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeZonedDateTime_zone_network_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getZoneNetworkAt2019_03_09Input());

        assertEquals(ZONE_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void decodeOffsetDateTime_zone_network_at2019_07_01() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeOffsetDateTime(getZoneNetworkAt2019_07_01Input());

        assertEquals(getZoneExpectedOffsetDateTimeAt2019_07_01(), offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_zone_network_at2019_07_01() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeOffsetTime(getZoneNetworkAt2019_07_01Input());

        // Important this is the offset at 2020-01-01, not 2019-07-01!
        assertEquals(getZoneExpectedOffsetTimeAt2019_07_01(), offsetTime);
    }

    @Test
    final void decodeZonedDateTime_zone_network_at2019_07_01() throws Exception {
        ZonedDateTime zonedDateTime = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .decodeZonedDateTime(getZoneNetworkAt2019_07_01Input());

        assertEquals(ZONE_ZONED_DATE_TIME_AT_2019_07_01, zonedDateTime);
    }

    @Test
    final void decodeOffsetDateTime_offset_littleEndian_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getOffsetLeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_offset_littleEndian_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getOffsetLeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeZonedDateTime_offset_littleEndian_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getOffsetLeAt2019_03_09Input());

        assertEquals(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void decodeOffsetDateTime_zone_littleEndian_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getZoneLeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_zone_littleEndian_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getZoneLeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeZonedDateTime_zone_littleEndian_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getZoneLeAt2019_03_09Input());

        assertEquals(ZONE_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void decodeOffsetDateTime_offset_bigEndian_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getOffsetBeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_offset_bigEndian_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getOffsetBeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeZonedDateTime_offset_bigEndian_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getOffsetBeAt2019_03_09Input());

        assertEquals(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void decodeOffsetDateTime_zone_bigEndian_at2019_03_09() throws Exception {
        OffsetDateTime offsetDateTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetDateTime(getZoneBeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09, offsetDateTime);
    }

    @Test
    final void decodeOffsetTime_zone_bigEndian_at2019_03_09() throws Exception {
        OffsetTime offsetTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeOffsetTime(getZoneBeAt2019_03_09Input());

        assertEquals(OFFSET_1_OFFSET_TIME, offsetTime);
    }

    @Test
    final void decodeZonedDateTime_zone_bigEndian_at2019_03_09() throws Exception {
        ZonedDateTime zonedDateTime = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .decodeZonedDateTime(getZoneBeAt2019_03_09Input());

        assertEquals(ZONE_ZONED_DATE_TIME_AT_2019_03_09, zonedDateTime);
    }

    @Test
    final void encodeOffsetDateTime_network_at2019_03_09() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetDateTime(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetNetworkAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetDateTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).encodeOffsetDateTime(null));
    }

    @Test
    final void encodeOffsetTime_network_at2019_03_09() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetTime(OFFSET_1_OFFSET_TIME);

        assertArrayEquals(getOffsetNetworkAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).encodeOffsetTime(null));
    }

    @Test
    final void encodeZonedDateTime_offset_network_at2019_03_09() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetNetworkAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_null() throws Exception {
        assertNull(getNetworkCodec(tzType, FIXED_AT_2019_03_09).encodeZonedDateTime(null));
    }

    @Test
    final void encodeZonedDateTime_zone_network_at2019_03_09() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(ZONE_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getZoneNetworkAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetDateTime_network_at2019_07_01() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .encodeOffsetDateTime(OFFSET_1_OFFSET_DATE_TIME_AT_2019_07_01);

        assertArrayEquals(getOffsetNetworkAt2019_07_01Expected(), encoded);
    }

    @Test
    final void encodeOffsetTime_network_at2019_07_01() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .encodeOffsetTime(OFFSET_1_OFFSET_TIME);

        assertArrayEquals(getOffsetNetworkAt2019_07_01Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_offset_network_at2019_07_01() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .encodeZonedDateTime(OFFSET_1_ZONED_DATE_TIME_AT_2019_07_01);

        assertArrayEquals(getOffsetNetworkAt2019_07_01Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_zone_network_at2019_07_01() throws Exception {
        byte[] encoded = getNetworkCodec(tzType, FIXED_AT_2019_07_01)
                .encodeZonedDateTime(ZONE_ZONED_DATE_TIME_AT_2019_07_01);

        assertArrayEquals(getZoneNetworkAt2019_07_01Expected(), encoded);
    }

    @Test
    final void encodeOffsetDateTime_littleEndian_at2019_03_09() throws Exception {
        byte[] encoded = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetDateTime(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetLeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetTime_littleEndian_at2019_03_09() throws Exception {
        byte[] encoded = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetTime(OFFSET_1_OFFSET_TIME);

        assertArrayEquals(getOffsetLeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_offset_littleEndian_at2019_03_09() throws Exception {
        byte[] encoded = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetLeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_zone_littleEndian_at2019_03_09() throws Exception {
        byte[] encoded = getLittleEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(ZONE_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getZoneLeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetDateTime_bigEndian_at2019_03_09() throws Exception {
        byte[] encoded = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetDateTime(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetBeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeOffsetTime_bigEndian_at2019_03_09() throws Exception {
        byte[] encoded = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeOffsetTime(OFFSET_1_OFFSET_TIME);

        assertArrayEquals(getOffsetBeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_offset_bigEndian_at2019_03_09() throws Exception {
        byte[] encoded = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(OFFSET_1_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getOffsetBeAt2019_03_09Expected(), encoded);
    }

    @Test
    final void encodeZonedDateTime_zone_bigEndian_at2019_03_09() throws Exception {
        byte[] encoded = getBigEndianCodec(tzType, FIXED_AT_2019_03_09)
                .encodeZonedDateTime(ZONE_ZONED_DATE_TIME_AT_2019_03_09);

        assertArrayEquals(getZoneBeAt2019_03_09Expected(), encoded);
    }

    @Test
    void getTimeZoneCodec() throws Exception {
        // NOTE Only testing for default datatype coder, this is about testing selection of right type
        for (int type : new int[] { tzType, tzType | 1 }) {
            FieldDescriptor descriptor = rowDescriptorBuilder().setType(type).toFieldDescriptor();
            TimeZoneDatatypeCoder.TimeZoneCodec codec = getDefaultTzCoder(FIXED_AT_2019_03_09)
                    .getTimeZoneCodecFor(descriptor);

            assertEquals(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09,
                    codec.decodeOffsetDateTime(getOffsetNetworkAt2019_03_09Input()));
            assertArrayEquals(getOffsetNetworkAt2019_03_09Expected(),
                    codec.encodeOffsetDateTime(OFFSET_1_OFFSET_DATE_TIME_AT_2019_03_09));
            assertEquals(OFFSET_1_OFFSET_TIME, codec.decodeOffsetTime(getOffsetNetworkAt2019_03_09Input()));
            assertArrayEquals(getOffsetNetworkAt2019_03_09Expected(), codec.encodeOffsetTime(OFFSET_1_OFFSET_TIME));
        }
    }

    abstract byte[] getOffsetNetworkAt2019_03_09Input();
    abstract byte[] getOffsetNetworkAt2019_03_09Expected();

    abstract byte[] getOffsetNetworkAt2019_07_01Input();
    abstract byte[] getOffsetNetworkAt2019_07_01Expected();
    abstract OffsetDateTime getOffsetExpectedOffsetDateTimeAt2019_07_01();
    abstract OffsetTime getOffsetExpectedOffsetTimeAt2019_07_01();

    abstract byte[] getZoneNetworkAt2019_03_09Input();
    abstract byte[] getZoneNetworkAt2019_03_09Expected();

    abstract byte[] getZoneNetworkAt2019_07_01Input();
    abstract byte[] getZoneNetworkAt2019_07_01Expected();
    abstract OffsetDateTime getZoneExpectedOffsetDateTimeAt2019_07_01();
    abstract OffsetTime getZoneExpectedOffsetTimeAt2019_07_01();

    abstract byte[] getOffsetLeAt2019_03_09Input();
    abstract byte[] getOffsetLeAt2019_03_09Expected();

    abstract byte[] getZoneLeAt2019_03_09Input();
    abstract byte[] getZoneLeAt2019_03_09Expected();

    abstract byte[] getOffsetBeAt2019_03_09Input();
    abstract byte[] getOffsetBeAt2019_03_09Expected();

    abstract byte [] getZoneBeAt2019_03_09Input();
    abstract byte [] getZoneBeAt2019_03_09Expected();

    static RowDescriptorBuilder rowDescriptorBuilder() {
        return new RowDescriptorBuilder(1, getDefaultDataTypeCoder());
    }

    final TimeZoneCodec getNetworkCodec(int type, Clock clock) throws SQLException {
        return getDefaultTzCoder(clock).getTimeZoneCodecFor(type);
    }

    @SuppressWarnings("SameParameterValue")
    final TimeZoneCodec getLittleEndianCodec(int type, Clock clock) throws SQLException {
        return getLittleEndianTzCoder(clock).getTimeZoneCodecFor(type);
    }

    @SuppressWarnings("SameParameterValue")
    final TimeZoneCodec getBigEndianCodec(int type, Clock clock) throws SQLException {
        return getBigEndianTzCoder(clock).getTimeZoneCodecFor(type);
    }

    static DefaultDatatypeCoder getDefaultDataTypeCoder() {
        return DefaultDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
    }

    static TimeZoneDatatypeCoder getDefaultTzCoder(Clock clock) {
        DatatypeCoder datatypeCoder = getDefaultDataTypeCoder();
        return new TimeZoneDatatypeCoder(datatypeCoder, clock);
    }

    static TimeZoneDatatypeCoder getLittleEndianTzCoder(Clock clock) {
        DatatypeCoder datatypeCoder = LittleEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return new TimeZoneDatatypeCoder(datatypeCoder, clock);
    }

    static TimeZoneDatatypeCoder getBigEndianTzCoder(Clock clock) {
        DatatypeCoder datatypeCoder = BigEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return new TimeZoneDatatypeCoder(datatypeCoder, clock);
    }

}
