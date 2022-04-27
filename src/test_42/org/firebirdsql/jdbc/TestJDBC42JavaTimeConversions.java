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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.jdbc.field.TypeConversionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the JDBC 4.2 conversions for <code>java.time</code> (JSR 310) types.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.2
 */
public class TestJDBC42JavaTimeConversions extends FBJUnit4TestBase {

    @SuppressWarnings("deprecation")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Connection connection;

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE javatimetest (" +
            "  ID INTEGER PRIMARY KEY," +
            "  aDate DATE," +
            "  aTime TIME," +
            "  aTimestamp TIMESTAMP," +
            "  aChar CHAR(100)," +
            "  aVarchar VARCHAR(100)" +
            ")";
    //@formatter:on

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        executeCreateTable(connection, CREATE_TABLE);
    }

    @After
    public void tearDown() throws Exception {
        closeQuietly(connection);
    }

    @Test
    public void testLocalDate_ToDateColumn_getDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            Date aDate = rs.getDate(1);
            LocalDate asLocalDate = aDate.toLocalDate();
            assertEquals("Expected retrieved java.time.LocalDate as DATE to be the same as inserted value",
                    localDate, asLocalDate);
        }
    }

    @Test
    public void testLocalDate_ToDateColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDate asLocalDate = rs.getObject(1, LocalDate.class);
            assertEquals("Expected retrieved java.time.LocalDate as DATE to be the same as inserted value",
                    localDate, asLocalDate);
        }
    }

    @Test
    public void testLocalDate_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final LocalDate localDate = LocalDate.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, localDate);
        }
    }

    @Test
    public void testLocalDate_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final LocalDate localDate = LocalDate.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, localDate);
        }
    }

    @Test
    public void testLocalDate_ToCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aChar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalDate as CHAR to have the same (trimmed) string value as the inserted value",
                    localDate.toString(), aChar.trim());
        }
    }

    @Test
    public void testLocalDate_ToCharColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            final LocalDate aCharDate = rs.getObject(1, LocalDate.class);
            assertEquals("Expected retrieved java.time.LocalDate as CHAR to have the same value as the inserted value",
                    localDate, aCharDate);
        }
    }

    @Test
    public void testLocalDate_ToVarCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aVarchar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalDate as VARCHAR to have the same string value as the inserted value",
                    localDate.toString(), aVarchar);
        }
    }

    @Test
    public void testLocalDate_ToVarCharColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            final LocalDate aVarcharDate = rs.getObject(1, LocalDate.class);
            assertEquals("Expected retrieved java.time.LocalDate as VARCHAR to have the same value as the inserted value",
                    localDate, aVarcharDate);
        }
    }

    @Test
    public void testLocalTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final LocalTime localTime = LocalTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, localTime);
        }
    }

    @Test
    public void testLocalTime_ToTimeColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            Time aTime = rs.getTime(1);
            LocalTime asLocalTime = aTime.toLocalTime();

            assertEquals("Expected retrieved java.time.LocalTime as TIME to be the same as inserted value",
                    localTime.truncatedTo(ChronoUnit.SECONDS), asLocalTime);
        }
    }

    @Test
    public void testLocalTime_ToTimeColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals("Expected retrieved java.time.LocalTime as TIME to be the same as inserted value",
                    localTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalTime);
        }
    }

    @Test
    public void testLocalTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final LocalTime localTime = LocalTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, localTime);
        }
    }

    @Test
    public void testLocalTime_ToCharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aChar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalTime as CHAR (trimmed) to be the same as toString of inserted value",
                    localTime.toString(), aChar.trim());
        }
    }

    @Test
    public void testLocalTime_ToCharColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals("Expected retrieved java.time.LocalTime as CHAR to be the same as inserted value",
                    localTime, asLocalTime);
        }
    }

    @Test
    public void testLocalTime_ToVarcharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aVarchar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalTime as VARCHAR to be the same as toString of inserted value",
                    localTime.toString(), aVarchar);
        }
    }

    @Test
    public void testLocalTime_ToVarcharColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals("Expected retrieved java.time.LocalTime as CHAR to be the same as inserted value",
                    localTime, asLocalTime);
        }
    }

    @Test
    public void testLocalDateTime_ToDateColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            Date aDate = rs.getDate(1);
            LocalDateTime asLocalDateTime = aDate.toLocalDate().atStartOfDay();
            assertEquals("Expected retrieved java.time.LocalDateTime as DATE to be the same as inserted value truncated to days",
                    localDateTime.truncatedTo(ChronoUnit.DAYS), asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToDateColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals("Expected retrieved java.time.LocalDateTime as DATE to be the same as inserted value truncated to days",
                    localDateTime.truncatedTo(ChronoUnit.DAYS), asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToTimeColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            Time aTime = rs.getTime(1);
            LocalTime asLocalTime = aTime.toLocalTime();
            assertEquals("Expected retrieved java.time.LocalDateTime as TIME to be the same as LocalTime portion of inserted value",
                    localDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS), asLocalTime);
        }
    }

    @Test
    public void testLocalDateTime_ToTimeColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals("Expected retrieved java.time.LocalDateTime as TIME to be the same as LocalTime portion of inserted value",
                    localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE).toLocalTime().atDate(LocalDate.of(1970, 1, 1)),
                    asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToTimestampColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTimestamp FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            Timestamp aTimestamp = rs.getTimestamp(1);
            LocalDateTime asLocalDateTime = aTimestamp.toLocalDateTime();
            assertEquals("Expected retrieved java.time.LocalDateTime as TIMESTAMP to be the same as inserted value",
                    localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToTimestampColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTimestamp FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals("Expected retrieved java.time.LocalDateTime as TIMESTAMP to be the same as inserted value",
                    localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToCharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aChar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalDateTime as CHAR (trimmed) to be the same as toString of inserted value",
                    localDateTime.toString(), aChar.trim());
        }
    }

    @Test
    public void testLocalDateTime_ToCharColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals("Expected retrieved java.time.LocalDateTime as CHAR to be the same as inserted value",
                    localDateTime, asLocalDateTime);
        }
    }

    @Test
    public void testLocalDateTime_ToVarcharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aVarchar = rs.getString(1);
            assertEquals("Expected retrieved java.time.LocalDateTime as VARCHAR to be the same as toString of inserted value",
                    localDateTime.toString(), aVarchar);
        }
    }

    @Test
    public void testLocalDateTime_ToVarcharColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals("Expected retrieved java.time.LocalDateTime as VARCHAR to be the same as inserted value",
                    localDateTime, asLocalDateTime);
        }
    }

    @Test
    public void testOffsetTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetTime);
        }
    }

    @Test
    public void testOffsetTime_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetTime);
        }
    }

    @Test
    public void testOffsetTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetTime);
        }
    }

    @Test
    public void testOffsetTime_ToCharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aChar = rs.getString(1);
            assertEquals("Expected retrieved java.time.OffsetTime as CHAR (trimmed) to be the same as toString of inserted value",
                    offsetTime.toString(), aChar.trim());
        }
    }

    @Test
    public void testOffsetTime_ToCharColumn_getObject_OffsetTime() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            OffsetTime asOffsetTime = rs.getObject(1, OffsetTime.class);
            assertEquals("Expected retrieved java.time.OffsetTime as CHAR to be the same as inserted value",
                    offsetTime, asOffsetTime);
        }
    }

    @Test
    public void testOffsetTime_ToVarcharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aVarchar = rs.getString(1);
            assertEquals("Expected retrieved java.time.OffsetTime as VARCHAR to be the same as toString of inserted value",
                    offsetTime.toString(), aVarchar);
        }
    }

    @Test
    public void testOffsetTime_ToVarcharColumn_getObject_OffsetTime() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            OffsetTime asOffsetTime = rs.getObject(1, OffsetTime.class);
            assertEquals("Expected retrieved java.time.OffsetTime as VARCHAR to be the same as inserted value",
                    offsetTime, asOffsetTime);
        }
    }

    @Test
    public void testOffsetDateTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetDateTime);
        }
    }

    @Test
    public void testOffsetDateTime_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetDateTime);
        }
    }

    @Test
    public void testOffsetDateTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            expectedException.expect(TypeConversionException.class);

            pstmt.setObject(1, offsetDateTime);
        }
    }

    @Test
    public void testOffsetDateTime_ToCharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aChar = rs.getString(1);
            assertEquals("Expected retrieved java.time.OffsetDateTime as CHAR (trimmed) to be the same as toString of inserted value",
                    offsetDateTime.toString(), aChar.trim());
        }
    }

    @Test
    public void testOffsetDateTime_ToCharColumn_getObject_OffsetDateTime() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            OffsetDateTime asOffsetDateTime = rs.getObject(1, OffsetDateTime.class);
            assertEquals("Expected retrieved java.time.OffsetDateTime as CHAR to be the same as inserted value",
                    offsetDateTime, asOffsetDateTime);
        }
    }

    @Test
    public void testOffsetDateTime_ToVarcharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            String aVarchar = rs.getString(1);
            assertEquals("Expected retrieved java.time.OffsetDateTime as VARCHAR to be the same as toString of inserted value",
                    offsetDateTime.toString(), aVarchar);
        }
    }

    @Test
    public void testOffsetDateTime_ToVarcharColumn_getObject_OffsetDateTime() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue("Expected a row", rs.next());
            OffsetDateTime asOffsetDateTime = rs.getObject(1, OffsetDateTime.class);
            assertEquals("Expected retrieved java.time.OffsetDateTime as VARCHAR to be the same as inserted value",
                    offsetDateTime, asOffsetDateTime);
        }
    }

    private static final class MaxFbTimePrecision implements TemporalUnit {

        private static final Duration PRECISION_DURATION = Duration.ofNanos(100_000);
        private static final MaxFbTimePrecision INSTANCE = new MaxFbTimePrecision();

        @Override
        public Duration getDuration() {
            return PRECISION_DURATION;
        }

        @Override
        public boolean isDurationEstimated() {
            return false;
        }

        @Override
        public boolean isDateBased() {
            return false;
        }

        @Override
        public boolean isTimeBased() {
            return true;
        }

        @Override
        public boolean isSupportedBy(Temporal temporal) {
            return temporal.isSupported(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R) temporal.plus(amount, this);
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }
    }
}
