/*
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

import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version10.V10WireOperations;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11WireOperations extends V10WireOperations {

    private static final Logger log = LoggerFactory.getLogger(V11WireOperations.class);

    /**
     * Actions on this object need to be synchronized on {@link #getSynchronizationObject()}.
     */
    private final List<DeferredAction> deferredActions = new ArrayList<>();

    public V11WireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        super(connection, defaultWarningMessageCallback, syncObject);
    }

    @Override
    public final void enqueueDeferredAction(DeferredAction deferredAction) {
        synchronized (getSynchronizationObject()) {
            deferredActions.add(deferredAction);
        }
    }

    @Override
    public final void processDeferredActions() {
        synchronized (getSynchronizationObject()) {
            if (deferredActions.size() == 0) return;

            final DeferredAction[] actions = deferredActions.toArray(new DeferredAction[0]);
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
