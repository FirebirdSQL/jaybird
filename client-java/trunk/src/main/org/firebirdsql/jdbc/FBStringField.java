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

import java.math.BigDecimal;
import java.util.Calendar;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.sql.DataTruncation;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.firebirdsql.gds.XSQLVAR;

/**
 * Describe class <code>FBStringField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 * @todo implement data handling code
 *
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 *
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * @todo check if the setBinaryStream(null) is allowed by specs.
 * @todo implement data hadnling code
 *
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 *
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * @todo check if the setBinaryStream(null) is allowed by specs.
 * @todo implement data hadnling code
 *
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 *
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * @todo check if the setBinaryStream(null) is allowed by specs.
 * @todo implement data hadnling code
 *
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 *
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * @todo check if the setBinaryStream(null) is allowed by specs.
 */
public class FBStringField extends FBField {
    private static final String SHORT_TRUE = "Y";
    private static final String SHORT_FALSE = "N";
    private static final String LONG_TRUE = "TRUE";
    private static final String LONG_FALSE = "FALSE";
    private static final String SHORT_TRUE_2 = "T";
//    private static final String SHORT_FALSE_2 = "F";
    

    FBStringField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }
	 
    //----- Math code

    byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        try {
            return Byte.parseByte(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                BYTE_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        try {
            return Short.parseShort(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                SHORT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        try {
            return Integer.parseInt(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                INT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        try {
            return Long.parseLong(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                LONG_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        /**@todo check what exceptions can be thrown here */
        return new BigDecimal(getString().trim());
    }
    float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        try {
            return Float.parseFloat(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                FLOAT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    double getDouble() throws SQLException {
        if (rs.row[numCol]==null) return DOUBLE_NULL_VALUE;

        try {
            return Double.parseDouble(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                DOUBLE_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }

    //----- getBoolean, getString and getObject code

    boolean getBoolean() throws SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return getString().trim().equalsIgnoreCase(LONG_TRUE) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE_2);
    }
    String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return field.decodeString(rs.row[numCol], javaEncoding);
    }
    Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return getString();
    }

    //----- getXXXStream code

    InputStream getBinaryStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return new ByteArrayInputStream(rs.row[numCol]);
    }
    InputStream getUnicodeStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    InputStream getAsciiStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    byte[] getBytes() throws SQLException {
        if (rs.row[numCol]==null) return BYTES_NULL_VALUE;

        return rs.row[numCol];
    }

    //----- getDate, getTime and getTimestamp code

    Date getDate(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return field.decodeDate(getDate(),cal);
    }
    Date getDate() throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return Date.valueOf(getString().trim());
    }
    Time getTime(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return field.decodeTime(getTime(),cal);
    }
    Time getTime() throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return Time.valueOf(getString().trim());
    }
    Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;
		  
        return field.decodeTimestamp(getTimestamp(),cal);
    }
    Timestamp getTimestamp() throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;

        return Timestamp.valueOf(getString().trim());
    }

    //--- setXXX methods
	 
    //----- Math code

    void setByte(byte value) throws SQLException {
        setString(Byte.toString(value));
    }
    void setShort(short value) throws SQLException {
        setString(Short.toString(value));
    }
    void setInteger(int value) throws SQLException {
        setString(Integer.toString(value));
    }
    void setLong(long value) throws SQLException {
        setString(Long.toString(value));
    }
    void setFloat(float value) throws SQLException {
        setString(Float.toString(value));
    }
    void setDouble(double value) throws SQLException {
        setString(Double.toString(value));
    }
    void setBigDecimal(BigDecimal value) throws SQLException {
        setString(value.toString());
    }

    //----- setBoolean, setString and setObject code

    void setBoolean(boolean value) throws SQLException {
        if (field.sqllen == 1)
            setString(value ? SHORT_TRUE : SHORT_FALSE);
        else
        if (field.sqllen > 4)
            setString(value ? LONG_TRUE : LONG_FALSE);
    }
    void setString(String value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }
        field.sqldata = field.encodeString(value,javaEncoding);

        if (field.sqldata.length > field.sqllen)
            throw new DataTruncation(-1, true, false, field.sqldata.length, field.sqllen);
    }

    //----- setXXXStream code

    void setAsciiStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            field.sqldata = null;
            return;
        }

        setBinaryStream(in, length);
    }
    void setUnicodeStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            field.sqldata = null;
            return;
        }

        setBinaryStream(in, length);
    }
    void setBinaryStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            field.sqldata = null;
            return;
        }

        if (length > field.sqllen)
            throw new DataTruncation(-1, true, false, length, field.sqllen);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            
            int counter = 0;
            int toRead = length;
            
            while ((counter = in.read(buff, 0, toRead > buff.length ? buff.length : toRead)) != -1) {
                out.write(buff, 0, counter);
                toRead -= counter;
            }
            
            /*
            field.sqldata = new byte[length];
            if (javaEncoding==null){
                System.arraycopy(out.toByteArray(), 0, field.sqldata, 0, length);
            }
            else {
                try {
                    field.sqldata = (new String(out.toByteArray(), 0, length, javaEncoding)).getBytes();
                } catch(UnsupportedEncodingException ex) {
                    System.arraycopy(out.toByteArray(), 0, field.sqldata, 0, length);
                }
            }
            */
            setBytes(out.toByteArray());
        }
        catch (IOException ioex) {
            throw (SQLException) createException(
                BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == null) {
            field.sqldata = null;
            return;
        }

        try {
            StringWriter out = new StringWriter();
            char[] buff = new char[4096];
            int counter = 0;
            while ((counter = in.read(buff)) != -1)
                out.write(buff, 0, counter);
				String outString = out.toString();
            setString(outString.substring(0, length));
        }
        catch (IOException ioex) {
            throw (SQLException) createException(
                CHARACTER_STREAM_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    void setBytes(byte[] value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        // 2.07.2003. Commented out by R.Rokytskyy due to inconsistency with
        // getBytes() method. Also it is not clear if this encoding
        // is needed here at all. Should be removed in one month if not fixed
        // earlier.
        //
        //field.sqldata = field.encodeString(value,javaEncoding);

        field.sqldata = value;

        if (field.sqldata.length > field.sqllen)
            throw new DataTruncation(-1, true, false, field.sqldata.length, field.sqllen);
    }

    //----- setDate, setTime and setTimestamp code

    void setDate(Date value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setDate(field.encodeDate(value,cal));
    }
    void setDate(Date value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setString(value.toString());
    }
    void setTime(Time value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTime(field.encodeTime(value,cal));
    }
    void setTime(Time value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setString(value.toString());
    }
    void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setTimestamp(field.encodeTimestamp(value,cal));
    }
    void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            field.sqldata = null;
            return;
        }

        setString(value.toString());
    }

}
