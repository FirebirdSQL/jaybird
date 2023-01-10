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
import org.firebirdsql.gds.ng.AbstractFbBlob;
import org.firebirdsql.gds.ng.LockCloseable;

import java.sql.SQLException;

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
}
