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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;

import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaTransaction extends AbstractFbTransaction {

    // TODO: Clear on commit/rollback?
    private final IntByReference handle;
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];

    /**
     * Initializes AbstractFbTransaction.
     *
     * @param database
     *         FbDatabase that created this handle.
     * @param transactionHandle
     *         Transaction handle
     * @param initialState
     *         Initial transaction state (allowed values are {@link org.firebirdsql.gds.ng.TransactionState#ACTIVE}
     *         and {@link org.firebirdsql.gds.ng.TransactionState#PREPARED}.
     */
    protected JnaTransaction(JnaDatabase database, IntByReference transactionHandle, TransactionState initialState) {
        super(initialState, database);
        handle = transactionHandle;
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
        synchronized (getSynchronizationObject()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            final FbClientLibrary clientLibrary = db.getClientLibrary();
            switchState(TransactionState.COMMITTING);
            synchronized (db.getSynchronizationObject()) {
                clientLibrary.isc_commit_transaction(statusVector, handle);
            }
            processStatusVector();
            switchState(TransactionState.COMMITTED);
        }
    }

    @Override
    public void rollback() throws SQLException {
        synchronized (getSynchronizationObject()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            final FbClientLibrary clientLibrary = db.getClientLibrary();
            switchState(TransactionState.ROLLING_BACK);
            synchronized (db.getSynchronizationObject()) {
                clientLibrary.isc_rollback_transaction(statusVector, handle);
            }
            processStatusVector();
            switchState(TransactionState.ROLLED_BACK);
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        synchronized (getSynchronizationObject()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            final FbClientLibrary clientLibrary = db.getClientLibrary();
            switchState(TransactionState.PREPARING);
            synchronized (db.getSynchronizationObject()) {
                if (recoveryInformation == null || recoveryInformation.length == 0) {
                    clientLibrary.isc_prepare_transaction(statusVector, handle);
                } else {
                    clientLibrary.isc_prepare_transaction2(statusVector, handle, (short) recoveryInformation.length,
                            recoveryInformation);
                }
            }
            processStatusVector();
            switchState(TransactionState.PREPARED);
        }
    }

    @Override
    public byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
        synchronized (getSynchronizationObject()) {
            final JnaDatabase db = getDatabase();
            db.checkConnected();
            final FbClientLibrary clientLibrary = db.getClientLibrary();
            synchronized (db.getSynchronizationObject()) {
                clientLibrary.isc_transaction_info(statusVector, handle, (short) requestItems.length, requestItems,
                        (short) maxBufferLength, responseBuffer);
            }
            processStatusVector();
            final byte[] responseArray = new byte[maxBufferLength];
            responseBuffer.get(responseArray);
            return responseArray;
        }
    }

    private void processStatusVector() throws SQLException {
        getDatabase().processStatusVector(statusVector, null);
    }
}
