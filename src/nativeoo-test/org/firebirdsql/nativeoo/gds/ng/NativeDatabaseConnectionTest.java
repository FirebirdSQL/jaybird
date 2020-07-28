package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.junit.Assert.*;

public class NativeDatabaseConnectionTest {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsFBOONativeOnly();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final FbConnectionProperties connectionInfo;
    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
    }

    @Test
    public void construct_clientLibraryNull_IllegalArgument() throws Exception {
        expectedException.expect(NullPointerException.class);

        new NativeDatabaseConnection(null, connectionInfo);
    }

    @Test
    public void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        NativeDatabaseConnection connection = new NativeDatabaseConnection(clientLibrary, connectionInfo);

        assertSame("Expected returned client library to be identical", clientLibrary, connection.getClientLibrary());
    }

    @Test
    public void identify_unconnected() throws Exception {
        NativeDatabaseConnection connection = new NativeDatabaseConnection(factory.getClientLibrary(), connectionInfo);

        FbDatabase db = connection.identify();

        assertFalse("Expected isAttached() to return false", db.isAttached());
        assertNull("Expected version string to be null", db.getServerVersion());
        assertNull("Expected version should be null", db.getServerVersion());
    }

}
