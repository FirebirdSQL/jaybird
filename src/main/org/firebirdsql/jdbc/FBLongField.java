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
 * This class represents LONG datatype and performs all necessary type
 * conversions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBLongField extends FBField {
    FBLongField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }

    byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        Long value = (Long)rs.row[numCol];

        // check if value is withing bounds
        if (value.longValue() > MAX_BYTE_VALUE ||
            value.longValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        Long value = (Long)rs.row[numCol];

        // check if value is withing bounds
        if (value.longValue() > MAX_SHORT_VALUE ||
            value.longValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.shortValue();
    }
    int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        Long value = (Long)rs.row[numCol];

        // check if value is withing bounds
        if (value.longValue() > MAX_INT_VALUE ||
            value.longValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.intValue();
    }
    long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        return ((Long)rs.row[numCol]).longValue();
    }
    float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        return ((Long)rs.row[numCol]).floatValue();
    }
    double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        return ((Long)rs.row[numCol]).doubleValue();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        return BigDecimal.valueOf(((Long)rs.row[numCol]).longValue());
    }
    Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return rs.row[numCol];
    }
    boolean getBoolean() throws java.sql.SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return ((Long)rs.row[numCol]).intValue() == 1;
    }
    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return ((Long)rs.row[numCol]).toString();
    }

    //--- setXXX methods

    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        try {
            setLong(Long.parseLong(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    void setShort(short value) throws java.sql.SQLException {
        setLong((long)value);
    }
    void setBoolean(boolean value) throws java.sql.SQLException {
        setLong(value ? 1 : 0);
    }
    void setFloat(float value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setLong((long)value);
    }
    void setDouble(double value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setLong((long)value);
    }
    void setLong(long value) throws java.sql.SQLException {
        field.sqldata = new Long(value);
    }
    void setInteger(int value) throws java.sql.SQLException {
        setLong((long)value);
    }
    void setByte(byte value) throws java.sql.SQLException {
        setLong((long)value);
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BigDecimal.valueOf(MAX_LONG_VALUE)) > 0 ||
            value.compareTo(BigDecimal.valueOf(MIN_LONG_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setLong(value.longValue());
    }
}
