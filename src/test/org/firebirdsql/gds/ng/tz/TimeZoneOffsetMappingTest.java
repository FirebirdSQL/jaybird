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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class TimeZoneOffsetMappingTest {

    private static final int SIGN_POSITIVE = 1;
    private static final int SIGN_NEGATIVE = -1;
    private static final TimeZoneMapping mapping = TimeZoneMapping.getInstance();
    private static final ZoneId UTC_ZONE_UID = ZoneOffset.UTC;

    private final int firebirdZoneId;
    private final int sign;
    private final int hours;
    private final int minutes;
    private final ZoneId zoneId;

    public TimeZoneOffsetMappingTest(int firebirdZoneId, int sign, int hours, int minutes, ZoneId zoneId) {
        this.firebirdZoneId = firebirdZoneId;
        this.sign = sign;
        this.hours = hours;
        this.minutes = minutes;
        this.zoneId = zoneId;
    }

    @Test
    public void testTimeZoneById() {
        assertEquals("Unexpected mapping", zoneId, mapping.timeZoneById(firebirdZoneId));
    }

    @Test
    public void testToOffsetMinutes() {
        final int offsetMinutes = getOffsetMinutes();

        assertEquals("Unexpected offset", offsetMinutes, mapping.toOffsetMinutes(firebirdZoneId));
    }

    @Test
    public void testToTimeZoneId_minutes() {
        final int offsetMinutes = getOffsetMinutes();

        assertEquals("Unexpected timezone id", firebirdZoneId, mapping.toTimeZoneId(offsetMinutes));
    }

    @Test
    public void testToTimeZoneId_ZoneOffset() {
        assumeTrue("Test requires ZoneOffset", isInImplementationRange());

        assertEquals("Unexpected mapping", firebirdZoneId, mapping.toTimeZoneId((ZoneOffset) zoneId));
    }

    @Test
    public void testIsSupportedOffsetTimezone() {
        final boolean expectedSupport = isInImplementationRange();

        assertEquals(expectedSupport, mapping.isSupportedOffsetTimezone(firebirdZoneId));
    }

    @Test
    public void testIsOffsetTimeZone() {
        assertTrue(mapping.isOffsetTimeZone(firebirdZoneId));
    }

    /**
     * @return {@code true} if the offset is within the Jaybird implementation range.
     */
    private boolean isInImplementationRange() {
        return hours >= 0 && (hours < 17 || hours == 18 && minutes == 0);
    }

    private int getOffsetMinutes() {
        return sign * (hours * 60 + minutes);
    }

    @Parameterized.Parameters(name = "{0} => {4}")
    public static Collection<Object[]> testCases() {
        List<Object[]> testData = new ArrayList<>(24 * 4 * 2 + 2);
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
        return testData;
    }

    private static Object[] testCase(int sign, int hours, int minutes) {
        int firebirdZoneId = calculateFirebirdZoneId(sign, hours, minutes);
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes);
        return testCase(firebirdZoneId, sign, hours, minutes, zoneOffset);
    }

    private static Object[] outOfRangeCase(int sign, int hours, int minutes) {
        int firebirdZoneId = calculateFirebirdZoneId(sign, hours, minutes);
        return testCase(firebirdZoneId, sign, hours, minutes, UTC_ZONE_UID);
    }

    private static int calculateFirebirdZoneId(int sign, int hours, int minutes) {
        return sign * (hours * 60 + minutes) + 1439;
    }

    private static Object[] testCase(int firebirdZoneId, int sign, int hours, int minutes, ZoneId zoneId) {
        return new Object[] { firebirdZoneId, sign, hours, minutes, zoneId };
    }
}
