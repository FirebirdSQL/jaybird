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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.Assert.assertEquals;

/**
 * This method tests correctness of {@link FBParameterMetaData} class.
 *
 * @author <a href="mailto:skidder@users.sourceforge.net">Nickolay Samofatov</a>
 * @version 1.0
 */
public class FBParameterMetaDataTest extends FBJUnit4TestBase {

    //@formatter:off
    public static String CREATE_EXECUTABLE_SP =
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

    @Test
    public void testParameterMetaData_callableStatement() throws SQLException {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");

        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            DdlHelper.executeDDL(connection, CREATE_EXECUTABLE_SP);

            CallableStatement stmt = connection.prepareCall("{call sp_executable(?, ?)}");
            try {
                ParameterMetaData metaData = stmt.getParameterMetaData();

                assertEquals("parameterCount", 2, metaData.getParameterCount());
                // Checks for parameter 1
                assertEquals("1:parameterMode", ParameterMetaData.parameterModeIn, metaData.getParameterMode(1));
                assertEquals("1:parameterType", Types.INTEGER, metaData.getParameterType(1));

                // Checks for parameter 2
                assertEquals("2:parameterMode", ParameterMetaData.parameterModeIn, metaData.getParameterMode(2));
                assertEquals("2:parameterType", Types.VARCHAR, metaData.getParameterType(2));

            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
    }
}
