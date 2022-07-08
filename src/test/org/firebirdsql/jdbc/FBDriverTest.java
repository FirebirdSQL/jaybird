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
import org.firebirdsql.common.extension.DatabaseUserExtension;
import org.firebirdsql.common.extension.RunEnvironmentExtension.EnvironmentRequirement;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBDriverTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @RegisterExtension
    final DatabaseUserExtension databaseUser = DatabaseUserExtension.withDatabaseUser();

    @Test
    void testAcceptsURL() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        assertTrue(driver.acceptsURL(getUrl()));
    }

    @Test
    void testConnect() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());
        try (Connection connection = driver.connect(getUrl(), getDefaultPropertiesForConnection())) {
            assertNotNull(connection, "Connection is null");
        }
    }

    @Test
    void testJdbcCompliant() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        assertTrue(driver.jdbcCompliant());
    }

    /**
     * This method tests if driver correctly handles warnings returned from
     * database. We use SQL dialect mismatch between client and server to
     * make server return us a warning.
     */
    @Test
    void testWarnings() throws Exception {
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("set_db_sql_dialect", "1");

        // open connection and convert DB to SQL dialect 1
        try (Connection connection = DriverManager.getConnection(getUrl(), info)) {
            SQLWarning warning = connection.getWarnings();

            assertNotNull(warning, "Connection should have at least one warning");
            assertThat(warning, allOf(
                    isA(SQLWarning.class),
                    errorCodeEquals(ISCConstants.isc_dialect_reset_warning),
                    message(startsWith(getFbMessage(ISCConstants.isc_dialect_reset_warning)))
            ));

            connection.clearWarnings();

            assertNull(connection.getWarnings(), "After clearing no warnings should be present");
        } finally {
            // Reset db dialect back to 3 to avoid issues with following tests
            info.setProperty("set_db_sql_dialect", "3");
            try (Connection connection1 = DriverManager.getConnection(getUrl(), info)) {
                assertTrue(connection1.isValid(0));
            }
        }
    }

    @Test
    void testDialect1() throws Exception {
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("isc_dpb_sql_dialect", "1");

        try (Connection connection = DriverManager.getConnection(getUrl(), info);
             Statement stmt = connection.createStatement()) {
            // Dialect 1 allows double quotes in strings
            ResultSet rs = stmt.executeQuery("SELECT  cast(\"today\" as date) - 7 FROM rdb$database");

            assertTrue(rs.next(), "Should have at least one row");
        }
    }

    @Test
    void testGetSQLState() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            SQLException exception = assertThrows(SQLSyntaxErrorException.class,
                    () -> stmt.executeQuery("select * from"));
            assertThat(exception, sqlState(is(SQLStateConstants.SQL_STATE_SYNTAX_ERROR)));
        }
    }

    @Test
    void testLongRange() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE LONGTEST (LONGID DECIMAL(18) NOT NULL PRIMARY KEY)");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MAX_VALUE + ")");
            try (ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST")) {
                assertTrue(rs.next(), "Should have one row");
                assertEquals(Long.MAX_VALUE, rs.getLong(1), "Retrieved wrong value");
            }

            s.execute("DELETE FROM LONGTEST");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MIN_VALUE + ")");
            try (ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST")) {
                assertTrue(rs.next(), "Should have one row");
                assertEquals(Long.MIN_VALUE, rs.getLong(1), "Retrieved wrong value");
            }
        }
    }

    private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    @Test
    void testDate() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE DATETEST (DATEID INTEGER NOT NULL PRIMARY KEY, TESTDATE TIMESTAMP)");
            Calendar cal = new GregorianCalendar(timeZoneUTC);
            Timestamp x = Timestamp.valueOf("1917-02-17 20:59:31");

            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO DATETEST (DATEID, TESTDATE) VALUES (?,?)")) {
                ps.setInt(1, 1);
                ps.setTimestamp(2, x, cal);
                ps.execute();
            }

            try (ResultSet rs = s.executeQuery("SELECT TESTDATE FROM DATETEST WHERE DATEID=1")) {
                assertTrue(rs.next(), "Should have one row");
                assertEquals(x, rs.getTimestamp(1, cal), "Retrieved wrong value");
            }
        }
    }

    /**
     * This test checks if transaction is rolled back when connection is closed,
     * but still has an active transaction associated with it.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    void testRollbackOnClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE test(id INTEGER, test_value INTEGER)");
            stmt.executeUpdate("INSERT INTO test VALUES (1, 1)");
            connection.setAutoCommit(false);
            stmt.executeUpdate("UPDATE test SET test_value = 2 WHERE id = 1");
        }

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT test_value FROM test WHERE id = 1")) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals(1, rs.getInt(1), "Value should be 1");
                assertFalse(rs.next(), "Should have only one row");
            }
        }
    }

    @Test
    void testDummyPacketIntervalConnect() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("soTimeout", "2000");

        Driver driver = DriverManager.getDriver(getUrl());
        try (Connection connection = driver.connect(getUrl(), props)) {
            assertNotNull(connection, "Connection is null");
        }
    }

    /**
     * Connection url parsing itself is tested in {@code DbAttachInfoTest}.
     */
    @Test
    void testInvalidConnectionUrl() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        Driver driver = DriverManager.getDriver("jdbc:firebirdsql://localhost:c:/data/db/test.fdb");

        SQLException exception = assertThrows(SQLNonTransientConnectionException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignore = driver.connect("jdbc:firebirdsql://localhost:c:/data/db/test.fdb", props)) {
                // just in case we do create a connection
            }
        });
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionString),
                message(containsString("Bad port: 'c:' is not a number"))));
    }

    @Test
    void testTransactionConfigThroughPropertiesObject() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        props.setProperty("TRANSACTION_READ_COMMITTED",
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertEquals(4, tpb.size());
            assertTrue(tpb.hasArgument(isc_tpb_read_committed), "expected isc_tpb_read_committed");
            assertTrue(tpb.hasArgument(isc_tpb_no_rec_version), "expected isc_tpb_no_rec_version");
            assertTrue(tpb.hasArgument(isc_tpb_write), "expected isc_tpb_write");
            assertTrue(tpb.hasArgument(isc_tpb_nowait), "expected isc_tpb_nowait");
        }
    }

    @Test
    void testTransactionConfigThroughConnectionString() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        String jdbcUrl = getUrl() + "?TRANSACTION_READ_COMMITTED=isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, props)) {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertEquals(4, tpb.size());
            assertTrue(tpb.hasArgument(isc_tpb_read_committed), "expected isc_tpb_read_committed");
            assertTrue(tpb.hasArgument(isc_tpb_no_rec_version), "expected isc_tpb_no_rec_version");
            assertTrue(tpb.hasArgument(isc_tpb_write), "expected isc_tpb_write");
            assertTrue(tpb.hasArgument(isc_tpb_nowait), "expected isc_tpb_nowait");
        }
    }

    static Stream<String> testConnectionAuthenticationPlugin() {
        return Stream.of(
                LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME,
                SrpAuthenticationPluginSpi.SRP_AUTH_NAME,
                Srp224AuthenticationPluginSpi.SRP_224_AUTH_NAME,
                Srp256AuthenticationPluginSpi.SRP_256_AUTH_NAME,
                Srp384AuthenticationPluginSpi.SRP_384_AUTH_NAME,
                Srp512AuthenticationPluginSpi.SRP_512_AUTH_NAME);
    }

    // Test might fail if plugin not enabled
    @ParameterizedTest
    @MethodSource
    void testConnectionAuthenticationPlugin(String pluginName) throws Exception {
        assumeThat("Test doesn't work with embedded", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(3, 0), "Requires Firebird 3 or higher");
        assumeTrue(EnvironmentRequirement.ALL_SRP_PLUGINS.isMet(), "Requires " + pluginName);
        // NOTE: If the test still fails, then this plugin is not enabled in the Firebird AuthServer config
        assumeTrue(getDefaultSupportInfo().supportsAuthenticationPlugin(pluginName),
                "Test requires support for authentication plugin " + pluginName);

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("authPlugins", pluginName);
        /* For JNA connections both authPlugins and wireCrypt setting generate content for a single config string
         * Setting both values explicitly here ensures that we also check if that config string gets generated correctly
         */
        props.setProperty("wireCrypt",
                pluginName.equals(LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME) ? "DISABLED" : "REQUIRED");

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "select mon$auth_method from mon$attachments where mon$attachment_id = current_connection")) {
            assertTrue(rs.next(), "expected row");
            assertEquals(pluginName, rs.getString(1), "Unexpected authentication method");
        }
    }

    @Test
    void testAuthPluginsUnknown_pureJava() {
        assumeThat("Type is pure Java", FBTestProperties.GDS_TYPE, isPureJavaType());
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("authPlugins", "flup");

        SQLException exception = assertThrows(SQLException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignore = DriverManager.getConnection(getUrl(), props)) {
                // just in case we do create a connection
            }
        });
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_noKnownAuthPlugins));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Srp", "Legacy_Auth" })
    void testAuthenticateDatabaseUsingCaseSensitive(String authPlugin) throws SQLException {
        assumeThat("Test requires GDS type that performs real authentication", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsCaseSensitiveUserNames(),
                "Test requires case sensitive user name support");
        final String username = "\"CaseSensitiveUser\"";
        final String password = "password";
        databaseUser.createUser(username, password,
                authPlugin.equalsIgnoreCase("Legacy_Auth") ? "Legacy_UserManager" : authPlugin);
        Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        connectionProperties.setProperty("authPlugins", authPlugin);
        try (Connection connection = DriverManager.getConnection(getUrl(), connectionProperties);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$AUTH_METHOD, MON$USER "
                             + "from MON$ATTACHMENTS "
                             + "where MON$ATTACHMENT_ID = CURRENT_CONNECTION")
        ) {
            assertTrue(resultSet.next(), "Expected a row with attachment information");
            assertEquals(authPlugin, resultSet.getString(1), "Unexpected authentication method");
            assertEquals("CaseSensitiveUser", resultSet.getString(2).trim(), "Unexpected user name");
        }
    }

    @Test
    void testUrlEncodedPropertiesDecode()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Using reflection to access internal implementation
        Method convertUrlParams = FBDriver.class.getDeclaredMethod("convertUrlParams", String.class, Map.class);
        convertUrlParams.setAccessible(true);

        String url = "jdbc:firebird://localhost/database?key=value&key+semicolon=value%3bsemicolon"
                + "&key+percent=value%25percent&key+plus=value%2Bplus&key+ampersand=value%26ampersand"
                + "&key+equals_unescaped=value=equals&key+equals_escaped=value%3Dequals"
                + "&key+euro=value%e2%82%aceuro&key space=value space";
        Map<String, String> props = new HashMap<>();

        convertUrlParams.invoke(null, url, props);

        assertEquals("value", props.get("key"), "key");
        assertEquals("value;semicolon", props.get("key semicolon"), "key semicolon");
        assertEquals("value%percent", props.get("key percent"), "key percent");
        assertEquals("value+plus", props.get("key plus"), "key plus");
        assertEquals("value&ampersand", props.get("key ampersand"), "key ampersand");
        assertEquals("value=equals", props.get("key equals_unescaped"), "key equals_unescaped");
        assertEquals("value=equals", props.get("key equals_escaped"), "key equals_escaped");
        assertEquals("value\u20aceuro", props.get("key euro"), "key euro");
        assertEquals("value space", props.get("key space"), "key space");
    }

    @Test
    void testUrlEncodedPropertiesDecode_illegalEscape() throws Throwable {
        // Using reflection to access internal implementation
        Method convertUrlParams = FBDriver.class.getDeclaredMethod("convertUrlParams", String.class, Map.class);
        convertUrlParams.setAccessible(true);

        String url = "jdbc:firebird://localhost/database?key+invalid_escape=value%xyinvalid";
        Map<String, String> props = new HashMap<>();

        SQLException exception = assertThrows(SQLNonTransientConnectionException.class, () -> {
            try {
                convertUrlParams.invoke(null, url, props);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionString),
                message(containsString(url)),
                message(containsString("java.lang.IllegalArgumentException"))));
    }

    @Test
    void testNormalizeProperties() throws Exception {
        Properties props = new Properties();
        props.put("socket_buffer_size", "8192");
        props.put("blob_buffer_size", "16384");
        props.put("TRANSACTION_READ_COMMITTED", "read_committed,no_rec_version,write,wait");
        props.put("nonStandard1", "value1");
        props.put("database", "xyz");
        String url = "jdbc:firebirdsql://localhost/database?socket_buffer_size=32767"
                + "&TRANSACTION_REPEATABLE_READ=concurrency,write,no_wait&columnLabelForName&soTimeout=1000"
                + "&nonStandard2=value2";

        Map<String, String> mergedProps = FBDriver.normalizeProperties(url, props);

        assertEquals(9, mergedProps.size(), "size");
        // NOTE: actual property name resulting from normalization should be considered an implementation detail
        // This might change in a future version
        assertEquals("32767", mergedProps.get("socketBufferSize"), "socketBufferSize");
        assertEquals("16384", mergedProps.get("blobBufferSize"), "blobBufferSize");
        assertEquals("read_committed,no_rec_version,write,wait", mergedProps.get("TRANSACTION_READ_COMMITTED"),
                "TRANSACTION_READ_COMMITTED");
        assertEquals("concurrency,write,no_wait", mergedProps.get("TRANSACTION_REPEATABLE_READ"),
                "TRANSACTION_REPEATABLE_READ");
        assertEquals("", mergedProps.get("columnLabelForName"), "columnLabelForName");
        assertEquals("1000", mergedProps.get("soTimeout"), "soTimeout");
        assertEquals("value1", mergedProps.get("nonStandard1"), "nonStandard1");
        assertEquals("value2", mergedProps.get("nonStandard2"), "nonStandard2");
        assertEquals("xyz", mergedProps.get("attachObjectName"), "attachObjectName (database)");
    }

    @Test
    void testNormalizeProperties_dpbShortAliasAndLongAlias_merged() throws Exception {
        Properties props = new Properties();
        props.put("socket_buffer_size", "1024");
        String url = "jdbc:firebirdsql://localhost/database?socket_buffer_size=32767";

        Map<String, String> mergedProps = FBDriver.normalizeProperties(url, props);

        assertEquals(1, mergedProps.size(), "size");
        assertTrue(mergedProps.containsKey("socketBufferSize"), "socketBufferSize");
    }

    @Test
    void testNormalizeProperties_multipleAliases_throwsException() {
        Properties props = new Properties();
        props.put("socket_buffer_size", "1024");
        String url = "jdbc:firebirdsql://localhost/database?socketBufferSize=32767";

        assertThrows(SQLException.class, () -> FBDriver.normalizeProperties(url, props));
    }

    /**
     * Test Srp authentication with an account (DAVIDS) that produces a hash with leading zero.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-635">JDBC-635</a>.
     * </p>
     */
    @Test
    void testProblematicUserAccount_DAVIDS() throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(3, 0), "Requires Firebird 3 or higher");
        assumeTrue(EnvironmentRequirement.ALL_SRP_PLUGINS.isMet(), "Requires Srp256");
        String username = "DAVIDS";
        String password = "aaa123";
        databaseUser.createUser(username, password, "Srp");

        Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        connectionProperties.setProperty("authPlugins", "Srp256");

        try (Connection connection = DriverManager.getConnection(getUrl(), connectionProperties)) {
            assertTrue(connection.isValid(1000));
        }
    }
}

