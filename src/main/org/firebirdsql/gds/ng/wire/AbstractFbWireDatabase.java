/*
 * $Id$
 * 
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.impl.BlobParameterBufferImp;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Abstract class for operations common to all version of the wire protocol implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireDatabase extends AbstractFbDatabase implements FbWireDatabase {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbWireDatabase.class);

    protected final ProtocolDescriptor protocolDescriptor;
    private final DatatypeCoder datatypeCoder;
    protected final WireConnection connection;

    /**
     * Creates an AbstractFbWireDatabase instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected AbstractFbWireDatabase(WireConnection connection, ProtocolDescriptor descriptor) {
        if (connection == null) throw new IllegalArgumentException("parameter connection should be non-null");
        if (descriptor == null) throw new IllegalArgumentException("parameter descriptor should be non-null");
        this.connection = connection;
        protocolDescriptor = descriptor;
        datatypeCoder = new DefaultDatatypeCoder(connection.getEncodingFactory());
    }

    @Override
    public final short getConnectionDialect() {
        return connection.getConnectionDialect();
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return connection.getEncodingFactory();
    }

    @Override
    public final Encoding getEncoding() {
        return connection.getEncoding();
    }

    @Override
    public final DatatypeCoder getDatatypeCoder() {
        return datatypeCoder;
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
    protected abstract void checkAttached() throws SQLException;

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) throws SQLException {
        return protocolDescriptor.createOutputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) throws SQLException {
        return protocolDescriptor.createInputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer, blobId);
    }

    @Override
    public BlobParameterBuffer createBlobParameterBuffer() {
        return new BlobParameterBufferImp();
    }

    @Override
    public TransactionParameterBufferImpl createTransactionParameterBuffer() {
        return new TransactionParameterBufferImpl();
    }

    @Override
    public void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback) {
        while (numberOfResponses > 0) {
            numberOfResponses--;
            try {
                readResponse(warningCallback);
            } catch (Exception e) {
                // TODO Wrap in SQLWarning and register on warning callback?
                // ignoring exceptions
                log.debug("Exception in consumePackets", e);
            }
        }
    }

    /**
     * Processes any deferred actions. Protocol versions that do not support deferred actions should simply do nothing.
     */
    protected abstract void processDeferredActions();

    /**
     * @param response
     *         Response to process
     * @throws java.sql.SQLException
     *         For errors returned from the server.
     */
    public void processResponse(Response response) throws SQLException {
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            SQLException exception = genericResponse.getException();
            if (exception != null && !(exception instanceof SQLWarning)) {
                throw exception;
            }
        }
    }

    /**
     * Checks if the response included a warning and signals that warning to the
     * WarningMessageCallback.
     *
     * @param response
     *         Response to process
     */
    public void processResponseWarnings(final Response response, WarningMessageCallback warningCallback) {
        if (warningCallback == null) {
            warningCallback = getDatabaseWarningCallback();
        }
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            SQLException exception = genericResponse.getException();
            if (exception != null && exception instanceof SQLWarning) {
                warningCallback.processWarning((SQLWarning) exception);
            }
        }
    }

    /**
     * Reads the next operation. Forwards call to {@link WireConnection#readNextOperation()}.
     *
     * @return next operation
     * @throws java.io.IOException
     */
    public int readNextOperation() throws IOException {
        synchronized (getSynchronizationObject()) {
            processDeferredActions();
            return connection.readNextOperation();
        }
    }

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
     * @see org.firebirdsql.gds.ng.wire.WireConnection#writeDirect(byte[])
     */
    protected final void writeDirect(byte[] data) throws IOException {
        connection.writeDirect(data);
    }

    public EventHandle createEventHandle(String eventName, EventHandler eventHandler) {
        return new WireEventHandle(eventName, eventHandler, getEncoding());
    }

    public void countEvents(EventHandle eventHandle) throws SQLException {
        if (!(eventHandle instanceof WireEventHandle))
            throw new SQLException("Invalid event handle, type: " + eventHandle.getClass().getName());

        ((WireEventHandle) eventHandle).calculateCount();
    }

    /**
     * Initializes the asynchronous channel (for event notification).
     *
     * @throws java.sql.SQLException
     *         For errors establishing the channel, or if the channel already exists.
     */
    // TODO make protected?
    public abstract FbWireAsynchronousChannel initAsynchronousChannel() throws SQLException;
}
