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
