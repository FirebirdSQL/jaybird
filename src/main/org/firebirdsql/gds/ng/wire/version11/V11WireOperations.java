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
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version10.V10WireOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V11WireOperations extends V10WireOperations {

    /**
     * Actions on this object need to be locked on {@link #withLock()}.
     */
    private final ArrayList<DeferredAction> deferredActions = new ArrayList<>();

    public V11WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    public final void enqueueDeferredAction(DeferredAction deferredAction) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            deferredActions.add(deferredAction);
            afterEnqueueDeferredAction();
        }
    }

    /**
     * Action to perform after the deferred action has been queued in {@link #enqueueDeferredAction(DeferredAction)}.
     * <p>
     * This method should only be called by {@link #enqueueDeferredAction(DeferredAction)}, and can be used to implement
     * forcing processing of deferred actions if too many are queued.
     * </p>
     *
     * @throws SQLException
     *         for errors forcing handling of oversized queue using {@code op_ping} (or {@code op_batch_sync})
     */
    protected void afterEnqueueDeferredAction() throws SQLException {
        // do nothing
    }

    /**
     * The number of deferred actions currently waiting.
     * <p>
     * This method should be called when locked on {@link #withLock()}.
     * </p>
     *
     * @return number of deferred actions
     */
    protected final int deferredActionCount() {
        return deferredActions.size();
    }

    /**
     * Reports if an explicit sync action is required to complete deferred actions.
     * <p>
     * For wire protocol v11 - v15, the only sync action needed is a flush, in v16 and higher an {@code op_ping} or
     * {@code op_batch_sync} is needed (in some cases, a flush would suffice, but we're considering the worst case
     * here).
     * </p>
     * <p>
     * Failure to flush or sync (depending on the protocol version) may result in indefinite blocking. The sync action
     * is not needed when deferred actions are processed as part of a normal request/response cycle (as there the
     * request will behave as the sync action).
     * </p>
     *
     * @return {@code true} if one or more of the deferred action require an explicit sync action.
     * @since 6
     */
    protected final boolean completeDeferredActionsRequiresSync() {
        return deferredActions.stream().anyMatch(DeferredAction::requiresSync);
    }

    @Override
    public void completeDeferredActions() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (completeDeferredActionsRequiresSync()) {
                // We sometimes forgo flushing of the request for deferred operations, flush to be able to complete
                getXdrOut().flush();
            }
            processDeferredActions();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    @Override
    public final void processDeferredActions() {
        try (LockCloseable ignored = withLock()) {
            if (deferredActions.isEmpty()) return;

            final DeferredAction[] actions = deferredActions.toArray(new DeferredAction[0]);
            deferredActions.clear();
            for (DeferredAction action : actions) {
                try {
                    action.processResponse(readResponse(action.getWarningMessageCallback()));
                } catch (IOException e) {
                    action.onException(FbExceptionBuilder.ioReadError(e));
                } catch (Exception e) {
                    action.onException(e);
                }
            }
            afterProcessDeferredActions(actions.length);
        }
    }

    /**
     * Can be used for additional actions after processing deferred actions (e.g. trim a large deferred actions list to
     * its default capacity).
     * <p>
     * This implementation trims if {@code processedDeferredActions > 10}. When overridden, it is recommend to call this
     * method through {@code super} to still trim (e.g. in a more limited set of circumstances) and perform any other
     * actions this method may perform. If the overridden method wants to forgo trimming, it should pass {@code -1} for
     * {@code processedDeferredActions}.
     * </p>
     *
     * @param processedDeferredActions
     *         number of processed deferred actions, or {@code -1} to ensure no trim is performed
     */
    protected void afterProcessDeferredActions(int processedDeferredActions) {
        // NOTE: The value is more or less chosen based on the default capacity of an ArrayList in Java 17
        if (processedDeferredActions > 10) {
            deferredActions.trimToSize();
        }
    }
}
