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
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.JulianFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;

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
