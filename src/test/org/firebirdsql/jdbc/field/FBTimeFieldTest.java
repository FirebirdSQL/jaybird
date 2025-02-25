// SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2002 David Jencks
// SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalTime;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FBTimeField}
 *
 * @author Mark Rotteveel
 */
class FBTimeFieldTest extends BaseJUnit5TestFBField<FBTimeField, Time> {

    private static final LocalTime TEST_LOCAL_TIME = LocalTime.parse("13:37:59");
    private static final Time TEST_SQL_TIME = Time.valueOf(TEST_LOCAL_TIME);

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TYPE_TIME);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimeField(fieldDescriptor, fieldData, Types.TIME);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(TEST_SQL_TIME, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    void setObjectNonNull() throws SQLException {
        field.setObject(TEST_SQL_TIME);

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("13:37:59", field.getString(), "Unexpected value for getString");
    }

    @Test
    void getObject_String() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals("13:37:59", field.getObject(String.class), "Unexpected value for getObject(String.class");
    }

    @Test
    void setStringNonNull() throws SQLException {
        field.setString("13:37:59");

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    void setObject_String() throws SQLException {
        field.setObject("13:37:59");

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    void setStringNonTimeValue() {
        assertThrows(TypeConversionException.class, () -> field.setString("2016-01-1 13:37:59"));
    }

    @Test
    @Override
    void getTimeNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(TEST_SQL_TIME, field.getTime(), "Unexpected value for getTime");
    }

    @Test
    @Override
    void getObject_java_sql_Time() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(TEST_SQL_TIME, field.getObject(Time.class), "Unexpected value for getObject(Time.class)");
    }

    @Test
    @Override
    void setTimeNonNull() throws SQLException {
        field.setTime(TEST_SQL_TIME);

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    @Override
    void getTimeCalendarNonNull() throws SQLException {
        toReturnTimeExpectations(TEST_LOCAL_TIME);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals(Time.valueOf("14:37:59"), field.getTime(calendar), "Unexpected value for getTime(Calendar)");
    }

    @Test
    @Override
    void setTimeCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(Time.valueOf("14:37:59"), calendar);
        
        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    @Override
    void getTimestampNonNull() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(expectedValue, field.getTimestamp(), "Unexpected value for getTimestamp");
    }

    @Test
    @Override
    void getObject_java_sql_Timestamp() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(expectedValue, field.getObject(Timestamp.class),
                "Unexpected value for getObject(Timestamp.class)");
    }

    @Test
    @Override
    void getObject_java_util_Date() throws SQLException {
        Timestamp expectedValue = Timestamp.valueOf("1970-01-01 13:37:59");
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        // Test depends on the fact that we currently return Timestamp
        assertEquals(expectedValue, field.getObject(java.util.Date.class),
                "Unexpected value for getObject(java.util.Date.class)");
    }

    @Test
    @Override
    void getObject_Calendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Timestamp.valueOf("1970-01-01 13:37:59"));
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(calendar, field.getObject(Calendar.class), "Unexpected value for getObject(Calendar.class)");
    }

    @Test
    @Override
    void setTimestampNonNull() throws SQLException {
        field.setTimestamp(Timestamp.valueOf("2016-01-01 13:37:59"));

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    void setObject_Timestamp() throws SQLException {
        field.setObject(Timestamp.valueOf("2016-01-01 13:37:59"));

        verifySetTime(TEST_LOCAL_TIME);
    }

    @Test
    @Override
    void getTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());
        toReturnTimeExpectations(TEST_LOCAL_TIME);

        assertEquals(Timestamp.valueOf("1970-01-01 14:37:59"), field.getTimestamp(calendar),
                "Unexpected value for getTimestamp(Calendar)");
    }

    @Test
    @Override
    void setTimestampCalendarNonNull() throws SQLException {
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(Timestamp.valueOf("2016-01-01 14:37:59"), calendar);
        
        verifySetTime(TEST_LOCAL_TIME);
    }

    @Override
    Time getNonNullObject() {
        return Time.valueOf("13:37:59");
    }
}
