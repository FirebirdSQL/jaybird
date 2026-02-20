// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.AbstractFbBlob;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_op;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.jaybird.util.ByteArrayHelper.validateBufferLength;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbBlob} for native client access.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class JnaBlob extends AbstractFbBlob implements FbBlob, DatabaseListener {

    private final LongByReference blobId;
    private final boolean outputBlob;
    private final IntByReference jnaHandle = new IntByReference(0);
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];
    private final FbClientLibrary clientLibrary;
    private @Nullable ByteBuffer byteBuffer;

    /**
     * Creates a blob for output (writing to the database).
     *
     * @param database
     *         database
     * @param transaction
     *         transaction
     * @param blobParameterBuffer
     *         blob parameter buffer
     */
    public JnaBlob(JnaDatabase database, JnaTransaction transaction, BlobParameterBuffer blobParameterBuffer)
            throws SQLException {
        this(database, transaction, blobParameterBuffer, NO_BLOB_ID, true);
    }

    /**
     * Creates a blob for input (reading from the database).
     *
     * @param database
     *         database
     * @param transaction
     *         transaction
     * @param blobParameterBuffer
     *         blob parameter buffer
     * @param blobId
     *         blob id
     */
    public JnaBlob(JnaDatabase database, JnaTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        this(database, transaction, blobParameterBuffer, blobId, false);
    }

    private JnaBlob(JnaDatabase database, JnaTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId, boolean outputBlob) throws SQLException {
        super(database, transaction, blobParameterBuffer);
        this.blobId = new LongByReference(blobId);
        this.outputBlob = outputBlob;
        clientLibrary = database.getClientLibrary();
    }

    @Override
    public JnaDatabase getDatabase() {
        return (JnaDatabase) super.getDatabase();
    }

    @Override
    public @Nullable JnaTransaction getTransaction() {
        return (JnaTransaction) super.getTransaction();
    }

    @Override
    protected final JnaTransaction requireActiveTransaction() throws SQLException {
        return (JnaTransaction) super.requireActiveTransaction();
    }

    @Override
    public int getHandle() {
        return jnaHandle.getValue();
    }

    public final IntByReference getJnaHandle() {
        return jnaHandle;
    }

    @Override
    public final long getBlobId() {
        return blobId.getValue();
    }

    @Override
    public void open() throws SQLException {
        try {
            if (isOutput() && getBlobId() != NO_BLOB_ID) {
                throw FbExceptionBuilder.toNonTransientException(isc_segstr_no_op);
            }

            byte[] bpb = getBlobParameterBuffer().toBytesWithType();
            try (var ignored = withLock()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobClosed();
                clearDeferredException();

                JnaDatabase db = getDatabase();
                JnaTransaction transaction = requireActiveTransaction();
                if (isOutput()) {
                    clientLibrary.isc_create_blob2(statusVector, db.getJnaHandle(), transaction.getJnaHandle(),
                            getJnaHandle(), blobId, (short) bpb.length, bpb);
                } else {
                    clientLibrary.isc_open_blob2(statusVector, db.getJnaHandle(), transaction.getJnaHandle(),
                            getJnaHandle(), blobId, (short) bpb.length, bpb);
                }
                processStatusVector();
                setState(BlobState.OPEN);
                resetEof();
                throwAndClearDeferredException();
            }
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final boolean isOutput() {
        return outputBlob;
    }

    @Override
    public byte[] getSegment(int sizeRequested) throws SQLException {
        try (var ignored = withLock()) {
            if (sizeRequested <= 0) {
                throw FbExceptionBuilder.forException(jb_blobGetSegmentNegative)
                        .messageParameter(sizeRequested)
                        .toSQLException();
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            if (isEof()) {
                return ByteArrayHelper.emptyByteArray();
            }

            ShortByReference actualLength = new ShortByReference();
            ByteBuffer responseBuffer = getSegment0(sizeRequested, actualLength);
            throwAndClearDeferredException();
            byte[] segment = new byte[actualLength.getValue() & 0xFFFF];
            responseBuffer.get(segment);
            return segment;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    private ByteBuffer getSegment0(int sizeRequested, ShortByReference actualLength) throws SQLException {
        sizeRequested = Math.min(sizeRequested, getMaximumSegmentSize());
        ByteBuffer responseBuffer = getByteBuffer(sizeRequested);
        clientLibrary.isc_get_segment(statusVector, getJnaHandle(), actualLength, (short) sizeRequested,
                responseBuffer);
        int status = statusVector[1].intValue();
        // status 0 means: more to come, isc_segment means: buffer was too small, rest will be returned on next call
        if (status == ISCConstants.isc_segstr_eof) {
            setEof();
        } else if (!(status == 0 || status == ISCConstants.isc_segment)) {
            processStatusVector();
        }
        return responseBuffer;
    }

    @Override
    protected int get(final byte[] b, final int off, final int len, final int minLen) throws SQLException {
        try (var ignored = withLock())  {
            validateBufferLength(b, off, len);
            if (len == 0) return 0;
            if (minLen <= 0 || minLen > len ) {
                throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidStringLength)
                        .messageParameter("minLen", len, minLen)
                        .toSQLException();
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            var actualLength = new ShortByReference();
            int count = 0;
            while (count < minLen && !isEof()) {
                // We honor the configured buffer size unless we somehow already allocated a bigger buffer earlier
                ByteBuffer segmentBuffer = getSegment0(
                        Math.min(len - count, Math.max(getBlobBufferSize(), currentBufferCapacity())),
                        actualLength);
                int dataLength = actualLength.getValue() & 0xFFFF;
                segmentBuffer.get(b, off + count, dataLength);
                count += dataLength;
            }
            throwAndClearDeferredException();
            return count;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    private int getBlobBufferSize() {
        return getDatabase().getConnectionProperties().getBlobBufferSize();
    }

    @Override
    public void put(final byte[] b, final int off, final int len) throws SQLException {
        try (var ignored = withLock()) {
            validateBufferLength(b, off, len);
            if (len == 0) {
                throw FbExceptionBuilder.toException(jb_blobPutSegmentEmpty);
            }
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            int count = 0;
            if (off == 0) {
                // no additional buffer allocation needed, so we can send with max segment size
                count = Math.min(len, getMaximumSegmentSize());
                clientLibrary.isc_put_segment(statusVector, getJnaHandle(), (short) count, b);
                processStatusVector();
                if (count == len) {
                    // put complete
                    return;
                }
            }

            byte[] segmentBuffer =
                    new byte[Math.min(len - count, Math.min(getBlobBufferSize(), getMaximumSegmentSize()))];
            while (count < len) {
                int segmentLength = Math.min(len - count, segmentBuffer.length);
                System.arraycopy(b, off + count, segmentBuffer, 0, segmentLength);
                clientLibrary.isc_put_segment(statusVector, getJnaHandle(), (short) segmentLength, segmentBuffer);
                processStatusVector();
                count += segmentLength;
            }
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void seek(int offset, SeekMode seekMode) throws SQLException {
        try (var ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();

            // result is the current position in the blob (see .NET provider source)
            // We ignore the result TODO check if useful; not used in wire protocol either
            IntByReference result = new IntByReference();
            clientLibrary.isc_seek_blob(statusVector, getJnaHandle(), (short) seekMode.getSeekModeId(), offset,
                    result);
            processStatusVector();
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer;
            try (var ignored = withLock()) {
                responseBuffer = getByteBuffer(bufferLength);
                checkDatabaseAttached();
                checkBlobOpen();
                clientLibrary.isc_blob_info(statusVector, getJnaHandle(),
                        (short) requestItems.length, requestItems,
                        (short) bufferLength, responseBuffer);
                processStatusVector();
                throwAndClearDeferredException();
            }
            byte[] responseArr = new byte[bufferLength];
            responseBuffer.get(responseArr);
            return responseArr;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    protected void closeImpl() throws SQLException {
        try {
            clientLibrary.isc_close_blob(statusVector, getJnaHandle());
            processStatusVector();
        } finally {
            releaseResources();
        }
    }

    @Override
    protected void cancelImpl() throws SQLException {
        try {
            clientLibrary.isc_cancel_blob(statusVector, getJnaHandle());
            processStatusVector();
        } finally {
            releaseResources();
        }
    }

    @Override
    protected void releaseResources() {
        byteBuffer = null;
    }

    private void processStatusVector() throws SQLException {
        getDatabase().processStatusVector(statusVector, null);
    }

    private ByteBuffer getByteBuffer(int requiredSize) {
        ByteBuffer byteBuffer = this.byteBuffer;
        if (byteBuffer == null || byteBuffer.capacity() < requiredSize) {
            // Allocate buffer in increments of 512
            return this.byteBuffer = ByteBuffer.allocateDirect((1 + (requiredSize - 1) / 512) * 512);
        }
        byteBuffer.clear();
        return byteBuffer;
    }

    private int currentBufferCapacity() {
        ByteBuffer byteBuffer = this.byteBuffer;
        return byteBuffer != null ? byteBuffer.capacity() : 0;
    }

}
