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

import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;

import static org.firebirdsql.gds.ISCConstants.fb_cancel_abort;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabase} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class JnaDatabase extends AbstractFbDatabase<JnaDatabaseConnection> implements TransactionListener {

    // TODO Find out if there are any exception from JNA that we need to be prepared to handle.

    private static final ParameterConverter PARAMETER_CONVERTER = new JnaParameterConverter();
    public static final int STATUS_VECTOR_SIZE = 20;
    public static final int MAX_STATEMENT_LENGTH = 64 * 1024;

    private final FbClientLibrary clientLibrary;
    private final IntByReference handle = new IntByReference(0);
    private final ISC_STATUS[] statusVector = new ISC_STATUS[STATUS_VECTOR_SIZE];

    public JnaDatabase(JnaDatabaseConnection connection) {
        super(connection, connection.createDatatypeCoder());
        clientLibrary = connection.getClientLibrary();
    }

    /**
     * @return The client library instance associated with the database.
     */
    protected final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            // TODO Update message / externalize
            throw new SQLException("The connection is not attached to a database", FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
    }

    @Override
    protected void internalDetach() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_detach_database(statusVector, handle);
                processStatusVector();
            } catch (SQLException ex) {
                throw ex;
            } catch (Exception ex) {
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(connection.getServerName())
                        .cause(ex)
                        .toSQLException();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void attach() throws SQLException {
        DatabaseParameterBuffer dpb = ((DatabaseParameterBufferExtension) PARAMETER_CONVERTER
                .toDatabaseParameterBuffer(connection.getAttachProperties(), getEncodingFactory()))
                .removeExtensionParams();
        attachOrCreate(dpb, false);
    }

    protected void attachOrCreate(final DatabaseParameterBuffer dpb, final boolean create) throws SQLException {
        if (isAttached()) {
            throw new SQLException("Already attached to a database");
        }
        final byte[] dbName = getEncoding().encodeToCharset(connection.getAttachUrl());
        final byte[] dpbArray = dpb.toBytesWithType();

        synchronized (getSynchronizationObject()) {
            try {
                if (create) {
                    clientLibrary.isc_create_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray, getConnectionDialect());
                } else {
                    clientLibrary.isc_attach_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray);
                }
                processStatusVector();
            } catch (SQLException ex) {
                safelyDetach();
                throw ex;
            } catch (Exception ex) {
                safelyDetach();
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(connection.getServerName())
                        .cause(ex)
                        .toSQLException();
            }
            setAttached();
            afterAttachActions();
        }
    }

    /**
     * Additional tasks to execute directly after attach operation.
     * <p>
     * Implementation retrieves database information like dialect ODS and server
     * version.
     * </p>
     *
     * @throws SQLException
     *         For errors reading or writing database information.
     */
    protected void afterAttachActions() throws SQLException {
        getDatabaseInfo(getDescribeDatabaseInfoBlock(), 1024, getDatabaseInformationProcessor());
    }

    @Override
    public void createDatabase() throws SQLException {
        DatabaseParameterBuffer dpb = ((DatabaseParameterBufferExtension) PARAMETER_CONVERTER
                .toDatabaseParameterBuffer(connection.getAttachProperties(), getEncodingFactory()))
                .removeExtensionParams();
        attachOrCreate(dpb, true);
    }

    @Override
    public void dropDatabase() throws SQLException {
        checkConnected();
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_drop_database(statusVector, handle);
                processStatusVector();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        checkConnected();
        // TODO Test what happens with 2.1 and earlier client library
        // No synchronization, otherwise cancel will never work; might conflict with sync policy of JNA (TODO: find out)
        try {
            clientLibrary.fb_cancel_operation(statusVector, handle, (short) kind);
        } finally {
            if (kind == fb_cancel_abort) {
                setDetached();
            }
        }
    }

    @Override
    public JnaTransaction startTransaction(final TransactionParameterBuffer tpb) throws SQLException {
        checkConnected();
        final IntByReference transactionHandle = new IntByReference(0);
        byte[] tpbArray = tpb.toBytesWithType();
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_start_transaction(statusVector, transactionHandle, (short) 1, handle, (short) tpbArray.length, tpbArray);
            processStatusVector();

            final JnaTransaction transaction = new JnaTransaction(this, transactionHandle, TransactionState.ACTIVE);
            transactionAdded(transaction);
            return transaction;
        }
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        checkConnected();
        byte[] transactionIdBuffer = new byte[4];
        // Note: This uses an atypical encoding (as this is actually a TPB without a type)
        for (int i = 0; i < 4; i++) {
            transactionIdBuffer[i] = (byte) (transactionId >>> (i * 8));
        }
        final IntByReference transactionHandle = new IntByReference(0);
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_reconnect_transaction(statusVector, handle, transactionHandle,
                    (short) transactionIdBuffer.length, transactionIdBuffer);
            processStatusVector();

            final JnaTransaction transaction = new JnaTransaction(this, transactionHandle, TransactionState.PREPARED);
            transactionAdded(transaction);
            return transaction;
        }
    }

    @Override
    public JnaStatement createStatement(FbTransaction transaction) throws SQLException {
        checkConnected();
        JnaStatement stmt = new JnaStatement(this);
        stmt.setTransaction(transaction);
        return stmt;
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer)
            throws SQLException {
        return new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        return new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer, blobId);
    }

    @Override
    public byte[] getDatabaseInfo(final byte[] requestItems, final int maxBufferLength) throws SQLException {
        final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_database_info(statusVector, handle, (short) requestItems.length, requestItems,
                    (short) maxBufferLength, responseBuffer);
            processStatusVector();
        }
        byte[] responseArray = new byte[maxBufferLength];
        responseBuffer.get(responseArray);
        return responseArray;
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
        if (isAttached()) {
            if (transaction == null) {
                // TODO SQLState and/or Firebird specific error
                throw new SQLException("executeImmediate requires a transaction when attached");
            } else if (!(transaction instanceof JnaTransaction)) {
                // TODO SQLState and/or Firebird specific error
                throw new SQLNonTransientException(String.format("Invalid transaction handle type: %s, expected: %s",
                        transaction.getClass(), JnaTransaction.class));
            }
            checkTransactionActive(transaction);
        } else if (transaction != null) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLException("executeImmediate when not attached should have no transaction");
        }

        final byte[] statementArray = getEncoding().encodeToCharset(statementText);
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_dsql_execute_immediate(statusVector, handle,
                    transaction != null ? ((JnaTransaction) transaction).getJnaHandle() : new IntByReference(),
                    (short) statementArray.length, statementArray, getConnectionDialect(), null);
            processStatusVector();

            if (!isAttached()) {
                setAttached();
                afterAttachActions();
            }
        }
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    public IntByReference getJnaHandle() {
        return handle;
    }

    public final EncodingDefinition getEncodingDefinition() {
        return connection.getEncodingDefinition();
    }

    protected JnaEventHandle validateEventHandle(EventHandle eventHandle) throws SQLException {
        if (!(eventHandle instanceof JnaEventHandle)) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLNonTransientException(String.format("Invalid event handle type: %s, expected: %s",
                    eventHandle.getClass(), JnaEventHandle.class));
        }
        JnaEventHandle jnaEventHandle = (JnaEventHandle) eventHandle;
        if (jnaEventHandle.getSize() == -1) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLTransientException("Event handle hasn't been initialized");
        }
        return jnaEventHandle;
    }

    @Override
    public JnaEventHandle createEventHandle(String eventName, EventHandler eventHandler) throws SQLException {
        final JnaEventHandle eventHandle = new JnaEventHandle(eventName, eventHandler, getEncoding());
        synchronized (getSynchronizationObject()) {
            int size = clientLibrary.isc_event_block(eventHandle.getEventBuffer(), eventHandle.getResultBuffer(),
                    (short) 1, eventHandle.getEventNameMemory());
            eventHandle.setSize(size);
        }
        return eventHandle;
    }

    @Override
    public void countEvents(EventHandle eventHandle) throws SQLException {
        final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_event_counts(statusVector, (short) jnaEventHandle.getSize(),
                    jnaEventHandle.getEventBuffer().getValue(), jnaEventHandle.getResultBuffer().getValue());
        }
        jnaEventHandle.setEventCount(statusVector[0].intValue());
    }

    @Override
    public void queueEvent(EventHandle eventHandle) throws SQLException {
        checkConnected();
        final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

        synchronized (getSynchronizationObject()) {
            if (Platform.isWindows()) {
                ((WinFbClientLibrary) clientLibrary).isc_que_events(statusVector, getJnaHandle(), jnaEventHandle.getJnaEventId(),
                        (short) jnaEventHandle.getSize(), jnaEventHandle.getEventBuffer().getValue(),
                        (WinFbClientLibrary.IscEventStdCallback) jnaEventHandle.getCallback(), jnaEventHandle.getResultBuffer().getValue());
            } else {
                clientLibrary.isc_que_events(statusVector, getJnaHandle(), jnaEventHandle.getJnaEventId(),
                        (short) jnaEventHandle.getSize(), jnaEventHandle.getEventBuffer().getValue(),
                        jnaEventHandle.getCallback(), jnaEventHandle.getResultBuffer().getValue());
            }
            processStatusVector();
        }
    }

    @Override
    public void cancelEvent(EventHandle eventHandle) throws SQLException {
        checkConnected();
        final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_cancel_events(statusVector, getJnaHandle(), jnaEventHandle.getJnaEventId());
                processStatusVector();
            } finally {
                jnaEventHandle.releaseMemory(clientLibrary);
            }
        }
    }

    private void processStatusVector() throws SQLException {
        processStatusVector(statusVector, getDatabaseWarningCallback());
    }

    public void processStatusVector(ISC_STATUS[] statusVector, WarningMessageCallback warningMessageCallback)
            throws SQLException {
        if (warningMessageCallback == null) {
            warningMessageCallback = getDatabaseWarningCallback();
        }
        connection.processStatusVector(statusVector, warningMessageCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (isAttached()) {
                safelyDetach();
            }
        } finally {
            super.finalize();
        }
    }
}
