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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;

import java.sql.SQLException;
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

    private final FieldDataSize fieldDataSize;

    FBBigDecimalField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) throws SQLException 
    {
        super(field, dataProvider, requiredType);
        fieldDataSize = FieldDataSize.getFieldDataSize(field);
        if (fieldDataSize == null) {
            throw new SQLException("FBBigDecimal, unsupported field sqltype: " + field.sqltype);
        }
    }

    public boolean getBoolean() throws SQLException {
        return getByte() == 1;
    }

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_BYTE_VALUE ||
            longValue < MIN_BYTE_VALUE)
                throw (SQLException)createException(
                    BYTE_CONVERSION_ERROR).fillInStackTrace();


        return (byte)longValue;
    }

    public double getDouble() throws SQLException {
        BigDecimal value = getBigDecimal();
        
        if (value == BIGDECIMAL_NULL_VALUE)
            return DOUBLE_NULL_VALUE;
        
        return value.doubleValue();
    }

    public float getFloat() throws SQLException {
        BigDecimal value = getBigDecimal();
        
        if (value == BIGDECIMAL_NULL_VALUE)
            return FLOAT_NULL_VALUE;
        
        return value.floatValue();
    }

    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_INT_VALUE ||
            longValue < MIN_INT_VALUE)
                throw (SQLException)createException(
                    INT_CONVERSION_ERROR).fillInStackTrace();

        return (int)longValue;
    }

    public long getLong() throws SQLException {
        BigDecimal value = getBigDecimal();
        
        if (value == BIGDECIMAL_NULL_VALUE)
            return LONG_NULL_VALUE;
        
        return value.longValue();
    }

    /*
    public Object getObject() throws SQLException {
        return getBigDecimal();
    }
    */

    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_SHORT_VALUE ||
            longValue < MIN_SHORT_VALUE)
                throw (SQLException)createException(
                    SHORT_CONVERSION_ERROR).fillInStackTrace();

        return (short)longValue;
    }

    public String getString() throws SQLException {
        BigDecimal value = getBigDecimal();
        
        if (value == BIGDECIMAL_NULL_VALUE)
            return STRING_NULL_VALUE;
        
        return value.toString();
    }

    public BigDecimal getBigDecimal() throws SQLException {
        if (getFieldData()==null) return BIGDECIMAL_NULL_VALUE;

        return fieldDataSize.decode(field, getFieldData());
    }

    //--- setXXX methods

    public void setBoolean(boolean value) throws SQLException {
        setLong(value ? 1 : 0);
    }

    public void setByte(byte value) throws SQLException {
        setLong(value);
    }

    public void setDouble(double value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value));
    }

    public void setFloat(float value) throws SQLException {
        setDouble(value);
    }

    public void setInteger(int value) throws SQLException {
        setLong(value);
    }

    public void setLong(long value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value, 0));
    }

    public void setShort(short value) throws SQLException {
        setLong(value);
    }

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        
        try {
            setBigDecimal(new BigDecimal(value));
        } catch(NumberFormatException nex) {
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR).fillInStackTrace();
        }
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }

        setFieldData(fieldDataSize.encode(field, value));
    }
    
    /**
     * Enum for handling the different fielddata sizes of NUMERIC/DECIMAL fields.
     * 
     * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
     */
    private enum FieldDataSize {
        SHORT {
            @Override
            protected BigDecimal decode(final XSQLVAR field, final byte[] fieldData) {
                long value = field.decodeShort(fieldData);
                return BigDecimal.valueOf(value, -field.sqlscale);
            }

            @Override
            protected byte[] encode(final XSQLVAR field, final BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -field.sqlscale);
                if (unscaledValue.compareTo(MAX_SHORT) > 0 || unscaledValue.compareTo(MIN_SHORT) < 0) {
                    throw (SQLException)createException(BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
                }
                return field.encodeShort(unscaledValue.shortValue());
            }
        },
        INTEGER {
            @Override
            protected BigDecimal decode(final XSQLVAR field, final byte[] fieldData) {
                long value = field.decodeInt(fieldData);
                return BigDecimal.valueOf(value, -field.sqlscale);
            }

            @Override
            protected byte[] encode(final XSQLVAR field, final BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -field.sqlscale);
                if (unscaledValue.compareTo(MAX_INT) > 0 || unscaledValue.compareTo(MIN_INT) < 0) {
                    throw (SQLException)createException(BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
                }
                return field.encodeInt(unscaledValue.intValue());
            }
        },
        LONG {
            @Override
            protected BigDecimal decode(final XSQLVAR field, final byte[] fieldData) {
                long value = field.decodeLong(fieldData);
                return BigDecimal.valueOf(value, -field.sqlscale);
            }

            @Override
            protected byte[] encode(final XSQLVAR field, final BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -field.sqlscale);
                if (unscaledValue.compareTo(MAX_LONG) > 0 || unscaledValue.compareTo(MIN_LONG) < 0) {
                    throw (SQLException)createException(BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
                }
                return field.encodeLong(unscaledValue.longValue());
            }
        };
        
        /**
         * Decodes the provided fieldData to a BigDecimal
         * 
         * @param field XSQLVAR field instance
         * @param fieldData encoded data
         * @return BigDecimal instance
         */
        protected abstract BigDecimal decode(final XSQLVAR field, final byte[] fieldData);

        /**
         * Encodes the provided BigDecimal to fieldData
         * @param field XSQLVAR field instance
         * @param value BigDecimal instance
         * @return encoded data
         * @throws SQLException
         */
        protected abstract byte[] encode(final XSQLVAR field, final BigDecimal value) throws SQLException;
        
        /**
         * Helper method to rescale the BigDecimal to the provided scale and return the unscaled value of
         * the resulting BigDecimal.
         * 
         * @param value BigDecimal instance
         * @param scale Required scale
         * @return Unscaled value of the rescaled BigDecimal
         */
        private static BigInteger normalize(final BigDecimal value, final int scale) {
            BigDecimal valueToScale = value.setScale(scale, BigDecimal.ROUND_HALF_UP);
            return valueToScale.unscaledValue();
        }
        
        /**
         * Returns the FieldDataSize instance for the provided field.
         * @param field XSQLVAR field instance
         * @return FieldDataSize for the field, or null if none match
         */
        protected static FieldDataSize getFieldDataSize(XSQLVAR field) {
            switch (field.sqltype & ~1) {
            case ISCConstants.SQL_SHORT:
                return SHORT;
            case ISCConstants.SQL_LONG:
                return INTEGER;
            case ISCConstants.SQL_INT64:
                return LONG;
            default:
                return null;
            }
        }
    }

}
