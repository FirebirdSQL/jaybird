/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;

/**
 * Tests for connection timeouts.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestFBConnectionTimeout extends FBTestBase {
    
    public TestFBConnectionTimeout(String name) {
		super(name);
	}
    
    static {
    	try {
    		// needed for the test to work under Java 5
			Class.forName("org.firebirdsql.jdbc.FBDriver");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
    }

	/**
     * IP address which does not exist (we simply assume that this site local address does not exist in
     * the network when running the test). This assumption is a lot cheaper than testing various addresses
     * or applying some heuristic based on the local interface address. 
     */
    private static final String NON_EXISTENT_IP = "10.253.253.253";
    /**
     * Delta for timeout, it is about 100-120 on my machine
     */
    private static final double TIMEOUT_DELTA_MS = 200;
    
    @Override
    public void setUp() {
        // Test won't work for embedded
        assertTrue(!getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test won't work for for native
        assertTrue(!getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }
    
    @Override
    public void tearDown() {
    	// do nothing
    }
    
    /**
     * Test for default connect timeout.
     * <p>
     * This test is ignored by default, as it is OS dependent (eg on Windows 8 it is 20 seconds).  
     * </p>
     */
    public void _testDefaultConnectTimeout() {
        // Ensure default timeout is used
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\", message: " + e.getMessage(), 335544721, e.getErrorCode());
            System.out.printf("Timeout: %f%n", difference / 1000.0);
        }
    }
    
    /**
     * Test for connect timeout specified through {@link java.sql.DriverManager#setLoginTimeout(int)}
     */
    public void testConnectTimeoutFromDriverManager() {
        // Timeout set through DriverManager
        DriverManager.setLoginTimeout(2);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\", message: " + e.getMessage(), 335544721, e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        }
    }
    
    /**
     * Test for connect timeout specified through connection property (in the url)
     */
    public void testConnectTimeoutFromProperty() {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL() + "?connectTimeout=1", "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\", message: " + e.getMessage(), 335544721, e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 1000, difference, TIMEOUT_DELTA_MS);
        }
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified in the connection properties.
     */
    public void testNormalConnectionWithTimeoutFromProperty() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        super.setUp();
        try {
            Properties properties = getDefaultPropertiesForConnection();
            properties.setProperty("connectTimeout", "1");
            Connection connection = DriverManager.getConnection(getUrl(), properties);
            connection.close();
        } finally {
            super.tearDown();
        }
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified through DriverManager
     */
    public void testNormalConnectionWithTimeoutFromDriverManager() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(2);
        super.setUp();
        try {
            Properties properties = getDefaultPropertiesForConnection();
            Connection connection = DriverManager.getConnection(getUrl(), properties);
            connection.close();
        } finally {
            super.tearDown();
        }
    }
    
    /**
     * Builds the test URL (to a non-existent IP) for the current GDS testtype.
     * 
     * @return Test URL to a non-existent IP
     */
    private String buildTestURL() {
        GDSType gdsType = getGdsType();
        try {
            return GDSFactory.getJdbcUrl(gdsType, GDSFactory.getDatabasePath(gdsType, NON_EXISTENT_IP, null, "db"));
        } catch (GDSException e) {
            fail("Unable to generate testURL");
        }
        return null;
    }
}

