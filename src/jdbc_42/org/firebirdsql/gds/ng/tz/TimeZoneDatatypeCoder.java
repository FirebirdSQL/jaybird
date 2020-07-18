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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DatatypeCoder.RawDateTimeStruct;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.time.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Datatype coder for {@code TIME WITH TIME ZONE} and {@code TIMESTAMP WITH TIME ZONE}.
 * <p>
 * As this uses Java 8 and higher types, this is not part of datatype coder itself.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public class TimeZoneDatatypeCoder {

    // Given implementation, the actual max should be 1 (maybe 2 if a combination of pure java and native is used)
    private static final int MAX_CACHED = 4;
    private static final Map<DatatypeCoder, TimeZoneDatatypeCoder> instanceCache = new ConcurrentHashMap<>(MAX_CACHED);
    // Date used by Firebird for deciding UTC time of TIME WITH TIME ZONE types
    private static final LocalDate TIME_TZ_BASE_DATE = LocalDate.of(2020, 1, 1);
    private final DatatypeCoder datatypeCoder;
    private final TimeZoneMapping timeZoneMapping = TimeZoneMapping.getInstance();
    // Always cache because this is the default mapping of the type
    private final DefaultTimeZoneCodec defaultTimeZoneCodec = new DefaultTimeZoneCodec();
    // Lazily cache because this is the exception
    private ExtendedTimeZoneCodec extendedTimeZoneCodec;

    /**
     * Initializes a time zone datatype coder.
     *
     * @param datatypeCoder
     *         datatype coder
     */
    public TimeZoneDatatypeCoder(DatatypeCoder datatypeCoder) {
        this.datatypeCoder = datatypeCoder;
    }

    /**
     * Obtains the {@link TimeZoneCodec} implementation for the field described by {@code fieldDescriptor}.
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @return Suitable instance of {@code TimeZoneCodec}
     * @throws SQLException
     *         When {@code fieldDescriptor} is not a TIME/TIMESTAMP WITH TIME ZONE type field
     * @see #getTimeZoneCodecFor(int)
     */
    public TimeZoneCodec getTimeZoneCodecFor(FieldDescriptor fieldDescriptor) throws SQLException {
        return getTimeZoneCodecFor(fieldDescriptor.getType());
    }

    /**
     * Obtains the {@link TimeZoneCodec} implementation for the field with the specified Firebird type.
     *
     * @param fieldType
     *         Firebird type of the field
     * @return Suitable instance of {@code TimeZoneCodec}
     * @throws SQLException
     *         When {@code fieldType} is not a TIME/TIMESTAMP WITH TIME ZONE type
     */
    public TimeZoneCodec getTimeZoneCodecFor(int fieldType) throws SQLException {
        switch (fieldType & ~1) {
        case SQL_TIMESTAMP_TZ:
        case SQL_TIME_TZ:
            return defaultTimeZoneCodec;
        case SQL_TIMESTAMP_TZ_EX:
        case SQL_TIME_TZ_EX:
            if (extendedTimeZoneCodec != null) {
                return extendedTimeZoneCodec;
            }
            return extendedTimeZoneCodec = new ExtendedTimeZoneCodec();
        default:
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                    .messageParameter(fieldType)
                    .toFlatSQLException();
        }
    }

    private OffsetDateTime decodeTimestampTz(byte[] timestampTzBytes) {
        assert timestampTzBytes.length == 12 : "timestampTzBytes not length 12";
        return decodeTimestampTzImpl(timestampTzBytes);
    }

    private OffsetDateTime decodeExTimestampTz(byte[] exTimestampTzBytes) {
        assert exTimestampTzBytes.length == sizeOfExTimestampTz() : "exTimestampTzBytes wrong length";
        return decodeTimestampTzImpl(exTimestampTzBytes);
    }

    private OffsetDateTime decodeTimestampTzImpl(byte[] timestampTzBytes) {
        int encodedDate = datatypeCoder.decodeInt(timestampTzBytes);
        int encodedTime = datatypeCoder.decodeInt(timestampTzBytes, 4);
        int timeZoneId = datatypeCoder.decodeShort(timestampTzBytes, 8) & 0xFFFF; // handle as unsigned short
        RawDateTimeStruct raw = new RawDateTimeStruct(encodedDate, true, encodedTime, true);

        LocalDateTime utcDateTime = LocalDateTime
                .of(raw.year, raw.month, raw.day, raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos());

        ZoneId zoneId = timeZoneMapping.timeZoneById(timeZoneId);
        Instant instant = utcDateTime.toInstant(ZoneOffset.UTC);

        return OffsetDateTime.ofInstant(instant, zoneId);
    }

    private byte[] encodeTimestampTz(OffsetDateTime offsetDateTime) {
        return encodeTimestampTzImpl(offsetDateTime, 12);
    }

    private byte[] encodeExTimestampTz(OffsetDateTime offsetDateTime) {
        return encodeTimestampTzImpl(offsetDateTime, sizeOfExTimestampTz());
    }

    private byte[] encodeTimestampTzImpl(OffsetDateTime offsetDateTime, int bufferSize) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(offsetDateTime.getOffset());

        OffsetDateTime utcDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
        RawDateTimeStruct raw = new RawDateTimeStruct();
        raw.year = utcDateTime.getYear();
        raw.month = utcDateTime.getMonthValue();
        raw.day = utcDateTime.getDayOfMonth();
        raw.hour = utcDateTime.getHour();
        raw.minute = utcDateTime.getMinute();
        raw.second = utcDateTime.getSecond();
        raw.setFractionsFromNanos(utcDateTime.getNano());

        byte[] timestampTzBytes = new byte[bufferSize];
        datatypeCoder.encodeInt(raw.getEncodedDate(), timestampTzBytes, 0);
        datatypeCoder.encodeInt(raw.getEncodedTime(), timestampTzBytes, 4);
        datatypeCoder.encodeShort(firebirdZoneId, timestampTzBytes, 8);

        return timestampTzBytes;
    }

    private OffsetTime decodeTimeTz(byte[] timeTzBytes) {
        assert timeTzBytes.length == 8 : "timeTzBytes not length 8";
        return decodeTimeTzImpl(timeTzBytes);
    }

    private OffsetTime decodeExTimeTz(byte[] exTimeTzBytes) {
        assert exTimeTzBytes.length == sizeOfExTimeTz() : "exTimeTzBytes wrong length";
        return decodeTimeTzImpl(exTimeTzBytes);
    }

    private OffsetTime decodeTimeTzImpl(byte[] timeTzBytes) {
        int encodedTime = datatypeCoder.decodeInt(timeTzBytes);
        int timeZoneId = datatypeCoder.decodeShort(timeTzBytes, 4) & 0xFFFF; // handle as unsigned short
        RawDateTimeStruct raw = new RawDateTimeStruct(0, false, encodedTime, true);

        LocalTime utcTime = LocalTime.of(raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos());
        ZoneId zoneId = timeZoneMapping.timeZoneById(timeZoneId);

        if (zoneId instanceof ZoneOffset) {
            // Simple case: can be done with offsets only
            return OffsetTime.of(utcTime, ZoneOffset.UTC)
                    .withOffsetSameInstant((ZoneOffset) zoneId);
        }

        // We need to base on a date to determine value, we use the 2020-01-01 date;
        // this aligns closest with Firebird behaviour

        return ZonedDateTime.of(TIME_TZ_BASE_DATE, utcTime, ZoneOffset.UTC)
                .withZoneSameInstant(zoneId)
                .toOffsetDateTime()
                .toOffsetTime();
    }

    private byte[] encodeTimeTz(OffsetTime offsetTime) {
        return encodeTimeTzImpl(offsetTime, 8);
    }

    private byte[] encodeExTimeTz(OffsetTime offsetTime) {
        return encodeTimeTzImpl(offsetTime, sizeOfExTimeTz());
    }

    private byte[] encodeTimeTzImpl(OffsetTime offsetTime, int bufferSize) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(offsetTime.getOffset());

        OffsetTime utcTime = offsetTime.withOffsetSameInstant(ZoneOffset.UTC);
        RawDateTimeStruct raw = new RawDateTimeStruct();
        raw.hour = utcTime.getHour();
        raw.minute = utcTime.getMinute();
        raw.second = utcTime.getSecond();
        raw.setFractionsFromNanos(utcTime.getNano());

        byte[] timeTzBytes = new byte[bufferSize];
        datatypeCoder.encodeInt(raw.getEncodedTime(), timeTzBytes, 0);
        datatypeCoder.encodeShort(firebirdZoneId, timeTzBytes, 4);

        return timeTzBytes;
    }

    private int sizeOfExTimestampTz() {
        return datatypeCoder.sizeOfShort() == 4 ? 16 : 12;
    }

    private int sizeOfExTimeTz() {
        return datatypeCoder.sizeOfShort() == 4 ? 12 : 8;
    }

    /**
     * Gets or creates an instance of time zone datatype coder for a datatype coder.
     *
     * @param datatypeCoder
     *         Datatype coder instance
     * @return Cached or new instance of {@code TimeZoneDatatypeCoder}
     */
    public static TimeZoneDatatypeCoder getInstanceFor(DatatypeCoder datatypeCoder) {
        // We don't need encoding specific info, using root can reduce needed instances.
        // In theory this could still be a non-root, but in actual implementation that shouldn't occur
        DatatypeCoder rootCoder = datatypeCoder.unwrap();
        TimeZoneDatatypeCoder cachedValue = instanceCache.get(rootCoder);
        if (cachedValue != null) {
            return cachedValue;
        }
        return createdCachedInstance(rootCoder);
    }

    private static TimeZoneDatatypeCoder createdCachedInstance(DatatypeCoder rootCoder) {
        if (instanceCache.size() > MAX_CACHED) {
            LoggerFactory.getLogger(TimeZoneDatatypeCoder.class)
                    .info("Clearing TimeZoneDatatypeCoder.instanceCache");
            instanceCache.clear();
        }
        TimeZoneDatatypeCoder value = new TimeZoneDatatypeCoder(rootCoder);
        instanceCache.putIfAbsent(rootCoder, value);
        return value;
    }

    /**
     * Simpler API for encoding or decoding offset date times.
     */
    public interface TimeZoneCodec {

        /**
         * Encode an offset date time to an encoded TIMESTAMP WITH TIME ZONE value.
         *
         * @param offsetDateTime
         *         Offset date time instance
         * @return Byte array with encoded value
         */
        byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime);

        /**
         * Decodes an encoded TIMESTAMP WITH TIME ZONE value to an offset date time.
         *
         * @param fieldData
         *         Byte array with encoded value
         * @return Offset date time instance
         */
        OffsetDateTime decodeOffsetDateTime(byte[] fieldData);

        /**
         * Encode an offset time to an encoded TIME WITH TIME ZONE value.
         *
         * @param offsetTime
         *         Offset time instance
         * @return Byte array with encoded value
         */
        byte[] encodeOffsetTime(OffsetTime offsetTime);

        /**
         * Decodes an encoded TIME WITH TIME ZONE value to an offset time.
         *
         * @param fieldData
         *         Byte array with encoded value
         * @return Offset time instance
         */
        OffsetTime decodeOffsetTime(byte[] fieldData);

    }

    /**
     * Codec for the 'normal' WITH TIME ZONE types.
     */
    private class DefaultTimeZoneCodec implements TimeZoneCodec {

        @Override
        public byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime) {
            return encodeTimestampTz(offsetDateTime);
        }

        @Override
        public OffsetDateTime decodeOffsetDateTime(byte[] fieldData) {
            return decodeTimestampTz(fieldData);
        }

        @Override
        public byte[] encodeOffsetTime(OffsetTime offsetTime) {
            return encodeTimeTz(offsetTime);
        }

        @Override
        public OffsetTime decodeOffsetTime(byte[] fieldData) {
            return decodeTimeTz(fieldData);
        }
    }

    /**
     * Codec for the 'extended' WITH TIME ZONE types.
     */
    private class ExtendedTimeZoneCodec implements TimeZoneCodec {

        @Override
        public byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime) {
            return encodeExTimestampTz(offsetDateTime);
        }

        @Override
        public OffsetDateTime decodeOffsetDateTime(byte[] fieldData) {
            return decodeExTimestampTz(fieldData);
        }

        @Override
        public byte[] encodeOffsetTime(OffsetTime offsetTime) {
            return encodeExTimeTz(offsetTime);
        }

        @Override
        public OffsetTime decodeOffsetTime(byte[] fieldData) {
            return decodeExTimeTz(fieldData);
        }
    }

}
