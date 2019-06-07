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

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Calendar;

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.Time;

/**
 * Field implementation for {@code TIME (WITHOUT TIME ZONE)}.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBTimeField extends AbstractWithoutTimeZoneField {

    FBTimeField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getTime());
    }

    public Time getTime(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimeCalendar(getFieldData(), cal);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return new java.sql.Timestamp(getDatatypeCoder().decodeTimeCalendar(getFieldData(), cal).getTime());
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }
        try {
            setTime(Time.valueOf(value));
        } catch (RuntimeException e) {
            throw new TypeConversionException(TIME_CONVERSION_ERROR, e);
        }
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }
        setFieldData(getDatatypeCoder().encodeTimeCalendar(new java.sql.Time(value.getTime()), cal));
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeTimeCalendar(value, cal));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimeRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (raw == null) {
            setNull();
            return;
        }
        setFieldData(getDatatypeCoder().encodeTimeRaw(raw));
    }
}
