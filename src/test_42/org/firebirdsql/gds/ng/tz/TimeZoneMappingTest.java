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

import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

/**
 * See also {@link TimeZoneByNameMappingTest} and {@link TimeZoneOffsetMappingTest}.
 */
public class TimeZoneMappingTest {

    private static final TimeZoneMapping mapping = TimeZoneMapping.getInstance();

    @Test
    public void invalidZoneIdYieldsUTC() {
        // 64904 is current lowest id, this is one below that
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(64903));
    }

    @Test
    public void timeZoneById_outOfRangeZoneIdYieldsUTC_negativeOne() {
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(-1));
    }

    @Test
    public void timeZoneById_outOfRangeZoneIdYieldsUTC_exceeds0xFFFF() {
        assertEquals(ZoneOffset.UTC, mapping.timeZoneById(0xFFFF + 1));
    }

    @Test
    public void toOffsetMinutes_outOfRangeZoneIdYieldsZero_lowerLimit() {
        assertEquals(0, mapping.toOffsetMinutes(-1));
    }

    @Test
    public void toOffsetMinutes_outOfRangeZoneIdYieldsZero_upperLimit() {
        assertEquals(0, mapping.toOffsetMinutes(2879));
    }

    @Test
    public void toTimeZoneId_outOfRangeOffsetYieldsEncodedZeroOffset_lowerLimit() {
        assertEquals(1439, mapping.toTimeZoneId(-1440));
    }

    @Test
    public void toTimeZoneId_outOfRangeOffsetYieldsEncodedZeroOffset_upperLimit() {
        assertEquals(1439, mapping.toTimeZoneId(1440));
    }

}