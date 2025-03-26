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
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version10.V10OutputBlob;
import org.firebirdsql.logging.LoggerFactory;

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
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            checkTransactionActive();
            checkBlobClosed();
            clearDeferredException();

            if (getBlobId() != FbBlob.NO_BLOB_ID) {
                throw new FbExceptionBuilder().nonTransientException(isc_segstr_no_op).toSQLException();
            }

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
                sendOpen(BlobOpenOperation.OUTPUT_BLOB, false);
                getDatabase().enqueueDeferredAction(wrapDeferredResponse(new DeferredResponse<Response>() {
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
                                    "Expected response of type GenericResponse for blob create, but received a %s",
                                    response != null ? response.getClass().getName() : "(null)");
                        }
                    }
                }, Function.identity()));
            }
        }
    }

}
