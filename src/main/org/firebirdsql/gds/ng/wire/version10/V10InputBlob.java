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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.SQLStateConstants;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10InputBlob extends AbstractFbWireInputBlob implements FbWireBlob, DatabaseListener {
    
    private static final int STATE_END_OF_BLOB = 2;

    // TODO V10OutputBlob and V10InputBlob share some common behavior and information (eg in open() and getMaximumSegmentSize()), find a way to unify this

    public V10InputBlob(FbWireDatabase database, FbWireTransaction transaction,
                        BlobParameterBuffer blobParameterBuffer, long blobId) {
        super(database, transaction, blobParameterBuffer, blobId);
    }

    // TODO Need blob specific warning callback?

    @Override
    public void open() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobClosed();

            sendOpen(BlobOpenOperation.INPUT_BLOB);
            receiveOpenResponse();
            // TODO Request information on the blob?
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void receiveOpenResponse() throws SQLException {
        try {
            GenericResponse genericResponse = getDatabase().readGenericResponse(null);
            setHandle(genericResponse.objectHandle());
            setOpen(true);
            resetEof();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public byte[] getSegment(final int sizeRequested) throws SQLException {
        try {
            if (sizeRequested <= 0) {
                throw FbExceptionBuilder.forException(jb_blobGetSegmentNegative)
                        .messageParameter(sizeRequested)
                        .toSQLException();
            }
            final GenericResponse response;
            try (LockCloseable ignored = withLock()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();

                int actualSize = segmentRequestSize(sizeRequested);
                try {
                    sendGetSegment(actualSize);
                    getXdrOut().flush();
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioWriteError(e);
                }
                try {
                    response = getDatabase().readGenericResponse(null);
                    if (response.objectHandle() == STATE_END_OF_BLOB) {
                        // TODO what if I seek on a stream blob?
                        setEof();
                    }
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioReadError(e);
                }
            }

            final byte[] responseBuffer = response.data();
            if (responseBuffer.length == 0) {
                return responseBuffer;
            }

            final byte[] data = new byte[getTotalSegmentSize(responseBuffer)];
            int responsePos = 0;
            int dataPos = 0;
            while (responsePos < responseBuffer.length) {
                int segmentLength = iscVaxInteger2(responseBuffer, responsePos);
                responsePos += 2;
                System.arraycopy(responseBuffer, responsePos, data, dataPos, segmentLength);
                responsePos += segmentLength;
                dataPos += segmentLength;
            }
            return data;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Calculates the total size of all segments in {@code segmentBuffer}.
     *
     * @param segmentBuffer
     *         segment buffer (contains 1 or more segments of [2-byte length][length bytes]...).
     * @return total length of segments
     */
    private static int getTotalSegmentSize(byte[] segmentBuffer) {
        int count = 0;
        int pos = 0;
        while (pos < segmentBuffer.length) {
            int segmentLength = VaxEncoding.iscVaxInteger2(segmentBuffer, pos);
            pos += 2 + segmentLength;
            count += segmentLength;
        }
        return count;
    }

    private int segmentRequestSize(int size) {
        // The request size is the total buffer, but segments are prefixed with 2 bytes for the size. It is possible
        // a single response contains multiple segments, but we don't take that into account for the size calculation.
        return Math.min(Math.max(size, size + 2), getMaximumSegmentSize());
    }

    protected void sendGetSegment(int len) throws SQLException, IOException {
        XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(op_get_segment);
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(len);
        xdrOut.writeInt(0); // length of segment send buffer (always 0 in get)
    }

    @Override
    protected int get(final byte[] b, final int off, final int len, final int minLen) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            validateBufferLength(b, off, len);
            if (len == 0) return 0;
            if (minLen <= 0 || minLen > len ) {
                throw new SQLNonTransientException("Value out of range 0 < minLen <= len, minLen was: " + minLen,
                        SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            final FbWireOperations wireOps = getDatabase().getWireOperations();
            final XdrOutputStream xdrOut = getXdrOut();
            final XdrInputStream xdrIn = getXdrIn();
            int count = 0;
            while (count < minLen && !isEof()) {
                try {
                    sendGetSegment(segmentRequestSize(len - count));
                    xdrOut.flush();
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioWriteError(e);
                }
                try {
                    final int opCode = wireOps.readNextOperation();
                    if (opCode != op_response) {
                        wireOps.readOperationResponse(opCode, null);
                        throw new SQLException("Unexpected response to op_get_segment: " + opCode);
                    }
                    final int objHandle = xdrIn.readInt();
                    xdrIn.skipNBytes(8); // blob-id (unused)

                    final int bufferLength = xdrIn.readInt();
                    if (bufferLength > 0) {
                        int bufferRemaining = bufferLength;
                        while (bufferRemaining > 2) {
                            int segmentLength = VaxEncoding.decodeVaxInteger2WithoutLength(xdrIn);
                            bufferRemaining -= 2;
                            if (segmentLength > bufferRemaining) {
                                throw new IOException(
                                        "Inconsistent segment buffer: segment length %d, remaining buffer was %d"
                                                .formatted(segmentLength, bufferRemaining));
                            } else if (segmentLength > len - count) {
                                throw new IOException("Returned segment length %d exceeded remaining size %d"
                                        .formatted(segmentLength, len - count));
                            }
                            xdrIn.readFully(b, off + count, segmentLength);
                            bufferRemaining -= segmentLength;
                            count += segmentLength;
                        }

                        // Safety measure: read remaining (shouldn't happen in practice)
                        xdrIn.skipNBytes(bufferRemaining);
                        // Skip buffer padding
                        xdrIn.skipPadding(bufferLength);
                    }

                    SQLException exception = wireOps.readStatusVector();
                    if (exception != null && !(exception instanceof SQLWarning)) {
                        // NOTE: SQLWarning is unlikely for this operation, so we don't do anything to report it
                        throw exception;
                    }

                    if (objHandle == STATE_END_OF_BLOB) {
                        setEof();
                    }
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioReadError(e);
                }
            }

            return count;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void seek(int offset, SeekMode seekMode) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();

            sendSeek(offset, seekMode);
            receiveSeekResponse();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendSeek(int offset, SeekMode seekMode) throws SQLException {
        try {
            XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_seek_blob);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(seekMode.getSeekModeId());
            xdrOut.writeInt(offset);
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveSeekResponse() throws SQLException {
        try {
            getDatabase().readResponse(null);
            // object handle in response is the current position in the blob (see .NET provider source)
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }
}
