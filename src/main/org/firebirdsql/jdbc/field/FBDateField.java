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

import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Describe class <code>FBDateField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBDateField extends FBField {

    FBDateField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return new java.sql.Timestamp(getDatatypeCoder().decodeDateCalendar(getFieldData(), cal).getTime());
    }

    public Timestamp getTimestamp() throws SQLException {
        if (isNull()) return null;
        return new Timestamp(getDate().getTime());
    }

    public Date getDate(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDateCalendar(getFieldData(), cal);
    }

    public Date getDate() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDate(getFieldData());
    }

    public String getString() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDate(getFieldData()).toString();
    }

    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setDate(Date.valueOf(value));
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeDateCalendar(new java.sql.Date(value.getTime()), cal));
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setDate(new Date(value.getTime()));
    }

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeDateCalendar(value, cal));
    }

    public void setDate(Date value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeDate(value));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDateRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (raw == null) {
            setNull();
            return;
        }
        setFieldData(getDatatypeCoder().encodeDateRaw(raw));
    }
}
