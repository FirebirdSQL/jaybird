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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jaybird.xca.FatalErrorHelper;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbTransaction} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaTransaction extends AbstractFbTransaction {

    private static final Logger log = LoggerFactory.getLogger(JnaTransaction.class);

    private final IntByReference handle;
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];
    private final FbClientLibrary clientLibrary;

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
            checkDbAttached();
            switchState(TransactionState.COMMITTING);
            clientLibrary.isc_commit_transaction(statusVector, handle);
            processStatusVector();
            switchState(TransactionState.COMMITTED);
        } catch (SQLException e) {
            if (FatalErrorHelper.isBrokenConnection(e)) {
                forceAbortedUnknownState();
            }
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            final TransactionState transactionState = getState();
            if (transactionState != TransactionState.COMMITTED && log.isWarnEnabled()) {
                String message = "Commit not completed, state was " + transactionState;
                if (log.isDebugEnabled()) {
                    log.warnDebug(message, new RuntimeException("Commit not completed"));
                } else {
                    log.warn(message + "; see debug level for stacktrace");
                }
            }
        }
    }

    @Override
    public void rollback() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDbAttached();
            switchState(TransactionState.ROLLING_BACK);
            clientLibrary.isc_rollback_transaction(statusVector, handle);
            processStatusVector();
            switchState(TransactionState.ROLLED_BACK);
        } catch (SQLException e) {
            if (FatalErrorHelper.isBrokenConnection(e)) {
                forceAbortedUnknownState();
            }
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            final TransactionState transactionState = getState();
            if (transactionState != TransactionState.ROLLED_BACK && log.isWarnEnabled()) {
                String message = "Rollback not completed, state was " + transactionState;
                if (log.isDebugEnabled()) {
                    log.warnDebug(message, new RuntimeException("Rollback not completed"));
                } else {
                    log.warn(message + "; see debug level for stacktrace");
                }
            }
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        boolean noRecoveryInfo = recoveryInformation == null || recoveryInformation.length == 0;
        try (LockCloseable ignored = withLock()) {
            checkDbAttached();
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
            if (FatalErrorHelper.isBrokenConnection(e)) {
                forceAbortedUnknownState();
            }
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            final TransactionState transactionState = getState();
            if (transactionState != TransactionState.PREPARED && log.isWarnEnabled()) {
                String message = "Prepare not completed, state was " + transactionState;
                if (log.isDebugEnabled()) {
                    log.warnDebug(message, new RuntimeException("Prepare not completed"));
                } else {
                    log.warn(message + "; see debug level for stacktrace");
                }
            }
        }
    }

    @Override
    public byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
            try (LockCloseable ignored = withLock()) {
                checkDbAttached();
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

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        try {
            JnaDatabase db = getDatabase();
            if (handle.getValue() == 0
                    || !db.hasFeature(FbClientFeature.FB_DISCONNECT_TRANSACTION)
                    || !db.isAttached()) return;
            /*
             ACTIVE transactions are held in AbstractFbDatabase.activeTransactions, so such cleanup would only
             happen when the connection itself was also GC'd, which means an attempt to roll back would likely fail
             anyway (and the server will perform a rollback eventually), so we don't perform any action other than
             fb_disconnect_transaction
            */
            db.getClientLibrary()
                    .fb_disconnect_transaction(statusVector, handle);
            // We intentionally ignore the status vector result
        } finally {
            super.finalize();
        }
    }
}
