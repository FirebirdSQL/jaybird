// SPDX-FileCopyrightText: Copyright 2025-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version10.V10OutputBlob;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.function.Function;

import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_op;

/**
 * Output {@link org.firebirdsql.gds.ng.wire.FbWireBlob} implementation for the version 11 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 5.0.7
 */
public class V11OutputBlob extends V10OutputBlob {

    public V11OutputBlob(FbWireDatabase database, FbWireTransaction transaction,
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

            setHandle(WireProtocolConstants.INVALID_OBJECT);
            setState(BlobState.DELAYED_OPEN);
            resetEof();
        }
    }

    @Override
    protected void checkBlobOpen() throws SQLException {
        try (var ignored = withLock()) {
            super.checkBlobOpen();
            if (getState() == BlobState.DELAYED_OPEN) {
                setState(BlobState.PENDING_OPEN);
                sendOpen(BlobOpenOperation.OUTPUT_BLOB, false);
                getDatabase().enqueueDeferredAction(wrapDeferredResponse(new DeferredResponse<>() {
                    @Override
                    public void onResponse(@Nullable Response response) {
                        if (response instanceof GenericResponse genericResponse) {
                            try {
                                processOpenResponse(genericResponse);
                            } catch (SQLException e) {
                                registerDeferredException(e);
                            }
                        } else {
                            System.getLogger(getClass().getName()).log(System.Logger.Level.DEBUG,
                                    "Expected response of type GenericResponse for blob create, but received a {0}",
                                    response != null ? response.getClass().getName() : "(null)");
                        }
                    }
                }, Function.identity()));
            }
        }
    }

}
