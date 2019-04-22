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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFirebirdVersionMetaData {

    @Test
    public void shouldReturn4_0ForFirebird4_0() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V4.0.0.459 Firebird 4.0");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_4_0, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

    @Test
    public void shouldReturn3_0ForFirebird3_0() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V3.0.1.32609 Firebird 3.0");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_3_0, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

    @Test
    public void shouldReturn2_5ForFirebird2_5() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V2.5.6.27020 Firebird 2.5");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_2_5, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

    @Test
    public void shouldReturn2_1ForFirebird2_1() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V2.1.7.18553 Firebird 2.1");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_2_1, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

    @Test
    public void shouldReturn2_0ForFirebird2_0() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V2.0.7.13318 Firebird 2.0");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_2_0, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

    @Test
    public void shouldReturn2_0ForFirebird1_5() throws Exception {
        GDSServerVersion version = GDSServerVersion.parseRawVersion("WI-V1.5.6.18482 Firebird 1.5");

        assertEquals(FirebirdVersionMetaData.FIREBIRD_2_0, FirebirdVersionMetaData.getVersionMetaDataFor(version));
    }

}
