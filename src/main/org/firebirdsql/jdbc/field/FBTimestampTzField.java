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
