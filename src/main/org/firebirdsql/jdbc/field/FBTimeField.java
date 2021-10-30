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

import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.Time;

/**
 * Field implementation for {@code TIME (WITHOUT TIME ZONE)}.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBTimeField extends AbstractWithoutTimeZoneField {

    FBTimeField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getTime();
    }

    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getTime());
    }

    public Time getTime(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimeCalendar(getFieldData(), cal);
    }

    @Override
    LocalTime getLocalTime() throws SQLException {
        if (isNull()) return null;
        // TODO Push down into DatatypeCoder
        final DatatypeCoder.RawDateTimeStruct raw = getDatatypeCoder().decodeTimeRaw(getFieldData());
        return LocalTime.of(raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos());
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return new java.sql.Timestamp(getDatatypeCoder().decodeTimeCalendar(getFieldData(), cal).getTime());
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        LocalTime localTime = getLocalTime();
        return localTime != null ? localTime.atDate(LocalDate.EPOCH) : null;
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setTime(Time.valueOf(string));
        } catch (RuntimeException e) {
            SQLException conversionException = invalidSetConversion(String.class, string);
            conversionException.initCause(e);
            throw conversionException;
        }
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;
        setFieldData(getDatatypeCoder().encodeTimeCalendar(new java.sql.Time(value.getTime()), cal));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalTime(value != null ? value.toLocalTime() : null);
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeTimeCalendar(value, cal));
    }

    @Override
    void setLocalTime(LocalTime value) throws SQLException {
        if (setWhenNull(value)) return;
        // TODO Push down into DatatypeCoder
        setFieldData(getDatatypeCoder().encodeLocalTime(
                value.getHour(), value.getMinute(), value.getSecond(), value.getNano()));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimeRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (setWhenNull(raw)) return;
        setFieldData(getDatatypeCoder().encodeTimeRaw(raw));
    }
}
