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
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Tests for {@link FBBinaryField}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBinaryField extends BaseJUnit4TestFBField<FBBinaryField, byte[]> {

    private static final int FIELD_LENGTH = 15;
    private final Random rnd = new Random();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_VARYING);
        rowDescriptorBuilder.setSubType(ISCConstants.CS_BINARY);
        rowDescriptorBuilder.setLength(FIELD_LENGTH);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBBinaryField(fieldDescriptor, fieldData, Types.VARBINARY);
    }

    @Test
    @Override
    public void getCharacterStreamNonNull() throws Exception {
        final byte[] bytes = getRandomBytes();
        final String expectedString = new String(bytes);
        toReturnValueExpectations(bytes);

        Reader reader = field.getCharacterStream();
        StringBuilder stringBuilder = new StringBuilder();
        int characterValue;
        while ((characterValue = reader.read()) != -1) {
            stringBuilder.append((char) characterValue);
        }

        assertEquals(expectedString, stringBuilder.toString());
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final String expectedString = new String(bytes);
        toReturnValueExpectations(bytes);

        String value = field.getString();

        assertEquals(expectedString, value);
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        final String string = "hdgkehgfjdfjdfe";
        final byte[] bytes = string.getBytes();
        setValueExpectations(bytes);

        field.setString(string);
    }

    @Test
    @Override
    public void getAsciiStreamNonNull() throws Exception {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        InputStream stream = field.getAsciiStream();

        assertArrayEquals(bytes, streamToBytes(stream));
    }

    @Test
    @Override
    public void setAsciiStreamNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        setValueExpectations(bytes);
        InputStream stream = new ByteArrayInputStream(bytes);

        field.setAsciiStream(stream, FIELD_LENGTH);
    }

    @Test
    @Override
    public void getBinaryStreamNonNull() throws Exception {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        InputStream stream = field.getBinaryStream();

        assertArrayEquals(bytes, streamToBytes(stream));
    }

    @Test
    @Override
    public void setBinaryStreamNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        setValueExpectations(bytes);
        InputStream stream = new ByteArrayInputStream(bytes);

        field.setBinaryStream(stream, FIELD_LENGTH);
    }

    @Test
    public void setCharacterStreamNonNull() throws SQLException {
        final String string = "hdgkehgfjdfjdfe";
        final byte[] bytes = string.getBytes();
        setValueExpectations(bytes);
        Reader reader = new StringReader(string);

        field.setCharacterStream(reader, FIELD_LENGTH);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        Object value = field.getObject();

        assertThat(value, instanceOf(byte[].class));
        assertArrayEquals(bytes, (byte[]) value);
        assertNotSame("Expected a clone of the bytes", bytes, value);
    }

    @Test
    public void getObjectNonNull_typeBinary() throws SQLException {
        rowDescriptorBuilder.setType(ISCConstants.SQL_TEXT);
        rowDescriptorBuilder.setSubType(ISCConstants.CS_BINARY);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBBinaryField(fieldDescriptor, fieldData, Types.BINARY);

        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        Object value = field.getObject();

        assertThat(value, instanceOf(byte[].class));
        assertArrayEquals(bytes, (byte[]) value);
        assertNotSame("Expected a clone of the bytes", bytes, value);
    }

    @Test
    public void setObjectNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        setValueExpectations(bytes);

        field.setObject(bytes);
    }

    @Test
    @Override
    public void getBytesNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        byte[] value = field.getBytes();

        assertArrayEquals(bytes, value);
        assertNotSame("Expected a clone of the bytes", bytes, value);
    }

    @Test
    @Override
    public void setBytesNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        setValueExpectations(bytes);

        field.setBytes(bytes);
    }

    private byte[] getRandomBytes() {
        return getRandomBytes(FIELD_LENGTH);
    }

    private byte[] getRandomBytes(int length) {
        final byte[] bytes = new byte[length];
        rnd.nextBytes(bytes);
        return bytes;
    }

    private byte[] streamToBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int byteValue;
        while ((byteValue = stream.read()) != -1) {
            baos.write(byteValue);
        }

        return baos.toByteArray();
    }

    @Override
    protected byte[] getNonNullObject() {
        return new byte[0];
    }
}
