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

import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.Synchronizable;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * Describe class <code>FBBlobField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBBlobField extends FBField implements FBFlushableField {

    private FBBlob blob;
    private long length;
    private InputStream binaryStream;
    private Reader characterStream;
    private byte[] bytes;

    FBBlobField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public void close() throws SQLException {
        try {
            if (blob != null) blob.free();
        } finally {
            // forget this blob instance, resource waste but simplifies our life. BLOB handle will be
            // released by a server automatically later
            blob = null;
            bytes = null;
            binaryStream = null;
            characterStream = null;
            length = 0;
        }
    }

    @Override
    public Blob getBlob() throws SQLException {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;

        /*@todo convert this into a method of FirebirdConnection */
        blob = new FBBlob(gdsHelper, getDatatypeCoder().decodeLong(bytes));

        return blob;
    }

    @Override
    public Clob getClob() throws SQLException {
        FBBlob blob = (FBBlob) getBlob();
        if (blob == null) return null;
        return new FBClob(blob);
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlob();
        if (blob == null) return null;

        return blob.getBinaryStream();
    }

    @Override
    public byte[] getBytes() throws SQLException {
        return getBytesInternal();
    }

    public byte[] getBytesInternal() throws SQLException {
        final byte[] blobIdBuffer = getFieldData();
        if (blobIdBuffer == null) return null;

        final long blobId = getDatatypeCoder().decodeLong(blobIdBuffer);
        synchronized (((Synchronizable) getBlob()).getSynchronizationObject()) {
            try (FbBlob blobHandle = gdsHelper.openBlob(blobId, FBBlob.SEGMENTED)) {
                final int blobLength = (int) blobHandle.length();
                final int bufferLength = gdsHelper.getBlobBufferLength();
                final byte[] resultBuffer = new byte[blobLength];

                int offset = 0;

                while (offset < blobLength) {
                    final byte[] segmentBuffer = blobHandle.getSegment(bufferLength);

                    if (segmentBuffer.length == 0) {
                        // unexpected EOF
                        throw new TypeConversionException(BYTES_CONVERSION_ERROR);
                    }

                    System.arraycopy(segmentBuffer, 0, resultBuffer, offset, segmentBuffer.length);
                    offset += segmentBuffer.length;
                }

                return resultBuffer;
            }
        }
    }

    @Override
    public byte[] getCachedData() throws SQLException {
        if (isNull()) {
            return bytes;
        }
        return getBytesInternal();
    }

    @Override
    public FBFlushableField.CachedObject getCachedObject() throws SQLException {
        if (isNull())
            return new FBFlushableField.CachedObject(bytes, binaryStream, characterStream, length);
        final byte[] bytes = getBytesInternal();
        return new CachedObject(bytes, null, null, bytes.length);
    }

    @Override
    public void setCachedObject(FBFlushableField.CachedObject cachedObject) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        bytes = cachedObject.bytes;
        binaryStream = cachedObject.binaryStream;
        characterStream = cachedObject.characterStream;
        length = cachedObject.length;
    }

    @Override
    public String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (fieldDescriptor.getSubType() < 0)
            throw new TypeConversionException(STRING_CONVERSION_ERROR);

        Blob blob = getBlob();

        if (blob == null) return null;

        return getDatatypeCoder().decodeString(getBytes(), getDatatypeCoder().getEncodingFactory().getDefaultEncoding(),
                mappingPath);
    }

    //--- setXXX methods

    @Override
    protected void setCharacterStreamInternal(Reader in, long length) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            characterStream = in;
            this.length = length;
        }
    }

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            binaryStream = in;
            this.length = length;
        }
    }

    @Override
    public void flushCachedData() throws SQLException {
        if (binaryStream != null) {
            copyBinaryStream(binaryStream, length);
        } else if (characterStream != null) {
            copyCharacterStream(characterStream, length,
                    getDatatypeCoder().getEncodingFactory().getDefaultEncoding().getCharsetName());
        } else if (bytes != null) {
            copyBytes(bytes, (int) length);
        } else if (blob == null) {
            setNull();
        }

        this.characterStream = null;
        this.binaryStream = null;
        this.bytes = null;
        this.length = 0;
    }

    private void copyBinaryStream(InputStream in, long length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }

    private void copyCharacterStream(Reader in, long length, String encoding) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyCharacterStream(in, length, encoding);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }

    private void copyBytes(byte[] bytes, int length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyBytes(bytes, 0, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != null) {
            bytes = value;
            length = value.length;
        }
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setBytes(getDatatypeCoder().encodeString(value, getDatatypeCoder().getEncodingFactory().getDefaultEncoding(),
                mappingPath));
    }

    @Override
    public void setBlob(FBBlob blob) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        this.blob = blob;
    }

    @Override
    public void setClob(FBClob clob) throws SQLException {
        FBBlob blob = clob.getWrappedBlob();
        setBlob(blob);
    }

    @Override
    public void setNull() {
        super.setNull();
        try {
            if (blob != null) blob.free();
        } catch (SQLException e) {
            //ignore
        } finally {
            blob = null;
            binaryStream = null;
            characterStream = null;
            bytes = null;
            length = 0;
        }
    }
}
