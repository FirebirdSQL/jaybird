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

import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.FbDatetimeConversion;

import java.sql.Time;

/**
 * Field implementation for {@code TIME (WITHOUT TIME ZONE)}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
final class FBTimeField extends AbstractWithoutTimeZoneField {

    private static final LocalDate LOCAL_DATE_EPOCH = LocalDate.of(1970, 1, 1);

    FBTimeField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getTime();
    }

    @Override
    public String getString() throws SQLException {
        return convertForGet(getLocalTime(), FbDatetimeConversion::formatSqlTime, String.class);
    }

    @Override
    public Time getTime(Calendar cal) throws SQLException {
        return getDatatypeCoder().decodeTimeCalendar(getFieldData(), cal);
    }

    @Override
    LocalTime getLocalTime() throws SQLException {
        return getDatatypeCoder().decodeLocalTime(getFieldData());
    }

    @Override
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        Time time = getTime(cal);
        return time != null ? new Timestamp(time.getTime()) : null;
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        LocalTime localTime = getLocalTime();
        return localTime != null ? localTime.atDate(LOCAL_DATE_EPOCH) : null;
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        setLocalTime(fromString(value, FbDatetimeConversion::parseSqlTime));
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        setTime(value != null ? new Time(value.getTime()) : null, cal);
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalTime(value != null ? value.toLocalTime() : null);
    }

    @Override
    public void setTime(Time value, Calendar cal) throws SQLException {
        setFieldData(getDatatypeCoder().encodeTimeCalendar(value, cal));
    }

    @Override
    void setLocalTime(LocalTime value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalTime(value));
    }

    @SuppressWarnings("removal")
    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        return convertForGet(getLocalTime(),
                v -> {
                    var raw = new DatatypeCoder.RawDateTimeStruct();
                    raw.updateTime(v);
                    return raw;
                }, DatatypeCoder.RawDateTimeStruct.class);
    }

    @SuppressWarnings("removal")
    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        setLocalTime(convertForSet(raw, DatatypeCoder.RawDateTimeStruct::toLocalTime,
                DatatypeCoder.RawDateTimeStruct.class));
    }

}
