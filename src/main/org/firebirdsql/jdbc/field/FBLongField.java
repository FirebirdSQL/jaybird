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

package org.firebirdsql.jdbc.field;

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
    FBLongField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (getRow(numCol)==null) return BYTE_NULL_VALUE;

        Long value = new Long(field.decodeLong(getRow(numCol)));

        // check if value is withing bounds
        if (value.longValue() > MAX_BYTE_VALUE ||
            value.longValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    public short getShort() throws SQLException {
        if (getRow(numCol)==null) return SHORT_NULL_VALUE;

        Long value = new Long(field.decodeLong(getRow(numCol)));

        // check if value is withing bounds
        if (value.longValue() > MAX_SHORT_VALUE ||
            value.longValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.shortValue();
    }
    public int getInt() throws SQLException {
        if (getRow(numCol)==null) return INT_NULL_VALUE;

        Long value = new Long(field.decodeLong(getRow(numCol)));

        // check if value is withing bounds
        if (value.longValue() > MAX_INT_VALUE ||
            value.longValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.intValue();
    }
    public long getLong() throws SQLException {
        if (getRow(numCol)==null) return LONG_NULL_VALUE;

        return field.decodeLong(getRow(numCol));
    }
    public float getFloat() throws SQLException {
        if (getRow(numCol)==null) return FLOAT_NULL_VALUE;

        return (float) field.decodeLong(getRow(numCol));
    }
    public double getDouble() throws SQLException {
        if (getRow(numCol)==null) return DOUBLE_NULL_VALUE;

        return (double) field.decodeLong(getRow(numCol));
    }
    public BigDecimal getBigDecimal() throws SQLException {
        if (getRow(numCol)==null) return BIGDECIMAL_NULL_VALUE;

        return BigDecimal.valueOf(field.decodeLong(getRow(numCol)));
    }
    
    /*
    public Object getObject() throws SQLException {
        if (getRow(numCol)==null) return OBJECT_NULL_VALUE;

        return new Long(field.decodeLong(getRow(numCol)));
    }
    */
    
    public boolean getBoolean() throws SQLException {
        if (getRow(numCol)==null) return BOOLEAN_NULL_VALUE;

        return field.decodeLong(getRow(numCol)) == 1;
    }
    public String getString() throws SQLException {
        if (getRow(numCol)==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeLong(getRow(numCol)));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        try {
            setLong(Long.parseLong(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    public void setShort(short value) throws SQLException {
        setLong((long)value);
    }
    public void setBoolean(boolean value) throws SQLException {
        setLong(value ? 1 : 0);
    }
    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setLong((long)value);
    }
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setLong((long)value);
    }
    public void setLong(long value) throws SQLException {
        field.sqldata = field.encodeLong(value);
    }
    public void setInteger(int value) throws SQLException {
        setLong((long)value);
    }
    public void setByte(byte value) throws SQLException {
        setLong((long)value);
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
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
