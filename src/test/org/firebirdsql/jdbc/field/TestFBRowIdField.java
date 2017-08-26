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
import org.firebirdsql.jdbc.FBRowId;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBRowIdField extends BaseJUnit4TestFBField<FBRowIdField, RowId> {

    // Note some of the tests were copied from FBBinaryField to check if the field is (largely) backwards compatible

    private static final int FIELD_LENGTH = 8;
    private final Random rnd = new Random();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TEXT);
        rowDescriptorBuilder.setSubType(ISCConstants.CS_BINARY);
        rowDescriptorBuilder.setLength(FIELD_LENGTH);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBRowIdField(fieldDescriptor, fieldData, Types.ROWID);
    }

    @Test
    @Override
    public void getRowIdNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        RowId rowId = field.getRowId();
        
        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    @Override
    public void setRowIdNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final RowId rowId = new FBRowId(bytes);
        setValueExpectations(bytes);

        field.setRowId(rowId);
    }

    @Test
    @Override
    public void getObject_RowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        RowId rowId = field.getObject(RowId.class);

        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    public void getObject_RowId_null() throws SQLException {
        toReturnNullExpectations();

        RowId rowId = field.getObject(RowId.class);

        assertNull(rowId);
    }

    @Test
    @Override
    public void getObject_FBRowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        FBRowId rowId = field.getObject(FBRowId.class);

        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    public void getObject_FBRowId_null() throws SQLException {
        toReturnNullExpectations();

        FBRowId rowId = field.getObject(FBRowId.class);

        assertNull(rowId);
    }

    @Test
    @Override
    public void setObject_RowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final RowId rowId = new FBRowId(bytes);
        setValueExpectations(bytes);

        field.setObject(rowId);
    }

    @Test
    @Override
    public void getCharacterStreamNonNull() throws Exception {
        final byte[] bytes = getRandomBytes();
        final String expectedString = datatypeCoder.decodeString(bytes);
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
    public void getObject_Reader() throws Exception {
        final byte[] bytes = getRandomBytes();
        final String expectedString = datatypeCoder.decodeString(bytes);
        toReturnValueExpectations(bytes);

        Reader reader = field.getObject(Reader.class);
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
        final String expectedString = datatypeCoder.decodeString(bytes);
        toReturnValueExpectations(bytes);

        String value = field.getString();

        assertEquals(expectedString, value);
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final String expectedString = datatypeCoder.decodeString(bytes);
        toReturnValueExpectations(bytes);

        String value = field.getObject(String.class);

        assertEquals(expectedString, value);
    }

    @Test
    @Override
    public void setStringNonNull() throws SQLException {
        final String string = "hdgkehgf";
        final byte[] bytes = string.getBytes();
        setValueExpectations(bytes);

        field.setString(string);
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
    public void getObject_InputStream() throws Exception {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        InputStream stream = field.getObject(InputStream.class);

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
        final String string = "hdgkehgf";
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

        assertThat(value, instanceOf(RowId.class));
        assertArrayEquals(bytes, ((RowId) value).getBytes());
    }

    @Test
    public void setObjectNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        setValueExpectations(bytes);

        field.setObject(new FBRowId(bytes));
    }

    @Test
    public void setObjectNonNull_bytes() throws SQLException {
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
    public void getObject_byteArray() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        byte[] value = field.getObject(byte[].class);

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
    protected RowId getNonNullObject() {
        return new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
    }
}
