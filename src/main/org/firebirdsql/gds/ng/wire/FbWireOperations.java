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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Common connection operations shared by database and service handles
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbWireOperations {

    /**
     * @return Instance of {@link XdrStreamAccess} for this service.
     */
    XdrStreamAccess getXdrStreamAccess();

    /**
     * Process the status vector and returns the associated {@link SQLException}
     * instance.
     * <p>
     * NOTE: This method <b>returns</b> the SQLException read from the
     * status vector, and only <b>throws</b> SQLException when an error occurs
     * processing the status vector.
     * </p>
     *
     * @return SQLException from the status vector
     * @throws SQLException
     *         for errors reading or processing the status vector
     */
    SQLException readStatusVector() throws SQLException;

    /**
     * Reads the response from the server.
     *
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return {@link Response} read.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to read
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    Response readResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Reads the next operation code, after processing deferred packets.
     * <p>
     * In general, calling {@link #readResponse(WarningMessageCallback)} or one of the specific {@code readXXXResponse}
     * methods should be preferred to read the response code and the response body. Use this method only for reading
     * custom responses, or if you need to process the response in a way that is not possible with
     * {@link #readResponse(WarningMessageCallback)}.
     * </p>
     *
     * @return next operation
     * @throws java.io.IOException
     *         for errors reading the operation from the connection
     * @since 6
     */
    int readNextOperation() throws IOException;

    /**
     * Reads the response from the server when the operation code has already been read.
     *
     * @param operationCode
     *         The operation code
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return {@link Response} read.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to read
     * @throws IOException
     *         For errors reading the response from the connection.
     * @see #readResponse(WarningMessageCallback)
     */
    Response readOperationResponse(int operationCode, WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Convenience method to read a Response to a GenericResponse
     *
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return GenericResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    GenericResponse readGenericResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Convenience method to read a Response to a SqlResponse
     *
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return SqlResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    SqlResponse readSqlResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Handles the database encryption key callback.
     *
     * @param dbCryptCallback
     *         Database encryption callback plugin
     * @throws IOException
     *         For errors reading data from the socket
     * @throws SQLException
     *         For database errors
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If this protocol version does not support crypt key callbacks
     * @since 4.0
     */
    void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException;

    /**
     * Enqueue a deferred action.
     * <p>
     * FbDatabase implementations that do not support deferred actions are allowed to throw an
     * {@link java.lang.UnsupportedOperationException} (which the default implementation does).
     * </p>
     *
     * @param deferredAction
     *         Deferred action
     * @throws SQLException
     *         for errors forcing handling of oversized queue using {@code op_ping} (or {@code op_batch_sync})
     */
    default void enqueueDeferredAction(DeferredAction deferredAction) throws SQLException {
        throw new UnsupportedOperationException("enqueueDeferredAction is not supported in " + getClass().getName());
    }

    /**
     * Completes pending deferred actions.
     * <p>
     * Wire protocol implementations that do not support deferred actions should simply do nothing.
     * </p>
     *
     * @throws SQLException
     *         for errors forcing ping/batch sync
     * @since 6
     */
    default void completeDeferredActions() throws SQLException {
        // do nothing
    }

    /**
     * Consumes packets notifying for warnings, but ignoring exceptions thrown from the packet.
     * <p>
     * This method should only be used inside the implementation if either packets need to be ignored,
     * or to ensure that there is no backlog of packets (eg when an exception occurs during processing of multiple
     * package responses).
     * </p>
     *
     * @param numberOfResponses
     *         Number of responses to consume.
     * @param warningCallback
     *         Callback for warnings
     */
    void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback);

    /**
     * Processes any deferred actions. Protocol versions that do not support deferred actions should simply do nothing.
     * <p>
     * WARNING: If the server queues deferred responses, and expects an operation (e.g. {@code op_batch_sync},
     * {@code op_batch_exec} or {@code op_ping}) to actual send those responses, this method may block indefinitely.
     * </p>
     */
    default void processDeferredActions() { }

    /**
     * @param response
     *         Response to process
     * @throws java.sql.SQLException
     *         For errors returned from the server.
     */
    void processResponse(Response response) throws SQLException;

    /**
     * Checks if the response included a warning and signals that warning to the
     * WarningMessageCallback.
     *
     * @param response
     *         Response to process
     */
    void processResponseWarnings(Response response, WarningMessageCallback warningCallback);

    /**
     * Writes directly to the {@code OutputStream} of the underlying connection.
     * <p>
     * Use of this method might lead to hard to find race conditions in the protocol. It is currently only used
     * to allow {@link org.firebirdsql.gds.ng.FbDatabase#cancelOperation(int)} to work.
     * </p>
     *
     * @param data
     *         Data to write
     * @throws IOException
     *         If there is no socket, the socket is closed, or for errors writing to the socket.
     * @see WireConnection#writeDirect(byte[])
     */
    void writeDirect(byte[] data) throws IOException;

    /**
     * Receive authentication response from the server.
     * <p>
     * This method is only relevant for protocol V13 or higher.
     * </p>
     *
     * @param acceptPacket
     *         Packet with {@code op_cond_accept} data, or {@code null} when the data should be read from the
     *         connection.
     * @param dbCryptCallback
     *         Database encryption callback (ignored by protocols v12 and lower)
     * @param processAttachCallback
     *         Callback for processing the final attach response
     * @throws IOException
     *         For errors reading the response from the connection.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     */
    void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket,
            DbCryptCallback dbCryptCallback,
            ProcessAttachCallback processAttachCallback)
            throws IOException, SQLException;

    /**
     * Sets the network timeout for this attachment.
     *
     * @param milliseconds
     *         Timeout in milliseconds; 0 means no timeout. If the attachment doesn't support milliseconds, it should
     *         round up to the nearest second.
     * @throws SQLException
     *         If this attachment is closed, the value of {@code milliseconds} is smaller than 0, or if setting the
     *         timeout fails.
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If this attachment doesn't support changing the network timeout.
     */
    void setNetworkTimeout(int milliseconds) throws SQLException;

    interface ProcessAttachCallback {
        void processAttachResponse(GenericResponse response);
    }
}
