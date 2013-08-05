/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link FbTransaction} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class V10Transaction extends AbstractFbTransaction implements FbWireTransaction {

    private FbWireDatabase database;
    private final Object syncObject = new Object();
    private int handle;

    /**
     * Creates a new instance of V10Transaction for the specified database.
     *
     * @param database FbWireDatabase implementation
     */
    public V10Transaction(FbWireDatabase database) {
        this.database = database;
    }

    protected final XdrInputStream getXdrIn() throws SQLException {
        return database.getXdrIn();
    }

    protected final XdrOutputStream getXdrOut() throws SQLException {
        return database.getXdrOut();
    }

    protected final Object getSynchronizationObject() {
        return syncObject;
    }

    protected final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public int getHandle() {
        return handle;
    }

    // TODO Should transaction begin here, or on creation in a FbDatabase implementation?
    @Override
    public void beginTransaction(TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (getState() != TransactionState.NO_TRANSACTION) {
                // TODO check if right message
                throw new FbExceptionBuilder().exception(ISCConstants.isc_transaction_in_use).toSQLException();
            }
            GenericResponse response;
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_transaction);
                    xdrOut.writeInt(getDatabase().getHandle());
                    xdrOut.writeTyped(ISCConstants.isc_tpb_version3, (Xdrable) tpb);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    response = (GenericResponse) getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            handle = response.getObjectHandle();
            switchState(TransactionState.ACTIVE);
        }
    }

    @Override
    public void commit() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_commit);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.NO_TRANSACTION);
        }
    }

    @Override
    public void commitRetaining() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_commit_retaining);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.ACTIVE);
        }
    }

    @Override
    public void rollback() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_rollback);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.NO_TRANSACTION);
        }
    }

    @Override
    public void rollbackRetaining() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_rollback_retaining);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.ACTIVE);
        }
    }

    // TODO two-phase commit implementation

    private void checkTransactionActive() throws SQLException {
        if (getState() != TransactionState.ACTIVE) {
            // TODO check if right message
            throw new FbExceptionBuilder().exception(ISCConstants.isc_tra_state).toSQLException();
        }
    }
}
