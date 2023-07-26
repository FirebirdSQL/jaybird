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
import org.firebirdsql.jaybird.util.FbDatetimeConversion;

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
        Date date = getDate(cal);
        return date != null ? new Timestamp(date.getTime()) : null;
    }

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        return getDatatypeCoder().decodeDateCalendar(getFieldData(), cal);
    }

    @Override
    LocalDate getLocalDate() throws SQLException {
        return getDatatypeCoder().decodeLocalDate(getFieldData());
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        LocalDate localDate = getLocalDate();
        return localDate != null ? localDate.atStartOfDay() : null;
    }

    public String getString() throws SQLException {
        return FbDatetimeConversion.formatSqlDate(getLocalDate());
    }

    @Override
    public void setString(String value) throws SQLException {
        setLocalDate(fromString(value, FbDatetimeConversion::parseSqlDate));
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        setDate(value != null ? new Date(value.getTime()) : null, cal);
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalDate(value != null ? value.toLocalDate() : null);
    }

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        setFieldData(getDatatypeCoder().encodeDateCalendar(value, cal));
    }

    @Override
    void setLocalDate(LocalDate value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalDate(value));
    }

    @SuppressWarnings("removal")
    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        return convertForGet(getLocalDate(),
                v -> {
                    var raw = new DatatypeCoder.RawDateTimeStruct();
                    raw.updateDate(v);
                    return raw;
                },
                DatatypeCoder.RawDateTimeStruct.class);
    }

    @SuppressWarnings("removal")
    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        setLocalDate(convertForSet(raw, DatatypeCoder.RawDateTimeStruct::toLocalDate,
                DatatypeCoder.RawDateTimeStruct.class));
    }
    
}
