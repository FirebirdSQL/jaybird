package org.firebirdsql.jdbc.field;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

import org.firebirdsql.gds.XSQLVAR;

/**
 * FBField implementation for NULL fields (eg in condition ? IS NULL).
 */
public class FBNullField extends FBField {

    private static final String NULL_CONVERSION_ERROR = 
        "Received non-NULL value of a NULL field.";

    private static final byte[] DUMMY_OBJECT = new byte[0];

    public FBNullField(XSQLVAR field, FieldDataProvider dataProvider,
            int requiredType) throws SQLException {
        super(field, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        checkNull();
        return OBJECT_NULL_VALUE;
    }

    @Override
    public void setObject(Object value) throws SQLException {
        if (value == OBJECT_NULL_VALUE)
            setNull();
        else
            setDummyObject();
    }

    // TODO set/getClob and set/getBlob are missing, relevant to add?

    private void setDummyObject() {
        setFieldData(DUMMY_OBJECT);
    }

    private void checkNull() throws SQLException {
        if (getFieldData() != null) {
            throw (SQLException) createException(NULL_CONVERSION_ERROR);
        }
    }

    // ----- Math code

    public byte getByte() throws SQLException {
        checkNull();
        return BYTE_NULL_VALUE;
    }

    public short getShort() throws SQLException {
        checkNull();
        return SHORT_NULL_VALUE;
    }

    public int getInt() throws SQLException {
        checkNull();
        return INT_NULL_VALUE;
    }

    public long getLong() throws SQLException {
        checkNull();
        return LONG_NULL_VALUE;
    }

    public BigDecimal getBigDecimal() throws SQLException {
        checkNull();
        return BIGDECIMAL_NULL_VALUE;
    }

    public float getFloat() throws SQLException {
        checkNull();
        return FLOAT_NULL_VALUE;
    }

    public double getDouble() throws SQLException {
        checkNull();
        return DOUBLE_NULL_VALUE;
    }

    // ----- getBoolean, getString and getObject code

    public boolean getBoolean() throws SQLException {
        checkNull();
        return BOOLEAN_NULL_VALUE;
    }

    public String getString() throws SQLException {
        checkNull();
        return STRING_NULL_VALUE;
    }

    // ----- getXXXStream code

    public InputStream getBinaryStream() throws SQLException {
        checkNull();
        return STREAM_NULL_VALUE;
    }

    public InputStream getUnicodeStream() throws SQLException {
        checkNull();
        return STREAM_NULL_VALUE;
    }

    public InputStream getAsciiStream() throws SQLException {
        checkNull();
        return STREAM_NULL_VALUE;
    }

    public byte[] getBytes() throws SQLException {
        checkNull();
        return BYTES_NULL_VALUE;
    }

    // ----- getDate, getTime and getTimestamp code

    public Date getDate(Calendar cal) throws SQLException {
        checkNull();
        return DATE_NULL_VALUE;
    }

    public Date getDate() throws SQLException {
        checkNull();
        return DATE_NULL_VALUE;
    }

    public Time getTime(Calendar cal) throws SQLException {
        checkNull();
        return TIME_NULL_VALUE;
    }

    public Time getTime() throws SQLException {
        checkNull();
        return TIME_NULL_VALUE;
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        checkNull();
        return TIMESTAMP_NULL_VALUE;
    }

    public Timestamp getTimestamp() throws SQLException {
        checkNull();
        return TIMESTAMP_NULL_VALUE;
    }

    // --- setXXX methods

    public void setByte(byte value) throws SQLException {
        setDummyObject();
    }

    public void setShort(short value) throws SQLException {
        setDummyObject();
    }

    public void setInteger(int value) throws SQLException {
        setDummyObject();
    }

    public void setLong(long value) throws SQLException {
        setDummyObject();
    }

    public void setFloat(float value) throws SQLException {
        setDummyObject();
    }

    public void setDouble(double value) throws SQLException {
        setDummyObject();
    }

    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    // ----- setBoolean, setObject and setObject code

    public void setBoolean(boolean value) throws SQLException {
        setDummyObject();
    }

    // ----- setXXXStream code

    public void setAsciiStream(InputStream in, int length) throws SQLException {
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    public void setUnicodeStream(InputStream in, int length) throws SQLException {
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    public void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == READER_NULL_VALUE) {
            setNull();
            return;
        }
        // TODO Do we need to consume and/or close streams?
        setDummyObject();
    }

    public void setBytes(byte[] value) throws SQLException {
        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    // ----- setDate, setTime and setTimestamp code

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    public void setDate(Date value) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    public void setTime(Time value) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        setDummyObject();
    }
}
