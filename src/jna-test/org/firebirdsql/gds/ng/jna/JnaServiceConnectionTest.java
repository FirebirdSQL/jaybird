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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.FBTestProperties.getDefaultServiceProperties;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaServiceConnectionTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbServiceProperties connectionInfo = getDefaultServiceProperties();

    @Test
    void construct_clientLibraryNull_IllegalArgument() {
        assertThrows(NullPointerException.class, () -> new JnaServiceConnection(null, connectionInfo));
    }

    @Test
    void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        JnaServiceConnection connection = new JnaServiceConnection(clientLibrary, connectionInfo);

        assertSame(clientLibrary, connection.getClientLibrary(), "Expected returned client library to be identical");
    }

    @Test
    void identify_unconnected() throws Exception {
        JnaServiceConnection connection = new JnaServiceConnection(factory.getClientLibrary(), connectionInfo);

        FbService db = connection.identify();
        try {
            assertFalse(db.isAttached(), "Expected isAttached() to return false");
            assertEquals(0, db.getHandle(), "Expected zero-valued connection handle");
            assertNull(db.getServerVersion(), "Expected version string to be null");
            assertNull(db.getServerVersion(), "Expected version should be null");
        } finally {
            closeQuietly(db);
        }
    }
}
