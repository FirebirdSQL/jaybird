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
    private static final BigDecimal BD_MAX_FLOAT = new BigDecimal(MAX_FLOAT_VALUE);
    private static final BigDecimal BD_MIN_FLOAT = new BigDecimal(MIN_FLOAT_VALUE);
    
    FBFloatField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        float value = field.decodeFloat(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE ||
            value < MIN_BYTE_VALUE)
                throw new TypeConversionException(BYTE_CONVERSION_ERROR + " " + value);

        return (byte) value;
    }
    
    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        float value = field.decodeFloat(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw new TypeConversionException(SHORT_CONVERSION_ERROR + " " + value);

        return (short) value;
    }
    
    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        float value = field.decodeFloat(getFieldData());

        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        return (int) value;
    }
    
    public long getLong() throws SQLException {
        if (getFieldData()==null) return LONG_NULL_VALUE;

        float value = field.decodeFloat(getFieldData());

        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw new TypeConversionException(LONG_CONVERSION_ERROR + " " + value);

        return (long) value;
    }
    
    public float getFloat() throws SQLException {
        if (getFieldData()==null) return FLOAT_NULL_VALUE;

        return field.decodeFloat(getFieldData());
    }
    
    public double getDouble() throws SQLException {
        if (getFieldData()==null) return DOUBLE_NULL_VALUE;

        return field.decodeFloat(getFieldData());
    }
    
    public BigDecimal getBigDecimal() throws SQLException {
        if (getFieldData()==null) return BIGDECIMAL_NULL_VALUE;

        return new BigDecimal(field.decodeFloat(getFieldData()));
    }
    
    /*
    public Object getObject() throws SQLException {
        if (getFieldData()==null) return OBJECT_NULL_VALUE;

        return new Double(field.decodeFloat(getFieldData()));
    }
    */
    
    public boolean getBoolean() throws SQLException {
        if (getFieldData()==null) return BOOLEAN_NULL_VALUE;

        return field.decodeFloat(getFieldData()) == 1;
    }
    
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeFloat(getFieldData()));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        try {
            setFloat(Float.parseFloat(value));
        } catch(NumberFormatException nfex) {
            throw new TypeConversionException(FLOAT_CONVERSION_ERROR + " " + value);
        }
    }
    
    public void setShort(short value) throws SQLException {
        setFloat(value);
    }
    
    public void setBoolean(boolean value) throws SQLException {
        setFloat(value ? 1.0f : 0.0f);
    }
    
    public void setFloat(float value) throws SQLException {
        setFieldData(field.encodeFloat(value));
    }
    
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        // TODO: Shouldn't we just overflow to +/-INF?
        if (value > MAX_FLOAT_VALUE ||
            value < MIN_FLOAT_VALUE)
                throw new TypeConversionException(FLOAT_CONVERSION_ERROR + " " + value);

        setFloat((float)value);
    }
    
    public void setLong(long value) throws SQLException {
        setFloat(value);
    }
    
    public void setInteger(int value) throws SQLException {
        setFloat(value);
    }
    
    public void setByte(byte value) throws SQLException {
        setFloat(value);
    }
    
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BD_MAX_FLOAT) > 0 ||
            value.compareTo(BD_MIN_FLOAT) < 0)
                throw new TypeConversionException(BIGDECIMAL_CONVERSION_ERROR + " " + value);

        setFloat(value.floatValue());
    }
}
