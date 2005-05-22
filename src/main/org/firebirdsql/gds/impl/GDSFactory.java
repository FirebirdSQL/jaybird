/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The class <code>GDSFactory</code> exists to provide a way to obtain objects
 * implementing GDS and Clumplet.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class GDSFactory {

    private static class ReversedStringComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;
            
            // note, we compare here s2 to s1, 
            // this causes descending sorting
            return s2.compareTo(s1);
        }
        
    }
    
    private static HashSet registeredPlugins = new HashSet();
    private static HashMap typeToPluginMap = new HashMap();
    private static TreeMap jdbcUrlToPluginMap = new TreeMap(new ReversedStringComparator());

    private static GDSType defaultType;
    
    // initialize this class using the Sun implementation of the plugins interface
    static {
        Iterator iter = sun.misc.Service.providers(GDSFactoryPlugin.class);
        while(iter.hasNext()) {
            GDSFactoryPlugin plugin = (GDSFactoryPlugin) iter.next();
            registerPlugin(plugin);
        }
    }
    
    /**
     * Register plugin for this factory. Usually there is no need to register
     * plugins, since this happens automatically during initialization of this
     * class. However, there might be a situation when automatic plugin 
     * registration does not work.
     * 
     * @param plugin instance of {@link GDSFactoryPlugin} to register.
     */
    public static void registerPlugin(GDSFactoryPlugin plugin) {
        
        registeredPlugins.add(plugin);
        
        GDSType type = GDSType.registerType(plugin.getTypeName());
        typeToPluginMap.put(type, plugin);
        
        // set the default type
        if (defaultType == null)
            defaultType = type;
        
        // register aliases
        String[] aliases = plugin.getTypeAliases();
        for (int i = 0; i < aliases.length; i++) {
            GDSType aliasType = GDSType.registerType(aliases[i]);
            typeToPluginMap.put(aliasType, plugin);
        }
        
        String[] jdbcUrls = (String[])plugin.getSupportedProtocols();
        for (int i = 0; i < jdbcUrls.length; i++) {
            
            if (jdbcUrlToPluginMap.containsKey(jdbcUrls[i]))
                throw new IllegalArgumentException("Duplicate JDBC URL pattern detected.");
            
            jdbcUrlToPluginMap.put(jdbcUrls[i], plugin);
        }
    }
    
    /**
     * Get an instance of the default <code>GDS</code> implemenation.
     * 
     * @return A default <code>GDS</code> instance
     */
    public static GDS getDefaultGDS() {
        return getGDSForType(defaultType);
    }

    /**
     * Get an instance of the specified implemenation of <code>GDS</code>.
     * 
     * @param gdsType
     *            The type of the <code>GDS</code> instance to be returned
     * @return A <code>GDS</code> implementation of the given type
     */
    public synchronized static GDS getGDSForType(GDSType gdsType) {
        if (gdsType == null)
            gdsType = defaultType;
        
        GDSFactoryPlugin gdsPlugin = 
            (GDSFactoryPlugin)typeToPluginMap.get(gdsType);
        
        if (gdsPlugin == null)
            throw new IllegalArgumentException(
                    "Specified GDS type " + gdsType + " is unknown.");

        return gdsPlugin.getGDS();
    }
    
    /**
     * Get connection string for the specified server name, port and database
     * name/path. This method delegates call to the factory plugin corresponding
     * to the specified type.
     * 
     * @param gdsType instance of {@link GDSType} for which connection string
     * should be returned.
     * 
     * @param server name or IP address of the database server, applies only to
     * IPC and TCP connection modes, in other cases should be <code>null</code>.
     * 
     * @param port port on which database server opened listening socket, applies
     * to TCP connection mode only, may be <code>null</code>.
     * 
     * @param path database name or path to the database
     * 
     * @return full connection string that can be passed to 
     * {@link GDS#iscAttachDatabase(String, IscDbHandle, DatabaseParameterBuffer)}
     * method.
     * 
     * @throws GDSException if connection string cannot be obtained.
     */
    public static String getDatabasePath(GDSType gdsType, String server,
            Integer port, String path) throws GDSException {
        
        return getPlugin(gdsType).getDatabasePath(server, port, path);
    }

    public static String getDatabasePath(GDSType gdsType, String jdbcUrl) throws GDSException {
        return getPlugin(gdsType).getDatabasePath(jdbcUrl);
    }

    public static Set getSupportedProtocols() {
        return jdbcUrlToPluginMap.keySet();
    }
    
    public static String getJdbcUrl(GDSType gdsType, String databasePath) {
        return getPlugin(gdsType).getDefaultProtocol() + databasePath;
    }
    
    public static GDSType getTypeForProtocol(String jdbcUrl) {
        for (Iterator iter = jdbcUrlToPluginMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            String jdbcProtocol = (String)entry.getKey();
            GDSFactoryPlugin plugin = (GDSFactoryPlugin)entry.getValue();
            
            if (jdbcUrl.startsWith(jdbcProtocol))
                return GDSType.getType(plugin.getTypeName());
        }
        
        return null;
    }
    
    public static Class getConnectionClass(GDSType gdsType) {
        return getPlugin(gdsType).getConnectionClass();
    }

    private static GDSFactoryPlugin getPlugin(GDSType gdsType) {
        GDSFactoryPlugin gdsPlugin = 
            (GDSFactoryPlugin) typeToPluginMap.get(gdsType);
        if (gdsPlugin == null)
            throw new IllegalArgumentException("Specified GDS type " + gdsType
                    + " is unknown.");
        return gdsPlugin;
    }
}
