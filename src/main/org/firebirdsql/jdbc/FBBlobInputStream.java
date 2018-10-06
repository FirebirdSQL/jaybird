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

import org.firebirdsql.gds.ng.FbBlob;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * An input stream for reading directly from a FBBlob instance.
 */
public final class FBBlobInputStream extends InputStream implements FirebirdBlob.BlobInputStream {

    /**
     * Maximum blob segment size, see IB 6 Data Definition Guide, page 78 ("BLOB segment length")
     */
    private static final int READ_FULLY_BUFFER_SIZE = 32 * 1024;
    private static final byte[] EMPTY_BUFFER = new byte[0];

    private byte[] buffer = EMPTY_BUFFER;
    private FbBlob blobHandle;
    private int pos = 0;

    private boolean closed;

    private final FBBlob owner;

    FBBlobInputStream(FBBlob owner) throws SQLException {
        if (owner.isNew())
            throw new FBSQLException("You can't read a new blob");

        this.owner = owner;
        closed = false;

        synchronized (owner.getSynchronizationObject()) {
            blobHandle = owner.getGdsHelper().openBlob(owner.getBlobId(), FBBlob.SEGMENTED);
        }
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
        synchronized (owner.getSynchronizationObject()) {
            checkClosed();
            try {
                blobHandle.seek(position, seekMode);
            } catch (SQLException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
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

    @Override
    public int available() throws IOException {
        assert buffer != null : "Buffer should never be null";
        return buffer.length - pos;
    }

    /**
     * Checks the available buffer size, retrieving a segment from the server if necessary.
     *
     * @return The number of bytes available in the buffer, or <code>-1</code> if the end of the stream is reached.
     * @throws IOException if an I/O error occurs, or if the stream has been closed.
     */
    private int checkBuffer() throws IOException {
        assert buffer != null : "Buffer should never be null";
        synchronized (owner.getSynchronizationObject()) {
            checkClosed();
            if (pos < buffer.length) {
                return buffer.length - pos;
            }
            if (blobHandle.isEof()) {
                return -1;
            }

            try {
                buffer = blobHandle.getSegment(owner.getBufferLength());
                pos = 0;
                return buffer.length != 0 ? buffer.length : -1;
            } catch (SQLException ge) {
                throw new IOException("Blob read problem: " + ge.toString(), ge);
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (checkBuffer() == -1) {
            return -1;
        }
        return buffer[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        final int toCopy = Math.min(checkBuffer(), len);
        if (toCopy == -1) {
            return -1;
        }
        System.arraycopy(buffer, pos, b, off, toCopy);
        pos += toCopy;
        return toCopy;
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        int counter = 0;
        int pos = off;
        byte[] buffer = new byte[Math.min(READ_FULLY_BUFFER_SIZE, len)];

        int toRead = len;

        while (toRead > 0 && (counter = read(buffer, 0, Math.min(toRead, buffer.length))) != -1) {
            System.arraycopy(buffer, 0, b, pos, counter);
            pos += counter;
            toRead -= counter;
        }

        if (counter == -1)
            throw new EOFException();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        synchronized (owner.getSynchronizationObject()) {
            if (blobHandle != null) {
                try {
                    blobHandle.close();
                    owner.notifyClosed(this);
                } catch (SQLException ge) {
                    throw new IOException("couldn't close blob: " + ge);
                }
                blobHandle = null;
                closed = true;
                buffer = EMPTY_BUFFER;
                pos = 0;
            }
        }
    }

    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Input stream is already closed.");
    }
}