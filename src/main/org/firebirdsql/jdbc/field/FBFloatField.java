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
import java.sql.SQLException;

/**
 * The class <code>FBFloatField</code>represents a FLOAT datatype and performs all necessary
 * conversions.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBFloatField extends FBField {
    @SuppressWarnings("java:S2111")
    private static final BigDecimal BD_MAX_FLOAT = new BigDecimal(MAX_FLOAT_VALUE);
    @SuppressWarnings("java:S2111")
    private static final BigDecimal BD_MIN_FLOAT = new BigDecimal(MIN_FLOAT_VALUE);

    @NullMarked
    FBFloatField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        // See JDBC 4.3, B.3 JDBC Types Mapped to Java Object Types
        return getDouble();
    }

    @Override
    public byte getByte() throws SQLException {
        float value = getFloat();
        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw outOfRangeGetConversion("byte", value);
        }

        return (byte) value;
    }

    @NullMarked
    private SQLException outOfRangeGetConversion(String type, float value) {
        return invalidGetConversion(type, "value %f out of range".formatted(value));
    }

    @Override
    public short getShort() throws SQLException {
        float value = getFloat();
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw outOfRangeGetConversion("short", value);
        }

        return (short) value;
    }

    @Override
    public int getInt() throws SQLException {
        float value = getFloat();
        // check if value is within bounds
        if (value > MAX_INT_VALUE || value < MIN_INT_VALUE) {
            throw outOfRangeGetConversion("int", value);
        }

        return (int) value;
    }

    @Override
    public long getLong() throws SQLException {
        float value = getFloat();
        // check if value is within bounds
        if (value > MAX_LONG_VALUE || value < MIN_LONG_VALUE) {
            throw outOfRangeGetConversion("long", value);
        }

        return (long) value;
    }

    @Override
    public float getFloat() throws SQLException {
        return getDatatypeCoder().decodeFloat(getFieldData());
    }

    @Override
    public double getDouble() throws SQLException {
        return getFloat();
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getFloat());
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getFloat() == 1;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getFloat());
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setFloat(Float.parseFloat(string));
        } catch (NumberFormatException nfex) {
            throw invalidSetConversion(String.class, string, nfex);
        }
    }

    @Override
    public void setShort(short value) throws SQLException {
        setFloat(value);
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setFloat(value ? 1.0f : 0.0f);
    }

    @Override
    public void setFloat(float value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeFloat(value));
    }

    @Override
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value == Double.NEGATIVE_INFINITY) {
            setFloat(Float.NEGATIVE_INFINITY);
        } else if (value == Double.POSITIVE_INFINITY) {
            setFloat(Float.POSITIVE_INFINITY);
        } else if (value > MAX_FLOAT_VALUE || value < MIN_FLOAT_VALUE) {
            // TODO: Shouldn't we just overflow to +/-INF?
            throw invalidSetConversion("double", String.format("value %f out of range", value));
        } else {
            setFloat((float) value);
        }
    }

    @Override
    public void setLong(long value) throws SQLException {
        setFloat(value);
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setFloat(value);
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setFloat(value);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BD_MAX_FLOAT) > 0 || value.compareTo(BD_MIN_FLOAT) < 0) {
            throw invalidSetConversion(BigDecimal.class, String.format("value %f out of range", value));
        }

        setFloat(value.floatValue());
    }

}
