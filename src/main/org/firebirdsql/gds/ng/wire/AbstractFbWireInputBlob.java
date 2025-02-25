// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireInputBlob extends AbstractFbWireBlob {

    private final long blobId;

    protected AbstractFbWireInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) {
        super(database, transaction, blobParameterBuffer);
        this.blobId = blobId;
    }

    @Override
    public final long getBlobId() {
        return blobId;
    }

    @Override
    public final boolean isOutput() {
        return false;
    }

    @Override
    public final void putSegment(byte[] segment) throws SQLException {
        throw writeNotSupported();
    }

    @Override
    public final void put(byte[] b, int off, int len) throws SQLException {
        throw writeNotSupported();
    }

    private SQLException writeNotSupported() {
        SQLException e = FbExceptionBuilder.toNonTransientException(ISCConstants.isc_segstr_no_write);
        exceptionListenerDispatcher.errorOccurred(e);
        return e;
    }

}
