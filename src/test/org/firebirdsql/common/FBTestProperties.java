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
package org.firebirdsql.common;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBServiceManager;
import org.firebirdsql.management.ServiceManager;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Helper class for test properties (database user, password, paths etc)
 */
public final class FBTestProperties {

    private static FirebirdSupportInfo firebirdSupportInfo;
    
    static {
        // Needed for supporting tests that don't reference DriverManager
        try {
            Class.forName(FBDriver.class.getName());
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("No suitable driver.");
        }
    }
    
    private static final ResourceBundle testDefaults = ResourceBundle.getBundle("unit_test_defaults");

    public static String getProperty(String property) {
        return getProperty(property, null);
    }

    public static String getProperty(String property, String defaultValue) {
        try {
            return System.getProperty(property, testDefaults.getString(property));
        } catch (MissingResourceException ex) {
            return System.getProperty(property, defaultValue);
        }
    }

    /**
     * Default name of database file to use for the test case.
     */
    public static final String DB_NAME = "fbtest.fdb";
    public static final String DB_USER = getProperty("test.user", "sysdba");
    public static final String DB_PASSWORD = getProperty("test.password", "masterkey");
    public static final String DB_PATH = getProperty("test.db.dir", "");
    public static final String DB_SERVER_URL = getProperty("test.db.host", "localhost");
    public static final int DB_SERVER_PORT = Integer.parseInt(getProperty("test.db.port", "3050"));
    public static final String DB_LC_CTYPE = getProperty("test.db.lc_ctype", "NONE");
    public static final boolean DB_ON_DOCKER = Boolean.parseBoolean(getProperty("test.db_on_docker", "false"));
    public static final String DB_DATASOURCE_URL = getdbpath(DB_NAME);
    public static final String GDS_TYPE = getProperty("test.gds_type", "PURE_JAVA");
    public static final boolean USE_FIREBIRD_AUTOCOMMIT =
            Boolean.parseBoolean(getProperty("test.use_firebird_autocommit", "false"));

    public static String getDatabasePath() {
        return getDatabasePath(DB_NAME);
    }

    public static String getDatabasePath(String name) {
        if (!("127.0.0.1".equals(DB_SERVER_URL) || "localhost".equals(DB_SERVER_URL)) || DB_ON_DOCKER)
            return DB_PATH + "/" + name;
        else
            return new File(DB_PATH, name).getAbsolutePath();
    }

    /**
     * Builds a firebird database connection string for the supplied database
     * file.
     * 
     * @param name Database name
     * @return URL or path for the gds type.
     */
    public static String getdbpath(String name) {
        final String gdsType = getProperty("test.gds_type", null);
        if ("EMBEDDED".equalsIgnoreCase(gdsType)) {
            return new File(DB_PATH, name).getAbsolutePath();
        } else {
            return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + getDatabasePath(name);
        }
    }

    /**
     * @return Default database connection properties for this testrun
     */
    public static Properties getDefaultPropertiesForConnection() {
        final Properties returnValue = new Properties();

        returnValue.setProperty("user", DB_USER);
        returnValue.setProperty("password", DB_PASSWORD);
        returnValue.setProperty("lc_ctype", DB_LC_CTYPE);
        if (USE_FIREBIRD_AUTOCOMMIT) {
            returnValue.setProperty("useFirebirdAutocommit", "true");
        }

        return returnValue;
    }

    public static FbConnectionProperties getDefaultFbConnectionProperties() {
        FbConnectionProperties connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding(DB_LC_CTYPE);
        return connectionInfo;
    }

    public static FbServiceProperties getDefaultServiceProperties() {
        FbServiceProperties connectionInfo = new FbServiceProperties();
        connectionInfo.setServerName(DB_SERVER_URL);
        connectionInfo.setPortNumber(DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        return connectionInfo;
    }

    /**
     * Creates a default TPB (read_committed, rec_version, write, wait).
     *
     * @return TPB
     */
    public static TransactionParameterBuffer getDefaultTpb() {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(TpbItems.isc_tpb_read_committed);
        tpb.addArgument(TpbItems.isc_tpb_rec_version);
        tpb.addArgument(TpbItems.isc_tpb_write);
        tpb.addArgument(TpbItems.isc_tpb_wait);
        return tpb;
    }

    /**
     * Configures the host, port, user and password of a service manager using the default properties.
     *
     * @param serviceManager Service manager to configure
     */
    public static <T extends ServiceManager> T configureServiceManager(T serviceManager) {
        serviceManager.setServerName(DB_SERVER_URL);
        serviceManager.setPortNumber(DB_SERVER_PORT);
        serviceManager.setUser(DB_USER);
        serviceManager.setPassword(DB_PASSWORD);
        return serviceManager;
    }

    /**
     * @return {@link GDSType} for this testrun
     */
    public static GDSType getGdsType() {
        final GDSType gdsType = GDSType.getType(GDS_TYPE);
        if (gdsType == null) {
            throw new RuntimeException("Unrecognized value for 'test.gds_type' property.");
        }
        return gdsType;
    }

    /**
     * The {@link org.firebirdsql.util.FirebirdSupportInfo} for the default test server.
     *
     * @return Support info object
     */
    public static FirebirdSupportInfo getDefaultSupportInfo() {
        try {
            if (firebirdSupportInfo == null) {
                final GDSType gdsType = getGdsType();
                final FBServiceManager fbServiceManager = new FBServiceManager(gdsType);
                if (gdsType == GDSType.getType("PURE_JAVA")
                        || gdsType == GDSType.getType("NATIVE")
                        || gdsType == GDSType.getType("OOREMOTE")
                        || gdsType == GDSType.getType("FBOONATIVE")) {
                    fbServiceManager.setServerName(DB_SERVER_URL);
                    fbServiceManager.setPortNumber(DB_SERVER_PORT);
                }
                fbServiceManager.setUser(FBTestProperties.DB_USER);
                fbServiceManager.setPassword(FBTestProperties.DB_PASSWORD);
                firebirdSupportInfo = FirebirdSupportInfo.supportInfoFor(fbServiceManager.getServerVersion());
            }
            return firebirdSupportInfo;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize support info", e);
        }
    }

    private static final Map<GDSType, String> gdsTypeToUrlPrefixMap = new HashMap<>();
    static {
        gdsTypeToUrlPrefixMap.put(GDSType.getType("PURE_JAVA"), "jdbc:firebirdsql:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("EMBEDDED"), "jdbc:firebirdsql:embedded:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NATIVE"), "jdbc:firebirdsql:native:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("FBOOEMBEDDED"), "jdbc:firebirdsql:fboo:embedded:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("FBOONATIVE"), "jdbc:firebirdsql:fboo:native:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("OOREMOTE"), "jdbc:firebirdsql:oo:");

        // TODO Replace with an external definition/way to add additional types for third party plugins?
        // Not part of Jaybird:
        gdsTypeToUrlPrefixMap.put(GDSType.getType("ORACLE_MODE"), "jdbc:firebirdsql:oracle:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NIO"), "jdbc:firebirdsql:nio:");
    }

    /**
     * @return JDBC URL (without parameters) for this testrun
     */
    public static String getUrl() {
        return gdsTypeToUrlPrefixMap.get(getGdsType()) + getdbpath(DB_NAME);
    }

    /**
     * @param dbPath  Absolute path of the database
     * @return JDBC URL (without parameters) for this testrun
     */
    public static String getUrl(String dbPath) {
        if ("EMBEDDED".equalsIgnoreCase(GDS_TYPE)) {
            return gdsTypeToUrlPrefixMap.get(getGdsType()) + dbPath;
        } else {
            return gdsTypeToUrlPrefixMap.get(getGdsType()) + DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + dbPath;
        }
    }

    // FACTORY METHODS
    //
    // These methods should be used where possible so as to create the objects
    // bound to the appropriate GDS implementation.

    public static FbDatabaseFactory getFbDatabaseFactory() {
        return GDSFactory.getDatabaseFactoryForType(getGdsType());
    }

    public static FBManagedConnectionFactory createFBManagedConnectionFactory(boolean shared) {
        return new FBManagedConnectionFactory(shared, getGdsType());
    }

    public static FBManagedConnectionFactory createDefaultMcf() {
        return createDefaultMcf(true);
    }

    public static FBManagedConnectionFactory createDefaultMcf(boolean shared) {
        FBManagedConnectionFactory mcf = createFBManagedConnectionFactory(shared);
        mcf.setDatabaseName(DB_DATASOURCE_URL);
        mcf.setUser(DB_USER);
        mcf.setPassword(DB_PASSWORD);
        mcf.setEncoding(DB_LC_CTYPE);

        return mcf;
    }

    public static FBManager createFBManager() {
        return new FBManager(getGdsType());
    }

    public static FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return (FirebirdConnection) DriverManager.getConnection(getUrl(),
                getDefaultPropertiesForConnection());
    }

    /**
     * Creates the default test database, and configures the passed in FBManager with the server and type of test.
     *
     * @param fbManager
     *         instance used for creation of the database
     */
    public static void defaultDatabaseSetUp(FBManager fbManager) throws Exception {
        final GDSType gdsType = getGdsType();
        if (gdsType == GDSType.getType("PURE_JAVA")
                || gdsType == GDSType.getType("NATIVE")
                || gdsType == GDSType.getType("OOREMOTE")
                || gdsType == GDSType.getType("FBOONATIVE")) {
            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
        }
        fbManager.start();
        fbManager.setForceCreate(true);
        // disable force write for minor increase in test throughput
        fbManager.setForceWrite(false);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
    }

    /**
     * Deletes the default test database using the supplied FBManager (in
     * general this should be the same instance as was used for creating it).
     * 
     * @param fbManager
     *            FBManager instance
     */
    public static void defaultDatabaseTearDown(FBManager fbManager) throws Exception {
        try {
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        } finally {
            fbManager.stop();
        }
    }

    private FBTestProperties() {
        // No instantiation
    }
}
