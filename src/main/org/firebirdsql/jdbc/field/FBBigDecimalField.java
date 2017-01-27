/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Describe class <code>FBBigDecimalField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBBigDecimalField extends FBField {

    private static final BigInteger MAX_SHORT = BigInteger.valueOf(Short.MAX_VALUE);
    private static final BigInteger MIN_SHORT = BigInteger.valueOf(Short.MIN_VALUE);

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);

    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    private static final BigDecimal BD_MAX_DOUBLE = new BigDecimal(MAX_DOUBLE_VALUE);
    private static final BigDecimal BD_MIN_DOUBLE = new BigDecimal(MIN_DOUBLE_VALUE);

    private final FieldDataSize fieldDataSize;

    FBBigDecimalField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        fieldDataSize = FieldDataSize.getFieldDataSize(fieldDescriptor);
    }

    public boolean getBoolean() throws SQLException {
        return getByte() == 1;
    }

    public byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_BYTE_VALUE || longValue < MIN_BYTE_VALUE)
            throw new TypeConversionException(BYTE_CONVERSION_ERROR);

        return (byte) longValue;
    }

    public double getDouble() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return DOUBLE_NULL_VALUE;

        return value.doubleValue();
    }

    public float getFloat() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return FLOAT_NULL_VALUE;

        return value.floatValue();
    }

    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_INT_VALUE || longValue < MIN_INT_VALUE)
            throw new TypeConversionException(INT_CONVERSION_ERROR);

        return (int) longValue;
    }

    public long getLong() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return LONG_NULL_VALUE;

        return value.longValue();
    }

    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_SHORT_VALUE || longValue < MIN_SHORT_VALUE)
            throw new TypeConversionException(SHORT_CONVERSION_ERROR);

        return (short) longValue;
    }

    public String getString() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return null;

        return value.toString();
    }

    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;

        return fieldDataSize.decode(fieldDescriptor, getFieldData());
    }

    @Override
    public BigInteger getBigInteger() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return null;
        return value.toBigInteger();
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
        if (value == null) {
            setNull();
            return;
        }

        try {
            setBigDecimal(new BigDecimal(value));
        } catch (NumberFormatException nex) {
            throw new TypeConversionException(STRING_CONVERSION_ERROR);
        }
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setFieldData(fieldDataSize.encode(fieldDescriptor, value));
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setBigDecimal(new BigDecimal(value));
    }

    /**
     * Enum for handling the different field data sizes of NUMERIC/DECIMAL fields.
     *
     * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
     */
    private enum FieldDataSize {
        SHORT {
            @Override
            protected BigDecimal decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
                long value = fieldDescriptor.getDatatypeCoder().decodeShort(fieldData);
                return BigDecimal.valueOf(value, -1 * fieldDescriptor.getScale());
            }

            @Override
            protected byte[] encode(FieldDescriptor fieldDescriptor, BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -1 * fieldDescriptor.getScale());
                if (unscaledValue.compareTo(MAX_SHORT) > 0 || unscaledValue.compareTo(MIN_SHORT) < 0) {
                    throw new TypeConversionException(BIGDECIMAL_CONVERSION_ERROR);
                }
                return fieldDescriptor.getDatatypeCoder().encodeShort(unscaledValue.shortValue());
            }
        },
        INTEGER {
            @Override
            protected BigDecimal decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
                long value = fieldDescriptor.getDatatypeCoder().decodeInt(fieldData);
                return BigDecimal.valueOf(value, -1 * fieldDescriptor.getScale());
            }

            @Override
            protected byte[] encode(FieldDescriptor fieldDescriptor, BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -1 * fieldDescriptor.getScale());
                if (unscaledValue.compareTo(MAX_INT) > 0 || unscaledValue.compareTo(MIN_INT) < 0) {
                    throw new TypeConversionException(BIGDECIMAL_CONVERSION_ERROR);
                }
                return fieldDescriptor.getDatatypeCoder().encodeInt(unscaledValue.intValue());
            }
        },
        LONG {
            @Override
            protected BigDecimal decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
                long value = fieldDescriptor.getDatatypeCoder().decodeLong(fieldData);
                return BigDecimal.valueOf(value, -1 * fieldDescriptor.getScale());
            }

            @Override
            protected byte[] encode(FieldDescriptor fieldDescriptor, BigDecimal value) throws SQLException {
                BigInteger unscaledValue = normalize(value, -1 * fieldDescriptor.getScale());
                if (unscaledValue.compareTo(MAX_LONG) > 0 || unscaledValue.compareTo(MIN_LONG) < 0) {
                    throw new TypeConversionException(BIGDECIMAL_CONVERSION_ERROR);
                }
                return fieldDescriptor.getDatatypeCoder().encodeLong(unscaledValue.longValue());
            }
        },
        DOUBLE {
            @Override
            protected BigDecimal decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
                BigDecimal value = new BigDecimal(fieldDescriptor.getDatatypeCoder().decodeDouble(fieldData));
                return value.setScale(Math.abs(fieldDescriptor.getScale()), BigDecimal.ROUND_HALF_EVEN);
            }

            @Override
            protected byte[] encode(FieldDescriptor fieldDescriptor, BigDecimal value) throws SQLException {
                // check if value is within bounds
                if (value.compareTo(BD_MAX_DOUBLE) > 0 ||
                        value.compareTo(BD_MIN_DOUBLE) < 0)
                    throw new TypeConversionException(DOUBLE_CONVERSION_ERROR + " " + value);

                return fieldDescriptor.getDatatypeCoder().encodeDouble(value.doubleValue());
            }
        };

        /**
         * Decodes the provided fieldData to a BigDecimal
         *
         * @param fieldDescriptor
         *         Field descriptor
         * @param fieldData
         *         encoded data
         * @return BigDecimal instance
         */
        protected abstract BigDecimal decode(final FieldDescriptor fieldDescriptor, final byte[] fieldData);

        /**
         * Encodes the provided BigDecimal to fieldData
         *
         * @param fieldDescriptor
         *         Field descriptor
         * @param value
         *         BigDecimal instance
         * @return encoded data
         */
        protected abstract byte[] encode(final FieldDescriptor fieldDescriptor,
                final BigDecimal value) throws SQLException;

        /**
         * Helper method to rescale the BigDecimal to the provided scale and return the unscaled value of
         * the resulting BigDecimal.
         *
         * @param value
         *         BigDecimal instance
         * @param scale
         *         Required scale
         * @return Unscaled value of the rescaled BigDecimal
         */
        private static BigInteger normalize(final BigDecimal value, final int scale) {
            BigDecimal valueToScale = value.setScale(scale, BigDecimal.ROUND_HALF_UP);
            return valueToScale.unscaledValue();
        }

        /**
         * Returns the FieldDataSize instance for the provided field.
         *
         * @param fieldDescriptor
         *         Field descriptor
         * @return FieldDataSize for the field
         * @throws SQLException For unsupported field types
         */
        protected static FieldDataSize getFieldDataSize(FieldDescriptor fieldDescriptor) throws SQLException {
            switch (fieldDescriptor.getType() & ~1) {
            case ISCConstants.SQL_SHORT:
                return SHORT;
            case ISCConstants.SQL_LONG:
                return INTEGER;
            case ISCConstants.SQL_INT64:
                return LONG;
            case ISCConstants.SQL_DOUBLE:
                return DOUBLE;
            default:
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                        .messageParameter(fieldDescriptor.getType())
                        .toFlatSQLException();
            }
        }
    }

}
