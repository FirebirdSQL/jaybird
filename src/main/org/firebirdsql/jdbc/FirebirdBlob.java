/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;

import java.sql.Blob;
import java.sql.SQLException;

import java.io.*;

/**
 * Firebird Blob abstraction. This interface defines methods to read and write
 * Blob content.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdBlob extends Blob {
    
    /**
     * Blob input stream. This interface defines methods to access contents
     * of the Blob field. Some method signatures are copied from the 
     * {@link InputStream} only because it is abstract class and not interface
     * that we can extend.
     */
    interface BlobInputStream {
        
        int SEEK_MODE_ABSOLUTE = 0;
        int SEEK_MODE_RELATIVE = 1;
        int SEEK_MODE_FROM_TAIL = 2;
        
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
         * @throws IOException if I/O error occured.
         */
        int available() throws IOException;
        
        /**
         * Close this stream.
         * 
         * @throws IOException if I/O error occurs.
         */
        void close() throws IOException;
        
        /**
         * Get Blob length. This is shortcut method for the 
         * <code>inputStream.getBlob().length()</code> call, however is more
         * resource friendly, because no new Blob handle is created.
         * 
         * @return length of the blob.
         * 
         * @throws IOException if I/O error occurs.
         */
        long length() throws IOException;
        
        /**
         * Read single byte from the stream.
         * 
         * @return next byte read from the stream or -1 if end of stream was
         * reached.
         * 
         * @throws IOException if I/O error occurs.
         */
        int read() throws IOException;
        
        /**
         * Read some bytes from the stream without blocking.
         * 
         * @param buffer buffer into which data should be read.
         * @param offset offset in the buffer where to start.
         * @param length number of bytes to read.
         * 
         * @return number of bytes that were read.
         * 
         * @throws IOException if I/O error occurs.
         */
        int read(byte[] buffer, int offset, int length) throws IOException;
        
        /**
         * Read <code>length</code> from the stream into the specified buffer.
         * This method can block until desired number of bytes is read, it can
         * throw an exception if end of stream was reached during read.
         * 
         * @param buffer buffer where data should be read.
         * @param offset offset in the buffer where to start.
         * @param length number of bytes to read.
         * 
         * @throws EOFException if stream end was reached when reading data.
         * @throws IOException if I/O error occurs.
         */
        void readFully(byte[] buffer, int offset, int length) throws IOException;
        
        /**
         * Read <code>buffer.length</code> bytes from the buffer. This is a 
         * shortcut method for <code>readFully(buffer, 0, buffer.length)</code>
         * call.
         * 
         * @param buffer buffer where data should be read.
         * 
         * @throws IOException if I/O error occurs.
         */
        void readFully(byte[] buffer) throws IOException;
        
        /**
         * Move current position in the Blob stream. This is a shortcut method
         * to {@link #seek(int, int)} passing {@link #SEEK_MODE_ABSOLUTE} as 
         * seek mode.
         * 
         * @param position absolute position to seek, starting position is
         * 0 (note, in {@link Blob#getBytes(long, int)} starting position is 1).
         * 
         * @throws IOException if I/O error occurs.
         */
        void seek(int position) throws IOException;
        
        /**
         * Move current position in the Blob stream. Depending on the specified
         * seek mode, position can be either positive or negative.
         * <p>
         * Note, this method allows to move position in the Blob stream only
         * forward. If you need to read data before the current position, new
         * stream must be opened.
         * 
         * @param position position in the stream, starting position is
         * 0 (note, in {@link Blob#getBytes(long, int)} starting position is 1).
         * 
         * @param seekMode mode of seek operation, one of {@link #SEEK_MODE_ABSOLUTE},
         * {@link #SEEK_MODE_RELATIVE} or {@link #SEEK_MODE_FROM_TAIL}.
         * 
         * @throws IOException if I/O erro occurs.
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
     * blob streams. This method creates an new instance of blob object with
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
     * @return <code>true</code> if this blob is segmented, 
     * otherwise <code>false</code>
     */
    boolean isSegmented() throws SQLException;
    
    /**
     * Retrieves a stream that can be used to write to the Blob value that this 
     * object represents.  The stream begins at position <code>position</code>.
     * <p>
     * This method is directly copied from JDBC 3.0 classes to make it available
     * in JDBC 2.0 environment.
     *
     * @param position the position in the <code>BLOB</code> value at which
     * to start writing; currently only position 0 is supported.
     * 
     * @return a <code>java.io.OutputStream</code> object to which data can 
     * be written
     * 
     * @throws SQLException if something went wrong.
     */
    OutputStream setBinaryStream(long position) throws SQLException;
}