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
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * The class <code>FBIntegerField</code> represents an INTEGER datatype and performs all necessary
 * conversions.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBIntegerField extends FBField {

    private static final BigDecimal BD_MAX_INT = BigDecimal.valueOf(MAX_INT_VALUE);
    private static final BigDecimal BD_MIN_INT = BigDecimal.valueOf(MIN_INT_VALUE);
    private static final BigInteger BI_MAX_INT = BigInteger.valueOf(MAX_INT_VALUE);
    private static final BigInteger BI_MIN_INT = BigInteger.valueOf(MIN_INT_VALUE);

    @NullMarked
    FBIntegerField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        return getInt();
    }

    @Override
    public byte getByte() throws SQLException {
        int value = getInt();
        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw outOfRangeGetConversion("byte", value);
        }

        return (byte) value;
    }

    @NullMarked
    private SQLException outOfRangeGetConversion(String type, int value) {
        return invalidGetConversion(type, "value %d out of range".formatted(value));
    }

    @Override
    public short getShort() throws SQLException {
        int value = getInt();
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw outOfRangeGetConversion("short", value);
        }

        return (short) value;
    }

    @Override
    public int getInt() throws SQLException {
        return getDatatypeCoder().decodeInt(getFieldData());
    }

    @Override
    public long getLong() throws SQLException {
        return getInt();
    }

    @Override
    public float getFloat() throws SQLException {
        return getInt();
    }

    @Override
    public double getDouble() throws SQLException {
        return getInt();
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getInt());
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getInt() == 1;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getInt());
    }

    @Override
    public BigInteger getBigInteger() throws SQLException {
        if (isNull()) return null;
        return BigInteger.valueOf(getInt());
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setInteger(Integer.parseInt(string));
        } catch (NumberFormatException nfex) {
            throw invalidSetConversion(String.class, string, nfex);
        }
    }

    @Override
    public void setShort(short value) throws SQLException {
        setInteger(value);
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setInteger(value ? 1 : 0);
    }

    @Override
    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw invalidSetConversion("float", String.format("value %f out of range", value));
        }

        setInteger((int) value);
    }

    @Override
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw invalidSetConversion("double", String.format("value %f out of range", value));
        }

        setInteger((int) value);
    }

    @Override
    public void setLong(long value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw invalidSetConversion("long", String.format("value %d out of range", value));
        }

        setInteger((int) value);
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeInt(value));
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setInteger(value);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BD_MAX_INT) > 0 || value.compareTo(BD_MIN_INT) < 0) {
            throw invalidSetConversion(BigDecimal.class, String.format("value %f out of range", value));
        }

        setInteger(value.intValue());
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BI_MAX_INT) > 0 || value.compareTo(BI_MIN_INT) < 0) {
            throw invalidSetConversion(BigInteger.class, String.format("value %d out of range", value));
        }

        setLong(value.intValueExact());
    }

}
