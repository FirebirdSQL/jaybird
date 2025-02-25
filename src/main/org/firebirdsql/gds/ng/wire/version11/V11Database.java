// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version10.V10Database;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 11 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V11Database extends V10Database {

    /**
     * Creates a V11Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected V11Database(WireDatabaseConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    public void releaseObject(int operation, int objectId) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            doReleaseObjectPacket(operation, objectId);
            // NOTE: Intentionally no flush!
            switch (operation) {
            case op_close_blob, op_cancel_blob -> enqueueDeferredAction(new DeferredAction() {
                @Override
                public void processResponse(Response response) {
                    processReleaseObjectResponse(response);
                }

                @Override
                public boolean requiresSync() {
                    return true;
                }
            });
            // According to Firebird source code for other operations we need to process response normally,
            // however we only expect calls for op_close_blob and op_cancel_blob
            default -> throw new IllegalArgumentException(
                    "Unexpected operation in V11Databsase.releaseObject: %d".formatted(operation));
            }
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }
}
