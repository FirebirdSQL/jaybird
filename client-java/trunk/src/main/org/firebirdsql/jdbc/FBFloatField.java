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

/*
 * This class represents a FLOAT datatype and performs all necessary
 * conversions.
 */
/**
 * The class <code>FBFloatField</code>represents a FLOAT datatype and performs all necessary
 * conversions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBFloatField extends FBField {
    FBFloatField(XSQLVAR field, Object[] row, int numCol) throws SQLException {
        super(field, row, numCol);
    }

    byte getByte() throws SQLException {
        if (row[numCol]==null) return BYTE_NULL_VALUE;

        Float value = (Float)row[numCol];

        // check if value is withing bounds
        if (value.floatValue() > MAX_BYTE_VALUE ||
            value.floatValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)row[numCol]).byteValue();
    }
    short getShort() throws SQLException {
        if (row[numCol]==null) return SHORT_NULL_VALUE;

        Float value = (Float)row[numCol];

        // check if value is withing bounds
        if (value.floatValue() > MAX_SHORT_VALUE ||
            value.floatValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)row[numCol]).shortValue();
    }
    int getInt() throws SQLException {
        if (row[numCol]==null) return INT_NULL_VALUE;

        Float value = (Float)row[numCol];

        // check if value is withing bounds
        if (value.floatValue() > MAX_INT_VALUE ||
            value.floatValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)row[numCol]).intValue();
    }
    long getLong() throws SQLException {
        if (row[numCol]==null) return LONG_NULL_VALUE;

        Float value = (Float)row[numCol];

        // check if value is withing bounds
        if (value.floatValue() > MAX_LONG_VALUE ||
            value.floatValue() < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return ((Float)row[numCol]).longValue();
    }
    float getFloat() throws SQLException {
        if (row[numCol]==null) return FLOAT_NULL_VALUE;

        return ((Float)row[numCol]).floatValue();
    }
    double getDouble() throws SQLException {
        if (row[numCol]==null) return DOUBLE_NULL_VALUE;

        return ((Float)row[numCol]).doubleValue();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        if (row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        return new java.math.BigDecimal(((Float)row[numCol]).doubleValue());
    }
    Object getObject() throws SQLException {
        if (row[numCol]==null) return OBJECT_NULL_VALUE;

        return row[numCol];
    }
    boolean getBoolean() throws java.sql.SQLException {
        if (row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return ((Float)row[numCol]).intValue() == 1;
    }
    String getString() throws SQLException {
        if (row[numCol]==null) return STRING_NULL_VALUE;

        return ((Float)row[numCol]).toString();
    }

    //--- setXXX methods

    void setString(String value) throws java.sql.SQLException {
        if (value == null) {
            field.sqldata = null;
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
    }
    void setByte(byte value) throws java.sql.SQLException {
        setFloat((float)value);
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
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
