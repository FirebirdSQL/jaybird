/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.GDS;

import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Blob;

import java.math.BigDecimal;
import java.util.Calendar;

import java.io.InputStream;
import java.io.Reader;

/**
 * Describe class <code>FBField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
abstract class FBField {
    static String BYTE_CONVERSION_ERROR =
        "Error converting to byte.";
    static String SHORT_CONVERSION_ERROR =
        "Error converting to short.";
    static String INT_CONVERSION_ERROR =
        "Error converting to int.";
    static String LONG_CONVERSION_ERROR =
        "Error converting to long.";

    static String FLOAT_CONVERSION_ERROR =
        "Error converting to float.";
    static String DOUBLE_CONVERSION_ERROR =
        "Error converting to double.";
    static String BIGDECIMAL_CONVERSION_ERROR =
        "Error converting to big decimal.";

    static String BOOLEAN_CONVERSION_ERROR =
        "Error converting to boolean.";

    static String STRING_CONVERSION_ERROR =
        "Error converting to string.";

    static String OBJECT_CONVERSION_ERROR =
        "Error converting to object.";

    static String DATE_CONVERSION_ERROR =
        "Error converting to date.";
    static String TIME_CONVERSION_ERROR =
        "Error converting to time.";
    static String TIMESTAMP_CONVERSION_ERROR =
        "Error converting to timestamp.";

    static String ASCII_STREAM_CONVERSION_ERROR =
        "Error converting to ascii stream.";
    static String UNICODE_STREAM_CONVERSION_ERROR =
        "Error converting to unicode stream.";
    static String BINARY_STREAM_CONVERSION_ERROR =
        "Error converting to binary stream.";
    static String CHARACTER_STREAM_CONVERSION_ERROR =
        "Error converting to character stream.";

    static String BYTES_CONVERSION_ERROR =
        "Error converting to array of bytes.";
        
    static String BLOB_CONVERSION_ERROR = 
        "Error converting to Firebird BLOB object";

    static String SQL_TYPE_NOT_SUPPORTED =
        "SQL type for this field is not yet supported.";


    static final byte BYTE_NULL_VALUE = 0;
    static final short SHORT_NULL_VALUE = 0;
    static final int INT_NULL_VALUE = 0;
    static final long LONG_NULL_VALUE = 0;
    static final float FLOAT_NULL_VALUE = 0.0f;
    static final double DOUBLE_NULL_VALUE = 0.0;
    static final BigDecimal BIGDECIMAL_NULL_VALUE = null;

    static final String STRING_NULL_VALUE = null;
    static final Object OBJECT_NULL_VALUE = null;

    static final boolean BOOLEAN_NULL_VALUE = false;

    static final Date DATE_NULL_VALUE = null;
    static final Time TIME_NULL_VALUE = null;
    static final Timestamp TIMESTAMP_NULL_VALUE = null;

    static final InputStream STREAM_NULL_VALUE = null;
    static final byte[] BYTES_NULL_VALUE = null;
    static final FBBlob BLOB_NULL_VALUE = null;
    
    static final byte MAX_BYTE_VALUE = Byte.MAX_VALUE;
    static final byte MIN_BYTE_VALUE = (byte)(-1 * MAX_BYTE_VALUE - 1);
    
    static final short MAX_SHORT_VALUE = Short.MAX_VALUE;
    static final short MIN_SHORT_VALUE = (short)(-1 * MAX_SHORT_VALUE - 1);
    
    static final int MAX_INT_VALUE = Integer.MAX_VALUE;
    static final int MIN_INT_VALUE = -1 * MAX_INT_VALUE - 1;
    
    static final long MAX_LONG_VALUE = Long.MAX_VALUE;
    static final long MIN_LONG_VALUE = -1 * MAX_LONG_VALUE - 1;
    
    static final float MAX_FLOAT_VALUE = Float.MAX_VALUE;
    static final float MIN_FLOAT_VALUE = -1 * MAX_FLOAT_VALUE;
    
    static final double MAX_DOUBLE_VALUE = Double.MAX_VALUE;
    static final double MIN_DOUBLE_VALUE = -1 * MAX_DOUBLE_VALUE;
    
    XSQLVAR field;
    FBResultSet rs;
    int numCol;
    FBConnection c = null;
    String IscEncoding = null;
    String javaEncoding	= null;

    FBField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        if (field == null) throw new SQLException(
            "Cannot create FBField instance for null as XSQLVAR.");
        this.field = field;
        this.rs = rs;
        this.numCol = numCol;
    }

    /**
     * Constructs an exception with appropriate message.
     * @todo add XSQLVAR type into the message
     */
    static Throwable createException(String message) {
        return new TypeConvertionException(message);
    }

    /**
     * @return <code>true</code> if the corresponding <code>field</code>
     * is <code>null</code>, otherwise <code>false</code>.
     */
    boolean isNull() {
        return (rs.row[numCol] == null);
    }

    void setNull() {
        field.sqldata = null;
    }

    void setConnection(FBConnection c) {
        this.c = c;
        if (c!=null)
            IscEncoding = c.getIscEncoding();
        if (IscEncoding!= null && IscEncoding.equalsIgnoreCase("NONE"))
            IscEncoding = null;
        // Java encoding		  
        if (IscEncoding!= null)
            javaEncoding = FBConnection.getJavaEncoding(IscEncoding);
        else
            javaEncoding = null;			  
        // this method only do something for FBStringField and FBBlobField
    }
    /**
     * @return <code>true</code> if the field is of type <code>type</code>.
     * @todo write correct GDS.SQL_QUAD support
     */
    final static boolean isType(XSQLVAR field, int type) {
        // turn off null flag, in this case we're not interested in it.
        int tempType = field.sqltype & ~1;
        switch(tempType) {
            case GDS.SQL_ARRAY :
                return (type == Types.ARRAY);

            case GDS.SQL_BLOB :
                if (field.sqlsubtype < 0)
                    return (type == Types.BLOB);
                if (field.sqlsubtype == 1)
                    return (type == Types.LONGVARCHAR);
                else
                    return (type == Types.LONGVARBINARY) ||
                           (type == Types.VARBINARY) ||
                           (type == Types.BINARY);

            case GDS.SQL_D_FLOAT :
                return false; // not supported right now

            case GDS.SQL_DOUBLE :
                return (type == Types.DOUBLE);

            case GDS.SQL_FLOAT :
                return (type == Types.FLOAT);

            case GDS.SQL_INT64 :
                return (type == Types.BIGINT);

            case GDS.SQL_LONG :
                return  (type == Types.INTEGER);

            case GDS.SQL_QUAD:
                return false; //not supported right now

            case GDS.SQL_SHORT:
                return (type == Types.SMALLINT);

            case GDS.SQL_TEXT:
                return (type == Types.CHAR);

            case GDS.SQL_TIMESTAMP:
                return (type == Types.TIMESTAMP);

            case GDS.SQL_TYPE_DATE:
                return (type == Types.DATE);

            case GDS.SQL_TYPE_TIME:
                return (type == Types.TIME);

            case GDS.SQL_VARYING:
                return (type == Types.VARCHAR);

            default:
                return false;
        }
    }

    /**
     * This method implements the type compatibility matrix from
     * "JDBC(tm): A Java SQL API, version 1.20" whitepaper, page 21.
     */
    final static boolean isCompatible(XSQLVAR field, int type) {
        // turn off null flag, in this case we're not interested in it.
        int tempType = field.sqltype & ~1;
        switch(tempType) {
            // this type does not belong to JDBC v.1.20, but as long as
            // Firebird supports arrays, lets use them.
            case GDS.SQL_ARRAY :
                return (type == Types.ARRAY);

            // this type does not belong to JDBC v.1.20, but as long as
            // Firebird supports arrays, lets use them.
            case GDS.SQL_BLOB :
                return  (type == Types.BLOB) ||
                        (type == Types.BINARY) ||
                        (type == Types.VARBINARY) ||
                        (type == Types.LONGVARBINARY) ||
                        (type == Types.LONGVARCHAR)
                        ;

            // unfortunatelly we do not know the SQL correspondence to these type
            case GDS.SQL_QUAD:
            case GDS.SQL_D_FLOAT :
                return false;

            // currently we do not provide compatibilty with CHAR and VARCHAR
            case GDS.SQL_DOUBLE :
            case GDS.SQL_FLOAT :
            case GDS.SQL_INT64 :
            case GDS.SQL_LONG :
            case GDS.SQL_SHORT:
                return  (type == Types.DOUBLE) ||
                        (type == Types.FLOAT) ||
                        (type == Types.REAL) ||
                        (type == Types.BIGINT) ||
                        (type == Types.INTEGER) ||
                        (type == Types.SMALLINT) ||
                        (type == Types.TINYINT) ||
                        (type == Types.NUMERIC) ||
                        (type == Types.DECIMAL) ||
                        (type == Types.BIT)
                        ;

            case GDS.SQL_TEXT:
            case GDS.SQL_VARYING:
                return  (type == Types.CHAR) ||
                        (type == Types.VARCHAR) ||
                        (type == Types.LONGVARCHAR)
                        ;

            case GDS.SQL_TIMESTAMP:
                return  (type == Types.TIMESTAMP) ||
                        (type == Types.TIME) ||
                        (type == Types.DATE);

            case GDS.SQL_TYPE_DATE:
                return  (type == Types.DATE) ||
                        (type == Types.TIMESTAMP);

            case GDS.SQL_TYPE_TIME:
                return  (type == Types.TIME) ||
                        (type == Types.TIMESTAMP);

            default:
                return false;
        }
    }

    /**
     * This is a factory method that creates appropriate instance of the
     * <code>FBField</code> class according to the SQL datatype. This instance
     * knows how to perform all necessary type conversions.
     */
    final static FBField createField(XSQLVAR field, FBResultSet rs, int numCol, boolean cached) 
    throws SQLException {
        if (isType(field, Types.SMALLINT))
            if (field.sqlscale == 0)
                return new FBShortField(field, rs, numCol);
            else
                return new FBBigDecimalField(field, rs, numCol,1);
        else
        if (isType(field, Types.INTEGER))
            if (field.sqlscale == 0)
                return new FBIntegerField(field, rs, numCol);
            else
                return new FBBigDecimalField(field, rs, numCol,2);
        else
        if (isType(field, Types.BIGINT))
            if (field.sqlscale == 0)
                return new FBLongField(field, rs, numCol);
            else
                return new FBBigDecimalField(field, rs, numCol,3);
        else
        if (isType(field, Types.FLOAT))
            return new FBFloatField(field, rs, numCol);
        else
        if (isType(field, Types.DOUBLE))
            return new FBDoubleField(field, rs, numCol);
        else
        if (isType(field, Types.CHAR))
            return new FBStringField(field, rs, numCol);
        else
        if (isType(field, Types.VARCHAR))
            return new FBStringField(field, rs, numCol);
        else
        if (isType(field, Types.DATE))
            return new FBDateField(field, rs, numCol);
        else
        if (isType(field, Types.TIME))
            return new FBTimeField(field, rs, numCol);
        else
        if (isType(field, Types.TIMESTAMP))
            return new FBTimestampField(field, rs, numCol);
        else
        if (isType(field, Types.BLOB) || 
            isType(field, Types.LONGVARBINARY) ||
            isType(field, Types.LONGVARCHAR))
            if (cached)
                return new FBCachedBlobField(field, rs, numCol);
				else		  
                return new FBBlobField(field, rs, numCol);
        else
            throw (SQLException)createException(
                SQL_TYPE_NOT_SUPPORTED);
    }
    
    /**
     * Convert byte array to string taking into account InterBase encoding.
     * 
     * @param bytes byte array to convert.
     * @param iscEncoding InterBase encoding to use.
     * @return converted string.
     */

    protected final String toString(byte[] bytes) {
        if (javaEncoding == null)
            return new String(bytes);
        else {
            try {
                return new String(bytes, javaEncoding);
            } catch(java.io.UnsupportedEncodingException ex) {
                return new String(bytes);
            }
        }
    }

    /**
     * Convert byte array to string taking into account InterBase encoding.
     * 
     * @param bytes byte array to convert.
     * @param offset offset in array.
     * @param count how many bytes to convert.
     * @param iscEncoding InterBase encoding to use.
     * @return converted string.
     */
    protected final String toString(byte[] bytes, int offset, int count) 
    {
        if (javaEncoding==null){
            return new String(bytes, offset, count);
        }
        else {
            try {
                return new String(bytes, offset, count, javaEncoding);
            } catch(java.io.UnsupportedEncodingException ex) {
                return new String(bytes, offset, count);
            }
        }
    }
        
    /**
     * Convert the string into byte array taking the specified encoding into
     * account.
     * 
     * @param str string to convert.
     * @param iscEncoding InterBase encoding to use.
     * @return converted byte array
     */
	 
    protected final byte[] getBytes(String str) {
        if (javaEncoding==null){
            return str.getBytes();
        }
        else {
            try {
                return str.getBytes(javaEncoding);
            } catch(java.io.UnsupportedEncodingException ex) {
                return str.getBytes();
            }
        }
    }


    /**
     * Returns the name of the column as declared in the XSQLVAR.
     */
    public String getName() { return field.sqlname; }

    /**
     * Returns the alias of the column as declared in XSQLVAR.
     */
    public String getAlias() { return field.aliasname; }

    /**
     * Returns the relation to which belongs column as declared in XSQLVAR.
     */
    public String getRelationName() { return field.relname; }

    /*
     * All these methods simply throw an exception
     * when invoked. All subclasses should implement
     * relevant mathods with conversions.
     */

    //--- getters

    byte getByte() throws SQLException {
        throw (SQLException)createException(
            BYTE_CONVERSION_ERROR).fillInStackTrace();
    }
    short getShort() throws SQLException {
        throw (SQLException)createException(
            SHORT_CONVERSION_ERROR).fillInStackTrace();
    }
    int getInt() throws SQLException {
        throw (SQLException)createException(
            INT_CONVERSION_ERROR).fillInStackTrace();
    }
    long getLong() throws SQLException {
        throw (SQLException)createException(
            LONG_CONVERSION_ERROR).fillInStackTrace();
    }
    float getFloat() throws SQLException {
        throw (SQLException)createException(
            FLOAT_CONVERSION_ERROR).fillInStackTrace();
    }
    double getDouble() throws SQLException {
        throw (SQLException)createException(
            DOUBLE_CONVERSION_ERROR).fillInStackTrace();
    }
    java.math.BigDecimal getBigDecimal() throws SQLException {
        throw (SQLException)createException(
            BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
    }
    java.math.BigDecimal getBigDecimal(int scale) throws SQLException {
        return getBigDecimal();
    }
    boolean getBoolean() throws SQLException {
        throw (SQLException)createException(
            BOOLEAN_CONVERSION_ERROR).fillInStackTrace();
    }
    String getString() throws SQLException {
        throw (SQLException)createException(
            STRING_CONVERSION_ERROR).fillInStackTrace();
    }
    Object getObject() throws SQLException {
        throw (SQLException)createException(
            OBJECT_CONVERSION_ERROR);
    }
    public Object getObject(java.util.Map map) throws  SQLException {
              throw new SQLException("Not yet implemented");
    }
    InputStream getAsciiStream() throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    InputStream getUnicodeStream() throws SQLException {
        throw (SQLException)createException(
            UNICODE_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    InputStream getBinaryStream() throws SQLException {
        throw (SQLException)createException(
            BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    java.io.Reader getCharacterStream() throws  SQLException {
        InputStream is =  getUnicodeStream();
        if (is==null)
            return null;
        else
            return new java.io.InputStreamReader(getUnicodeStream());
    }	 
    byte[] getBytes() throws SQLException {
        throw (SQLException)createException(
            BYTES_CONVERSION_ERROR).fillInStackTrace();
    }
    Blob getBlob() throws SQLException {
        throw (SQLException)createException(
            BLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Date getDate() throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Date getDate(Calendar cal)
        throws  SQLException
    {
        java.sql.Date d = getDate();
        if (cal == null) 
        {
            return d;
        } // end of if ()
        else
        {
            cal.setTime(d);
            return new java.sql.Date(cal.getTime().getTime());    
        } // end of else
    }	 
    java.sql.Time getTime() throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Time getTime(Calendar cal)
        throws  SQLException
    {
        java.sql.Time d = getTime();
        if (cal == null) 
        {
            return d;
        } // end of if ()
        else
        {
            cal.setTime(d);
            return new java.sql.Time(cal.getTime().getTime());    
        } // end of else
    }	 
    java.sql.Timestamp getTimestamp() throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Timestamp getTimestamp(Calendar cal)
        throws  SQLException
    {
        java.sql.Timestamp x = getTimestamp();
        //return d;
        
        if (cal == null) 
        {
            return x;
        } // end of if ()
        else
        {
            long time = x.getTime() + cal.getTimeZone().getRawOffset();
            return new java.sql.Timestamp(time);    
        } // end of else
        
    }
    java.sql.Ref getRef() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }
    java.sql.Clob getClob() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }
    java.sql.Array getArray() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }
    //--- setters

    void setByte(byte value) throws SQLException {
        throw (SQLException)createException(
            BYTE_CONVERSION_ERROR).fillInStackTrace();
    }
    void setShort(short value) throws SQLException {
        throw (SQLException)createException(
            SHORT_CONVERSION_ERROR).fillInStackTrace();
    }
    void setInteger(int value) throws SQLException {
        throw (SQLException)createException(
            INT_CONVERSION_ERROR).fillInStackTrace();
    }
    void setLong(long value) throws SQLException {
        throw (SQLException)createException(
            LONG_CONVERSION_ERROR).fillInStackTrace();
    }
    void setFloat(float value) throws SQLException {
        throw (SQLException)createException(
            FLOAT_CONVERSION_ERROR).fillInStackTrace();
    }
    void setDouble(double value) throws SQLException {
        throw (SQLException)createException(
            DOUBLE_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBigDecimal(java.math.BigDecimal value) throws SQLException {
        throw (SQLException)createException(
            BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBoolean(boolean value) throws SQLException {
        throw (SQLException)createException(
            BOOLEAN_CONVERSION_ERROR).fillInStackTrace();
    }
    void setString(String value) throws SQLException {
        throw (SQLException)createException(
            STRING_CONVERSION_ERROR).fillInStackTrace();
    }
    void setObject(Object value) throws SQLException {
        /*
        throw (SQLException)createException(
            OBJECT_CONVERSION_ERROR).fillInStackTrace();
        */
        if (value == null) {
            field.sqldata = null;
            return;
        }

        if (value instanceof BigDecimal) {
            setBigDecimal((BigDecimal) value);
        } else
        if (value instanceof Blob) {
            if (value instanceof FBBlob)
                setBlob((FBBlob)value);
            else
                setBinaryStream(((Blob) value).getBinaryStream(), 
                    (int)((Blob)value).length());
        } else
        if (value instanceof Boolean) {
            setBoolean(((Boolean) value).booleanValue());
        } else
        if (value instanceof Byte) {
            setByte(((Byte) value).byteValue());
        } else
        if (value instanceof byte[]) {
            setBytes((byte[]) value);
        } else
        if (value instanceof Date) {
            setDate((Date) value);
        } else
        if (value instanceof Double) {
            setDouble(((Double) value).doubleValue());
        } else
        if (value instanceof Float) {
            setFloat(((Float) value).floatValue());
        } else
        if (value instanceof Integer) {
            setInteger(((Integer) value).intValue());
        } else
        if (value instanceof Long) {
            setLong(((Long) value).longValue());
        } else
        if (value instanceof Short) {
            setShort(((Short) value).shortValue());
        } else
        if (value instanceof String) {
            setString((String) value);
        } else
        if (value instanceof Time) {
            setTime((Time) value);
        } else
        if (value instanceof Timestamp) {
            setTimestamp((Timestamp) value);
        } else {
            throw (SQLException) createException(
                OBJECT_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    void setAsciiStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setUnicodeStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            UNICODE_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBinaryStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setCharacterStream(Reader in, int length) throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBytes(byte[] value) throws SQLException {
        throw (SQLException)createException(
            BYTES_CONVERSION_ERROR).fillInStackTrace();
    }
    void setDate(java.sql.Date value) throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    void setTime(java.sql.Time value) throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    void setTimestamp(java.sql.Timestamp value) throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBlob(FBBlob blob) throws SQLException {
        throw (SQLException)createException(
            BLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    //
    // This method is only for the tests
    //
    void copyOI(){
        rs.row[numCol] = field.sqldata;
    }
}
