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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi;
import org.firebirdsql.gds.ng.wire.auth.srp.*;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDriver {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private Connection connection;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        closeQuietly(connection);
    }

    @Test
    public void testAcceptsURL() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        assertTrue(driver.acceptsURL(getUrl()));
    }

    @Test
    public void testConnect() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());
        connection = driver.connect(getUrl(), getDefaultPropertiesForConnection());

        assertNotNull("Connection is null", connection);
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
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("set_db_sql_dialect", "1");

        // open connection and convert DB to SQL dialect 1
        connection = DriverManager.getConnection(getUrl(), info);
        SQLWarning warning = connection.getWarnings();

        assertNotNull("Connection should have at least one warning.", warning);
        assertThat(warning, allOf(
                isA(SQLWarning.class),
                errorCodeEquals(ISCConstants.isc_dialect_reset_warning),
                message(startsWith(getFbMessage(ISCConstants.isc_dialect_reset_warning)))
        ));

        connection.clearWarnings();

        assertNull("After clearing no warnings should be present.", connection.getWarnings());
    }

    @Test
    public void testDialect1() throws Exception {
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("isc_dpb_sql_dialect", "1");

        connection = DriverManager.getConnection(getUrl(), info);
        try (Statement stmt = connection.createStatement()) {
            // Dialect 1 allows double quotes in strings
            ResultSet rs = stmt.executeQuery("SELECT  cast(\"today\" as date) - 7 FROM rdb$database");

            assertTrue("Should have at least one row.", rs.next());
        }
    }

    @Test
    public void testGetSQLState() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            expectedException.expect(SQLSyntaxErrorException.class);
            expectedException.expect(sqlState(is(SQLStateConstants.SQL_STATE_SYNTAX_ERROR)));

            stmt.executeQuery("select * from");
        }
    }

    @Test
    public void testLongRange() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement s = connection.createStatement()) {
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
        connection = getConnectionViaDriverManager();
        try (Statement s = connection.createStatement()) {
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
    public void testClose() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE test(id INTEGER, test_value INTEGER)");
            stmt.executeUpdate("INSERT INTO test VALUES (1, 1)");
            connection.setAutoCommit(false);
            stmt.executeUpdate("UPDATE test SET test_value = 2 WHERE id = 1");
        } finally {
            connection.close();
        }

        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT test_value FROM test WHERE id = 1")) {
                assertTrue("Should have at least one row", rs.next());
                assertEquals("Value should be 1.", 1, rs.getInt(1));
                assertTrue("Should have only one row.", !rs.next());
            }
        } finally {
            connection.close();
        }
    }

    @Test
    public void testDummyPacketIntervalConnect() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("soTimeout", "2000");

        Driver driver = DriverManager.getDriver(getUrl());
        connection = driver.connect(getUrl(), props);

        assertNotNull("Connection is null", connection);
    }

    /**
     * Connection url parsing itself is tested in {@link org.firebirdsql.gds.impl.TestDbAttachInfo}.
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
        connection = DriverManager.getConnection(getUrl(), props);
        FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
        TransactionParameterBuffer tpb = fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

        assertEquals(4, tpb.size());
        assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(ISCConstants.isc_tpb_read_committed));
        assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(ISCConstants.isc_tpb_no_rec_version));
        assertTrue("expected isc_tpb_write", tpb.hasArgument(ISCConstants.isc_tpb_write));
        assertTrue("expected isc_tpb_nowait", tpb.hasArgument(ISCConstants.isc_tpb_nowait));
    }

    @Test
    public void testTransactionConfigThroughConnectionString() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        String jdbcUrl = getUrl() + "?TRANSACTION_READ_COMMITTED=isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait";
        connection = DriverManager.getConnection(jdbcUrl, props);
        FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
        TransactionParameterBuffer tpb = fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

        assertEquals(4, tpb.size());
        assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(ISCConstants.isc_tpb_read_committed));
        assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(ISCConstants.isc_tpb_no_rec_version));
        assertTrue("expected isc_tpb_write", tpb.hasArgument(ISCConstants.isc_tpb_write));
        assertTrue("expected isc_tpb_nowait", tpb.hasArgument(ISCConstants.isc_tpb_nowait));
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
        assumeThat("Srp224 not supported in Java 7 (SHA-224 not available by default)",
                System.getProperty("java.version"), not(startsWith("1.7")));
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
        connection = DriverManager.getConnection(getUrl(), props);

        try (Statement statement = connection.createStatement();
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

        connection = DriverManager.getConnection(getUrl(), props);
    }

}

