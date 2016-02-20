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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.common.BlackholeServer;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.wire.version10.Version10Descriptor;
import org.firebirdsql.gds.ng.wire.version13.Version13Descriptor;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestWireDatabaseConnection extends FBJUnit4TestBase {

    @ClassRule
    public static final GdsTypeRule testTypes = GdsTypeRule.excludes(
            EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME,
            NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

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
    
    private final FbConnectionProperties connectionInfo;
    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        // TODO consider keeping NONE the default in WireConnection if not specified
        connectionInfo.setEncoding("NONE");
    }

    /**
     * Tests {@link WireConnection#isConnected()} when no connection has been
     * established yet.
     */
    @Test
    public void testIsConnectedNoConnection() throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        assertFalse("Not connected, isConnected() should return false", gdsConnection.isConnected());
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established.
     */
    @Test
    public void testIsConnectedWithConnection() throws Exception {
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo)) {
            gdsConnection.socketConnect();
            assertTrue("Connected to existing server, isConnected() should return true", gdsConnection.isConnected());
        }
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established and closed.
     */
    @Test
    public void testIsConnectedAfterDisconnect() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        gdsConnection.socketConnect();
        gdsConnection.close();
        
        assertFalse("Disconnected, isConnected() should return false", gdsConnection.isConnected());
    }

    /**
     * Tests a successful connection identification phase.
     */
    @Test
    public void testIdentifyExistingDb() throws Exception {
        ProtocolDescriptor expectedProtocol = new Version10Descriptor();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.createInstance((EncodingDefinition) null), ProtocolCollection.create(expectedProtocol))) {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected());

            FbWireDatabase database = gdsConnection.identify();

            assertEquals("Unexpected FbWireDatabase implementation",
                    org.firebirdsql.gds.ng.wire.version10.V10Database.class, database.getClass());
            assertEquals("Unexpected architecture", expectedProtocol.getArchitecture(),
                    gdsConnection.getProtocolArchitecture());
            assertEquals("Unexpected type", expectedProtocol.getMaximumType(), gdsConnection.getProtocolMinimumType());
            assertEquals("Unexpected version", expectedProtocol.getVersion(), gdsConnection.getProtocolVersion());
        }
    }

    /**
     * Tests a successful connection identification phase.
     */
    @Test
    public void testIdentifyExistingDb_v13() throws Exception {
        assumeTrue("Requires protocol v13 support", FBTestProperties.getDefaultSupportInfo().supportsProtocol(13));
        ProtocolDescriptor expectedProtocol = new Version13Descriptor();
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.createInstance((EncodingDefinition) null), ProtocolCollection.create(expectedProtocol))) {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected());

            FbWireDatabase database = gdsConnection.identify();

            assertEquals("Unexpected FbWireDatabase implementation",
                    org.firebirdsql.gds.ng.wire.version13.V13Database.class, database.getClass());
            assertEquals("Unexpected architecture", expectedProtocol.getArchitecture(),
                    gdsConnection.getProtocolArchitecture());
            assertEquals("Unexpected type", expectedProtocol.getMaximumType(), gdsConnection.getProtocolMinimumType());
            assertEquals("Unexpected version", expectedProtocol.getVersion(), gdsConnection.getProtocolVersion());
        }
    }

    /**
     * Tests the connect timeout when connecting to a non-existent server.
     */
    @Test
    public void testConnectTimeout_nonExistentServer() {
        long startTime = System.currentTimeMillis();
        try {
            connectionInfo.setServerName(NON_EXISTENT_IP);
            connectionInfo.setConnectTimeout(2);
            WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
            gdsConnection.socketConnect();
            
            fail("Expected connection to fail");
        } catch (SQLTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (SQLException e) {
            e.printStackTrace();
            
            fail("Expected SQLTimeoutException to be thrown");
        }
    }

    /**
     * Tests the connect timeout if the server does not respond.
     */
    @Test
    public void testConnectTimeout_noResponse() throws Exception {
        BlackholeServer server = new BlackholeServer();
        Thread thread = new Thread(server);
        thread.start();

        long startTime = System.currentTimeMillis();
        try {
            connectionInfo.setPortNumber(server.getPort());
            connectionInfo.setDatabaseName("somedb");
            connectionInfo.setConnectTimeout(2);
            WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
            gdsConnection.socketConnect();
            gdsConnection.identify();
            
            fail("Expected connection to fail");
        } catch (SQLTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (SQLException e) {
            e.printStackTrace();
            
            fail("Expected SQLTimeoutException to be thrown");
        } finally {
            server.stop();
            thread.join();
        }
    }

    /**
     * Tests the connect timeout if the server does not respond.
     */
    @Test
    public void testSocketTimeout_noResponse() throws Exception {
        BlackholeServer server = new BlackholeServer();
        Thread thread = new Thread(server);
        thread.start();

        long startTime = System.currentTimeMillis();
        try {
            connectionInfo.setPortNumber(server.getPort());
            connectionInfo.setDatabaseName("somedb");
            connectionInfo.setSoTimeout(2000);
            WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
            gdsConnection.socketConnect();
            gdsConnection.identify();
            
            fail("Expected connection to fail");
        } catch (SQLTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (SQLException e) {
            e.printStackTrace();
            
            fail("Expected SQLTimeoutException to be thrown");
        } finally {
            server.stop();
            thread.join();
        }
    }

    /**
     * Tests if calling {@link XdrStreamAccess#getXdrIn()} obtained from {@link WireConnection#getXdrStreamAccess()} throws an
     * SQLException when not connected.
     */
    @Test(expected = SQLException.class)
    public void testUnconnected_CreateXdrIn() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        gdsConnection.getXdrStreamAccess().getXdrIn();
    }

    /**
     * Tests if calling {@link XdrStreamAccess#getXdrOut()} obtained from {@link WireConnection#getXdrStreamAccess()} throws an
     * SQLException when not connected.
     */
    @Test(expected = SQLException.class)
    public void testUnconnected_CreateXdrOut() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        gdsConnection.getXdrStreamAccess().getXdrOut();
    }

    /**
     * Tests if calling {@link WireConnection#close()} does not throw an
     * exception when not connected.
     */
    @Test
    public void testUnconnected_Disconnect() throws Exception {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo);
        gdsConnection.close();
    }
}
