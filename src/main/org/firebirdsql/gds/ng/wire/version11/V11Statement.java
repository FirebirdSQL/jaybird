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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
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

    /**
     * Statement description information items for the V11 protocol
     */
    private static final byte[] STATEMENT_INFO_REQUEST_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end,

            ISCConstants.isc_info_sql_bind,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            // TODO: Information not available in normal queries, check for procedures, otherwise remove
            //ISCConstants.isc_info_sql_field,
            //ISCConstants.isc_info_sql_alias,
            //ISCConstants.isc_info_sql_relation,
            //ISCConstants.isc_info_sql_relation_alias,
            //ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end
    };

    // TODO Do we actually need this separate from above?
    private static final byte[] PARAMETER_DESCRIPTION_INFO_REQUEST_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end
    };

    @Override
    public void prepare(final String statementText) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkTransactionActive(getTransaction());
            final StatementState currentState = getState();
            if (!isPrepareAllowed(currentState)) {
                throw new SQLNonTransientException(String.format("Current statement state (%s) does not allow call to prepare", currentState));
            }
            resetAll();
            final FbWireDatabase db = getDatabase();
            synchronized (db.getSynchronizationObject()) {
                int expectedResponseCount = 0;
                try {
                    if (currentState == StatementState.NEW) {
                        sendAllocate();
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
                    try {
                        if (currentState == StatementState.NEW) {
                            expectedResponseCount--;
                            processAllocateResponse(db.readGenericResponse(getStatementWarningCallback()));
                        }
                        expectedResponseCount--;
                        processPrepareResponse(db.readGenericResponse(getStatementWarningCallback()));
                    } finally {
                        db.consumePackets(expectedResponseCount, getStatementWarningCallback());
                    }
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        }
    }

    @Override
    protected void free(final int option) throws SQLException {
        synchronized (getSynchronizationObject()) {
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    doFreePacket(option);
                    // intentionally no flush
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

    @Override
    public byte[] getStatementInfoRequestItems() {
        return STATEMENT_INFO_REQUEST_ITEMS.clone();
    }

    @Override
    public byte[] getParameterDescriptionInfoRequestItems() {
        return PARAMETER_DESCRIPTION_INFO_REQUEST_ITEMS.clone();
    }
}
