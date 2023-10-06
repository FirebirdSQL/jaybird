/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;

import java.sql.SQLException;

/**
 * Interface for blob operations.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbBlob extends ExceptionListenable, AutoCloseable {

    long NO_BLOB_ID = 0;

    /**
     * @return The Firebird blob id
     */
    long getBlobId();

    /**
     * @return The Firebird blob handle identifier
     */
    int getHandle();

    /**
     * @return The database connection that created this blob
     */
    FbDatabase getDatabase();

    /**
     * Opens an existing input blob, or creates an output blob.
     *
     * @throws SQLException
     *         If the blob is already open, this is a (closed) output blob and it already has a blobId, the
     *         transaction is not active, or a database connection error occurred
     */
    void open() throws SQLException;

    /**
     * @return {@code true} if this blob is currently open.
     */
    boolean isOpen();

    /**
     * @return {@code true} if this blob has reached the end or has been closed, always {@code true} for an
     * open output blob.
     */
    boolean isEof();

    /**
     * Closes the blob.
     * <p>
     * Closing an already closed blob is a no-op.
     * </p>
     *
     * @throws SQLException
     *         If the transaction is not active, or a database connection error occurred
     */
    void close() throws SQLException;

    /**
     * Cancels an output blob (which means its contents will be thrown away).
     * <p>
     * Calling cancel on an input blob will close it. Contrary to {@link #close()}, calling cancel on an
     * already closed (or cancelled) blob will throw an {@link java.sql.SQLException}.
     * </p>
     *
     * @throws SQLException
     *         If the blob has already been closed, the transaction is not active, or a database connection error
     *         occurred.
     */
    void cancel() throws SQLException;

    /**
     * @return {@code true} if this is an output blob (write only), {@code false} if this is an
     * input blob (read only)
     */
    boolean isOutput();

    /**
     * Gets a segment of blob data.
     * <p>
     * When {@code sizeRequested} exceeds {@link #getMaximumSegmentSize()} it is silently reduced to the maximum
     * segment size.
     * </p>
     *
     * @param sizeRequested
     *         Requested segment size (&gt; 0).
     * @return Retrieved segment (size may be less than requested)
     * @throws SQLException
     *         If this is an output blob, the blob is closed, the transaction is not active, or a database connection
     *         error occurred.
     * @see #get(byte[], int, int) 
     */
    byte[] getSegment(int sizeRequested) throws SQLException;

    /**
     * Reads content from the blob into {@code b} starting at {@code off} for {@code len} bytes.
     * <p>
     * Implementations must read the requested number of bytes ({@code len}), unless end-of-blob is reached before
     * the requested number of bytes were read. The return value of this method is the actual number of bytes read.
     * </p>
     * <p>
     * If the implementation cannot perform reads without additional allocation, it should use at most
     * {@link DatabaseConnectionProperties#getBlobBufferSize()} as an internal buffer. If the implementation can
     * perform reads without additional allocation, it is recommended it performs reads using (at most)
     * {@link #getMaximumSegmentSize()}.
     * </p>
     * <p>
     * Contrary to similar methods like {@link java.io.InputStream#read(byte[], int, int)}, this method returns
     * {@code 0} when no bytes were read if end-of-blob is reached without reading any bytes, not {@code -1}.
     * </p>
     *
     * @param b
     *         target byte array
     * @param off
     *         offset to start
     * @param len
     *         number of bytes
     * @return actual number of bytes read; this will only be less than {@code len} when end-of-blob was reached
     * @throws SQLException
     *         for database access errors, if {@code off < 0}, {@code len < 0}, or if {@code off + len > b.length}
     * @since 6
     */
    int get(byte[] b, int off, int len) throws SQLException;

    /**
     * Writes a segment of blob data.
     * <p>
     * Implementations must handle segment lengths exceeding {@link #getMaximumSegmentSize()} by batching. This method
     * should either call {@code put(segment, 0, segment.length)}, or produce the same effects as that call.
     * </p>
     * <p>
     * Passing a section that is length 0 will throw an {@code SQLException}.
     * </p>
     *
     * @param segment
     *         segment to write
     * @throws SQLException
     *         if this is an input blob, the blob is closed, the transaction is not active, the segment is length 0, or
     *         a database connection error occurred
     * @see #put(byte[], int, int)
     */
    void putSegment(byte[] segment) throws SQLException;

    /**
     * Writes content of {@code b} starting at {@code off} for {@code length} bytes to the blob.
     * <p>
     * Implementations must write all bytes to the blob, using multiple round-trips if necessary.
     * </p>
     * <p>
     * If the implementation cannot perform writes without additional allocation, it should use at most
     * {@link DatabaseConnectionProperties#getBlobBufferSize()} as an internal buffer. If the implementation can
     * perform writes without additional allocation, it is recommended it performs reads using (at most)
     * {@link #getMaximumSegmentSize()}.
     * </p>
     *
     * @param b
     *         source byte array
     * @param off
     *         offset to start
     * @param len
     *         number of bytes
     * @throws SQLException
     *         for database access errors, if {@code off < 0}, {@code len < 0}, or if {@code off + len > b.length}
     * @since 6
     */
    void put(byte[] b, int off, int len) throws SQLException;

    /**
     * Performs a seek on a blob with the specified {@code seekMode} and {@code offset}.
     * <p>
     * Firebird only supports seek on stream blobs.
     * </p>
     *
     * @param offset
     *         Offset of the seek, effect depends on value of {@code seekMode}
     * @param seekMode
     *         Value of {@link org.firebirdsql.gds.ng.FbBlob.SeekMode}
     * @throws SQLException
     *         If the blob is closed, the transaction is not active, or a database error occurred.
     */
    void seek(int offset, SeekMode seekMode) throws SQLException;

    /**
     * The maximum segment size allowed by the protocol for {@link #getSegment(int)} and {@link #putSegment(byte[])}.
     * <p>
     * This value is <strong>not</strong> the segment size (optionally) defined for the column.
     * </p>
     *
     * @return The maximum segment size allowed for get or put.
     */
    int getMaximumSegmentSize();

    /**
     * Request blob info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link org.firebirdsql.gds.ng.InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T> T getBlobInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException;

    /**
     * Requests the blob length from the server.
     *
     * @return Length of the blob.
     * @throws SQLException
     *         For Errors retrieving the length, or if the blob is not associated with a blob id, or the database is not
     *         attached.
     */
    long length() throws SQLException;

    /**
     * Request blob info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @return Response buffer
     */
    byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException;

    /**
     * Seek mode for {@link FbBlob#seek(int, org.firebirdsql.gds.ng.FbBlob.SeekMode)}.
     */
    enum SeekMode {
        /**
         * Absolute seek from start of blob.
         */
        ABSOLUTE(ISCConstants.blb_seek_from_head),
        /**
         * Relative seek from current position in blob.
         */
        RELATIVE(ISCConstants.blb_seek_relative),
        /**
         * Absolute seek from end of blob.
         */
        ABSOLUTE_FROM_END(ISCConstants.blb_seek_from_tail);

        final int seekModeId;

        SeekMode(int seekModeId) {
            this.seekModeId = seekModeId;
        }

        /**
         * @return Seek mode value used within Firebird (and the native API, wire protocol)
         */
        public int getSeekModeId() {
            return seekModeId;
        }

        /**
         * Get the SeekMode instance by the (Firebird) seekmode id.
         * <p>
         * Valid values are:
         * <ul>
         * <li>{@code 0} - for {@link SeekMode#ABSOLUTE}</li>
         * <li>{@code 1} - for {@link SeekMode#RELATIVE}</li>
         * <li>{@code 2} - for {@link SeekMode#ABSOLUTE_FROM_END}</li>
         * </ul>
         * </p>
         *
         * @param seekModeId
         *         The seekmode id.
         * @return Instance of SeekMode matching the id.
         * @throws java.lang.IllegalArgumentException
         *         For values that do not have a SeekMode instance.
         */
        public static SeekMode getById(int seekModeId) {
            for (SeekMode seekMode : values()) {
                if (seekMode.getSeekModeId() == seekModeId) return seekMode;
            }
            throw new IllegalArgumentException(String.format("No SeekMode with id %d", seekModeId));
        }
    }
}
