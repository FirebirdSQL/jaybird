package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class TestIServiceConnectionImpl {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) GDSFactory.getDatabaseFactoryForType(GDSType.getType("FBOONATIVE"));

    private final FbServiceProperties connectionInfo;
    {
        connectionInfo = new FbServiceProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
    }

    @Test
    public void construct_clientLibraryNull_IllegalArgument() throws Exception {
        expectedException.expect(NullPointerException.class);

        new IServiceConnectionImpl(null, connectionInfo);
    }

    @Test
    public void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        IServiceConnectionImpl connection = new IServiceConnectionImpl(clientLibrary, connectionInfo);

        assertSame("Expected returned client library to be identical", clientLibrary, connection.getClientLibrary());
    }

    @Test
    public void identify_unconnected() throws Exception {
        IServiceConnectionImpl connection = new IServiceConnectionImpl(factory.getClientLibrary(), connectionInfo);

        FbService db = connection.identify();

        assertFalse("Expected isAttached() to return false", db.isAttached());
        assertNull("Expected version string to be null", db.getServerVersion());
        assertNull("Expected version should be null", db.getServerVersion());
    }
}
