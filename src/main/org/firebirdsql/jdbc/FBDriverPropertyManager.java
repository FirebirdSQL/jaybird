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

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.*;

/**
 * Manager of the DPB properties.
 */
final class FBDriverPropertyManager {

    private FBDriverPropertyManager() {
        // no instances
    }

    /**
     * Normalize the properties. This method resolves the aliases to their original names.
     *
     * @param props
     *         instance of {@link Properties} containing original properties.
     * @return instance of {@link Map} containing the normalized ones.
     * @throws SQLException
     *         if original properties reference the same connection property multiple times.
     */
    static Map<String, String> normalize(Map<String, String> props) throws SQLException {
        Map<String, String> result = new HashMap<>(props.size());
        ConnectionPropertyRegistry propertyRegistry = ConnectionPropertyRegistry.getInstance();

        for (Map.Entry<String, String> entry : props.entrySet()) {
            String propName = entry.getKey();
            ConnectionProperty property = propertyRegistry.getByName(propName);

            String primaryName = property != null ? property.name() : propName;
            boolean hasDuplicate = property != null && result.containsKey(primaryName);
            if (hasDuplicate) {
                throw new FBSQLException("Specified properties contain multiple references to a property: "
                        + "primary name " + primaryName + ", current name: " + propName);
            }
            String propValue = entry.getValue();
            result.put(primaryName, propValue);
        }
        return result;
    }

    /**
     * Get property information for the specified properties.
     *
     * @param props
     *         instance of {@link Properties}.
     * @return array of {@link DriverPropertyInfo} instances.
     */
    static DriverPropertyInfo[] getDriverPropertyInfo(Properties props) {
        ConnectionPropertyRegistry propertyRegistry = ConnectionPropertyRegistry.getInstance();

        List<DriverPropertyInfo> result = new ArrayList<>(props.size());
        for (String propName : props.stringPropertyNames()) {
            Object propValue = props.getProperty(propName);

            ConnectionProperty propInfo = propertyRegistry.getByName(propName);

            DriverPropertyInfo driverPropInfo =
                    new DriverPropertyInfo(propName, propValue != null ? propValue.toString() : "");

            if (propInfo != null && !propInfo.choices().isEmpty()) {
                driverPropInfo.choices = propInfo.choices().toArray(new String[0]);
            }

            result.add(driverPropInfo);
        }

        return result.toArray(new DriverPropertyInfo[0]);
    }

}
