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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.time.*;
import java.util.Calendar;

import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FBTimeTzFieldTest extends BaseJUnit4TestFBField<FBTimeTzField, OffsetTime> {

    private static final String TIMETZ = "07:45:51+01:00";
    private static final OffsetTime TIMETZ_OFFSETTIME = OffsetTime.parse(TIMETZ);
    // Defined using offset
    private static final String TIMETZ_OFFSET_NETWORK_HEX = "0E83AAF0000005DB";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TIME_TZ);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimeTzField(fieldDescriptor, fieldData, Types.TIME_WITH_TIMEZONE);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals("Unexpected value for getObject()", getNonNullObject(), field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setNonNullOffsetTimeExpectations();

        field.setObject(getNonNullObject());
    }

    @Test
    public void getObject_OffsetTime() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals("Unexpected value for getObject(OffsetTime.class)",
                getNonNullObject(), field.getObject(OffsetTime.class));
    }

    @Test
    public void getObjectNull_OffsetTime() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(OffsetTime.class)", field.getObject(OffsetTime.class));
    }

    @Test
    public void getObject_OffsetDateTime() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals("Unexpected value for getObject(OffsetDateTime.class)",
                getExpectedNonNullOffsetDateTime(), field.getObject(OffsetDateTime.class));
    }

    @Test
    public void getObjectNull_OffsetDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(OffsetDateTime.class)", field.getObject(OffsetDateTime.class));
    }

    @Test
    public void setObject_OffsetDateTime() throws SQLException {
        setNonNullOffsetTimeExpectations();
        // note: offset date time applies current date
        OffsetDateTime offsetDateTime = getExpectedNonNullOffsetDateTime();

        field.setObject(offsetDateTime);
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals("Unexpected value for getObject(String.class)", TIMETZ, field.getObject(String.class));
    }

    @Test
    public void setObject_String() throws SQLException {
        setNonNullOffsetTimeExpectations();

        field.setString(TIMETZ);
    }

    @Test
    @Override
    public void getTimeNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getTime()", new java.sql.Time(expectedMillis), field.getTime());
    }

    @Test
    @Override
    public void getTimeCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getTime(Calendar)", new java.sql.Time(expectedMillis),
                field.getTime(calendar));
    }

    @Test
    @Override
    public void getObject_java_sql_Time() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getObject(java.sql.Time.class)", new java.sql.Time(expectedMillis),
                field.getObject(java.sql.Time.class));
    }

    @Test
    @Override
    public void setTimeNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        field.setTime(java.sql.Time.valueOf("16:12:01"));
    }

    @Test
    @Override
    public void setTimeCalendarNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(java.sql.Time.valueOf("16:12:01"), calendar);
    }

    @Test
    public void setObject_java_sql_Time() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDate.now(), LocalTime.parse("16:12:01"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        field.setObject(java.sql.Time.valueOf("16:12:01"));
    }

    @Test
    @Override
    public void getTimestampNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getTimestamp()", new java.sql.Timestamp(expectedMillis),
                field.getTimestamp());
    }

    @Test
    @Override
    public void getTimestampCalendarNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getTimestamp(Calendar)", new java.sql.Timestamp(expectedMillis),
                field.getTimestamp(calendar));
    }

    @Test
    @Override
    public void getObject_java_sql_Timestamp() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        assertEquals("Unexpected value for getObject(java.sql.Timestamp.class)", new java.sql.Timestamp(expectedMillis),
                field.getObject(java.sql.Timestamp.class));
    }

    @Test
    @Override
    public void getObject_java_util_Date() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();

        // NOTE: This actually returns a java.sql.Timestamp
        assertEquals("Unexpected value for getObject(java.util.Date.class)", new java.sql.Timestamp(expectedMillis),
                field.getObject(java.util.Date.class));
    }

    @Test
    @Override
    public void getObject_Calendar() throws SQLException {
        toReturnNonNullOffsetTime();

        LocalDate localDate = OffsetDateTime.now(TIMETZ_OFFSETTIME.getOffset()).toLocalDate();
        long expectedMillis = TIMETZ_OFFSETTIME.atDate(localDate).toInstant().toEpochMilli();
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTimeInMillis(expectedMillis);

        assertEquals("Unexpected value for getObject(java.util.Calendar.class)", expectedCalendar,
                field.getObject(java.util.Calendar.class));
    }

    @Test
    @Override
    public void setTimestampNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        field.setTimestamp(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"));
    }

    @Test
    @Override
    public void setTimestampCalendarNonNull() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"), calendar);
    }

    @Test
    public void setObject_java_sql_Timestamp() throws SQLException {
        OffsetDateTime offsetDateTime = ZonedDateTime
                .of(LocalDateTime.parse("2019-03-09T07:45:51.1234"), ZoneId.systemDefault())
                .toOffsetDateTime();
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        field.setObject(java.sql.Timestamp.valueOf("2019-03-09 07:45:51.1234"));
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnNonNullOffsetTime();

        assertEquals("Unexpected value for getString()", TIMETZ, field.getString());
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setNonNullOffsetTimeExpectations();

        field.setString(TIMETZ);
    }

    @Test
    public void getStringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getString()", field.getString());
    }

    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();

        field.setString(null);
    }

    @Test
    public void setString_acceptsOffsetDateTimeString() throws SQLException {
        String offsetDateTimeString = "2019-03-09T07:45:51+01:00";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(offsetDateTimeString);
        setOffsetTimeExpectations(offsetDateTime.toOffsetTime());

        field.setString(offsetDateTimeString);
    }

    @Test
    public void setString_illegalFormat_throwsTypeConversionException() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setString("GARBAGE");
    }

    @Test
    public void setString_illegalFormatWithT_throwsTypeConversionException() throws SQLException {
        // Presence of T in string is used to determine flow used for parsing; implementation artifact
        expectedException.expect(TypeConversionException.class);

        field.setString("GARBAGE WITH T");
    }

    @Override
    protected OffsetTime getNonNullObject() {
        return TIMETZ_OFFSETTIME;
    }

    private void toReturnNonNullOffsetTime() {
        toReturnValueExpectations(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));
    }

    private void setNonNullOffsetTimeExpectations() {
        setValueExpectations(fromHexString(TIMETZ_OFFSET_NETWORK_HEX));
    }

    private void setOffsetTimeExpectations(OffsetTime offsetDateTime) {
        setValueExpectations(TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder).encodeTimeTz(offsetDateTime));
    }

    private OffsetDateTime getExpectedNonNullOffsetDateTime() {
        ZoneOffset offset = TIMETZ_OFFSETTIME.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        return OffsetDateTime.of(today.toLocalDate(), TIMETZ_OFFSETTIME.toLocalTime(), offset);
    }
}
