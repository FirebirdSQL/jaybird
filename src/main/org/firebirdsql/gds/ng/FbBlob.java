/*
 * $Id$
 *
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

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface FbBlob {

    /**
     * @return The Firebird blob id
     */
    long getBlobId();

    /**
     * @return The Firebird blob handle identifier
     */
    int getHandle();

    /**
     * Opens an existing input blob, or creates an output blob.
     *
     * @throws SQLException
     *         If the blob is already open, this is a (closed) output blob and it already has a blobId, the
     *         transaction is not active, or a database connection error occurred
     */
    void open() throws SQLException;

    /**
     * @return <code>true</code> if this blob is currently open.
     */
    boolean isOpen();

    /**
     * @return <code>true</code> if this blob has reached the end or has been closed, always <code>true</code> for an
     *         open output blob.
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
     * @return <code>true</code> if this is an output blob (write only), <code>false</code> if this is an
     *         input blob (read only)
     */
    boolean isOutput();

    /**
     * Get synchronization object.
     *
     * @return object, cannot be <code>null</code>.
     */
    Object getSynchronizationObject();

    // TODO Consider different blob api, eg more like InputStream / OutputStream methods taking a buffer with offset and length?

    /**
     * Gets a segment of blob data.
     * <p>
     * When <code>sizeRequested</code> exceeds {@link #getMaximumSegmentSize()} it is silently reduced to the maximum
     * segment size.
     * </p>
     * TODO: Consider allowing this and have the implementation handle longer segments by sending multiple (batched?) requests.
     *
     * @param sizeRequested
     *         Requested segment size (> 0).
     * @return Retrieved segment (size may be less than requested)
     * @throws SQLException
     *         If this is an output blob, the blob is closed, the transaction is not active, or a database connection
     *         error occurred.
     */
    byte[] getSegment(int sizeRequested) throws SQLException;

    /**
     * Writes a segment of blob data.
     * <p>
     * Implementation must handle segment length exceeding {@link #getMaximumSegmentSize()} by batching. TODO: reconsider and let caller handle that?
     * </p>
     * <p>
     * Passing a section that is length 0 will throw an <code>SQLException</code>.
     * </p>
     *
     * @param segment
     *         Segment to write
     * @throws SQLException
     *         If this is an input blob, the blob is closed, the transaction is not active, the segment is length 0 or
     *         longer than the maximum segment size, or a database connection error occurred.
     */
    void putSegment(byte[] segment) throws SQLException;

    /**
     * The maximum segment size allowed by the protocol for {@link #getSegment(int)} and {@link #putSegment(byte[])}.
     * <p>
     * This value is <strong>not</strong> the segment size (optionally) defined for the column.
     * </p>
     *
     * @return The maximum segment size allowed for get or put.
     */
    int getMaximumSegmentSize();
}
