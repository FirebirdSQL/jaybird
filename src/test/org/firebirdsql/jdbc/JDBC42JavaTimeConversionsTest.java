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
import org.firebirdsql.jdbc.field.TypeConversionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the JDBC 4.2 conversions for <code>java.time</code> (JSR 310) types.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.2
 */
class JDBC42JavaTimeConversionsTest {

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

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static Connection connection;

    @BeforeAll
    static void setUpAll() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setup() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from javatimetest");
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void testLocalDate_ToDateColumn_getDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            Date aDate = rs.getDate(1);
            LocalDate asLocalDate = aDate.toLocalDate();
            assertEquals(localDate, asLocalDate,
                    "Expected retrieved java.time.LocalDate as DATE to be the same as inserted value");
        }
    }

    @Test
    void testLocalDate_ToDateColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDate asLocalDate = rs.getObject(1, LocalDate.class);
            assertEquals(localDate, asLocalDate,
                    "Expected retrieved java.time.LocalDate as DATE to be the same as inserted value");
        }
    }

    @Test
    void testLocalDate_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final LocalDate localDate = LocalDate.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, localDate));
        }
    }

    @Test
    void testLocalDate_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final LocalDate localDate = LocalDate.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, localDate));
        }
    }

    @Test
    void testLocalDate_ToCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aChar = rs.getString(1);
            assertEquals(localDate.toString(), aChar.trim(),
                    "Expected retrieved java.time.LocalDate as CHAR to have the same (trimmed) string value as the inserted value");
        }
    }

    @Test
    void testLocalDate_ToCharColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            final LocalDate aCharDate = rs.getObject(1, LocalDate.class);
            assertEquals(localDate, aCharDate,
                    "Expected retrieved java.time.LocalDate as CHAR to have the same value as the inserted value");
        }
    }

    @Test
    void testLocalDate_ToVarCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aVarchar = rs.getString(1);
            assertEquals(localDate.toString(), aVarchar,
                    "Expected retrieved java.time.LocalDate as VARCHAR to have the same string value as the inserted value");
        }
    }

    @Test
    void testLocalDate_ToVarCharColumn_getObject_LocalDate() throws Exception {
        final LocalDate localDate = LocalDate.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            final LocalDate aVarcharDate = rs.getObject(1, LocalDate.class);
            assertEquals(localDate, aVarcharDate,
                    "Expected retrieved java.time.LocalDate as VARCHAR to have the same value as the inserted value");
        }
    }

    @Test
    void testLocalTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final LocalTime localTime = LocalTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, localTime));
        }
    }

    @Test
    void testLocalTime_ToTimeColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            Time aTime = rs.getTime(1);
            LocalTime asLocalTime = aTime.toLocalTime();

            assertEquals(localTime.truncatedTo(ChronoUnit.SECONDS), asLocalTime,
                    "Expected retrieved java.time.LocalTime as TIME to be the same as inserted value");
        }
    }

    @Test
    void testLocalTime_ToTimeColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals(localTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalTime,
                    "Expected retrieved java.time.LocalTime as TIME to be the same as inserted value");
        }
    }

    @Test
    void testLocalTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final LocalTime localTime = LocalTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, localTime));
        }
    }

    @Test
    void testLocalTime_ToCharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aChar = rs.getString(1);
            assertEquals(localTime.toString(), aChar.trim(),
                    "Expected retrieved java.time.LocalTime as CHAR (trimmed) to be the same as toString of inserted value");
        }
    }

    @Test
    void testLocalTime_ToCharColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals(localTime, asLocalTime,
                    "Expected retrieved java.time.LocalTime as CHAR to be the same as inserted value");
        }
    }

    @Test
    void testLocalTime_ToVarcharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aVarchar = rs.getString(1);
            assertEquals(localTime.toString(), aVarchar,
                    "Expected retrieved java.time.LocalTime as VARCHAR to be the same as toString of inserted value");
        }
    }

    @Test
    void testLocalTime_ToVarcharColumn_getObject_LocalTime() throws Exception {
        final LocalTime localTime = LocalTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalTime asLocalTime = rs.getObject(1, LocalTime.class);
            assertEquals(localTime, asLocalTime,
                    "Expected retrieved java.time.LocalTime as CHAR to be the same as inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToDateColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            Date aDate = rs.getDate(1);
            LocalDateTime asLocalDateTime = aDate.toLocalDate().atStartOfDay();
            assertEquals(localDateTime.truncatedTo(ChronoUnit.DAYS), asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as DATE to be the same as inserted value truncated to days");
        }
    }

    @Test
    void testLocalDateTime_ToDateColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals(localDateTime.truncatedTo(ChronoUnit.DAYS), asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as DATE to be the same as inserted value truncated to days");
        }
    }

    @Test
    void testLocalDateTime_ToTimeColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            Time aTime = rs.getTime(1);
            LocalTime asLocalTime = aTime.toLocalTime();
            assertEquals(localDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS), asLocalTime,
                    "Expected retrieved java.time.LocalDateTime as TIME to be the same as LocalTime portion of inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToTimeColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals(
                    localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE).toLocalTime().atDate(LocalDate.of(1970, 1, 1)),
                    asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as TIME to be the same as LocalTime portion of inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToTimestampColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTimestamp FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            Timestamp aTimestamp = rs.getTimestamp(1);
            LocalDateTime asLocalDateTime = aTimestamp.toLocalDateTime();
            assertEquals(localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as TIMESTAMP to be the same as inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToTimestampColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aTimestamp FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals(localDateTime.truncatedTo(MaxFbTimePrecision.INSTANCE), asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as TIMESTAMP to be the same as inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToCharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aChar = rs.getString(1);
            assertEquals(localDateTime.toString(), aChar.trim(),
                    "Expected retrieved java.time.LocalDateTime as CHAR (trimmed) to be the same as toString of inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToCharColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals(localDateTime, asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as CHAR to be the same as inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToVarcharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aVarchar = rs.getString(1);
            assertEquals(localDateTime.toString(), aVarchar,
                    "Expected retrieved java.time.LocalDateTime as VARCHAR to be the same as toString of inserted value");
        }
    }

    @Test
    void testLocalDateTime_ToVarcharColumn_getObject_LocalDateTime() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            LocalDateTime asLocalDateTime = rs.getObject(1, LocalDateTime.class);
            assertEquals(localDateTime, asLocalDateTime,
                    "Expected retrieved java.time.LocalDateTime as VARCHAR to be the same as inserted value");
        }
    }

    @Test
    void testOffsetTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetTime));
        }
    }

    @Test
    void testOffsetTime_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetTime));
        }
    }

    @Test
    void testOffsetTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final OffsetTime offsetTime = OffsetTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetTime));
        }
    }

    @Test
    void testOffsetTime_ToCharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aChar = rs.getString(1);
            assertEquals(offsetTime.toString(), aChar.trim(),
                    "Expected retrieved java.time.OffsetTime as CHAR (trimmed) to be the same as toString of inserted value");
        }
    }

    @Test
    void testOffsetTime_ToCharColumn_getObject_OffsetTime() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            OffsetTime asOffsetTime = rs.getObject(1, OffsetTime.class);
            assertEquals(offsetTime, asOffsetTime,
                    "Expected retrieved java.time.OffsetTime as CHAR to be the same as inserted value");
        }
    }

    @Test
    void testOffsetTime_ToVarcharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aVarchar = rs.getString(1);
            assertEquals(offsetTime.toString(), aVarchar,
                    "Expected retrieved java.time.OffsetTime as VARCHAR to be the same as toString of inserted value");
        }
    }

    @Test
    void testOffsetTime_ToVarcharColumn_getObject_OffsetTime() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            OffsetTime asOffsetTime = rs.getObject(1, OffsetTime.class);
            assertEquals(offsetTime, asOffsetTime,
                    "Expected retrieved java.time.OffsetTime as VARCHAR to be the same as inserted value");
        }
    }

    @Test
    void testOffsetDateTime_ToDateColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetDateTime));
        }
    }

    @Test
    void testOffsetDateTime_ToTimeColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetDateTime));
        }
    }

    @Test
    void testOffsetDateTime_ToTimestampColumn() throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)")) {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();

            assertThrows(TypeConversionException.class, () -> pstmt.setObject(1, offsetDateTime));
        }
    }

    @Test
    void testOffsetDateTime_ToCharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aChar = rs.getString(1);
            assertEquals(offsetDateTime.toString(), aChar.trim(),
                    "Expected retrieved java.time.OffsetDateTime as CHAR (trimmed) to be the same as toString of inserted value");
        }
    }

    @Test
    void testOffsetDateTime_ToCharColumn_getObject_OffsetDateTime() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            OffsetDateTime asOffsetDateTime = rs.getObject(1, OffsetDateTime.class);
            assertEquals(offsetDateTime, asOffsetDateTime,
                    "Expected retrieved java.time.OffsetDateTime as CHAR to be the same as inserted value");
        }
    }

    @Test
    void testOffsetDateTime_ToVarcharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            String aVarchar = rs.getString(1);
            assertEquals(offsetDateTime.toString(), aVarchar,
                    "Expected retrieved java.time.OffsetDateTime as VARCHAR to be the same as toString of inserted value");
        }
    }

    @Test
    void testOffsetDateTime_ToVarcharColumn_getObject_OffsetDateTime() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)")) {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1")) {
            assertTrue(rs.next(), "Expected a row");
            OffsetDateTime asOffsetDateTime = rs.getObject(1, OffsetDateTime.class);
            assertEquals(offsetDateTime, asOffsetDateTime,
                    "Expected retrieved java.time.OffsetDateTime as VARCHAR to be the same as inserted value");
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
