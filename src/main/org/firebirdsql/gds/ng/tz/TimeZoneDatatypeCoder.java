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

    private static final int SIZE_OF_TIMESTAMPTZ = 12;
    private static final int SIZE_OF_TIMETZ = 8;
    // Given implementation, the actual max should be 1 (maybe 2 if a combination of pure java and native is used)
    private static final int MAX_CACHED = 4;
    private static final Map<DatatypeCoder, TimeZoneDatatypeCoder> instanceCache = new ConcurrentHashMap<>(MAX_CACHED);
    // Date used by Firebird for deciding UTC time of TIME WITH TIME ZONE types
    private static final LocalDate TIME_TZ_BASE_DATE = LocalDate.of(2020, 1, 1);
    private final DatatypeCoder datatypeCoder;
    private final Clock utcClock;
    private final TimeZoneMapping timeZoneMapping = TimeZoneMapping.getInstance();
    // Fields below are lazily cached, we don't care about possible race conditions in initialization
    private TimeZoneCodec standardTimeWithTimeZoneCodec;
    private TimeZoneCodec standardTimestampWithTimeZoneCodec;
    private TimeZoneCodec extendedTimeWithTimeZoneCodec;
    private TimeZoneCodec extendedTimestampWithTimeZoneCodec;

    /**
     * Initializes a time zone datatype coder.
     *
     * @param datatypeCoder
     *         datatype coder
     */
    public TimeZoneDatatypeCoder(DatatypeCoder datatypeCoder) {
        this(datatypeCoder, Clock.systemUTC());
    }

    /**
     * Initializes a time zone datatype coder, for testing purposes
     *
     * @param datatypeCoder
     *         datatype coder
     * @param utcClock Clock used for deriving current data value
     */
    TimeZoneDatatypeCoder(DatatypeCoder datatypeCoder, Clock utcClock) {
        this.datatypeCoder = datatypeCoder;
        this.utcClock = utcClock;
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
            if (standardTimestampWithTimeZoneCodec != null) {
                return standardTimestampWithTimeZoneCodec;
            }
            return standardTimestampWithTimeZoneCodec = new TimestampWithTimeZoneCodec(fieldType);
        case SQL_TIME_TZ:
            if (standardTimeWithTimeZoneCodec != null) {
                return standardTimeWithTimeZoneCodec;
            }
            return standardTimeWithTimeZoneCodec = new TimeWithTimeZoneCodec(fieldType);
        case SQL_TIMESTAMP_TZ_EX:
            if (extendedTimestampWithTimeZoneCodec != null) {
                return extendedTimestampWithTimeZoneCodec;
            }
            return extendedTimestampWithTimeZoneCodec = new TimestampWithTimeZoneCodec(fieldType);
        case SQL_TIME_TZ_EX:
            if (extendedTimeWithTimeZoneCodec != null) {
                return extendedTimeWithTimeZoneCodec;
            }
            return extendedTimeWithTimeZoneCodec = new TimeWithTimeZoneCodec(fieldType);
        default:
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                    .messageParameter(fieldType)
                    .toFlatSQLException();
        }
    }

    private OffsetDateTime decodeTimestampTzToOffsetDateTime(byte[] timestampTzBytes) {
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

    private byte[] encodeOffsetDateTimeToTimestampTz(OffsetDateTime offsetDateTime, int bufferSize) {
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

    private OffsetTime decodeTimeTzToOffsetTime(byte[] timeTzBytes) {
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

    private byte[] encodeOffsetTimeToTimeTz(OffsetTime offsetTime, int bufferSize) {
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
     * Simpler API for encoding or decoding {@link java.time} types.
     * <p>
     * The data encoded or decoded depends on the specific Firebird type used to obtain this codec.
     * </p>
     */
    public interface TimeZoneCodec {

        /**
         * Encode an offset date time to an encoded value.
         *
         * @param offsetDateTime
         *         Offset date time instance
         * @return Byte array with encoded value
         */
        byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime);

        /**
         * Decodes an encoded value to an offset date time.
         *
         * @param fieldData
         *         Byte array with encoded value
         * @return Offset date time instance
         */
        OffsetDateTime decodeOffsetDateTime(byte[] fieldData);

        /**
         * Encode an offset time to an encoded value.
         *
         * @param offsetTime
         *         Offset time instance
         * @return Byte array with encoded value
         */
        byte[] encodeOffsetTime(OffsetTime offsetTime);

        /**
         * Decodes an encoded value to an offset time.
         *
         * @param fieldData
         *         Byte array with encoded value
         * @return Offset time instance
         */
        OffsetTime decodeOffsetTime(byte[] fieldData);

    }

    /**
     * Codec for {@code TIME WITH TIME ZONE}.
     */
    private class TimeWithTimeZoneCodec implements TimeZoneCodec {

        private final int encodedSize;

        private TimeWithTimeZoneCodec(int type) {
            type = type & ~1;
            assert type == SQL_TIME_TZ || type == SQL_TIME_TZ_EX
                    : "Not a TIME WITH TIME ZONE type, was " + type;
            encodedSize = type == SQL_TIME_TZ
                    ? SIZE_OF_TIMETZ
                    : SIZE_OF_TIMETZ + (datatypeCoder.sizeOfShort() == 2 ? 0 : 4);
        }

        @Override
        public byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime) {
            return encodeOffsetTime(offsetDateTime.toOffsetTime());
        }

        @Override
        public OffsetDateTime decodeOffsetDateTime(byte[] fieldData) {
            OffsetTime offsetTime = decodeOffsetTime(fieldData);
            OffsetDateTime today = OffsetDateTime.ofInstant(utcClock.instant(), offsetTime.getOffset());
            return offsetTime.atDate(today.toLocalDate());
        }

        @Override
        public byte[] encodeOffsetTime(OffsetTime offsetTime) {
            return encodeOffsetTimeToTimeTz(offsetTime, encodedSize);
        }

        @Override
        public OffsetTime decodeOffsetTime(byte[] fieldData) {
            assert fieldData.length == encodedSize : "timestampTzBytes not length " + encodedSize;
            return decodeTimeTzToOffsetTime(fieldData);
        }
    }

    private class TimestampWithTimeZoneCodec implements TimeZoneCodec {

        private final int encodedSize;

        private TimestampWithTimeZoneCodec(int type) {
            type = type & ~1;
            assert type == SQL_TIMESTAMP_TZ || type == SQL_TIMESTAMP_TZ_EX
                    : "Not a TIMESTAMP WITH TIME ZONE type, was " + type;
            encodedSize = type == SQL_TIMESTAMP_TZ
                    ? SIZE_OF_TIMESTAMPTZ
                    : SIZE_OF_TIMESTAMPTZ + (datatypeCoder.sizeOfShort() == 2 ? 0 : 4);
        }

        @Override
        public byte[] encodeOffsetDateTime(OffsetDateTime offsetDateTime) {
            return encodeOffsetDateTimeToTimestampTz(offsetDateTime, encodedSize);
        }

        @Override
        public OffsetDateTime decodeOffsetDateTime(byte[] fieldData) {
            assert fieldData.length == encodedSize : "timestampTzBytes not length " + encodedSize;
            return decodeTimestampTzToOffsetDateTime(fieldData);
        }

        @Override
        public byte[] encodeOffsetTime(OffsetTime offsetTime) {
            // We need to base on a date to determine value, we use the current date; this will be inconsistent depending
            // on the date, but this aligns closest with Firebird behaviour and SQL standard
            ZoneOffset offset = offsetTime.getOffset();
            OffsetDateTime today = OffsetDateTime.ofInstant(utcClock.instant(), offsetTime.getOffset());
            OffsetDateTime timeToday = OffsetDateTime.of(today.toLocalDate(), offsetTime.toLocalTime(), offset);
            return encodeOffsetDateTime(timeToday);
        }

        @Override
        public OffsetTime decodeOffsetTime(byte[] fieldData) {
            return decodeOffsetDateTime(fieldData).toOffsetTime();
        }
    }
}
