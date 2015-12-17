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

import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import static org.junit.Assert.*;

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
 * @param <T> FBField implementation under test
 * @param <O> Object type of FBField implementation under test
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class BaseJUnit4TestFBField<T extends FBField, O> {
    protected static final String ALIAS_VALUE = "aliasvalue";
    protected static final String NAME_VALUE = "namevalue";
    protected static final String RELATION_NAME_VALUE = "relationnamevalue";

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setThreadingPolicy(new Synchroniser());
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected FieldDataProvider fieldData;
    protected final RowDescriptorBuilder rowDescriptorBuilder =
            new RowDescriptorBuilder(1, DefaultDatatypeCoder.getDefaultInstance());
    protected FieldDescriptor fieldDescriptor;
    protected T field;

    @Before
    public void setUp() throws Exception {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        fieldData = context.mock(FieldDataProvider.class);
        rowDescriptorBuilder
                .setFieldName(ALIAS_VALUE)
                .setOriginalName(NAME_VALUE)
                .setOriginalTableName(RELATION_NAME_VALUE);
    }

    @Test
    public void getAlias() throws SQLException {
        assertEquals("Unexpected value for getAlias()", ALIAS_VALUE, field.getAlias());
    }

    @Test
    public void getArrayNonNull() throws SQLException {
        expectedException.expect(FBDriverNotCapableException.class);
        field.getArray();
    }

    @Test
    public void getAsciiStreamNonNull() throws Exception {
        expectedException.expect(TypeConversionException.class);
        field.getAsciiStream();
    }

    @Test
    public void setAsciiStreamNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setAsciiStream(context.mock(InputStream.class), 100);
    }

    @Test
    public void getBigDecimalNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getBigDecimal();
    }

    @Test
    public void setBigDecimalNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBigDecimal(BigDecimal.ONE);
    }

    @Test
    public void getBigDecimalIntNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getBigDecimal(1);
    }

    @Test
    public void getBinaryStreamNonNull() throws Exception {
        expectedException.expect(TypeConversionException.class);
        field.getBinaryStream();
    }

    @Test
    public void setBinaryStreamNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBinaryStream(context.mock(InputStream.class), 100);
    }

    @Test
    public void getBlobNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getBlob();
    }

    @Test
    public void setBlobNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBlob(context.mock(FBBlob.class));
    }

    @Test
    public void getBooleanNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getBoolean();
    }

    @Test
    public void setBoolean() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBoolean(true);
    }

    @Test
    public void getByteNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getByte();
    }

    @Test
    public void setByte() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setByte((byte)1);
    }

    @Test
    public void getBytesNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getBytes();
    }

    @Test
    public void setBytesNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBytes(new byte[] { 1, 2 });
    }

    @Test
    public void getCharacterStreamNonNull() throws Exception {
        expectedException.expect(TypeConversionException.class);
        field.getCharacterStream();
    }

    @Test
    public void setCharacterStreamNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setCharacterStream(context.mock(Reader.class), 100);
    }

    @Test
    public void getClobNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getClob();
    }

    @Test
    public void setClobNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setClob(context.mock(FBClob.class));
    }

    @Test
    public void getDateNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getDate();
    }

    @Test
    public void setDateNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setDate(java.sql.Date.valueOf("2012-03-11"));
    }

    @Test
    public void getDateCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getDate(Calendar.getInstance());
    }

    @Test
    public void setDateCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setDate(java.sql.Date.valueOf("2012-03-11"), Calendar.getInstance());
    }

    @Test
    public void getDoubleNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getDouble();
    }

    @Test
    public void setDouble() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setDouble(1.0);
    }

    @Test
    public void getFloatNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getFloat();
    }

    @Test
    public void setFloat() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setFloat(1.0f);
    }

    @Test
    public void getIntNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getInt();
    }

    @Test
    public void setInteger() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setInteger(1);
    }

    @Test
    public void getLongNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getLong();
    }

    @Test
    public void setLong() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setLong(1);
    }

    /**
     * @throws SQLException
     */
    @Test
    public void setNull() throws SQLException {
        setNullExpectations();

        field.setNull();
    }

    /**
     * @throws SQLException
     */
    @Test
    public void getName() throws SQLException {
        assertEquals("Unexpected value for getName()", NAME_VALUE, field.getName());
    }

    @Test
    public void getObjectNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getObject();
    }

    @Test
    public void setObjectNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setObject(getNonNullObject());
    }

    @Test
    public void setObjectUnsupportedType() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setObject(new Object());
    }

    @Test
    public void setObjectNull() throws SQLException {
        setNullExpectations();

        field.setObject(null);
    }

    @Test
    public void getObjectMapNonNull() throws SQLException {
        expectedException.expect(FBDriverNotCapableException.class);
        field.getObject(new HashMap<String,Class<?>>());
    }

    @Test
    public void getRefNonNull() throws SQLException {
        expectedException.expect(FBDriverNotCapableException.class);
        field.getRef();
    }

    /**
     * @throws SQLException
     */
    @Test
    public void getRelationName() throws SQLException {
        assertEquals("Unexpected value for getRelationName()", RELATION_NAME_VALUE, field.getRelationName());
    }

    @Test
    public void getShortNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getShort();
    }

    @Test
    public void setShort() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setShort((short)1);
    }

    @Test
    public void getStringNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getString();
    }

    @Test
    public void setStringNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setString("");
    }

    @Test
    public void getTimeNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getTime();
    }

    @Test
    public void setTimeNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setTime(java.sql.Time.valueOf("01:00:01"));
    }

    @Test
    public void getTimeCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getTime(Calendar.getInstance());
    }

    @Test
    public void setTimeCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setTime(java.sql.Time.valueOf("01:00:01"), Calendar.getInstance());
    }

    @Test
    public void getTimestampNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getTimestamp();
    }

    @Test
    public void setTimestampNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
    }

    @Test
    public void getTimestampCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.getTimestamp(Calendar.getInstance());
    }

    @Test
    public void setTimestampCalendarNonNull() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance());
    }

    @Test
    public void isNull_nullValue() throws SQLException {
        toReturnNullExpectations();

        assertTrue("Expected isNull() to return true for null-field", field.isNull());
    }

    @Test
    public void isNull_nonNullValue() throws SQLException {
        // TODO Check if this is sufficient, otherwise we may need to add an abstract toReturnNonNull
        toReturnValueExpectations(new byte[0]);

        assertFalse("Expected isNull() to return false for non-null-field", field.isNull());
    }

    /**
     * @return A non-null object of the right type for the field under test
     */
    protected abstract O getNonNullObject();

    // Expectation methods

    /**
     * Expectations for setting field to the supplied byte array
     * @param data byte array with expected data
     */
    protected final void setValueExpectations(final byte[] data) {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(data);
        }});
    }

    /**
     * Expectations to return a byte array from fielddata
     * @param data byte array with data to return
     */
    protected final void toReturnValueExpectations(final byte[] data) {
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(data));
        }});
    }

    /**
     * Expectations for setting field to null
     */
    protected final void setNullExpectations() {
        setValueExpectations(null);
    }

    /**
     * Expectations to return null from fieldData.
     */
    protected final void toReturnNullExpectations() {
        toReturnValueExpectations(null);
    }

    /**
     * Expectations for setting fieldData to a specific double value.
     * @param value Double value that is expected to be set
     */
    protected final void setDoubleExpectations(final double value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDouble(value));
    }

    /**
     * Expectations to return a specific double value from fieldData.
     * @param value Double value to return
     */
    protected final void toReturnDoubleExpectations(final double value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDouble(value));
    }

    /**
     * Expectations for setting fieldData to a specific short value.
     * @param value Short value that is expected to be set
     */
    protected final void setShortExpectations(final short value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeShort(value));
    }

    /**
     * Expectations to return a specific short value from fieldData.
     * @param value Short value to return
     */
    protected final void toReturnShortExpectations(final short value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeShort(value));
    }

    /**
     * Expectations for setting fieldData to a specific integer value.
     * @param value Integer value that is expected to be set
     */
    protected final void setIntegerExpectations(final int value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeInt(value));
    }

    /**
     * Expectations to return a specific integer value from fieldData.
     * @param value Integer value to return
     */
    protected final void toReturnIntegerExpectations(final int value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeInt(value));
    }

    /**
     * Expectations for setting fieldData to a specific long value.
     * @param value Long value that is expected to be set
     */
    protected final void setLongExpectations(final long value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLong(value));
    }

    /**
     * Expectations to return a specific long value from fieldData.
     * @param value Long value to return
     */
    protected final void toReturnLongExpectations(final long value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeLong(value));
    }

    /**
     * Expectations for setting fieldData to a specific Date value.
     * @param value Date value that is expected to be set
     */
    protected final void setDateExpectations(final java.sql.Date value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDate(value));
    }

    /**
     * Expectations to return a specific Date value from fieldData.
     * @param value Date value to return
     */
    protected final void toReturnDateExpectations(final java.sql.Date value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDate(value));
    }

    /**
     * Expectations for setting fieldData to a specific Date value.
     * @param value Date value that is expected to be set
     * @param calendar Calendar instance for timezone
     */
    protected final void setDateExpectations(final java.sql.Date value, Calendar calendar) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDateCalendar(value, calendar));
    }

    /**
     * Expectations to return a specific Date value from fieldData.
     * @param value Date value to return
     * @param calendar Calendar instance for timezone
     */
    protected final void toReturnDateExpectations(final java.sql.Date value, Calendar calendar) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeDateCalendar(value, calendar));
    }

    /**
     * Expectations to return a specific boolean value from fieldData.
     * @param value Boolean value to return
     */
    protected final void toReturnBooleanExpectations(final boolean value) {
        toReturnValueExpectations(fieldDescriptor.getDatatypeCoder().encodeBoolean(value));
    }

    /**
     * Expectations for setting fieldData to a specific boolean value.
     * @param value Boolean value that is expected to be set
     */
    protected final void setBooleanExpectations(final boolean value) {
        setValueExpectations(fieldDescriptor.getDatatypeCoder().encodeBoolean(value));
    }
}
