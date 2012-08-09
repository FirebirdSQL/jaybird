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
    private static final BigDecimal BD_MAX_DOUBLE = new BigDecimal(MAX_DOUBLE_VALUE);
    private static final BigDecimal BD_MIN_DOUBLE = new BigDecimal(MIN_DOUBLE_VALUE);

    FBDoubleField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        double value = field.decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE ||
            value < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (byte) value;
    }
    
    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        double value = field.decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (short) value;
    }
    
    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        double value = field.decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (int) value;
    }
    
    public long getLong() throws SQLException {
        if (getFieldData()==null) return LONG_NULL_VALUE;

        double value = field.decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return (long) value;
    }
    
    public float getFloat() throws SQLException {
        if (getFieldData()==null) return FLOAT_NULL_VALUE;

        // TODO Does this match with the way getDouble() works?
        double value = field.decodeDouble(getFieldData());
        float cValue = (float) value;
        // check if value is within bounds
        if (cValue == Float.POSITIVE_INFINITY || 
        	cValue == Float.NEGATIVE_INFINITY)
            throw (SQLException)createException(
                FLOAT_CONVERSION_ERROR + " " + value).fillInStackTrace();

        return cValue;
    }
    
    public double getDouble() throws SQLException {
        if (getFieldData()==null) return DOUBLE_NULL_VALUE;

        double result = field.decodeDouble(getFieldData());
        
        // TODO Is this even possible? Wouldn't it be seen as a FBBigDecimalField ?
        // TODO Mismatch with all other getters
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
        // TODO Is this even possible? Wouldn't it be seen as a FBBigDecimalField ?
        if (field.sqlscale != 0)
            result = result.setScale(Math.abs(field.sqlscale), BigDecimal.ROUND_HALF_EVEN);
        
        return result;
    }
    
    /*
    public Object getObject() throws SQLException {
        if (getFieldData()==null) return OBJECT_NULL_VALUE;

        return new Double(field.decodeDouble(getFieldData()));
    }
    */
    
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
                DOUBLE_CONVERSION_ERROR + " " + value).fillInStackTrace();
        }
    }
    
    public void setShort(short value) throws SQLException {
        setDouble(value);
    }
    
    public void setBoolean(boolean value) throws SQLException {
        setDouble(value ? 1 : 0);
    }
    
    public void setFloat(float value) throws SQLException {
        setDouble(value);
    }
    
    public void setDouble(double value) throws SQLException {
        setFieldData(field.encodeDouble(value));
    }
    
    public void setLong(long value) throws SQLException {
        setDouble(value);
    }
    
    public void setInteger(int value) throws SQLException {
        setDouble(value);
    }
    
    public void setByte(byte value) throws SQLException {
        setDouble(value);
    }
    
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BD_MAX_DOUBLE) > 0 ||
            value.compareTo(BD_MIN_DOUBLE) < 0)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR + " " + value).fillInStackTrace();

        setDouble(value.doubleValue());
    }
}
