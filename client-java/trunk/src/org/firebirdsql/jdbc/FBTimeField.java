/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Roman Rokytskyy, David Jencks
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import java.sql.Time;

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