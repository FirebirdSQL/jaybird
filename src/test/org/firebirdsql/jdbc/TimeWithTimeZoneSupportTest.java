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

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@code TIME WITH TIME ZONE} support, which is only available in Firebird 4 and Jaybird for Java 8 or higher.
 * <p>
 * This test requires support for the EXTENDED TIME WITH TIME ZONE type, which is only available in 4.0.0.1795 or
 * higher.
 * </p>
 *
 * @author Mark Rotteveel
 */
class TimeWithTimeZoneSupportTest {

    @RegisterExtension
    // NOTE: For native tests this also requires use of a Firebird 4 client library
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsTimeZones, "Test requires TIME WITH TIME ZONE support on server")
            .build();

    private static final String CREATE_TABLE =
            "create table withtimetz ("
            + "  id integer,"
            + "  timetz TIME WITH TIME ZONE"
            + ")";
    private static final String INSERT = "insert into withtimetz(id, timetz) values (?, ?)";
    private static final String SELECT = "select id, timetz from withtimetz";
    private static final String SELECT_CONDITION = SELECT + " where timetz = ?";
    private static final String[] TEST_DATA = {
            "insert into withtimetz(id, timetz) values (1, time'13:25:32.1234+01:00')",
            // Date sensitive
            "insert into withtimetz(id, timetz) values (2, time'13:25:32.1235 Europe/Amsterdam')",
            "insert into withtimetz(id, timetz) values (3, time'23:59:59.9999+13:59')",
            "insert into withtimetz(id, timetz) values (3, null)",
    };

    private static final OffsetTime VALUE_1 = OffsetTime.parse("13:25:32.1234+01:00");
    private static final ZonedDateTime VALUE_1_ZONED =
            rebaseOnCurrentDate(ZonedDateTime.parse("2020-01-01T13:25:32.1234+01:00"));
    private static final OffsetTime VALUE_2 =  OffsetTime.parse("13:25:32.1235+01:00");
    private static final ZonedDateTime VALUE_2_ZONED =
            rebaseOnCurrentDate(ZonedDateTime.parse("2020-01-01T13:25:32.1235+01:00[Europe/Amsterdam]"));
    private static final OffsetTime VALUE_3 = OffsetTime.parse("23:59:59.9999+13:59");
    private static final ZonedDateTime VALUE_3_ZONED =
            rebaseOnCurrentDate(ZonedDateTime.parse("2020-01-01T23:59:59.9999+13:59"));
    private static final Collection<String> BINDS = unmodifiableCollection(asList(null, "TIME ZONE TO EXTENDED"));

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase(
            Stream.concat(Stream.of(CREATE_TABLE, "COMMIT WORK"), Stream.of(TEST_DATA)).collect(Collectors.toList()));

    @Test
    void testSimpleSelect() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT + " order by id")) {
                assertTrue(rs.next(), "Expected row 1");
                assertEquals(VALUE_1, rs.getObject(2), "Unexpected value row 1");

                assertTrue(rs.next(), "Expected row 2");
                assertEquals(VALUE_2, rs.getObject(2), "Unexpected value row 2");

                assertTrue(rs.next(), "Expected row 3");
                assertEquals(VALUE_3, rs.getObject(2), "Unexpected value row 3");

                assertTrue(rs.next(), "Expected row 4");
                assertNull(rs.getObject(2), "Unexpected value for row 4");

                assertFalse(rs.next(), "Expected no more rows");
            }
        }
    }

    @Test
    void testSimpleSelect_ZonedDateTime() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT + " order by id")) {
                assertTrue(rs.next(), "Expected row 1");
                assertEquals(VALUE_1_ZONED, rs.getObject(2, ZonedDateTime.class), "Unexpected value row 1");

                assertTrue(rs.next(), "Expected row 2");
                assertEquals(VALUE_2_ZONED, rs.getObject(2, ZonedDateTime.class), "Unexpected value row 2");

                assertTrue(rs.next(), "Expected row 3");
                assertEquals(VALUE_3_ZONED, rs.getObject(2, ZonedDateTime.class), "Unexpected value row 3");

                assertTrue(rs.next(), "Expected row 4");
                assertNull(rs.getObject(2), "Unexpected value for row 4");

                assertFalse(rs.next(), "Expected no more rows");
            }
        }
    }

    /**
     * Tests if the ResultSetMetaData contains the right information on TIME WITH TIME ZONE columns.
     */
    @Test
    void testSimpleSelect_ResultSetMetaData() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT)) {
                final ResultSetMetaData rsmd = rs.getMetaData();
                assertEquals(Types.TIME_WITH_TIMEZONE, rsmd.getColumnType(2),
                        "Unexpected type for TIME WITH TIME ZONE column");
                assertEquals("TIME WITH TIME ZONE", rsmd.getColumnTypeName(2),
                        "Unexpected type name for TIME WITH TIME ZONE column");
                assertEquals(19, rsmd.getPrecision(2), "Unexpected precision for TIME WITH TIME ZONE column");
                // Not testing other values
            }
        }
    }

    @Test
    void testParameterizedInsert() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("delete from withtimetz where id in (5, 6, 7)");
                }
                try (PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
                    OffsetTime value5 = OffsetTime.parse("12:34:56.7891+03:30");
                    OffsetTime value6 = OffsetTime.parse("00:00:00-08:15");

                    pstmt.setInt(1, 5);
                    pstmt.setObject(2, value5);
                    pstmt.execute();

                    pstmt.setInt(1, 6);
                    pstmt.setObject(2, value6);
                    pstmt.execute();

                    pstmt.setInt(1, 7);
                    pstmt.setObject(2, null);
                    pstmt.execute();

                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery(SELECT + " where id > 4 order by id")) {
                        assertTrue(rs.next(), "Expected row 1");
                        assertEquals(value5, rs.getObject(2, OffsetTime.class));

                        assertTrue(rs.next(), "Expected row 2");
                        assertEquals(value6, rs.getObject(2, OffsetTime.class));

                        assertTrue(rs.next(), "Expected row 3");
                        assertNull(rs.getObject(2, OffsetTime.class));

                        assertFalse(rs.next(), "Expected no more rows");
                    }
                }
            }
        }
    }

    @Test
    void testParameterizedInsert_ZonedDateTime() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("delete from withtimetz where id in (5, 6, 7, 8, 9)");
                }
                try (PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
                    ZonedDateTime value5Zoned = ZonedDateTime.parse("2020-01-01T12:34:56.7891+03:30");
                    ZonedDateTime value6Zoned = ZonedDateTime.parse("2020-01-01T00:00:00-08:15");
                    ZonedDateTime value8Zoned = ZonedDateTime.parse("2020-01-01T07:58:01+01:00[Europe/Amsterdam]");
                    ZonedDateTime value9Zoned = ZonedDateTime.parse("2020-06-02T07:58:01+02:00[Europe/Amsterdam]");

                    pstmt.setInt(1, 5);
                    pstmt.setObject(2, value5Zoned);
                    pstmt.addBatch();

                    pstmt.setInt(1, 6);
                    pstmt.setObject(2, value6Zoned);
                    pstmt.addBatch();

                    pstmt.setInt(1, 7);
                    pstmt.setObject(2, null);
                    pstmt.addBatch();

                    pstmt.setInt(1, 8);
                    pstmt.setObject(2, value8Zoned);
                    pstmt.addBatch();

                    pstmt.setInt(1, 9);
                    pstmt.setObject(2, value9Zoned);
                    pstmt.addBatch();

                    pstmt.executeBatch();

                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery(SELECT + " where id > 4 order by id")) {
                        assertTrue(rs.next(), "Expected row 1");
                        assertEquals(rebaseOnCurrentDate(value5Zoned), rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 2");
                        assertEquals(rebaseOnCurrentDate(value6Zoned), rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 3");
                        assertNull(rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 4");
                        assertEquals(rebaseOnCurrentDate(value8Zoned), rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 5");
                        assertEquals(rebaseOnCurrentDate(value9Zoned), rs.getObject(2, ZonedDateTime.class));

                        assertFalse(rs.next(), "Expected no more rows");
                    }
                }
            }
        }
    }

    /**
     * Tests if the ParameterMetaData contains the right information on TIME WITH TIME ZONE columns.
     */
    @Test
    void testParametrizedInsert_ParameterMetaData() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props);
                 PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
                final ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
                assertEquals(Types.TIME_WITH_TIMEZONE, parameterMetaData.getParameterType(2),
                        "Unexpected type for TIME WITH TIME ZONE column");
                assertEquals("TIME WITH TIME ZONE", parameterMetaData.getParameterTypeName(2),
                        "Unexpected type name for TIME WITH TIME ZONE column");
                assertEquals(19, parameterMetaData.getPrecision(2),
                        "Unexpected precision for TIME WITH TIME ZONE column");
                // Not testing other values
            }
        }
    }

    @Test
    void testSelectCondition() throws Exception {
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props);
                 PreparedStatement pstmt = connection.prepareStatement(SELECT_CONDITION + " order by id")) {
                // Should match id 1
                pstmt.setObject(1, VALUE_1);

                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue(rs.next(), "Expected row 1");
                    assertEquals(1, rs.getInt(1), "Expected id 1");
                    assertEquals(VALUE_1, rs.getObject(2), "Unexpected value for row 1");

                    assertFalse(rs.next(), "Expected no more rows");
                }
            }
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for TIME WITH TIME ZONE).
     */
    @Test
    void testMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
                DatabaseMetaData dbmd = connection.getMetaData();

                try (ResultSet rs = dbmd.getTypeInfo()) {
                    boolean foundTimeWithTimeZoneType = false;
                    while (rs.next()) {
                        if (!"TIME WITH TIME ZONE".equals(rs.getString("TYPE_NAME"))) {
                            continue;
                        }
                        foundTimeWithTimeZoneType = true;
                        assertEquals(Types.TIME_WITH_TIMEZONE, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                        assertEquals(19, rs.getInt("PRECISION"), "Unexpected PRECISION");
                        assertEquals(DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                        assertFalse(rs.getBoolean("CASE_SENSITIVE"), "Unexpected CASE_SENSITIVE");
                        assertEquals(DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"), "Unexpected SEARCHABLE");
                        assertTrue(rs.getBoolean("UNSIGNED_ATTRIBUTE"), "Unexpected UNSIGNED_ATTRIBUTE");
                        assertTrue(rs.getBoolean("FIXED_PREC_SCALE"), "Unexpected FIXED_PREC_SCALE");
                        assertFalse(rs.getBoolean("AUTO_INCREMENT"), "Unexpected AUTO_INCREMENT");
                        assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                        // Not testing other values
                    }
                    assertTrue(foundTimeWithTimeZoneType, "Expected to find TIME WITH TIME ZONE type in typeInfo");
                }
            }
        }
    }

    /**
     * Test {@link FBDatabaseMetaData#getColumns(String, String, String, String)} for a TIME WITH TIME ZONE column.
     */
    @Test
    void testMetaData_getColumns() throws Exception {
        // TODO Consider moving to TestFBDatabaseMetaDataColumns
        for (String dataTypeBind : BINDS) {
            Properties props = getDefaultPropertiesForConnection();
            if (dataTypeBind != null) {
                props.setProperty("dataTypeBind", dataTypeBind);
            }
            try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
                DatabaseMetaData dbmd = connection.getMetaData();
                try (ResultSet rs = dbmd.getColumns(null, null, "WITHTIMETZ", "TIMETZ")) {
                    assertTrue(rs.next(), "Expected a row");
                    assertEquals("TIMETZ", rs.getString("COLUMN_NAME"), "Unexpected COLUMN_NAME");
                    assertEquals(Types.TIME_WITH_TIMEZONE, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                    assertEquals("TIME WITH TIME ZONE", rs.getString("TYPE_NAME"), "Unexpected TYPE_NAME");
                    assertEquals(19, rs.getInt("COLUMN_SIZE"), "Unexpected COLUMN_SIZE");
                    assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                    assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                    assertEquals("NO", rs.getString("IS_AUTOINCREMENT"), "Unexpected IS_AUTOINCREMENT");

                    assertFalse(rs.next(), "Expected no second row");
                }
            }
        }
    }

    private static ZonedDateTime rebaseOnCurrentDate(ZonedDateTime zonedDateTime) {
        ZoneId zoneId = zonedDateTime.getZone();
        LocalDate currentDateInZone = ZonedDateTime.now(zoneId).toLocalDate();
        return zonedDateTime.with(TemporalAdjusters.ofDateAdjuster(date -> currentDateInZone));
    }
}
