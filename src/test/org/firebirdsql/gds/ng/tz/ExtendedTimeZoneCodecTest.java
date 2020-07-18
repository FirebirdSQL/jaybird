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

import static org.firebirdsql.gds.ISCConstants.SQL_TIMESTAMP_TZ_EX;
import static org.firebirdsql.gds.ISCConstants.SQL_TIME_TZ_EX;
import static org.firebirdsql.util.ByteArrayHelper.fromHexString;

public class ExtendedTimeZoneCodecTest extends TimeZoneCodecAbstractTest {

    public ExtendedTimeZoneCodecTest() {
        super(SQL_TIME_TZ_EX, SQL_TIMESTAMP_TZ_EX);
    }

    @Override
    byte[] getTimestampTzOffsetNetworkInput() {
        return fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetNetworkExpected() {
        return fromHexString(EXTIMESTAMPTZ_OFFSET_NETWORK_HEX_ENCODED);
    }

    @Override
    byte[] getTimestampTzZoneNetworkInput() {
        return fromHexString(EXTIMESTAMPTZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getTimestampTzZoneNetworkExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetLeInput() {
        return fromHexString(EXTIMESTAMPTZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetLeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneLeInput() {
        return fromHexString(EXTIMESTAMPTZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneLeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetBeInput() {
        return fromHexString(EXTIMESTAMPTZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetBeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneBeInput() {
        return fromHexString(EXTIMESTAMPTZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneBeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMESTAMPTZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetNetworkInput() {
        return fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX);
    }

    @Override
    byte[] getTimeTzOffsetNetworkExpected() {
        return fromHexString(EXTIMETZ_OFFSET_NETWORK_HEX_ENCODED);
    }

    @Override
    byte[] getTimeTzZoneNetworkInput() {
        return fromHexString(EXTIMETZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getTimeTzZoneNetworkExpected() {
        return getTimeTzZoneNetworkInput();
    }

    @Override
    byte[] getTimeTzOffsetLeInput() {
        return fromHexString(EXTIMETZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetLeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimeTzZoneLeInput() {
        return fromHexString(EXTIMETZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimeTzZoneLeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetBeInput() {
        return fromHexString(EXTIMETZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetBeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimeTzZoneBeInput() {
        return fromHexString(EXTIMETZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getTimeTzZoneBeExpected() {
        // Encoded result is identical to that for the non-EX version (secondary offset is zeroed)
        return fromHexString(TIMETZ_ZONE_BE_HEX);
    }

}