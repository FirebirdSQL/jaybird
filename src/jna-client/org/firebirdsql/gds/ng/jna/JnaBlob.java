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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.AbstractFbBlob;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentTooLong;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbBlob} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaBlob extends AbstractFbBlob implements FbBlob, DatabaseListener {

    private final LongByReference blobId;
    private final boolean outputBlob;
    private final IntByReference jnaHandle = new IntByReference(0);
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];
    private final FbClientLibrary clientLibrary;
    private ByteBuffer byteBuffer;

    public JnaBlob(JnaDatabase database, JnaTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        this(database, transaction, blobParameterBuffer, NO_BLOB_ID);
    }

    public JnaBlob(JnaDatabase database, JnaTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) {
        super(database, transaction, blobParameterBuffer);
        this.blobId = new LongByReference(blobId);
        outputBlob = blobId == NO_BLOB_ID;
        clientLibrary = database.getClientLibrary();
    }

    @Override
    public JnaDatabase getDatabase() {
        return (JnaDatabase) super.getDatabase();
    }

    @Override
    public JnaTransaction getTransaction() {
        return (JnaTransaction) super.getTransaction();
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
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_op).toSQLException();
            }

            final BlobParameterBuffer blobParameterBuffer = getBlobParameterBuffer();
            final byte[] bpb;
            if (blobParameterBuffer != null) {
                bpb = blobParameterBuffer.toBytesWithType();
            } else {
                bpb = new byte[0];
            }
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobClosed();

                final JnaDatabase db = getDatabase();
                if (isOutput()) {
                    clientLibrary.isc_create_blob2(statusVector, db.getJnaHandle(), getTransaction().getJnaHandle(),
                            getJnaHandle(), blobId, (short) bpb.length, bpb);
                } else {
                    clientLibrary.isc_open_blob2(statusVector, db.getJnaHandle(), getTransaction().getJnaHandle(),
                            getJnaHandle(), blobId, (short) bpb.length, bpb);
                }
                processStatusVector();
                setOpen(true);
                resetEof();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final boolean isOutput() {
        return outputBlob;
    }

    @Override
    public byte[] getSegment(int sizeRequested) throws SQLException {
        try {
            if (sizeRequested <= 0) {
                throw new FbExceptionBuilder().exception(jb_blobGetSegmentNegative)
                        .messageParameter(sizeRequested)
                        .toSQLException();
            }
            // TODO Honour request for larger sizes by looping?
            sizeRequested = Math.min(sizeRequested, getMaximumSegmentSize());
            final ByteBuffer responseBuffer;
            final ShortByReference actualLength = new ShortByReference();
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();
                responseBuffer = getByteBuffer(sizeRequested);

                clientLibrary.isc_get_segment(statusVector, getJnaHandle(), actualLength, (short) sizeRequested,
                        responseBuffer);
                final int status = statusVector[1].intValue();
                // status 0 means: more to come, isc_segment means: buffer was too small, rest will be returned on next call
                if (!(status == 0 || status == ISCConstants.isc_segment)) {
                    if (status == ISCConstants.isc_segstr_eof) {
                        setEof();
                    } else {
                        processStatusVector();
                    }
                }
            }
            final int actualLengthInt = ((int) actualLength.getValue()) & 0xFFFF;
            final byte[] segment = new byte[actualLengthInt];
            responseBuffer.get(segment);
            return segment;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void putSegment(byte[] segment) throws SQLException {
        try {
            if (segment.length == 0) {
                throw new FbExceptionBuilder().exception(jb_blobPutSegmentEmpty).toSQLException();
            }
            // TODO Handle by performing multiple puts? (Wrap in byte buffer, use position to move pointer?)
            if (segment.length > getMaximumSegmentSize()) {
                throw new FbExceptionBuilder().exception(jb_blobPutSegmentTooLong).toSQLException();
            }
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();

                clientLibrary.isc_put_segment(statusVector, getJnaHandle(), (short) segment.length, segment);
                processStatusVector();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void seek(int offset, SeekMode seekMode) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();

                // result is the current position in the blob (see .NET provider source)
                // We ignore the result TODO check if useful; not used in wire protocol either
                IntByReference result = new IntByReference();
                clientLibrary.isc_seek_blob(statusVector, getJnaHandle(), (short) seekMode.getSeekModeId(), offset,
                        result);
                processStatusVector();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer;
            synchronized (getSynchronizationObject()) {
                responseBuffer = getByteBuffer(bufferLength);
                checkDatabaseAttached();
                clientLibrary.isc_blob_info(statusVector, getJnaHandle(),
                        (short) requestItems.length, requestItems,
                        (short) bufferLength, responseBuffer);
                processStatusVector();
            }

            byte[] responseArr = new byte[bufferLength];
            responseBuffer.get(responseArr);
            return responseArr;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    protected void closeImpl() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_close_blob(statusVector, getJnaHandle());
                processStatusVector();
            } finally {
                byteBuffer = null;
            }
        }
    }

    @Override
    protected void cancelImpl() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_cancel_blob(statusVector, getJnaHandle());
                processStatusVector();
            } finally {
                byteBuffer = null;
            }
        }
    }

    private void processStatusVector() throws SQLException {
        getDatabase().processStatusVector(statusVector, null);
    }

    private ByteBuffer getByteBuffer(int requiredSize) {
        if (byteBuffer == null || byteBuffer.capacity() < requiredSize) {
            byteBuffer = ByteBuffer.allocateDirect(requiredSize);
        } else {
            byteBuffer.clear();
        }
        return byteBuffer;
    }
}
