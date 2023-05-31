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

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

/**
 * Field implementation for {@code TIMESTAMP (WITHOUT TIME ZONE)}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
class FBTimestampField extends AbstractWithoutTimeZoneField {

    FBTimestampField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getTimestamp();
    }

    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getTimestamp());
    }

    public Date getDate(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return new java.sql.Date(getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal).getTime());
    }

    @Override
    LocalDate getLocalDate() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }

    public Time getTime(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return new java.sql.Time(getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal).getTime());
    }

    @Override
    LocalTime getLocalTime() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalTime() : null;
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal);
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeLocalDateTime(getFieldData());
    }

    public void setString(String value) throws SQLException {
        setTimestamp(fromString(value, Timestamp::valueOf));
    }

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()), cal));
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()), cal));
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(value, cal));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        if (setWhenNull(value)) return;
        setFieldData(getDatatypeCoder().encodeLocalDateTime(value));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimestampRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (setWhenNull(raw)) return;
        setFieldData(getDatatypeCoder().encodeTimestampRaw(raw));
    }
}
