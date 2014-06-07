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
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Statement extends AbstractFbWireStatement implements FbWireStatement {

    // TODO Handle error state in a consistent way (eg when does an exception lead to the error state, or when is it 'just' valid feedback)
    // TODO Fix state transitions

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
    public byte[] getSqlInfo(final byte[] requestItems, final int bufferLength) throws SQLException {
        synchronized (getSynchronizationObject()) {
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendInfoSql(requestItems, bufferLength);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    return processInfoSqlResponse(getDatabase().readGenericResponse(getStatementWarningCallback()));
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
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
    protected void sendInfoSql(final byte[] requestItems, final int bufferLength) throws IOException, SQLException {
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
        synchronized (getSynchronizationObject()) {
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    doFreePacket(option);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processFreeResponse(getDatabase().readResponse(getStatementWarningCallback()));
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
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
                sendFree(option);

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
    protected void sendFree(int option) throws IOException, SQLException {
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

    private static final EnumSet<StatementState> PREPARE_ALLOWED_STATES = EnumSet.of(StatementState.ALLOCATED, StatementState.PREPARED);

    /**
     * Is a call to {@link #prepare(String)} allowed for the supplied {@link StatementState}.
     *
     * @param state The statement state
     * @return <code>true</code> call to <code>prepare</code> is allowed
     */
    protected boolean isPrepareAllowed(final StatementState state) {
        return PREPARE_ALLOWED_STATES.contains(state);
    }

    @Override
    public void prepare(final String statementText) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            checkTransactionActive(getTransaction());
            final StatementState currentState = getState();
            if (!isPrepareAllowed(currentState)) {
                throw new SQLNonTransientException(String.format("Current statement state (%s) does not allow call to prepare", currentState));
            }
            resetAll();
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendPrepare(statementText);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processPrepareResponse(getDatabase().readGenericResponse(getStatementWarningCallback()));
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
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
    protected void sendPrepare(final String statementText) throws SQLException, IOException {
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
            switchState(StatementState.PREPARED);
        }
    }

    public void setCursorName(String cursorName) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            // TODO Check other statement states?

            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(WireProtocolConstants.op_set_cursor);
                    xdrOut.writeInt(getHandle());
                    // TODO Potentially needs additional null-termination
                    xdrOut.writeString(cursorName, getDatabase().getEncoding());
                    xdrOut.writeInt(0); // Cursor type
                    xdrOut.flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    // TODO Do we need to do anything else with this response?
                    getDatabase().readGenericResponse(getStatementWarningCallback());
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
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
        // TODO Validate transaction state?
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            checkTransactionActive(getTransaction());
            validateParameters(parameters);
            reset(false);

            synchronized (getDatabase().getSynchronizationObject()) {
                // TODO Which state to switch to when an exception occurs (always ERROR might be wrong, see to do at start of class)
                switchState(StatementState.EXECUTING);
                final StatementType statementType = getType();
                final SqlCountProcessor sqlCountProcessor;
                try {
                    sendExecute(statementType.isTypeWithSingletonResult() ? WireProtocolConstants.op_execute2 : WireProtocolConstants.op_execute, parameters);
                    if (!statementType.isTypeWithCursor()) {
                        // TODO Test if batching requests like this also work with earlier version of Firebird (works on 2.5)
                        sqlCountProcessor = createSqlCountProcessor();
                        sendInfoSql(sqlCountProcessor.getRecordCountInfoItems(), getDefaultSqlInfoSize());
                    } else {
                        sqlCountProcessor = null;
                    }
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    final boolean hasFields = getFieldDescriptor() != null && getFieldDescriptor().getCount() > 0;
                    final WarningMessageCallback statementWarningCallback = getStatementWarningCallback();
                    if (statementType.isTypeWithSingletonResult()) {
                        // A type with a singleton result (ie an execute procedure), doesn't actually have a result set that will be fetched, instead we have a singleton result if we have fields
                        statementListenerDispatcher.statementExecuted(this, false, hasFields);
                        processExecuteSingletonResponse(getDatabase().readSqlResponse(statementWarningCallback));
                        if (hasFields) {
                            setAllRowsFetched(true);
                        }
                    } else {
                        // A normal execute is never a singleton result (even if it only produces a single result)
                        statementListenerDispatcher.statementExecuted(this, hasFields, false);
                    }
                    processExecuteResponse(getDatabase().readGenericResponse(statementWarningCallback));
                    if (sqlCountProcessor != null) {
                        statementListenerDispatcher.sqlCounts(this, sqlCountProcessor.process(processInfoSqlResponse(getDatabase().readGenericResponse(statementWarningCallback))));
                    }

                    if (getState() != StatementState.ERROR) {
                        switchState(statementType.isTypeWithCursor() ? StatementState.CURSOR_OPEN : StatementState.PREPARED);
                    }
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
    protected void sendExecute(final int operation, final List<FieldValue> parameters) throws IOException, SQLException {
        assert operation == WireProtocolConstants.op_execute || operation == WireProtocolConstants.op_execute2 : "Needs to be called with operation op_execute or op_execute2";
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(operation);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(getTransaction().getHandle());

            final RowDescriptor parameterDescriptor = getParameterDescriptor();
            if (parameterDescriptor != null && parameterDescriptor.getCount() > 0) {
                xdrOut.writeBuffer(calculateBlr(parameterDescriptor));
                xdrOut.writeInt(0); // message number = in_message_type
                xdrOut.writeInt(1); // Number of messages
                writeSqlData(parameters);
            } else {
                xdrOut.writeBuffer(null);
                xdrOut.writeInt(0); // message number = in_message_type
                xdrOut.writeInt(0); // Number of messages
            }

            if (operation == WireProtocolConstants.op_execute2) {
                final RowDescriptor fieldDescriptor = getFieldDescriptor();
                xdrOut.writeBuffer(fieldDescriptor != null && fieldDescriptor.getCount() > 0 ? calculateBlr(fieldDescriptor) : null);
                xdrOut.writeInt(0); // out_message_number = out_message_type
            }
        }
    }

    /**
     * Process the execute response for statements with a singleton response (<code>op_execute2</code>; stored procedures).
     *
     * @param sqlResponse
     *         SQL response object
     * @throws SQLException
     * @throws IOException
     */
    protected void processExecuteSingletonResponse(SqlResponse sqlResponse) throws SQLException, IOException {
        if (sqlResponse.getCount() > 0) {
            queueRowData(readSqlData());
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

    @Override
    public void fetchRows(int fetchSize) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            if (!getState().isCursorOpen()) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
            }
            if (isAllRowsFetched()) return;

            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    sendFetch(fetchSize);
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
            while (!isAllRowsFetched() && (response = getDatabase().readResponse(getStatementWarningCallback())) instanceof FetchResponse) {
                final FetchResponse fetchResponse = (FetchResponse) response;
                if (fetchResponse.getCount() > 0 && fetchResponse.getStatus() == WireProtocolConstants.FETCH_OK) {
                    queueRowData(readSqlData());
                } else if (fetchResponse.getStatus() == WireProtocolConstants.FETCH_NO_MORE_ROWS) {
                    setAllRowsFetched(true);
                    getSqlCounts();
                    // Note: we are not explicitly 'closing' the cursor here
                } else {
                    // TODO Log, raise exception, or simply 'not possible'?
                    break;
                }
            }
            // TODO Handle other response type?
        }
    }

    /**
     * Sends the fetch request to the database.
     *
     * @param fetchSize Number of rows to fetch.
     * @throws SQLException
     * @throws IOException
     */
    protected void sendFetch(int fetchSize) throws SQLException, IOException {
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
    protected List<FieldValue> readSqlData() throws SQLException, IOException {
        final List<FieldValue> rowData = new ArrayList<FieldValue>(getFieldDescriptor().getCount());
        final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();

        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrInputStream xdrIn = getXdrIn();

            for (FieldDescriptor field : getFieldDescriptor()) {
                int len = blrCalculator.calculateIoLength(field);
                final FieldValue fieldValue = field.createDefaultFieldValue();
                byte[] buffer;
                if (len == 0) {
                    // Length specified in response
                    len = xdrIn.readInt();
                    buffer = new byte[len];
                    xdrIn.readFully(buffer, 0, len);
                    xdrIn.skipPadding(len);
                } else if (len < 0) {
                    // Buffer is not padded
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
                        final int buflen = buffer.length;
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

    @Override
    public void allocateStatement() throws SQLException {
        if (getState() != StatementState.NEW) {
            // TODO Is there a better sqlstate?
            throw new SQLNonTransientException("allocateOldStatement only allowed when current state is NEW", FBSQLException.SQL_STATE_GENERAL_ERROR);
        }
        synchronized (getDatabase().getSynchronizationObject()) {
            try {
                sendAllocate();
                getXdrOut().flush();
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                processAllocateResponse(getDatabase().readGenericResponse(getStatementWarningCallback()));
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
    protected void sendAllocate() throws SQLException, IOException {
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
    protected void processAllocateResponse(GenericResponse response) throws SQLException {
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
