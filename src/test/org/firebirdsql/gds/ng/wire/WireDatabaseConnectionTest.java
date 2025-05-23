// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.common.BlackholeServer;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.NoArgSocketFactory;
import org.firebirdsql.common.PropertiesSocketFactory;
import org.firebirdsql.common.StringSocketFactory;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.wire.version10.Version10Descriptor;
import org.firebirdsql.gds.ng.wire.version13.Version13Descriptor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.util.Map;
import java.util.Properties;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
class WireDatabaseConnectionTest {

    @RegisterExtension
    @Order(1)
    static final GdsTypeExtension gdsType = GdsTypeExtension.excludesNativeOnly();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    /**
     * IP address which does not exist (we simply assume that this site local
     * address does not exist in the network when running the test). This
     * assumption is a lot cheaper than testing various addresses or applying
     * some heuristic based on the local interface address.
     */
    private static final String NON_EXISTENT_IP = "10.253.253.253";
    /**
     * Delta for timeout, it is about 100-120 on my machine
     */
    private static final double TIMEOUT_DELTA_MS = 200;
    
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    /**
     * Tests {@link WireConnection#isConnected()} when no connection has been
     * established yet.
     */
    @Test
    void testIsConnectedNoConnection() throws Exception {
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            assertFalse(gdsConnection.isConnected(), "Not connected, isConnected() should return false");
        }
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established.
     */
    @Test
    void testIsConnectedWithConnection() throws Exception {
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected(), "Connected to existing server, isConnected() should return true");
        }
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established and closed.
     */
    @Test
    void testIsConnectedAfterDisconnect() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        gdsConnection.socketConnect();
        gdsConnection.close();
        
        assertFalse(gdsConnection.isConnected(), "Disconnected, isConnected() should return false");
    }

    /**
     * Tests a successful connection identification phase.
     */
    @Test
    void testIdentifyExistingDb() throws Exception {
        ProtocolDescriptor expectedProtocol = new Version10Descriptor();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.getPlatformDefault(), ProtocolCollection.create(expectedProtocol))) {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected());

            FbWireDatabase database = gdsConnection.identify();

            assertEquals(org.firebirdsql.gds.ng.wire.version10.V10Database.class,
                    database.getClass(), "Unexpected FbWireDatabase implementation");
            assertEquals(expectedProtocol.getArchitecture(), gdsConnection.getProtocolArchitecture(),
                    "Unexpected architecture");
            assertEquals(expectedProtocol.getMaximumType(), gdsConnection.getProtocolType(), "Unexpected type");
            assertEquals(expectedProtocol.getVersion(), gdsConnection.getProtocolVersion(), "Unexpected version");
        }
    }

    /**
     * Tests a successful connection identification phase.
     */
    @Test
    void testIdentifyExistingDb_v13() throws Exception {
        assumeTrue(FBTestProperties.getDefaultSupportInfo().supportsProtocol(13), "Requires protocol v13 support");
        ProtocolDescriptor expectedProtocol = new Version13Descriptor();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.getPlatformDefault(), ProtocolCollection.create(expectedProtocol))) {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected());

            FbWireDatabase database = gdsConnection.identify();

            assertEquals(org.firebirdsql.gds.ng.wire.version13.V13Database.class, database.getClass(),
                    "Unexpected FbWireDatabase implementation");
            assertEquals(expectedProtocol.getArchitecture(), gdsConnection.getProtocolArchitecture(),
                    "Unexpected architecture");
            assertEquals(expectedProtocol.getMaximumType(), gdsConnection.getProtocolType(), "Unexpected type");
            assertEquals(expectedProtocol.getVersion(), gdsConnection.getProtocolVersion(), "Unexpected version");
        }
    }

    /**
     * Tests the connect timeout when connecting to a non-existent server.
     */
    @Test
    @Timeout(5)
    void testConnectTimeout_nonExistentServer() throws Exception {
        connectionInfo.setServerName(NON_EXISTENT_IP);
        connectionInfo.setConnectTimeout(2);
        long startTime = System.currentTimeMillis();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            SQLException exception = assertThrows(SQLTimeoutException.class, gdsConnection::socketConnect);
            long endTime = System.currentTimeMillis();
            assertThat("Expected error code for \"Unable to complete network request\"",
                    exception, errorCodeEquals(ISCConstants.isc_network_error));

            long difference = endTime - startTime;
            assertEquals(2000, difference, TIMEOUT_DELTA_MS, "Unexpected timeout duration (in ms)");
        }
    }

    /**
     * Tests the connect timeout if the server does not respond.
     */
    @Test
    @Timeout(5)
    void testConnectTimeout_noResponse() throws Exception {
        BlackholeServer server = new BlackholeServer();
        Thread thread = new Thread(server);
        thread.start();

        connectionInfo.setPortNumber(server.getPort());
        connectionInfo.setDatabaseName("somedb");
        connectionInfo.setConnectTimeout(2);
        long startTime = System.currentTimeMillis();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();
            SQLException exception = assertThrows(SQLTimeoutException.class, gdsConnection::identify);
            long endTime = System.currentTimeMillis();
            assertThat("Expected error code for \"Unable to complete network request\"",
                    exception, errorCodeEquals(ISCConstants.isc_network_error));

            long difference = endTime - startTime;
            assertEquals(2000, difference, TIMEOUT_DELTA_MS, "Unexpected timeout duration (in ms)");
        } finally {
            server.stop();
            thread.join();
        }
    }

    /**
     * Tests the connect timeout if the server does not respond.
     */
    @Test
    @Timeout(5)
    void testSocketTimeout_noResponse() throws Exception {
        BlackholeServer server = new BlackholeServer();
        Thread thread = new Thread(server);
        thread.start();

        connectionInfo.setPortNumber(server.getPort());
        connectionInfo.setDatabaseName("somedb");
        connectionInfo.setSoTimeout(2000);
        long startTime = System.currentTimeMillis();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();
            SQLException exception = assertThrows(SQLTimeoutException.class, gdsConnection::identify);
            long endTime = System.currentTimeMillis();
            assertThat("Expected error code for \"Unable to complete network request\"",
                    exception, errorCodeEquals(ISCConstants.isc_network_error));

            long difference = endTime - startTime;
            assertEquals(2000, difference, TIMEOUT_DELTA_MS, "Unexpected timeout duration (in ms)");
        } finally {
            server.stop();
            thread.join();
        }
    }

    /**
     * Tests if calling {@link XdrStreamAccess#getXdrIn()} obtained from {@link WireConnection#getXdrStreamAccess()}
     * throws an SQLException when not connected.
     */
    @Test
    void testUnconnected_CreateXdrIn() throws Exception {
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            assertThrows(SQLException.class, () -> gdsConnection.getXdrStreamAccess().getXdrIn());
        }
    }

    /**
     * Tests if calling {@link XdrStreamAccess#getXdrOut()} obtained from {@link WireConnection#getXdrStreamAccess()}
     * throws an SQLException when not connected.
     */
    @Test
    void testUnconnected_CreateXdrOut() throws Exception {
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            assertThrows(SQLException.class, () -> gdsConnection.getXdrStreamAccess().getXdrOut());
        }
    }

    /**
     * Tests if calling {@link WireConnection#close()} does not throw an exception when not connected.
     */
    @Test
    @SuppressWarnings("resource")
    void testUnconnected_Disconnect() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        assertDoesNotThrow(gdsConnection::close);
    }

    @Test
    void customSocketFactory_noArg() throws Exception {
        connectionInfo.setSocketFactory(NoArgSocketFactory.class.getName());
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();

            assertTrue(NoArgSocketFactory.getCreateSocketCalledOnThread(),
                    "Expected custom socket factory createSocket() to have been called");
        } finally {
            NoArgSocketFactory.clearCurrentThread();
        }
    }

    @Test
    void customSocketFactory_properties() throws Exception {
        connectionInfo.setSocketFactory(PropertiesSocketFactory.class.getName());
        connectionInfo.setProperty("customString@socketFactory", "customStringValue");
        connectionInfo.setIntProperty("customInt@socketFactory", 7812);
        connectionInfo.setBooleanProperty("customBoolean@socketFactory", true);
        connectionInfo.setProperty("customString@notSocketFactory", "notIncluded");
        try (var gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();

            assertTrue(PropertiesSocketFactory.getCreateSocketCalledOnThread(),
                    "Expected custom socket factory createSocket() to have been called");

            var expectedProps = new Properties();
            expectedProps.putAll(Map.of("customString@socketFactory", "customStringValue",
                    "customInt@socketFactory", "7812",
                    "customBoolean@socketFactory", "true"));
            assertEquals(expectedProps, PropertiesSocketFactory.getLastPropertiesOnThread(),
                    "Unexpected properties received during socket factory creation");
        } finally {
            PropertiesSocketFactory.clearCurrentThread();
        }
    }

    @Test
    void customSocketFactory_classNotFound() throws Exception {
        String socketFactory = "org.firebirdsql.gds.ng.wire.DoesNotExist";
        connectionInfo.setSocketFactory(socketFactory);
        try (var gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            var exception = assertThrows(SQLNonTransientConnectionException.class, gdsConnection::socketConnect);
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_socketFactoryClassNotFound, socketFactory));
        }
    }

    @Test
    void customSocketFactory_noSocketFactory() throws Exception {
        String socketFactory = getClass().getName();
        connectionInfo.setSocketFactory(socketFactory);
        try (var gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            var exception = assertThrows(SQLNonTransientConnectionException.class, gdsConnection::socketConnect);
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_socketFactoryClassNotFound, socketFactory));
        }
    }

    @Test
    void customSocketFactory_noNoArgOrPropertiesConstructor() throws Exception {
        String socketFactory = StringSocketFactory.class.getName();
        connectionInfo.setSocketFactory(socketFactory);
        try (var gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            var exception = assertThrows(SQLNonTransientConnectionException.class, gdsConnection::socketConnect);
            assertThat(exception,
                    fbMessageStartsWith(JaybirdErrorCodes.jb_socketFactoryConstructorNotFound, socketFactory));
        }
    }
    
}
