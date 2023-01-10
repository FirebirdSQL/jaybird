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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link FBDateField}
 * 
 * @author Mark Rotteveel
 */
class FBDateFieldTest extends BaseJUnit5TestFBField<FBDateField, java.sql.Date> {

    // TODO Check if a calendar with a bigger offset might be better
    // TODO Check if dynamic selection of timezone is needed to prevent location-dependent and/or summer/winter time issues
    private final Calendar tzCalendar = Calendar.getInstance(getOneHourBehindTimeZone());
    private static final LocalDate TEST_LOCAL_DATE = LocalDate.parse("2012-03-11");
    private static final java.sql.Date TEST_SQL_DATE = java.sql.Date.valueOf(TEST_LOCAL_DATE);

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();
        rowDescriptorBuilder.setType(ISCConstants.SQL_TYPE_DATE);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBDateField(fieldDescriptor, fieldData, Types.DATE);
    }
    
    // TODO Add tests for unsupported conversions
    // TODO Add set/getObject test

    @Test
    void getDateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDate(), "Expected null for getDate()");
    }

    @Test
    void getObject_java_sql_DateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(java.sql.Date.class), "Expected null for getObject(java.sql.Date.class)");
    }

    @Test
    void getObject_java_util_DateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(java.util.Date.class), "Expected null for getObject(java.sql.Date.class)");
    }
    
    @Test
    @Override
    void getDateNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals(TEST_SQL_DATE, field.getDate(), "Unexpected value for getDate()");
    }

    @Test
    @Override
    void getObject_java_sql_Date() throws SQLException {
        toReturnTestSqlDateExpectations();

        assertEquals(TEST_SQL_DATE, field.getObject(Date.class), "Unexpected value for getObject(java.sql.Date.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        toReturnTestSqlDateExpectations();

        assertEquals(TEST_SQL_DATE, field.getObject(java.util.Date.class),
                "Unexpected value for getObject(java.sql.Date.class)");
    }

    @Test
    void setDateNull() throws SQLException {
        field.setDate(null);

        verifySetNull();
    }
    
    @Test
    @Override
    void setDateNonNull() throws SQLException {
        field.setDate(TEST_SQL_DATE);

        verifySetTestSqlDate();
    }

    @Test
    void getDateCalendarNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull(field.getDate(tzCalendar), "Expected null for getDate(Calendar)");
    }
    
    @Test
    @Override
    void getDateCalendarNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        // TODO Not sure if this is the correct result given the Javadoc of java.sql.Date
        //assertEquals(TEST_SQL_DATE, field.getDate(tzCalendar), "Unexpected value for getDate(Calendar)");
        Date date = field.getDate(tzCalendar);
        assertNotNull(date);
        assertEquals(TEST_SQL_DATE.toString(), date.toString(), "Unexpected value for getDate(Calendar)");
    }
    
    @Test
    void setDateCalendarNull() throws SQLException {
        field.setDate(null, tzCalendar);

        verifySetNull();
    }
    
    @Test
    @Override
    void setDateCalendarNonNull() throws SQLException {
        // TODO This test assumes it is running in CET
        // TODO Not sure if this is the correct result given the Javadoc of java.sql.Date
        // In GMT+1 the date is 2012-03-12, not 2012-03-11
        field.setDate(java.sql.Date.valueOf("2012-03-12"), tzCalendar);

        verifySetTestSqlDate();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        System.out.println(dateFormat.format(TEST_DATE_2012_03_11));
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals(TEST_SQL_DATE, field.getObject(), "Unexpected value for getObject()");
    }
    
    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(TEST_SQL_DATE);

        verifySetTestSqlDate();
    }
    
    // TODO Check if other objecttypes need to be tested as well.
    
    @Test
    void getStringNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull(field.getString(), "Expected null for getString()");
    }

    @Test
    void getObject_StringNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(String.class), "Expected null for getObject(String.class)");
    }
    
    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals("2012-03-11", field.getString(), "Unexpected value for getString()");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnTestSqlDateExpectations();

        assertEquals("2012-03-11", field.getObject(String.class), "Unexpected value for getObject(String.class)");
    }
    
    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }
    
    @Test
    @Override
    void setStringNonNull() throws SQLException {
        field.setString("2012-03-11");

        verifySetTestSqlDate();
    }
    
    @Test
    void getTimestampNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull(field.getTimestamp(), "Expected null for getTimestamp()");
    }

    @Test
    void getObject_java_sql_TimestampNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(java.sql.Timestamp.class), "Expected null for getObject(java.sql.Timestamp.class)");
    }
    
    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        //TODO Verify assumptions about the conversion to timestamp
        Timestamp expectedTimestamp = new java.sql.Timestamp(TEST_SQL_DATE.getTime());
        
        assertEquals(expectedTimestamp, field.getTimestamp(), "Unexpected value for getTimestamp()");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        toReturnTestSqlDateExpectations();

        //TODO Verify assumptions about the conversion to timestamp
        Timestamp expectedTimestamp = new java.sql.Timestamp(TEST_SQL_DATE.getTime());

        assertEquals(expectedTimestamp, field.getObject(Timestamp.class), "Unexpected value for getTimestamp()");
    }
    
    @Test
    void setTimestampNull() throws SQLException {
        field.setTimestamp(null);

        verifySetNull();
    }
    
    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        // NOTE Time varies with execution
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.YEAR, 2012);

        Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());

        field.setTimestamp(timestamp);

        verifySetTestSqlDate();
    }
    
    @Test
    void getTimestampCalendarNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull(field.getTimestamp(tzCalendar), "Expected no for getTimestamp(Calendar)");
    }
    
    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        //TODO Verify assumptions about the conversion to timestamp
        //Timestamp expectedTimestamp = new java.sql.Timestamp(TEST_SQL_DATE.getTime());
        Timestamp expectedTimestamp = java.sql.Timestamp.valueOf("2012-03-11 01:00:00");
        
        assertEquals(expectedTimestamp, field.getTimestamp(tzCalendar), "Unexpected value for getTimestamp(Calendar)");
    }
    
    @Test
    void setTimestampCalendarNull() throws SQLException {
        field.setTimestamp(null, tzCalendar);

        verifySetNull();
    }
    
    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        // NOTE Time varies with execution
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.YEAR, 2012);

        Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());

        field.setTimestamp(timestamp, tzCalendar);

        verifySetTestSqlDate();
    }

    @Test
    void getObject_CalendarNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Calendar.class), "Expected null for getObject(Calendar.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        toReturnTestSqlDateExpectations();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TEST_SQL_DATE);

        assertEquals(calendar, field.getObject(Calendar.class), "Unexpected value for field.getObject(Calendar.class)");
    }

    @Test
    @Override
    void getRawDateTimeStructNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct = field.getRawDateTimeStruct();

        assertNotNull(rawDateTimeStruct);
        assertEquals(2012, rawDateTimeStruct.year, "year");
        assertEquals(3, rawDateTimeStruct.month, "month");
        assertEquals(11, rawDateTimeStruct.day, "day");
        assertEquals(0, rawDateTimeStruct.hour, "hour");
        assertEquals(0, rawDateTimeStruct.minute, "minute");
        assertEquals(0, rawDateTimeStruct.second, "second");
        assertEquals(0, rawDateTimeStruct.fractions, "fractions");
    }

    @Test
    @Override
    void getObject_RawDateTimeStruct() throws SQLException {
        toReturnTestSqlDateExpectations();

        final DatatypeCoder.RawDateTimeStruct rawDateTimeStruct =
                field.getObject(DatatypeCoder.RawDateTimeStruct.class);

        assertEquals(2012, rawDateTimeStruct.year, "year");
        assertEquals(3, rawDateTimeStruct.month, "month");
        assertEquals(11, rawDateTimeStruct.day, "day");
        assertEquals(0, rawDateTimeStruct.hour, "hour");
        assertEquals(0, rawDateTimeStruct.minute, "minute");
        assertEquals(0, rawDateTimeStruct.second, "second");
        assertEquals(0, rawDateTimeStruct.fractions, "fractions");
    }

    @Test
    @Override
    void setRawDateTimeStructNonNull() throws SQLException {
        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.year = 2012;
        raw.month = 3;
        raw.day = 11;

        field.setRawDateTimeStruct(raw);

        verifySetTestSqlDate();
    }

    @Test
    @Override
    void setObject_RawDateTimeStruct() throws SQLException {
        final DatatypeCoder.RawDateTimeStruct raw = new DatatypeCoder.RawDateTimeStruct();
        raw.year = 2012;
        raw.month = 3;
        raw.day = 11;

        field.setObject(raw);

        verifySetTestSqlDate();
    }
    
    /**
     * Expectations to return {@link #TEST_LOCAL_DATE} from fieldData
     */
    private void toReturnTestSqlDateExpectations() {
        toReturnDateExpectations(TEST_LOCAL_DATE);
    }
    
    private void verifySetTestSqlDate() {
        verifySetDate(TEST_LOCAL_DATE);
    }

    @Override
    Date getNonNullObject() {
        return TEST_SQL_DATE;
    }
}
