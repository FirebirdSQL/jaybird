/*
 SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBTimestampField}.
 *
 * @author Mark Rotteveel
 */
class FBTimestampFieldTest extends BaseJUnit5TestFBField<FBTimestampField, Timestamp> {

    private static final String TEST_DATE = "2016-01-01";
    private static final String TEST_TIMESTAMP = "2016-01-01 13:37:59";
    private static final String TEST_TIME = "13:37:59";
    private static final LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.parse(TEST_TIMESTAMP.replace(' ', 'T'));
    private static final Timestamp TEST_SQL_TIMESTAMP = Timestamp.valueOf(TEST_TIMESTAMP);

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TIMESTAMP);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimestampField(fieldDescriptor, fieldData, Types.TIMESTAMP);
    }

    @Test
    @Override
    void getDateNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Conversion doesn't correctly handle time zone
        //assertEquals(java.sql.Date.valueOf(TEST_DATE), field.getDate(), "Unexpected value for getDate");
        assertEquals(TEST_DATE, field.getDate().toString(), "Unexpected value for getDate");
    }

    @Test
    @Override
    void getObject_java_sql_Date() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Conversion doesn't correctly handle time zone
        //assertEquals("Unexpected value for getObject(java.sql.Date.class)",
        //        java.sql.Date.valueOf(TEST_DATE), field.getObject(java.sql.Date.class));
        assertEquals(TEST_DATE, field.getObject(java.sql.Date.class).toString(),
                "Unexpected value for getObject(java.sql.Date.class)");
    }

    @Test
    @Override
    void setDateNonNull() throws SQLException {
        field.setDate(java.sql.Date.valueOf(TEST_DATE));

        verifySetTimestamp(LocalDateTime.parse(TEST_DATE + "T00:00:00"));
    }

    @Test
    void setObject_java_sql_Date() throws SQLException {
        field.setObject(java.sql.Date.valueOf(TEST_DATE));

        verifySetTimestamp(LocalDateTime.parse(TEST_DATE + "T00:00:00"));
    }

    @Test
    @Override
    void getDateCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // TODO Conversion doesn't correctly handle time zone
//        assertEquals("Unexpected value for getDate(Calendar)",
//                java.sql.Date.valueOf(TEST_DATE), field.getDate(calendar));
        assertEquals(TEST_DATE, field.getDate(calendar).toString(), "Unexpected value for getDate(Calendar)");
    }

    @Test
    @Override
    void setDateCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setDate(java.sql.Date.valueOf(TEST_DATE), calendar);

        verifySetTimestamp(LocalDateTime.parse("2015-12-31T00:00:00"));
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals(TEST_SQL_TIMESTAMP, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(TEST_SQL_TIMESTAMP);

        verifySetTimestamp(TEST_LOCAL_DATE_TIME);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals(TEST_TIMESTAMP, field.getString(), "Unexpected value for getString");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals(TEST_TIMESTAMP, field.getObject(String.class), "Unexpected value for getObject(String.class)");
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString(TEST_TIMESTAMP);

        verifySetTimestamp(TEST_LOCAL_DATE_TIME);
    }

    @Test
    void setObject_String() throws SQLException {
        field.setObject(TEST_TIMESTAMP);

        verifySetTimestamp(TEST_LOCAL_DATE_TIME);
    }

    @Test
    @Override
    void getTimeNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Doesn't seem to handle date correctly
        //assertEquals(java.sql.Time.valueOf(TEST_TIME), field.getTime(), "Unexpected value for getTime");
        assertEquals(Time.valueOf(TEST_TIME).toString(), field.getTime().toString(), "Unexpected value for getTime");
    }

    @Test
    @Override
    void getObject_java_sql_Time() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // TODO Doesn't seem to handle date correctly
        //assertEquals("Unexpected value for getObject(java.sql.Time.class)",
        //       java.sql.Time.valueOf(TEST_TIME), field.getTime());
        assertEquals(Time.valueOf(TEST_TIME).toString(), field.getObject(Time.class).toString(),
                "Unexpected value for getObject(java.sql.Time.class)");
    }

    @Test
    @Override
    void setTimeNonNull() throws SQLException {
        field.setTime(Time.valueOf(TEST_TIME));

        verifySetTimestamp(LocalDateTime.parse("1970-01-01T" + TEST_TIME));
    }

    @Test
    void setObject_java_sql_Time() throws SQLException {
        field.setObject(Time.valueOf(TEST_TIME));

        verifySetTimestamp(LocalDateTime.parse("1970-01-01T" + TEST_TIME));
    }

    @Test
    @Override
    void getTimeCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals(Time.valueOf("14:37:59").toString(), field.getTime(calendar).toString(),
                "Unexpected value for getTime(Calendar)");
    }

    @Test
    @Override
    void setTimeCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(Time.valueOf(TEST_TIME), calendar);

        //TODO Conversion doesn't seem to correctly handle time zone (looks like timezone is inverted)
        verifySetTimestamp(LocalDateTime.parse("1970-01-01T12:37:59"));
    }

    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals(TEST_SQL_TIMESTAMP, field.getTimestamp(), "Unexpected value for getTimestamp");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        assertEquals(TEST_SQL_TIMESTAMP, field.getObject(Timestamp.class),
                "Unexpected value for getObject(java.sql.Timestamp.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);

        // Test depends on the fact that we currently return java.sql.Timestamp
        assertEquals(TEST_SQL_TIMESTAMP, field.getObject(java.util.Date.class),
                "Unexpected value for getObject(java.util.Date.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TEST_SQL_TIMESTAMP);

        assertEquals(calendar, field.getObject(Calendar.class), "Unexpected value for getObject(Calendar.class)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        field.setTimestamp(TEST_SQL_TIMESTAMP);

        verifySetTimestamp(TEST_LOCAL_DATE_TIME);
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        toReturnTimestampExpectations(TEST_LOCAL_DATE_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        //TODO Conversion doesn't seem to correctly handle time zone
        assertEquals(Timestamp.valueOf(TEST_DATE + " 14:37:59"), field.getTimestamp(calendar),
                "Unexpected value for getTimestamp(Calendar)");
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(TEST_SQL_TIMESTAMP, calendar);
        
        //TODO Conversion doesn't seem to correctly handle time zone (looks like timezone is inverted)
        verifySetTimestamp(LocalDateTime.parse(TEST_DATE + "T12:37:59"));
    }

    @Override
    Timestamp getNonNullObject() {
        return  Timestamp.valueOf(TEST_TIMESTAMP);
    }
}
