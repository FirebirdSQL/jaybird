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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version10.V10InputBlob;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.ByteArrayHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_blob;

/**
 * Input {@link org.firebirdsql.gds.ng.wire.FbWireBlob} implementation for the version 11 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 5.0.7
 */
public class V11InputBlob extends V10InputBlob {

    private byte[] cachedBlobInfo = ByteArrayHelper.emptyByteArray();

    public V11InputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        super(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    public void open() throws SQLException {
        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock()) {
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
            public void onResponse(Response response) {
                if (response instanceof GenericResponse) {
                    try {
                        processOpenResponse((GenericResponse) response);
                    } catch (SQLException e) {
                        registerDeferredException(e);
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).debugf(
                            "Expected response of type GenericResponse for blob open, but received a %s",
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
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_info_blob);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(0); // incarnation
            xdrOut.writeBuffer(knownBlobInfoItems);
            xdrOut.writeInt(512);
        } catch (IOException e) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
        }
        getDatabase().enqueueDeferredAction(wrapDeferredResponse(new DeferredResponse<>() {
            @Override
            public void onResponse(Response response) {
                if (response instanceof GenericResponse) {
                    processBlobInfoOnDeferredOpenResponse((GenericResponse) response);
                } else {
                    LoggerFactory.getLogger(getClass()).debugf(
                            "Expected response of type GenericResponse for blob info on deferred open, but received a %s",
                            response != null ? response.getClass().getName() : "(null)");
                }
            }
        }, Function.identity()));
    }

    private void processBlobInfoOnDeferredOpenResponse(GenericResponse genericResponse) {
        cachedBlobInfo = genericResponse.getData();
    }

    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobOpen();
            // Given the delayed open requests the known info items, complete it now, so we can use its response instead
            // of sending another request
            completePendingOpen();
            Optional<byte[]> fromCache = blobInfoFromCache(requestItems);
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
                getXdrOut().flush();
            } catch (IOException e) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
            }
            getDatabase().getWireOperations().processDeferredActions();
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    private Optional<byte[]> blobInfoFromCache(byte[] requestItems) {
        if (cachedBlobInfo.length == 0) {
            // Nothing cached (yet), defer to server
            return Optional.empty();
        }
        try {
            ClumpletReader requested = new ClumpletReader(ClumpletReader.Kind.InfoItems, requestItems);
            ClumpletReader cached = new ClumpletReader(ClumpletReader.Kind.InfoResponse, cachedBlobInfo);
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            for (requested.rewind(); !requested.isEof(); requested.moveNext()) {
                int requestItem = requested.getClumpTag();
                if (!cached.find(requestItem)) {
                    // Unknown blob info item, defer to server
                    LoggerFactory.getLogger(getClass()).debugf(
                            "Requested blob info item %s not in cache, deferring to server", requestItem);
                    return Optional.empty();
                }
                byte[] data = cached.getBytes();
                response.write(requestItem);
                VaxEncoding.encodeVaxInteger2WithoutLength(response, data.length);
                response.write(data);
            }
            response.write(isc_info_end);
            return Optional.of(response.toByteArray());
        } catch (IOException | SQLException e) {
            LoggerFactory.getLogger(getClass()).warn("Error in blobInfoFromCache, deferring to server", e);
            return Optional.empty();
        }
    }

}
