package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.ptr.LongByReference;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.AbstractFbBlob;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentTooLong;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IBlobImpl extends AbstractFbBlob implements FbBlob, DatabaseListener {

    private final LongByReference blobId;
    private final boolean outputBlob;
    private ByteBuffer byteBuffer;
    private IBlob blob;

    public IBlobImpl(IDatabaseImpl database, ITransactionImpl transaction, BlobParameterBuffer blobParameterBuffer) {
        this(database, transaction, blobParameterBuffer, NO_BLOB_ID);
    }

    public IBlobImpl(IDatabaseImpl database, ITransactionImpl transaction, BlobParameterBuffer blobParameterBuffer,
                   long blobId) {
        super(database, transaction, blobParameterBuffer);
        this.blobId = new LongByReference(blobId);
        outputBlob = blobId == NO_BLOB_ID;
    }

    @Override
    protected void closeImpl() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                IDatabaseImpl database = (IDatabaseImpl)getDatabase();
                blob.close(database.getStatus());
            } finally {
                byteBuffer = null;
            }
        }
    }

    @Override
    protected void cancelImpl() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                IDatabaseImpl database = (IDatabaseImpl)getDatabase();
                blob.cancel(database.getStatus());
            } finally {
                byteBuffer = null;
            }
        }
    }

    @Override
    public long getBlobId() {
        return blobId.getValue();
    }

    @Override
    public int getHandle() {
        throw new UnsupportedOperationException( "Native OO API not support blob handle" );
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

                IDatabaseImpl database = (IDatabaseImpl)getDatabase();
                IAttachment attachment = database.getAttachment();
                if (isOutput()) {
                    blob = attachment.createBlob(database.getStatus(), ((ITransactionImpl)getTransaction()).getTransaction(),
                            blobId, bpb.length, bpb);
                } else {
                    blob = attachment.openBlob(database.getStatus(), ((ITransactionImpl)getTransaction()).getTransaction(),
                            blobId, bpb.length, bpb);
                }
                setOpen(true);
                resetEof();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public boolean isOutput() {
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
            final com.sun.jna.Pointer actualLength = new CloseableMemory(1024);
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();
                responseBuffer = getByteBuffer(sizeRequested);
                try (CloseableMemory memory = new CloseableMemory(sizeRequested)) {

                    IDatabaseImpl database = (IDatabaseImpl) getDatabase();
                    IStatus status = database.getStatus();
                    int result = blob.getSegment(status, sizeRequested, memory, actualLength);
                    // result 0 means: more to come, isc_segment means: buffer was too small,
                    // rest will be returned on next call
                    if (!(IStatus.RESULT_OK == result || result == IStatus.RESULT_SEGMENT)) {
                        if (result == IStatus.RESULT_NO_DATA) {
                            setEof();
                        }
                    }
                    memory.read(0, responseBuffer.array(), 0, sizeRequested);
                }
            }
            final int actualLengthInt = actualLength.getInt(0) & 0xFFFF;
            ((CloseableMemory) actualLength).close();
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

                try (CloseableMemory memory = new CloseableMemory(segment.length)) {
                    memory.write(0, segment, 0, segment.length);

                    IDatabaseImpl database = (IDatabaseImpl) getDatabase();
                    IStatus status = database.getStatus();
                    blob.putSegment(status, segment.length, memory);
                }
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

                IDatabaseImpl database = (IDatabaseImpl)getDatabase();
                IStatus status = database.getStatus();
                // result is the current position in the blob
                // We ignore the result
                blob.seek(status, seekMode.getSeekModeId(), offset);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            byte[] responseArr = new byte[bufferLength];
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkBlobOpen();

                IDatabaseImpl database = (IDatabaseImpl)getDatabase();
                IStatus status = database.getStatus();
                blob.getInfo(status, requestItems.length, requestItems, bufferLength, responseArr);
            }

            return responseArr;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private ByteBuffer getByteBuffer(int requiredSize) {
        if (byteBuffer == null || byteBuffer.capacity() < requiredSize) {
            byteBuffer = ByteBuffer.allocate(requiredSize);
        } else {
            byteBuffer.clear();
        }
        return byteBuffer;
    }
}
