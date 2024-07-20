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
import org.firebirdsql.util.InternalApi;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Objects;

/**
 * An input stream for reading directly from a FBBlob instance.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link FirebirdBlob.BlobInputStream} interface.
 * </p>
 */
@InternalApi
public final class FBBlobInputStream extends InputStream implements FirebirdBlob.BlobInputStream {

    private byte[] buffer = ByteArrayHelper.emptyByteArray();
    private FbBlob blobHandle;
    private int pos;
    private int lim;
    private boolean closed;

    private final FBBlob owner;

    FBBlobInputStream(FBBlob owner) throws SQLException {
        if (owner.isNew()) {
            throw new SQLException("Cannot read a new blob", SQLStateConstants.SQL_STATE_LOCATOR_EXCEPTION);
        }
        this.owner = owner;
        blobHandle = owner.openBlob();
    }

    @Override
    public FirebirdBlob getBlob() {
        return owner;
    }

    @Override
    public void seek(int position) throws IOException {
        seek(position, FbBlob.SeekMode.ABSOLUTE);
    }

    @Override
    public void seek(int position, int seekMode) throws IOException {
        seek(position, FbBlob.SeekMode.getById(seekMode));
    }

    public void seek(int position, FbBlob.SeekMode seekMode) throws IOException {
        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            blobHandle.seek(position, seekMode);
        } catch (SQLException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
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

    @Override
    public int available() throws IOException {
        return lim - pos;
    }

    /**
     * Checks the available buffer size, retrieving a segment from the server if necessary.
     *
     * @return the number of bytes available in the buffer, or {@code -1} if the end of the stream is reached.
     * @throws IOException
     *         if an I/O error occurs, or if the stream has been closed.
     */
    private int checkBuffer() throws IOException {
        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            if (pos < lim) {
                return lim - pos;
            }
            if (blobHandle.isEof()) return -1;

            byte[] buffer = requireBuffer();
            lim = blobHandle.get(buffer, 0, buffer.length, 0.9f);
            pos = 0;
            return lim > 0 ? lim : -1;
        } catch (SQLException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw blobReadProblem(e);
        }
    }

    private static IOException blobReadProblem(Exception e) {
        return new IOException("Blob read problem: " + e, e);
    }

    /**
     * @return buffer with length equal to {@code owner.getBufferLength()}.
     */
    private byte[] requireBuffer() {
        byte[] buffer = this.buffer;
        if (buffer.length > 0) {
            return buffer;
        }
        return this.buffer = new byte[owner.getBufferLength()];
    }

    @Override
    public int read() throws IOException {
        if (checkBuffer() == -1) {
            return -1;
        }
        return buffer[pos++] & 0xFF;
    }

    @Override
    public int read(final byte[] b, int off, final int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) return 0;

        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            // Optimization: for small lengths use buffer, otherwise only check if we currently have data in buffer
            final int smallBufferLimit = Math.min(owner.getBufferLength(), blobHandle.getMaximumSegmentSize()) / 2;
            final int avail = len <= smallBufferLimit ? checkBuffer() : available();
            if (avail == -1) {
                return -1;
            }
            int count = Math.min(avail, len);
            if (count > 0) {
                System.arraycopy(buffer, this.pos, b, off, count);
                this.pos += count;
                if (len - count < smallBufferLimit) {
                    // Remaining bytes are small, better if this method is called again (which will use a buffer)
                    return count;
                }
            }

            if (count < len) {
                count += blobHandle.get(b, off + count, len - count, 0.9f);
            }
            // When we haven't read anything, report end-of-blob
            return count == 0 ? -1 : count;
        } catch (SQLException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw blobReadProblem(e);
        }
    }

    @Override
    public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) return 0;

        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            final int count = Math.min(available(), len);
            if (count > 0) {
                System.arraycopy(buffer, pos, b, off, count);
                pos += count;
                if (count == len) return len;
            }
            return count + blobHandle.get(b, off + count, len - count);
        } catch (SQLException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw blobReadProblem(e);
        }
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int read = readNBytes(b, off, len);
        if (read != len) {
            throw new EOFException(
                    "End-of-blob reached after reading %d bytes, required %d bytes".formatted(read, len));
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public long transferTo(final OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");

        try (LockCloseable ignored = owner.withLock()) {
            checkClosed();
            int read = checkBuffer();
            if (read == -1) return 0;
            final byte[] buffer = requireBuffer();
            if (read != 0) {
                out.write(buffer, pos, read);
                pos = lim = 0;
            }
            try {
                long transferred = read;
                while (!blobHandle.isEof()) {
                    read = blobHandle.get(buffer, 0, buffer.length, 0.9f);
                    out.write(buffer, 0, read);
                    transferred += read;
                }
                return transferred;
            } catch (SQLException e) {
                if (e.getCause() instanceof IOException ioe) {
                    throw ioe;
                }
                throw blobReadProblem(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try (LockCloseable ignored = owner.withLock()) {
            if (blobHandle == null) {
                return;
            }
            try {
                blobHandle.close();
                owner.notifyClosed(this);
            } catch (SQLException e) {
                throw new IOException("Couldn't close blob: " + e, e);
            } finally {
                blobHandle = null;
                closed = true;
                buffer = ByteArrayHelper.emptyByteArray();
                pos = 0;
                lim = 0;
            }
        }
    }

    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Input stream is already closed.");
    }
}