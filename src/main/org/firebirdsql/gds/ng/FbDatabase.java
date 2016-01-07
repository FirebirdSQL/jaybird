/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.TransactionParameterBuffer;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface FbDatabase extends FbAttachment {

    /**
     * Creates a new database, connection remains attached to database.
     *
     * @throws SQLException
     */
    void createDatabase() throws SQLException;

    /**
     * Drops (and deletes) the currently attached database.
     *
     * @throws SQLException
     */
    void dropDatabase() throws SQLException;

    /**
     * Cancels the current operation.
     *
     * @param kind
     *         TODO Document parameter kind of cancelOperation
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
     * @throws SQLException
     */
    FbTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException;

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
     *         FbTransaction to associate with this statement (can be <code>null</code>).
     * @return FbStatement
     * @throws SQLException
     */
    FbStatement createStatement(FbTransaction transaction) throws SQLException;

    /**
     * Creates a blob for write access to a new blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     *
     * @param transaction
     *         Transaction associated with the blob.
     * @param blobParameterBuffer
     *         Blob Parameter Buffer
     * @return Instance of {@link FbBlob}
     */
    FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer);

    /**
     * Creates a blob for read access to an existing blob on the server.
     * <p>
     * The blob is initially closed.
     * </p>
     *
     * @param transaction
     *         Transaction associated with the blob.
     * @param blobParameterBuffer
     *         Blob Parameter Buffer
     * @param blobId
     *         Handle id of the blob
     * @return Instance of {@link FbBlob}
     */
    FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId);

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
     * @return The response buffer (note: length is the actual length of the
     * response, not <code>maxBufferLength</code>
     * @throws SQLException
     *         For errors retrieving the information.
     */
    byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException;

    /**
     * Performs an execute immediate of a statement.
     * <p>
     * A call to this method is the equivalent of a <code>isc_dsql_execute_immediate()</code> without parameters.
     * </p>
     *
     * @param statementText
     *         Statement text
     * @param transaction
     *         Transaction (<code>null</code> only allowed if database is not attached!)
     * @throws SQLException
     *         For errors executing the statement, or if <code>transaction</code> is <code>null</code> when the
     *         database is attached or not <code>null</code> when the database is not attached
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
     * Adds a {@link DatabaseListener} instance to this database.
     *
     * @param listener
     *         Database listener
     */
    void addDatabaseListener(DatabaseListener listener);

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
}
