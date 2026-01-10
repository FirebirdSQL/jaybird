// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.ng.AbstractFbStatement;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jaybird.util.Cleaners;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireStatement extends AbstractFbStatement implements FbWireStatement {

    private final Map<RowDescriptor, byte[]> blrCache = new WeakHashMap<>();
    private volatile int handle = WireProtocolConstants.INVALID_OBJECT;
    private final FbWireDatabase database;
    private Cleaner.Cleanable cleanable = Cleaners.getNoOp();

    protected AbstractFbWireStatement(FbWireDatabase database) {
        this.database = requireNonNull(database, "database");
    }

    @Override
    public final LockCloseable withLock() {
        return database.withLock();
    }

    /**
     * @see XdrStreamAccess#getXdrIn()
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * @see XdrStreamAccess#withTransmitLock(TransmitAction)
     * @since 7
     */
    protected final void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException {
        getXdrStreamAccess().withTransmitLock(transmitAction);
    }

    private XdrStreamAccess getXdrStreamAccess() {
        return database.getXdrStreamAccess();
    }

    @Override
    public final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public final int getHandle() {
        return handle;
    }

    protected final void setHandle(int handle) {
        this.handle = handle;
        cleanable = Cleaners.getJbCleaner().register(this, new CleanupAction(this));
    }

    @Override
    public FbWireTransaction getTransaction() {
        return (FbWireTransaction) super.getTransaction();
    }

    /**
     * Returns the (possibly cached) blr byte array for a {@link RowDescriptor}, or {@code null} if the parameter is null.
     *
     * @param rowDescriptor
     *         The row descriptor.
     * @return blr byte array or {@code null} when {@code rowDescriptor} is {@code null}
     * @throws SQLException
     *         When the {@link RowDescriptor} contains an unsupported field type.
     */
    @SuppressWarnings("java:S1168")
    protected final byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException {
        if (rowDescriptor == null) return null;
        byte[] blr = blrCache.get(rowDescriptor);
        if (blr == null) {
            blr = getBlrCalculator().calculateBlr(rowDescriptor);
            blrCache.put(rowDescriptor, blr);
        }
        return blr;
    }

    /**
     * Returns the blr byte array for a {@link RowValue}, or {@code null} if the parameter is null.
     * <p>
     * Contrary to {@link #calculateBlr(org.firebirdsql.gds.ng.fields.RowDescriptor)}, it is not allowed
     * to cache this value as it depends on the actual row value.
     * </p>
     *
     * @param rowValue
     *         The row value.
     * @return blr byte array or {@code null} when {@code rowValue} is {@code null}
     * @throws SQLException
     *         When the {@link RowValue} contains an unsupported field type.
     */
    @SuppressWarnings("java:S1168")
    protected final byte[] calculateBlr(RowDescriptor rowDescriptor, RowValue rowValue) throws SQLException {
        if (rowDescriptor == null || rowValue == null) return null;
        return getBlrCalculator().calculateBlr(rowDescriptor, rowValue);
    }

    /**
     * @return The {@link BlrCalculator} instance for this statement (currently always the one from
     * the {@link FbWireDatabase} instance).
     */
    protected final BlrCalculator getBlrCalculator() {
        return getDatabase().getBlrCalculator();
    }

    @Override
    public final void close() throws SQLException {
        try {
            super.close();
        } finally {
            // TODO Preferably this should be done elsewhere and AbstractFbStatement.close() should be final
            cleanable.clean();
            try (LockCloseable ignored = withLock()) {
                blrCache.clear();
            }
        }
    }

    @Override
    protected boolean isValidTransactionClass(Class<? extends FbTransaction> transactionClass) {
        return FbWireTransaction.class.isAssignableFrom(transactionClass);
    }

    @Override
    public final RowDescriptor emptyRowDescriptor() {
        return database.emptyRowDescriptor();
    }

    @Override
    public byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            checkStatementValid();
            return getInfo(WireProtocolConstants.op_info_sql, requestItems, bufferLength);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected byte[] getInfo(int operation, byte[] requestItems, int bufferLength) throws SQLException {
        return getDatabase().getInfo(operation, getHandle(), requestItems, bufferLength, getStatementWarningCallback());
    }

    /**
     * Handle the inline blob response from an {@code op_execute2} or {@code op_fetch_response}.
     *
     * @param inlineBlobResponse
     *         inline blob response
     * @since 7
     */
    protected void handleInlineBlobResponse(InlineBlobResponse inlineBlobResponse) {
        // ignored
    }

    /**
     * Wraps a deferred response to produce a deferred action that can be added
     * using {@link FbWireDatabase#enqueueDeferredAction(DeferredAction)}, notifying the exception listener of this
     * statement for exceptions, and forcing the ERROR state for IO errors.
     *
     * @param deferredResponse
     *         deferred response to wrap
     * @param responseMapper
     *         Function to map a {@link Response} to the response object expected by the deferred response
     * @param requiresSync
     *         {@code true} if the deferred response requires a sync action or flush before it can be processed
     * @param <T>
     *         type of deferred response
     * @return deferred action
     */
    protected final <T> DeferredAction wrapDeferredResponse(DeferredResponse<T> deferredResponse,
            Function<Response, T> responseMapper, boolean requiresSync) {
        return DeferredAction.wrapDeferredResponse(deferredResponse, responseMapper, getStatementWarningCallback(),
                this::deferredExceptionHandler, requiresSync);
    }

    /**
     * Handler for exceptions to a deferred response.
     * <p>
     * If the exception is a {@code SQLException}, the exception listener dispatcher is notified. If the exception
     * or its cause is an {@code IOException}, the statement state is forced to {@code ERROR}.
     * </p>
     *
     * @param exception exception received in a deferred response, or thrown while receiving the deferred response
     */
    private void deferredExceptionHandler(Exception exception) {
        if (exception instanceof SQLException sqle) {
            exceptionListenerDispatcher.errorOccurred(sqle);
        }
        if (exception instanceof IOException || exception.getCause() instanceof IOException) {
            forceState(StatementState.ERROR);
        }
    }

    @SuppressWarnings("resource")
    private static final class CleanupAction implements Runnable, StatementListener, DatabaseListener {

        private static final AtomicReferenceFieldUpdater<CleanupAction, FbWireDatabase> databaseUpdater =
                AtomicReferenceFieldUpdater.newUpdater(CleanupAction.class, FbWireDatabase.class, "database");

        private static final DeferredAction CLEANUP_FREE_DEFERRED_ACTION = new DeferredAction() {
            @Override
            public void processResponse(Response response) {
                // nothing to do
            }

            @Override
            public boolean requiresSync() {
                return true;
            }
        };

        private final int handle;
        private volatile FbWireDatabase database;

        private CleanupAction(AbstractFbWireStatement statement) {
            // NOTE: Care should be taken not to retain a handle to statement itself here
            handle = statement.getHandle();
            FbWireDatabase database = statement.getDatabase();
            databaseUpdater.set(this, database);
            database.addWeakDatabaseListener(this);
            statement.addWeakStatementListener(this);
        }

        @Override
        public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
            if (newState == StatementState.CLOSING) {
                releaseDatabaseReference();
                sender.removeStatementListener(this);
            }
        }

        @Override
        public void detaching(FbDatabase database) {
            releaseDatabaseReference();
        }

        private void releaseDatabaseReference() {
            FbWireDatabase database = databaseUpdater.getAndSet(this, null);
            if (database != null) {
                database.removeDatabaseListener(this);
            }
        }

        private FbWireDatabase releaseAndGetDatabaseReference() {
            FbWireDatabase database = databaseUpdater.getAndSet(this, null);
            if (database != null) {
                database.removeDatabaseListener(this);
            }
            return database;
        }

        @Override
        public void run() {
            FbWireDatabase database = releaseAndGetDatabaseReference();
            if (database == null) return;
            try (var ignored = database.withLock()) {
                if (!database.isAttached()) return;
                database.getXdrStreamAccess().withTransmitLock(xdrOut -> {
                    // TODO: This duplicates V10Statement.sendFreeStatementMsg; rethink this if it needs to be
                    //  versioned. Maybe move the message sending to FBWireOperations?
                    xdrOut.writeInt(WireProtocolConstants.op_free_statement); // p_operation
                    xdrOut.writeInt(handle); // p_sqlfree_statement
                    xdrOut.writeInt(ISCConstants.DSQL_drop); // p_sqlfree_option
                    xdrOut.flush();
                });
                // TODO: This may process deferred actions on the cleaner thread, we may want to change that
                database.enqueueDeferredAction(CLEANUP_FREE_DEFERRED_ACTION);
            } catch (SQLException | IOException e) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.TRACE,
                        "Ignored exception during statement clean up", e);
            }
        }
    }

}
