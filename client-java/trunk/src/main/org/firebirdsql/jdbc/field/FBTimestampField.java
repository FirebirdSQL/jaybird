 /*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc.field;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.util.Calendar;

import org.firebirdsql.gds.XSQLVAR;

/**
 * Describe class <code>FBTimestampField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBTimestampField extends FBField {
    
    FBTimestampField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }
    
    /*
    public Object getObject() throws SQLException {
        if (getFieldData()==null) return OBJECT_NULL_VALUE;

        return field.decodeTimestamp(getFieldData());
    }
    */
    
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeTimestamp(getFieldData()));
    }
    public Date getDate(Calendar cal) throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        return new java.sql.Date(field.decodeTimestampCalendar(getFieldData(),cal).getTime());
    }
    public Date getDate() throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        return new Date(getTimestamp().getTime());
    }
    public Time getTime(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        return new java.sql.Time(field.decodeTimestampCalendar(getFieldData(),cal).getTime());
    }
    public Time getTime() throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        return new Time(getTimestamp().getTime());
    }
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;
		  
        return field.decodeTimestampCalendar(getFieldData(),cal);
    }
    public Timestamp getTimestamp() throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;

        return field.decodeTimestamp(getFieldData());
    }
    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        setTimestamp(Timestamp.valueOf(value));
    }
    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()),cal));
    }
    public void setDate(Date value) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }
    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTimestampCalendar(new java.sql.Timestamp(value.getTime()),cal));
    }
    public void setTime(Time value) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTimestampCalendar(value,cal));
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTimestamp(value));
    }
}
