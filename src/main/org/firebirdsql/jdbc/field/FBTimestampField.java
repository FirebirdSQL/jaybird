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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Describe class <code>FBTimestampField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBTimestampField extends FBField {

    FBTimestampField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getDatatypeCoder().decodeTimestamp(getFieldData()));
    }

    public Date getDate(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return new java.sql.Date(getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal).getTime());
    }

    public Date getDate() throws SQLException {
        if (isNull()) return null;

        return new Date(getTimestamp().getTime());
    }

    public Time getTime(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return new java.sql.Time(getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal).getTime());
    }

    public Time getTime() throws SQLException {
        if (isNull()) return null;

        return new Time(getTimestamp().getTime());
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;

        return getDatatypeCoder().decodeTimestampCalendar(getFieldData(), cal);
    }

    public Timestamp getTimestamp() throws SQLException {
        if (isNull()) return null;

        return getDatatypeCoder().decodeTimestamp(getFieldData());
    }

    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setTimestamp(Timestamp.valueOf(value));
    }

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()), cal));
    }

    public void setDate(Date value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()), cal));
    }

    public void setTime(Time value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeTimestampCalendar(value, cal));
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(getDatatypeCoder().encodeTimestamp(value));
    }

    @Override
    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimestampRaw(getFieldData());
    }

    @Override
    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        if (raw == null) {
            setNull();
            return;
        }
        setFieldData(getDatatypeCoder().encodeTimestampRaw(raw));
    }
}
