// SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2002 David Jencks
// SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FBStringField}.
 *
 * @author Mark Rotteveel
 */
class FBStringFieldTest extends BaseJUnit5TestFBField<FBStringField, String> {

    private static final short TEST_STRING_SIZE = 40;

    // TEST_STRING_LONG should be shorter than TEST_STRING_SIZE
    private static final String TEST_STRING_SHORT = "This is short string.";

    // TEST_STRING_LONG should be longer than TEST_STRING_SIZE
    private static final String TEST_STRING_LONG = "And this string should be longer than short one.";

    private static final Encoding encoding = EncodingFactory.getPlatformEncoding();

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        rowDescriptorBuilder.setLength(TEST_STRING_SIZE);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBStringField(fieldDescriptor, fieldData, Types.VARCHAR);
        datatypeCoder = fieldDescriptor.getDatatypeCoder();
    }

    @Test
    void getString() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        assertEquals(TEST_STRING_SHORT, field.getString().trim(), "String does not equal to assigned one.");
    }

   @Test
    void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getBigDecimal(), "Expected null for getBigDecimal");
    }

    @Test
    @Override
    void getBigDecimalNonNull() throws SQLException {
        toReturnStringExpectations("837.47394", encoding);

        assertEquals(new BigDecimal("837.47394"), field.getBigDecimal(), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        toReturnStringExpectations("837.47394", encoding);

        assertEquals(new BigDecimal("837.47394"), field.getObject(BigDecimal.class),
                "Unexpected value for getObject(BigDecimal.class)");
    }

    @Test
    @Override
    void setBigDecimalNonNull() throws SQLException {
        field.setBigDecimal(new BigDecimal("5.381093"));

        verifySetString("5.381093", encoding);
    }

    @Disabled
    @Test
    @Override
    void getBigDecimalIntNonNull() throws SQLException {
        // TODO Build suitable test
        fail("test should be disabled");
    }

    @Test
    void getBinaryStreamNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getBinaryStream(), "Expected null for getBinaryStream");
    }

    @Test
    @Override
    void getBinaryStreamNonNull() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        String fromStream = new String(field.getBinaryStream().readAllBytes());

        assertEquals(TEST_STRING_SHORT, fromStream.trim(), "Binary stream values test failure");
    }

    @Test
    @Override
    void getObject_InputStream() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        String fromStream = new String(field.getObject(InputStream.class).readAllBytes());

        assertEquals(TEST_STRING_SHORT, fromStream.trim(), "Binary stream values test failure");
    }

    @Test
    @Override
    void setBinaryStreamNonNull() throws Exception {
        byte[] bytes = TEST_STRING_SHORT.getBytes();

        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
        
        verifySetString(TEST_STRING_SHORT, encoding);
    }

    @Test
    void setBinaryStream_tooLong() {
        byte[] bytes = TEST_STRING_LONG.getBytes();

        assertThrows(java.sql.DataTruncation.class,
                () -> field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length));
    }

    @Test
    void getBooleanNull() throws Exception {
        toReturnNullExpectations();

        assertFalse(field.getBoolean(), "Expected false for getBoolean on null");
    }

    @Test
    @Override
    void getBooleanNonNull() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_TRUE, encoding);

        assertTrue(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_TRUE, encoding);

        assertTrue(field.getObject(Boolean.class), "Unexpected value for getObject(Boolean.class)");
    }

    @Test
    void getBoolean_shortTrue() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE, encoding);

        assertTrue(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBoolean_shortTrue2() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE_2, encoding);

        assertTrue(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBoolean_shortTrue3() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE_3, encoding);

        assertTrue(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBoolean_longFalse() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_FALSE, encoding);

        assertFalse(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBoolean_shortFalse() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_FALSE, encoding);

        assertFalse(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBoolean_otherValueIsFalse() throws SQLException {
        toReturnStringExpectations("jdsd", encoding);

        assertFalse(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    @Override
    void setBoolean() throws SQLException {
        field.setBoolean(true);

        verifySetString(FBStringField.LONG_TRUE, encoding);
    }

    @Test
    void setBoolean_false() throws SQLException {
        field.setBoolean(false);

        verifySetString(FBStringField.LONG_FALSE, encoding);
    }

    @Test
    @Override
    void getByteNonNull() throws SQLException {
        toReturnStringExpectations("89", encoding);

        assertEquals(89, field.getByte(), "Unexpected value for getByte");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        toReturnStringExpectations("89", encoding);

        assertEquals(89, (byte) field.getObject(Byte.class), "Unexpected value for getByte");
    }

    @Test
    @Override
    void setByte() throws SQLException {
        field.setByte(Byte.MIN_VALUE);

        verifySetString("-128", encoding);
    }

    @Test
    @Override
    void getBytesNonNull() throws SQLException {
        toReturnValueExpectations(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getBytes());
        assertEquals(TEST_STRING_SHORT, fromBytes.trim(), "Bytes stream values test failure");
    }

    @Test
    @Override
    void getObject_byteArray() throws SQLException {
        toReturnValueExpectations(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getObject(byte[].class));
        assertEquals(TEST_STRING_SHORT, fromBytes.trim(), "Bytes value test failure");
    }

    @Test
    @Override
    void setBytesNonNull() throws SQLException {
        field.setBytes(TEST_STRING_SHORT.getBytes());

        verifySetString(TEST_STRING_SHORT, encoding);
    }

    @Test
    void setBytes_TooLong() {
        assertThrows(java.sql.DataTruncation.class, () -> field.setBytes(TEST_STRING_LONG.getBytes()));
    }

    @Test
    void getCharacterStreamNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getCharacterStream(), "Expected null for getCharacterStream");
    }

    @Test
    @Override
    void getCharacterStreamNonNull() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        String value = IOUtils.toString(field.getCharacterStream(), -1);
        assertEquals(TEST_STRING_SHORT, value, "Unexpected value from getCharacterStream");
    }

    @Test
    @Override
    void getObject_Reader() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        String value = IOUtils.toString(field.getObject(Reader.class), -1);
        assertEquals(TEST_STRING_SHORT, value, "Unexpected value from getCharacterStream");
    }

    @Test
    @Override
    void setCharacterStreamNonNull() throws Exception {
        field.setCharacterStream(new StringReader(TEST_STRING_SHORT), TEST_STRING_SHORT.length());

        verifySetString(TEST_STRING_SHORT, encoding);
    }

    @Test
    void setCharacterStream_tooLong() {
        assertThrows(DataTruncation.class,
                () -> field.setCharacterStream(new StringReader(TEST_STRING_LONG), TEST_STRING_LONG.length()));

        verifyNotSet();
    }

    @Test
    void getDateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDate(), "Expected null for getDate");
    }

    @Test
    @Override
    void getDateNonNull() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);

        assertEquals(java.sql.Date.valueOf("2016-04-24"), field.getDate(), "Unexpected value for getDate");
    }

    @Test
    @Override
    void getDateCalendarNonNull() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals("2016-04-24", field.getDate(calendar).toString(), "Unexpected value for getDate(Calendar)");
    }

    @Test
    @Override
    void setDateCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setDate(java.sql.Date.valueOf("2016-04-24"), calendar);
        
        verifySetString("2016-04-23", encoding);
    }

    @Test
    @Override
    void getObject_java_sql_Date() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);

        assertEquals(java.sql.Date.valueOf("2016-04-24"), field.getObject(java.sql.Date.class),
                "Unexpected value for getObject(java.sql.Date.class)");
    }

    @Test
    @Override
    void setDateNonNull() throws SQLException {
        field.setDate(java.sql.Date.valueOf("2016-04-24"));

        verifySetString("2016-04-24", encoding);
    }

    @Test
    void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals(0, field.getDouble(), 0, "Expected 0 for null getDouble");
    }

    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals(9.32892, field.getDouble(), 0, "Unexpected value for getDouble");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals(9.32892, field.getObject(Double.class), 0, "Unexpected value for getObject(Double.class)");
    }

    @Test
    @Override
    void setDouble() throws SQLException {
        field.setDouble(739.389932);

        verifySetString(Double.toString(739.389932), encoding);
    }

    @Test
    @Override
    void getFloatNonNull() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals(9.32892, field.getFloat(), 0.0001, "Unexpected value for getFloat");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals(9.32892, field.getObject(Float.class), 0.0001, "Unexpected value for getObject(Float.class)");
    }

    @Test
    @Override
    void setFloat() throws SQLException {
        field.setFloat(3.472f);

        verifySetString(Float.toString(3.472f), encoding);
    }

    @Test
    @Override
    void getIntNonNull() throws SQLException {
        toReturnStringExpectations("734823", encoding);

        assertEquals(734823, field.getInt(), "Unexpected value for getInt");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        toReturnStringExpectations("734823", encoding);

        assertEquals(734823, field.getObject(Integer.class).intValue(), "Unexpected value for getObject(Integer.class)");
    }

    @Test
    @Override
    void setInteger() throws SQLException {
        field.setInteger(-124579);

        verifySetString("-124579", encoding);
    }

    @Test
    @Override
    void getLongNonNull() throws SQLException {
        toReturnStringExpectations("9378037472243", encoding);

        assertEquals(9378037472243L, field.getLong(), "Unexpected value for getLong");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        toReturnStringExpectations("9378037472243", encoding);

        assertEquals(9378037472243L, field.getObject(Long.class).longValue(),
                "Unexpected value for getObject(Long.class)");
    }

    @Test
    @Override
    void setLong() throws SQLException {
        field.setLong(735987378945L);

        verifySetString("735987378945", encoding);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnStringExpectations("some string value", encoding);

        assertEquals("some string value", field.getObject(), "Unexpected value for getObject");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(TEST_STRING_SHORT);

        verifySetString(TEST_STRING_SHORT, encoding);
    }

    @Test
    void setObject_tooLong() {
        assertThrows(DataTruncation.class,
                () -> field.setObject(TEST_STRING_LONG));
        
        verifyNotSet();
    }

    @Test
    @Override
    void getShortNonNull() throws SQLException {
        toReturnStringExpectations("840", encoding);

        assertEquals(840, field.getShort(), "Unexpected value for getShort");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        toReturnStringExpectations("840", encoding);

        assertEquals(840, field.getObject(Short.class).shortValue(), "Unexpected value for getObject(Short.class)");
    }

    @Test
    @Override
    void setShort() throws SQLException {
        field.setShort((short) 7301);

        verifySetString("7301", encoding);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        assertEquals(TEST_STRING_SHORT, field.getString(), "Unexpected value for getString");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        assertEquals(TEST_STRING_SHORT, field.getObject(String.class), "Unexpected value for getObject(String.class)");
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString(TEST_STRING_SHORT);

        verifySetString(TEST_STRING_SHORT, encoding);
    }

    @Test
    @Override
    void getTimeNonNull() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);

        assertEquals("10:05:01", field.getTime().toString(), "Unexpected value for getTime");
    }

    @Test
    void getObject_java_sql_Time() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);

        assertEquals("10:05:01", field.getObject(java.sql.Time.class).toString(),
                "Unexpected value for getObject(java.sql.Time.class)");
    }

    @Test
    void setTimeNonNull() throws SQLException {
        field.setTime(Time.valueOf("10:05:01"));

        verifySetString("10:05:01", encoding);
    }

    @Test
    @Override
    void getTimeCalendarNonNull() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // java.sql.Time.toString() will render in the current time zone
        assertEquals("11:05:01", field.getTime(calendar).toString(), "Unexpected value for getTime");
    }

    @Test
    @Override
    void setTimeCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(java.sql.Time.valueOf("10:05:01"), calendar);
        
        verifySetString("09:05:01", encoding);
    }

    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        assertEquals("2016-05-02 10:57:01.0", field.getTimestamp().toString(), "Unexpected value for getTimestamp");
    }

    @Test
    void getTimestamp_isoFormat() throws SQLException {
        toReturnStringExpectations("2016-05-02T10:57:01", encoding);

        assertEquals("2016-05-02 10:57:01.0", field.getTimestamp().toString(), "Unexpected value for getTimestamp");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        assertEquals("2016-05-02 10:57:01.0", field.getObject(Timestamp.class).toString(),
                "Unexpected value for getObject(java.sql.Timestamp.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        // Test depends on the fact that we currently return java.sql.Timestamp
        assertEquals("2016-05-02 10:57:01.0", field.getObject(java.util.Date.class).toString(),
                "Unexpected value for getObject(java.util.Date.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Timestamp.valueOf("2016-05-02 10:57:01"));

        assertEquals(calendar, field.getObject(Calendar.class), "Unexpected value for getObject(Calendar.class)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        field.setTimestamp(Timestamp.valueOf("2016-05-02 10:57:01"));

        verifySetString("2016-05-02 10:57:01", encoding);
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
            toReturnStringExpectations("2016-05-02 10:57:01", encoding);
            var calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));

            // java.sql.Timestamp.toString() will render in the current time zone
            assertEquals("2016-05-02 11:57:01.0", field.getTimestamp(calendar).toString(),
                    "Unexpected value for getTimestamp(Calendar)");
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
            var calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));

            field.setTimestamp(Timestamp.valueOf("2016-05-02 10:57:01"), calendar);

            verifySetString("2016-05-02 09:57:01", encoding);
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    @Override
    void getObject_BigInteger() throws SQLException {
        toReturnStringExpectations("10", encoding);

        assertEquals(BigInteger.TEN, field.getObject(BigInteger.class),
                "Unexpected value for getObject(BigInteger.class)");
    }

    @Test
    void getObject_BigInteger_notANumber() {
        toReturnStringExpectations("xyz", encoding);

        assertThrows(TypeConversionException.class, () -> field.getObject(BigInteger.class));
    }

    @Test
    @Override
    void setObject_BigInteger() throws SQLException {
        field.setObject(BigInteger.TEN);

        verifySetString("10", encoding);
    }

    @Test
    @Override
    void getDecimalNonNull() throws SQLException {
        toReturnStringExpectations("1.34578", encoding);

        Decimal128 expectedValue = Decimal128.valueOf("1.34578");
        assertEquals(expectedValue, field.getDecimal(), "Unexpected value for getDecimal");
    }

    @Test
    void getDecimalNonNull_notANumber() {
        toReturnStringExpectations("xyz", encoding);

        assertThrows(TypeConversionException.class, field::getDecimal);
    }

    @Test
    void getDecimal_null() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDecimal(), "expected null for getDecimal");
    }

    @Test
    @Override
    void setDecimalNonNull() throws SQLException {
        field.setDecimal(Decimal128.valueOf("10"));

        verifySetString("10", encoding);
    }

    @Test
    void setDecimalNull() throws SQLException {
        field.setDecimal(null);

        verifySetNull();
    }

    @Test
    void setObject_java_time_OffsetTime() throws SQLException {
        String offsetTimeString = "20:58+02:00";
        OffsetTime offsetTime = OffsetTime.parse(offsetTimeString);

        field.setObject(offsetTime);
        
        verifySetString(offsetTimeString, encoding);
    }

    @Test
    void getObject_java_time_OffsetTime() throws SQLException {
        String offsetTimeString = "20:58+02:00";
        OffsetTime expectedOffsetTime = OffsetTime.parse(offsetTimeString);
        toReturnStringExpectations(offsetTimeString, encoding);

        assertEquals(expectedOffsetTime, field.getObject(OffsetTime.class));
    }

    @Test
    void setObject_java_time_OffsetDateTime() throws SQLException {
        String offsetDateTimeString = "2020-06-02T20:58+02:00";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(offsetDateTimeString);

        field.setObject(offsetDateTime);

        verifySetString(offsetDateTimeString, encoding);
    }

    @Test
    void getObject_java_time_OffsetDateTime() throws SQLException {
        String offsetDateTimeString = "2020-06-02T20:58+02:00";
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse(offsetDateTimeString);
        toReturnStringExpectations(offsetDateTimeString, encoding);

        assertEquals(expectedOffsetDateTime, field.getObject(OffsetDateTime.class));
    }

    @Test
    void setObject_java_time_ZonedDateTime() throws SQLException {
        String zonedDateTimeString = "2020-06-02T20:58+02:00[Europe/Amsterdam]";
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(zonedDateTimeString);

        field.setObject(zonedDateTime);

        verifySetString(zonedDateTimeString, encoding);
    }

    @Test
    void getObject_java_time_ZonedDateTime() throws SQLException {
        String zonedDateTimeString = "2020-06-02T20:58+02:00[Europe/Amsterdam]";
        ZonedDateTime expectedZonedDateTime = ZonedDateTime.parse(zonedDateTimeString);
        toReturnStringExpectations(zonedDateTimeString, encoding);

        assertEquals(expectedZonedDateTime, field.getObject(ZonedDateTime.class));
    }

    @Test
    void setObject_java_time_LocalDate() throws Exception {
        String localDateString = "2023-07-24";
        var localDate = LocalDate.parse(localDateString);

        field.setObject(localDate);

        verifySetString(localDateString, encoding);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            fieldValue, expectedValue
            2023-07-24, 2023-07-24
            2023-7-24,  2023-07-24
            2023-12-2,  2023-12-02
            """)
    void getObject_java_time_LocalDate(String fieldValue, LocalDate expectedValue) throws Exception {
        toReturnStringExpectations(fieldValue, encoding);

        assertEquals(expectedValue, field.getObject(LocalDate.class));
        // Double-check if the behaviour is the same for Date
        assertEquals(Date.valueOf(expectedValue), field.getObject(Date.class));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            localTime,     expectedFieldValue
            15:31:23,      15:31:23
            15:31:23.1,    15:31:23.100
            15:31:23.12,   15:31:23.120
            15:31:23.123,  15:31:23.123
            15:31:23.1234, 15:31:23.123400
            """)
    void setObject_java_time_LocalTime(LocalTime localTime, String expectedFieldValue) throws Exception {
        field.setObject(localTime);

        verifySetString(expectedFieldValue, encoding);
    }

    @Test
    void getObject_java_time_LocalTime() throws Exception {
        String localTimeString = "15:31:23.1";
        toReturnStringExpectations(localTimeString, encoding);

        assertEquals(LocalTime.parse(localTimeString), field.getObject(LocalTime.class));
        // Double-check if the behaviour is the same for Time (NOTE: value is truncated to seconds)
        assertEquals(Time.valueOf("15:31:23"), field.getObject(Time.class));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            localDateTime,            expectedFieldValue
            2023-07-24T15:31:23,      2023-07-24T15:31:23
            2023-07-24T15:31:23.1,    2023-07-24T15:31:23.100
            2023-07-24T15:31:23.12,   2023-07-24T15:31:23.120
            2023-07-24T15:31:23.123,  2023-07-24T15:31:23.123
            2023-07-24T15:31:23.1234, 2023-07-24T15:31:23.123400
            """)
    void setObject_java_time_LocalDateTime(LocalDateTime localDateTime, String expectedFieldValue) throws Exception {
        field.setObject(localDateTime);

        verifySetString(expectedFieldValue, encoding);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            fieldValue,               expectedValue
            2023-07-23 15:44,         2023-07-23T15:44
            2023-7-23 15:44:12,       2023-07-23T15:44:12
            2023-07-23T15:44:12,      2023-07-23T15:44:12
            2023-12-2 01:23:12.12,    2023-12-02T01:23:12.12
            2023-12-02T01:23:12.1234, 2023-12-02T01:23:12.1234
            """)
    void getObject(String fieldValue, LocalDateTime expectedValue) throws Exception {
        toReturnStringExpectations(fieldValue, encoding);

        assertEquals(expectedValue, field.getObject(LocalDateTime.class));
        // Double-check if the behaviour is the same for Timestamp
        assertEquals(Timestamp.valueOf(expectedValue), field.getObject(Timestamp.class));
    }

    @Test
    void setString_surrogatePairs_maxLength() throws Exception {
        fieldDescriptor = rowDescriptorBuilder
                .setType(ISCConstants.SQL_TEXT)
                .setLength(4 /* bytes */)
                .setSubType(4 /* UTF8 */)
                .toFieldDescriptor();
        field = new FBStringField(fieldDescriptor, fieldData, Types.CHAR);
        datatypeCoder = fieldDescriptor.getDatatypeCoder();
        Encoding encoding = datatypeCoder.getEncoding();
        assertEquals("UTF-8", encoding.getCharsetName(), "Unexpected charset for field");

        // character Smiling Face with Open Mouth; uses surrogate pairs
        String surrogatePairsValue = Character.toString(0x1f603);
        assertEquals(2, surrogatePairsValue.length(), "Expected string with 2 characters (surrogate pairs)");
        field.setString(surrogatePairsValue);

        verifySetString(surrogatePairsValue, encoding);
    }

    @Test
    void getString_surrogatePairs_maxLength() throws Exception {
        fieldDescriptor = rowDescriptorBuilder
                .setType(ISCConstants.SQL_TEXT)
                .setLength(4 /* bytes */)
                .setSubType(4 /* UTF8 */)
                .toFieldDescriptor();
        field = new FBStringField(fieldDescriptor, fieldData, Types.CHAR);
        datatypeCoder = fieldDescriptor.getDatatypeCoder();
        Encoding encoding = datatypeCoder.getEncoding();
        assertEquals("UTF-8", encoding.getCharsetName(), "Unexpected charset for field");

        // character Smiling Face with Open Mouth; uses surrogate pairs
        String surrogatePairsValue = Character.toString(0x1f603);
        assertEquals(2, surrogatePairsValue.length(), "Expected string with 2 characters (surrogate pairs)");
        toReturnStringExpectations(surrogatePairsValue, encoding);

        assertEquals(surrogatePairsValue, field.getString(), "Unexpected value for string with surrogate pairs");
    }

    @Override
    String getNonNullObject() {
        return TEST_STRING_SHORT;
    }
}
