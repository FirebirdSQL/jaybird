/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Describe class <code>FBDoubleField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBDoubleField extends FBField {
    private static final BigDecimal BD_MAX_DOUBLE = new BigDecimal(MAX_DOUBLE_VALUE);
    private static final BigDecimal BD_MIN_DOUBLE = new BigDecimal(MIN_DOUBLE_VALUE);

    FBDoubleField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        double value = getDatatypeCoder().decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE ||
            value < MIN_BYTE_VALUE)
                throw new TypeConversionException(BYTE_CONVERSION_ERROR + " " + value);

        return (byte) value;
    }
    
    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        double value = getDatatypeCoder().decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
            value < MIN_SHORT_VALUE)
                throw new TypeConversionException(SHORT_CONVERSION_ERROR + " " + value);

        return (short) value;
    }
    
    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        double value = getDatatypeCoder().decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_INT_VALUE ||
            value < MIN_INT_VALUE)
                throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        return (int) value;
    }
    
    public long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;

        double value = getDatatypeCoder().decodeDouble(getFieldData());

        // check if value is within bounds
        if (value > MAX_LONG_VALUE ||
            value < MIN_LONG_VALUE)
                throw new TypeConversionException(LONG_CONVERSION_ERROR + " " + value);

        return (long) value;
    }
    
    public float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        // TODO Does this match with the way getDouble() works?
        double value = getDatatypeCoder().decodeDouble(getFieldData());
        float cValue = (float) value;
        // check if value is within bounds
        if (cValue == Float.POSITIVE_INFINITY || 
        	cValue == Float.NEGATIVE_INFINITY)
            throw new TypeConversionException(FLOAT_CONVERSION_ERROR + " " + value);

        return cValue;
    }
    
    public double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        return getDatatypeCoder().decodeDouble(getFieldData());
    }
    
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;

        return new BigDecimal(getDatatypeCoder().decodeDouble(getFieldData()));
    }

    public boolean getBoolean() throws SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;

        return getDatatypeCoder().decodeDouble(getFieldData()) == 1;
    }
    
    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getDatatypeCoder().decodeDouble(getFieldData()));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            setDouble(Double.parseDouble(value));
        } catch(NumberFormatException nfex) {
            throw new TypeConversionException(DOUBLE_CONVERSION_ERROR + " " + value);
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
        setFieldData(getDatatypeCoder().encodeDouble(value));
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
        if (value == null) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BD_MAX_DOUBLE) > 0 ||
            value.compareTo(BD_MIN_DOUBLE) < 0)
                throw new TypeConversionException(DOUBLE_CONVERSION_ERROR + " " + value);

        setDouble(value.doubleValue());
    }
}
