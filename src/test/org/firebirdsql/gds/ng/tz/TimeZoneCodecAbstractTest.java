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
package org.firebirdsql.gds.ng.tz;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder.TimeZoneCodec;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Common test behaviour for {@link TimeZoneCodec} implementation tests.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class TimeZoneCodecAbstractTest {

    private static final String TIMESTAMPTZ = "2019-03-09T07:45:51+01:00";
    private static final OffsetDateTime TIMESTAMPTZ_OFFSETDATETIME = OffsetDateTime.parse(TIMESTAMPTZ);
    private static final String TIMETZ = "07:45:51+01:00";
    private static final OffsetTime TIMETZ_OFFSETTIME = OffsetTime.parse(TIMETZ);

    // Defined using offset
    static final String TIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB";
    static final String TIMESTAMPTZ_OFFSET_LE_HEX = "B7E40000F0AA830EDB050000";
    static final String TIMESTAMPTZ_OFFSET_BE_HEX = "0000E4B70E83AAF005DB0000";
    static final String EXTIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB0000003C";
    static final String EXTIMESTAMPTZ_OFFSET_NETWORK_HEX_ENCODED = "0000E4B70E83AAF0000005DB00000000";
    static final String EXTIMESTAMPTZ_OFFSET_LE_HEX = "B7E40000F0AA830EDB053C00";
    static final String EXTIMESTAMPTZ_OFFSET_BE_HEX = "0000E4B70E83AAF005DB003C";
    // Defined using Europe/Amsterdam
    static final String TIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE49";
    static final String TIMESTAMPTZ_ZONE_LE_HEX = "B7E40000F0AA830E49FE0000";
    static final String TIMESTAMPTZ_ZONE_BE_HEX = "0000E4B70E83AAF0FE490000";
    static final String EXTIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE490000003C";
    static final String EXTIMESTAMPTZ_ZONE_LE_HEX = "B7E40000F0AA830E49FE3C00";
    static final String EXTIMESTAMPTZ_ZONE_BE_HEX = "0000E4B70E83AAF0FE49003C";
    // Defined using offset
    static final String TIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB";
    static final String TIMETZ_OFFSET_LE_HEX = "F0AA830EDB050000";
    static final String TIMETZ_OFFSET_BE_HEX = "0E83AAF005DB0000";
    static final String EXTIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB0000003C";
    static final String EXTIMETZ_OFFSET_NETWORK_HEX_ENCODED = "0E83AAF0000005DB00000000";
    static final String EXTIMETZ_OFFSET_LE_HEX = "F0AA830EDB053C00";
    static final String EXTIMETZ_OFFSET_BE_HEX = "0E83AAF005DB003C";
    // Defined using Europe/Amsterdam (note: offset is date-sensitive)
    static final String TIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE49";
    static final String TIMETZ_ZONE_LE_HEX = "F0AA830E49FE0000";
    static final String TIMETZ_ZONE_BE_HEX = "0E83AAF0FE490000";
    static final String EXTIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE490000003C";
    static final String EXTIMETZ_ZONE_LE_HEX = "F0AA830E49FE3C00";
    static final String EXTIMETZ_ZONE_BE_HEX = "0E83AAF0FE49003C";

    private final int sqlTypeTimeTz;
    private final int sqlTypeTimestampTz;

    TimeZoneCodecAbstractTest(int sqlTypeTimeTz, int sqlTypeTimestampTz) {
        this.sqlTypeTimeTz = sqlTypeTimeTz;
        this.sqlTypeTimestampTz = sqlTypeTimestampTz;
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_offset_network() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzOffsetNetworkInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_zone_network() throws Exception {
        OffsetDateTime offsetDateTime = getNetworkCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzZoneNetworkInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_offset_littleEndian() throws Exception {
        OffsetDateTime offsetDateTime = getLittleEndianCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzOffsetLeInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_zone_littleEndian() throws Exception {
        OffsetDateTime offsetDateTime = getLittleEndianCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzZoneLeInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_offset_bigEndian() throws Exception {
        OffsetDateTime offsetDateTime = getBigEndianCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzOffsetBeInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzDecodeOffsetDateTime_zone_bigEndian() throws Exception {
        OffsetDateTime offsetDateTime = getBigEndianCodec(sqlTypeTimestampTz)
                .decodeOffsetDateTime(getTimestampTzZoneBeInput());

        assertEquals(TIMESTAMPTZ_OFFSETDATETIME, offsetDateTime);
    }

    @Test
    public final void timestampTzEncodeOffsetDateTime_network() throws Exception {
        byte[] encoded = getNetworkCodec(sqlTypeTimestampTz).encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(getTimestampTzOffsetNetworkExpected(), encoded);
    }

    @Test
    public final void timestampTzEncodeOffsetDateTime_littleEndian() throws Exception {
        byte[] encoded = getLittleEndianCodec(sqlTypeTimestampTz).encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(getTimestampTzOffsetLeExpected(), encoded);
    }

    @Test
    public final void timestampTzEncodeOffsetDateTime_bigEndian() throws Exception {
        byte[] encoded = getBigEndianCodec(sqlTypeTimestampTz).encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME);

        assertArrayEquals(getTimestampTzOffsetBeExpected(), encoded);
    }

    @Test
    public final void timeTzDecodeOffsetTime_offset_network() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzOffsetNetworkInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public final void timeTzDecodeOffsetTime_zone_network() throws Exception {
        OffsetTime offsetTime = getNetworkCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzZoneNetworkInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public final void timeTzDecodeOffsetTime_offset_littleEndian() throws Exception {
        OffsetTime offsetTime = getLittleEndianCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzOffsetLeInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public void timeTzDecodeOffsetTime_zone_littleEndian() throws Exception {
        OffsetTime offsetTime = getLittleEndianCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzZoneLeInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public final void timeTzDecodeOffsetTime_offset_bigEndian() throws Exception {
        OffsetTime offsetTime = getBigEndianCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzOffsetBeInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public final void timeTzDecodeOffsetTime_zone_bigEndian() throws Exception {
        OffsetTime offsetTime = getBigEndianCodec(sqlTypeTimeTz).decodeOffsetTime(getTimeTzZoneBeInput());

        assertEquals(TIMETZ_OFFSETTIME, offsetTime);
    }

    @Test
    public final void timeTzEncodeOffsetTime_network() throws Exception {
        byte[] encoded = getNetworkCodec(sqlTypeTimeTz).encodeOffsetTime(TIMETZ_OFFSETTIME);

        assertArrayEquals(getTimeTzOffsetNetworkExpected(), encoded);
    }

    @Test
    public final void timeTzEncodeOffsetTime_littleEndian() throws Exception {
        byte[] encoded = getLittleEndianCodec(sqlTypeTimeTz).encodeOffsetTime(TIMETZ_OFFSETTIME);

        assertArrayEquals(getTimeTzOffsetLeExpected(), encoded);
    }

    @Test
    public final void timeTzEncodeOffsetTime_bigEndian() throws Exception {
        byte[] encoded = getBigEndianCodec(sqlTypeTimeTz).encodeOffsetTime(TIMETZ_OFFSETTIME);

        assertArrayEquals(getTimeTzOffsetBeExpected(), encoded);
    }

    @Test
    public void getTimeZoneCodec() throws Exception {
        // NOTE Only testing for default datatype coder, this is about testing selection of right type
        for (int baseType : new int[] { sqlTypeTimestampTz, sqlTypeTimeTz }) {
            for (int type : new int[] { baseType, baseType | 1 }) {
                FieldDescriptor descriptor = rowDescriptorBuilder().setType(type).toFieldDescriptor();
                TimeZoneDatatypeCoder.TimeZoneCodec codec = getDefaultTzCoder().getTimeZoneCodecFor(descriptor);

                assertEquals(TIMESTAMPTZ_OFFSETDATETIME,
                        codec.decodeOffsetDateTime(getTimestampTzOffsetNetworkInput()));
                assertEquals(TIMETZ_OFFSETTIME, codec.decodeOffsetTime(getTimeTzOffsetNetworkInput()));
                assertArrayEquals(getTimestampTzOffsetNetworkExpected(),
                        codec.encodeOffsetDateTime(TIMESTAMPTZ_OFFSETDATETIME));
                assertArrayEquals(getTimeTzOffsetNetworkExpected(), codec.encodeOffsetTime(TIMETZ_OFFSETTIME));
            }
        }
    }

    abstract byte[] getTimestampTzOffsetNetworkInput();

    abstract byte[] getTimestampTzOffsetNetworkExpected();

    abstract byte[] getTimestampTzZoneNetworkInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimestampTzZoneNetworkExpected();

    abstract byte[] getTimestampTzOffsetLeInput();

    abstract byte[] getTimestampTzOffsetLeExpected();

    abstract byte[] getTimestampTzZoneLeInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimestampTzZoneLeExpected();

    abstract byte[] getTimestampTzOffsetBeInput();

    abstract byte[] getTimestampTzOffsetBeExpected();

    abstract byte[] getTimestampTzZoneBeInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimestampTzZoneBeExpected();

    abstract byte[] getTimeTzOffsetNetworkInput();

    abstract byte[] getTimeTzOffsetNetworkExpected();

    abstract byte[] getTimeTzZoneNetworkInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimeTzZoneNetworkExpected();

    abstract byte[] getTimeTzOffsetLeInput();

    abstract byte[] getTimeTzOffsetLeExpected();

    abstract byte[] getTimeTzZoneLeInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimeTzZoneLeExpected();

    abstract byte[] getTimeTzOffsetBeInput();

    abstract byte[] getTimeTzOffsetBeExpected();

    abstract byte[] getTimeTzZoneBeInput();

    @SuppressWarnings("unused")
    abstract byte[] getTimeTzZoneBeExpected();

    static RowDescriptorBuilder rowDescriptorBuilder() {
        return new RowDescriptorBuilder(1, getDefaultDataTypeCoder());
    }

    final TimeZoneCodec getNetworkCodec(int type) throws SQLException {
        return getDefaultTzCoder().getTimeZoneCodecFor(type);
    }

    final TimeZoneCodec getLittleEndianCodec(int type) throws SQLException {
        return getLittleEndianTzCoder().getTimeZoneCodecFor(type);
    }

    final TimeZoneCodec getBigEndianCodec(int type) throws SQLException {
        return getBigEndianTzCoder().getTimeZoneCodecFor(type);
    }

    static TimeZoneDatatypeCoder getDefaultTzCoder() {
        DatatypeCoder datatypeCoder = getDefaultDataTypeCoder();
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    static DefaultDatatypeCoder getDefaultDataTypeCoder() {
        return DefaultDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
    }

    static TimeZoneDatatypeCoder getLittleEndianTzCoder() {
        DatatypeCoder datatypeCoder = LittleEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

    static TimeZoneDatatypeCoder getBigEndianTzCoder() {
        DatatypeCoder datatypeCoder = BigEndianDatatypeCoder
                .forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
        return TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder);
    }

}
