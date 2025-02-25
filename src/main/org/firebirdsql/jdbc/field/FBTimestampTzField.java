// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.jspecify.annotations.NullMarked;

import java.sql.Date;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Calendar;

/**
 * Field for {@code TIMESTAMP WITH TIME ZONE}.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FBTimestampTzField extends AbstractWithTimeZoneField {

    @NullMarked
    FBTimestampTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getOffsetDateTime();
    }

    @Override
    public Date getDate() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        return offsetDateTime != null ? new Date(offsetDateTime.toInstant().toEpochMilli()) : null;
    }

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        return getDate();
    }

    @Override
    public void setDate(Date value) throws SQLException {
        if (setWhenNull(value)) return;
        setOffsetDateTime(value.toLocalDate().atStartOfDay().atZone(getDefaultZoneId()).toOffsetDateTime());
    }

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        setDate(value);
    }

    @Override
    public String getString() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        return offsetDateTime != null ? offsetDateTime.toString() : null;
    }

}
