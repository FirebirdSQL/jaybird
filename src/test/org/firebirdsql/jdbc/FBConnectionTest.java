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
import org.firebirdsql.common.rules.DatabaseUserRule;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.wire.crypt.FBSQLEncryptException;
import org.firebirdsql.jca.FBManagedConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 * Test cases for FirebirdConnection interface.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBConnectionTest {

    private final ExpectedException expectedException = ExpectedException.none();
    private final DatabaseUserRule databaseUserRule = DatabaseUserRule.withDatabaseUser();
    private final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @Rule
    public final TestRule ruleChain = RuleChain
            .outerRule(usesDatabase)
            .around(databaseUserRule)
            .around(expectedException);

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE test (" +
            "  col1 INTEGER" +
            ")";

    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    //@formatter:on

    /**
     * Test if {@link FirebirdConnection#setTransactionParameters(int, int[])}
     * method works correctly.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTpbMapping() throws Exception {
        try (Connection conA = getConnectionViaDriverManager()) {
            executeCreateTable(conA, CREATE_TABLE);

            conA.setAutoCommit(false);

            PreparedStatement ps = conA.prepareStatement(INSERT_DATA);
            ps.setInt(1, 1);
            ps.execute();
            ps.close();

            conA.commit();

            try (Connection conB = getConnectionViaDriverManager()) {
                conB.setAutoCommit(false);

                /*

                // This is correct way to set transaction parameters
                // However, we use deprecated methods to check the
                // backward compatibility

                TransactionParameterBuffer tpb = ((FirebirdConnection)conB).createTransactionParameterBuffer();
                tpb.addArgument(TransactionParameterBuffer.READ_COMMITTED);
                tpb.addArgument(TransactionParameterBuffer.REC_VERSION);
                tpb.addArgument(TransactionParameterBuffer.WRITE);
                tpb.addArgument(TransactionParameterBuffer.NOWAIT);

                ((FirebirdConnection)conB).setTransactionParameters(tpb);
                */

                ((FirebirdConnection) conB).setTransactionParameters(
                        Connection.TRANSACTION_READ_COMMITTED,
                        new int[] {
                                FirebirdConnection.TPB_READ_COMMITTED,
                                FirebirdConnection.TPB_REC_VERSION,
                                FirebirdConnection.TPB_WRITE,
                                FirebirdConnection.TPB_NOWAIT
                        });
                conB.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                try (Statement stmtA = conA.createStatement();
                     Statement stmtB = conB.createStatement()) {

                    stmtA.execute("UPDATE test SET col1 = 2");

                    expectedException.expect(SQLException.class);
                    expectedException.reportMissingExceptionWithMessage("Should notify about a deadlock.");

                    stmtB.execute("UPDATE test SET col1 = 3");
                }
            }
        }
    }

    @Test
    public void testStatementCompletion() throws Exception {
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
    public void testExecuteStatementTwice() throws Exception {
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
                for (int i = 0; i < 10 && rs.next(); i++)
                    ;   // do something
                rs.close();

                // on the following 2nd call the exception gets thrown
                rs = stmt.executeQuery(select1);  // throws Exception on the 2nd call
                rs.close();

                pstmt.setString(1, "ABC");
                rs = pstmt.executeQuery();
                for (int i = 0; i < 10 && rs.next(); i++)
                    ;   // do something
                rs.close();
            } finally {
                connection.commit();
            }
        }
    }

    @Test
    public void testLockTable() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, "CREATE TABLE test_lock(col1 INTEGER)");
        }

        try (FirebirdConnection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {

            TransactionParameterBuffer tpb = connection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            if (tpb.hasArgument(TransactionParameterBuffer.WAIT)) {
                tpb.removeArgument(TransactionParameterBuffer.WAIT);
                tpb.addArgument(TransactionParameterBuffer.NOWAIT);
            }

            connection.setTransactionParameters(Connection.TRANSACTION_READ_COMMITTED, tpb);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            connection.setAutoCommit(false);

            try (FirebirdConnection anotherConnection = getConnectionViaDriverManager()) {
                anotherConnection.setAutoCommit(false);
                TransactionParameterBuffer anotherTpb = anotherConnection.createTransactionParameterBuffer();

                anotherTpb.addArgument(TransactionParameterBuffer.CONSISTENCY);
                anotherTpb.addArgument(TransactionParameterBuffer.WRITE);
                anotherTpb.addArgument(TransactionParameterBuffer.NOWAIT);

                anotherTpb.addArgument(TransactionParameterBuffer.LOCK_WRITE, "TEST_LOCK");
                anotherTpb.addArgument(TransactionParameterBuffer.PROTECTED);

                anotherConnection.setTransactionParameters(anotherTpb);

                try (Statement anotherStmt = anotherConnection.createStatement()) {
                    anotherStmt.execute("INSERT INTO test_lock VALUES(1)");
                }

                expectedException.expect(errorCodeEquals(ISCConstants.isc_lock_conflict));
                expectedException.reportMissingExceptionWithMessage("Should throw an error because of lock conflict.");

                stmt.execute("INSERT INTO test_lock VALUES(2)");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Test
    public void testMetaDataTransaction() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "RDB$DATABASE", null);
            rs.close();
        }
    }

    @Test
    public void testTransactionCoordinatorAutoCommitChange() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM rdb$database")) {
                connection.setAutoCommit(false);
            }
            // TODO This doesn't seem to test anything
        }
    }

    @Test
    public void testDefaultHoldableResultSet() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("defaultHoldable", "");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            Statement stmt1 = connection.createStatement();
            assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, stmt1.getResultSetHoldability());

            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery("SELECT rdb$collation_name, rdb$character_set_id FROM rdb$collations");
            while (rs1.next()) {
                ResultSet rs2 = stmt2.executeQuery("SELECT rdb$character_set_name FROM rdb$character_sets " +
                        "WHERE rdb$character_set_id = " + rs1.getInt(2));

                assertTrue("Should find corresponding charset.", rs2.next());
            }
        }
    }

    @Test
    public void testGetAttachments() throws Exception {
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager()) {
            FbDatabase database = connection.getFbDatabase();

            byte[] infoRequest = new byte[] { ISCConstants.isc_info_user_names, ISCConstants.isc_info_end };
            byte[] reply = database.getDatabaseInfo(infoRequest, 1024);

            int i = 0;

            while (reply[i] != ISCConstants.isc_info_end) {
                switch (reply[i++]) {
                case ISCConstants.isc_info_user_names:
                    //iscVaxInteger2(reply, i); // can be ignored
                    i += 2;
                    int strLen = reply[i] & 0xff;
                    i += 1;
                    String userName = new String(reply, i, strLen);
                    i += strLen;
                    System.out.println(userName);
                    break;
                default:
                    break;
                }
            }
        }
    }

    @Test
    public void testWireProtocolCompatibility() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT max(rdb$format) FROM rdb$formats");
            assertTrue("Should fetch some rows.", rs.next());
        }
    }

    /**
     * Test if not explicitly specifying a connection character set results in a warning on the connection when
     * system property {@code org.firebirdsql.jdbc.defaultConnectionEncoding} has been set.
     *
     * @see #testNoCharacterSetExceptionWithoutDefaultConnectionEncoding()
     */
    @Test
    public void testNoCharacterSetWarningWithDefaultConnectionEncoding() throws Exception {
        String defaultConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.defaultConnectionEncoding has not been specified",
                defaultConnectionEncoding, nullValue());

        try {
            System.setProperty("org.firebirdsql.jdbc.defaultConnectionEncoding", "WIN1252");
            Properties props = getDefaultPropertiesForConnection();
            props.remove("lc_ctype");
            try (Connection con = DriverManager.getConnection(getUrl(), props)) {
                SQLWarning warnings = con.getWarnings();
                assertNotNull("Expected a warning for not specifying connection character set", warnings);
                assertEquals("Unexpected warning message for not specifying connection character set",
                        FBManagedConnection.WARNING_NO_CHARSET + "WIN1252", warnings.getMessage());
                IConnectionProperties connectionProperties =
                        con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
                assertEquals("Unexpected connection encoding", "WIN1252", connectionProperties.getEncoding());
            }
        } finally {
            System.clearProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        }
    }

    /**
     * Test if not explicitly specifying a connection character set results in an exception on the connection when
     * system property {@code org.firebirdsql.jdbc.defaultConnectionEncoding} has not been set.
     *
     * @see #testNoCharacterSetWarningWithDefaultConnectionEncoding()
     */
    @Test
    public void testNoCharacterSetExceptionWithoutDefaultConnectionEncoding() throws Exception {
        String defaultConnectionEncoding = System.getProperty("org.firebirdsql.jdbc.defaultConnectionEncoding");
        assumeThat("Test only works if org.firebirdsql.jdbc.defaultConnectionEncoding has not been specified",
                defaultConnectionEncoding, nullValue());
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        expectedException.expect(SQLNonTransientConnectionException.class);
        expectedException.expectMessage(FBManagedConnection.ERROR_NO_CHARSET);

        DriverManager.getConnection(getUrl(), props);
    }

    /**
     * Test if explicitly specifying a connection character set does not add a warning (or exception) on the connection.
     */
    @Test
    public void testCharacterSetFirebirdNoWarning() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "WIN1252");

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
            SQLWarning warnings = con.getWarnings();
            assertNull("Expected no warning when specifying connection character set", warnings);
            IConnectionProperties connectionProperties =
                    con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
            assertEquals("Unexpected connection encoding", "WIN1252", connectionProperties.getEncoding());
        }
    }

    /**
     * Test if explicitly specifying a connection character set does not add a warning (or exception) on the connection.
     */
    @Test
    public void testCharacterSetJavaNoWarning() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.remove("lc_ctype");
        props.setProperty("charSet", "Cp1254");

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
            SQLWarning warnings = con.getWarnings();
            assertNull("Expected no warning when specifying connection character set", warnings);
            IConnectionProperties connectionProperties =
                    con.unwrap(FirebirdConnection.class).getFbDatabase().getConnectionProperties();
            assertEquals("Unexpected connection encoding", "WIN1254", connectionProperties.getEncoding());
        }
    }

    @Test
    public void testClientInfo() throws Exception {
        try (FBConnection connection = (FBConnection) getConnectionViaDriverManager()) {
            assumeTrue("This test requires GET_CONTEXT/SET_CONTEXT support",
                    supportInfoFor(connection).supportsGetSetContext());

            connection.setClientInfo("TestProperty", "testValue");
            String checkValue = connection.getClientInfo("TestProperty");
            assertEquals("testValue", checkValue);
        }
    }

    @Test
    public void testProcessNameThroughConnectionProperty() throws Exception {
        assumeTrue("Test requires monitoring tables", getDefaultSupportInfo().supportsMonitoringTables());
        final Properties props = getDefaultPropertiesForConnection();
        final String processName = "Test process name";
        props.setProperty("processName", processName);

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PROCESS from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")){
            assertTrue(resultSet.next());
            assertEquals(processName, resultSet.getString(1));
        }
    }

    @Test
    public void testProcessIdThroughConnectionProperty() throws Exception {
        assumeTrue("Test requires monitoring tables", getDefaultSupportInfo().supportsMonitoringTables());
        assumeTrue("Test only works in pure java", Arrays.asList(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME, OOGDSFactoryPlugin.TYPE_NAME).contains(FBTestProperties.GDS_TYPE));
        final Properties props = getDefaultPropertiesForConnection();
        final int processId = 5843;
        props.setProperty("processId", String.valueOf(processId));

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PID from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")){
            assertTrue(resultSet.next());
            assertEquals(processId, resultSet.getInt(1));
        }
    }

    @Test
    public void testProcessAndIdThroughConnectionPropertyTakesPrecedence() throws Exception {
        assumeTrue("Test requires monitoring tables", getDefaultSupportInfo().supportsMonitoringTables());
        assumeTrue("Test only works in pure java", Arrays.asList(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME, OOGDSFactoryPlugin.TYPE_NAME).contains(FBTestProperties.GDS_TYPE));
        final Properties props = getDefaultPropertiesForConnection();
        final String processNameThroughConnection = "Process name in connection property";
        props.setProperty("processName", processNameThroughConnection);
        final int processIdThroughConnection = 513;
        props.setProperty("processId", String.valueOf(processIdThroughConnection));
        final String processNameThroughSystemProp = "Process name in system property";
        final int processIdThroughSystemProp = 132;
        System.setProperty("org.firebirdsql.jdbc.processName", processNameThroughSystemProp);
        System.setProperty("org.firebirdsql.jdbc.pid", String.valueOf(processIdThroughSystemProp));

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select MON$REMOTE_PROCESS, MON$REMOTE_PID from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")){
            assertTrue(resultSet.next());
            assertEquals(processNameThroughConnection, resultSet.getString(1));
            assertEquals(processIdThroughConnection, resultSet.getInt(2));
        } finally {
            System.clearProperty("org.firebirdsql.jdbc.processName");
            System.clearProperty("org.firebirdsql.jdbc.pid");
        }
    }

    @Test
    public void testConnectionsViaDriverManagerAreDistinct() throws Exception {
        try (Connection connection1 = getConnectionViaDriverManager();
             Connection connection2 = getConnectionViaDriverManager()) {
             assertNotSame(connection1, connection2);

            connection1.close();
            assertTrue(connection1.isClosed());
            assertFalse(connection2.isClosed());
        }
    }

    @Test
    public void testIPv6AddressHandling() throws Exception {
        assumeThat("Test only works for pure java", FBTestProperties.GDS_TYPE,
                isIn(Arrays.asList(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME, OOGDSFactoryPlugin.TYPE_NAME)));
        assumeTrue("Firebird 3 or higher required for IPv6 testing", getDefaultSupportInfo().isVersionEqualOrAbove(3, 0));
        try (Connection connection = DriverManager.getConnection("jdbc:firebirdsql://[::1]/" + getDatabasePath() + "?charSet=utf-8", DB_USER, DB_PASSWORD)) {
            assertTrue(connection.isValid(0));
        }
    }

    @Test
    public void testIPv6AddressHandling_native() throws Exception {
        assumeTrue("Firebird 3 or higher required for IPv6 testing", getDefaultSupportInfo().isVersionEqualOrAbove(3, 0));
        assumeTrue("Test only works for native", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME.equals(FBTestProperties.GDS_TYPE));
        try (Connection connection = DriverManager.getConnection("jdbc:firebirdsql:native://[::1]/" + getDatabasePath() + "?charSet=utf-8", DB_USER, DB_PASSWORD)) {
            assertTrue(connection.isValid(0));
        }
    }

    @Test
    public void testWireCrypt_DISABLED_FB2_5_and_earlier() throws Exception {
        testWireCrypt_FB2_5_and_earlier(WireCrypt.DISABLED);
    }

    @Test
    public void testWireCrypt_ENABLED_FB2_5_and_earlier() throws Exception {
        testWireCrypt_FB2_5_and_earlier(WireCrypt.ENABLED);
    }

    @Test
    public void testWireCrypt_DEFAULT_FB2_5_and_earlier() throws Exception {
        testWireCrypt_FB2_5_and_earlier(WireCrypt.DEFAULT);
    }

    @Test
    public void testWireCrypt_REQUIRED_FB2_5_and_earlier() throws Exception {
        testWireCrypt_FB2_5_and_earlier(WireCrypt.REQUIRED);
    }

    private void testWireCrypt_FB2_5_and_earlier(WireCrypt wireCrypt) throws Exception {
        assumeFalse("Test for Firebird versions without wire encryption support",
                getDefaultSupportInfo().supportsWireEncryption());
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", wireCrypt.name());
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertTrue(connection.isValid(0));
        }
    }

    @Test
    public void testWireCrypt_DISABLED_FB3_0_and_later() throws Exception {
        testWireCrypt_FB3_0_and_later(WireCrypt.DISABLED);
    }

    @Test
    public void testWireCrypt_ENABLED_FB3_0_and_later() throws Exception {
        testWireCrypt_FB3_0_and_later(WireCrypt.ENABLED);
    }

    @Test
    public void testWireCrypt_DEFAULT_FB3_0_and_later() throws Exception {
        testWireCrypt_FB3_0_and_later(WireCrypt.DEFAULT);
    }

    @Test
    public void testWireCrypt_REQUIRED_FB3_0_and_later() throws Exception {
        testWireCrypt_FB3_0_and_later(WireCrypt.REQUIRED);
    }

    private void testWireCrypt_FB3_0_and_later(WireCrypt wireCrypt) throws Exception {
        assumeTrue("Test for Firebird versions with wire encryption support",
                getDefaultSupportInfo().supportsWireEncryption());
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
                assertTrue("Expected wire encryption to be used for wireCrypt=" + wireCrypt, encryptionUsed);
                break;
            case DISABLED:
                assertFalse("Expected wire encryption not to be used for wireCrypt=" + wireCrypt, encryptionUsed);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ISCConstants.isc_wirecrypt_incompatible) {
                System.err.println("WARNING: wire encryption level " + wireCrypt + " requested, but rejected by server."
                        + " Consider re-running the test with a WireCrypt=Enabled in firebird.conf");
            }
        }
    }

    @Test
    public void legacyAuthUserWithWireCrypt_ENABLED_canCreateConnection() throws Exception {
        assumeTrue("Test for Firebird versions with wire encryption support",
                getDefaultSupportInfo().supportsWireEncryption());
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUserRule.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("wireCrypt", "ENABLED");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertFalse("Expected wire encryption not to be used when connecting with legacy auth user",
                    serverVersion.isWireEncryptionUsed());
        }
    }

    @Test
    public void legacyAuthUserWithWireCrypt_REQUIRED_hasConnectionRejected() throws Exception {
        assumeTrue("Test for Firebird versions with wire encryption support",
                getDefaultSupportInfo().supportsWireEncryption());
        final String user = "legacy_auth";
        final String password = "leg_auth";
        databaseUserRule.createUser(user, password, "Legacy_UserManager");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("wireCrypt", "REQUIRED");

        expectedException.expect(FBSQLEncryptException.class);
        expectedException.expect(errorCodeEquals(ISCConstants.isc_wirecrypt_incompatible));

        DriverManager.getConnection(getUrl(), props);
    }

    @Test
    public void invalidValueForWireCrypt() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("wireCrypt", "NOT_A_VALID_VALUE");

        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionPropertyValue),
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidConnectionPropertyValue, "NOT_A_VALID_VALUE", "wireCrypt")));

        DriverManager.getConnection(getUrl(), props);
    }
}
