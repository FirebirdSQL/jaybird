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

import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

/**
 * Field for {@code TIMESTAMP WITH TIME ZONE}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
class FBTimestampTzField extends AbstractWithTimeZoneField {

    FBTimestampTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    OffsetDateTime getOffsetDateTime() throws SQLException {
        if (isNull()) return null;

        return getTimeZoneDatatypeCoder().decodeTimestampTz(getFieldData());
    }

    @Override
    void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        setFieldData(getTimeZoneDatatypeCoder().encodeTimestampTz(offsetDateTime));
    }

    @Override
    OffsetTime getOffsetTime() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        return offsetDateTime != null ? offsetDateTime.toOffsetTime() : null;
    }

    @Override
    void setOffsetTime(OffsetTime offsetTime) {
        // We need to base on a date to determine value, we use the current date; this will be inconsistent depending
        // on the date, but this aligns closest with Firebird behaviour and SQL standard
        ZoneOffset offset = offsetTime.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        OffsetDateTime timeToday = OffsetDateTime.of(today.toLocalDate(), offsetTime.toLocalTime(), offset);

        setOffsetDateTime(timeToday);
    }

    @Override
    public Object getObject() throws SQLException {
        return getOffsetDateTime();
    }

    @Override
    public java.sql.Date getDate() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        if (offsetDateTime == null) {
            return null;
        }
        return new java.sql.Date(offsetDateTime.toInstant().toEpochMilli());
    }

    @Override
    public java.sql.Date getDate(Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        return getDate();
    }

    @Override
    public void setDate(java.sql.Date value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        OffsetDateTime offsetDateTime = value.toLocalDate()
                .atStartOfDay()
                .atZone(getDefaultZoneId())
                .toOffsetDateTime();
        setOffsetDateTime(offsetDateTime);
    }

    @Override
    public void setDate(java.sql.Date value, Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        setDate(value);
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getOffsetDateTime());
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            setStringParse(value);
        } catch (DateTimeParseException e) {
            throw new TypeConversionException("Unable to convert value '" + value + "' to type TIMESTAMP WITH TIME ZONE", e);
        }
    }

}
