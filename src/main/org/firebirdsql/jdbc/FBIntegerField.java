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
 * The class <code>FBIntegerField</code> represents an INTEGER datatype and performs all necessary
 * conversions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBIntegerField extends FBField {

    FBIntegerField(XSQLVAR field) throws SQLException {
        super(field);
    }

    byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        Integer value = (Integer)field.sqldata;

        // check if value is withing bounds
        if (value.intValue() > MAX_BYTE_VALUE ||
            value.intValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Integer)field.sqldata).byteValue();
    }
    short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        Integer value = (Integer)field.sqldata;

        // check if value is withing bounds
        if (value.intValue() > MAX_SHORT_VALUE ||
            value.intValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Integer)field.sqldata).shortValue();
    }
    int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        return ((Integer)field.sqldata).intValue();
    }
    long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;

        return ((Integer)field.sqldata).longValue();
    }
    float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        return ((Integer)field.sqldata).floatValue();
    }
    double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        return ((Integer)field.sqldata).doubleValue();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return BIGDECIMAL_NULL_VALUE;

        return BigDecimal.valueOf(((Integer)field.sqldata).longValue());
    }
    Object getObject() throws SQLException {
        if (isNull()) return OBJECT_NULL_VALUE;

        return field.sqldata;
    }
    boolean getBoolean() throws java.sql.SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;

        return ((Integer)field.sqldata).intValue() == 1;
    }
    String getString() throws SQLException {
        if (isNull()) return STRING_NULL_VALUE;

        return ((Integer)field.sqldata).toString();
    }
    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        try {
            setInteger(Integer.parseInt(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                INT_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    void setShort(short value) throws java.sql.SQLException {
        setInteger((int)value);
    }
    void setBoolean(boolean value) throws java.sql.SQLException {
        setInteger(value ? 1 : 0);
    }
    void setFloat(float value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw (SQLException)createException(
                    FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setInteger((int)value);
    }
    void setDouble(double value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setInteger((int)value);
    }
    void setLong(long value) throws java.sql.SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setInteger((int)value);
    }
    void setInteger(int value) throws java.sql.SQLException {
        field.sqldata = new Integer((int)value);
        setNull(false);
    }
    void setByte(byte value) throws java.sql.SQLException {
        setInteger((int)value);
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BigDecimal.valueOf(MAX_INT_VALUE)) > 0 ||
            value.compareTo(BigDecimal.valueOf(MIN_INT_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setInteger(value.intValue());
    }
}
