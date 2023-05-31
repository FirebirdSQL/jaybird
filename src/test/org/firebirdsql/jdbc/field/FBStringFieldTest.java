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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;

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
        String fromStream = new String(IOUtils.toBytes(field.getBinaryStream(), Integer.MAX_VALUE));

        assertEquals(TEST_STRING_SHORT, fromStream.trim(), "Binary stream values test failure");
    }

    @Test
    @Override
    void getObject_InputStream() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        String fromStream = new String(IOUtils.toBytes(field.getObject(InputStream.class), Integer.MAX_VALUE));

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

        String value = IOUtils.toString(field.getCharacterStream(), Integer.MAX_VALUE);
        assertEquals(TEST_STRING_SHORT, value, "Unexpected value from getCharacterStream");
    }

    @Test
    @Override
    void getObject_Reader() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        String value = IOUtils.toString(field.getObject(Reader.class), Integer.MAX_VALUE);
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
        
        verifySetString("2016-04-24", encoding);
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
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        assertEquals("2016-05-02 10:57:01.0", field.getObject(java.sql.Timestamp.class).toString(),
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
        calendar.setTime(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"));

        assertEquals(calendar, field.getObject(Calendar.class), "Unexpected value for getObject(Calendar.class)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        field.setTimestamp(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"));

        verifySetString("2016-05-02 10:57:01.0", encoding);
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // java.sql.Timestamp.toString() will render in the current time zone
        assertEquals("2016-05-02 11:57:01.0", field.getTimestamp(calendar).toString(),
                "Unexpected value for getTimestamp(Calendar)");
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"), calendar);
        
        verifySetString("2016-05-02 09:57:01.0", encoding);
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

    @Override
    String getNonNullObject() {
        return TEST_STRING_SHORT;
    }
}
