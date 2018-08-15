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
import org.firebirdsql.common.matchers.SQLExceptionMatchers;
import org.firebirdsql.common.rules.DatabaseExistsRule;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.ds.FBConnectionPoolDataSource;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.firebirdsql.ds.FBXADataSource;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.FBMaintenanceManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.firebirdsql.management.MaintenanceManager;
import org.firebirdsql.management.StatisticsManager;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to database encryption.
 * <p>
 * Tests in this class require the existence of an encrypted database with alias crypttest, and encryption key passed
 * through callback (eg IBPhoenix AES128/256 with Callback configuration), and an encryption key value of
 * {@code TestKey}.
 * </p>
 * <p>
 * The ignored service tests require a global {@code KeyHolderPlugin} configuration instead of a database-specific
 * configuration in {@code databases.conf}.
 * </p>
 * <p>
 * The ignored encrypted security db tests require an encrypted database with alias cryptsec that also serves as its own
 * security database, with a sysdba password of {@code alt-masterkey}. Like crypttest, its encryption key passed through
 * callback (eg IBPhoenix AES128/256 with Callback configuration), and an encryption key value of {@code TestKey}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DatabaseEncryptionTest {

    private static final String CRYPTTEST_DB = "crypttest";
    private static final String CRYPTSEC_DB = "cryptsec";
    private static final String CRYPTSEC_SYSDBA_PW = "alt-masterkey";
    private static final String ENCRYPTION_KEY = "TestKey";
    private static final String BASE64_ENCRYPTION_KEY = "VGVzdEtleQ==";

    @ClassRule
    public static final TestRule requiresCryptTestDB = RuleChain
            .outerRule(GdsTypeRule.excludesNativeOnly())
            .around(RequireProtocol.requireProtocolVersion(13))
            .around(DatabaseExistsRule.requireExistence(CRYPTTEST_DB));

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEncryptedDatabaseConnection() throws Exception {
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
    public void testEncryptedDatabaseConnection_base64Value() throws Exception {
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
    public void testEncryptedDatabaseConnection_base64ValueInURL() throws Exception {
        String url = FBTestProperties.getUrl(CRYPTTEST_DB) + "?dbCryptConfig=" + "base64:" + BASE64_ENCRYPTION_KEY;
        System.out.println(url);
        try (Connection connection = DriverManager.getConnection(url, FBTestProperties.DB_USER, FBTestProperties.DB_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    public void testFBSimpleDataSource() throws Exception {
        final FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabase(getUrlWithoutProtocol(CRYPTTEST_DB));
        ds.setUserName(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.GDS_TYPE);
        ds.setDbCryptConfig("base64:" + BASE64_ENCRYPTION_KEY);
        try (Connection connection = ds.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
            assertTrue(rs.next());
            System.out.println(rs.getObject(1));
        }
    }

    @Test
    public void testFBConnectionPoolDataSource() throws Exception {
        final FBConnectionPoolDataSource ds = new FBConnectionPoolDataSource();
        ds.setDatabaseName(CRYPTTEST_DB);
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            ds.setServerName(FBTestProperties.DB_SERVER_URL);
            ds.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        }
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.GDS_TYPE);
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
    public void testFBXADataSource() throws Exception {
        final FBXADataSource ds = new FBXADataSource();
        ds.setDatabaseName(CRYPTTEST_DB);
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            ds.setServerName(FBTestProperties.DB_SERVER_URL);
            ds.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        }
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.GDS_TYPE);
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
    @Ignore("Requires global KeyHolderPlugin configuration")
    public void testServiceManagerConnection_gstatException() throws Exception {
        FBStatisticsManager statManager = new FBStatisticsManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            statManager.setHost(DB_SERVER_URL);
            statManager.setPort(DB_SERVER_PORT);
        }
        statManager.setUser(DB_USER);
        statManager.setPassword(DB_PASSWORD);
        statManager.setDatabase(CRYPTTEST_DB);
        statManager.setDbCryptConfig(ENCRYPTION_KEY);

        expectedException.expect(SQLException.class);
        // TODO Update ISCConstants with missing error constants
        final int errorCodeGstatEncryptedDb = 336920631;
        expectedException.expect(allOf(
                SQLExceptionMatchers.errorCodeEquals(errorCodeGstatEncryptedDb),
                SQLExceptionMatchers.fbMessageStartsWith(errorCodeGstatEncryptedDb)));
        statManager.getDatabaseStatistics(StatisticsManager.SYSTEM_TABLE_STATISTICS);
    }

    @Test
    @Ignore("Requires global KeyHolderPlugin configuration")
    public void testDatabaseValidation() throws Exception {
        FBMaintenanceManager maintenanceManager = new FBMaintenanceManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            maintenanceManager.setHost(DB_SERVER_URL);
            maintenanceManager.setPort(DB_SERVER_PORT);
        }
        maintenanceManager.setUser(DB_USER);
        maintenanceManager.setPassword(DB_PASSWORD);
        maintenanceManager.setDatabase(CRYPTTEST_DB);
        maintenanceManager.setDbCryptConfig(ENCRYPTION_KEY);

        maintenanceManager.validateDatabase(MaintenanceManager.VALIDATE_READ_ONLY);
   }

   @Test
   @Ignore("Requires encrypted self-security db + protocol v15")
   public void testEncryptedSelfSecurityDb() throws Exception {
       String url = FBTestProperties.getUrl(CRYPTSEC_DB);
       System.out.println(url);
       Properties props = new Properties();
       props.setProperty("user", "SYSDBA");
       props.setProperty("password", CRYPTSEC_SYSDBA_PW);
       props.setProperty("dbCryptConfig", ENCRYPTION_KEY);
       props.setProperty("lc_ctype", "NONE");
       try (Connection connection = DriverManager.getConnection(url, props);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from rdb$database")) {
           assertTrue(rs.next());
           System.out.println(rs.getObject(1));
       }
   }

    private static String getUrlWithoutProtocol(String dbPath) {
        final String gdsType = FBTestProperties.GDS_TYPE;
        if ("EMBEDDED".equalsIgnoreCase(gdsType) || "LOCAL".equalsIgnoreCase(gdsType)) {
            return dbPath;
        } else {
            return FBTestProperties.DB_SERVER_URL + "/" + FBTestProperties.DB_SERVER_PORT + ":" + dbPath;
        }
    }
}
