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

package org.firebirdsql.jdbc;

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
    FBTimestampField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }
    Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return XSQLVAR.decodeTimestamp(rs.row[numCol]);
    }
    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return String.valueOf(XSQLVAR.decodeTimestamp(rs.row[numCol]));
    }
    Date getDate(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return XSQLVAR.decodeDate(getDate(),cal);
    }
    Date getDate() throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return new Date(getTimestamp().getTime());
    }
    Time getTime(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return XSQLVAR.decodeTime(getTime(),cal);
    }
    Time getTime() throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return new Time(getTimestamp().getTime());
    }
    Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;
		  
        return XSQLVAR.decodeTimestamp(getTimestamp(),cal);
    }
    Timestamp getTimestamp() throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;

        return XSQLVAR.decodeTimestamp(rs.row[numCol]);
    }
    //--- setXXX methods

    void setString(String value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTimestamp(Timestamp.valueOf(value));
    }
    void setDate(Date value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setDate(XSQLVAR.encodeDate(value,cal));
    }
    void setDate(Date value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }
    void setTime(Time value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTime(XSQLVAR.encodeTime(value,cal));
    }
    void setTime(Time value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTimestamp(new Timestamp(value.getTime()));
    }
    void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTimestamp(XSQLVAR.encodeTimestamp(value,cal));
    }
    void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        field.sqldata = XSQLVAR.encodeTimestamp(value);
    }
}
