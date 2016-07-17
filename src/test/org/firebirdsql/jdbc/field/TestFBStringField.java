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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.util.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Tests for {@link FBStringField}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBStringField extends BaseJUnit4TestFBField<FBStringField, String> {

    static short TEST_STRING_SIZE = 40;

    // TEST_STRING_LONG should be shorter than TEST_STRING_SIZE
    static String TEST_STRING_SHORT = "This is short string.";

    // TEST_STRING_LONG should be longer than TEST_STRING_SIZE
    static String TEST_STRING_LONG = "And this string should be longer than short one.";

    private static final Encoding encoding = EncodingFactory.getPlatformEncoding();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        rowDescriptorBuilder.setLength(TEST_STRING_SIZE);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBStringField(fieldDescriptor, fieldData, Types.VARCHAR);
    }

    @Test
    public void getString() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        assertEquals("String does not equal to assigned one.", TEST_STRING_SHORT, field.getString().trim());
        /*
        // Commented out by R.Rokytskyy: FBStringField was changed to allow
        // server complain about data truncation, not the driver. This was done
        // in order to avoid problems in case of multi-byte character fields.
        try {
            field.setString(TEST_STRING_LONG);
            assertTrue("String longer than available space should not be allowed", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
        */
    }

   @Test
    public void getBigDecimalNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getBigDecimal", field.getBigDecimal());
    }

    @Test
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        toReturnStringExpectations("837.47394", encoding);

        assertEquals("Unexpected value for getBigDecimal", new BigDecimal("837.47394"), field.getBigDecimal());
    }

    @Test
    @Override
    public void getObject_BigDecimal() throws SQLException {
        toReturnStringExpectations("837.47394", encoding);

        assertEquals("Unexpected value for getObject(BigDecimal.class)",
                new BigDecimal("837.47394"), field.getObject(BigDecimal.class));
    }

    @Test
    @Override
    public void setBigDecimalNonNull() throws SQLException {
        setStringExpectations("5.381093", encoding);

        field.setBigDecimal(new BigDecimal("5.381093"));
    }

    @Ignore
    @Test
    @Override
    public void getBigDecimalIntNonNull() throws SQLException {
        // TODO Build suitable test
    }

    @Test
    public void getBinaryStreamNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getBinaryStream", field.getBinaryStream());
    }

    @Test
    @Override
    public void getBinaryStreamNonNull() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        String fromStream = new String(IOUtils.toBytes(field.getBinaryStream(), Integer.MAX_VALUE));

        assertEquals("Binary stream values test failure", TEST_STRING_SHORT, fromStream.trim());
    }

    @Test
    @Override
    public void getObject_InputStream() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);
        String fromStream = new String(IOUtils.toBytes(field.getObject(InputStream.class), Integer.MAX_VALUE));

        assertEquals("Binary stream values test failure", TEST_STRING_SHORT, fromStream.trim());
    }

    @Test
    @Override
    public void setBinaryStreamNonNull() throws Exception {
        setStringExpectations(TEST_STRING_SHORT, encoding);
        byte[] bytes = TEST_STRING_SHORT.getBytes();

        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
    }

    @Test
    public void setBinaryStream_tooLong() throws Exception {
        expectedException.expect(java.sql.DataTruncation.class);
        byte[] bytes = TEST_STRING_LONG.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
    }

    @Test
    public void getBooleanNull() throws Exception {
        toReturnNullExpectations();

        assertFalse("Expected false for getBoolean on null", field.getBoolean());
    }

    @Test
    @Override
    public void getBooleanNonNull() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_TRUE, encoding);

        assertTrue("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    @Override
    public void getObject_Boolean() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_TRUE, encoding);

        assertTrue("Unexpected value for getObject(Boolean.class)", field.getObject(Boolean.class));
    }

    @Test
    public void getBoolean_shortTrue() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE, encoding);

        assertTrue("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_shortTrue2() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE_2, encoding);

        assertTrue("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_shortTrue3() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_TRUE_3, encoding);

        assertTrue("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_longFalse() throws SQLException {
        toReturnStringExpectations(FBStringField.LONG_FALSE, encoding);

        assertFalse("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_shortFalse() throws SQLException {
        toReturnStringExpectations(FBStringField.SHORT_FALSE, encoding);

        assertFalse("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBoolean_otherValueIsFalse() throws SQLException {
        toReturnStringExpectations("jdsd", encoding);

        assertFalse("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    @Override
    public void setBoolean() throws SQLException {
        setStringExpectations(FBStringField.LONG_TRUE, encoding);

        field.setBoolean(true);
    }

    @Test
    public void setBoolean_false() throws SQLException {
        setStringExpectations(FBStringField.LONG_FALSE, encoding);

        field.setBoolean(false);
    }

    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        toReturnStringExpectations("89", encoding);

        assertEquals("Unexpected value for getByte", 89, field.getByte());
    }

    @Test
    @Override
    public void getObject_Byte() throws SQLException {
        toReturnStringExpectations("89", encoding);

        assertEquals("Unexpected value for getByte", 89, (byte) field.getObject(Byte.class));
    }

    @Test
    @Override
    public void setByte() throws SQLException {
        setStringExpectations("-128", encoding);

        field.setByte(Byte.MIN_VALUE);
    }

    @Test
    @Override
    public void getBytesNonNull() throws SQLException {
        toReturnValueExpectations(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getBytes());
        assertEquals("Bytes stream values test failure", TEST_STRING_SHORT, fromBytes.trim());
    }

    @Test
    @Override
    public void getObject_byteArray() throws SQLException {
        toReturnValueExpectations(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getObject(byte[].class));
        assertEquals("Bytes value test failure", TEST_STRING_SHORT, fromBytes.trim());
    }

    @Test
    @Override
    public void setBytesNonNull() throws SQLException {
        setStringExpectations(TEST_STRING_SHORT, encoding);

        field.setBytes(TEST_STRING_SHORT.getBytes());
    }

    @Test
    public void setBytes_TooLong() throws SQLException {
        expectedException.expect(java.sql.DataTruncation.class);
        field.setBytes(TEST_STRING_LONG.getBytes());
    }

    @Test
    public void getCharacterStreamNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getCharacterStream", field.getCharacterStream());
    }

    @Test
    @Override
    public void getCharacterStreamNonNull() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        String value = IOUtils.toString(field.getCharacterStream(), Integer.MAX_VALUE);
        assertEquals("Unexpected value from getCharacterStream", TEST_STRING_SHORT, value);
    }

    @Test
    @Override
    public void getObject_Reader() throws Exception {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        String value = IOUtils.toString(field.getObject(Reader.class), Integer.MAX_VALUE);
        assertEquals("Unexpected value from getCharacterStream", TEST_STRING_SHORT, value);
    }

    @Test
    @Override
    public void setCharacterStreamNonNull() throws Exception {
        setStringExpectations(TEST_STRING_SHORT, encoding);

        field.setCharacterStream(new StringReader(TEST_STRING_SHORT), TEST_STRING_SHORT.length());
    }

    @Test
    public void setCharacterStream_tooLong() throws Exception {
        // TODO: length not validated in current implementation
        // expectedException.expect(TypeConversionException.class);
        setStringExpectations(TEST_STRING_LONG, encoding);

        field.setCharacterStream(new StringReader(TEST_STRING_LONG), TEST_STRING_LONG.length());
    }

    @Test
    public void getDateNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getDate", field.getDate());
    }

    @Test
    @Override
    public void getDateNonNull() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);

        assertEquals("Unexpected value for getDate", java.sql.Date.valueOf("2016-04-24"), field.getDate());
    }

    @Test
    @Override
    public void getDateCalendarNonNull() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        assertEquals("Unexpected value for getDate(Calendar)", "2016-04-24", field.getDate(calendar).toString());
    }

    @Test
    @Override
    public void setDateCalendarNonNull() throws SQLException {
        setStringExpectations("2016-04-24", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setDate(java.sql.Date.valueOf("2016-04-24"), calendar);
    }

    @Test
    @Override
    public void getObject_java_sql_Date() throws SQLException {
        toReturnStringExpectations("2016-04-24", encoding);

        assertEquals("Unexpected value for getObject(java.sql.Date.class)",
                java.sql.Date.valueOf("2016-04-24"), field.getObject(java.sql.Date.class));
    }

    @Test
    @Override
    public void setDateNonNull() throws SQLException {
        setStringExpectations("2016-04-24", encoding);

        field.setDate(java.sql.Date.valueOf("2016-04-24"));
    }

    @Test
    public void getDoubleNull() throws SQLException {
        toReturnNullExpectations();

        assertEquals("Expected 0 for null getDouble", 0, field.getDouble(), 0);
    }

    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals("Unexpected value for getDouble", 9.32892, field.getDouble(), 0);
    }

    @Test
    @Override
    public void getObject_Double() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals("Unexpected value for getObject(Double.class)", 9.32892, field.getObject(Double.class), 0);
    }

    @Test
    @Override
    public void setDouble() throws SQLException {
        setStringExpectations(Double.toString(739.389932), encoding);

        field.setDouble(739.389932);
    }

    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals("Unexpected value for getFloat", 9.32892, field.getFloat(), 0.0001);
    }

    @Test
    @Override
    public void getObject_Float() throws SQLException {
        toReturnStringExpectations("9.32892", encoding);

        assertEquals("Unexpected value for getObject(Float.class)", 9.32892, field.getObject(Float.class), 0.0001);
    }

    @Test
    @Override
    public void setFloat() throws SQLException {
        setStringExpectations(Float.toString(3.472f), encoding);

        field.setFloat(3.472f);
    }

    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        toReturnStringExpectations("734823", encoding);

        assertEquals("Unexpected value for getInt", 734823, field.getInt());
    }

    @Test
    @Override
    public void getObject_Integer() throws SQLException {
        toReturnStringExpectations("734823", encoding);

        assertEquals("Unexpected value for getObject(Integer.class)", 734823, field.getObject(Integer.class).intValue());
    }

    @Test
    @Override
    public void setInteger() throws SQLException {
        setStringExpectations("-124579", encoding);

        field.setInteger(-124579);
    }

    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        toReturnStringExpectations("9378037472243", encoding);

        assertEquals("Unexpected value for getLong", 9378037472243L, field.getLong());
    }

    @Test
    @Override
    public void getObject_Long() throws SQLException {
        toReturnStringExpectations("9378037472243", encoding);

        assertEquals("Unexpected value for getObject(Long.class)",
                9378037472243L, field.getObject(Long.class).longValue());
    }

    @Test
    @Override
    public void setLong() throws SQLException {
        setStringExpectations("735987378945", encoding);

        field.setLong(735987378945L);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnStringExpectations("some string value", encoding);

        assertEquals("Unexpected value for getObject", "some string value", field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setStringExpectations(TEST_STRING_SHORT, encoding);

        field.setObject(TEST_STRING_SHORT);
    }

    @Test
    public void setObject_tooLong() throws SQLException {
        //expectedException.expect(DataTruncation.class);
        //TODO Current implementation will accept too long values and leave error to server
        setStringExpectations(TEST_STRING_LONG, encoding);

        field.setObject(TEST_STRING_LONG);
    }

    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        toReturnStringExpectations("840", encoding);

        assertEquals("Unexpected value for getShort", 840, field.getShort());
    }

    @Test
    @Override
    public void getObject_Short() throws SQLException {
        toReturnStringExpectations("840", encoding);

        assertEquals("Unexpected value for getObject(Short.class)", 840, field.getObject(Short.class).shortValue());
    }

    @Test
    @Override
    public void setShort() throws SQLException {
        setStringExpectations("7301", encoding);

        field.setShort((short) 7301);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        assertEquals("Unexpected value for getString", TEST_STRING_SHORT, field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnStringExpectations(TEST_STRING_SHORT, encoding);

        assertEquals("Unexpected value for getObject(String.class)", TEST_STRING_SHORT, field.getObject(String.class));
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        setStringExpectations(TEST_STRING_SHORT, encoding);

        field.setString(TEST_STRING_SHORT);
    }

    @Test
    @Override
    public void getTimeNonNull() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);

        assertEquals("Unexpected value for getTime", "10:05:01", field.getTime().toString());
    }

    @Test
    public void getObject_java_sql_Time() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);

        assertEquals("Unexpected value for getObject(java.sql.Time.class)",
                "10:05:01", field.getObject(java.sql.Time.class).toString());
    }

    @Test
    public void setTimeNonNull() throws SQLException {
        setStringExpectations("10:05:01", encoding);

        field.setTime(Time.valueOf("10:05:01"));
    }

    @Test
    @Override
    public void getTimeCalendarNonNull() throws SQLException {
        toReturnStringExpectations("10:05:01", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // java.sql.Time.toString() will render in the current time zone
        assertEquals("Unexpected value for getTime", "11:05:01", field.getTime(calendar).toString());
    }

    @Test
    @Override
    public void setTimeCalendarNonNull() throws SQLException {
        setStringExpectations("09:05:01", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTime(java.sql.Time.valueOf("10:05:01"), calendar);
    }

    @Test
    @Override
    public void getTimestampNonNull() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        assertEquals("Unexpected value for getTimestamp", "2016-05-02 10:57:01.0", field.getTimestamp().toString());
    }

    @Test
    @Override
    public void getObject_java_sql_Timestamp() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        assertEquals("Unexpected value for getObject(java.sql.Timestamp.class)",
                "2016-05-02 10:57:01.0", field.getObject(java.sql.Timestamp.class).toString());
    }

    @Test
    @Override
    public void getObject_java_util_Date() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);

        // Test depends on the fact that we currently return java.sql.Timestamp
        assertEquals("Unexpected value for getObject(java.util.Date.class)",
                "2016-05-02 10:57:01.0", field.getObject(java.util.Date.class).toString());
    }

    @Test
    @Override
    public void getObject_Calendar() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"));

        assertEquals("Unexpected value for getObject(Calendar.class)", calendar, field.getObject(Calendar.class));
    }

    @Test
    @Override
    public void setTimestampNonNull() throws SQLException {
        setStringExpectations("2016-05-02 10:57:01.0", encoding);

        field.setTimestamp(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"));
    }

    @Test
    @Override
    public void getTimestampCalendarNonNull() throws SQLException {
        toReturnStringExpectations("2016-05-02 10:57:01", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        // java.sql.Timestamp.toString() will render in the current time zone
        assertEquals("Unexpected value for getTimestamp(Calendar)",
                "2016-05-02 11:57:01.0", field.getTimestamp(calendar).toString());
    }

    @Test
    @Override
    public void setTimestampCalendarNonNull() throws SQLException {
        setStringExpectations("2016-05-02 09:57:01.0", encoding);
        Calendar calendar = Calendar.getInstance(getOneHourBehindTimeZone());

        field.setTimestamp(java.sql.Timestamp.valueOf("2016-05-02 10:57:01"), calendar);
    }

    @Override
    protected String getNonNullObject() {
        return TEST_STRING_SHORT;
    }
}
