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

import static org.firebirdsql.gds.ISCConstants.SQL_TIME_TZ_EX;
import static org.firebirdsql.jaybird.util.ByteArrayHelper.fromHexString;

class TimeZoneCodecExtendedTimeTzTest extends TimeZoneCodecAbstractTimeTzTest {

    @Override
    int getTzType() {
        return SQL_TIME_TZ_EX;
    }

    @Override
    byte[] getOffsetNetworkAt2019_03_09Input() {
        return fromHexString(EXTIMETZ_OFFSET_1_NETWORK_HEX);
    }

    @Override
    byte[] getOffsetNetworkAt2019_03_09Expected() {
        return fromHexString(EXTIMETZ_OFFSET_1_NETWORK_HEX_ENCODED);
    }

    @Override
    protected byte[] getOffsetNetworkAt2019_07_01Input() {
        return getOffsetNetworkAt2019_03_09Input();
    }

    @Override
    protected byte[] getOffsetNetworkAt2019_07_01Expected() {
        return getOffsetNetworkAt2019_03_09Expected();
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
        return fromHexString(EXTIMETZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getZoneNetworkAt2019_03_09Expected() {
        return fromHexString(EXTIMETZ_ZONE_NETWORK_HEX_ENCODED);
    }

    @Override
    byte[] getZoneNetworkAt2019_07_01Input() {
        return getZoneNetworkAt2019_03_09Input();
    }

    @Override
    byte[] getZoneNetworkAt2019_07_01Expected() {
        return getZoneNetworkAt2019_03_09Expected();
    }

    @Override
    OffsetDateTime getZoneExpectedOffsetDateTimeAt2019_07_01() {
        return OFFSET_2_OFFSET_DATE_TIME_AT_2019_07_01;
    }

    @Override
    OffsetTime getZoneExpectedOffsetTimeAt2019_07_01() {
        // As if the value is still at 2020-01-01
        return OFFSET_1_OFFSET_TIME;
    }

    @Override
    byte[] getOffsetLeAt2019_03_09Input() {
        return fromHexString(EXTIMETZ_OFFSET_1_LE_HEX);
    }

    @Override
    byte[] getOffsetLeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_OFFSET_1_LE_HEX);
    }

    @Override
    byte[] getZoneLeAt2019_03_09Input() {
        return fromHexString(EXTIMETZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getZoneLeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getOffsetBeAt2019_03_09Input() {
        return fromHexString(EXTIMETZ_OFFSET_1_BE_HEX);
    }

    @Override
    byte[] getOffsetBeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_OFFSET_1_BE_HEX);
    }

    @Override
    byte[] getZoneBeAt2019_03_09Input() {
        return fromHexString(EXTIMETZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getZoneBeAt2019_03_09Expected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_ZONE_BE_HEX);
    }

}