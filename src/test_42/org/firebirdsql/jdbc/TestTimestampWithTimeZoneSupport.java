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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests {@code TIMESTAMP WITH TIME ZONE} support, which is only available in Firebird 4 and Jaybird for Java 8 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestTimestampWithTimeZoneSupport {

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
    private static final OffsetDateTime VALUE_2 = VALUE_1;
    private static final OffsetDateTime VALUE_3 = OffsetDateTime.parse("2019-12-31T23:59:59.9999+13:59");

    @Rule
    public final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(
            Stream.concat(Stream.of(CREATE_TABLE, "COMMIT WORK"), Stream.of(TEST_DATA)).collect(Collectors.toList()));

    @BeforeClass
    public static void checkTimestampWithTimeZoneSupport() {
        // NOTE: For native tests this also requires use of a Firebird 4 client library
        assumeTrue("Test requires TIMESTAMP WITH TIME ZONE support on server",
                getDefaultSupportInfo().supportsTimeZones());
    }

    @Test
    public void testSimpleSelect() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT + " order by id")) {
            assertTrue("Expected row 1", rs.next());
            assertEquals("Unexpected value row 1", VALUE_1, rs.getObject(2));

            assertTrue("Expected row 2", rs.next());
            assertEquals("Unexpected value row 2", VALUE_2, rs.getObject(2));

            assertTrue("Expected row 3", rs.next());
            assertEquals("Unexpected value row 3", VALUE_3, rs.getObject(2));

            assertTrue("Expected row 4", rs.next());
            assertNull("Unexpected value for row 4", rs.getObject(2));

            assertFalse("Expected no more rows", rs.next());
        }
    }

    /**
     * Tests if the ResultSetMetaData contains the right information on TIMESTAMP WITH TIME ZONE columns.
     */
    @Test
    public void testSimpleSelect_ResultSetMetaData() throws Exception {
        try (Connection connection = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT)) {
            final ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Unexpected type for TIMESTAMP WITH TIME ZONE column",
                    Types.TIMESTAMP_WITH_TIMEZONE, rsmd.getColumnType(2));
            assertEquals("Unexpected type name for TIMESTAMP WITH TIME ZONE column",
                    "TIMESTAMP WITH TIME ZONE", rsmd.getColumnTypeName(2));
            assertEquals("Unexpected precision for TIMESTAMP WITH TIME ZONE column",
                    30, rsmd.getPrecision(2));
            // Not testing other values
        }
    }

    @Test
    public void testParameterizedInsert() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
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
                assertTrue("Expected row 1", rs.next());
                assertEquals(value5, rs.getObject(2, OffsetDateTime.class));

                assertTrue("Expected row 2", rs.next());
                assertEquals(value6, rs.getObject(2, OffsetDateTime.class));

                assertTrue("Expected row 3", rs.next());
                assertNull(rs.getObject(2, OffsetDateTime.class));

                assertFalse("Expected no more rows", rs.next());
            }
        }
    }

    /**
     * Tests if the ParameterMetaData contains the right information on TIMESTAMP WITH TIME ZONE columns.
     */
    @Test
    public void testParametrizedInsert_ParameterMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement pstmt = connection.prepareStatement(INSERT)) {
            final ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
            assertEquals("Unexpected type for TIMESTAMP WITH TIME ZONE column",
                    Types.TIMESTAMP_WITH_TIMEZONE, parameterMetaData.getParameterType(2));
            assertEquals("Unexpected type name for TIMESTAMP WITH TIME ZONE column",
                    "TIMESTAMP WITH TIME ZONE", parameterMetaData.getParameterTypeName(2));
            assertEquals("Unexpected precision for TIMESTAMP WITH TIME ZONE column",
                    30, parameterMetaData.getPrecision(2));
            // Not testing other values
        }
    }

    @Test
    public void testSelectCondition() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_CONDITION + " order by id")) {
            // Should match ids 1 and 2
            pstmt.setObject(1, VALUE_1);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue("Expected row 1", rs.next());
                assertEquals("Expected id 1", 1, rs.getInt(1));
                assertEquals("Unexpected value for row 1", VALUE_1, rs.getObject(2));

                assertTrue("Expected row 2", rs.next());
                assertEquals("Expected id 2", 2, rs.getInt(1));
                assertEquals("Unexpected value for row 2", VALUE_2, rs.getObject(2));

                assertFalse("Expected no more rows", rs.next());
            }
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for TIMESTAMP WITH TIME ZONE).
     */
    @Test
    public void testMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        try (Connection connection = getConnectionViaDriverManager()) {
            DatabaseMetaData dbmd = connection.getMetaData();

            try (ResultSet rs = dbmd.getTypeInfo()) {
                boolean foundTimestampWithTimeZoneType = false;
                while (rs.next()) {
                    if (!"TIMESTAMP WITH TIME ZONE".equals(rs.getString("TYPE_NAME"))) {
                        continue;
                    }
                    foundTimestampWithTimeZoneType = true;
                    assertEquals("Unexpected DATA_TYPE", Types.TIMESTAMP_WITH_TIMEZONE, rs.getInt("DATA_TYPE"));
                    assertEquals("Unexpected PRECISION", 30, rs.getInt("PRECISION"));
                    assertEquals("Unexpected NULLABLE", DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"));
                    assertFalse("Unexpected CASE_SENSITIVE", rs.getBoolean("CASE_SENSITIVE"));
                    assertEquals("Unexpected SEARCHABLE", DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"));
                    assertTrue("Unexpected UNSIGNED_ATTRIBUTE", rs.getBoolean("UNSIGNED_ATTRIBUTE"));
                    assertTrue("Unexpected FIXED_PREC_SCALE", rs.getBoolean("FIXED_PREC_SCALE"));
                    assertFalse("Unexpected AUTO_INCREMENT", rs.getBoolean("AUTO_INCREMENT"));
                    assertEquals("Unexpected NUM_PREC_RADIX", 10, rs.getInt("NUM_PREC_RADIX"));
                    // Not testing other values
                }
                assertTrue("Expected to find TIMESTAMP WITH TIME ZONE type in typeInfo", foundTimestampWithTimeZoneType);
            }
        }
    }

    /**
     * Test {@link FBDatabaseMetaData#getColumns(String, String, String, String)} for a TIMESTAMP WITH TIME ZONE column.
     */
    @Test
    public void testMetaData_getColumns() throws Exception {
        // TODO Consider moving to TestFBDatabaseMetaDataColumns
        try (Connection connection = getConnectionViaDriverManager()) {
            DatabaseMetaData dbmd = connection.getMetaData();
            try (ResultSet rs = dbmd.getColumns(null, null, "WITHTIMESTAMPTZ", "TIMESTAMPTZ")) {
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected COLUMN_NAME", "TIMESTAMPTZ", rs.getString("COLUMN_NAME"));
                assertEquals("Unexpected DATA_TYPE", Types.TIMESTAMP_WITH_TIMEZONE, rs.getInt("DATA_TYPE"));
                assertEquals("Unexpected TYPE_NAME", "TIMESTAMP WITH TIME ZONE", rs.getString("TYPE_NAME"));
                assertEquals("Unexpected COLUMN_SIZE", 30, rs.getInt("COLUMN_SIZE"));
                assertEquals("Unexpected NUM_PREC_RADIX", 10, rs.getInt("NUM_PREC_RADIX"));
                assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
                assertEquals("Unexpected IS_AUTOINCREMENT", "NO", rs.getString("IS_AUTOINCREMENT"));

                assertFalse("Expected no second row", rs.next());
            }
        }
    }
}
