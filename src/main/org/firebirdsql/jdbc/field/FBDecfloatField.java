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

import org.firebirdsql.extern.decimal.Decimal;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.firebirdsql.extern.decimal.OverflowHandling;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBDriverNotCapableException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Field for the SQL:2016 DECFLOAT type (decimal floating point), backed by an IEEE-754 Decimal64 or Decimal128.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBDecfloatField<T extends Decimal<T>> extends FBField {

    private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

    private final Class<T> decimalType;
    private final DecimalHandling<T> decimalHandling;

    FBDecfloatField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            Class<T> decimalType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        if (!(fieldDescriptor.isFbType(ISCConstants.SQL_DEC16) || fieldDescriptor.isFbType(ISCConstants.SQL_DEC34))) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                    .messageParameter(fieldDescriptor.getType())
                    .toFlatSQLException();
        }
        this.decimalType = decimalType;
        decimalHandling = getDecimalHandling(fieldDescriptor, decimalType);
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        T value = getDecimal();
        if (value == null) return null;

        try {
            return value.toBigDecimal();
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        try {
            setDecimalInternal(value != null ? decimalHandling.valueOf(value) : null);
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    @Override
    public T getDecimal() throws SQLException {
        if (isNull()) return null;

        return decimalHandling.decode(fieldDescriptor, getFieldData());
    }

    @Override
    public void setDecimal(Decimal<?> value) throws SQLException {
        try {
            setDecimalInternal(value != null ? value.toDecimal(decimalType, OverflowHandling.THROW_EXCEPTION) : null);
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    private void setDecimalInternal(T value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        // TODO Reject +/-Infinity, +/-Nan and +/-sNaN?
        try {
            setFieldData(decimalHandling.encode(fieldDescriptor, value));
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    @Override
    public double getDouble() throws SQLException {
        T value = getDecimal();
        return value != null ? value.doubleValue() : DOUBLE_NULL_VALUE;
    }

    @Override
    public void setDouble(double value) throws SQLException {
        setDecimalInternal(decimalHandling.valueOf(value));
    }

    @Override
    public float getFloat() throws SQLException {
        return (float) getDouble();
    }

    @Override
    public void setFloat(float value) throws SQLException {
        setDouble(value);
    }

    @Override
    public long getLong() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return LONG_NULL_VALUE;

        if (BD_MIN_LONG.compareTo(value) > 0 || value.compareTo(BD_MAX_LONG) > 0) {
            throw new TypeConversionException(LONG_CONVERSION_ERROR);
        }
        return value.longValue();
    }

    @Override
    public void setLong(long value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value));
    }

    @Override
    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_INT_VALUE || longValue < MIN_INT_VALUE) {
            throw new TypeConversionException(INT_CONVERSION_ERROR);
        } else {
            return (int) longValue;
        }
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setLong(value);
    }

    @Override
    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_SHORT_VALUE || longValue < MIN_SHORT_VALUE) {
            throw new TypeConversionException(SHORT_CONVERSION_ERROR);
        }

        return (short) longValue;
    }

    @Override
    public void setShort(short value) throws SQLException {
        setLong(value);
    }
    
    @Override
    public byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;
        long longValue = getLong();

        // check if value is within bounds
        if (longValue > MAX_BYTE_VALUE || longValue < MIN_BYTE_VALUE) {
            throw new TypeConversionException(BYTE_CONVERSION_ERROR);
        }

        return (byte) longValue;
    }

    @Override
    public void setByte(byte value) throws SQLException {
        setLong(value);
    }
    
    @Override
    public BigInteger getBigInteger() throws SQLException {
        BigDecimal value = getBigDecimal();
        if (value == null) return null;
        return value.toBigInteger();
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setBigDecimal(new BigDecimal(value));
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return BigDecimal.ONE.equals(getBigDecimal());
    }

    public void setBoolean(boolean value) throws SQLException {
        setBigDecimal(value ? BigDecimal.ONE : BigDecimal.ZERO);
    }

    @Override
    public String getString() throws SQLException {
        T value = getDecimal();
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            setDecimalInternal(decimalHandling.valueOf(value));
        } catch (NumberFormatException nex) {
            throw new TypeConversionException(STRING_CONVERSION_ERROR);
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    @SuppressWarnings("unchecked")
    private DecimalHandling<T> getDecimalHandling(FieldDescriptor fieldDescriptor, Class<T> decimalType)
            throws FBDriverNotCapableException {
        if (decimalType == Decimal64.class && fieldDescriptor.isFbType(ISCConstants.SQL_DEC16)) {
            return (DecimalHandling<T>) Decimal64Handling.INSTANCE;
        } else if (decimalType == Decimal128.class && fieldDescriptor.isFbType(ISCConstants.SQL_DEC34)) {
            return (DecimalHandling<T>) Decimal128Handling.INSTANCE;
        } else {
            throw new FBDriverNotCapableException("Unsupported type " + decimalType.getName() + " and/or field type " +
                    fieldDescriptor.getType());
        }
    }

    private interface DecimalHandling<T extends Decimal<T>> {

        byte[] encode(FieldDescriptor fieldDescriptor, T value);

        T decode(FieldDescriptor fieldDescriptor, byte[] fieldData);

        T valueOf(double value);

        T valueOf(String value);

        T valueOf(BigDecimal value);

    }

    private static final class Decimal64Handling implements DecimalHandling<Decimal64> {

        private static final Decimal64Handling INSTANCE = new Decimal64Handling();

        @Override
        public byte[] encode(FieldDescriptor fieldDescriptor, Decimal64 value) {
            return fieldDescriptor.getDatatypeCoder().encodeDecimal64(value);
        }

        @Override
        public Decimal64 decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
            return fieldDescriptor.getDatatypeCoder().decodeDecimal64(fieldData);
        }

        @Override
        public Decimal64 valueOf(double value) {
            return Decimal64.valueOf(value);
        }

        @Override
        public Decimal64 valueOf(String value) {
            return Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        }

        @Override
        public Decimal64 valueOf(BigDecimal value) {
            return Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        }

    }

    private static final class Decimal128Handling implements DecimalHandling<Decimal128> {

        private static final Decimal128Handling INSTANCE = new Decimal128Handling();

        @Override
        public byte[] encode(FieldDescriptor fieldDescriptor, Decimal128 value) {
            return fieldDescriptor.getDatatypeCoder().encodeDecimal128(value);
        }

        @Override
        public Decimal128 decode(FieldDescriptor fieldDescriptor, byte[] fieldData) {
            return fieldDescriptor.getDatatypeCoder().decodeDecimal128(fieldData);
        }

        @Override
        public Decimal128 valueOf(double value) {
            return Decimal128.valueOf(value);
        }

        @Override
        public Decimal128 valueOf(String value) {
            return Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        }

        @Override
        public Decimal128 valueOf(BigDecimal value) {
            return Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        }

    }
}
