// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_op;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10OutputBlob extends AbstractFbWireOutputBlob implements FbWireBlob, DatabaseListener {

    // TODO V10OutputBlob and V10InputBlob share some common behavior and information (eg in open()), find a way to unify this

    /**
     * Number of op_put_segment calls batched by {@link #put(byte[], int, int)} without retrieving response.
     */
    private static final int OUTSTANDING_PUT_SEGMENT_PACKETS = 8;

    public V10OutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) {
        super(database, transaction, blobParameterBuffer);
    }

    @Override
    public void open() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobClosed();

            if (getBlobId() != FbBlob.NO_BLOB_ID) {
                throw FbExceptionBuilder.toNonTransientException(isc_segstr_no_op);
            }

            sendOpen(BlobOpenOperation.OUTPUT_BLOB);
            receiveCreateResponse();
            // TODO Request information on the blob?
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void receiveCreateResponse() throws SQLException {
        try {
            final GenericResponse genericResponse = getDatabase().readGenericResponse(null);
            setHandle(genericResponse.objectHandle());
            setBlobId(genericResponse.blobId());
            setOpen(true);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public void put(final byte[] b, final int off, final int len) throws SQLException {
        try (LockCloseable ignored = withLock())  {
            validateBufferLength(b, off, len);
            if (len == 0) {
                throw FbExceptionBuilder.toException(jb_blobPutSegmentEmpty);
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            batchPutSegment(b, off, len);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void batchPutSegment(byte[] b, int off, int len) throws SQLException {
        int requestCount = 0;
        try {
            XdrOutputStream xdrOut = getXdrOut();
            int count = 0;
            while (count < len) {
                int segmentLength = Math.min(len - count, getMaximumSegmentSize());
                xdrOut.writeInt(op_put_segment);
                xdrOut.writeInt(getHandle());
                xdrOut.writeInt(segmentLength);
                xdrOut.writeBuffer(b, off + count, segmentLength);
                count += segmentLength;
                if (++requestCount >= OUTSTANDING_PUT_SEGMENT_PACKETS) {
                    xdrOut.flush();
                    try {
                        consumePutSegmentResponses(requestCount);
                    } finally {
                        requestCount = 0;
                    }
                }
            }
            xdrOut.flush();
        } catch (IOException e) {
            getDatabase().consumePackets(requestCount, w -> {});
            throw FbExceptionBuilder.ioWriteError(e);
        }

        consumePutSegmentResponses(requestCount);
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
