/*
 SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
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
import java.util.function.IntSupplier;

import static org.firebirdsql.jaybird.util.ConditionalHelpers.firstNonNull;

/**
 * This is Blob-based implementation of {@link FBStringField}. It should be used
 * for fields declared in database as {@code BLOB SUB_TYPE 1}. This
 * implementation provides all conversion routines {@link FBStringField} has.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FBLongVarCharField extends FBStringField implements FBCloseableField, FBFlushableField, BlobField {

    // TODO Reduce duplication with FBBlobField, maybe make it wrap an FBBlobField?

    private FBBlob blob;
    private boolean blobExplicitNull;

    // Rather than hold cached data in the XSQLDAVar we will hold it in here.
    private long length;
    private byte[] bytes;
    private InputStream binaryStream;
    private Reader characterStream;
    private FBObjectListener.@NonNull BlobListener blobListener = FBObjectListener.NoActionBlobListener.instance();
    final FBBlob.@NonNull Config blobConfig;
    private @NonNull IntSupplier lengthRestriction = DEFAULT_LENGTH_RESTRICTION;

    @NullMarked
    FBLongVarCharField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
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
    public final void restrictLength(@Nullable IntSupplier maxLengthSupplier) {
        lengthRestriction = firstNonNull(maxLengthSupplier, DEFAULT_LENGTH_RESTRICTION);
    }

    /**
     * @return the maximum size of a value, {@code 0} or negative values mean no restriction
     * @see #restrictLength(IntSupplier)
     * @since 7
     */
    final int maxFieldSize() {
        return lengthRestriction.getAsInt();
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

    @Override
    public Blob getBlob() throws SQLException {
        final FirebirdBlob blob = getBlobInternal();
        return blob != null ? registerWithTransaction(blob.detach()) : null;
    }

    protected FirebirdBlob getBlobInternal() {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;

        return blob = new FBBlob(gdsHelper, getDatatypeCoder().decodeLong(bytes), blobListener, blobConfig);
    }

    @Override
    public Clob getClob() throws SQLException {
        final FBBlob blob = (FBBlob) getBlobInternal();
        if (blob == null) return null;
        return new FBClob(registerWithTransaction(blob.detach()));
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        final Blob blob = getBlobInternal();
        return blob != null ? blob.getBinaryStream() : null;
    }

    @Override
    public byte[] getBytes() throws SQLException {
        final FirebirdBlob blob = getBlobInternal();
        if (blob == null) return null;
        final int maxFieldSize = maxFieldSize();
        return maxFieldSize > 0 ? blob.getBytes(1, maxFieldSize) : blob.getBytes();
    }

    @Override
    public byte[] getCachedData() throws SQLException {
        if (isNull()) return bytes;
        return getBytes();
    }

    @Override
    public FBFlushableField.@NonNull CachedObject getCachedObject() throws SQLException {
        if (isNull()) {
            return new CachedObject(bytes, binaryStream, characterStream, length);
        }

        final byte[] bytes = getBytes();
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
        return applyTrimTrailing(getDatatypeCoder().decodeString(getBytes()));
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
    protected void setCharacterStreamInternal(Reader in, long length) {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            this.characterStream = in;
            this.length = length;
            blobExplicitNull = false;
        }
    }

    @Override
    public void setString(String value) throws SQLException {
        setBytes(getDatatypeCoder().encodeString(value));
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != null) {
            this.bytes = value;
            this.length = value.length;
            blobExplicitNull = false;
        }
    }

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) {
        // setNull() to reset field to empty state
        setNull();
        if (in != null) {
            this.binaryStream = in;
            this.length = length;
            blobExplicitNull = false;
        }
    }

    @Override
    public void flushCachedData() throws SQLException {
        if (binaryStream != null) {
            copyBinaryStream(this.binaryStream, this.length);
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
    
    private void copyBinaryStream(@NonNull InputStream in, long length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
    }

    private void copyCharacterStream(@NonNull Reader in, long length) throws SQLException {
        FBClob clob =  createClob();
        clob.copyCharacterStream(in, length);
        setFieldData(getDatatypeCoder().encodeLong(clob.getWrappedBlob().getBlobId()));
        blobExplicitNull = false;
    }
    
    private void copyBytes(byte @NonNull [] bytes, int length) throws SQLException {
        FBBlob blob = createBlob();
        blob.copyBytes(bytes, 0, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        blobExplicitNull = false;
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