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

package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.SQLException;
import java.math.BigDecimal;

/*
 * This class represents a FLOAT datatype and performs all necessary
 * conversions.
 */
/**
 * The class <code>FBFloatField</code>represents a FLOAT datatype and performs all necessary
 * conversions.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
class FBFloatField extends FBField {
    FBFloatField(XSQLVAR field, FBResultSet rs, int numCol, int requiredType) 
        throws SQLException 
    {
        super(field, rs, numCol, requiredType);
    }

    public byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        Float value = new Float(field.decodeFloat(rs.row[numCol]));

        // check if value is withing bounds
        if (value.floatValue() > MAX_BYTE_VALUE ||
            value.floatValue() < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.byteValue();
    }
    public short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        Float value = new Float(field.decodeFloat(rs.row[numCol]));

        // check if value is withing bounds
        if (value.floatValue() > MAX_SHORT_VALUE ||
            value.floatValue() < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.shortValue();
    }
    public int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        Float value = new Float(field.decodeFloat(rs.row[numCol]));

        // check if value is withing bounds
        if (value.floatValue() > MAX_INT_VALUE ||
            value.floatValue() < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.intValue();
    }
    public long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        Float value = new Float(field.decodeFloat(rs.row[numCol]));

        // check if value is withing bounds
        if (value.floatValue() > MAX_LONG_VALUE ||
            value.floatValue() < MIN_LONG_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        return value.longValue();
    }
    public float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        return field.decodeFloat(rs.row[numCol]);
    }
    public double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        return field.decodeFloat(rs.row[numCol]);
    }
    public BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        return new BigDecimal(field.decodeFloat(rs.row[numCol]));
    }
    
    /*
    public Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return new Double(field.decodeFloat(rs.row[numCol]));
    }
    */
    
    public boolean getBoolean() throws SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return field.decodeFloat(rs.row[numCol]) == 1;
    }
    public String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return String.valueOf(field.decodeFloat(rs.row[numCol]));
    }

    //--- setXXX methods

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }

        try {
            setFloat(Float.parseFloat(value));
        } catch(NumberFormatException nfex) {
            throw (SQLException)createException(
                FLOAT_CONVERSION_ERROR+" "+value).fillInStackTrace();
        }
    }
    public void setShort(short value) throws SQLException {
        setFloat((float)value);
    }
    public void setBoolean(boolean value) throws SQLException {
        setFloat(value ? 1.0f : 0.0f);
    }
    public void setFloat(float value) throws SQLException {
        field.sqldata = field.encodeFloat(value);
    }
    public void setDouble(double value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_FLOAT_VALUE ||
            value < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    DOUBLE_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat((float)value);
    }
    public void setLong(long value) throws SQLException {
        // check if value is within bounds
        if (value > MAX_FLOAT_VALUE ||
            value < MIN_FLOAT_VALUE)
                throw (SQLException)createException(
                    LONG_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat((float)value);
    }
    public void setInteger(int value) throws SQLException {
        setFloat((float)value);
    }
    public void setByte(byte value) throws SQLException {
        setFloat((float)value);
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }

        // check if value is within bounds
        if (value.compareTo(new BigDecimal(MAX_FLOAT_VALUE)) > 0 ||
            value.compareTo(new BigDecimal(MIN_FLOAT_VALUE)) < 0)
                throw (SQLException)createException(
                    BIGDECIMAL_CONVERSION_ERROR+" "+value).fillInStackTrace();

        setFloat(value.floatValue());
    }
}
