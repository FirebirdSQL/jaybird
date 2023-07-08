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

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jdbc.ClientInfoProvider.ClientInfoProperty;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.sql.PooledConnection;
import java.sql.ClientInfoStatus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests the client info properties implementation of {@link FBConnection} (and {@link ClientInfoProvider}).
 * <p>
 * Specifically tests methods {@link FBConnection#getClientInfo(String)}, {@link FBConnection#getClientInfo()},
 * {@link FBConnection#setClientInfo(Properties)} and {@link FBConnection#getClientInfo()}
 * </p>
 */
class FBConnectionClientInfoPropertiesTest {

    @RegisterExtension
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsGetSetContext, "Test requires GET/SET_CONTEXT support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getInitStatements());

    // NOTE: Value set by trigger must match result of valueFor(TRANS_PROP)
    private static final String TRANSACTION_TRIGGER_ADD_TRANSACTION_PROP = """
            create trigger TRANS_START_ADD_TRANS_PROP on transaction start
            as
            begin
              RDB$SET_CONTEXT('USER_TRANSACTION', 'TEST_VALUE', 'Value of TEST_VALUE@USER_TRANSACTION');
            end""";

    private static final ClientInfoProperty TRANS_PROP = new ClientInfoProperty("TEST_VALUE", "USER_TRANSACTION");
    private static final ClientInfoProperty DEFAULT_NOT_KNOWN =
            new ClientInfoProperty("DefaultNotKnown", "USER_SESSION");

    private static FBConnection connection;

    private static List<String> getInitStatements() {
        if (getDefaultSupportInfo().supportsDatabaseTriggers()) {
            return List.of(TRANSACTION_TRIGGER_ADD_TRANSACTION_PROP);
        }
        return List.of();
    }

    @BeforeAll
    static void openConnection() throws Exception {
        connection = (FBConnection) getConnectionViaDriverManager();
    }

    @BeforeEach
    void resetConnect() throws Exception {
        connection.setAutoCommit(true);
    }

    @AfterAll
    static void closeConnection() throws Exception {
        Connection connection = FBConnectionClientInfoPropertiesTest.connection;
        if (connection != null) {
            try {
                connection.close();
            } finally {
                FBConnectionClientInfoPropertiesTest.connection = null;
            }
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            AUTO_COMMIT, NAME,                      VALUE,                          EXPECT_ON_RETRIEVE
            true,        property,                  Test property,                  true
            false,       property,                  Test property,                  true
            true,        property@USER_TRANSACTION, Test property@USER_TRANSACTION, false
            false,       property@USER_TRANSACTION, Test property@USER_TRANSACTION, true
            true,        property@USER_SESSION,     Test property@USER_TRANSACTION, true
            false,       property@USER_SESSION,     Test property@USER_TRANSACTION, true
            """)
    void testsetGetClientInfo(boolean autoCommit, String name, String value, boolean expectOnRetrieve)
            throws Exception {
        connection.setAutoCommit(autoCommit);

        connection.setClientInfo(name, value);
        assertEquals(expectOnRetrieve ? value : null, connection.getClientInfo(name));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGetClientInfo_ApplicationName(boolean autoCommit) throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(2, 5, 3),
                "Test requires CLIENT_PROCESS@SYSTEM support");
        Properties props = getDefaultPropertiesForConnection();
        var processName = "Test process name";
        props.setProperty(PropertyNames.processName, processName);

        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            connection.setAutoCommit(autoCommit);
            var applicationName = "Test ApplicationName in context";
            connection.setClientInfo("ApplicationName", applicationName);
            assertEquals(applicationName, connection.getClientInfo("ApplicationName"));

            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery("""
                         select
                           RDB$GET_CONTEXT('SYSTEM', 'CLIENT_PROCESS'),
                           RDB$GET_CONTEXT('USER_SESSION', 'ApplicationName')
                         from rdb$database""")) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(processName, rs.getString(1), "CLIENT_PROCESS@SYSTEM");
                assertEquals(applicationName, rs.getString(2), "ApplicationName@USER_SESSION");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGetClientInfo_ApplicationName_fallbackToCLIENT_PROCESS_in_SYSTEM(boolean autoCommit) throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(2, 5, 3),
                "Test requires CLIENT_PROCESS@SYSTEM support");
        Properties props = getDefaultPropertiesForConnection();
        var processName = "Test process name";
        props.setProperty(PropertyNames.processName, processName);

        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            connection.setAutoCommit(autoCommit);
            assertEquals(processName, connection.getClientInfo("ApplicationName"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGetClientInfo_propertyFromUSER_TRANSACTION(boolean autoCommit) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDatabaseTriggers(), "Test requires support for database triggers");
        // We don't query USER_TRANSACTION properties in auto-commit mode
        String expectedValue = autoCommit ? null : valueFor(TRANS_PROP);
        connection.setAutoCommit(autoCommit);

        assertEquals(expectedValue, connection.getClientInfo(TRANS_PROP.toString()));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGetClientInfo_propertyFromSYSTEM(boolean autoCommit) throws Exception {
        String expectedValue;
        try (var stmt = connection.createStatement();
             // DB_NAME exists since introduction RDB$GET_CONTEXT
             var rs = stmt.executeQuery("select RDB$GET_CONTEXT('SYSTEM', 'DB_NAME') from RDB$DATABASE")) {
            assertTrue(rs.next(), "Expected a row");
            expectedValue = rs.getString(1);
            assertNotNull(expectedValue, "Expected a non-null value");
        }
        connection.setAutoCommit(autoCommit);

        assertEquals(expectedValue, connection.getClientInfo("DB_NAME@SYSTEM"));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            AUTO_COMMIT, NAME_SUFFIX
            true,        ''
            false,       ''
            true,        @USER_SESSION
            false,       @USER_SESSION
            true,        @USER_TRANSACTION
            false,       @USER_TRANSACTION
            true,        @SYSTEM
            false,       @SYSTEM
            """)
    void testGetClientInfo_unknownProperty_returnsNull(boolean autoCommit, String nameSuffix) throws Exception {
        connection.setAutoCommit(autoCommit);

        String name = "DOES_NOT_EXIST" + nameSuffix;
        assertNull(connection.getClientInfo(name), "Expected null result for " + name);
    }

    @Test
    void testSetClientInfo_disallowSetSYSTEM() {
        String name = "DB_NAME@SYSTEM";
        var exception = assertThrows(SQLClientInfoException.class,
                () -> connection.setClientInfo(name, "Value will not be set"));

        assertSQLClientInfoException(exception, startsWith("Properties in SYSTEM context are read-only"),
                Map.of(name, ClientInfoStatus.REASON_UNKNOWN));
    }

    @Test
    void testGetSetClientInfo_nameNull() {
        var exceptionOnSet = assertThrows(SQLClientInfoException.class,
                () -> connection.setClientInfo(null, "value"));
        assertSQLClientInfoException(exceptionOnSet, startsWith("Invalid client info property name"),
                Map.of("<null>", ClientInfoStatus.REASON_UNKNOWN));

        var exceptionOnGet = assertThrows(SQLNonTransientException.class, () -> connection.getClientInfo(null));
        assertThat(exceptionOnGet, message(startsWith("Invalid client info property name")));
    }

    @Test
    void testGetClientInfo_Properties_initialDefaultOnly() throws Exception {
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            // Set default known properties and a "not-known" property by "conventional" means
            Set<ClientInfoProperty> registeredProperties = defaultPropertiesAnd(DEFAULT_NOT_KNOWN);
            populateProperties(connection, registeredProperties);

            Properties props1 = connection.getClientInfo();
            ClientInfoProvider provider = connection.getClientInfoProvider();
            assertEquals(provider.getDefaultClientInfoPropertyNames(), props1.stringPropertyNames(),
                    "Unexpected property names retrieved");
            for (ClientInfoProperty prop : defaultProperties()) {
                assertEquals(valueFor(prop), props1.getProperty(prop.toString()), "Unexpected value for " + prop);
            }

            assertEquals(valueFor(DEFAULT_NOT_KNOWN), connection.getClientInfo(DEFAULT_NOT_KNOWN.toString()),
                    "Unexpected value for " + DEFAULT_NOT_KNOWN);

            Set<String> expectedNames = registeredProperties.stream()
                    .map(Object::toString)
                    .collect(toUnmodifiableSet());
            Properties props2 = connection.getClientInfo();
            assertEquals(expectedNames, props2.stringPropertyNames());
            for (ClientInfoProperty prop : registeredProperties) {
                assertEquals(valueFor(prop), props2.getProperty(prop.toString()), "Unexpected value for " + prop);
            }
        }
    }

    @Test
    void testGetClientInfo_Properties_ignoreInvalidSYSTEMProperty() throws Exception {
        var invalidSystemProperty = new ClientInfoProperty("INVALID_PROPERTY", "SYSTEM");
        var validSystemProperty = new ClientInfoProperty("DB_NAME", "SYSTEM");
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            populateProperties(connection, defaultPropertiesAnd());
            ClientInfoProvider provider = connection.getClientInfoProvider();
            // Explicitly register invalidSystemProperty, because normal paths would reject it
            provider.registerKnownProperty(invalidSystemProperty);

            // Implicitly registers validSystemProperty as a known property
            String validSystemPropValue = connection.getClientInfo(validSystemProperty.toString());
            assertNotNull(validSystemPropValue, "Expected a non-null value");

            Set<String> expectedNames = defaultPropertiesAnd(validSystemProperty).stream()
                    .map(Object::toString)
                    .collect(toUnmodifiableSet());
            Properties props = connection.getClientInfo();
            assertEquals(expectedNames, props.stringPropertyNames());
            for (ClientInfoProperty prop : defaultProperties()) {
                assertEquals(valueFor(prop), props.getProperty(prop.toString()), "Unexpected value for " + prop);
            }
            assertEquals(validSystemPropValue, props.getProperty(validSystemProperty.toString()),
                    "Unexpected value for " + validSystemProperty);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGetClientInfo_Properties_excludeUSER_TRANSACTION_inAutoCommit(boolean autoCommit) throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDatabaseTriggers(), "Test requires support for database triggers");
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            populateProperties(connection, defaultProperties());
            // Implicitly registers TRANS_PROP as a known property (this must be done while auto-commit is false!)
            assertEquals(valueFor(TRANS_PROP), connection.getClientInfo(TRANS_PROP.toString()),
                    "Unexpected value for " + TRANS_PROP);

            connection.setAutoCommit(autoCommit);

            Set<ClientInfoProperty> expectedProperties = autoCommit
                    ? defaultProperties()
                    : defaultPropertiesAnd(TRANS_PROP);
            Set<String> expectedNames = expectedProperties.stream()
                    .map(Object::toString)
                    .collect(toUnmodifiableSet());
            Properties props = connection.getClientInfo();
            assertEquals(expectedNames, props.stringPropertyNames());
            for (ClientInfoProperty prop : expectedProperties) {
                assertEquals(valueFor(prop), props.getProperty(prop.toString()), "Unexpected value for " + prop);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void setClientInfo_Properties(boolean autoCommit) throws Exception {
        var additionalUserSessionProperty = new ClientInfoProperty("additionalProperty", "USER_SESSION");
        var additionalUserTransactionProperty = new ClientInfoProperty("additionalProperty", "USER_TRANSACTION");
        var validSystemProperty = new ClientInfoProperty("DB_NAME", "SYSTEM");
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            // registered to check that no attempt is made to clear a SYSTEM property (or, does not trigger an error)
            connection.getClientInfoProvider().registerKnownProperty(validSystemProperty);
            Set<ClientInfoProperty> clientInfoProperties =
                    defaultPropertiesAnd(additionalUserSessionProperty, additionalUserTransactionProperty);
            var propertiesToSet = new Properties();
            clientInfoProperties.stream()
                    .map(Object::toString)
                    .forEach(name -> propertiesToSet.setProperty(name, valueFor(name)));
            connection.setAutoCommit(autoCommit);

            connection.setClientInfo(propertiesToSet);

            Set<ClientInfoProperty> expectedProperties = clientInfoProperties.stream()
                    .filter(prop -> "USER_SESSION".equals(prop.context())
                                    || "USER_TRANSACTION".equals(prop.context()) && !autoCommit)
                    .collect(toUnmodifiableSet());
            Set<String> expectedNames = expectedProperties.stream()
                    .map(Objects::toString)
                    .collect(toCollection(HashSet::new));
            // We're expecting this system property, but not checking its value
            expectedNames.add(validSystemProperty.toString());
            Properties retrievedProps = connection.getClientInfo();
            assertEquals(expectedNames, retrievedProps.stringPropertyNames());
            for (ClientInfoProperty prop : expectedProperties) {
                assertEquals(valueFor(prop), retrievedProps.getProperty(prop.toString()),
                        "Unexpected value for " + prop);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void setClientInfo_Properties_clearKnownButNotIncludedProperties(boolean autoCommit) throws Exception {
        var additionalUserSessionProperty = new ClientInfoProperty("additionalProperty", "USER_SESSION");
        var additionalUserTransactionProperty = new ClientInfoProperty("additionalProperty", "USER_TRANSACTION");
        var validSystemProperty = new ClientInfoProperty("DB_NAME", "SYSTEM");
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            // registered to check that no attempt is made to clear a SYSTEM property (or, does not trigger an error)
            connection.getClientInfoProvider().registerKnownProperty(validSystemProperty);
            Set<ClientInfoProperty> clientInfoProperties =
                    defaultPropertiesAnd(additionalUserSessionProperty, additionalUserTransactionProperty);
            populateProperties(connection, clientInfoProperties);

            var propertiesToSet = new Properties();
            propertiesToSet.setProperty("ApplicationName", "Changed ApplicationName value");
            connection.setAutoCommit(autoCommit);

            connection.setClientInfo(propertiesToSet);

            var expectedNames = Set.of("ApplicationName", validSystemProperty.toString());
            Properties retrievedProps = connection.getClientInfo();
            assertEquals(expectedNames, retrievedProps.stringPropertyNames());
            assertEquals("Changed ApplicationName value", retrievedProps.getProperty("ApplicationName"),
                    "Unexpected value for ApplicationName");
        }
    }

    // Formally, this belongs in FBConnectionPoolDataSourceTest or FBPooledConnectionMockTest, this also implicitly
    // tests FBConnection.resetKnownClientInfoProperties() and ClientInfoProvider.resetKnownClientInfoProperties()
    @Test
    void pooledConnectionResetsKnownPropertiesOnGetConnection() throws Exception {
        var customProperty = new ClientInfoProperty("CustomProperty", "USER_SESSION");
        // Using a separate connection to have no interference from known properties registered or set by other tests
        try (var connection = (FBConnection) getConnectionViaDriverManager()) {
            ClientInfoProvider provider = connection.getClientInfoProvider();
            provider.registerKnownProperty(customProperty);

            var clazz = Class.forName("org.firebirdsql.ds.FBPooledConnection");
            var constructor = clazz.getDeclaredConstructor(Connection.class);
            constructor.setAccessible(true);
            PooledConnection pooledConnection = (PooledConnection) constructor.newInstance(connection);
            try {
                try (var ignored = pooledConnection.getConnection()) {
                    // customProperty is no longer registered as a known property
                    assertThat(customProperty, not(in(provider.getKnownProperties())));
                    provider.registerKnownProperty(customProperty);
                    assertThat(customProperty, in(provider.getKnownProperties()));
                }

                try (var ignored = pooledConnection.getConnection()) {
                    // customProperty is no longer registered as a known property
                    assertThat(customProperty, not(in(provider.getKnownProperties())));
                }
            } finally {
                pooledConnection.close();
            }
        }
    }

    private static Set<ClientInfoProperty> defaultProperties() throws SQLException {
        return Set.copyOf(connection.getClientInfoProvider().getDefaultClientInfoProperties());
    }

    private static Set<ClientInfoProperty> defaultPropertiesAnd(ClientInfoProperty... additionalProperties)
            throws SQLException {
        var props = new HashSet<>(defaultProperties());
        props.addAll(Arrays.asList(additionalProperties));
        return Set.copyOf(props);
    }

    private static String valueFor(Object prop) {
        return "Value of " + prop;
    }

    private static void populateProperties(Connection connection, Collection<ClientInfoProperty> clientInfoProperties)
            throws SQLException {
        var sb = new StringBuilder("select ");
        boolean afterFirst = false;
        for (ClientInfoProperty prop : clientInfoProperties) {
            if (afterFirst) {
                sb.append(",");
            } else {
                afterFirst = true;
            }
            prop.appendAsSetContext(sb, QuoteStrategy.DIALECT_3, valueFor(prop));
        }
        sb.append(" from RDB$DATABASE");
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sb.toString())) {
            assertTrue(rs.next());
        }
    }

    private static void assertSQLClientInfoException(SQLClientInfoException e, Matcher<String> messageMatcher,
            Map<String, ClientInfoStatus> expectedFailures) {
        assertThat(e, message(messageMatcher));
        assertEquals(expectedFailures, e.getFailedProperties());
    }

}
