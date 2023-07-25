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

import org.firebirdsql.gds.ng.DatatypeCoder.RawDateTimeStruct;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.FbDatetimeConversion;

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

    @Override
    public String getString() throws SQLException {
        return convertForGet(getLocalDateTime(), FbDatetimeConversion::formatSqlTimestamp, String.class);
    }

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        Timestamp timestamp = getTimestamp(cal);
        return timestamp != null ? new Date(timestamp.getTime()) : null;
    }

    @Override
    LocalDate getLocalDate() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }

    @Override
    public Time getTime(Calendar cal) throws SQLException {
        Timestamp timestamp = getTimestamp(cal);
        return timestamp != null ? new Time(timestamp.getTime()) : null;
    }

    @Override
    LocalTime getLocalTime() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalTime() : null;
    }

    @Override
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        return getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal);
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        return getDatatypeCoder().decodeLocalDateTime(getFieldData());
    }

    @Override
    public void setString(String value) throws SQLException {
        setLocalDateTime(fromString(value, FbDatetimeConversion::parseIsoOrSqlTimestamp));
    }

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        setTimestamp(value != null ? new Timestamp(value.getTime()) : null, cal);
    }

    @Override
    public void setTime(Time value, Calendar cal) throws SQLException {
        setTimestamp(value != null ? new Timestamp(value.getTime()) : null, cal);
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        setFieldData(getDatatypeCoder().encodeTimestampCalendar(value, cal));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalDateTime(value));
    }

    @Override
    public RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        return convertForGet(getLocalDateTime(),
                v -> {
                    var raw = new RawDateTimeStruct();
                    raw.updateDateTime(v);
                    return raw;
                },
                RawDateTimeStruct.class);
    }

    @Override
    public void setRawDateTimeStruct(RawDateTimeStruct raw) throws SQLException {
        setLocalDateTime(convertForSet(raw, RawDateTimeStruct::toLocalDateTime, RawDateTimeStruct.class));
    }
    
}
