/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version10.V10Database;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 11 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11Database extends V10Database {

    private static final Logger log = LoggerFactory.getLogger(V11Database.class, false);

    /**
     * Actions on this object need to be synchronized on {@link #getSynchronizationObject()}.
     */
    private final List<DeferredAction> deferredActions = new ArrayList<DeferredAction>();

    /**
     * Creates a V11Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected V11Database(WireConnection connection,
            ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    public final void enqueueDeferredAction(DeferredAction deferredAction) {
        synchronized (getSynchronizationObject()) {
            deferredActions.add(deferredAction);
        }
    }

    // TODO Implement trusted auth?

    @Override
    public void releaseObject(int operation, int objectId) throws SQLException {
        checkAttached();
        synchronized (getSynchronizationObject()) {
            try {
                doReleaseObjectPacket(operation, objectId);
                // NOTE: Intentionally no flush!
                enqueueDeferredAction(new DeferredAction() {
                    @Override
                    public void processResponse(Response response) {
                        processReleaseObjectResponse(response);
                    }
                    @Override
                    public WarningMessageCallback getWarningMessageCallback() {
                        return null;
                    }
                });
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
        }
    }

    @Override
    public final int readNextOperation() throws IOException {
        synchronized (getSynchronizationObject()) {
            processDeferredActions();
            return super.readNextOperation();
        }
    }

    protected void processDeferredActions() {
        synchronized (getSynchronizationObject()) {
            if (deferredActions.size() == 0) return;

            final DeferredAction[] actions = deferredActions.toArray(new DeferredAction[deferredActions.size()]);
            deferredActions.clear();
            for (DeferredAction action : actions) {
                try {
                    action.processResponse(readSingleResponse(action.getWarningMessageCallback()));
                } catch (Exception ex) {
                    // This only happen if the connection is no longer available
                    // We ignore the exception and assume the next operation by the caller will fail as well
                    log.debug("Exception in processDeferredActions", ex);
                }
            }
        }
    }
}
