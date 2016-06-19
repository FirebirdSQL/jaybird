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

import java.sql.SQLException;
import java.sql.Blob;
import java.io.*;

/**
 * This class represents a cached blob field.
 */
public class FBCachedBlob implements FirebirdBlob, Synchronizable {

    private static final byte[] BYTES_NULL_VALUE = null;

    private static final InputStream STREAM_NULL_VALUE = null;

    private final Object syncObject = new Object();

    private byte[] blobData;

    public FirebirdBlob detach() throws SQLException {
        return this;
    }

    public boolean isSegmented() throws SQLException {
        return false;
    }
    
    /**
     * Create an instance using the cached data.
     * 
     * @param data
     *            array of bytes containing the cached data.
     */
    public FBCachedBlob(byte[] data) {
        blobData = data;
    }

    /**
     * Get the length of the cached blob field.
     * 
     * @return length of the cached blob field or -1 if the field is null.
     */
    public long length() throws SQLException {
        if (blobData == null) return -1;

        return blobData.length;
    }

    /**
     * Get part of the blob field.
     * 
     * @param pos
     *            starting position to copy.
     * @param length
     *            amount of bytes to copy.
     */
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (blobData == null) return BYTES_NULL_VALUE;

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
    public long position(byte[] pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Find the first entry of the specified pattern.
     * 
     * @throws SQLException
     *             always, not yet implemented.
     */
    public long position(Blob pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Get contents of blob as binary stream.
     */
    public InputStream getBinaryStream() throws SQLException {
        if (blobData == null) return STREAM_NULL_VALUE;

        return new ByteArrayInputStream(blobData);
    }

    /**
     * Set contents of the blob.
     * 
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    public int setBytes(long l, byte abyte0[]) throws SQLException {
        throw new FBSQLException("Blob in auto-commit mode is read-only.");
    }

    /**
     * Set the contents of blob.
     * 
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    public int setBytes(long l, byte abyte0[], int i, int j)
            throws SQLException {
        throw new FBSQLException("Blob in auto-commit mode is read-only.");
    }

    /**
     * Set the contents of blob as binary stream.
     * 
     * @throws SQLException
     *             always, set methods are not relevant in cached state.
     */
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new FBSQLException("Blob in auto-commit mode is read-only.");
    }

    /**
     * Truncate the blob to specified length.
     * 
     * @throws SQLException
     *             always, truncate is not relevant in cached state.
     */
    public void truncate(long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public final Object getSynchronizationObject() {
        return syncObject;
    }

    public void free() throws SQLException {
        this.blobData = null;
    }

    public InputStream getBinaryStream(long pos, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }
    
    
}
