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
