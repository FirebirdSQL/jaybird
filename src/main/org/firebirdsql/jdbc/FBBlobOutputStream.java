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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.ByteArrayHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link java.io.OutputStream} for writing Firebird blobs.
 */
public final class FBBlobOutputStream extends OutputStream implements FirebirdBlob.BlobOutputStream {

    private FbBlob blobHandle;
    private final FBBlob owner;
    private byte[] buf;
    private int count;

    FBBlobOutputStream(FBBlob owner) throws SQLException {
        this.owner = owner;
        buf = new byte[owner.getBufferLength()];

        try (LockCloseable ignored = owner.withLock()) {
            blobHandle = owner.createBlob();
            if (owner.isNew()) {
                owner.setBlobId(blobHandle.getBlobId());
            }
        }
    }

    @Override
    public FirebirdBlob getBlob() {
        return owner;
    }

    @Override
    public long length() throws IOException {
        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            return blobHandle.length();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Writes are buffered up to the buffer length of the blob (optionally specified by the connection
     * property {@code blobBufferSize}).
     * </p>
     */
    @Override
    public void write(int b) throws IOException {
        checkClosed();
        if (count >= buf.length) flush();

        buf[count++] = (byte) b;

        if (count == buf.length) flush();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Writes are buffered up to the buffer length of the blob (optionally specified by the connection
     * property {@code blobBufferSize}).
     * </p>
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) return;

        int avail = buf.length - count;
        if (avail >= len && len != buf.length) {
            // Value fits into buffer, but avoid copy overhead if buf is empty and len is equal to buffer size
            copyToBuf(b, off, len);
        } else if (len <= buf.length / 2) {
            // small size, use buffer
            // fill and flush current buffer
            copyToBuf(b, off, avail);
            // buffer remaining bytes
            copyToBuf(b, off + avail, len - avail);
        } else {
            // large size, write directly
            flush();
            writeInternal(b, off, len);
        }
    }

    private void copyToBuf(byte[] b, int off, int len) throws IOException {
        System.arraycopy(b, off, buf, count, len);
        count += len;
        if (count == buf.length) flush();
    }

    /**
     * Performs unbuffered writes to the blob.
     *
     * @param b
     *         byte array to write
     * @param off
     *         offset to start
     * @param len
     *         length to write
     * @throws IOException
     *         if an I/O error occurs
     */
    private void writeInternal(byte[] b, int off, int len) throws IOException {
        try {
            blobHandle.put(b, off, len);
        } catch (SQLException ge) {
            throw new IOException("Problem writing to FBBlobOutputStream: " + ge.getMessage(), ge);
        }
    }

    @Override
    public void flush() throws IOException {
        if (count > 0) {
            writeInternal(buf, 0, count);
            count = 0;
        }
    }

    @Override
    public void close() throws IOException {
        if (blobHandle == null) return;
        try (LockCloseable ignored = owner.withLock()) {
            flush();
            blobHandle.close();
            owner.setBlobId(blobHandle.getBlobId());
        } catch (SQLException ge) {
            throw new IOException("could not close blob: " + ge.getMessage(), ge);
        } finally {
            blobHandle = null;
            buf = ByteArrayHelper.emptyByteArray();
            count = 0;
        }
    }

    /**
     * @throws IOException
     *         when this output stream has been closed
     */
    private void checkClosed() throws IOException {
        if (blobHandle == null || !blobHandle.isOpen()) {
            throw new IOException("Output stream is already closed.");
        }
    }
}