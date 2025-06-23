// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2003-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.firebirdsql.event.FBEventManager;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.props.ServiceConnectionProperties;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBServiceManager;
import org.firebirdsql.management.ServiceManager;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;
import static org.hamcrest.Matchers.not;

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
            throw new ExceptionInInitializerError("Could not load FBDriver class");
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
    public static final String GDS_TYPE = getProperty("test.gds_type", "PURE_JAVA");
    public static final boolean USE_FIREBIRD_AUTOCOMMIT =
            Boolean.parseBoolean(getProperty("test.use_firebird_autocommit", "false"));
    public static final String ENABLE_PROTOCOL = trimToNull(getProperty("test.enableProtocol", "*"));

    public static boolean isLocalhost() {
        return "localhost".equals(DB_SERVER_URL) || "127.0.0.1".equals(DB_SERVER_URL);
    }

    public static String getDatabasePath() {
        return getDatabasePath(DB_NAME);
    }

    public static String getDatabasePath(String name) {
        if (not(isEmbeddedType()).matches(GDS_TYPE) && (!isLocalhost() || DB_ON_DOCKER)) {
            return DB_PATH + "/" + name;
        }
        return Path.of(DB_PATH, name).toAbsolutePath().toString();
    }

    /**
     * Builds a firebird database connection string for the supplied database file.
     * 
     * @param name Database name
     * @return URL or path for the gds type.
     */
    public static String getdbpath(String name) {
        if (isEmbeddedType().matches(GDS_TYPE)) {
            return Path.of(DB_PATH, name).toAbsolutePath().toString();
        } else {
            return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + getDatabasePath(name);
        }
    }

    /**
     * @return Default database connection properties for this testrun
     */
    public static Properties getDefaultPropertiesForConnection() {
        var props = new Properties();

        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("lc_ctype", DB_LC_CTYPE);
        if (USE_FIREBIRD_AUTOCOMMIT) {
            props.setProperty("useFirebirdAutocommit", "true");
        }
        if (ENABLE_PROTOCOL != null) {
            props.setProperty("enableProtocol", ENABLE_PROTOCOL);
        }

        return props;
    }

    /**
     * @return default database connection properties, with {@code k1 = v1} added
     */
    public static Properties getPropertiesForConnection(String k1, String v1) {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(k1, v1);
        return props;
    }

    /**
     * @return default database connection properties, with {@code additionalProperties} added
     */
    public static Properties getPropertiesForConnection(Map<String, String> additionalProperties) {
        Properties props = getDefaultPropertiesForConnection();
        additionalProperties.forEach(props::setProperty);
        return props;
    }

    /**
     * @return new connection properties object, configured with the test defaults
     * @see #configureDefaultDbProperties(DatabaseConnectionProperties)
     */
    public static FbConnectionProperties getDefaultFbConnectionProperties() {
        return configureDefaultDbProperties(new FbConnectionProperties());
    }

    /**
     * @return new service properties object, configured with the test defaults
     * @see #configureDefaultServiceProperties(ServiceConnectionProperties)
     */
    public static FbServiceProperties getDefaultServiceProperties() {
        return configureDefaultServiceProperties(new FbServiceProperties());
    }

    public static <T extends DatabaseConnectionProperties> T configureDefaultDbProperties(T connectionInfo) {
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        return configureDefaultAttachmentProperties(connectionInfo);
    }

    public static <T extends ServiceConnectionProperties> T configureDefaultServiceProperties(T connectionInfo) {
        return configureDefaultAttachmentProperties(connectionInfo);
    }

    public static <T extends AttachmentProperties> T configureDefaultAttachmentProperties(T connectionInfo) {
        if (not(isEmbeddedType()).matches(GDS_TYPE)) {
            connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
            connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        }
        // FBServiceManager and FBEventManager don't allow setting type after construction
        if (!(connectionInfo instanceof FBServiceManager || connectionInfo instanceof FBEventManager)) {
            connectionInfo.setType(GDS_TYPE);
        }
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setEncoding(DB_LC_CTYPE);
        connectionInfo.setEnableProtocol(ENABLE_PROTOCOL);
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
        configureDefaultAttachmentProperties(serviceManager);
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
                GDSType gdsType = getGdsType();
                FBServiceManager fbServiceManager = configureDefaultServiceProperties(new FBServiceManager(gdsType));
                fbServiceManager.setEnableProtocol("*");
                firebirdSupportInfo = FirebirdSupportInfo.supportInfoFor(fbServiceManager.getServerVersion());
            }
            return firebirdSupportInfo;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize support info", e);
        }
    }

    private static String getProtocolPrefix() {
        return GDSFactory.getPlugin(getGdsType()).getDefaultProtocol();
    }

    /**
     * @return JDBC URL (without parameters) for this testrun
     */
    public static String getUrl() {
        return getProtocolPrefix() + getdbpath(DB_NAME);
    }

    /**
     * @param dbPath  Absolute path of the database
     * @return JDBC URL (without parameters) for this test run
     */
    public static String getUrl(String dbPath) {
        if (isEmbeddedType().matches(GDS_TYPE)) {
            return getProtocolPrefix() + dbPath;
        } else {
            return getProtocolPrefix() + DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + dbPath;
        }
    }

    /**
     * Convenience method equivalent to {@code getUrl(dbPath.toAbsolutePath().toString())}.
     *
     * @param dbPath
     *         path of the database
     * @return JDBC URL (without parameters) for this test run
     */
    public static String getUrl(Path dbPath) {
        return getUrl(dbPath.toAbsolutePath().toString());
    }

    // FACTORY METHODS
    //
    // These methods should be used where possible, to create the objects bound to the appropriate GDS implementation.

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
        return configureDefaultDbProperties(createFBManagedConnectionFactory(shared));
    }

    public static FBManager createFBManager() {
        FBManager fbManager = new FBManager(getGdsType());
        fbManager.setEnableProtocol(ENABLE_PROTOCOL);
        return fbManager;
    }

    public static FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return getConnectionViaDriverManager(getDefaultPropertiesForConnection());
    }

    public static FirebirdConnection getConnectionViaDriverManager(Properties props) throws SQLException {
        return DriverManager.getConnection(getUrl(), props).unwrap(FirebirdConnection.class);
    }

    /**
     * The property {@code k1 = v1} is used in addition (and possibly overwriting) the default properties
     */
    public static FirebirdConnection getConnectionViaDriverManager(String k1, String v1) throws SQLException {
        return getConnectionViaDriverManager(getPropertiesForConnection(k1, v1));
    }

    /**
     * The properties {@code additionalProperties) are used in addition (and possibly overwriting) the default
     * properties.
     */
    public static FirebirdConnection getConnectionViaDriverManager(Map<String, String> additionalProperties)
            throws SQLException {
        return getConnectionViaDriverManager(getPropertiesForConnection(additionalProperties));
    }

    public static <T extends FBManager> T configureFBManager(T fbManager) throws Exception {
        return configureFBManager(fbManager, true);
    }

    public static <T extends FBManager> T configureFBManager(T fbManager, boolean start) throws Exception {
        configureDefaultAttachmentProperties(fbManager);
        if (start) fbManager.start();
        fbManager.setForceCreate(true);
        // disable force write for minor increase in test throughput
        fbManager.setForceWrite(false);
        return fbManager;
    }

    /**
     * Creates the default test database, and configures the passed in FBManager with the server and type of test.
     *
     * @param fbManager
     *         instance used for creation of the database
     */
    public static void defaultDatabaseSetUp(FBManager fbManager) throws Exception {
        configureFBManager(fbManager);
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

    /**
     * If schema support is available, returns {@code forSchema}, otherwise returns {@code withoutSchema}.
     *
     * @param forSchema
     *         value to return when schema support is available
     * @param withoutSchema
     *         value to return when schema support is not available
     * @return {@code forSchema} if schema support is available, otherwise {@code withoutSchema}
     * @see FirebirdSupportInfo#ifSchemaElse(Object, Object)
     */
    public static <T> T ifSchemaElse(T forSchema, T withoutSchema) {
        return getDefaultSupportInfo().ifSchemaElse(forSchema, withoutSchema);
    }

    private FBTestProperties() {
        // No instantiation
    }
}
