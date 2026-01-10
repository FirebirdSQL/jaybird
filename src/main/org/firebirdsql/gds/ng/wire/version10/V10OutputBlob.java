// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_op;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.jaybird.util.ByteArrayHelper.validateBufferLength;

/**
 * Output {@link org.firebirdsql.gds.ng.wire.FbWireBlob} implementation for the version 10 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3
 */
public class V10OutputBlob extends AbstractFbWireOutputBlob implements FbWireBlob, DatabaseListener {

    // TODO V10OutputBlob and V10InputBlob share some common behavior and information (eg in open()), find a way to unify this

    /**
     * Number of op_put_segment calls batched by {@link #put(byte[], int, int)} without retrieving response.
     */
    private static final int OUTSTANDING_PUT_SEGMENT_PACKETS = 8;

    public V10OutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) throws SQLException {
        super(database, transaction, blobParameterBuffer);
    }

    @Override
    public void open() throws SQLException {
        try (var ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobClosed();
            clearDeferredException();

            if (getBlobId() != FbBlob.NO_BLOB_ID) {
                throw FbExceptionBuilder.toNonTransientException(isc_segstr_no_op);
            }

            sendOpen(BlobOpenOperation.OUTPUT_BLOB, true);
            receiveOpenResponse();
            throwAndClearDeferredException();
            // TODO Request information on the blob?
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void put(final byte[] b, final int off, final int len) throws SQLException {
        try (var ignored = withLock())  {
            validateBufferLength(b, off, len);
            if (len == 0) {
                throw FbExceptionBuilder.toException(jb_blobPutSegmentEmpty);
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            batchPutSegment(b, off, len);
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    private void batchPutSegment(byte[] b, int off, int len) throws SQLException {
        int requestCount = 0;
        try {
            // NOTE: Previously, we called getHandle on each iteration; the value could change from INVALID_OBJECT to
            // the actual handle; getting this once, and maybe using INVALID_OBJECT for all iterations, should be fine.
            final int handle = getHandle();
            int count = 0;
            while (count < len) {
                int segmentLength = Math.min(len - count, getMaximumSegmentSize());
                int offsetForTransmit = off + count;
                withTransmitLock(xdrOut ->
                        sendPutSegmentMsg(xdrOut, handle, b, offsetForTransmit, segmentLength));
                count += segmentLength;
                if (++requestCount >= OUTSTANDING_PUT_SEGMENT_PACKETS) {
                    withTransmitLock(OutputStream::flush);
                    try {
                        consumePutSegmentResponses(requestCount);
                    } finally {
                        requestCount = 0;
                    }
                }
            }
            withTransmitLock(OutputStream::flush);
        } catch (IOException e) {
            getDatabase().consumePackets(requestCount, w -> {});
            throw FbExceptionBuilder.ioWriteError(e);
        }

        consumePutSegmentResponses(requestCount);
    }

    /**
     * Sends the put segment message (struct {@code p_sgmt_blob}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param handle
     *         blob handle
     * @param b
     *         byte array
     * @param off
     *         offset into byte array
     * @param len
     *         number of bytes to write
     * @throws IOException
     *         for errors writing to the output stream, or invalid {@code off} or {@code len} for {@code b}
     * @since 7
     */
    protected void sendPutSegmentMsg(XdrOutputStream xdrOut, int handle, byte[] b, int off, int len)
            throws IOException {
        xdrOut.writeInt(op_put_segment); // p_operation
        xdrOut.writeInt(handle); // p_sgmt_blob
        xdrOut.writeInt(len); // p_sgmt_length
        xdrOut.writeBuffer(b, off, len); // p_sgmt_segment
    }

    protected void consumePutSegmentResponses(int requestCount) throws SQLException {
        if (requestCount == 0) return;
        var chain = new SQLExceptionChainBuilder();
        try {
            while (requestCount-- > 0) {
                consumePutSegmentResponse(chain);
            }
            chain.throwIfPresent();
        } catch (IOException e) {
            getDatabase().consumePackets(requestCount, w -> {});
            SQLException exception = FbExceptionBuilder.ioReadError(e);
            chain.optException().ifPresent(exception::addSuppressed);
            throw exception;
        }
    }

    private void consumePutSegmentResponse(SQLExceptionChainBuilder chain) throws IOException {
        try {
            getDatabase().readResponse(null);
        } catch (SQLException e) {
            chain.append(e);
        }
    }

}
