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

import org.firebirdsql.gds.XSQLVAR;
import java.sql.SQLException;
import java.math.BigDecimal;

/**
 * This class represents a FLOAT datatype and performs all necessary
 * conversions.
 */
class FBFloatField extends FBField {
    FBFloatField(XSQLVAR field) throws SQLException {
        super(field);
    }

    byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        Float value = (Float)field.sqldata;

        // check if value is withing bounds
        if (value.floatValue() > MAX_BYTE_VALUE ||
            value.floatValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)field.sqldata).byteValue();
    }
    short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        Float value = (Float)field.sqldata;

        // check if value is withing bounds
        if (value.floatValue() > MAX_SHORT_VALUE ||
            value.floatValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)field.sqldata).shortValue();
    }
    int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        Float value = (Float)field.sqldata;

        // check if value is withing bounds
        if (value.floatValue() > MAX_INT_VALUE ||
            value.floatValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)field.sqldata).intValue();
    }
    long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;

        Float value = (Float)field.sqldata;

        // check if value is withing bounds
        if (value.floatValue() > MAX_LONG_VALUE ||
            value.floatValue() < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)field.sqldata).longValue();
    }
    float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        return ((Float)field.sqldata).floatValue();
    }
    double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        return ((Float)field.sqldata).doubleValue();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return BIGDECIMAL_NULL_VALUE;

        return new java.math.BigDecimal(((Float)field.sqldata).doubleValue());
    }
    Object getObject() throws SQLException {
        if (isNull()) return OBJECT_NULL_VALUE;

        return field.sqldata;
    }
    boolean getBoolean() throws java.sql.SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;

        return ((Float)field.sqldata).intValue() == 1;
    }
    String getString() throws SQLException {
        if (isNull()) return STRING_NULL_VALUE;

        return ((Float)field.sqldata).toString();
    }
    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        try {
            setFloat(Float.parseFloat(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    void setShort(short value) throws java.sql.SQLException {
        setFloat((float)value);
    }
    void setBoolean(boolean value) throws java.sql.SQLException {
        setFloat(value ? 1.0f : 0.0f);
    }
    void setFloat(float value) throws java.sql.SQLException {
        field.sqldata = new Float((float)value);
        setNull(false);
    }
    void setDouble(double value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_FLOAT_VALUE ||
            value < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat((float)value);
    }
    void setLong(long value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_FLOAT_VALUE ||
            value < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat((float)value);
    }
    void setInteger(int value) throws java.sql.SQLException {
        setFloat((float)value);
        setNull(false);
    }
    void setByte(byte value) throws java.sql.SQLException {
        setFloat((float)value);
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        // check if value is within bounds
        if (value.compareTo(new BigDecimal(MAX_FLOAT_VALUE)) > 0 ||
            value.compareTo(new BigDecimal(MIN_FLOAT_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat(value.floatValue());
    }
}
