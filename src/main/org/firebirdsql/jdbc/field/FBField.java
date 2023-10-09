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
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.LegacyDatetimeConversions;
import org.firebirdsql.jdbc.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.*;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Function;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_USE_NULL;

/**
 * Base class for fields (for use by prepared statement and result set to represent columns and parameters).
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public abstract class FBField {

    static final String SQL_TYPE_NOT_SUPPORTED = "SQL type for this field is not yet supported";
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

    protected final FieldDescriptor fieldDescriptor;
    private final FieldDataProvider dataProvider;
    protected GDSHelper gdsHelper;
    protected int requiredType;

    FBField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        if (fieldDescriptor == null) {
            throw new SQLNonTransientException("Cannot create FBField instance with fieldDescriptor null",
                    SQL_STATE_INVALID_USE_NULL);
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
     * {@code FBField} class according to the SQL datatype. This instance
     * knows how to perform all necessary type conversions.
     */
    public static FBField createField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider,
            GDSHelper gdsHelper, boolean cached) throws SQLException {
        final int jdbcType = JdbcTypeConverter.toJdbcType(fieldDescriptor);
        switch (jdbcType) {
        case Types.LONGVARCHAR:
            if (cached) {
                return new FBCachedLongVarCharField(fieldDescriptor, dataProvider, jdbcType, gdsHelper);
            } else {
                return new FBLongVarCharField(fieldDescriptor, dataProvider, jdbcType, gdsHelper);
            }
        case Types.BLOB:
        case Types.LONGVARBINARY:
            if (cached) {
                return new FBCachedBlobField(fieldDescriptor, dataProvider, jdbcType, gdsHelper);
            } else {
                return new FBBlobField(fieldDescriptor, dataProvider, jdbcType, gdsHelper);
            }
        default:
            final FBField result = FBField.createField(jdbcType, fieldDescriptor, dataProvider);
            result.setConnection(gdsHelper);
            return result;
        }
    }

    private static FBField createField(int jdbcType, FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider)
            throws SQLException {
        return switch (jdbcType) {
            case Types.SMALLINT -> new FBShortField(fieldDescriptor, dataProvider, jdbcType);
            case Types.INTEGER -> new FBIntegerField(fieldDescriptor, dataProvider, jdbcType);
            case Types.BIGINT -> new FBLongField(fieldDescriptor, dataProvider, jdbcType);
            case Types.NUMERIC, Types.DECIMAL -> new FBBigDecimalField(fieldDescriptor, dataProvider, jdbcType);
            case JaybirdTypeCodes.DECFLOAT -> switch (fieldDescriptor.getType() & ~1) {
                case ISCConstants.SQL_DEC16 ->
                        new FBDecfloatField<>(fieldDescriptor, dataProvider, jdbcType, Decimal64.class);
                case ISCConstants.SQL_DEC34 ->
                        new FBDecfloatField<>(fieldDescriptor, dataProvider, jdbcType, Decimal128.class);
                default -> throw new FBDriverNotCapableException(
                        "Unexpected field type for DECFLOAT: " + fieldDescriptor.getType());
            };
            case Types.FLOAT -> new FBFloatField(fieldDescriptor, dataProvider, jdbcType);
            case Types.DOUBLE -> new FBDoubleField(fieldDescriptor, dataProvider, jdbcType);
            case Types.TIME -> new FBTimeField(fieldDescriptor, dataProvider, jdbcType);
            case Types.DATE -> new FBDateField(fieldDescriptor, dataProvider, jdbcType);
            case Types.TIMESTAMP -> new FBTimestampField(fieldDescriptor, dataProvider, jdbcType);
            case Types.TIMESTAMP_WITH_TIMEZONE -> new FBTimestampTzField(fieldDescriptor, dataProvider, jdbcType);
            case Types.TIME_WITH_TIMEZONE -> new FBTimeTzField(fieldDescriptor, dataProvider, jdbcType);
            case Types.CHAR, Types.VARCHAR -> new FBStringField(fieldDescriptor, dataProvider, jdbcType);
            case Types.VARBINARY, Types.BINARY -> new FBBinaryField(fieldDescriptor, dataProvider, jdbcType);
            case Types.BOOLEAN -> new FBBooleanField(fieldDescriptor, dataProvider, jdbcType);
            case Types.NULL -> new FBNullField(fieldDescriptor, dataProvider, jdbcType);
            case Types.ROWID -> new FBRowIdField(fieldDescriptor, dataProvider, jdbcType);
            case Types.ARRAY -> throw new FBDriverNotCapableException(FBField.SQL_ARRAY_NOT_SUPPORTED);
            default -> throw new FBDriverNotCapableException(FBField.SQL_TYPE_NOT_SUPPORTED);
        };
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
        throw invalidGetConversion("byte");
    }

    public short getShort() throws SQLException {
        throw invalidGetConversion("short");
    }

    public int getInt() throws SQLException {
        throw invalidGetConversion("int");
    }

    public long getLong() throws SQLException {
        throw invalidGetConversion("long");
    }

    public float getFloat() throws SQLException {
        throw invalidGetConversion("float");
    }

    public double getDouble() throws SQLException {
        throw invalidGetConversion("double");
    }

    public BigDecimal getBigDecimal() throws SQLException {
        throw invalidGetConversion(BigDecimal.class);
    }

    @SuppressWarnings("unused")
    public final BigDecimal getBigDecimal(int scale) throws SQLException {
        return getBigDecimal();
    }

    public boolean getBoolean() throws SQLException {
        throw invalidGetConversion("boolean");
    }

    public String getString() throws SQLException {
        throw invalidGetConversion(String.class);
    }

    public Object getObject() throws SQLException {
        if (isNull()) return null;
        throw invalidGetConversion(Object.class);
    }

    @SuppressWarnings("unused")
    public Object getObject(Map<String, Class<?>> map) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public <T> T getObject(Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLNonTransientException("getObject called with type null");
        }
        Object result = switch (type.getName()) {
            case BOOLEAN_CLASS_NAME -> isNull() ? null : getBoolean();
            case BYTE_CLASS_NAME -> isNull() ? null : getByte();
            case SHORT_CLASS_NAME -> isNull() ? null : getShort();
            case INTEGER_CLASS_NAME -> isNull() ? null : getInt();
            case LONG_CLASS_NAME -> isNull() ? null : getLong();
            case FLOAT_CLASS_NAME -> isNull() ? null : getFloat();
            case DOUBLE_CLASS_NAME -> isNull() ? null : getDouble();
            case BIG_DECIMAL_CLASS_NAME -> getBigDecimal();
            case BIG_INTEGER_CLASS_NAME -> getBigInteger();
            case STRING_CLASS_NAME -> getString();
            case BYTE_ARRAY_CLASS_NAME -> getBytes();
            case SQL_DATE_CLASS_NAME -> getDate();
            case LOCAL_DATE_CLASS_NAME -> getLocalDate();
            case UTIL_DATE_CLASS_NAME, TIMESTAMP_CLASS_NAME -> getTimestamp();
            case LOCAL_DATE_TIME_CLASS_NAME -> getLocalDateTime();
            case TIME_CLASS_NAME -> getTime();
            case LOCAL_TIME_CLASS_NAME -> getLocalTime();
            case CALENDAR_CLASS_NAME -> {
                if (isNull()) {
                    yield null;
                } else {
                    Timestamp timestamp = getTimestamp();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp.getTime());
                    yield calendar;
                }
            }
            case OFFSET_TIME_CLASS_NAME -> getOffsetTime();
            case OFFSET_DATE_TIME_CLASS_NAME -> getOffsetDateTime();
            case ZONED_DATE_TIME_CLASS_NAME -> getZonedDateTime();
            case CLOB_CLASS_NAME, NCLOB_CLASS_NAME -> getClob();
            case BLOB_CLASS_NAME, FIREBIRD_BLOB_CLASS_NAME -> getBlob();
            case INPUT_STREAM_CLASS_NAME -> getBinaryStream();
            case READER_CLASS_NAME -> getCharacterStream();
            case ROW_ID_CLASS_NAME, FB_ROW_ID_CLASS_NAME -> getRowId();
            case DECIMAL_CLASS_NAME -> getDecimal();
            case DECIMAL32_CLASS_NAME -> getDecimal(Decimal32.class);
            case DECIMAL64_CLASS_NAME -> getDecimal(Decimal64.class);
            case DECIMAL128_CLASS_NAME -> getDecimal(Decimal128.class);
            default -> throw invalidGetConversion(type);
        };
        return type.cast(result);
    }

    private String getJdbcTypeName() {
        return getJdbcTypeName(requiredType);
    }

    static String getJdbcTypeName(int jdbcType) {
        if (jdbcType == JaybirdTypeCodes.DECFLOAT) {
            return "DECFLOAT";
        }
        try {
            return JDBCType.valueOf(jdbcType).name();
        } catch (IllegalArgumentException e) {
            return String.valueOf(jdbcType);
        }
    }

    public InputStream getBinaryStream() throws SQLException {
        throw invalidGetConversion(InputStream.class);
    }

    public Reader getCharacterStream() throws SQLException {
        final InputStream is = getBinaryStream();
        return is != null ? getDatatypeCoder().createReader(is) : null;
    }

    public byte[] getBytes() throws SQLException {
        throw invalidGetConversion("byte[]");
    }

    public Blob getBlob() throws SQLException {
        throw invalidGetConversion(Blob.class);
    }

    public Date getDate() throws SQLException {
        return getDate(null);
    }

    public Date getDate(Calendar cal) throws SQLException {
        return convertForGet(getLocalDate(), v -> LegacyDatetimeConversions.toDate(v, cal), Date.class);
    }

    LocalDate getLocalDate() throws SQLException {
        throw invalidGetConversion(LocalDate.class);
    }

    public Time getTime() throws SQLException {
        return getTime(null);
    }

    public Time getTime(Calendar cal) throws SQLException {
        return convertForGet(getLocalTime(), v -> LegacyDatetimeConversions.toTime(v, cal), Time.class);
    }

    LocalTime getLocalTime() throws SQLException {
        throw invalidGetConversion(LocalTime.class);
    }

    public Timestamp getTimestamp() throws SQLException {
        return getTimestamp(null);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        return convertForGet(getLocalDateTime(), v -> LegacyDatetimeConversions.toTimestamp(v, cal), Timestamp.class);
    }

    LocalDateTime getLocalDateTime() throws SQLException {
        throw invalidGetConversion(LocalDateTime.class);
    }

    OffsetTime getOffsetTime() throws SQLException {
        throw invalidGetConversion(OffsetTime.class);
    }

    OffsetDateTime getOffsetDateTime() throws SQLException {
        throw invalidGetConversion(OffsetDateTime.class);
    }

    ZonedDateTime getZonedDateTime() throws SQLException {
        throw invalidGetConversion(ZonedDateTime.class);
    }

    public Ref getRef() throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    public Clob getClob() throws SQLException {
        throw invalidGetConversion(Clob.class);
    }

    public Array getArray() throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    public BigInteger getBigInteger() throws SQLException {
        throw invalidGetConversion(BigInteger.class);
    }

    public RowId getRowId() throws SQLException {
        throw invalidGetConversion(RowId.class);
    }

    // --- setters

    public void setByte(byte value) throws SQLException {
        throw invalidSetConversion("byte");
    }

    public void setShort(short value) throws SQLException {
        throw invalidSetConversion("short");
    }

    public void setInteger(int value) throws SQLException {
        throw invalidSetConversion("int");
    }

    public void setLong(long value) throws SQLException {
        throw invalidSetConversion("long");
    }

    public void setFloat(float value) throws SQLException {
        throw invalidSetConversion("float");
    }

    public void setDouble(double value) throws SQLException {
        throw invalidSetConversion("double");
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        throw invalidSetConversion(BigDecimal.class);
    }

    public void setBoolean(boolean value) throws SQLException {
        throw invalidSetConversion("boolean");
    }

    public void setString(String value) throws SQLException {
        throw invalidSetConversion(String.class);
    }

    public void setBigInteger(BigInteger value) throws SQLException {
        throw invalidSetConversion(BigInteger.class);
    }

    public void setObject(Object value) throws SQLException {
        if (setWhenNull(value)) return;
        // As a form of optimization, we switch on the class name.
        // For non-final classes we'll also try using instanceof in the switch default.
        switch (value.getClass().getName()) {
        case BIG_DECIMAL_CLASS_NAME -> setBigDecimal((BigDecimal) value);
        case FB_BLOB_CLASS_NAME -> setBlob((FBBlob) value);
        case FB_CLOB_CLASS_NAME -> setClob((FBClob) value);
        case BOOLEAN_CLASS_NAME -> setBoolean((boolean) value);
        case BYTE_CLASS_NAME -> setByte((byte) value);
        case BYTE_ARRAY_CLASS_NAME -> setBytes((byte[]) value);
        case SQL_DATE_CLASS_NAME -> setDate((Date) value);
        case LOCAL_DATE_CLASS_NAME -> setLocalDate((LocalDate) value);
        case DOUBLE_CLASS_NAME -> setDouble((double) value);
        case FLOAT_CLASS_NAME -> setFloat((float) value);
        case INTEGER_CLASS_NAME -> setInteger((int) value);
        case LONG_CLASS_NAME -> setLong((long) value);
        case SHORT_CLASS_NAME -> setShort((short) value);
        case STRING_CLASS_NAME -> setString((String) value);
        case TIME_CLASS_NAME -> setTime((Time) value);
        case LOCAL_TIME_CLASS_NAME -> setLocalTime((LocalTime) value);
        case TIMESTAMP_CLASS_NAME -> setTimestamp((Timestamp) value);
        case LOCAL_DATE_TIME_CLASS_NAME -> setLocalDateTime((LocalDateTime) value);
        case OFFSET_TIME_CLASS_NAME -> setOffsetTime((OffsetTime) value);
        case OFFSET_DATE_TIME_CLASS_NAME -> setOffsetDateTime((OffsetDateTime) value);
        case ZONED_DATE_TIME_CLASS_NAME -> setZonedDateTime((ZonedDateTime) value);
        case UTIL_DATE_CLASS_NAME -> setTimestamp(new Timestamp(((java.util.Date) value).getTime()));
        case BIG_INTEGER_CLASS_NAME -> setBigInteger((BigInteger) value);
        case FB_ROW_ID_CLASS_NAME -> setRowId((RowId) value);
        case DECIMAL32_CLASS_NAME, DECIMAL64_CLASS_NAME, DECIMAL128_CLASS_NAME -> setDecimal((Decimal<?>) value);
        default -> {
            if (value instanceof BigDecimal) {
                setBigDecimal((BigDecimal) value);
            } else if (value instanceof BigInteger) {
                setBigInteger((BigInteger) value);
            } else if (value instanceof RowId) {
                setRowId((RowId) value);
            } else if (value instanceof InputStream) {
                setBinaryStream((InputStream) value);
            } else if (value instanceof Reader) {
                setCharacterStream((Reader) value);
            } else if (value instanceof Clob) {
                if (value instanceof FBClob) {
                    setClob((FBClob) value);
                } else {
                    setCharacterStream(((Clob) value).getCharacterStream());
                }
            } else if (value instanceof Blob) {
                if (value instanceof FBBlob) {
                    setBlob((FBBlob) value);
                } else {
                    setBinaryStream(((Blob) value).getBinaryStream());
                }
            } else if (value instanceof Date) {
                setDate((Date) value);
            } else if (value instanceof Time) {
                setTime((Time) value);
            } else if (value instanceof Timestamp) {
                setTimestamp((Timestamp) value);
            } else if (value instanceof java.util.Date) {
                setTimestamp(new Timestamp(((java.util.Date) value).getTime()));
            } else if (value instanceof Decimal) {
                setDecimal((Decimal<?>) value);
            } else {
                throw invalidSetConversion(value.getClass());
            }
        }
        }
    }

    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        throw invalidSetConversion(InputStream.class);
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
        throw invalidSetConversion(Reader.class);
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
        throw invalidSetConversion("byte[]");
    }

    public void setDate(Date value, Calendar cal) throws SQLException {
        setLocalDate(convertForSet(value, v -> LegacyDatetimeConversions.toLocalDate(v, cal), Date.class));
    }

    public void setDate(Date value) throws SQLException {
        setDate(value, null);
    }

    void setLocalDate(LocalDate value) throws SQLException {
        throw invalidSetConversion(LocalDate.class);
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        setLocalTime(convertForSet(value, v -> LegacyDatetimeConversions.toLocalTime(v, cal), Time.class));
    }

    public void setTime(Time value) throws SQLException {
        setTime(value, null);
    }

    void setLocalTime(LocalTime value) throws SQLException {
        throw invalidSetConversion(LocalTime.class);
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        setLocalDateTime(convertForSet(value, v -> LegacyDatetimeConversions.toLocalDateTime(v, cal), Timestamp.class));
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        setTimestamp(value, null);
    }

    void setLocalDateTime(LocalDateTime value) throws SQLException {
        throw invalidSetConversion(LocalDateTime.class);
    }

    void setOffsetTime(OffsetTime value) throws SQLException {
        throw invalidSetConversion(OffsetTime.class);
    }

    void setOffsetDateTime(OffsetDateTime value) throws SQLException {
        throw invalidSetConversion(OffsetDateTime.class);
    }

    void setZonedDateTime(ZonedDateTime value) throws SQLException {
        throw invalidSetConversion(ZonedDateTime.class);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        throw invalidSetConversion(Blob.class);
    }

    public void setBlob(Blob blob) throws SQLException {
        throw invalidSetConversion(Blob.class);
    }

    FBBlob createBlob() throws SQLException {
        throw invalidSetConversion(Blob.class);
    }

    public void setClob(FBClob clob) throws SQLException {
        throw invalidSetConversion(Clob.class);
    }

    public void setClob(Clob clob) throws SQLException {
        throw invalidSetConversion(Clob.class);
    }

    final FBClob createClob() throws SQLException {
        return new FBClob(createBlob());
    }

    public void setRowId(RowId rowId) throws SQLException {
        throw invalidSetConversion(RowId.class);
    }

    /**
     * Sets the field to {@code NULL}, when {@code value} is {@code null}.
     *
     * @param value
     *         Value to check for {@code null}
     * @return {@code true} when {@code value} was {@code null}, {@code false} otherwise
     */
    final boolean setWhenNull(Object value) {
        if (value != null) return false;
        setNull();
        return true;
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
        return convertForGet(getBigDecimal(), v -> Decimal128.valueOf(v, OverflowHandling.THROW_EXCEPTION),
                Decimal128.class, "value %s out of range"::formatted);
    }

    public final <D extends Decimal<D>> D getDecimal(Class<D> targetType) throws SQLException {
        return convertForGet(getDecimal(), v -> v.toDecimal(targetType, OverflowHandling.THROW_EXCEPTION), targetType,
                "value %s out of range"::formatted);
    }

    /**
     * Sets the value as a Decimal type.
     * <p>
     * The default for this method is implemented in terms of {@link #setBigDecimal(BigDecimal)}.
     * </p>
     *
     * @param decimal
     *         Value to set
     */
    public void setDecimal(Decimal<?> decimal) throws SQLException {
        setBigDecimal(convertForSet(decimal, v -> decimal.toBigDecimal(), Decimal.class,
                "value %s out of range"::formatted));
    }

    final SQLException invalidGetConversion(Class<?> requestedType) {
        return invalidGetConversion(requestedType, null, null);
    }

    final SQLException invalidGetConversion(Class<?> requestedType, String reason) {
        return invalidGetConversion(requestedType, reason, null);
    }

    final SQLException invalidGetConversion(Class<?> requestedType, String reason, Throwable cause) {
        return invalidGetConversion(requestedType.getName(), reason, cause);
    }

    final SQLException invalidGetConversion(String requestedTypeName) {
        return invalidGetConversion(requestedTypeName, null, null);
    }

    final SQLException invalidGetConversion(String requestedTypeName, String reason) {
        return invalidGetConversion(requestedTypeName, reason, null);
    }

    final SQLException invalidGetConversion(String requestedTypeName, String reason, Throwable cause) {
        String message = String.format(
                "Unsupported get conversion requested for field %s at index %d (JDBC type %s), target type: %s",
                getAlias(), fieldDescriptor.getPosition() + 1, getJdbcTypeName(), requestedTypeName);
        if (reason != null) {
            message = message + ", reason: " + reason;
        }
        return cause != null ? new TypeConversionException(message, cause) : new TypeConversionException(message);
    }

    final SQLException invalidSetConversion(Class<?> sourceType) {
        return invalidSetConversion(sourceType, null, null);
    }

    final SQLException invalidSetConversion(Class<?> sourceType, Throwable cause) {
        return invalidSetConversion(sourceType, null, cause);
    }

    final SQLException invalidSetConversion(Class<?> sourceType, String reason) {
        return invalidSetConversion(sourceType, reason, null);
    }

    final SQLException invalidSetConversion(Class<?> sourceType, String reason, Throwable cause) {
        return invalidSetConversion(sourceType.getName(), reason, cause);
    }

    final SQLException invalidSetConversion(String sourceTypeName) {
        return invalidSetConversion(sourceTypeName, null, null);
    }

    final SQLException invalidSetConversion(String sourceTypeName, String reason) {
        return invalidSetConversion(sourceTypeName, reason, null);
    }

    final SQLException invalidSetConversion(String sourceTypeName, String reason, Throwable cause) {
        String message = "Unsupported set conversion requested for field %s at index %d (JDBC type %s), source type: %s"
                .formatted(getAlias(), fieldDescriptor.getPosition() + 1, getJdbcTypeName(), sourceTypeName);
        if (reason != null) {
            message = message + ", reason: " + reason;
        }
        return cause != null ? new TypeConversionException(message, cause) : new TypeConversionException(message);
    }

    final <T> T fromString(String value, Function<String, T> converter) throws SQLException {
        return convertForSet(value, converter.compose(String::trim), String.class);
    }

    final <S, T> T convertForSet(S value, Function<S, T> converter, Class<? extends S> sourceType) throws SQLException {
        return convertForSet(value, converter, sourceType, String::valueOf);
    }

    final <S, T> T convertForSet(S value, Function<S, T> converter, Class<? extends S> sourceType,
            Function<S, String> reasonFactory) throws SQLException {
        if (value == null) return null;
        try {
            return converter.apply(value);
        } catch (RuntimeException e) {
            throw invalidSetConversion(sourceType, reasonFactory.apply(value), e);
        }
    }

    final <S, T> T convertForGet(S value, Function<S, T> converter, Class<? extends T> requestedType)
            throws SQLException {
        return convertForGet(value, converter, requestedType, String::valueOf);
    }

    final <S, T> T convertForGet(S value, Function<S, T> converter, Class<? extends T> requestedType,
            Function<S, String> reasonFactory) throws SQLException {
        if (value == null) return null;
        try {
            return converter.apply(value);
        } catch (RuntimeException e) {
            throw invalidGetConversion(requestedType, reasonFactory.apply(value), e);
        }
    }

}
