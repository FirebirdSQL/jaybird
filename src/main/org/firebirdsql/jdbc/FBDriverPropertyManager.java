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

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ParameterBufferHelper;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.jdbc.FBConnectionProperties.DATABASE_PROPERTY;
import static org.firebirdsql.jdbc.FBConnectionProperties.TYPE_PROPERTY;

/**
 * Manager of the DPB properties.
 */
class FBDriverPropertyManager {

    private static final Logger log = LoggerFactory.getLogger(FBDriverPropertyManager.class);
    private static final String RES = "driver_property_info";

    private static ResourceBundle info;
    static {
        try {
            info = ResourceBundle.getBundle(RES);
        } catch (MissingResourceException ex) {
            info = null;
        }
    }

    /**
     * Container class for the driver properties.
     */
    private static class PropertyInfo {
        private final String alias;
        private final String dpbName;
        private final Integer dpbKey;
        private final String description;

        private final int hashCode;

        public PropertyInfo(String alias, String dpbName, Integer dpbKey, String description) {
            this.alias = alias;
            this.dpbName = dpbName;
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
        final Map<String, PropertyInfo> tempAliases = new HashMap<>();
        final Map<String, PropertyInfo> tempDpbMap = new HashMap<>();
        // process aliases and descriptions first
        if (info != null) {
            for (Enumeration<String> en = info.getKeys(); en.hasMoreElements(); ) {
                String key = en.nextElement();
                String value = info.getString(key);

                int hashIndex = value.indexOf('#');

                String dpbName;
                String description = "";
                if (hashIndex != -1) {
                    dpbName = value.substring(0, hashIndex).trim();
                    description = value.substring(hashIndex + 1).trim();
                } else
                    dpbName = value.trim();

                // skip incorrect mappings
                if (!dpbName.startsWith(ParameterBufferHelper.DPB_PREFIX))
                    continue;

                Integer dpbKey = ParameterBufferHelper.getDpbKey(dpbName);

                // skip unknown elements
                if (dpbKey == null)
                    continue;

                PropertyInfo propInfo = new PropertyInfo(key, dpbName,
                        dpbKey, description);

                tempAliases.put(propInfo.alias, propInfo);
                tempDpbMap.put(propInfo.dpbName, propInfo);
            }
        }

        // fill rest of the properties
        for (Map.Entry<String, Integer> entry : ParameterBufferHelper.getDpbMap().entrySet()) {
            String dpbName = entry.getKey();
            Integer dpbKey = entry.getValue();

            if (!dpbName.startsWith(ParameterBufferHelper.DPB_PREFIX))
                continue;

            if (tempDpbMap.containsKey(dpbName))
                continue;

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
        Map<String, String> tempProps = new HashMap<>();
        for (String propertyName : props.stringPropertyNames()) {
            tempProps.put(propertyName, props.getProperty(propertyName));
        }

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : tempProps.entrySet()) {
            String propName = entry.getKey();
            String propValue = entry.getValue();

            PropertyInfo propInfo = aliases.get(propName);

            // check if alias is not used together with original property
            if (propInfo != null) {
                String originalName = propInfo.dpbName;
                String shortName = propInfo.dpbName.substring(
                        ParameterBufferHelper.DPB_PREFIX.length());

                boolean hasDuplicate =
                        tempProps.keySet().contains(originalName)
                                || tempProps.keySet().contains(shortName);

                hasDuplicate &= !propName.equals(shortName);
                hasDuplicate &= !propName.equals(originalName);

                if (hasDuplicate)
                    throw new FBSQLException("Specified properties contain " +
                            "reference to a DPB parameter under original and " +
                            "alias names: original name " + propInfo.dpbName +
                            ", alias : " + propInfo.alias);
            }

            // if the specified property is not an alias, check
            // the full list
            if (propInfo == null) {
                String tempKey = propName;
                if (!tempKey.startsWith(ParameterBufferHelper.DPB_PREFIX))
                    tempKey = ParameterBufferHelper.DPB_PREFIX + tempKey;

                propInfo = dpbMap.get(tempKey);
            }

            if (propInfo != null) {
                result.put(propInfo.dpbName, propValue);
            } else {
                // add using original name if nothing is found
                result.put(propName, propValue);
            }
        }

        // database has special meaning and should not be set through properties
        result.keySet().removeAll(Collections.singletonList(DATABASE_PROPERTY));

        handleEncodings(result);

        return result;
    }

    public static String getCanonicalName(String propertyName) {
        PropertyInfo propInfo = aliases.get(propertyName);

        if (propInfo == null) {
            String tempKey = propertyName;
            if (!tempKey.startsWith(ParameterBufferHelper.DPB_PREFIX))
                tempKey = ParameterBufferHelper.DPB_PREFIX + tempKey;

            propInfo = dpbMap.get(tempKey);
        }

        if (propInfo == null)
            return propertyName;

        return propInfo.dpbName;
    }

    /**
     * Handle character encoding parameters. This method ensures that both
     * java encoding an client connection encodings are correctly set.
     * Additionally method handles the character translation stuff.
     *
     * @param info
     *         connection properties
     * @throws SQLException
     *         if the mapping path is specified but does not exist
     */
    public static void handleEncodings(Map<String, String> info) throws SQLException {
        final IEncodingFactory encodingFactory = EncodingFactory.getPlatformDefault();
        final String iscEncoding = info.get("isc_dpb_lc_ctype");
        final String localEncoding = info.get("isc_dpb_local_encoding");

        final EncodingDefinition encodingDefinition = encodingFactory.getEncodingDefinition(iscEncoding, localEncoding);
        if (encodingDefinition != null) {
            if (!encodingDefinition.isInformationOnly()) {
                info.put("isc_dpb_local_encoding", encodingDefinition.getJavaEncodingName());
            }

            info.put("isc_dpb_lc_ctype", encodingDefinition.getFirebirdEncodingName());
        } else if (iscEncoding != null) {
            log.warn("No EncodingDefinition for " + iscEncoding + " / " + localEncoding + " specifying " + iscEncoding
                    + " as connection encoding");
        } else if (localEncoding != null) {
            log.warn("No EncodingDefinition for " + localEncoding);
        }

        // ensure that we fail before any connection is obtained
        // in case when incorrect mapping path is specified 
        // (note, EncodingFactory.getEncoding(String, String) throws exception)
        String mappingPath = info.get("isc_dpb_mapping_path");
        if (encodingDefinition != null && mappingPath != null) {
            encodingDefinition.getEncoding().withTranslation(encodingFactory.getCharacterTranslator(mappingPath));
        }
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
                String tempKey = propName;
                if (!tempKey.startsWith(ParameterBufferHelper.DPB_PREFIX))
                    tempKey = ParameterBufferHelper.DPB_PREFIX + tempKey;

                propInfo = dpbMap.get(tempKey);
            }

            DriverPropertyInfo driverPropInfo = new DriverPropertyInfo(
                    propName, propValue != null ? propValue.toString() : "");

            if (propInfo != null)
                driverPropInfo.description = propInfo.description;

            result.add(driverPropInfo);
        }

        return result.toArray(new DriverPropertyInfo[0]);
    }
}
