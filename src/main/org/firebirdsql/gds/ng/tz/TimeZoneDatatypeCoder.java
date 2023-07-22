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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.SQLException;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNullElseGet;
import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Datatype coder for {@code TIME WITH TIME ZONE} and {@code TIMESTAMP WITH TIME ZONE}.
 * <p>
 * As this uses Java 8 and higher types, this is not part of datatype coder itself.
 * </p>
 *
 * @author Mark Rotteveel
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
    private static final TemporalAdjuster TIME_TZ_BASE_DATE_ADJUSTER =
            TemporalAdjusters.ofDateAdjuster(currentDate -> TIME_TZ_BASE_DATE);
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
     * @param utcClock
     *         Clock used for deriving current data value
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
        return switch (fieldType & ~1) {
            case SQL_TIMESTAMP_TZ -> requireNonNullElseGet(standardTimestampWithTimeZoneCodec,
                    () -> standardTimestampWithTimeZoneCodec = new TimestampWithTimeZoneCodec(fieldType));
            case SQL_TIME_TZ -> requireNonNullElseGet(standardTimeWithTimeZoneCodec,
                    () -> standardTimeWithTimeZoneCodec = new TimeWithTimeZoneCodec(fieldType));
            case SQL_TIMESTAMP_TZ_EX -> requireNonNullElseGet(extendedTimestampWithTimeZoneCodec,
                    () -> extendedTimestampWithTimeZoneCodec = new TimestampWithTimeZoneCodec(fieldType));
            case SQL_TIME_TZ_EX -> requireNonNullElseGet(extendedTimeWithTimeZoneCodec,
                    () -> extendedTimeWithTimeZoneCodec = new TimeWithTimeZoneCodec(fieldType));
            default -> throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                    .messageParameter(fieldType)
                    .toSQLException();
        };
    }

    /**
     * Decodes the time zone id from a byte array with a time zone value.
     *
     * @param tzValueBytes Time zone value byte array
     * @param zoneIdOffset Offset of the zone id in {@code tzValueBytes}
     * @return The zone id (or a fallback value)
     */
    private ZoneId decodeTimeZoneId(byte[] tzValueBytes, int zoneIdOffset) {
        int timeZoneId = datatypeCoder.decodeShort(tzValueBytes, zoneIdOffset) & 0xFFFF; // handle as unsigned short
        return timeZoneMapping.timeZoneById(timeZoneId);
    }

    private <T extends Temporal> T decodeTimestampTz(byte[] timestampTzBytes,
            BiFunction<Instant, ZoneId, T> conversionFunction) {
        Instant instant = decodeTimestampTzAsInstant(timestampTzBytes);
        ZoneId zoneId = decodeTimeZoneId(timestampTzBytes, 8);
        return conversionFunction.apply(instant, zoneId);
    }

    private Instant decodeTimestampTzAsInstant(byte[] timestampTzBytes) {
        return datatypeCoder.decodeLocalDateTime(timestampTzBytes).toInstant(ZoneOffset.UTC);
    }

    private byte[] encodeOffsetDateTimeToTimestampTz(OffsetDateTime offsetDateTime, int bufferSize) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(offsetDateTime.getOffset());

        LocalDateTime utcDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        return encodeTimestampTz(utcDateTime, firebirdZoneId, bufferSize);
    }

    private byte[] encodeZonedDateTimeToTimestampTz(ZonedDateTime zonedDateTime, int bufferSize) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(zonedDateTime.getZone());

        LocalDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        return encodeTimestampTz(utcDateTime, firebirdZoneId, bufferSize);
    }

    private byte[] encodeTimestampTz(LocalDateTime utcDateTime, int firebirdZoneId, int bufferSize) {
        byte[] timestampTzBytes = new byte[bufferSize];
        datatypeCoder.encodeLocalDateTime(utcDateTime, timestampTzBytes, 0);
        // casting zoneId to short to ensure 'signed' values are written in the network format
        // this is not technically necessary, but is consistent with the values received from Firebird
        datatypeCoder.encodeShort((short) firebirdZoneId, timestampTzBytes, 8);

        return timestampTzBytes;
    }

    private LocalTime decodeTimeTzToUtcLocalTime(byte[] timeTzBytes) {
        return datatypeCoder.decodeLocalTime(timeTzBytes);
    }

    private OffsetTime decodeTimeTzToOffsetTime(byte[] timeTzBytes) {
        LocalTime utcTime = decodeTimeTzToUtcLocalTime(timeTzBytes);
        ZoneId zoneId = decodeTimeZoneId(timeTzBytes, 4);

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

    private OffsetDateTime decodeTimeTzToOffsetDateTime(byte[] timeTzBytes) {
        LocalTime utcTime = decodeTimeTzToUtcLocalTime(timeTzBytes);
        ZoneId zoneId = decodeTimeZoneId(timeTzBytes, 4);

        if (zoneId instanceof ZoneOffset) {
            // Simple case: can be done with offsets only
            LocalDate utcToday = OffsetDateTime.ofInstant(utcClock.instant(), ZoneOffset.UTC).toLocalDate();
            return OffsetDateTime.of(utcToday, utcTime, ZoneOffset.UTC)
                    .withOffsetSameInstant((ZoneOffset) zoneId);
        }

        return decodeTimeTzToZonedDateTime(utcTime, zoneId)
                .toOffsetDateTime();
    }

    private ZonedDateTime decodeTimeTzToZonedDateTime(byte[] timeTzBytes) {
        LocalTime utcTime = decodeTimeTzToUtcLocalTime(timeTzBytes);
        ZoneId zoneId = decodeTimeZoneId(timeTzBytes, 4);

        return decodeTimeTzToZonedDateTime(utcTime, zoneId);
    }

    private ZonedDateTime decodeTimeTzToZonedDateTime(LocalTime utcTime, ZoneId zoneId) {
        // We need to base on a date to determine value, we use the 2020-01-01 date;
        // this aligns closest with Firebird behaviour

        ZonedDateTime timeAtBaseDate = ZonedDateTime.of(TIME_TZ_BASE_DATE, utcTime, ZoneOffset.UTC)
                .withZoneSameInstant(zoneId);
        LocalDate currentDateInZone = ZonedDateTime.ofInstant(utcClock.instant(), zoneId).toLocalDate();
        return timeAtBaseDate.with(TemporalAdjusters.ofDateAdjuster(date -> currentDateInZone));
    }

    private byte[] encodeOffsetTimeToTimeTz(OffsetTime offsetTime, int bufferSize) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(offsetTime.getOffset());

        LocalTime utcTime = offsetTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime();
        return encodeTimeTz(utcTime, firebirdZoneId, bufferSize);
    }

    private byte[] encodeZonedDateTimeToTimeTz(ZonedDateTime zonedDateTime, int bufferSize) {
        ZoneId zone = zonedDateTime.getZone();
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(zone);
        if (!timeZoneMapping.isOffsetTimeZone(firebirdZoneId)) {
            // transform to base date for correct behaviour
            zonedDateTime = zonedDateTime.with(TIME_TZ_BASE_DATE_ADJUSTER);
        }

        LocalTime utcTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalTime();
        return encodeTimeTz(utcTime, firebirdZoneId, bufferSize);
    }

    private byte [] encodeTimeTz(LocalTime utcTime, int firebirdZoneId, int bufferSize) {
        byte[] timeTzBytes = new byte[bufferSize];
        datatypeCoder.encodeLocalTime(utcTime, timeTzBytes, 0);
        // casting zoneId to short to ensure 'signed' values are written in the network format
        // this is not technically necessary, but is consistent with the values received from Firebird
        datatypeCoder.encodeShort((short) firebirdZoneId, timeTzBytes, 4);

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
            System.getLogger(TimeZoneDatatypeCoder.class.getName())
                    .log(System.Logger.Level.INFO, "Clearing TimeZoneDatatypeCoder.instanceCache");
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

        /**
         * Encode a zoned date time to an encoded value.
         *
         * @param zonedDateTime
         *         Zoned date time instance
         * @return Byte array with encoded value
         */
        byte[] encodeZonedDateTime(ZonedDateTime zonedDateTime);

        /**
         * Decodes an encoded value to a zoned date time.
         *
         * @param fieldData
         *         Byte array with encoded value
         * @return Zoned date time value
         */
        ZonedDateTime decodeZonedDateTime(byte[] fieldData);

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
            return decodeTimeTzToOffsetDateTime(fieldData);
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

        @Override
        public byte[] encodeZonedDateTime(ZonedDateTime zonedDateTime) {
            return encodeZonedDateTimeToTimeTz(zonedDateTime, encodedSize);
        }

        @Override
        public ZonedDateTime decodeZonedDateTime(byte[] fieldData) {
            return decodeTimeTzToZonedDateTime(fieldData);
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
            return decodeTimestampTz(fieldData, OffsetDateTime::ofInstant);
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

        @Override
        public byte[] encodeZonedDateTime(ZonedDateTime zonedDateTime) {
            return encodeZonedDateTimeToTimestampTz(zonedDateTime, encodedSize);
        }

        @Override
        public ZonedDateTime decodeZonedDateTime(byte[] fieldData) {
            assert fieldData.length == encodedSize : "timestampTzBytes not length " + encodedSize;
            return decodeTimestampTz(fieldData, ZonedDateTime::ofInstant);
        }
    }
}
