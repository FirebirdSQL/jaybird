package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.jna.JnaServiceConnection;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.FBTestProperties.getDefaultServiceProperties;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OO API service connection implementation.
 * See {@link org.firebirdsql.nativeoo.gds.ng.IServiceConnectionImpl}.
 *
 * @since 5.0
 */
class IServiceConnectionImplTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsFBOONativeOnly();

    private final AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbServiceProperties connectionInfo = getDefaultServiceProperties();

    @Test
    void construct_clientLibraryNull_IllegalArgument() {
        assertThrows(NullPointerException.class, () -> new IServiceConnectionImpl(null, connectionInfo));
    }

    @Test
    void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        IServiceConnectionImpl connection = new IServiceConnectionImpl(clientLibrary, connectionInfo);

        assertSame(clientLibrary, connection.getClientLibrary(), "Expected returned client library to be identical");
    }

    @Test
    void identify_unconnected() throws Exception {
        IServiceConnectionImpl connection = new IServiceConnectionImpl(factory.getClientLibrary(), connectionInfo);

        FbService db = connection.identify();
        try {
            assertFalse(db.isAttached(), "Expected isAttached() to return false");
            assertNull(db.getServerVersion(), "Expected version string to be null");
            assertNull(db.getServerVersion(), "Expected version should be null");
        } finally {
            closeQuietly(db);
        }
    }
}
