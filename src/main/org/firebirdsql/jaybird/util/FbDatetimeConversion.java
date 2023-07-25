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

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.JulianFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Helpers and classes for Firebird/Jaybird datetime handling.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class FbDatetimeConversion {

    /**
     * The Firebird unit of time, i.e. 100 microseconds.
     */
    public static final TemporalUnit FB_TIME_UNIT = new FbTimeUnit();
    /**
     * Field for Firebird time.
     */
    public static final TemporalField FB_TIME_FIELD = new FbTimeField();

    /**
     * Number of microseconds per Firebird unit of time (i.e. 100 microseconds).
     */
    public static final long MICROS_PER_UNIT = 100L;
    /**
     * Number of nanoseconds per Firebird unit of time (i.e. 100 microseconds, or 100,000 nanoseconds).
     */
    public static final long NANOS_PER_UNIT = 100_000L;

    /**
     * A formatter that can parse {@code yyyy-[m]m-[d]d} as specified in {@link java.sql.Date#valueOf(String)}.
     * <p>
     * Use {@link DateTimeFormatter#ISO_LOCAL_DATE} for formatting.
     * </p>
     */
    private static final DateTimeFormatter SQL_DATE_PARSE = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    /**
     * A formatter that can parse {@code yyyy-[m]m-[d]d hh:mm[:ss[.f...]]} as specified in
     * {@link java.sql.Timestamp#valueOf(String)}.
     * <p>
     * NOTE: Contrary to {@link java.sql.Timestamp#valueOf(String)}, this format allows
     * {@code yyyy-[m]m-[d]d hh:mm[:ss[.f...]]} (i.e. seconds are also optional).
     * </p>
     * <p>
     * Use {@link #SQL_TIMESTAMP_FORMAT} for formatting.
     * </p>
     */
    private static final DateTimeFormatter SQL_TIMESTAMP_PARSE = new DateTimeFormatterBuilder()
            .append(SQL_DATE_PARSE)
            .appendLiteral(' ')
            .append(ISO_LOCAL_TIME)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    /**
     * A formatter that will format {@code yyyy-mm-dd hh:mm:ss[.f...]} similar to {@link java.sql.Timestamp#toString()}.
     * <p>
     * NOTE: Contrary to {@link java.sql.Timestamp#toString()}, this will not print {@code .0} if there is no sub-second
     * value.
     * </p>
     * <p>
     * Use {@link #SQL_TIMESTAMP_PARSE} for parsing.
     * </p>
     */
    private static final DateTimeFormatter SQL_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(ISO_LOCAL_TIME)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    private FbDatetimeConversion() {
        // no instances
    }

    /**
     * Converts a local date to a Modified Julian Date.
     * <p>
     * Firebird's {@code DATE} type uses Modified Julian Date to store dates. Current Firebird versions only support
     * values in the range [0001-01-01, 9999-12-31] or [-678575, 2973483]; this method does not enforce this range. We
     * leave that to the server to enforce, in case it changes in the future.
     * </p>
     *
     * @param localDate
     *         local date value
     * @return Modified Julian Date value
     * @throws ArithmeticException
     *         if the resulting Modified Julian Date is out of range of an {@code int}
     * @see #toModifiedJulianDate(LocalDate)
     * @see #updateModifiedJulianDate(Temporal, int)
     */
    public static int toModifiedJulianDate(LocalDate localDate) {
        // Use getLong, otherwise an exception is thrown as the range of Java's MJD requires a long. On the other hand,
        // Jaybird's range is a lot smaller and fits in an int.
        long mjd = localDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
        return Math.toIntExact(mjd);
    }

    /**
     * Converts a Modified Julian Date to the corresponding {@link LocalDate}.
     * <p>
     * A Modified Julian Date is the number of days since 17 November 1858.
     * </p>
     *
     * @param mjd
     *         Modified Julian Date value
     * @return local date corresponding to {@code mjd}
     * @see #toModifiedJulianDate(LocalDate)
     * @see #updateModifiedJulianDate(Temporal, int)
     */
    public static LocalDate fromModifiedJulianDate(int mjd) {
        return updateModifiedJulianDate(LocalDate.EPOCH, mjd);
    }

    /**
     * Returns a copy of {@code datetime}, updated so its date is now set to the value corresponding to the Modified
     * Julian Date {@code mjd}.
     *
     * @param datetime
     *         datetime value to modify
     * @param mjd
     *         Modified Julian Date value
     * @param <R>
     *         type of {@code datetime}
     * @return copy of the same type as {@code datetime}, with the date changed to match {@code mjd}
     * @see #fromModifiedJulianDate(int)
     * @see #toModifiedJulianDate(LocalDate)
     */
    public static <R extends Temporal> R updateModifiedJulianDate(R datetime, int mjd) {
        return JulianFields.MODIFIED_JULIAN_DAY.adjustInto(datetime, mjd);
    }

    /**
     * Converts a local time to Firebird time units (a.k.a. fractions, or 100 microseconds).
     *
     * @param localTime
     *         local time value
     * @return number of 100 microseconds to represent {@code localTime}
     * @see #fromFbTimeUnits(int)
     * @see #updateFbTimeUnits(Temporal, int)
     */
    public static int toFbTimeUnits(LocalTime localTime) {
        return localTime.get(FB_TIME_FIELD);
    }

    /**
     * Converts Firebird time units (a.k.a. fractions, or 100 microseconds) to a local time.
     *
     * @param timeUnits
     *         number of 100 microseconds
     * @return local time
     * @see #toFbTimeUnits(LocalTime)
     * @see #updateFbTimeUnits(Temporal, int)
     */
    public static LocalTime fromFbTimeUnits(int timeUnits) {
        return updateFbTimeUnits(LocalTime.MIN, timeUnits);
    }

    /**
     * Returns a copy of {@code datetime}, updated so its time is now set to the value corresponding to the Firebird
     * time units ({@code timeUnits)}.
     *
     * @param datetime
     *         datetime value to modify
     * @param timeUnits
     *         number of 100 microseconds
     * @param <R>
     *         type of {@code datetime}
     * @return copy of the same type as {@code datetime}, with the time changed to match {@code timeUnits}
     * @see #fromFbTimeUnits(int)
     * @see #toFbTimeUnits(LocalTime)
     */
    public static <R extends Temporal> R updateFbTimeUnits(R datetime, int timeUnits) {
        return FB_TIME_FIELD.adjustInto(datetime, timeUnits);
    }

    /**
     * Parse a string as a ISO 8601 datetime (yyyy-mm-dd{T|t}hh:mm[:ss[.f...]] <em>or</em> as a SQL timestamp value (see
     * {@link #parseSqlTimestamp(String)}).
     *
     * @param datetimeString
     *         datetime value to parse
     * @return local date time value, or {@code null} if {@code datetimeString} is {@code null}
     * @throws java.time.format.DateTimeParseException
     *         if {@code datetimeString} cannot be parsed
     * @see #parseSqlTimestamp(String)
     */
    public static LocalDateTime parseIsoOrSqlTimestamp(String datetimeString) {
        if (datetimeString == null) return null;
        if (datetimeString.length() >= 16 && (datetimeString.charAt(10) == 'T' || datetimeString.charAt(10) == 't')) {
            return LocalDateTime.parse(datetimeString);
        }
        return parseSqlTimestamp(datetimeString);
    }

    /**
     * Parse a string as a SQL timestamp value ({@code yyyy-[m]m-[d]d hh:mm[:ss[.f...]]}).
     *
     * @param datetimeString
     *         datetime value to parse
     * @return local date time value, or {@code null} if {@code datetimeString} is {@code null}
     * @throws java.time.format.DateTimeParseException
     *         if {@code datetimeString} cannot be parsed
     * @see #parseIsoOrSqlTimestamp(String)
     * @see java.sql.Timestamp#valueOf(String)
     */
    public static LocalDateTime parseSqlTimestamp(String datetimeString) {
        return datetimeString != null ? LocalDateTime.parse(datetimeString, SQL_TIMESTAMP_PARSE) : null;
    }

    /**
     * Formats a local date time as a SQL timestamp value ({@code yyyy-mm-dd hh:mm:ss[.f...]}).
     *
     * @param localDateTime
     *         local date time
     * @return formatted string, or {@code null} if {@code localDateTime} is {@code null}
     */
    public static String formatSqlTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(SQL_TIMESTAMP_FORMAT) : null;
    }

    /**
     * Parse a string as a SQL time value ({@code hh:mm[:ss[.f...]]}).
     * <p>
     * Contrary to {@link java.sql.Time}, fractional seconds are supported.
     * </p>
     *
     * @param timeString
     *         time value to parse
     * @return local time value, or {@code null} if {@code timeString} is {@code null}
     * @throws java.time.format.DateTimeParseException
     *         if {@code timeString} cannot be parsed
     * @see java.sql.Time#valueOf(String)
     */
    public static LocalTime parseSqlTime(String timeString) {
        return timeString != null ? LocalTime.parse(timeString) : null;
    }

    /**
     * Formats a local time as a SQL time value ({@code hh:mm:ss[.f...]}).
     *
     * @param localTime
     *         local time
     * @return formatted string, or {@code null} if {@code localTime} is {@code null}
     */
    public static String formatSqlTime(LocalTime localTime) {
        return localTime != null ? localTime.format(ISO_LOCAL_TIME) : null;
    }

    /**
     * Parse a string as a SQL date value ({@code yyyy-[m]m-[d]d}).
     *
     * @param dateString
     *         date value to parse
     * @return local date value, or {@code null} if {@code dateString} is {@code null}
     * @throws java.time.format.DateTimeParseException
     *         if {@code dateString} cannot be parsed
     * @see java.sql.Date#valueOf(String)
     */
    public static LocalDate parseSqlDate(String dateString) {
        return dateString != null ? LocalDate.parse(dateString, SQL_DATE_PARSE) : null;
    }

    /**
     * Formats a local date as a SQL date value ({@code yyyy-mm-dd}).
     *
     * @param localDate
     *         local date
     * @return formatted string, or {@code null} if {@code localDate} is {@code null}
     */
    public static String formatSqlDate(LocalDate localDate) {
        return localDate != null ? localDate.toString() : null;
    }

    /**
     * A {@link TemporalField} to query {@code java.time} objects for time in {@link FbTimeUnit}.
     */
    private static final class FbTimeField implements TemporalField {

        // max is number of 100 microseconds in a day
        private static final ValueRange RANGE = ValueRange.of(0, 24 * 60 * 60 * 10_000 - 1);

        private FbTimeField() {
            // effectively singleton
        }

        @Override
        public TemporalUnit getBaseUnit() {
            return FB_TIME_UNIT;
        }

        @Override
        public TemporalUnit getRangeUnit() {
            return ChronoUnit.DAYS;
        }

        @Override
        public ValueRange range() {
            return RANGE;
        }

        @Override
        public boolean isDateBased() {
            return false;
        }

        @Override
        public boolean isTimeBased() {
            return true;
        }

        @Override
        public boolean isSupportedBy(TemporalAccessor temporal) {
            // We could also use MICRO_OF_DAY, but generally that is implemented in terms of NANO_OF_DAY
            return temporal.isSupported(ChronoField.NANO_OF_DAY);
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (!isSupportedBy(temporal)) {
                throw new DateTimeException("Unsupported field: " + this);
            }
            return range();
        }

        @Override
        public long getFrom(TemporalAccessor temporal) {
            // We could also use MICRO_OF_DAY, but generally that is implemented in terms of NANO_OF_DAY
            return temporal.getLong(ChronoField.NANO_OF_DAY) / NANOS_PER_UNIT;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            // We could also use MICRO_OF_DAY, but generally that is implemented in terms of NANO_OF_DAY
            // No need for Math.multiplyExact, as the range check makes sure there won't be an overflow
            return (R) temporal.with(ChronoField.NANO_OF_DAY, RANGE.checkValidValue(newValue, this) * NANOS_PER_UNIT);
        }

        @Override
        public String toString() {
            return "FbTimeField";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FbTimeField;
        }

        @Override
        public int hashCode() {
            return FbTimeField.class.hashCode();
        }

    }

    /**
     * A {@link TemporalUnit} representing 100 microseconds, the unit of time used by Firebird.
     */
    private static final class FbTimeUnit implements TemporalUnit {

        // A Firebird time unit is expressed in "fractions", which are 100 microseconds, or 100,000 nanoseconds
        private static final Duration UNIT_DURATION = Duration.ofNanos(NANOS_PER_UNIT);

        private FbTimeUnit() {
            // effectively singleton
        }

        @Override
        public Duration getDuration() {
            return UNIT_DURATION;
        }

        @Override
        public boolean isDurationEstimated() {
            return false;
        }

        @Override
        public boolean isDateBased() {
            return false;
        }

        @Override
        public boolean isTimeBased() {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R) temporal.plus(Math.multiplyExact(amount, MICROS_PER_UNIT), ChronoUnit.MICROS);
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            return temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.MICROS) / MICROS_PER_UNIT;
        }

        @Override
        public String toString() {
            return "FbTimeUnit";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FbTimeUnit;
        }

        @Override
        public int hashCode() {
            return FbTimeUnit.class.hashCode();
        }

    }

}
