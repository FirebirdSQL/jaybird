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
import java.util.Properties;
import java.util.TimeZone;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class SessionTimeZoneTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.noDatabase();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void requireTimeZones() throws Exception {
        assumeTrue("Requires time zone support", getDefaultSupportInfo().supportsTimeZones());
        usesDatabase.createDefaultDatabase();
    }

    @Test
    public void appliesServerDefaultTimeZone() throws Exception {
        checkForTimeZone("server", "timestamp'2019-03-23 14:05:12.12' at local", "2019-03-23 14:05:12.12",
                "2019-03-23 14:05:12.12");
    }

    @Test
    public void appliesJvmDefaultTimeZone() throws Exception {
        checkForTimeZone(null, "timestamp'2019-03-23 14:05:12.12' at local", "2019-03-23 14:05:12.12",
                "2019-03-23 14:05:12.1200 " + TimeZone.getDefault().getID());
    }

    @Test
    public void appliesSpecificTimeZone() throws Exception {
        checkForTimeZone("America/New_York", "timestamp'2019-03-23 14:05:12.12' at local", "2019-03-23 14:05:12.12",
                "2019-03-23 14:05:12.1200 America/New_York");
    }

    @Test
    public void appliesSpecificTimeZone_differentValue() throws Exception {
        checkForTimeZone("America/New_York", "timestamp'2019-03-23 14:05:12.12 Europe/Amsterdam'",
                "2019-03-23 09:05:12.12", "2019-03-23 14:05:12.1200 Europe/Amsterdam");
    }

    @Test
    public void errorOnInvalidTimeZoneName() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sessionTimeZone", "does_not_exist");

        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_invalid_timezone_region),
                fbMessageStartsWith(ISCConstants.isc_invalid_timezone_region, "does_not_exist")));

        //noinspection EmptyTryBlock
        try (Connection ignore = DriverManager.getConnection(getUrl(), props)) {
            // ensure connection is closed if this does not fail as expected
        }
    }

    private void checkForTimeZone(String zoneName, String timeValue, String expectedLocal, String expectedZonedString)
            throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        if (zoneName != null) {
            props.setProperty("sessionTimeZone", zoneName);
        }
        try (Connection connection = DriverManager.getConnection(getUrl(), props);
             Statement stmt = connection.createStatement()) {
            // TODO Replace with connection property once implemented
            stmt.execute("set time zone bind legacy");

            try (ResultSet rs = stmt.executeQuery(
                    "select " + timeValue + ", "
                            + "cast(" + timeValue + " as varchar(100)) from rdb$database")) {
                assertTrue("expected a row", rs.next());
                Timestamp timestamp = rs.getTimestamp(1);
                // TODO Need to revise expectedLocal values if we take time zone into account for deriving Time/Timestamp values
                assertEquals(expectedLocal, timestamp.toString());
                // Need to take into account server-defined zone which we can't know in advance
                assertThat(rs.getString(2), startsWith(expectedZonedString));
            }
        }
    }


}
