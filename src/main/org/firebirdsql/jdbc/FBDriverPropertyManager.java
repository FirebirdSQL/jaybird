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

import org.firebirdsql.gds.ParameterBufferHelper;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.gds.ParameterBufferHelper.DPB_PREFIX;
import static org.firebirdsql.jdbc.FBConnectionProperties.DATABASE_PROPERTY;
import static org.firebirdsql.jdbc.FBConnectionProperties.TYPE_PROPERTY;

/**
 * Manager of the DPB properties.
 */
class FBDriverPropertyManager {

    /**
     * Container class for the driver properties.
     */
    private static class PropertyInfo {
        private final String alias;
        private final String dpbName;
        private final String dpbShortName;
        private final Integer dpbKey;
        private final String description;

        private final int hashCode;

        public PropertyInfo(String alias, String dpbName, Integer dpbKey, String description) {
            assert dpbName.startsWith(DPB_PREFIX) : "dpName should start with " + DPB_PREFIX + ", was: " + dpbName;
            this.alias = alias;
            this.dpbName = dpbName;
            this.dpbShortName = dpbName.substring(DPB_PREFIX.length());
            this.dpbKey = dpbKey;
            this.description = description;

            hashCode = Objects.hash(alias, dpbName, dpbKey);
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof PropertyInfo)) return false;

            PropertyInfo that = (PropertyInfo) obj;

            boolean result = Objects.equals(this.alias, that.alias);
            result &= this.dpbName.equals(that.dpbName);
            result &= this.dpbKey.equals(that.dpbKey);

            return result;
        }
    }

    private static final Map<String, PropertyInfo> aliases;
    private static final Map<String, PropertyInfo> dpbMap;

    static {
        final Map<String, PropertyInfo> tempAliases = new HashMap<>(64);
        final Map<String, PropertyInfo> tempDpbMap = new HashMap<>(256);
        // process aliases and descriptions first
        ResourceBundle info = getDriverPropertyInfoResourceBundle();
        if (info != null) {
            for (Enumeration<String> en = info.getKeys(); en.hasMoreElements(); ) {
                String alias = en.nextElement();
                String value = info.getString(alias);

                int hashIndex = value.indexOf('#');

                String dpbName;
                String description;
                if (hashIndex != -1) {
                    dpbName = value.substring(0, hashIndex).trim();
                    description = value.substring(hashIndex + 1).trim();
                } else {
                    dpbName = value.trim();
                    description = "";
                }

                // skip incorrect mappings
                if (!dpbName.startsWith(DPB_PREFIX))
                    continue;

                Integer dpbKey = ParameterBufferHelper.getDpbKey(dpbName);

                // skip unknown elements
                if (dpbKey == null)
                    continue;

                PropertyInfo propInfo = new PropertyInfo(alias, dpbName, dpbKey, description);

                tempAliases.put(propInfo.alias, propInfo);
                tempDpbMap.put(propInfo.dpbName, propInfo);
            }
        }

        // fill rest of the properties
        for (Map.Entry<String, Integer> entry : ParameterBufferHelper.getDpbMap().entrySet()) {
            String dpbName = entry.getKey();
            Integer dpbKey = entry.getValue();

            if (!dpbName.startsWith(DPB_PREFIX) || tempDpbMap.containsKey(dpbName)) {
                continue;
            }

            PropertyInfo propInfo = new PropertyInfo(null, dpbName, dpbKey, "");

            tempDpbMap.put(dpbName, propInfo);
        }

        aliases = Collections.unmodifiableMap(tempAliases);
        dpbMap = Collections.unmodifiableMap(tempDpbMap);
    }

    /**
     * Normalize the properties. This method resolves the aliases to their
     * original names. Also it restores the short syntax for the DPB parameters.
     *
     * @param props
     *         instance of {@link Properties} containing original properties.
     * @return instance of {@link Map} containing the normalized ones.
     * @throws SQLException
     *         if original properties reference the same DPB parameter using both alias and original name.
     */
    public static Map<String, String> normalize(Properties props) throws SQLException {
        Map<String, String> result = new HashMap<>(props.size());
        
        for (String propName : props.stringPropertyNames()) {
            PropertyInfo propInfo = aliases.get(propName);

            // check if alias is not used together with original property
            if (propInfo != null) {
                boolean hasDuplicate =
                        !(propName.equals(propInfo.dpbShortName) || propName.equals(propInfo.dpbName))
                                && (props.containsKey(propInfo.dpbName) ||
                                props.containsKey(propInfo.dpbShortName));

                if (hasDuplicate)
                    throw new FBSQLException("Specified properties contain " +
                            "reference to a DPB parameter under original and " +
                            "alias names: original name " + propInfo.dpbName +
                            ", alias : " + propName);
            }

            // if the specified property is not an alias, check the full list
            if (propInfo == null) {
                propInfo = getPropertyInfo(propName);
            }

            String propValue = props.getProperty(propName);
            if (propInfo != null) {
                result.put(propInfo.dpbName, propValue);
            } else {
                // add using original name if nothing is found
                result.put(propName, propValue);
            }
        }

        // database and type have special meaning and should not be set through properties
        result.keySet().removeAll(Arrays.asList(DATABASE_PROPERTY, TYPE_PROPERTY));

        return result;
    }

    public static String getCanonicalName(String propertyName) {
        PropertyInfo propInfo = aliases.get(propertyName);

        if (propInfo == null) {
            propInfo = getPropertyInfo(propertyName);
        }

        if (propInfo == null)
            return propertyName;

        return propInfo.dpbName;
    }

    /**
     * Get property information for the specified properties.
     *
     * @param props
     *         instance of {@link Properties}.
     * @return array of {@link DriverPropertyInfo} instances.
     */
    public static DriverPropertyInfo[] getDriverPropertyInfo(Properties props) {
        List<DriverPropertyInfo> result = new ArrayList<>();
        for (String propName : props.stringPropertyNames()) {
            Object propValue = props.getProperty(propName);

            PropertyInfo propInfo = aliases.get(propName);

            // if the specified property is not an alias, check
            // the full list
            if (propInfo == null) {
                propInfo = getPropertyInfo(propName);
            }

            DriverPropertyInfo driverPropInfo = new DriverPropertyInfo(
                    propName, propValue != null ? propValue.toString() : "");

            if (propInfo != null)
                driverPropInfo.description = propInfo.description;

            result.add(driverPropInfo);
        }

        return result.toArray(new DriverPropertyInfo[0]);
    }

    private static PropertyInfo getPropertyInfo(final String propName) {
        String propertyKey = propName.startsWith(DPB_PREFIX)
                ? propName
                : DPB_PREFIX + propName;

        return dpbMap.get(propertyKey);
    }

    private static ResourceBundle getDriverPropertyInfoResourceBundle() {
        try {
            return ResourceBundle.getBundle("driver_property_info");
        } catch (MissingResourceException ex) {
            return null;
        }
    }
}
