/*
 * $Id$
 *
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
package org.firebirdsql.management;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for {@link org.firebirdsql.management.FBServiceManager}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBServiceManager extends FBJUnit4TestBase{

    @Test
    public void testGetServerVersion() throws Exception {
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setHost(FBTestProperties.DB_SERVER_URL);
        fbServiceManager.setPort(FBTestProperties.DB_SERVER_PORT);
        fbServiceManager.setUser(FBTestProperties.DB_USER);
        fbServiceManager.setPassword(FBTestProperties.DB_PASSWORD);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }
}
