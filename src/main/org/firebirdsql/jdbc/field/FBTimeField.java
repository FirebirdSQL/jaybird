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
 * Field implementation for {@code TIME (WITHOUT TIME ZONE)}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
final class FBTimeField extends AbstractWithoutTimeZoneField {

    @NullMarked
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
    LocalTime getLocalTime() throws SQLException {
        return getDatatypeCoder().decodeLocalTime(getFieldData());
    }

    @Override
    LocalDateTime getLocalDateTime() throws SQLException {
        LocalTime localTime = getLocalTime();
        return localTime != null ? localTime.atDate(LocalDate.EPOCH) : null;
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        setLocalTime(fromString(value, FbDatetimeConversion::parseSqlTime));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalTime(value != null ? value.toLocalTime() : null);
    }

    @Override
    void setLocalTime(LocalTime value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalTime(value));
    }

}
