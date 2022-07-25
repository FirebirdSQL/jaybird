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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.util.Calendar;

import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FBTimestampTzFieldTest extends BaseJUnit5TestFBField<FBTimestampTzField, OffsetDateTime> {

    private static final String TIMESTAMPTZ = "2019-03-09T07:45:51+01:00";
    private static final String TIMESTAMP_NAMED = TIMESTAMPTZ + "[Europe/Amsterdam]";
    private static final OffsetDateTime TIMESTAMPTZ_OFFSETDATETIME = OffsetDateTime.parse(TIMESTAMPTZ);
    private static final ZonedDateTime TIMESTAMPTZ_NAMED_ZONEDDATETIME = ZonedDateTime.parse(TIMESTAMP_NAMED);
    // Defined using offset
    private static final String TIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB";
    // Defined using Europe/Amsterdam
    private static final String TIMESTAMPTZ_ZONE_NETWORK_HEX = "0000E4B70E83AAF0FFFFFE49";

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();
        
        rowDescriptorBuilder.setType(ISCConstants.SQL_TIMESTAMP_TZ);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimestampTzField(fieldDescriptor, fieldData, Types.TIMESTAMP_WITH_TIMEZONE);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals(getNonNullObject(), field.getObject(), "Unexpected value for getObject()");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(getNonNullObject());

        verifySetNonNullOffsetDateTime();
    }

    @Test
    void getObject_OffsetDateTime() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals(getNonNullObject(), field.getObject(OffsetDateTime.class),
                "Unexpected value for getObject(OffsetDateTime.class)");
    }

    @Test
    void getObjectNull_OffsetDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(OffsetDateTime.class), "Unexpected value for getObject(OffsetDateTime.class)");
    }

    @Test
    void getObject_OffsetTime() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals(getNonNullObject().toOffsetTime(), field.getObject(OffsetTime.class),
                "Unexpected value for getObject(OffsetTime.class)");
    }

    @Test
    void getObjectNull_OffsetTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(OffsetTime.class), "Unexpected value for getObject(OffsetTime.class)");
    }

    @Test
    void setObject_OffsetTime() throws SQLException {
        // note: offset time applies current date
        OffsetTime offsetTime = getNonNullObject().toOffsetTime();

        field.setObject(offsetTime);

        verifySetOffsetTime(offsetTime);
    }

    @Test
    void getObject_ZonedDateTime() throws SQLException {
        toReturnNonNullNamedZonedDateTime();

        assertEquals(TIMESTAMPTZ_NAMED_ZONEDDATETIME, field.getObject(ZonedDateTime.class),
                "Unexpected value for getObject(ZonedDateTime.class)");
    }

    @Test
    void getObjectNull_ZonedDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(ZonedDateTime.class), "Unexpected value for getObject(ZonedDateTime.class)");
    }

    @Test
    void setObject_ZonedDateTime() throws SQLException {
        field.setObject(TIMESTAMPTZ_NAMED_ZONEDDATETIME);

        verifySetNonNullNamedZonedDateTime();
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals(TIMESTAMPTZ, field.getObject(String.class), "Unexpected value for getObject(String.class)");
    }

    @Test
    void setObject_String() throws SQLException {
        field.setString(TIMESTAMPTZ);

        verifySetNonNullOffsetDateTime();
    }

    @Test
    @Override
    void getTimeNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getTime(), "Unexpected value for getTime()");
    }

    @Test
    @Override
    void getTimeCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getTime(calendar),
                "Unexpected value for getTime(Calendar)");
    }

    @Test
    @Override
    void getObject_java_sql_Time() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getObject(java.sql.Time.class),
                "Unexpected value for getObject(java.sql.Time.class)");
    }

    @Test
    @Override
    void setTimeNonNull() throws SQLException {
        OffsetDateTime expectedTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setTime(java.sql.Time.valueOf("16:12:01"));

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void setTimeCalendarNonNull() throws SQLException {
        OffsetDateTime expectedTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(java.sql.Time.valueOf("16:12:01"), calendar);

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    void setObject_java_sql_Time() throws SQLException {
        OffsetDateTime expectedTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setObject(java.sql.Time.valueOf("16:12:01"));

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new Timestamp(expectedMillis), field.getTimestamp(), "Unexpected value for getTimestamp()");
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new Timestamp(expectedMillis), field.getTimestamp(calendar),
                "Unexpected value for getTimestamp(Calendar)");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new Timestamp(expectedMillis), field.getObject(Timestamp.class),
                "Unexpected value for getObject(java.sql.Timestamp.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        // NOTE: This actually returns a java.sql.Timestamp
        assertEquals(new Timestamp(expectedMillis), field.getObject(java.util.Date.class),
                "Unexpected value for getObject(java.util.Date.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTimeInMillis(expectedMillis);

        assertEquals(expectedCalendar, field.getObject(Calendar.class),
                "Unexpected value for getObject(java.util.Calendar)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        OffsetDateTime expectedTime = LocalDateTime.parse("2019-03-09T07:45:51.1234")
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setTimestamp(Timestamp.valueOf("2019-03-09 07:45:51.1234"));

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        OffsetDateTime expectedTime = LocalDateTime.parse("2019-03-09T07:45:51.1234")
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(Timestamp.valueOf("2019-03-09 07:45:51.1234"), calendar);

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    void setObject_java_sql_Timestamp() throws SQLException {
        OffsetDateTime expectedTime = LocalDateTime.parse("2019-03-09T07:45:51.1234")
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setObject(Timestamp.valueOf("2019-03-09 07:45:51.1234"));

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void getDateNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Date(expectedMillis), field.getDate(), "Unexpected value for getDate()");
    }

    @Test
    @Override
    void getDateCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Date(expectedMillis), field.getDate(calendar),
                "Unexpected value for getDate(Calendar)");
    }

    @Test
    @Override
    void getObject_java_sql_Date() throws SQLException {
        toReturnNonNullOffsetDateTime();

        long expectedMillis = TIMESTAMPTZ_OFFSETDATETIME.toInstant().toEpochMilli();

        assertEquals(new java.sql.Date(expectedMillis), field.getObject(java.sql.Date.class),
                "Unexpected value for getObject(java.sql.Date.class)");
    }

    @Test
    @Override
    void setDateNonNull() throws SQLException {
        OffsetDateTime expectedTime = LocalDateTime.parse("2019-03-09T00:00:00")
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setDate(java.sql.Date.valueOf("2019-03-09"));

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void setDateCalendarNonNull() throws SQLException {
        OffsetDateTime expectedTime = LocalDateTime.parse("2019-03-09T00:00:00")
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setDate(java.sql.Date.valueOf("2019-03-09"), calendar);

        verifySetOffsetDateTime(expectedTime);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals(TIMESTAMPTZ, field.getString(), "Unexpected value for getString()");
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString(TIMESTAMPTZ);

        verifySetNonNullOffsetDateTime();
    }

    @Test
    void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getString(), "Unexpected value for getString()");
    }

    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }

    @Test
    void setString_acceptsOffsetTimeString() throws SQLException {
        String offsetTimeString = "07:45:51+01:00";
        OffsetTime offsetTime = OffsetTime.parse(offsetTimeString);

        field.setString(offsetTimeString);

        verifySetOffsetTime(offsetTime);
    }

    @Test
    void setString_illegalFormat_throwsTypeConversionException() {
        assertThrows(TypeConversionException.class, () -> field.setString("GARBAGE"));
    }

    @Test
    void setString_illegalFormatWithT_throwsTypeConversionException() {
        // Presence of T in string is used to determine flow used for parsing; implementation artifact
        assertThrows(TypeConversionException.class, () -> field.setString("GARBAGE WITH T"));
    }

    @Override
    OffsetDateTime getNonNullObject() {
        return TIMESTAMPTZ_OFFSETDATETIME;
    }

    private void toReturnNonNullOffsetDateTime() {
        toReturnValueExpectations(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));
    }

    private void toReturnNonNullNamedZonedDateTime() {
        toReturnValueExpectations(fromHexString(TIMESTAMPTZ_ZONE_NETWORK_HEX));
    }

    private void verifySetNonNullOffsetDateTime() {
        verifySetValue(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));
    }

    private void verifySetNonNullNamedZonedDateTime() {
        verifySetValue(fromHexString(TIMESTAMPTZ_ZONE_NETWORK_HEX));
    }

    private void verifySetOffsetDateTime(OffsetDateTime offsetDateTime) throws SQLException {
        verifySetValue(TimeZoneDatatypeCoder
                .getInstanceFor(datatypeCoder)
                .getTimeZoneCodecFor(fieldDescriptor)
                .encodeOffsetDateTime(offsetDateTime));
    }

    private void verifySetOffsetTime(OffsetTime offsetTime) throws SQLException {
        ZoneOffset offset = offsetTime.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        OffsetDateTime timeToday = OffsetDateTime
                .of(today.toLocalDate(), offsetTime.toLocalTime(), offset);
        verifySetOffsetDateTime(timeToday);
    }
}
