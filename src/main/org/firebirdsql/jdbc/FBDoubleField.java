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

import org.firebirdsql.gds.XSQLVAR;
import java.sql.SQLException;
import java.math.BigDecimal;

/**
 * Describe class <code>FBDoubleField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBDoubleField extends FBField {
    FBDoubleField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }

    byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        Double value = (Double)rs.row[numCol];

        // check if value is withing bounds
        if (value.doubleValue() > MAX_BYTE_VALUE ||
            value.doubleValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        Double value = (Double)rs.row[numCol];

        // check if value is withing bounds
        if (value.doubleValue() > MAX_SHORT_VALUE ||
            value.doubleValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.shortValue();
    }
    int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        Double value = (Double)rs.row[numCol];

        // check if value is withing bounds
        if (value.doubleValue() > MAX_INT_VALUE ||
            value.doubleValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.intValue();
    }
    long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        Double value = (Double)rs.row[numCol];

        // check if value is withing bounds
        if (value.doubleValue() > MAX_LONG_VALUE ||
            value.doubleValue() < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.longValue();
    }
    float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        Double value = (Double)rs.row[numCol];
        float cValue = value.floatValue();
        // check if value is withing bounds
        if (cValue == Float.POSITIVE_INFINITY || cValue == Float.NEGATIVE_INFINITY)
            throw (SQLException)createException(
                FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.floatValue();
    }
    double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        return ((Double)rs.row[numCol]).doubleValue();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        return new java.math.BigDecimal(((Double)rs.row[numCol]).doubleValue());
    }
    Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return (Double)rs.row[numCol];
    }
    boolean getBoolean() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return ((Double)rs.row[numCol]).intValue() == 1;
    }
    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return String.valueOf((Double)rs.row[numCol]);
    }

    //--- setXXX methods

    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        try {
            setDouble(Double.parseDouble(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    void setShort(short value) throws java.sql.SQLException {
        setDouble((double)value);
    }
    void setBoolean(boolean value) throws java.sql.SQLException {
        setDouble(value ? 1 : 0);
    }
    void setFloat(float value) throws java.sql.SQLException {
        setDouble((double)value);
    }
    void setDouble(double value) throws java.sql.SQLException {
        field.sqldata = new Double(value);
    }
    void setLong(long value) throws java.sql.SQLException {
        setDouble((double)value);
    }
    void setInteger(int value) throws java.sql.SQLException {
        setDouble((double)value);
    }
    void setByte(byte value) throws java.sql.SQLException {
        setDouble((double)value);
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        // check if value is within bounds
        if (value.compareTo(new BigDecimal(MAX_DOUBLE_VALUE)) > 0 ||
            value.compareTo(new BigDecimal(MIN_DOUBLE_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setDouble(value.doubleValue());
    }
}
