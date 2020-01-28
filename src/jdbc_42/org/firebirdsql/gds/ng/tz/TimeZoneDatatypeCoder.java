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

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DatatypeCoder.RawDateTimeStruct;
import org.firebirdsql.logging.LoggerFactory;

import java.time.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final DatatypeCoder datatypeCoder;
    private final TimeZoneMapping timeZoneMapping = TimeZoneMapping.getInstance();

    /**
     * Initializes a time zone datatype coder.
     *
     * @param datatypeCoder
     *         datatype coder
     */
    public TimeZoneDatatypeCoder(DatatypeCoder datatypeCoder) {
        this.datatypeCoder = datatypeCoder;
    }

    public OffsetDateTime decodeTimestampTz(byte[] timestampTzBytes) {
        assert timestampTzBytes.length == 12 : "timestampTzBytes not length 12";
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

    public byte[] encodeTimestampTz(OffsetDateTime offsetDateTime) {
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

        byte[] timestampTzBytes = new byte[12];
        datatypeCoder.encodeInt(raw.getEncodedDate(), timestampTzBytes, 0);
        datatypeCoder.encodeInt(raw.getEncodedTime(), timestampTzBytes, 4);
        datatypeCoder.encodeShort(firebirdZoneId, timestampTzBytes, 8);

        return timestampTzBytes;
    }

    public OffsetTime decodeTimeTz(byte[] timeTzBytes) {
        assert timeTzBytes.length == 8 : "timeTzBytes not length 8";
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

        // We need to base on a date to determine value, we use the current date; this will be inconsistent depending
        // on the date, but this aligns closest with Firebird behaviour and SQL standard

        LocalDate utcDate = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        return ZonedDateTime.of(utcDate, utcTime, ZoneOffset.UTC)
                .withZoneSameInstant(zoneId)
                .toOffsetDateTime()
                .toOffsetTime();
    }

    public byte[] encodeTimeTz(OffsetTime offsetTime) {
        int firebirdZoneId = timeZoneMapping.toTimeZoneId(offsetTime.getOffset());

        OffsetTime utcTime = offsetTime.withOffsetSameInstant(ZoneOffset.UTC);
        RawDateTimeStruct raw = new RawDateTimeStruct();
        raw.hour = utcTime.getHour();
        raw.minute = utcTime.getMinute();
        raw.second = utcTime.getSecond();
        raw.setFractionsFromNanos(utcTime.getNano());

        byte[] timeTzBytes = new byte[8];
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

}
