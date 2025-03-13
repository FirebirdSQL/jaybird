// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;

import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireOutputBlob extends AbstractFbWireBlob {

    private long blobId;

    protected AbstractFbWireOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) {
        super(database, transaction, blobParameterBuffer);
    }

    @Override
    public final long getBlobId() {
        return blobId;
    }

    /**
     * Sets the blob id.
     *
     * @param blobId
     *         Blob id.
     * @throws SQLException
     *         If this is an input blob, or if this is an output blob whose blobId was already set.
     */
    protected final void setBlobId(long blobId) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (getBlobId() != FbBlob.NO_BLOB_ID) {
                throw FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_blobIdAlreadySet);
            }
            this.blobId = blobId;
        }
    }

    @Override
    protected void processOpenResponse(GenericResponse genericResponse) throws SQLException {
        setBlobId(genericResponse.blobId());
        super.processOpenResponse(genericResponse);
    }

    @Override
    public final boolean isOutput() {
        return true;
    }

    @Override
    public final byte[] getSegment(int sizeRequested) throws SQLException {
        throw readNotSupported();
    }

    private SQLException readNotSupported() {
        SQLException e = FbExceptionBuilder.toNonTransientException(ISCConstants.isc_segstr_no_read);
        errorOccurred(e);
        return e;
    }

    @Override
    protected final int get(byte[] b, int off, int len, int minLen) throws SQLException {
        throw readNotSupported();
    }

    @Override
    public final void seek(int offset, SeekMode seekMode) throws SQLException {
        throw readNotSupported();
    }
}
