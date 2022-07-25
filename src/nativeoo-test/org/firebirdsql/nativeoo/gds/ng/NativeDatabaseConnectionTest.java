package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *  Tests for OO API database connection. See {@link org.firebirdsql.nativeoo.gds.ng.NativeDatabaseConnection}.
 *
 * @since 5.0
 */
class NativeDatabaseConnectionTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsFBOONativeOnly();

    private final AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    @Test
    void construct_clientLibraryNull_IllegalArgument() throws Exception {
        assertThrows(NullPointerException.class, () -> new NativeDatabaseConnection(null, connectionInfo));
    }

    @Test
    void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        NativeDatabaseConnection connection = new NativeDatabaseConnection(clientLibrary, connectionInfo);

        assertSame(clientLibrary, connection.getClientLibrary(), "Expected returned client library to be identical");
    }

    @Test
    void identify_unconnected() throws Exception {
        NativeDatabaseConnection connection = new NativeDatabaseConnection(factory.getClientLibrary(), connectionInfo);

        FbDatabase db = connection.identify();

        assertFalse(db.isAttached(), "Expected isAttached() to return false");
        assertNull(db.getServerVersion(), "Expected version string to be null");
        assertNull(db.getServerVersion(), "Expected version should be null");
    }

}
