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

import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.firebirdsql.gds.ISCConstants.SQL_TIMESTAMP_TZ_EX;
import static org.firebirdsql.util.ByteArrayHelper.fromHexString;

class TimeZoneCodecExtendedTimestampTzTest extends TimeZoneCodecAbstractTimestampTzTest {

    @Override
    int getTzType() {
        return SQL_TIMESTAMP_TZ_EX;
    }

    @Override
    byte[] getOffsetNetworkAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_NETWORK_HEX);
    }

    @Override
    byte[] getOffsetNetworkAt2019_03_09Expected() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_NETWORK_HEX_ENCODED);
    }

    @Override
    byte[] getOffsetNetworkAt2019_07_01Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_07_01_OFFSET_1_NETWORK_HEX);
    }

    @Override
    byte[] getOffsetNetworkAt2019_07_01Expected() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_07_01_OFFSET_1_NETWORK_HEX_ENCODED);
    }

    @Override
    OffsetDateTime getOffsetExpectedOffsetDateTimeAt2019_07_01() {
        return OFFSET_1_OFFSET_DATE_TIME_AT_2019_07_01;
    }

    @Override
    OffsetTime getOffsetExpectedOffsetTimeAt2019_07_01() {
        return OFFSET_1_OFFSET_TIME;
    }

    @Override
    byte[] getZoneNetworkAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getZoneNetworkAt2019_03_09Expected() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_ZONE_NETWORK_HEX_ENCODED);
    }

    @Override
    byte[] getZoneNetworkAt2019_07_01Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_07_01_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getZoneNetworkAt2019_07_01Expected() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_07_01_ZONE_NETWORK_HEX_ENCODED);
    }

    @Override
    OffsetDateTime getZoneExpectedOffsetDateTimeAt2019_07_01() {
        return OFFSET_2_OFFSET_DATE_TIME_AT_2019_07_01;
    }

    @Override
    OffsetTime getZoneExpectedOffsetTimeAt2019_07_01() {
        return OFFSET_2_OFFSET_TIME;
    }

    @Override
    byte[] getOffsetLeAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_LE_HEX);
    }

    @Override
    byte[] getOffsetLeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_AT_2019_03_09_OFFSET_1_LE_HEX);
    }

    @Override
    byte[] getZoneLeAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_ZONE_LE_HEX);
    }

    @Override
    byte[] getZoneLeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_AT_2019_03_09_ZONE_LE_HEX);
    }

    @Override
    byte[] getOffsetBeAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_BE_HEX);
    }

    @Override
    byte[] getOffsetBeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_AT_2019_03_09_OFFSET_1_BE_HEX);
    }

    @Override
    byte[] getZoneBeAt2019_03_09Input() {
        return fromHexString(EXTIMESTAMPTZ_AT_2019_03_09_ZONE_BE_HEX);
    }

    @Override
    byte[] getZoneBeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_AT_2019_03_09_ZONE_BE_HEX);
    }

}