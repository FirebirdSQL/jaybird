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
import org.firebirdsql.common.rules.DatabaseUserRule;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.*;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDriverTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final DatabaseUserRule databaseUserRule = DatabaseUserRule.withDatabaseUser();

    @Test
    public void testAcceptsURL() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        assertTrue(driver.acceptsURL(getUrl()));
    }

    @Test
    public void testConnect() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());
        try (Connection connection = driver.connect(getUrl(), getDefaultPropertiesForConnection())) {
            assertNotNull("Connection is null", connection);
        }
    }

    @Test
    public void testJdbcCompliant() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        // current driver is not JDBC compliant.
        assertTrue(driver.jdbcCompliant());
    }

    /**
     * This method tests if driver correctly handles warnings returned from
     * database. We use SQL dialect mismatch between client and server to
     * make server return us a warning.
     */
    @Test
    public void testWarnings() throws Exception {
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("set_db_sql_dialect", "1");

        // open connection and convert DB to SQL dialect 1
        try (Connection connection = DriverManager.getConnection(getUrl(), info)) {
            SQLWarning warning = connection.getWarnings();

            assertNotNull("Connection should have at least one warning.", warning);
            assertThat(warning, allOf(
                    isA(SQLWarning.class),
                    errorCodeEquals(ISCConstants.isc_dialect_reset_warning),
                    message(startsWith(getFbMessage(ISCConstants.isc_dialect_reset_warning)))
            ));

            connection.clearWarnings();

            assertNull("After clearing no warnings should be present.", connection.getWarnings());
        } finally {
            // Reset db dialect back to 3 to avoid issues with following tests
            info.setProperty("set_db_sql_dialect", "3");
            try (Connection connection1 = DriverManager.getConnection(getUrl(), info)) {
                assertTrue(connection1.isValid(0));
            }
        }
    }

    @Test
    public void testDialect1() throws Exception {
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("isc_dpb_sql_dialect", "1");

        try (Connection connection = DriverManager.getConnection(getUrl(), info);
             Statement stmt = connection.createStatement()) {
            // Dialect 1 allows double quotes in strings
            ResultSet rs = stmt.executeQuery("SELECT  cast(\"today\" as date) - 7 FROM rdb$database");

            assertTrue("Should have at least one row.", rs.next());
        }
    }

    @Test
    public void testGetSQLState() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            expectedException.expect(SQLSyntaxErrorException.class);
            expectedException.expect(sqlState(is(SQLStateConstants.SQL_STATE_SYNTAX_ERROR)));

            stmt.executeQuery("select * from");
        }
    }

    @Test
    public void testLongRange() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE LONGTEST (LONGID DECIMAL(18) NOT NULL PRIMARY KEY)");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MAX_VALUE + ")");
            try (ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST")) {
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", Long.MAX_VALUE, rs.getLong(1));
            }

            s.execute("DELETE FROM LONGTEST");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MIN_VALUE + ")");
            try (ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST")) {
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", Long.MIN_VALUE, rs.getLong(1));
            }
        }
    }

    private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    @Test
    public void testDate() throws Exception {
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
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", x, rs.getTimestamp(1, cal));
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
    public void testRollbackOnClose() throws Exception {
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
                assertTrue("Should have at least one row", rs.next());
                assertEquals("Value should be 1.", 1, rs.getInt(1));
                assertFalse("Should have only one row.", rs.next());
            }
        }
    }

    @Test
    public void testDummyPacketIntervalConnect() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("soTimeout", "2000");

        Driver driver = DriverManager.getDriver(getUrl());
        try (Connection connection = driver.connect(getUrl(), props)) {
            assertNotNull("Connection is null", connection);
        }
    }

    /**
     * Connection url parsing itself is tested in {@code DbAttachInfoTest}.
     */
    @Test
    public void testInvalidConnectionUrl() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        Driver driver = DriverManager.getDriver("jdbc:firebirdsql://localhost:c:/data/db/test.fdb");
        expectedException.expect(SQLNonTransientConnectionException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionString),
                message(containsString("Bad port: 'c:' is not a number"))));

        driver.connect("jdbc:firebirdsql://localhost:c:/data/db/test.fdb", props);
    }

    @Test
    public void testTransactionConfigThroughPropertiesObject() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        props.setProperty("TRANSACTION_READ_COMMITTED",
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertEquals(4, tpb.size());
            assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(isc_tpb_read_committed));
            assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(isc_tpb_no_rec_version));
            assertTrue("expected isc_tpb_write", tpb.hasArgument(isc_tpb_write));
            assertTrue("expected isc_tpb_nowait", tpb.hasArgument(isc_tpb_nowait));
        }
    }

    @Test
    public void testTransactionConfigThroughConnectionString() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        String jdbcUrl = getUrl() + "?TRANSACTION_READ_COMMITTED=isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, props)) {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertEquals(4, tpb.size());
            assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(isc_tpb_read_committed));
            assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(isc_tpb_no_rec_version));
            assertTrue("expected isc_tpb_write", tpb.hasArgument(isc_tpb_write));
            assertTrue("expected isc_tpb_nowait", tpb.hasArgument(isc_tpb_nowait));
        }
    }

    @Test
    public void testConnectionLegacy_Auth() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME);
    }

    @Test
    public void testConnectionSrp() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(SrpAuthenticationPluginSpi.SRP_AUTH_NAME);
    }

    @Test
    public void testConnectionSrp224() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(Srp224AuthenticationPluginSpi.SRP_224_AUTH_NAME);
    }

    @Test
    public void testConnectionSrp256() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(Srp256AuthenticationPluginSpi.SRP_256_AUTH_NAME);
    }

    @Test
    public void testConnectionSrp384() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(Srp384AuthenticationPluginSpi.SRP_384_AUTH_NAME);
    }

    @Test
    public void testConnectionSrp512() throws Exception {
        // Might fail if plugin not enabled
        checkAuthenticationPlugin(Srp512AuthenticationPluginSpi.SRP_512_AUTH_NAME);
    }

    private void checkAuthenticationPlugin(String pluginName) throws Exception {
        assumeThat("Test doesn't work with embedded", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        assumeTrue("Requires Firebird 3 or higher", getDefaultSupportInfo().isVersionEqualOrAbove(3, 0));
        // NOTE: If the test still fails, then this plugin is not enabled in the Firebird AuthServer config
        assumeTrue("Test requires support for authentication plugin " + pluginName,
                getDefaultSupportInfo().supportsAuthenticationPlugin(pluginName));

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
            assertTrue("expected row", rs.next());
            assertEquals("Unexpected authentication method", pluginName, rs.getString(1));
        }
    }

    @Test
    public void testAuthPluginsUnknown_pureJava() throws Exception {
        assumeThat("Type is pure Java", FBTestProperties.GDS_TYPE, isPureJavaType());
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("authPlugins", "flup");

        expectedException.expect(errorCodeEquals(JaybirdErrorCodes.jb_noKnownAuthPlugins));

        try (Connection ignore = DriverManager.getConnection(getUrl(), props)) {
            // ignore
        }
    }

    @Test
    public void authenticateDatabaseUsingCaseSensitiveSrpAccount() throws Exception {
        checkCaseSensitiveLogin("Srp");
    }

    @Test
    public void authenticateDatabaseUsingCaseSensitiveLegacyAccount() throws Exception {
        checkCaseSensitiveLogin("Legacy_Auth");
    }

    private void checkCaseSensitiveLogin(String authPlugin) throws SQLException {
        assumeThat("Test requires GDS type that performs real authentication", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue("Test requires case sensitive user name support",
                getDefaultSupportInfo().supportsCaseSensitiveUserNames());
        final String username = "\"CaseSensitiveUser\"";
        final String password = "password";
        databaseUserRule.createUser(username, password,
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
            assertTrue("Expected a row with attachment information", resultSet.next());
            assertEquals("Unexpected authentication method", authPlugin, resultSet.getString(1));
            assertEquals("Unexpected user name", "CaseSensitiveUser", resultSet.getString(2).trim());
        }
    }

    @Test
    public void testUrlEncodedPropertiesDecode()
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

        assertEquals("key", "value", props.get("key"));
        assertEquals("key semicolon", "value;semicolon", props.get("key semicolon"));
        assertEquals("key percent", "value%percent", props.get("key percent"));
        assertEquals("key plus", "value+plus", props.get("key plus"));
        assertEquals("key ampersand", "value&ampersand", props.get("key ampersand"));
        assertEquals("key equals_unescaped", "value=equals", props.get("key equals_unescaped"));
        assertEquals("key equals_escaped", "value=equals", props.get("key equals_escaped"));
        assertEquals("key euro", "value\u20aceuro", props.get("key euro"));
        assertEquals("key space", "value space", props.get("key space"));
    }

    @Test
    public void testUrlEncodedPropertiesDecode_illegalEscape() throws Throwable {
        // Using reflection to access internal implementation
        Method convertUrlParams = FBDriver.class.getDeclaredMethod("convertUrlParams", String.class, Map.class);
        convertUrlParams.setAccessible(true);

        String url = "jdbc:firebird://localhost/database?key+invalid_escape=value%xyinvalid";
        Map<String, String> props = new HashMap<>();

        expectedException.expect(SQLNonTransientConnectionException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionString),
                message(containsString(url)),
                message(containsString("java.lang.IllegalArgumentException"))));

        try {
            convertUrlParams.invoke(null, url, props);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testNormalizeProperties() throws Exception {
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

        assertEquals("size", 9, mergedProps.size());
        // NOTE: actual property name resulting from normalization should be considered an implementation detail
        // This might change in a future version
        assertEquals("socketBufferSize", "32767", mergedProps.get("socketBufferSize"));
        assertEquals("blobBufferSize", "16384", mergedProps.get("blobBufferSize"));
        assertEquals("TRANSACTION_READ_COMMITTED", "read_committed,no_rec_version,write,wait",
                mergedProps.get("TRANSACTION_READ_COMMITTED"));
        assertEquals("TRANSACTION_REPEATABLE_READ", "concurrency,write,no_wait",
                mergedProps.get("TRANSACTION_REPEATABLE_READ"));
        assertEquals("columnLabelForName", "", mergedProps.get("columnLabelForName"));
        assertEquals("soTimeout", "1000", mergedProps.get("soTimeout"));
        assertEquals("nonStandard1", "value1", mergedProps.get("nonStandard1"));
        assertEquals("nonStandard2", "value2", mergedProps.get("nonStandard2"));
        assertEquals("attachObjectName (database)", "xyz", mergedProps.get("attachObjectName"));
    }

    @Test
    public void testNormalizeProperties_dpbShortAliasAndLongAlias_merged() throws Exception {
        Properties props = new Properties();
        props.put("socket_buffer_size", "1024");
        String url = "jdbc:firebirdsql://localhost/database?socket_buffer_size=32767";

        Map<String, String> mergedProps = FBDriver.normalizeProperties(url, props);

        assertEquals("size", 1, mergedProps.size());
        assertTrue("socketBufferSize", mergedProps.containsKey("socketBufferSize"));
    }

    @Test
    public void testNormalizeProperties_multipleAliases_throwsException() throws Exception {
        Properties props = new Properties();
        props.put("socket_buffer_size", "1024");
        String url = "jdbc:firebirdsql://localhost/database?socketBufferSize=32767";

        expectedException.expect(SQLException.class);

        FBDriver.normalizeProperties(url, props);
    }

    /**
     * Test Srp authentication with an account (DAVIDS) that produces a hash with leading zero.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-635">JDBC-635</a>.
     * </p>
     */
    @Test
    public void testProblematicUserAccount_DAVIDS() throws Exception {
        assumeTrue("Requires Firebird 3 or higher", getDefaultSupportInfo().isVersionEqualOrAbove(3, 0));
        String username = "DAVIDS";
        String password = "aaa123";
        databaseUserRule.createUser(username, password, "Srp");

        Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        connectionProperties.setProperty("authPlugins", "Srp256");

        try (Connection connection = DriverManager.getConnection(getUrl(), connectionProperties)) {
            assertTrue(connection.isValid(1000));
        }
    }
}

