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
package org.firebirdsql.util;

import java.util.Arrays;

/**
 * Firebird error code facility.
 * <p>
 * See also {@code src\include\firebird\impl\msg_helper.h} (of Firebird 5.0)
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
enum Facility {

    JRD(0),
    QLI(1),
    GFIX(3),
    GPRE(4),
    DSQL(7),
    DYN(8),
    INSTALL(10),
    TEST(11),
    GBAK(12),
    SQLERR(13),
    SQLWARN(14),
    JRD_BUGCHK(15),
    ISQL(17),
    GSEC(18),
    GSTAT(21),
    FBSVCMGR(22),
    UTL(23),
    NBACKUP(24),
    FBTRACEMGR(25),
    // Jaybird error messages are defined in src/resources/org/firebirdsql/jaybird_error_msg.properties of Jaybird
    JAYBIRD(26),
    // R2DBC Firebird error message are defined in R2DBC Firebird project
    R2DBC_FIREBIRD(27),
    ;

    private static final int ISC_CODE = 0x14000000;

    private final int code;

    Facility(int code) {
        this.code = code;
    }

    int code() {
        return code;
    }

    int toErrorCode(int number) {
        return ISC_CODE | ((code & 0x1F) << 16) | (number & 0x3FFF);
    }

    static Facility of(int code) {
        Facility[] map = MappingHolder.codeToFacility;
        if (0 <= code && code < map.length) {
            Facility value = map[code];
            if (value != null) {
                return value;
            }
        }
        throw new IllegalArgumentException("No facility defined for code " + code);
    }

    private static class MappingHolder {

        static final Facility[] codeToFacility;

        static {
            Facility[] facilities = Facility.values();
            int maxFacility = Arrays.stream(facilities).mapToInt(Facility::code).max().getAsInt();
            Facility[] map = new Facility[maxFacility + 1];
            for (Facility facility : facilities) {
                map[facility.code] = facility;
            }
            codeToFacility = map;
        }
    }
}
