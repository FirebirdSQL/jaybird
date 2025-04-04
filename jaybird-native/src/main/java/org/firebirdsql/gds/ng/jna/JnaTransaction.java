// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2016 Adriano dos Santos Fernandes
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbTransaction} for native client access.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class JnaTransaction extends AbstractFbTransaction {

    private static final System.Logger log = System.getLogger(JnaTransaction.class.getName());

    private final IntByReference handle;
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];
    private final FbClientLibrary clientLibrary;
    private final Cleaner.Cleanable cleanable;

    /**
     * Initializes AbstractFbTransaction.
     *
     * @param database
     *         FbDatabase that created this handle.
     * @param transactionHandle
     *         Transaction handle
     * @param initialState
     *         Initial transaction state (allowed values are {@link org.firebirdsql.gds.ng.TransactionState#ACTIVE}
     *         and {@link org.firebirdsql.gds.ng.TransactionState#PREPARED})
     */
    public JnaTransaction(JnaDatabase database, IntByReference transactionHandle, TransactionState initialState) {
        super(initialState, database);
        handle = transactionHandle;
        clientLibrary = database.getClientLibrary();
        cleanable = Cleaners.getJbCleaner().register(this, new CleanupAction(transactionHandle, database));
    }

    @Override
    public JnaDatabase getDatabase() {
        return (JnaDatabase) super.getDatabase();
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    public IntByReference getJnaHandle() {
        return handle;
    }

    @Override
    public void commit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            switchState(TransactionState.COMMITTING);
            clientLibrary.isc_commit_transaction(statusVector, handle);
            processStatusVector();
            switchState(TransactionState.COMMITTED);
            cleanable.clean();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            logUnexpectedState(TransactionState.COMMITTED, log);
        }
    }

    @Override
    public void rollback() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            switchState(TransactionState.ROLLING_BACK);
            clientLibrary.isc_rollback_transaction(statusVector, handle);
            processStatusVector();
            switchState(TransactionState.ROLLED_BACK);
            cleanable.clean();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            logUnexpectedState(TransactionState.ROLLED_BACK, log);
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        boolean noRecoveryInfo = recoveryInformation == null || recoveryInformation.length == 0;
        try (LockCloseable ignored = withLock()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            switchState(TransactionState.PREPARING);
            if (noRecoveryInfo) {
                clientLibrary.isc_prepare_transaction(statusVector, handle);
            } else {
                clientLibrary.isc_prepare_transaction2(statusVector, handle, (short) recoveryInformation.length,
                        recoveryInformation);
            }
            processStatusVector();
            switchState(TransactionState.PREPARED);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            logUnexpectedState(TransactionState.PREPARED, log);
        }
    }

    @Override
    public byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
            try (LockCloseable ignored = withLock()) {
                final JnaDatabase db = getDatabase();
                db.checkConnected();
                clientLibrary.isc_transaction_info(statusVector, handle, (short) requestItems.length, requestItems,
                        (short) maxBufferLength, responseBuffer);
                processStatusVector();
            }
            final byte[] responseArray = new byte[maxBufferLength];
            responseBuffer.get(responseArray);
            return responseArray;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void processStatusVector() throws SQLException {
        getDatabase().processStatusVector(statusVector, null);
    }

    private static final class CleanupAction implements Runnable, DatabaseListener {

        private final IntByReference handle;
        @SuppressWarnings("java:S3077")
        private volatile JnaDatabase database;

        private CleanupAction(IntByReference handle, JnaDatabase database) {
            this.handle = handle;
            this.database = database;
            database.addWeakDatabaseListener(this);
        }

        @Override
        public void detaching(FbDatabase database) {
            this.database = null;
            database.removeDatabaseListener(this);
        }

        @Override
        public void run() {
            final JnaDatabase database = this.database;
            if (database == null) return;
            detaching(database);
            if (handle.getValue() == 0
                    || !database.hasFeature(FbClientFeature.FB_DISCONNECT_TRANSACTION)
                    || !database.isAttached()) return;
            /*
             ACTIVE transactions are held in AbstractFbDatabase.activeTransactions, so such cleanup would only
             happen when the connection itself was also GC'd, which means an attempt to roll back would likely fail
             anyway (and the server will perform a rollback eventually), so we don't perform any action other than
             fb_disconnect_transaction
            */
            database.getClientLibrary()
                    .fb_disconnect_transaction(new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE], handle);
            // We intentionally ignore the status vector result
        }
    }
}
