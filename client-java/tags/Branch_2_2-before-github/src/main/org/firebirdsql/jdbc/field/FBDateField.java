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
import java.util.Calendar;
import java.sql.Date;
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;

/**
 * Describe class <code>FBDateField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBDateField extends FBField {

    FBDateField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException
    {
        super(field, dataProvider, requiredType);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;
		  
        return new java.sql.Timestamp(field.decodeDateCalendar(getFieldData(),cal).getTime());
    }
    public Timestamp getTimestamp() throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;

        return new Timestamp(getDate().getTime());
    }
    public Date getDate(Calendar cal) throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        return field.decodeDateCalendar(getFieldData(),cal);
    }
    public Date getDate() throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        return field.decodeDate(getFieldData());
    }
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return field.decodeDate(getFieldData()).toString();
    }
    
    //--- setXXX methods
	 
    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        setDate(Date.valueOf(value));
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeDateCalendar(new java.sql.Date(value.getTime()),cal));
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setDate(new Date(value.getTime()));
    }
    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeDateCalendar(value,cal));
    }
    public void setDate(Date value) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(field.encodeDate(value));
    }
}
