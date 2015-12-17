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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Describe class <code>TestFBStringField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBStringField extends BaseTestFBField {

    static short TEST_STRING_SIZE = 40;

    // TEST_STRING_LONG should be shorter than TEST_STRING_SIZE
    static String TEST_STRING_SHORT = "This is short string.";

    // TEST_STRING_LONG should be longer than TEST_STRING_SIZE
    static String TEST_STRING_LONG = "And this string should be longer than short one.";

    @Before
    public void setUp() throws SQLException {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(1, DefaultDatatypeCoder.getDefaultInstance())
                .setFieldIndex(0)
                .setType(ISCConstants.SQL_TEXT)
                .setLength(TEST_STRING_SIZE)
                .addField()
                .toRowDescriptor();
        field = FBField.createField(rowDescriptor.getFieldDescriptor(0), createDataProvider(rowDescriptor), null, false);
    }

    @Test
    public void testBigDecimal() throws SQLException {
        BigDecimal testBigDecimal = new BigDecimal((double) TEST_LONG);
        field.setBigDecimal(testBigDecimal);
        assertEquals(testBigDecimal, field.getBigDecimal());
    }

    @Test
    public void testString() throws SQLException {
        field.setString(TEST_STRING_SHORT);
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
    public void testObject() throws SQLException {
        field.setObject(TEST_STRING_SHORT);
        assertEquals("String does not equal to assigned one.", TEST_STRING_SHORT, field.getString().trim());
    }

    @Test
    public void testBinaryStream() throws SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getBinaryStream()));
        assertEquals("ASCII stream values test failure", TEST_STRING_SHORT, fromStream.trim());

        expectedException.expect(java.sql.DataTruncation.class);
        bytes = TEST_STRING_LONG.getBytes();
        field.setBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
    }

    @Test
    public void testAsciiStream() throws SQLException {
        byte[] bytes = TEST_STRING_SHORT.getBytes();
        field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
        String fromStream = new String(readInputStream(field.getAsciiStream()));
        assertEquals("ASCII stream values test failure", TEST_STRING_SHORT, fromStream.trim());

        expectedException.expect(java.sql.DataTruncation.class);
        bytes = TEST_STRING_LONG.getBytes();
        field.setAsciiStream(new ByteArrayInputStream(bytes), bytes.length);
    }

    @Test
    public void testBytes() throws SQLException {
        field.setBytes(TEST_STRING_SHORT.getBytes());
        String fromBytes = new String(field.getBytes());
        assertEquals("ASCII stream values test failure", TEST_STRING_SHORT, fromBytes.trim());

        expectedException.expect(java.sql.DataTruncation.class);
        field.setBytes(TEST_STRING_LONG.getBytes());
    }

    @Test
    public void testDate() throws SQLException {
        field.setDate(TEST_DATE);
        // because of the date-string-date conversion we lose the time part of java.sql.Date, and strictly speaking
        // TEST_DATE and field.getDate() objects are not equal.
        assertEquals("Date values test failure", TEST_DATE.toString(), field.getDate().toString());
    }

    @Test
    public void testTime() throws SQLException {
        field.setTime(TEST_TIME);
        // because of the time-string-time conversion we lose the date part of java.sql.Time, and strictly speaking
        // TEST_TIME and field.getTime() objects are not equal.
        assertEquals("Time values test failure", TEST_TIME.toString(), field.getTime().toString());
    }

    @Test
    public void testNull() throws SQLException {
        field.setString(null);
        assertNull("Null value should be set.", field.getObject());
    }
}
