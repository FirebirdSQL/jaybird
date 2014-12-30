/*
 * $Id$
 *
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

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * {@link FBField} implementation for {@link java.sql.Types#BOOLEAN} (Firebird type {@link org.firebirdsql.gds.ISCConstants#SQL_BOOLEAN}).
 * <p>
 * This field type is only supported on Firebird 3.0
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2.4
 */
public class FBBooleanField extends FBField {
    // TODO Evaluate current choices for truth values for non-boolean types (especially if number types != 0 are all true)

    FBBooleanField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(field, dataProvider, requiredType);
    }

    public byte getByte() throws SQLException {
        if (isNull()) {
            return BYTE_NULL_VALUE;
        }
        return (byte) (getBoolean() ? 1 : 0);
    }

    public short getShort() throws SQLException {
        return getByte();
    }

    public int getInt() throws SQLException {
        if (isNull()) {
            return INT_NULL_VALUE;
        }
        return getBoolean() ? 1 : 0;
    }

    public long getLong() throws SQLException {
        return getInt();
    }

    public float getFloat() throws SQLException {
        return getInt();
    }

    public double getDouble() throws SQLException {
        return getInt();
    }

    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) {
            return BIGDECIMAL_NULL_VALUE;
        }
        return getBoolean() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    public String getString() throws SQLException {
        if (isNull()) {
            return STRING_NULL_VALUE;
        }
        return getBoolean() ? FBStringField.LONG_TRUE : FBStringField.LONG_FALSE;
    }

    public boolean getBoolean() throws SQLException {
        if (isNull()) {
            return BOOLEAN_NULL_VALUE;
        }
        return field.decodeBoolean(getFieldData());
    }

    public void setByte(byte value) throws SQLException {
        setBoolean(value != 0);
    }

    public void setShort(short value) throws SQLException {
        setBoolean(value != 0);
    }

    public void setInteger(int value) throws SQLException {
        setBoolean(value != 0);
    }

    public void setLong(long value) throws SQLException {
        setBoolean(value != 0);
    }

    public void setFloat(float value) throws SQLException {
        //TODO What if NaN?
        setBoolean(value != 0);
    }

    public void setDouble(double value) throws SQLException {
        //TODO What if NaN?
        setBoolean(value != 0);
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == null) {
            setNull();
        } else {
            setBoolean(value.compareTo(BigDecimal.ZERO) != 0);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses similar rules as {@link org.firebirdsql.jdbc.field.FBStringField#getBoolean()}. Sets this boolean to true for (case insensitive, ignoring whitespace):
     * <ul>
     *     <li>true</li>
     *     <li>Y</li>
     *     <li>T</li>
     *     <li>1</li>
     * </ul>
     * Sets to false for all other values.
     * </p>
     */
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
        } else {
            final String trimmedValue = value.trim();
            setBoolean(trimmedValue.equalsIgnoreCase(FBStringField.LONG_TRUE)
                    || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE)
                    || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE_2)
                    || trimmedValue.equalsIgnoreCase(FBStringField.SHORT_TRUE_3));
        }
    }

    public void setBoolean(boolean value) throws SQLException {
        setFieldData(field.encodeBoolean(value));
    }
}
