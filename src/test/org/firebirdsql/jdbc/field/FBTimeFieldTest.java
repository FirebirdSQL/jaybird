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
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalTime;
import java.util.Calendar;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBTimeField}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBTimeFieldTest extends BaseJUnit4TestFBField<FBTimeField, Time> {

    private static final LocalTime TEST_LOCAL_TIME = LocalTime.parse("13:37:59");
    private static final Time TEST_SQL_TIME = Time.valueOf(TEST_LOCAL_TIME);

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TYPE_TIME);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimeField(fieldDescriptor, fieldData, Types.TIME);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getObject", TEST_SQL_TIME, field.getObject());
    }

    @Test
    public void setObjectNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setObject(TEST_SQL_TIME);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getString", "13:37:59", field.getString());
    }

    @Test
    public void getObject_String() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getObject(String.class", "13:37:59", field.getObject(String.class));
    }

    @Test
    public void setStringNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setString("13:37:59");
    }

    @Test
    public void setObject_String() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setObject("13:37:59");
    }

    @Test
    public void setStringNonTimeValue() throws SQLException {
        expectedException.expect(TypeConversionException.class);

        field.setString("2016-01-1 13:37:59");
    }

    @Test
    @Override
    public void getTimeNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getTime", TEST_SQL_TIME, field.getTime());
    }

    @Test
    @Override
    public void getObject_java_sql_Time() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getObject(Time.class)",
                TEST_SQL_TIME, field.getObject(Time.class));
    }

    @Test
    @Override
    public void setTimeNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setTime(TEST_SQL_TIME);
    }

    @Test
    @Override
    public void getTimeCalendarNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals("Unexpected value for getTime(Calendar)",
                Time.valueOf("14:37:59"), field.getTime(calendar));
    }

    @Test
    @Override
    public void setTimeCalendarNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(Time.valueOf("14:37:59"), calendar);
    }

    @Test
    @Override
    public void getTimestampNonNull() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getTimestamp", expectedValue, field.getTimestamp());
    }

    @Test
    @Override
    public void getObject_java_sql_Timestamp() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getObject(Timestamp.class)",
                expectedValue, field.getObject(Timestamp.class));
    }

    @Test
    @Override
    public void getObject_java_util_Date() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        // Test depends on the fact that we currently return Timestamp
        assertEquals("Unexpected value for getObject(java.util.Date.class)",
                expectedValue, field.getObject(java.util.Date.class));
    }

    @Test
    @Override
    public void getObject_Calendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Timestamp.valueOf("1970-01-01 13:37:59"));
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getObject(Calendar.class)", calendar, field.getObject(Calendar.class));
    }

    @Test
    @Override
    public void setTimestampNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setTimestamp(Timestamp.valueOf("2016-01-01 13:37:59"));
    }

    @Test
    public void setObject_Timestamp() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setObject(Timestamp.valueOf("2016-01-01 13:37:59"));
    }

    @Test
    @Override
    public void getTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("Unexpected value for getTimestamp(Calendar)",
                Timestamp.valueOf("1970-01-01 14:37:59"), field.getTimestamp(calendar));
    }

    @Test
    @Override
    public void setTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        setTimeExpectations(TEST_LOCAL_TIME);

        field.setTimestamp(Timestamp.valueOf("2016-01-01 14:37:59"), calendar);
    }

    @Test
    @Override
    public void getRawDateTimeStructNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct = requireNonNull(field.getRawDateTimeStruct());

        assertEquals("year", 0, rawDateTimeStruct.year);
        assertEquals("month", 0, rawDateTimeStruct.month);
        assertEquals("day", 0, rawDateTimeStruct.day);
        assertEquals("hour", 13, rawDateTimeStruct.hour);
        assertEquals("minute", 37, rawDateTimeStruct.minute);
        assertEquals("second", 59, rawDateTimeStruct.second);
        assertEquals("fractions", 0, rawDateTimeStruct.fractions);
    }

    @Test
    @Override
    public void getObject_RawDateTimeStruct() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct = 
                field.getObject(DatatypeCoder.RawDateTimeStruct.class);

        assertEquals("year", 0, rawDateTimeStruct.year);
        assertEquals("month", 0, rawDateTimeStruct.month);
        assertEquals("day", 0, rawDateTimeStruct.day);
        assertEquals("hour", 13, rawDateTimeStruct.hour);
        assertEquals("minute", 37, rawDateTimeStruct.minute);
        assertEquals("second", 59, rawDateTimeStruct.second);
        assertEquals("fractions", 0, rawDateTimeStruct.fractions);
    }

    @Test
    @Override
    public void setRawDateTimeStructNonNull() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.hour = 13;
        raw.minute = 37;
        raw.second = 59;

        field.setRawDateTimeStruct(raw);
    }

    @Test
    @Override
    public void setObject_RawDateTimeStruct() throws SQLException {
        setTimeExpectations(TEST_LOCAL_TIME);

        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.hour = 13;
        raw.minute = 37;
        raw.second = 59;

        field.setObject(raw);
    }

    @Override
    protected Time getNonNullObject() {
        return Time.valueOf("13:37:59");
    }
}
