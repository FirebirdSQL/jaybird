// SPDX-FileCopyrightText: Copyright 2025-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.CachedInfoResponse;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version10.V10InputBlob;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.System.Logger.Level.DEBUG;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_blob;

/**
 * Input {@link org.firebirdsql.gds.ng.wire.FbWireBlob} implementation for the version 11 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 5.0.7
 */
public class V11InputBlob extends V10InputBlob {

    private CachedInfoResponse cachedBlobInfo = CachedInfoResponse.empty();

    public V11InputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        super(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    public void open() throws SQLException {
        try (var ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobClosed();
            clearDeferredException();

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
                deferredOpen();
                getBlobInfoOnDeferredOpen();
            }
        }
    }

    private void deferredOpen() throws SQLException {
        sendOpen(BlobOpenOperation.INPUT_BLOB, false);
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
                    System.getLogger(getClass().getName()).log(DEBUG,
                            "Expected response of type GenericResponse for blob open, but received a {0}",
                            response != null ? response.getClass().getName() : "(null)");
                }
            }
        }, Function.identity()));
    }

    private void getBlobInfoOnDeferredOpen() throws SQLException {
        // Request known blob info during deferred open, normal blob info requests use FbWireDatabase.getInfo(...)
        byte[] knownBlobInfoItems = getKnownBlobInfoItems();
        if (knownBlobInfoItems.length == 0) {
            // If we don't know any items, don't perform the request; This shouldn't happen in practice, but
            // the implementation can in theory return an empty array
            return;
        }
        try {
            withTransmitLock(xdrOut -> {
                // TODO Duplicates AbstractFbWireDatabase.sendGetInfoMsg; rethink this if it needs versioning
                xdrOut.writeInt(op_info_blob);
                xdrOut.writeInt(getHandle());
                xdrOut.writeInt(0); // incarnation
                xdrOut.writeBuffer(knownBlobInfoItems);
                xdrOut.writeInt(512);
            });
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
        getDatabase().enqueueDeferredAction(wrapDeferredResponse(new DeferredResponse<>() {
            @Override
            public void onResponse(@Nullable Response response) {
                if (response instanceof GenericResponse genericResponse) {
                    processBlobInfoOnDeferredOpenResponse(genericResponse);
                } else {
                    System.getLogger(getClass().getName()).log(DEBUG,
                            "Expected response of type GenericResponse for blob info on deferred open, but received a {0}",
                            response != null ? response.getClass().getName() : "(null)");
                }
            }
        }, Function.identity()));
    }

    private void processBlobInfoOnDeferredOpenResponse(GenericResponse genericResponse) {
        cachedBlobInfo = new CachedInfoResponse(genericResponse.data());
    }

    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try (var ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();
            // Given the delayed open requests the known info items, complete it now, so we can use its response instead
            // of sending another request
            completePendingOpen();
            Optional<byte[]> fromCache = cachedBlobInfo.filteredComplete(requestItems);
            return fromCache.isPresent() ? fromCache.get() : super.getBlobInfo(requestItems, bufferLength);
        }
    }

    private void completePendingOpen() throws SQLException {
        if (getState() != BlobState.PENDING_OPEN) return;
        completePendingOpen0();
    }

    private void completePendingOpen0() throws SQLException {
        try {
            try {
                withTransmitLock(OutputStream::flush);
            } catch (IOException e) {
                throw FbExceptionBuilder.ioWriteError(e);
            }
            getDatabase().completeDeferredActions();
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

}
