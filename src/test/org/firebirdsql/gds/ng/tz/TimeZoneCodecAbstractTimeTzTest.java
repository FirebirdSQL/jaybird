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

abstract class TimeZoneCodecAbstractTimeTzTest extends TimeZoneCodecAbstractTest {

    // 07:45:51+01:00 defined using offset
    static final String TIMETZ_OFFSET_1_NETWORK_HEX = "0E83AAF0000005DB";
    static final String TIMETZ_OFFSET_1_LE_HEX = "F0AA830EDB050000";
    static final String TIMETZ_OFFSET_1_BE_HEX = "0E83AAF005DB0000";
    static final String EXTIMETZ_OFFSET_1_NETWORK_HEX = "0E83AAF0000005DB0000003C";
    static final String EXTIMETZ_OFFSET_1_NETWORK_HEX_ENCODED = "0E83AAF0000005DB00000000";
    static final String EXTIMETZ_OFFSET_1_LE_HEX = "F0AA830EDB053C00";
    static final String EXTIMETZ_OFFSET_1_BE_HEX = "0E83AAF005DB003C";
    // 07:45:51 Europe/Amsterdam Defined using Europe/Amsterdam
    static final String TIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE49";
    static final String TIMETZ_ZONE_LE_HEX = "F0AA830E49FE0000";
    static final String TIMETZ_ZONE_BE_HEX = "0E83AAF0FE490000";
    static final String EXTIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE490000003C";
    static final String EXTIMETZ_ZONE_NETWORK_HEX_ENCODED = "0E83AAF0FFFFFE4900000000";
    static final String EXTIMETZ_ZONE_LE_HEX = "F0AA830E49FE3C00";
    static final String EXTIMETZ_ZONE_BE_HEX = "0E83AAF0FE49003C";

}
