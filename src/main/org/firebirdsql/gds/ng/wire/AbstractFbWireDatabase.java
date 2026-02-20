// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_notAttachedToDatabase;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_notConnectedToServer;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_unableToCancelEventReasonNotConnected;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION18;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_blob;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_cursor;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_database;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_request;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_sql;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_info_transaction;

/**
 * Abstract class for operations common to all version of the wire protocol implementation.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireDatabase extends AbstractFbDatabase<WireDatabaseConnection>
        implements FbWireDatabase {

    protected final ProtocolDescriptor protocolDescriptor;
    protected final FbWireOperations wireOperations;
    private @Nullable FbWireAsynchronousChannel asynchronousChannel;

    /**
     * Creates an AbstractFbWireDatabase instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected AbstractFbWireDatabase(WireDatabaseConnection connection, ProtocolDescriptor descriptor) {
        super(connection, DefaultDatatypeCoder.forEncodingFactory(connection.getEncodingFactory()));
        protocolDescriptor = requireNonNull(descriptor, "parameter descriptor should be non-null");
        wireOperations = descriptor.createWireOperations(connection, getDatabaseWarningCallback());
    }

    @Override
    public final int getHandle() {
        // The handle is always 0 for a TCP/IP database
        return 0;
    }

    @Override
    public void forceClose() throws SQLException {
        try {
            if (connection.isConnected()) {
                connection.close();
            }
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        } finally {
            databaseListenerDispatcher.detached(this);
            databaseListenerDispatcher.shutdown();
            exceptionListenerDispatcher.shutdown();
        }
    }

    /**
     * @see XdrStreamAccess#getXdrIn()
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * @see XdrStreamAccess#withTransmitLock(TransmitAction)
     * @since 7
     */
    protected final void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException {
        getXdrStreamAccess().withTransmitLock(transmitAction);
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    @Override
    public final FbWireOperations getWireOperations() {
        return wireOperations;
    }

    @Override
    public final boolean isAttached() {
        return super.isAttached() && isConnected();
    }

    /**
     * Checks if a physical connection to the server is established.
     *
     * @throws SQLException
     *         if not connected
     */
    @Override
    protected final void checkConnected() throws SQLException {
        if (!isConnected()) {
            throw FbExceptionBuilder.toNonTransientConnectionException(jb_notConnectedToServer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if a physical connection to the server is established
     */
    @Override
    protected final boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Checks if a physical connection to the server is established and if the
     * connection is attached to a database.
     * <p>
     * This method calls {@link #checkConnected()}, so it is not necessary to
     * call both.
     * </p>
     *
     * @throws SQLException
     *         If the database not connected or attached.
     */
    protected final void checkAttached() throws SQLException {
        checkConnected();
        if (!isAttached()) {
            throw FbExceptionBuilder.toNonTransientConnectionException(jb_notAttachedToDatabase);
        }
    }

    /**
     * Closes the WireConnection associated with this connection.
     *
     * @throws IOException
     *         For errors closing the connection.
     */
    protected final void closeConnection() throws IOException {
        if (!connection.isConnected()) return;
        try (LockCloseable ignored = withLock()) {
            try {
                connection.close();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkConnected();
            wireOperations.setNetworkTimeout(milliseconds);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final FbBlob createBlobForOutput(FbTransaction transaction,
            @Nullable BlobParameterBuffer blobParameterBuffer) throws SQLException {
        FbWireBlob outputBlob = protocolDescriptor.createOutputBlob(this, (FbWireTransaction) transaction,
                BlobParameterBuffer.orEmpty(blobParameterBuffer));
        outputBlob.addExceptionListener(exceptionListenerDispatcher);
        return outputBlob;
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, @Nullable BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        FbWireBlob inputBlob = protocolDescriptor.createInputBlob(this, (FbWireTransaction) transaction,
                BlobParameterBuffer.orEmpty(blobParameterBuffer), blobId);
        inputBlob.addExceptionListener(exceptionListenerDispatcher);
        return inputBlob;
    }

    @Override
    public final void consumePackets(int numberOfResponses, @Nullable WarningMessageCallback warningCallback) {
        // TODO Should consumePackets notify the exception listener or not?
        wireOperations.consumePackets(numberOfResponses, warningCallback);
    }

    @Override
    public final GenericResponse readGenericResponse(@Nullable WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return wireOperations.readGenericResponse(warningCallback);
    }

    @Override
    public final SqlResponse readSqlResponse(@Nullable WarningMessageCallback warningCallback) throws SQLException, IOException {
        return wireOperations.readSqlResponse(warningCallback);
    }

    @Override
    public final Response readResponse(@Nullable WarningMessageCallback warningCallback) throws SQLException, IOException {
        return wireOperations.readResponse(warningCallback);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation decorates {@code deferredAction} to ensure this database instance is notified of exceptions.
     * This can result in double notifications if the caller also notifies exceptions and this database instance is
     * registered as an exception listener for those notifications (this is not harmful).
     * </p>
     */
    @Override
    public final void enqueueDeferredAction(DeferredAction deferredAction) throws SQLException {
        wireOperations.enqueueDeferredAction(decorateWithExceptionNotification(deferredAction));
    }

    private DeferredAction decorateWithExceptionNotification(DeferredAction deferredAction) {
        return deferredAction instanceof ExceptionNotifyingDeferredAction
                // Don't decorate again
                ? deferredAction
                : new ExceptionNotifyingDeferredAction(deferredAction);
    }

    private final class ExceptionNotifyingDeferredAction extends DeferredAction.DelegatingDeferredAction {

        ExceptionNotifyingDeferredAction(DeferredAction delegate) {
            super(delegate);
        }

        @Override
        public void onException(Exception exception) {
            try {
                super.onException(exception);
            } finally {
                exceptionListenerDispatcher.errorOccurred(toReadSQLException(exception));
            }
        }

        private static SQLException toReadSQLException(Exception exception) {
            if (exception instanceof SQLException sqle) {
                return sqle;
            } else if (exception instanceof IOException ioe) {
                return FbExceptionBuilder.ioReadError(ioe);
            }
            return new SQLException(exception);
        }

    }

    @Override
    public final void completeDeferredActions() throws SQLException {
        wireOperations.completeDeferredActions();
    }

    @Override
    public final EventHandle createEventHandle(String eventName, EventHandler eventHandler) {
        return new WireEventHandle(eventName, eventHandler, getEncoding());
    }

    @Override
    public final void queueEvent(EventHandle eventHandle) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            FbWireAsynchronousChannel channel = asynchronousChannel;
            if (channel == null || !channel.isConnected()) {
                var asynchronousProcessor = AsynchronousProcessor.getInstance();
                if (channel != null) {
                    asynchronousProcessor.unregisterAsynchronousChannel(channel);
                }
                channel = asynchronousChannel = initAsynchronousChannel();
                asynchronousProcessor.registerAsynchronousChannel(channel);
            }
            channel.queueEvent(eventHandle);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final void cancelEvent(EventHandle eventHandle) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            FbWireAsynchronousChannel channel = asynchronousChannel;
            if (channel == null || !channel.isConnected()) {
                if (channel != null) {
                    AsynchronousProcessor.getInstance().unregisterAsynchronousChannel(channel);
                }
                throw FbExceptionBuilder.toNonTransientException(jb_unableToCancelEventReasonNotConnected);
            }
            channel.cancelEvent(eventHandle);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final void countEvents(EventHandle eventHandle) throws SQLException {
        try {
            if (!(eventHandle instanceof WireEventHandle))
                throw new SQLException("Invalid event handle, type: " + eventHandle.getClass().getName());

            ((WireEventHandle) eventHandle).calculateCount();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        try {
            checkAttached();
            return getInfo(op_info_database, 0, requestItems, maxBufferLength, null);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    @SuppressWarnings("java:S4274")
    public byte[] getInfo(int operation, int handle, byte[] requestItems, int maxBufferLength,
            @Nullable WarningMessageCallback warningMessageCallback) throws SQLException {
        try (var ignored = withLock()) {
            try {
                withTransmitLock(xdrOut -> {
                    sendGetInfoMsg(xdrOut, operation, handle, requestItems, maxBufferLength);
                    xdrOut.flush();
                });
            } catch (IOException e) {
                throw FbExceptionBuilder.ioWriteError(e);
            }
            try {
                final GenericResponse genericResponse = readGenericResponse(null);
                return genericResponse.data();
            } catch (IOException e) {
                throw FbExceptionBuilder.ioReadError(e);
            }
        }
    }

    /**
     * Sends the information request message (struct {@code p_info}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param operation
     *         information request operation code
     * @param handle
     *         object handle (of the right type for {@code operation})
     * @param requestItems
     *         information request items
     * @param maxBufferLength
     *         maximum response buffer length
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendGetInfoMsg(XdrOutputStream xdrOut, int operation, int handle, byte[] requestItems,
            int maxBufferLength) throws IOException {
        assert isValidOperationCode(operation) : "Unsupported operation code for info request " + operation;
        xdrOut.writeInt(operation); // p_operation
        xdrOut.writeInt(handle); // p_info_object
        xdrOut.writeInt(0); // p_info_incarnation
        xdrOut.writeBuffer(requestItems); // p_info_items
        xdrOut.writeInt(maxBufferLength); // p_info_buffer_length
    }

    private boolean isValidOperationCode(int operation) {
        return operation == op_info_sql || operation == op_info_blob || operation == op_info_database
                || operation == op_info_transaction || operation == op_info_request
                || operation == op_info_cursor && protocolDescriptor.getVersion() >= PROTOCOL_VERSION18;
    }

    /**
     * Initializes the asynchronous channel (for event notification).
     *
     * @throws java.sql.SQLException
     *         For errors establishing the channel, or if the channel already exists.
     */
    public abstract FbWireAsynchronousChannel initAsynchronousChannel() throws SQLException;

}
