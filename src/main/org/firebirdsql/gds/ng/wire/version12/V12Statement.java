/*
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
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version11.V11Statement;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class V12Statement extends V11Statement {

    /**
     * Creates a new instance of V11Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V12Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    public void execute(final RowValue parameters) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                checkTransactionActive(getTransaction());
                validateParameters(parameters);
                reset(false);

                final FbWireDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    // TODO Which state to switch to when an exception occurs (always ERROR might be wrong, see to do at start of class)
                    switchState(StatementState.EXECUTING);
                    final StatementType statementType = getType();
                    int expectedResponseCount = 0;
                    try {
                        if (statementType.isTypeWithSingletonResult()) {
                            expectedResponseCount++;
                        }
                        sendExecute(statementType.isTypeWithSingletonResult() ? WireProtocolConstants.op_execute2 : WireProtocolConstants.op_execute, parameters);
                        expectedResponseCount++;
                        getXdrOut().flush();
                    } catch (IOException ex) {
                        switchState(StatementState.ERROR);
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                    }

                    final WarningMessageCallback statementWarningCallback = getStatementWarningCallback();
                    try {
                        final boolean hasFields = getFieldDescriptor() != null && getFieldDescriptor().getCount() > 0;
                        try {
                            if (statementType.isTypeWithSingletonResult()) {
                                /* A type with a singleton result (ie an execute procedure), doesn't actually have a
                                 * result set that will be fetched, instead we have a singleton result if we have fields
                                 */
                                statementListenerDispatcher.statementExecuted(this, false, hasFields);
                                expectedResponseCount--;
                                processExecuteSingletonResponse(db.readSqlResponse(statementWarningCallback));
                                // TODO Do we need to set expectedResponseCount to 0 and exit if we get a cancelled error?
                                if (hasFields) {
                                    setAllRowsFetched(true);
                                }
                            } else {
                                // A normal execute is never a singleton result (even if it only produces a single result)
                                statementListenerDispatcher.statementExecuted(this, hasFields, false);
                            }
                            expectedResponseCount--;
                            processExecuteResponse(db.readGenericResponse(statementWarningCallback));
                        } finally {
                            db.consumePackets(expectedResponseCount, getStatementWarningCallback());
                        }

                        if (getState() != StatementState.ERROR) {
                            switchState(statementType.isTypeWithCursor() ? StatementState.CURSOR_OPEN : StatementState.PREPARED);
                        }
                    } catch (IOException ex) {
                        switchState(StatementState.ERROR);
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                    }

                    /* Note contrary to V10 (and V11) we need to split retrieving update counts from the actual execute
                     * otherwise a cancel will not work.
                     */
                    if (!statementType.isTypeWithCursor() && statementType.isTypeWithUpdateCounts()) {
                        getSqlCounts();
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }
}
