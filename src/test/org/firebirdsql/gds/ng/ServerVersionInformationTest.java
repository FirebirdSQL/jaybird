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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeThat;

/**
 * Tests for {@link org.firebirdsql.gds.ng.ServerVersionInformation}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class ServerVersionInformationTest {

    @Test
    public void testGetForVersion_versionTooLow_VERSION_1_0() {
        assertSame("Expected VERSION_1_0",
                ServerVersionInformation.VERSION_1_0, ServerVersionInformation.getForVersion(0, 9));
    }

    @Test
    public void testGetForVersion_version1_0_VERSION_1_0() {
        assertSame("Expected VERSION_1_0",
                ServerVersionInformation.VERSION_1_0, ServerVersionInformation.getForVersion(1, 0));
    }

    @Test
    public void testGetForVersion_version1_5_VERSION_1_0() {
        assertSame("Expected VERSION_1_0",
                ServerVersionInformation.VERSION_1_0, ServerVersionInformation.getForVersion(1, 5));
    }

    @Test
    public void testGetForVersion_version2_0_VERSION_2_0() {
        assertSame("Expected VERSION_2_0",
                ServerVersionInformation.VERSION_2_0, ServerVersionInformation.getForVersion(2, 0));
    }

    @Test
    public void testGetForVersion_version2_1_VERSION_2_0() {
        assertSame("Expected VERSION_2_0",
                ServerVersionInformation.VERSION_2_0, ServerVersionInformation.getForVersion(2, 1));
    }

    @Test
    public void testGetForVersion_version2_5_VERSION_2_0() {
        assertSame("Expected VERSION_2_0",
                ServerVersionInformation.VERSION_2_0, ServerVersionInformation.getForVersion(2, 5));
    }

    @Test
    public void testGetForVersion_version3_0_VERSION_2_0() {
        assertSame("Expected VERSION_2_0",
                ServerVersionInformation.VERSION_2_0, ServerVersionInformation.getForVersion(3, 0));
    }

    @Test
    public void testGetForVersion_versionTooHigh_VERSION_2_0() {
        final ServerVersionInformation[] values = ServerVersionInformation.values();
        assumeThat("Expected VERSION_2_0 to be the highest version",
                values[values.length - 1], sameInstance(ServerVersionInformation.VERSION_2_0));
        assertSame("Expected VERSION_2_0",
                ServerVersionInformation.VERSION_2_0, ServerVersionInformation.getForVersion(99, 0));
    }
}
