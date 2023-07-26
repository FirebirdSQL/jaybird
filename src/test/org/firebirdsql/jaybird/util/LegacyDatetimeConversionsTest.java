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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class LegacyDatetimeConversionsTest {

    private static TimeZone originalTimeZone;

    @BeforeAll
    static void changeDefaultTimeZone() {
        // Ensure stable test expectations by using a specific time zone
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterAll
    static void revertDefaultTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @ParameterizedTest
    // NOTE: timestamp is expressed in UTC
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            timestamp,                zoneId,              expectedLocalDateTime
            2023-07-26 11:15:23,      UTC,                 2023-07-26T11:15:23
            2023-07-26 11:15:23.1234, Europe/Amsterdam,    2023-07-26T13:15:23.1234
            2023-07-26 11:15:23.1234, America/New_York,    2023-07-26T07:15:23.1234
            """)
    void toLocalDateTime(Timestamp timestamp, String zoneId, LocalDateTime expectedLocalDateTime) {
        assertEquals(expectedLocalDateTime, LegacyDatetimeConversions.toLocalDateTime(timestamp, getCalendar(zoneId)));
    }

    private static Calendar getCalendar(String zoneId) {
        return Calendar.getInstance(TimeZone.getTimeZone(zoneId));
    }

    @ParameterizedTest
    // NOTE: expectedTimestamp is expressed in UTC
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            localDateTime,            zoneId,           expectedTimestamp
            2023-07-26T11:15:23,      UTC,              2023-07-26 11:15:23
            2023-07-26T13:15:23.1234, Europe/Amsterdam, 2023-07-26 11:15:23.1234
            2023-07-26T07:15:23.1234, America/New_York, 2023-07-26 11:15:23.1234
            """)
    void toTimestamp(LocalDateTime localDateTime, String zoneId, Timestamp expectedTimestamp) {
        assertEquals(expectedTimestamp, LegacyDatetimeConversions.toTimestamp(localDateTime, getCalendar(zoneId)));
    }

    @ParameterizedTest
    // NOTE: timestamp is expressed in UTC, rules as of 1970-01-01 apply(!)
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            time,     zoneId,           expectedLocalTime
            11:15:23, UTC,              11:15:23
            11:15:23, Europe/Amsterdam, 12:15:23
            11:15:23, America/New_York, 06:15:23
            """)
    void toLocalTime(Time time, String zoneId, LocalTime expectedLocalTime) {
        assertEquals(expectedLocalTime, LegacyDatetimeConversions.toLocalTime(time, getCalendar(zoneId)));
    }

    @ParameterizedTest
    // NOTE: expectedTimestamp is expressed in UTC, rules as of 1970-01-01 apply(!)
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            localTime, zoneId,           expectedTime
            11:15:23,  UTC,              11:15:23
            13:15:23,  Europe/Amsterdam, 12:15:23
            07:15:23,  America/New_York, 12:15:23
            """)
    void toTime(LocalTime localTime, String zoneId, Time expectedTime) {
        assertEquals(expectedTime, LegacyDatetimeConversions.toTime(localTime, getCalendar(zoneId)));
    }

    @ParameterizedTest
    // NOTE: date is expressed as date on 00:00:00 UTC
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            date,       zoneId,           expectedLocalDate
            2023-07-26, UTC,              2023-07-26
            2023-07-26, Europe/Amsterdam, 2023-07-26
            2023-07-26, America/New_York, 2023-07-25
            """)
    void toLocalDate(Date date, String zoneId, LocalDate expectedLocalDate) {
        assertEquals(expectedLocalDate, LegacyDatetimeConversions.toLocalDate(date, getCalendar(zoneId)));
    }

    @ParameterizedTest
    // NOTE: expectedLocalDate is expressed as date on 00:00:00 UTC
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            date,       zoneId,           expectedLocalDate
            2023-07-26, UTC,              2023-07-26
            2023-07-26, Europe/Amsterdam, 2023-07-25
            2023-07-26, America/New_York, 2023-07-26
            """)
    void toDate(LocalDate localDate, String zoneId, String expectedDate) {
        // Using toString() to avoid issues with the fact java.sql.Date wraps milliseconds
        assertEquals(expectedDate, LegacyDatetimeConversions.toDate(localDate, getCalendar(zoneId)).toString());
    }

}
