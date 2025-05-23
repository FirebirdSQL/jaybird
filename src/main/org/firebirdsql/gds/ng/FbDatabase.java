// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;

import java.sql.SQLException;

/**
 * Connection handle to a database.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface, and those exceptions notified by all {@link ExceptionListenable} implementations created
 * from them.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbDatabase extends FbAttachment {

    /**
     * Creates a new database, connection remains attached to database.
     */
    void createDatabase() throws SQLException;

    /**
     * Drops (and deletes) the currently attached database.
     */
    void dropDatabase() throws SQLException;

    /**
     * Cancels the current operation.
     * <p>
     * The cancellation types are:
     * <dl>
     *     <dt>{@link org.firebirdsql.gds.ISCConstants#fb_cancel_disable}</dt>
     *     <dd>disables execution of fb_cancel_raise requests for the specified attachment. It can be useful when your
     *     program is executing critical operations, such as cleanup, for example.</dd>
     *     <dt>{@link org.firebirdsql.gds.ISCConstants#fb_cancel_enable}</dt>
     *     <dd>re-enables delivery of a cancel execution that was previously disabled. The 'cancel' state is effective
     *     by default, being initialized when the attachment is created.</dd>
     *     <dt>{@link org.firebirdsql.gds.ISCConstants#fb_cancel_raise}</dt>
     *     <dd>cancels any activity related to the database handle. The effect will be that, as soon as possible, the
     *     engine will try to stop the running request and return an exception to the caller</dd>
     *     <dt>{@link org.firebirdsql.gds.ISCConstants#fb_cancel_abort}</dt>
     *     <dd>forcibly close client side of connection. Useful if you need to close a connection urgently. All active
     *     transactions will be rolled back by the server. 'Success' is always returned to the application. Use with
     *     care!</dd>
     * </dl>
     * </p>
     *
     * @param kind
     *         Cancellation type
     * @throws SQLException
     *         For errors cancelling, or if the cancel operation is not supported.
     */
    void cancelOperation(int kind) throws SQLException;

    /**
     * Creates and starts a transaction.
     *
     * @param tpb
     *         TransactionParameterBuffer with the required transaction
     *         options
     * @return FbTransaction
     */
    FbTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Creates and starts a transaction using a SQL statement
     *
     * @param statementText
     *         statement which starts a transaction
     * @return FbTransaction
     * @throws SQLException
     *         for database access error
     * @since 6
     */
    FbTransaction startTransaction(String statementText) throws SQLException;

    /**
     * Reconnects a prepared transaction.
     * <p>
     * Reconnecting transactions is only allowed for transactions in limbo (prepared, but not committed or rolled back).
     * </p>
     *
     * @param transactionId
     *         The id of the transaction to reconnect.
     * @return FbTransaction
     * @throws SQLException
     *         For errors reconnecting the transaction
     */
    FbTransaction reconnectTransaction(long transactionId) throws SQLException;

    /**
     * Creates a statement associated with a transaction
     *
     * @param transaction
     *         FbTransaction to associate with this statement (can be {@code null})
     * @return FbStatement
     */
    FbStatement createStatement(FbTransaction transaction) throws SQLException;

    /**
     * Creates a blob for write access to a new blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @param blobParameterBuffer
     *         blob parameter buffer
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     */
    FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) throws SQLException;

    /**
     * Creates a blob for write access to a new blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     * <p>
     * Equivalent to calling {@link #createBlobForOutput(FbTransaction, BlobParameterBuffer)} with {@code null} for
     * {@code blobParameterBuffer}.
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     * @since 5
     */
    default FbBlob createBlobForOutput(FbTransaction transaction) throws SQLException {
        return createBlobForOutput(transaction, (BlobParameterBuffer) null);
    }

    /**
     * Creates a blob for write access to a new blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @param blobConfig
     *         blob config (cannot be {@code null})
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     * @since 5
     */
    default FbBlob createBlobForOutput(FbTransaction transaction, BlobConfig blobConfig) throws SQLException {
        BlobParameterBuffer blobParameterBuffer = createBlobParameterBuffer();
        blobConfig.writeOutputConfig(blobParameterBuffer);
        return createBlobForOutput(transaction, blobParameterBuffer);
    }

    /**
     * Creates a blob for read access to an existing blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     * <p>
     * If the server supports inline blobs, a locally cached blob may be returned if an inline blob was received for
     * {@code transaction} and {@code blobId}, and if {@code blobParameterBuffer} is {@code null} or empty
     * ({@link BlobParameterBuffer#isEmpty()}).
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @param blobParameterBuffer
     *         blob parameter buffer
     * @param blobId
     *         id of the blob
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     */
    FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId)
            throws SQLException;

    /**
     * Creates a blob for read access to an existing blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     * <p>
     * Equivalent to calling {@link #createBlobForInput(FbTransaction, BlobParameterBuffer, long)} with {@code null} for
     * {@code blobParameterBuffer}.
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @param blobId
     *         id of the blob
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     * @since 5
     */
    default FbBlob createBlobForInput(FbTransaction transaction, long blobId) throws SQLException {
        return createBlobForInput(transaction, (BlobParameterBuffer) null, blobId);
    }

    /**
     * Creates a blob for read access to an existing blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     *
     * @param transaction
     *         transaction associated with the blob
     * @param blobConfig
     *         blob config (cannot be {@code null})
     * @param blobId
     *         handle id of the blob
     * @return instance of {@link FbBlob}
     * @throws SQLException
     *         if the database is not attached or the transaction is not active
     * @since 5
     */
    default FbBlob createBlobForInput(FbTransaction transaction, BlobConfig blobConfig, long blobId)
            throws SQLException {
        BlobParameterBuffer blobParameterBuffer = createBlobParameterBuffer();
        blobConfig.writeInputConfig(blobParameterBuffer);
        return createBlobForInput(transaction, blobParameterBuffer, blobId);
    }

    /**
     * Creates a blob parameter buffer that is usable with {@link #createBlobForInput(FbTransaction,
     * org.firebirdsql.gds.BlobParameterBuffer, long)}
     * and {@link #createBlobForOutput(FbTransaction, org.firebirdsql.gds.BlobParameterBuffer)} of this instance.
     *
     * @return A blob parameter buffer.
     */
    BlobParameterBuffer createBlobParameterBuffer();

    /**
     * Creates a transaction parameter buffer that is usable with {@link #startTransaction(org.firebirdsql.gds.TransactionParameterBuffer)}.
     *
     * @return A transaction parameter buffer
     */
    TransactionParameterBuffer createTransactionParameterBuffer();

    /**
     * Request database info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException;

    /**
     * Performs a database info request.
     *
     * @param requestItems
     *         Information items to request
     * @param maxBufferLength
     *         Maximum response buffer length to use
     * @return The response buffer (note: length is the actual length of the response, not {@code maxBufferLength}
     * @throws SQLException
     *         For errors retrieving the information.
     */
    byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException;

    /**
     * Performs an execute immediate of a statement.
     * <p>
     * A call to this method is the equivalent of a {@code isc_dsql_execute_immediate()} without parameters.
     * </p>
     *
     * @param statementText
     *         Statement text
     * @param transaction
     *         Transaction ({@code null} only allowed if database is not attached!)
     * @throws SQLException
     *         For errors executing the statement, or if {@code transaction} is {@code null} when the database is
     *         attached or not {@code null} when the database is not attached
     */
    void executeImmediate(String statementText, FbTransaction transaction) throws SQLException;

    /**
     * @return The database dialect
     */
    short getDatabaseDialect();

    /**
     * @return The client connection dialect
     */
    short getConnectionDialect();

    /**
     * @return The database handle value
     */
    @Override
    int getHandle();

    /**
     * @return ODS major version
     */
    int getOdsMajor();

    /**
     * @return ODS minor version
     */
    int getOdsMinor();

    /**
     * @return ODS version
     * @since 6
     */
    default OdsVersion getOdsVersion() {
        return OdsVersion.of(getOdsMajor(), getOdsMinor());
    }

    /**
     * Adds a {@link DatabaseListener} instance to this database.
     *
     * @param listener
     *         Database listener
     */
    void addDatabaseListener(DatabaseListener listener);

    /**
     * Adds a {@link DatabaseListener} instance to this database using a weak reference.
     * <p>
     * If the listener is already strongly referenced, this call will be ignored
     * </p>
     *
     * @param listener
     *         Database listener
     */
    void addWeakDatabaseListener(DatabaseListener listener);

    /**
     * Removes a {@link DatabaseListener} instance from this database.
     *
     * @param listener
     *         Database Listener
     */
    void removeDatabaseListener(DatabaseListener listener);

    /**
     * Creates an event handle for this database type.
     * <p>
     * The returned event handle can be used with {@link #queueEvent(org.firebirdsql.gds.EventHandle)}.
     * </p>
     *
     * @param eventName
     *         Name of the event
     * @param eventHandler
     *         The event handler to call when the event occurred
     * @return A suitable event handle instance
     * @throws java.sql.SQLException
     *         For errors creating the event handle
     */
    EventHandle createEventHandle(String eventName, EventHandler eventHandler) throws SQLException;

    /**
     * Counts the events occurred.
     *
     * @param eventHandle
     *         The event handle
     * @throws SQLException
     *         When the count can not be done (as - for example - the event handle is of the wrong type)
     */
    void countEvents(EventHandle eventHandle) throws SQLException;

    /**
     * Queues a wait for an event.
     *
     * @param eventHandle
     *         The event handle (created using {@link #createEventHandle(String, EventHandler)} of this instance).
     * @throws SQLException
     *         For errors establishing the asynchronous channel, or for queuing the event.
     */
    void queueEvent(EventHandle eventHandle) throws SQLException;

    /**
     * Cancels a registered event.
     * <p>
     * After cancellation, the event handle should be considered unusable. Before queueing a new event, an new
     * handle needs to be created.
     * </p>
     *
     * @param eventHandle
     *         The event handle to cancel
     * @throws SQLException
     *         For errors cancelling the event
     */
    void cancelEvent(EventHandle eventHandle) throws SQLException;

    /**
     * @return An immutable copy of the connection properties of this database
     */
    IConnectionProperties getConnectionProperties();

    /**
     * @return A potentially cached empty row descriptor for this database.
     */
    RowDescriptor emptyRowDescriptor();
}
