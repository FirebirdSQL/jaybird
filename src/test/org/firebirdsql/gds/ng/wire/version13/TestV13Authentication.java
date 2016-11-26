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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.DatabaseUserRule;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.management.FBServiceManager;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Test authentication in the V13 protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV13Authentication {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(13);

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludesNativeOnly();

    private final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();
    private final DatabaseUserRule databaseUserRule = DatabaseUserRule.withDatabaseUser();
    @Rule
    public final TestRule ruleChain = RuleChain
            .outerRule(usesDatabase)
            .around(databaseUserRule);

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Legacy_UserManager}.
     */
    @Test
    public void authenticateDatabaseUsingLegacyAuth() throws Exception {
        final String username = "legacyauth";
        final String password = "legacy";
        databaseUserRule.createUser(username, password, "Legacy_UserManager");
        Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        try (Connection connection = DriverManager.getConnection(getUrl(), connectionProperties);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT MON$AUTH_METHOD FROM MON$ATTACHMENTS WHERE MON$ATTACHMENT_ID = CURRENT_CONNECTION")
        ) {
            assertTrue("Expected a row with attachment information", resultSet.next());
            assertEquals("Unexpected authentication method", "Legacy_Auth", resultSet.getString(1));
        }
    }

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Legacy_UserManager}.
     * <p>
     * Replicates the test of {@link org.firebirdsql.management.TestFBServiceManager#testGetServerVersion()}.
     * </p>
     */
    @Test
    public void authenticateServiceUsingLegacyAuth() throws Exception {
        final String username = "legacyauth";
        final String password = "legacy";
        databaseUserRule.createUser(username, password, "Legacy_UserManager");
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setHost(FBTestProperties.DB_SERVER_URL);
        fbServiceManager.setPort(FBTestProperties.DB_SERVER_PORT);
        fbServiceManager.setUser(username);
        fbServiceManager.setPassword(password);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }

    /**
     * This test assumes that the Firebird 3 config for {@code UserManager} contains {@code Srp}.
     * <p>
     * Replicates the test of {@link org.firebirdsql.management.TestFBServiceManager#testGetServerVersion()}.
     * </p>
     */
    @Test
    public void authenticateServiceUsingSrpAuth() throws Exception {
        final String username = "srpauth";
        final String password = "srp";
        databaseUserRule.createUser(username, password, "Srp");
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setHost(FBTestProperties.DB_SERVER_URL);
        fbServiceManager.setPort(FBTestProperties.DB_SERVER_PORT);
        fbServiceManager.setUser(username);
        fbServiceManager.setPassword(password);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }
}
