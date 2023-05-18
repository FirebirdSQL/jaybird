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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link FbTransaction} implementation for the version 10 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10Transaction extends AbstractFbTransaction implements FbWireTransaction {

    private static final System.Logger log = System.getLogger(V10Transaction.class.getName());

    private final int handle;

    /**
     * Creates a new instance of V10Transaction for the specified database.
     * <p>
     * This can either be used for an active handle (with <code>initialState</code> {@link org.firebirdsql.gds.ng.TransactionState#ACTIVE}),
     * or a reconnected (prepared) handle (with <code>initialState</code> {@link org.firebirdsql.gds.ng.TransactionState#PREPARED}).
     * </p>
     *
     * @param database
     *         FbWireDatabase implementation
     * @param transactionHandle
     *         Transaction handle
     * @param initialState
     *         The initial state of the transaction (only <code>ACTIVE</code> or <code>PREPARED</code> allowed).
     */
    public V10Transaction(FbWireDatabase database, int transactionHandle, TransactionState initialState) {
        super(initialState, database);
        handle = transactionHandle;
    }

    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getDatabase().getXdrStreamAccess().getXdrOut();
    }

    @Override
    protected FbWireDatabase getDatabase() {
        return (FbWireDatabase) super.getDatabase();
    }

    @Override
    public int getHandle() {
        return handle;
    }

    @Override
    public void commit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            switchState(TransactionState.COMMITTING);
            finishTransaction(op_commit);
            switchState(TransactionState.COMMITTED);
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
            switchState(TransactionState.ROLLING_BACK);
            finishTransaction(op_rollback);
            switchState(TransactionState.ROLLED_BACK);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            logUnexpectedState(TransactionState.ROLLED_BACK, log);
        }
    }

    private void finishTransaction(final int commitOrRollback) throws SQLException {
        assert commitOrRollback == op_commit || commitOrRollback == op_rollback
                : "Unsupported operation code " + commitOrRollback;
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(commitOrRollback);
            xdrOut.writeInt(handle);
            xdrOut.flush();
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
        }
        try {
            getDatabase().readResponse(null);
        } catch (IOException ioex) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            switchState(TransactionState.PREPARING);
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                if (recoveryInformation != null) {
                    xdrOut.writeInt(op_prepare2);
                    xdrOut.writeInt(handle);
                    xdrOut.writeBuffer(recoveryInformation);
                } else {
                    xdrOut.writeInt(op_prepare);
                    xdrOut.writeInt(handle);
                }
                xdrOut.flush();
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
            }
            try {
                getDatabase().readResponse(null);
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
            }
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
            return getDatabase().getInfo(op_info_transaction, getHandle(), requestItems, maxBufferLength, null);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /*
     NOTE: V10Transaction does not perform any clean through a Cleaner, because there is realistically no option:
     - ACTIVE transactions are held in AbstractFbDatabase.activeTransactions, so such cleanup would only happen when
       the connection itself was also GC'd, which means an attempt to roll back would likely fail anyway (and the server
       will perform a rollback eventually)
     - There is no wire protocol equivalent of fb_disconnect_transaction, so we can't release the handle if we wanted to
    */

}
