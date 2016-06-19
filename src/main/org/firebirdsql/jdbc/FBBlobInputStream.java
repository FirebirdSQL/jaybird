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

import java.io.*;
import java.sql.SQLException;

import org.firebirdsql.gds.*;

/**
 * An input stream for reading directly from a FBBlob instance.
 */
public class FBBlobInputStream extends InputStream 
    implements FirebirdBlob.BlobInputStream
{


    private byte[] buffer = null;
    private IscBlobHandle blobHandle;
    private int pos = 0;
    
    private boolean closed;
    
    private FBBlob owner;

    FBBlobInputStream(FBBlob owner) throws SQLException {
        this.owner = owner;
        
        closed = false;
        
        if (owner.isNew) 
            throw new FBSQLException("You can't read a new blob");

        synchronized(owner.getSynchronizationObject()) {
            try {
                blobHandle = owner.gdsHelper.openBlob(owner.blob_id, FBBlob.SEGMENTED);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }
    }
    
    public FirebirdBlob getBlob() {
        return owner;
    }

    public void seek(int position) throws IOException {
        seek(position, SEEK_MODE_ABSOLUTE);
    }

    public void seek(int position, int seekMode) throws IOException {
        synchronized(owner.getSynchronizationObject()) {
            checkClosed();
            try {
                owner.gdsHelper.seekBlob(blobHandle, position, seekMode);
            } catch (GDSException ex) {
                /** @todo fix this */
                throw new IOException(ex.getMessage());
            }
        }
    }
    
    public long length() throws IOException {
        synchronized(owner.getSynchronizationObject()) {
            checkClosed();
            try {
                byte[] info = owner.gdsHelper.getBlobInfo(
                    blobHandle, new byte[] {ISCConstants.isc_info_blob_total_length}, 20);

                return owner.interpretLength(info, 0);
            } catch (GDSException ex) {
                throw new IOException(ex.getMessage());
            } catch (SQLException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }

    public int available() throws IOException {
        synchronized(owner.getSynchronizationObject()) {
            checkClosed();
            if (buffer == null) {
                if (blobHandle.isEof()) {
                    return -1;
                }
                
                try {
                    //bufferlength is in FBBlob enclosing class
                    buffer = owner.gdsHelper.getBlobSegment(blobHandle, owner.bufferlength);
                } catch (GDSException ge) {
                    throw new IOException("Blob read problem: " +
                        ge.toString());
                }
                
                pos = 0;
                if (buffer.length == 0) {
                   return -1;
                }
            }
            return buffer.length - pos;
        }
    }

    public int read() throws IOException {
        if (available() <= 0) {
            return -1;
        }
        int result = buffer[pos++] & 0x00FF;//& seems to convert signed byte to unsigned byte
        if (pos == buffer.length) {
            buffer = null;
        }
        return result;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = available();
        if (result <= 0) {
            return -1;
        }
        if (result > len) {//not expected to happen
            System.arraycopy(buffer, pos, b, off, len);
            pos += len;
            return len;
        }
        System.arraycopy(buffer, pos, b, off, result);
        buffer = null;
        pos = 0;
        return result;
    }
    
    public void readFully(byte[] b, int off, int len) throws IOException {
        int counter = 0;
        int pos = 0;
        byte[] buffer = new byte[Math.min(FBBlob.READ_FULLY_BUFFER_SIZE, len)];

        int toRead = len;

        while(toRead > 0 && (counter = read(buffer, 0, toRead)) != -1) {
            System.arraycopy(buffer, 0, b, pos, counter);
            pos += counter;
            
            toRead -= counter;
        }
        
        if (counter == -1)
            throw new EOFException();
    }
    
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void close() throws IOException {
        synchronized(owner.getSynchronizationObject()) {
            if (blobHandle != null) {
                try {
                    owner.gdsHelper.closeBlob(blobHandle);
                    
                    owner.inputStreams.remove(this);
                } catch (GDSException ge) {
                    throw new IOException("couldn't close blob: " + ge);
                }
                blobHandle = null;
                closed = true;
            }
        }
    }
    
    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Input stream is already closed.");
    }
}