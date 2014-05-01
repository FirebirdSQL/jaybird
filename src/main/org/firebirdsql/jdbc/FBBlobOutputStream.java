package org.firebirdsql.jdbc;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;

public class FBBlobOutputStream extends OutputStream implements FirebirdBlob.BlobOutputStream {

    private IscBlobHandle blobHandle;
    private final FBBlob owner;

    FBBlobOutputStream(FBBlob owner) throws SQLException {
        this.owner = owner;

        synchronized(owner.getSynchronizationObject()) {
            try {
                DatabaseParameterBuffer dpb = owner.gdsHelper.getDatabaseParameterBuffer();
                
                boolean useStreamBlobs = 
                    dpb.hasArgument(DatabaseParameterBufferExtension.USE_STREAM_BLOBS);
                
                blobHandle = owner.gdsHelper.createBlob(!useStreamBlobs);
                
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }
        
        if (owner.isNew) {
            owner.setBlobId(blobHandle.getBlobId());
        }
    }
    
    public void seek(int position, int seekMode) throws SQLException {
        synchronized(owner.getSynchronizationObject()) {
            try {
                owner.gdsHelper.seekBlob(blobHandle, position, seekMode);
            } catch(GDSException ex) {
                throw new FBSQLException(ex);
            }
        }
    }
    
    public long length() throws IOException {
        synchronized(owner.getSynchronizationObject()) {
            try {
                byte[] info = owner.gdsHelper.getBlobInfo(
                    blobHandle, new byte[] {ISCConstants.isc_info_blob_total_length}, 20);

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
        synchronized(syncObject) {
            owner.gdsHelper.putBlobSegment(blobHandle, buf);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        try {
            if (off == 0 && len == b.length && len < owner.bufferlength) {
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
                int chunk = owner.bufferlength;
                byte[] buf = new byte[chunk];
                int lastChunk = chunk;
                while (len > 0) {
                    if (len < chunk) chunk = len;

                    // this allows us to reused the chunk if its size has not changed
                    if (chunk != lastChunk) {
                        buf = new byte[chunk];
                        lastChunk = chunk;
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

    public void close() throws IOException {
        if (blobHandle != null) {
            try {
                synchronized(owner.getSynchronizationObject()) {
                    owner.gdsHelper.closeBlob(blobHandle);
                }
                
                owner.setBlobId(blobHandle.getBlobId());
            } catch (GDSException ge) {
                throw new IOException("could not close blob: " + ge.getMessage(), ge);
            }
            
            blobHandle = null;
        }
    }
}