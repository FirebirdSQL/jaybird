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

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * This is Blob-based implementation of {@link FBStringField}. It should be used
 * for fields declared in database as <code>BLOB SUB_TYPE 1</code>. This
 * implementation provides all conversion routines {@link FBStringField} has.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBLongVarCharField extends FBStringField implements FBFlushableField {

    // TODO Reduce duplication with FBBlobField

    private static final int BUFF_SIZE = 4096;

    private FBBlob blob;

    // Rather then hold cached data in the XSQLDAVar we will hold it in here.
    private int length;
    private byte[] bytes;
    private InputStream binaryStream;
    private Reader characterStream;

    FBLongVarCharField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(field, dataProvider, requiredType);
    }

    public void close() throws SQLException {
        try {
            if (blob != null) blob.close();
        } catch (IOException ioex) {
            throw new FBSQLException(ioex);
        } finally {
            // forget this blob instance, resource waste
            // but simplifies our life. BLOB handle will be
            // released by a server automatically later

            blob = null;
            bytes = null;
            binaryStream = null;
            characterStream = null;
            length = 0;
        }
    }

    public Blob getBlob() throws SQLException {
        if (blob != null) return blob;
        final byte[] fieldData = getFieldData();
        if (fieldData == null) return BLOB_NULL_VALUE;

        blob = new FBBlob(gdsHelper, field.decodeLong(fieldData));

        return blob;
    }

    public Clob getClob() throws SQLException {
        FBBlob blob = (FBBlob) getBlob();
        if (blob == BLOB_NULL_VALUE) return CLOB_NULL_VALUE;
        return new FBClob(blob);
    }

    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlob();
        if (blob == BLOB_NULL_VALUE) return STREAM_NULL_VALUE;
        return blob.getBinaryStream();
    }

    public byte[] getBytes() throws SQLException {
        final Blob blob = getBlob();
        if (blob == BLOB_NULL_VALUE) return BYTES_NULL_VALUE;

        final InputStream in = blob.getBinaryStream();
        if (in == STREAM_NULL_VALUE) return BYTES_NULL_VALUE;

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();
        try {
            final byte[] buff = new byte[BUFF_SIZE];
            int counter;
            while ((counter = in.read(buff)) != -1) {
                bout.write(buff, 0, counter);
            }
        } catch (IOException ioex) {
            chain.append(createException(BYTES_CONVERSION_ERROR + " " + ioex.getMessage()));
        } finally {
            try {
                in.close();
            } catch (IOException ioex) {
                chain.append(new FBSQLException(ioex));
            }
        }

        if (chain.hasException()) {
            throw chain.getException();
        }

        return bout.toByteArray();
    }

    public byte[] getCachedData() throws SQLException {
        if (getFieldData() != null) {
            return getBytes();
        } else if (bytes != null) {
            return bytes;
        } else {
            return BYTES_NULL_VALUE;
        }
    }

    public CachedObject getCachedObject() throws SQLException {
        if (getFieldData() == null) {
            return new CachedObject(bytes, binaryStream, characterStream, length);
        }

        final byte[] bytes = getBytes();
        return new CachedObject(bytes, null, null, 0/* bytes.length*/);
    }

    public void setCachedObject(FBFlushableField.CachedObject cachedObject) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        bytes = cachedObject.bytes;
        binaryStream = cachedObject.binaryStream;
        characterStream = cachedObject.characterStream;
        length = cachedObject.length;
    }

    public String getString() throws SQLException {
        final byte[] data = getBytes();
        if (data == BYTES_NULL_VALUE) return STRING_NULL_VALUE;

        return field.decodeString(data, javaEncoding, mappingPath);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        setFieldData(field.encodeLong(blob.getBlobId()));
        this.blob = blob;
    }

    public void setClob(FBClob clob) throws SQLException {
        FBBlob blob = clob.getWrappedBlob();
        setBlob(blob);
    }

    public void setCharacterStream(Reader in, int length) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (in != READER_NULL_VALUE) {
            this.characterStream = in;
            this.length = length;
        }
    }

    public void setString(String value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != STRING_NULL_VALUE) {
            setBytes(field.encodeString(value, javaEncoding, mappingPath));
        }
    }

    public void setBytes(byte[] value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != BYTES_NULL_VALUE) {
            this.bytes = value;
            this.length = value.length;
        }
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (in != STREAM_NULL_VALUE) {
            this.binaryStream = in;
            this.length = length;
        }
    }

    public void flushCachedData() throws SQLException {
        if (binaryStream != null) {
            copyBinaryStream(binaryStream, length);
        } else if (characterStream != null) {
            copyCharacterStream(characterStream, length, javaEncoding);
        } else if (bytes != null) {
            copyBytes(bytes, length);
        } else if (blob == null) {
            setNull();
        }

        this.characterStream = null;
        this.binaryStream = null;
        this.bytes = null;
        this.length = 0;
    }

    @Override
    public void setNull() {
        super.setNull();
        try {
            if (blob != null) blob.close();
        } catch (IOException e) {
            //ignore
        } finally {
            blob = null;
            binaryStream = null;
            characterStream = null;
            bytes = null;
            length = 0;
        }
    }

    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyStream(in, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

    private void copyCharacterStream(Reader in, int length, String encoding) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyCharacterStream(in, length, encoding);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

    private void copyBytes(byte[] bytes, int length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyBytes(bytes, 0, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

}