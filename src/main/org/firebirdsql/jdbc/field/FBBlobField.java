/*
 SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002 Mark O'Donohue
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2020 Vasiliy Yashkov
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBObjectListener;
import org.firebirdsql.jdbc.FirebirdBlob;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Field implementation for blobs other than {@code BLOB SUB_TYPE TEXT}.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
class FBBlobField extends FBField implements FBCloseableField, FBFlushableField, BlobListenableField {

    FirebirdBlob blob;
    private boolean blobExplicitNull;
    private long length;
    private InputStream binaryStream;
    private Reader characterStream;
    private byte[] bytes;
    private FBObjectListener.@NonNull BlobListener blobListener = FBObjectListener.NoActionBlobListener.instance();
    final FBBlob.@NonNull Config blobConfig;

    @NullMarked
    FBBlobField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            @Nullable GDSHelper gdsHelper) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        setConnection(gdsHelper);
        // NOTE: If gdsHelper is really null, it will fail at a later point when attempting to open the blob
        // It should only be null for certain types of tests
        blobConfig = gdsHelper != null
                ? FBBlob.createConfig(fieldDescriptor, gdsHelper.getConnectionProperties())
                : FBBlob.createConfig(fieldDescriptor.getSubType(), PropertyConstants.DEFAULT_STREAM_BLOBS,
                        PropertyConstants.DEFAULT_BLOB_BUFFER_SIZE, fieldDescriptor.getDatatypeCoder());
    }

    @Override
    @NullMarked
    public void setBlobListener(FBObjectListener.BlobListener blobListener) {
        this.blobListener = blobListener;
    }

    @Override
    public void close() throws SQLException {
        try {
            if (blob != null) blob.free();
        } finally {
            blob = null;
            blobExplicitNull = false;
            bytes = null;
            binaryStream = null;
            characterStream = null;
            length = 0;
            blobListener = FBObjectListener.NoActionBlobListener.instance();
        }
    }

    FirebirdBlob getBlobInternal() {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;

        return blob = new FBBlob(gdsHelper, getDatatypeCoder().decodeLong(bytes), blobListener, blobConfig);
    }

    @Override
    public Object getObject() throws SQLException {
        return requiredType != Types.BLOB ? getBytes() : getBlob();
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
        return blob != null ? new FBClob(blob) : null;
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlobInternal();
        return blob != null ? blob.getBinaryStream() : null;
    }

    @Override
    public byte[] getBytes() throws SQLException {
        return getBytesInternal();
    }

    public byte[] getBytesInternal() throws SQLException {
        FirebirdBlob blob = getBlobInternal();
        return blob != null ? blob.getBytes() : null;
    }

    @Override
    public byte[] getCachedData() throws SQLException {
        if (isNull()) {
            return bytes;
        }
        return getBytesInternal();
    }

    @Override
    public FBFlushableField.@NonNull CachedObject getCachedObject() throws SQLException {
        if (isNull()) {
            return new CachedObject(bytes, binaryStream, characterStream, length);
        }
        final byte[] bytes = getBytesInternal();
        return new CachedObject(bytes, null, null, bytes.length);
    }

    @Override
    public void setCachedObject(FBFlushableField.@NonNull CachedObject cachedObject) {
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

    private void copyBinaryStream(@NonNull InputStream in, long length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    private void copyCharacterStream(@NonNull Reader in, long length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyCharacterStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    private void copyBytes(byte @NonNull [] bytes, int length) throws SQLException {
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
        if (blob instanceof FBBlob || blob == null) {
            setBlob((FBBlob) blob);
        } else {
            FBBlob fbb = createBlob();
            fbb.copyStream(blob.getBinaryStream());
            setBlob(fbb);
        }
    }

    @Override
    @NonNull FBBlob createBlob() {
        return new FBBlob(gdsHelper, blobListener, blobConfig);
    }

    @Override
    public void setClob(FBClob clob) throws SQLException {
        setBlob(clob != null ? clob.getWrappedBlob() : null);
    }

    @Override
    public void setClob(Clob clob) throws SQLException {
        if (clob instanceof FBClob || clob == null) {
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

    @NullMarked
    private <T extends FirebirdBlob> T registerWithTransaction(T blob) {
        if (blob instanceof TransactionListener transactionListener) {
            FbTransaction currentTransaction = gdsHelper.getCurrentTransaction();
            if (currentTransaction != null) {
                currentTransaction.addWeakTransactionListener(transactionListener);
            }
        }
        return blob;
    }
    
}
