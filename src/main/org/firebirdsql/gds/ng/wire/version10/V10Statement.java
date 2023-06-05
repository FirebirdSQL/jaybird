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
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.*;
import org.firebirdsql.gds.ng.wire.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireStatement} implementation for the version 10 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10Statement extends AbstractFbWireStatement implements FbWireStatement {

    // TODO Handle error state in a consistent way (eg when does an exception lead to the error state, or when is it 'just' valid feedback)
    // TODO Fix state transitions

    private static final int NULL_INDICATOR_NOT_NULL = 0;
    private static final int NULL_INDICATOR_NULL = -1;

    private static final System.Logger log = System.getLogger(V10Statement.class.getName());

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
    protected void free(final int option) throws SQLException {
        try (LockCloseable ignored = withLock()) {
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

    /**
     * Handles sending the <em>free statement</em> packet and associated state changes on this statement
     *
     * @param option
     *         <em>free statement</em> option
     */
    protected void doFreePacket(int option) throws SQLException, IOException {
        sendFree(option);

        // Reset statement information
        reset(option == ISCConstants.DSQL_drop);
    }

    /**
     * Sends the <em>free statement</em> to the database
     *
     * @param option
     *         Free statement option
     */
    protected void sendFree(int option) throws IOException, SQLException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_free_statement);
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(option);
    }

    /**
     * Processes the response to the <em>free statement</em>.
     *
     * @param response
     *         Response object
     */
    protected void processFreeResponse(@SuppressWarnings("UnusedParameters") Response response) {
        // No processing needed
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

            final FbWireDatabase db = getDatabase();
            if (initialState == StatementState.NEW) {
                try {
                    sendAllocate();
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processAllocateResponse(db.readGenericResponse(getStatementWarningCallback()));
                    switchState(StatementState.ALLOCATED);
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                } catch (SQLException e) {
                    forceState(StatementState.NEW);
                    throw e;
                }
            } else {
                checkStatementValid();
            }

            try {
                sendPrepare(statementText);
                getXdrOut().flush();
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                processPrepareResponse(db.readGenericResponse(getStatementWarningCallback()));
            } catch (IOException ex) {
                switchState(StatementState.ERROR);
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            } catch (SQLException e) {
                switchState(StatementState.ALLOCATED);
                throw e;
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Sends the statement <em>prepare</em> to the connection.
     *
     * @param statementText
     *         Statement
     */
    protected void sendPrepare(final String statementText) throws SQLException, IOException {
        switchState(StatementState.PREPARING);
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_prepare_statement);
        xdrOut.writeInt(getTransaction().getHandle());
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(getDatabase().getConnectionDialect());
        xdrOut.writeString(statementText, getDatabase().getEncoding());
        xdrOut.writeBuffer(getStatementInfoRequestItems());
        xdrOut.writeInt(getDefaultSqlInfoSize());
    }

    /**
     * Processes the <em>prepare</em> response from the server.
     *
     * @param genericResponse
     *         GenericResponse
     */
    protected void processPrepareResponse(final GenericResponse genericResponse) throws SQLException {
        parseStatementInfo(genericResponse.getData());
        switchState(StatementState.PREPARED);
    }

    public void setCursorName(String cursorName) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            // TODO Check other statement states?

            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(WireProtocolConstants.op_set_cursor);
                xdrOut.writeInt(getHandle());
                // Null termination is needed due to a quirk of the protocol
                xdrOut.writeString(cursorName + '\0', getDatabase().getEncoding());
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
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void execute(final RowValue parameters) throws SQLException {
        final StatementState initialState = getState();
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            checkTransactionActive(getTransaction());
            validateParameters(parameters);
            reset(false);

            switchState(StatementState.EXECUTING);

            final StatementType statementType = getType();
            final boolean hasSingletonResult = hasSingletonResult();
            int expectedResponseCount = 0;

            try (OperationCloseHandle operationCloseHandle = signalExecute()) {
                if (operationCloseHandle.isCancelled()) {
                    // operation was synchronously cancelled from an OperationAware implementation
                    throw FbExceptionBuilder.forException(ISCConstants.isc_cancelled).toSQLException();
                }
                try {
                    if (hasSingletonResult) {
                        expectedResponseCount++;
                    }
                    sendExecute(hasSingletonResult
                                    ? WireProtocolConstants.op_execute2
                                    : WireProtocolConstants.op_execute,
                            parameters);
                    expectedResponseCount++;
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }

                final WarningMessageCallback statementWarningCallback = getStatementWarningCallback();
                try {
                    final FbWireDatabase db = getDatabase();
                    try {
                        expectedResponseCount--;
                        Response response = db.readResponse(statementWarningCallback);
                        if (hasSingletonResult) {
                            /* A type with a singleton result (ie an execute procedure with return fields), doesn't actually
                             * have a result set that will be fetched, instead we have a singleton result if we have fields
                             */
                            statementListenerDispatcher.statementExecuted(this, false, true);
                            if (response instanceof SqlResponse) {
                                processExecuteSingletonResponse((SqlResponse) response);
                                expectedResponseCount--;
                                response = db.readResponse(statementWarningCallback);
                            } else {
                                // We didn't get an op_sql_response first, something is iffy, maybe cancellation or very low level problem?
                                // We don't expect any more responses after this
                                expectedResponseCount = 0;
                                SQLWarning sqlWarning = new SQLWarning(
                                        "Expected an SqlResponse, instead received a " + response.getClass().getName());
                                log.log(System.Logger.Level.WARNING, "Unexpected response; see debug level for stacktrace");
                                log.log(System.Logger.Level.DEBUG, "Unexpected response", sqlWarning);
                                statementWarningCallback.processWarning(sqlWarning);
                            }
                            setAfterLast();
                        } else {
                            // A normal execute is never a singleton result (even if it only produces a single result)
                            statementListenerDispatcher.statementExecuted(this, hasFields(), false);
                        }

                        // This should always be a GenericResponse, otherwise something went fundamentally wrong anyway
                        processExecuteResponse((GenericResponse) response);
                    } catch (SQLException e) {
                        if (e.getErrorCode() == ISCConstants.isc_cancelled) {
                            expectedResponseCount = 0;
                        }
                        throw e;
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
            }
        } catch (SQLException e) {
            if (getState() != StatementState.ERROR) {
                switchState(initialState);
            }
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Sends the <em>execute</em> (for {@code op_execute} or {@code op_execute2}) to the database.
     *
     * @param operation
     *         Operation ({@code op_execute} or {@code op_execute2})
     * @param parameters
     *         Parameters
     */
    protected void sendExecute(final int operation, final RowValue parameters) throws IOException, SQLException {
        assert operation == WireProtocolConstants.op_execute || operation == WireProtocolConstants.op_execute2 : "Needs to be called with operation op_execute or op_execute2";
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(operation);
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(getTransaction().getHandle());

        if (parameters != null && parameters.getCount() > 0) {
            final RowDescriptor parameterDescriptor = getParameterDescriptor();
            xdrOut.writeBuffer(calculateBlr(parameterDescriptor, parameters));
            xdrOut.writeInt(0); // message number = in_message_type
            xdrOut.writeInt(1); // Number of messages
            writeSqlData(parameterDescriptor, parameters, true);
        } else {
            xdrOut.writeBuffer(null);
            xdrOut.writeInt(0); // message number = in_message_type
            xdrOut.writeInt(0); // Number of messages
        }

        if (operation == WireProtocolConstants.op_execute2) {
            final RowDescriptor fieldDescriptor = getRowDescriptor();
            xdrOut.writeBuffer(fieldDescriptor != null && fieldDescriptor.getCount() > 0 ? calculateBlr(fieldDescriptor) : null);
            xdrOut.writeInt(0); // out_message_number = out_message_type
        }
    }

    /**
     * Process the <em>execute</em> response for statements with a singleton response ({@code op_execute2}; stored
     * procedures).
     *
     * @param sqlResponse
     *         SQL response object
     */
    protected void processExecuteSingletonResponse(SqlResponse sqlResponse) throws SQLException, IOException {
        if (sqlResponse.getCount() > 0) {
            queueRowData(readSqlData());
        }
    }

    /**
     * Process the <em>execute</em> response.
     *
     * @param genericResponse
     *         Generic response object
     */
    protected void processExecuteResponse(GenericResponse genericResponse) {
        // Nothing to do here
    }

    @Override
    public void fetchRows(int fetchSize) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            if (!getState().isCursorOpen()) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
            }
            if (isAfterLast()) return;

            try (OperationCloseHandle operationCloseHandle = signalFetch()) {
                if (operationCloseHandle.isCancelled()) {
                    // operation was synchronously cancelled from an OperationAware implementation
                    throw FbExceptionBuilder.forException(ISCConstants.isc_cancelled).toSQLException();
                }
                try {
                    sendFetch(fetchSize);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    processFetchResponse(FetchDirection.FORWARD);
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Process the <em>fetch</em> response by reading the returned rows and queuing them.
     *
     * @param direction
     *         fetch direction
     */
    protected void processFetchResponse(FetchDirection direction) throws IOException, SQLException {
        Response response;
        while ((response = getDatabase().readResponse(getStatementWarningCallback())) instanceof FetchResponse) {
            final FetchResponse fetchResponse = (FetchResponse) response;
            if (fetchResponse.getCount() > 0 && fetchResponse.getStatus() == ISCConstants.FETCH_OK) {
                queueRowData(readSqlData());
            } else if (fetchResponse.getStatus() == ISCConstants.FETCH_NO_MORE_ROWS) {
                switch (direction) {
                case IN_PLACE:
                    if (isBeforeFirst()) {
                        setBeforeFirst();
                    } else {
                        setAfterLast();
                    }
                    break;
                case UNKNOWN:
                    // Not generally applicable, but handling as after-last
                case FORWARD:
                    setAfterLast();
                    break;
                case REVERSE:
                    setBeforeFirst();
                    break;
                }
                // Note: we are not explicitly 'closing' the cursor here as we might be scrolling
                // Exit loop
                break;
            } else {
                log.log(System.Logger.Level.DEBUG, "Received unexpected fetch response {0}, ignored", fetchResponse);
                break;
            }
        }
        // TODO Handle other response type?
    }

    /**
     * Sends the <em>fetch</em> request to the database.
     *
     * @param fetchSize Number of rows to fetch.
     */
    protected void sendFetch(int fetchSize) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_fetch);
        xdrOut.writeInt(getHandle());
        xdrOut.writeBuffer(hasFetched() ? null : calculateBlr(getRowDescriptor()));
        xdrOut.writeInt(0); // out_message_number = out_message_type
        xdrOut.writeInt(fetchSize); // fetch size
    }

    /**
     * Reads a single row from the database.
     *
     * @return Row as a {@link RowValue}
     */
    protected RowValue readSqlData() throws SQLException, IOException {
        final RowDescriptor rowDescriptor = getRowDescriptor();
        final RowValue rowValue = rowDescriptor.createDefaultFieldValues();
        final BlrCalculator blrCalculator = getBlrCalculator();

        final XdrInputStream xdrIn = getXdrIn();

        for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
            final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
            final int len = blrCalculator.calculateIoLength(fieldDescriptor);
            byte[] buffer = readColumnData(xdrIn, len);
            if (xdrIn.readInt() == NULL_INDICATOR_NULL) {
                buffer = null;
            }
            rowValue.setFieldData(idx, buffer);
        }
        return rowValue;
    }

    protected byte[] readColumnData(XdrInputStream xdrIn, int len) throws IOException {
        if (len == 0) {
            // Length specified in response
            return xdrIn.readBuffer();
        } else if (len < 0) {
            // Buffer is not padded
            return xdrIn.readRawBuffer(-len);
        } else {
            // len is incremented in calculateIoLength to avoid value 0, so it must be decremented
            return xdrIn.readBuffer(len - 1);
        }
    }

    /**
     * Write a set of SQL data from a {@link RowValue}.
     *
     * @param rowDescriptor
     *         The row descriptor
     * @param fieldValues
     *         The List containing the SQL data to be written
     * @param useActualLength
     *         Should actual field length be used (applies to CHAR)
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    protected void writeSqlData(RowDescriptor rowDescriptor, RowValue fieldValues, boolean useActualLength)
        throws IOException, SQLException {
        final XdrOutputStream xdrOut = getXdrOut();
        final BlrCalculator blrCalculator = getBlrCalculator();
        for (int idx = 0; idx < fieldValues.getCount(); idx++) {
            final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
            final byte[] buffer = fieldValues.getFieldData(idx);
            final int len = useActualLength
                    ? blrCalculator.calculateIoLength(fieldDescriptor, buffer)
                    : blrCalculator.calculateIoLength(fieldDescriptor);
            writeColumnData(xdrOut, len, buffer, fieldDescriptor);
            // sqlind (null indicator)
            xdrOut.writeInt(buffer != null ? NULL_INDICATOR_NOT_NULL : NULL_INDICATOR_NULL);
        }
    }

    protected void writeColumnData(XdrOutputStream xdrOut, int len, byte[] buffer, FieldDescriptor fieldDescriptor)
            throws IOException {
        final int tempType = fieldDescriptor.getType() & ~1;

        // TODO Correctly pad with 0x00 instead of 0x20 for octets.
        if (tempType == ISCConstants.SQL_NULL) {
            // Nothing to write for SQL_NULL (except null indicator, which happens at end in writeSqlData)
        } else if (len == 0) {
            if (buffer != null) {
                len = buffer.length;
                xdrOut.writeInt(len);
                xdrOut.write(buffer, 0, len);
                xdrOut.writePadding((4 - len) & 3, fieldDescriptor.getPaddingByte());
            } else {
                xdrOut.writeInt(0);
            }
        } else if (len < 0) {
            if (buffer != null) {
                xdrOut.write(buffer, 0, -len);
            } else {
                xdrOut.writeZeroPadding(-len);
            }
        } else {
            // decrement length because it was incremented before
            // increment happens in BlrCalculator.calculateIoLength
            len--;
            if (buffer != null) {
                final int buflen = buffer.length;
                if (buflen >= len) {
                    xdrOut.write(buffer, 0, len);
                    xdrOut.writePadding((4 - len) & 3, fieldDescriptor.getPaddingByte());
                } else {
                    xdrOut.write(buffer, 0, buflen);
                    xdrOut.writePadding(len - buflen + ((4 - len) & 3), fieldDescriptor.getPaddingByte());
                }
            } else {
                xdrOut.writePadding(len + ((4 - len) & 3), fieldDescriptor.getPaddingByte());
            }
        }
    }

    /**
     * Sends the <em>allocate</em> request to the server.
     */
    protected void sendAllocate() throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_allocate_statement);
        xdrOut.writeInt(0);
    }

    /**
     * Processes the <em>allocate</em> response from the server.
     *
     * @param response
     *         GenericResponse
     */
    protected void processAllocateResponse(GenericResponse response) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            setHandle(response.getObjectHandle());
            reset();
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
}
