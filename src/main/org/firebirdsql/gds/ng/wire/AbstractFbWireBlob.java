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
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;

import java.io.IOException;
import java.sql.SQLException;

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
                                 BlobParameterBuffer blobParameterBuffer) {
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
     *
     * @param releaseOperation
     *         Either {@link WireProtocolConstants#op_close_blob} or {@link WireProtocolConstants#op_cancel_blob}
     * @throws SQLException
     *         For database communication errors.
     */
    protected void releaseBlob(int releaseOperation) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            getDatabase().releaseObject(releaseOperation, getHandle());
        }
    }

    /**
     * Operation codes to open an input or output blob. For use with {@link #sendOpen(BlobOpenOperation)}.
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

    protected final void sendOpen(BlobOpenOperation openOperation) throws SQLException {
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
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
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
        try {
            return getDatabase()
                    .getInfo(WireProtocolConstants.op_info_blob, getHandle(), requestItems, bufferLength, null);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
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
}
