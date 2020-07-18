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

import static org.firebirdsql.gds.ISCConstants.SQL_TIMESTAMP_TZ;
import static org.firebirdsql.gds.ISCConstants.SQL_TIME_TZ;
import static org.firebirdsql.util.ByteArrayHelper.fromHexString;

public class DefaultTimeZoneCodecTest extends TimeZoneCodecAbstractTest {

    public DefaultTimeZoneCodecTest() {
        super(SQL_TIME_TZ, SQL_TIMESTAMP_TZ);
    }

    @Override
    byte[] getTimestampTzOffsetNetworkInput() {
        return fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetNetworkExpected() {
        return getTimestampTzOffsetNetworkInput();
    }

    @Override
    byte[] getTimestampTzZoneNetworkInput() {
        return fromHexString(TIMESTAMPTZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getTimestampTzZoneNetworkExpected() {
        return getTimestampTzOffsetNetworkInput();
    }

    @Override
    byte[] getTimestampTzOffsetLeInput() {
        return fromHexString(TIMESTAMPTZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetLeExpected() {
        return getTimestampTzOffsetLeInput();
    }

    @Override
    byte[] getTimestampTzZoneLeInput() {
        return fromHexString(TIMESTAMPTZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneLeExpected() {
        return getTimestampTzZoneLeInput();
    }

    @Override
    byte[] getTimestampTzOffsetBeInput() {
        return fromHexString(TIMESTAMPTZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimestampTzOffsetBeExpected() {
        return getTimestampTzOffsetBeInput();
    }

    @Override
    byte[] getTimestampTzZoneBeInput() {
        return fromHexString(TIMESTAMPTZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getTimestampTzZoneBeExpected() {
        return getTimestampTzZoneBeInput();
    }

    @Override
    byte[] getTimeTzOffsetNetworkInput() {
        return fromHexString(TIMETZ_OFFSET_NETWORK_HEX);
    }

    @Override
    byte[] getTimeTzOffsetNetworkExpected() {
        return getTimeTzOffsetNetworkInput();
    }

    @Override
    byte[] getTimeTzZoneNetworkInput() {
        return fromHexString(TIMETZ_ZONE_NETWORK_HEX);
    }

    @Override
    byte[] getTimeTzZoneNetworkExpected() {
        return getTimeTzZoneNetworkInput();
    }

    @Override
    byte[] getTimeTzOffsetLeInput() {
        return fromHexString(TIMETZ_OFFSET_LE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetLeExpected() {
        return getTimeTzOffsetLeInput();
    }

    @Override
    byte[] getTimeTzZoneLeInput() {
        return fromHexString(TIMETZ_ZONE_LE_HEX);
    }

    @Override
    byte[] getTimeTzZoneLeExpected() {
        return getTimeTzZoneLeInput();
    }

    @Override
    byte[] getTimeTzOffsetBeInput() {
        return fromHexString(TIMETZ_OFFSET_BE_HEX);
    }

    @Override
    byte[] getTimeTzOffsetBeExpected() {
        return getTimeTzOffsetBeInput();
    }

    @Override
    byte[] getTimeTzZoneBeInput() {
        return fromHexString(TIMETZ_ZONE_BE_HEX);
    }

    @Override
    byte[] getTimeTzZoneBeExpected() {
        return getTimeTzZoneBeInput();
    }

}