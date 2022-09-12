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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version10.V10WireOperations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11WireOperations extends V10WireOperations {

    /**
     * Actions on this object need to be locked on {@link #withLock()}.
     */
    private final List<DeferredAction> deferredActions = new ArrayList<>();

    public V11WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    public final void enqueueDeferredAction(DeferredAction deferredAction) {
        try (LockCloseable ignored = withLock()) {
            deferredActions.add(deferredAction);
        }
    }

    @Override
    public final void processDeferredActions() {
        try (LockCloseable ignored = withLock()) {
            if (deferredActions.size() == 0) return;

            final DeferredAction[] actions = deferredActions.toArray(new DeferredAction[0]);
            deferredActions.clear();
            for (DeferredAction action : actions) {
                try {
                    action.processResponse(readResponse(action.getWarningMessageCallback()));
                } catch (IOException ex) {
                    action.onException(FbExceptionBuilder.forException(ISCConstants.isc_net_read_err).cause(ex)
                            .toSQLException());
                } catch (Exception ex) {
                    action.onException(ex);
                }
            }
        }
    }
}
