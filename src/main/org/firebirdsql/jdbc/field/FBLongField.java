/*
 SPDX-FileCopyrightText: Copyright 2002-2004 Roman Rokytskyy
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
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * This class represents LONG datatype and performs all necessary type
 * conversions.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBLongField extends FBField {
    private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(MAX_LONG_VALUE);
    private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(MIN_LONG_VALUE);
    private static final BigInteger BI_MAX_LONG = BigInteger.valueOf(MAX_LONG_VALUE);
    private static final BigInteger BI_MIN_LONG = BigInteger.valueOf(MIN_LONG_VALUE);

    @NullMarked
    FBLongField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        return getLong();
    }

    @Override
    public byte getByte() throws SQLException {
        long value = getLong();
        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw outOfRangeGetConversion("byte", value);
        }

        return (byte) value;
    }

    @NullMarked
    private SQLException outOfRangeGetConversion(String type, long value) {
        return invalidGetConversion(type, "value %d out of range".formatted(value));
    }

    @Override
    public short getShort() throws SQLException {
        long value = getLong();
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw outOfRangeGetConversion("short", value);
        }

        return (short) value;
    }

    @Override
    public int getInt() throws SQLException {
        long value = getLong();
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw outOfRangeGetConversion("int", value);
        }

        return (int) value;
    }

    @Override
    public long getLong() throws SQLException {
        return getDatatypeCoder().decodeLong(getFieldData());
    }

    @Override
    public float getFloat() throws SQLException {
        return getLong();
    }

    @Override
    public double getDouble() throws SQLException {
        return getLong();
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getLong());
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getLong() == 1L;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getLong());
    }

    @Override
    public BigInteger getBigInteger() throws SQLException {
        if (isNull()) return null;
        return BigInteger.valueOf(getLong());
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setLong(Long.parseLong(string));
        } catch (NumberFormatException nfex) {
            throw invalidSetConversion(String.class, string, nfex);
        }
    }

    @Override
    public void setShort(short value) throws SQLException {
        setLong(value);
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setLong(value ? 1 : 0);
    }

    @Override
    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE || value < MIN_LONG_VALUE) {
            throw invalidSetConversion("float", String.format("value %f out of range", value));
        }

        setLong((long) value);
    }

    @Override
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_LONG_VALUE || value < MIN_LONG_VALUE) {
            throw invalidSetConversion("double", String.format("value %f out of range", value));
        }

        setLong((long) value);
    }

    @Override
    public void setLong(long value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLong(value));
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setLong(value);
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setLong(value);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BD_MAX_LONG) > 0 || value.compareTo(BD_MIN_LONG) < 0) {
            throw invalidSetConversion(BigDecimal.class, String.format("value %f out of range", value));
        }

        setLong(value.longValue());
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BI_MAX_LONG) > 0 || value.compareTo(BI_MIN_LONG) < 0) {
            throw invalidSetConversion(BigInteger.class, String.format("value %d out of range", value));
        }

        setLong(value.longValueExact());
    }

}
