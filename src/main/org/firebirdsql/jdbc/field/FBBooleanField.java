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
import java.sql.SQLException;

/**
 * {@link FBField} implementation for {@link java.sql.Types#BOOLEAN} (Firebird type {@link org.firebirdsql.gds.ISCConstants#SQL_BOOLEAN}).
 * <p>
 * This field type is only supported on Firebird 3.0
 * </p>
 *
 * @author Mark Rotteveel
 * @since 2.2.4
 */
final class FBBooleanField extends FBField {
    // TODO Evaluate current choices for truth values for non-boolean types (especially if number types != 0 are all true)

    FBBooleanField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        if (isNull()) return null;
        return getBoolean();
    }

    @Override
    public byte getByte() throws SQLException {
        return (byte) (getBoolean() ? 1 : 0);
    }

    @Override
    public short getShort() throws SQLException {
        return getByte();
    }

    @Override
    public int getInt() throws SQLException {
        return getBoolean() ? 1 : 0;
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
        return getBoolean() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return getBoolean() ? FBStringField.LONG_TRUE : FBStringField.LONG_FALSE;
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getDatatypeCoder().decodeBoolean(getFieldData());
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setBoolean(value != 0);
    }

    @Override
    public void setShort(short value) throws SQLException {
        setBoolean(value != 0);
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setBoolean(value != 0);
    }

    @Override
    public void setLong(long value) throws SQLException {
        setBoolean(value != 0);
    }

    @Override
    public void setFloat(float value) throws SQLException {
        //TODO What if NaN?
        setBoolean(value != 0);
    }

    @Override
    public void setDouble(double value) throws SQLException {
        //TODO What if NaN?
        setBoolean(value != 0);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (setWhenNull(value)) return;
        setBoolean(value.compareTo(BigDecimal.ZERO) != 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses similar rules as {@link org.firebirdsql.jdbc.field.FBStringField#getBoolean()}. Sets this boolean to true for (case insensitive, ignoring whitespace):
     * <ul>
     * <li>true</li>
     * <li>Y</li>
     * <li>T</li>
     * <li>1</li>
     * </ul>
     * Sets to false for all other values.
     * </p>
     */
    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        final String trimmedValue = value.trim();
        setBoolean(trimmedValue.equalsIgnoreCase(FBStringField.LONG_TRUE)
                || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE)
                || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE_2)
                || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE_3));
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setFieldData(getDatatypeCoder().encodeBoolean(value));
    }

}
