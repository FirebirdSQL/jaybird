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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TimeZoneOffsetMappingTest {

    private static final int SIGN_POSITIVE = 1;
    private static final int SIGN_NEGATIVE = -1;
    private static final TimeZoneMapping mapping = TimeZoneMapping.getInstance();
    private static final ZoneId UTC_ZONE_UID = ZoneOffset.UTC;

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "{0} => {4}")
    @MethodSource("testCases")
    void testTimeZoneById(int firebirdZoneId, int sign, int hours, int minutes, ZoneId zoneId) {
        assertEquals(zoneId, mapping.timeZoneById(firebirdZoneId), "Unexpected mapping");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testToOffsetMinutes(int firebirdZoneId, int sign, int hours, int minutes) {
        final int offsetMinutes = getOffsetMinutes(sign, hours, minutes);

        assertEquals(offsetMinutes, mapping.toOffsetMinutes(firebirdZoneId), "Unexpected offset");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testToTimeZoneId_minutes(int firebirdZoneId, int sign, int hours, int minutes) {
        final int offsetMinutes = getOffsetMinutes(sign, hours, minutes);

        assertEquals(firebirdZoneId, mapping.toTimeZoneId(offsetMinutes), "Unexpected timezone id");
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "{4} => {0}")
    @MethodSource("testCases")
    void testToTimeZoneId_ZoneOffset(int firebirdZoneId, int sign, int hours, int minutes, ZoneId zoneId) {
        assumeTrue(isInImplementationRange(hours, minutes), "Test requires ZoneOffset");

        assertEquals(firebirdZoneId, mapping.toTimeZoneId((ZoneOffset) zoneId), "Unexpected mapping");
    }

    @SuppressWarnings("unused")
    @ParameterizedTest
    @MethodSource("testCases")
    void testIsSupportedOffsetTimezone(int firebirdZoneId, int sign, int hours, int minutes) {
        final boolean expectedSupport = isInImplementationRange(hours, minutes);

        assertEquals(expectedSupport, mapping.isSupportedOffsetTimezone(firebirdZoneId));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsOffsetTimeZone(int firebirdZoneId) {
        assertTrue(mapping.isOffsetTimeZone(firebirdZoneId));
    }

    /**
     * @return {@code true} if the offset is within the Jaybird implementation range.
     */
    private static boolean isInImplementationRange(int hours, int minutes) {
        return hours >= 0 && (hours < 17 || hours == 18 && minutes == 0);
    }

    private static int getOffsetMinutes(int sign, int hours, int minutes) {
        return sign * (hours * 60 + minutes);
    }

    static Stream<Arguments> testCases() {
        List<Arguments> testData = new ArrayList<>(24 * 4 * 2 + 2);
        testData.add(testCase(1439, 1, 0, 0, ZoneOffset.ofHoursMinutes(0, 0)));
        // Maximum range supported by Jaybird due to ZoneOffset limit of [-18:00, +18:00]
        testData.add(testCase(SIGN_POSITIVE, 18, 0));
        testData.add(testCase(SIGN_NEGATIVE, 18, 0));
        for (int hours = 0; hours < 17; hours++) {
            for (int minutes = 0; minutes < 60; minutes += 15) {
                if (hours == 0 && minutes == 0) {
                    // negative indistinguishable from positive for offset 00:00; separate explicit test case
                    continue;
                }
                testData.add(testCase(SIGN_POSITIVE, hours, minutes));
                testData.add(testCase(SIGN_NEGATIVE, hours, minutes));
            }
        }

        // technically valid for Firebird, but considered out of range due to ZoneOffset limit of [-18:00, +18:00]
        testData.add(testCase(0, SIGN_NEGATIVE, 23, 59, UTC_ZONE_UID));
        testData.add(testCase(2878, SIGN_POSITIVE, 23, 59, UTC_ZONE_UID));
        for (int hours = 18; hours < 24; hours++) {
            for (int minutes = 0; minutes < 60; minutes += 15) {
                if (hours == 18 && minutes == 0) {
                    // in range value, skip
                    continue;
                }
                testData.add(outOfRangeCase(SIGN_POSITIVE, hours, minutes));
                testData.add(outOfRangeCase(SIGN_NEGATIVE, hours, minutes));
            }
        }
        return testData.stream();
    }

    private static Arguments testCase(int sign, int hours, int minutes) {
        int firebirdZoneId = calculateFirebirdZoneId(sign, hours, minutes);
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes);
        return testCase(firebirdZoneId, sign, hours, minutes, zoneOffset);
    }

    private static Arguments outOfRangeCase(int sign, int hours, int minutes) {
        int firebirdZoneId = calculateFirebirdZoneId(sign, hours, minutes);
        return testCase(firebirdZoneId, sign, hours, minutes, UTC_ZONE_UID);
    }

    private static int calculateFirebirdZoneId(int sign, int hours, int minutes) {
        return sign * (hours * 60 + minutes) + 1439;
    }

    private static Arguments testCase(int firebirdZoneId, int sign, int hours, int minutes, ZoneId zoneId) {
        return Arguments.of(firebirdZoneId, sign, hours, minutes, zoneId);
    }
}
