// SPDX-FileCopyrightText: Copyright 2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * FBField implementation for NULL fields (e.g. in condition ? IS NULL).
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
final class FBNullField extends FBField {

    private static final String NULL_CONVERSION_ERROR = "Received non-NULL value of a NULL field.";

    @NullMarked
    FBNullField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getAsNull();
    }

    @Override
    public void setObject(Object value) throws SQLException {
        if (value == null)
            setNull();
        else
            setDummyObject();
    }

    // TODO set/getClob and set/getBlob are missing, relevant to add?

    private void setDummyObject() {
        setFieldData(ByteArrayHelper.emptyByteArray());
    }

    private void checkNull() throws SQLException {
        if (isNull()) {
            throw new TypeConversionException(NULL_CONVERSION_ERROR);
        }
    }

    private <T> T getAsNull() throws SQLException {
        checkNull();
        return null;
    }

    // ----- Math code

    @Override
    public byte getByte() throws SQLException {
        checkNull();
        return BYTE_NULL_VALUE;
    }

    @Override
    public short getShort() throws SQLException {
        checkNull();
        return SHORT_NULL_VALUE;
    }

    @Override
    public int getInt() throws SQLException {
        checkNull();
        return INT_NULL_VALUE;
    }

    @Override
    public long getLong() throws SQLException {
        checkNull();
        return LONG_NULL_VALUE;
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        return getAsNull();
    }

    @Override
    public float getFloat() throws SQLException {
        checkNull();
        return FLOAT_NULL_VALUE;
    }

    @Override
    public double getDouble() throws SQLException {
        checkNull();
        return DOUBLE_NULL_VALUE;
    }

    // ----- getBoolean, getString and getObject code

    @Override
    public boolean getBoolean() throws SQLException {
        checkNull();
        return BOOLEAN_NULL_VALUE;
    }

    @Override
    public String getString() throws SQLException {
        return getAsNull();
    }

    // ----- getXXXStream code

    @Override
    public InputStream getBinaryStream() throws SQLException {
        return getAsNull();
    }

    @Override
    public byte[] getBytes() throws SQLException {
        return getAsNull();
    }

    // ----- getDate, getTime and getTimestamp code

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        return getAsNull();
    }

    @Override
    public Date getDate() throws SQLException {
        return getAsNull();
    }

    @Override
    public Time getTime(Calendar cal) throws SQLException {
        return getAsNull();
    }

    @Override
    public Time getTime() throws SQLException {
        return getAsNull();
    }

    @Override
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        return getAsNull();
    }

    @Override
    public Timestamp getTimestamp() throws SQLException {
        return getAsNull();
    }

    // --- setXXX methods

    @Override
    public void setByte(byte value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setShort(short value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setLong(long value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setFloat(float value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setDouble(double value) throws SQLException {
        setDummyObject();
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        setObject(value);
    }

    // ----- setBoolean, setObject and setObject code

    @Override
    public void setBoolean(boolean value) throws SQLException {
        setDummyObject();
    }

    // ----- setXXXStream code

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        if (setWhenNull(in)) return;
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    @Override
    protected void setCharacterStreamInternal(Reader in, long length) throws SQLException {
        if (setWhenNull(in)) return;
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        setObject(value);
    }

    // ----- setDate, setTime and setTimestamp code

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        setObject(value);
    }

    @Override
    public void setDate(Date value) throws SQLException {
        setObject(value);
    }

    @Override
    public void setTime(Time value, Calendar cal) throws SQLException {
        setObject(value);
    }

    @Override
    public void setTime(Time value) throws SQLException {
        setObject(value);
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        setObject(value);
    }

    @Override
    public void setTimestamp(Timestamp value) throws SQLException {
        setObject(value);
    }

    @Override
    public void setString(String value) throws SQLException {
        setObject(value);
    }
}
