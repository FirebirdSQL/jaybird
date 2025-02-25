/*
 SPDX-FileCopyrightText: Copyright 2002-2009 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.FbDatetimeConversion;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Field implementation for {@code TIMESTAMP (WITHOUT TIME ZONE)}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
class FBTimestampField extends AbstractWithoutTimeZoneField {

    @NullMarked
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
    LocalDate getLocalDate() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }

    @Override
    LocalTime getLocalTime() throws SQLException {
        LocalDateTime localDateTime = getLocalDateTime();
        return localDateTime != null ? localDateTime.toLocalTime() : null;
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
    void setLocalDate(LocalDate value) throws SQLException {
        setLocalDateTime(convertForSet(value, LocalDate::atStartOfDay, LocalDate.class));
    }

    @Override
    void setLocalTime(LocalTime value) throws SQLException {
        setLocalDateTime(convertForSet(value, LocalDate.EPOCH::atTime, LocalTime.class));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalDateTime(value));
    }

}
