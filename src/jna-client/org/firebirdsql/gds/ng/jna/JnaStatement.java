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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.jna.fbclient.XSQLDA;
import org.firebirdsql.jna.fbclient.XSQLVAR;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbStatement} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaStatement extends AbstractFbStatement {

    private final IntByReference handle = new IntByReference(0);
    private JnaDatabase database;
    private final ISC_STATUS[] statusVector = new ISC_STATUS[JnaDatabase.STATUS_VECTOR_SIZE];
    private final FbClientLibrary clientLibrary;
    private XSQLDA inXSqlDa;
    private XSQLDA outXSqlDa;

    public JnaStatement(JnaDatabase database) {
        this.database = database;
        clientLibrary = database.getClientLibrary();
    }

    @Override
    protected void setParameterDescriptor(RowDescriptor parameterDescriptor) {
        final XSQLDA xsqlda = allocateXSqlDa(parameterDescriptor);
        synchronized (getSynchronizationObject()) {
            inXSqlDa = xsqlda;
            super.setParameterDescriptor(parameterDescriptor);
        }
    }

    @Override
    protected void setFieldDescriptor(RowDescriptor fieldDescriptor) {
        final XSQLDA xsqlda = allocateXSqlDa(fieldDescriptor);
        synchronized (getSynchronizationObject()) {
            outXSqlDa = xsqlda;
            super.setFieldDescriptor(fieldDescriptor);
        }
    }

    @Override
    protected void free(int option) throws SQLException {
        synchronized (getSynchronizationObject()) {
            final JnaDatabase db = getDatabase();
            synchronized (db.getSynchronizationObject()) {
                clientLibrary.isc_dsql_free_statement(statusVector, handle, (short) option);
            }
            processStatusVector();
            // Reset statement information
            reset(option == ISCConstants.DSQL_drop);
        }
    }

    @Override
    protected boolean isValidTransactionClass(Class<? extends FbTransaction> transactionClass) {
        return JnaTransaction.class.isAssignableFrom(transactionClass);
    }

    @Override
    public JnaDatabase getDatabase() {
        return database;
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    @Override
    public JnaTransaction getTransaction() {
        return (JnaTransaction) super.getTransaction();
    }

    @Override
    public void prepare(String statementText) throws SQLException {
        try {
            final byte[] statementArray = getDatabase().getEncoding().encodeToCharset(statementText);
            if (statementArray.length > JnaDatabase.MAX_STATEMENT_LENGTH) {
                // TODO Message + sqlstate
                throw new SQLException(String.format("Implementation limit exceeded, maximum statement length is %d bytes",
                        JnaDatabase.MAX_STATEMENT_LENGTH));
            }
            synchronized (getSynchronizationObject()) {
                checkTransactionActive(getTransaction());
                final StatementState currentState = getState();
                if (!isPrepareAllowed(currentState)) {
                    throw new SQLNonTransientException(String.format("Current statement state (%s) does not allow call to prepare", currentState));
                }
                resetAll();
                final JnaDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    if (currentState == StatementState.NEW) {
                        clientLibrary.isc_dsql_allocate_statement(statusVector, db.getJnaHandle(), handle);
                        processStatusVector();
                        setAllRowsFetched(false);
                        switchState(StatementState.ALLOCATED);
                        setType(StatementType.NONE);
                    } else {
                        checkStatementValid();
                    }

                    // Information in tempXSqlDa is ignored, as we are retrieving more detailed information using getSqlInfo
                    final XSQLDA tempXSqlDa = new XSQLDA();
                    clientLibrary.isc_dsql_prepare(statusVector, getTransaction().getJnaHandle(), handle,
                            (short) statementArray.length, statementArray, db.getConnectionDialect(), tempXSqlDa);
                    processStatusVector();

                    final byte[] statementInfoRequestItems = getStatementInfoRequestItems();
                    final int responseLength = getDefaultSqlInfoSize();
                    byte[] statementInfo = getSqlInfo(statementInfoRequestItems, responseLength);
                    parseStatementInfo(statementInfo);
                }
                switchState(StatementState.PREPARED);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void execute(RowValue parameters) throws SQLException {
        final StatementState initialState = getState();
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                checkTransactionActive(getTransaction());
                validateParameters(parameters);
                reset(false);

                final JnaDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    switchState(StatementState.EXECUTING);

                    setXSqlDaData(inXSqlDa, getParameterDescriptor(), parameters);
                    final StatementType statementType = getType();
                    if (statementType.isTypeWithSingletonResult()) {
                        clientLibrary.isc_dsql_execute2(statusVector, getTransaction().getJnaHandle(), handle,
                                inXSqlDa.version, inXSqlDa, outXSqlDa);
                    } else {
                        clientLibrary.isc_dsql_execute(statusVector, getTransaction().getJnaHandle(), handle,
                                inXSqlDa.version, inXSqlDa);
                    }
                    processStatusVector();

                    final boolean hasFields = getFieldDescriptor() != null && getFieldDescriptor().getCount() > 0;
                    if (statementType.isTypeWithSingletonResult()) {
                        /* A type with a singleton result (ie an execute procedure), doesn't actually have a
                         * result set that will be fetched, instead we have a singleton result if we have fields
                         */
                        statementListenerDispatcher.statementExecuted(this, false, hasFields);
                        if (hasFields) {
                            queueRowData(toRowValue(getFieldDescriptor(), outXSqlDa));
                            setAllRowsFetched(true);
                        }
                    } else {
                        // A normal execute is never a singleton result (even if it only produces a single result)
                        statementListenerDispatcher.statementExecuted(this, hasFields, false);
                    }

                    if (!statementType.isTypeWithCursor() && statementType.isTypeWithUpdateCounts()) {
                        getSqlCounts();
                    }

                    if (getState() != StatementState.ERROR) {
                        switchState(statementType.isTypeWithCursor() ? StatementState.CURSOR_OPEN : StatementState.PREPARED);
                    }
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
     * Creates and populates a XSQLDA from the row descriptor and parameter values.
     *
     * @param xSqlDa
     *         XSQLDA
     * @param rowDescriptor
     *         Row descriptor
     * @param parameters
     *         Parameter values
     */
    protected void setXSqlDaData(final XSQLDA xSqlDa, final RowDescriptor rowDescriptor, final RowValue parameters) {
        for (int idx = 0; idx < parameters.getCount(); idx++) {
            final XSQLVAR xSqlVar = xSqlDa.sqlvar[idx];
            // Zero-fill sqldata
            xSqlVar.getSqlData().clear();

            FieldValue value = parameters.getFieldValue(idx);
            byte[] fieldData = value.getFieldData();
            if (fieldData == null) {
                // Note this only works because we mark the type as nullable in allocateXSqlDa
                xSqlVar.sqlind.setValue(XSQLVAR.SQLIND_NULL);
            } else {
                xSqlVar.sqlind.setValue(XSQLVAR.SQLIND_NOT_NULL);

                // TODO Throw truncation error if fieldData longer than sqllen?

                final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
                int bufferOffset = 0;
                if (fieldDescriptor.isVarying()) {
                    // Only send the data we need
                    xSqlVar.sqllen = (short) Math.min(fieldDescriptor.getLength(), fieldData.length);
                    xSqlVar.sqldata.setShort(0, (short) fieldData.length);
                    bufferOffset = 2;
                } else if (fieldDescriptor.isFbType(ISCConstants.SQL_TEXT)) {
                    // Only send the data we need
                    xSqlVar.sqllen = (short) Math.min(fieldDescriptor.getLength(), fieldData.length);
                    if (fieldDescriptor.getSubType() != ISCConstants.CS_BINARY) {
                        // Non-binary CHAR field: fill with spaces
                        xSqlVar.sqldata.setMemory(0, xSqlVar.sqllen & 0xff, (byte) ' ');
                    }
                }
                xSqlVar.sqldata.write(bufferOffset, fieldData, 0, fieldData.length);
            }
        }
    }

    /**
     * Creates an XSQLDA, populates type information and allocates memory for the sqldata fields.
     *
     * @param rowDescriptor
     *         The row descriptor
     * @return Allocated XSQLDA without data
     */
    protected XSQLDA allocateXSqlDa(RowDescriptor rowDescriptor) {
        if (rowDescriptor == null || rowDescriptor.getCount() == 0) {
            final XSQLDA xSqlDa = new XSQLDA(1);
            xSqlDa.sqld = xSqlDa.sqln = 0;
            return xSqlDa;
        }
        final XSQLDA xSqlDa = new XSQLDA(rowDescriptor.getCount());

        for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
            final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
            final XSQLVAR xSqlVar = xSqlDa.sqlvar[idx];

            xSqlVar.sqltype = (short) (fieldDescriptor.getType() | 1); // Always make nullable
            xSqlVar.sqlsubtype = (short) fieldDescriptor.getSubType();
            xSqlVar.sqlscale = (short) fieldDescriptor.getScale();
            xSqlVar.sqllen = (short) fieldDescriptor.getLength();
            xSqlVar.sqlind = new ShortByReference();

            final int requiredDataSize = fieldDescriptor.isVarying()
                    ? fieldDescriptor.getLength() + 3 // 2 bytes for length, 1 byte for nul terminator
                    : fieldDescriptor.getLength() + 1; // 1 byte for nul terminator

            xSqlVar.sqldata = new Memory(requiredDataSize);
        }
        return xSqlDa;
    }

    /**
     * Converts the data from an XSQLDA to a RowValue.
     *
     * @param rowDescriptor
     *         Row descriptor
     * @param xSqlDa
     *         XSQLDA
     * @return Row value
     */
    protected RowValue toRowValue(RowDescriptor rowDescriptor, XSQLDA xSqlDa) {
        final RowValueBuilder row = new RowValueBuilder(rowDescriptor);

        for (int idx = 0; idx < xSqlDa.sqlvar.length; idx++) {
            final XSQLVAR xSqlVar = xSqlDa.sqlvar[idx];

            row.setFieldIndex(idx);

            if (xSqlVar.sqlind.getValue() == XSQLVAR.SQLIND_NULL) {
                row.set(null);
            } else {
                int bufferOffset = 0;
                int bufferLength = xSqlVar.sqllen;

                if (rowDescriptor.getFieldDescriptor(idx).isVarying()) {
                    bufferOffset = 2;
                    bufferLength = xSqlVar.sqldata.getShort(0) & 0xff;
                }

                byte[] data = new byte[bufferLength];
                xSqlVar.sqldata.read(bufferOffset, data, 0, bufferLength);
                row.set(data);
            }
        }
        return row.toRowValue(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The JNA implementation ignores the specified {@code fetchSize} to prevent problems with - for example -
     * positioned updates with named cursors. For the wire protocol that case is handled by the server ignoring the
     * fetch size. Internally the native fetch will batch a number of records, but the number is outside our control.
     * </p>
     */
    @Override
    public void fetchRows(int fetchSize) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                if (!getState().isCursorOpen()) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
                }
                if (isAllRowsFetched()) return;

                final JnaDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    final ISC_STATUS fetchStatus = clientLibrary.isc_dsql_fetch(statusVector, handle, outXSqlDa.version,
                            outXSqlDa);
                    processStatusVector();

                    int fetchStatusInt = fetchStatus.intValue();
                    if (fetchStatusInt == ISCConstants.FETCH_OK) {
                        queueRowData(toRowValue(getFieldDescriptor(), outXSqlDa));
                    } else if (fetchStatusInt == ISCConstants.FETCH_NO_MORE_ROWS) {
                        setAllRowsFetched(true);
                        getSqlCounts();
                        // Note: we are not explicitly 'closing' the cursor here
                    } else {
                        // TODO Log, raise exception, or simply 'not possible'?
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(bufferLength);

            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                final JnaDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    clientLibrary.isc_dsql_sql_info(statusVector, handle,
                            (short) requestItems.length, requestItems,
                            (short) bufferLength, responseBuffer);
                }
                processStatusVector();
            }

            byte[] responseArr = new byte[bufferLength];
            responseBuffer.get(responseArr);
            return responseArr;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int getDefaultSqlInfoSize() {
        // TODO Test for an optimal buffer size
        return getMaxSqlInfoSize();
    }

    @Override
    public int getMaxSqlInfoSize() {
        // TODO Is this the actual max, or is it 65535?
        return 32767;
    }

    @Override
    public void setCursorName(String cursorName) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                final JnaDatabase db = getDatabase();
                synchronized (db.getSynchronizationObject()) {
                    clientLibrary.isc_dsql_set_cursor_name(statusVector, handle,
                            // Null termination is needed due to a quirk of the protocol
                            db.getEncoding().encodeToCharset(cursorName + '\0'),
                            // Cursor type
                            (short) 0);
                }
                processStatusVector();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void processStatusVector() throws SQLException {
        getDatabase().processStatusVector(statusVector, getStatementWarningCallback());
    }
}
