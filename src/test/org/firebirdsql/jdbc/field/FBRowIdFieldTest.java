// SPDX-FileCopyrightText: Copyright 2016-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBRowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 */
class FBRowIdFieldTest extends BaseJUnit5TestFBField<FBRowIdField, RowId> {

    // Note some tests were copied from FBBinaryField to check if the field is (largely) backwards compatible

    private static final int FIELD_LENGTH = 8;

    @BeforeEach
    @Override
    void setUp() throws Exception {
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_TEXT);
        rowDescriptorBuilder.setSubType(ISCConstants.CS_BINARY);
        rowDescriptorBuilder.setLength(FIELD_LENGTH);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBRowIdField(fieldDescriptor, fieldData, Types.ROWID);
        datatypeCoder = fieldDescriptor.getDatatypeCoder();
    }

    @Test
    @Override
    void getRowIdNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        RowId rowId = field.getRowId();
        
        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    @Override
    void setRowIdNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final RowId rowId = new FBRowId(bytes);

        field.setRowId(rowId);

        verifySetValue(bytes);
    }

    @Test
    @Override
    void getObject_RowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        RowId rowId = field.getObject(RowId.class);

        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    void getObject_RowId_null() throws SQLException {
        toReturnNullExpectations();

        RowId rowId = field.getObject(RowId.class);

        assertNull(rowId);
    }

    @Test
    @Override
    void getObject_FBRowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        FBRowId rowId = field.getObject(FBRowId.class);

        assertArrayEquals(bytes, rowId.getBytes());
    }

    @Test
    void getObject_FBRowId_null() throws SQLException {
        toReturnNullExpectations();

        FBRowId rowId = field.getObject(FBRowId.class);

        assertNull(rowId);
    }

    @Test
    @Override
    void setObject_RowId() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final RowId rowId = new FBRowId(bytes);

        field.setObject(rowId);

        verifySetValue(bytes);
    }

    @Test
    @Override
    void getCharacterStreamNonNull() throws Exception {
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
    void getObject_Reader() throws Exception {
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
    void getStringNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final String expectedString = datatypeCoder.decodeString(bytes);
        toReturnValueExpectations(bytes);

        String value = field.getString();

        assertEquals(expectedString, value);
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        final byte[] bytes = getRandomBytes();
        final String expectedString = datatypeCoder.decodeString(bytes);
        toReturnValueExpectations(bytes);

        String value = field.getObject(String.class);

        assertEquals(expectedString, value);
    }

    @Test
    @Override
    void setStringNonNull() throws SQLException {
        final String string = "hdgkehgf";
        final byte[] bytes = string.getBytes();

        field.setString(string);

        verifySetValue(bytes);
    }

    @Test
    @Override
    void getBinaryStreamNonNull() throws Exception {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        InputStream stream = field.getBinaryStream();

        assertArrayEquals(bytes, streamToBytes(stream));
    }

    @Test
    @Override
    void getObject_InputStream() throws Exception {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        InputStream stream = field.getObject(InputStream.class);

        assertArrayEquals(bytes, streamToBytes(stream));
    }

    @Test
    @Override
    void setBinaryStreamNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        InputStream stream = new ByteArrayInputStream(bytes);

        field.setBinaryStream(stream, FIELD_LENGTH);

        verifySetValue(bytes);
    }

    @Test
    void setCharacterStreamNonNull() throws SQLException {
        final String string = "hdgkehgf";
        final byte[] bytes = string.getBytes();
        Reader reader = new StringReader(string);

        field.setCharacterStream(reader, FIELD_LENGTH);

        verifySetValue(bytes);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        Object value = field.getObject();

        assertThat(value, instanceOf(RowId.class));
        assertArrayEquals(bytes, ((RowId) value).getBytes());
    }

    @Test
    void setObjectNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();

        field.setObject(new FBRowId(bytes));

        verifySetValue(bytes);
    }

    @Test
    void setObjectNonNull_bytes() throws SQLException {
        final byte[] bytes = getRandomBytes();

        field.setObject(bytes);

        verifySetValue(bytes);
    }

    @Test
    @Override
    void getBytesNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        byte[] value = field.getBytes();

        assertArrayEquals(bytes, value);
        assertNotSame(bytes, value, "Expected a clone of the bytes");
    }

    @Test
    @Override
    void getObject_byteArray() throws SQLException {
        final byte[] bytes = getRandomBytes();
        toReturnValueExpectations(bytes);

        byte[] value = field.getObject(byte[].class);

        assertArrayEquals(bytes, value);
        assertNotSame(bytes, value, "Expected a clone of the bytes");
    }

    @Test
    @Override
    void setBytesNonNull() throws SQLException {
        final byte[] bytes = getRandomBytes();

        field.setBytes(bytes);

        verifySetValue(bytes);
    }

    private byte[] getRandomBytes() {
        return getRandomBytes(FIELD_LENGTH);
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
    RowId getNonNullObject() {
        return new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
    }
}
