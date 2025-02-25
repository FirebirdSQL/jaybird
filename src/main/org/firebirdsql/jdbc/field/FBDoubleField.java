/*
 SPDX-FileCopyrightText: Copyright 2002-2007 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Describe class <code>FBDoubleField</code> here.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
class FBDoubleField extends FBField {
    @SuppressWarnings("java:S2111")
    private static final BigDecimal BD_MAX_DOUBLE = new BigDecimal(MAX_DOUBLE_VALUE);
    @SuppressWarnings("java:S2111")
    private static final BigDecimal BD_MIN_DOUBLE = new BigDecimal(MIN_DOUBLE_VALUE);

    @NullMarked
    FBDoubleField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        return getDouble();
    }

    @Override
    public byte getByte() throws SQLException {
        double value = getDouble();
        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw outOfRangeGetConversion("byte", value);
        }

        return (byte) value;
    }

    @NullMarked
    private SQLException outOfRangeGetConversion(String type, double value) {
        return invalidGetConversion(type, "value %f out of range".formatted(value));
    }

    @Override
    public short getShort() throws SQLException {
        double value = getDouble();
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw outOfRangeGetConversion("short", value);
        }

        return (short) value;
    }

    @Override
    public int getInt() throws SQLException {
        double value = getDouble();
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw outOfRangeGetConversion("int", value);
        }

        return (int) value;
    }

    @Override
    public long getLong() throws SQLException {
        double value = getDouble();
        // check if value is within bounds
        if (value > MAX_LONG_VALUE || value < MIN_LONG_VALUE) {
            throw outOfRangeGetConversion("long", value);
        }

        return (long) value;
    }

    @Override
    public float getFloat() throws SQLException {
        // TODO Does this match with the way getDouble() works?
        double value = getDouble();
        float cValue = (float) value;
        // check if value is within bounds
        if (cValue == Float.POSITIVE_INFINITY || cValue == Float.NEGATIVE_INFINITY) {
            throw outOfRangeGetConversion("float", value);
        }

        return cValue;
    }

    @Override
    public double getDouble() throws SQLException {
        return getDatatypeCoder().decodeDouble(getFieldData());
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getDouble());
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getDouble() == 1;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getDouble());
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setDouble(Double.parseDouble(string));
        } catch(NumberFormatException nfex) {
            throw invalidSetConversion(String.class, string, nfex);
        }
    }

    @Override
    public void setShort(short value) throws SQLException {
        setDouble(value);
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setDouble(value ? 1 : 0);
    }

    @Override
    public void setFloat(float value) throws SQLException {
        setDouble(value);
    }

    @Override
    public void setDouble(double value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeDouble(value));
    }

    @Override
    public void setLong(long value) throws SQLException {
        setDouble(value);
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setDouble(value);
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setDouble(value);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BD_MAX_DOUBLE) > 0 || value.compareTo(BD_MIN_DOUBLE) < 0) {
            throw invalidSetConversion(BigDecimal.class, String.format("value %f out of range", value));
        }

        setDouble(value.doubleValue());
    }

}
