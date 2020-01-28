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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.SyncObject;

import java.sql.SQLException;
import java.sql.Blob;
import java.io.*;

/**
 * This class represents a cached blob field.
 */
@SuppressWarnings("RedundantThrows")
public final class FBCachedBlob implements FirebirdBlob, Synchronizable {

    private static final byte[] BYTES_NULL_VALUE = null;
    private static final InputStream STREAM_NULL_VALUE = null;
    private static final byte[] FREED_MARKER = new byte[0];
    static final String BLOB_READ_ONLY = "Cached blob is read-only";

    private final Object syncObject = new SyncObject();

    private volatile byte[] blobData;

    /**
     * Create an instance using the cached data.
     *
     * @param data
     *            array of bytes containing the cached data.
     */
    public FBCachedBlob(byte[] data) {
        blobData = data;
    }

    @Override
    public FirebirdBlob detach() throws SQLException {
        checkClosed();
        return new FBCachedBlob(blobData);
    }

    /**
     * {@inheritDoc}
     * <p>
     * An instance of <code>FBCachedBlob</code> returns <code>false</code> always.
     * </p>
     */
    @Override
    public boolean isSegmented() throws SQLException {
        checkClosed();
        return false;
    }

    /**
     * Get the length of the cached blob field.
     *
     * @return length of the cached blob field or -1 if the field is null.
     */
    @Override
    public long length() throws SQLException {
        checkClosed();
        return blobData != null ? blobData.length : -1;
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Expected value of pos > 0, got " + pos,
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }
        if (length < 0) {
            throw new SQLException("Expected value of length >= 0, got " + length,
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }
        checkClosed();
        if (blobData == null) return BYTES_NULL_VALUE;

        // TODO What if pos or length are beyond blobData
        byte[] result = new byte[length];
        System.arraycopy(blobData, (int) pos - 1, result, 0, length);
        return result;
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
        checkClosed();
        if (blobData == null) return STREAM_NULL_VALUE;

        return new ByteArrayInputStream(blobData);
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
        throw new FBSQLException(BLOB_READ_ONLY);
    }

    /**
     * Set the contents of blob.
     *
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new FBSQLException(BLOB_READ_ONLY);
    }

    /**
     * Set the contents of blob as binary stream.
     *
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new FBSQLException(BLOB_READ_ONLY);
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
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public void free() throws SQLException {
        blobData = FREED_MARKER;
    }

    private void checkClosed() throws SQLException {
        if (blobData == FREED_MARKER) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_blobClosed).toFlatSQLException();
        }
    }
}
