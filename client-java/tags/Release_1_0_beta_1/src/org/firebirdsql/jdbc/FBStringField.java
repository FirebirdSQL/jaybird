/*
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 *
 *  Contributor(s): Roman Rokytskyy, David Jencks
 *
 *  Alternatively, the contents of this file may be used under the
 *  terms of the GNU Lesser General Public License Version 2.1 or later
 *  (the "LGPL"), in which case the provisions of the LGPL are applicable
 *  instead of those above.  If you wish to allow use of your
 *  version of this file only under the terms of the LGPL and not to
 *  allow others to use your version of this file under the MPL,
 *  indicate your decision by deleting the provisions above and
 *  replace them with the notice and other provisions required by
 *  the LGPL.  If you do not delete the provisions above, a recipient
 *  may use your version of this file under either the MPL or the
 *  LGPL.
 */
package org.firebirdsql.jdbc;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.sql.DataTruncation;
import java.sql.Types;

import java.text.DateFormat;
import java.text.ParseException;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.firebirdsql.gds.XSQLVAR;

/**
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
class FBStringField extends FBField {
    static final String SHORT_TRUE = "Y";
    static final String SHORT_FALSE = "N";
    static final String LONG_TRUE = "TRUE";
    static final String LONG_FALSE = "FALSE";
    
    FBConnection c;

    FBStringField(XSQLVAR field) throws SQLException {
        super(field);
    }
    
    void setConnection(FBConnection c) {
        this.c = c;
    }
    
    String getIscEncoding() {
        if (c != null)
            return c.getIscEncoding();
        else
            return null;
    }

    //----- Math code

    byte getByte() throws java.sql.SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        try {
            return Byte.parseByte(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                BYTE_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        try {
            return Short.parseShort(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                SHORT_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        try {
            return Integer.parseInt(getString().trim());
        } catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                INT_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;

        try {
            return Long.parseLong(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                LONG_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    BigDecimal getBigDecimal() throws java.sql.SQLException {
        if (isNull()) return BIGDECIMAL_NULL_VALUE;

        /**@todo check what exceptions can be thrown here */
        return new BigDecimal(getString().trim());
    }
    float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        try {
            return Float.parseFloat(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                FLOAT_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    double getDouble() throws java.sql.SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        try {
            return Double.parseDouble(getString().trim());
        }
        catch (NumberFormatException nfex) {
            throw (SQLException) createException(
                DOUBLE_CONVERSION_ERROR).fillInStackTrace();
        }
    }

    //----- getBoolean, getString and getObject code

    boolean getBoolean() throws SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;

        return getString().trim().equalsIgnoreCase(LONG_TRUE) ||
                getString().trim().equalsIgnoreCase(SHORT_TRUE);
    }
    String getString() throws java.sql.SQLException {
        if (isNull()) return STRING_NULL_VALUE;

        // this is a fix for the FBDatabaseMetaData class
        if (field.sqldata instanceof String)
            return (String)field.sqldata;

        return toString((byte[])field.sqldata, getIscEncoding());
    }
    Object getObject() throws SQLException {
        if (isNull()) return OBJECT_NULL_VALUE;

        return getString();
    }

    //----- getXXXStream code

    InputStream getBinaryStream() throws SQLException {
        if (isNull()) return STREAM_NULL_VALUE;

        return new ByteArrayInputStream((byte[]) field.sqldata);
    }
    InputStream getUnicodeStream() throws SQLException {
        if (isNull()) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    InputStream getAsciiStream() throws SQLException {
        if (isNull()) return STREAM_NULL_VALUE;

        return getBinaryStream();
    }
    byte[] getBytes() throws SQLException {
        if (isNull()) return BYTES_NULL_VALUE;

        return (byte[]) field.sqldata;
    }

    //----- getDate, getTime and getTimestamp code

    Date getDate() throws SQLException {
        if (isNull()) return DATE_NULL_VALUE;

        return Date.valueOf(getString().trim());
    }
    Time getTime() throws SQLException {
        if (isNull()) return TIME_NULL_VALUE;

        return Time.valueOf(getString().trim());
    }
    Timestamp getTimestamp() throws SQLException {
        if (isNull()) return TIMESTAMP_NULL_VALUE;

        return Timestamp.valueOf(getString().trim());
    }

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
            setNull(true);
            return;
        }

        // Do we have to do this by hands? Or is this Firebird's job?
        if (isType(field, Types.CHAR) && (value.length() < field.sqllen)) {
            StringBuffer padded = new StringBuffer(field.sqllen);
            padded.append(value);
            for (int i = 0; i < field.sqllen - value.length(); i++)
                padded.append(' ');
            value = padded.toString();
        }


        byte[] supplied = getBytes(value, getIscEncoding());
        if (supplied.length > field.sqllen)
            throw new DataTruncation(-1, true, false, supplied.length, field.sqllen);

        field.sqldata = supplied;
        setNull(false);
    }

    //----- setXXXStream code

    void setAsciiStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            setNull(true);
            return;
        }

        setBinaryStream(in, length);
    }
    void setUnicodeStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            setNull(true);
            return;
        }

        setBinaryStream(in, length);
    }
    void setBinaryStream(InputStream in, int length) throws SQLException {
        if (in == null) {
            setNull(true);
            return;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int counter = 0;
            while ((counter = in.read(buff)) != -1)
                out.write(buff, 0, counter);
            setString(toString(out.toByteArray(), 0, length, getIscEncoding()));
        }
        catch (IOException ioex) {
            throw (SQLException) createException(
                BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
        }
    }
    void setBytes(byte[] value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        setString(toString(value, getIscEncoding()));
    }

    //----- setDate, setTime and setTimestamp code

    void setDate(Date value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        setString(value.toString());
    }
    void setTime(Time value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        setString(value.toString());
    }
    void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            setNull(true);
            return;
        }

        setString(value.toString());
    }

}
