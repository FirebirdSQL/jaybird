// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005-2006 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;

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
                throw new SQLException(
                        "Specified properties contain multiple references to a property: primary name %s, current name: %s"
                                .formatted(primaryName, propName), SQL_STATE_GENERAL_ERROR);
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
