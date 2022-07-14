/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for connection timeouts.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class FBConnectionTimeoutTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

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

    /**
     * Test for default connect timeout.
     */
    @Test
    @Disabled("This test is disabled by default, as it is OS dependent (e.g. on Windows 8 it is 20 seconds).")
    void defaultConnectTimeout() {
        // Ensure default timeout is used
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        SQLException exception = assertThrows(SQLException.class,
                () -> DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey"),
                "Expected connection to fail");
        long endTime = System.currentTimeMillis();
        assertThat("Expected error code for \"Unable to complete network request\"",
                exception, errorCodeEquals(ISCConstants.isc_network_error));
        long difference = endTime - startTime;
        System.out.printf("Timeout: %f%n", difference / 1000.0);
    }
    
    /**
     * Test for connect timeout specified through {@link java.sql.DriverManager#setLoginTimeout(int)}
     */
    @Test
    @Timeout(5)
    void connectTimeoutFromDriverManager() {
        // Timeout set through DriverManager
        DriverManager.setLoginTimeout(2);
        try {
            long startTime = System.currentTimeMillis();
            SQLException exception = assertThrows(SQLException.class,
                    () -> DriverManager.getConnection(buildTestURL(), "sysdba", "masterkey"),
                    "Expected connection to fail");
            long endTime = System.currentTimeMillis();
            assertThat("Expected error code for \"Unable to complete network request\"",
                    exception, errorCodeEquals(ISCConstants.isc_network_error));
            long difference = endTime - startTime;
            assertEquals(2000, difference, TIMEOUT_DELTA_MS, "Unexpected timeout duration (in ms)");
        } finally {
            // Reset to default
            DriverManager.setLoginTimeout(0);
        }
    }
    
    /**
     * Test for connect timeout specified through connection property (in the url)
     */
    @Test
    @Timeout(5)
    void connectTimeoutFromProperty() {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        long startTime = System.currentTimeMillis();
        SQLException exception = assertThrows(SQLException.class,
                () -> DriverManager.getConnection(buildTestURL() + "?connectTimeout=1", "sysdba", "masterkey"),
                "Expected connection to fail");
        long endTime = System.currentTimeMillis();
        assertThat("Expected error code for \"Unable to complete network request\"",
                exception, errorCodeEquals(ISCConstants.isc_network_error));
        long difference = endTime - startTime;
        assertEquals(1000, difference, TIMEOUT_DELTA_MS, "Unexpected timeout duration (in ms)");
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified in the connection properties.
     */
    @Test
    void normalConnectionWithTimeoutFromProperty() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(0);
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
            properties.setProperty("connectTimeout", "1");
            assertDoesNotThrow(() -> {
                Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), properties);
                connection.close();
            });
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }
    
    /**
     * Test if a normal connection will work when the timeout is specified through DriverManager
     */
    @Test
    void normalConnectionWithTimeoutFromDriverManager() throws Exception {
        // Reset DriverManager timeout
        DriverManager.setLoginTimeout(2);
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
            assertDoesNotThrow(() -> {
                Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), properties);
                connection.close();
            });
        } finally {
            // Reset to default
            DriverManager.setLoginTimeout(0);
            defaultDatabaseTearDown(fbManager);
        }
    }
    
    /**
     * Builds the test URL (to a non-existent IP) for the current GDS testtype.
     * 
     * @return Test URL to a non-existent IP
     */
    private static String buildTestURL() {
        GDSType gdsType = FBTestProperties.getGdsType();
        FbConnectionProperties properties = new FbConnectionProperties();
        properties.setServerName(NON_EXISTENT_IP);
        properties.setDatabaseName("db");
        try {
            return GDSFactory.getJdbcUrl(gdsType, properties);
        } catch (SQLException e) {
            fail("Unable to generate testURL");
        }
        throw new AssertionError("Should not be reached");
    }
}
