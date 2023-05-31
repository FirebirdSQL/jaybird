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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.firebirdsql.gds.ISCConstants.fb_cancel_abort;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabase} for native client access.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class JnaDatabase extends AbstractFbDatabase<JnaDatabaseConnection>
        implements JnaAttachment, TransactionListener, FbClientFeatureAccess {

    // TODO Find out if there are any exception from JNA that we need to be prepared to handle.

    private static final ParameterConverter<JnaDatabaseConnection, ?> PARAMETER_CONVERTER = new JnaParameterConverter();
    public static final int STATUS_VECTOR_SIZE = 20;
    public static final int MAX_STATEMENT_LENGTH = 64 * 1024;

    private final FbClientLibrary clientLibrary;
    private final Set<FbClientFeature> clientFeatures;
    protected final IntByReference handle = new IntByReference(0);
    protected final ISC_STATUS[] statusVector = new ISC_STATUS[STATUS_VECTOR_SIZE];
    private Cleaner.Cleanable cleanable = Cleaners.getNoOp();

    public JnaDatabase(JnaDatabaseConnection connection) {
        super(connection, connection.createDatatypeCoder());
        clientLibrary = connection.getClientLibrary();
        if (clientLibrary instanceof FbClientFeatureAccess) {
            clientFeatures = ((FbClientFeatureAccess) clientLibrary).getFeatures();
        } else {
            clientFeatures = emptySet();
        }
    }

    /**
     * @return The client library instance associated with the database.
     */
    protected final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    protected void setDetachedJna() {
        try {
            cleanable.clean();
        } finally {
            setDetached();
        }
    }

    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notAttachedToDatabase).toSQLException();
        }
    }

    @Override
    protected void internalDetach() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            try {
                clientLibrary.isc_detach_database(statusVector, handle);
                processStatusVector();
            } catch (SQLException ex) {
                throw ex;
            } catch (Exception ex) {
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(connection.getAttachUrl())
                        .cause(ex)
                        .toSQLException();
            } finally {
                setDetachedJna();
            }
        }
    }

    @Override
    public void attach() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = PARAMETER_CONVERTER.toDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, false);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected void attachOrCreate(final DatabaseParameterBuffer dpb, final boolean create) throws SQLException {
        if (isAttached()) {
            throw new SQLException("Already attached to a database");
        }
        final byte[] dbName = getEncoding().encodeToCharset(connection.getAttachUrl());
        final byte[] dpbArray = dpb.toBytesWithType();

        try (LockCloseable ignored = withLock()) {
            try {
                if (create) {
                    clientLibrary.isc_create_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray, getConnectionDialect());
                } else {
                    clientLibrary.isc_attach_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray);
                }
                if (handle.getValue() != 0) {
                    cleanable = Cleaners.getJbCleaner().register(this, new CleanupAction(handle, clientLibrary));
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
                        .messageParameter(connection.getAttachUrl())
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
        try {
            final DatabaseParameterBuffer dpb = PARAMETER_CONVERTER.toDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, true);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void dropDatabase() throws SQLException {
        try {
            checkConnected();
            try (LockCloseable ignored = withLock()) {
                try {
                    clientLibrary.isc_drop_database(statusVector, handle);
                    processStatusVector();
                } finally {
                    setDetachedJna();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        try {
            checkConnected();
            // TODO Test what happens with 2.1 and earlier client library
            // No synchronization, otherwise cancel will never work; might conflict with sync policy of JNA (TODO: find out)
            try {
                clientLibrary.fb_cancel_operation(statusVector, handle, (short) kind);
            } finally {
                if (kind == fb_cancel_abort) {
                    setDetachedJna();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public JnaTransaction startTransaction(final TransactionParameterBuffer tpb) throws SQLException {
        try {
            checkConnected();
            final IntByReference transactionHandle = new IntByReference(0);
            final byte[] tpbArray = tpb.toBytesWithType();
            try (LockCloseable ignored = withLock()) {
                clientLibrary.isc_start_transaction(statusVector, transactionHandle, (short) 1, handle,
                        (short) tpbArray.length, tpbArray);
                processStatusVector();

                final JnaTransaction transaction = new JnaTransaction(this, transactionHandle, TransactionState.ACTIVE);
                transactionAdded(transaction);
                return transaction;
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        try {
            checkConnected();
            final byte[] transactionIdBuffer = getTransactionIdBuffer(transactionId);

            final IntByReference transactionHandle = new IntByReference(0);
            try (LockCloseable ignored = withLock()) {
                clientLibrary.isc_reconnect_transaction(statusVector, handle, transactionHandle,
                        (short) transactionIdBuffer.length, transactionIdBuffer);
                processStatusVector();

                final JnaTransaction transaction =
                        new JnaTransaction(this, transactionHandle, TransactionState.PREPARED);
                transactionAdded(transaction);
                return transaction;
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public JnaStatement createStatement(FbTransaction transaction) throws SQLException {
        try {
            checkConnected();
            final JnaStatement stmt = new JnaStatement(this);
            stmt.addExceptionListener(exceptionListenerDispatcher);
            stmt.setTransaction(transaction);
            return stmt;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        final JnaBlob jnaBlob = new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer);
        jnaBlob.addExceptionListener(exceptionListenerDispatcher);
        return jnaBlob;
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) {
        final JnaBlob jnaBlob = new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer, blobId);
        jnaBlob.addExceptionListener(exceptionListenerDispatcher);
        return jnaBlob;
    }

    @Override
    public byte[] getDatabaseInfo(final byte[] requestItems, final int maxBufferLength) throws SQLException {
        try {
            final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
            try (LockCloseable ignored = withLock()) {
                clientLibrary.isc_database_info(statusVector, handle, (short) requestItems.length, requestItems,
                        (short) maxBufferLength, responseBuffer);
                processStatusVector();
            }
            final byte[] responseArray = new byte[maxBufferLength];
            responseBuffer.get(responseArray);
            return responseArray;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
        try {
            if (isAttached()) {
                if (transaction == null) {
                    throw FbExceptionBuilder
                            .forException(JaybirdErrorCodes.jb_executeImmediateRequiresTransactionAttached)
                            .toSQLException();
                } else if (!(transaction instanceof JnaTransaction)) {
                    // TODO SQLState and/or Firebird specific error
                    throw new SQLNonTransientException(format("Invalid transaction handle type: %s, expected: %s",
                            transaction.getClass(), JnaTransaction.class));
                }
                checkTransactionActive(transaction);
            } else if (transaction != null) {
                throw FbExceptionBuilder
                        .forException(JaybirdErrorCodes.jb_executeImmediateRequiresNoTransactionDetached)
                        .toSQLException();
            }

            final byte[] statementArray = getEncoding().encodeToCharset(statementText);
            try (LockCloseable ignored = withLock()) {
                clientLibrary.isc_dsql_execute_immediate(statusVector, handle,
                        transaction != null ? ((JnaTransaction) transaction).getJnaHandle() : new IntByReference(),
                        (short) statementArray.length, statementArray, getConnectionDialect(), null);
                processStatusVector();

                if (!isAttached()) {
                    setAttached();
                    afterAttachActions();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        throw new FBDriverNotCapableException(
                "Setting network timeout not supported in native implementation");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new FBDriverNotCapableException(
                "Getting network timeout not supported in native implementation");
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
            throw new SQLNonTransientException(format("Invalid event handle type: %s, expected: %s",
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
        // TODO Any JNA errors we need to track and convert to SQLException here?
        final JnaEventHandle eventHandle = new JnaEventHandle(eventName, eventHandler, getEncoding());
        try (LockCloseable ignored = withLock()) {
            synchronized (eventHandle) {
                int size = clientLibrary.isc_event_block(eventHandle.getEventBuffer(), eventHandle.getResultBuffer(),
                        (short) 1, eventHandle.getEventNameMemory());
                eventHandle.setSize(size);
            }
        }
        return eventHandle;
    }

    @Override
    public void countEvents(EventHandle eventHandle) throws SQLException {
        try {
            final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

            try (LockCloseable ignored = withLock()) {
                synchronized (jnaEventHandle) {
                    clientLibrary.isc_event_counts(statusVector, (short) jnaEventHandle.getSize(),
                            jnaEventHandle.getEventBuffer().getValue(), jnaEventHandle.getResultBuffer().getValue());
                }
            }
            jnaEventHandle.setEventCount(statusVector[0].intValue());
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void queueEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkConnected();
            final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

            try (LockCloseable ignored = withLock()) {
                synchronized (jnaEventHandle) {
                    if (Platform.isWindows()) {
                        ((WinFbClientLibrary) clientLibrary).isc_que_events(statusVector, getJnaHandle(),
                                jnaEventHandle.getJnaEventId(),
                                (short) jnaEventHandle.getSize(), jnaEventHandle.getEventBuffer().getValue(),
                                (WinFbClientLibrary.IscEventStdCallback) jnaEventHandle.getCallback(),
                                jnaEventHandle.getResultBuffer().getValue());
                    } else {
                        clientLibrary.isc_que_events(statusVector, getJnaHandle(), jnaEventHandle.getJnaEventId(),
                                (short) jnaEventHandle.getSize(), jnaEventHandle.getEventBuffer().getValue(),
                                jnaEventHandle.getCallback(), jnaEventHandle.getResultBuffer().getValue());
                    }
                }
                processStatusVector();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void cancelEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkConnected();
            final JnaEventHandle jnaEventHandle = validateEventHandle(eventHandle);

            try (LockCloseable ignored = withLock()) {
                synchronized (jnaEventHandle) {
                    try {
                        clientLibrary.isc_cancel_events(statusVector, getJnaHandle(), jnaEventHandle.getJnaEventId());
                        processStatusVector();
                    } finally {
                        jnaEventHandle.releaseMemory(clientLibrary);
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbBatch createBatch(FbTransaction transaction, String statement, FbMessageMetadata metadata, BatchParameterBuffer parameters) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    @Override
    public FbBatch createBatch(FbTransaction transaction, String statement, BatchParameterBuffer parameters) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    @Override
    public FbMetadataBuilder getMetadataBuilder(int fieldCount) throws SQLException  {
        throw new FBDriverNotCapableException();
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
    public boolean hasFeature(FbClientFeature clientFeature) {
        return clientFeatures.contains(clientFeature);
    }

    @Override
    public Set<FbClientFeature> getFeatures() {
        return clientFeatures;
    }

    private record CleanupAction(IntByReference handle, FbClientLibrary library) implements Runnable {
        @Override
        public void run() {
            if (handle.getValue() == 0) return;
            try {
                library.isc_detach_database(new ISC_STATUS[STATUS_VECTOR_SIZE], handle);
            } finally {
                handle.setValue(0);
            }
        }
    }
    
}
