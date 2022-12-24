/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBObjectListener;
import org.firebirdsql.jdbc.FirebirdBlob;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Field implementation for blobs other than {@code BLOB SUB_TYPE TEXT}.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBBlobField extends FBField implements FBCloseableField, FBFlushableField, BlobListenableField {

    protected FirebirdBlob blob;
    private boolean blobExplicitNull;
    private long length;
    private InputStream binaryStream;
    private Reader characterStream;
    private byte[] bytes;
    private FBObjectListener.BlobListener blobListener = FBObjectListener.NoActionBlobListener.instance();
    final FBBlob.Config blobConfig;

    FBBlobField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType, GDSHelper gdsHelper)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        this.gdsHelper = gdsHelper;
        // NOTE: If gdsHelper is really null, it will fail at a later point when attempting to open the blob
        // It should only be null for certain types of tests
        blobConfig = gdsHelper != null
                ? FBBlob.createConfig(fieldDescriptor, gdsHelper.getConnectionProperties())
                : FBBlob.createConfig(fieldDescriptor.getSubType(), PropertyConstants.DEFAULT_STREAM_BLOBS,
                        PropertyConstants.DEFAULT_BLOB_BUFFER_SIZE, fieldDescriptor.getDatatypeCoder());
    }

    @Override
    public void setBlobListener(FBObjectListener.BlobListener blobListener) {
        this.blobListener = blobListener;
    }

    @Override
    public void close() throws SQLException {
        try {
            if (blob != null) blob.free();
        } finally {
            // forget this blob instance, resource waste but simplifies our life. BLOB handle will be
            // released by a server automatically later
            blob = null;
            blobExplicitNull = false;
            bytes = null;
            binaryStream = null;
            characterStream = null;
            length = 0;
            blobListener = null;
        }
    }

    protected FirebirdBlob getBlobInternal() {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;

        blob = new FBBlob(gdsHelper, getDatatypeCoder().decodeLong(bytes), blobListener, blobConfig);
        return blob;
    }

    @Override
    public Object getObject() throws SQLException {
        if (requiredType == Types.BLOB) {
            return getBlob();
        }
        return getBytes();
    }

    @Override
    public Blob getBlob() throws SQLException {
        FirebirdBlob blob = getBlobInternal();
        // Need to use detached blob to ensure the blob is usable after resultSet.next()
        return blob != null ? registerWithTransaction(blob.detach()) : null;
    }

    @Override
    public Clob getClob() throws SQLException {
        FBBlob blob = (FBBlob) getBlob();
        if (blob == null) return null;
        return new FBClob(blob);
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlobInternal();
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
        try (LockCloseable ignored = gdsHelper.withLock();
             FbBlob blobHandle = gdsHelper.openBlob(blobId, blobConfig)) {
            final int blobLength = (int) blobHandle.length();
            final int bufferLength = blobConfig.blobBufferSize();
            final byte[] resultBuffer = new byte[blobLength];

            int offset = 0;

            while (offset < blobLength) {
                final byte[] segmentBuffer = blobHandle.getSegment(bufferLength);

                if (segmentBuffer.length == 0) {
                    throw invalidGetConversion("byte[]", "unexpected EOF");
                }

                System.arraycopy(segmentBuffer, 0, resultBuffer, offset, segmentBuffer.length);
                offset += segmentBuffer.length;
            }

            return resultBuffer;
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
        blobExplicitNull = bytes == null && binaryStream == null && characterStream == null;
    }

    @Override
    public String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (fieldDescriptor.getSubType() < 0) {
            throw invalidGetConversion(String.class, String.format("BLOB SUB_TYPE %d", fieldDescriptor.getSubType()));
        }

        Blob blob = getBlobInternal();

        if (blob == null) return null;

        return getDatatypeCoder().decodeString(getBytes());
    }

    //--- setXXX methods

    @Override
    protected void setCharacterStreamInternal(Reader in, long length) {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            characterStream = in;
            this.length = length;
            blobExplicitNull = false;
        }
    }

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            binaryStream = in;
            this.length = length;
            blobExplicitNull = false;
        }
    }

    @Override
    public void flushCachedData() throws SQLException {
        if (binaryStream != null) {
            copyBinaryStream(binaryStream, length);
        } else if (characterStream != null) {
            copyCharacterStream(characterStream, length);
        } else if (bytes != null) {
            copyBytes(bytes, (int) length);
        } else if (blob == null && blobExplicitNull) {
            setNull();
        }

        this.characterStream = null;
        this.binaryStream = null;
        this.bytes = null;
        this.length = 0;
    }

    private void copyBinaryStream(InputStream in, long length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    private void copyCharacterStream(Reader in, long length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyCharacterStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    private void copyBytes(byte[] bytes, int length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyBytes(bytes, 0, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != null) {
            bytes = value;
            length = value.length;
            blobExplicitNull = false;
        }
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setBytes(getDatatypeCoder().encodeString(value));
    }

    @Override
    public void setBlob(FBBlob blob) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (blob != null) {
            setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
            this.blob = blob;
            blobExplicitNull = false;
        }
    }

    @Override
    public void setBlob(Blob blob) throws SQLException {
        if (blob == null) {
            setNull();
        } else if (blob instanceof FBBlob) {
            setBlob((FBBlob) blob);
        } else {
            FBBlob fbb = createBlob();
            fbb.copyStream(blob.getBinaryStream());
            setBlob(fbb);
        }
    }

    @Override
    FBBlob createBlob() {
        return new FBBlob(gdsHelper, blobListener, blobConfig);
    }

    @Override
    public void setClob(FBClob clob) throws SQLException {
        setBlob(clob != null ? clob.getWrappedBlob() : null);
    }

    @Override
    public void setClob(Clob clob) throws SQLException {
        if (clob == null) {
            setNull();
        } else if (clob instanceof FBClob) {
            setClob((FBClob) clob);
        } else {
            FBClob fbc = createClob();
            fbc.copyCharacterStream(clob.getCharacterStream());
            setClob(fbc);
        }
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
            blobExplicitNull = true;
            binaryStream = null;
            characterStream = null;
            bytes = null;
            length = 0;
        }
    }

    private <T extends FirebirdBlob> T registerWithTransaction(T blob) {
        if (blob instanceof TransactionListener) {
            FbTransaction currentTransaction = gdsHelper.getCurrentTransaction();
            if (currentTransaction != null) {
                currentTransaction.addWeakTransactionListener((TransactionListener) blob);
            }
        }
        return blob;
    }
}
