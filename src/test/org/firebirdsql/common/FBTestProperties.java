// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2003-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
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
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.firebirdsql.gds.ng.jna.FbClientFeature;
import org.firebirdsql.gds.ng.jna.FbClientFeatureAccess;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.ServiceConnectionProperties;
import org.firebirdsql.jaybird.util.BasicVersion;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBServiceManager;
import org.firebirdsql.management.ServiceManager;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNullElse;
import static org.firebirdsql.common.PathUtils.posixPathString;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;
import static org.hamcrest.Matchers.not;

/**
 * Helper class for test properties (database user, password, paths etc).
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
    private static final Path DB_DIR_PATH;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Path> DB_MAPPED_LOCAL_PATH =
            Optional.ofNullable(trimToNull(getProperty("test.db.mapped"))).map(Path::of);
    public static final String DB_SERVER_URL = getProperty("test.db.host", "localhost");
    public static final int DB_SERVER_PORT = Integer.parseInt(getProperty("test.db.port", "3050"));
    public static final String DB_LC_CTYPE = getProperty("test.db.lc_ctype", "NONE");
    public static final boolean DB_ON_DOCKER = Boolean.parseBoolean(getProperty("test.db_on_docker", "false"));
    private static final @Nullable Boolean EVENTS_AVAILABLE;
    public static final String GDS_TYPE = getProperty("test.gds_type", "PURE_JAVA");
    public static final boolean USE_FIREBIRD_AUTOCOMMIT =
            Boolean.parseBoolean(getProperty("test.use_firebird_autocommit", "false"));
    public static final String ENABLE_PROTOCOL = trimToNull(getProperty("test.enableProtocol", "*"));
    // Allows running native tests against Firebird 2.5 or older with a Firebird 3.0 or newer fbclient.
    private static final boolean NATIVE_LEGACY_AUTH_COMPAT =
            Boolean.parseBoolean(getProperty("test.native_legacy_auth_compat", "false"));
    private static final String NATIVE_LEGACY_AUTH_COMPAT_AUTH_PLUGINS = "Legacy_Auth";

    static {
        var dbDirPath = Path.of(DB_PATH.isEmpty() ? "." : DB_PATH);
        if (isSameHostServer()) {
            dbDirPath = dbDirPath.toAbsolutePath();
        }
        DB_DIR_PATH = dbDirPath;

        String eventsAvailable = trimToNull(getProperty("test.event.available"));
        EVENTS_AVAILABLE = eventsAvailable != null ? Boolean.valueOf(eventsAvailable) : null;
    }

    public static boolean isLocalhost() {
        return "localhost".equalsIgnoreCase(DB_SERVER_URL)
                || "127.0.0.1".equals(DB_SERVER_URL)
                || "::1".equals(DB_SERVER_URL);
    }

    public static boolean isDbOnDocker() {
        return DB_ON_DOCKER;
    }

    public static boolean isDefaultPort() {
        return DB_SERVER_PORT == PropertyConstants.DEFAULT_PORT;
    }

    public static boolean isEventPortAvailable() {
        return requireNonNullElse(EVENTS_AVAILABLE, !DB_ON_DOCKER);
    }

    public static String getDatabasePath() {
        return getDatabasePath(DB_NAME);
    }

    public static String getDatabasePath(String name) {
        return getDatabasePath(Path.of(DB_PATH, name));
    }

    public static String getDatabasePath(Path path) {
        return isSameHostServer() ? path.toString() : posixPathString(path);
    }

    /**
     * Check if database paths and Firebird server (or embedded) is truly local.
     * <p>
     * Specifically, it means that the server accesses paths on the local filesystem of this machine and that the return
     * values of {@link #getDatabasePath()} and {@link #getDatabasePath(String)} are local to this machine.
     * </p>
     *
     * @return {@code true} if server has direct access to the filesystem of this host
     * @see #hasMappedDatabaseDirectory()
     */
    public static boolean isSameHostServer() {
        return isEmbeddedType().matches(GDS_TYPE) || (isLocalhost() && !isDbOnDocker());
    }

    /**
     * Check if database files are locally accessible to the tests.
     * <p>
     * Specifically, it means that either the server has direct filesystem access (i.e. {@link #isSameHostServer()}
     * returns {@code true}), or the directory specified in system property {@code test.db.dir} is mapped (e.g. as a
     * volume, or network share) in the directory specified in system property {@code test.db.mapped}, and
     * {@link #getMappedDatabaseDirectory()}, {@link #getMappedDatabasePath()} and {@link #getMappedDatabasePath(String)} return
     * non-empty.
     * </p>
     * <p>
     * Contrary to {@link #isSameHostServer()}, this method returning {@code true} does not necessarily mean that the
     * Firebird server has direct filesystem access to this machine, nor that it would recognize paths as returned by
     * the {@code getLocalDatabase...} methods.
     * </p>
     *
     * @return {@code true} if database files are locally accessible
     */
    public static boolean hasMappedDatabaseDirectory() {
        return isSameHostServer() || DB_MAPPED_LOCAL_PATH.isPresent();
    }

    /**
     * Builds a firebird database connection string for the supplied database file.
     *
     * @param name
     *         Database name
     * @return URL or path for the gds type.
     */
    public static String getdbpath(String name) {
        String databasePath = getDatabasePath(name);
        if (isEmbeddedType().matches(GDS_TYPE)) {
            return databasePath;
        } else {
            return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + databasePath;
        }
    }

    /**
     * The mapped database directory.
     * <p>
     * For local databases, this is the normal database directory ({@code test.db.dir}). For remote databases, or
     * databases on Docker, this can be configured using the {@code test.db.mapped} system property. For remote
     * databases, this can be the local mount of a network share, or for databases on Docker, the host directory backing
     * the volume.
     * </p>
     *
     * @return mapped database directory, or empty
     */
    public static Optional<Path> getMappedDatabaseDirectory() {
        return isSameHostServer() ? Optional.of(DB_DIR_PATH) : DB_MAPPED_LOCAL_PATH;
    }

    /**
     * The database name {@code name} resolved against {@link #getMappedDatabaseDirectory()}.
     * <p>
     * The {@code name} expects a relative path; absolute paths will likely not work unless {@link #isSameHostServer()}
     * is {@code true}).
     * </p>
     *
     * @param name
     *         database name (or other file)
     * @return mapped database file path, or empty ({@link #hasMappedDatabaseDirectory()} returns {@code false})
     */
    public static Optional<Path> getMappedDatabasePath(String name) {
        return getMappedDatabaseDirectory().map(p -> p.resolve(name));
    }

    /**
     * The database name of the default test database.
     *
     * @return mapped database file path, or empty ({@link #hasMappedDatabaseDirectory()} returns {@code false})
     */
    public static Optional<Path> getMappedDatabasePath() {
        return getMappedDatabasePath(DB_NAME);
    }

    /**
     * Transforms (remaps) the mapped database path to the server-side database path.
     *
     * @param mappedPath
     *         mapped path (i.e. relative to {@link #getMappedDatabaseDirectory()})
     * @return database file path
     */
    public static Optional<Path> transformMappedToDatabasePath(Path mappedPath) {
        return getMappedDatabaseDirectory()
                .map(mappedDir -> DB_DIR_PATH.resolve(mappedDir.relativize(mappedPath)));
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
        if (isEnableNativeLegacyAuthCompat()) {
            props.setProperty("authPlugins", NATIVE_LEGACY_AUTH_COMPAT_AUTH_PLUGINS);
        }

        return props;
    }

    /**
     * @return default database connection properties, with {@code k1 = v1} added
     */
    public static Properties getPropertiesForConnection(String k1, String v1) {
        Properties props = getDefaultPropertiesForConnection();
        if (v1 != null) {
            props.setProperty(k1, v1);
        }
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
        if (isEnableNativeLegacyAuthCompat()) {
            connectionInfo.setAuthPlugins(NATIVE_LEGACY_AUTH_COMPAT_AUTH_PLUGINS);
        }
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
     * Convenience method equivalent to {@code getUrl(getDatabasePath(dbPath))}.
     *
     * @param dbPath
     *         path of the database
     * @return JDBC URL (without parameters) for this test run
     */
    public static String getUrl(Path dbPath) {
        return getUrl(getDatabasePath(dbPath));
    }

    /**
     * Convenience method equivalent to {@code getUrl(mappedPath.toServerPath())}.
     *
     * @param mappedPath
     *         path of the database
     * @return JDBC URL (without parameters) for this test run
     */
    public static String getUrl(MappedPath mappedPath) {
        return getUrl(mappedPath.toServerPath());
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
        if (isEnableNativeLegacyAuthCompat()) {
            fbManager.setAuthPlugins(NATIVE_LEGACY_AUTH_COMPAT_AUTH_PLUGINS);
        }
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
     * @return {@code true} if modern URLs (e.g. inet:// ...) are supported, {@code false} otherwise (i.e. a native test
     * where a client library of Firebird 2.5 or older is used, or for pure Java)
     */
    public static boolean supportsNativeModernUrls() {
        if (isPureJavaType().matches(GDS_TYPE)) {
            return false;
        } else {
            try {
                Method getClientLibrary = AbstractNativeDatabaseFactory.class.getDeclaredMethod("getClientLibrary");
                getClientLibrary.setAccessible(true);
                FbClientLibrary clientLibrary = (FbClientLibrary) getClientLibrary.invoke(
                        FBTestProperties.getFbDatabaseFactory());
                if (clientLibrary instanceof FbClientFeatureAccess) {
                    return ((FbClientFeatureAccess) clientLibrary).hasFeature(FbClientFeature.FB_PING);
                }
                return false;
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isEnableNativeLegacyAuthCompat() {
        return NATIVE_LEGACY_AUTH_COMPAT && isOtherNativeType().matches(GDS_TYPE);
    }

    /**
     * @return the Java major/feature version (as defined in {@code java.lang.Runtime.Version} in Java 10 and higher)
     */
    public static int getJavaFeatureVersion() {
        return Runtime.version().feature();
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
    public static <T extends @Nullable Object> T ifSchemaElse(T forSchema, T withoutSchema) {
        return getDefaultSupportInfo().ifSchemaElse(forSchema, withoutSchema);
    }

    /**
     * Helper method that replaces {@code "PUBLIC"} or {@code "SYSTEM"} with {@code ""} if schemas are not supported.
     *
     * @param schemaName
     *         schema name
     * @return {@code schemaName}, or &mdash; if {@code schemaName} is {@code "PUBLIC"} or {@code "SYSTEM"} and schemas
     * are not supported &mdash; {@code ""}
     */
    public static String resolveSchema(String schemaName) {
        if (!getDefaultSupportInfo().supportsSchemas()
                && ("PUBLIC".equals(schemaName) || "SYSTEM".equals(schemaName))) {
            return "";
        }
        return schemaName;
    }

    public static BasicVersion minimumVersionSupported() {
        return BasicVersion.of(3);
    }

    public static BasicVersion maximumVersionSupported() {
        return BasicVersion.of(6);
    }

    private FBTestProperties() {
        // No instantiation
    }
}
