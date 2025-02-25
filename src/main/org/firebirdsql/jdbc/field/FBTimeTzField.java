// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.time.OffsetTime;

/**
 * Field for {@code TIME WITH TIME ZONE}.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FBTimeTzField extends AbstractWithTimeZoneField {

    @NullMarked
    FBTimeTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getOffsetTime();
    }

    @Override
    public String getString() throws SQLException {
        OffsetTime offsetTime = getOffsetTime();
        return offsetTime != null ? offsetTime.toString() : null;
    }

}
