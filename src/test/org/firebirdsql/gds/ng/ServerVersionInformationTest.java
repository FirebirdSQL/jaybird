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
package org.firebirdsql.gds.ng;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link org.firebirdsql.gds.ng.ServerVersionInformation}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class ServerVersionInformationTest {

    @ParameterizedTest
    @CsvSource({
            // Too low version "upgrades" to 1.0
            "0, 9, VERSION_1_0",
            "1, 0, VERSION_1_0",
            // 1.5 uses same as 1.0
            "1, 5, VERSION_1_0",
            "2, 0, VERSION_2_0",
            // Higher versions use same as 2.0
            "2, 5, VERSION_2_0",
            "3, 0, VERSION_2_0",
            "4, 0, VERSION_2_0",
            "5, 0, VERSION_2_0",
    })
    void testGetForVersion(int major, int minor, ServerVersionInformation expectedServerVersionInformation) {
        assertSame(expectedServerVersionInformation, ServerVersionInformation.getForVersion(major, minor));
    }

    @Test
    void testGetForVersion_versionTooHigh_VERSION_2_0() {
        final ServerVersionInformation[] values = ServerVersionInformation.values();
        assumeThat("Expected VERSION_2_0 to be the highest version",
                values[values.length - 1], sameInstance(ServerVersionInformation.VERSION_2_0));
        assertSame(ServerVersionInformation.VERSION_2_0,
                ServerVersionInformation.getForVersion(99, 0), "Expected VERSION_2_0");
    }
}
