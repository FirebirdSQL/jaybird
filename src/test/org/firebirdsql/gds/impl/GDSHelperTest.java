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
package org.firebirdsql.gds.impl;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GDSHelperTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void testCompareToVersion() throws Exception {
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager()) {
            GDSHelper gdsHelper = connection.getGDSHelper();
            final int actualMajor = gdsHelper.getDatabaseProductMajorVersion();
            final int actualMinor = gdsHelper.getDatabaseProductMinorVersion();

            assertEquals(0, gdsHelper.compareToVersion(actualMajor, actualMinor),
                    "compareToVersion should return 0 for identical versions");
            assertEquals(0, gdsHelper.compareToVersion(actualMajor),
                    "compareToVersion should return 0 for identical versions");
            assertTrue(gdsHelper.compareToVersion(actualMajor, actualMinor + 1) < 0,
                    "compareToVersion should return negative value for comparison with same major and bigger minor");
            assertTrue(gdsHelper.compareToVersion(actualMajor, actualMinor - 1) > 0,
                    "compareToVersion should return positive value for comparison with same major and smaller minor");
            assertTrue(gdsHelper.compareToVersion(actualMajor + 1, 0) < 0,
                    "compareToVersion should return negative value for comparison with bigger major");
            assertTrue(gdsHelper.compareToVersion(actualMajor + 1) < 0,
                    "compareToVersion should return negative value for comparison with bigger major");
            assertTrue(gdsHelper.compareToVersion(actualMajor - 1, 999999) > 0,
                    "compareToVersion should return positive value for comparison with smaller major");
            assertTrue(gdsHelper.compareToVersion(actualMajor - 1) > 0,
                    "compareToVersion should return positive value for comparison with smaller major");
        }
    }

}
