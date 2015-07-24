/*
 * $Id$
 *
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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.ng.FbBlob;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * {@link java.io.OutputStream} for writing Firebird blobs.
 */
public final class FBBlobOutputStream extends OutputStream implements FirebirdBlob.BlobOutputStream {

    private static final byte[] EMPTY_BUFFER = new byte[0];

    private FbBlob blobHandle;
    private final FBBlob owner;
    private byte[] buf;
    private int count;

    FBBlobOutputStream(FBBlob owner) throws SQLException {
        this.owner = owner;
        buf = new byte[owner.getBufferLength()];

        synchronized (owner.getSynchronizationObject()) {
            DatabaseParameterBuffer dpb = owner.getGdsHelper().getDatabaseParameterBuffer();
            boolean useStreamBlobs = dpb.hasArgument(DatabaseParameterBufferExtension.USE_STREAM_BLOBS);
            blobHandle = owner.getGdsHelper().createBlob(!useStreamBlobs);
        }

        if (owner.isNew()) {
            owner.setBlobId(blobHandle.getBlobId());
        }
    }

    @Override
    public FirebirdBlob getBlob() {
        return owner;
    }

    @Override
    public long length() throws IOException {
        synchronized (owner.getSynchronizationObject()) {
            checkClosed();
            try {
                return blobHandle.length();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Writes are buffered up to the buffer length of the blob (optionally specified by the connection
     * property <code>blobBufferSize</code>).
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
     * Writes a byte array directly to the blob.
     *
     * @param buf
     *         Byte array to write
     * @throws SQLException
     *         For errors writing to the blob
     */
    private void writeSegment(byte[] buf) throws SQLException {
        synchronized (owner.getSynchronizationObject()) {
            blobHandle.putSegment(buf);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Writes are buffered up to the buffer length of the blob (optionally specified by the connection
     * property <code>blobBufferSize</code>).
     * </p>
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        // array doesn't fit in remainder of buffer,
        // or full array written is equal to buffer size: flush and write immediately (saves copying)
        if (off == 0 && len == buf.length || len > buf.length - count) {
            flush();
            writeInternal(b, off, len);
        } else {
            System.arraycopy(b, off, buf, count, len);
            count += len;

            if (count == buf.length) flush();
        }
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
     *         If an I/O error occurs.
     */
    private void writeInternal(byte[] b, int off, int len) throws IOException {
        try {
            if (off == 0 && len == b.length && len <= owner.getBufferLength()) {
                /*
                 * If we are just writing the entire byte array, we need to
                 * do nothing but just write it over
                 */
                writeSegment(b);
            } else {
                /*
                 * In this case, we need to chunk it over since <code>putBlobSegment</code>
                 * cannot currently support length and offset.
                 */
                int chunk = Math.min(owner.getBufferLength(), len);
                byte[] buffer = new byte[chunk];
                while (len > 0) {
                    chunk = Math.min(len, chunk);

                    // this allows us to reuse the buffer if its size has not changed
                    if (chunk != buffer.length) {
                        buffer = new byte[chunk];
                    }
                    System.arraycopy(b, off, buffer, 0, chunk);
                    writeSegment(buffer);

                    len -= chunk;
                    off += chunk;
                }
            }
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
        flush();

        try {
            synchronized (owner.getSynchronizationObject()) {
                blobHandle.close();
            }
            owner.setBlobId(blobHandle.getBlobId());
        } catch (SQLException ge) {
            throw new IOException("could not close blob: " + ge.getMessage(), ge);
        } finally {
            blobHandle = null;
            buf = EMPTY_BUFFER;
            count = 0;
        }
    }

    /**
     * @throws IOException When this output stream has been closed.
     */
    private void checkClosed() throws IOException {
        if (blobHandle == null || !blobHandle.isOpen()) {
            throw new IOException("Output stream is already closed.");
        }
    }
}