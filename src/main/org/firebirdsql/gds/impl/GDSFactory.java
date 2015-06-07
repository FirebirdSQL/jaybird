/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * The class <code>GDSFactory</code> exists to provide a way to obtain objects
 * implementing GDS and Clumplet.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class GDSFactory {

    private static Logger log = LoggerFactory.getLogger(GDSFactory.class
    );

    /**
     * Class for string comparison in the descendant order. This effectively
     * puts the most short JDBC URLs at the end of the list, so the correct
     * default protocol handling can be implemented.
     */
    private static class ReversedStringComparator implements Comparator<String>, Serializable {

        public int compare(String s1, String s2) {
            // note, we compare here s2 to s1,
            // this causes descending sorting
            return s2.compareTo(s1);
        }
    }

    private static final Set<GDSFactoryPlugin> registeredPlugins = new HashSet<>();

    private static final Map<GDSType, GDSFactoryPlugin> typeToPluginMap = new HashMap<>();

    private static final TreeMap<String, GDSFactoryPlugin> jdbcUrlToPluginMap = new TreeMap<>(new ReversedStringComparator());

    private static GDSType defaultType;

    // TODO: Replace with explicit initializer from GDSType?
    static {
        // register first all plugins that belong to the same class loader
        // in which this class is loaded
        final List<ClassLoader> classLoaders = classLoadersForLoading();
        try {
            for (ClassLoader classLoader : classLoaders) {
                loadPluginsFromClassLoader(classLoader);
            }
        } catch (Exception ex) {
            if (log != null) log.error("Can't register plugins ", ex);
        }

        if (jdbcUrlToPluginMap.isEmpty()) {
            if (log != null) log.warn("No plugins loaded from META-INF/services, falling back to fixed registration of default plugins");
            for (ClassLoader classLoader : classLoaders) {
                loadPluginsFallback(classLoader);
            }
        }
    }

    /**
     * List of class loaders to use for loading the {@link GDSFactoryPlugin} implementations.
     *
     * @return Collection of {@link ClassLoader} instances
     */
    private static List<ClassLoader> classLoadersForLoading() {
        final List<ClassLoader> classLoaders = new ArrayList<>(2);
        final ClassLoader classLoader = GDSFactory.class.getClassLoader();
        if (classLoader != null) {
            classLoaders.add(classLoader);
        } else {
            classLoaders.add(ClassLoader.getSystemClassLoader());
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null && !classLoaders.contains(contextClassLoader)) {
            classLoaders.add(contextClassLoader);
        }
        return classLoaders;
    }

    /**
     * Load all existing plugins from the specified class loader.
     * 
     * @param classLoader instance of {@link ClassLoader}.
     */
    private static void loadPluginsFromClassLoader(ClassLoader classLoader) {
        ServiceLoader<GDSFactoryPlugin> pluginLoader = ServiceLoader.load(GDSFactoryPlugin.class, classLoader);
        for (GDSFactoryPlugin plugin : pluginLoader) {
            registerPlugin(plugin);
        }
    }

    /**
     * Loads the plugins from a hardcoded list of class names.
     * <p>
     * This method is intended as a fallback in case the plugins could not be discovered from the
     * {@code META-INF/services/org.firebirdsql.gds.impl.GDSFactoryPlugin} file(s). See also
     * <a href="http://tracker.firebirdsql.org/browse/JDBC-325">issue JDBC-325</a>
     * </p>
     */
    private static void loadPluginsFallback(final ClassLoader classLoader) {
        String[] pluginClasses = new String[] {
                "org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin",
                "org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin",
                "org.firebirdsql.gds.impl.jni.LocalGDSFactoryPlugin",
                "org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin",
                "org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin"
        };
        for (String className : pluginClasses) {
            try {
                Class<?> clazz = classLoader.loadClass(className);
                GDSFactoryPlugin plugin = (GDSFactoryPlugin) clazz.newInstance();
                registerPlugin(plugin);
            } catch (Exception ex) {
                log.error("Can't register plugin" + className, ex);
            }
        }
    }

    /**
     * Register plugin for this factory. Usually there is no need to register
     * plugins, since this happens automatically during initialization of this
     * class. However, there might be a situation when automatic plugin
     * registration does not work.
     * 
     * @param plugin
     *            instance of {@link GDSFactoryPlugin} to register.
     */
    public static void registerPlugin(GDSFactoryPlugin plugin) {
        boolean newPlugin = registeredPlugins.add(plugin);
        if (!newPlugin)
            return;

        GDSType type = GDSType.registerType(plugin.getTypeName());
        typeToPluginMap.put(type, plugin);

        // set the default type
        if (defaultType == null) defaultType = type;

        // register aliases
        String[] aliases = plugin.getTypeAliases();
        for (String alias : aliases) {
            GDSType aliasType = GDSType.registerType(alias);
            typeToPluginMap.put(aliasType, plugin);
        }

        String[] jdbcUrls = plugin.getSupportedProtocols();
        for (String jdbcUrl : jdbcUrls) {
            GDSFactoryPlugin otherPlugin = jdbcUrlToPluginMap.put(jdbcUrl, plugin);

            if (otherPlugin != null && !otherPlugin.equals(plugin))
                throw new IllegalArgumentException(
                        "Duplicate JDBC URL pattern detected: URL " + jdbcUrl + ", " +
                                "plugin " + plugin.getTypeName() + ", other plugin " + otherPlugin.getTypeName());
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
     *            instance of {@link GDSType} for which connection string should
     *            be returned.
     * 
     * @param server
     *            name or IP address of the database server, applies only to IPC
     *            and TCP connection modes, in other cases should be
     *            <code>null</code>.
     * 
     * @param port
     *            port on which database server opened listening socket, applies
     *            to TCP connection mode only, may be <code>null</code>.
     * 
     * @param path
     *            database name or path to the database
     * 
     * @return full connection string
     * 
     * @throws GDSException
     *             if connection string cannot be obtained.
     */
    public static String getDatabasePath(GDSType gdsType, String server, Integer port, String path)
            throws GDSException {
        return getPlugin(gdsType).getDatabasePath(server, port, path);
    }

    /**
     * Get path to the database from the specified JDBC URL. This method finds
     * the appropriate plugin and delegates the call to it. Plugin is
     * responsible for the call execution.
     * 
     * @param gdsType
     *            type of the plugin, to which operation will be delegated to.
     * @param jdbcUrl
     *            JDBC url from which the database path must be extracted.
     * 
     * @return path to the database specified in the JDBC URL.
     * 
     * @throws GDSException
     *             error when database path cannot be extracted.
     */
    public static String getDatabasePath(GDSType gdsType, String jdbcUrl)
            throws GDSException {
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
     * Create JDBC URL for the specified GDS type and database path.
     * 
     * @param gdsType
     *            type of the plugin, to which operation will be delegated to.
     * @param databasePath
     *            path to the database.
     * 
     * @return newly created JDBC URL.
     */
    public static String getJdbcUrl(GDSType gdsType, String databasePath) {
        return getPlugin(gdsType).getDefaultProtocol() + databasePath;
    }

    /**
     * Get GDS type for the specified JDBC URL. This method finds the plugin
     * corresponding to the specified type and delegates the call to it.
     * 
     * @param jdbcUrl
     *            JDBC URL for which GDS type should be obtained.
     * 
     * @return instance of {@link GDSType}.
     */
    public static GDSType getTypeForProtocol(String jdbcUrl) {
        // TODO use TreeMap functionality to locate protocol (eg using floorKey() or ceilingKey())?
        for (Entry<String, GDSFactoryPlugin> entry : jdbcUrlToPluginMap.entrySet()) {
            String jdbcProtocol = entry.getKey();

            if (jdbcUrl.startsWith(jdbcProtocol))
                return GDSType.getType(entry.getValue().getTypeName());
        }

        return null;
    }

    /**
     * Get class extending the {@link org.firebirdsql.jdbc.FBConnection}
     * that will be instantiated when new connection is created. This method
     * finds the plugin for the specified type and delegates the call to it.
     * 
     * @param gdsType
     *            instance of {@link GDSType}
     * 
     * @return class to instantiate for the database connection.
     */
    public static Class<?> getConnectionClass(GDSType gdsType) {
        return getPlugin(gdsType).getConnectionClass();
    }

    /**
     * Get plugin for the specified GDS type.
     * 
     * @param gdsType
     *            GDS type.
     * 
     * @return instance of {@link GDSFactoryPlugin}
     * 
     * @throws IllegalArgumentException
     *             if specified type is not known.
     */
    private static GDSFactoryPlugin getPlugin(GDSType gdsType) {
        GDSFactoryPlugin gdsPlugin = typeToPluginMap.get(gdsType);
        if (gdsPlugin == null)
            throw new IllegalArgumentException("Specified GDS type " + gdsType + " is unknown.");
        return gdsPlugin;
    }
}
