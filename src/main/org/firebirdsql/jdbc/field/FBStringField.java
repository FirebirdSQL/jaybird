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
import org.firebirdsql.jdbc.FBResultSet;

/**
 * Describe class <code>FBStringField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 * 
 * @todo implement data handling code
 *
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 *
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * 
 * @todo check if the setBinaryStream(null) is allowed by specs.
 */
public class FBStringField extends FBField {
    private static final String SHORT_TRUE = "Y";
    private static final String SHORT_FALSE = "N";
    private static final String LONG_TRUE = "true";
    private static final String LONG_FALSE = "false";
    private static final String SHORT_TRUE_2 = "T";
    private static final String SHORT_TRUE_3 = "1";
    

    FBStringField(XSQLVAR field, FBResultSet rs, int numCol, int requiredType) 
        throws SQLException 
    {
        super(field, rs, numCol, requiredType);
    }
	 
    //----- Math code

    public byte getByte() throws SQLException {
        if (rs.row[numCol]==null) return BYTE_NULL_VALUE;

        try {
            return Byte.parseByte(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                BYTE_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    public short getShort() throws SQLException {
        if (rs.row[numCol]==null) return SHORT_NULL_VALUE;

        try {
            return Short.parseShort(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                SHORT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    public int getInt() throws SQLException {
        if (rs.row[numCol]==null) return INT_NULL_VALUE;

        try {
            return Integer.parseInt(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                INT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    public long getLong() throws SQLException {
        if (rs.row[numCol]==null) return LONG_NULL_VALUE;

        try {
            return Long.parseLong(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                LONG_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    public BigDecimal getBigDecimal() throws SQLException {
        if (rs.row[numCol]==null) return BIGDECIMAL_NULL_VALUE;

        /**@todo check what exceptions can be thrown here */
        return new BigDecimal(getString().trim());
    }
    public float getFloat() throws SQLException {
        if (rs.row[numCol]==null) return FLOAT_NULL_VALUE;

        try {
            return Float.parseFloat(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                FLOAT_CONVERSION_ERROR+" "+getString().trim()).fillInStackTrace();
        }
    }
    public double getDouble() throws SQLException {
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

    public boolean getBoolean() throws SQLException {
        if (rs.row[numCol]==null) return BOOLEAN_NULL_VALUE;

        return getString().trim().equalsIgnoreCase(LONG_TRUE) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE_2) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE_3);
    }
    public String getString() throws SQLException {
        if (rs.row[numCol]==null) return STRING_NULL_VALUE;

        return field.decodeString(rs.row[numCol], javaEncoding, mappingPath);
    }
    
    /*
    public Object getObject() throws SQLException {
        if (rs.row[numCol]==null) return OBJECT_NULL_VALUE;

        return getString();
    }
    */

    //----- getXXXStream code

    public InputStream getBinaryStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return new ByteArrayInputStream(rs.row[numCol]);
    }
    public InputStream getUnicodeStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    public InputStream getAsciiStream() throws SQLException {
        if (rs.row[numCol]==null) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    public byte[] getBytes() throws SQLException {
        if (rs.row[numCol]==null) return BYTES_NULL_VALUE;

        return rs.row[numCol];
    }

    //----- getDate, getTime and getTimestamp code

    public Date getDate(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return field.decodeDate(getDate(),cal);
    }
    public Date getDate() throws SQLException {
        if (rs.row[numCol]==null) return DATE_NULL_VALUE;

        return Date.valueOf(getString().trim());
    }
    public Time getTime(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return field.decodeTime(getTime(),cal);
    }
    public Time getTime() throws SQLException {
        if (rs.row[numCol]==null) return TIME_NULL_VALUE;

        return Time.valueOf(getString().trim());
    }
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;
		  
        return field.decodeTimestamp(getTimestamp(),cal);
    }
    public Timestamp getTimestamp() throws SQLException {
        if (rs.row[numCol]==null) return TIMESTAMP_NULL_VALUE;

        return Timestamp.valueOf(getString().trim());
    }

    //--- setXXX methods
	 
    //----- Math code

    public void setByte(byte value) throws SQLException {
        setString(Byte.toString(value));
    }
    public void setShort(short value) throws SQLException {
        setString(Short.toString(value));
    }
    public void setInteger(int value) throws SQLException {
        setString(Integer.toString(value));
    }
    public void setLong(long value) throws SQLException {
        setString(Long.toString(value));
    }
    public void setFloat(float value) throws SQLException {
        setString(Float.toString(value));
    }
    public void setDouble(double value) throws SQLException {
        setString(Double.toString(value));
    }
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == BIGDECIMAL_NULL_VALUE) {
            setNull();
            return;
        }
        
        setString(value.toString());
    }

    //----- setBoolean, setString and setObject code

    public void setBoolean(boolean value) throws SQLException {
        if (field.sqllen == 1)
            setString(value ? SHORT_TRUE : SHORT_FALSE);
        else
        if (field.sqllen > 4)
            setString(value ? LONG_TRUE : LONG_FALSE);
    }
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }
        field.sqldata = field.encodeString(value,javaEncoding, mappingPath);

//        if (field.sqldata.length > field.sqllen)
//            throw new DataTruncation(-1, true, false, field.sqldata.length, field.sqllen);
    }

    //----- setXXXStream code

    public void setAsciiStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }
    public void setUnicodeStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }
    public void setBinaryStream(InputStream in, int length) throws SQLException {
        if (in == STREAM_NULL_VALUE) {
            setNull();
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
            
            setBytes(out.toByteArray());
        }
        catch (IOException ioex) {
            throw (SQLException) createException(
                BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    public void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == READER_NULL_VALUE) {
            setNull();
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
    public void setBytes(byte[] value) throws SQLException {
        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }

        field.sqldata = value;

        if (field.sqldata.length > field.sqllen)
            throw new DataTruncation(-1, true, false, field.sqldata.length, field.sqllen);
    }

    //----- setDate, setTime and setTimestamp code

    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setDate(field.encodeDate(value,cal));
    }
    public void setDate(Date value) throws SQLException {
        if (value == DATE_NULL_VALUE) {
            setNull();
            return;
        }

        setString(value.toString());
    }
    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setTime(field.encodeTime(value,cal));
    }
    public void setTime(Time value) throws SQLException {
        if (value == TIME_NULL_VALUE) {
            setNull();
            return;
        }

        setString(value.toString());
    }
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setTimestamp(field.encodeTimestamp(value,cal));
    }
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == TIMESTAMP_NULL_VALUE) {
            setNull();
            return;
        }

        setString(value.toString());
    }

}
