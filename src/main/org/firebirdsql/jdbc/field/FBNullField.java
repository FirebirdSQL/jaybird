package org.firebirdsql.jdbc.field;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

import org.firebirdsql.gds.XSQLVAR;


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
        if (getFieldData() != null)
            throw (SQLException)createException(NULL_CONVERSION_ERROR);
        
        return OBJECT_NULL_VALUE;
    }
    
    @Override
    public void setObject(Object value) throws SQLException {
        if (value == null)
            setNull();
        else
            setFieldData(DUMMY_OBJECT);
    }
    
    //----- Math code

    public byte getByte() throws SQLException {
        if (getFieldData()==null) return BYTE_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public short getShort() throws SQLException {
        if (getFieldData()==null) return SHORT_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public int getInt() throws SQLException {
        if (getFieldData()==null) return INT_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public long getLong() throws SQLException {
        if (getFieldData()==null) return LONG_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public BigDecimal getBigDecimal() throws SQLException {
        if (getFieldData()==null) return BIGDECIMAL_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public float getFloat() throws SQLException {
        if (getFieldData()==null) return FLOAT_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public double getDouble() throws SQLException {
        if (getFieldData()==null) return DOUBLE_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }

    //----- getBoolean, getString and getObject code

    public boolean getBoolean() throws SQLException {
        if (getFieldData()==null) return BOOLEAN_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public String getString() throws SQLException {
        if (getFieldData()==null) return STRING_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    
    //----- getXXXStream code

    public InputStream getBinaryStream() throws SQLException {
        if (getFieldData()==null) return STREAM_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public InputStream getUnicodeStream() throws SQLException {
        if (getFieldData()==null) return STREAM_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public InputStream getAsciiStream() throws SQLException {
        if (getFieldData()==null) return STREAM_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public byte[] getBytes() throws SQLException {
        if (getFieldData()==null) return BYTES_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }

    //----- getDate, getTime and getTimestamp code

    public Date getDate(Calendar cal) throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public Date getDate() throws SQLException {
        if (getFieldData()==null) return DATE_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public Time getTime(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public Time getTime() throws SQLException {
        if (getFieldData()==null) return TIME_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;
          
        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    public Timestamp getTimestamp() throws SQLException {
        if (getFieldData()==null) return TIMESTAMP_NULL_VALUE;

        throw (SQLException)createException(NULL_CONVERSION_ERROR);
    }
    
    //--- setXXX methods

    
    public void setByte(byte value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setShort(short value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setInteger(int value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setLong(long value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setFloat(float value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setDouble(double value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }
        
        setObject(DUMMY_OBJECT);
    }

    //----- setBoolean, setObject and setObject code

    public void setBoolean(boolean value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }

    //----- setXXXStream code

    public void setAsciiStream(InputStream in, int length) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setUnicodeStream(InputStream in, int length) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
    public void setBinaryStream(InputStream in, int length) throws SQLException {
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == READER_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setBytes(byte[] value) throws SQLException {
        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }

    //----- setDate, setTime and setTimestamp code

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setDate(Date value) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setTime(Time value) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setObject(DUMMY_OBJECT);
    }

    @Override
    public void setString(String value) throws SQLException {
        setObject(DUMMY_OBJECT);
    }
}
