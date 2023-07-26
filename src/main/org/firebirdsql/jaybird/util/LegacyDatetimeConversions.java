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
package org.firebirdsql.jaybird.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Class to hold conversions and manipulation of legacy datetime types ({@link java.sql.Date}, {@link java.sql.Time}
 * and {@link java.sql.Timestamp}).
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class LegacyDatetimeConversions {

    private LegacyDatetimeConversions() {
        // no instances
    }

    /**
     * Converts a timestamp to a local date time using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Timestamp#toLocalDateTime()} instead of {@code c}.
     * </p>
     *
     * @param val
     *         timestamp
     * @param c
     *         calendar
     * @return local date time
     */
    public static LocalDateTime toLocalDateTime(Timestamp val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return val.toLocalDateTime();
        c.setTime(val);
        return toLocalDateTime(c, val.getNanos());
    }

    /**
     * Converts a local date time to a timestamp using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Timestamp#valueOf(LocalDateTime)} instead of {@code c}.
     * </p>
     *
     * @param val
     *         local date time
     * @param c
     *         calendar
     * @return timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return Timestamp.valueOf(val);
        LegacyDatetimeConversions.updateCalendar(c, val);
        var timestamp = new Timestamp(c.getTimeInMillis());
        timestamp.setNanos(val.getNano());
        return timestamp;
    }

    /**
     * Converts a time to a local time using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Time#toLocalTime()} instead of {@code c}.
     * </p>
     *
     * @param val
     *         time
     * @param c
     *         calendar
     * @return local time
     */
    public static LocalTime toLocalTime(Time val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return val.toLocalTime();
        c.setTime(val);
        return toLocalTime(c, 0);
    }

    /**
     * Converts a local time to a time using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Time#valueOf(LocalTime)} instead of {@code c}.
     * </p>
     *
     * @param val
     *         local time
     * @param c
     *         calendar
     * @return time
     */
    public static Time toTime(LocalTime val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return Time.valueOf(val);
        LegacyDatetimeConversions.updateCalendar(c, LocalDate.EPOCH, val);
        return new Time(c.getTimeInMillis());
    }

    /**
     * Converts a date to a local date using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Date#toLocalDate()} instead of {@code c}.
     * </p>
     *
     * @param val
     *         time
     * @param c
     *         calendar
     * @return local time
     */
    public static LocalDate toLocalDate(Date val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return val.toLocalDate();
        c.setTime(val);
        return toLocalDate(c);
    }

    /**
     * Converts a local date to a date using calendar {@code c}.
     * <p>
     * If {@code c} is null, or the time zone is equal to the JVM default time zone, conversion is performed using
     * {@link Date#valueOf(LocalDate)} instead of {@code c}.
     * </p>
     *
     * @param val
     *         local time
     * @param c
     *         calendar
     * @return time
     */
    public static Date toDate(LocalDate val, Calendar c) {
        if (c == null || Objects.equals(c.getTimeZone(), TimeZone.getDefault())) return Date.valueOf(val);
        updateCalendar(c, val, LocalTime.MIDNIGHT);
        return new Date(c.getTimeInMillis());
    }

    /**
     * Converts a calendar to a {@link LocalDateTime}.
     *
     * @param c
     *         calendar
     * @param nanoOfSecond
     *         sub-second nanoseconds value, use {@code -1} to use the {@link Calendar#MILLISECOND} field
     * @return local date time
     */
    private static LocalDateTime toLocalDateTime(Calendar c, int nanoOfSecond) {
        return LocalDateTime.of(toLocalDate(c), toLocalTime(c, nanoOfSecond));
    }

    /**
     * Converts a calendar to a {@link LocalDate}.
     *
     * @param c
     *         calendar
     * @return local date
     */
    private static LocalDate toLocalDate(Calendar c) {
        return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts a calendar to a {@link LocalTime}.
     *
     * @param c
     *         calendar
     * @param nanoOfSecond
     *         sub-second nanoseconds value, use {@code -1} to use the {@link Calendar#MILLISECOND} field
     * @return local time
     */
    private static LocalTime toLocalTime(Calendar c, int nanoOfSecond) {
        if (nanoOfSecond == -1) {
            nanoOfSecond = (int) TimeUnit.MILLISECONDS.toNanos(c.get(Calendar.MILLISECOND));
        }
        return LocalTime.of(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), nanoOfSecond);
    }

    /**
     * Sets the fields of calendar {@code c} with the values of {@code localDateTime}
     *
     * @param c
     *         calendar
     * @param localDateTime
     *         local date time
     */
    private static void updateCalendar(Calendar c, LocalDateTime localDateTime) {
        updateCalendar(c, localDateTime.toLocalDate(), localDateTime.toLocalTime());
    }

    /**
     * Sets the fields of calendar {@code c} with the values of {@code localDate} and {@code localTime}
     *
     * @param c
     *         calendar
     * @param localDate
     *         local date
     * @param localTime
     *         local time
     */
    private static void updateCalendar(Calendar c, LocalDate localDate, LocalTime localTime) {
        updateCalendar(c, localDate);
        updateCalendar(c, localTime);
    }

    /**
     * Sets the fields of calendar {@code c} with the values of {@code localDate}
     *
     * @param c
     *         calendar
     * @param localDate
     *         local date
     */
    @SuppressWarnings("MagicConstant")
    private static void updateCalendar(Calendar c, LocalDate localDate) {
        c.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
    }

    /**
     * Sets the fields of calendar {@code c} with the values of {@code localTime}
     * <p>
     * Depending on the needs, it may be advisable to call {@link #updateCalendar(Calendar, LocalDate)} with
     * {@link LocalDate#EPOCH} or otherwise reset it to a base value.
     * </p>
     *
     * @param c
     *         calendar
     * @param localTime
     *         local time
     */
    private static void updateCalendar(Calendar c, LocalTime localTime) {
        c.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        c.set(Calendar.MINUTE, localTime.getMinute());
        c.set(Calendar.SECOND, localTime.getSecond());
        c.set(Calendar.MILLISECOND, (int) TimeUnit.NANOSECONDS.toMillis(localTime.getNano()));
    }

}
