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
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * This class represents a field of type SHORT and performs all necessary
 * conversions.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class FBShortField extends FBField {

    FBShortField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }


    byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        Short value = new Short(field.decodeShort(rs.row[numCol]));

        // check if value is withing bounds
        if (value.shortValue() > MAX_BYTE_VALUE ||
            value.shortValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]);
    }
    int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]);
    }
    long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]);
    }
    float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]);
    }
    double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]);
    }
    BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        return BigDecimal.valueOf(field.decodeShort(rs.row[numCol]));
    }
    Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return new Short(field.decodeShort(rs.row[numCol]));
    }
    boolean getBoolean() throws SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return field.decodeShort(rs.row[numCol]) == 1;
    }
    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeShort(rs.row[numCol]));
    }

    //--- setXXX methods

    void setString(String value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        try {
            setShort(Short.parseShort(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    void setShort(short value) throws SQLException {
        field.sqldata = field.encodeShort(value);
    }
    void setBoolean(boolean value) throws SQLException {
        setShort((short)(value ? 1 : 0));
    }
    void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setShort((short)value);
    }
    void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setShort((short)value);
    }
    void setLong(long value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setShort((short)value);
    }
    void setInteger(int value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setShort((short)value);
    }
    void setByte(byte value) throws SQLException {
        setShort((short)value);
    }
    void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BigDecimal.valueOf(MAX_SHORT_VALUE)) > 0 ||
            value.compareTo(BigDecimal.valueOf(MIN_SHORT_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setShort(value.shortValue());
    }

}
