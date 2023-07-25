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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    // updateFbTimeUnits is tested through fromFbTimeUnits

    static Stream<String> localTimeValues() {
        return Stream.of("00:00", "10:18:39.1234", "11:23:15.1234", "15:45:53.9", "19:30:02.0001", "23:59:59.9999");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            datetime,                 iso8601DateTime
            1979-1-1 12:00:00,        1979-01-01T12:00
            1979-01-01T12:00:00,      1979-01-01T12:00
            1979-01-01t12:00:00,      1979-01-01T12:00
            2023-07-22 11:33:12.1234, 2023-07-22T11:33:12.1234
            2023-07-22T11:33:12.1234, 2023-07-22T11:33:12.1234
            2023-07-22 11:33:12.123,  2023-07-22T11:33:12.123
            2023-07-22T11:33:12.123,  2023-07-22T11:33:12.123
            2023-7-22 11:33:12.12,    2023-07-22T11:33:12.12
            2023-07-22t11:33:12.12,   2023-07-22T11:33:12.12
            2023-07-22 11:33:12.1,    2023-07-22T11:33:12.1
            2023-07-22T11:33:12.1,    2023-07-22T11:33:12.1
            2025-12-31 23:54:59.9999, 2025-12-31T23:54:59.9999
            2025-12-31t23:54:59.9999, 2025-12-31T23:54:59.9999
            ,
            """)
    void testParseIsoOrSqlTimestamp(String datetime, LocalDateTime iso8601DateTime) {
        LocalDateTime fromSqlDateTime = FbDatetimeConversion.parseIsoOrSqlTimestamp(datetime);
        assertEquals(iso8601DateTime, fromSqlDateTime);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            sqlDateTime,              iso8601DateTime
            1979-1-1 12:00:00,        1979-01-01T12:00
            2023-07-22 11:33:12.1234, 2023-07-22T11:33:12.1234
            2023-07-22 11:33:12.123,  2023-07-22T11:33:12.123
            2023-7-22 11:33:12.12,    2023-07-22T11:33:12.12
            2023-07-22 11:33:12.1,    2023-07-22T11:33:12.1
            2025-12-31 23:54:59.9999, 2025-12-31T23:54:59.9999
            """)
    void testSqlTimestampParseAndFormat(String sqlDateTime, LocalDateTime iso8601DateTime) {
        LocalDateTime fromSqlDateTime = FbDatetimeConversion.parseSqlTimestamp(sqlDateTime);

        assertEquals(iso8601DateTime, fromSqlDateTime);

        Timestamp timestampFromSqlDateTime = Timestamp.valueOf(sqlDateTime);
        assertEquals(timestampFromSqlDateTime.toLocalDateTime(), fromSqlDateTime);
        String timestampString = timestampFromSqlDateTime.toString();
        if (timestampString.endsWith(".0")) {
            timestampString = timestampString.substring(0, timestampString.length() - 2);
        }
        assertEquals(timestampString, FbDatetimeConversion.formatSqlTimestamp(fromSqlDateTime));
    }

    @Test
    void testParseSqlTimestamp_null() {
        assertNull(FbDatetimeConversion.parseSqlTimestamp(null));
    }

    @Test
    void testFormatSqlTimestamp_null() {
        assertNull(FbDatetimeConversion.formatSqlTimestamp(null));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            sqlTime,       compareLegacy
            00:00:00,      true
            00:00:00.1234, false
            01:02:03,      true
            01:02:03.1234, false
            23:59:59,      true
            23:59:59.9999, false
            23:59:59.999,  false
            23:59:59.99,   false
            23:59:59.9,    false
            23:59,         false
            """)
    void testSqlTimeParseAndFormat(String sqlTime, boolean compareLegacy) {
        LocalTime fromSqlTime = FbDatetimeConversion.parseSqlTime(sqlTime);
        String formattedSqlTime = FbDatetimeConversion.formatSqlTime(fromSqlTime);

        assertEquals(sqlTime.length() > 5 ? sqlTime : sqlTime + ":00", formattedSqlTime);

        if (compareLegacy) {
            Time timeFromSqlTime = Time.valueOf(sqlTime);
            assertEquals(timeFromSqlTime.toLocalTime(), fromSqlTime);
            assertEquals(timeFromSqlTime.toString(), formattedSqlTime);
        }
    }

    @Test
    void testParseSqlTime_null() {
        assertNull(FbDatetimeConversion.parseSqlTime(null));
    }

    @Test
    void testFormatSqlTime_null() {
        assertNull(FbDatetimeConversion.formatSqlTime(null));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            sqlDate,    expectedDate
            0001-01-01, 0001-01-01
            1980-2-3,   1980-02-03
            1980-02-03, 1980-02-03
            2032-7-11,  2032-07-11
            2024-12-2,  2024-12-02
            9999-12-31, 9999-12-31
            """)
    void testSqlDateParseAndFormat(String sqlDate, String expectedDate) {
        LocalDate fromSqlDate = FbDatetimeConversion.parseSqlDate(sqlDate);

        String formattedSqlDate = FbDatetimeConversion.formatSqlDate(fromSqlDate);
        assertEquals(expectedDate, formattedSqlDate);

        Date dateFromSqlDate = Date.valueOf(sqlDate);
        assertEquals(dateFromSqlDate.toLocalDate(), fromSqlDate);
        assertEquals(dateFromSqlDate.toString(), formattedSqlDate);
    }

    @Test
    void testParseSqlDate_null() {
        assertNull(FbDatetimeConversion.parseSqlDate(null));
    }

    @Test
    void testFormatSqlDate_null() {
        assertNull(FbDatetimeConversion.formatSqlDate(null));
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