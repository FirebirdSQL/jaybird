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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractFbBlob;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_create_blob;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_create_blob2;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_open_blob;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_open_blob2;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireBlob extends AbstractFbBlob implements FbWireBlob {

    private int blobHandle;

    protected AbstractFbWireBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) throws SQLException {
        super(database, transaction, blobParameterBuffer);
    }

    @Override
    public FbWireDatabase getDatabase() {
        return (FbWireDatabase) super.getDatabase();
    }

    @Override
    public final int getHandle() {
        try (LockCloseable ignored = withLock()) {
            return blobHandle;
        }
    }

    /**
     * @param blobHandle
     *         The Firebird blob handle identifier
     */
    protected final void setHandle(int blobHandle) {
        try (LockCloseable ignored = withLock()) {
            this.blobHandle = blobHandle;
        }
    }

    /**
     * Release this blob with the specified operation.
     * <p>
     * Implementations <strong>should only</strong> do the operation and not perform any further clean up or checks
     * on attached database and active transaction, as those checks and clean up should be done by the caller.
     * </p>
     * <p>
     * If the blob state is {@link org.firebirdsql.gds.ng.AbstractFbBlob.BlobState#DELAYED_OPEN}, this method is
     * effectively a no-op.
     * </p>
     *
     * @param releaseOperation
     *         Either {@link WireProtocolConstants#op_close_blob} or {@link WireProtocolConstants#op_cancel_blob}
     * @throws SQLException
     *         For database communication errors.
     */
    protected void releaseBlob(int releaseOperation) throws SQLException {
        try (var ignored = withLock()) {
            // If DELAYED_OPEN (but not PENDING_OPEN), there is nothing to release as nothing was opened server-side
            if (getState() == BlobState.DELAYED_OPEN) return;
            getDatabase().releaseObject(releaseOperation, getHandle());
        }
    }

    /**
     * Operation codes to open an input or output blob. For use with {@link #sendOpen(BlobOpenOperation, boolean)}.
     */
    protected enum BlobOpenOperation {
        INPUT_BLOB(op_open_blob, op_open_blob2),
        OUTPUT_BLOB(op_create_blob, op_create_blob2);

        private final int opCodeWithoutBpb;
        private final int opCodeWithBpb;

        BlobOpenOperation(int opCodeWithoutBpb, int opCodeWithBpb) {
            this.opCodeWithoutBpb = opCodeWithoutBpb;
            this.opCodeWithBpb = opCodeWithBpb;
        }

        public final int opCodeWithoutBpb() {
            return opCodeWithoutBpb;
        }

        public final int opCodeWithBpb() {
            return opCodeWithBpb;
        }
    }

    protected final void sendOpen(BlobOpenOperation openOperation, boolean flush) throws SQLException {
        try {
            XdrOutputStream xdrOut = getXdrOut();
            BlobParameterBuffer blobParameterBuffer = getBlobParameterBuffer();
            if (blobParameterBuffer == null) {
                xdrOut.writeInt(openOperation.opCodeWithoutBpb());
            } else {
                xdrOut.writeInt(openOperation.opCodeWithBpb());
                xdrOut.writeTyped(blobParameterBuffer);
            }
            xdrOut.writeInt(getTransaction().getHandle());
            xdrOut.writeLong(getBlobId());
            if (flush) xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    protected final void receiveOpenResponse() throws SQLException {
        try {
            processOpenResponse(getDatabase().readGenericResponse(null));
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    protected void processOpenResponse(GenericResponse genericResponse) throws SQLException {
        setHandle(genericResponse.objectHandle());
        setState(BlobState.OPEN);
    }

    @Override
    protected void closeImpl() throws SQLException {
        try {
            releaseBlob(WireProtocolConstants.op_close_blob);
        } finally {
            releaseResources();
        }
    }

    @Override
    protected void cancelImpl() throws SQLException {
        try {
            releaseBlob(WireProtocolConstants.op_cancel_blob);
        } finally {
            releaseResources();
        }
    }

    @Override
    protected void releaseResources() {
        // Nothing to release
    }

    // NOTE If we need to override some of the blob operations below in the future, consider introducing a separate
    // object that is injected by the ProtocolDescriptor so that we don't need to override separately for input and output.

    @Override
    public byte[] getBlobInfo(final byte[] requestItems, final int bufferLength) throws SQLException {
        try (var ignored = withLock()) {
            checkBlobOpen();
            byte[] result = getDatabase()
                    .getInfo(WireProtocolConstants.op_info_blob, getHandle(), requestItems, bufferLength, null);
            throwAndClearDeferredException();
            return result;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    /**
     * Gets the XdrInputStream.
     *
     * @return instance of XdrInputStream
     * @throws SQLException
     *         if no connection is opened or when exceptions occur retrieving the InputStream
     * @since 6
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * Gets the XdrOutputStream.
     *
     * @return instance of XdrOutputStream
     * @throws SQLException
     *         if no connection is opened or when exceptions occur retrieving the OutputStream
     * @since 6
     */
    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getXdrStreamAccess().getXdrOut();
    }

    private XdrStreamAccess getXdrStreamAccess() {
        return getDatabase().getXdrStreamAccess();
    }

    /**
     * Wraps a deferred response to produce a deferred action that can be added using
     * {@link FbWireDatabase#enqueueDeferredAction(DeferredAction)}, notifying the exception listener of this blob for
     * exceptions.
     * <p>
     * This should only be used with protocol versions that support deferred responses. Its placement in the hierarchy
     * is due to support for both input and output blobs.
     * </p>
     *
     * @param deferredResponse
     *         deferred response to wrap
     * @param responseMapper
     *         Function to map a {@link Response} to the object expected by the deferred response
     * @param <T>
     *         type of deferred response
     * @return deferred action
     * @since 5.0.7
     */
    protected final <T> DeferredAction wrapDeferredResponse(DeferredResponse<T> deferredResponse,
            Function<Response, T> responseMapper) {
        return DeferredAction.wrapDeferredResponse(deferredResponse, responseMapper, null,
                this::deferredExceptionHandler, false);
    }

    /**
     * Handler for exceptions to a deferred response.
     * <p>
     * If the exception is a {@code SQLException} or {@code IOException}, the exception listener dispatcher is notified.
     * </p>
     *
     * @param exception
     *         exception received in a deferred response, or thrown while receiving the deferred response
     * @since 5.0.7
     */
    private void deferredExceptionHandler(Exception exception) {
        if (exception instanceof SQLException sqle) {
            registerDeferredException(sqle);
            // NOTE: Intentionally *not* calling AbstractFbBlob.errorOccurred
            exceptionListenerDispatcher.errorOccurred(sqle);
        } else if (exception instanceof IOException ioe) {
            // NOTE: Intentionally *not* calling AbstractFbBlob.errorOccurred
            exceptionListenerDispatcher.errorOccurred(FbExceptionBuilder.ioReadError(ioe));
        }
    }

}
