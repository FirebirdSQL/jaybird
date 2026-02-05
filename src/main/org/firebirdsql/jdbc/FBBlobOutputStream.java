// SPDX-FileCopyrightText: Copyright 2007 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link java.io.OutputStream} for writing Firebird blobs.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link FirebirdBlob.BlobOutputStream} interface.
 * </p>
 */
@InternalApi
@NullMarked
public final class FBBlobOutputStream extends OutputStream implements FirebirdBlob.BlobOutputStream {

    private @Nullable FbBlob blobHandle;
    private final FBBlob owner;
    private byte[] buf;
    private int count;

    FBBlobOutputStream(FBBlob owner) throws SQLException {
        this.owner = owner;
        buf = new byte[owner.getBufferLength()];

        try (var ignored = owner.withLock()) {
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
    @SuppressWarnings("resource")
    public long length() throws IOException {
        try (var ignored = withLock()) {
            return requireOpenBlobHandle().length();
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
    @SuppressWarnings("resource")
    public void write(int b) throws IOException {
        try (var ignored = withLock()) {
            requireOpenBlobHandle();
            if (count >= buf.length) flushNoLock();

            buf[count++] = (byte) b;

            if (count == buf.length) flushNoLock();
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
    @SuppressWarnings("resource")
    public void write(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) return;

        try (var ignored = withLock()) {
            requireOpenBlobHandle();
            int avail = buf.length - count;
            if (avail >= len && len != buf.length) {
                // Value fits into buffer, but avoid copy overhead if buf is empty and len is equal to buffer size
                copyToBuf(b, off, len);
            } else if (len <= buf.length / 2) {
                // small size, use buffer
                // fill and flush current buffer
                if (avail > 0) {
                    copyToBuf(b, off, avail);
                } else {
                    flushNoLock();
                }
                // buffer remaining bytes
                copyToBuf(b, off + avail, len - avail);
            } else {
                // large size, write directly
                flushNoLock();
                writeInternal(b, off, len);
            }
        }
    }

    private void copyToBuf(byte[] b, int off, int len) throws IOException {
        System.arraycopy(b, off, buf, count, len);
        count += len;
        if (count == buf.length) flushNoLock();
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
    @SuppressWarnings("resource")
    private void writeInternal(byte[] b, int off, int len) throws IOException {
        try {
            requireOpenBlobHandle().put(b, off, len);
        } catch (SQLException ge) {
            throw new IOException("Problem writing to FBBlobOutputStream: " + ge.getMessage(), ge);
        }
    }

    @Override
    public void flush() throws IOException {
        try (var ignored = withLock()) {
            flushNoLock();
        }
    }

    /**
     * Implementation of {@link #flush()} <em>without</em> lock.
     * <p>
     * In general, it should only be used if the caller is already holding the lock of {@link #withLock()}.
     * </p>
     */
    private void flushNoLock() throws IOException {
        if (count <= 0) return;
        writeInternal(buf, 0, count);
        count = 0;
    }

    @Override
    public void close() throws IOException {
        try (var ignored = withLock()) {
            FbBlob blobHandle = this.blobHandle;
            if (blobHandle == null) return;
            flushNoLock();
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

    private FbBlob requireOpenBlobHandle() throws IOException {
        FbBlob blobHandle = this.blobHandle;
        if (blobHandle == null || !blobHandle.isOpen()) {
            throw new IOException("Output stream is already closed.");
        }
        return blobHandle;
    }

    private LockCloseable withLock() {
        return owner.withLock();
    }

}