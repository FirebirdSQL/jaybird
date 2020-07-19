/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import java.sql.SQLException;
import java.time.format.DateTimeParseException;

/**
 * Field for {@code TIME WITH TIME ZONE}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
class FBTimeTzField extends AbstractWithTimeZoneField {

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
        if (isNull()) return null;

        return String.valueOf(getOffsetTime());
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            setStringParse(value);
        } catch (DateTimeParseException e) {
            throw new TypeConversionException("Unable to convert value '" + value + "' to type TIME WITH TIME ZONE", e);
        }
    }

}
