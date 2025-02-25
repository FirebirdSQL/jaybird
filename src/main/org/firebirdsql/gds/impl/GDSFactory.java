/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2023 Tobias Weimer
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.util.PluginLoader;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * The class {@code GDSFactory} exists to provide a way to obtain objects
 * implementing GDS and Clumplet.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
public final class GDSFactory {
    private static final Set<GDSFactoryPlugin> registeredPlugins = new HashSet<>();
    private static final Map<GDSType, GDSFactoryPlugin> typeToPluginMap = new HashMap<>();
    /**
     * This sorting effectively puts the shortest JDBC URLs at the end of the map,
     * so the correct default protocol handling can be implemented.
     */
    private static final TreeMap<String, GDSFactoryPlugin> jdbcUrlToPluginMap = new TreeMap<>(Comparator.reverseOrder());

    private static GDSType defaultType;

    static {
        PluginLoader.findPlugins(GDSFactoryPlugin.class, List.of("org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin"))
                .forEach(GDSFactory::registerPlugin);

        GDSType pureJavaType = GDSType.getType(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME);
        if (pureJavaType != null && defaultType != pureJavaType && typeToPluginMap.containsKey(pureJavaType)) {
            // ensure defaultType is PURE_JAVA if that plugin was registered
            defaultType = pureJavaType;
        }
    }

    private GDSFactory() {
        // no instances
    }

    /**
     * Register plugin for this factory. Usually there is no need to register
     * plugins, since this happens automatically during initialization of this
     * class. However, there might be a situation when automatic plugin
     * registration does not work.
     *
     * @param plugin
     *         instance of {@link GDSFactoryPlugin} to register.
     */
    public static void registerPlugin(GDSFactoryPlugin plugin) {
        boolean newPlugin = registeredPlugins.add(plugin);
        if (!newPlugin)
            return;

        GDSType type = GDSType.registerType(plugin.getTypeName());
        typeToPluginMap.put(type, plugin);

        // set the default type (see also the static initializer which ensures PURE_JAVA will be default if available)
        if (defaultType == null) defaultType = type;

        // register aliases
        for (String alias : plugin.getTypeAliasList()) {
            GDSType aliasType = GDSType.registerType(alias);
            typeToPluginMap.put(aliasType, plugin);
        }

        for (String jdbcUrl : plugin.getSupportedProtocolList()) {
            GDSFactoryPlugin otherPlugin = jdbcUrlToPluginMap.put(jdbcUrl, plugin);

            if (otherPlugin != null && !otherPlugin.equals(plugin))
                throw new IllegalArgumentException(
                        "Duplicate JDBC URL pattern detected: URL %s, plugin %s, other plugin %s".formatted(
                                jdbcUrl, plugin.getTypeName(), otherPlugin.getTypeName()));
        }
    }

    /**
     * Get default GDS type.
     *
     * @return instance of {@link GDSType}.
     */
    public static GDSType getDefaultGDSType() {
        return defaultType;
    }

    public static FbDatabaseFactory getDatabaseFactoryForType(GDSType gdsType) {
        if (gdsType == null) gdsType = defaultType;
        return getPlugin(gdsType).getDatabaseFactory();
    }

    /**
     * Get connection string for the specified server name, port and database
     * name/path. This method delegates call to the factory plugin corresponding
     * to the specified type.
     *
     * @param gdsType
     *         instance of {@link GDSType} for which connection string should be returned.
     * @param server
     *         name or IP address of the database server, applies only to IPC and TCP connection modes, in other cases
     *         should be {@code null}.
     * @param port
     *         port on which database server opened listening socket, applies to TCP connection mode only, may be
     *         {@code null}.
     * @param path
     *         database name or path to the database
     * @return full connection string
     * @throws SQLException
     *         if connection string cannot be obtained.
     */
    public static String getDatabasePath(GDSType gdsType, String server, Integer port, String path)
            throws SQLException {
        return getPlugin(gdsType).getDatabasePath(server, port, path);
    }

    /**
     * Get path to the database from the specified JDBC URL. This method finds
     * the appropriate plugin and delegates the call to it. Plugin is
     * responsible for the call execution.
     *
     * @param gdsType
     *         type of the plugin, to which operation will be delegated to.
     * @param jdbcUrl
     *         JDBC url from which the database path must be extracted.
     * @return path to the database specified in the JDBC URL.
     * @throws SQLException
     *         error when database path cannot be extracted.
     */
    public static String getDatabasePath(GDSType gdsType, String jdbcUrl) throws SQLException {
        return getPlugin(gdsType).getDatabasePath(jdbcUrl);
    }

    /**
     * Get collection of the supported JDBC protocols.
     *
     * @return set of the supported protocols.
     */
    public static Set<String> getSupportedProtocols() {
        return Collections.unmodifiableSet(jdbcUrlToPluginMap.keySet());
    }

    /**
     * Create JDBC URL for the specified GDS type and database connection properties.
     *
     * @param gdsType
     *         type of the plugin, to which operation will be delegated to.
     * @param dbConnectionProperties
     *         Database connection properties
     * @return newly created JDBC URL
     * @throws SQLException When required information is missing to build the URL
     */
    public static String getJdbcUrl(GDSType gdsType, DatabaseConnectionProperties dbConnectionProperties)
            throws SQLException {
        DbAttachInfo dbAttachInfo = DbAttachInfo.of(dbConnectionProperties);
        GDSFactoryPlugin plugin = getPlugin(gdsType);
        return plugin.getDefaultProtocol() + plugin.getDatabasePath(dbAttachInfo);
    }

    /**
     * Get GDS type for the specified JDBC URL. This method finds the plugin
     * corresponding to the specified type and delegates the call to it.
     *
     * @param jdbcUrl
     *         JDBC URL for which GDS type should be obtained.
     * @return instance of {@link GDSType}.
     */
    public static GDSType getTypeForProtocol(String jdbcUrl) {
        for (Entry<String, GDSFactoryPlugin> entry : jdbcUrlToPluginMap.entrySet()) {
            String jdbcProtocol = entry.getKey();

            if (jdbcUrl.startsWith(jdbcProtocol)) {
                return GDSType.getType(entry.getValue().getTypeName());
            }
        }
        return null;
    }

    /**
     * Get class extending the {@link org.firebirdsql.jdbc.FBConnection}
     * that will be instantiated when new connection is created. This method
     * finds the plugin for the specified type and delegates the call to it.
     *
     * @param gdsType
     *         instance of {@link GDSType}
     * @return class to instantiate for the database connection.
     */
    public static Class<?> getConnectionClass(GDSType gdsType) {
        return getPlugin(gdsType).getConnectionClass();
    }

    /**
     * Get plugin for the specified GDS type.
     *
     * @param gdsType
     *         GDS type.
     * @return instance of {@link GDSFactoryPlugin}
     * @throws IllegalArgumentException
     *         if specified type is not known.
     */
    @InternalApi
    public static GDSFactoryPlugin getPlugin(GDSType gdsType) {
        GDSFactoryPlugin gdsPlugin = typeToPluginMap.get(gdsType);
        if (gdsPlugin == null) {
            throw new IllegalArgumentException("Specified GDS type " + gdsType + " is unknown.");
        }
        return gdsPlugin;
    }
}
