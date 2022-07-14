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

abstract class TimeZoneCodecAbstractTimestampTzTest extends TimeZoneCodecAbstractTest {

    // 2019-03-09T07:45:51+01:00 Defined using offset
    static final String TIMESTAMPTZ_AT_2019_03_09_OFFSET_1_NETWORK_HEX = "0000E4B70E83AAF0000005DB";
    static final String TIMESTAMPTZ_AT_2019_03_09_OFFSET_1_LE_HEX = "B7E40000F0AA830EDB050000";
    static final String TIMESTAMPTZ_AT_2019_03_09_OFFSET_1_BE_HEX = "0000E4B70E83AAF005DB0000";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_NETWORK_HEX = "0000E4B70E83AAF0000005DB0000003C";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_NETWORK_HEX_ENCODED = "0000E4B70E83AAF0000005DB00000000";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_LE_HEX = "B7E40000F0AA830EDB053C00";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_OFFSET_1_BE_HEX = "0000E4B70E83AAF005DB003C";
    // 2019-07-01T07:45:51+01:00 Defined using offset
    static final String TIMESTAMPTZ_AT_2019_07_01_OFFSET_1_NETWORK_HEX = "0000E5290E83AAF0000005DB";
    static final String EXTIMESTAMPTZ_AT_2019_07_01_OFFSET_1_NETWORK_HEX = "0000E5290E83AAF0000005DB0000003C";
    static final String EXTIMESTAMPTZ_AT_2019_07_01_OFFSET_1_NETWORK_HEX_ENCODED = "0000E5290E83AAF0000005DB00000000";
    // 2019-03-09T07:45:51 Europe/Amsterdam Defined using Europe/Amsterdam
    static final String TIMESTAMPTZ_AT_2019_03_09_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE49";
    static final String TIMESTAMPTZ_AT_2019_03_09_ZONE_LE_HEX = "B7E40000F0AA830E49FE0000";
    static final String TIMESTAMPTZ_AT_2019_03_09_ZONE_BE_HEX = "0000E4B70E83AAF0FE490000";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE490000003C";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_ZONE_NETWORK_HEX_ENCODED = "0000E4B70E83AAF0FFFFFE4900000000";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_ZONE_LE_HEX = "B7E40000F0AA830E49FE3C00";
    static final String EXTIMESTAMPTZ_AT_2019_03_09_ZONE_BE_HEX = "0000E4B70E83AAF0FE49003C";
    // 2019-07-01T07:45:51 Europe/Amsterdam Defined using Europe/Amsterdam
    static final String TIMESTAMPTZ_AT_2019_07_01_ZONE_NETWORK_HEX = "0000E5290C5E59F0FFFFFE49";
    static final String EXTIMESTAMPTZ_AT_2019_07_01_ZONE_NETWORK_HEX = "0000E5290C5E59F0FFFFFE4900000078";
    static final String EXTIMESTAMPTZ_AT_2019_07_01_ZONE_NETWORK_HEX_ENCODED = "0000E5290C5E59F0FFFFFE4900000000";

}
