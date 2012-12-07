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

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.management.FBManager;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for connection timeouts.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class TestFBConnectionTimeout {
    // This test does not extend FBJUnit4TestBase as a lot of these tests don't need an actual database
    
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
    
    @BeforeClass
    public static void verifyTestType() {
        // Test won't work for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test won't work for for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }
    
    /**
     * Test for default connect timeout.
     * <p>
     * This test is ignored by default, as it is OS dependent (eg on Windows 8 it is 20 seconds).  
     * </p>
     */
    @Test
    @Ignore
    public void defaultConnectTimeout() {
        // Ensure default timeout is used
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721, e.getErrorCode());
            System.out.printf("Timeout: %f%n", difference / 1000.0);
        }
    }
    
    /**
     * Test for connect timeout specified through {@link java.sql.DriverManager#setLoginTimeout(int)}
     */
    @Test
    public void connectTimeoutFromDriverManager() {
        // Timeout set through DriverManager
        DriverManager.setLoginTimeout(2);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721, e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 2000, difference, TIMEOUT_DELTA_MS);
        }
    }
    
    /**
     * Test for connect timeout specified through connection property (in the url)
     */
    @Test
    public void connectTimeoutFromProperty() {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        try {
            DriverManager.getConnection(buildTestURL() + "?connectTimeout=1", "sysdba", "masterkey");
        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            assertEquals("Expected error code for \"Unable to complete network request\"", 335544721, e.getErrorCode());
            assertEquals("Unexpected timeout duration (in ms)", 1000, difference, TIMEOUT_DELTA_MS);
        }
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified in the connection properties.
     */
    @Test
    public void normalConnectionWithTimeoutFromProperty() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        FBManager fbManager = FBTestProperties.defaultDatabaseSetUp();
        try {
            Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
            properties.setProperty("connectTimeout", "1");
            Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), properties);
            connection.close();
        } finally {
            FBTestProperties.defaultDatabaseTearDown(fbManager);
        }
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified through DriverManager
     */
    @Test
    public void normalConnectionWithTimeoutFromDriverManager() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(2);
        FBManager fbManager = FBTestProperties.defaultDatabaseSetUp();
        try {
            Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
            Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), properties);
            connection.close();
        } finally {
            FBTestProperties.defaultDatabaseTearDown(fbManager);
        }
    }
    
    /**
     * Builds the test URL (to a non-existent IP) for the current GDS testtype.
     * 
     * @return Test URL to a non-existent IP
     */
    private static String buildTestURL() {
        GDSType gdsType = FBTestProperties.getGdsType();
        try {
            return GDSFactory.getJdbcUrl(gdsType, GDSFactory.getDatabasePath(gdsType, NON_EXISTENT_IP, null, "db"));
        } catch (GDSException e) {
            fail("Unable to generate testURL");
        }
        return null;
    }
}
