/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.jdbc.FBConnection;

/**
 * Tests for {@link GDSHelper}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestGDSHelper extends FBTestBase {

    public TestGDSHelper(String name) {
        super(name);
    }

    public void testCompareToVersion() throws Exception {
        FBConnection connection = (FBConnection) getConnectionViaDriverManager();
        GDSHelper gdsHelper = connection.getGDSHelper();
        final int actualMajor = gdsHelper.getDatabaseProductMajorVersion();
        final int actualMinor = gdsHelper.getDatabaseProductMinorVersion();

        try {
            assertEquals("compareToVersion should return 0 for identical versions", 0,
                    gdsHelper.compareToVersion(actualMajor, actualMinor));
            assertTrue(
                    "compareToVersion should return negative value for comparison with same major and bigger minor",
                    gdsHelper.compareToVersion(actualMajor, actualMinor + 1) < 0);
            assertTrue(
                    "compareToVersion should return positive value for comparison with same major and smaller minor",
                    gdsHelper.compareToVersion(actualMajor, actualMinor - 1) > 0);
            assertTrue(
                    "compareToVersion should return negative value for comparison with bigger major",
                    gdsHelper.compareToVersion(actualMajor + 1, 0) < 0);
            assertTrue(
                    "compareToVersion should return positive value for comparison with smaller major",
                    gdsHelper.compareToVersion(actualMajor - 1, 999999) > 0);
        } finally {
            connection.close();
        }
    }

}
