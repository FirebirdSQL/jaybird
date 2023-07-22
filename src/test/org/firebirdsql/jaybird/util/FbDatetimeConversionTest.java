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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FbDatetimeConversionTest {

    @ParameterizedTest
    @MethodSource("localDateValues")
    void testToModifiedJulianDate(LocalDate localDate) {
        assertEquals(alternativeMjdCalculation(localDate), FbDatetimeConversion.toModifiedJulianDate(localDate));
    }

    @ParameterizedTest
    @MethodSource("localDateValues")
    void testFromModifiedJulianDate(LocalDate localDate) {
        assertEquals(localDate, FbDatetimeConversion.fromModifiedJulianDate(alternativeMjdCalculation(localDate)));
    }

    // updateModifiedJulianDate is tested through fromModifiedJulianDate

    static Stream<String> localDateValues() {
        return Stream.of("0001-01-01", "9999-12-31", "1600-07-02", "1653-06-12", "1653-06-13", "1970-01-01",
                "1970-01-02", "1980-01-30", "2023-07-10", "2023-10-07", "1979-01-12", "1979-01-13", "1979-12-01",
                "1979-12-02");
    }

    @ParameterizedTest
    @MethodSource("localTimeValues")
    void testToFbTimeUnits(LocalTime localTime) {
        assertEquals(alternativeTimeUnitCalculation(localTime), FbDatetimeConversion.toFbTimeUnits(localTime));
    }

    @ParameterizedTest
    @MethodSource("localTimeValues")
    void testFromFbTimeUnits(LocalTime localTime) {
        assertEquals(localTime, FbDatetimeConversion.fromFbTimeUnits(alternativeTimeUnitCalculation(localTime)));
    }

    static Stream<String> localTimeValues() {
        return Stream.of("00:00", "10:18:39.1234", "11:23:15.1234", "15:45:53.9", "19:30:02.0001", "23:59:59.9999");
    }

    /**
     * Performs an alternative calculation to produce a Modified Julian Date from a local date.
     * <p>
     * This is the calculation which Jaybird used in Jaybird 5 and earlier.
     * </p>
     *
     * @param localDate
     *         local date value
     * @return Modified Julian Date value
     */
    private static int alternativeMjdCalculation(LocalDate localDate) {
        int cpMonth = localDate.getMonthValue();
        int cpYear = localDate.getYear();

        if (cpMonth > 2) {
            cpMonth -= 3;
        } else {
            cpMonth += 9;
            cpYear -= 1;
        }

        int c = cpYear / 100;
        int ya = cpYear - 100 * c;

        return ((146097 * c) / 4 +
                (1461 * ya) / 4 +
                (153 * cpMonth + 2) / 5 +
                localDate.getDayOfMonth() + 1721119 - 2400001);
    }

    private static final int NANOSECONDS_PER_FRACTION = (int) FbDatetimeConversion.NANOS_PER_UNIT;
    private static final int FRACTIONS_PER_MILLISECOND = 10;
    private static final int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
    private static final int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
    private static final int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;

    /**
     * Performs an alternative calculation to produce Firebird time units from a local time.
     * <p>
     * This is the calculation which Jaybird used in Jaybird 5 and earlier.
     * </p>
     *
     * @param localTime
     *         local time value
     * @return number of 100 microseconds to represent {@code localTime}
     */
    private static int alternativeTimeUnitCalculation(LocalTime localTime) {
        return localTime.getHour() * FRACTIONS_PER_HOUR
               + localTime.getMinute() * FRACTIONS_PER_MINUTE
               + localTime.getSecond() * FRACTIONS_PER_SECOND
               + localTime.getNano() / NANOSECONDS_PER_FRACTION;
    }

}