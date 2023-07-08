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

import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.OptionalInt;

import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * Helper to retrieve option configuration information from a Firebird server.
 * <p>
 * This only works for Firebird 4.0 or higher.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5.0.2
 */
public final class ConfigHelper {

    private ConfigHelper() {
        // no instances
    }

    /**
     * Retrieves the value of configuration option {@code configOption} from the {@code RDB$CONFIG} table.
     *
     * @param connection
     *         connection
     * @param configOption
     *         configuration option name
     * @return value of {@code configOption}, or {@code null} if it does not exist (NOTE: {@code null} can also be
     * a proper value of a configuration option)
     * @throws SQLException
     *         for errors executing the query
     * @throws UnsupportedOperationException
     *         when {@code connection} does not support {@code RDB$CONFIG} (i.e. Firebird 3.0 or earlier)
     */
    public static String getConfigValue(Connection connection, String configOption) throws SQLException {
        var supportInfo = FirebirdSupportInfo.supportInfoFor(connection);
        if (!supportInfo.supportsRDB$CONFIG()) {
            throw new UnsupportedOperationException("This Firebird server does not support RDB$CONFIG");
        }
        try (var pstmt = connection.prepareStatement(
                "select RDB$CONFIG_VALUE from RDB$CONFIG where RDB$CONFIG_NAME = ?")) {
            pstmt.setString(1, configOption);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    /**
     * Retrieves the value of configuration option {@code configOption} from the {@code RDB$CONFIG} table as an optional
     * int.
     *
     * @param connection
     *         connection
     * @param configOption
     *         configuration option name
     * @return value of {@code configOption} if an int, or {@code empty} if it does not exist or had a blank value
     * @throws SQLException
     *         for errors executing the query
     * @throws NumberFormatException
     *         if {@code configOption} has a value, but cannot be parsed to {@code int}
     * @throws UnsupportedOperationException
     *         when {@code connection} does not support {@code RDB$CONFIG} (i.e. Firebird 3.0 or earlier)
     */
    public static OptionalInt getIntConfigValue(Connection connection, String configOption) throws SQLException {
        String configValue = trimToNull(getConfigValue(connection, configOption));
        if (configValue == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(configValue));
    }

}
