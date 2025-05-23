// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Field for the SQL:2016 DECFLOAT type (decimal floating point), backed by an IEEE-754 Decimal64 or Decimal128.
 *
 * @author Mark Rotteveel
 */
final class FBDecfloatField<T extends Decimal<T>> extends FBField {

    private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

    private final @NonNull Class<T> decimalType;
    private final @NonNull DecimalHandling<T> decimalHandling;

    @NullMarked
    FBDecfloatField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            Class<T> decimalType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        if (!(fieldDescriptor.isFbType(ISCConstants.SQL_DEC16) || fieldDescriptor.isFbType(ISCConstants.SQL_DEC34))) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unsupportedFieldType)
                    .messageParameter(fieldDescriptor.getType())
                    .toSQLException();
        }
        this.decimalType = decimalType;
        decimalHandling = getDecimalHandling(fieldDescriptor, decimalType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getBigDecimal();
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        return convertForGet(getDecimal(), Decimal::toBigDecimal, BigDecimal.class, "value %s out of range"::formatted);
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        setDecimalInternal(convertForSet(value, decimalHandling::valueOf, BigDecimal.class,
                "value %f out of range"::formatted));
    }

    @Override
    public T getDecimal() throws SQLException {
        return decimalHandling.decode(fieldDescriptor, getFieldData());
    }

    @Override
    public void setDecimal(Decimal<?> value) throws SQLException {
        try {
            setDecimalInternal(value != null ? value.toDecimal(decimalType, OverflowHandling.THROW_EXCEPTION) : null);
        } catch (ArithmeticException e) {
            throw invalidSetConversion(requireNonNull(value).getClass(), format("value %s out of range", value), e);
        }
    }

    private void setDecimalInternal(T value) throws SQLException {
        setFieldData(convertForSet(value, v -> decimalHandling.encode(fieldDescriptor, v), decimalType,
                "value %s out of range"::formatted));
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
        // check if value is within bounds
        if (BD_MIN_LONG.compareTo(value) > 0 || value.compareTo(BD_MAX_LONG) > 0) {
            throw invalidGetConversion("long", format("value %f out of range", value));
        }

        return value.longValue();
    }

    @Override
    public void setLong(long value) throws SQLException {
        setBigDecimal(BigDecimal.valueOf(value));
    }

    @Override
    public int getInt() throws SQLException {
        long longValue = getLong();
        // check if value is within bounds
        if (longValue > MAX_INT_VALUE || longValue < MIN_INT_VALUE) {
            throw invalidGetConversion("int", format("value %d out of range", longValue));
        }

        return (int) longValue;
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setLong(value);
    }

    @Override
    public short getShort() throws SQLException {
        long longValue = getLong();
        // check if value is within bounds
        if (longValue > MAX_SHORT_VALUE || longValue < MIN_SHORT_VALUE) {
            throw invalidGetConversion("short", format("value %d out of range", longValue));
        }

        return (short) longValue;
    }

    @Override
    public void setShort(short value) throws SQLException {
        setLong(value);
    }
    
    @Override
    public byte getByte() throws SQLException {
        long longValue = getLong();
        // check if value is within bounds
        if (longValue > MAX_BYTE_VALUE || longValue < MIN_BYTE_VALUE) {
            throw invalidGetConversion("byte", format("value %d out of range", longValue));
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
        return value != null ? value.toBigInteger() : null;
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        setBigDecimal(value != null ? new BigDecimal(value) : null);
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return BigDecimal.ONE.equals(getBigDecimal());
    }

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setBigDecimal(value ? BigDecimal.ONE : BigDecimal.ZERO);
    }

    @Override
    public String getString() throws SQLException {
        T value = getDecimal();
        return value != null ? value.toString() : null;
    }

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        String string = value.trim();
        try {
            setDecimalInternal(decimalHandling.valueOf(string));
        } catch (NumberFormatException nex) {
            throw invalidSetConversion(String.class, string, nex);
        } catch (ArithmeticException e) {
            throw invalidSetConversion(String.class, format("value %s out of range", string), e);
        }
    }

    @SuppressWarnings("unchecked")
    @NullMarked
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
