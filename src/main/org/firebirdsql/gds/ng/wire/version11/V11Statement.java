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
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version10.V10Statement;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireStatement} implementation for the version 11 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11Statement extends V10Statement {
    /**
     * Creates a new instance of V11Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V11Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    public void prepare(final String statementText) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkTransactionActive(getTransaction());
            final StatementState initialState = getState();
            if (!isPrepareAllowed(initialState)) {
                throw new SQLNonTransientException(String.format("Current statement state (%s) does not allow call to prepare", initialState));
            }
            resetAll();

            int expectedResponseCount = 0;
            try {
                if (initialState == StatementState.NEW) {
                    sendAllocate();
                    // We're assuming allocation is successful, as changing it when processing the response breaks
                    // the state transition in sendPrepare
                    switchState(StatementState.ALLOCATED);
                    expectedResponseCount++;
                } else {
                    checkStatementValid();
                }
                sendPrepare(statementText);
                expectedResponseCount++;

                getXdrOut().flush();
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }

            try {
                final FbWireDatabase db = getDatabase();
                try {
                    if (initialState == StatementState.NEW) {
                        try {
                            expectedResponseCount--;
                            processAllocateResponse(db.readGenericResponse(getStatementWarningCallback()));
                        } catch (SQLException e) {
                            forceState(StatementState.NEW);
                            throw e;
                        }
                    }
                    try {
                        expectedResponseCount--;
                        processPrepareResponse(db.readGenericResponse(getStatementWarningCallback()));
                    } catch (SQLException e) {
                        switchState(StatementState.ALLOCATED);
                        throw e;
                    }
                } finally {
                    db.consumePackets(expectedResponseCount, getStatementWarningCallback());
                }
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    protected void free(final int option) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            try {
                doFreePacket(option);
                /*
                 Don't flush close of cursor, only flush drop or unprepare of statement.
                 This balances network efficiencies with preventing statements
                 retaining locks on metadata objects too long
                */
                if (option != ISCConstants.DSQL_close) {
                    getXdrOut().flush();
                }
                // process response later
                getDatabase().enqueueDeferredAction(new DeferredAction() {
                    @Override
                    public void processResponse(Response response) {
                        processFreeResponse(response);
                    }
                    @Override
                    public WarningMessageCallback getWarningMessageCallback() {
                        return getStatementWarningCallback();
                    }
                });
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
        }
    }
}
