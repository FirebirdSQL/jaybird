// SPDX-FileCopyrightText: Copyright 2003-2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Blob;
import java.sql.SQLException;

import java.io.*;

/**
 * Firebird Blob abstraction. This interface defines methods to read and write
 * Blob content.
 * 
 * @author Roman Rokytskyy
 */
@NullMarked
public interface FirebirdBlob extends Blob {
    
    /**
     * Blob input stream. This interface defines methods to access contents
     * of the Blob field. Some method signatures are copied from the 
     * {@link InputStream} only because it is abstract class and not interface
     * that we can extend.
     */
    @SuppressWarnings("unused")
    interface BlobInputStream extends AutoCloseable {
        
        /** Seek based on the absolute beginning of the stream */
        int SEEK_MODE_ABSOLUTE = ISCConstants.blb_seek_from_head;

        /** Seek relative to the current position in the stream */
        int SEEK_MODE_RELATIVE = ISCConstants.blb_seek_relative;

        /** Seek relative to the tail end of the stream */
        int SEEK_MODE_FROM_TAIL = ISCConstants.blb_seek_from_tail;
        
        /**
         * Get instance of {@link FirebirdBlob} to which this stream belongs to.
         * <p>
         * Note, code
         * <pre>
         * FirebirdBlob.BlobInputStream otherStream = (FirebirdBlob.BlobInputStream)
         *     inputStream.getBlob().getBinaryStream();
         * </pre>
         * will return new stream object.
         * 
         * @return instance of {@link FirebirdBlob}.
         */
        FirebirdBlob getBlob();
        
        /**
         * Get number of available bytes that can be read without blocking.
         * This method will return number of bytes of the last read blob segment 
         * in the blob buffer.
         * 
         * @return number of bytes available without blocking or -1 if end of
         * stream is reached.
         * 
         * @throws IOException if I/O error occurred.
         */
        int available() throws IOException;
        
        /**
         * Close this stream.
         * 
         * @throws IOException if I/O error occurs.
         */
        @Override
        void close() throws IOException;

        /**
         * Get Blob length. This is a shortcut for {@code inputStream.getBlob().length()} call, and is more resource
         * friendly, because no new Blob handle is created.
         *
         * @return length of the blob
         * @throws IOException
         *         if I/O error occurs
         */
        long length() throws IOException;

        /**
         * Read a single byte from the stream.
         *
         * @return next byte read from the stream or {@code -1} if end of stream was reached
         * @throws IOException
         *         if I/O error occurs
         * @see InputStream#read()
         */
        int read() throws IOException;

        /**
         * Read some bytes from the stream into {@code buffer}.
         * <p>
         * The implementation may read less bytes than requested. Implementations may perform multiple roundtrips to
         * the server to fill {@code buffer} up to the requested length.
         * </p>
         *
         * @param buffer
         *         buffer into which data should be read
         * @param offset
         *         offset in the buffer where to start
         * @param length
         *         number of bytes to read
         * @return number of bytes that were actually read, returns {@code 0} if {@code len == 0}, {@code -1} if
         * end-of-blob was reached without reading any bytes
         * @throws IOException
         *         if I/O error occurs
         * @see InputStream#read(byte[], int, int)
         */
        int read(byte[] buffer, int offset, int length) throws IOException;

        /**
         * Read {@code length} from the stream into the specified buffer.
         * <p>
         * This method will throw an {@code EOFException} if end-of-blob was reached before reading {@code length}
         * bytes.
         * </p>
         *
         * @param buffer
         *         buffer where data should be read
         * @param offset
         *         offset in the buffer where to start
         * @param length
         *         number of bytes to read
         * @throws EOFException
         *         if stream end was reached when reading data.
         * @throws IOException
         *         if I/O error occurs.
         */
        void readFully(byte[] buffer, int offset, int length) throws IOException;

        /**
         * Read {@code buffer.length} bytes from the buffer. This is a shortcut method for
         * {@code readFully(buffer, 0, buffer.length)} call.
         *
         * @param buffer
         *         buffer where data should be read
         * @throws IOException
         *         if I/O error occurs
         */
        void readFully(byte[] buffer) throws IOException;

        /**
         * @see InputStream#readNBytes(byte[], int, int)
         */
        int readNBytes(byte[] b, int off, int len) throws IOException;

        /**
         * @see InputStream#readNBytes(int)
         */
        byte[] readNBytes(int len) throws IOException;

        /**
         * Move current position in the Blob stream. This is a shortcut method to {@link #seek(int, int)} passing
         * {@link #SEEK_MODE_ABSOLUTE} as seek mode.
         *
         * @param position
         *         absolute position to seek, starting position is 0 (note, in {@link Blob#getBytes(long, int)} starting
         *         position is 1).
         * @throws IOException
         *         if I/O error occurs.
         */
        void seek(int position) throws IOException;

        /**
         * Move current position in the Blob stream. Depending on the specified seek mode, position can be either
         * positive or negative.
         *
         * @param position
         *         position in the stream, starting position is 0 (note, in {@link Blob#getBytes(long, int)} starting
         *         position is 1)
         * @param seekMode
         *         mode of seek operation, one of {@link #SEEK_MODE_ABSOLUTE}, {@link #SEEK_MODE_RELATIVE} or
         *         {@link #SEEK_MODE_FROM_TAIL}
         * @throws IOException
         *         if I/O error occurs
         */
        void seek(int position, int seekMode) throws IOException;

    }
    
    /**
     * Blob output stream. This interface defines methods to write contents
     * of the Blob field. Some method signatures are copied from the 
     * {@link OutputStream} only because it is abstract class and not interface
     * that we can extend.
     */
    interface BlobOutputStream {

        /**
         * Get instance of {@link FirebirdBlob} to which this stream belongs to.
         * <p>
         * Note, code
         * <pre>
         * FirebirdBlob.BlobOutputStream otherStream = (FirebirdBlob.BlobOutputStream)
         *     inputStream.getBlob().setBinaryStream(1);
         * </pre>
         * will return new stream object.
         *
         * @return instance of {@link FirebirdBlob}.
         */
        FirebirdBlob getBlob();
        
        /**
         * Close this stream. Calling this method closes Blob stream and moves
         * Blob from temporary into permanent state making any further content
         * updates impossible.
         * 
         * @throws IOException if I/O error occurs.
         */
        void close() throws IOException;
        
        /**
         * Get Blob length. This method is the only available way to obtain 
         * length of a Blob that is in temporary state,
         * 
         * @return length of the blob.
         * 
         * @throws IOException if I/O error occurs.
         */
        long length() throws IOException;
        
        /**
         * Write data from the buffer into this stream.
         * 
         * @param buffer buffer from which data should be written.
         * @param offset offset in the buffer.
         * @param length number of bytes to write.
         * 
         * @throws IOException if I/O error occurs.
         */
        void write(byte[] buffer, int offset, int length) throws IOException;
        
        /**
         * Write single byte into the stream.
         * 
         * @param data data to write, only lowest 8 bits are written.
         * 
         * @throws IOException if I/O error occurs.
         */
        void write(int data) throws IOException;
    }
    
    /**
     * Detach this blob. This method creates new instance of the same blob 
     * database object that is not under result set control. When result set
     * is closed, all associated resources are also released, including open
     * blob streams. This method creates a new instance of blob object with
     * the same blob ID that can be used even when result set is closed.
     * <p>
     * Note, detached blob will not remember the stream position of this object.
     * This means that you cannot start reading data from the blob, then detach
     * it, and then continue reading. Reading from detached blob will begin at
     * the blob start.
     * 
     * @return instance of {@link FirebirdBlob} that is not under result set 
     * control.
     * 
     * @throws SQLException if Blob cannot be detached.
     */    
    FirebirdBlob detach() throws SQLException;
    
    /**
     * Check if blob is segmented. If Blob is segmented, you cannot use 
     * {@link BlobInputStream#seek(int)} method.
     * 
     * @return {@code true} if this blob is segmented, otherwise {@code false}
     */
    boolean isSegmented() throws SQLException;

    /**
     * Gets the byte content of this blob as a byte array.
     *
     * @return byte array with blob content (may return {@code null} for certain cached blobs)
     * @throws SQLException
     *         for database access errors, or if the blob size exceeds the maximum safe array size (i.e.
     *         {@link Integer#MAX_VALUE} - 8)
     * @since 6
     */
    byte @Nullable [] getBytes() throws SQLException;

}