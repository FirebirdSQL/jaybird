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
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.firebirdsql.util.ByteArrayHelper.fromHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FBTimestampTzFieldTest extends BaseJUnit4TestFBField<FBTimestampTzField, OffsetDateTime> {

    private static final String TIMESTAMPTZ = "2019-03-09T07:45:51+01:00";
    private static final OffsetDateTime TIMESTAMPTZ_OFFSETDATETIME = OffsetDateTime.parse(TIMESTAMPTZ);
    // Defined using offset
    private static final String TIMESTAMPTZ_OFFSET_NETWORK_HEX = "0000E4B70E83AAF0000005DB";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        rowDescriptorBuilder.setType(ISCConstants.SQL_TIMESTAMP_TZ);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBTimestampTzField(fieldDescriptor, fieldData, Types.TIMESTAMP_WITH_TIMEZONE);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals("Unexpected value for getObject()", getNonNullObject(), field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setNonNullOffsetDateTimeExpectations();

        field.setObject(getNonNullObject());
    }

    @Test
    public void getObject_OffsetDateTime() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals("Unexpected value for getObject(OffsetDateTime.class)",
                getNonNullObject(), field.getObject(OffsetDateTime.class));
    }

    @Test
    public void getObjectNull_OffsetDateTime() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(OffsetDateTime.class)", field.getObject(OffsetDateTime.class));
    }

    @Test
    public void getObject_OffsetTime() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals("Unexpected value for getObject(OffsetTime.class)",
                getNonNullObject().toOffsetTime(), field.getObject(OffsetTime.class));
    }

    @Test
    public void getObjectNull_OffsetTime() throws SQLException {
        toReturnNullExpectations();

        assertNull("Unexpected value for getObject(OffsetTime.class)", field.getObject(OffsetTime.class));
    }

    @Test
    public void setObject_OffsetTime() throws SQLException {
        // note: offset time applies current date
        OffsetTime offsetTime = getNonNullObject().toOffsetTime();
        setOffsetTimeExpectations(offsetTime);

        field.setObject(offsetTime);
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals("Unexpected value for getObject(String.class)", TIMESTAMPTZ, field.getObject(String.class));
    }

    @Test
    public void setObject_String() throws SQLException {
        setNonNullOffsetDateTimeExpectations();

        field.setString(TIMESTAMPTZ);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnNonNullOffsetDateTime();

        assertEquals("Unexpected value for getString()", TIMESTAMPTZ, field.getString());
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setNonNullOffsetDateTimeExpectations();

        field.setString(TIMESTAMPTZ);
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
    public void setString_acceptsOffsetTimeString() throws SQLException {
        String offsetTimeString = "07:45:51+01:00";
        OffsetTime offsetTime = OffsetTime.parse(offsetTimeString);
        setOffsetTimeExpectations(offsetTime);

        field.setString(offsetTimeString);
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
    protected OffsetDateTime getNonNullObject() {
        return TIMESTAMPTZ_OFFSETDATETIME;
    }

    private void toReturnNonNullOffsetDateTime() {
        toReturnValueExpectations(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));
    }

    private void setNonNullOffsetDateTimeExpectations() {
        setValueExpectations(fromHexString(TIMESTAMPTZ_OFFSET_NETWORK_HEX));
    }

    private void setOffsetDateTimeExpectations(OffsetDateTime offsetDateTime) {
        setValueExpectations(TimeZoneDatatypeCoder.getInstanceFor(datatypeCoder).encodeTimestampTz(offsetDateTime));
    }

    private void setOffsetTimeExpectations(OffsetTime offsetTime) {
        ZoneOffset offset = offsetTime.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        OffsetDateTime timeToday = OffsetDateTime
                .of(today.toLocalDate(), offsetTime.toLocalTime(), offset);
        setOffsetDateTimeExpectations(timeToday);
    }
}
