/*
 * Firebird Open Source JDBC Driver
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
        if (isNull()) return BYTE_NULL_VALUE;

        long value = getDatatypeCoder().decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw invalidGetConversion("byte", String.format("value %d out of range", value));
        }

        return (byte) value;
    }

    @Override
    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        long value = getDatatypeCoder().decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw invalidGetConversion("short", String.format("value %d out of range", value));
        }

        return (short) value;
    }

    @Override
    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        long value = getDatatypeCoder().decodeLong(getFieldData());

        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw invalidGetConversion("int", String.format("value %d out of range", value));
        }

        return (int) value;
    }

    @Override
    public long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;
        return getDatatypeCoder().decodeLong(getFieldData());
    }

    @Override
    public float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;
        return getDatatypeCoder().decodeLong(getFieldData());
    }

    @Override
    public double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;
        return getDatatypeCoder().decodeLong(getFieldData());
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getDatatypeCoder().decodeLong(getFieldData()));
    }

    @Override
    public boolean getBoolean() throws SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;
        return getDatatypeCoder().decodeLong(getFieldData()) == 1;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getDatatypeCoder().decodeLong(getFieldData()));
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
            SQLException conversionException = invalidSetConversion(String.class, string);
            conversionException.initCause(nfex);
            throw conversionException;
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
