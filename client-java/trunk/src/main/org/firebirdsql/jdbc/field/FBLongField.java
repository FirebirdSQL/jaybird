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
    private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(MAX_LONG_VALUE);
    private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(MIN_LONG_VALUE);

    FBLongField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        long value = field.decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE ||
            value < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (byte) value;
    }
    
    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        long value = field.decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (short) value;
    }
    
    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        long value = field.decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (int) value;
    }
    
    public long getLong() throws SQLException {
        if (getFieldData()==null) return LONG_NULL_VALUE;

        return field.decodeLong(getFieldData());
    }
    
    public float getFloat() throws SQLException {
        if (getFieldData()==null) return FLOAT_NULL_VALUE;

        return (float) field.decodeLong(getFieldData());
    }
    
    public double getDouble() throws SQLException {
        if (getFieldData()==null) return DOUBLE_NULL_VALUE;

        return (double) field.decodeLong(getFieldData());
    }
    
    public BigDecimal getBigDecimal() throws SQLException {
        if (getFieldData()==null) return BIGDECIMAL_NULL_VALUE;

        return BigDecimal.valueOf(field.decodeLong(getFieldData()));
    }
    
    /*
    public Object getObject() throws SQLException {
        if (getFieldData()==null) return OBJECT_NULL_VALUE;

        return new Long(field.decodeLong(getFieldData()));
    }
    */
    
    public boolean getBoolean() throws SQLException {
        if (getFieldData()==null) return BOOLEAN_NULL_VALUE;

        return field.decodeLong(getFieldData()) == 1;
    }
    
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeLong(getFieldData()));
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
                LONG_CONVERSION_ERROR + " " + value).fillInStackTrace();
        }
    }
    
    public void setShort(short value) throws SQLException {
        setLong(value);
    }
    
    public void setBoolean(boolean value) throws SQLException {
        setLong(value ? 1 : 0);
    }
    
    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR + " " + value).fillInStackTrace();

        setLong((long) value);
    }
    
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR + " " + value).fillInStackTrace();

        setLong((long) value);
    }
    
    public void setLong(long value) throws SQLException {
        setFieldData(field.encodeLong(value));
    }
    
    public void setInteger(int value) throws SQLException {
        setLong(value);
    }
    
    public void setByte(byte value) throws SQLException {
        setLong(value);
    }
    
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BD_MAX_LONG) > 0 ||
            value.compareTo(BD_MIN_LONG) < 0)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR + " " + value).fillInStackTrace();

        setLong(value.longValue());
    }
}
