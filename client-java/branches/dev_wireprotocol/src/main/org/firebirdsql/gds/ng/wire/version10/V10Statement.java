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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.AbstractFbWireStatement;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireStatement;
import org.firebirdsql.gds.ng.wire.GenericResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class V10Statement extends AbstractFbWireStatement implements FbWireStatement {

    /**
     * Statement description information items for the V10 protocol
     */
    private static final byte[] STATEMENT_INFO_REQUEST_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            //ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end,

            ISCConstants.isc_info_sql_bind,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            //ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end
    };

    // TODO Do we actually need this separate from above?
    private static final byte[] PARAMETER_DESCRIPTION_INFO_REQUEST_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            //ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end
    };

    /**
     * Creates a new instance of V10Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V10Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    public <T> T getSqlInfo(final byte[] requestItems, final int bufferLength, final InfoProcessor<T> infoProcessor) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] getSqlInfo(final byte[] requestItems, final int bufferLength) throws SQLException {
        synchronized (getSynchronizationObject()) {
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendInfoSqlToBuffer(requestItems, bufferLength);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    return processInfoSqlResponse(database.readGenericResponse());
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        }
    }

    /**
     * Sends the info sql request to the database
     *
     * @param requestItems
     *         Info request items
     * @param bufferLength
     *         Requested response buffer length
     * @throws IOException
     * @throws SQLException
     */
    protected void sendInfoSqlToBuffer(final byte[] requestItems, final int bufferLength) throws IOException, SQLException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_info_sql);
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(0); // incarnation
        xdrOut.writeBuffer(requestItems);
        xdrOut.writeInt(bufferLength);
    }

    /**
     * Processes the info sql response.
     *
     * @param response
     *         GenericResponse
     * @return info sql response buffer
     */
    protected byte[] processInfoSqlResponse(GenericResponse response) {
        return response.getData();
    }

    @Override
    protected void free(final int option) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepare(final String statementText) throws SQLException {
        synchronized (getSynchronizationObject()) {
            // TODO Do we actually need a transaction for the prepare?
            if (getTransaction() == null || getTransaction().getState() != TransactionState.ACTIVE) {
                // TODO throw exception with right error code
                throw new SQLNonTransientException();
            }
            reset(true);
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    if (getState() == StatementState.CLOSED) {
                        allocateStatement();
                    }

                    try {
                        sendPrepareToBuffer(statementText);
                        getXdrOut().flush();
                    } catch (IOException ex) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                    }
                    try {
                        processPrepareResponse(getDatabase().readGenericResponse());
                    } catch (IOException ex) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                    }
                } catch (SQLException ex) {
                    if (getState() == StatementState.ALLOCATED) {
                        setState(StatementState.ERROR);
                    }
                    throw ex;
                }
            }
        }
    }

    /**
     * Sends the statement prepare to the connection.
     *
     * @param statementText
     *         Statement
     * @throws SQLException
     * @throws IOException
     */
    protected void sendPrepareToBuffer(final String statementText) throws SQLException, IOException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_prepare_statement);
            xdrOut.writeInt(getTransaction().getHandle());
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(getDatabase().getConnectionDialect());
            // TODO Include encoding!
            xdrOut.writeString(statementText);
            xdrOut.writeBuffer(getStatementInfoRequestItems());
            xdrOut.writeInt(getDefaultSqlInfoSize());
        }
    }

    /**
     * Processes the prepare response from the server.
     *
     * @param genericResponse
     *         GenericResponse
     * @throws SQLException
     */
    protected void processPrepareResponse(final GenericResponse genericResponse) throws SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            parseStatementInfo(genericResponse.getData());
        }
    }

    /**
     * Parse the statement info response in <code>statementInfoResponse</code>. If the response is truncated, a new
     * request is done using {@link #getStatementInfoRequestItems()}
     *
     * @param statementInfoResponse
     *         Statement info response
     */
    protected void parseStatementInfo(final byte[] statementInfoResponse) throws SQLException {
        final V10StatementInfoProcessor infoProcessor = new V10StatementInfoProcessor(this, this.getDatabase());
        InfoProcessor.StatementInfo statementInfo = infoProcessor.process(statementInfoResponse);

        setType(statementInfo.getStatementType());
        setFields(statementInfo.getFields());
        setParameters(statementInfo.getParameters());
    }

    @Override
    public void execute() throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute(final String statementText) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Resets the statement for next execution.
     *
     * @param resetAll
     *         Also reset field and parameter info
     */
    protected void reset(boolean resetAll) {
        synchronized (getSynchronizationObject()) {
            // TODO Clear rows etc
            allRowsFetched.set(false);

            if (resetAll) {
                // TODO Clear field and parameters
            }
        }
    }

    /**
     * Allocates a statement handle on the server
     *
     * @throws SQLException
     */
    protected void allocateStatement() throws SQLException {
        if (getState() != StatementState.CLOSED) {
            throw new IllegalStateException("allocateStatement only allowed when current state is CLOSED");
        }
        synchronized (getDatabase().getSynchronizationObject()) {
            try {
                sendAllocateToBuffer();
                getXdrOut().flush();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                processAllocateResponse(getDatabase().readGenericResponse());
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
    }

    /**
     * Sends the allocate request to the server.
     *
     * @throws SQLException
     * @throws IOException
     */
    protected void sendAllocateToBuffer() throws SQLException, IOException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_allocate_statement);
            xdrOut.writeInt(getDatabase().getHandle());
        }
    }

    /**
     * Processes the allocate response from the server.
     *
     * @param response
     *         GenericResponse
     */
    protected void processAllocateResponse(GenericResponse response) {
        synchronized (getSynchronizationObject()) {
            setHandle(response.getObjectHandle());
            allRowsFetched.set(false);
            setState(StatementState.ALLOCATED);
            setType(StatementType.NONE);
        }
    }

    @Override
    public int getDefaultSqlInfoSize() {
        // TODO Test for an optimal buffer size
        return getMaxSqlInfoSize();
    }

    @Override
    public int getMaxSqlInfoSize() {
        // TODO Is this the actual max for the v10 protocol, or is it 65536?
        return 32768;
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
