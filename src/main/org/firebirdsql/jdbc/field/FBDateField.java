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

/**
 * Describe class <code>FBDateField</code> here.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
final class FBDateField extends AbstractWithoutTimeZoneField {

    @NullMarked
    FBDateField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getDate();
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

    @Override
    public String getString() throws SQLException {
        return FbDatetimeConversion.formatSqlDate(getLocalDate());
    }

    @Override
    public void setString(String value) throws SQLException {
        setLocalDate(fromString(value, FbDatetimeConversion::parseSqlDate));
    }

    @Override
    void setLocalDateTime(LocalDateTime value) throws SQLException {
        setLocalDate(value != null ? value.toLocalDate() : null);
    }

    @Override
    void setLocalDate(LocalDate value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLocalDate(value));
    }

}
