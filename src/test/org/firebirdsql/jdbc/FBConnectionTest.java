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
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.wire.crypt.FBSQLEncryptException;
import org.firebirdsql.jaybird.Version;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
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
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Test cases for FirebirdConnection interface.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBConnectionTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @RegisterExtension
    final DatabaseUserExtension databaseUser = DatabaseUserExtension.withDatabaseUser();

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE test (" +
            "  col1 INTEGER" +
            ")";

    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    //@formatter:on

    /**
     * Test if {@link FirebirdConnection#setTransactionParameters(int, int[])} method works correctly.
     */
    @SuppressWarnings("deprecation")
    @Test
    void testTpbMapping() throws Exception {
        try (Connection conA = getConnectionViaDriverManager()) {
            executeCreateTable(conA, CREATE_TABLE);

            conA.setAutoCommit(false);

            PreparedStatement ps = conA.prepareStatement(INSERT_DATA);
            ps.setInt(1, 1);
            ps.execute();
            ps.close();

            conA.commit();

            try (FirebirdConnection conB = getConnectionViaDriverManager()) {
                conB.setAutoCommit(false);

                /*

                // This is the correct way to set transaction parameters
                // However, we use deprecated methods to check the
                // backward compatibility

                TransactionParameterBuffer tpb = ((FirebirdConnection)conB).createTransactionParameterBuffer();
                tpb.addArgument(TransactionParameterBuffer.READ_COMMITTED);
                tpb.addArgument(TransactionParameterBuffer.REC_VERSION);
                tpb.addArgument(TransactionParameterBuffer.WRITE);
                tpb.addArgument(TransactionParameterBuffer.NOWAIT);

                ((FirebirdConnection)conB).setTransactionParameters(tpb);
                */

                conB.setTransactionParameters(
                        Connection.TRANSACTION_READ_COMMITTED,
                        new int[] {
                                isc_tpb_read_committed,
                                isc_tpb_rec_version,
                                isc_tpb_write,
                                isc_tpb_nowait
                        });
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
                stmt.executeQuery("SELECT * FROM rdb$database");
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
            ResultSet rs = metaData.getTables(null, null, "RDB$DATABASE", null);
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
        String defaultConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.defaultConnectionEncoding has not been specified",
                defaultConnectionEncoding, nullValue());

        try {
            System.setProperty("org.firebirdsql.jdbc.defaultConnectionEncoding", "WIN1252");
            Properties props = getDefaultPropertiesForConnection();
            props.remove("lc_ctype");
            try (Connection con = DriverManager.getConnection(getUrl(), props)) {
                // Previously, a warning was registered, verify that doesn't happen
                assertNull(con.getWarnings(), "Expected no warning for not specifying connection character set");
                IConnectionProperties connectionProperties =
                        con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
                assertEquals("WIN1252", connectionProperties.getEncoding(), "Unexpected connection encoding");
            }
        } finally {
            System.clearProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        }
    }

    /**
     * Test if not explicitly specifying a connection character set does not result in an exception on the connection
     * and sets encoding to NONE when system properties {@code org.firebirdsql.jdbc.defaultConnectionEncoding} and
     * {@code org.firebirdsql.jdbc.requireConnectionEncoding} have not been set.
     */
    @Test
    public void testNoCharacterSetWithoutDefaultConnectionEncodingDefaultsToNONEIfEncodingNotRequired() throws Exception {
        String defaultConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.defaultConnectionEncoding has not been specified",
                defaultConnectionEncoding, nullValue());
        String requireConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.requireConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.requireConnectionEncoding has not been specified",
                requireConnectionEncoding, nullValue());
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
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
        String defaultConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.defaultConnectionEncoding has not been specified",
                defaultConnectionEncoding, nullValue());
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");

        try {
            System.setProperty("org.firebirdsql.jdbc.requireConnectionEncoding", "true");
            SQLException exception = assertThrows(SQLNonTransientConnectionException.class, () -> {
                //noinspection EmptyTryBlock
                try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                    // Using try-with-resources just in case connection is created
                }
            });
            assertThat(exception, message(equalTo(FBManagedConnection.ERROR_NO_CHARSET)));
        } finally {
            System.clearProperty("org.firebirdsql.jdbc.requireConnectionEncoding");
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
    void testClientInfo() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsGetSetContext(), "Test requires GET_CONTEXT/SET_CONTEXT support");
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager()) {
            connection.setClientInfo("TestProperty", "testValue");
            String checkValue = connection.getClientInfo("TestProperty");
            assertEquals("testValue", checkValue);
        }
    }

    @Test
    void testProcessNameThroughConnectionProperty() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        final Properties props = getDefaultPropertiesForConnection();
        final String processName = "Test process name";
        props.setProperty(PropertyNames.processName, processName);

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PROCESS from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
            assertTrue(resultSet.next());
            assertEquals(processName, resultSet.getString(1));
        }
    }

    @Test
    void testProcessIdThroughConnectionProperty() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        assumeThat("Test only works in pure java", GDS_TYPE, isPureJavaType());
        final Properties props = getDefaultPropertiesForConnection();
        final int processId = 5843;
        props.setProperty(PropertyNames.processId, String.valueOf(processId));

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PID from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
            assertTrue(resultSet.next());
            assertEquals(processId, resultSet.getInt(1));
        }
    }

    @Test
    void testProcessAndIdThroughConnectionPropertyTakesPrecedence() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsMonitoringTables(), "Test requires monitoring tables");
        assumeThat("Test only works in pure java", GDS_TYPE, isPureJavaType());
        final Properties props = getDefaultPropertiesForConnection();
        final String processNameThroughConnection = "Process name in connection property";
        props.setProperty(PropertyNames.processName, processNameThroughConnection);
        final int processIdThroughConnection = 513;
        props.setProperty(PropertyNames.processId, String.valueOf(processIdThroughConnection));
        final String processNameThroughSystemProp = "Process name in system property";
        final int processIdThroughSystemProp = 132;
        System.setProperty(JaybirdSystemProperties.PROCESS_NAME_PROP, processNameThroughSystemProp);
        System.setProperty(JaybirdSystemProperties.PROCESS_ID_PROP, String.valueOf(processIdThroughSystemProp));

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PROCESS, MON$REMOTE_PID from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
            assertTrue(resultSet.next());
            assertEquals(processNameThroughConnection, resultSet.getString(1));
            assertEquals(processIdThroughConnection, resultSet.getInt(2));
        } finally {
            System.clearProperty("org.firebirdsql.jdbc.processName");
            System.clearProperty("org.firebirdsql.jdbc.pid");
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

        SQLException exception = assertThrows(FBSQLEncryptException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
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

        SQLException exception = assertThrows(FBSQLEncryptException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
        // TODO Check if we can make behavior consistent between native and pure java
        assertThat(exception, anyOf(
                errorCodeEquals(ISCConstants.isc_wirecrypt_incompatible),
                errorCodeEquals(ISCConstants.isc_miss_wirecrypt)));
    }

    @Test
    void invalidValueForWireCrypt() {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", "NOT_A_VALID_VALUE");

        SQLException exception = assertThrows(SQLException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionPropertyValue),
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidConnectionPropertyValue, "NOT_A_VALID_VALUE", "wireCrypt")));
    }

    @Test
    void expectedWireCryptPluginApplied() throws Exception {
        assumeThat("Test doesn't work with embedded", GDS_TYPE, not(isEmbeddedType()));
        assumeTrue(getDefaultSupportInfo().supportsWireEncryption(),
                "Test for Firebird versions with wire encryption support");
        assumeTrue(getDefaultSupportInfo().isVersionEqualOrAbove(4, 0), "Requires fb_info_wire_crypt support");
        String expectedCryptPlugin = System.getProperty("java.specification.version").equals("1.8")
                || Version.JAYBIRD_DISPLAY_VERSION.endsWith(".java8") ? "Arc4" : "ChaCha";
        if (isOtherNativeType().matches(GDS_TYPE) && getDefaultSupportInfo().isVersionEqualOrAbove(4, 0, 1)) {
            expectedCryptPlugin = "ChaCha64";
        }

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
                    throw new FbExceptionBuilder().exception(JaybirdErrorCodes.jb_unexpectedInfoResponse)
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

        SQLException exception = assertThrows(SQLNonTransientConnectionException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
        assertThat(exception, message(equalTo(
                "No valid encoding definition for Firebird encoding DOES_NOT_EXIST and/or Java charset null")));
    }

    @Test
    void connectingWithUnknownJavaCharacterSetName() {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        props.setProperty("charSet", "DOES_NOT_EXIST");

        SQLException exception = assertThrows(SQLNonTransientConnectionException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
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
        SQLException exception = assertThrows(SQLInvalidAuthorizationSpecException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignored = DriverManager.getConnection(getUrl(), props)) {
                // Using try-with-resources just in case connection is created
            }
        });
        assertThat(exception, errorCodeEquals(ISCConstants.isc_login));
    }

    @Test
    @Timeout(10)
    void setNetworkTimeout_isUsed() throws Exception {
        assumeThat("Test assumes pure Java implementation (native doesn't support setNetworkTimeout)",
                GDS_TYPE, isPureJavaType());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Workaround for bug with TPBMapper being shared (has conflict with testTpbMapping)
        Properties props = getDefaultPropertiesForConnection();
        props.put("TRANSACTION_READ_COMMITTED", "read_committed,rec_version,write,wait");
        try (Connection connection1 = getConnectionViaDriverManager();
             Statement statement1 = connection1.createStatement();
             Connection connection2 = DriverManager.getConnection(getUrl(), props)) {
            executeCreateTable(connection1, "create table locking (id integer, colval varchar(50))");
            statement1.execute("insert into locking(id, colval) values (1, 'abc')");

            try (ResultSet rs1 = statement1.executeQuery("select id, colval from locking with lock")) {
                rs1.next();

                connection2.setNetworkTimeout(executor, 50);
                long start = 0;
                try (Statement statement2 = connection2.createStatement();
                     ResultSet rs2 = statement2.executeQuery("select id, colval from locking with lock")) {
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
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void setNetworkTimeout_invalidTimeout() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try (Connection connection = getConnectionViaDriverManager()) {
            SQLException exception = assertThrows(SQLException.class,
                    () -> connection.setNetworkTimeout(executorService, -1));
            assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_invalidTimeout));
        } finally {
            executorService.shutdown();
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
    void setNetworkTimeout_getAndSetSeries() throws Exception {
        assumeThat("Type is pure Java", GDS_TYPE, isPureJavaType());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try (Connection connection = getConnectionViaDriverManager()) {
            assertEquals(0, connection.getNetworkTimeout(), "Expected 0 as initial network timeout");

            connection.setNetworkTimeout(executorService, 500);
            final CyclicBarrier barrier = new CyclicBarrier(2);
            Runnable waitForBarrier = () -> {
                try {
                    barrier.await(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            };
            executorService.execute(waitForBarrier);
            barrier.await(500, TimeUnit.MILLISECONDS);
            assertEquals(500, connection.getNetworkTimeout(), "Unexpected getNetworkTimeout");

            barrier.reset();
            connection.setNetworkTimeout(executorService, 0);
            executorService.execute(waitForBarrier);
            barrier.await(500, TimeUnit.MILLISECONDS);
            assertEquals(0, connection.getNetworkTimeout(), "Unexpected getNetworkTimeout");
        } finally {
            executorService.shutdown();
        }
    }

    @Test
    void testWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().supportsWireCompression(), "Test requires wire compression");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCompression", "true");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
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

            TransactionParameterBufferImpl newParameters = new TransactionParameterBufferImpl();
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
        try (Connection ignored = DriverManager.getConnection(getUrl())) {
            fail("expected exception when connecting without user name and password");
        } catch (SQLException e) {
            assertThat(e, allOf(
                    errorCodeEquals(ISCConstants.isc_login),
                    fbMessageStartsWith(ISCConstants.isc_login)));
        }
    }

}
