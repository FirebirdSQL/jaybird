/*
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

package org.firebirdsql.jdbc;

import org.firebirdsql.gds.XSQLVAR;
import java.sql.SQLException;
import java.sql.Types;
import java.math.BigDecimal;

/**
 * Describe class <code>FBBigDecimalField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBBigDecimalField extends FBField {

    FBBigDecimalField(XSQLVAR field) throws SQLException {
        super(field);
    }

    boolean getBoolean() throws SQLException {
        return getByte() == 1;
    }

    byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        long longValue = getLong();

        // check if value is withing bounds
        if (longValue > MAX_BYTE_VALUE ||
            longValue < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR).fillInStackTrace();


        return (byte)longValue;
    }

    double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        return getBigDecimal().doubleValue();
    }

    float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        double doubleValue = getDouble();

        // check if value is withing bounds
        if (doubleValue > MAX_FLOAT_VALUE ||
            doubleValue < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    FLOAT_CONVERSION_ERROR).fillInStackTrace();


        return (float)doubleValue;

    }

    int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        long longValue = getLong();

        // check if value is withing bounds
        if (longValue > MAX_INT_VALUE ||
            longValue < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR).fillInStackTrace();


        return (int)longValue;

    }

    long getLong() throws SQLException {
        return getBigDecimal().longValue();
    }

    Object getObject() throws SQLException {
        return getBigDecimal();
    }

    short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        long longValue = getLong();

        // check if value is withing bounds
        if (longValue > MAX_SHORT_VALUE ||
            longValue < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR).fillInStackTrace();


        return (short)longValue;
    }

    String getString() throws SQLException {
        if (isNull()) return STRING_NULL_VALUE;
        
        return getBigDecimal().toString();
    }

    BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return BIGDECIMAL_NULL_VALUE;

        long longValue;

        if (field.sqldata instanceof Integer)
            longValue = ((Integer)field.sqldata).longValue();
        else
        if (field.sqldata instanceof Long)
            longValue = ((Long)field.sqldata).longValue();
        else
        if (field.sqldata instanceof Short)
            longValue = ((Short)field.sqldata).longValue();
        else
            throw (SQLException)createException(
                BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

        return BigDecimal.valueOf(longValue, -field.sqlscale);
    }

    void setBoolean(boolean value) throws SQLException {
        setInteger(value ? 1 : 0);
    }

    void setByte(byte value) throws SQLException {
        setLong(value);
    }

    void setDouble(double value) throws SQLException {
        setBigDecimal(new BigDecimal(value));
    }

    void setFloat(float value) throws SQLException {
        setDouble(value);
    }

    void setInteger(int value) throws SQLException {
        setLong(value);
    }

    void setLong(long value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value));
    }

    void setShort(short value) throws SQLException {
        setLong(value);
    }

    void setString(String value) throws SQLException {
        try {
            setBigDecimal(new BigDecimal(value));
        } catch(NumberFormatException nex) {
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR).fillInStackTrace();
        }
    }

    void setBigDecimal(BigDecimal value) throws SQLException {
        value = value.setScale(-field.sqlscale, BigDecimal.ROUND_HALF_UP);

        if (isType(field, Types.SMALLINT)) {
            long longValue = value.unscaledValue().longValue();

            // check if value is withing bounds
            if (longValue > MAX_SHORT_VALUE ||
                longValue < MIN_SHORT_VALUE)
                    throw (SQLException)createException(
                        BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

            field.sqldata = new Short((short)longValue);
        } else
        if (isType(field, Types.INTEGER)) {
            long longValue = value.unscaledValue().longValue();

            // check if value is withing bounds
            if (longValue > MAX_INT_VALUE ||
                longValue < MIN_INT_VALUE)
                    throw (SQLException)createException(
                        BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

            field.sqldata = new Integer((int)longValue);
        } else
        if (isType(field, Types.BIGINT)) {
            field.sqldata = new Long(value.unscaledValue().longValue());
        } else
            throw (SQLException)createException(
                BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

        setNull(false);
    }



}