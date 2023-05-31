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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * Describe class <code>FBDateField</code> here.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
final class FBDateField extends AbstractWithoutTimeZoneField {

    FBDateField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getDate();
    }

    @Override
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return new java.sql.Timestamp(getDatatypeCoder().decodeDateCalendar(getFieldData(), cal).getTime());
    }

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDateCalendar(getFieldData(), cal);
    }

    @Override
    LocalDate getLocalDate() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeLocalDate(getFieldData());
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        LocalDate localDate = getLocalDate();
        return localDate != null ? localDate.atStartOfDay() : null;
    }

    public String getString() throws SQLException {
        LocalDate localDate = getLocalDate();
        return localDate != null ? localDate.toString() : null;
    }

    @Override
    public void setString(String value) throws SQLException {
        setDate(fromString(value, Date::valueOf));
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeDateCalendar(new java.sql.Date(value.getTime()), cal));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalDate(value != null ? value.toLocalDate() : null);
    }

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        if (setWhenNull(value)) return;

        setFieldData(getDatatypeCoder().encodeDateCalendar(value, cal));
    }

    @Override
    void setLocalDate(LocalDate value) throws SQLException {
        if (setWhenNull(value)) return;
        setFieldData(getDatatypeCoder().encodeLocalDate(value));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDateRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (setWhenNull(raw)) return;
        setFieldData(getDatatypeCoder().encodeDateRaw(raw));
    }
}
