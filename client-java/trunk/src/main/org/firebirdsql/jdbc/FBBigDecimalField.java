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
import java.math.BigInteger;

/**
 * Describe class <code>FBBigDecimalField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBBigDecimalField extends FBField {
    
    private static final BigInteger MAX_SHORT = BigInteger.valueOf(Short.MAX_VALUE);
    private static final BigInteger MIN_SHORT = BigInteger.valueOf(Short.MIN_VALUE);
    
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    
    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    int fieldType = 0; // 1.- Short, 2.- Integer, 3.- Long

    FBBigDecimalField(XSQLVAR field, FBResultSet rs, int numCol, int fieldType) throws SQLException {
        super(field, rs, numCol);
        this.fieldType = fieldType;
    }

    boolean getBoolean() throws SQLException {
        return getByte() == 1;
    }

    byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        long longValue = getLong();

        // check if value is withing bounds
        if (longValue > MAX_BYTE_VALUE ||
            longValue < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR).fillInStackTrace();


        return (byte)longValue;
    }

    double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        return getBigDecimal().doubleValue();
    }

    float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        double doubleValue = getDouble();

        // check if value is withing bounds
        if (doubleValue > MAX_FLOAT_VALUE ||
            doubleValue < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    FLOAT_CONVERSION_ERROR).fillInStackTrace();


        return (float)doubleValue;

    }

    int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

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
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        long longValue = getLong();

        // check if value is withing bounds
        if (longValue > MAX_SHORT_VALUE ||
            longValue < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR).fillInStackTrace();


        return (short)longValue;
    }

    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;
        
        return getBigDecimal().toString();
    }

    BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        long longValue;

        if (fieldType==2)
            longValue = XSQLVAR.decodeInt(rs.row[numCol]);
//            longValue = ((Integer)rs.row[numCol]).longValue();
        else
        if (fieldType==3)
            longValue = XSQLVAR.decodeLong(rs.row[numCol]);
//            longValue = ((Long)rs.row[numCol]).longValue();
        else
        if (fieldType==1)
            longValue = XSQLVAR.decodeShort(rs.row[numCol]);
//            longValue = ((Short)rs.row[numCol]).longValue();
        else
            throw (SQLException)createException(
                BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

        return BigDecimal.valueOf(longValue, -field.sqlscale);
    }

    //--- setXXX methods

    void setBoolean(boolean value) throws SQLException {
        setInteger(value ? 1 : 0);
    }

    void setByte(byte value) throws SQLException {
        setLong(value);
    }

    void setDouble(double value) throws SQLException {
        setBigDecimal(new BigDecimal(Double.toString(value)));
    }

    void setFloat(float value) throws SQLException {
        setDouble(value);
    }

    void setInteger(int value) throws SQLException {
        setLong(value);
    }

    void setLong(long value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value, 0));
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

        if (fieldType == 1) {

            // check if value is withing bounds
            if (value.unscaledValue().compareTo(MAX_SHORT) > 0 ||
                value.unscaledValue().compareTo(MIN_SHORT) < 0)
                    throw (SQLException)createException(
                        BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

            field.sqldata = XSQLVAR.encodeShort(value.unscaledValue().shortValue());
//            field.sqldata = new Short(value.unscaledValue().shortValue());
        } else
        if (fieldType == 2) {

            // check if value is withing bounds
            if (value.unscaledValue().compareTo(MAX_INT) > 0 ||
                value.unscaledValue().compareTo(MIN_INT) < 0)
                    throw (SQLException)createException(
                        BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

            field.sqldata = XSQLVAR.encodeInt(value.unscaledValue().intValue());
//            field.sqldata = new Integer(value.unscaledValue().intValue());
        } else
        if (fieldType == 3) {
            
            // check if value is withing bounds
            if (value.unscaledValue().compareTo(MAX_LONG) > 0 ||
                value.unscaledValue().compareTo(MIN_LONG) < 0)
                    throw (SQLException)createException(
                        BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
            
            field.sqldata = XSQLVAR.encodeLong(value.unscaledValue().longValue());
//            field.sqldata = new Long(value.unscaledValue().longValue());
        } else
            throw (SQLException)createException(
                BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();

    }



}
