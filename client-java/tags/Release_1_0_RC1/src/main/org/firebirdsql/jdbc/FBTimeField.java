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
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import java.sql.Time;

/**
 * Describe class <code>FBTimeField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBTimeField extends FBField {
    FBTimeField(XSQLVAR field) throws SQLException {
        super(field);
    }

    Object getObject() throws java.sql.SQLException {
        if (isNull()) return OBJECT_NULL_VALUE;

        return field.sqldata;
    }
    String getString() throws java.sql.SQLException {
        if (isNull()) return STRING_NULL_VALUE;

        return field.sqldata.toString();
    }
    Time getTime() throws java.sql.SQLException {
        if (isNull()) return TIME_NULL_VALUE;

        return (Time)field.sqldata;
    }
    Timestamp getTimestamp() throws java.sql.SQLException {
        if (isNull()) return TIMESTAMP_NULL_VALUE;

        return new Timestamp(getTime().getTime());
    }
    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            setNull(true);
            return;
        }
        setTime(Time.valueOf(value));
    }
    void setTimestamp(Timestamp value) throws java.sql.SQLException {
        if (value == null) {
            setNull(true);
            return;
        }
        setTime(new Time(value.getTime()));
    }
    void setTime(Time value) throws java.sql.SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        field.sqldata = value;
        setNull(false);
    }
}
