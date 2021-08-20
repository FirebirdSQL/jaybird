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

import org.firebirdsql.extern.decimal.*;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;

/**
 * Describe class <code>FBField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class FBField {
    static final String BYTE_CONVERSION_ERROR = "Error converting to byte.";
    static final String SHORT_CONVERSION_ERROR = "Error converting to short.";
    static final String INT_CONVERSION_ERROR = "Error converting to int.";
    static final String LONG_CONVERSION_ERROR = "Error converting to long.";
    static final String FLOAT_CONVERSION_ERROR = "Error converting to float.";
    static final String DOUBLE_CONVERSION_ERROR = "Error converting to double.";
    static final String BIGDECIMAL_CONVERSION_ERROR = "Error converting to big decimal.";
    static final String BIG_INTEGER_CONVERSION_ERROR = "Error converting to BigInteger.";
    static final String DECIMAL_CONVERSION_ERROR = "Error converting to Decimal";
    static final String INT128_CONVERSION_ERROR = "Error converting to Int128";
    static final String OVERFLOW_ERROR =
            "Value is too large to fit in target type, or cannot be represented by the target type";
    static final String BOOLEAN_CONVERSION_ERROR = "Error converting to boolean.";
    static final String STRING_CONVERSION_ERROR = "Error converting to string.";
    static final String OBJECT_CONVERSION_ERROR = "Error converting to object.";
    static final String DATE_CONVERSION_ERROR = "Error converting to date.";
    static final String TIME_CONVERSION_ERROR = "Error converting to time.";
    static final String TIMESTAMP_CONVERSION_ERROR = "Error converting to timestamp.";
    static final String BINARY_STREAM_CONVERSION_ERROR = "Error converting to binary stream.";
    static final String CHARACTER_STREAM_CONVERSION_ERROR = "Error converting to character stream.";
    static final String BYTES_CONVERSION_ERROR = "Error converting to array of bytes.";
    static final String BLOB_CONVERSION_ERROR = "Error converting to Firebird BLOB object";
    static final String CLOB_CONVERSION_ERROR = "Error converting to Firebird CLOB object";
    static final String ROWID_CONVERSION_ERROR = "Error converting to Firebird RowId object";

    static final String SQL_TYPE_NOT_SUPPORTED = "SQL type for this field is not yet supported.";
    static final String SQL_ARRAY_NOT_SUPPORTED = "Types.ARRAY: " + FBField.SQL_TYPE_NOT_SUPPORTED;

    static final byte BYTE_NULL_VALUE = 0;
    static final short SHORT_NULL_VALUE = 0;
    static final int INT_NULL_VALUE = 0;
    static final long LONG_NULL_VALUE = 0;
    static final float FLOAT_NULL_VALUE = 0.0f;
    static final double DOUBLE_NULL_VALUE = 0.0;
    static final boolean BOOLEAN_NULL_VALUE = false;

    static final byte MAX_BYTE_VALUE = Byte.MAX_VALUE;
    static final byte MIN_BYTE_VALUE = Byte.MIN_VALUE;

    static final short MAX_SHORT_VALUE = Short.MAX_VALUE;
    static final short MIN_SHORT_VALUE = Short.MIN_VALUE;

    static final int MAX_INT_VALUE = Integer.MAX_VALUE;
    static final int MIN_INT_VALUE = Integer.MIN_VALUE;

    static final long MAX_LONG_VALUE = Long.MAX_VALUE;
    static final long MIN_LONG_VALUE = Long.MIN_VALUE;

    static final float MAX_FLOAT_VALUE = Float.MAX_VALUE;
    static final float MIN_FLOAT_VALUE = -1 * FBField.MAX_FLOAT_VALUE;

    static final double MAX_DOUBLE_VALUE = Double.MAX_VALUE;
    static final double MIN_DOUBLE_VALUE = -1 * FBField.MAX_DOUBLE_VALUE;

    private static final ObjectConverter OBJECT_CONVERTER = ObjectConverterHolder.INSTANCE.getObjectConverter();

    protected final FieldDescriptor fieldDescriptor;
    private final FieldDataProvider dataProvider;
    protected GDSHelper gdsHelper;
    protected int requiredType;
    protected int scale = -1;

    FBField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        if (fieldDescriptor == null) {
            throw new FBSQLException("Cannot create FBField instance with fieldDescriptor null.",
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }

        this.fieldDescriptor = fieldDescriptor;
        this.dataProvider = dataProvider;
        this.requiredType = requiredType;
    }

    protected final byte[] getFieldData() {
        return dataProvider.getFieldData();
    }

    protected final void setFieldData(byte[] data) {
        dataProvider.setFieldData(data);
    }

    protected final ObjectConverter getObjectConverter() {
        return OBJECT_CONVERTER;
    }

    protected final DatatypeCoder getDatatypeCoder() {
        return fieldDescriptor.getDatatypeCoder();
    }

    /**
     * @return {@code true} if the corresponding field is {@code null}, otherwise {@code false}
     */
    public final boolean isNull() throws SQLException {
        return getFieldData() == null;
    }

    public void setNull() {
        setFieldData(null);
    }

    public void setConnection(GDSHelper gdsHelper) {
        this.gdsHelper = gdsHelper;
    }

    /**
     * Set the required type for {@link #getObject()} conversion.
     *
     * @param requiredType
     *         required type, one of the {@link java.sql.Types} constants.
     */
    public void setRequiredType(int requiredType) {
        this.requiredType = requiredType;
    }

    /**
     * This is a factory method that creates appropriate instance of the
     * <code>FBField</code> class according to the SQL datatype. This instance
     * knows how to perform all necessary type conversions.
     */
    public static FBField createField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider,
            GDSHelper gdsHelper, boolean cached) throws SQLException {
        final FBField result = FBField.createField(fieldDescriptor, dataProvider, cached);
        result.setConnection(gdsHelper);
        return result;
    }

    private static FBField createField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider,
            boolean cached) throws SQLException {
        final int jdbcType = JdbcTypeConverter.toJdbcType(fieldDescriptor);
        switch (jdbcType) {
        case Types.SMALLINT:
            return new FBShortField(fieldDescriptor, dataProvider, jdbcType);
        case Types.INTEGER:
            return new FBIntegerField(fieldDescriptor, dataProvider, jdbcType);
        case Types.BIGINT:
            return new FBLongField(fieldDescriptor, dataProvider, jdbcType);
        case Types.NUMERIC:
        case Types.DECIMAL:
            return new FBBigDecimalField(fieldDescriptor, dataProvider, jdbcType);
        case JaybirdTypeCodes.DECFLOAT:
            switch (fieldDescriptor.getType() & ~1) {
            case ISCConstants.SQL_DEC16:
                return new FBDecfloatField<>(fieldDescriptor, dataProvider, jdbcType, Decimal64.class);
            case ISCConstants.SQL_DEC34:
                return new FBDecfloatField<>(fieldDescriptor, dataProvider, jdbcType, Decimal128.class);
            }
        case Types.FLOAT:
            return new FBFloatField(fieldDescriptor, dataProvider, jdbcType);
        case Types.DOUBLE:
            return new FBDoubleField(fieldDescriptor, dataProvider, jdbcType);
        case Types.TIME:
            return new FBTimeField(fieldDescriptor, dataProvider, jdbcType);
        case Types.DATE:
            return new FBDateField(fieldDescriptor, dataProvider, jdbcType);
        case Types.TIMESTAMP:
            return new FBTimestampField(fieldDescriptor, dataProvider, jdbcType);
        case Types.TIMESTAMP_WITH_TIMEZONE:
            return new FBTimestampTzField(fieldDescriptor, dataProvider, jdbcType);
        case Types.TIME_WITH_TIMEZONE:
            return new FBTimeTzField(fieldDescriptor, dataProvider, jdbcType);
        case Types.CHAR:
        case Types.VARCHAR:
            /*
             * TODO: Remove workaround
             * Commented by R.Rokytskyy. Until the bug is fixed in the server
             * we use "workaround" implementation of the string field. Should
             * be replaced with original one as soon as bug is fixed in the
             * engine.
             *
             * return new FBStringField(field, dataProvider, jdbcType);
             */
            return new FBWorkaroundStringField(fieldDescriptor, dataProvider, jdbcType);
        case Types.LONGVARCHAR:
            if (cached) {
                return new FBCachedLongVarCharField(fieldDescriptor, dataProvider, jdbcType);
            } else {
                return new FBLongVarCharField(fieldDescriptor, dataProvider, jdbcType);
            }
        case Types.VARBINARY:
        case Types.BINARY:
            return new FBBinaryField(fieldDescriptor, dataProvider, jdbcType);
        case Types.BLOB:
        case Types.LONGVARBINARY:
            if (cached) {
                return new FBCachedBlobField(fieldDescriptor, dataProvider, jdbcType);
            } else {
                return new FBBlobField(fieldDescriptor, dataProvider, jdbcType);
            }
        case Types.BOOLEAN:
            return new FBBooleanField(fieldDescriptor, dataProvider, jdbcType);
        case Types.NULL:
            return new FBNullField(fieldDescriptor, dataProvider, jdbcType);
        case Types.ROWID:
            return new FBRowIdField(fieldDescriptor, dataProvider, jdbcType);
        case Types.ARRAY:
            throw new FBDriverNotCapableException(FBField.SQL_ARRAY_NOT_SUPPORTED);
        default:
            throw new FBDriverNotCapableException(FBField.SQL_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * Returns the name of the column as declared in the XSQLVAR.
     */
    public String getName() {
        return fieldDescriptor.getOriginalName();
    }

    /**
     * Returns the alias of the column as declared in XSQLVAR.
     */
    public String getAlias() {
        return fieldDescriptor.getFieldName();
    }

    /**
     * Returns the relation to which belongs column as declared in XSQLVAR.
     */
    public String getRelationName() {
        return fieldDescriptor.getOriginalTableName();
    }

    /*
     * All these methods simply throw an exception when invoked. All subclasses
     * should implement relevant methods with conversions.
     */

    // --- getters

    public byte getByte() throws SQLException {
        throw new TypeConversionException(FBField.BYTE_CONVERSION_ERROR);
    }

    public short getShort() throws SQLException {
        throw new TypeConversionException(FBField.SHORT_CONVERSION_ERROR);
    }

    public int getInt() throws SQLException {
        throw new TypeConversionException(FBField.INT_CONVERSION_ERROR);
    }

    public long getLong() throws SQLException {
        throw new TypeConversionException(FBField.LONG_CONVERSION_ERROR);
    }

    public float getFloat() throws SQLException {
        throw new TypeConversionException(FBField.FLOAT_CONVERSION_ERROR);
    }

    public double getDouble() throws SQLException {
        throw new TypeConversionException(FBField.DOUBLE_CONVERSION_ERROR);
    }

    public BigDecimal getBigDecimal() throws SQLException {
        throw new TypeConversionException(FBField.BIGDECIMAL_CONVERSION_ERROR);
    }

    public BigDecimal getBigDecimal(int scale) throws SQLException {
        return getBigDecimal();
    }

    public boolean getBoolean() throws SQLException {
        throw new TypeConversionException(FBField.BOOLEAN_CONVERSION_ERROR);
    }

    public String getString() throws SQLException {
        throw new TypeConversionException(FBField.STRING_CONVERSION_ERROR);
    }

    public Object getObject() throws SQLException {
        if (isNull()) {
            return null;
        }

        switch (requiredType) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return getString();

        case Types.NUMERIC:
        case Types.DECIMAL:
            if (scale == -1) {
                return getBigDecimal();
            } else {
                return getBigDecimal(scale);
            }
        case JaybirdTypeCodes.DECFLOAT:
            return getBigDecimal();

        case Types.BIT:
        case Types.BOOLEAN:
            return getBoolean();

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return getInt();

        case Types.BIGINT:
            return getLong();

        case Types.REAL:
            return getFloat();

        case Types.FLOAT:
        case Types.DOUBLE:
            return getDouble();

        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return getBytes();

        case Types.DATE:
            return getDate();

        case Types.TIME:
            return getTime();

        case Types.TIMESTAMP:
            return getTimestamp();

        case Types.CLOB:
            return getClob();

        case Types.BLOB:
            return getBlob();

        case Types.ARRAY:
            return getArray();

        case Types.ROWID:
            return getRowId();

        default:
            throw new TypeConversionException(FBField.OBJECT_CONVERSION_ERROR);
        }
    }

    public Object getObject(Map<String, Class<?>> map) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLNonTransientException("getObject called with type null");
        }
        switch (type.getName()) {
        case BOOLEAN_CLASS_NAME:
            return isNull() ? null : (T) Boolean.valueOf(getBoolean());
        case BYTE_CLASS_NAME:
            return isNull() ? null : (T) Byte.valueOf(getByte());
        case SHORT_CLASS_NAME:
            return isNull() ? null : (T) Short.valueOf(getShort());
        case INTEGER_CLASS_NAME:
            return isNull() ? null : (T) Integer.valueOf(getInt());
        case LONG_CLASS_NAME:
            return isNull() ? null : (T) Long.valueOf(getLong());
        case FLOAT_CLASS_NAME:
            return isNull() ? null : (T) Float.valueOf(getFloat());
        case DOUBLE_CLASS_NAME:
            return isNull() ? null : (T) Double.valueOf(getDouble());
        case BIG_DECIMAL_CLASS_NAME:
            return (T) getBigDecimal();
        case BIG_INTEGER_CLASS_NAME:
            return (T) getBigInteger();
        case STRING_CLASS_NAME:
            return (T) getString();
        case BYTE_ARRAY_CLASS_NAME: // byte[]
            return (T) getBytes();
        case SQL_DATE_CLASS_NAME:
            return (T) getDate();
        case UTIL_DATE_CLASS_NAME:
        case TIMESTAMP_CLASS_NAME:
            return (T) getTimestamp();
        case TIME_CLASS_NAME:
            return (T) getTime();
        case CALENDAR_CLASS_NAME:
            if (isNull()) {
                return null;
            } else {
                Timestamp timestamp = getTimestamp();
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(timestamp.getTime());
                return (T) calendar;
            }
        case CLOB_CLASS_NAME:
        case NCLOB_CLASS_NAME:
            return (T) getClob();
        case BLOB_CLASS_NAME:
        case FIREBIRD_BLOB_CLASS_NAME:
            return (T) getBlob();
        case INPUT_STREAM_CLASS_NAME:
            return (T) getBinaryStream();
        case READER_CLASS_NAME:
            return (T) getCharacterStream();
        case ROW_ID_CLASS_NAME:
        case FB_ROW_ID_CLASS_NAME:
            return (T) getRowId();
        case RAW_DATE_TIME_STRUCT_CLASS_NAME:
            return (T) getRawDateTimeStruct();
        case DECIMAL_CLASS_NAME:
            return (T) getDecimal();
        case DECIMAL32_CLASS_NAME:
            return (T) getDecimal(Decimal32.class);
        case DECIMAL64_CLASS_NAME:
            return (T) getDecimal(Decimal64.class);
        case DECIMAL128_CLASS_NAME:
            return (T) getDecimal(Decimal128.class);
        }
        return getObjectConverter().getObject(this, type);
    }

    public InputStream getBinaryStream() throws SQLException {
        throw new TypeConversionException(FBField.BINARY_STREAM_CONVERSION_ERROR);
    }

    public Reader getCharacterStream() throws SQLException {
        final InputStream is = getBinaryStream();
        if (is == null) {
            return null;
        } else {
            return getDatatypeCoder().createReader(is);
        }
    }

    public byte[] getBytes() throws SQLException {
        throw new TypeConversionException(FBField.BYTES_CONVERSION_ERROR);
    }

    public Blob getBlob() throws SQLException {
        throw new TypeConversionException(FBField.BLOB_CONVERSION_ERROR);
    }

    public Date getDate() throws SQLException {
        throw new TypeConversionException(FBField.DATE_CONVERSION_ERROR);
    }

    public Date getDate(Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.DATE_CONVERSION_ERROR);
    }

    public Time getTime() throws SQLException {
        throw new TypeConversionException(FBField.TIME_CONVERSION_ERROR);
    }

    public Time getTime(Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.TIME_CONVERSION_ERROR);
    }

    public Timestamp getTimestamp() throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    public Ref getRef() throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    public Clob getClob() throws SQLException {
        throw new TypeConversionException(FBField.BLOB_CONVERSION_ERROR);
    }

    public Array getArray() throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    public BigInteger getBigInteger() throws SQLException {
        throw new TypeConversionException(FBField.BIG_INTEGER_CONVERSION_ERROR);
    }

    public RowId getRowId() throws SQLException {
        throw new TypeConversionException(FBField.ROWID_CONVERSION_ERROR);
    }

    // --- setters

    public void setByte(byte value) throws SQLException {
        throw new TypeConversionException(FBField.BYTE_CONVERSION_ERROR);
    }

    public void setShort(short value) throws SQLException {
        throw new TypeConversionException(FBField.SHORT_CONVERSION_ERROR);
    }

    public void setInteger(int value) throws SQLException {
        throw new TypeConversionException(FBField.INT_CONVERSION_ERROR);
    }

    public void setLong(long value) throws SQLException {
        throw new TypeConversionException(FBField.LONG_CONVERSION_ERROR);
    }

    public void setFloat(float value) throws SQLException {
        throw new TypeConversionException(FBField.FLOAT_CONVERSION_ERROR);
    }

    public void setDouble(double value) throws SQLException {
        throw new TypeConversionException(FBField.DOUBLE_CONVERSION_ERROR);
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        throw new TypeConversionException(FBField.BIGDECIMAL_CONVERSION_ERROR);
    }

    public void setBoolean(boolean value) throws SQLException {
        throw new TypeConversionException(FBField.BOOLEAN_CONVERSION_ERROR);
    }

    public void setString(String value) throws SQLException {
        throw new TypeConversionException(FBField.STRING_CONVERSION_ERROR);
    }

    public void setBigInteger(BigInteger value) throws SQLException {
        throw new TypeConversionException(FBField.BIG_INTEGER_CONVERSION_ERROR);
    }

    public void setObject(Object value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        if (value instanceof BigDecimal) {
            setBigDecimal((BigDecimal) value);
        } else if (value instanceof Blob) {
            if (value instanceof FBBlob) {
                setBlob((FBBlob) value);
            } else {
                setBinaryStream(((Blob) value).getBinaryStream());
            }
        } else if (value instanceof InputStream) {
            setBinaryStream((InputStream) value);
        } else if (value instanceof Reader) {
            setCharacterStream((Reader) value);
        } else if (value instanceof Boolean) {
            setBoolean((Boolean) value);
        } else if (value instanceof Byte) {
            setByte((Byte) value);
        } else if (value instanceof byte[]) {
            setBytes((byte[]) value);
        } else if (value instanceof Date) {
            setDate((Date) value);
        } else if (value instanceof Double) {
            setDouble((Double) value);
        } else if (value instanceof Float) {
            setFloat((Float) value);
        } else if (value instanceof Integer) {
            setInteger((Integer) value);
        } else if (value instanceof Long) {
            setLong((Long) value);
        } else if (value instanceof Short) {
            setShort((Short) value);
        } else if (value instanceof String) {
            setString((String) value);
        } else if (value instanceof Time) {
            setTime((Time) value);
        } else if (value instanceof Timestamp) {
            setTimestamp((Timestamp) value);
        } else if (value instanceof DatatypeCoder.RawDateTimeStruct) {
            setRawDateTimeStruct((DatatypeCoder.RawDateTimeStruct) value);
        } else if (value instanceof BigInteger) {
            setBigInteger((BigInteger) value);
        } else if (value instanceof RowId) {
            setRowId((RowId) value);
        } else if (value instanceof Decimal) {
            setDecimal((Decimal<?>) value);
        } else if (!getObjectConverter().setObject(this, value)) {
            throw new TypeConversionException(FBField.OBJECT_CONVERSION_ERROR);
        }
    }

    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        throw new TypeConversionException(FBField.BINARY_STREAM_CONVERSION_ERROR);
    }

    public final void setBinaryStream(InputStream in, long length) throws SQLException {
        if (length < 0) {
            throw new SQLNonTransientException("Length needs to be >= 0, was: " + length);
        }
        setBinaryStreamInternal(in, length);
    }

    public final void setBinaryStream(InputStream in) throws SQLException {
        setBinaryStreamInternal(in, -1L);
    }

    public final void setBinaryStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, (long) length);
    }

    protected void setCharacterStreamInternal(Reader in, long length) throws SQLException {
        throw new TypeConversionException(FBField.CHARACTER_STREAM_CONVERSION_ERROR);
    }

    public final void setCharacterStream(Reader in, long length) throws SQLException {
        if (length < 0) {
            throw new SQLNonTransientException("Length needs to be >= 0, was: " + length);
        }
        setCharacterStreamInternal(in, length);
    }

    public final void setCharacterStream(Reader in) throws SQLException {
        setCharacterStreamInternal(in, -1L);
    }

    public final void setCharacterStream(Reader in, int length) throws SQLException {
        setCharacterStream(in, (long) length);
    }

    public void setBytes(byte[] value) throws SQLException {
        throw new TypeConversionException(FBField.BYTES_CONVERSION_ERROR);
    }

    public void setDate(Date value, Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.DATE_CONVERSION_ERROR);
    }

    public void setDate(Date value) throws SQLException {
        throw new TypeConversionException(FBField.DATE_CONVERSION_ERROR);
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.TIME_CONVERSION_ERROR);
    }

    public void setTime(Time value) throws SQLException {
        throw new TypeConversionException(FBField.TIME_CONVERSION_ERROR);
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        throw new TypeConversionException(FBField.BLOB_CONVERSION_ERROR);
    }

    public void setClob(FBClob clob) throws SQLException {
        throw new TypeConversionException(FBField.CLOB_CONVERSION_ERROR);
    }

    public void setRowId(RowId rowId) throws SQLException {
        throw new TypeConversionException(FBField.ROWID_CONVERSION_ERROR);
    }

    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        throw new TypeConversionException(FBField.TIMESTAMP_CONVERSION_ERROR);
    }

    /**
     * Returns the value as a Decimal type.
     * <p>
     * The default for this method is implemented in terms of {@link #getBigDecimal()}, and
     * returning a {@link Decimal128}. Implementations may return a {@link Decimal64} (or even
     * {@link Decimal32}).
     * </p>
     *
     * @return The value as decimal
     * @throws SQLException
     *         For database access errors, or values that cannot be converted.
     */
    public Decimal<?> getDecimal() throws SQLException {
        BigDecimal bdValue = getBigDecimal();
        try {
            return bdValue != null
                    ? Decimal128.valueOf(bdValue, OverflowHandling.THROW_EXCEPTION)
                    : null;
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    public final <D extends Decimal<D>> D getDecimal(Class<D> targetType) throws SQLException {
        final Decimal<?> value = getDecimal();
        try {
            return value != null
                    ? value.toDecimal(targetType, OverflowHandling.THROW_EXCEPTION)
                    : null;
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    /**
     * Sets the value as a Decimal type.
     * <p>
     * The default for this method is implemented in terms of {@link #setBigDecimal(BigDecimal)}.
     * </p>
     *
     * @param decimal
     *         Value to set
     * @throws SQLException
     */
    public void setDecimal(Decimal<?> decimal) throws SQLException {
        try {
            setBigDecimal(decimal != null ? decimal.toBigDecimal() : null);
        } catch (ArithmeticException e) {
            throw new TypeConversionException(OVERFLOW_ERROR, e);
        }
    }

    @SuppressWarnings("deprecation")
    protected boolean isInvertTimeZone() {
        if (gdsHelper == null) return false;

        final IConnectionProperties props = gdsHelper.getConnectionProperties();
        return props.isTimestampUsesLocalTimezone();
    }
}
