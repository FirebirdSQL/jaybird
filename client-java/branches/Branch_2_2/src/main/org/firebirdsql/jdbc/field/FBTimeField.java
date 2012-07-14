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
import java.sql.SQLException;
import java.util.Calendar;
import org.firebirdsql.gds.XSQLVAR;

import java.sql.Time;

/**
 * Describe class <code>FBTimeField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBTimeField extends FBField {
    FBTimeField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeTime(getFieldData()));
    }
    public Time getTime(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        return field.decodeTimeCalendar(getFieldData(),cal);
    }
    public Time getTime() throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        return field.decodeTime(getFieldData());
    }
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;
		  
        return new java.sql.Timestamp(field.decodeTimeCalendar(getFieldData(),cal).getTime());
    }
    public Timestamp getTimestamp() throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;

        return new Timestamp(getTime().getTime());
    }
    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        setTime(Time.valueOf(value));
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }
        setFieldData(field.encodeTimeCalendar(new java.sql.Time(value.getTime()),cal));
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }
        setTime(new Time(value.getTime()));
    }
    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTimeCalendar(value,cal));
    }
    public void setTime(Time value) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeTime(value));
    }
}
