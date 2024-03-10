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
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SessionTimeZoneTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void withTimeZone_appliesServerDefaultTimeZone() throws Exception {
        requireTimeZoneSupport();
        checkForTimeZone("server", "timestamp'2019-03-23 14:05:12.12' at local", "2019-03-23 14:05:12.12",
                "2019-03-23 14:05:12.1200");
    }

    @Test
    void withoutTimeZone_appliesServerDefaultTimeZone() throws Exception {
        long expectedMillis = getMillis("2019-03-23T14:05:12.120", TimeZone.getDefault());
        checkForWithoutTimeZone("server", "timestamp'2019-03-23 14:05:12.12'", expectedMillis);
    }

    @Test
    void withTimeZone_appliesJvmDefaultTimeZone() throws Exception {
        requireTimeZoneSupport();
        // NOTE: Can potentially fail if Java default TimeZone id does not exist in Firebird
        checkForTimeZone(null, "timestamp'2019-03-23 14:05:12.12' at local", "2019-03-23 14:05:12.12",
                "2019-03-23 14:05:12.1200 " + TimeZone.getDefault().getID());
    }

    @Test
    void withoutTimeZone_appliesJvmDefaultTimeZone() throws Exception {
        long expectedMillis = getMillis("2019-03-23T14:05:12.120", TimeZone.getDefault());
        checkForWithoutTimeZone(null, "timestamp'2019-03-23 14:05:12.12'", expectedMillis);
    }

    @Test
    void withTimeZone_appliesSpecificTimeZone() throws Exception {
        requireTimeZoneSupport();
        long expectedMillis = getMillis("2019-03-23T14:05:12.120", TimeZone.getTimeZone("America/New_York"));
        checkForTimeZone("America/New_York", "timestamp'2019-03-23 14:05:12.12' at local", expectedMillis,
                "2019-03-23 14:05:12.1200 America/New_York");
    }

    @Test
    void withoutTimeZone_appliesSpecificTimeZone() throws Exception {
        long expectedMillis = getMillis("2019-03-23T14:05:12.120", TimeZone.getTimeZone("America/New_York"));
        checkForWithoutTimeZone("America/New_York", "timestamp'2019-03-23 14:05:12.12'", expectedMillis);
    }

    @Test
    void withTimeZone_appliesSpecificTimeZone_differentValue() throws Exception {
        requireTimeZoneSupport();
        long expectedMillis = getMillis("2019-03-23T14:05:12.120", TimeZone.getTimeZone("Europe/Amsterdam"));
        checkForTimeZone("America/Buenos_Aires", "timestamp'2019-03-23 14:05:12.12 Europe/Amsterdam'",
                expectedMillis, "2019-03-23 14:05:12.1200 Europe/Amsterdam");
    }

    @Test
    void errorOnInvalidTimeZoneName() {
        requireTimeZoneSupport();
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sessionTimeZone", "does_not_exist");

        SQLException exception = assertThrows(SQLException.class, () -> {
            //noinspection EmptyTryBlock
            try (Connection ignore = DriverManager.getConnection(getUrl(), props)) {
                // ensure connection is closed if this does not fail as expected
            }
        });
        assertThat(exception, allOf(
                errorCodeEquals(ISCConstants.isc_invalid_timezone_region),
                fbMessageStartsWith(ISCConstants.isc_invalid_timezone_region, "does_not_exist")));
    }

    /**
     * Rationale: Firebird expects offset name {@code [+-]HH:MM}, while Java expects {@code GMT[+-]HH:MM}.
     */
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            sessionTimeZone, expectedFirebirdZoneName
            +05:17,          +05:17
            GMT+05:17,       +05:17
            -08:17,          -08:17
            GMT-08:17,       -08:17
            """)
    void verifyOffsetTimeZoneBehaviour(String sessionTimeZone, String expectedFirebirdZoneName) throws Exception {
        requireTimeZoneSupport();
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.sessionTimeZone, sessionTimeZone);
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery("""
                     select
                       rdb$get_context('SYSTEM', 'SESSION_TIMEZONE') as TZ_NAME,
                       localtimestamp as LOCAL_TS
                     from RDB$DATABASE""")) {
            assertTrue(rs.next(), "expected a row");
            assertEquals(expectedFirebirdZoneName, rs.getString(1));
            Timestamp timestampValue = rs.getTimestamp(2);
            assertEquals((double) System.currentTimeMillis(), timestampValue.getTime(), 1000,
                    "Unexpected value with offset time zone");

            assertNotEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES),
                    rs.getObject(2, LocalDateTime.class).truncatedTo(ChronoUnit.MINUTES),
                    "sanity check: expected a value not equal to the current default zone");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void checkForTimeZone(String zoneName, String timeValue, String expectedLocal, String expectedZonedString)
            throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        if (zoneName != null) {
            props.setProperty("sessionTimeZone", zoneName);
        }
        props.setProperty("dataTypeBind", "timestamp with time zone to legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("select " + timeValue + ", "
                    + "cast(" + timeValue + " as varchar(100)) from rdb$database")) {
                assertTrue(rs.next(), "expected a row");
                Timestamp timestamp = rs.getTimestamp(1);
                assertEquals(expectedLocal, timestamp.toString());
                // Need to take into account server-defined zone which we can't know in advance
                assertThat(rs.getString(2), startsWith(expectedZonedString));
            }
        }
    }

    private void checkForTimeZone(String zoneName, String timeValue, long expectedMillis, String expectedZonedString)
            throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        if (zoneName != null) {
            props.setProperty("sessionTimeZone", zoneName);
        }
        props.setProperty("dataTypeBind", "timestamp with time zone to legacy");
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("select " + timeValue + ", "
                    + "cast(" + timeValue + " as varchar(100)) from rdb$database")) {
                assertTrue(rs.next(), "expected a row");
                Timestamp timestamp = rs.getTimestamp(1);
                assertEquals(expectedMillis, timestamp.getTime());
                // Need to take into account server-defined zone which we can't know in advance
                assertThat(rs.getString(2), startsWith(expectedZonedString));
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void checkForWithoutTimeZone(String zoneName, String timeValue, long expectedMillis)
            throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        if (zoneName != null) {
            props.setProperty("sessionTimeZone", zoneName);
        }
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("select " + timeValue + " from rdb$database")) {
                assertTrue(rs.next(), "expected a row");
                Timestamp timestamp = rs.getTimestamp(1);
                assertEquals(expectedMillis, timestamp.getTime());
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static long getMillis(String localDateTimeString, TimeZone zone) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(zone);
        Date date = sdf.parse(localDateTimeString);
        return date.getTime();
    }

    private static void requireTimeZoneSupport() {
        assumeTrue(getDefaultSupportInfo().supportsTimeZones(), "Requires time zone support");
    }

}
