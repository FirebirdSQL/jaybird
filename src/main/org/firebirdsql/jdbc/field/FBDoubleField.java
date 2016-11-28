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
 * Describe class <code>FBDoubleField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBDoubleField extends FBField {
    FBDoubleField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        Double value = new Double(field.decodeDouble(getFieldData()));

        // check if value is withing bounds
        if (value.doubleValue() > MAX_BYTE_VALUE ||
            value.doubleValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        Double value = new Double(field.decodeDouble(getFieldData()));

        // check if value is withing bounds
        if (value.doubleValue() > MAX_SHORT_VALUE ||
            value.doubleValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.shortValue();
    }
    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        Double value = new Double(field.decodeDouble(getFieldData()));

        // check if value is withing bounds
        if (value.doubleValue() > MAX_INT_VALUE ||
            value.doubleValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.intValue();
    }
    public long getLong() throws SQLException {
        if (getFieldData()==null) return LONG_NULL_VALUE;

        Double value = new Double(field.decodeDouble(getFieldData()));

        // check if value is withing bounds
        if (value.doubleValue() > MAX_LONG_VALUE ||
            value.doubleValue() < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.longValue();
    }
    public float getFloat() throws SQLException {
        if (getFieldData()==null) return FLOAT_NULL_VALUE;

        Double value = new Double(field.decodeDouble(getFieldData()));
        float cValue = value.floatValue();
        // check if value is withing bounds
        if (cValue == Float.POSITIVE_INFINITY || cValue == Float.NEGATIVE_INFINITY)
            throw (SQLException)createException(
                FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.floatValue();
    }
    public double getDouble() throws SQLException {
        if (getFieldData()==null) return DOUBLE_NULL_VALUE;

        double result = field.decodeDouble(getFieldData());
        
        if (field.sqlscale != 0) {
            BigDecimal tempValue = new BigDecimal(result);
            tempValue = tempValue.setScale(Math.abs(field.sqlscale), BigDecimal.ROUND_HALF_EVEN);
            result = tempValue.doubleValue();
        }
        
        return result;
    }
    public BigDecimal getBigDecimal() throws SQLException {
        if (getFieldData()==null) return BIGDECIMAL_NULL_VALUE;

        BigDecimal result = new BigDecimal(field.decodeDouble(getFieldData()));
        
        if (field.sqlscale != 0)
            result = result.setScale(Math.abs(field.sqlscale), BigDecimal.ROUND_HALF_EVEN);
        
        return result;
    }
    
    public boolean getBoolean() throws SQLException {
        if (getFieldData()==null) return BOOLEAN_NULL_VALUE;

        return field.decodeDouble(getFieldData()) == 1;
    }
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeDouble(getFieldData()));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        try {
            setDouble(Double.parseDouble(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    public void setShort(short value) throws SQLException {
        setDouble((double)value);
    }
    public void setBoolean(boolean value) throws SQLException {
        setDouble(value ? 1 : 0);
    }
    public void setFloat(float value) throws SQLException {
        setDouble((double)value);
    }
    public void setDouble(double value) throws SQLException {
        setFieldData(field.encodeDouble(value));
    }
    public void setLong(long value) throws SQLException {
        setDouble((double)value);
    }
    public void setInteger(int value) throws SQLException {
        setDouble((double)value);
    }
    public void setByte(byte value) throws SQLException {
        setDouble((double)value);
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
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
