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
 * The class <code>FBIntegerField</code> represents an INTEGER datatype and performs all necessary
 * conversions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBIntegerField extends FBField {

    private static final BigDecimal BD_MAX_INT = BigDecimal.valueOf(MAX_INT_VALUE);
    private static final BigDecimal BD_MIN_INT = BigDecimal.valueOf(MIN_INT_VALUE);

    FBIntegerField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        int value = getDatatypeCoder().decodeInt(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE ||
                value < MIN_BYTE_VALUE)
            throw new TypeConversionException(BYTE_CONVERSION_ERROR + " " + value);

        return (byte) value;
    }

    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        int value = getDatatypeCoder().decodeInt(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE ||
                value < MIN_SHORT_VALUE)
            throw new TypeConversionException(SHORT_CONVERSION_ERROR + " " + value);

        return (short) value;
    }

    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;
        return getDatatypeCoder().decodeInt(getFieldData());
    }

    public long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;
        return getDatatypeCoder().decodeInt(getFieldData());
    }

    public float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;
        return getDatatypeCoder().decodeInt(getFieldData());
    }

    public double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;
        return getDatatypeCoder().decodeInt(getFieldData());
    }

    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getDatatypeCoder().decodeInt(getFieldData()));
    }

    public boolean getBoolean() throws SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;
        return getDatatypeCoder().decodeInt(getFieldData()) == 1;
    }

    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getDatatypeCoder().decodeInt(getFieldData()));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            setInteger(Integer.parseInt(value));
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);
        }
    }

    public void setShort(short value) throws SQLException {
        setInteger(value);
    }

    public void setBoolean(boolean value) throws SQLException {
        setInteger(value ? 1 : 0);
    }

    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE)
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        setInteger((int) value);
    }

    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE)
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        setInteger((int) value);
    }

    public void setLong(long value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE)
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        setInteger((int) value);
    }

    public void setInteger(int value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeInt(value));
    }

    public void setByte(byte value) throws SQLException {
        setInteger(value);
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(BD_MAX_INT) > 0 ||
                value.compareTo(BD_MIN_INT) < 0)
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + value);

        setInteger(value.intValue());
    }
}
