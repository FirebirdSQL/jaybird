// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSServerVersion;
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

    @Test@SuppressWarnings("DataFlowIssue")

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
            assertEquals(GDSServerVersion.INVALID_VERSION, db.getServerVersion(), "Expected version to be invalid");
        } finally {
            closeQuietly(db);
        }
    }
}
