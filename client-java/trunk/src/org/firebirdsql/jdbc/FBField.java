/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): David Jencks, Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
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

import java.io.InputStream;

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

    static String BYTES_CONVERSION_ERROR =
        "Error converting to array of bytes.";

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

    XSQLVAR field;

    FBField(XSQLVAR field) throws SQLException {
        if (field == null) throw new SQLException(
            "Cannot create FBField instance for null as XSQLVAR.");
        this.field = field;
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
     * is <code>null</code> or <code>field.sqlind == -1</code>, otherwise
     * <code>false</code>.
     */
    boolean isNull() {
        return (field.sqlind == -1);
    }

    /**
     * Sets the "null" flag for the field.
     */
    void setNull(boolean value) {
        field.sqlind = value ? -1 : 0;
        if (value)
            field.sqldata = null;
    }

    /**
     * @return <code>true</code> if the field is of type <code>type</code>.
     * @todo write correct GDS.SQL_QUAD support
     */
    static boolean isType(XSQLVAR field, int type) {
        // turn off null flag, in this case we're not interested in it.
        int tempType = field.sqltype & ~1;
        switch(tempType) {
            case GDS.SQL_ARRAY :
                return (type == Types.ARRAY);

            case GDS.SQL_BLOB :
                return (type == Types.BLOB);

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
    static boolean isCompatible(XSQLVAR field, int type) {
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
                        (type == Types.LONGVARBINARY)
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
    static FBField createField(XSQLVAR field) throws SQLException {
        if (isType(field, Types.SMALLINT))
            if (field.sqlscale == 0)
                return new FBShortField(field);
            else
                return new FBBigDecimalField(field);
        else
        if (isType(field, Types.INTEGER))
            if (field.sqlscale == 0)
                return new FBIntegerField(field);
            else
                return new FBBigDecimalField(field);
        else
        if (isType(field, Types.BIGINT))
            if (field.sqlscale == 0)
                return new FBLongField(field);
            else
                return new FBBigDecimalField(field);
        else
        if (isType(field, Types.FLOAT))
            return new FBFloatField(field);
        else
        if (isType(field, Types.DOUBLE))
            return new FBDoubleField(field);
        else
        if (isType(field, Types.CHAR))
            return new FBStringField(field);
        else
        if (isType(field, Types.VARCHAR))
            return new FBStringField(field);
        else
        if (isType(field, Types.DATE))
            return new FBDateField(field);
        else
        if (isType(field, Types.TIME))
            return new FBTimeField(field);
        else
        if (isType(field, Types.TIMESTAMP))
            return new FBTimestampField(field);
        else
        if (isType(field, Types.BLOB))
            return new FBBlobField(field);
        else
            throw (SQLException)createException(
                SQL_TYPE_NOT_SUPPORTED);
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
    byte[] getBytes() throws SQLException {
        throw (SQLException)createException(
            BYTES_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Date getDate() throws SQLException {
        throw (SQLException)createException(
            DATE_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Time getTime() throws SQLException {
        throw (SQLException)createException(
            TIME_CONVERSION_ERROR).fillInStackTrace();
    }
    java.sql.Timestamp getTimestamp() throws SQLException {
        throw (SQLException)createException(
            TIMESTAMP_CONVERSION_ERROR).fillInStackTrace();
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
        throw (SQLException)createException(
            OBJECT_CONVERSION_ERROR).fillInStackTrace();
    }
    void setAsciiStream(InputStream in) throws SQLException {
        throw (SQLException)createException(
            ASCII_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setUnicodeStream(InputStream in) throws SQLException {
        throw (SQLException)createException(
            UNICODE_STREAM_CONVERSION_ERROR).fillInStackTrace();
    }
    void setBinaryStream(InputStream in) throws SQLException {
        throw (SQLException)createException(
            BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
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
}