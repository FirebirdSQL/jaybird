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
import java.time.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;

/**
 * Describe class <code>FBField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class FBField {

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

    protected final FieldDescriptor fieldDescriptor;
    private final FieldDataProvider dataProvider;
    protected GDSHelper gdsHelper;
    protected int requiredType;

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
            return new FBStringField(fieldDescriptor, dataProvider, jdbcType);
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
        if (isNull()) {
            return null;
        }
        throw invalidGetConversion(Object.class);
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
        case LOCAL_DATE_CLASS_NAME:
            return (T) getLocalDate();
        case UTIL_DATE_CLASS_NAME:
        case TIMESTAMP_CLASS_NAME:
            return (T) getTimestamp();
        case LOCAL_DATE_TIME_CLASS_NAME:
            return (T) getLocalDateTime();
        case TIME_CLASS_NAME:
            return (T) getTime();
        case LOCAL_TIME_CLASS_NAME:
            return (T) getLocalTime();
        case CALENDAR_CLASS_NAME:
            if (isNull()) {
                return null;
            } else {
                Timestamp timestamp = getTimestamp();
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(timestamp.getTime());
                return (T) calendar;
            }
        case OFFSET_TIME_CLASS_NAME:
            return (T) getOffsetTime();
        case OFFSET_DATE_TIME_CLASS_NAME:
            return (T) getOffsetDateTime();
        case ZONED_DATE_TIME_CLASS_NAME:
            return (T) getZonedDateTime();
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
        default:
            throw invalidGetConversion(type);
        }
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
        if (is == null) {
            return null;
        } else {
            return getDatatypeCoder().createReader(is);
        }
    }

    public byte[] getBytes() throws SQLException {
        throw invalidGetConversion("byte[]");
    }

    public Blob getBlob() throws SQLException {
        throw invalidGetConversion(Blob.class);
    }

    public Date getDate() throws SQLException {
        throw invalidGetConversion(Date.class);
    }

    public Date getDate(Calendar cal) throws SQLException {
        throw invalidGetConversion(Date.class);
    }

    LocalDate getLocalDate() throws SQLException {
        throw invalidGetConversion(LocalDate.class);
    }

    public Time getTime() throws SQLException {
        throw invalidGetConversion(Time.class);
    }

    public Time getTime(Calendar cal) throws SQLException {
        throw invalidGetConversion(Time.class);
    }

    LocalTime getLocalTime() throws SQLException {
        throw invalidGetConversion(LocalTime.class);
    }

    public Timestamp getTimestamp() throws SQLException {
        throw invalidGetConversion(Timestamp.class);
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        throw invalidGetConversion(Timestamp.class);
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
        if (value == null) {
            setNull();
            return;
        }

        String typeName = value.getClass().getName();
        // As a form of optimization, we switch on the class name.
        // For non-final classes we'll also try using instanceof in the switch default.
        switch (typeName) {
        case BIG_DECIMAL_CLASS_NAME:
            setBigDecimal((BigDecimal) value);
            return;
        case FB_BLOB_CLASS_NAME:
            setBlob((FBBlob) value);
            return;
        case FB_CLOB_CLASS_NAME:
            setClob((FBClob) value);
            return;
        case BOOLEAN_CLASS_NAME:
            setBoolean((boolean) value);
            return;
        case BYTE_CLASS_NAME:
            setByte((byte) value);
            return;
        case BYTE_ARRAY_CLASS_NAME:
            setBytes((byte[]) value);
            return;
        case SQL_DATE_CLASS_NAME:
            setDate((Date) value);
            return;
        case LOCAL_DATE_CLASS_NAME:
            setLocalDate((LocalDate) value);
            return;
        case DOUBLE_CLASS_NAME:
            setDouble((double) value);
            return;
        case FLOAT_CLASS_NAME:
            setFloat((float) value);
            return;
        case INTEGER_CLASS_NAME:
            setInteger((int) value);
            return;
        case LONG_CLASS_NAME:
            setLong((long) value);
            return;
        case SHORT_CLASS_NAME:
            setShort((short) value);
            return;
        case STRING_CLASS_NAME:
            setString((String) value);
            return;
        case TIME_CLASS_NAME:
            setTime((Time) value);
            return;
        case LOCAL_TIME_CLASS_NAME:
            setLocalTime((LocalTime) value);
            return;
        case TIMESTAMP_CLASS_NAME:
            setTimestamp((Timestamp) value);
            return;
        case LOCAL_DATE_TIME_CLASS_NAME:
            setLocalDateTime((LocalDateTime) value);
            return;
        case OFFSET_TIME_CLASS_NAME:
            setOffsetTime((OffsetTime) value);
            return;
        case OFFSET_DATE_TIME_CLASS_NAME:
            setOffsetDateTime((OffsetDateTime) value);
            return;
        case ZONED_DATE_TIME_CLASS_NAME:
            setZonedDateTime((ZonedDateTime) value);
            return;
        case UTIL_DATE_CLASS_NAME:
            setTimestamp(new Timestamp(((java.util.Date) value).getTime()));
            return;
        case RAW_DATE_TIME_STRUCT_CLASS_NAME:
            setRawDateTimeStruct((DatatypeCoder.RawDateTimeStruct) value);
            return;
        case BIG_INTEGER_CLASS_NAME:
            setBigInteger((BigInteger) value);
            return;
        case FB_ROW_ID_CLASS_NAME:
            setRowId((RowId) value);
            return;
        case DECIMAL32_CLASS_NAME:
        case DECIMAL64_CLASS_NAME:
        case DECIMAL128_CLASS_NAME:
            setDecimal((Decimal<?>) value);
            return;
        default:
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
        throw invalidSetConversion(Date.class);
    }

    public void setDate(Date value) throws SQLException {
        throw invalidSetConversion(Date.class);
    }

    void setLocalDate(LocalDate value) throws SQLException {
        throw invalidSetConversion(LocalDate.class);
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        throw invalidSetConversion(Time.class);
    }

    public void setTime(Time value) throws SQLException {
        throw invalidSetConversion(Time.class);
    }

    void setLocalTime(LocalTime value) throws SQLException {
        throw invalidSetConversion(LocalTime.class);
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        throw invalidSetConversion(Timestamp.class);
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        throw invalidSetConversion(Timestamp.class);
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

    public void setClob(FBClob clob) throws SQLException {
        throw invalidSetConversion(Clob.class);
    }

    public void setRowId(RowId rowId) throws SQLException {
        throw invalidSetConversion(RowId.class);
    }

    public DatatypeCoder.RawDateTimeStruct getRawDateTimeStruct() throws SQLException {
        throw invalidGetConversion(DatatypeCoder.RawDateTimeStruct.class);
    }

    public void setRawDateTimeStruct(DatatypeCoder.RawDateTimeStruct raw) throws SQLException {
        throw invalidSetConversion(DatatypeCoder.RawDateTimeStruct.class);
    }

    /**
     * Sets the field to {@code NULL}, when {@code value} is {@code null}.
     *
     * @param value
     *         Value to check for {@code null}
     * @return {@code true} when {@code value} was {@code null}, {@code false} otherwise
     */
    final boolean setWhenNull(Object value) {
        if (value == null) {
            setNull();
            return true;
        }
        return false;
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
            SQLException conversionException = invalidGetConversion(Decimal128.class,
                    String.format("value %s out of range", bdValue));
            conversionException.initCause(e);
            throw conversionException;
        }
    }

    public final <D extends Decimal<D>> D getDecimal(Class<D> targetType) throws SQLException {
        final Decimal<?> value = getDecimal();
        try {
            return value != null
                    ? value.toDecimal(targetType, OverflowHandling.THROW_EXCEPTION)
                    : null;
        } catch (ArithmeticException e) {
            SQLException conversionException = invalidGetConversion(targetType,
                    String.format("value %s out of range", value));
            conversionException.initCause(e);
            throw conversionException;
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
     */
    public void setDecimal(Decimal<?> decimal) throws SQLException {
        try {
            setBigDecimal(decimal != null ? decimal.toBigDecimal() : null);
        } catch (ArithmeticException e) {
            SQLException conversionException = invalidSetConversion(requireNonNull(decimal).getClass(),
                    String.format("value %s out of range", decimal));
            conversionException.initCause(e);
            throw conversionException;
        }
    }

    @SuppressWarnings("deprecation")
    protected boolean isInvertTimeZone() {
        if (gdsHelper == null) return false;

        final IConnectionProperties props = gdsHelper.getConnectionProperties();
        return props.isTimestampUsesLocalTimezone();
    }

    final SQLException invalidGetConversion(Class<?> requestedType) {
        return invalidGetConversion(requestedType.getName(), null);
    }

    final SQLException invalidGetConversion(Class<?> requestedType, String reason) {
        return invalidGetConversion(requestedType.getName(), reason);
    }

    final SQLException invalidGetConversion(String requestedTypeName) {
        return invalidGetConversion(requestedTypeName, null);
    }

    final SQLException invalidGetConversion(String requestedTypeName, String reason) {
        String message = String.format(
                "Unsupported get conversion requested for field %s at index %d (JDBC type %s), target type: %s",
                getAlias(), fieldDescriptor.getPosition() + 1, getJdbcTypeName(), requestedTypeName);
        if (reason != null) {
            message = message + ", reason: " + reason;
        }
        return new TypeConversionException(message);
    }

    final SQLException invalidSetConversion(Class<?> sourceType) {
        return invalidSetConversion(sourceType.getName(), null);
    }

    final SQLException invalidSetConversion(Class<?> sourceType, String reason) {
        return invalidSetConversion(sourceType.getName(), reason);
    }

    final SQLException invalidSetConversion(String sourceTypeName) {
        return invalidSetConversion(sourceTypeName, null);
    }

    final SQLException invalidSetConversion(String sourceTypeName, String reason) {
        String message = String.format(
                "Unsupported set conversion requested for field %s at index %d (JDBC type %s), source type: %s",
                getAlias(), fieldDescriptor.getPosition() + 1, getJdbcTypeName(), sourceTypeName);
        if (reason != null) {
            message = message + ", reason: " + reason;
        }
        return new TypeConversionException(message);
    }

    final <T> T fromString(String value, Function<String, T> converter) throws SQLException {
        if (value == null) return null;
        String string = value.trim();
        try {
            return converter.apply(value);
        } catch (RuntimeException e) {
            SQLException conversionException = invalidSetConversion(String.class, string);
            conversionException.initCause(e);
            throw conversionException;
        }
    }

}
