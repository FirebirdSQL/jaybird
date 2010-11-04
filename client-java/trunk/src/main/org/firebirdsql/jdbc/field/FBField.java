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

package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.*;

import java.sql.Array;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Blob;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

import java.io.InputStream;
import java.io.Reader;

/**
 * Describe class <code>FBField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public abstract class FBField {
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
    
    static String CLOB_CONVERSION_ERROR =
    	"Error converting to Firebird CLOB object";

    static String SQL_TYPE_NOT_SUPPORTED =
        "SQL type for this field is not yet supported.";
    
    static String SQL_ARRAY_NOT_SUPPORTED = 
        "Types.ARRAY: " + SQL_TYPE_NOT_SUPPORTED;


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
    static final Reader READER_NULL_VALUE = null;
    static final byte[] BYTES_NULL_VALUE = null;
    static final FBBlob BLOB_NULL_VALUE = null;
    static final FBClob CLOB_NULL_VALUE = null;
    
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
    
    protected XSQLVAR field;
    private FieldDataProvider dataProvider;
    protected int numCol;
    protected GDSHelper gdsHelper = null;
    protected String iscEncoding = null;
    protected String javaEncoding	= null;
    protected String mappingPath = null;
    protected int requiredType;
    protected int scale = -1;

    FBField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        if (field == null) throw new FBSQLException(
            "Cannot create FBField instance for null as XSQLVAR.",
            FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
        
        this.field = field;
        this.dataProvider = dataProvider;
        this.requiredType = requiredType;
    }

    protected byte[] getFieldData() {
        return dataProvider.getFieldData();
    }
    
    protected void setFieldData(byte[] data) {
        dataProvider.setFieldData(data);
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
    public boolean isNull() throws SQLException {
        return dataProvider.getFieldData() == null;
    }

    public void setNull() {
        setFieldData(null);
    }

    public void setConnection(GDSHelper gdsHelper) {
        this.gdsHelper = gdsHelper;
        if (gdsHelper != null)
            iscEncoding = gdsHelper.getIscEncoding();
        
        if (iscEncoding != null && (iscEncoding.equalsIgnoreCase("NONE") 
		  || iscEncoding.equalsIgnoreCase("BINARY")))
            iscEncoding = null;
        
        if (gdsHelper != null) {
            javaEncoding = gdsHelper.getJavaEncoding();
            mappingPath = gdsHelper.getMappingPath();
        }
    }
    
    /**
     * Set the required type for {@link #getObject()} conversion.
     * 
     * @param requiredType required type, one of the {@link java.sql.Types}
     * constants.
     */
    public void setRequiredType(int requiredType) {
        this.requiredType =requiredType;
    }
    
    /**
     * @return <code>true</code> if the field is of type <code>type</code>.
     * TODO write correct ISCConstants.SQL_QUAD support
     */
    public final static boolean isType(XSQLVAR field, int type) {
        // turn off null flag, in this case we're not interested in it.
        int tempType = field.sqltype & ~1;
        switch(tempType) {
            case ISCConstants.SQL_ARRAY :
                return (type == Types.ARRAY);

            case ISCConstants.SQL_BLOB :
                if (field.sqlsubtype < 0)
                    return (type == Types.BLOB);
                if (field.sqlsubtype == 1)
                    return (type == Types.LONGVARCHAR);
                else
                    return (type == Types.LONGVARBINARY) ||
                           (type == Types.VARBINARY) ||
                           (type == Types.BINARY);

            case ISCConstants.SQL_D_FLOAT :
                return false; // not supported right now

            case ISCConstants.SQL_DOUBLE :
                return (type == Types.DOUBLE);

            case ISCConstants.SQL_FLOAT :
                return (type == Types.FLOAT);

            case ISCConstants.SQL_INT64 :
                return (type == Types.BIGINT);

            case ISCConstants.SQL_LONG :
                return  (type == Types.INTEGER);

            case ISCConstants.SQL_QUAD:
                return false; //not supported right now

            case ISCConstants.SQL_SHORT:
                return (type == Types.SMALLINT);

            case ISCConstants.SQL_TEXT:
                return (type == Types.CHAR);

            case ISCConstants.SQL_TIMESTAMP:
                return (type == Types.TIMESTAMP);

            case ISCConstants.SQL_TYPE_DATE:
                return (type == Types.DATE);

            case ISCConstants.SQL_TYPE_TIME:
                return (type == Types.TIME);

            case ISCConstants.SQL_VARYING:
                return (type == Types.VARCHAR);
                
            case ISCConstants.SQL_NULL:
                return false;

            default:
                return false;
        }
    }

    /**
     * This method implements the type compatibility matrix from
     * "JDBC(tm): A Java SQL API, version 1.20" whitepaper, page 21.
     */
    public final static boolean isCompatible(XSQLVAR field, int type) {
        // turn off null flag, in this case we're not interested in it.
        int tempType = field.sqltype & ~1;
        switch(tempType) {
            // this type does not belong to JDBC v.1.20, but as long as
            // Firebird supports arrays, lets use them.
            case ISCConstants.SQL_ARRAY :
                return (type == Types.ARRAY);

            // this type does not belong to JDBC v.1.20, but as long as
            // Firebird supports arrays, lets use them.
            case ISCConstants.SQL_BLOB :
                return  (type == Types.BLOB) ||
                        (type == Types.BINARY) ||
                        (type == Types.VARBINARY) ||
                        (type == Types.LONGVARBINARY) ||
                        (type == Types.LONGVARCHAR)
                        ;

            // unfortunatelly we do not know the SQL correspondence to these type
            case ISCConstants.SQL_QUAD:
            case ISCConstants.SQL_D_FLOAT :
                return false;

            // currently we do not provide compatibilty with CHAR and VARCHAR
            case ISCConstants.SQL_DOUBLE :
            case ISCConstants.SQL_FLOAT :
            case ISCConstants.SQL_INT64 :
            case ISCConstants.SQL_LONG :
            case ISCConstants.SQL_SHORT:
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

            case ISCConstants.SQL_TEXT:
            case ISCConstants.SQL_VARYING:
                return  (type == Types.CHAR) ||
                        (type == Types.VARCHAR) ||
                        (type == Types.LONGVARCHAR)
                        ;

            case ISCConstants.SQL_TIMESTAMP:
                return  (type == Types.TIMESTAMP) ||
                        (type == Types.TIME) ||
                        (type == Types.DATE);

            case ISCConstants.SQL_TYPE_DATE:
                return  (type == Types.DATE) ||
                        (type == Types.TIMESTAMP);

            case ISCConstants.SQL_TYPE_TIME:
                return  (type == Types.TIME) ||
                        (type == Types.TIMESTAMP);
                
            case ISCConstants.SQL_NULL:
                return true;

            default:
                return false;
        }
    }
    
    public final static boolean isNullType(XSQLVAR field) {
        int tempType = field.sqltype & ~1;

        return tempType == ISCConstants.SQL_NULL
                || field.sqltype == ISCConstants.SQL_NULL;
    }
    
    /**
     * This is a factory method that creates appropriate instance of the
     * <code>FBField</code> class according to the SQL datatype. This instance
     * knows how to perform all necessary type conversions.
     */
    public final static FBField createField(XSQLVAR field, FieldDataProvider dataProvider, GDSHelper gdsHelper, boolean cached)
    throws SQLException {
        FBField result = createField(field, dataProvider, cached);
        result.setConnection(gdsHelper);
        return result;
    }
        
    private static FBField createField(XSQLVAR field, FieldDataProvider dataProvider, boolean cached)
        throws SQLException {
        
        if (isType(field, Types.SMALLINT))
            if (field.sqlscale == 0)
                return new FBShortField(field, dataProvider, Types.SMALLINT);
            else
                return new FBBigDecimalField(field, dataProvider, 1, Types.NUMERIC);
        else
        if (isType(field, Types.INTEGER))
            if (field.sqlscale == 0)
                return new FBIntegerField(field, dataProvider, Types.INTEGER);
            else
                return new FBBigDecimalField(field, dataProvider,2, Types.NUMERIC);
        else
        if (isType(field, Types.BIGINT))
            if (field.sqlscale == 0)
                return new FBLongField(field, dataProvider, Types.BIGINT);
            else
                return new FBBigDecimalField(field, dataProvider,3, Types.NUMERIC);
        else
        if (isType(field, Types.FLOAT))
            return new FBFloatField(field, dataProvider, Types.FLOAT);
        else
        if (isType(field, Types.DOUBLE))
            return new FBDoubleField(field, dataProvider, Types.DOUBLE);
        else
        if (isType(field, Types.CHAR))
            /*
            
            // Commented by R.Rokytskyy. Until the bug is fixed in the server
            // we use "workaround" implementation of the string field. Should
            // be replaced with original one as soon as bug is fixed in the 
            // engine.
            
            return new FBStringField(field, dataProvider, Types.CHAR);
            */
            return new FBWorkaroundStringField(field, dataProvider, Types.CHAR);
        else
        if (isType(field, Types.VARCHAR))
            /*
            
            // Commented by R.Rokytskyy. Until the bug is fixed in the server
            // we use "workaround" implementation of the string field. Should
            // be replaced with original one as soon as bug is fixed in the 
            // engine.
            
            return new FBStringField(field, dataProvider, Types.VARCHAR);
            */
            return new FBWorkaroundStringField(field, dataProvider, Types.VARCHAR);
        else
        if (isType(field, Types.DATE))
            return new FBDateField(field, dataProvider, Types.DATE);
        else
        if (isType(field, Types.TIME))
            return new FBTimeField(field, dataProvider, Types.TIME);
        else
        if (isType(field, Types.TIMESTAMP))
            return new FBTimestampField(field, dataProvider, Types.TIMESTAMP);
        else
        if (isType(field, Types.BLOB)) {
                if (cached)
                    return new FBCachedBlobField(field, dataProvider, Types.BLOB);
                else          
                    return new FBBlobField(field, dataProvider, Types.BLOB);
        } else
        if (isType(field, Types.LONGVARBINARY)) {
            if (cached)
                return new FBCachedBlobField(field, dataProvider, Types.LONGVARBINARY);
            else		  
                return new FBBlobField(field, dataProvider, Types.LONGVARBINARY);
        } else
        if (isType(field, Types.LONGVARCHAR))
            if (cached)
                return new FBCachedLongVarCharField(field, dataProvider, Types.LONGVARCHAR);
            else
                return new FBLongVarCharField(field, dataProvider, Types.LONGVARCHAR);
        else
        if (isType(field, Types.ARRAY))
            throw (SQLException)createException(SQL_ARRAY_NOT_SUPPORTED);
        else
        if (isNullType(field))
            return new FBNullField(field, dataProvider, Types.VARCHAR);
        else
            throw (SQLException)createException(SQL_TYPE_NOT_SUPPORTED);
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
    
    /**
     * Close this field. This method tells field implementation to release all
     * resources allocated when field methods were called.
     * 
     * @throws SQLException if field cannot be closed.
     */
    public void close() throws SQLException {
        // default behaviour is to do nothing.
    }

    /*
     * All these methods simply throw an exception
     * when invoked. All subclasses should implement
     * relevant mathods with conversions.
     */

    //--- getters

    public byte getByte() throws SQLException {
        throw (SQLException)createException(
            BYTE_CONVERSION_ERROR).fillInStackTrace();
    }
    public short getShort() throws SQLException {
        throw (SQLException)createException(
            SHORT_CONVERSION_ERROR).fillInStackTrace();
    }
    public int getInt() throws SQLException {
        throw (SQLException)createException(
            INT_CONVERSION_ERROR).fillInStackTrace();
    }
    public long getLong() throws SQLException {
        throw (SQLException)createException(
            LONG_CONVERSION_ERROR).fillInStackTrace();
    }
    public float getFloat() throws SQLException {
        throw (SQLException)createException(
            FLOAT_CONVERSION_ERROR).fillInStackTrace();
    }
    public double getDouble() throws SQLException {
        throw (SQLException)createException(
            DOUBLE_CONVERSION_ERROR).fillInStackTrace();
    }
    public BigDecimal getBigDecimal() throws SQLException {
        throw (SQLException)createException(
            BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
    }
    public BigDecimal getBigDecimal(int scale) throws SQLException {
        return getBigDecimal();
    }
    public boolean getBoolean() throws SQLException {
        throw (SQLException)createException(
            BOOLEAN_CONVERSION_ERROR).fillInStackTrace();
    }
    public String getString() throws SQLException {
        throw (SQLException)createException(
            STRING_CONVERSION_ERROR).fillInStackTrace();
    }
    
    private boolean isOctetsAsBytes() {
        
        if (gdsHelper == null)
            return false;
        
        return gdsHelper.getDatabaseParameterBuffer().hasArgument(
            DatabaseParameterBufferExtension.OCTETS_AS_BYTES);
    }
    
    public Object getObject() throws SQLException {
        
        if (isNull())
            return null;
        
        switch (requiredType) {
            case Types.CHAR :
            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                // check whether OCTETS should be returned as byte[]
                if (isOctetsAsBytes() && field.sqlsubtype == 1)
                    return getBytes();
                else
                    return getString();
                
            case Types.NUMERIC :
            case Types.DECIMAL :
                if (scale == -1)
                    return getBigDecimal();
                else
                    return getBigDecimal(scale);
                
            case Types.BIT :
            case 16 : // 16 is a value of Types.BOOLEAN in JDBC 3.0
                    return new Boolean(getBoolean());
                
            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                return new Integer(getInt());
                
            case Types.BIGINT :
                return new Long(getLong());
                
            case Types.REAL :
                return new Float(getFloat());
                
            case Types.FLOAT :
            case Types.DOUBLE :
                return new Double(getDouble());
                
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                return getBytes();
                
            case Types.DATE :
                return getDate();
                
            case Types.TIME :
                return getTime();
                
            case Types.TIMESTAMP :
                return getTimestamp();
                
            case Types.CLOB :
                return getClob();
                
            case Types.BLOB :
                return getBlob();
                
            case Types.ARRAY :
                return getArray();
                
            default :
                throw (SQLException)createException(
                    OBJECT_CONVERSION_ERROR);                
        }
        
    }
    public Object getObject(Map map) throws  SQLException {
              throw new FBDriverNotCapableException();
    }
    public InputStream getAsciiStream() throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public InputStream getUnicodeStream() throws SQLException {
        throw (SQLException)createException(
            UNICODE_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public InputStream getBinaryStream() throws SQLException {
        throw (SQLException)createException(
            BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public Reader getCharacterStream() throws  SQLException {
        InputStream is =  getBinaryStream();
        if (is==null)
            return READER_NULL_VALUE;
        else
            return TranslatingReader.getInstance(is, javaEncoding, mappingPath);
    }	 
    public byte[] getBytes() throws SQLException {
        throw (SQLException)createException(
            BYTES_CONVERSION_ERROR).fillInStackTrace();
    }
    public Blob getBlob() throws SQLException {
        throw (SQLException)createException(
            BLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    public Date getDate() throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    public Date getDate(Calendar cal) throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }	 
    public Time getTime() throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    public Time getTime(Calendar cal) throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }	 
    public Timestamp getTimestamp() throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
    }
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();		 
    }
    public Ref getRef() throws  SQLException {
                throw new FBDriverNotCapableException();
    }
    public Clob getClob() throws  SQLException {
        throw (SQLException)createException(
                BLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    public Array getArray() throws  SQLException {
                throw new FBDriverNotCapableException();
    }
    //--- setters

    public void setByte(byte value) throws SQLException {
        throw (SQLException)createException(
            BYTE_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setShort(short value) throws SQLException {
        throw (SQLException)createException(
            SHORT_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setInteger(int value) throws SQLException {
        throw (SQLException)createException(
            INT_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setLong(long value) throws SQLException {
        throw (SQLException)createException(
            LONG_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setFloat(float value) throws SQLException {
        throw (SQLException)createException(
            FLOAT_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setDouble(double value) throws SQLException {
        throw (SQLException)createException(
            DOUBLE_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        throw (SQLException)createException(
            BIGDECIMAL_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setBoolean(boolean value) throws SQLException {
        throw (SQLException)createException(
            BOOLEAN_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setString(String value) throws SQLException {
        throw (SQLException)createException(
            STRING_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setObject(Object value) throws SQLException {
        /*
        throw (SQLException)createException(
            OBJECT_CONVERSION_ERROR).fillInStackTrace();
        */
        if (value == null) {
            setNull();
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
    public void setAsciiStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setUnicodeStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            UNICODE_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setBinaryStream(InputStream in, int length) throws SQLException {
        throw (SQLException)createException(
            BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setCharacterStream(Reader in, int length) throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setBytes(byte[] value) throws SQLException {
        throw (SQLException)createException(
            BYTES_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setDate(Date value, Calendar cal) throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setDate(Date value) throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setTime(Time value, Calendar cal) throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setTime(Time value) throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
    }
    public void setBlob(FBBlob blob) throws SQLException {
        throw (SQLException)createException(
            BLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    
    public void setClob(FBClob clob) throws SQLException {
    	throw (SQLException)createException(
    			CLOB_CONVERSION_ERROR).fillInStackTrace();
    }
    
    // This method is only for the tests
    //
    void copyOI(){
        dataProvider.setFieldData(dataProvider.getFieldData());
    }

    protected boolean isInvertTimeZone() {
        if (gdsHelper == null) return false;
        
        DatabaseParameterBuffer dpb = gdsHelper.getDatabaseParameterBuffer();
        return dpb.hasArgument(DatabaseParameterBufferExtension.TIMESTAMP_USES_LOCAL_TIMEZONE);
    }

	
}
