/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import static org.junit.Assert.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.TimeZone;

import org.firebirdsql.gds.ISCConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link FBDateField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDateField extends BaseJUnit4TestFBField<FBDateField, java.sql.Date> {

    // TODO Check if a calendar with a bigger offset might be better
    // TODO Check if dynamic selection of timezone is needed to prevent location-dependent and/or summer/winter time issues
    private static final Calendar TZ_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
    private static final java.sql.Date TEST_SQL_DATE = java.sql.Date.valueOf("2012-03-11");

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        xsqlvar.sqltype = ISCConstants.SQL_TYPE_DATE;
        field = new FBDateField(xsqlvar, fieldData, Types.DATE);
    }
    
    // TODO Add tests for unsupported conversions
    // TODO Add set/getObject test

    @Test
    public void getDateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getDate()", field.getDate());
    }
    
    @Test
    @Override
    public void getDateNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals("Unexpected value for getDate()", TEST_SQL_DATE, field.getDate());
    }

    @Test
    public void setDateNull() throws SQLException {
        setNullExpectations();
        
        field.setDate(null);
    }
    
    @Test
    @Override
    public void setDateNonNull() throws SQLException {
        setTestSqlDateExpectations();
        
        field.setDate(TEST_SQL_DATE);
    }

    @Test
    public void getDateCalendarNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull("Expected null for getDate(Calendar)", field.getDate(TZ_CALENDAR));
    }
    
    @Test
    @Override
    public void getDateCalendarNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        // TODO Not sure if this is the correct result given the Javadoc of java.sql.Date
        // TODO Expected time portion to be an hour earlier (as Date is in GMT)
        assertEquals("Unexpected value for getDate(Calendar)", TEST_SQL_DATE, field.getDate(TZ_CALENDAR));
    }
    
    @Test
    public void setDateCalendarNull() throws SQLException {
        setNullExpectations();
        
        field.setDate(null, TZ_CALENDAR);
    }
    
    @Test
    @Override
    public void setDateCalendarNonNull() throws SQLException {
        setTestSqlDateExpectations();
        
        // TODO Not sure if this is the correct result given the Javadoc of java.sql.Date
        // In GMT+1 the date is 2012-03-12, not 2012-03-11
        field.setDate(TEST_SQL_DATE, TZ_CALENDAR);
        
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        System.out.println(dateFormat.format(TEST_DATE_2012_03_11));
    }
    
    @Test
    public void setNull() throws SQLException {
        setNullExpectations();
        
        field.setNull();
    }
    
    @Test
    @Ignore
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals("Unexpected value for getObject()", TEST_SQL_DATE, field.getObject());
    }
    
    @Test
    @Ignore
    @Override
    public void setObjectNonNull() throws SQLException {
        setTestSqlDateExpectations();
        
        field.setObject(TEST_SQL_DATE);
    }
    
    // TODO Check if other objecttypes need to be tested as well.
    
    @Test
    public void getStringNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull("Expected null for getString()", field.getString());
    }
    
    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        assertEquals("Unexpected value for getString()", "2012-03-11", field.getString());
    }
    
    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();
        
        field.setString(null);
    }
    
    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setTestSqlDateExpectations();
        
        field.setString("2012-03-11");
    }
    
    @Test
    public void getTimestampNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull("Expected null for getTimestamp()", field.getTimestamp());
    }
    
    @Test
    @Override
    public void getTimestampNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        //TODO Verify assumptions about the conversion to timestamp
        Timestamp expectedTimestamp = new java.sql.Timestamp(TEST_SQL_DATE.getTime());
        
        assertEquals("Unexpected value for getTimestamp()", expectedTimestamp, field.getTimestamp());
    }
    
    @Test
    public void setTimestampNull() throws SQLException {
        setNullExpectations();
        
        field.setTimestamp(null);
    }
    
    @Test
    @Override
    public void setTimestampNonNull() throws SQLException {
        setTestSqlDateExpectations();

        // NOTE Time varies with execution
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.YEAR, 2012);
        
        Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());
        
        field.setTimestamp(timestamp);
    }
    
    @Test
    public void getTimestampCalendarNull() throws SQLException {
        toReturnNullExpectations();
        
        assertNull("Expected no for getTimestamp(Calendar)", field.getTimestamp(TZ_CALENDAR));
    }
    
    @Test
    @Override
    public void getTimestampCalendarNonNull() throws SQLException {
        toReturnTestSqlDateExpectations();
        
        //TODO Verify assumptions about the conversion to timestamp
        Timestamp expectedTimestamp = new java.sql.Timestamp(TEST_SQL_DATE.getTime());
        
        assertEquals("Unexpected value for getTimestamp(Calendar)", expectedTimestamp, field.getTimestamp(TZ_CALENDAR));
    }
    
    @Test
    public void setTimestampCalendarNull() throws SQLException {
        setNullExpectations();
        
        field.setTimestamp(null, TZ_CALENDAR);
    }
    
    @Test
    @Override
    public void setTimestampCalendarNonNull() throws SQLException {
        setTestSqlDateExpectations();
        
        // NOTE Time varies with execution
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.YEAR, 2012);
        
        Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());
        
        field.setTimestamp(timestamp, TZ_CALENDAR);
    }
    
    /**
     * Expectations to return {@link TEST_SQL_DATE} from fieldData
     */
    private void toReturnTestSqlDateExpectations() {
        toReturnDateExpectations(TEST_SQL_DATE);
    }
    
    private void setTestSqlDateExpectations() {
        setDateExpectations(TEST_SQL_DATE);
    }

    @Override
    protected Date getNonNullObject() {
        return TEST_SQL_DATE;
    }
}
