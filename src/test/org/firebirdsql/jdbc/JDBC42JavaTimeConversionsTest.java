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
import org.firebirdsql.jaybird.util.FbDatetimeConversion;
import org.firebirdsql.jdbc.field.TypeConversionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.*;
import java.time.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the JDBC 4.2 conversions for {@code java.time} (JSR 310) types.
 */
class JDBC42JavaTimeConversionsTest {

    private static final String CREATE_TABLE = """
            CREATE TABLE javatimetest (
              ID INTEGER PRIMARY KEY,
              aDate DATE,
              aTime TIME,
              aTimestamp TIMESTAMP,
              aChar CHAR(100),
              aVarchar VARCHAR(100)
            )""";

    private enum TestColumn {
        ID("ID", JDBCType.INTEGER),
        A_DATE("aDate", JDBCType.DATE),
        A_TIME("aTime", JDBCType.TIME),
        A_TIMESTAMP("aTimestamp", JDBCType.TIMESTAMP),
        A_CHAR("aChar", JDBCType.CHAR),
        A_VARCHAR("aVarchar", JDBCType.VARCHAR);

        private final String columnName;
        private final JDBCType columnType;

        TestColumn(String columnName, JDBCType columnType) {
            this.columnName = columnName;
            this.columnType = columnType;
        }

        String columnName() {
            return columnName;
        }

        JDBCType columnType() {
            return columnType;
        }
        
    }

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
    void testLocalDate_ToDateColumn() throws Throwable {
        var localDate = LocalDate.now();
        insertValue(TestColumn.A_DATE, localDate);

        assertRow(TestColumn.A_DATE, rs -> {
            assertEquals(Date.valueOf(localDate), rs.getDate(1), "getDate on DATE for LocalDate");
            assertEquals(localDate, rs.getObject(1, LocalDate.class), "getObject(LocalDate) on DATE for LocalDate");
            assertEquals(localDate.toString(), rs.getString(1), "getString on DATE for LocalDate");
            assertEquals(localDate.atStartOfDay(), rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on DATE for LocalDate");
            assertEquals(Timestamp.valueOf(localDate.atStartOfDay()), rs.getTimestamp(1),
                    "getTimestamp on DATE for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime) on DATE for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getTime(1), "getTime on DATE for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on DATE for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on DATE for LocalDate");
        });
    }

    @Test
    void testLocalDate_ToTimeColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_TIME)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, LocalDate.now()),
                    "setObject(LocalDate) on TIME");
        }
    }

    @Test
    void testLocalDate_ToTimestampColumn() throws Throwable {
        var localDate = LocalDate.now();
        insertValue(TestColumn.A_TIMESTAMP, localDate);

        assertRow(TestColumn.A_TIMESTAMP, rs -> {
            assertEquals(Date.valueOf(localDate), rs.getDate(1), "getDate on TIMESTAMP for LocalDate");
            assertEquals(localDate, rs.getObject(1, LocalDate.class),
                    "getObject(LocalDate) on TIMESTAMP for LocalDate");
            assertEquals(localDate + " 00:00:00", rs.getString(1), "getString on TIMESTAMP for LocalDate");
            assertEquals(localDate.atStartOfDay(), rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on TIMESTAMP for LocalDate");
            assertEquals(Timestamp.valueOf(localDate.atStartOfDay()), rs.getTimestamp(1),
                    "getTimestamp on TIMESTAMP for LocalDate");
            assertEquals(LocalTime.MIDNIGHT, rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime) on TIMESTAMP for LocalDate");
            assertEquals(Time.valueOf(LocalTime.MIDNIGHT), rs.getTime(1), "getTime on TIMESTAMP for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on TIMESTAMP for LocalDate");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on TIMESTAMP for LocalDate");
        });
    }

    @ParameterizedTest
    @EnumSource(value = TestColumn.class, names = { "A_CHAR", "A_VARCHAR" })
    void testLocalDate_ToStringColumn(TestColumn stringColumn) throws Throwable {
        var localDate = LocalDate.now();
        insertValue(stringColumn, localDate);

        assertRow(stringColumn, rs -> {
            String message = "%s on " + stringColumn.columnType() + " for %s";
            assertEquals(Date.valueOf(localDate), rs.getDate(1), message.formatted("getDate", "LocalDate"));
            assertEquals(localDate, rs.getObject(1, LocalDate.class),
                    message.formatted("getObject(LocalDate)", "LocalDate"));
            assertEquals(localDate.toString(), rs.getString(1).trim(), message.formatted("getString", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDateTime.class),
                    message.formatted("getObject(LocalDateTime)", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getTimestamp(1),
                    message.formatted("getTimestamp", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    message.formatted("getObject(LocalTime)", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getTime(1), message.formatted("getTime", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    message.formatted("getObject(OffsetTime)", "LocalDate"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    message.formatted("getObject(OffsetDateTime)", "LocalDate"));
        });
    }

    @Test
    void testLocalTime_ToDateColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_DATE)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, LocalTime.now()),
                    "setObject(LocalTime) on DATE");
        }
    }

    @Test
    void testLocalTime_ToTimeColumn() throws Throwable {
        var insertValue = LocalTime.now();
        insertValue(TestColumn.A_TIME, insertValue);
        LocalTime localTime = insertValue.truncatedTo(FbDatetimeConversion.FB_TIME_UNIT);

        assertRow(TestColumn.A_TIME, rs -> {
            assertThrows(TypeConversionException.class, () -> rs.getDate(1), "getDate on TIME for LocalTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    "getObject(LocalDate)  on TIME for LocalTime");
            assertEquals(FbDatetimeConversion.formatSqlTime(localTime),
                    rs.getString(1), "getString on TIME for LocalTime");
            LocalDateTime localDateTime = LocalDate.EPOCH.atTime(localTime);
            assertEquals(localDateTime, rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on TIME for LocalTime");
            assertEquals(Timestamp.valueOf(localDateTime), rs.getTimestamp(1),
                    "getTimestamp on TIME for LocalTime");
            assertEquals(localTime, rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime)  on TIME for LocalTime");
            assertEquals(Time.valueOf(localTime), rs.getTime(1), "getTime on TIME for LocalTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on TIME for LocalTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on TIME for LocalTime");
        });
    }

    @Test
    void testLocalTime_ToTimestampColumn() throws Throwable {
        var insertValue = LocalTime.now();
        insertValue(TestColumn.A_TIMESTAMP, insertValue);
        LocalTime localTime = insertValue.truncatedTo(FbDatetimeConversion.FB_TIME_UNIT);

        assertRow(TestColumn.A_TIMESTAMP, rs -> {
            assertEquals(Date.valueOf(LocalDate.EPOCH), rs.getDate(1), "getDate on TIMESTAMP for LocalTime");
            assertEquals(LocalDate.EPOCH, rs.getObject(1, LocalDate.class),
                    "getObject(LocalDate) on TIMESTAMP for LocalTime");
            LocalDateTime localDateTime = LocalDate.EPOCH.atTime(localTime);
            assertEquals(FbDatetimeConversion.formatSqlTimestamp(localDateTime), rs.getString(1),
                    "getString on TIMESTAMP for LocalTime");
            assertEquals(localDateTime, rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on TIMESTAMP for LocalTime");
            assertEquals(Timestamp.valueOf(localDateTime), rs.getTimestamp(1),
                    "getTimestamp on TIMESTAMP for LocalTime");
            assertEquals(localTime, rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime) on TIMESTAMP for LocalTime");
            assertEquals(Time.valueOf(localTime), rs.getTime(1), "getTime on TIMESTAMP for LocalTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on TIMESTAMP for LocalTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on TIMESTAMP for LocalTime");
        });
    }

    @ParameterizedTest
    @EnumSource(value = TestColumn.class, names = { "A_CHAR", "A_VARCHAR" })
    void testLocalTime_ToStringColumn(TestColumn stringColumn) throws Throwable {
        var localTime = LocalTime.now();
        insertValue(stringColumn, localTime);

        assertRow(stringColumn, rs -> {
            String message = "%s on " + stringColumn.columnType() + " for %s";
            assertThrows(TypeConversionException.class, () -> rs.getDate(1), message.formatted("getDate", "LocalTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    message.formatted("getObject(LocalDate)", "LocalTime"));
            assertEquals(localTime.toString(), rs.getString(1).trim(), message.formatted("getString", "LocalTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDateTime.class),
                    message.formatted("getObject(LocalDateTime)", "LocalTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTimestamp(1),
                    message.formatted("getTimestamp", "LocalTime"));
            assertEquals(localTime, rs.getObject(1, LocalTime.class),
                    message.formatted("getObject(LocalTime)", "LocalTime"));
            assertEquals(Time.valueOf(localTime), rs.getTime(1), message.formatted("getTime", "LocalTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    message.formatted("getObject(OffsetTime)", "LocalTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    message.formatted("getObject(OffsetDateTime)", "LocalTime"));
        });
    }

    @Test
    void testLocalDateTime_ToDateColumn() throws Throwable {
        var insertValue = LocalDateTime.now();
        insertValue(TestColumn.A_DATE, insertValue);
        LocalDate localDate = insertValue.toLocalDate();

        assertRow(TestColumn.A_DATE, rs -> {
            assertEquals(Date.valueOf(localDate), rs.getDate(1), "getDate on DATE for LocalDateTime");
            assertEquals(localDate, rs.getObject(1, LocalDate.class), "getObject(LocalDate) on DATE for LocalDateTime");
            assertEquals(localDate.toString(), rs.getString(1), "getString on DATE for LocalDateTime");
            assertEquals(localDate.atStartOfDay(), rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on DATE for LocalDateTime");
            assertEquals(Timestamp.valueOf(localDate.atStartOfDay()), rs.getTimestamp(1),
                    "getTimestamp on DATE for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime) on DATE for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getTime(1), "getTime on DATE for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on DATE for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on DATE for LocalDateTime");
        });
    }

    @Test
    void testLocalDateTime_ToTimeColumn() throws Throwable {
        var insertValue = LocalDateTime.now();
        insertValue(TestColumn.A_TIME, insertValue);
        LocalTime localTime = insertValue.toLocalTime().truncatedTo(FbDatetimeConversion.FB_TIME_UNIT);

        assertRow(TestColumn.A_TIME, rs -> {
            assertThrows(TypeConversionException.class, () -> rs.getDate(1), "getDate on TIME for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    "getObject(LocalDate)  on TIME for LocalDateTime");
            assertEquals(FbDatetimeConversion.formatSqlTime(localTime),
                    rs.getString(1), "getString on TIME for LocalDateTime");
            LocalDateTime localDateTime = LocalDate.EPOCH.atTime(localTime);
            assertEquals(localDateTime, rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on TIME for LocalDateTime");
            assertEquals(Timestamp.valueOf(localDateTime), rs.getTimestamp(1),
                    "getTimestamp on TIME for LocalDateTime");
            assertEquals(localTime, rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime)  on TIME for LocalDateTime");
            assertEquals(Time.valueOf(localTime), rs.getTime(1), "getTime on TIME for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on TIME for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on TIME for LocalDateTime");
        });
    }

    @Test
    void testLocalDateTime_ToTimestampColumn() throws Throwable {
        var insertValue = LocalDateTime.now();
        insertValue(TestColumn.A_TIMESTAMP, insertValue);
        LocalDateTime localDateTime = insertValue.truncatedTo(FbDatetimeConversion.FB_TIME_UNIT);

        assertRow(TestColumn.A_TIMESTAMP, rs -> {
            LocalDate localDate = localDateTime.toLocalDate();
            assertEquals(Date.valueOf(localDate), rs.getDate(1),
                    "getDate on TIMESTAMP for LocalDateTime");
            assertEquals(localDate, rs.getObject(1, LocalDate.class),
                    "getObject(LocalDate) on TIMESTAMP for LocalDateTime");
            assertEquals(FbDatetimeConversion.formatSqlTimestamp(localDateTime), rs.getString(1),
                    "getString on TIMESTAMP for LocalDateTime");
            assertEquals(localDateTime, rs.getObject(1, LocalDateTime.class),
                    "getObject(LocalDateTime) on TIMESTAMP for LocalDateTime");
            assertEquals(Timestamp.valueOf(localDateTime), rs.getTimestamp(1),
                    "getTimestamp on TIMESTAMP for LocalDateTime");
            LocalTime localTime = localDateTime.toLocalTime();
            assertEquals(localTime, rs.getObject(1, LocalTime.class),
                    "getObject(LocalTime) on TIMESTAMP for LocalDateTime");
            assertEquals(Time.valueOf(localTime), rs.getTime(1), "getTime on TIMESTAMP for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    "getObject(OffsetTime) on TIMESTAMP for LocalDateTime");
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    "getObject(OffsetDateTime) on TIMESTAMP for LocalDateTime");
        });
    }

    @ParameterizedTest
    @EnumSource(value = TestColumn.class, names = { "A_CHAR", "A_VARCHAR" })
    void testLocalDateTime_ToStringColumn(TestColumn stringColumn) throws Throwable {
        var localDateTime = LocalDateTime.now();
        insertValue(stringColumn, localDateTime);

        assertRow(stringColumn, rs -> {
            String message = "%s on " + stringColumn.columnType() + " for %s";
            assertThrows(TypeConversionException.class, () -> rs.getDate(1),
                    message.formatted("getDate", "LocalDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    message.formatted("getObject(LocalDate)", "LocalDateTime"));
            assertEquals(localDateTime.toString(), rs.getString(1).trim(),
                    message.formatted("getString", "LocalDateTime"));
            assertEquals(localDateTime, rs.getObject(1, LocalDateTime.class),
                    message.formatted("getObject(LocalDateTime)", "LocalDateTime"));
            assertEquals(Timestamp.valueOf(localDateTime), rs.getTimestamp(1),
                    message.formatted("getTimestamp", "LocalDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    message.formatted("getObject(LocalTime)", "LocalDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTime(1),
                    message.formatted("getTime", "LocalDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    message.formatted("getObject(OffsetTime)", "LocalDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    message.formatted("getObject(OffsetDateTime)", "LocalDateTime"));
        });
    }

    @Test
    void testOffsetTime_ToDateColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_DATE)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetTime.now()));
        }
    }

    @Test
    void testOffsetTime_ToTimeColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_TIME)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetTime.now()));
        }
    }

    @Test
    void testOffsetTime_ToTimestampColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_TIMESTAMP)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetTime.now()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = TestColumn.class, names = { "A_CHAR", "A_VARCHAR" })
    void testOffsetTime_ToStringColumn(TestColumn stringColumn) throws Throwable {
        var offsetTime = OffsetTime.now();
        insertValue(stringColumn, offsetTime);

        assertRow(stringColumn, rs -> {
            String message = "%s on " + stringColumn.columnType() + " for %s";
            assertThrows(TypeConversionException.class, () -> rs.getDate(1),
                    message.formatted("getDate", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    message.formatted("getObject(LocalDate)", "OffsetTime"));
            assertEquals(offsetTime.toString(), rs.getString(1).trim(),
                    message.formatted("getString", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDateTime.class),
                    message.formatted("getObject(LocalDateTime)", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTimestamp(1),
                    message.formatted("getTimestamp", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    message.formatted("getObject(LocalTime)", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTime(1),
                    message.formatted("getTime", "OffsetTime"));
            assertEquals(offsetTime, rs.getObject(1, OffsetTime.class),
                    message.formatted("getObject(OffsetTime)", "OffsetTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetDateTime.class),
                    message.formatted("getObject(OffsetDateTime)", "OffsetTime"));
        });
    }

    @Test
    void testOffsetDateTime_ToDateColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_DATE)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetDateTime.now()));
        }
    }

    @Test
    void testOffsetDateTime_ToTimeColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_TIME)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetDateTime.now()));
        }
    }

    @Test
    void testOffsetDateTime_ToTimestampColumn() throws Throwable {
        try (PreparedStatement pstmt = prepareInsert(TestColumn.A_TIMESTAMP)) {
            assertThrows(TypeConversionException.class, () -> pstmt.setObject(2, OffsetDateTime.now()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = TestColumn.class, names = { "A_CHAR", "A_VARCHAR" })
    void testOffsetDateTime_ToStringColumn(TestColumn stringColumn) throws Throwable {
        var offsetDateTime = OffsetDateTime.now();
        insertValue(stringColumn, offsetDateTime);

        assertRow(stringColumn, rs -> {
            String message = "%s on " + stringColumn.columnType() + " for %s";
            assertThrows(TypeConversionException.class, () -> rs.getDate(1),
                    message.formatted("getDate", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDate.class),
                    message.formatted("getObject(LocalDate)", "OffsetDateTime"));
            assertEquals(offsetDateTime.toString(), rs.getString(1).trim(),
                    message.formatted("getString", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalDateTime.class),
                    message.formatted("getObject(LocalDateTime)", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTimestamp(1),
                    message.formatted("getTimestamp", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, LocalTime.class),
                    message.formatted("getObject(LocalTime)", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getTime(1),
                    message.formatted("getTime", "OffsetDateTime"));
            assertThrows(TypeConversionException.class, () -> rs.getObject(1, OffsetTime.class),
                    message.formatted("getObject(OffsetTime)", "OffsetDateTime"));
            assertEquals(offsetDateTime, rs.getObject(1, OffsetDateTime.class),
                    message.formatted("getObject(OffsetDateTime)", "OffsetDateTime"));
        });
    }

    private void insertValue(TestColumn testColumn, Object value) throws SQLException {
        insertValue(testColumn, 1, value);
    }

    private void insertValue(TestColumn testColumn, int id, Object value) throws SQLException {
        try (PreparedStatement pstmt = prepareInsert(testColumn)) {
            pstmt.setInt(1, id);
            pstmt.setObject(2, value);
            pstmt.execute();
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    private PreparedStatement prepareInsert(TestColumn testColumn) throws SQLException {
        return connection.prepareStatement(
                "insert into javatimetest (ID, " + testColumn.columnName() + ") values (?, ?)");
    }

    private void assertRow(TestColumn testColumn, ThrowingConsumer<ResultSet> rowAssertion) throws Throwable {
        assertRow(testColumn, 1, rowAssertion);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    private void assertRow(TestColumn testColumn, int id, ThrowingConsumer<ResultSet> rowAssertion)
            throws Throwable {
        try (var pstmt = connection.prepareStatement(
                "select " + testColumn.columnName() + " from javatimetest where id = ?")) {
            pstmt.setInt(1, id);
            try (var rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                rowAssertion.accept(rs);
            }
        }
    }

}
