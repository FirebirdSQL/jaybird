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

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;

import java.io.IOException;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireDatabase extends AbstractFbDatabase<WireDatabaseConnection>
        implements FbWireDatabase {

    protected final ProtocolDescriptor protocolDescriptor;
    protected final FbWireOperations wireOperations;
    private FbWireAsynchronousChannel asynchronousChannel;

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
        wireOperations = descriptor.createWireOperations(connection, getDatabaseWarningCallback(),
                getSynchronizationObject());
    }

    @Override
    public void forceClose() throws SQLException {
        try {
            if (connection.isConnected()) {
                connection.close();
            }
        } catch (IOException e) {
            throw new FbExceptionBuilder()
                    .exception(ISCConstants.isc_net_write_err)
                    .cause(e)
                    .toFlatSQLException();
        } finally {
            databaseListenerDispatcher.detached(this);
            databaseListenerDispatcher.shutdown();
            exceptionListenerDispatcher.shutdown();
        }
    }

    /**
     * Gets the XdrInputStream.
     *
     * @return Instance of XdrInputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the InputStream
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * Gets the XdrOutputStream.
     *
     * @return Instance of XdrOutputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the OutputStream
     */
    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getXdrStreamAccess().getXdrOut();
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    @Override
    public final boolean isAttached() {
        return super.isAttached() && connection.isConnected();
    }

    /**
     * Checks if a physical connection to the server is established.
     *
     * @throws SQLException
     *         If not connected.
     */
    @Override
    protected final void checkConnected() throws SQLException {
        if (!connection.isConnected()) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notConnectedToServer)
                    .toFlatSQLException();
        }
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
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notAttachedToDatabase)
                    .toFlatSQLException();
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
        synchronized (getSynchronizationObject()) {
            try {
                connection.close();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                checkConnected();
                wireOperations.setNetworkTimeout(milliseconds);
            } catch (SQLException e) {
                exceptionListenerDispatcher.errorOccurred(e);
                throw e;
            }
        }
    }

    @Override
    public final FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        final FbWireBlob outputBlob =
                protocolDescriptor.createOutputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer);
        outputBlob.addExceptionListener(exceptionListenerDispatcher);
        return outputBlob;
    }

    @Override
    public final FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) {
        final FbWireBlob inputBlob =
                protocolDescriptor.createInputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer, blobId);
        inputBlob.addExceptionListener(exceptionListenerDispatcher);
        return inputBlob;
    }

    @Override
    public final void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback) {
        // TODO Should consumePackets notify the exception listener or not?
        wireOperations.consumePackets(numberOfResponses, warningCallback);
    }

    @Override
    public final GenericResponse readGenericResponse(WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return wireOperations.readGenericResponse(warningCallback);
    }

    @Override
    public final SqlResponse readSqlResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        return wireOperations.readSqlResponse(warningCallback);
    }

    @Override
    public final Response readResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        return wireOperations.readResponse(warningCallback);
    }

    @Override
    public final void enqueueDeferredAction(DeferredAction deferredAction) {
        wireOperations.enqueueDeferredAction(deferredAction);
    }

    @Override
    public final EventHandle createEventHandle(String eventName, EventHandler eventHandler) {
        return new WireEventHandle(eventName, eventHandler, getEncoding());
    }

    @Override
    public final void queueEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                if (asynchronousChannel == null || !asynchronousChannel.isConnected()) {
                    asynchronousChannel = initAsynchronousChannel();
                    AsynchronousProcessor.getInstance().registerAsynchronousChannel(asynchronousChannel);
                }
                asynchronousChannel.queueEvent(eventHandle);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final void cancelEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                if (asynchronousChannel == null || !asynchronousChannel.isConnected()) {
                    throw new FbExceptionBuilder()
                            .nonTransientException(JaybirdErrorCodes.jb_unableToCancelEventReasonNotConnected)
                            .toFlatSQLException();
                }
                asynchronousChannel.cancelEvent(eventHandle);
            }
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
            return getInfo(op_info_database, getHandle(), requestItems, maxBufferLength, null);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getInfo(int operation, int handle, byte[] requestItems, int maxBufferLength,
            WarningMessageCallback warningMessageCallback) throws SQLException {
        assert operation == op_info_sql || operation == op_info_blob || operation == op_info_database
                || operation == op_info_transaction || operation == op_info_request
                || operation == op_info_cursor && protocolDescriptor.getVersion() >= PROTOCOL_VERSION18
                : "Unsupported operation code for info request " + operation;
        synchronized (getSynchronizationObject()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(operation);
                xdrOut.writeInt(handle);
                xdrOut.writeInt(0); // incarnation
                xdrOut.writeBuffer(requestItems);
                xdrOut.writeInt(maxBufferLength);
                xdrOut.flush();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                final GenericResponse genericResponse = readGenericResponse(null);
                return genericResponse.getData();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
    }

    /**
     * Initializes the asynchronous channel (for event notification).
     *
     * @throws java.sql.SQLException
     *         For errors establishing the channel, or if the channel already exists.
     */
    public abstract FbWireAsynchronousChannel initAsynchronousChannel() throws SQLException;

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!connection.isConnected()) return;
            if (isAttached()) {
                safelyDetach();
            } else {
                closeConnection();
            }
        } finally {
            super.finalize();
        }
    }
}
