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

/**
 * Describe class <code>FBDateField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBDateField extends FBField {

    FBDateField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }

    Timestamp getTimestamp() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;

        return new Timestamp(getDate().getTime());
    }
    Date getDate() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return XSQLVAR.decodeDate(rs.row[numCol]);
    }
    String getString() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return XSQLVAR.decodeDate(rs.row[numCol]).toString();
    }
    Object getObject() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return XSQLVAR.decodeDate(rs.row[numCol]);
    }

    //--- setXXX methods
	 
    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setDate(Date.valueOf(value));
    }
    void setTimestamp(Timestamp value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setDate(new Date(value.getTime()));
    }
    void setDate(Date value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        field.sqldata = XSQLVAR.encodeDate(value);
    }
}
