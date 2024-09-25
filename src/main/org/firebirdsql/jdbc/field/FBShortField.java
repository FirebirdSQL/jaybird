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
 * This class represents a field of type SHORT and performs all necessary
 * conversions.
 * @author Roman Rokytskyy
 * @author David Jencks
 * @author Mark Rotteveel
 */
class FBShortField extends FBField {

    private static final BigDecimal BD_MAX_SHORT = BigDecimal.valueOf(MAX_SHORT_VALUE);
    private static final BigDecimal BD_MIN_SHORT = BigDecimal.valueOf(MIN_SHORT_VALUE);
    private static final BigInteger BI_MAX_SHORT = BigInteger.valueOf(MAX_SHORT_VALUE);
    private static final BigInteger BI_MIN_SHORT = BigInteger.valueOf(MIN_SHORT_VALUE);

    @NullMarked
    FBShortField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        // See JDBC 4.3, B.3 JDBC Types Mapped to Java Object Types
        return getInt();
    }

    @Override
    public byte getByte() throws SQLException {
        short value = getShort();
        // check if value is within bounds
        if (value > MAX_BYTE_VALUE || value < MIN_BYTE_VALUE) {
            throw invalidGetConversion("byte", String.format("value %d out of range", value));
        }

        return (byte) value;
    }

    @Override
    public short getShort() throws SQLException {
        return getDatatypeCoder().decodeShort(getFieldData());
    }

    @Override
    public int getInt() throws SQLException {
        return getShort();
    }

    @Override
    public long getLong() throws SQLException {
        return getShort();
    }

    @Override
    public float getFloat() throws SQLException {
        return getShort();
    }

    @Override
    public double getDouble() throws SQLException {
        return getShort();
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;
        return BigDecimal.valueOf(getShort());
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getShort() == 1;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return String.valueOf(getShort());
    }

    @Override
    public BigInteger getBigInteger() throws SQLException {
        if (isNull()) return null;
        return BigInteger.valueOf(getShort());
    }

    //--- setXXX methods

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setShort(Short.parseShort(string));
        } catch(NumberFormatException nfex) {
            throw invalidSetConversion(String.class, string, nfex);
        }
    }

    @Override
    public void setShort(short value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeShort(value));
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setShort((short) (value ? 1 : 0));
    }

    @Override
    public void setFloat(float value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw invalidSetConversion("float", String.format("value %f out of range", value));
        }

        setShort((short) value);
    }

    @Override
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw invalidSetConversion("double", String.format("value %f out of range", value));
        }

        setShort((short) value);
    }

    @Override
    public void setLong(long value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw invalidSetConversion("long", String.format("value %d out of range", value));
        }

        setShort((short) value);
    }

    @Override
    public void setInteger(int value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_SHORT_VALUE || value < MIN_SHORT_VALUE) {
            throw invalidSetConversion("int", String.format("value %d out of range", value));
        }

        setShort((short) value);
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setShort(value);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BD_MAX_SHORT) > 0 || value.compareTo(BD_MIN_SHORT) < 0) {
            throw invalidSetConversion(BigDecimal.class, String.format("value %f out of range", value));
        }

        setShort(value.shortValue());
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (setWhenNull(value)) return;
        // check if value is within bounds
        if (value.compareTo(BI_MAX_SHORT) > 0 || value.compareTo(BI_MIN_SHORT) < 0) {
            throw invalidSetConversion(BigInteger.class, String.format("value %d out of range", value));
        }

        setShort(value.shortValueExact());
    }

}
