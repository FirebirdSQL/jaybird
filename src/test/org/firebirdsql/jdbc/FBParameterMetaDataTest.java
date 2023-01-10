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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This method tests correctness of {@link FBParameterMetaData} class.
 *
 * @author Nickolay Samofatov
 * @version 1.0
 */
class FBParameterMetaDataTest {

    //@formatter:off
    private static final String CREATE_EXECUTABLE_SP =
            "CREATE PROCEDURE sp_executable(" +
            "  int_field INTEGER," +
            "  varchar_field VARCHAR(35)" +
            ")" +
            "  RETURNS (res1 CHAR(2)) " +
            "AS " +
            "DECLARE VARIABLE dummy INTEGER;\n" +
            "BEGIN " +
            "  dummy = 1 + 1;\n" +
            "END";
    //@formatter:on

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase(
            CREATE_EXECUTABLE_SP);

    @Test
    void testParameterMetaData_callableStatement() throws SQLException {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             CallableStatement stmt = connection.prepareCall("{call sp_executable(?, ?)}")) {
            ParameterMetaData metaData = stmt.getParameterMetaData();

            assertEquals(2, metaData.getParameterCount(), "parameterCount");
            // Checks for parameter 1
            assertEquals(ParameterMetaData.parameterModeIn, metaData.getParameterMode(1), "1:parameterMode");
            assertEquals(Types.INTEGER, metaData.getParameterType(1), "1:parameterType");

            // Checks for parameter 2
            assertEquals(ParameterMetaData.parameterModeIn, metaData.getParameterMode(2), "2:parameterMode");
            assertEquals(Types.VARCHAR, metaData.getParameterType(2), "2:parameterType");
        }
    }
}
