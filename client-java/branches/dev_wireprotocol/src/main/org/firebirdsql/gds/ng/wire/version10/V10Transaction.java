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

import java.io.IOException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.FbException;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.GenericResponse;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class V10Transaction extends AbstractFbTransaction {
    
    private FbWireDatabase database;
    private final Object syncObject = new Object();
    private int handle;
    
    public V10Transaction(FbWireDatabase database) {
        this.database = database;
    }
    
    protected final XdrInputStream getXdrIn() throws FbException {
        return database.getXdrIn();
    }

    protected final XdrOutputStream getXdrOut() throws FbException {
        return database.getXdrOut();
    }
    
    protected final Object getSynchronizationObject() {
        return syncObject;
    }
    
    protected final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public void beginTransaction(TransactionParameterBuffer tpb) throws FbException {
        synchronized (getSynchronizationObject()) {
            if (getState() != TransactionState.NO_TRANSACTION) {
                // TODO check if right message
                throw new FbException(ISCConstants.isc_transaction_in_use);
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
                    throw new FbException(ISCConstants.isc_net_write_err, ioex);
                }
                try {
                    response = (GenericResponse) getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_read_err, ioex);
                }
            }
            handle = response.getObjectHandle();
            switchState(TransactionState.ACTIVE);
        }
    }

    @Override
    public void commit() throws FbException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_commit);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_write_err, ioex);
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_read_err, ioex);
                }
            }
            switchState(TransactionState.NO_TRANSACTION);
        }
    }

    @Override
    public void commitRetaining() throws FbException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_commit_retaining);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_write_err, ioex);
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_read_err, ioex);
                }
            }
            switchState(TransactionState.ACTIVE);
        }
    }

    @Override
    public void rollback() throws FbException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_rollback);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_write_err, ioex);
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_read_err, ioex);
                }
            }
            switchState(TransactionState.NO_TRANSACTION);
        }
    }

    @Override
    public void rollbackRetaining() throws FbException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_rollback_retaining);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_write_err, ioex);
                }
                try {
                    getDatabase().readResponse();
                } catch (IOException ioex) {
                    throw new FbException(ISCConstants.isc_net_read_err, ioex);
                }
            }
            switchState(TransactionState.ACTIVE);
        }
    }
    
    // TODO two-phase commit

    private void checkTransactionActive() throws FbException {
        if (getState() != TransactionState.ACTIVE) {
            // TODO check if right message
            throw new FbException(ISCConstants.isc_tra_state);
        }
    }
}
