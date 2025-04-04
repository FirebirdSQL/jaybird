// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@code TIMESTAMP WITH TIME ZONE} support, which is only available in Firebird 4 and Jaybird for Java 8 or
 * higher.
 * <p>
 * This test requires support for the EXTENDED TIME WITH TIME ZONE type, which is only available in 4.0.0.1795 or
 * higher.
 * </p>
 *
 * @author Mark Rotteveel
 */
class TimestampWithTimeZoneSupportTest {

    @RegisterExtension
    // NOTE: For native tests this also requires use of a Firebird 4 client library
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsTimeZones, "Test requires TIMESTAMP WITH TIME ZONE support on server")
            .build();

    private static final String CREATE_TABLE =
            "create table withtimestamptz ("
            + "  id integer,"
            + "  timestamptz TIMESTAMP WITH TIME ZONE"
            + ")";
    private static final String INSERT = "insert into withtimestamptz(id, timestamptz) values (?, ?)";
    private static final String SELECT = "select id, timestamptz from withtimestamptz";
    private static final String SELECT_CONDITION = SELECT + " where timestamptz = ?";
    private static final String[] TEST_DATA = {
            "insert into withtimestamptz(id, timestamptz) values (1, timestamp'2019-03-09 13:25:32.1234+01:00')",
            "insert into withtimestamptz(id, timestamptz) values (2, timestamp'2019-03-09 13:25:32.1234 Europe/Amsterdam')",
            "insert into withtimestamptz(id, timestamptz) values (3, timestamp'2019-12-31 23:59:59.9999+13:59')",
            "insert into withtimestamptz(id, timestamptz) values (3, null)",
    };

    private static final OffsetDateTime VALUE_1 = OffsetDateTime.parse("2019-03-09T13:25:32.1234+01:00");
    private static final ZonedDateTime VALUE_1_ZONED = ZonedDateTime.parse("2019-03-09T13:25:32.1234+01:00");
    private static final OffsetDateTime VALUE_2 = VALUE_1;
    private static final ZonedDateTime VALUE_2_ZONED =
            ZonedDateTime.parse("2019-03-09T13:25:32.1234+01:00[Europe/Amsterdam]");
    private static final OffsetDateTime VALUE_3 = OffsetDateTime.parse("2019-12-31T23:59:59.9999+13:59");
    private static final ZonedDateTime VALUE_3_ZONED = ZonedDateTime.parse("2019-12-31T23:59:59.9999+13:59");
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
     * Tests if the ResultSetMetaData contains the right information on TIMESTAMP WITH TIME ZONE columns.
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
                assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, rsmd.getColumnType(2),
                        "Unexpected type for TIMESTAMP WITH TIME ZONE column");
                assertEquals("TIMESTAMP WITH TIME ZONE", rsmd.getColumnTypeName(2),
                        "Unexpected type name for TIMESTAMP WITH TIME ZONE column");
                assertEquals(30, rsmd.getPrecision(2), "Unexpected precision for TIMESTAMP WITH TIME ZONE column");
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
                    stmt.execute("delete from withtimestamptz where id in (5, 6, 7)");
                }
                try (PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
                    OffsetDateTime value5 = OffsetDateTime.parse("1901-11-21T12:34:56.7891+03:30");
                    OffsetDateTime value6 = OffsetDateTime.parse("2146-06-21T00:00:00-08:15");

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
                        assertEquals(value5, rs.getObject(2, OffsetDateTime.class));

                        assertTrue(rs.next(), "Expected row 2");
                        assertEquals(value6, rs.getObject(2, OffsetDateTime.class));

                        assertTrue(rs.next(), "Expected row 3");
                        assertNull(rs.getObject(2, OffsetDateTime.class));

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
                    stmt.execute("delete from withtimestamptz where id in (5, 6, 7, 8, 9)");
                }
                try (PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
                    ZonedDateTime value5Zoned = ZonedDateTime.parse("1901-11-21T12:34:56.7891+03:30");
                    ZonedDateTime value6Zoned = ZonedDateTime.parse("2146-06-21T00:00:00-08:15");
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
                        assertEquals(value5Zoned, rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 2");
                        assertEquals(value6Zoned, rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 3");
                        assertNull(rs.getObject(2, OffsetDateTime.class));

                        assertTrue(rs.next(), "Expected row 4");
                        assertEquals(value8Zoned, rs.getObject(2, ZonedDateTime.class));

                        assertTrue(rs.next(), "Expected row 5");
                        assertEquals(value9Zoned, rs.getObject(2, ZonedDateTime.class));

                        assertFalse(rs.next(), "Expected no more rows");
                    }
                }
            }
        }
    }

    /**
     * Tests if the ParameterMetaData contains the right information on TIMESTAMP WITH TIME ZONE columns.
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
                assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameterMetaData.getParameterType(2),
                        "Unexpected type for TIMESTAMP WITH TIME ZONE column");
                assertEquals("TIMESTAMP WITH TIME ZONE", parameterMetaData.getParameterTypeName(2),
                        "Unexpected type name for TIMESTAMP WITH TIME ZONE column");
                assertEquals(30, parameterMetaData.getPrecision(2),
                        "Unexpected precision for TIMESTAMP WITH TIME ZONE column");
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
                // Should match ids 1 and 2
                pstmt.setObject(1, VALUE_1);

                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue(rs.next(), "Expected row 1");
                    assertEquals(1, rs.getInt(1), "Expected id 1");
                    assertEquals(VALUE_1, rs.getObject(2), "Unexpected value for row 1");

                    assertTrue(rs.next(), "Expected row 2");
                    assertEquals(2, rs.getInt(1), "Expected id 2");
                    assertEquals(VALUE_2, rs.getObject(2), "Unexpected value for row 2");

                    assertFalse(rs.next(), "Expected no more rows");
                }
            }
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for TIMESTAMP WITH TIME ZONE).
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
                    boolean foundTimestampWithTimeZoneType = false;
                    while (rs.next()) {
                        if (!"TIMESTAMP WITH TIME ZONE".equals(rs.getString("TYPE_NAME"))) {
                            continue;
                        }
                        foundTimestampWithTimeZoneType = true;
                        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                        assertEquals(30, rs.getInt("PRECISION"), "Unexpected PRECISION");
                        assertEquals(DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                        assertFalse(rs.getBoolean("CASE_SENSITIVE"), "Unexpected CASE_SENSITIVE");
                        assertEquals(DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"), "Unexpected SEARCHABLE");
                        assertTrue(rs.getBoolean("UNSIGNED_ATTRIBUTE"), "Unexpected UNSIGNED_ATTRIBUTE");
                        assertFalse(rs.getBoolean("FIXED_PREC_SCALE"), "Unexpected FIXED_PREC_SCALE");
                        assertFalse(rs.getBoolean("AUTO_INCREMENT"), "Unexpected AUTO_INCREMENT");
                        assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                        // Not testing other values
                    }
                    assertTrue(foundTimestampWithTimeZoneType, "Expected to find TIMESTAMP WITH TIME ZONE type in typeInfo");
                }
            }
        }
    }

    /**
     * Test {@link FBDatabaseMetaData#getColumns(String, String, String, String)} for a TIMESTAMP WITH TIME ZONE column.
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
                try (ResultSet rs = dbmd.getColumns(null, null, "WITHTIMESTAMPTZ", "TIMESTAMPTZ")) {
                    assertTrue(rs.next(), "Expected a row");
                    assertEquals("TIMESTAMPTZ", rs.getString("COLUMN_NAME"), "Unexpected COLUMN_NAME");
                    assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                    assertEquals("TIMESTAMP WITH TIME ZONE", rs.getString("TYPE_NAME"), "Unexpected TYPE_NAME");
                    assertEquals(30, rs.getInt("COLUMN_SIZE"), "Unexpected COLUMN_SIZE");
                    assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                    assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                    assertEquals("NO", rs.getString("IS_AUTOINCREMENT"), "Unexpected IS_AUTOINCREMENT");

                    assertFalse(rs.next(), "Expected no second row");
                }
            }
        }
    }
}
