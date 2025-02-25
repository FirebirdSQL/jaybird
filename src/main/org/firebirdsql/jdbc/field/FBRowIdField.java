// SPDX-FileCopyrightText: Copyright 2017-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;

/**
 * Field for row id fields (DB_KEY/RDB$DB_KEY).
 * <p>
 * The implementation inherits from {@link FBBinaryField} so it can still behave in a backwards-compatible manner
 * with previous Jaybird versions (except for the behavior of {@link #getObject()}).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FBRowIdField extends FBBinaryField {

    @NullMarked
    FBRowIdField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getRowId();
    }
}
