// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.fields.BlrCalculator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbWireDatabase extends FbDatabase, FbWireAttachment {

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
     * Release object.
     *
     * @param operation
     *         Operation
     * @param objectId
     *         ID of the object to release
     */
    void releaseObject(int operation, int objectId) throws SQLException;

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
     * @return The {@link BlrCalculator} instance for this database.
     */
    BlrCalculator getBlrCalculator();

    /**
     * Enqueue a deferred action.
     * <p>
     * FbDatabase implementations that do not support deferred actions are allowed to throw an
     * {@link java.lang.UnsupportedOperationException}
     * </p>
     *
     * @param deferredAction
     *         Deferred action
     * @throws SQLException
     *         for errors forcing handling of oversized queue using {@code op_ping} (or {@code op_batch_sync})
     */
    void enqueueDeferredAction(DeferredAction deferredAction) throws SQLException;

    /**
     * Completes pending deferred actions.
     * <p>
     * FbDatabase implementations that do not support deferred actions should simply do nothing.
     * </p>
     *
     * @throws SQLException
     *         for errors forcing ping/batch sync
     * @since 6
     */
    void completeDeferredActions() throws SQLException;

    /**
     * Consumes packets notifying for warnings, but ignoring exceptions thrown from the packet.
     * <p>
     * This method should only be used inside the implementation if either packets need to be ignored,
     * or to ensure that there is no backlog of packets (eg when an exception occurs during processing of multiple
     * package responses).
     * </p>
     *
     * @param numberOfResponses Number of responses to consume.
     * @param warningCallback Callback for warnings
     */
    void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback);

    /**
     * Generic info request.
     * <p>
     * The implementation does not perform handle validation nor notification of error dispatchers. Doing that is the
     * responsibility of the caller.
     * </p>
     *
     * @param operation
     *         Operation code
     * @param handle
     *         Handle (db, transaction, statement, blob, etc)
     * @param requestItems
     *         Information items to request
     * @param maxBufferLength
     *         Maximum response buffer length to use
     * @param warningMessageCallback
     *         Callback for warnings when reading the response (can be {@code null} to use database default)
     * @return The response buffer (note: length is the actual length of the response, not {@code maxBufferLength})
     * @throws SQLException
     *         For errors retrieving the information
     */
    byte[] getInfo(int operation, int handle, byte[] requestItems, int maxBufferLength,
            WarningMessageCallback warningMessageCallback) throws SQLException;
    
}
