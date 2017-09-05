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

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Calendar;

/**
 * Describe class <code>FBStringField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 * @todo implement data handling code
 * @todo implement correct exception throwing in all setXXX methods that use
 * setString(String), currently it will raise an exception with string conversion
 * error message, instead it should complain about error coresponding to the XXX.
 * @todo think about the right setBoolean and getBoolean (currently it is "Y"
 * and "N", or "TRUE" and "FALSE").
 * <p>
 * TODO check if the setBinaryStream(null) is allowed by specs.
 */
class FBStringField extends FBField {
    static final String SHORT_TRUE = "Y";
    static final String SHORT_FALSE = "N";
    static final String LONG_TRUE = "true";
    static final String LONG_FALSE = "false";
    static final String SHORT_TRUE_2 = "T";
    static final String SHORT_TRUE_3 = "1";

    protected final int possibleCharLength;

    FBStringField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);

        int charLength = fieldDescriptor.getCharacterLength();
        // TODO This might wreak havoc if field is a FBLongVarcharField
        // TODO currently avoiding -1 to avoid problems in FBLongVarcharField (eg with setBoolean); need to fix that
        possibleCharLength = charLength != -1 ? charLength : fieldDescriptor.getLength();
    }

    @Override
    public byte getByte() throws SQLException {
        if (isNull()) return BYTE_NULL_VALUE;

        try {
            return Byte.parseByte(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(BYTE_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public short getShort() throws SQLException {
        if (isNull()) return SHORT_NULL_VALUE;

        try {
            return Short.parseShort(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(SHORT_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public int getInt() throws SQLException {
        if (isNull()) return INT_NULL_VALUE;

        try {
            return Integer.parseInt(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(INT_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public long getLong() throws SQLException {
        if (isNull()) return LONG_NULL_VALUE;

        try {
            return Long.parseLong(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(LONG_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public BigDecimal getBigDecimal() throws SQLException {
        if (isNull()) return null;

        try {
            return new BigDecimal(getString().trim());
        } catch (NumberFormatException e) {
            throw new TypeConversionException(BIGDECIMAL_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public float getFloat() throws SQLException {
        if (isNull()) return FLOAT_NULL_VALUE;

        try {
            return Float.parseFloat(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(FLOAT_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    @Override
    public double getDouble() throws SQLException {
        if (isNull()) return DOUBLE_NULL_VALUE;

        try {
            return Double.parseDouble(getString().trim());
        } catch (NumberFormatException nfex) {
            throw new TypeConversionException(DOUBLE_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    //----- getBoolean, getString and getObject code

    @Override
    public boolean getBoolean() throws SQLException {
        if (isNull()) return BOOLEAN_NULL_VALUE;

        final String trimmedValue = getString().trim();
        return trimmedValue.equalsIgnoreCase(LONG_TRUE) ||
                trimmedValue.equalsIgnoreCase(SHORT_TRUE) ||
                trimmedValue.equalsIgnoreCase(SHORT_TRUE_2) ||
                trimmedValue.equalsIgnoreCase(SHORT_TRUE_3);
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeString(getFieldData());
    }

    //----- getXXXStream code

    @Override
    public InputStream getBinaryStream() throws SQLException {
        if (isNull()) return null;
        return new ByteArrayInputStream(getFieldData());
    }

    @Override
    public byte[] getBytes() throws SQLException {
        if (isNull()) return null;
        // protect against unintentional modification of cached or shared byte-arrays (eg in DatabaseMetaData)
        return getFieldData().clone();
    }

    //----- getDate, getTime and getTimestamp code

    @Override
    public Date getDate(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeDate(getDate(), cal);
    }

    @Override
    public Date getDate() throws SQLException {
        if (isNull()) return null;
        return Date.valueOf(getString().trim());
    }

    @Override
    public Time getTime(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTime(getTime(), cal, isInvertTimeZone());
    }

    @Override
    public Time getTime() throws SQLException {
        if (isNull()) return null;
        return Time.valueOf(getString().trim());
    }

    @Override
    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeTimestamp(getTimestamp(), cal, isInvertTimeZone());
    }

    @Override
    public Timestamp getTimestamp() throws SQLException {
        if (isNull()) return null;
        return Timestamp.valueOf(getString().trim());
    }

    @Override
    public BigInteger getBigInteger() throws SQLException {
        if (isNull()) return null;

        try {
            return new BigInteger(getString().trim());
        } catch (NumberFormatException e) {
            throw new TypeConversionException(BIG_INTEGER_CONVERSION_ERROR + " " + getString().trim());
        }
    }

    //--- setXXX methods

    //----- Math code

    @Override
    public void setByte(byte value) throws SQLException {
        setString(Byte.toString(value));
    }

    @Override
    public void setShort(short value) throws SQLException {
        setString(Short.toString(value));
    }

    @Override
    public void setInteger(int value) throws SQLException {
        setString(Integer.toString(value));
    }

    @Override
    public void setLong(long value) throws SQLException {
        setString(Long.toString(value));
    }

    @Override
    public void setFloat(float value) throws SQLException {
        setString(Float.toString(value));
    }

    @Override
    public void setDouble(double value) throws SQLException {
        setString(Double.toString(value));
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setString(value.toString());
    }

    //----- setBoolean, setString and setObject code

    @Override
    public void setBoolean(boolean value) throws SQLException {
        if (possibleCharLength > 4) {
            setString(value ? LONG_TRUE : LONG_FALSE);
        } else if (possibleCharLength >= 1) {
            setString(value ? SHORT_TRUE : SHORT_FALSE);
        }
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }
        setFieldData(getDatatypeCoder().encodeString(value));
    }

    //----- setXXXStream code

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        if (in == null) {
            setNull();
            return;
        }

        // TODO More specific value
        if (length > Integer.MAX_VALUE) {
            throw new FBDriverNotCapableException("Only length <= Integer.MAX_VALUE supported");
        }

        try {
            setBytes(IOUtils.toBytes(in, (int) length));
        } catch (IOException ioex) {
            throw new TypeConversionException(BINARY_STREAM_CONVERSION_ERROR);
        }
    }

    @Override
    protected void setCharacterStreamInternal(Reader in, long length) throws SQLException {
        if (in == null) {
            setNull();
            return;
        }

        // TODO More specific value
        if (length > Integer.MAX_VALUE) {
            throw new FBDriverNotCapableException("Only length <= Integer.MAX_VALUE supported");
        }

        try {
            setString(IOUtils.toString(in, (int) length));
        } catch (IOException ioex) {
            throw new TypeConversionException(CHARACTER_STREAM_CONVERSION_ERROR);
        }
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        if (value.length > fieldDescriptor.getLength()) {
            throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, value.length,
                    fieldDescriptor.getLength());
        }

        setFieldData(value);
    }

    //----- setDate, setTime and setTimestamp code

    @Override
    public void setDate(Date value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setDate(getDatatypeCoder().encodeDate(value, cal));
    }

    @Override
    public void setDate(Date value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setString(value.toString());
    }

    @Override
    public void setTime(Time value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setTime(getDatatypeCoder().encodeTime(value, cal, isInvertTimeZone()));
    }

    @Override
    public void setTime(Time value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setString(value.toString());
    }

    @Override
    public void setTimestamp(Timestamp value, Calendar cal) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setTimestamp(getDatatypeCoder().encodeTimestamp(value, cal, isInvertTimeZone()));
    }

    @Override
    public void setTimestamp(Timestamp value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setString(value.toString());
    }

    @Override
    public void setBigInteger(BigInteger value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setString(value.toString());
    }
}
