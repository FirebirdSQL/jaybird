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

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * See also Java 7 companion test {@link TimeZoneBindLegacyTest}.
 */
public class TimeZoneBindTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.noDatabase();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void requireTimeZoneSupport() throws Exception {
        assumeTrue("Test requires time zone support (Firebird 4+)", getDefaultSupportInfo().supportsTimeZones());
        usesDatabase.createDefaultDatabase();
    }

    @Test
    public void testCurrentTimestamp_noBind() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_TIMESTAMP from RDB$DATABASE")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected TIMESTAMP (WITHOUT TIME ZONE)", Types.TIMESTAMP_WITH_TIMEZONE, rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            assertThat(rs.getObject(1), instanceOf(OffsetDateTime.class));
        }
    }

    @Test
    public void testCurrentTimestamp_emptyBind() throws Exception {
        checkForBindValue("");
    }

    @Test
    public void testCurrentTimestamp_nativeBind() throws Exception {
        checkForBindValue("native");
    }

    @Test
    public void testCurrentTimestamp_NaTIVEBind() throws Exception {
        checkForBindValue("NaTIVE");
    }

    @Test
    public void testCurrentTimestamp_invalidBind() throws Exception {
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_time_zone_bind),
                fbMessageStartsWith(ISCConstants.isc_time_zone_bind, "doesnotexist")));

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("timeZoneBind", "doesnotexist");
        //noinspection EmptyTryBlock
        try (Connection ignore = DriverManager.getConnection(getUrl(), props)) {
            // ensure connection is closed if this doesn't fail
        }
    }

    @Test
    public void verifySessionReset_retainsSetting() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("timeZoneBind", "legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {
            stmt.execute("alter session reset");

            verifyLegacyTimestamp(stmt);
        }
    }

    @Test
    public void verifySessionReset_afterExplicitChange() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("timeZoneBind", "legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {

            verifyLegacyTimestamp(stmt);

            stmt.execute("set time zone bind native");

            verifyTimestampWithTimezone(stmt);

            stmt.execute("alter session reset");

            verifyLegacyTimestamp(stmt);
        }
    }

    private void checkForBindValue(String bindValue) throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("timeZoneBind", bindValue);
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (Statement stmt = connection.createStatement()) {
                verifyTimestampWithTimezone(stmt);
            }
        }
    }

    private void verifyLegacyTimestamp(Statement stmt) throws SQLException {
        verifyTimestampType(stmt, JDBCType.TIMESTAMP, Timestamp.class);
    }

    private void verifyTimestampWithTimezone(Statement stmt) throws SQLException {
        verifyTimestampType(stmt, JDBCType.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime.class);
    }

    private void verifyTimestampType(Statement stmt, JDBCType expectedJdbcType, Class<?> expectedType) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("select CURRENT_TIMESTAMP from RDB$DATABASE")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Expected " + expectedJdbcType, 
                    expectedJdbcType.getVendorTypeNumber().intValue(), rsmd.getColumnType(1));
            assertTrue("Expected a row", rs.next());
            assertThat(rs.getObject(1), instanceOf(expectedType));
        }
    }

}
