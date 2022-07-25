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
import java.sql.Types;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;

import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FBTimeTzFieldTest extends BaseJUnit5TestFBField<FBTimeTzField, OffsetTime> {

    private static final String TIMETZ = "07:45:51+01:00";
    private static final OffsetTime TIMETZ_OFFSETTIME = OffsetTime.parse(TIMETZ);
    private static final ZonedDateTime TIMETZ_NAMED_ZONEDDATETIME =
            ZonedDateTime.parse("2020-01-01T" + TIMETZ + "[Europe/Amsterdam]");
    // Defined using offset
    private static final String TIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB";
    // Defined using Europe/Amsterdam
    private static final String TIMETZ_ZONE_NETWORK_HEX = "0E83AAF0FFFFFE49";

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TIME_TZ);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimeTzField(fieldDescriptor, fieldData, Types.TIME_WITH_TIMEZONE);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals(getNonNullObject(), field.getObject(), "Unexpected value for getObject()");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(getNonNullObject());

        verifySetNonNullOffsetTime();
    }

    @Test
    void getObject_OffsetTime() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals(getNonNullObject(), field.getObject(OffsetTime.class),
                "Unexpected value for getObject(OffsetTime.class)");
    }

    @Test
    void getObjectNull_OffsetTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(OffsetTime.class), "Unexpected value for getObject(OffsetTime.class)");
    }

    @Test
    void getObject_OffsetDateTime() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals(getExpectedNonNullOffsetDateTime(), field.getObject(OffsetDateTime.class),
                "Unexpected value for getObject(OffsetDateTime.class)");
    }

    @Test
    void getObjectNull_OffsetDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(OffsetDateTime.class), "Unexpected value for getObject(OffsetDateTime.class)");
    }

    @Test
    void setObject_OffsetDateTime() throws SQLException {
        // note: offset date time applies current date
        OffsetDateTime offsetDateTime = getExpectedNonNullOffsetDateTime();

        field.setObject(offsetDateTime);

        verifySetNonNullOffsetTime();
    }

    @Test
    void getObject_ZonedDateTime() throws SQLException {
        toReturnNonNullNamedZonedDateTime();

        assertEquals(getTimeTzExpectedNamedZonedDateTime(), field.getObject(ZonedDateTime.class),
                "Unexpected value for getObject(ZonedDateTime.class)");
    }

    @Test
    void getObjectNull_ZonedDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(ZonedDateTime.class), "Unexpected value for getObject(ZonedDateTime.class)");
    }

    @Test
    void setObject_ZonedDateTime() throws SQLException {
        field.setObject(getTimeTzExpectedNamedZonedDateTime());

        verifySetNonNullNamedZonedDateTime();
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals(TIMETZ, field.getObject(String.class), "Unexpected value for getObject(String.class)");
    }

    @Test
    void setObject_String() throws SQLException {
        field.setString(TIMETZ);

        verifySetNonNullOffsetTime();
    }

    @Test
    @Override
    void getTimeNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getTime(), "Unexpected value for getTime()");
    }

    @Test
    @Override
    void getTimeCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getTime(calendar),
                "Unexpected value for getTime(Calendar)");
    }

    @Test
    @Override
    void getObject_java_sql_Time() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Time(expectedMillis), field.getObject(java.sql.Time.class),
                "Unexpected value for getObject(java.sql.Time.class)");
    }

    @Test
    @Override
    void setTimeNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setTime(java.sql.Time.valueOf("16:12:01"));

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    @Override
    void setTimeCalendarNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(java.sql.Time.valueOf("16:12:01"), calendar);

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    void setObject_java_sql_Time() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setObject(java.sql.Time.valueOf("16:12:01"));

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Timestamp(expectedMillis), field.getTimestamp(),
                "Unexpected value for getTimestamp()");
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Timestamp(expectedMillis), field.getTimestamp(calendar),
                "Unexpected value for getTimestamp(Calendar)");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals(new java.sql.Timestamp(expectedMillis), field.getObject(java.sql.Timestamp.class),
                "Unexpected value for getObject(java.sql.Timestamp.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        // NOTE: This actually returns a java.sql.Timestamp
        assertEquals(new java.sql.Timestamp(expectedMillis), field.getObject(java.util.Date.class),
                "Unexpected value for getObject(java.util.Date.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTimeInMillis(expectedMillis);

        assertEquals(expectedCalendar, field.getObject(Calendar.class),
                "Unexpected value for getObject(java.util.Calendar.class)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setTimestamp(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"));

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"), calendar);

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    void setObject_java_sql_Timestamp() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();

        field.setObject(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"));

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals(TIMETZ, field.getString(), "Unexpected value for getString()");
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString(TIMETZ);

        verifySetNonNullOffsetTime();
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
    void setString_acceptsOffsetDateTimeString() throws SQLException {
        String offsetDateTimeString = "2019-03-09T07:45:51+01:00";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(offsetDateTimeString);

        field.setString(offsetDateTimeString);

        verifySetOffsetTime(offsetDateTime.toOffsetTime());
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
    OffsetTime getNonNullObject() {
        return TIMETZ_OFFSETTIME;
    }

    private void toReturnNonNullOffsetTime() {
        toReturnValueExpectations(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));
    }

    private void toReturnNonNullNamedZonedDateTime() {
        toReturnValueExpectations(fromHexString(TIMETZ_ZONE_NETWORK_HEX));
    }

    private void verifySetNonNullOffsetTime() {
        verifySetValue(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));
    }

    private void verifySetNonNullNamedZonedDateTime() {
        verifySetValue(fromHexString(TIMETZ_ZONE_NETWORK_HEX));
    }

    private void verifySetOffsetTime(OffsetTime offsetTime) throws SQLException {
        verifySetValue(TimeZoneDatatypeCoder
                .getInstanceFor(datatypeCoder)
                .getTimeZoneCodecFor(fieldDescriptor)
                .encodeOffsetTime(offsetTime));
    }

    private OffsetDateTime getExpectedNonNullOffsetDateTime() {
        ZoneOffset offset = TIMETZ_OFFSETTIME.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        return OffsetDateTime.of(today.toLocalDate(), TIMETZ_OFFSETTIME.toLocalTime(), offset);
    }

    private ZonedDateTime getTimeTzExpectedNamedZonedDateTime() {
        ZoneId zoneId = TIMETZ_NAMED_ZONEDDATETIME.getZone();
        LocalDate currentDateInZone = ZonedDateTime.now(zoneId).toLocalDate();
        return TIMETZ_NAMED_ZONEDDATETIME.with(TemporalAdjusters.ofDateAdjuster(date -> currentDateInZone));
    }

}
