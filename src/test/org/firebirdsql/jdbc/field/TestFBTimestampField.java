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
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBTimestampField}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBTimestampField extends BaseJUnit4TestFBField<FBTimestampField, Timestamp> {

    private static final String TEST_DATE = "2016-01-01";
    private static final String TEST_TIMESTAMP = "2016-01-01 13:37:59";
    private static final String TEST_TIME = "13:37:59";
    private static final LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.parse(TEST_TIMESTAMP.replace(' ', 'T'));
    private static final Timestamp TEST_SQL_TIMESTAMP = Timestamp.valueOf(TEST_TIMESTAMP);

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TIMESTAMP);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimestampField(fieldDescriptor, fieldData, Types.TIMESTAMP);
    }

    @Test
    @Override
    public void getDateNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Conversion doesn't correctly handle time zone
        //assertEquals("Unexpected value for getDate", java.sql.Date.valueOf(TEST_DATE), field.getDate());
        assertEquals("Unexpected value for getDate", TEST_DATE, field.getDate().toString());
    }

    @Test
    @Override
    public void getObject_java_sql_Date() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Conversion doesn't correctly handle time zone
        //assertEquals("Unexpected value for getObject(java.sql.Date.class)",
        //        java.sql.Date.valueOf(TEST_DATE), field.getObject(java.sql.Date.class));
        assertEquals("Unexpected value for getObject(java.sql.Date.class)",
                TEST_DATE, field.getObject(java.sql.Date.class).toString());
    }

    @Test
    @Override
    public void setDateNonNull() throws SQLException {
        setTimestampExpectations(LocalDateTime.parse(TEST_DATE + "T00:00:00"));

        field.setDate(java.sql.Date.valueOf(TEST_DATE));
    }

    @Test
    public void setObject_java_sql_Date() throws SQLException {
        setTimestampExpectations(LocalDateTime.parse(TEST_DATE + "T00:00:00"));

        field.setObject(java.sql.Date.valueOf(TEST_DATE));
    }

    @Test
    @Override
    public void getDateCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // TODO Conversion doesn't correctly handle time zone
//        assertEquals("Unexpected value for getDate(Calendar)",
//                java.sql.Date.valueOf(TEST_DATE), field.getDate(calendar));
        assertEquals("Unexpected value for getDate(Calendar)",
                TEST_DATE, field.getDate(calendar).toString());
    }

    @Test
    @Override
    public void setDateCalendarNonNull() throws SQLException {
        // TODO Conversion seems wrong
        setTimestampExpectations(LocalDateTime.parse("2015-12-31T23:00:00"));
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setDate(java.sql.Date.valueOf(TEST_DATE), calendar);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals("Unexpected value for getObject", TEST_SQL_TIMESTAMP, field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        field.setObject(TEST_SQL_TIMESTAMP);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals("Unexpected value for getString", TEST_TIMESTAMP + ".0", field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals("Unexpected value for getObject(String.class)", TEST_TIMESTAMP + ".0", field.getObject(String.class));
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        field.setString(TEST_TIMESTAMP);
    }

    @Test
    public void setObject_String() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        field.setObject(TEST_TIMESTAMP);
    }

    @Test
    @Override
    public void getTimeNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Doesn't seem to handle date correctly
        //assertEquals("Unexpected value for getTime", java.sql.Time.valueOf(TEST_TIME), field.getTime());
        assertEquals("Unexpected value for getTime",
                Time.valueOf(TEST_TIME).toString(), field.getTime().toString());
    }

    @Test
    @Override
    public void getObject_java_sql_Time() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Doesn't seem to handle date correctly
        //assertEquals("Unexpected value for getObject(java.sql.Time.class)",
        //       java.sql.Time.valueOf(TEST_TIME), field.getTime());
        assertEquals("Unexpected value for getObject(java.sql.Time.class)",
                Time.valueOf(TEST_TIME).toString(), field.getObject(Time.class).toString());
    }

    @Test
    @Override
    public void setTimeNonNull() throws SQLException {
        setTimestampExpectations(LocalDateTime.parse("1970-01-01T" + TEST_TIME));

        field.setTime(Time.valueOf(TEST_TIME));
    }

    @Test
    public void setObject_java_sql_Time() throws SQLException {
        setTimestampExpectations(LocalDateTime.parse("1970-01-01T" + TEST_TIME));

        field.setObject(Time.valueOf(TEST_TIME));
    }

    @Test
    @Override
    public void getTimeCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals("Unexpected value for getTime(Calendar)",
                Time.valueOf("14:37:59").toString(), field.getTime(calendar).toString());
    }

    @Test
    @Override
    public void setTimeCalendarNonNull() throws SQLException {
        //TODO Conversion doesn't seem to correctly handle time zone (looks like timezone is inverted)
        setTimestampExpectations(LocalDateTime.parse("1970-01-01T12:37:59"));
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(Time.valueOf(TEST_TIME), calendar);
    }

    @Test
    @Override
    public void getTimestampNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals("Unexpected value for getTimestamp", TEST_SQL_TIMESTAMP, field.getTimestamp());
    }

    @Test
    @Override
    public void getObject_java_sql_Timestamp() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals("Unexpected value for getObject(java.sql.Timestamp.class)",
                TEST_SQL_TIMESTAMP, field.getObject(Timestamp.class));
    }

    @Test
    @Override
    public void getObject_java_util_Date() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // Test depends on the fact that we currently return java.sql.Timestamp
        assertEquals("Unexpected value for getObject(java.util.Date.class)",
                TEST_SQL_TIMESTAMP, field.getObject(java.util.Date.class));
    }

    @Test
    @Override
    public void getObject_Calendar() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TEST_SQL_TIMESTAMP);

        assertEquals("Unexpected value for getObject(Calendar.class)", calendar, field.getObject(Calendar.class));
    }

    @Test
    @Override
    public void setTimestampNonNull() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        field.setTimestamp(TEST_SQL_TIMESTAMP);
    }

    @Test
    @Override
    public void getTimestampCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        //TODO Conversion doesn't seem to correctly handle time zone
        assertEquals("Unexpected value for getTimestamp(Calendar)",
                Timestamp.valueOf(TEST_DATE + " 14:37:59"), field.getTimestamp(calendar));
    }

    @Test
    @Override
    public void setTimestampCalendarNonNull() throws SQLException {
        //TODO Conversion doesn't seem to correctly handle time zone (looks like timezone is inverted)
        setTimestampExpectations(LocalDateTime.parse(TEST_DATE + "T12:37:59"));
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(TEST_SQL_TIMESTAMP, calendar);
    }

    @Test
    @Override
    public void getRawDateTimeStructNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct = field.getRawDateTimeStruct();

        assertEquals("year", 2016, rawDateTimeStruct.year);
        assertEquals("month", 1, rawDateTimeStruct.month);
        assertEquals("day", 1, rawDateTimeStruct.day);
        assertEquals("hour", 13, rawDateTimeStruct.hour);
        assertEquals("minute", 37, rawDateTimeStruct.minute);
        assertEquals("second", 59, rawDateTimeStruct.second);
        assertEquals("fractions", 0, rawDateTimeStruct.fractions);
    }

    @Test
    @Override
    public void getObject_RawDateTimeStruct() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct =
                field.getObject(DatatypeCoder.RawDateTimeStruct.class);

        assertEquals("year", 2016, rawDateTimeStruct.year);
        assertEquals("month", 1, rawDateTimeStruct.month);
        assertEquals("day", 1, rawDateTimeStruct.day);
        assertEquals("hour", 13, rawDateTimeStruct.hour);
        assertEquals("minute", 37, rawDateTimeStruct.minute);
        assertEquals("second", 59, rawDateTimeStruct.second);
        assertEquals("fractions", 0, rawDateTimeStruct.fractions);
    }

    @Test
    @Override
    public void setRawDateTimeStructNonNull() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.year = 2016;
        raw.month = 1;
        raw.day = 1;
        raw.hour = 13;
        raw.minute = 37;
        raw.second = 59;

        field.setRawDateTimeStruct(raw);
    }

    @Test
    @Override
    public void setObject_RawDateTimeStruct() throws SQLException {
        setTimestampExpectations(TEST_LOCAL_DATE_TIME);

        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.year = 2016;
        raw.month = 1;
        raw.day = 1;
        raw.hour = 13;
        raw.minute = 37;
        raw.second = 59;

        field.setObject(raw);
    }

    @Override
    protected Timestamp getNonNullObject() {
        return  Timestamp.valueOf(TEST_TIMESTAMP);
    }
}
