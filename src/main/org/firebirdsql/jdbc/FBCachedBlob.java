/*
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003-2007 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.sql.Blob;
import java.io.*;

import static java.util.Objects.requireNonNull;

/**
 * This class represents a cached blob field.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link java.sql.Blob} and {@link FirebirdBlob} interfaces.
 * </p>
 */
@InternalApi
@NullMarked
public final class FBCachedBlob implements FirebirdBlob {

    // NOTE: Do not assign ByteArrayHelper.empty(), as this must be a unique instance
    private static final byte[] FREED_MARKER = new byte[0];
    static final String BLOB_READ_ONLY = "Cached blob is read-only";

    @SuppressWarnings("java:S3077")
    private volatile byte[] blobData;

    /**
     * Create an instance using the cached data.
     *
     * @param data
     *         array of bytes containing the cached data
     */
    public FBCachedBlob(byte[] data) {
        blobData = requireNonNull(data, "data");
    }

    @Override
    public FirebirdBlob detach() throws SQLException {
        return new FBCachedBlob(requireBlobData());
    }

    /**
     * {@inheritDoc}
     * <p>
     * An instance of <code>FBCachedBlob</code> returns <code>false</code> always.
     * </p>
     */
    @Override
    public boolean isSegmented() throws SQLException {
        checkOpen();
        return false;
    }

    /**
     * Get the length of the cached blob field.
     *
     * @return length of the cached blob field
     */
    @Override
    public long length() throws SQLException {
        return requireBlobData().length;
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Expected value of pos > 0, got " + pos,
                    SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        } else if (length < 0) {
            throw new SQLException("Expected value of length >= 0, got " + length,
                    SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        }
        byte[] blobData = requireBlobData();

        if (pos > blobData.length) return ByteArrayHelper.emptyByteArray();
        length = (int) Math.min(length, blobData.length - pos + 1L);

        byte[] result = new byte[length];
        System.arraycopy(blobData, (int) pos - 1, result, 0, length);
        return result;
    }

    @Override
    public byte[] getBytes() throws SQLException {
        return requireBlobData().clone();
    }

    /**
     * Find the first entry of the specified pattern.
     *
     * @throws SQLException
     *             always, not yet implemented.
     */
    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException("Method Method position(byte[], long) is not supported");
    }

    /**
     * Find the first entry of the specified pattern.
     *
     * @throws SQLException
     *             always, not yet implemented.
     */
    @Override
    public long position(Blob pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException("Method position(Blob, long) is not supported");
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        return new ByteArrayInputStream(requireBlobData());
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        throw new FBDriverNotCapableException("Method getBinaryStream(long, long) is not supported");
    }

    /**
     * Set contents of the blob.
     *
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new SQLException(BLOB_READ_ONLY, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     * Set the contents of blob.
     *
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new SQLException(BLOB_READ_ONLY, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     * Set the contents of blob as binary stream.
     *
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new SQLException(BLOB_READ_ONLY, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     * Truncate the blob to specified length.
     *
     * @throws SQLException
     *             always, truncate is not relevant in cached state.
     */
    @Override
    public void truncate(long length) throws SQLException {
        throw new FBDriverNotCapableException("Method truncate(long) is not supported");
    }

    @Override
    public void free() throws SQLException {
        blobData = FREED_MARKER;
    }

    /**
     * Checks if the blob is open (not freed).
     * <p>
     * If a method needs access to the blob data, use {@link #requireBlobData()} instead.
     * </p>
     *
     * @see #requireBlobData()
     */
    private void checkOpen() throws SQLException {
        if (blobData == FREED_MARKER) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_blobClosed);
        }
    }

    private byte[] requireBlobData() throws SQLException {
        byte[] blobData = this.blobData;
        if (blobData == FREED_MARKER) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_blobClosed);
        }
        return blobData;
    }
}
