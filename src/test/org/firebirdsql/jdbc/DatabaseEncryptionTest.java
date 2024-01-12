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
import org.firebirdsql.common.extension.DatabaseExistsExtension;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.matchers.SQLExceptionMatchers;
import org.firebirdsql.ds.FBConnectionPoolDataSource;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.firebirdsql.ds.FBXADataSource;
import org.firebirdsql.management.FBMaintenanceManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.firebirdsql.management.MaintenanceManager;
import org.firebirdsql.management.StatisticsManager;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests related to database encryption.
 * <p>
 * Tests in this class require the existence of an encrypted database with alias crypttest, and encryption key passed
 * through callback (e.g. IBPhoenix AES128/256 with Callback configuration), and an encryption key value of
 * {@code TestKey}.
 * </p>
 * <p>
 * The encrypted security db tests require an encrypted database with alias cryptsec that also serves as its own
 * security database, with a sysdba password of {@code alt-masterkey}. Like crypttest, its encryption key passed through
 * callback (eg IBPhoenix AES128/256 with Callback configuration), and an encryption key value of {@code TestKey}.
 * </p>
 *
 * @author Mark Rotteveel
 */
class DatabaseEncryptionTest {

    private static final String CRYPTTEST_DB = "crypttest";
    private static final String CRYPTSEC_DB = "cryptsec";
    private static final String CRYPTSEC_SYSDBA_PW = "alt-masterkey";
    private static final String ENCRYPTION_KEY = "TestKey";
    private static final String BASE64_ENCRYPTION_KEY = "VGVzdEtleQ==";

    @RegisterExtension
    @Order(1)
    static final GdsTypeExtension gdsType = GdsTypeExtension.excludesNativeOnly();

    @RegisterExtension
    @Order(2)
    static final RequireProtocolExtension requireProtocol = RequireProtocolExtension.requireProtocolVersion(13);

    @RegisterExtension
    static final DatabaseExistsExtension databaseExists = DatabaseExistsExtension.requireExistence(CRYPTTEST_DB);

    @Test
    void testEncryptedDatabaseConnection() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB);
        System.out.println(url);
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty("dbCryptConfig", ENCRYPTION_KEY);
        try (Connection connection = DriverManager.getConnection(url, props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testEncryptedDatabaseConnection_base64Value() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB);
        System.out.println(url);
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty("dbCryptConfig", "base64:" + BASE64_ENCRYPTION_KEY);
        try (Connection connection = DriverManager.getConnection(url, props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testEncryptedDatabaseConnection_base64ValueInURL() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB) + "?dbCryptConfig=base64:" + BASE64_ENCRYPTION_KEY;
        System.out.println(url);
        try (Connection connection = DriverManager.getConnection(url, FBTestProperties.DB_USER, FBTestProperties.DB_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testEncryptedDatabaseConnection_base64urlValue() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB);
        System.out.println(url);
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty("dbCryptConfig", "base64url:" + BASE64_ENCRYPTION_KEY);
        try (Connection connection = DriverManager.getConnection(url, props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testEncryptedDatabaseConnection_base64urlValueInURL() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB) + "?dbCryptConfig=base64url:" + BASE64_ENCRYPTION_KEY;
        System.out.println(url);
        try (Connection connection = DriverManager.getConnection(url, FBTestProperties.DB_USER, FBTestProperties.DB_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testFBSimpleDataSource() throws Exception {
        final FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());
        ds.setServerName(null);
        ds.setDatabaseName(getUrlWithoutProtocol(CRYPTTEST_DB));
        ds.setDbCryptConfig("base64:" + BASE64_ENCRYPTION_KEY);
        try (Connection connection = ds.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    void testFBConnectionPoolDataSource() throws Exception {
        final FBConnectionPoolDataSource ds = configureDefaultDbProperties(new FBConnectionPoolDataSource());
        ds.setDatabaseName(CRYPTTEST_DB);
        ds.setDbCryptConfig("base64:" + BASE64_ENCRYPTION_KEY);
        final PooledConnection pooledConnection = ds.getPooledConnection();
        try (Connection connection = pooledConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        } finally {
            pooledConnection.close();
        }
    }

    @Test
    void testFBXADataSource() throws Exception {
        final FBXADataSource ds = configureDefaultDbProperties(new FBXADataSource());
        ds.setDatabaseName(CRYPTTEST_DB);
        ds.setDbCryptConfig("base64:" + BASE64_ENCRYPTION_KEY);
        final XAConnection xaConnection = ds.getXAConnection();
        try (Connection connection = xaConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        } finally {
            xaConnection.close();
        }
    }

    @Test
    void testServiceManagerConnection_gstatException() {
        FBStatisticsManager statManager = configureServiceManager(new FBStatisticsManager(getGdsType()));
        statManager.setDatabase(CRYPTTEST_DB);
        statManager.setDbCryptConfig(ENCRYPTION_KEY);

        // TODO Update ISCConstants with missing error constants
        final int errorCodeGstatEncryptedDb = 336920631;
        SQLException exception = assertThrows(SQLException.class,
                () -> statManager.getDatabaseStatistics(StatisticsManager.SYSTEM_TABLE_STATISTICS));
        assertThat(exception, allOf(
                SQLExceptionMatchers.errorCodeEquals(errorCodeGstatEncryptedDb),
                SQLExceptionMatchers.fbMessageStartsWith(errorCodeGstatEncryptedDb)));
    }

    @Test
    void testDatabaseValidation() throws Exception {
        FBMaintenanceManager maintenanceManager = configureServiceManager(new FBMaintenanceManager(getGdsType()));
        maintenanceManager.setDatabase(CRYPTTEST_DB);
        maintenanceManager.setDbCryptConfig(ENCRYPTION_KEY);

        maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_READ_ONLY);
   }

   @Test
   void testEncryptedSelfSecurityDb() throws Exception {
       assumeTrue(getDefaultSupportInfo().supportsProtocol(15),
               "Protocol version 15 is required for encrypted security database with callback, but not supported");
       String url = FBTestProperties.getUrl(CRYPTSEC_DB);
       Properties props = new Properties();
       props.setProperty("user", "SYSDBA");
       props.setProperty("password", CRYPTSEC_SYSDBA_PW);
       props.setProperty("dbCryptConfig", ENCRYPTION_KEY);
       props.setProperty("lc_ctype", "NONE");
       props.setProperty("enableProtocol", ENABLE_PROTOCOL);
       try (Connection connection = DriverManager.getConnection(url, props);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
           assertTrue(rs.next());
           System.out.println(rs.getObject(1));
       }
   }

    @SuppressWarnings("SameParameterValue")
    private static String getUrlWithoutProtocol(String dbPath) {
        if (isEmbeddedType().matches(FBTestProperties.GDS_TYPE)) {
            return dbPath;
        } else {
            return FBTestProperties.DB_SERVER_URL + "/" + FBTestProperties.DB_SERVER_PORT + ":" + dbPath;
        }
    }
}
