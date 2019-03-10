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
