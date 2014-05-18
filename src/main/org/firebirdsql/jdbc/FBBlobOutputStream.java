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
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public final class FBBlobOutputStream extends OutputStream implements FirebirdBlob.BlobOutputStream {

    private IscBlobHandle blobHandle;
    private final FBBlob owner;

    FBBlobOutputStream(FBBlob owner) throws SQLException {
        this.owner = owner;

        synchronized (owner.getSynchronizationObject()) {
            try {
                DatabaseParameterBuffer dpb = owner.getGdsHelper().getDatabaseParameterBuffer();
                boolean useStreamBlobs = dpb.hasArgument(DatabaseParameterBufferExtension.USE_STREAM_BLOBS);
                blobHandle = owner.getGdsHelper().createBlob(!useStreamBlobs);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }

        if (owner.isNew()) {
            owner.setBlobId(blobHandle.getBlobId());
        }
    }

    public long length() throws IOException {
        synchronized (owner.getSynchronizationObject()) {
            try {
                final byte[] info = owner.getGdsHelper().getBlobInfo(blobHandle, FBBlob.BLOB_LENGTH_REQUEST, 20);
                return owner.interpretLength(info, 0);
            } catch (GDSException ex) {
                throw new IOException(ex.getMessage(), ex);
            } catch (SQLException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
    }

    public void write(int b) throws IOException {
        //This won't be called, don't implement
        throw new IOException("FBBlobOutputStream.write(int b) not implemented");
    }

    public void writeSegment(byte[] buf) throws GDSException {
        Object syncObject = owner.getSynchronizationObject();
        synchronized (syncObject) {
            owner.getGdsHelper().putBlobSegment(blobHandle, buf);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        try {
            if (off == 0 && len == b.length && len < owner.getBufferLength()) {
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
                byte[] buf = new byte[chunk];
                while (len > 0) {
                    chunk = Math.min(len, chunk);

                    // this allows us to reuse the buffer if its size has not changed
                    if (chunk != buf.length) {
                        buf = new byte[chunk];
                    }

                    System.arraycopy(b, off, buf, 0, chunk);
                    writeSegment(buf);

                    len -= chunk;
                    off += chunk;
                }
            }
        } catch (GDSException ge) {
            throw new IOException("Problem writing to FBBlobOutputStream: " + ge.getMessage(), ge);
        }
    }

    @Override
    public void close() throws IOException {
        if (blobHandle == null) return;
        try {
            synchronized (owner.getSynchronizationObject()) {
                owner.getGdsHelper().closeBlob(blobHandle);
            }

            owner.setBlobId(blobHandle.getBlobId());
        } catch (GDSException ge) {
            throw new IOException("could not close blob: " + ge.getMessage(), ge);
        } finally {
            blobHandle = null;
        }
    }
}