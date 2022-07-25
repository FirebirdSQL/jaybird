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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.DatabaseUserExtension;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.management.FBServiceManager;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Firebird 3 and higher authentication introduced in protocol v13.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class Firebird3PlusAuthenticationTest {

    @RegisterExtension
    @Order(1)
    static final RequireProtocolExtension requireProtocol = RequireProtocolExtension.requireProtocolVersion(13);

    @RegisterExtension
    @Order(2)
    static final GdsTypeExtension gdsType = GdsTypeExtension.excludesNativeOnly();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @RegisterExtension
    final DatabaseUserExtension databaseUser = DatabaseUserExtension.withDatabaseUser();

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Legacy_UserManager}.
     */
    @Test
    void authenticateDatabaseUsingLegacyAuth() throws Exception {
        final String username = "legacyauth";
        final String password = "legacy";
        databaseUser.createUser(username, password, "Legacy_UserManager");
        Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        connectionProperties.setProperty("authPlugins", "Legacy_Auth");
        try (Connection connection = DriverManager.getConnection(getUrl(), connectionProperties);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT MON$AUTH_METHOD FROM MON$ATTACHMENTS WHERE MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
            assertTrue(resultSet.next(), "Expected a row with attachment information");
            assertEquals("Legacy_Auth", resultSet.getString(1), "Unexpected authentication method");
        }
    }

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Legacy_UserManager}.
     * <p>
     * Replicates the test of {@code FBServiceManagerTest.testGetServerVersion()}.
     * </p>
     */
    @Test
    void authenticateServiceUsingLegacyAuth() throws Exception {
        final String username = "legacyauth";
        final String password = "legacy";
        databaseUser.createUser(username, password, "Legacy_UserManager");
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setServerName(FBTestProperties.DB_SERVER_URL);
        fbServiceManager.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        fbServiceManager.setUser(username);
        fbServiceManager.setPassword(password);
        fbServiceManager.setAuthPlugins("Legacy_Auth");

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Srp}.
     * <p>
     * Replicates the test of {@code FBServiceManagerTest.testGetServerVersion()}.
     * </p>
     */
    @Test
    void authenticateServiceUsingSrpAuth() throws Exception {
        final String username = "srpauth";
        final String password = "srp";
        databaseUser.createUser(username, password, "Srp");
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setServerName(FBTestProperties.DB_SERVER_URL);
        fbServiceManager.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        fbServiceManager.setUser(username);
        fbServiceManager.setPassword(password);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }
}
