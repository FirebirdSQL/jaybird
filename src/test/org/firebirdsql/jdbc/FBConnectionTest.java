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

import org.firebirdsql.common.ConfigHelper;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.DatabaseUserExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.wire.crypt.FBSQLEncryptException;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.SystemPropertyHelper.withTemporarySystemProperty;
import static org.firebirdsql.common.assertions.CustomAssertions.assertThrowsForAutoCloseable;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.firebirdsql.gds.ISCConstants.fb_info_wire_crypt;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.ISCConstants.isc_net_read_err;
import static org.firebirdsql.gds.JaybirdSystemProperties.DEFAULT_CONNECTION_ENCODING_PROPERTY;
import static org.firebirdsql.gds.JaybirdSystemProperties.PROCESS_ID_PROP;
import static org.firebirdsql.gds.JaybirdSystemProperties.PROCESS_NAME_PROP;
import static org.firebirdsql.gds.JaybirdSystemProperties.REQUIRE_CONNECTION_ENCODING_PROPERTY;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Test cases for FirebirdConnection interface.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class FBConnectionTest {

    private static final String CREATE_TABLE = """
            CREATE TABLE test (
              col1 INTEGER
            )""";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            List.of(CREATE_TABLE));
    
    private static final String IGNORE_PROCESS_NAME = "##IGNORE_PROCESS_NAME##";

    @RegisterExtension
    final DatabaseUserExtension databaseUser = DatabaseUserExtension.withDatabaseUser();

    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String CLEAR_DATA = "DELETE FROM test";

    /**
     * Test if {@link FirebirdConnection#setTransactionParameters(int, int[])} method works correctly.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testTpbMapping(boolean useTpbOption) throws Exception {
        try (Connection conA = getConnectionViaDriverManager()) {
            conA.setAutoCommit(false);
            try (var stmt = conA.createStatement()) {
                stmt.execute(CLEAR_DATA);
            }

            PreparedStatement ps = conA.prepareStatement(INSERT_DATA);
            ps.setInt(1, 1);
            ps.execute();
            ps.close();

            conA.commit();

            try (FirebirdConnection conB = getConnectionViaDriverManager()) {
                conB.setAutoCommit(false);

                if (useTpbOption) {
                    // This is the correct way to set transaction parameters
                    TransactionParameterBuffer tpb = conB.createTransactionParameterBuffer();
                    tpb.addArgument(isc_tpb_read_committed);
                    tpb.addArgument(isc_tpb_rec_version);
                    tpb.addArgument(isc_tpb_write);
                    tpb.addArgument(isc_tpb_nowait);

                    conB.setTransactionParameters(Connection.TRANSACTION_READ_COMMITTED, tpb);
                } else {
                    // This is the deprecated method, tested to check the backward compatibility
                    conB.setTransactionParameters(
                            Connection.TRANSACTION_READ_COMMITTED,
                            new int[] {
                                    isc_tpb_read_committed,
                                    isc_tpb_rec_version,
                                    isc_tpb_write,
                                    isc_tpb_nowait
                            });
                }
                conB.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                try (Statement stmtA = conA.createStatement();
                     Statement stmtB = conB.createStatement()) {

                    stmtA.execute("UPDATE test SET col1 = 2");

                    assertThrows(SQLException.class, () -> stmtB.execute("UPDATE test SET col1 = 3"),
                            "Should notify about a deadlock");
                }
            }
        }
    }

    @Test
    void testStatementCompletion() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery("SELECT * FROM rdb$database");
                connection.rollback();
                connection.setAutoCommit(true);
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery("SELECT * FROM rdb$database");
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM rdb$database"));
            }
        }
    }

    @Test
    void testExecuteStatementTwice() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, "CREATE TABLE test_exec_twice(col1 VARCHAR(100))");

            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                String select1 = "SELECT * FROM test_exec_twice";
                String select2 = select1 + " WHERE col1 > ? ORDER BY col1";

                PreparedStatement pstmt = connection.prepareStatement(select2);

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(select1);  // throws Exception on the 2nd call
                rs.close();

                pstmt.setString(1, "ABC");
                rs = pstmt.executeQuery();
                //noinspection StatementWithEmptyBody
                for (int i = 0; i < 10 && rs.next(); i++)
                    ;   // do something
                rs.close();

                // on the following 2nd call the exception gets thrown
                rs = stmt.executeQuery(select1);  // throws Exception on the 2nd call
                rs.close();

                pstmt.setString(1, "ABC");
                rs = pstmt.executeQuery();
                //noinspection StatementWithEmptyBody
                for (int i = 0; i < 10 && rs.next(); i++)
                    ;   // do something
                rs.close();
            } finally {
                connection.commit();
            }
        }
    }

    @Test
    void testLockTable() throws Exception {
        try (FirebirdConnection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            executeCreateTable(connection, "CREATE TABLE test_lock(col1 INTEGER)");

            TransactionParameterBuffer tpb = connection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            if (tpb.hasArgument(isc_tpb_wait)) {
                tpb.removeArgument(isc_tpb_wait);
                tpb.addArgument(isc_tpb_nowait);
            }

            connection.setTransactionParameters(Connection.TRANSACTION_READ_COMMITTED, tpb);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            connection.setAutoCommit(false);

            try (FirebirdConnection anotherConnection = getConnectionViaDriverManager()) {
                anotherConnection.setAutoCommit(false);
                TransactionParameterBuffer anotherTpb = anotherConnection.createTransactionParameterBuffer();

                anotherTpb.addArgument(isc_tpb_consistency);
                anotherTpb.addArgument(isc_tpb_write);
                anotherTpb.addArgument(isc_tpb_nowait);

                anotherTpb.addArgument(isc_tpb_lock_write, "TEST_LOCK");
                anotherTpb.addArgument(isc_tpb_protected);

                anotherConnection.setTransactionParameters(anotherTpb);

                try (Statement anotherStmt = anotherConnection.createStatement()) {
                    anotherStmt.execute("INSERT INTO test_lock VALUES(1)");
                }

                SQLException exception = assertThrows(SQLException.class,
                        () -> stmt.execute("INSERT INTO test_lock VALUES(2)"),
                        "Should throw an error because of lock conflict");
                assertThat(exception, errorCodeEquals(ISCConstants.isc_lock_conflict));
            }
        }
    }

    @Test
    void testMetaDataTransaction() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = assertDoesNotThrow(() -> metaData.getTables(null, null, "RDB$DATABASE", null));
            rs.close();
        }
    }

    @Test
    void testTransactionCoordinatorAutoCommitChange() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            try (PreparedStatement ignored = connection.prepareStatement("SELECT * FROM rdb$database")) {
                connection.setAutoCommit(false);
            }
            // TODO This doesn't seem to test anything
        }
    }

    @Test
    void testDefaultHoldableResultSet() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("defaultHoldable", "true");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            Statement stmt1 = connection.createStatement();
            assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, stmt1.getResultSetHoldability());

            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery("SELECT rdb$collation_name, rdb$character_set_id FROM rdb$collations");
            while (rs1.next()) {
                ResultSet rs2 = stmt2.executeQuery("SELECT rdb$character_set_name FROM rdb$character_sets " +
                        "WHERE rdb$character_set_id = " + rs1.getInt(2));

                assertTrue(rs2.next(), "Should find corresponding charset");
            }
        }
    }

    @Test
    void testGetAttachments() throws Exception {
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager()) {
            FbDatabase database = connection.getFbDatabase();

            byte[] infoRequest = new byte[] { ISCConstants.isc_info_user_names, ISCConstants.isc_info_end };
            byte[] reply = database.getDatabaseInfo(infoRequest, 1024);

            int i = 0;

            while (reply[i] != ISCConstants.isc_info_end) {
                if (reply[i++] == ISCConstants.isc_info_user_names) {
                    //iscVaxInteger2(reply, i); // can be ignored
                    i += 2;
                    int strLen = reply[i] & 0xff;
                    i += 1;
                    String userName = new String(reply, i, strLen);
                    i += strLen;
                    // This assumes unquoted, non-mapped users
                    assertThat(userName, equalToIgnoringCase(DB_USER));
                }
            }
        }
    }

    @Test
    void testWireProtocolCompatibility() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT max(rdb$format) FROM rdb$formats");
            assertTrue(rs.next(), "Should fetch some rows");
        }
    }

    /**
     * Test if not explicitly specifying a connection character set connects with the character set specified in system
     * property {@code org.firebirdsql.jdbc.defaultConnectionEncoding}.
     */
    @Test
    void testNoCharacterSetWithDefaultConnectionEncoding() throws Exception {
        try (var ignored = withTemporarySystemProperty(DEFAULT_CONNECTION_ENCODING_PROPERTY, "WIN1252")){
            Properties props = getDefaultPropertiesForConnection();
            props.remove("lc_ctype");
            try (Connection con = DriverManager.getConnection(getUrl(), props)) {
                // Previously, a warning was registered, verify that doesn't happen
                assertNull(con.getWarnings(), "Expected no warning for not specifying connection character set");
                IConnectionProperties connectionProperties =
                        con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
                assertEquals("WIN1252", connectionProperties.getEncoding(), "Unexpected connection encoding");
            }
        }
    }

    /**
     * Test if not explicitly specifying a connection character set does not result in an exception on the connection
     * and sets encoding to NONE when system properties {@code org.firebirdsql.jdbc.defaultConnectionEncoding} and
     * {@code org.firebirdsql.jdbc.requireConnectionEncoding} have not been set.
     */
    @Test
    void testNoCharacterSetWithoutDefaultConnectionEncodingDefaultsToNONEIfEncodingNotRequired() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");

        try (var ignored1 = withTemporarySystemProperty(REQUIRE_CONNECTION_ENCODING_PROPERTY, null);
             var ignored2 = withTemporarySystemProperty(DEFAULT_CONNECTION_ENCODING_PROPERTY, null);
             var connection = DriverManager.getConnection(getUrl(), props)) {
            // Previously, a warning was registered, verify that doesn't happen
            assertNull(connection.getWarnings(), "Expected no warning for not specifying connection character set");
            IConnectionProperties connectionProperties =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
            assertEquals("NONE", connectionProperties.getEncoding(), "Unexpected connection encoding");
        }
    }

    /**
     * Test if not explicitly specifying a connection character set results in an exception on the connection when
     * system property {@code org.firebirdsql.jdbc.defaultConnectionEncoding} has not been set and
     * {@code org.firebirdsql.jdbc.requireConnectionEncoding} has been set to {@code true}
     */
    @Test
    void testNoCharacterSetExceptionWithoutDefaultConnectionEncodingAndEncodingRequired() {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");

        try (var ignored1 = withTemporarySystemProperty(DEFAULT_CONNECTION_ENCODING_PROPERTY, null);
             var ignored2 = withTemporarySystemProperty(REQUIRE_CONNECTION_ENCODING_PROPERTY, "true")){
            var exception = assertThrowsForAutoCloseable(SQLNonTransientConnectionException.class,
                    () -> DriverManager.getConnection(getUrl(), props));
            assertThat(exception, message(equalTo(FBManagedConnection.ERROR_NO_CHARSET)));
        }
    }

    /**
     * Test if explicitly specifying a connection character set does not add a warning (or exception) on the connection.
     */
    @Test
    void testCharacterSetFirebirdNoWarning() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "WIN1252");

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
            SQLWarning warnings = con.getWarnings();
            assertNull(warnings, "Expected no warning when specifying connection character set");
            IConnectionProperties connectionProperties =
                    con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
            assertEquals("WIN1252", connectionProperties.getEncoding(), "Unexpected connection encoding");
        }
    }

    /**
     * Test if explicitly specifying a connection character set does not add a warning (or exception) on the connection.
     */
    @Test
    void testCharacterSetJavaNoWarning() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        props.setProperty("charSet", "Cp1254");

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
            SQLWarning warnings = con.getWarnings();
            assertNull(warnings, "Expected no warning when specifying connection character set");
            IConnectionProperties connectionProperties =
                    con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
            assertEquals("WIN1254", connectionProperties.getEncoding(), "Unexpected connection encoding");
        }
    }

    @Test
    void testProcessNameThroughConnectionProperty() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        final Properties props = getDefaultPropertiesForConnection();
        final var processName = "Test process name";
        props.setProperty(PropertyNames.processName, processName);

        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            assertProcessNameAndId(connection, processName, -1);
        }
    }

    /**
     * Asserts the process name and process id of a connection.
     *
     * @param connection
     *         connection
     * @param expectedProcessName
     *         expected process name ({@code null} for no process name, use {@link #IGNORE_PROCESS_NAME} to ignore this
     *         check)
     * @param expectedProcessId
     *         expected process id (use {@code -1} to ignore this check)
     */
    private void assertProcessNameAndId(Connection connection, String expectedProcessName, int expectedProcessId)
            throws SQLException {
        if (IGNORE_PROCESS_NAME.equals(expectedProcessName) && expectedProcessId == -1) return;
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(
                     "select MON$REMOTE_PROCESS, MON$REMOTE_PID from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
            assertTrue(resultSet.next());
            if (!IGNORE_PROCESS_NAME.equals(expectedProcessName)) {
                assertEquals(expectedProcessName, resultSet.getString(1), "process name");
            }
            if (expectedProcessId != -1) {
                assertEquals(expectedProcessId, resultSet.getInt(2), "process id");
            }
        }
    }

    @Test
    void testProcessIdThroughConnectionProperty() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        assumeThat("Test only works in pure java", GDS_TYPE, isPureJavaType());
        final Properties props = getDefaultPropertiesForConnection();
        final int processId = 5843;
        props.setProperty(PropertyNames.processId, String.valueOf(processId));

        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            assertProcessNameAndId(connection, IGNORE_PROCESS_NAME, processId);
        }
    }

    @Test
    void testUseActualProcessId() throws Exception {
        assumeThat("embedded does not report process id", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        final Properties props = getDefaultPropertiesForConnection();
        final long actualProcessId = ProcessHandle.current().pid();

        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            assertProcessNameAndId(connection, IGNORE_PROCESS_NAME, (int) actualProcessId);
        }
    }

    @Test
    void testProcessNameAndIdThroughConnectionPropertyTakesPrecedence() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        assumeThat("Test only works in pure java", GDS_TYPE, isPureJavaType());
        final Properties props = getDefaultPropertiesForConnection();
        final var processNameThroughConnection = "Process name in connection property";
        props.setProperty(PropertyNames.processName, processNameThroughConnection);
        final int processIdThroughConnection = 513;
        props.setProperty(PropertyNames.processId, String.valueOf(processIdThroughConnection));
        final var processNameThroughSystemProp = "Process name in system property";
        final int processIdThroughSystemProp = 132;

        try (var ignored1 = withTemporarySystemProperty(PROCESS_NAME_PROP, processNameThroughSystemProp);
             var ignored2 = withTemporarySystemProperty(PROCESS_ID_PROP, String.valueOf(processIdThroughSystemProp));
             var connection = DriverManager.getConnection(getUrl(), props)) {
            assertProcessNameAndId(connection, processNameThroughConnection, processIdThroughConnection);
        }
    }

    @Test
    void testConnectionsViaDriverManagerAreDistinct() throws Exception {
        try (Connection connection1 = getConnectionViaDriverManager();
             Connection connection2 = getConnectionViaDriverManager()) {
            assertNotSame(connection1, connection2);

            connection1.close();
            assertTrue(connection1.isClosed());
            assertFalse(connection2.isClosed());
        }
    }

    @Test
    void testIPv6AddressHandling() throws Exception {
        assumeThat("Test only works in pure java", GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(3, 0), "Firebird 3 or higher required for IPv6 testing");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:firebirdsql://[::1]/" + getDatabasePath() + "?charSet=utf-8", DB_USER, DB_PASSWORD)) {
            assertTrue(connection.isValid(0));
        }
    }

    @Test
    void testIPv6AddressHandling_native() throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(3, 0), "Firebird 3 or higher required for IPv6 testing");
        assumeThat("Test only works for native", GDS_TYPE, isOtherNativeType());
        try (Connection connection = DriverManager.getConnection(
                "jdbc:firebirdsql:native://[::1]/" + getDatabasePath() + "?charSet=utf-8", DB_USER, DB_PASSWORD)) {
            assertTrue(connection.isValid(0));
        }
    }

    @ParameterizedTest
    @EnumSource
    void testWireCrypt_FB2_5_and_earlier(WireCrypt wireCrypt) throws Exception {
        assumeFalse(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions without wire encryption support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", wireCrypt.name());
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertTrue(connection.isValid(0));
        }
    }

    @ParameterizedTest
    @EnumSource
    void testWireCrypt_FB3_0_and_later(WireCrypt wireCrypt) {
        assumeThat("Test doesn't work with embedded", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", wireCrypt.name());
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            boolean encryptionUsed = serverVersion.isWireEncryptionUsed();
            switch (wireCrypt) {
            case DEFAULT:
            case ENABLED:
                if (!encryptionUsed) {
                    System.err.println("WARNING: wire encryption level " + wireCrypt + " requested, but no encryption "
                            + "used. Consider re-running the test with a WireCrypt=Enabled in firebird.conf");
                }
                // intentional fall-through
            case REQUIRED:
                assertTrue(encryptionUsed, "Expected wire encryption to be used for wireCrypt=" + wireCrypt);
                break;
            case DISABLED:
                assertFalse(encryptionUsed, "Expected wire encryption not to be used for wireCrypt=" + wireCrypt);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ISCConstants.isc_wirecrypt_incompatible) {
                System.err.println("WARNING: wire encryption level " + wireCrypt + " requested, but rejected by server."
                        + " Consider re-running the test with a WireCrypt=Enabled in firebird.conf");
            }
        }
    }

    @Test
    void legacyAuthUserWithWireCrypt_ENABLED_canCreateConnection() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUser.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("wireCrypt", "ENABLED");
        props.setProperty("authPlugins", "Legacy_Auth");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertFalse(serverVersion.isWireEncryptionUsed(),
                    "Expected wire encryption not to be used when connecting with legacy auth user");
        }
    }

    @Test
    void legacyAuthUserWithWireCrypt_REQUIRED_hasConnectionRejected_tryLegacy_AuthOnly() throws Exception {
        assumeThat("Test doesn't work with embedded", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUser.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("wireCrypt", "REQUIRED");
        // Using only Legacy_Auth produces different error than trying Srp and then Legacy_Auth
        props.setProperty("authPlugins", "Legacy_Auth");

        var exception = assertThrowsForAutoCloseable(FBSQLEncryptException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_miss_wirecrypt));
    }

    @Test
    void legacyAuthUserWithWireCrypt_REQUIRED_hasConnectionRejected_trySrpFirst() throws Exception {
        assumeThat("Test doesn't work with embedded", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUser.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("wireCrypt", "REQUIRED");
        // Using only Legacy_Auth produces different error than trying Srp and then Legacy_Auth
        props.setProperty("authPlugins", "Srp,Legacy_Auth");

        var exception = assertThrowsForAutoCloseable(FBSQLEncryptException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        // TODO Check if we can make behavior consistent between native and pure java
        assertThat(exception, anyOf(
                errorCodeEquals(ISCConstants.isc_wirecrypt_incompatible),
                errorCodeEquals(ISCConstants.isc_miss_wirecrypt)));
    }

    @Test
    void invalidValueForWireCrypt() {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", "NOT_A_VALID_VALUE");

        var exception = assertThrowsForAutoCloseable(SQLException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionPropertyValue),
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidConnectionPropertyValue, "NOT_A_VALID_VALUE", "wireCrypt")));
    }

    /* When testing NATIVE, this test may fail if fbclient doesn't have the ChaCha plugin, e.g. when using fbclient
       installed in %windir%\System32 instead of the Firebird installation directory. */
    @Test
    void expectedWireCryptPluginApplied() throws Exception {
        assumeThat("Test doesn't work with embedded", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(4, 0), "Requires fb_info_wire_crypt support");
        String expectedCryptPlugin = getDefaultSupportInfo().supportsWireCryptChaCha64() ? "ChaCha64" : "ChaCha";

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", "REQUIRED");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            FbDatabase fbDatabase = connection.unwrap(FirebirdConnection.class).getFbDatabase();
            InfoProcessor<String> getPluginName = info -> {
                if (info.length == 0) {
                    throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                            .messageParameter("database")
                            .toSQLException();
                }
                if ((info[0] & 0xFF) != fb_info_wire_crypt) {
                    throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                            .messageParameter(
                                    "transaction", "fb_info_wire_crypt", ISCConstants.fb_info_wire_crypt, info[0])
                            .toSQLException();
                }
                int dataLength = iscVaxInteger2(info, 1);
                if (dataLength == 0) {
                    return null;
                }
                return new String(info, 3, dataLength, StandardCharsets.US_ASCII);
            };
            String cryptPlugin = fbDatabase.getDatabaseInfo(
                    new byte[] { (byte) fb_info_wire_crypt, isc_info_end }, 100, getPluginName);

            assertEquals(expectedCryptPlugin, cryptPlugin);
        }
    }

    @Test
    void connectingWithUnknownFirebirdCharacterSetName() {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "DOES_NOT_EXIST");

        var exception = assertThrowsForAutoCloseable(SQLNonTransientConnectionException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        assertThat(exception, message(equalTo(
                "No valid encoding definition for Firebird encoding DOES_NOT_EXIST and/or Java charset null")));
    }

    @Test
    void connectingWithUnknownJavaCharacterSetName() {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        props.setProperty("charSet", "DOES_NOT_EXIST");

        var exception = assertThrowsForAutoCloseable(SQLNonTransientConnectionException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        assertThat(exception, message(equalTo(
                "No valid encoding definition for Firebird encoding null and/or Java charset DOES_NOT_EXIST")));
    }

    @Test
    void legacyAuthUserCannotConnectByDefault() throws Exception {
        assumeThat("Test assumes pure Java implementation (native uses fbclient defaults)", GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().supportsProtocol(13),
                "Test for Firebird versions with v13 or higher protocol");
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUser.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);

        // We don't try Legacy_Auth by default
        var exception = assertThrowsForAutoCloseable(SQLInvalidAuthorizationSpecException.class,
                () -> DriverManager.getConnection(getUrl(), props));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_login));
    }

    @Test
    @Timeout(10)
    void setNetworkTimeout_isUsed(@Mock ExecutorService executor) throws Exception {
        assumeThat("Test assumes pure Java implementation (native doesn't support setNetworkTimeout)",
                GDS_TYPE, isPureJavaType());
        try (var connection1 = getConnectionViaDriverManager();
             var statement1 = connection1.createStatement();
             var connection2 = getConnectionViaDriverManager(
                     "TRANSACTION_READ_COMMITTED", "read_committed,rec_version,write,wait")) {
            executeCreateTable(connection1, "create table locking (id integer, colval varchar(50))");
            statement1.execute("insert into locking(id, colval) values (1, 'abc')");

            try (var rs1 = statement1.executeQuery("select id, colval from locking with lock")) {
                rs1.next();

                connection2.setNetworkTimeout(executor, 50);
                long start = 0;
                try (var statement2 = connection2.createStatement();
                     var rs2 = statement2.executeQuery("select id, colval from locking with lock")) {
                    start = System.currentTimeMillis();
                    // Fetch will block waiting for lock held by statement1/rs1 to be released
                    SQLException exception = assertThrows(SQLException.class, rs2::next);
                    assertThat(exception, errorCodeEquals(isc_net_read_err));
                } finally {
                    long end = System.currentTimeMillis();
                    System.out.println("Time taken: " + (end - start));
                }
            } catch (SQLException e) {
                // ignore
            }

            assertTrue(connection2.isClosed(), "Expected connection2 to be closed by timeout");
        }
    }

    @Test
    void setNetworkTimeout_invalidTimeout(@Mock ExecutorService executor) throws Exception {
        try (var connection = getConnectionViaDriverManager()) {
            var exception = assertThrows(SQLException.class, () -> connection.setNetworkTimeout(executor, -1));
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_invalidTimeout));
        }
    }

    @Test
    void setNetworkTimeout_invalidExecutor() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            SQLException exception = assertThrows(SQLException.class, () -> connection.setNetworkTimeout(null, 500));
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_invalidExecutor));
        }
    }

    @Test
    void setNetworkTimeout_getAndSetSeries(@Mock ExecutorService executor) throws Exception {
        assumeThat("Type is pure Java", GDS_TYPE, isPureJavaType());
        try (var connection = getConnectionViaDriverManager()) {
            assertEquals(0, connection.getNetworkTimeout(), "Expected 0 as initial network timeout");

            connection.setNetworkTimeout(executor, 500);
            assertEquals(500, connection.getNetworkTimeout(), "Unexpected getNetworkTimeout");

            connection.setNetworkTimeout(executor, 0);
            assertEquals(0, connection.getNetworkTimeout(), "Unexpected getNetworkTimeout");
        }
    }

    @Test
    void testWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().supportsWireCompression(), "Test requires wire compression");
        try (var connection = getConnectionViaDriverManager("wireCompression", "true")) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertTrue(serverVersion.isWireCompressionUsed(), "expected wire compression in use");
        }
    }

    /**
     * Rationale: see <a href="http://tracker.firebirdsql.org/browse/JDBC-386">JDBC-386</a>
     */
    @Test
    void transactionSettingsNotShared() throws Exception {
        try (FBConnection con1 = getConnectionViaDriverManager().unwrap(FBConnection.class);
             FBConnection con2 = getConnectionViaDriverManager().unwrap(FBConnection.class)) {
            TransactionParameterBuffer con2Original =
                    con2.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ);

            TransactionParameterBuffer newParameters = con1.getFbDatabase().createTransactionParameterBuffer();
            newParameters.addArgument(isc_tpb_consistency);
            newParameters.addArgument(isc_tpb_read);
            newParameters.addArgument(isc_tpb_nowait);

            con1.setTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ, newParameters);

            assertEquals(newParameters, con1.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ),
                    "Setting of con1 update");
            assertEquals(con2Original,
                    con2.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ), "Setting of con2 unchanged");
            assertNotEquals(newParameters, con2.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ),
                    "Setting of con2 not equal to new config of con1");
        }
    }

    /**
     * Rationale: Jaybird 3.0 and 4.0 threw "wrong" error (isc_connect_reject)
     */
    @Test
    void testErrorWhenNoCredentials() {
        assumeThat("Embedded does not use authentication", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        String url = ENABLE_PROTOCOL == null ? getUrl() : getUrl() + "?enableProtocol=" + ENABLE_PROTOCOL;
        try (Connection ignored = DriverManager.getConnection(url)) {
            fail("expected exception when connecting without user name and password");
        } catch (SQLException e) {
            assertThat(e, allOf(
                    errorCodeEquals(ISCConstants.isc_login),
                    fbMessageStartsWith(ISCConstants.isc_login)));
        }
    }

    @Test
    void errorConnectingToUnsupportedVersion() {
        assumeFalse(getDefaultSupportInfo().isSupportedVersion(), "test can only work on unsupported version");
        assumeThat("restricted protocol support only in the pure Java protocol",
                FBTestProperties.GDS_TYPE, isPureJavaType());
        Properties props = getDefaultPropertiesForConnection();
        props.remove("enableProtocol");

        try (var ignored = DriverManager.getConnection(getUrl(), props)) {
            fail("expected exception when connecting to unsupported version");
        } catch (SQLException e) {
            assertThat(e, allOf(
                    errorCodeEquals(ISCConstants.isc_connect_reject),
                    fbMessageStartsWith(ISCConstants.isc_connect_reject)));
        }
    }

    @Test
    void specifyParallelWorkers() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsParallelWorkers(), "test requires support for parallel workers");
        final int maxParallelWorkers;
        try (var connection = getConnectionViaDriverManager()) {
            maxParallelWorkers = ConfigHelper.getIntConfigValue(connection, "MaxParallelWorkers").orElse(1);
        }

        Properties props = getDefaultPropertiesForConnection();
        String parallelWorkersValue = String.valueOf(maxParallelWorkers + 1);
        props.setProperty(PropertyNames.parallelWorkers, parallelWorkersValue);

        // There currently is no way to check the actual value, so relying on the fact that specifying a value higher
        // than the maximum results in a warning
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            SQLWarning warning = connection.getWarnings();
            assertNotNull(warning, "expected a warning");
            assertThat(warning, allOf(
                    errorCodeEquals(ISCConstants.isc_bad_par_workers),
                    fbMessageStartsWith(ISCConstants.isc_bad_par_workers,
                            parallelWorkersValue, String.valueOf(maxParallelWorkers))));
        }
    }

    @Test
    void abortClosesConnectionImmediately() throws Exception {
        var executor = new DelayingExecutor();
        try (var connection = getConnectionViaDriverManager()) {
            assertFalse(connection.isClosed(), "Expected open connection");
            connection.abort(executor);
            assertTrue(connection.isClosed(), "Expected closed connection immediately after abort");
            executor.proceedAndAwait();
        }
    }

    @Test
    void abortClosesStatementsAndResultSets() throws Exception {
        var executor = new DelayingExecutor();
        try (var connection = getConnectionViaDriverManager();
             var stmt1 = connection.createStatement();
             var stmt2 = connection.prepareStatement("select * from RDB$DATABASE")) {
            connection.setAutoCommit(false);
            try (var rs1 = stmt1.executeQuery("select * from RDB$DATABASE");
                 var rs2 = stmt2.executeQuery()) {
                assertFalse(connection.isClosed(), "Expected open connection");
                assertFalse(stmt1.isClosed(), "Expected open stmt1");
                assertFalse(rs1.isClosed(), "Expected open rs1");
                assertFalse(stmt2.isClosed(), "Expected open stmt2");
                assertFalse(rs2.isClosed(), "Expected open rs2");

                connection.abort(executor);

                assertTrue(connection.isClosed(), "Expected closed connection");
                assertFalse(stmt1.isClosed(), "Expected open stmt1");
                assertFalse(rs1.isClosed(), "Expected open rs1");
                assertFalse(stmt2.isClosed(), "Expected open stmt2");
                assertFalse(rs2.isClosed(), "Expected open rs2");

                executor.proceedAndAwait();

                assertTrue(connection.isClosed(), "Expected closed connection");
                assertTrue(stmt1.isClosed(), "Expected closed stmt1");
                assertTrue(rs1.isClosed(), "Expected closed rs1");
                assertTrue(stmt2.isClosed(), "Expected closed stmt2");
                assertTrue(rs2.isClosed(), "Expected closed rs2");
            }
        }
    }

    @Test
    void setReadOnlyIsPreservedAfterSetTransactionIsolation() throws Exception {
        try (var connection = getConnectionViaDriverManager()) {
            assertFalse(connection.isReadOnly(), "Connection initially not read-only");
            connection.setReadOnly(true);
            assertTrue(connection.isReadOnly(), "Connection read-only after setReadOnly(true)");
            connection.setTransactionIsolation(connection.getTransactionIsolation());
            assertTrue(connection.isReadOnly(), "Connection should still be read-only after setTransactionIsolation");
        }
    }

    @Test
    void setReadOnly_happyPath() throws Exception {
        try (var connection = getConnectionViaDriverManager()) {
            assertFalse(connection.isReadOnly(), "Connection initially not read-only");
            connection.setReadOnly(true);
            assertTrue(connection.isReadOnly(), "Connection read-only after setReadOnly(true)");

            try (var stmt = connection.prepareStatement(INSERT_DATA)) {
                stmt.setInt(1, 5);
                var exception = assertThrows(SQLException.class, stmt::executeUpdate);
                assertThat(exception, errorCodeEquals(ISCConstants.isc_read_only_trans));
            }
        }
    }

    @Test
    void readOnlyShouldInheritFromTransactionConfigurationOfDefaultIsolation() throws Exception {
        try (var connection = getConnectionViaDriverManager(
                "TRANSACTION_READ_COMMITTED", "read_committed,rec_version,read,wait")) {
            assertTrue(connection.isReadOnly(), "Connection initially read-only");

            try (var stmt = connection.prepareStatement(INSERT_DATA)) {
                stmt.setInt(1, 5);
                var exception = assertThrows(SQLException.class, stmt::execute);
                assertThat(exception, errorCodeEquals(ISCConstants.isc_read_only_trans));
            }

            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            try (var stmt = connection.prepareStatement(INSERT_DATA)) {
                stmt.setInt(1, 5);
                var exception = assertThrows(SQLException.class, stmt::execute);
                assertThat(exception, errorCodeEquals(ISCConstants.isc_read_only_trans));
            }
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "ALL", "all" })
    void reportSQLWarnings_ALL_or_default_reportsWarning(String reportSQLWarning) throws Exception {
        Map<String, String> props =
                reportSQLWarning != null ? Map.of(PropertyNames.reportSQLWarnings, reportSQLWarning) : Map.of();
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager(props)) {
            var warning = new SQLWarning("test");
            connection.addWarning(warning);

            assertSame(warning, connection.getWarnings(), "Expected warning to be reported");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "NONE", "none" })
    void reportSQLWarnings_NONE_ignoresWarning(String reportSQLWarning) throws Exception {
        try (FBConnection connection =
                     (FBConnection) getConnectionViaDriverManager(PropertyNames.reportSQLWarnings, reportSQLWarning)) {
            connection.addWarning(new SQLWarning("test"));

            assertNull(connection.getWarnings(), "Expected warning to be ignored");
        }
    }

    /**
     * Single-use executor, delays the command to be executed until signalled.
     */
    @NullMarked
    private static final class DelayingExecutor implements Executor {

        private final CountDownLatch countDownLatch = new CountDownLatch(1);
        private volatile @Nullable Thread thread;

        @Override
        public void execute(Runnable command) {
            Thread thread = this.thread;
            if (thread != null) throw new IllegalStateException("Can only be used once");
            thread = this.thread = new Thread(() -> {
                try {
                    countDownLatch.await();
                    command.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
        }

        void proceed() {
            countDownLatch.countDown();
        }

        /**
         * Signal the executor to complete the action, and then await completion.
         */
        void proceedAndAwait() throws InterruptedException {
            proceed();
            Thread thread = this.thread;
            if (thread != null) thread.join();
        }
    }

}
