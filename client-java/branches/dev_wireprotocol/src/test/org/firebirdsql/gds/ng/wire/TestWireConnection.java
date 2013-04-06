/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.firebirdsql.common.BlackholeServer;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.ng.FbConnectTimeoutException;
import org.firebirdsql.gds.ng.FbException;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version10.Version10Descriptor;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestWireConnection extends FBJUnit4TestBase {

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

    @BeforeClass
    public static void verifyTestType() {
        // Test irrelevant for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test irrelevant for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    /**
     * Tests {@link WireConnection#isConnected()} when no connection has been
     * established yet.
     */
    @Test
    public void testIsConnectedNoConnection() {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        assertFalse("Not connected, isConnected() should return false", gdsConnection.isConnected());
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established.
     */
    @Test
    public void testIsConnectedWithConnection() throws FbException {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        try {
            gdsConnection.socketConnect();
            assertTrue("Connected to existing server, isConnected() should return true", gdsConnection.isConnected());
        } finally {
            try {
                gdsConnection.disconnect();
            } catch (IOException e) {
                // Ignore, but print for troubleshooting
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests {@link WireConnection#isConnected()} when a connection has been
     * established and closed.
     */
    @Test
    public void testIsConnectedAfterDisconnect() throws Exception {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        gdsConnection.socketConnect();
        gdsConnection.disconnect();
        
        assertFalse("Disconnected, isConnected() should return false", gdsConnection.isConnected());
    }

    /**
     * Tests a successful connection identification phase.
     */
    @Test
    public void testIdentifyExistingDb() throws FbException {
        ProtocolDescriptor expectedProtocol = new Version10Descriptor();
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        try {
            gdsConnection.socketConnect();
            assertTrue(gdsConnection.isConnected());

            FbWireDatabase database = gdsConnection.identify(FBTestProperties.getDatabasePath());
            
            assertEquals("Unexpected FbWireDatabase implementation",
                    org.firebirdsql.gds.ng.wire.version10.V10Database.class, database.getClass());
            assertEquals("Unexpected architecture", expectedProtocol.getArchitecture(),
                    gdsConnection.getProtocolArchitecture());
            assertEquals("Unexpected type", expectedProtocol.getMaximumType(), gdsConnection.getProtocolMinimumType());
            assertEquals("Unexpected version", expectedProtocol.getVersion(), gdsConnection.getProtocolVersion());
        } finally {
            try {
                gdsConnection.disconnect();
            } catch (IOException e) {
                // Ignore, but print for troubleshooting
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests the connect timeout when connecting to a non-existent server.
     */
    @Test
    public void testConnectTimeout_nonExistentServer() {
        long startTime = System.currentTimeMillis();
        try {
            WireConnection gdsConnection = new WireConnection(NON_EXISTENT_IP, FBTestProperties.DB_SERVER_PORT, -1, 2,
                    ProtocolCollection.getDefaultCollection());
            gdsConnection.socketConnect();
            
            fail("Expected connection to fail");
        } catch (FbConnectTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getFbErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (FbException e) {
            e.printStackTrace();
            
            fail("Expected GdsConnectTimeoutException to be thrown");
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
            WireConnection gdsConnection = new WireConnection("localhost", server.getPort(), -1, 2,
                    ProtocolCollection.getDefaultCollection());
            gdsConnection.socketConnect();
            gdsConnection.identify("somedb");
            
            fail("Expected connection to fail");
        } catch (FbConnectTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getFbErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (FbException e) {
            e.printStackTrace();
            
            fail("Expected GdsConnectTimeoutException to be thrown");
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
            WireConnection gdsConnection = new WireConnection("localhost", server.getPort());
            gdsConnection.setSocketTimeout(2000);
            gdsConnection.socketConnect();
            gdsConnection.identify("somedb");
            
            fail("Expected connection to fail");
        } catch (FbConnectTimeoutException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721,
                    e.getFbErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        } catch (FbException e) {
            e.printStackTrace();
            
            fail("Expected GdsConnectTimeoutException to be thrown");
        } finally {
            server.stop();
            thread.join();
        }
    }

    /**
     * Tests if calling {@link WireConnection#getXdrIn()} throws an
     * IOException when not connected.
     */
    @Test(expected = FbException.class)
    public void testUnconnected_CreateXdrIn() throws Exception {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        gdsConnection.getXdrIn();
    }

    /**
     * Tests if calling {@link WireConnection#getXdrOut()} throws an
     * IOException when not connected.
     */
    @Test(expected = FbException.class)
    public void testUnconnected_CreateXdrOut() throws Exception {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        gdsConnection.getXdrOut();
    }

    /**
     * Tests if calling {@link WireConnection#disconnect()} does not throw an
     * exception when not connected.
     */
    @Test
    public void testUnconnected_Disconnect() throws Exception {
        WireConnection gdsConnection = new WireConnection(FBTestProperties.DB_SERVER_URL,
                FBTestProperties.DB_SERVER_PORT);
        gdsConnection.disconnect();
    }
}
