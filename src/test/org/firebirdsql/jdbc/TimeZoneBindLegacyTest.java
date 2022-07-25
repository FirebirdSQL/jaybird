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

import static org.firebirdsql.common.FBTestProperties.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * See also companion test {@link TimeZoneBindTest}.
 */
class TimeZoneBindLegacyTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    /**
     * Checks if {@code CURRENT_TIMESTAMP} returns a timestamp without time zone when time zone bind is set to legacy.
     * <p>
     * NOTE: We are not checking the Firebird version as this property will be ignored on earlier versions, so the
     * test should work on earlier versions as well.
     * </p>
     */
    @Test
    void testCurrentTimestamp_legacyBind() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("dataTypeBind", "timestamp with time zone to legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_TIMESTAMP from RDB$DATABASE")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals(Types.TIMESTAMP, rsmd.getColumnType(1), "Expected TIMESTAMP (WITHOUT TIME ZONE)");
            assertTrue(rs.next(), "Expected a row");
            assertThat(rs.getObject(1), instanceOf(Timestamp.class));
        }
    }

    /**
     * Checks if {@code CURRENT_TIME} returns a time without time zone when time zone bind is set to legacy.
     * <p>
     * NOTE: We are not checking the Firebird version as this property will be ignored on earlier versions, so the
     * test should work on earlier versions as well.
     * </p>
     */
    @Test
    void testCurrentTime_legacyBind() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("dataTypeBind", "time with time zone to legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_TIME from RDB$DATABASE")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals(Types.TIME, rsmd.getColumnType(1), "Expected TIME (WITHOUT TIME ZONE)");
            assertTrue(rs.next(), "Expected a row");
            assertThat(rs.getObject(1), instanceOf(Time.class));
        }
    }

    @Test
    void testRoundTrip_legacyBind() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsTimeZones(), "Requires time zone support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("dataTypeBind", "time with time zone to legacy;timestamp with time zone to legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("create table testtbl ("
                        + "id integer, "
                        + "timeval time with time zone, "
                        + "timestampval timestamp with time zone"
                        + ")");
            }

            long timeMillis = System.currentTimeMillis();
            Timestamp timestampVal = new Timestamp(timeMillis);
            Time timeVal = new Time(timeMillis);

            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into testtbl(id, timeval, timestampval) values (?, ?, ?)")) {
                pstmt.setInt(1, 1);
                pstmt.setTime(2, timeVal);
                pstmt.setTimestamp(3, timestampVal);

                pstmt.execute();
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select timeval, timestampval, cast(timestampval as varchar(50)) from testtbl where id = 1")) {
                assertTrue(rs.next(), "expected a row");
                System.out.println(rs.getString(3));
                // Using toString to avoid 'date' differences
                assertEquals(timeVal.toString(), rs.getTime(1).toString(), "TIME");
                assertEquals(timestampVal, rs.getTimestamp(2), "TIMESTAMP");
            }
        }

    }
}
