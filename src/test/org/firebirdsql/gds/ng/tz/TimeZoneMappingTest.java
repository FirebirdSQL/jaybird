// SPDX-FileCopyrightText: Copyright 2019-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.tz;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * See also {@link TimeZoneByNameMappingTest} and {@link TimeZoneOffsetMappingTest}.
 */
class TimeZoneMappingTest {

    private static final int CURRENT_MIN_ZONE_ID;
    static {
        try {
            Properties timeZoneMapping = TimeZoneMapping.loadTimeZoneMapping();
            CURRENT_MIN_ZONE_ID = Integer.parseInt(timeZoneMapping.getProperty(TimeZoneMapping.KEY_MIN_ZONE_ID));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private static final TimeZoneMapping mapping = TimeZoneMapping.getInstance();

    @Test
    void invalidZoneIdYieldsUTC() {
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(CURRENT_MIN_ZONE_ID - 1));
    }

    @Test
    void timeZoneById_outOfRangeZoneIdYieldsUTC_negativeOne() {
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(-1));
    }

    @Test
    void timeZoneById_outOfRangeZoneIdYieldsUTC_exceeds0xFFFF() {
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(0xFFFF + 1));
    }

    @Test
    void toOffsetMinutes_outOfRangeZoneIdYieldsZero_lowerLimit() {
        assertEquals(0, mapping.toOffsetMinutes(-1));
    }

    @Test
    void toOffsetMinutes_outOfRangeZoneIdYieldsZero_upperLimit() {
        assertEquals(0, mapping.toOffsetMinutes(2879));
    }

    @Test
    void toTimeZoneId_outOfRangeOffsetYieldsEncodedZeroOffset_lowerLimit() {
        assertEquals(1439, mapping.toTimeZoneId(-1440));
    }

    @Test
    void toTimeZoneId_outOfRangeOffsetYieldsEncodedZeroOffset_upperLimit() {
        assertEquals(1439, mapping.toTimeZoneId(1440));
    }

}