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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.firebirdsql.extern.decimal.OverflowHandling;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.FBRowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Abstract base class for testing {@link FBField} implementations.
 * <p>
 * Basic naming conventions for tests:
 * <ul>
 * <li>methods returning objects or primitives : get&lt;type&gt;NonNull</li>
 * <li>methods receiving objects : set&lt;type&gt;NonNull</li>
 * <li>methods receiving primitive types : set&lt;type&gt;</li>
 * </ul>
 * </p>
 * <p>
 * The basic idea is that this class tests for the type conversion exceptions thrown by
 * all methods of FBField, while extending tests will override (and add) tests for the
 * specific implementation.
 * </p>
 *
 * @param <T>
 *         FBField implementation under test
 * @param <O>
 *         Object type of FBField implementation under test
 * @author Mark Rotteveel
 */
@SuppressWarnings("removal")
@ExtendWith(MockitoExtension.class)
abstract class BaseJUnit5TestFBField<T extends FBField, O> {

    private static final Random rnd = new Random();
    static final String ALIAS_VALUE = "aliasvalue";
    static final String NAME_VALUE = "namevalue";
    static final String RELATION_NAME_VALUE = "relationnamevalue";
    DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Mock
    FieldDataProvider fieldData;
    final RowDescriptorBuilder rowDescriptorBuilder = new RowDescriptorBuilder(1, datatypeCoder);
    FieldDescriptor fieldDescriptor;
    T field;

    @BeforeEach
    void setUp() throws Exception {
        rowDescriptorBuilder
                .setFieldName(ALIAS_VALUE)
                .setOriginalName(NAME_VALUE)
                .setOriginalTableName(RELATION_NAME_VALUE);
    }

    @Test
    void getAlias() {
        assertEquals(ALIAS_VALUE, field.getAlias(), "Unexpected value for getAlias()");
    }

    @Test
    void getArrayNonNull() {
        assertThrows(FBDriverNotCapableException.class, field::getArray);
    }

    @Test
    void getBigDecimalNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getBigDecimal);
    }

    @Test
    void getObject_BigDecimal() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(BigDecimal.class));
    }

    @Test
    void setBigDecimalNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setBigDecimal(BigDecimal.ONE));
    }

    @Test
    void getBigDecimalIntNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getBigDecimal(1));
    }

    @Test
    void getBinaryStreamNonNull() throws Exception {
        assertThrows(TypeConversionException.class, field::getBinaryStream);
    }

    @Test
    void getObject_InputStream() throws Exception {
        assertThrows(TypeConversionException.class, () -> field.getObject(InputStream.class));
    }

    @Test
    void setBinaryStreamNonNull() throws Exception {
        assertThrows(TypeConversionException.class, () -> field.setBinaryStream(mock(InputStream.class), 100));
    }

    @Test
    void getBlobNonNull() {
        assertThrows(TypeConversionException.class, field::getBlob);
    }

    @Test
    void getObject_Blob() {
        assertThrows(TypeConversionException.class, () -> field.getObject(Blob.class));
    }

    @Test
    void getObject_FirebirdBlob() {
        assertThrows(TypeConversionException.class, () -> field.getObject(Blob.class));
    }

    @Test
    void setBlobNonNull() {
        assertThrows(TypeConversionException.class, () -> field.setBlob(mock(FBBlob.class)));
    }

    @Test
    void getBooleanNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getBoolean());
    }

    @Test
    void getObject_Boolean() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Boolean.class));
    }

    @Test
    void setBoolean() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setBoolean(true));
    }

    @Test
    void getByteNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getByte);
    }

    @Test
    void getObject_Byte() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Byte.class));
    }

    @Test
    void setByte() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setByte((byte) 1));
    }

    @Test
    void getBytesNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getBytes);
    }

    @Test
    void getObject_byteArray() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(byte[].class));
    }

    @Test
    void setBytesNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setBytes(new byte[] { 1, 2 }));
    }

    @Test
    void getCharacterStreamNonNull() throws Exception {
        assertThrows(TypeConversionException.class, field::getCharacterStream);
    }

    @Test
    void getObject_Reader() throws Exception {
        assertThrows(TypeConversionException.class, () -> field.getObject(Reader.class));
    }

    @Test
    void setCharacterStreamNonNull() throws Exception {
        assertThrows(TypeConversionException.class, () -> field.setCharacterStream(new StringReader("test"), 100));
    }

    @Test
    void getClobNonNull() {
        assertThrows(TypeConversionException.class, field::getClob);
    }

    @Test
    void getObject_Clob() {
        assertThrows(TypeConversionException.class, () -> field.getObject(Clob.class));
    }

    @Test
    void getObject_NClob() {
        assertThrows(TypeConversionException.class, () -> field.getObject(NClob.class));
    }

    @Test
    void setClobNonNull() {
        assertThrows(TypeConversionException.class, () -> field.setClob(mock(FBClob.class)));
    }

    @Test
    void getDateNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getDate);
    }

    @Test
    void getObject_java_sql_Date() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(java.sql.Date.class));
    }

    @Test
    void setDateNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setDate(java.sql.Date.valueOf("2012-03-11")));
    }

    @Test
    void getDateCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getDate(Calendar.getInstance()));
    }

    @Test
    void setDateCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setDate(java.sql.Date.valueOf("2012-03-11"), Calendar.getInstance()));
    }

    @Test
    void getDoubleNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getDouble);
    }

    @Test
    void getObject_Double() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Double.class));
    }

    @Test
    void setDouble() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setDouble(1.0));
    }

    @Test
    void getFloatNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getFloat());
    }

    @Test
    void getObject_Float() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Float.class));
    }

    @Test
    void setFloat() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setFloat(1.0f));
    }

    @Test
    void getIntNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getInt());
    }

    @Test
    void getObject_Integer() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Integer.class));
    }

    @Test
    void setInteger() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setInteger(1));
    }

    @Test
    void getLongNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getLong);
    }

    @Test
    void getObject_Long() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Long.class));
    }

    @Test
    void setLong() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setLong(1));
    }

    @Test
    void setNull() {
        field.setNull();

        verifySetNull();
    }

    @Test
    void getName() {
        assertEquals(NAME_VALUE, field.getName(), "Unexpected value for getName()");
    }

    @Test
    void getObjectNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getObject);
    }

    @Test
    void setObjectNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setObject(getNonNullObject()));
    }

    @Test
    void setObjectUnsupportedType() {
        assertThrows(TypeConversionException.class, () -> field.setObject(new Object()));
    }

    @Test
    void setObjectNull() throws SQLException {
        field.setObject(null);

        verifySetNull();
    }

    @Test
    void getObjectMapNonNull() {
        assertThrows(FBDriverNotCapableException.class, () -> field.getObject(new HashMap<>()));
    }

    @Test
    void getRefNonNull() {
        assertThrows(FBDriverNotCapableException.class, field::getRef);
    }

    @Test
    void getRelationName() {
        assertEquals(RELATION_NAME_VALUE, field.getRelationName(), "Unexpected value for getRelationName()");
    }

    @Test
    void getShortNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getShort());
    }

    @Test
    void getObject_Short() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Short.class));
    }

    @Test
    void setShort() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setShort((short) 1));
    }

    @Test
    void getStringNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getString);
    }

    @Test
    void getObject_String() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(String.class));
    }

    @Test
    void setStringNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setString(""));
    }

    @Test
    void getTimeNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getTime);
    }

    @Test
    void getObject_java_sql_Time() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(Time.class));
    }

    @Test
    void setTimeNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setTime(Time.valueOf("01:00:01")));
    }

    @Test
    void getTimeCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getTime(Calendar.getInstance()));
    }

    @Test
    void setTimeCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setTime(Time.valueOf("01:00:01"), Calendar.getInstance()));
    }

    @Test
    void getTimestampNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getTimestamp);
    }

    @Test
    void getObject_java_sql_Timestamp() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(Timestamp.class));
    }

    @Test
    void getObject_java_util_Date() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(java.util.Date.class));
    }

    @Test
    void getObject_Calendar() throws SQLException {
        ignoringFieldData();
        assertThrows(TypeConversionException.class, () -> field.getObject(Calendar.class));
    }

    @Test
    void setTimestampNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis())));
    }

    @Test
    void getTimestampCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getTimestamp(Calendar.getInstance()));
    }

    @Test
    void setTimestampCalendarNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance()));
    }

    @Test
    void getRawDateTimeStructNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getRawDateTimeStruct);
    }

    @Test
    void getObject_RawDateTimeStruct() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(DatatypeCoder.RawDateTimeStruct.class));
    }

    @Test
    void setRawDateTimeStructNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setRawDateTimeStruct(new DatatypeCoder.RawDateTimeStruct()));
    }

    @Test
    void setObject_RawDateTimeStruct() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setObject(new DatatypeCoder.RawDateTimeStruct()));
    }

    @Test
    void getObject_BigInteger() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(BigInteger.class));
    }

    @Test
    void setObject_BigInteger() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setObject(BigInteger.ONE));
    }

    @Test
    void setRowIdNonNull() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setRowId(new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 })));
    }

    @Test
    void getRowIdNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getRowId);
    }

    @Test
    void getObject_RowId() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(RowId.class));
    }

    @Test
    void getObject_FBRowId() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.getObject(FBRowId.class));
    }

    @Test
    void setObject_RowId() throws SQLException {
        assertThrows(TypeConversionException.class,
                () -> field.setObject(new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 })));
    }

    @Test
    void isNull_nullValue() throws SQLException {
        toReturnNullExpectations();

        assertTrue(field.isNull(), "Expected isNull() to return true for null-field");
    }

    @Test
    void isNull_nonNullValue() throws SQLException {
        toReturnValueExpectations(new byte[0]);

        assertFalse(field.isNull(), "Expected isNull() to return false for non-null-field");
    }

    @Test
    void getObject_TypeNull() throws SQLException {
        assertThrows(SQLException.class, () -> field.getObject((Class<?>) null));
    }

    @Test
    void getDecimalNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, field::getDecimal);
    }

    @Test
    void setDecimalNonNull() throws SQLException {
        assertThrows(TypeConversionException.class, () -> field.setDecimal(Decimal128.valueOf("1")));
    }

    /**
     * @return A non-null object of the right type for the field under test
     */
    abstract O getNonNullObject();

    // Expectation methods

    /**
     * Verification for setting field to the supplied byte array
     *
     * @param data
     *         byte array with expected data
     */
    final void verifySetValue(byte[] data) {
        verify(fieldData).setFieldData(data);
    }

    final void verifyNotSet() {
        verify(fieldData, never()).setFieldData(any());
    }

    /**
     * Expectations to return a byte array from fielddata
     *
     * @param data
     *         byte array with data to return
     */
    final void toReturnValueExpectations(final byte[] data) {
        when(fieldData.getFieldData()).thenReturn(data);
    }

    /**
     * Verification for setting field to null
     */
    final void verifySetNull() {
        verifySetValue(null);
    }

    /**
     * Expectations to return null from fieldData.
     */
    final void toReturnNullExpectations() {
        toReturnValueExpectations(null);
    }

    /**
     * Expectations for setting fieldData to a specific double value.
     *
     * @param value
     *         Double value that is expected to be set
     */
    final void verifySetDouble(final double value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeDouble(value));
    }

    /**
     * Expectations to return a specific double value from fieldData.
     *
     * @param value
     *         Double value to return
     */
    final void toReturnDoubleExpectations(final double value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDouble(value));
    }

    /**
     * Expectations for setting fieldData to a specific float value.
     *
     * @param value
     *         Float value that is expected to be set
     */
    final void verifySetFloat(final float value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeFloat(value));
    }

    /**
     * Expectations to return a specific float value from fieldData.
     *
     * @param value
     *         Float value to return
     */
    final void toReturnFloatExpectations(final float value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeFloat(value));
    }

    /**
     * Expectations for setting fieldData to a specific short value.
     *
     * @param value
     *         Short value that is expected to be set
     */
    final void verifySetShort(final int value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeShort(value));
    }

    /**
     * Expectations to return a specific short value from fieldData.
     *
     * @param value
     *         Short value to return
     */
    final void toReturnShortExpectations(final int value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeShort(value));
    }

    /**
     * Expectations for setting fieldData to a specific integer value.
     *
     * @param value
     *         Integer value that is expected to be set
     */
    final void verifySetInteger(final int value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeInt(value));
    }

    /**
     * Expectations to return a specific integer value from fieldData.
     *
     * @param value
     *         Integer value to return
     */
    final void toReturnIntegerExpectations(final int value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeInt(value));
    }

    /**
     * Expectations for setting fieldData to a specific long value.
     *
     * @param value
     *         Long value that is expected to be set
     */
    final void verifySetLong(final long value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeLong(value));
    }

    /**
     * Expectations to return a specific long value from fieldData.
     *
     * @param value
     *         Long value to return
     */
    final void toReturnLongExpectations(final long value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLong(value));
    }

    /**
     * Expectations for setting fieldData to a specific decfloat(16) (Decimal64) value.
     *
     * @param value
     *         String representation of the value to be set
     */
    final void verifySetDecfloat16(final String value) {
        Decimal64 parsedValue = Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        verifySetValue(parsedValue.toBytes());
    }

    /**
     * Expectations to return a specific decfloat(16) (Decimal64) value from fieldData.
     *
     * @param value
     *         String representation of the value to return
     */
    final void toReturnDecfloat16Expectations(final String value) {
        Decimal64 parsedValue = Decimal64.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        toReturnValueExpectations(parsedValue.toBytes());
    }

    /**
     * Expectations for setting fieldData to a specific decfloat(34) (Decimal128) value.
     *
     * @param value
     *         String representation of the value to be set
     */
    final void verifySetDecfloat34(final String value) {
        Decimal128 parsedValue = Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        verifySetValue(parsedValue.toBytes());
    }

    /**
     * Expectations to return a specific decfloat(34) (Decimal128) value from fieldData.
     *
     * @param value
     *         String representation of the value to return
     */
    final void toReturnDecfloat34Expectations(final String value) {
        Decimal128 parsedValue = Decimal128.valueOf(value, OverflowHandling.THROW_EXCEPTION);
        toReturnValueExpectations(parsedValue.toBytes());
    }

    final void verifySetInt128(final BigInteger value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeInt128(value));
    }

    final void toReturnInt128Expectations(final BigInteger value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeInt128(value));
    }

    /**
     * Expectations for setting fieldData to a specific Date value.
     *
     * @param value
     *         Date value that is expected to be set
     */
    final void verifySetDate(LocalDate value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeLocalDate(value));
    }

    /**
     * Expectations to return a specific Date value from fieldData.
     *
     * @param value
     *         Date value to return
     */
    final void toReturnDateExpectations(LocalDate value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLocalDate(value));
    }

    final void toReturnTimeExpectations(LocalTime value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLocalTime(value));
    }

    final void verifySetTime(LocalTime value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeLocalTime(value));
    }

    final void toReturnTimestampExpectations(LocalDateTime value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLocalDateTime(value));
    }

    final void verifySetTimestamp(LocalDateTime value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeLocalDateTime(value));
    }

    /**
     * Expectations to return a specific boolean value from fieldData.
     *
     * @param value
     *         Boolean value to return
     */
    final void toReturnBooleanExpectations(final boolean value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeBoolean(value));
    }

    /**
     * Expectations for setting fieldData to a specific boolean value.
     *
     * @param value
     *         Boolean value that is expected to be set
     */
    final void verifySetBoolean(final boolean value) {
        verifySetValue(fieldDescriptor.getDatatypeCoder().encodeBoolean(value));
    }

    /**
     * Expectations to return a specific String value from fieldData.
     *
     * @param value
     *         String value to return
     * @param encoding
     *         Encoding to use
     */
    final void toReturnStringExpectations(final String value, Encoding encoding) {
        toReturnValueExpectations(encoding.encodeToCharset(value));
    }

    /**
     * Expectations for setting fieldData to a specific String value.
     *
     * @param value
     *         String value that is expected to be set
     * @param encoding
     *         Encoding to use
     */
    final void verifySetString(final String value, Encoding encoding) {
        verifySetValue(encoding.encodeToCharset(value));
    }

    void ignoringFieldData() {
        // Ensure field doesn't return null
        when(fieldData.getFieldData()).thenReturn(new byte[0]);
    }

    TimeZone getOneHourBehindTimeZone() {
        var defaultZone = TimeZone.getDefault();
        int defaultOffset = defaultZone.getRawOffset();
        int oneHourBehind = defaultOffset - (int) TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        return new SimpleTimeZone(oneHourBehind, "JAYBIRD_TEST");
    }

    private static final byte[] BASIC_ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            .getBytes(StandardCharsets.US_ASCII);

    /**
     * Generates random bytes that correspond with ASCII values 0-9, A-Z, a-z.
     * <p>
     * The limit to alphanumerics is to avoid edge cases in string conversion (especially stream vs non-stream),
     * especially with UTF-8 conversion.
     * </p>
     *
     * @param length
     *         Number of bytes
     * @return Random populated array
     */
    static byte[] getRandomBytes(int length) {
        final byte[] bytes = new byte[length];
        for (int idx = 0; idx < length; idx++) {
            bytes[idx] = BASIC_ALPHA_NUM[rnd.nextInt(BASIC_ALPHA_NUM.length)];
        }
        return bytes;
    }

}
