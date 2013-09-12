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
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class V10Statement extends AbstractFbWireStatement implements FbWireStatement {

    // TODO Handle error state in a consistent way

    private static final int NULL_INDICATOR_NOT_NULL = 0;
    private static final int NULL_INDICATOR_NULL = -1;

    /**
     * Statement description information items for the V10 protocol
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
            //ISCConstants.isc_info_sql_relation_alias,
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
        return infoProcessor.process(getSqlInfo(requestItems, bufferLength));
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
                    return processInfoSqlResponse(getDatabase().readGenericResponse());
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
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_info_sql);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(0); // incarnation
            xdrOut.writeBuffer(requestItems);
            xdrOut.writeInt(bufferLength);
        }
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
    protected void free(final int option) throws SQLException {
        if (freeNotNeeded(option)) {
            return;
        }
        synchronized (getSynchronizationObject()) {
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    doFreePacket(option);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processFreeResponse(getDatabase().readResponse());
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        }
    }

    /**
     * Handles sending the free statement packet and associated state changes on this statement
     *
     * @param option
     *         Free statement option
     * @throws SQLException
     */
    protected void doFreePacket(int option) throws SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            try {
                sendFreeToBuffer(option);

                // Reset statement information
                reset(option == ISCConstants.DSQL_drop);
            } catch (IOException e) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
            }
        }
    }

    /**
     * Sends the free statement to the database
     *
     * @param option
     *         Free statement option
     * @throws IOException
     * @throws SQLException
     */
    protected void sendFreeToBuffer(int option) throws IOException, SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_free_statement);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(option);
        }
    }

    /**
     * Processes the response to the free statement.
     *
     * @param response
     *         Response object
     * @throws IOException
     * @throws SQLException
     */
    protected void processFreeResponse(Response response) throws IOException, SQLException {
        // No processing needed
    }

    /**
     * Decides whether sending free is actually required.
     *
     * @param option
     *         Free statement option
     * @return <code>true</code> when freeing is needed, <code>false</code> otherwise
     */
    protected boolean freeNotNeeded(final int option) {
        // Does	not	seem to	be possible	or necessary to	close an execute procedure statement.
        // TODO Verify if this is correct
        return getType() == StatementType.STORED_PROCEDURE && option == ISCConstants.DSQL_close;
    }

    @Override
    public void prepare(final String statementText) throws SQLException {
        synchronized (getSynchronizationObject()) {
            // TODO Do we actually need a transaction for the prepare?
            if (getTransaction() == null || getTransaction().getState() != TransactionState.ACTIVE) {
                throw new SQLNonTransientException("No transaction or transaction not ACTIVE", FBSQLException.SQL_STATE_INVALID_TX_STATE);
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
                        switchState(StatementState.ERROR);
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
            xdrOut.writeString(statementText, getDatabase().getEncoding());
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
        setFieldDescriptor(statementInfo.getFields());
        setParameterDescriptor(statementInfo.getParameters());
    }

    @Override
    public void execute(final List<FieldValue> parameters) throws SQLException {
        if (getState() == StatementState.CLOSED) {
            // TODO Throw correct error, sqlstate, etc
            throw new SQLException("Invalid statement state");
        }
        // TODO Validate transaction state?
        synchronized (getSynchronizationObject()) {
            validateParameters(parameters);
            reset(false);
            final boolean performExecute2 = getType() == StatementType.STORED_PROCEDURE;
            // TODO Records affected?
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendExecuteToBuffer(performExecute2 ? WireProtocolConstants.op_execute2 : WireProtocolConstants.op_execute, parameters);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    final boolean hasResultSet = getFieldDescriptor() != null && getFieldDescriptor().getCount() > 0;
                    statementListenerDispatcher.statementExecuted(this, hasResultSet, performExecute2);
                    if (performExecute2) {
                        processStoredProcedureExecuteResponse(getDatabase().readSqlResponse());
                        setAllRowsFetched(true);
                    }
                    processExecuteResponse(getDatabase().readGenericResponse());

                    // TODO .NET implementation retrieves affected rows here

                    switchState(StatementState.EXECUTED);
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        }
    }

    /**
     * Sends the execute (for <code>op_execute</code> or <code>op_execute2</code>) to the database.
     *
     * @param operation
     *         Operation (<code>op_execute</code> or <code>op_execute2</code>)
     * @param parameters
     *         Parameters
     * @throws IOException
     * @throws SQLException
     */
    protected void sendExecuteToBuffer(final int operation, final List<FieldValue> parameters) throws IOException, SQLException {
        assert operation == WireProtocolConstants.op_execute || operation == WireProtocolConstants.op_execute2 : "Needs to be called with operation op_execute or op_execute2";
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(operation);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(getTransaction().getHandle());

            // TODO What if 0 parameters?
            if (getParameterDescriptor() != null) {
                xdrOut.writeBuffer(calculateBlr(getParameterDescriptor()));
                xdrOut.writeInt(0); // message number = in_message_type
                xdrOut.writeInt(1); // Number of messages
                writeSqlData(parameters);
            } else {
                xdrOut.writeBuffer(null);
                xdrOut.writeInt(0); // message number = in_message_type
                xdrOut.writeInt(0); // Number of messages
            }

            if (operation == WireProtocolConstants.op_execute2) {
                // TODO What if 0 fields?
                xdrOut.writeBuffer(getFieldDescriptor() != null ? calculateBlr(getFieldDescriptor()) : null);
                xdrOut.writeInt(0); // out_message_number = out_message_type
            }
        }
    }

    /**
     * Process the execute response for stored procedures (<code>op_execute2</code>.
     *
     * @param sqlResponse
     *         SQL response object
     * @throws SQLException
     * @throws IOException
     */
    protected void processStoredProcedureExecuteResponse(SqlResponse sqlResponse) throws SQLException, IOException {
        if (sqlResponse.getCount() > 0) {
            queueRowData(readRowData());
        }
    }

    /**
     * Process the execute response.
     *
     * @param genericResponse
     *         Generic response object
     */
    protected void processExecuteResponse(GenericResponse genericResponse) {
        // Nothing to do here
    }

    // TODO Validate that other types don't return rows
    protected static final EnumSet<StatementType> STATEMENT_TYPES_WITH_ROWS = EnumSet.of(StatementType.SELECT, StatementType.SELECT_FOR_UPDATE);

    @Override
    public void fetchRows(int fetchSize) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementOpen();
            if (getState() != StatementState.EXECUTED) {
                // TODO Check if this state is sufficient
                throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
            }
            if (!STATEMENT_TYPES_WITH_ROWS.contains(getType())) {
                // TODO Throw exception instead?
                return;
            }
            if (isAllRowsFetched()) return;

            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendFetchToBuffer(fetchSize);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processFetchResponse();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        }
    }

    /**
     * Process the fetch response by reading the returned rows and queuing them.
     *
     * @throws IOException
     * @throws SQLException
     */
    protected void processFetchResponse() throws IOException, SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            Response response;
            while (!isAllRowsFetched() && (response = getDatabase().readResponse()) instanceof FetchResponse) {
                final FetchResponse fetchResponse = (FetchResponse) response;
                if (fetchResponse.getCount() > 0 && fetchResponse.getStatus() == WireProtocolConstants.FETCH_OK) {
                    queueRowData(readRowData());
                } else if (fetchResponse.getStatus() == WireProtocolConstants.FETCH_NO_MORE_ROWS) {
                    setAllRowsFetched(true);
                } else {
                    // TODO Log, raise exception, or simply 'not possible'?
                    break;
                }
            }
        }
    }

    /**
     * Sends the fetch requestion to the database.
     *
     * @param fetchSize Number of rows to fetch.
     * @throws SQLException
     * @throws IOException
     */
    protected void sendFetchToBuffer(int fetchSize) throws SQLException, IOException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_fetch);
            xdrOut.writeInt(getHandle());
            xdrOut.writeBuffer(calculateBlr(getFieldDescriptor()));
            xdrOut.writeInt(0); // out_message_number = out_message_type
            xdrOut.writeInt(fetchSize); // fetch size
        }
    }

    /**
     * Reads a single row from the database.
     *
     * @return Row as a list of {@link FieldValue} instances
     * @throws SQLException
     * @throws IOException
     */
    protected List<FieldValue> readRowData() throws SQLException, IOException {
        final List<FieldValue> rowData = new ArrayList<FieldValue>(getFieldDescriptor().getCount());
        final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();

        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrInputStream xdrIn = getXdrIn();

            for (FieldDescriptor field : getFieldDescriptor()) {
                int len = blrCalculator.calculateIoLength(field);
                final FieldValue fieldValue = field.createDefaultFieldValue();
                byte[] buffer;
                if (len == 0) {
                    len = xdrIn.readInt();
                    buffer = new byte[len];
                    xdrIn.readFully(buffer, 0, len);
                    xdrIn.skipPadding(len);
                } else if (len < 0) {
                    buffer = new byte[-len];
                    xdrIn.readFully(buffer, 0, -len);
                } else {
                    // len is incremented in calculateIoLength to avoid value 0 so it must be decremented
                    len--;
                    buffer = new byte[len];
                    xdrIn.readFully(buffer, 0, len);
                    xdrIn.skipPadding(len);
                }
                if (xdrIn.readInt() == -1)
                    buffer = null;
                fieldValue.setFieldData(buffer);
                rowData.add(fieldValue);
            }
        }
        return rowData;
    }

    /**
     * Write a set of SQL data from a list of {@link FieldValue} instances.
     *
     * @param fieldValues
     *         The List containing the SQL data to be written
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    protected void writeSqlData(final List<FieldValue> fieldValues) throws IOException, SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();
            for (final FieldValue fieldValue : fieldValues) {
                // We assume that the FieldDescriptors of the FieldValues have already been validated with the expected types
                final FieldDescriptor fieldDescriptor = fieldValue.getFieldDescriptor();

                int len = blrCalculator.calculateIoLength(fieldDescriptor);
                final byte[] buffer = fieldValue.getFieldData();
                final int tempType = fieldDescriptor.getType() & ~1;

                // TODO Correctly pad with 0x00 instead of 0x20 for octets.
                if (tempType == ISCConstants.SQL_NULL) {
                    // Nothing to write for SQL_NULL (except null indicator, which happens at end)
                } else if (len == 0) {
                    if (buffer != null) {
                        len = buffer.length;
                        xdrOut.writeInt(len);
                        xdrOut.write(buffer, 0, len, (4 - len) & 3);
                    } else {
                        xdrOut.writeInt(0);
                    }
                } else if (len < 0) {
                    if (buffer != null) {
                        xdrOut.write(buffer, 0, -len);
                    } else {
                        xdrOut.writePadding(-len, 0x00); // TODO Used to be 0x20, check if use of 0x00 here is correct
                    }
                } else {
                    // decrement length because it was incremented before
                    // increment happens in BlrCalculator.calculateIoLength
                    len--;
                    if (buffer != null) {
                        int buflen = buffer.length;
                        if (buflen >= len) {
                            xdrOut.write(buffer, 0, len, (4 - len) & 3);
                        } else {
                            xdrOut.write(buffer, 0, buflen, 0);
                            xdrOut.writePadding(len - buflen + ((4 - len) & 3), 0x20);
                        }
                    } else {
                        xdrOut.writePadding(len + ((4 - len) & 3), 0x20);
                    }
                }
                // sqlind (null indicator)
                xdrOut.writeInt(buffer != null ? NULL_INDICATOR_NOT_NULL : NULL_INDICATOR_NULL);
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
            // TODO Is there a better sqlstate?
            throw new SQLNonTransientException("allocateStatement only allowed when current state is CLOSED", FBSQLException.SQL_STATE_GENERAL_ERROR);
        }
        synchronized (getDatabase().getSynchronizationObject()) {
            try {
                sendAllocateToBuffer();
                getXdrOut().flush();
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                processAllocateResponse(getDatabase().readGenericResponse());
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
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
            setAllRowsFetched(false);
            switchState(StatementState.ALLOCATED);
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
        // TODO Is this the actual max for the v10 protocol, or is it 65535?
        return 32767;
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
